package distributeblocks.net.processor;

import distributeblocks.net.message.ConnectionFailedMessage;

public class ConnectionFailedProcessor extends AbstractMessageProcessor<ConnectionFailedMessage> {

	@Override
	public void processMessage(ConnectionFailedMessage message) {
		System.out.println("Got connection failed message.");

		// TODO: Need to make sure to grab new peers or try to reastablish.
	}
}
