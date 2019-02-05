//Block for the blockchain
//NOTE: The data stored in the block must be a serializable object

import java.util.Date;
import java.io.Serializable;

public class Block implements Serializable {

	//Variables
	private String hashBlock;			//Hash of the entire block (not including this field)
	private String hashData;			//Hash of the data
	private String hashPrevious;		//Hash of the previous block
	private String nonce;				//Nonce used in the hash of the block to get the right number of zeros
	private Object data;				//Data being stored in the block. Should be serializable.
	private long timestamp;				//timestamp for the block
	
	//Getter methods
	public String getHashBlock() {return hashBlock;}
	public String getHashData() {return hashData;}
	public String getHashPrevious() {return hashPrevious;}
	public String getNonce() {return nonce;}
	public Object getData() {return data;}
	public long getTimestamp() {return timestamp;}
	
	//Setter methods
	public void setNonce(String nonce) throws FailedToHashException
	{
		this.nonce = nonce;
		this.hashBlock = Hasher.calculateHash(this);
	}
	
	public Block (Object data, String hashPrevious) throws FailedToHashException
	{
		this.data = data;
		this.hashPrevious = hashPrevious;
		this.timestamp = new Date().getTime();
		this.hashData = Hasher.calculateHash(data);
		this.hashBlock = Hasher.calculateHash(this);
	}	
}