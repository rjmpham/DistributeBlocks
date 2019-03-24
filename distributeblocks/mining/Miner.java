package distributeblocks.mining;

import distributeblocks.Block;
import distributeblocks.FailedToHashException;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.BlockBroadcastMessage;
import distributeblocks.net.message.MiningFinishedMessage;
import sun.nio.ch.Net;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Miner {



    ExecutorService executorService;
    BlockMiner miner;
    Future minerFuture;



    public Miner() {

        executorService = Executors.newFixedThreadPool(1);
    }


    public void startMining(String data, Block previousBlock, int targetNumZeros){


        if (minerFuture != null && !minerFuture.isDone()){

           stopMining();
        }

        System.out.println("Starting new mining operation.");
        miner = new BlockMiner(previousBlock, data, targetNumZeros);
        minerFuture = executorService.submit(miner);
    }


    public void stopMining(){

        // Kill it with fire.
        System.out.println("Killing miner.");
        miner.killMiner();


        try {
            minerFuture.get();
            System.out.println("Miner has been violently murdered.");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        executorService.shutdownNow();
    }




    private class BlockMiner implements Runnable{


        private volatile boolean stop = false;
        private Block currentBlock;
        private Block previousBlock;
        private String data;
        private int targetNumZeros;


        public BlockMiner(Block previousBlock, String data, int targetNumZeros) {
            this.previousBlock = previousBlock;
            this.data = data;
            this.targetNumZeros = targetNumZeros;
        }

        public void killMiner(){

            stop = true;

            if (currentBlock != null){
                currentBlock.setStopMining(true);
            }
        }


        @Override
        public void run() {

            while (!stop) {

                try {

                    currentBlock = new Block(data, previousBlock.getHashBlock(), targetNumZeros);
                    currentBlock.mineBlock();

                    // TODO Mining done, broadcast it!
                    NetworkService.getNetworkManager().asyncSendToAllPeers(new BlockBroadcastMessage(currentBlock)); // Send block to peers.
                    NetworkService.getNetworkManager().asyncEnqueue(new MiningFinishedMessage(currentBlock));        // Send block to be processed on processing queue for this node
                    stop = true;

                } catch (FailedToHashException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
