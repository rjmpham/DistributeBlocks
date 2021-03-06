package distributeblocks.net.message;

import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkService;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.ShakeProcessor;

public class ShakeMessage extends AbstractMessage {


	public String shakeMessage;
	public int listeningPort;
	public IPAddress localAddress;


	/**
	 *
	 * @param shakeMessage
	 * @param listeningPort
	 *   This is the port the node will be listening on (the sender of the message).
	 */
	public ShakeMessage(String shakeMessage, int listeningPort) {
		this.shakeMessage = shakeMessage;
		this.listeningPort = listeningPort;
		localAddress = NetworkService.getLocalAddress();
	}

	public String getShakeMessage() {
		return shakeMessage;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new ShakeProcessor();
	}

}
