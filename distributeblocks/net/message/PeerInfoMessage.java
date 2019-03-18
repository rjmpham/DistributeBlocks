package distributeblocks.net.message;

import distributeblocks.net.IPAddress;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.PeerInfoProcessor;

import java.util.ArrayList;

public class PeerInfoMessage extends AbstractMessage {


	public ArrayList<IPAddress> peerAddresses;

	public PeerInfoMessage(ArrayList<IPAddress> peerAddresses) {
		this.peerAddresses = peerAddresses;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new PeerInfoProcessor();
	}
}
