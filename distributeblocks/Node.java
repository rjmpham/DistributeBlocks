package distributeblocks;

import distributeblocks.crypto.Crypto;
import distributeblocks.cli.CommandLineInterface;
import distributeblocks.io.ConfigManager;
import distributeblocks.io.WalletManager;
import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkActions;
import distributeblocks.net.NetworkConfig;
import distributeblocks.net.NetworkService;
import picocli.CommandLine;

import java.lang.reflect.Field;
import java.util.LinkedList;

/* TODO: THIS IS A BIG ONE:
 *		We need some way to get other node's public keys and save them to files.
 *		Without this, we won't be able to send anyone money.
 */

/* TODO: ALSO A BIG ONE:
 * 		We need a way for the network manager to call our wallet methods to
 * 		receive funds and clear out onHold once a block gets to be 6 deep.
 */
public class Node {
	public static String PEER_CONFIG_FILE = "./peer_config.txt";
	public static String BLOCKCHAIN_FILE = "./blockchain.txt";
	public static int HASH_DIFFICULTY = 4;
	
	private boolean started = false;
	private boolean mining = false;
	private Wallet wallet;
	private String walletPath;
	
	/*
	 * Starts up the network threads and marks the node as started.
	 */
	public void initializeNetworkService(NetworkConfig config) {
		NetworkService.init(config);
		started = true;
	}
	
	/*
	 * Closes all threads and safely kills the node.
	 * This will also save the wallet state for the user.
	 */
	// TODO: implement the rest of this
	public void exit() {
		WalletManager.saveWallet(walletPath, wallet);
	}
	
	/*
	 * Creates a new wallet with a private key/ public key
	 * pair. This will also save the key pair to a specified
	 * file location.
	 */
	public void createWallet(String path) {
		wallet = new Wallet();
		walletPath = path;
		WalletManager.saveWallet(path, wallet);
	}
	
	/*
	 * Loads a wallet with a private key/ public key pair.
	 */
	public void loadWallet(String path) {
		walletPath = path;
		wallet = WalletManager.loadWallet(path);
	}
	
	/*
	 * Counts the funds within the linked wallet.
	 */
	public void countFunds() {
		if (walletLoaded()) {
			System.out.println(String.format("Available funds: %d", wallet.availableFunds()));
			System.out.println(String.format("Funds on hold: %d", wallet.fundsOnHold()));
		} 
		else {
			System.out.println("No wallet loaded!");
		}
	}
	
	/*
	 * Rescinds all held funds within the linked wallet.
	 */
	public void rescindHeldFunds() {
		if (walletLoaded()) {
			wallet.rescindHeldFunds();
		} 
		else {
			System.out.println("No wallet loaded!");
		}
	}
	
	/*
	 * Creates and broadcasts a new transaction.
	 */
	public void createTransaction(String recipientKeyPath, float amount) {
		if (walletLoaded()) {
			Transaction transaction = wallet.makeTransaction(WalletManager.loadPublicKey(
															recipientKeyPath, Crypto.GEN_ALGORITHM), 
															amount);
			NetworkService.getNetworkManager().broadcastTransaction(transaction);
		} 
		else {
			System.out.println("No wallet loaded!");
		}
	}
	
	/*
	 * Enables mining within this node.
	 */
	public void enableMining() {
		NetworkService.getNetworkManager().startMining();
		mining = true;
	}
	
	/*
	 * Disables mining within this node.
	 */
	public void disableMining() {
		if (mining) {
			NetworkService.getNetworkManager().stopMining();
			mining = false;
		}
	}
	
	/*
	 * Returns whether the node has been started or not.
	 * This is used to block commands that require the node
	 * to be running first.
	 */
	public boolean started() {
		return started;
	}
	
	/*
	 * Returns whether the node has a wallet loaded for
	 * use. This is used to block commands that require
	 * the node to have a loaded wallet.
	 */
	public boolean walletLoaded() {
		return wallet != null;
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
