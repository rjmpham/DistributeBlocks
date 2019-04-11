package distributeblocks;

import java.io.Serializable;
import java.security.*;
import java.util.ArrayList;
import java.util.Date;
import distributeblocks.util.Validator;
import distributeblocks.crypto.*;

/**
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
	
	private String transactionId; //Hash of the contents of the Transaction
	private PublicKey pk_Sender; // senders address
	private PublicKey pk_Receiver; // receivers address
	private float exchange; // the amount to be exchanged
	private byte[] signature; // for user's personal wallet
	private ArrayList<String> sourceIds = new ArrayList<String>();	// the ids of TransactionResults this uses
	private ArrayList<TransactionResult> input = new ArrayList<TransactionResult>();
	private ArrayList<TransactionResult> output = new ArrayList<TransactionResult>();
	private long timestamp; //timestamp for the block
	//private static int count_Transactions = 0; // estimates number of transactions created.

	/**
   	* Constructor for a new transaction
   	* Generating transactions requires the public keys of both
   	* the sender and receiver as well as the amount.
   	* 
   	* @param senderPrivateKey	PrivateKey of the sender
   	* @param send				PublicKey of the sender
   	* @param receive			PublicKey of the receiver
   	* @param amount				Amount to send
   	* @param variables			Inputs being used
   	*/
	public Transaction(PrivateKey senderPrivateKey, PublicKey send, PublicKey recieve , float amount,  ArrayList<TransactionResult> variables) {
		this.pk_Sender = send;
		this.pk_Receiver = recieve;
		this.exchange = amount;
		this.input = variables;
		
		this.sourceIds = new ArrayList<String>();
		for (TransactionResult r: this.input) {
			this.sourceIds.add(r.getId());
		}
		
		this.timestamp = new Date().getTime();
		try {
			this.transactionId = calculateHash();
		} catch (FailedToHashException e) {
			e.printStackTrace();
		}
		generateSignature(senderPrivateKey);
	}

	/**
   	* Calculate id_Transaction
   	* This hash is based on the public keys of the sender and receiver,
   	* the amount to be sent, and the timestamp of the transaction.
   	* 
   	* @return the hash of the transaction
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

	/**
	* Signs the hash/id of the transaction
   	* (which is a hash of the public keys for the sender/receiver, the amount sent, and the number of transactions in existence)
   	* Sets the signature field of this transaction class.
   	* 
   	* @param privateKey		The PrivateKey used to sign the transaction
   	*/
	public void generateSignature(PrivateKey privateKey) {
	  this.signature = Crypto.signMessage(privateKey, this.transactionId);
	  return;
  }
  
	/**
	 * Verifies that the signature of this transaction is correct by seeing if the
	 * signature and hash/id of this transaction correspond to the public key of the sender
	 * 
	 * @return true if the signature matches the public key of the sender
	 */
	public boolean verifySignature() {
	  return Crypto.verifySignature(this.pk_Sender, this.transactionId, this.signature);
  }
  
	/**
   	* Method to handle the transaction. This will verify that
   	* the transaction is valid, and create the appropriate
   	* outputs if so.
   	* 
   	* @return true if the transaction is created, false otherwise
   	*/
	// TODO: why did we call this "transactionEnforcer"? maybe we should rename it
	public boolean transactionEnforcer() {
  		if(verifySignature() == false) {
  			System.out.println("Transaction Signature failed to verify");
  			return false;
  		}
  		
  		// Verify the transaction against the blockchain
  		if (!Validator.isValidTransaction(this)) {
  			System.out.println("Failed to validate transaction inputs against the blockchain");
  			return false;
  		}

  		// Verify that the transaction is large enough
  		if(getInputExchange() < MIN_TRANSACTION_AMOUNT) {
  			System.out.println("Transaction inputs too small: " + getInputExchange());
  			return false;
  		}
  		
  		try {
  		//generate transaction output:
  		float remaining = getInputExchange() - exchange;
  		output.add(new TransactionResult(this.pk_Receiver, exchange, transactionId, sourceIds));		// Send exchange to receiver
  		if (remaining != 0.0f)
  			output.add(new TransactionResult(this.pk_Sender, remaining, transactionId, sourceIds)); 	// Send the left over 'change' back to sender

  		return true;
  		
  		} catch (FailedToHashException e) {
  			System.out.println("Failed to hash transaction");
  			return false;
  		}
	}
  
	public ArrayList<TransactionResult> getTransactionInputs() {
		return this.input;
	}

  	/**
  	 * Returns sum of exchange values being used
  	 * to create this transaction.
  	 * 
  	 * @return sum of exchange values being used
  	 */
  	public float getInputExchange() {
  		float total = 0;
  		for(TransactionResult i : input) {
  			total += i.getExchange();
  		}
  		return total;
  	}
  	
  	public float getExchange() {
  		return this.exchange;
  	}

  	/**
  	 * Returns sum of exchange values being
  	 * sent as a result of this transaction.
  	 * 
  	 * @return sum of exchange values being sent
  	 */
  	public float getOutputExchange() {
  		float total = 0;
  		for(TransactionResult o : output) {
  			total += o.getExchange();
  		}
  		return total;
  	}

  	// Getter methods
  	public byte[] getSignature() { return this.signature; }
   	public ArrayList<TransactionResult> getInput() { return input; }
   	public ArrayList<TransactionResult> getOutput() { return output; }
	public String getTransactionId() { return transactionId; }
    public PublicKey getPublicKeySender(){ return pk_Sender; }
    public ArrayList<String> getParentIds() { return sourceIds; }
}
