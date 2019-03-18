package distributeblocks.net.message;

import distributeblocks.Block;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.BlockProcessor;

public class BlockMessage extends AbstractMessage {

	public Block block;
	public int blockHeight;

	public BlockMessage(Block block, int blockHeight) {
		this.block = block;
		this.blockHeight = blockHeight;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new BlockProcessor();
	}
}
