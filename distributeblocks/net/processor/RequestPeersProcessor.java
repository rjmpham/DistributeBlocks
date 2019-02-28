package distributeblocks.net.processor;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.PeerNode;
import distributeblocks.net.message.PeerInfoMessage;
import distributeblocks.net.message.RequestPeersMessage;

import java.util.ArrayList;
import java.util.Random;

public class RequestPeersProcessor extends AbstractMessageProcessor<RequestPeersMessage> {
	@Override
	public void processMessage(RequestPeersMessage message) {

		// TODO: Disconnect from the node if its a seed node!!!!!!!!!!!!! Actualy maybe seed ndoe should just do the disconnecting.

		System.out.println("Got a request peers message from: " + message.senderNode.getAddress());
		NetworkManager networkManager = NetworkService.getNetworkManager();
		ArrayList<IPAddress> addresses = new ArrayList<>();

		if (networkManager.inSeedMode()){
			// TODO: In seed mode we want to make some intelligent descisions on which addresses to send probably?
			// Fuck it, using random number generator!
			ConfigManager configManager = new ConfigManager();
			ArrayList<PeerNode> nodes = configManager.readPeerNodes();

			if (nodes.size() > 0) {

				// TODO: Add a configure for the ammount of ndoes that get send back in seed mode?
				int adressShareCount = Math.min(nodes.size(), 10);
				Random ran = new Random();

				while (addresses.size() < adressShareCount) {
					addresses.add(nodes.remove(ran.nextInt(Math.max(nodes.size() - 1, 1))).getAddress());
				}
			}

		} else {

			// Use connected peers only, dont want to try and set people up with dead friends.
			for (PeerNode p : NetworkService.getNetworkManager().getPeerNodes()){

				IPAddress address = p.getAddress();
				address.port = p.getListenPort(); // So that they will connect to the ServerSocket of the node.

				addresses.add(address);
			}
		}

		// Send them off woo that was easy.
		message.senderNode.asyncSendMessage(new PeerInfoMessage(addresses));
	}
}
