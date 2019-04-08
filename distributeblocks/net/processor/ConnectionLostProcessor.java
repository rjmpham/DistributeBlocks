package distributeblocks.net.processor;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.ConnectionLostMessage;
import distributeblocks.io.Console;

public class ConnectionLostProcessor extends AbstractMessageProcessor<ConnectionLostMessage> {
	@Override
	public void processMessage(ConnectionLostMessage message) {
		Console.log("Lost connection to: " + message.peerNode.getListeningAddress());

		NetworkService.getNetworkManager().removeNode(message.peerNode); 
		NetworkService.getNetworkManager().removeTemporaryNode(message.peerNode);

		if (!NetworkService.getNetworkManager().inSeedMode()) {
			new ConfigManager().removeNodeAndWrite(message.peerNode);
		}
	}
}
