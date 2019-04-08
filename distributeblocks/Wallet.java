package distributeblocks;

import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import distributeblocks.crypto.*;
import distributeblocks.io.WalletManager;


/*
 * Wallet keeps track of all TransactionOut
 * objects which resulted from transactions
 * to its owner.
 *
 * The Wallet is able to receive funds from Transactions,
 * create new Transactions, and check the total funds
 * available.
 */
public class Wallet {
	// Coin base keys are used for signing block reward transactions from a static source
	private static final String COIN_BASE_ID = "COIN_BASE";
	private static final String COIN_BASE_DIR = "/coinBase";
	private static final KeyPair COIN_BASE_KEYS = WalletManager.loadKeyPair(COIN_BASE_DIR, Crypto.GEN_ALGORITHM);
	private static final float BLOCK_REWARD_AMOUNT = 5.0f;

	private PrivateKey privateKey;
	private PublicKey publicKey;
	private HashMap<String, TransactionOut> funds_HashMap = new HashMap<String,TransactionOut>(); 	// Funds in this wallet.
	private HashMap<String, TransactionOut> onHold_HashMap = new HashMap<String,TransactionOut>(); 	// Spent funds waiting to be removed

	/*
	 * Constructor to create a new empty wallet
	 */
	public Wallet(){
	  KeyPair pair = Crypto.keyPairGenerator();
	  privateKey = pair.getPrivate();
	  publicKey = pair.getPublic();
	}

	/*
	 * Constructor to reload a wallet
	 */
	public Wallet(KeyPair keys,
					HashMap<String,TransactionOut> funds_HashMap,
					HashMap<String,TransactionOut> onHold_HashMap) {
		this.privateKey = keys.getPrivate();
		this.publicKey = keys.getPublic();

		if (funds_HashMap != null)
			this.funds_HashMap = funds_HashMap;

		if (onHold_HashMap != null)
			this.onHold_HashMap = onHold_HashMap;
	}

	/*
	 * Returns the total number of funds this wallet has available.
	 */
	public float availableFunds(){
		float sum = 0;
		for (Map.Entry<String,TransactionOut> i: funds_HashMap.entrySet()){
			TransactionOut funds = i.getValue();
			sum += funds.getExchange();
		}
		return sum;
	}

	/*
	 * Returns the total number of funds on hold in this wallet
	 */
	public float fundsOnHold() {
		float sum = 0;
		for (Map.Entry<String,TransactionOut> i: onHold_HashMap.entrySet()){
			TransactionOut funds = i.getValue();
			sum += funds.getExchange();
		}
		return sum;
	}

	/*
	 * Checks over each transaction in verifiedTransactions and adds any matching
	 * this wallet's public key to its own funds.
	 * 
	 * This method is called whenever a block becomes 6 deep from the head of the chain,
	 * and all transactions on the block are considered validated.
	 */
	// TODO: call this method when a block becomes 6 deep from the head of the chain
	public void receiveFunds(HashMap<String, TransactionOut> verifiedTransactions) {
		for (Map.Entry<String,TransactionOut> i: verifiedTransactions.entrySet()){
			TransactionOut funds = i.getValue();

			//check to see if the funds have this publicKey as owner
			if(funds.isMine(publicKey)){
				//if yes, account for them as part of the funds
				funds_HashMap.put(funds.getId(), funds);
			}
		}
	}

	/*
	 * Checks over each transaction in verifiedTransaction and removed any matching
	 * transactions which were on hold in this wallet. This essentially marks the
	 * funds as permanently spent by removing them from the wallet completely.
	 * 
	 * This method is called whenever a block becomes 6 deep from the head of the chain,
	 * and all transactions on the block are considered validated.
	 */
	// TODO: call this method when a block becomes 6 deep from the head of the chain
	public void clearFundsOnHold(HashMap<String, TransactionOut> verifiedTransactions) {
		for (Map.Entry<String,TransactionOut> i: verifiedTransactions.entrySet()){
			TransactionOut spent = onHold_HashMap.get(i.getKey());
			if (spent != null) {
				onHold_HashMap.remove(spent.getId());
			}
		}
	}

	/*
	 * Returns all transaction which was spent and on hold back into
	 * the HashMap of available funds. This method may be called when
	 * a transaction is disregarded by the network, and the user would
	 * like to attempt to use the funds for a new transaction instead.
	 * 
	 * This method is called when the user gives up on previous transactions
	 * and wishes to re-spend the funds they had tried to spend previously.
	 */
	public void rescindHeldFunds() {
		for (Map.Entry<String,TransactionOut> i: onHold_HashMap.entrySet()){
			rescindHeldFund(i.getKey());
		}
	}
	
	/*
	 * Returns a transaction which was spent and on hold back into
	 * the HashMap of available funds. This method may be called when
	 * a transaction is disregarded by the network, and the user would
	 * like to attempt to use the funds for a new transaction instead.
	 */
	public void rescindHeldFund(String transactionOutId) {
		TransactionOut rescinded = onHold_HashMap.get(transactionOutId);
		if (rescinded != null) {
			onHold_HashMap.remove(transactionOutId);
			funds_HashMap.put(rescinded.getId(), rescinded);
		}
	}

	/*
	 * Makes a new transaction from this wallet to send money.
	 * This will create a new transaction utilizing the funds available.
	 * Any overage will be sent back to this wallet.
	 *
	 * Available funds which were used to create this transaction will be
	 * put 'on hold' until they are either verified and cleared, or rescinded
	 * to the wallet.
	 */
	public Transaction makeTransaction(PublicKey receiver, float amount){
		if(availableFunds() < amount){
			//check funds to see if a transaction is possible
			System.out.println("Insuficient funds to generate transaction.");
			return null;
		}
		// Create a list of the inputs needed to fulfill this transaction
		ArrayList<TransactionIn> transaction_ArrayList = new ArrayList<TransactionIn>();
		float sum = 0;
		for (Map.Entry<String, TransactionOut> item: funds_HashMap.entrySet()){
			// Add funds to the transaction_ArrayList
			TransactionOut funds = item.getValue();
			sum += funds.getExchange();
			transaction_ArrayList.add(new TransactionIn(funds.getId(), funds.getExchange()));

			// Until the requested amount is exceeded
			if(sum > amount) break;
		}

		/* Create the transaction, without enforcing it, the transaction enforcer checks that the trasnaction is
		 * valid, with enough inputs to make the exchange.
		 * The transaction should be enforced when it is called
		 * elsewhere.
		 */
		Transaction newTransaction = new Transaction(privateKey, publicKey, receiver, amount, transaction_ArrayList);

		// put the funds used to create this transaction on hold
		for(TransactionIn i: transaction_ArrayList){
			TransactionOut spent = funds_HashMap.get(i.getSourceId());
			funds_HashMap.remove(i.getSourceId());
			onHold_HashMap.put(spent.getId(), spent);
		}
		return newTransaction;
	}
	
	/*
	 * Takes all of the TransactionOut objects used to make a Transaction out
	 * of the onHold_hashMap and puts them back into the funds_HashMap. 
	 * 
	 * This method is called when a transaction is created but ultimately fails, 
	 * and should be reversed.
	 */
	public void reverseTransaction(Transaction failedTransaction) {
		// put the funds used to create the transaction back into the available funds
		for(TransactionIn i: failedTransaction.getInput()){
			rescindHeldFund(i.getSourceId());
		}
	}

	/*
	 * Makes a new transaction from the COIN_BASE.
	 * The block reward transaction can go to any PublicKey, but
	 * is usually given to the creator of the block (calling node).
	 */
	public static Transaction makeBlockReward(PublicKey receiver) {
		/* Create a TransactionIn array from the COIN_BASE to be consumed by the block
		 * reward transaction. This will remain empty.
		 */
		ArrayList<TransactionIn> transaction_ArrayList = new ArrayList<TransactionIn>();
		transaction_ArrayList.add(new TransactionIn(COIN_BASE_ID, BLOCK_REWARD_AMOUNT));

		// Create a block reward Transaction, gives coins to the receiver
		Transaction newTransaction = new Transaction(COIN_BASE_KEYS.getPrivate(),
				COIN_BASE_KEYS.getPublic(),
				receiver,
				BLOCK_REWARD_AMOUNT,
				transaction_ArrayList);
		return newTransaction;
	}

	/*
	 * Returns the private key of this Wallet
	 */
	public PrivateKey getPrivateKey(){
		return privateKey;
	}

	/*
	 * Returns the public key of this Wallet
	 */
	public PublicKey getPublicKey(){
		return publicKey;
	}

	/*
	 * Returns the funds hashmap of this wallet
	 */
	public HashMap<String, TransactionOut> getFundsHashMap() {
		return funds_HashMap;
	}

	/*
	 * Returns the onhold hashmap of this wallet
	 */
	public HashMap<String, TransactionOut> getOnHoldHashMap() {
		return onHold_HashMap;
	}

}
