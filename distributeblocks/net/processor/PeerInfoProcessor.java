package distributeblocks.net.processor;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.PeerNode;
import distributeblocks.net.message.PeerInfoMessage;

import java.util.Random;

public class PeerInfoProcessor extends AbstractMessageProcessor<PeerInfoMessage> {
	@Override
	public void processMessage(PeerInfoMessage message) {
		System.out.println("Got a peer info message from: " + message.senderNode.getAddress());

		for (IPAddress a : message.peerAddresses){
			System.out.println(" - " + a);
		}

		NetworkManager networkManager = NetworkService.getNetworkManager();
		int needed = networkManager.getMinPeers() - networkManager.getPeerNodes().size();
		int suceeded = 0;
		Random ran = new Random();
		ConfigManager configManager = new ConfigManager();

		for (int i = 0; i < needed ; i ++){ // If none are needed, this message is ignored.

			if (message.peerAddresses.size() == 0){
				break;
			}

			IPAddress address = message.peerAddresses.remove(ran.nextInt(Math.max(1, message.peerAddresses.size() - 1)));

			if (address.port <= 0 || !networkManager.connectToNode(address)){
				i --;
			} else {
				suceeded ++;
				configManager.addNodeAndWrite(new PeerNode(address));
			}
		}

		if (suceeded < needed){
			// TODO: Do something about this!
		}
	}
}
