package distributeblocks.net.processor;

import distributeblocks.net.message.AbstractMessage;

public abstract class AbstractMessageProcessor<T extends AbstractMessage> {

	public abstract void processMessage(T message);

}
