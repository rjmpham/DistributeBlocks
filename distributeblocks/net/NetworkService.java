package distributeblocks.net;

public class NetworkService {


	private static NetworkManager networkManager;

	public static NetworkManager getNetworkManager(){
		return networkManager;
	}

	public static void init(int minPeers, int maxPeers){
		networkManager = new NetworkManager(minPeers, maxPeers);
	}
}
