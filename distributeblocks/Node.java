package distributeblocks;

import distributeblocks.crypto.Crypto;
import distributeblocks.cli.CommandLineInterface;
import distributeblocks.io.WalletManager;
import distributeblocks.net.NetworkConfig;
import distributeblocks.net.NetworkService;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

// TODO: normalize our Console.log() statements. Some of them seem unprofessional 
// TODO: normalize our documentation across the system. Some of them seem unprofessional
//			DON'T FORGET: coding style  and consistency IS FOR MARKS

/* TODO: THIS IS A BIG ONE:
 *		We need some way to get other node's public keys and save them to files.
 *		Without this, we won't be able to send anyone money. Even if this is
 *		just send each other's PKs through some file share, we need to set this
 *		up for the demo.
 */

/* TODO: ALSO A BIG ONE:
 * 		We need a way for the network manager to call our wallet methods to
 * 		receive funds and clear out onHold once a block gets to be 6 deep.
 */
public class Node {

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
	 * Updates the node's wallet by clearing out any
	 * held funds which were waiting to be verified, and
	 * updating how much money it has from all the transactions
	 * on the block.
	 * 
	 * This method is called whenever a block becomes 6 deep from the head of the chain,
	 * and all transactions on the block are considered verified. 
	 */
	// TODO: call this when a block becomes 6 deep from the head
	public void updateWallet(Block block) {
		HashMap<String, Transaction> blockData = block.getData();
		for (Map.Entry<String,Transaction> i: blockData.entrySet()){
			wallet.update(i.getValue());
		}
	}
	
	/*
	 * Creates and broadcasts a new transaction.
	 * 
	 * This method may fail if a wallet has not been loaded, or
	 * the node has not yet been started (and has no connection to
	 * the network). If the intended transaction is invalid, the 
	 * operation will be aborted and used funds returned.
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
				wallet.reverseTransaction(transaction);
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
  
	/*
	 * Getter method for the node's wallet.
	 */
	public Wallet getWallet() {
		return wallet;
	}

	/*
	 * Main method to run a node on the system.
	 * This will start the node as well as the CLI.
	 */
	public static void main (String[] args){
		// Initialize this node
		Node node = new Node();
		NodeService.init(node);

		// Parse initial args then run the cli
		CommandLineInterface cli = new CommandLineInterface(node);
		cli.parseCommand(args);
		cli.run();
	}
}
