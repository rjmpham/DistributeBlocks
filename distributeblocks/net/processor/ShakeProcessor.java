package distributeblocks.net.processor;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.PeerNode;
import distributeblocks.net.message.ShakeMessage;
import distributeblocks.net.message.ShakeResponseMessage;

import java.util.Date;
import java.util.HashMap;

public class ShakeProcessor extends AbstractMessageProcessor<ShakeMessage> {


	public void processMessage(ShakeMessage message) {

		System.out.println("Listening port: " + message.listeningPort);
		message.senderNode.setListenPort(message.listeningPort);
		message.senderNode.setLocalAddress(message.localAddress);

		System.out.println("Got a shake message from " + message.senderNode.getAddress() + ". AbstractMessage: " + message.getShakeMessage());

		boolean wantMoreFriends = NetworkService.getNetworkManager().inSeedMode() ?
				false : NetworkService.getNetworkManager().canHaveMorePeers();

		if (NetworkService.getNetworkManager().isConnectedToNode(message.senderNode.getListeningAddress())){
			wantMoreFriends = false;
		}

		message.senderNode.asyncSendMessage(new ShakeResponseMessage("Hey back at ya ;): WantMoreFriend: " + wantMoreFriends,
				NetworkService.getNetworkManager().getPort(), wantMoreFriends));

		if (wantMoreFriends) {
			NetworkService.getNetworkManager().addNode(message.senderNode);
			NetworkService.getNetworkManager().removeTemporaryNode(message.senderNode);
		}

		//NetworkService.getNetworkManager().removeTemporaryNode(message.senderNode);
		// Remove the node in the peer info processor.


		if (NetworkService.getNetworkManager().inSeedMode()){

			ConfigManager configManager = new ConfigManager();
			configManager.addNodeAndWrite(message.senderNode);
			System.out.println("Adding node in seed mode.");

			// Also record this connection time in our timeouts file
			HashMap<String, Long> timeouts = configManager.readTimeoutFile();

			System.out.println(timeouts == null);

			timeouts.put(message.senderNode.getListeningAddress().toString(), new Date().getTime());
			System.out.println("Writing to timeouts file");
			configManager.writeTimeoutFile(timeouts);
		}
	}
}
