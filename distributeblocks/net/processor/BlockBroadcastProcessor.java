package distributeblocks.net.processor;

import distributeblocks.Block;
import distributeblocks.BlockChain;
import distributeblocks.Node;
import distributeblocks.NodeService;
import distributeblocks.io.ConfigManager;
import distributeblocks.mining.Miner;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.BlockBroadcastMessage;
import distributeblocks.io.Console;

import java.util.LinkedList;

public class BlockBroadcastProcessor extends AbstractMessageProcessor<BlockBroadcastMessage> {
    @Override
    public void processMessage(BlockBroadcastMessage message) {
        Console.log("Got a block broadcast.");

        if (NetworkService.getNetworkManager().sentBlockBefore(message.block)) {
        	Console.log("Block has already been sent");
        	return;
        }

        ConfigManager configManager = new ConfigManager();
        BlockChain blockChain = new BlockChain();


        // Check to see if our chain already has this block.
        if (!blockChain.getAllBlocks().containsKey(message.block.getHashBlock())) {

        	// try to add the transaction, and bail if that fails
            boolean added = blockChain.addBlock(message.block);
            if (!added) {
            	return;
            }
            blockChain.save();
            System.out.println("In BlockBroadcastProcessor: Added block to the chain!");
            
            Block lastVerified = blockChain.getLastVerifiedBlock();
            if (lastVerified != null) {
				// Update node wallet with the block which is now verified
				NodeService.getNode().updateWallet(lastVerified);
				// Update the transaction pools now that a new block is verified
				NetworkService.getNetworkManager().updateTransactionPools();
            }

            // TODO Here is the spot to stop mining and restart mining
            NetworkService.getNetworkManager().beginMining();
            NetworkService.getNetworkManager().asyncSendToAllPeers(new BlockBroadcastMessage(message.block)); // Propogate on the network.
            NetworkService.getNetworkManager().addSentBlock(message.block);
        }

        // If we already have it do nothing.
    }
}
