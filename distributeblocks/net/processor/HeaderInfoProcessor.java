package distributeblocks.net.processor;

import distributeblocks.net.NetworkService;
import distributeblocks.net.message.AbstractMessage;
import distributeblocks.net.message.HeaderInfoMessage;
import distributeblocks.io.Console;

public class HeaderInfoProcessor extends AbstractMessageProcessor<HeaderInfoMessage> {
	@Override
	public void processMessage(HeaderInfoMessage message) {

		Console.log("Got block headers!");
		NetworkService.getNetworkManager().addBlockHeader(message.blockHeader);
	}
}
