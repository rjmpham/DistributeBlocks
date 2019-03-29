package distributeblocks.net.message;

import distributeblocks.Block;
import distributeblocks.mining.Miner;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.MiningFinishedProcessor;

public class MiningFinishedMessage extends AbstractMessage {


    public Block block;
    public Miner miner;

    public MiningFinishedMessage(Block block, Miner miner) {
        this.block = block;
        this.miner = miner;
    }

    @Override
    public AbstractMessageProcessor getProcessor() {
        return new MiningFinishedProcessor();
    }
}
