package distributeblocks.net;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.message.AbstractMessage;
import distributeblocks.net.message.ServerCrashMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.*;

public class NetworkManager {

	private LinkedBlockingQueue<AbstractMessage> incommingQueue;

	private int minPeers = 0;
	private int maxPeers = Integer.MAX_VALUE;
	private int port = -1;
	private IPAddress seedNodeAddr;

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
	public NetworkManager(int minPeers, int maxPeers, int port, IPAddress seedNodeAddr){

		this.maxPeers = maxPeers;
		this.minPeers = minPeers;
		this.port = port;
		this.seedNodeAddr = seedNodeAddr;

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

		connectToPeers();

		if (peerNodes.size() < minPeers){
			// TODO: Do something to search for more peers.
		}

		// Begin listening for connections, and start the message processor

		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not create server socket.");
		}

		executorService.execute(new Connectionlistener());
		executorService.execute(new IncommingQueueProcessor());
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

					System.out.println("Received connection from: " + socket.getInetAddress());

					PeerNode peerNode = new PeerNode(socket);
					peerNodes.add(peerNode);

				} catch (IOException e) {
					e.printStackTrace();
					asyncEnqueue(new ServerCrashMessage());
				}
			}

		}
	}

}
