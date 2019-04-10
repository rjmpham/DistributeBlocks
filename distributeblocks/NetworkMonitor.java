package distributeblocks;

import distributeblocks.net.IPAddress;
import distributeblocks.net.message.*;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.SourceBase;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkMonitor {


	ExecutorService executorService;
	ServerSocket serverSocket;
	SingleGraph graph;
	NodeGraphGenerator generator;

	HashMap<String, Boolean> connectedNodes;


	public NetworkMonitor() {

		executorService = Executors.newCachedThreadPool();
		connectedNodes = new HashMap<>();
		graph = new SingleGraph("Network Graph");
		generator = new NodeGraphGenerator();
		generator.addSink(graph);
		graph.display();


		// First need to discover at least one node on the network
		discoverNodes();


		try (ServerSocket serverSocket = new ServerSocket(Node.MONITOR_PORT)) {


			while (true) {

				Socket socket = serverSocket.accept();


				final String key = socket.getInetAddress().toString() + socket.getPort();

				System.out.println(key);



				executorService.submit(new MonitorListener(socket));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void discoverNodes(){

		System.out.println("Attempting to connect to seed");
		try (Socket socket = new Socket("165.22.129.19", 3271)) {

			System.out.println("connected to seed");
			ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

			outputStream.writeObject(new ShakeMessage("hello", Node.MONITOR_PORT));
			outputStream.writeObject(new RequestPeersMessage());

			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

			ShakeResponseMessage shakeResponseMessage = (ShakeResponseMessage) inputStream.readObject();
			PeerInfoMessage message = (PeerInfoMessage)inputStream.readObject();

			executorService.submit(new NodeAnnouncer(message.peerAddresses));

		} catch (Exception e) {
			e.printStackTrace();
			discoverNodes();
		}
	}

	private class NodeGraphGenerator extends SourceBase implements Generator{

		int currentIndex = 0;
		int edgeId = 0;


		@Override
		public void begin() {
			addNode();
		}

		@Override
		public boolean nextEvents() {
			addNode();
			return true;
		}

		@Override
		public void end() {
			// Nothing to do
		}

		protected void addNode() {
			sendNodeAdded(sourceId, Integer.toString(currentIndex));

			for (int i = 0; i < currentIndex; i++)
				sendEdgeAdded(sourceId, Integer.toString(edgeId++),
						Integer.toString(i), Integer.toString(currentIndex), false);

			currentIndex++;
		}
	}

	private class MonitorListener implements Runnable {


		Socket socket;
		String address;


		public MonitorListener(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {


			try {
				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

				while (true) {


					try {

						System.out.println("Waiting for a message");
						MonitorDataMessage message = (MonitorDataMessage) input.readObject();
						System.out.println("Got a message");

						if (!connectedNodes.containsKey(message.listeningAddress.toString())) {

							synchronized (connectedNodes) {
								connectedNodes.put(message.listeningAddress.toString(), true);
							}

							address = message.listeningAddress.toString();
							graph.addNode(address);
						}



						for (IPAddress address : message.connectedPeers){
							String edgeId = message.listeningAddress.toString() + address.toString();
							String node1 = message.listeningAddress.toString();
							String node2 = address.toString();

							graph.addEdge(message.listeningAddress.toString() + address.toString(), message.listeningAddress.toString(), address.toString());
						}

					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

					System.out.println("got to end of loop");
				}


			} catch (IOException e) {
				e.printStackTrace();
			} finally {

				if (address != null) {
					graph.removeNode(address);
				}

				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class NodeAnnouncer implements Runnable {

		ArrayList<IPAddress> addresses;
	//	ArrayList<Socket> connectedNodes;
		HashMap<Socket, ObjectOutputStream> connectedNodes;

		public NodeAnnouncer(ArrayList<IPAddress> addresses) {
			this.addresses = addresses;
		}

		@Override
		public void run() {


			//ArrayList<ObjectOutputStream> outs = new ArrayList<>();
			connectedNodes = new HashMap<>();

			for (IPAddress ip : addresses){

				Socket socket = null;

				try {
					System.out.println("Attempting a connection");
					socket = new Socket(ip.ip, ip.port);
					ObjectOutputStream o = new ObjectOutputStream(socket.getOutputStream());
					connectedNodes.put(socket, o);
					o.writeObject(new ShakeMessage("hello", Node.MONITOR_PORT));
					//outs.add(o);
				} catch (IOException e) {
					connectedNodes.remove(socket);
					e.printStackTrace();
				}
			}



			while (true){

				Socket currentSocket = null;

				try {
					Thread.sleep(2000);

					MonitorNotifierMessage msg = new MonitorNotifierMessage();

					for (Map.Entry<Socket, ObjectOutputStream> entry : connectedNodes.entrySet()){
						currentSocket = entry.getKey();
						entry.getValue().writeObject(msg);
					}


				} catch (InterruptedException e) {
					e.printStackTrace();
					discoverNodes();
					break;
				} catch (IOException e) {
					e.printStackTrace();
					discoverNodes();
					break;
				} finally {
				}


			}

		}
	}
}
