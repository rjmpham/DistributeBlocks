package distributeblocks.net.processor;

import distributeblocks.net.NetworkService;
import distributeblocks.net.message.ShakeMessage;
import distributeblocks.net.message.ShakeResponseMessage;

public class ShakeProcessor extends AbstractMessageProcessor<ShakeMessage> {


	public void processMessage(ShakeMessage message) {
		System.out.println("Got a shake message from " + message.senderNode.getAddress() + ". AbstractMessage: " + message.getShakeMessage());

		message.senderNode.sendMessage(new ShakeResponseMessage("Hey back at ya ;)"));

	}
}
