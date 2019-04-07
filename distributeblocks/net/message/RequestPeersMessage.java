package distributeblocks.net.message;

import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkService;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.RequestPeersProcessor;

public class RequestPeersMessage extends AbstractMessage {


	public IPAddress localAddress;
	public boolean friend;


	/**
	 *
	 * @param friends
	 *   Its kind of a bad system, but this variable is set so that
	 *   nodes disconnect  from eachother if they dont want to be friends.
	 *   THe process is:
	 *       ShakeMessage -> ShakeResponse -> RequestPeers -> PeerInfo -> *disconnect if friend in PeerInfo is false*
	 */
	public RequestPeersMessage(boolean friends){
		localAddress = NetworkService.getNetworkManager().getLocalAddr();
		this.friend = friends;
	}

	public RequestPeersMessage() {
		localAddress = NetworkService.getNetworkManager().getLocalAddr();
		friend = true;
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new RequestPeersProcessor();
	}
}
