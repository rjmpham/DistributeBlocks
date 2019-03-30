package distributeblocks;

import java.security.*;
import distributeblocks.crypto.*;

public class TransactionOut {

	public String id; // of the transaction
	public PublicKey pk_Receiver; // of the coins
	public float exchange; // amount transfered / receiver owns
	public String id_Parent; //the id of the transaction this output was created in

	//
	public float getExchange() {return exchange;}

	//Constructor
	public TransactionOut(PublicKey pk_Target, float amount, String id_Input) throws FailedToHashException{
		this.pk_Receiver = pk_Target;
		this.exchange = amount;
		this.id_Parent = id_Input;
		this.id = Crypto.calculateObjectHash(Crypto.keyToString(pk_Target)+Float.toString(amount)+ id_Input);
	}

	//Check if coin belongs to you
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == pk_Receiver);
	}

}
