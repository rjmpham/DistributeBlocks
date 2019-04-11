package distributeblocks;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.MissingBlockMessage;
import distributeblocks.io.Console;
import distributeblocks.util.Validator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class BlockChain implements Serializable {
	private static final int VERIFIED_DEPTH = 2;				// depth from the head we consider a block to be verified

	private ArrayList<LinkedList<Block>> blockChain;

	private static Object blockLock = new Object();
	private HashMap<String, Block> allBlocks; 									// To make looking up blocks much faster.
	private HashMap<String, Transaction> allTransactions = new HashMap<>();					// Easy access to every root Transaction
	private HashMap<String, TransactionResult> allTransactionResults = new HashMap<>();		// Easy access to every TransactionResult in the longest chain

	/**
	 * Automatically loads chain from file.
	 */
	public BlockChain() {
		load();
	}

	/**
	 * Adds block to the chain. Automatically puts it on the right fork.
	 *
	 * @param block
	 * 
	 * @return true if the block was added successfully
	 */
	public boolean addBlock(Block block){

		if (block == null){
			Console.log("GOT NULL BLOCK!");
			return false;
		}

		// Check to see if we already have the block. If we do it's valid already
		if (allBlocks.containsKey(block.getHashBlock())){
			return false;
		}

		allBlocks.put(block.getHashBlock(), block);

		String previous = block.getHashPrevious();


		Validator validator = new Validator();
		//Checks for the parent block on all chains
		for (LinkedList<Block> ls : blockChain){

			//If the block is a new header block add it to the head of a chain
			if (ls.getLast().getHashBlock().equals(previous)){
        
				try {
					if (validator.isValidBlock(block,this.getVerifiedTransactions(ls))) {
						ls.add(block);
						updateAllTransactions();
						updateAllTransactionResults();
						return true;
					}
				} catch (FailedToHashException e) {
					Console.log("Failed to hash exception! BlockChain class under addBlock.");
				}

				//else, if the parent is down the chain make a fork
			} else {

				// Wasnt at the end, so either we dont have the previous, or this is a new fork.
				int index = 0;
				for (Block b : ls){

					if (b.getHashBlock().equals(previous)){

						// We found it, so this is a new fork.
						Console.log("===== New fork was created ====");
						LinkedList newFork = new LinkedList();

						int i = 0;
						for (Block bl : ls){ // Go through and add every block up to and including previousBlock

							if (i++ > index){
								break;
							}

							newFork.add(bl);
						}

						try {
							if (validator.isValidBlock(block,this.getVerifiedTransactions(newFork))) {
								/* Finish off by adding the new block. */
								newFork.add(block);
								blockChain.add(newFork); // And add the new fork.
								updateAllTransactions();
								updateAllTransactionResults();
								return true;
							}
							else {
								return false;
							}
						} catch (FailedToHashException e) {
							Console.log("Failed to hash exception! BlockChain class under addBlock.");
						}
					}

					index ++;
				}
			}
		}

		// Uh oh, WE DIDNT HAVE THE PREVIOUS BLOCK, PANIC!!!!
		allBlocks.remove(block.getHashBlock());
		NetworkService.getNetworkManager().asyncEnqueue(new MissingBlockMessage(block.getHashPrevious()));
		return false;
	}

	/**
	 * Get the longest chain.
	 *
	 * @return
	 */
	public LinkedList<Block> getLongestChain(){
		
		try {
			//find the longest block
			LinkedList<Block> highest = blockChain.get(0);
			for (LinkedList<Block> ls : blockChain){
				if (ls.size() > highest.size()){
					highest = ls;
				}
			}
			return highest;
			
		}
		catch (IndexOutOfBoundsException e) {
			Console.log("BlockChain is empty, no longest chain!");
			return null;
		}
	}

	/**
	 * All the blocks, in every fork.
	 *
	 * @return
	 */
	public HashMap<String, Block> getAllBlocks(){
		return allBlocks;
	}
	
	/**
	 * Gets the block which is at the verified depth from the tail in
	 * the longest chain. The transactions on the returned block
	 * are considered to be verified because they are deep enough
	 * in the chain that it is extremely likely a competing branch
	 * will catch up.
	 * 
	 * @return		Last verified block (null if longest chain is too short)
	 */
	public Block getLastVerifiedBlock() {
		LinkedList<Block> longest = getLongestChain();
		Block block = null;
		
		try {
			block = longest.get(longest.size() - VERIFIED_DEPTH);
		}
		catch(IndexOutOfBoundsException e) {
			Console.log("Longest chain is shorter than the verified depth");
		}
		return block;
	}

	/**
	 * Creates a HashMap of Strings to Transactions of every verified
	 * transaction before the input block. This is from the genesis
	 * block, up to and excluding the block to be verified;
	 * TODO RICHARD THIS
	 * @param chain the block to be considered the head of the chain for the validation check
	 * @return HashMap of Strings to Transaction of every verified transaction before the input block
	 */
	public HashMap<String, Transaction> getVerifiedTransactions(LinkedList<Block> chain) {
		HashMap<String, Transaction> allVerifiedTransactions = new HashMap<String, Transaction>();
		
		// Go from the genesis block to the current
		// TODO: time complexity of this: is Java LinkedList doubly linked, or is going from the head faster?
		for(int i = 0; i < chain.size(); i++) {
			// Add every transaction on the block
			Block block = chain.get(i);
			allVerifiedTransactions.putAll(block.getData());
		}
		return allVerifiedTransactions;
	}

	/**
	 * Creates a HashMap of Strings to Transactions of every
	 * transaction within the longest chain. This is from the genesis
	 * block, up to and including the head of the chain;
	 * 
	 * @return HashMap of Strings to Transaction of every verified transaction
	 */
	// TODO: don't call thi method so often! it's time complexity is bad!
	private void updateAllTransactionResults() {
		LinkedList<Block> longest = getLongestChain();
		HashMap<String, TransactionResult> all = new HashMap<String, TransactionResult>();
		
		// the chain is empty
		if (longest == null) {
			this.allTransactionResults = all;
			return;
		}
		
		// Go from the genesis block to the current
		// TODO: time complexity of this: is Java LinkedList doubly linked, or is going from the head faster?
		for(int i = 0; i < longest.size(); i++) {
			// Add every transaction on the block
			Block block = longest.get(i);
			for(Transaction t: block.getData().values()) {
				for(TransactionResult r: t.getInput()) {
					all.put(r.getId(), r);
				}
			}
		}
		// update the local copy, and return it
		this.allTransactionResults = all;
	}
	
	/**
	 * Creates a HashMap of Strings to Transactions of every
	 * transaction within the longest chain. This is from the genesis
	 * block, up to and including the head of the chain;
	 * 
	 * @return HashMap of Strings to Transaction of every verified transaction
	 */
	// TODO: don't call this method so often! it's time complexity is bad!
	private void updateAllTransactions() {
		LinkedList<Block> longest = getLongestChain();
		HashMap<String, Transaction> all = new HashMap<String, Transaction>();
		
		// the chain is empty
		if (longest == null) {
			this.allTransactions= all;
			return;
		}
		
		// Go from the genesis block to the current
		// TODO: time complexity of this: is Java LinkedList doubly linked, or is going from the head faster?
		for(int i = 0; i < longest.size(); i++) {
			// Add every transaction on the block
			Block block = longest.get(i);
			all.putAll(block.getData());
		}
		// update the local copy, and return it
		this.allTransactions = all;
	}
	
	/**
	 * Get a hashmap of all transaction results on the longest chain.
	 * 
	 * @return hashmap from String to TransactionResult of every transaction on the longest chain
	 */
	public synchronized HashMap<String, TransactionResult> getAllTransactionResults() {
		return this.allTransactionResults;
	}
	
	/**
	 * Get a hashmap of all transactions on the longest chain.
	 * 
	 * @return hashmap from String to Transaction of every transaction on the longest chain
	 */
	public HashMap<String, Transaction> getAllTransactions() {

		synchronized (blockLock) {

			return this.allTransactions;
		}
	}

	public HashMap<String, Transaction> getAllTransactionsFromLongestChain(){

		HashMap<String, Transaction> trans = new HashMap<>();

		synchronized (blockLock){

			for (Block b : getLongestChain()){
				trans.putAll(b.getData());
			}
		}
		return trans;
	}

	/**
	 * Loads blockchain from file.
	 */
	public void load(){

		synchronized (blockLock) {
      
			this.blockChain = new ConfigManager().loadBlockChain();

			allBlocks = new HashMap<>();

			for (LinkedList<Block> ls : blockChain) {
				for (Block b : ls) {
					allBlocks.put(b.getHashBlock(), b);
				}
			}
		}
		updateAllTransactionResults();
	}

	public void save(){
		synchronized (blockLock) {
			new ConfigManager().saveBlockChain(this.blockChain,this.getLongestChain());
		}
	}
}
