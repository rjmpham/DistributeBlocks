package distributeblocks.net.message;

import distributeblocks.net.IPAddress;
import distributeblocks.net.processor.AbstractMessageProcessor;

public class MonitorDataMessage extends AbstractMessage {

	AbstractMessage message;


	public MonitorDataMessage(AbstractMessage message) {
		this.message = message;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return null;
	}
}
