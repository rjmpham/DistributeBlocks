package distributeblocks;

import distributeblocks.crypto.Crypto;
import distributeblocks.cli.CommandLineInterface;
import distributeblocks.io.ConfigManager;
import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkActions;
import distributeblocks.net.NetworkConfig;
import distributeblocks.net.NetworkService;
import picocli.CommandLine;

import java.lang.reflect.Field;
import java.util.LinkedList;

public class Node {
	public static String PEER_CONFIG_FILE = "./peer_config.txt";
	public static String BLOCKCHAIN_FILE = "./blockchain.txt";
	public static int HASH_DIFFICULTY = 4;
	
	private boolean started = false;
	// TODO: The node needs a wallet
	
	/*
	 * Starts up the network threads and marks the node as started.
	 */
	public void initializeNetworkService(NetworkConfig config) {
		NetworkService.init(config);
		started = true;
	}
	
	/*
	 * Closes all threads and safely kills the node.
	 */
	// TODO: implement this
	public void exit() {
	}
	
	/*
	 * Returns whether the node has been started or not.
	 * This is used to block commands that require the node
	 * to be running first.
	 */
	public boolean started() {
		return started;
	}

	public static void init(){
		new BlockChain(); // Load the chain (generates the file).
	}

	public static Block getGenisisBlock(){

		// TODO: Deal with the damn timestamp!!!!!

		try {
			Block block = new Block("Genisis", "", 0);


			// TODO: This is a crappy hack to get all the nodes to have the same genesis block. Do something else?
			try {
				Field timeStamp = Block.class.getDeclaredField("timestamp");
				timeStamp.setAccessible(true);
				timeStamp.set(block, 0);

				Field hashBlock = Block.class.getDeclaredField("hashBlock");
				hashBlock.setAccessible(true);
				hashBlock.set(block, Crypto.calculateBlockHash(block));

			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			block.mineBlock();

			return block;
		} catch (FailedToHashException e) {
			e.printStackTrace();
			throw new RuntimeException("The genisis block failed to hash, something got messed up.");
		}
	}

	public static void main (String[] args){
		// Initialize this node
		Node node = new Node();
		Node.init();
		
		// Parse initial args then run the cli
		CommandLineInterface cli = new CommandLineInterface(node);
		cli.parseCommand(args);
		cli.run();
	}
}
