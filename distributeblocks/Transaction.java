/* Transaction Class for the contents of blocks in the chain.
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
package distributeblocks;


import java.security.*;
import java.util.ArrayList;
import java.util.Date;

import distributeblocks.crypto.*;

public class Transaction {
  public String id_Transaction; //Hash of the contents of the Transaction
  public PublicKey pk_Sender; // senders address
  public PublicKey pk_Receiver; // receivers address
  public float exchange; // the amount to be exchanged
  public byte[] signature; // for user's personal wallet
  public ArrayList<TransactionIn> input = new ArrayList<TransactionIn>();
  public ArrayList<TransactionOut> output = new ArrayList<TransactionOut>();
  //private static int count_Transactions = 0; // estimates number of transactions created.
  private long timestamp; //timestamp for the block

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

  // Calculate id_Transaction
  //This hash is based on the public keys of the sender and receiver,
  //the amount to be sent, and the timestamp of the transaction
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
  public void generateSignature( PrivateKey privateKey ) {
	  this.signature = Crypto.signMessage(privateKey, this.id_Transaction);
	  return;
  }
  
  //A method to return this transaction's signature
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
  
  // Method to handle the transaction. Returns true if the transaction is created
  public boolean transactionEnforcer() throws FailedToHashException{
  		if(verifySignature() == false) {
  			System.out.println("#Transaction Signature failed to verify");
  			return false;
  		}
  
  		//gather transactions that are inputs (Make sure they are unspent):
  		for(TransactionIn i : input) {
  			i.funds = testDriver.funds_HashMap.get(i.id_Transaction_Out);
  		}

  		//check if a transaction is valid:
  		if(getExchangeAmount() < testDriver.minimumTransactionAmount) {
  			System.out.println("# Inputs to small: " + getExchangeAmount());
  			return false;
  		}

  		//generate transaction output:
  		float remaining = getExchangeAmount() - exchange;
      //get exchange amount of input then the left over change:
  		//id_Transaction = calculateHash(); this is no longer necessary but leaving it in case want to revert, should delete once confidence is gained
  		output.add(new TransactionOut( this.pk_Receiver, exchange,id_Transaction)); //send exchange to receiver
  		output.add(new TransactionOut( this.pk_Sender, remaining,id_Transaction)); //send the left over 'change' back to sender

  		//add output to funds list
  		for(TransactionOut o : output) {
  			testDriver.funds_HashMap.put(o.id , o);
  		}

  		//remove transaction input from funds lists as spent:
  		for(TransactionIn i : input) {
  			if(i.funds == null) continue; //if the transaction can't be found skip it
  			testDriver.funds_HashMap.remove(i.funds.id);
  		}

  		return true;
  	}

  //returns sum of exchanges values
  	public float getExchangeAmount() {
  		float total = 0;
  		for(TransactionIn i : input) {
  			if(i.funds == null) continue; //if the transaction can't be found skip it
  			total += i.funds.exchange;
  		}
  		return total;
  	}

  //returns sum of output:
  	public float getExchangeOutput() {
  		float total = 0;
  		for(TransactionOut o : output) {
  			total += o.exchange;
  		}
  		return total;
  	}

}
