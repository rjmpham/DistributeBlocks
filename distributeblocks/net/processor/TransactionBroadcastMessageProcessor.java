package distributeblocks.net.processor;

import distributeblocks.net.NetworkService;
import distributeblocks.net.message.TransactionBroadcastMessage;

public class TransactionBroadcastMessageProcessor extends AbstractMessageProcessor<TransactionBroadcastMessage> {
	@Override
	public void processMessage(TransactionBroadcastMessage message) {
		System.out.println("Got transaction broadcast message.");

		NetworkService.getNetworkManager().addTransaction(message.transaction);
	}
}
