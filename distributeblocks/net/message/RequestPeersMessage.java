package distributeblocks.net.message;

import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.RequestPeersProcessor;

public class RequestPeersMessage extends AbstractMessage {
	@Override
	public AbstractMessageProcessor getProcessor() {
		return new RequestPeersProcessor();
	}
}
