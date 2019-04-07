package distributeblocks.net;

import distributeblocks.net.message.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.*;

public class PeerNode {

	public int MAX_CACHED_MESSAGES = 16;


	private IPAddress address;
	private IPAddress localAddress;
	private int listenPort = -1;
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
		this.listenPort = address.port;
		this.localAddress = address; // Since this is for outgoing its always going to be the local address.
		//this.localAddress = address;
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

		// StartHandler the listening right away.
		executorService.execute(new Listener());
		executorService.execute(new Sender());
	}

	/**
	 * @deprecated Havnt used this method in a wile, its probably brocken.
	 *
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

				// Connection success! StartHandler listening
				executorService.execute(new Listener());
				executorService.execute(new Sender());

				asyncSendMessage(new ShakeMessage("Hey there ;)", NetworkService.getNetworkManager().getPort()));

			} catch (IOException e) {
				//e.printStackTrace();
				NetworkService.getNetworkManager().asyncEnqueue(new ConnectionFailedMessage(PeerNode.this));
			}
		});
	}

	/**
	 * Same as async connect, but does not trigger a connection fail message,
	 * and obviously isnt async.
	 *
	 * @return
	 *   True if connection was sucessfull, false otherwise.
	 */
	public boolean connect(){

		if (address == null) {
			throw new RuntimeException("You had a null address while trying to connect a peer node!");
		}

			try {
				socket = new Socket(address.ip, address.port);
				System.out.println("Connected to: " + address);

				// Connection success! StartHandler listening
				executorService.execute(new Listener());
				executorService.execute(new Sender());

				asyncSendMessage(new ShakeMessage("Hey there ;)", NetworkService.getNetworkManager().getPort()));

			} catch (IOException e) {
				System.out.println("Failed to connect to " + address);
				NetworkService.getNetworkManager().asyncEnqueue(new ConnectionFailedMessage(this));
				return false;
			}

			return true;
	}


	/**
	 * Sends the given method to the node represented by this instance.
	 *
	 * @param message
	 */
	public void asyncSendMessage(AbstractMessage message) {

		System.out.println("Sending message to peer!");

		try {
			outQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
			// TODO: Handle this?
		}
	}


	/**
	 *
	 * @return
	 *   Deep copy of address.
	 */
	public IPAddress getAddress() {
		return new IPAddress(address.ip, address.port);
	}

	public IPAddress getListeningAddress(){
		IPAddress addr = this.getAddress();
		addr.port = this.listenPort;
		return addr;
	}

	public void shutDown() {

		System.out.println("SHUTDOWN WAS CALLED ON NODE: " + getListeningAddress());
		shutDown = true;
		executorService.shutdown();

		try {
			executorService.awaitTermination(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			executorService.shutdownNow();
		}

		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}

	public int getListenPort() {
		return listenPort;
	}

	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}

	public void setLocalAddress(IPAddress localAddress) {
		this.localAddress = localAddress;
		this.address = localAddress;
	}

	public void setAddress(IPAddress address) {
		this.address = address;
	}

	public IPAddress getLocalAddress() {
		return localAddress;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PeerNode)) return false;
		PeerNode peerNode = (PeerNode) o;

		if (NetworkService.getNetworkManager().inSeedMode()){
			return getLocalAddress().equals(peerNode.getLocalAddress()) && listenPort == peerNode.getListenPort();
		} else {
			return getListeningAddress().equals(peerNode.getListeningAddress());
		}
	}


	@Override
	public int hashCode() {
		return Objects.hash(getListeningAddress());
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
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Got an EOFException.");
				System.out.println(new Date().getTime());
				//NetworkService.getNetworkManager().asyncEnqueue(new ConnectionLostMessage(PeerNode.this));
			} catch (IOException e) {
				System.out.println(new Date().getTime());
				//e.printStackTrace();
				NetworkService.getNetworkManager().asyncEnqueue(new ConnectionLostMessage(PeerNode.this));
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
				NetworkService.getNetworkManager().asyncEnqueue(new ConnectionLostMessage(PeerNode.this));
			}

			while (!shutDown) {

				AbstractMessage message = null;

				try {

					message = outQueue.take();
					stream.writeObject(message);

				} catch (InterruptedException | IOException e) {
					//e.printStackTrace();
					NetworkService.getNetworkManager().asyncEnqueue(new SendFailMessage(PeerNode.this,message ));
				}
			}
		}
	}

}
