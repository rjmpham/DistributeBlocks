package distributeblocks.net.message;

import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.ShakeProcessor;

public class ShakeMessage extends AbstractMessage {


	private String shakeMessage;

	public ShakeMessage(String shakeMessage) {
		this.shakeMessage = shakeMessage;
	}

	public String getShakeMessage() {
		return shakeMessage;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new ShakeProcessor();
	}

}
