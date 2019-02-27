package distributeblocks.net.message;

import distributeblocks.net.PeerNode;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.SendFailProcessor;

public class SendFailMessage extends AbstractMessage {


	public PeerNode peerNode;
	public AbstractMessage message;

	public SendFailMessage(PeerNode peerNode, AbstractMessage message) {
		this.peerNode = peerNode;
		this.message = message;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new SendFailProcessor();
	}
}
