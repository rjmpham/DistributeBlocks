package distributeblocks.net;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.message.AbstractMessage;
import distributeblocks.net.message.RequestPeersMessage;
import distributeblocks.net.message.ServerCrashMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

public class NetworkManager {

	private LinkedBlockingQueue<AbstractMessage> incommingQueue;

	private int minPeers = 0;
	private int maxPeers = Integer.MAX_VALUE;
	private int port = -1;
	private IPAddress seedNodeAddr;
	private boolean seed;

	/**
	 * Time in seconds between attempts at discovering more peer nodes.
	 */
	private int peerSearchRate = 60;

	private ExecutorService executorService;
	private ScheduledExecutorService scheduledExecutorService; // For requesting new peers.
	private ArrayList<PeerNode> peerNodes;
	private ServerSocket serverSocket;


	/**
	 * Flag that the queue processors will use to determine if they should shutdown.
	 */
	private volatile boolean shutDown = false;

	/**
	 *
	 * @param minPeers
	 *   Minimum number of connected peers. Will periodicaly attempt to discover new peers if
	 *   number of connected peers is less than this value.
	 * @param maxPeers
	 *   Maximum number of conencted peers.
	 */
	public NetworkManager(int minPeers, int maxPeers, int port, IPAddress seedNodeAddr, boolean seed){

		this.maxPeers = maxPeers;
		this.minPeers = minPeers;
		this.port = port;
		this.seedNodeAddr = seedNodeAddr;
		this.seed = seed;

		if (seed){
			System.out.println("Starting in seed mode.");
		}

		// May want seperate services to make shutdowns easier?
		executorService = Executors.newCachedThreadPool();
		scheduledExecutorService = Executors.newScheduledThreadPool(1);
		incommingQueue = new LinkedBlockingQueue<>();
	}


	/**
	 * Starts handshake process with known nodes.
	 */
	public void initialize(){


		// Grab known peer nodes from file.
		ConfigManager configManager = new ConfigManager();
		peerNodes = configManager.readPeerNodes();

		// Begin listening for connections, and start the message processor
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not create server socket.");
		}

		executorService.execute(new Connectionlistener());
		executorService.execute(new IncommingQueueProcessor());

		if (!seed) {
			connectToPeers();

			/*if (peerNodes.size() == 0){

				// Use the seed node to get peers.
				PeerNode seedNode = new PeerNode(seedNodeAddr);
				if (!seedNode.connect()){
					throw new RuntimeException("I have no friends and seed node wont talk to me :(");
				}

				seedNode.asyncSendMessage(new RequestPeersMessage());

			} else if (peerNodes.size() < minPeers){

				System.out.println("Asking peers for new friends.");
				// Ask everyone for new neigbors.
				for (PeerNode p : peerNodes){
					p.asyncSendMessage(new RequestPeersMessage());
				}
			}*/

			scheduledExecutorService.schedule(new CheckNeedNodes(), 2, TimeUnit.SECONDS);
		}
	}

	public void addNode(PeerNode node){
		this.peerNodes.add(node);

		ConfigManager manager = new ConfigManager();
		manager.addNodeAndWrite(node);
	}

	public void removeNode(PeerNode node){
		peerNodes.remove(node);

		// Also remove any node with the same address
		for (int i = peerNodes.size() -1; i >= 0; i --){
			if (peerNodes.get(i).getListeningAddress().equals(node.getListeningAddress())){
				peerNodes.remove(i);
			}
		}
	}

	public boolean needMorePeers(){
		return peerNodes.size() < minPeers;
	}

	public boolean inSeedMode(){
		return seed;
	}

	public ArrayList<PeerNode> getPeerNodes(){
		return peerNodes;
	}

	public int getMinPeers() {
		return minPeers;
	}

	public int getPort() {
		return port;
	}

	/**
	 * Add a incomming message to a processing queue.
	 *
	 * This does not block.
	 *
	 * @param message
	 *   The message that is to be processed.
	 */
	public void asyncEnqueue(AbstractMessage message){
		incommingQueue.add(message);
	}

	/**
	 * Connects to a new node.
	 *
	 * NOTE: This is synchronous!
	 *
	 * @param address
	 *
	 * @return
	 *   True on success, false on failure.
	 */
	public boolean connectToNode(IPAddress address){

		PeerNode node = new PeerNode(address);

		if (node.connect()){

			peerNodes.add(node);
			return true;
		}

		return false;
	}

	public boolean isConnectedToNode(IPAddress address){ //TODO: Would be less confusing to use PeerNode?


		for (PeerNode p : peerNodes){
			if (p.getListeningAddress().equals(address)){
				return true;
			}
		}

		return false;
	}

	public boolean isSeedNode(PeerNode node){

		if (node.getListeningAddress().equals(seedNodeAddr)){
			return true;
		}

		return false;
	}

	private void connectToPeers(){

		for (PeerNode p : peerNodes){
			p.asyncConnect();
		}
	}



	/**
	 * Processes the incomming queue.
	 */
	private class IncommingQueueProcessor implements Runnable {


		@Override
		public synchronized void run() {

			while (!shutDown){

				// Process a message!
				AbstractMessage m = null;
				try {
					m = incommingQueue.take();
					m.getProcessor().processMessage(m); // It wasnt supposed to be like this!!!
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Listens for new connections from other nodes. (these dont have to be among the peer list,
	 * maybe its someone who just wants to ask for friends).
	 */
	private class Connectionlistener implements Runnable {

		@Override
		public void run() {

			while (true){

				try {

					System.out.println("Listening for connections!");

					Socket socket = serverSocket.accept();

					//TODO: In seed mode, should terminate connections after some time limit?
					// Doesnt really matter for project I guess.

					System.out.println("Received connection from: " + socket.getInetAddress());

					PeerNode peerNode = new PeerNode(socket);
					peerNodes.add(peerNode);


					// If in seed mode, add the node to the list of known nodes.
					// TODO: Need to do periodic alive checks to these nodes in order to hav a well maintained list.
					if (seed){


					}

				} catch (IOException e) {
					e.printStackTrace();
					asyncEnqueue(new ServerCrashMessage());
				}
			}

		}
	}

	/**
	 * Do be run periodically
	 */
	private class CheckNeedNodes implements Runnable {


		@Override
		public void run() {

			//System.out.println("Checking if I need more friends.");
			if (peerNodes.size() < minPeers){

				//System.out.println("I do");
				System.out.println("Getting more peers.");

				if (peerNodes.size() > 0) {
					for (PeerNode p : peerNodes) {
						p.asyncSendMessage(new RequestPeersMessage());
					}
				} else {
					PeerNode seed = new PeerNode(seedNodeAddr);
					seed.connect();
					seed.asyncSendMessage(new RequestPeersMessage());
				}
			} else {
				//System.out.println("I don't");
			}


			// Consolidate connections. Maybe there could be a way to avoid duplicate connections in the first place?
			HashMap<IPAddress, Boolean> addresses = new HashMap<>();

			for (int i = peerNodes.size() - 1; i >= 0; i --){
				if (addresses.containsKey(peerNodes.get(i).getListeningAddress())){
					peerNodes.remove(i);
				} else {
					addresses.put(peerNodes.get(i).getListeningAddress(), true);
				}
			}

			System.out.println("Connected Nodes: ");
			for (PeerNode p : peerNodes){
				System.out.println(" - " + p.getListeningAddress());
			}

			scheduledExecutorService.schedule(new CheckNeedNodes(), 10, TimeUnit.SECONDS);
		}
	}

}
