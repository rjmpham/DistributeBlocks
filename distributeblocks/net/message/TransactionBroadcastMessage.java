package distributeblocks.net.message;

import distributeblocks.Transaction;
import distributeblocks.TransactionOut;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.TransactionBroadcastMessageProcessor;

public class TransactionBroadcastMessage extends AbstractMessage {

	public Transaction transaction;

	public TransactionBroadcastMessage(Transaction transaction) {
		this.transaction = transaction;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new TransactionBroadcastMessageProcessor();
	}
}
