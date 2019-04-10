package distributeblocks.net.processor;

import distributeblocks.net.message.ServerCrashMessage;
import distributeblocks.io.Console;

public class ServerCrashProcessor extends AbstractMessageProcessor<ServerCrashMessage> {
	@Override
	public void processMessage(ServerCrashMessage message) {
		// Probably want to restart the server.
		// TODO: Add server restart method to NetworkManager

		Console.log("Got server crash message");

	}
}
