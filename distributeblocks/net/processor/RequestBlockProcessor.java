package distributeblocks.net.processor;

import distributeblocks.BlockChain;
import distributeblocks.net.message.BlockMessage;
import distributeblocks.net.message.RequestBlockMessage;
import distributeblocks.io.Console;

public class RequestBlockProcessor extends AbstractMessageProcessor<RequestBlockMessage> {
	@Override
	public void processMessage(RequestBlockMessage message) {
		Console.log("Got block request");

		// Send em the block.

		BlockMessage blockMessage = new BlockMessage(new BlockChain().getAllBlocks().get(message.blockHash), message.blockHeight);

		if (blockMessage.block == null){
			Console.log("Could not find the requested block.");
		} else {
			message.senderNode.asyncSendMessage(blockMessage);
		}



	}
}


