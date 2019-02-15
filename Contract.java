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
  public PublicKey pk_Reciever; // recivers address
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
		this.pk_Reciever = recieve;
		this.exchange = amount;
		this.input = varriables;
	}

  // Calculate id_Contract
  private String calulateHash() throws FailedToHashException{
    count_Contracts++; //method to prevent identical hashes
    return Crypto.calculateObjectHash(
      Crypto.keyToString(pk_Sender) +
      Crypto.keyToString(pk_Reciever) +
      Float.toString(exchange) + count_Contracts
      );
  }


  // Method to hendel the contract. Returns true of the contract is created
  public boolean contractEnforcer() throws FailedToHashException{
  /*
  		if(verifiySignature() == false) {
  			System.out.println("#Transaction Signature failed to verify");
  			return false;
  		}
  */
  		//gather transaction input (Make sure they are unspent):
  		for(Contract_In i : input) {
  			i.funds = testDriver.funds.get(i.id_Contract_Out);
  		}

  		//check if transaction is valid:
  		if(getInputsValue() < testDriver.minimumTransaction) {
  			System.out.println("#Transaction Inputs to small: " + getInputsValue());
  			return false;
  		}

  		//generate transaction output:
  		float remaining = getInputsValue() - exchange; //get exchange of input then the left over change:
  		id_Contract = calulateHash();
  		output.add(new Contract_Out( this.pk_Reciever, exchange,id_Contract)); //send exchange to recipient
  		output.add(new Contract_Out( this.pk_Sender, remaining,id_Contract)); //send the left over 'change' back to sender

  		//add output to Unspent list
  		for(Contract_Out o : output) {
  			testDriver.funds.put(o.id , o);
  		}

  		//remove transaction input from funds lists as spent:
  		for(Contract_In i : input) {
  			if(i.funds == null) continue; //if Transaction can't be found skip it
  			testDriver.funds.remove(i.funds.id);
  		}

  		return true;
  	}

  //returns sum of input(funds) exchanges
  	public float getInputsValue() {
  		float total = 0;
  		for(Contract_In i : input) {
  			if(i.funds == null) continue; //if Transaction can't be found skip it
  			total += i.funds.exchange;
  		}
  		return total;
  	}

  //returns sum of output:
  	public float getOutputsValue() {
  		float total = 0;
  		for(Contract_Out o : output) {
  			total += o.exchange;
  		}
  		return total;
  	}

}
