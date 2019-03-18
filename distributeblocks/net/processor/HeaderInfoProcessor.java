package distributeblocks.net.processor;

import distributeblocks.net.NetworkService;
import distributeblocks.net.message.AbstractMessage;
import distributeblocks.net.message.HeaderInfoMessage;

public class HeaderInfoProcessor extends AbstractMessageProcessor<HeaderInfoMessage> {
	@Override
	public void processMessage(HeaderInfoMessage message) {

		System.out.println("Got block headers!");
		NetworkService.getNetworkManager().addBlockHeader(message.blockHeader);
	}
}
