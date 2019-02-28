package distributeblocks.net.processor;

import distributeblocks.io.ConfigManager;
import distributeblocks.net.NetworkManager;
import distributeblocks.net.NetworkService;
import distributeblocks.net.PeerNode;
import distributeblocks.net.message.ShakeMessage;
import distributeblocks.net.message.ShakeResponseMessage;

import java.util.ArrayList;

public class ShakeProcessor extends AbstractMessageProcessor<ShakeMessage> {


	public void processMessage(ShakeMessage message) {
		System.out.println("Got a shake message from " + message.senderNode.getAddress() + ". AbstractMessage: " + message.getShakeMessage());

		System.out.println("Listening port: " + message.listeningPort);
		message.senderNode.setListenPort(message.listeningPort);

		boolean wantMoreFriends = NetworkService.getNetworkManager().inSeedMode() ?
				false : NetworkService.getNetworkManager().needMorePeers();

		if (NetworkService.getNetworkManager().isConnectedToNode(message.senderNode.getListeningAddress())){
			wantMoreFriends = true;
		}

		message.senderNode.asyncSendMessage(new ShakeResponseMessage("Hey back at ya ;)",
				NetworkService.getNetworkManager().getPort(), wantMoreFriends));
		NetworkService.getNetworkManager().addNode(message.senderNode);


		if (NetworkService.getNetworkManager().inSeedMode()){

			ConfigManager configManager = new ConfigManager();
			configManager.addNodeAndWrite(message.senderNode);
		}
	}
}
