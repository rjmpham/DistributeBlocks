package distributeblocks.net.message;

import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.RequestBlockProcessor;

public class RequestBlockMessage extends AbstractMessage {

	public String blockHash;
	public int blockHeight;

	public RequestBlockMessage(String blockHash, int blockHeight) {
		this.blockHash = blockHash;
		this.blockHeight = blockHeight;

		System.out.println("Requesting block: " + blockHeight);
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new RequestBlockProcessor();
	}
}
