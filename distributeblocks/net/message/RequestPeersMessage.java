package distributeblocks.net.message;

import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkService;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.RequestPeersProcessor;

public class RequestPeersMessage extends AbstractMessage {


	IPAddress localAddress;

	public RequestPeersMessage() {
		localAddress = NetworkService.getNetworkManager().getLocalAddr();
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new RequestPeersProcessor();
	}
}
