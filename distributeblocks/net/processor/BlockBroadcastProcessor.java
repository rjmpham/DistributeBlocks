package distributeblocks.net.processor;

import distributeblocks.Block;
import distributeblocks.BlockChain;
import distributeblocks.Node;
import distributeblocks.io.ConfigManager;
import distributeblocks.mining.Miner;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.BlockBroadcastMessage;

import java.util.LinkedList;

public class BlockBroadcastProcessor extends AbstractMessageProcessor<BlockBroadcastMessage> {
    @Override
    public void processMessage(BlockBroadcastMessage message) {
        System.out.println("Got a block broadcast.");


        // TODO verify the block is actualy legit!
        ConfigManager configManager = new ConfigManager();
        BlockChain blockChain = new BlockChain();

        blockChain.addBlock(message.block);
        blockChain.save();
        System.out.println("Added block to the chain!");

        // TODO Here is the spot to stop mining and restart mining
        NetworkService.getNetworkManager().beginMining();
    }
}
