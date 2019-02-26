package distributeblocks.net.message;

import distributeblocks.net.PeerNode;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.ConnectionFailedProcessor;

public class ConnectionFailedMessage extends AbstractMessage {

	public PeerNode peerNode;

	public ConnectionFailedMessage(PeerNode peerNode) {
		this.peerNode = peerNode;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new ConnectionFailedProcessor();
	}
}
