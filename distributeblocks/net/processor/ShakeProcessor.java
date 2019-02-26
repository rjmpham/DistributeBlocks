package distributeblocks.net.processor;

import distributeblocks.net.message.ShakeMessage;

public class ShakeProcessor extends AbstractMessageProcessor<ShakeMessage> {


	public void processMessage(ShakeMessage message) {
		System.out.println("Got a shake message from " + message.senderIP + ". AbstractMessage: " + message.getShakeMessage());
	}
}
