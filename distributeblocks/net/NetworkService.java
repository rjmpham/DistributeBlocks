package distributeblocks.net;

public class NetworkService {


	private static NetworkManager networkManager;

	/**
	 * See NetworkActions before using this.
	 *
	 * @return
	 */
	public static NetworkManager getNetworkManager(){
		return networkManager;
	}

	/**
	 * If you dont know how NetworkManager works, use this method instead of "getNetworkManager()".
	 *
	 * @return
	 */
	public static NetworkActions getNetworkActions(){
		return networkManager;
	}

	public static void init(NetworkConfig networkConfig){
		networkManager = new NetworkManager(networkConfig);
		networkManager.initialize();
	}
}
