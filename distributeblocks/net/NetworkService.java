package distributeblocks.net;

public class NetworkService {


	private static NetworkManager networkManager;

	public static NetworkManager getNetworkManager(){
		return networkManager;
	}

	public static void init(NetworkConfig networkConfig){
		networkManager = new NetworkManager(networkConfig);
		networkManager.initialize();
	}
}
