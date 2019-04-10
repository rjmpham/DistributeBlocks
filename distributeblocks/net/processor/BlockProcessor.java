package distributeblocks.net.processor;

import distributeblocks.net.NetworkService;
import distributeblocks.net.message.BlockMessage;
import distributeblocks.io.Console;

public class BlockProcessor extends AbstractMessageProcessor<BlockMessage> {
	@Override
	public void processMessage(BlockMessage message) {

		Console.log("Got block: " + message.blockHeight);

		// TODO: Remove transactions from the pool that were added in this block.

		NetworkService.getNetworkManager().gotBlock(message);

	}
}
