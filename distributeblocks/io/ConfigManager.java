package distributeblocks.io;

import distributeblocks.net.IPAddress;
import distributeblocks.net.PeerNode;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Reading and writing to config files goes here.
 */
public class ConfigManager {

	private static final String PEER_CONFIG_FILE = "./peer_config.txt";


	public ConfigManager() {

		// Temporary
		ArrayList<PeerNode> peerNodes = new ArrayList<>();
		peerNodes.add(new PeerNode(new IPAddress("localhost", 5833)));

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

		Gson gson = new Gson();
		File file = new File(PEER_CONFIG_FILE);

		if (!file.exists()){
			file = createPeerConfigFile();
		}

		String json = "";
		IPAddress[] nodes;

		try (Scanner scanner = new Scanner(file)){

			while (scanner.hasNextLine()){
				// Use  stringbuilder maybe.
				json += scanner.nextLine();
			}

			nodes = gson.fromJson(json, IPAddress[].class);

		} catch (Exception e){
			e.printStackTrace();
			throw new RuntimeException("Could not read the peer node config file.");
		}

		if (nodes != null) {

			ArrayList<PeerNode> peerNodes = new ArrayList<PeerNode>();

			for (IPAddress ip : nodes){
				peerNodes.add(new PeerNode(ip));
			}

			return peerNodes;

		} else return new ArrayList<>();
	}


	public void writePeerNodes(ArrayList<PeerNode> peerNodes){

		IPAddress[] peers = new IPAddress[peerNodes.size()];

		for (int i = 0; i < peerNodes.size(); i ++){
			peers[i] = peerNodes.get(i).getListeningAddress();
		}

		Gson gson = new Gson();
		File file = new File(PEER_CONFIG_FILE);

		if (!file.exists()){
			file = createPeerConfigFile();
		}
		
		try (PrintWriter writer = new PrintWriter(file)){
			
			String json = gson.toJson(peers);
			writer.write(json);
			
		} catch (Exception e){
			e.printStackTrace();
			throw new RuntimeException("Could not write to peer node config file");
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


}
