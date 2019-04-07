package distributeblocks;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.MissingBlockMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class BlockChain implements Serializable {


	private ArrayList<LinkedList<Block>> blockChain;
	private HashMap<String, Block> allBlocks; // To make looking up blocks much faster.


	/**
	 * Automaticaly loads chain from file.
	 */
	public BlockChain() {

		load();
	}

	/**
	 * Adds block to the chain. Automaticaly puts it on the right fork.
	 *
	 * @param block
	 */
	public void addBlock(Block block){

		if (block == null){
			System.out.println("GOT NULL BLOCK!");
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
				return;
			} else {

				// Wasnt at the end, so either we dont have the previous, or this is a new fork.
				int index = 0;
				for (Block b : ls){

					if (b.getHashBlock().equals(previous)){

						// We found it, so this is a new fork.
						System.out.println("===== New fork was created ====");
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
	 * Loads blocklchain from file.
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
