package distributeblocks.net;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.message.AbstractMessage;

import java.util.ArrayList;
import java.util.concurrent.*;

public class NetworkManager {

	protected BlockingQueue<AbstractMessage> incommingQueue;

	private int minPeers = 0;
	private int maxPeers = Integer.MAX_VALUE;

	/**
	 * Time in seconds between attempts at discovering more peer nodes.
	 */
	private int peerSearchRate = 60;

	private ExecutorService executorService;
	private ScheduledExecutorService scheduledExecutorService; // For requesting new peers.
	private ArrayList<PeerNode> peerNodes;


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
	public NetworkManager(int minPeers, int maxPeers){

		this.maxPeers = maxPeers;
		this.minPeers = minPeers;

		// May want seperate services to make shutdowns easier?
		executorService = Executors.newCachedThreadPool();
		scheduledExecutorService = Executors.newScheduledThreadPool(1);
		incommingQueue = new ArrayBlockingQueue<AbstractMessage>(256);
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
		incommingQueue.notify();
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
		public void run() {

			while (!shutDown){

				if (incommingQueue.size() > 0){

					// Process a message!
					AbstractMessage m = incommingQueue.remove();
					m.getProcessor().processMessage(m); // It wasnt supposed to be like this!!!

				} else {

					// Wait until notify() is called in the incomming queue.
					try {
						incommingQueue.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}

	}

}
