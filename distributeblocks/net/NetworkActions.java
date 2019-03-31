package distributeblocks.net;

import distributeblocks.Transaction;

public interface NetworkActions {


	public void startMining();
	public void stopMining();
	public void broadcastTransaction(Transaction transaction);

}
