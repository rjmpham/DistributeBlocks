package distributeblocks;

import distributeblocks.io.Console;
import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkService;
import distributeblocks.net.PeerNode;
import distributeblocks.net.message.*;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.stream.SourceBase;
import org.graphstream.ui.view.Viewer;

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
import java.util.concurrent.*;

public class NetworkMonitor {


	ExecutorService executorService;
	ScheduledExecutorService scheduledExecutorService;
	ServerSocket serverSocket;
	MultiGraph graph;

	NodeGraphGenerator generator;
	ProxyPipe pipe;
	Viewer viewer;

	//HashMap<String, Boolean> connectedNodes;


	public NetworkMonitor() {

		executorService = Executors.newCachedThreadPool();
		scheduledExecutorService = Executors.newScheduledThreadPool(5);
		graph = new MultiGraph("Network Graph");
		generator = new NodeGraphGenerator();
		generator.addSink(graph);
		viewer = graph.display();
		pipe = viewer.newViewerPipe();
		pipe.addAttributeSink(graph);


		// First need to discover at least one node on the network
		Thread thread = new Thread(new NodeAnnouncer());
		thread.start();

		try (ServerSocket serverSocket = new ServerSocket(Node.MONITOR_PORT)) {


			while (true) {

				Socket socket = serverSocket.accept();

				final String key = socket.getInetAddress().toString() + socket.getPort();
				Console.log(key);
				executorService.submit(new MonitorListener(socket));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private class NodeGraphGenerator extends SourceBase implements Generator {

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
		ArrayList<String> edgeIDs;


		public MonitorListener(Socket socket) {
			this.socket = socket;
			Console.log("Starting listener on: " + socket.getInetAddress().getHostAddress().toString() + ":" + socket.getPort());
			edgeIDs = new ArrayList<>();
		}

		@Override
		public void run() {


			try {

				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

				while (true) {


					//System.out.println("Waiting for a message");
					MonitorDataMessage message = (MonitorDataMessage) input.readObject();
					//System.out.println("Got a message");


					address = message.listeningAddress.toString();

					if (graph.getNode(address) == null) {

						synchronized (graph) {
							org.graphstream.graph.Node gNode = graph.addNode(address);

							gNode.addAttribute("ui.style", "shape:circle;fill-color: blue;size: 25px; text-alignment: center;");
							gNode.addAttribute("ui.label", address);

							pipe.pump();
						}
					}


					synchronized (graph) {
						for (IPAddress address : message.connectedPeers) {

							try {
								if (graph.getNode(address.toString()) == null) {
									//graph.addNode(address.toString());
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

							String id = message.listeningAddress.toString() + address.toString();

							try {


								if (graph.getEdge(id) == null) {
									graph.addEdge(id, message.listeningAddress.toString(), address.toString());
									edgeIDs.add(id);
									pipe.pump();
								}


							} catch (Exception e) {
							//	e.printStackTrace();
							}

							//graph.
						}

						// Get the edge representing the communication path for this paticular message.

						try {

							Edge edge = graph.getEdge(message.listeningAddress + message.recipient.toString());

							if (edge != null){

								if (message.message instanceof BlockBroadcastMessage){
									edge.addAttribute("ui.style", "size: 5px; fill-color: green;");
									pipe.pump();
									scheduleThing(edge);
								} else if (message.message instanceof TransactionBroadcastMessage){
									edge.addAttribute("ui.style", "size: 5px; fill-color: orange;");
									pipe.pump();
									scheduleThing(edge);
								}


							}

						} catch (Exception e2){

						}
					}
					//System.out.println("got to end of loop");
				}

			} catch (Exception e) {
				e.printStackTrace();


				synchronized (graph) {
					try {
						graph.removeNode(address);
					} catch (Exception e2) {

					}

					for (String id : edgeIDs) {
						try {
							graph.removeEdge(id);

						} catch (Exception e2) {

						}
					}

					pipe.pump();
				}
			}


			System.out.println("Exiting listener.");
		}
	}

	private void scheduleThing(Edge edge){
		scheduledExecutorService.schedule(new Runnable() {
			@Override
			public void run() {

				synchronized (graph){
					edge.addAttribute("ui.style", "size: 1px; fill-color: back;");
					pipe.pump();
				}
			}
		}, 500, TimeUnit.MILLISECONDS);
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

	private abstract class GraphAction{

		public abstract void action();

	}

	//private class ActionProcessor
}
