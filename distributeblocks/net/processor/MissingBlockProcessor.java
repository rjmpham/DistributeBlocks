package distributeblocks.net.processor;

import distributeblocks.net.NetworkService;
import distributeblocks.net.message.MissingBlockMessage;

public class MissingBlockProcessor extends AbstractMessageProcessor<MissingBlockMessage> {
	@Override
	public void processMessage(MissingBlockMessage message) {

		System.out.println("Got missing block message.");
		NetworkService.getNetworkManager().beginAquireChainOperation();
	}
}
