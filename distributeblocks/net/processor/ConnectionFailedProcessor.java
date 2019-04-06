package distributeblocks.net.processor;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.ConnectionFailedMessage;

public class ConnectionFailedProcessor extends AbstractMessageProcessor<ConnectionFailedMessage> {

	@Override
	public void processMessage(ConnectionFailedMessage message) {
		System.out.println("Got connection failed message.");

		// TODO: Need to make sure to grab new peers or try to reastablish.
		NetworkService.getNetworkManager().removeNode(message.peerNode);
		NetworkService.getNetworkManager().removeTemporaryNode(message.peerNode);
		new ConfigManager().removeNodeAndWrite(message.peerNode); // Just going to remove the note from known peers.
	}
}
