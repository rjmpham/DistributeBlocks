package distributeblocks.net.processor;

import distributeblocks.BlockChain;
import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.BlockBroadcastMessage;
import distributeblocks.net.message.MiningFinishedMessage;

public class MiningFinishedProcessor extends AbstractMessageProcessor<MiningFinishedMessage> {
    @Override
    public void processMessage(MiningFinishedMessage message) {
        System.out.println("Got Finished mining. message");

        // TODO For now we are just going to mine continuely, but we should wait for transactions to come in or something..

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
