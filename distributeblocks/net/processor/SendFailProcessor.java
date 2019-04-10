package distributeblocks.net.processor;

import distributeblocks.net.message.SendFailMessage;
import distributeblocks.io.Console;

public class SendFailProcessor extends AbstractMessageProcessor<SendFailMessage> {


	@Override
	public void processMessage(SendFailMessage message) {

		Console.log("Got send fail message");
	}
}
