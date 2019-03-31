package distributeblocks;

import distributeblocks.crypto.Crypto;
import distributeblocks.io.ConfigManager;
import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkActions;
import distributeblocks.net.NetworkConfig;
import distributeblocks.net.NetworkService;

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


		// TODO proper type checking and crap.
		for (int i = 0; i < args.length; i ++){

			String a = args[i];

			System.out.println("got arg: " + args[i]);
			switch (a){
				case "minp":
					minPeers = Integer.parseInt(args[i+1]);
					break;
				case "maxp":
					maxPeers = Integer.parseInt(args[i+1]);
					break;
				case "port":
					port = Integer.parseInt(args[i+1]);
					System.out.println("Got port: " + port);
					break;
				case "seedAddr":
					String seedAddr = args[i+1];
					int seedPort = Integer.parseInt(args[i+2]);
					seedNode = new IPAddress(seedAddr, seedPort);
					break;
				case "seed":
					seed = true;
					break;
				case "config":
					PEER_CONFIG_FILE = args[i+1];
					break;
				case "chainfile":
					BLOCKCHAIN_FILE = args[i+1];
					break;
				case "mining":
					String state = args[i+1];
					if (state.equals("y")){
						mining = true;
					} else {
						mining = false;
					}
					break;
			}
		}

		Node.init();


		NetworkConfig config = new NetworkConfig();
		config.maxPeers = maxPeers;
		config.minPeers = minPeers;
		config.port = port;
		config.seed = seed;
		config.seedNode = seedNode;
		config.mining = mining;

		// TODO: Maybe replace param list with config object.
		NetworkService.init(config);

		/*while (true){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Still alive");
		}*/

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
