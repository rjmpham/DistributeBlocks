package distributeblocks.net.processor;

import distributeblocks.net.NetworkService;
import distributeblocks.net.message.TransactionBroadcastMessage;
import distributeblocks.io.Console;

public class TransactionBroadcastMessageProcessor extends AbstractMessageProcessor<TransactionBroadcastMessage> {
	@Override
	public void processMessage(TransactionBroadcastMessage message) {
		Console.log("Got transaction broadcast message.");

		NetworkService.getNetworkManager().addTransaction(message.transaction);
	}
}
