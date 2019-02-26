package distributeblocks.net;

import distributeblocks.net.message.AbstractMessage;
import distributeblocks.net.message.ConnectionFailedMessage;
import distributeblocks.net.message.SendFailMessage;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.*;

public class PeerNode{

	public int MAX_CACHED_MESSAGES = 16;


	private IPAddress address;
	private ExecutorService executorService;
	private Socket socket;
	private LinkedBlockingQueue<AbstractMessage> outQueue;


	private volatile boolean shutDown = false;


	/**
	 * For creating a new peer node for an outgoing connection.
	 *
	 * Make sure to call asyncConnect()
	 *
	 * @param address
	 */
	public PeerNode(IPAddress address) {
		this.address = address;
	}

	/**
	 * For creating a new peernode from an incomming connection.
	 *
	 * @param socket
	 */
	public PeerNode(Socket socket){

		this.socket = socket;
		outQueue = new LinkedBlockingQueue<>();
	}

	/**
	 * Connects to the node asynchronously.
	 *
	 * Will enque a ConnectionFailed message if the connection is not successfull.
	 *
	 * @return
	 *   False if connection was not succesfful
	 */
	public void asyncConnect()  {

		// TODO: make this async

		if (address == null){
			throw new RuntimeException("You had a null address while trying to connect a peer node!");
		}

		try {
			socket = new Socket(address.ip, address.port);
			System.out.println("Connected to: " + address);

		} catch (IOException e) {
			e.printStackTrace();
			NetworkService.getNetworkManager().asyncEnqueue(new ConnectionFailedMessage(this));
		}
	}

	public void sendMessage(AbstractMessage message){

		try {
			outQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
			// TODO: Handle this?
		}
	}


	public IPAddress getAddress() {
		return address;
	}

	public void shutDown(){

		shutDown = true;
		outQueue.notifyAll();
		executorService.shutdown();

		try {
			executorService.awaitTermination(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			executorService.shutdownNow();
		}

		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private class Listener implements Runnable {


		@Override
		public void run() {

		}
	}

	private class Sender implements Runnable {


		private AbstractMessage message;

		public Sender(AbstractMessage message) {
			this.message = message;
		}

		@Override
		public void run() {


			while (!shutDown){

				if (outQueue.size() > 0){

					try {
						ObjectOutputStream stream = new ObjectOutputStream(socket.getOutputStream());
					} catch(IOException e){
						e.printStackTrace();

					}
				}
			}

		}
	}
}
