/* Contract Class for the contents of blocks in the chain.
 * Contracts can be expanded to have more features but
 * currently only enforce the exchange of our currency.
 *
 * Each instance of Contract facilitates the exchange of
 * some amount of coin in one direction, but may be used
 * several times.
 *
 * Signatures have yet to be implemented and would verify
 * that it is the sender authorizing the sending of coin.
 */



import java.security.*;
import java.util.ArrayList;

public class Contract {
  public String id_Contract; //Hash of the contents of the Contract
  public PublicKey pk_Sender; // senders address
  public PublicKey pk_Receiver; // recivers address
  public float exchange; // the amount to be exchanged
  public byte[] signature; // for user's personal wallet
  public ArrayList<Contract_In> input = new ArrayList<Contract_In>();
  public ArrayList<Contract_Out> output = new ArrayList<Contract_Out>();
  private static int count_Contracts = 0; // estimates number of contracts created.

  /*
   * Generating contracts requires the public keys of both
   * the sender and reciever as well as the ammount.
   */
  public Contract(PublicKey send,PublicKey recieve , float amount,  ArrayList<Contract_In> varriables) {
		this.pk_Sender = send;
		this.pk_Receiver = recieve;
		this.exchange = amount;
		this.input = varriables;
	}

  // Calculate id_Contract
  private String calulateHash() throws FailedToHashException{
    count_Contracts++; //method to prevent identical hashes
    return Crypto.calculateObjectHash(
      Crypto.keyToString(pk_Sender) +
      Crypto.keyToString(pk_Receiver) +
      Float.toString(exchange) + count_Contracts
      );
  }


  // Method to handle the contract. Returns true of the contract is created
  public boolean contractEnforcer() throws FailedToHashException{

    //We haven't implemented signatures yet
  /*
  		if(verifiySignature() == false) {
  			System.out.println("#Transaction Signature failed to verify");
  			return false;
  		}
  */
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
  		id_Contract = calulateHash();
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
