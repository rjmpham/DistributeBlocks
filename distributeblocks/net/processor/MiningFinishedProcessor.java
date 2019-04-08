package distributeblocks.net.processor;

import distributeblocks.BlockChain;
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

        NetworkService.getNetworkManager().clearPendingTransactions();
        NetworkService.getNetworkManager().asyncSendToAllPeers(new BlockBroadcastMessage(message.block)); // Send block to peers.
        NetworkService.getNetworkManager().beginMining();
    }
}
