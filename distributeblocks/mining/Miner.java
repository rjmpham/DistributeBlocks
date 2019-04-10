package distributeblocks.mining;

import distributeblocks.*;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.AbstractMessage;
import distributeblocks.net.message.MiningFinishedMessage;
import distributeblocks.io.Console;

import java.util.HashMap;
import java.util.concurrent.*;

public class Miner {



    ExecutorService executorService;
    BlockMiner miner;
    Future minerFuture;



    public Miner() {

        executorService = Executors.newFixedThreadPool(1);
    }


    public synchronized void startMining(HashMap<String, Transaction> data, Block previousBlock, int targetNumZeros){


        if (minerFuture != null && !minerFuture.isDone()){

           stopMining();
        }

        Console.log("Starting new mining operation.");
        this.miner = new BlockMiner(previousBlock, data, targetNumZeros);
        minerFuture = executorService.submit(miner);
    }


    public synchronized void stopMining(){

        // Kill it with fire.

        if (miner != null) {
            Console.log("Killing miner.");
            miner.killMiner();
        }


        try {
            if (minerFuture != null) {
                minerFuture.get();
                Console.log("Miner has been violently murdered.");
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
        private HashMap<String, Transaction> data;
        private int targetNumZeros;


        public BlockMiner(Block previousBlock, HashMap<String, Transaction> data, int targetNumZeros) {
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


            Wallet wallet = NodeService.getNode().getWallet();

            while (!stop) {

                try {

                    Console.log("Beginning mining");

                    Transaction reward;
                    try {
                    	reward = CoinBase.makeBlockReward(wallet.getPublicKey());
                    } catch (NullPointerException e) {
                    	Console.log("Terminating mining");
                    	return;
                    }
                    reward.transactionEnforcer();	// TODO: can we always assume a block reward transaction will be correctly enforced?
                    
                   // Transaction rewardOut = new Transaction(wallet.getPrivateKey(), wallet.getPublicKey(), 5.0f, reward.getId_Transaction());
                    data.put(reward.getTransactionId(), reward);

                    currentBlock = new Block(data, previousBlock.getHashBlock(), targetNumZeros);

                    currentBlock.mineBlock();

                    if (currentBlock.isBlockMined()) {
                        Console.log("Sending mining finished message.");
                        AbstractMessage message = new MiningFinishedMessage(currentBlock, Miner.this);
                        NetworkService.getNetworkManager().asyncEnqueue(message);        // Send block to be processed on processing queue for this node
                    }

                    Console.log("Finished Mining");

                    stop = true;

                } catch (FailedToHashException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
