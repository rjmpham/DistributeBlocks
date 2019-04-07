package distributeblocks;

import distributeblocks.crypto.Crypto;
import distributeblocks.cli.CommandLineInterface;
import distributeblocks.io.Console;
import distributeblocks.io.WalletManager;
import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkConfig;
import distributeblocks.net.NetworkService;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

/* TODO: THIS IS A BIG ONE:
 *		We need some way to get other node's public keys and save them to files.
 *		Without this, we won't be able to send anyone money.
 */

/* TODO: ALSO A BIG ONE:
 * 		We need a way for the network manager to call our wallet methods to
 * 		receive funds and clear out onHold once a block gets to be 6 deep.
 */
public class Node {

	public static int HASH_DIFFICULTY = 4;
	public static int MONITOR_PORT = 7329;
	
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
	public void exit() {
		if (wallet != null){
			WalletManager.saveWallet(walletPath, wallet);
		}
		// TODO: do we need to safely close all other threads?
		System.exit(0);
		
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
		if (! walletLoaded()) {
			System.out.println("No wallet loaded!");
			return;
		}
			
		System.out.println(String.format("Available funds: %f", wallet.availableFunds()));
		System.out.println(String.format("Funds on hold: %f", wallet.fundsOnHold()));
	}
	
	/*
	 * Rescinds all held funds within the linked wallet.
	 */
	public void rescindHeldFunds() {
		if (!walletLoaded()) {
			System.out.println("No wallet loaded!");
			return;
		} 
		wallet.rescindHeldFunds();
	}
	
	/*
	 * Creates and broadcasts a new transaction.
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

		try {
			PublicKey recipientKey = WalletManager.loadPublicKey(System.getProperty("user.dir") + recipientKeyPath,
																Crypto.GEN_ALGORITHM);
			Transaction transaction = wallet.makeTransaction(recipientKey, amount);
			if(transaction.transactionEnforcer()){
				System.out.println("Transaction request has been made");
				NetworkService.getNetworkManager().broadcastTransaction(transaction);
			} else{
				//TODO error messages, one for key, one for amount, one for else
				System.out.println("Transaction request denied");
			}
			
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			System.out.println("Error: could not load KeyPair");
		} catch (IOException e) {
			System.out.println("Error: no KeyPair files found in path " + recipientKeyPath);
		}
	}
	
	/*
	 * Enables mining within this node.
	 */
	public void enableMining() {
		if (!started) {
			System.out.println("Node must be started first!");
			return;
		}
		NetworkService.getNetworkManager().startMining();
		mining = true;
	}
	
	/*
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
		//snew BlockChain(); // Load the chain (generates the file).
	}

	public Wallet getWallet() {
		return wallet;
	}

	public static void main (String[] args){		
		// Initialize this node
		Node node = new Node();
		NodeService.init(node);

		if (args.length == 1 && args[0].equals("--monitor")){
			startMonitor();
			return;
		}
		
		// Parse initial args then run the cli
		CommandLineInterface cli = new CommandLineInterface(node);
		cli.parseCommand(args);
		cli.run();
	}

	private static void startMonitor(){


		NetworkMonitor networkMonitor = new NetworkMonitor();

	}
}
