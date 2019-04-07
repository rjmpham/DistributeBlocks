package distributeblocks.net.message;

import distributeblocks.net.NetworkService;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.ShakeResponseProcessor;

public class ShakeResponseMessage extends AbstractMessage {


	public String messsage;
	public int listeningPort;
	public boolean letsBeFriends;
	public boolean seedNode;

	/**
	 *
	 * @param messsage
	 * @param listeningPort
	 *    The port that the sender is listening on.
	 */
	public ShakeResponseMessage(String messsage, int listeningPort, boolean letsBeFriends) {
		this.messsage = messsage;
		this.listeningPort = listeningPort;
		this.letsBeFriends = letsBeFriends;

		seedNode = NetworkService.getNetworkManager().inSeedMode();
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new ShakeResponseProcessor();
	}
}
