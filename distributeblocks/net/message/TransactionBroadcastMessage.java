package distributeblocks.net.message;

import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.TransactionBroadcastMessageProcessor;

public class TransactionBroadcastMessage extends AbstractMessage {
	@Override
	public AbstractMessageProcessor getProcessor() {
		return new TransactionBroadcastMessageProcessor();
	}
}
