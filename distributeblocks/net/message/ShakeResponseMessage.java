package distributeblocks.net.message;

import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.ShakeResponseProcessor;

public class ShakeResponseMessage extends AbstractMessage {


	public String messsage;

	public ShakeResponseMessage(String messsage) {
		this.messsage = messsage;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new ShakeResponseProcessor();
	}
}
