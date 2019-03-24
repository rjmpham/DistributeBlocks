package distributeblocks.net.message;

import distributeblocks.Block;
import distributeblocks.net.processor.AbstractMessageProcessor;

public class MiningFinishedMessage extends AbstractMessage {


    public Block block;

    public MiningFinishedMessage(Block block) {
        this.block = block;
    }

    @Override
    public AbstractMessageProcessor getProcessor() {
        return null;
    }
}
