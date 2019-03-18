package distributeblocks.net.processor;

import distributeblocks.net.message.AliveCheckMessage;

public class AliveCheckProcessor extends AbstractMessageProcessor<AliveCheckMessage> {
	@Override
	public void processMessage(AliveCheckMessage message) {
		System.out.println("Got an alive check from: " + message.senderNode.getAddress());
	}
}
