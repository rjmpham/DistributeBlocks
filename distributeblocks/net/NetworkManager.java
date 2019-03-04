package distributeblocks.net;

import distributeblocks.Block;
import distributeblocks.BlockHeader;
import distributeblocks.Node;
import distributeblocks.io.ConfigManager;
import distributeblocks.net.message.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class NetworkManager {

	private LinkedBlockingQueue<AbstractMessage> incommingQueue;
	private LinkedBlockingQueue<ArrayList<BlockHeader>> headerQueue;
	private LinkedBlockingQueue<BlockMessage> blockQueue;

	private AquireChain aquireChain; // TODO: Replace with events to make this not awfull?

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
	private List<PeerNode> peerNodes;
	private ServerSocket serverSocket;


	/**
	 * Flag that the queue processors will use to determine if they should shutdown.
	 */
	private volatile boolean shutDown = false;

	/**
	 * @param minPeers Minimum number of connected peers. Will periodicaly attempt to discover new peers if
	 *                 number of connected peers is less than this value.
	 * @param maxPeers Maximum number of conencted peers.
	 */
	public NetworkManager(int minPeers, int maxPeers, int port, IPAddress seedNodeAddr, boolean seed) {

		this.maxPeers = maxPeers;
		this.minPeers = minPeers;
		this.port = port;
		this.seedNodeAddr = seedNodeAddr;
		this.seed = seed;

		if (seed) {
			System.out.println("Starting in seed mode.");
		}

		// May want seperate services to make shutdowns easier?
		executorService = Executors.newCachedThreadPool();
		scheduledExecutorService = Executors.newScheduledThreadPool(1);
		incommingQueue = new LinkedBlockingQueue<>();
		headerQueue = new LinkedBlockingQueue<>();
		blockQueue = new LinkedBlockingQueue<>();
	}


	/**
	 * Starts handshake process with known nodes.
	 */
	public void initialize() {


		// Grab known peer nodes from file.
		ConfigManager configManager = new ConfigManager();
		peerNodes = Collections.synchronizedList(configManager.readPeerNodes());

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

			// Now request header info from everyone since we restarted (or started for the first time).
			this.aquireChain = new AquireChain();
			executorService.execute(this.aquireChain);
		}
	}

	public void addNode(PeerNode node) {
		
		synchronized (peerNodes) {

			this.peerNodes.add(node);

			ConfigManager manager = new ConfigManager();
			manager.addNodeAndWrite(node);
		}
	}


	/**
	 * Removes a node by reference.
	 *
	 * @param node
	 */
	public void removeNode(PeerNode node) {

		synchronized (peerNodes) {

			for (int i = 0; i < peerNodes.size(); i++) {
				if (peerNodes.get(i) == node) { // At least I think this only checks if the reference is the same.
					peerNodes.get(i).shutDown();
					peerNodes.remove(i);
					break;
				}
			}

		}
	}

	/**
	 * Remove node by index.
	 *
	 * @param index
	 */
	public synchronized void removeNode(int index) {

		synchronized (peerNodes) {
			peerNodes.get(index).shutDown();
			peerNodes.remove(index);
		}
	}

	/**
	 * Remove all nodes with the given address.
	 *
	 *
	 */
	/*public synchronized void removeNode(IPAddress listeningAddress) {

		ArrayList<Integer> toRemove = new ArrayList<>();

		for (int i = 0; i < peerNodes.size(); i++) {

			if (peerNodes.get(i).getListeningAddress().equals(listeningAddress)) {
				toRemove.add(i);
			}
		}

		for (int rem : toRemove) {
			peerNodes.remove(rem);
		}
	}*/

	public void removeDuplicateNodes() {

		synchronized (peerNodes) {
			HashMap<IPAddress, Boolean> addresses = new HashMap<>();
			ArrayList<Integer> toRemove = new ArrayList<>();

			for (int i = peerNodes.size() - 1; i >= 0; i--) {
				if (addresses.containsKey(peerNodes.get(i).getListeningAddress())) {
					toRemove.add(i);
				} else {
					addresses.put(peerNodes.get(i).getListeningAddress(), true);
				}
			}

			for (int i : toRemove) {
				System.out.println("Removing duplicate node: " + peerNodes.get(i).getListeningAddress());
				removeNode(i);
			}
		}
	}

	public synchronized void printConnectedNodes() {
		System.out.println("Connected Nodes: ");
		for (PeerNode p : getPeerNodes()) {
			System.out.println(" - " + p.getListeningAddress());
		}
	}


	public boolean needMorePeers() {
		return getPeerNodes().size() < minPeers;
	}

	public boolean inSeedMode() {
		return seed;
	}

	public List<PeerNode> getPeerNodes() {
		
		synchronized (peerNodes){
			
			ArrayList<PeerNode> copy = new ArrayList<>(peerNodes);
			return copy;
		}
	}

	public int getMinPeers() {
		return minPeers;
	}

	public int getPort() {
		return port;
	}

	public void addBlockHeader(ArrayList<BlockHeader> blockHeader) {
		headerQueue.add(blockHeader);
	}

	public void gotBlock(BlockMessage blockMessage){
		blockQueue.add(blockMessage);

		if (this.aquireChain != null){
			this.aquireChain.gotBlock(blockMessage);
		}
	}

	/**
	 * Add a incomming message to a processing queue.
	 * <p>
	 * This does not block.
	 *
	 * @param message The message that is to be processed.
	 */
	public void asyncEnqueue(AbstractMessage message) {
		incommingQueue.add(message);
	}

	/**
	 * Connects to a new node.
	 * <p>
	 * NOTE: This is synchronous!
	 *
	 * @param address
	 * @return True on success, false on failure.
	 */
	public boolean connectToNode(IPAddress address) {

		PeerNode node = new PeerNode(address);

		if (node.connect()) {
			
			addNode(node); // TODO: This may ahve caused issues with cfg file.

			return true;
		}

		return false;
	}

	public boolean isConnectedToNode(IPAddress address) { //TODO: Would be less confusing to use PeerNode?


		for (PeerNode p : getPeerNodes()) {
			if (p.getListeningAddress().equals(address)) {
				return true;
			}
		}

		return false;
	}

	public boolean isSeedNode(PeerNode node) {

		if (node.getListeningAddress().equals(seedNodeAddr)) {
			return true;
		}

		return false;
	}

	private void connectToPeers() {

		for (PeerNode p : getPeerNodes()) {
			p.connect();
		}
	}


	/**
	 * Processes the incomming queue.
	 */
	private class IncommingQueueProcessor implements Runnable {


		@Override
		public synchronized void run() {

			while (!shutDown) {

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

			while (true) {

				try {

					System.out.println("Listening for connections!");

					Socket socket = serverSocket.accept();

					//TODO: In seed mode, should terminate connections after some time limit?
					// Doesnt really matter for project I guess.

					System.out.println("Received connection from: " + socket.getInetAddress());

					PeerNode peerNode = new PeerNode(socket);
					peerNodes.add(peerNode);


					// TODO: Need to do periodic alive checks to these nodes in order to hav a well maintained list.

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

			try {

				List<PeerNode> nodes = getPeerNodes();
				
				//System.out.println("Checking if I need more friends.");
				if (nodes.size() < minPeers) {

					//System.out.println("I do");
					System.out.println("Getting more peers.");

					if (nodes.size() > 0) {
						for (PeerNode p : nodes) {
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
				/**/

				//removeDuplicateNodes();
				printConnectedNodes();


			} catch (Exception e) {
				e.printStackTrace();
				// Not sure whats going on here.
			}

			scheduledExecutorService.schedule(new CheckNeedNodes(), 10, TimeUnit.SECONDS);
		}
	}


	/**
	 * Aquires the chain woo.
	 */
	private class AquireChain implements Runnable {


		private HashMap<Integer, Long> requestTimes;
		private volatile int leftBound = 0;
		private volatile int rightBound = 10;

		private HashMap<Integer, Boolean> recievedBlocks;


		public AquireChain() {
			this.requestTimes = new HashMap<>();
			recievedBlocks = new HashMap<>();
		}

		public void gotBlock(BlockMessage blockMessage){
			recievedBlocks.put(blockMessage.blockHeight, true);

			// Update bounds.
			int min = Integer.MAX_VALUE;
			for (int i : recievedBlocks.keySet()){
				if (min > i){
					min = i;
				}
			}

			leftBound = min;
			rightBound = min + 10;
		}


		@Override
		public void run() {


			requestHeaders();

			try {


				Thread.sleep(5000);

				// Waiting a bit to get the responses.
				while (true) {

					if (headerQueue.isEmpty()) {
						System.out.println("Waiting for header info ...");
						Thread.sleep(5000);
						requestHeaders();
					} else {
						break;
					}
				}


				// Now that we got some headers, lets see what the highest block is.
				ArrayList<BlockHeader> highestHeaders = new ArrayList<>(); // A little janky.

				for (ArrayList<BlockHeader> headers : headerQueue){

					if (highestHeaders.size() < headers.size()){
						highestHeaders = headers;
					}
				}

				var temp = Node.getBlockchain();

				if (highestHeaders.size() <= Node.getBlockchain().size()){
					System.out.println("Already have the highest chain");
					return;
				}

				System.out.println("Aquireing blockchain with height: " + highestHeaders.size());

				// Now that we have identified the longest list of blockHeaders, request them all.
				Random rand = new Random();
				List<PeerNode> nodes = getPeerNodes();
				int i = 0;

				for (int j = 0; j < highestHeaders.size(); j ++){
					requestTimes.put(j, (long) 0.0); // too tired.
				}

				while (recievedBlocks.size() < highestHeaders.size()) {

					if (i > rightBound) {
						Thread.sleep(1000); // Wait for responses.

						if (requestTimes.get(i) + 5000 <  System.currentTimeMillis()) { // Only make another request every 5 seconds
							// Request lowest block again.
							nodes.get(nodes.size() == 1 ? 0 : rand.nextInt(nodes.size() - 1)).asyncSendMessage(new RequestBlockMessage(highestHeaders.get(leftBound).blockHash, leftBound));
							requestTimes.put(i, System.currentTimeMillis());
						}
					} else {


						if (requestTimes.get(i) + 5000 <  System.currentTimeMillis()) { // Only make another request every 5 seconds
							nodes.get(nodes.size() == 1 ? 0 : rand.nextInt(nodes.size() - 1)).asyncSendMessage(new RequestBlockMessage(highestHeaders.get(i).blockHash, i));
							requestTimes.put(i, System.currentTimeMillis());
						}

						if (i < highestHeaders.size() - 1){
							i ++;
						}
					}

				}

				System.out.println("AQUIRED THE BLOCKCHAIN!");

				// Process blocks into a chain and save wooo!
				LinkedList<Block> blockChain = new LinkedList<>();

				for (int j = 0; j < highestHeaders.size(); j ++) {

					for (BlockMessage m : blockQueue) {

						if (m.blockHeight == j) {
							blockChain.add(m.block);
							break;
						}
					}
				}

				ConfigManager configManager = new ConfigManager();
				configManager.saveBlockChain(blockChain);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		private void requestHeaders(){

			System.out.println("Requesting headers.");
			List<PeerNode> nodes  = getPeerNodes();
			for (PeerNode n : nodes) {
				n.asyncSendMessage(new RequestHeadersMessage());
			}
		}
	}
}
