package distributeblocks.net.processor;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.PeerNode;
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
		} else {
			//message.senderNode.shutDown(); // Dont shutdown you foo.

			if (!NetworkService.getNetworkManager().isConnectedToNode(message.senderNode.getListeningAddress())) {
				ConfigManager configManager = new ConfigManager();
				configManager.removeNodeAndWrite(message.senderNode);
			}
		}
	}
}
