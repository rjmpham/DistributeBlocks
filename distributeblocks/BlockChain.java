package distributeblocks;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.MissingBlockMessage;
import distributeblocks.io.Console;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class BlockChain implements Serializable {
	private static final int VERIFIED_DEPTH = 2;				// depth from the head we consider a block to be verified

	private ArrayList<LinkedList<Block>> blockChain;
	private HashMap<String, Block> allBlocks; 									// To make looking up blocks much faster.
	private HashMap<String, Transaction> allTransactions = new HashMap<>();		// Easy access to every transaction in the longest chain


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
	 */
	public void addBlock(Block block){

		if (block == null){
			Console.log("GOT NULL BLOCK!");
		}

		// Check to see if we already have the block.
		if (allBlocks.containsKey(block.getHashBlock())){
			return;
		}

		allBlocks.put(block.getHashBlock(), block);


		// Lets look at all the heads to see if we have previousBlock
		String previous = block.getHashPrevious();

		for (LinkedList<Block> ls : blockChain){

			if (ls.getLast().getHashBlock().equals(previous)){

				// Add it to current fork.
				ls.add(block);
				updateAllTransactions();
				return;
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

						// Finish off by adding the new block.
						newFork.add(block);
						blockChain.add(newFork); // And add the new fork.
						updateAllTransactions();
						return;
					}

					index ++;
				}
			}
		}

		// Uh oh, WE DIDNT HAVE THE PREVIOUS BLOCK, PANIC!!!!
		allBlocks.remove(block.getHashBlock());
		NetworkService.getNetworkManager().asyncEnqueue(new MissingBlockMessage(block.getHashPrevious()));
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
	 * Creates a HashMap of Strings to Transactions of every
	 * transaction within the longest chain. This is from the genesis
	 * block, up to and including the head of the chain;
	 * 
	 * @return HashMap of Strings to Transaction of every verified transaction
	 */
	private void updateAllTransactions() {
		LinkedList<Block> longest = getLongestChain();
		HashMap<String, Transaction> all = new HashMap<String, Transaction>();
		
		// the chain is empty
		if (longest == null) {
			this.allTransactions = all;
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
	 * Get a hashmap of all transactions on the longest chain.
	 * 
	 * @return hashmap from String to Transaction of every transaction on the longest chain
	 */
	public synchronized HashMap<String, Transaction> getAllTransactions() {
		return this.allTransactions;
	}

	/**
	 * Loads blockchain from file.
	 */
	public synchronized void load(){

		this.blockChain = new ConfigManager().loadBlockChain();

		allBlocks = new HashMap<>();

		for (LinkedList<Block> ls : blockChain){
			for (Block b : ls){
				allBlocks.put(b.getHashBlock(), b);
			}
		}
		updateAllTransactions();
	}

	public synchronized void save(){

		new ConfigManager().saveBlockChain(this.blockChain);
	}
}
