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
	private static final int VERIFIED_DEPTH = 6;	// depth from the head we consider a block to be verified

	private ArrayList<LinkedList<Block>> blockChain;
	private HashMap<String, Block> allBlocks; 		// To make looking up blocks much faster.


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

		// Check to see if we already have the block. If we do it's valid already
		if (allBlocks.containsKey(block.getHashBlock())){
			return;
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
						return;
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
								return;
							}
							else {
								return;
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
	}

	/**
	 * Get the longest chain.
	 *
	 * @return
	 */
	public LinkedList<Block> getLongestChain(){

		LinkedList<Block> highest = blockChain.get(0);

		for (LinkedList<Block> ls : blockChain){
			if (ls.size() > highest.size()){
				highest = ls;
			}
		}


		return highest;
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
	 * Creates a HashMap of Strings to Transactions of every verified
	 * transaction within the longest chain. This is from the genesis
	 * block, up to and including the last verified block;
	 *
	 * @return HashMap of Strings to Transaction of every verified transaction
	 */
	public HashMap<String, Transaction> getVerifiedTransactions() {
		LinkedList<Block> longest = getLongestChain();
		HashMap<String, Transaction> allVerifiedTransactions = new HashMap<String, Transaction>();

		// Go from the genesis block to the current
		// TODO: time complexity of this: is Java LinkedList doubly linked, or is going from the head faster?
		for(int i = 0; i < longest.size() - BlockChain.VERIFIED_DEPTH; i++) {
			// Add every transaction on the block
			Block block = longest.get(i);
			allVerifiedTransactions.putAll(block.getData());
		}
		return allVerifiedTransactions;
	}
	/**
	 * Loads blockchain from file.
	 */
	public synchronized void load(){

		this.blockChain = new ConfigManager().loadBlockCHain();

		allBlocks = new HashMap<>();

		for (LinkedList<Block> ls : blockChain){
			for (Block b : ls){
				allBlocks.put(b.getHashBlock(), b);
			}
		}

	}

	public synchronized void save(){

		new ConfigManager().saveBlockChain(this.blockChain);
	}
}
