package distributeblocks.mining;

import distributeblocks.Block;
import distributeblocks.FailedToHashException;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.AbstractMessage;
import distributeblocks.net.message.MiningFinishedMessage;

import java.util.concurrent.*;

public class Miner {



    ExecutorService executorService;
    BlockMiner miner;
    Future minerFuture;



    public Miner() {

        executorService = Executors.newFixedThreadPool(1);
    }


    public synchronized void startMining(String data, Block previousBlock, int targetNumZeros){


        if (minerFuture != null && !minerFuture.isDone()){

           stopMining();
        }

        System.out.println("Starting new mining operation.");
        this.miner = new BlockMiner(previousBlock, data, targetNumZeros);
        minerFuture = executorService.submit(miner);
    }


    public synchronized void stopMining(){

        // Kill it with fire.

        if (miner != null) {
            System.out.println("Killing miner.");
            miner.killMiner();
        }


        try {
            if (minerFuture != null) {
                minerFuture.get();
                System.out.println("Miner has been violently murdered.");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /*try {
            executorService.awaitTermination(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
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

                    System.out.println("Beginning mining");

                    currentBlock = new Block(data, previousBlock.getHashBlock(), targetNumZeros);
                    currentBlock.mineBlock();

                    if (currentBlock.isBlockMined()) {
                        System.out.println("Sending mining finished message.");
                        AbstractMessage message = new MiningFinishedMessage(currentBlock, Miner.this);
                        NetworkService.getNetworkManager().asyncEnqueue(message);        // Send block to be processed on processing queue for this node
                    }

                    System.out.println("Finished Mining");

                    stop = true;

                } catch (FailedToHashException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
