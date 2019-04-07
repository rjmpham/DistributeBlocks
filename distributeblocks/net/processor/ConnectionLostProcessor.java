package distributeblocks.net.processor;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.ConnectionLostMessage;

public class ConnectionLostProcessor extends AbstractMessageProcessor<ConnectionLostMessage> {
	@Override
	public void processMessage(ConnectionLostMessage message) {
		System.out.println("Lost connetion to: " + message.peerNode.getListeningAddress());

		NetworkService.getNetworkManager().removeNode(message.peerNode);
		new ConfigManager().removeNodeAndWrite(message.peerNode); // Just going to remove the note from known peers.
	}
}
