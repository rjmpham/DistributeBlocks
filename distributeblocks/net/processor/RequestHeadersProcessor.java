package distributeblocks.net.processor;

import distributeblocks.Block;
import distributeblocks.BlockChain;
import distributeblocks.BlockHeader;
import distributeblocks.net.message.HeaderInfoMessage;
import distributeblocks.net.message.RequestHeadersMessage;
import distributeblocks.io.Console;

import java.util.ArrayList;

public class RequestHeadersProcessor extends AbstractMessageProcessor<RequestHeadersMessage> {


	@Override
	public void processMessage(RequestHeadersMessage message) {
		Console.log("Got a reuqest header message.");


		int i = 0;
		ArrayList<BlockHeader> headers = new ArrayList<>();

		Console.log("Longest chain: " + new BlockChain().getLongestChain().size());

		for (Block b : new BlockChain().getLongestChain()){

			headers.add(new BlockHeader(b.getHashBlock(), i++));
		}

		message.senderNode.asyncSendMessage(new HeaderInfoMessage(headers));
	}
}
