package distributeblocks.net.processor;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.PeerNode;
import distributeblocks.net.message.ShakeMessage;
import distributeblocks.net.message.ShakeResponseMessage;

import java.util.ArrayList;

public class ShakeProcessor extends AbstractMessageProcessor<ShakeMessage> {


	public void processMessage(ShakeMessage message) {
		System.out.println("Got a shake message from " + message.senderNode.getAddress() + ". AbstractMessage: " + message.getShakeMessage());

		System.out.println("Listening port: " + message.listeningPort);
		message.senderNode.asyncSendMessage(new ShakeResponseMessage("Hey back at ya ;)", NetworkService.getNetworkManager().getPort()));
		message.senderNode.setListenPort(message.listeningPort);

		if (NetworkService.getNetworkManager().inSeedMode()){

			// TODO: Reading the entire file, and then writing the entire file, is a bit inefficient.
			ConfigManager configManager = new ConfigManager();
			ArrayList<PeerNode> configNodes = configManager.readPeerNodes();
			configNodes.add(message.senderNode);
			configManager.writePeerNodes(configNodes);
		}
	}
}
