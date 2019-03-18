package distributeblocks.net.message;

import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.AliveCheckProcessor;

public class AliveCheckMessage extends AbstractMessage {
	@Override
	public AbstractMessageProcessor getProcessor() {
		return new AliveCheckProcessor();
	}
}
