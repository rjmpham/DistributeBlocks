package distributeblocks.net;

public class NetworkService {


	private static NetworkManager networkManager;

	public static NetworkManager getNetworkManager(){
		return networkManager;
	}

	public static void init(int minPeers, int maxPeers, int port, IPAddress seedNode){
		networkManager = new NetworkManager(minPeers, maxPeers, port, seedNode);
		networkManager.initialize();
	}
}
