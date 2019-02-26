/* Contract Class for the contents of blocks in the chain.
 * Contracts can be expanded to have more features but
 * currently only enforce the exchange of our currency.
 *
 * Each instance of Contract facilitates the exchange of
 * some amount of coin in one direction, but may be used
 * several times.
 *
 * Signatures verify that it is the sender authorizing the sending of coin.
 * Signatures sign hash/id_contract of transaction (see calculateHash())
 * 
 * Contracts are signed upon creation by the sender
 * (this is different from the tutorial which required 2 separate steps (create then sign))
 */
package distributeblocks;


import java.security.*;
import java.util.ArrayList;
import java.util.Date;

import distributeblocks.crypto.*;

public class Contract {
  public String id_Contract; //Hash of the contents of the Contract
  public PublicKey pk_Sender; // senders address
  public PublicKey pk_Receiver; // recivers address
  public float exchange; // the amount to be exchanged
  public byte[] signature; // for user's personal wallet
  public ArrayList<Contract_In> input = new ArrayList<Contract_In>();
  public ArrayList<Contract_Out> output = new ArrayList<Contract_Out>();
  //private static int count_Contracts = 0; // estimates number of contracts created.
  private long timestamp; //timestamp for the block

  /*
   * Generating contracts requires the public keys of both
   * the sender and reciever as well as the ammount.
   */
  public Contract(PrivateKey senderPrivateKey, PublicKey send,PublicKey recieve , float amount,  ArrayList<Contract_In> varriables) {
		this.pk_Sender = send;
		this.pk_Receiver = recieve;
		this.exchange = amount;
		this.input = varriables;
		this.timestamp = new Date().getTime();
		try {
			this.id_Contract = calculateHash();
		} catch (FailedToHashException e) {
			e.printStackTrace();
		}
		generateSignature(senderPrivateKey);
	}

  // Calculate id_Contract
  //This hash is based on the public keys of the sender and receiver,
  //the amount to be sent, and the timestamp of the transaction
  private String calculateHash() throws FailedToHashException{
    //count_Contracts++; //method to prevent identical hashes
    return Crypto.calculateObjectHash(
      Crypto.keyToString(pk_Sender) +
      Crypto.keyToString(pk_Receiver) +
      Float.toString(exchange) + timestamp
      );
  }

  /*
   * Input: The private key used to sign a contract
   * Details: Signs the hash/id of the contract
   * (which is a hash of the public keys for the sender/receiver, the amount sent, and the number of contracts in existence)
   * Output: Sets the signature field of this contract class
   */
  public void generateSignature( PrivateKey privateKey ) {
	  this.signature = Crypto.signMessage(privateKey, this.id_Contract );
	  return;
  }
  
  //A method to return this contract's signature
  public byte[] getSignature() {
	  return this.signature;
  }
  
  /*
   * Details: Verifies that the signature of this contract is correct by seeing if the
   * signature and hash/id of this contract correspond to the public key of the sender
   * Output: Returns true if the signature matches the public key of the sender
   */
  public boolean verifySignature() {
	  return Crypto.verifySignature(this.pk_Sender, this.id_Contract, this.signature);
  }
  
  // Method to handle the contract. Returns true if the contract is created
  public boolean contractEnforcer() throws FailedToHashException{
  		if(verifySignature() == false) {
  			System.out.println("#Transaction Signature failed to verify");
  			return false;
  		}
  
  		//gather contracts that are inputs (Make sure they are unspent):
  		for(Contract_In i : input) {
  			i.funds = testDriver.funds_HashMap.get(i.id_Contract_Out);
  		}

  		//check if a contract is valid:
  		if(getExchangeAmount() < testDriver.minimumContractAmount) {
  			System.out.println("# Inputs to small: " + getExchangeAmount());
  			return false;
  		}

  		//generate contract output:
  		float remaining = getExchangeAmount() - exchange;
      //get exchange amount of input then the left over change:
  		//id_Contract = calculateHash(); this is no longer necessary but leaving it in case want to revert, should delete once confidence is gained
  		output.add(new Contract_Out( this.pk_Receiver, exchange,id_Contract)); //send exchange to reciever
  		output.add(new Contract_Out( this.pk_Sender, remaining,id_Contract)); //send the left over 'change' back to sender

  		//add output to funds list
  		for(Contract_Out o : output) {
  			testDriver.funds_HashMap.put(o.id , o);
  		}

  		//remove contract input from funds lists as spent:
  		for(Contract_In i : input) {
  			if(i.funds == null) continue; //if the contract can't be found skip it
  			testDriver.funds_HashMap.remove(i.funds.id);
  		}

  		return true;
  	}

  //returns sum of exchanges values
  	public float getExchangeAmount() {
  		float total = 0;
  		for(Contract_In i : input) {
  			if(i.funds == null) continue; //if the contract can't be found skip it
  			total += i.funds.exchange;
  		}
  		return total;
  	}

  //returns sum of output:
  	public float getExchangeOutput() {
  		float total = 0;
  		for(Contract_Out o : output) {
  			total += o.exchange;
  		}
  		return total;
  	}

}
