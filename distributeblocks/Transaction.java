package distributeblocks;

import java.io.Serializable;
import java.security.*;
import java.util.ArrayList;
import java.util.Date;

import distributeblocks.crypto.*;

/* 
 * Transaction Class for the contents of blocks in the chain.
 * Transactions can be expanded to have more features but
 * currently only enforce the exchange of our currency.
 *
 * Each instance of Transaction facilitates the exchange of
 * some amount of coin in one direction, but may be used
 * several times.
 *
 * Signatures verify that it is the sender authorizing the sending of coin.
 * Signatures sign hash/id_Transaction of transaction (see calculateHash())
 * 
 * Transactions are signed upon creation by the sender
 * (this is different from the tutorial which required 2 separate steps (create then sign))
 */
public class Transaction implements Serializable {
	private static final float MIN_TRANSACTION_AMOUNT = 0.1f;
	
	private String id_Transaction; //Hash of the contents of the Transaction
	private PublicKey pk_Sender; // senders address
	private PublicKey pk_Receiver; // receivers address
	private float exchange; // the amount to be exchanged
	private byte[] signature; // for user's personal wallet
	private ArrayList<TransactionIn> input = new ArrayList<TransactionIn>();
	private ArrayList<TransactionOut> output = new ArrayList<TransactionOut>();
	private long timestamp; //timestamp for the block
	//private static int count_Transactions = 0; // estimates number of transactions created.

	/*
   	* Generating transactions requires the public keys of both
   	* the sender and receiver as well as the amount.
   	*/
	public Transaction(PrivateKey senderPrivateKey, PublicKey send, PublicKey recieve , float amount,  ArrayList<TransactionIn> variables) {
		this.pk_Sender = send;
		this.pk_Receiver = recieve;
		this.exchange = amount;
		this.input = variables;
		this.timestamp = new Date().getTime();
		try {
			this.id_Transaction = calculateHash();
		} catch (FailedToHashException e) {
			e.printStackTrace();
		}
		generateSignature(senderPrivateKey);
	}

	/*
   	* Calculate id_Transaction
   	* This hash is based on the public keys of the sender and receiver,
   	* the amount to be sent, and the timestamp of the transaction
   	*/
	private String calculateHash() throws FailedToHashException{
    //count_Transactions++; //method to prevent identical hashes
    return Crypto.calculateObjectHash(
      Crypto.keyToString(pk_Sender) +
      Crypto.keyToString(pk_Receiver) +
      Float.toString(exchange) + 
      timestamp
      );
  }

	/*
   	* Input: The private key used to sign a transaction
   	* Details: Signs the hash/id of the transaction
   	* (which is a hash of the public keys for the sender/receiver, the amount sent, and the number of transactions in existence)
   	* Output: Sets the signature field of this transaction class
   	*/
	public void generateSignature(PrivateKey privateKey) {
	  this.signature = Crypto.signMessage(privateKey, this.id_Transaction);
	  return;
  }
  
	/*
	 * A method to return this transaction's signature
	 */
	public byte[] getSignature() {
	  return this.signature;
  }
  
	/*
	 * Details: Verifies that the signature of this transaction is correct by seeing if the
	 * signature and hash/id of this transaction correspond to the public key of the sender
	 * Output: Returns true if the signature matches the public key of the sender
	 */
	public boolean verifySignature() {
	  return Crypto.verifySignature(this.pk_Sender, this.id_Transaction, this.signature);
  }
  
	/*
   	* Method to handle the transaction. This will verify that
   	* the transaction is valid, and create the appropriate
   	* outputs if so.
   	* 
   	* Returns true if the transaction is created, false otherwise
   	*/
	public boolean transactionEnforcer() {
  		if(verifySignature() == false) {
  			System.out.println("#Transaction Signature failed to verify");
  			return false;
  		}
  
  		// TODO replace with actual validation? Verify that the incoming transactions are valid
  		for(TransactionIn i : input) {
  			if (! isValidSource(i.getSourceId())) {
  				System.out.println("Invalid source transaction: " + i.getSourceId());
  				return false;
  			}
  		}

  		// Verify that the transaction is large enough
  		if(getInputExchange() < MIN_TRANSACTION_AMOUNT) {
  			System.out.println("# Inputs too small: " + getInputExchange());
  			return false;
  		}
  		
  		try {
  		//generate transaction output:
  		float remaining = getInputExchange() - exchange;
  		output.add(new TransactionOut(this.pk_Receiver, exchange, id_Transaction));		// Send exchange to receiver
  		if (remaining != 0.0f)
  			output.add(new TransactionOut(this.pk_Sender, remaining, id_Transaction)); 	// Send the left over 'change' back to sender

  		return true;
  		
  		} catch (FailedToHashException e) {
  			System.out.println("Failed to hash transaction");
  			return false;
  		}
	}

  	/*
  	 * Returns sum of exchange values being used
  	 * to create this transaction.
  	 */
  	public float getInputExchange() {
  		float total = 0;
  		for(TransactionIn i : input) {
  			total += i.getExchange();
  		}
  		return total;
  	}

  	/*
  	 * Returns sum of exchange values being
  	 * sent as a result of this transaction.
  	 */
  	public float getOutputExchange() {
  		float total = 0;
  		for(TransactionOut o : output) {
  			total += o.getExchange();
  		}
  		return total;
  	}
  	
  	/*
  	 * TODO: IMPLEMENT THIS METHOD
  	 * This method must check against the block to see if a transaction
  	 * with the given id exists. If so, return true, else, return false.
  	 */
  	public static boolean isValidSource(String id_Transaction_Out) {
  		return true;
  	}

	public String getId_Transaction() {
		return id_Transaction;
	}

	public String getExchangeAmmountString() {
		return (String.valueOf(this.exchange));
	}

	public String getPublicSender(){
  		return pk_Sender.toString();
	}

	public String getPublicReceiver(){
		return pk_Receiver.toString();
	}
}
