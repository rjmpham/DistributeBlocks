package distributeblocks.net.processor;

import distributeblocks.net.NetworkService;
import distributeblocks.net.message.MissingBlockMessage;
import distributeblocks.io.Console;

public class MissingBlockProcessor extends AbstractMessageProcessor<MissingBlockMessage> {
	@Override
	public void processMessage(MissingBlockMessage message) {

		Console.log("Got missing block message.");
		NetworkService.getNetworkManager().beginAquireChainOperation();
	}
}
