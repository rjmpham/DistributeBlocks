package distributeblocks.net.processor;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.ConnectionLostMessage;

public class ConnectionLostProcessor extends AbstractMessageProcessor<ConnectionLostMessage> {
	@Override
	public void processMessage(ConnectionLostMessage message) {
		System.out.println("Lost connection to: " + message.peerNode.getListeningAddress());

		NetworkService.getNetworkManager().removeNode(message.peerNode); 
		NetworkService.getNetworkManager().removeTemporaryNode(message.peerNode);

		if (!NetworkService.getNetworkManager().inSeedMode()) {
			new ConfigManager().removeNodeAndWrite(message.peerNode);
		}
	}
}
