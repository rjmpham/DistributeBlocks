package distributeblocks.net.message;

import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkService;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.PeerInfoProcessor;

import java.util.ArrayList;
import java.security.PublicKey;

public class PeerInfoMessage extends AbstractMessage {


	public ArrayList<IPAddress> peerAddresses;
	public ArrayList<PublicKey> publicKeys;
	public ArrayList<String> aliases;
	public boolean seedNode;
	public boolean friend; // See the constructor comments in RequestPeersMessage for details.
	
	
	/**
	 *
	 * @param peerAddresses
	 * @param friends
	 *   See the constructor comments in RequestPeersMessage for details.
	 */
	public PeerInfoMessage(ArrayList<IPAddress> peerAddresses, ArrayList<PublicKey> publicKeys, ArrayList<String> aliases, boolean friends){
		friend = friends;
		this.peerAddresses = peerAddresses;
		this.publicKeys = publicKeys;
		this.aliases = aliases;
		seedNode = NetworkService.getNetworkManager().inSeedMode();
	}
	
	public PeerInfoMessage(ArrayList<IPAddress> peerAddresses, boolean friends){
		friend = friends;
		this.peerAddresses = peerAddresses;
		seedNode = NetworkService.getNetworkManager().inSeedMode();
	}


	public PeerInfoMessage(ArrayList<IPAddress> peerAddresses) {
		this.peerAddresses = peerAddresses;
		seedNode = NetworkService.getNetworkManager().inSeedMode();
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new PeerInfoProcessor();
	}
}
