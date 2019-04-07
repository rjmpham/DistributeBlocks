package distributeblocks.net.processor;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.ConnectionLostMessage;

public class ConnectionLostProcessor extends AbstractMessageProcessor<ConnectionLostMessage> {
	@Override
	public void processMessage(ConnectionLostMessage message) {
		System.out.println("Lost connection to: " + message.peerNode.getListeningAddress());

		// TODO: Need to make sure to grab new peers or try to reastablish.
		// This isnt needed anymore since the networkManager is checking if it needs new peers periodically

		NetworkService.getNetworkManager().removeNode(message.peerNode); // TODO: Maybe try to re-establish instead of removing?
		NetworkService.getNetworkManager().removeTemporaryNode(message.peerNode);

		if (!NetworkService.getNetworkManager().inSeedMode()) {
			new ConfigManager().removeNodeAndWrite(message.peerNode); // Just going to remove the note from known peers.

		}
	}
}
