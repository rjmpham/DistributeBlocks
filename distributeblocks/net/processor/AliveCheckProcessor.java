package distributeblocks.net.processor;

import distributeblocks.net.message.AliveCheckMessage;
import distributeblocks.io.Console;

public class AliveCheckProcessor extends AbstractMessageProcessor<AliveCheckMessage> {
	@Override
	public void processMessage(AliveCheckMessage message) {
		Console.log("Got an alive check from: " + message.senderNode.getAddress());
	}
}
