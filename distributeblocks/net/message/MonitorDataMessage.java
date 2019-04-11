package distributeblocks.net.message;

import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkService;
import distributeblocks.net.processor.AbstractMessageProcessor;

import java.util.ArrayList;

public class MonitorDataMessage extends AbstractMessage {

	public AbstractMessage message;
	public IPAddress recipient;
	public IPAddress listeningAddress;
	public ArrayList<IPAddress> connectedPeers;


	public MonitorDataMessage(AbstractMessage message, ArrayList<IPAddress> connectedPeers, IPAddress recipient) {
		this.listeningAddress = NetworkService.getNetworkManager().getLocalAddr();
		this.message = message;
		this.connectedPeers = connectedPeers;
		this.recipient = recipient;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return null;
	}
}
