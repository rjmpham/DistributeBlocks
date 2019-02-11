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
}
