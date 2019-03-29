package distributeblocks.net.message;

import distributeblocks.Block;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.BlockBroadcastProcessor;

public class BlockBroadcastMessage extends AbstractMessage {


    public Block block;

    public BlockBroadcastMessage(Block block) {
        this.block = block;
    }

    @Override
    public AbstractMessageProcessor getProcessor() {
        return new BlockBroadcastProcessor();
    }
}
