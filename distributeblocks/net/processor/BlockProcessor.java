package distributeblocks.net.processor;

import distributeblocks.net.NetworkService;
import distributeblocks.net.message.BlockMessage;

public class BlockProcessor extends AbstractMessageProcessor<BlockMessage> {
	@Override
	public void processMessage(BlockMessage message) {

		System.out.println("Got block: " + message.blockHeight);

		NetworkService.getNetworkManager().gotBlock(message);

	}
}
