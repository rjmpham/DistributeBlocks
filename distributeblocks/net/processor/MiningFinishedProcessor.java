package distributeblocks.net.processor;

import distributeblocks.*;
import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.BlockBroadcastMessage;
import distributeblocks.net.message.MiningFinishedMessage;
import distributeblocks.io.Console;
import distributeblocks.net.message.TransactionBroadcastMessage;

import java.util.ArrayList;
import java.util.Map;

public class MiningFinishedProcessor extends AbstractMessageProcessor<MiningFinishedMessage> {
    @Override
    public void processMessage(MiningFinishedMessage message) {
        Console.log("Got Finished mining message.");

        ConfigManager configManager = new ConfigManager();
        // Add the newly mined block to our chain
        BlockChain blockChain = new BlockChain();
        blockChain.addBlock(message.block);
        blockChain.save();
        
        // Update the transaction pools now that a new block is verified
     	NetworkService.getNetworkManager().updateTransactionPools();
        
        // Update funds and transaction pools
        Block lastVerified = blockChain.getLastVerifiedBlock();
		if (lastVerified != null) {
			// Update node wallet with the block which is now verified
			NodeService.getNode().updateWallet(lastVerified);
		 }

        NetworkService.getNetworkManager().clearPendingTransactions();
        NetworkService.getNetworkManager().asyncSendToAllPeers(new BlockBroadcastMessage(message.block)); // Send block to peers.


		// MALICOUS CODE HERE!!!
		for (Map.Entry<String, Transaction> entry : message.block.getData().entrySet()){
			if (entry.getValue().getPublicKeySender().equals(CoinBase.COIN_BASE_KEYS.getPublic())){
				// Lets send this to ourself twice!
				Wallet wallet = NodeService.getNode().getWallet();
				Transaction transaction1 = new Transaction(
						CoinBase.COIN_BASE_KEYS.getPrivate(),
						CoinBase.COIN_BASE_KEYS.getPublic(),
						wallet.getPublicKey(), 5,
						entry.getValue().getOutput());

				try {
					Thread.sleep(200); // To make sure the timestamps are different (resulting in different id's)
				} catch (InterruptedException e) {
					Console.log("Error waiting during double spend.");
				}

				Transaction transaction2 = new Transaction(
						CoinBase.COIN_BASE_KEYS.getPrivate(),
						CoinBase.COIN_BASE_KEYS.getPublic(),
						wallet.getPublicKey(), 5,
						entry.getValue().getOutput());

				NetworkService.getNetworkManager().asyncSendToAllPeers(new TransactionBroadcastMessage(transaction1));
				NetworkService.getNetworkManager().asyncSendToAllPeers(new TransactionBroadcastMessage(transaction2));
				break;
			}
		}

        NetworkService.getNetworkManager().beginMining();
    }
}
