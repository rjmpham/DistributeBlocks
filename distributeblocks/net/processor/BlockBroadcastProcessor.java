package distributeblocks.net.processor;

import distributeblocks.net.message.BlockBroadcastMessage;

public class BlockBroadcastProcessor extends AbstractMessageProcessor<BlockBroadcastMessage> {
    @Override
    public void processMessage(BlockBroadcastMessage message) {
        System.out.println("Got a block broadcast.");
    }
}
