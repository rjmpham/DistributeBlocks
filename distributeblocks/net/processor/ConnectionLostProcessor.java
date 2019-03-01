package distributeblocks.net.processor;

import distributeblocks.net.NetworkService;
import distributeblocks.net.message.ConnectionLostMessage;

public class ConnectionLostProcessor extends AbstractMessageProcessor<ConnectionLostMessage> {
	@Override
	public void processMessage(ConnectionLostMessage message) {
		System.out.println("Lost connetion to: " + message.peerNode.getListeningAddress());

		// TODO: Need to make sure to grab new peers or try to reastablish.
		// This isnt needed anymore since the networkManager is checking if it needs new peers periodically

		NetworkService.getNetworkManager().removeNode(message.peerNode); // TODO: Maybe try to re-establish instead of removing?
	}
}
