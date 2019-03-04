package distributeblocks.net.processor;

import distributeblocks.Block;
import distributeblocks.Node;
import distributeblocks.net.message.BlockMessage;
import distributeblocks.net.message.RequestBlockMessage;

public class RequestBlockProcessor extends AbstractMessageProcessor<RequestBlockMessage> {
	@Override
	public void processMessage(RequestBlockMessage message) {
		System.out.println("Got block request");

		// Send em the block.
		for (Block b : Node.getBlockchain()){
			if (b.getHashBlock().equals(message.blockHash)){
				message.senderNode.asyncSendMessage(new BlockMessage(b, message.blockHeight));
				break;
			}
		}
	}
}
