package distributeblocks.net.processor;

import distributeblocks.Block;
import distributeblocks.BlockChain;
import distributeblocks.Node;
import distributeblocks.net.message.BlockMessage;
import distributeblocks.net.message.RequestBlockMessage;

public class RequestBlockProcessor extends AbstractMessageProcessor<RequestBlockMessage> {
	@Override
	public void processMessage(RequestBlockMessage message) {
		System.out.println("Got block request");

		// Send em the block.
		message.senderNode.asyncSendMessage(new BlockMessage(new BlockChain().getAllBlocks().get(message.blockHash), message.blockHeight));

	}
}


