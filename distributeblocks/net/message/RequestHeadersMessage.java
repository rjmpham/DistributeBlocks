package distributeblocks.net.message;

import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.RequestHeadersProcessor;

public class RequestHeadersMessage extends AbstractMessage {
	@Override
	public AbstractMessageProcessor getProcessor() {
		return new RequestHeadersProcessor();
	}
}
