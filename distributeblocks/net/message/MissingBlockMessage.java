package distributeblocks.net.message;

import distributeblocks.Block;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.MissingBlockProcessor;

public class MissingBlockMessage extends AbstractMessage {


	public String missingBlockHash;


	public MissingBlockMessage(String missingBlockHash) {
		this.missingBlockHash = missingBlockHash;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new MissingBlockProcessor();
	}
}
