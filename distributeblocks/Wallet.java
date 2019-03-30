package distributeblocks;

import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import distributeblocks.crypto.*;


/*
 * Wallet keeps track of all TransactionOut
 * objects which resulted from transactions
 * to its owner.
 * 
 * The Wallet is able to receive funds from Transactions,
 * create new Transactions, and check the total funds
 * available.
 * 
 */
public class Wallet {

	private PrivateKey privateKey;
	private PublicKey publicKey;
	private HashMap<String, TransactionOut> funds_HashMap = new HashMap<String,TransactionOut>(); //funds in this wallet.

	public Wallet(){
	  KeyPair pair = Crypto.keyPairGenerator();
	  privateKey = pair.getPrivate();
	  publicKey = pair.getPublic();
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
	 * Checks over each transaction in incomingTransactions and adds any matching
	 * this wallet's public key to its own funds.
	 */
	public void receiveFunds(HashMap<String, TransactionOut> incomingTransactions) {
		for (Map.Entry<String,TransactionOut> i: incomingTransactions.entrySet()){
			TransactionOut funds = i.getValue();

			//check to see if the funds have this publicKey as owner
			if(funds.isMine(publicKey)){
				//if yes, account for them as part of the funds
				funds_HashMap.put(funds.getId(), funds);
			}
		}
	}

	/*
	 * Makes a new transaction from this wallet to send money.
	 * This will create a new transaction utilizing the funds available.
	 * Any overage will be sent back to this wallet.
	 */
	// TODO: allow for a transaction fee
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
		Transaction newTransaction = new Transaction(privateKey, publicKey, receiver, amount, transaction_ArrayList);

		// remove the funds used to create this transaction
		/*
		 * TODO: THIS IS DANGEROUS!
		 * What if the transaction never goes through?
		 * These transactions shouldn't be removed, but should be
		 * placed on hold until it is verified that they are used.
		 * 
		 * Only after they are verified should they be removed.
		 */
		for(TransactionIn i: transaction_ArrayList){
			funds_HashMap.remove(i.getSourceId());
		}
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
}
