package distributeblocks.net;

import distributeblocks.*;
import distributeblocks.io.ConfigManager;
import distributeblocks.mining.Miner;
import distributeblocks.net.message.*;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class NetworkManager implements NetworkActions {

	private HashMap<String, Transaction> transanctionPool;
	private HashMap<String, Transaction> orphanedTransactionPool;
	private HashMap<String, Transaction> pendingTransactionPool; // Transactions that are being put into a block.

	private LinkedBlockingQueue<AbstractMessage> incommingQueue;
	private LinkedBlockingQueue<ArrayList<BlockHeader>> headerQueue;
	private LinkedBlockingQueue<BlockMessage> blockQueue;

	private volatile AquireChain aquireChain; // TODO: Replace with events to make this not awfull?

	private int minPeers = 0;
	private int maxPeers = Integer.MAX_VALUE;
	private int port = -1;
	private int seedPeerTimeout = 30000; // mililiseconds
	private int seedCheckoutTimer = 35; // seconds
	private int aliveNotifierTime = 20; // seconds
	private IPAddress seedNodeAddr;
	private IPAddress localAddr;
	private boolean seed;
	private boolean mining;

	/**
	 * Time in seconds between attempts at discovering more peer nodes.
	 */
	private int peerSearchRate = 60;

	private ExecutorService executorService;
	private ScheduledExecutorService scheduledExecutorService; // For requesting new peers.
	private List<PeerNode> peerNodes;
	private List<PeerNode> temporaryPeerNodes;
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
		transanctionPool = new HashMap<>();
		orphanedTransactionPool = new HashMap<>();
		pendingTransactionPool = new HashMap<>();
	}


	/**
	 * Starts handshake process with known nodes.
	 */
	public void initialize() {


		// Grab known peer nodes from file.
		ConfigManager configManager = new ConfigManager();
		peerNodes = Collections.synchronizedList(configManager.readPeerNodes());
		temporaryPeerNodes = Collections.synchronizedList(new ArrayList<>());

		// Begin listening for connections, and start the message processor
		try {
			serverSocket = new ServerSocket(port);
			findLocalAddr();
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
			scheduledExecutorService.schedule(new AliveNotifier(),aliveNotifierTime, TimeUnit.SECONDS);
		} else {
			scheduledExecutorService.schedule(new TimeoutChecker(), seedCheckoutTimer, TimeUnit.SECONDS);
		}
	}

	/**
	 * Spawns a new thread running code to aquire the chain.
	 *
	 * To be called on startup, or when a block is received that references
	 * a block on the chain that this node does not have.
	 */
	public void beginAquireChainOperation(){

		if (this.aquireChain == null){
			this.aquireChain = new AquireChain();
			executorService.execute(this.aquireChain);
		}
	}


	/**
	 * Add node to the main peerNodes list.
	 *
	 * @param node
	 */
	public void addNode(PeerNode node) {
		
		synchronized (peerNodes) {

			this.peerNodes.add(node);

			ConfigManager manager = new ConfigManager();
			manager.addNodeAndWrite(node);
		}
	}


	/**
	 * Adds node to temporary node list.
	 *
	 * The temporary list is there so that peerRequest messages can be sent
	 * and received, even if the peer doesnt want to establish permanent relations.
	 *
	 *
	 * @param node
	 */
	public void addTemporaryNode(PeerNode node) {

		synchronized (temporaryPeerNodes) {

			this.temporaryPeerNodes.add(node);
		}
	}

	public IPAddress getLocalAddr() {
		return localAddr;
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
					System.out.println("Shutdown was called in removeNode()");
					PeerNode result = peerNodes.remove(i);
					result.shutDown();


					if (result == null){
						System.out.println("Failed to remove peer from pool.");
					} else {
						System.out.println("Removed peer from pool.");
					}
					break;
				}
			}
		}
	}

	/**
	 * Removes given node from the temporary pool.
	 *
	 * This is done by compareing references.
	 *
	 * @param node
	 */
	public void removeTemporaryNode(PeerNode node){

		synchronized (temporaryPeerNodes) {
			for (int i = 0; i < temporaryPeerNodes.size(); i++) {
				if (temporaryPeerNodes.get(i) == node) { // At least I think this only checks if the reference is the same.
					PeerNode result = temporaryPeerNodes.remove(i);
						// Not to self, do not call shutdown here! They may be moved to the actual node pool.

					if (result == null){
						System.out.println("Failed to remove peer from temporary pool.");
					} else {
						System.out.println("Removed peer from temporary pool.");
					}
					break;
				}
			}
		}

		System.out.println("No node to remove from temporary pool.");
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


	public void printConnectedNodes() {
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

	/**
	 * Get a copy of peerNodes list.
	 *
	 * @return
	 *   Copy of peerNodes list.
	 */
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

			//addNode(node); // TODO: This may ahve caused issues with cfg file.
			addTemporaryNode(node);
			//node.setLocalAddress(address); // Since we are connecting to it, it must already be the local address.
			node.asyncSendMessage(new ShakeMessage("Please be my friend.", port));

			return true;
		}

		return false;
	}

	/**
	 *
	 *
	 * @param address
	 * @return
	 *   True if the peerNodes list has a peerNode with the given address (listening address).
	 */
	public boolean isConnectedToNode(IPAddress address) { //TODO: Would be less confusing to use PeerNode?


		for (PeerNode p : getPeerNodes()) {
			if (p.getListeningAddress().equals(address)) {
				return true;
			}
		}

		return false;
	}

	/**
	 *
	 * Determine if a given node is a seed node.
	 *
	 * @param node
	 * @return
	 *   True if the given node has identified itself as a seed node.
	 */
	public boolean isSeedNode(PeerNode node) {

		if (node.getListeningAddress().equals(seedNodeAddr)) {
			return true;
		}
		return false;
	}

	public Miner getMiner(){
		return miner;
	}


	/**
	 * Validates transactions.
	 *
	 * Decides if transactions should go in main pool or orphan pool.
	 * A transaction goes into the orphan pool if it has inputs
	 * that cannot be found in the chain.
	 *
	 * @param transaction
	 */
	public void addTransaction(Transaction transaction){

		// TODO Ian figure out the validation crap.

		synchronized (transanctionPool) {

			// Only re-broadcast transaction if we have not seen it before.
			boolean found = false;
			HashMap<String, Transaction> combinedPool = new HashMap<>();
			combinedPool.putAll(transanctionPool);
			combinedPool.putAll(pendingTransactionPool);

			for (String id : combinedPool.keySet()){
				if (id.equals(transaction.getId_Transaction())){
					found = true;
					break;
				}
			}

			if (!found){
				asyncSendToAllPeers(new TransactionBroadcastMessage(transaction));
			}

			transanctionPool.put(transaction.getId_Transaction(), transaction);
		}
	}


	/**
	 * Simply connects to all the peers currently loaded into the peer nodes list.
	 * p.connect() is a blocking call.
	 */
	private void connectToPeers() {

		for (PeerNode p : getPeerNodes()) {
			//p.setLocalAddress(p.getAddress());
			temporaryPeerNodes.add(p);
			p.connect();
		}
	}


	/**
	 * Goes through the network interfaces, extracts InetAddresses,
	 * and uses the first one that isnt 127.0.0.1
	 *
	 * Sets localAddr.
	 */
	private void findLocalAddr(){

		try {

			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();


			while (interfaces.hasMoreElements()){
				NetworkInterface ni = interfaces.nextElement();
				System.out.println(ni.getDisplayName());
				Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();

				while (inetAddresses.hasMoreElements()){
					InetAddress addr = inetAddresses.nextElement();
					String hostAddress = addr.getHostAddress();
					System.out.println(hostAddress);

					if (!hostAddress.contains("127.0.0.1") && !hostAddress.contains("localhost") && hostAddress.split("\\.").length == 4){
						localAddr = new IPAddress(hostAddress, port);
						return;
					}
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		}

	}



	/**
	 * Begins mining, but only if mining is set to true.
	 *
	 * Internal to net stuff only.
	 *
	 * See startMining()
	 */
	 public void beginMining(){
		// TODO READ THE BELOW TODO
		if (mining){

			System.out.println("Mining: " + mining);

			// TODO When transaction broadcasts are added, trigger mining in the transaction broadcast processor basedstart on some condition.
			// At the moment just going to mine in a loop.
			LinkedList<Block> chain = new BlockChain().getLongestChain();

			synchronized (transanctionPool) {
				// TODO: Validate the entire pool again for no reason.
				HashMap<String, Transaction> poolCopy = (HashMap<String, Transaction>) transanctionPool.clone();
				transanctionPool.clear();

				pendingTransactionPool.putAll(poolCopy);
				miner.startMining(poolCopy, chain.get(chain.size() - 1), Node.HASH_DIFFICULTY);
			}
		}
	}

	public void clearPendingTransactions(){
	 	synchronized (transanctionPool){
	 		pendingTransactionPool.clear();
		}
	}


	// ============================= NetworkActions =============================


	/**
	 * Begins mining even if mining is set to false.
	 */
	@Override
	public void startMining(){
		mining = true;
		beginMining();
	}

	@Override
	public void stopMining() {

		if (miner != null){
			miner.stopMining();
		}

		mining = false;
	}

	@Override
	public void broadcastTransaction(Transaction transaction) {

		asyncSendToAllPeers(new TransactionBroadcastMessage(transaction));
	}


	// ==========================================================

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
					//peerNodes.add(peerNode);
					addTemporaryNode(peerNode);


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
						//seed.setLocalAddress(seedNodeAddr);
						temporaryPeerNodes.add(seed);
						seed.connect();
						// The requestPeersMessage was moved into the shake response handler to ensure things happen
						// in the right order.
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


	/**
	 * Nodes will use this to periodically announce themselves to the seed node.
	 * If the seed node does not receive an alive notification within some time period,
	 * (maybe 30 seconds for the purpose of the demo), then the seed node will remove it
	 * from its list.
	 */
	private class AliveNotifier implements Runnable {

		@Override
		public void run() {

			PeerNode seed = new PeerNode(seedNodeAddr);
			//seed.setLocalAddress(seedNodeAddr);
			temporaryPeerNodes.add(seed);
			seed.connect(); // Automaticaly sends a handshake, which is all we need to update the seeds peer list.
							// Note that a peer request message will also be sent, oh well.

			System.out.println("Send alive notification.");
			scheduledExecutorService.schedule(new AliveNotifier(),aliveNotifierTime, TimeUnit.SECONDS);
		}
	}

	/**
	 * Seed nodes will uise this periodicaly to go through its timeouts file, and remove nodes
	 * who have not contacted this node for some ammount of time.
	 */
	private class TimeoutChecker implements Runnable {

		@Override
		public void run() {

			ConfigManager manager = new ConfigManager();

			ArrayList<PeerNode> nodes = manager.readPeerNodes();
			ArrayList<PeerNode> newNodes = (ArrayList<PeerNode>) nodes.clone();
			HashMap<String, Long> timeoutData = manager.readTimeoutFile();
			Long currentTime = new Date().getTime();

			System.out.println("Running timeout checker.");

			for (PeerNode n : nodes) {
				boolean found = false;

				for (Map.Entry<String, Long> ent : timeoutData.entrySet()) {
					if (n.getListeningAddress().toString().equals(ent.getKey())) {

						found = true;

						if (currentTime - ent.getValue() > seedPeerTimeout) {
							// It has not contacted us for a wile, remove it from our list.
							System.out.println("Node " + n.getListeningAddress() + " has timed out.");
							newNodes.remove(n);
							timeoutData.remove(ent.getKey());
							break;
						}
					}
				}

				if (!found) {
					newNodes.remove(n);
				}
			}

			manager.writePeerNodes(newNodes);
			manager.writeTimeoutFile(timeoutData);

			// Feel free to not use seedPeerTimeout.
			scheduledExecutorService.schedule(new TimeoutChecker(), seedCheckoutTimer, TimeUnit.SECONDS);
		}
	}
}
