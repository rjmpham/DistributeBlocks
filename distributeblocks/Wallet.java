package distributeblocks;

import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import distributeblocks.crypto.*;
import distributeblocks.io.Console;

/**
 * Wallet keeps track of all TransactionOut
 * objects which resulted from transactions
 * to its owner.
 *
 * The Wallet is able to receive funds from Transactions,
 * create new Transactions, and check the total funds
 * available.
 */
public class Wallet {
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private HashMap<String, TransactionOut> funds_HashMap = new HashMap<String,TransactionOut>(); 	// Funds in this wallet.
	private HashMap<String, TransactionOut> onHold_HashMap = new HashMap<String,TransactionOut>(); 	// Spent funds waiting to be removed

	/**
	 * Constructor to create a new empty wallet
	 */
	public Wallet(){
	  KeyPair pair = Crypto.keyPairGenerator();
	  privateKey = pair.getPrivate();
	  publicKey = pair.getPublic();
	}

	/**
	 * Constructor to reload a wallet
	 * 
	 * @param keys				KeyPair of the wallet
	 * @param funds_HashMap		funds the wallet has
	 * @param onHold_HashMap	funds the wallet has on hold
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
	
	/**
	 * This method updates the state of the wallet from a verified
	 * transaction. Every TransactionOut will be checked, and:
	 * 		- if it was an output that was on hold in this wallet,
	 * 			it will be cleared out (verified as spent)
	 * 
	 * 		- if it belongs to the PK of this wallet, it will be added
	 * 			as available funds (verified as received)
	 * 
	 * This method is called whenever a block becomes verified (sufficiently deep).
	 * 
	 * @param transaction	The Transaction to process
	 */
	public void update(Transaction transaction) {
		// Construct a set of TransactionOut ids which were used as inputs
		HashSet<String> inputs = new HashSet<String>();
		for (TransactionIn i: transaction.getInput()) {
			inputs.add(i.getSourceId());
		}

		// Clear any held funds that were waiting for verification
		clearFundsOnHold(inputs);
		// Clear any funds spent, but rescinded (turns out they WERE spent)
		clearFundsRescinded(inputs);
		
		// Construct a map from ids to TransactionOut
		HashMap<String, TransactionOut> outputs = new HashMap<String, TransactionOut>();
		for (TransactionOut o: transaction.getOutput()) {
			outputs.put(o.getId(), o);
		}
		// Add any newly received funds
		receiveFunds(outputs);
	}

	/**
	 * Returns the total number of funds this wallet has available.
	 * 
	 * @return total funds available
	 */
	public float availableFunds(){
		float sum = 0;
		for (Map.Entry<String,TransactionOut> i: funds_HashMap.entrySet()){
			TransactionOut funds = i.getValue();
			sum += funds.getExchange();
		}
		return sum;
	}

	/**
	 * Returns the total number of funds on hold in this wallet
	 * 
	 * @return total funds on hold
	 */
	public float fundsOnHold() {
		float sum = 0;
		for (Map.Entry<String,TransactionOut> i: onHold_HashMap.entrySet()){
			TransactionOut funds = i.getValue();
			sum += funds.getExchange();
		}
		return sum;
	}

	/**
	 * Checks over each transaction in verifiedTransactions and adds any matching
	 * this wallet's public key to its own funds.
	 * 
	 * This method is called whenever a block becomes verified (sufficiently deep).
	 * 
	 * @param verifiedTransactions	HashMap from TransactionOut ids to TransactionOut to process
	 */
	public void receiveFunds(HashMap<String, TransactionOut> verifiedTransactions) {
		for (Map.Entry<String,TransactionOut> i: verifiedTransactions.entrySet()){
			TransactionOut funds = i.getValue();

			//check to see if the funds have this publicKey as owner
			if(funds.isMine(publicKey)){
				//if yes, account for them as part of the funds
				System.out.println("Received " + funds.getExchange() + " coins");
				funds_HashMap.put(funds.getId(), funds);
			}
		}
	}

	/**
	 * Checks over each TransactionOut id in verifiedTransactions and removed any matching
	 * transactions which were on hold in this wallet. This essentially marks the
	 * funds as permanently spent by removing them from the wallet completely.
	 * 
	 * This method is called whenever a block becomes verified (sufficiently deep).
	 * 
	 * @param verifiedTransactions	HashSet of TransactionOut ids to process
	 */
	public void clearFundsOnHold(HashSet<String> verifiedTransactions) {
		for (String id: verifiedTransactions) {
			TransactionOut spent = onHold_HashMap.get(id);
			if (spent != null) {
				onHold_HashMap.remove(spent.getId());
			}
		}
	}
	
	/**
	 * Checks over each TransactionOut id in verifiedTransactions and removed any matching
	 * transactions which were erroneously rescinded. This essentially marks the
	 * funds as permanently spent by removing them from the wallet completely.
	 * 
	 * This method is called whenever a block becomes verified (sufficiently deep).
	 * 
	 * @param verifiedTransactions	HashSet of TransactionOut ids to process
	 */
	public void clearFundsRescinded(HashSet<String> verifiedTransactions) {
		for (String id: verifiedTransactions) {
			TransactionOut spent = funds_HashMap.get(id);
			if (spent != null) {
				funds_HashMap.remove(spent.getId());
			}
		}
	}

	/**
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
	
	/**
	 * Returns a transaction which was spent and on hold back into
	 * the HashMap of available funds. This method may be called when
	 * a transaction is disregarded by the network, and the user would
	 * like to attempt to use the funds for a new transaction instead.
	 * 
	 * @param transactionOutId		id of specific transaction to rescind
	 */
	public void rescindHeldFund(String transactionOutId) {
		TransactionOut rescinded = onHold_HashMap.get(transactionOutId);
		if (rescinded != null) {
			onHold_HashMap.remove(transactionOutId);
			funds_HashMap.put(rescinded.getId(), rescinded);
		}
	}

	/**
	 * Makes a new transaction from this wallet to send money.
	 * This will create a new transaction utilizing the funds available.
	 * Any overage will be sent back to this wallet.
	 *
	 * Available funds which were used to create this transaction will be
	 * put 'on hold' until they are either verified and cleared, or rescinded
	 * to the wallet.
	 * 
	 * @param receiver		PublicKey of the receiver
	 * @param amount		how much is being sent
	 * 
	 * @return a newly created Transaction
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
			if(sum >= amount) break;
		}

		/* Create the transaction, without enforcing it, the transaction enforcer checks that the tramsaction is
		 * valid, with enough inputs to make the exchange.
		 * The transaction should be enforced when it is called
		 * elsewhere.
		 */
		Transaction newTransaction = new Transaction(privateKey, publicKey, receiver, amount, transaction_ArrayList);

		// put the funds used to create this transaction on hold
		for(TransactionIn i: transaction_ArrayList){
			i.setParentId(newTransaction.getTransactionId());
			TransactionOut spent = funds_HashMap.get(i.getSourceId());
			funds_HashMap.remove(i.getSourceId());
			onHold_HashMap.put(spent.getId(), spent);
		}
		return newTransaction;
	}
	
	/**
	 * Takes all of the TransactionOut objects used to make a Transaction out
	 * of the onHold_hashMap and puts them back into the funds_HashMap. 
	 * 
	 * This method is called when a transaction is created but ultimately fails, 
	 * and should be reversed.
	 * 
	 * @param failedTransaction		Transaction to reverse
	 */
	public void reverseTransaction(Transaction failedTransaction) {
		// put the funds used to create the transaction back into the available funds
		for(TransactionIn i: failedTransaction.getInput()){
			rescindHeldFund(i.getSourceId());
		}
	}

	// Getter methods
	public PrivateKey getPrivateKey(){ return privateKey; }
	public PublicKey getPublicKey(){ return publicKey; }
	public HashMap<String, TransactionOut> getFundsHashMap() { return funds_HashMap; }
	public HashMap<String, TransactionOut> getOnHoldHashMap() { return onHold_HashMap; }
}
