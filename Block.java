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
	private long targetNumZeros;		//How many zeros hashBlock must start with in order to be a mined block
	
	//Getter methods
	public String getHashBlock() {return hashBlock;}
	public String getHashData() {return hashData;}
	public String getHashPrevious() {return hashPrevious;}
	public String getNonce() {return nonce;}
	public Object getData() {return data;}
	public long getTimestamp() {return timestamp;}
	public long getTargetNumZeros() {return targetNumZeros;}
	
	//Setter methods
	public void setNonce(String nonce) throws FailedToHashException
	{
		this.nonce = nonce;
		this.hashBlock = Hasher.calculateBlockHash(this);
	}
	public void setTargetNumZeros(long targetNumZeros)
	{
		this.targetNumZeros = targetNumZeros;
	}
	
	public Block (Object data, String hashPrevious, long targetNumZeros) throws FailedToHashException
	{
		this.nonce = "";
		this.targetNumZeros = targetNumZeros;
		this.hashPrevious = hashPrevious;
		this.timestamp = new Date().getTime();
		this.data = data;
		this.hashData = Hasher.calculateObjectHash(data);
		this.hashBlock = Hasher.calculateBlockHash(this);
	}
}