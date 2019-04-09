package distributeblocks.net;

import distributeblocks.*;
import distributeblocks.io.ConfigManager;
import distributeblocks.mining.Miner;
import distributeblocks.net.message.*;
import distributeblocks.util.Validator;
import distributeblocks.io.Console;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

// TODO: make the pending transaction pool clearing intelligent
// TODO: force agreement on a comment block as the true head in case of a a branch (maybe shortest hash)
public class NetworkManager implements NetworkActions {

	private HashMap<String, Transaction> transactionPool;
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

	private IPAddress localAddr;
	private boolean seed;
	private boolean mining;

	/**
	 * Time in seconds between attempts at discovering more peer nodes.
	 */
	private int peerSearchRate = 10;

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
		this.seed = networkConfig.seed;
		this.mining = networkConfig.mining;
		this.miner = new Miner();

		ConfigManager configManager = new ConfigManager();

		ArrayList<IPAddress> seedNodes = configManager.readSeedNodes();

		boolean found = false;
		for (IPAddress addr : seedNodes){
			if (addr.equals(networkConfig.seedNode)){
				found = true;
				break;
			}
		}

		if (!found){
			seedNodes.add(networkConfig.seedNode);
			configManager.writeSeedNodes(seedNodes);
		}

		if (seed) {
			Console.log("Starting in seed mode.");
		}

		// May want seperate services to make shutdowns easier?
		executorService = Executors.newCachedThreadPool();
		scheduledExecutorService = Executors.newScheduledThreadPool(1);
		incommingQueue = new LinkedBlockingQueue<>();
		headerQueue = new LinkedBlockingQueue<>();
		blockQueue = new LinkedBlockingQueue<>();
		transactionPool = new HashMap<>();
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
					Console.log("Shutdown was called in removeNode()");
					PeerNode result = peerNodes.remove(i);
					result.shutDown();


					if (result == null){
						Console.log("Failed to remove peer from pool.");
					} else {
						Console.log("Removed peer from pool.");
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
						Console.log("Failed to remove peer from temporary pool.");
					} else {
						Console.log("Removed peer from temporary pool.");
					}
					break;
				}
			}
		}

		Console.log("No node to remove from temporary pool.");
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
		Console.log(" ======================== Connected Nodes: =============================");
		for (PeerNode p : getPeerNodes()) {

			Console.log(" - " + p.getListeningAddress());
		}
	}

	/**
	 * Returns true if the number of peers is less  then the required number of peers.
 	 * Used to tell the NetworkManager to keep searching for peers.
	 *
	 * @return
	 */
	public boolean needMorePeers() {
		return getPeerNodes().size() < minPeers;
	}

	/**
	 * Used to determine if the current node is a seed node. Used for functions that require
 	 * knowledge of this to make decisions.
	 *
	 * @return
	 */

	public boolean canHaveMorePeers(){
		return getPeerNodes().size() < maxPeers;
	}

	public boolean inSeedMode() {
		return seed;
	}

	/**
	 * Returns a copy of all the peers in the system for multiple purposes, like sending the list
 	 * off in a message
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

	/**
	 * Get a copy of the temporaryPeerNodes list.
	 *
	 * @return
	 */

	public List<PeerNode> getTempPeerNodes() {

		synchronized (temporaryPeerNodes){
			ArrayList<PeerNode> copy = new ArrayList<>(temporaryPeerNodes);
			return copy;
		}
	}

	public int getMinPeers() {
		return minPeers;
	}

	public int getPort() {
		return port;
	}


	/* Adds the block ID of a new block to the list of block headers
	 */
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
	 * Add a incoming message to a processing queue.
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


			//addNode(node); // TODO: This may have caused issues with cfg file.
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
	 *
	 * @param address
	 * @return
	 *   True if the peerNodes list has a peerNode with the given address (listening address).
	 */
	public boolean isConnectedToTempNode(IPAddress address) { //TODO: Would be less confusing to use PeerNode?


		for (PeerNode p : getTempPeerNodes()) {
			if (p.getListeningAddress().equals(address)) {
				return true;
			}
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
	// TODO: make the locks more reasonable here. maybe move code into synchronized sub methods
	// TODO: merge isUnspent, containsValidTransactionInputs, and existsInChain, then call it here
	public void addTransaction(Transaction transaction){
//		BlockChain chain = new BlockChain();
//		LinkedList<Block> longestChain = chain.getLongestChain();

		// TODO Ian figure out the validation crap.
//		if (!Validator.isUnspent(transaction, longestChain)) {
//			Console.log("Transaction was a double spend! aborting");
//			return;
//		}

		synchronized (transactionPool) {

			// Only re-broadcast transaction if we have not seen it before.
			boolean found = false;

			// TODO: should check over blockchain as well to find out if we've seen it there already
			// Compose a hashmap of the normal transaction pool and pending transactions
			HashMap<String, Transaction> combinedPool = new HashMap<>();
			combinedPool.putAll(transactionPool);
			combinedPool.putAll(pendingTransactionPool);

			// Check if we have seen this transaction before
			for (String id : combinedPool.keySet()){
				if (id.equals(transaction.getTransactionId())){
					found = true;
					break;
				}
			}

			if (!found){
				Console.log("Transaction " + transaction.getId_Transaction() + "is new. Broadcasting...");
				// if we've never seen this transaction before, send it to peers
				asyncSendToAllPeers(new TransactionBroadcastMessage(transaction));
				
//				// Put the transaction into the correct pool
//				if (Validator.containsValidTransactionInputs(transaction, longestChain)) {
					transactionPool.put(transaction.getId_Transaction(), transaction);
//					updateOrphanPool(transaction);
//				}
//				else {
//					orphanedTransactionPool.put(transaction.getId_Transaction(), transaction);
//				}
			}
		}
	}
	
	/**
	 * Updates the transaction pools to remove any transactions
	 * that have been verified on a block.
	 * 
	 * This method is called whenever a block becomes verified (sufficiently deep).
	 * 
	 * @param block	the most recently verified block of the longest chain
	 */
	public void updateTransactionPools(Block block) {
		Console.log("Updating local transaction pools from block " + block.getHashBlock());
		HashMap<String, Transaction> blockData = block.getData();
		updateOrphanPool(blockData);
		updateTransactionPool(blockData);
	}
	
	/**
	 * Checks over each transaction in the potentialParants and moves
	 * any orphaned transaction whose parant is discovered over the to
	 * normal transactionPool. 
	 * 
	 * This operation will be called recursively, as any transaction which is
	 * moved may be the parent of a different transaction.
	 * 
	 * This method is called whenever a block becomes verified (sufficiently deep),
	 * or when a new transaction is received
	 * 
	 * @param potentialParants		Hashmap of Transaction ids to Transactions
	 */
	// TODO: does this have to be recursive if we properly check if a transaction is an orphan or not when receiving a transaction?
	// TODO: should this be synchronized? it is called whenever a transaction is received
	public void updateOrphanPool(HashMap<String, Transaction> potentialParants) {
		// Recursive basecase
		if (potentialParants.isEmpty())
			return;
		
		Transaction transaction;
		HashMap<String, Transaction> newParents = new HashMap<String, Transaction>();
		
		// Process the parants, and keep track of any moved Transactions in newParants
		for (Map.Entry<String,Transaction> i: potentialParants.entrySet()){
			if(orphanedTransactionPool.containsKey(i.getKey())) { //TODO: shouldnt this be the parents id we check against?
				transaction = orphanedTransactionPool.get(i.getKey());
				orphanedTransactionPool.remove(i.getKey());
				
				transactionPool.put(i.getKey(), i.getValue());
				newParents.put(i.getKey(), i.getValue());
			}
		}
		// Call recursively on the new potential parents
		updateOrphanPool(newParents);
	}
	
	/**
	 * Moves any orphaned transaction whose who are children of the given
	 * transaction out of the orphaned transaction pool.
	 * 
	 * This operation is called whenever a new transaction is received and
	 * added to the transactionPool.
	 * 
	 * @param transaction		the potential parent Transactions
	 */
	public void updateOrphanPool(Transaction transaction) {
		HashMap<String, Transaction> container = new HashMap<String, Transaction>();
		container.put(transaction.getId_Transaction(), transaction);
		updateOrphanPool(container);
	}
	
	/**
	 * Checks over each transaction i the verifiedTransactions and removes
	 * any matches from the transactionPool, since they have been placed onto a 
	 * verified block.
	 * 
	 * This method is called whenever a block becomes verified (sufficiently deep).
	 * 
	 * @param verifiedTransactions 	Hashmap of Transaction ids to Transactions
	 */
	public void updateTransactionPool(HashMap<String, Transaction> verifiedTransactions) {
		for (Map.Entry<String,Transaction> i: verifiedTransactions.entrySet()){
			if(transactionPool.containsKey(i.getKey()))
				transactionPool.remove(i.getKey());
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
	 * and uses the first one that isn't 127.0.0.1
	 *
	 * Sets localAddr.
	 */
	private void findLocalAddr(){

		try {

			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();


			while (interfaces.hasMoreElements()){
				NetworkInterface ni = interfaces.nextElement();
				Console.log(ni.getDisplayName());
				Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();

				while (inetAddresses.hasMoreElements()){
					InetAddress addr = inetAddresses.nextElement();
					String hostAddress = addr.getHostAddress();
					Console.log(hostAddress);

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
		if (mining){

			Console.log("Mining: " + mining);

			LinkedList<Block> chain = new BlockChain().getLongestChain();

			synchronized (transactionPool) {
				HashMap<String, Transaction> poolCopy = (HashMap<String, Transaction>) transactionPool.clone();
				transactionPool.clear();

				pendingTransactionPool.putAll(poolCopy);
				miner.startMining(poolCopy, chain.get(chain.size() - 1), Node.HASH_DIFFICULTY);
			}
		}
	}

	public void clearPendingTransactions(){
	 	synchronized (transactionPool){
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

					Console.log("Listening for connections!");

					Socket socket = serverSocket.accept();

					Console.log("Received connection from: " + socket.getInetAddress());

					PeerNode peerNode = new PeerNode(socket);
					//peerNodes.add(peerNode);
					addTemporaryNode(peerNode);

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

				//Console.log("Checking if I need more friends.");
				if (nodes.size() < minPeers) {

					//Console.log("I do");
					Console.log("Getting more peers.");

					if (nodes.size() > 0) {
						for (PeerNode p : nodes) {
							p.asyncSendMessage(new RequestPeersMessage());
						}
					} else {

						ArrayList<IPAddress> seedNodes = new ConfigManager().readSeedNodes();

						for (int i = 0; i < seedNodes.size(); i ++){ // Try each seed node in the seed node file, starting with the first one.

							PeerNode seed = new PeerNode(seedNodes.get(i));
							temporaryPeerNodes.add(seed);

							if (seed.connect()){ // If the connection was successfull don't connect to any more.
								break;
							}
						}

						// The requestPeersMessage was moved into the shake response handler to ensure things happen
						// in the right order.
					}
				} else {
					//Console.log("I don't");
				}


				// Consolidate connections. Maybe there could be a way to avoid duplicate connections in the first place?
				/**/

				//removeDuplicateNodes();
				printConnectedNodes();


			} catch (Exception e) {
				e.printStackTrace();
				// Not sure whats going on here.
			}

			scheduledExecutorService.schedule(new CheckNeedNodes(), peerSearchRate, TimeUnit.SECONDS);
		}
	}


	/**
	 * Acquires the chain.
	 * Run on startup, or when a block is received that
	 * references a block we do not have.
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

				Console.log("Got new left bound: " + leftBound);
			}
		}


		@Override
		public void run() {

			// Ask for info about the blocks in the chain
			requestHeaders();

			try {
				Thread.sleep(5000);

				// Waiting a bit to get the responses.
				while (true) {

					if (headerQueue.isEmpty()) {
						Console.log("Waiting for header info ...");
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
					Console.log("Already have the highest chain");
					Console.log(blockChain.getLongestChain().size());
					Console.log(highestHeaders.size());
					beginMining();
					return;
				}

				Console.log("Aquireing blockchain with height: " + highestHeaders.size());

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
				Console.log("AQUIRED THE BLOCKCHAIN!");

				// Process blocks into a chain and save them
				for (int j = 0; j < highestHeaders.size(); j ++) {

					for (BlockMessage m : blockQueue) {

						if (m.blockHeight == j) {
							blockChain.addBlock(m.block);
							
							Block lastVerified = blockChain.getLastVerifiedBlock();
							if (lastVerified != null) {
								// Update node wallet with the block which is now verified
								NodeService.getNode().updateWallet(lastVerified);
								// Update the transaction pools now that a new block is verified
								NetworkService.getNetworkManager().updateTransactionPools(lastVerified);
							 }
							break;
						}
					}
				}

				blockChain.save();

				beginMining();		// TODO: is it necessary to begin mining here, or was this just for testing?
				aquireChain = null;

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		private void requestHeaders(){

			Console.log("Requesting headers.");
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

			ArrayList<IPAddress> seedNodes = new ConfigManager().readSeedNodes();

			for (int i = 0; i < seedNodes.size(); i ++) {
				PeerNode seed = new PeerNode(seedNodes.get(i));
				//seed.setLocalAddress(seedNodeAddr);
				temporaryPeerNodes.add(seed);


				if (seed.connect()) { // Automaticaly sends a handshake, which is all we need to update the seeds peer list.
					// Note that a peer request message will also be sent, oh well.

					Console.log("Send alive notification.");
					break; // Only send the alive notifcation to one.
				}
			}

			scheduledExecutorService.schedule(new AliveNotifier(), aliveNotifierTime, TimeUnit.SECONDS);
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

			Console.log("Running timeout checker.");

			for (PeerNode n : nodes) {
				boolean found = false;

				for (Map.Entry<String, Long> ent : timeoutData.entrySet()) {
					if (n.getListeningAddress().toString().equals(ent.getKey())) {

						found = true;

						if (currentTime - ent.getValue() > seedPeerTimeout) {
							// It has not contacted us for a wile, remove it from our list.
							Console.log("Node " + n.getListeningAddress() + " has timed out.");
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
