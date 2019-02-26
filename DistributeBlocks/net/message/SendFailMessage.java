package distributeblocks.net.message;

import distributeblocks.net.PeerNode;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.SendFailProcessor;

public class SendFailMessage extends AbstractMessage {


	public PeerNode peerNode;

	public SendFailMessage(PeerNode peerNode) {
		this.peerNode = peerNode;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new SendFailProcessor();
	}
}
