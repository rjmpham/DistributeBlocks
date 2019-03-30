package distributeblocks;

import java.security.*;
import distributeblocks.crypto.*;

public class TransactionOut {

	public String id; 				// ID of the transaction
	public PublicKey pk_Receiver; 	// Receiver of the coins
	public String id_Parent; 		// The id of the transaction this output was created in
	private float exchange; 		// Amount transfered / receiver owns

	//Constructor
	public TransactionOut(PublicKey pk_Target, float amount, String id_Input) throws FailedToHashException{
		this.pk_Receiver = pk_Target;
		this.exchange = amount;
		this.id_Parent = id_Input;
		this.id = Crypto.calculateObjectHash(Crypto.keyToString(pk_Target)+Float.toString(amount)+ id_Input);
	}

	/*
	 * Check if a coin belongs to the given key
	 */
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == pk_Receiver);
	}
	
	/*
	 * Returns the exchange value of this transaction
	 */
	public float getExchange() {
		return exchange;
	}

}
