package distributeblocks.net.processor;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.PeerNode;
import distributeblocks.net.message.PeerInfoMessage;
import distributeblocks.net.message.RequestPeersMessage;
import distributeblocks.io.Console;

import java.util.ArrayList;
import java.util.Random;

public class RequestPeersProcessor extends AbstractMessageProcessor<RequestPeersMessage> {
	@Override
	public void processMessage(RequestPeersMessage message) {

		Console.log("Got a request peers message from: " + message.senderNode.getAddress());
		NetworkManager networkManager = NetworkService.getNetworkManager();
		ArrayList<IPAddress> addresses = new ArrayList<>();
		message.senderNode.setLocalAddress(message.localAddress);

		if (networkManager.inSeedMode()){
			ConfigManager configManager = new ConfigManager();
			ArrayList<PeerNode> nodes = configManager.readPeerNodes();

			if (nodes.size() > 0) {
				int adressShareCount = Math.min(nodes.size(), 10);
				Random ran = new Random();

				while (nodes.size() > 0 && addresses.size() < adressShareCount) {

					PeerNode node =  nodes.remove(ran.nextInt(Math.max(nodes.size() - 1, 1)));
					IPAddress addr = node.getListeningAddress();

					// Dont send them the address if its their own address.
					if (addr.port > 0 && !addr.equals(message.senderNode.getListeningAddress())) {
						addresses.add(addr);
					}
				}
			}

		} else {

			// Use connected peers only, dont want to try and set people up with dead friends.
			for (PeerNode p : NetworkService.getNetworkManager().getPeerNodes()){

				IPAddress address = p.getListeningAddress();

				if (address.port > 0 && !address.equals(message.senderNode.getListeningAddress())) {
					addresses.add(address);
				}
			}
		}

		// Send them off woo that was easy.
		message.senderNode.asyncSendMessage(new PeerInfoMessage(addresses, message.friend));

	}
}
