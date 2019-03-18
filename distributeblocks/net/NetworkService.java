package distributeblocks.net;

public class NetworkService {


	private static NetworkManager networkManager;

	public static NetworkManager getNetworkManager(){
		return networkManager;
	}

	public static void init(int minPeers, int maxPeers, int port, IPAddress seedNode, boolean seed){
		networkManager = new NetworkManager(minPeers, maxPeers, port, seedNode, seed);
		networkManager.initialize();
	}
}
