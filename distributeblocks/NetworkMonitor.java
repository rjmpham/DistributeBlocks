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
import java.util.HashMap;
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

				if (!connectedNodes.containsKey(key)) {

					synchronized (connectedNodes) {
						connectedNodes.put(key, true);
					}


					graph.addNode(key);
				}

				executorService.submit(new MonitorListener(socket));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void discoverNodes(){

		try (Socket socket = new Socket("localhost", 1234)) {

			ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
			outputStream.writeObject(new ShakeMessage("hello", Node.MONITOR_PORT));
			outputStream.writeObject(new RequestPeersMessage());

			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

			ShakeResponseMessage shakeResponseMessage = (ShakeResponseMessage) inputStream.readObject();
			PeerInfoMessage message = (PeerInfoMessage)inputStream.readObject();

			executorService.submit(new NodeAnnouncer(message.peerAddresses));

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
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


		public MonitorListener(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {


			try {
				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

				while (true) {


					try {
						AbstractMessage message = (AbstractMessage)input.readObject();



					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}


			} catch (IOException e) {
				e.printStackTrace();
			} finally {
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

		public NodeAnnouncer(ArrayList<IPAddress> addresses) {
			this.addresses = addresses;
		}

		@Override
		public void run() {


			ArrayList<Socket> sockets = new ArrayList<>();
			ArrayList<ObjectOutputStream> outs = new ArrayList<>();

			for (IPAddress ip : addresses){

				try {
					Socket socket = new Socket(ip.ip, ip.port);
					sockets.add(socket);
					ObjectOutputStream o = new ObjectOutputStream(socket.getOutputStream());
					o.writeObject(new ShakeMessage("hello", Node.MONITOR_PORT));
					outs.add(o);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}



			while (true){

				try {
					Thread.sleep(2000);


					MonitorNotifierMessage msg = new MonitorNotifierMessage();
					for (ObjectOutputStream out : outs){
						out.writeObject(msg);
					}


				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}


			}

		}
	}
}
