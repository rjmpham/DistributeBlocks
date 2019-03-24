package distributeblocks.net.processor;

import distributeblocks.net.message.MiningFinishedMessage;

public class MiningFinishedProcessor extends AbstractMessageProcessor<MiningFinishedMessage> {
    @Override
    public void processMessage(MiningFinishedMessage message) {
        System.out.println("Finished mining.");
    }
}
