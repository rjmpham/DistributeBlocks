package distributeblocks.net.message;

import distributeblocks.net.PeerNode;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.ConnectionLostProcessor;

public class ConnectionLostMessage extends AbstractMessage {


	PeerNode peerNode;


	public ConnectionLostMessage(PeerNode peerNode) {
		this.peerNode = peerNode;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new ConnectionLostProcessor();
	}
}
