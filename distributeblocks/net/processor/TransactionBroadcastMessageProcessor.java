package distributeblocks.net.processor;
import distributeblocks.util.Validator;
import distributeblocks.BlockChain;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.TransactionBroadcastMessage;

public class TransactionBroadcastMessageProcessor extends AbstractMessageProcessor<TransactionBroadcastMessage> {
	@Override
	public void processMessage(TransactionBroadcastMessage message) {
		System.out.println("Got transaction broadcast message.");
		BlockChain blockchain = new BlockChain();
		Validator valid = new Validator();
		if(valid.isValidTransaction(message.transaction, blockchain.getLongestChain())){
			NetworkService.getNetworkManager().addTransaction(message.transaction);
		}
	}
}
