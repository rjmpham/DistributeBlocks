//Block for the blockchain
//NOTE: The data stored in the block must be a serializable object
package distributeblocks;

import java.util.ArrayList;
import java.util.Date;
import java.io.Serializable;
import java.util.HashMap;

import distributeblocks.crypto.*;



public class Block implements Serializable {

	//Variables
	private String hashBlock;								//Hash of the entire block (not including this field)
	private String hashData;								//Hash of the data
	private String hashPrevious;							//Hash of the previous block
	private int nonce;										//Nonce used in the hash of the block to get the right number of zeros
	private HashMap<String, Transaction> data;			//Data being stored in the block. Should be serializable.
	private long timestamp;									//timestamp for the block

	private int targetNumZeros;		//How many zeros hashBlock must start with in order to be a mined block
	private volatile boolean stopMining; // Flag that can be set to terminate a mining operation

	//Getter methods
	public String getHashBlock() {return hashBlock;}
	public String getHashData() {return hashData;}
	public String getHashPrevious() {return hashPrevious;}
	public int getNonce() {return nonce;}
	public HashMap<String, Transaction> getData() {return data;}
	public long getTimestamp() {return timestamp;}
	public long getTargetNumZeros() {return targetNumZeros;}

	/*
	 *  Block require a body, a pointer to the previous block (hashPrevious), and a target to mine.
	 *  This method attempts to mine the current data, by making the first hash which is unlikley
	 *  to be valid.
	 */
	public Block (HashMap<String, Transaction> data, String hashPrevious, int targetNumZeros) throws FailedToHashException
	{
		this.nonce = 0;
		this.targetNumZeros = targetNumZeros;
		this.hashPrevious = hashPrevious;
		this.timestamp = new Date().getTime();
		this.data = data;
		this.hashData = Crypto.calculateObjectHash(data);
		this.hashBlock = Crypto.calculateBlockHash(this);
		stopMining = false;
	}

	public void mineBlock() throws FailedToHashException {
		String target = new String(new char[targetNumZeros]).replace('\0', '0'); //Create a string with difficulty * "0"
		while(!hashBlock.substring( 0, targetNumZeros).equals(target) && !stopMining) {

			if (this.nonce + 1 < Integer.MAX_VALUE) {
				this.nonce += 1;
			} else {
				// No hash was found, update the timestamp and try again.
				timestamp = new Date().getTime();
				this.nonce=0;
			}
			this.hashBlock = Crypto.calculateBlockHash(this);
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


	public boolean isStopMining() {
		return stopMining;
	}

	public void setStopMining(boolean stopMining) {
		this.stopMining = stopMining;
	}
}
