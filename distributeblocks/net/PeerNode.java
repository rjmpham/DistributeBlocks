package distributeblocks.net;

import distributeblocks.net.message.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.*;

public class PeerNode {

	public int MAX_CACHED_MESSAGES = 16;


	private IPAddress address;
	private ExecutorService executorService;
	private Socket socket;
	private LinkedBlockingQueue<AbstractMessage> outQueue;


	private volatile boolean shutDown = false;



	/**
	 * For creating a new peer node for an outgoing connection.
	 * <p>
	 * Make sure to call asyncConnect()
	 *
	 * @param address
	 */
	public PeerNode(IPAddress address) {
		this.address = address;
		outQueue = new LinkedBlockingQueue<>();
		executorService = Executors.newCachedThreadPool();
	}

	/**
	 * For creating a new peernode from an incomming connection.
	 *
	 * @param socket
	 */
	public PeerNode(Socket socket) {

		this.socket = socket;
		this.address = new IPAddress(socket.getInetAddress().getHostName(), socket.getPort());
		outQueue = new LinkedBlockingQueue<>();
		executorService = Executors.newCachedThreadPool();

		// Start the listening right away.
		executorService.execute(new Listener());
		executorService.execute(new Sender());
	}

	/**
	 * Connects to the node asynchronously.
	 * <p>
	 * Will enque a ConnectionFailed message if the connection is not successfull.
	 *
	 * @return False if connection was not succesfful
	 */
	public void asyncConnect() {

		if (address == null) {
			throw new RuntimeException("You had a null address while trying to connect a peer node!");
		}

		executorService.execute(() -> {


			try {
				socket = new Socket(address.ip, address.port);
				System.out.println("Connected to: " + address);

				// Connection success! Start listening
				executorService.execute(new Listener());
				executorService.execute(new Sender());

				sendMessage(new ShakeMessage("Hey there ;)"));

			} catch (IOException e) {
				e.printStackTrace();
				NetworkService.getNetworkManager().asyncEnqueue(new ConnectionFailedMessage(PeerNode.this));
			}
		});
	}

	public void sendMessage(AbstractMessage message) {

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

	public void shutDown() {

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


	/**
	 * Processes incomming messages from the peer node and adds them to the networkmanager queue.
	 */
	private class Listener implements Runnable {


		@Override
		public void run() {

			try {
				ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
				while (true) {

					AbstractMessage message = (AbstractMessage) inputStream.readObject();
					message.senderNode = PeerNode.this; // TODO: Is this really the nicest solution here?
					NetworkService.getNetworkManager().asyncEnqueue(message);
				}

			} catch (EOFException e) {
				// Socket closed.
				NetworkService.getNetworkManager().asyncEnqueue(new ConnectionLostMessage(PeerNode.this));
			} catch (IOException e) {
				e.printStackTrace();
				NetworkService.getNetworkManager().asyncEnqueue(new ConnectionFailedMessage(PeerNode.this));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException("Class not found exception wut?");
			}
		}
	}

	/**
	 * Responsible for sending messages to the peer node.
	 */
	private class Sender implements Runnable {


		public Sender() {
		}

		@Override
		public void run() {

			ObjectOutputStream stream = null;

			try {
				stream = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
				NetworkService.getNetworkManager().asyncEnqueue(new ConnectionFailedMessage(PeerNode.this));
			}

			while (!shutDown) {

				AbstractMessage message = null;

				try {

					message = outQueue.take();
					stream.writeObject(message);

				} catch (InterruptedException | IOException e) {
					e.printStackTrace();
					NetworkService.getNetworkManager().asyncEnqueue(new SendFailMessage(PeerNode.this,message ));
				}
			}
		}
	}

}
