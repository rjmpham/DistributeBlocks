package distributeblocks.net.message;

import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkService;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.PeerInfoProcessor;

import java.util.ArrayList;

public class PeerInfoMessage extends AbstractMessage {


	public ArrayList<IPAddress> peerAddresses;
	public boolean seedNode;

	public PeerInfoMessage(ArrayList<IPAddress> peerAddresses) {
		this.peerAddresses = peerAddresses;
		seedNode = NetworkService.getNetworkManager().inSeedMode();
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new PeerInfoProcessor();
	}
}
