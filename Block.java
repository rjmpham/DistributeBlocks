//Block for the blockchain
//NOTE: The data stored in the block must be a serializable object

import java.util.Date;
import java.io.Serializable;

public class Block implements Serializable {

	//Variables
	private String hashBlock;			//Hash of the entire block (not including this field)
	private String hashData;			//Hash of the data
	private String hashPrevious;		//Hash of the previous block
	private int nonce;				//Nonce used in the hash of the block to get the right number of zeros
	private Object data;				//Data being stored in the block. Should be serializable.
	private long timestamp;				//timestamp for the block
	private int targetNumZeros;		//How many zeros hashBlock must start with in order to be a mined block

	//Getter methods
	public String getHashBlock() {return hashBlock;}
	public String getHashData() {return hashData;}
	public String getHashPrevious() {return hashPrevious;}
	public int getNonce() {return nonce;}
	public Object getData() {return data;}
	public long getTimestamp() {return timestamp;}
	public long getTargetNumZeros() {return targetNumZeros;}

	public Block (Object data, String hashPrevious, int targetNumZeros) throws FailedToHashException
	{
		this.nonce = 0;
		this.targetNumZeros = targetNumZeros;
		this.hashPrevious = hashPrevious;
		this.timestamp = new Date().getTime();
		this.data = data;
		this.hashData = Crypto.calculateObjectHash(data);
		this.hashBlock = Crypto.calculateBlockHash(this);
	}

	//Setter methods
	public void setNonce(int nonce) throws FailedToHashException
	{
		this.nonce = nonce;
		this.hashBlock = Crypto.calculateBlockHash(this);
	}
	public void setTargetNumZeros(int targetNumZeros)
	{
		this.targetNumZeros = targetNumZeros;
	}

	public void mineBlock() throws FailedToHashException {
		String target = new String(new char[targetNumZeros]).replace('\0', '0'); //Create a string with difficulty * "0"
		while(!hashBlock.substring( 0, targetNumZeros).equals(target)) {
			this.setNonce(nonce + 1);
		}

		// For testing.
		System.out.println("Block Mined!!! : " + hashBlock);
	}

	public boolean isBlockMined(){

		String target = new String(new char[targetNumZeros]).replace('\0', '0'); //Create a string with difficulty * "0"

		if (hashBlock.substring(0, targetNumZeros).equals(target)){
			return true;
		}

		return false;
	}
}
