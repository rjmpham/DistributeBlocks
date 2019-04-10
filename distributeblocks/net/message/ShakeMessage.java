package distributeblocks.net.message;

import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkService;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.ShakeProcessor;
import java.security.*;

public class ShakeMessage extends AbstractMessage {


	public String shakeMessage;
	public int listeningPort;
	public IPAddress localAddress;
	public PublicKey publicKey;
	public String alias;


	/**
	 *
	 * @param shakeMessage
	 * @param listeningPort
	 *   This is the port the node will be listening on (the sender of the message).
	 */
	public ShakeMessage(String shakeMessage, PublicKey publicKey, String alias, int listeningPort) {
		this.shakeMessage = shakeMessage;
		this.publicKey = publicKey;
		this.alias = alias;
		this.listeningPort = listeningPort;
		localAddress = NetworkService.getNetworkManager().getLocalAddr();
	}

	public String getShakeMessage() {
		return shakeMessage;
	}
	
	public PublicKey getPublicKey() {
		return publicKey;
	}
	
	public String getAlias() {
		return alias;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new ShakeProcessor();
	}

}
