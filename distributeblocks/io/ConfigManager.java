package distributeblocks.io;

import com.google.gson.reflect.TypeToken;
import distributeblocks.Block;
import distributeblocks.BlockChain;
import distributeblocks.Node;
import distributeblocks.Transaction;
import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkService;
import distributeblocks.net.PeerNode;
import com.google.gson.Gson;

import java.io.*;
import java.util.*;
import java.security.PublicKey;

/**
 * Reading and writing to config files goes here.
 */
public class ConfigManager {

	//private static final String PEER_CONFIG_FILE = "./peer_config.txt";

	private static final Object peerConfigLock = new Object();
	private static final Object blockChainLock = new Object();
	private static final Object timeoutLock = new Object();
	private static final Object seedListLock = new Object();

	public static String PEER_CONFIG_FILE = "./peer_config.txt";
	public static String PUBLIC_KEY_CONFIG_FILE = "./public_key_config.txt";
	public static String ALIAS_CONFIG_FILE = "./alias_config.txt";
	public static String BLOCKCHAIN_FILE = "./blockchain.txt";
	public static String HUMAN_READABLE= "./readableblockchain.txt";
	public static String TIMEOUT_FILE = "./timeoutfile.txt";
	public static String SEED_FILE = "./seedlist.txt";

	public ConfigManager() {

		// Temporary
		//ArrayList<PeerNode> peerNodes = new ArrayList<>();
		//peerNodes.add(new PeerNode(new IPAddress("localhost", 5833)));

		//writePeerNodes(peerNodes);
	}

	/**
	 * Reads peer node data from a config file.
	 * Creates the config file if it does not exist.
	 * 
	 * @return
	 *   All known peer nodes from the config file.
	 */
	public ArrayList<PeerNode> readPeerNodes(){

		synchronized (peerConfigLock) {

			//Read peers
			Gson gson = new Gson();
			File file = new File(PEER_CONFIG_FILE);

			if (!file.exists()) {
				file = createPeerConfigFile();
			}

			String json = "";
			IPAddress[] peers;

			try (Scanner scanner = new Scanner(file)) {

				while (scanner.hasNextLine()) {
					// Use  stringbuilder maybe.
					json += scanner.nextLine();
				}

				peers = gson.fromJson(json, IPAddress[].class);

			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Could not read the peer node config file.");
			}
			
			//Read aliases
			gson = new Gson();
			file = new File(ALIAS_CONFIG_FILE);

			if (!file.exists()) {
				file = createAliasConfigFile();
			}

			json = "";
			String[] aliases;

			try (Scanner scanner = new Scanner(file)) {

				while (scanner.hasNextLine()) {
					// Use  stringbuilder maybe.
					json += scanner.nextLine();
				}

				aliases = gson.fromJson(json, String[].class);

			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Could not read the alias node config file.");
			}

			if (peers != null) {

				ArrayList<PeerNode> peerNodes = new ArrayList<PeerNode>();

				for (int i = 0; i < peers.length; i++) {
					PeerNode peer = new PeerNode(peers[i]);
					peer.setAlias(aliases[i]);
					peerNodes.add(peer);
				}

				return peerNodes;

			} else return new ArrayList<>();
		}
	}


	public void writePeerNodes(ArrayList<PeerNode> peerNodes){


		synchronized (peerConfigLock) {

			IPAddress[] peers = new IPAddress[peerNodes.size()];
			PublicKey[] publicKeys = new PublicKey[peerNodes.size()];
			String[] aliases = new String[peerNodes.size()];
			
			Console.log("CM1: " + peerNodes.size());

			for (int i = 0; i < peerNodes.size(); i++) {
				PeerNode node = peerNodes.get(i);
				peers[i] = node.getListeningAddress();
				publicKeys[i] = node.getPublicKey();
				aliases[i] = node.getAlias();
			}

			//Write peers
			Gson gson = new Gson();
			File file = new File(PEER_CONFIG_FILE);

			if (!file.exists()) {
				file = createPeerConfigFile();
			}

			try (PrintWriter writer = new PrintWriter(file)) {

				String json = gson.toJson(peers);
				writer.write(json);

			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Could not write to peer node config file");
			}
			
			//Write aliases
			gson = new Gson();
			file = new File(ALIAS_CONFIG_FILE);

			if (!file.exists()) {
				file = createAliasConfigFile();
			}

			try (PrintWriter writer = new PrintWriter(file)) {

				String json = gson.toJson(aliases);
				writer.write(json);

			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Could not write to alises node config file");
			}
			
		}
		
	}


	/**
	 * Adds a node to the list of peer nodes,
	 * if the node already exists in the list, then does nothing.
	 *
	 * @param node
	 */
	public void addNodeAndWrite(PeerNode node){

		synchronized (peerConfigLock) {

			ArrayList<PeerNode> nodes = this.readPeerNodes();

			Console.log("Adding node: " + node.getListeningAddress());

			boolean found = false;
			for (PeerNode n : nodes) {
				if (n.equals(node)) {
					found = true;
					break;
				}
			}

			if (!found) {

				if (NetworkService.getNetworkManager().inSeedMode()) {
					node.setAddress(node.getLocalAddress());
				}

				nodes.add(node);
				this.writePeerNodes(nodes);
			}
		}
	}

	/**
	 * Removes the node from the config list and writes.
	 * If node isnt in list, does nothing.
	 *
	 * @param node
	 */
	public void removeNodeAndWrite(PeerNode node){

		synchronized (peerConfigLock) {
			ArrayList<PeerNode> nodes = this.readPeerNodes();
			nodes.remove(node);
			this.writePeerNodes(nodes);
		}
	}


	/**
	 * This takes in a block chain file and saves it to a file. Further, it takes the longest blockchain and does a
	 * one directional save to a file
	 * @param blockChain
	 */

	public synchronized void saveBlockChain(ArrayList<LinkedList<Block>> blockChain, LinkedList<Block> longestChain){

		synchronized (blockChainLock) {

			//Gson gson = new Gson();
			//TODO check if this file is needed
			File file = new File(BLOCKCHAIN_FILE);

			try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(BLOCKCHAIN_FILE))) {

				//String json = gson.toJson(blockChain);
				out.writeObject(blockChain);


			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not save blockchain to file.");
			} catch (IOException e) {
				e.printStackTrace();
			}


			/*
			 * This particular part saves the longest blockchain into a human readable form into a file
			 */


			//TODO Eric look at this 2 electric boogaloo
			File file2 = new File(HUMAN_READABLE);
			try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(HUMAN_READABLE))) {


				String outString = new String();

				//For each block make a section with the block information
				for( int i = longestChain.size()-1 ; i>= 0 ; i--){
					Block currentBlock = longestChain.get(i);
					HashMap<String, Transaction> blockTransactions = currentBlock.getData();

					outString += "========================================================================\n";
					outString += "Block number: " + i + '\n';

					outString += "The current block hash is: \n"+currentBlock.getHashBlock() + '\n';
					outString += "The previous block hash is: \n"+currentBlock.getHashPrevious() +'\n';
					outString += "========================================================================\n";
					outString += "TRANSACTIONS: \n";
					outString += "========================================================================\n";

					//For each transaction on a block make a section that represents the to and from transaction
					for (String id : blockTransactions.keySet())
					{
						outString += "\n";
						outString += "From: \n" +blockTransactions.get(id).getPublicSender()+"\n";
						outString += "To:   \n" +blockTransactions.get(id).getPublicReceiver()+"\n";
						outString += "Amount: " +blockTransactions.get(id).getExchangeAmmountString()+"\n";
					}

				}
				out.writeObject(outString);


			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not save blockchain to file.");
			} catch (IOException e) {
				e.printStackTrace();
			}


		}

	}

	public synchronized ArrayList<LinkedList<Block>> loadBlockChain(){

		synchronized (blockChainLock) {

			Gson gson = new Gson();
			File file = new File(BLOCKCHAIN_FILE);

			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("Could not create blockchain file!");
				}

				// Create new chain with genisis node.
				ArrayList<LinkedList<Block>> chain = new ArrayList<>();
				LinkedList newFork = new LinkedList();


				newFork.add(Block.getGenisisBlock());
				chain.add(newFork);
				saveBlockChain(chain,chain.get(0));

				//save(generateTestChain()); // TESTING ONLY.
			}

			String json = "";
			ArrayList<LinkedList<Block>> blockChain = new ArrayList<>();

			try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(BLOCKCHAIN_FILE))) {

				blockChain = (ArrayList<LinkedList<Block>>) in.readObject();
				return blockChain;

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException("could not read the blockchain file.");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			return null;
		}
	}


	/**
	 * Tries to create the peer config file.
	 *
	 * @return
	 *   File object for the peer config file.
	 */
	private File createPeerConfigFile(){

		File  file = new File(PEER_CONFIG_FILE);

		if (file.exists()){
			return file;
		}

		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// We dont need anything fancier than this..
		if (!file.exists()){
			throw new RuntimeException("Could not create peer config file.");
		}

		return file;
	}
	
	/**
	 * Tries to create the public key config file.
	 *
	 * @return
	 *   File object for the public key config file.
	 */
	private File createPublicKeyConfigFile(){

		File  file = new File(PUBLIC_KEY_CONFIG_FILE);

		if (file.exists()){
			return file;
		}

		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// We dont need anything fancier than this..
		if (!file.exists()){
			throw new RuntimeException("Could not create public key config file.");
		}

		return file;
	}
	
	/**
	 * Tries to create the alias config file.
	 *
	 * @return
	 *   File object for the alias config file.
	 */
	private File createAliasConfigFile(){

		File  file = new File(ALIAS_CONFIG_FILE);

		if (file.exists()){
			return file;
		}

		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// We dont need anything fancier than this..
		if (!file.exists()){
			throw new RuntimeException("Could not create alias config file.");
		}

		return file;
	}


	/**
	 * Creates a timeout file if one does not exist.
	 *
	 * This file is used be seeds to store the time that it last received
	 * a connection from each node.
	 *
	 * @return
	 */
	private File createTimeoutFile(){


		File  file = new File(TIMEOUT_FILE);

		if (file.exists()){
			return file;
		}

		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// We dont need anything fancier than this..
		if (!file.exists()){
			throw new RuntimeException("Could not create timeout file.");
		}

		return file;
	}

	public void writeTimeoutFile(HashMap<String, Long> times){


		synchronized (timeoutLock){

			File file = new File(TIMEOUT_FILE);

			if (!file.exists()){

				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					Console.log("Could not create timeout file.");
					return;
				}
			}

			Gson gson = new Gson();

			try (PrintWriter writer = new PrintWriter(file)){
				writer.write(gson.toJson(times));
			} catch (FileNotFoundException e) {
				Console.log("Could not write timeout file.");
			}

		}
	}

	/**
	 * See writeTimeoutFile() for info on what the file is for.
	 *
	 * @return
	 */
	public HashMap<String, Long> readTimeoutFile(){


		synchronized (timeoutLock){

			File file = new File(TIMEOUT_FILE);

			if (!file.exists()){

				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					Console.log("Could not create timeout file.");
					return null;
				}
			}

			Gson gson = new Gson();
			String json = "";

			try (Scanner scanner = new Scanner(file)){

				while (scanner.hasNextLine()){
					json += scanner.nextLine();
				}


				HashMap<String, Long> data = gson.fromJson(json, new TypeToken<HashMap<String, Long>>(){}.getType());
				return data == null ? new HashMap<String, Long>() : data;

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}


		}

		return null;
	}


	/**
	 * Writes to the seed nodes list file. The file that has a list of seed nodes.
	 *
	 * @return
	 */
	public void writeSeedNodes(ArrayList<IPAddress> seedNodes){

		synchronized (seedListLock){

			File file = new File(SEED_FILE);

			if(!file.exists()){
				try {
					file.createNewFile();
				} catch (IOException e) {
					Console.log("Could not create the seed list file.");
					return;
				}
			}

			Gson gson = new Gson();

			try (PrintWriter writer = new PrintWriter(file)){

				writer.write(gson.toJson(seedNodes));

			} catch (FileNotFoundException e) {
				Console.log("Could not write to seed file.");
			}

		}

	}

	/**
	 *
	 * Read seed nodes from the seed node file.
	 *
	 * @return
	 */
	public ArrayList<IPAddress> readSeedNodes(){

		synchronized (seedListLock){

			ArrayList<IPAddress> seedNodes = new ArrayList<>();
			File file = new File(SEED_FILE);

			if(!file.exists()){
				return new ArrayList<>();
			}

			Gson gson = new Gson();
			String json = "";

			try (Scanner scanner = new Scanner(file)){

				while (scanner.hasNextLine()){
					json += scanner.nextLine();
				}

				seedNodes = gson.fromJson(json, new TypeToken<ArrayList<IPAddress>>(){}.getType());

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}



			return  seedNodes;
		}

	}
	

}
