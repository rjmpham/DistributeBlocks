package distributeblocks.net.processor;

import distributeblocks.net.message.ShakeResponseMessage;

public class ShakeResponseProcessor extends AbstractMessageProcessor<ShakeResponseMessage> {
	@Override
	public void processMessage(ShakeResponseMessage message) {
		System.out.println("Got shake response: " + message.messsage);

		message.senderNode.setListenPort(message.listeningPort);
	}
}
