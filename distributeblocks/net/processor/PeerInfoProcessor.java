package distributeblocks.net.processor;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.PeerNode;
import distributeblocks.net.message.PeerInfoMessage;
import distributeblocks.io.Console;

import java.util.Date;
import java.util.Random;
import java.security.PublicKey;

public class PeerInfoProcessor extends AbstractMessageProcessor<PeerInfoMessage> {
	@Override
	public void processMessage(PeerInfoMessage message) {
		Console.log("Got a peer info message from: " + message.senderNode.getAddress());

		for (IPAddress a : message.peerAddresses){
			Console.log(" - " + a);
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
			PublicKey publicKey = message.publicKeys.remove(ran.nextInt(Math.max(1, message.publicKeys.size() - 1)));
			String alias = message.aliases.remove(ran.nextInt(Math.max(1, message.aliases.size() - 1)));

			if (address.port <= 0 || networkManager.isConnectedToNode(address)|| !networkManager.connectToNode(address,publicKey,alias)){
				i --;
			} else {
				suceeded ++;
				configManager.addNodeAndWrite(new PeerNode(address));
			}
		}

		if (suceeded < needed){
			Console.log("Did not get enough peers in peer info :(");
		}

		NetworkService.getNetworkManager().removeTemporaryNode(message.senderNode);

		if (message.seedNode){
			Console.log("SHUTTING DOWN NODE CONNECTION BECAUSE ITS A SEED");
			message.senderNode.shutDown();
		} else if (!message.friend){
			Console.log("SHUTTING DOWN NODE CONNECTION BECAUSE ITS NOT A FRIEND " + new Date().getTime());
		}
		// If the node is only in the tempory pool, then it will be disconnected.
		// this is what we want for the seed node.
	}
}
