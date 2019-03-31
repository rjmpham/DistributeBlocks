package distributeblocks.net;

import distributeblocks.Block;
import distributeblocks.BlockChain;
import distributeblocks.BlockHeader;
import distributeblocks.Node;
import distributeblocks.io.ConfigManager;
import distributeblocks.mining.Miner;
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

	private volatile AquireChain aquireChain; // TODO: Replace with events to make this not awfull?

	private int minPeers = 0;
	private int maxPeers = Integer.MAX_VALUE;
	private int port = -1;
	private IPAddress seedNodeAddr;
	private boolean seed;
	private boolean mining;

	/**
	 * Time in seconds between attempts at discovering more peer nodes.
	 */
	private int peerSearchRate = 60;

	private ExecutorService executorService;
	private ScheduledExecutorService scheduledExecutorService; // For requesting new peers.
	private List<PeerNode> peerNodes;
	private ServerSocket serverSocket;

	private Miner miner;


	/**
	 * Flag that the queue processors will use to determine if they should shutdown.
	 */
	private volatile boolean shutDown = false;

	/**
	 */
	public NetworkManager(NetworkConfig networkConfig) {

		this.maxPeers = networkConfig.maxPeers;
		this.minPeers = networkConfig.minPeers;
		this.port = networkConfig.port;
		this.seedNodeAddr = networkConfig.seedNode;
		this.seed = networkConfig.seed;
		this.mining = networkConfig.mining;
		this.miner = new Miner();

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



			scheduledExecutorService.schedule(new CheckNeedNodes(), 2, TimeUnit.SECONDS);

			// Now request header info from everyone since we restarted (or started for the first time).
			beginAquireChainOperation();
		}
	}

	public void beginAquireChainOperation(){

		if (this.aquireChain == null){
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
	public void removeNode(int index) {

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

	public void printConnectedNodes() {
		System.out.println("Connected Nodes: ");

		ArrayList<PeerNode> nodes = new ArrayList<>();

		for (PeerNode p : getPeerNodes()) {

			if (!nodes.contains(p)) {
				System.out.println(" - " + p.getListeningAddress());
				nodes.add(p);
			}
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
	 * Sends the given message to all connected peers.
	 *
	 * @param message
	 */
	public void asyncSendToAllPeers(AbstractMessage message){

		synchronized (peerNodes){
			for (PeerNode n : peerNodes){
				n.asyncSendMessage(message);
			}
		}
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

	public Miner getMiner(){
		return miner;
	}

	private void connectToPeers() {

		for (PeerNode p : getPeerNodes()) {
			p.connect();
		}
	}

	/**
	 * Begins mining, but only if mining is set to true.
	 */
	public void beginMining(){
		// TODO READ THE BELOW TODO
		if (mining){

			System.out.println("Mining: " + mining);

			// TODO When transaction broadcasts are added, trigger mining in the transaction broadcast processor based on some condition.
			// At the moment just going to mine in a loop.
			LinkedList<Block> chain = new BlockChain().getLongestChain();
			miner.startMining(chain.size() + 1 + "", chain.get(chain.size() -1), Node.HASH_DIFFICULTY);
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


		private volatile HashMap<Integer, Long> requestTimes;
		private volatile int leftBound = 0;
		private volatile int rightBound = 10;
		private int highestBlock = 0;

		private HashMap<Integer, Boolean> recievedBlocks;


		public AquireChain() {
			this.requestTimes = new HashMap<>();
			recievedBlocks = new HashMap<>();
		}

		public void gotBlock(BlockMessage blockMessage){

			synchronized (recievedBlocks) {

				recievedBlocks.put(blockMessage.blockHeight, true);

				// Update bounds.
				int min = -1;
				for (int i = 0; i < highestBlock; i++) {
					if (recievedBlocks.keySet().contains(i)) {
						min = i;
					} else {
						break; // We found the lowest recieved block.
					}
				}

				leftBound = Math.min(highestBlock - 1, min + 1); // So it doesnt try to grab n + 1 blocks.
				rightBound = min + 10;

				System.out.println("Got new left bound: " + leftBound);
			}
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


				BlockChain blockChain = new BlockChain();

				if (highestHeaders.size() <= blockChain.getLongestChain().size()){
					System.out.println("Already have the highest chain");
					System.out.println(blockChain.getLongestChain().size());
					System.out.println(highestHeaders.size());
					beginMining();
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
				highestBlock = highestHeaders.size();

				while (recievedBlocks.size() < highestHeaders.size()) {

					Thread.sleep(100);
					synchronized (recievedBlocks) {

						if (i > rightBound || i >= highestHeaders.size() - 1) {
						//	Thread.sleep(100); // Wait for responses.

							//if (requestTimes.get(leftBound) + 3000 <  System.currentTimeMillis()) { // Only make another request every 3 seconds
							// Request lowest block again.
							nodes.get(nodes.size() == 1 ? 0 : rand.nextInt(nodes.size() - 1)).asyncSendMessage(new RequestBlockMessage(highestHeaders.get(leftBound).blockHash, leftBound));
							requestTimes.put(i, System.currentTimeMillis());
							//}
						} else {


							//if (requestTimes.get(i) + 3000 <  System.currentTimeMillis()) { // Only make another request every 3 seconds
							nodes.get(nodes.size() == 1 ? 0 : rand.nextInt(nodes.size() - 1)).asyncSendMessage(new RequestBlockMessage(highestHeaders.get(i).blockHash, i));
							requestTimes.put(i, System.currentTimeMillis());
							//} else {
							//Thread.sleep(100);
							//}

							if (i < highestHeaders.size() - 1) {
								i++;
							}
						}
					}

				}

				System.out.println("AQUIRED THE BLOCKCHAIN!");

				// Process blocks into a chain and save wooo!

				for (int j = 0; j < highestHeaders.size(); j ++) {

					for (BlockMessage m : blockQueue) {

						if (m.blockHeight == j) {
							blockChain.addBlock(m.block);
							break;
						}
					}
				}

				blockChain.save();

				beginMining();
				aquireChain = null;

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
