package distributeblocks.net;

import distributeblocks.net.message.AbstractMessage;

import java.util.concurrent.ExecutorService;

public class PeerNode{


	private String address;
	private ExecutorService executorService;


	private volatile boolean shutDown = false;


	public PeerNode(String address) {
		this.address = address;
	}

	/**
	 * Connects to the node asynchronously.
	 *
	 * Will enque a ConnectionFailed message if the connection is not successfull.
	 *
	 * @return
	 *   False if connection was not succesfful
	 */
	public void asyncConnect(){


	}

	public void sendMessage(AbstractMessage message){

	}


	public String getAddress() {
		return address;
	}

	public void shutDown(){

	}
}
