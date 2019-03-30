package distributeblocks;

import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import distributeblocks.crypto.*;

public class Wallet {

	public PrivateKey privateKey;
	public PublicKey publicKey;
	public HashMap<String, TransactionOut> funds_HashMap = new HashMap<String,TransactionOut>(); //funds in this wallet.

	//Getter methods
	public PrivateKey getPrivateKey(){return privateKey;}
	public PublicKey getPublicKey(){return publicKey;}

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
	 * Checks over each transaction in incomingFunds and adds any matching
	 * this wallet's public key to its own funds.
	 */
	public void receiveFunds(HashMap<String, TransactionOut> incomingFunds) {
		for (Map.Entry<String,TransactionOut> i: incomingFunds.entrySet()){
			TransactionOut funds = i.getValue();

			//check to see if the funds have this publicKey as owner
			if(funds.isMine(publicKey)){
				//if yes, account for them as part of the funds
				funds_HashMap.put(funds.id,funds);
			}
		}
	}

	/*
	 * Makes a new transaction from this wallet to send money.
	 * This will create a new transaction utilizing the funds available.
	 * Any overage will be sent back to this wallet.
	 */
	public Transaction makeTransaction(PublicKey receiver, float amount){
		if(availableFunds() < amount){
			//check funds to see if a transaction is possible
			System.out.println("Insuficient funds to generate transaction.");
			return null;
		}
		// To access transaction inputs
		ArrayList<TransactionIn> transaction_ArrayList = new ArrayList<TransactionIn>();
		float sum = 0;
		for (Map.Entry<String, TransactionOut> item: funds_HashMap.entrySet()){
			// Add funds to the transaction_ArrayList
			TransactionOut funds = item.getValue();
			sum += funds.getExchange();
			transaction_ArrayList.add(new TransactionIn(funds.id));
			
			// Until the requested amount is exceeded
			/*
			 * TODO: WHAT ABOUT OVERAGE? 
			 * if the sum is greater than the amount,
			 * I want the difference back!
			 * 
			 * We need a new transaction back to this wallet
			 * with the same TransactionOut id, splitting up
			 * the last value between the two parties.
			 */
			if(sum > amount) break;
		}
		Transaction newTransaction = new Transaction(privateKey, publicKey, receiver, amount, transaction_ArrayList);

		//signatures not implemented
		//newTransaction.generateSignature(privateKey); no longer necessary as transactions are singed in constructor now, delete once confidence is gained

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
			funds_HashMap.remove(i.id_Transaction_Out);
		}
		return newTransaction;
	}

}
