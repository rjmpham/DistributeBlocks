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
//	public ShakeMessage(String shakeMessage, int listeningPort, PublicKey publicKey, String alias) {
	public ShakeMessage(String shakeMessage, int listeningPort) {
		this.shakeMessage = shakeMessage;
		this.listeningPort = listeningPort;
//		this.publicKey = publicKey;
//		this.alias = alias;
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
