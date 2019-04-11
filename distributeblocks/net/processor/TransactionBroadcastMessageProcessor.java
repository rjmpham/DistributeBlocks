package distributeblocks.net.processor;
import distributeblocks.io.DirectoryManager;
import distributeblocks.io.WalletManager;
import distributeblocks.util.Validator;
import distributeblocks.BlockChain;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.TransactionBroadcastMessage;
import distributeblocks.io.Console;
import distributeblocks.crypto.*;

import java.io.IOException;
import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class TransactionBroadcastMessageProcessor extends AbstractMessageProcessor<TransactionBroadcastMessage> {
	@Override
	public void processMessage(TransactionBroadcastMessage message) {

		Console.log("Got transaction broadcast message.");
		String fullPath = DirectoryManager.fullPathToDir("jalal");
		try {
			PublicKey publicKey = WalletManager.loadPublicKey(fullPath, Crypto.GEN_ALGORITHM);
			if(message.transaction.verifySignature()) {
				if (Crypto.verifySignature(publicKey, message.transaction.getTransactionId(),message.transaction.getSignature())){
					return;
				}else{
					NetworkService.getNetworkManager().addTransaction(message.transaction);
					return;
				}
			}
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			Console.log("DDoS TransactionBroadcastMessageProcessor error");
		}

			Console.log("Transaction signature failed to verify");
		return;
		}


}
