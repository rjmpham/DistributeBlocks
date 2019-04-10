package distributeblocks;

import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkService;
import distributeblocks.net.PeerNode;
import distributeblocks.net.message.*;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
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
	MultiGraph graph;

	NodeGraphGenerator generator;

	//HashMap<String, Boolean> connectedNodes;


	public NetworkMonitor() {

		executorService = Executors.newCachedThreadPool();
		graph = new MultiGraph("Network Graph");
		generator = new NodeGraphGenerator();
		generator.addSink(graph);
		graph.display();


		// First need to discover at least one node on the network
		Thread thread = new Thread(new NodeAnnouncer());
		thread.start();

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
			System.out.println("Starting listener on: " + socket.getInetAddress().getHostAddress().toString() + ":" + socket.getPort());
		}

		@Override
		public void run() {


			try {

				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

				while (true) {


					System.out.println("Waiting for a message");
					MonitorDataMessage message = (MonitorDataMessage) input.readObject();
					System.out.println("Got a message");




					address = message.listeningAddress.toString();

					if (graph.getNode(address) == null) {
						graph.addNode(address);
					}



					for (IPAddress address : message.connectedPeers) {
						String edgeId = message.listeningAddress.toString() + address.toString();
						String node1 = message.listeningAddress.toString();
						String node2 = message.listeningAddress.toString();

						try {
							if (graph.getNode(address.toString()) == null) {
								graph.addNode(address.toString());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						String id = message.listeningAddress.toString() + address.toString();

						try {
							if (graph.getEdge(id) == null) {
								graph.addEdge(id, message.listeningAddress.toString(), address.toString());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						//graph.
					}


					System.out.println("got to end of loop");
				}

			} catch (Exception e){
				e.printStackTrace();

				graph.removeNode(address);
			}


			System.out.println("Exiting listener.");
		}
	}

	private class NodeAnnouncer implements Runnable {


		@Override
		public void run() {


			//ArrayList<ObjectOutputStream> outs = new ArrayList<>();

			while (true) {

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				for (PeerNode node : NetworkService.getNetworkManager().getPeerNodes()) {

					node.asyncSendMessage(new MonitorNotifierMessage());
				}
			}
		}
	}
}
