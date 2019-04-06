package distributeblocks.net.processor;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.RequestPeersMessage;
import distributeblocks.net.message.ShakeResponseMessage;

public class ShakeResponseProcessor extends AbstractMessageProcessor<ShakeResponseMessage> {
	@Override
	public void processMessage(ShakeResponseMessage message) {
		System.out.println("Got shake response: " + message.messsage);
		message.senderNode.setListenPort(message.listeningPort);

		if (message.letsBeFriends && NetworkService.getNetworkManager().needMorePeers() &&
				!NetworkService.getNetworkManager().isConnectedToNode(message.senderNode.getListeningAddress()) &&
				!NetworkService.getNetworkManager().isSeedNode(message.senderNode)){
			// Dont do anything, maintain connection?
			// Add it to the node config list silly!
			ConfigManager configManager = new ConfigManager();
			configManager.addNodeAndWrite(message.senderNode);
			NetworkService.getNetworkManager().addNode(message.senderNode);
			NetworkService.getNetworkManager().removeTemporaryNode(message.senderNode);

		}

		// If the other node wants to be friends or not, send a peer info request.
		// The node will get removed from the temporary pool in the PeerInfoProcessor
		message.senderNode.asyncSendMessage(new RequestPeersMessage());

		if (!message.seedNode) {
			//NetworkService.getNetworkManager().removeTemporaryNode(message.senderNode);
			// Dont remove from temporary, do that in the peerinfo processor
		}

	}
}
