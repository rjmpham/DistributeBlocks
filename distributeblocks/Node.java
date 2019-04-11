package distributeblocks;

import distributeblocks.crypto.Crypto;
import distributeblocks.cli.CommandLineInterface;
import distributeblocks.io.WalletManager;
import distributeblocks.net.NetworkConfig;
import distributeblocks.net.NetworkService;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

// TODO: normalize our Console.log() statements. Some of them seem unprofessional 
// TODO: normalize our documentation across the system. Some of them seem unprofessional
// TODO: normalize the use of whitespace throughout the codebase
//			DON'T FORGET: coding style and consistency IS FOR MARKS

/* TODO: THIS IS A BIG ONE:
 *		We need some way to get other node's public keys and save them to files.
 *		Without this, we won't be able to send anyone money. Even if this is
 *		just send each other's PKs through some file share, we need to set this
 *		up for the demo.
 */

// TODO: the "coinBase" transaction id may cause problems with hashes and removing old funds that are marked as spent
// TODO: replace all file slashes with File.separator
// TODO: make killing mining sychronized with the new block broadcast, or make sure it doesn't kill it part way through
/**
 *  Represents an agent within the P2P network. This class houses a wallet,
 *  and may run all the thread necessary to perform network actions.
 *  
 *	Run the main method to start up a node and interact with the network.
 */
public class Node {

	public static int HASH_DIFFICULTY = 4;
	public static String DEFAULT_WALLET_DIR = "./wallet/";

	private boolean started = false;
	private boolean mining = false;
	private Wallet wallet;
	private String walletPath;

	/**
	 * Starts up the network threads and marks the node as started.
	 * 
	 * @param config	data container of network configuration
	 */
	public void initializeNetworkService(NetworkConfig config) {
		NetworkService.init(config);
		started = true;
		System.out.println("Node successfully started");
	}

	/**
	 * Closes all threads and safely kills the node.
	 * This will also save the wallet state for the user.
	 */
	public void exit() {
		System.out.println("Exiting program");
		if (wallet != null){
			try {
				WalletManager.saveWallet(walletPath, wallet);
			} catch (IOException e) {
				System.out.println("Failed to save wallet to " + walletPath);
			}
		}
		// TODO: do we need to safely close all other threads?
		System.exit(0);

	}

	/**
	 * Creates a new wallet with a private key/ public key
	 * pair. This will also save the key pair to a specified
	 * file location.
	 * 
	 * @param path		path to the directory where wallet info will be stored
	 * @param alias		Alias associated with the wallet
	 */
	public void createWallet(String path, String alias) {
		// ensure that it is safe to save wallet data to the directory
		String fullPath = System.getProperty("user.dir") + path;
		File file = new File(fullPath);
		if(!file.isDirectory()) {
				file.mkdir();
		}
		if(file.list().length != 0){
			System.out.println("Cannot create a wallet in a non-empty directory " + fullPath);
			return;
		}
		
		// keep a copy of the current wallet in case creation fails
		Wallet old = wallet;
		wallet = new Wallet(alias);
		walletPath = fullPath;
		
		boolean failed = false;
		try {
			WalletManager.saveWallet(path, wallet);
		} catch (IOException e) {
			System.out.println("Failed to save new wallet in " + fullPath);
			System.out.println("keeping previously wallet (if any)");
			failed = true;
			wallet = old;
		}
		if (!failed) {
			System.out.println("Successfully created wallet in " + fullPath);
		}
	}

	/**
	 * Loads a wallet with a private key/ public key pair.
	 * 
	 * @param path		path to the directory where wallet info is stored
	 */
	public void loadWallet(String path) {
		String fullPath = System.getProperty("user.dir") + path;
		
		// keep a copy of the current wallet in case creation fails
		Wallet old = wallet;
		
		try {
			wallet = WalletManager.loadWallet(fullPath);
			walletPath = fullPath;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | ClassNotFoundException e) {
			System.out.println("Failed to load wallet from " + fullPath);
			System.out.println("keeping previously wallet (if any)");
			wallet = old;
		}
	}
	
	/**
	 * Loads a default wallet on startup. This will first try
	 * to load a wallet out of the default dir. If that fails, it
	 * will try to create a new default wallet. If THAT fails, it
	 * will warn the user that no wallet has been loaded.
	 */
	private void loadDefaultWallet() {
		loadWallet(DEFAULT_WALLET_DIR);
		if (wallet == null)
			createWallet(DEFAULT_WALLET_DIR);
		if (wallet == null) {
			System.out.println("Warning: no default wallet loaded!");
		}else {
			System.out.println("Default wallet loaded");
		}
	}

	/**
	 * Counts the funds within the linked wallet.
	 * Prints to stdout.
	 */
	public void countFunds() {
		if (! walletLoaded()) {
			System.out.println("No wallet loaded!");
			return;
		}

		System.out.println(String.format("Available funds: %f", wallet.availableFunds()));
		System.out.println(String.format("Funds on hold: %f", wallet.fundsOnHold()));
	}

	/**
	 * Rescinds all held funds within the linked wallet.
	 */
	public void rescindHeldFunds() {
		if (!walletLoaded()) {
			System.out.println("No wallet loaded!");
			return;
		}
		wallet.rescindHeldFunds();
		System.out.println("On hold funds have been returned to the wallet");
	}

	/**
	 * Updates the node's wallet by clearing out any
	 * held funds which were waiting to be verified, and
	 * updating how much money it has from all the transactions
	 * on the block.
	 * 
	 * This method is called whenever a block becomes verified (sufficiently deep).
	 * 
	 * @param block	the most recently verified block of the longest chain
	 */
	public void updateWallet(Block block) {
		if (block == null)
			return;
					
		System.out.println("New verified block added. Updating local funds");
		// Process all the transactions in the block
		HashMap<String, Transaction> blockData = block.getData();
		for (Map.Entry<String, Transaction> i: blockData.entrySet()){
			wallet.update(i.getValue());
		}
	}
	
	/**
	 * Creates and broadcasts a new transaction.
	 * 
	 * This method may fail if a wallet has not been loaded, or
	 * the node has not yet been started (and has no connection to
	 * the network). If the intended transaction is invalid, the 
	 * operation will be aborted and used funds returned.
	 * 
	 * @param recipientKeyPath	path to a users PK (public.key)
	 */
	public void createTransaction(String recipientKeyPath, float amount) {
		if (!walletLoaded()) {
			System.out.println("No wallet loaded!");
			return;
		}
		else if (!started) {
			System.out.println("Node must be started first!");
			return;
		}

		String fullPath = System.getProperty("user.dir") + recipientKeyPath;
		try {
			// try to create a transaction
			PublicKey recipientKey = WalletManager.loadPublicKey(fullPath, Crypto.GEN_ALGORITHM);
			Transaction transaction = wallet.makeTransaction(recipientKey, amount);
			if(transaction.transactionEnforcer()){
				NetworkService.getNetworkManager().addTransaction(transaction);
				System.out.println("Transaction request has been made");
			} else{
				wallet.reverseTransaction(transaction);
				System.out.println("Transaction request denied");
			}

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			System.out.println("Error: could not load KeyPair from " + fullPath);
		} catch (IOException e) {
			System.out.println("Error: no KeyPair files found in path " + fullPath);
		}
	}

	/**
	 * Enables mining within this node.
	 */
	public void enableMining() {
		if (!started) {
			System.out.println("Node must be started first!");
			return;
		}
		NetworkService.getNetworkManager().startMining();
		mining = true;
		System.out.println("mining has been enabled");
	}

	/**
	 * Disables mining within this node.
	 */
	public void disableMining() {
		if (!started) {
			System.out.println("Node must be started first!");
			return;
		}
		if (mining) {
			NetworkService.getNetworkManager().stopMining();
			mining = false;
		}
		System.out.println("mining has been disabled");
	}

	/**
	 * Returns whether the node has been started or not.
	 * This is used to block commands that require the node
	 * to be running first.
	 * 
	 * @return	boolean indicated whether the node is on the network
	 */
	public boolean started() {
		return started;
	}

	/**
	 * Returns whether the node has a wallet loaded for
	 * use. This is used to block commands that require
	 * the node to have a loaded wallet.
	 * 
	 * @return	boolean indicated whether a wallet is loaded 
	 */
	public boolean walletLoaded() {
		return wallet != null;
	}
  
	// Getter methods
	public Wallet getWallet() {
		return wallet;
	}

	/**
	 * Main method to run a node on the system.
	 * This will start the node as well as the CLI.
	 * 
	 * @param args		command line args
	 */
	public static void main (String[] args){
		// Initialize this node
		Node node = new Node();
		NodeService.init(node);
		node.loadDefaultWallet();

		// Parse initial args then run the cli
		CommandLineInterface cli = new CommandLineInterface(node);
		cli.parseCommand(args);
		cli.run();
	}
}
