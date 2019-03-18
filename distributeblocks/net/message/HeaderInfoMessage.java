package distributeblocks.net.message;

import distributeblocks.BlockHeader;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.HeaderInfoProcessor;

import java.util.ArrayList;

public class HeaderInfoMessage extends AbstractMessage {

	public ArrayList<BlockHeader> blockHeader;

	public HeaderInfoMessage(ArrayList<BlockHeader> blockHeader) {
		this.blockHeader = blockHeader;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new HeaderInfoProcessor();
	}
}
