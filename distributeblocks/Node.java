package distributeblocks;

import distributeblocks.crypto.Crypto;
import distributeblocks.cli.*;
import distributeblocks.io.ConfigManager;
import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkActions;
import distributeblocks.net.NetworkConfig;
import distributeblocks.net.NetworkService;
import picocli.CommandLine;

import java.lang.reflect.Field;
import java.util.LinkedList;

public class Node {


	private static int minPeers = 3;
	private static int maxPeers = 10;
	private static int port = 5832;
	private static IPAddress seedNode = new IPAddress("localhost", 5831); // TODO: Support multiple seed nodes.
	private static boolean seed = false;
	private static boolean mining = false;

	public static String PEER_CONFIG_FILE = "./peer_config.txt";
	public static String BLOCKCHAIN_FILE = "./blockchain.txt";
	public static int HASH_DIFFICULTY = 4;


	/**
	 *
	 *
	 * @param args
	 */
	public static void main (String[] args){
		// Initialize this node
		Node.init();

		// Read the network config and start network services
		NetworkConfig config = CommandLine.call(new Start(), args);
		NetworkService.init(config);
		
		// TODO: add imput loop here to call command parser methods like the line above
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


}
