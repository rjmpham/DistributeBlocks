package distributeblocks.net.processor;

import distributeblocks.net.message.ConnectionLostMessage;

public class ConnectionLostProcessor extends AbstractMessageProcessor<ConnectionLostMessage> {
	@Override
	public void processMessage(ConnectionLostMessage message) {
		System.out.println("Got connection lost message.");

		// TODO: Need to make sure to grab new peers or try to reastablish.
	}
}
