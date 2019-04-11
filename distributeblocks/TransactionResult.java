package distributeblocks;

import java.io.Serializable;
import java.security.*;
import java.util.ArrayList;

import distributeblocks.crypto.*;

/**
 * TransactionResult is used to keep track of a
 * specific exchange of funds to some 
 * receiving party.
 * 
 * This is one of the required pieces for a
 * full Transaction.
 */
public class TransactionResult implements Serializable {

	private String id; 				// ID of the transaction
	private PublicKey pk_Receiver; 	// Receiver of the coins
	private String containerId = ""; 		// The id of the transaction this output was created in
	private ArrayList<String> sourceIds = new ArrayList<>(); // the ids of transaction where this came from
	private float exchange; 		// Amount transfered / receiver owns
	
	public TransactionResult(PublicKey pk_Target, float amount, String containerId, ArrayList<String> sourceIds) throws FailedToHashException{
		this.pk_Receiver = pk_Target;
		this.exchange = amount;
		this.containerId = containerId;
		this.id = Crypto.calculateObjectHash(Crypto.keyToString(pk_Target)+Float.toString(amount)+ containerId);
		this.sourceIds = sourceIds;
	}
	
	public TransactionResult(PublicKey pk_Target, float amount, ArrayList<String> sourceIds) throws FailedToHashException{
		this.pk_Receiver = pk_Target;
		this.exchange = amount;
		this.id = Crypto.calculateObjectHash(Crypto.keyToString(pk_Target)+Float.toString(amount)+ containerId);
		this.sourceIds = sourceIds;
	}

	public void setParentIds(ArrayList<String> parentIds) {
		this.sourceIds = parentIds;
	}
	
	public void setContainerId(String id) {
		this.containerId = id;
	}
	
	/**
	 * Check if a coin belongs to the given key
	 * 
	 * @param publicKey 	the PublicKey to check against
	 * 
	 * @return boolean indicating if this TransactionResult is to the publicKey
	 */
	public boolean isMine(PublicKey publicKey) {
		return (publicKey.hashCode() == pk_Receiver.hashCode());
	}
	
	// Getter methods
	public float getExchange() { return exchange; }
	public String getId() { return id; }
	public String getContainerId() { return containerId; }
	public ArrayList<String> getSourceIds() { return sourceIds; }
}
