package distributeblocks.net.processor;
import distributeblocks.util.Validator;
import distributeblocks.BlockChain;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.TransactionBroadcastMessage;
import distributeblocks.io.Console;

public class TransactionBroadcastMessageProcessor extends AbstractMessageProcessor<TransactionBroadcastMessage> {
	@Override
	public void processMessage(TransactionBroadcastMessage message) {

		Console.log("Got transaction broadcast message.");
		
		if(message.transaction.verifySignature()) {
			NetworkService.getNetworkManager().addTransaction(message.transaction);
		}
		else {
			Console.log("Transaction signature failed to verify");
		}

	}
}
