package distributeblocks.net.processor;

import distributeblocks.net.NetworkService;
import distributeblocks.net.message.BlockMessage;

public class BlockProcessor extends AbstractMessageProcessor<BlockMessage> {
	@Override
	public void processMessage(BlockMessage message) {

		System.out.println("Got block: " + message.blockHeight);

		// TODO: Remove transactions from the pool that were added in this block.

		NetworkService.getNetworkManager().gotBlock(message);

	}
}
