package distributeblocks.net.processor;

import distributeblocks.Block;
import distributeblocks.BlockChain;
import distributeblocks.NodeService;
import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.BlockBroadcastMessage;
import distributeblocks.net.message.MiningFinishedMessage;
import distributeblocks.io.Console;

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
        NetworkService.getNetworkManager().beginMining();
    }
}
