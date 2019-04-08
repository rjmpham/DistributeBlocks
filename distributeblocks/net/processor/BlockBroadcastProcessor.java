package distributeblocks.net.processor;

import distributeblocks.Block;
import distributeblocks.BlockChain;
import distributeblocks.Node;
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


        // TODO verify the block is actualy legit!
        ConfigManager configManager = new ConfigManager();
        BlockChain blockChain = new BlockChain();


        // Check to see if our chain already has this block.
        if (!blockChain.getAllBlocks().containsKey(message.block.getHashBlock())) {

            blockChain.addBlock(message.block);
            blockChain.save();
            Console.log("Added block to the chain!");

            // TODO Here is the spot to stop mining and restart mining
            NetworkService.getNetworkManager().beginMining();
            NetworkService.getNetworkManager().asyncSendToAllPeers(new BlockBroadcastMessage(message.block)); // Propogate on the network.
        }

        // If we already have it do nothing.
    }
}
