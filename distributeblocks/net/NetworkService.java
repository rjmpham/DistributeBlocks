package distributeblocks.net;

import distributeblocks.Node;
import distributeblocks.io.Console;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkService {


	private static NetworkManager networkManager;
	private static IPAddress localAddr;

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

	public static IPAddress getLocalAddress(){

		if (localAddr != null){
			return localAddr;
		}

		try {

			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();


			while (interfaces.hasMoreElements()){
				NetworkInterface ni = interfaces.nextElement();
				Console.log(ni.getDisplayName());
				Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();

				while (inetAddresses.hasMoreElements()){
					InetAddress addr = inetAddresses.nextElement();
					String hostAddress = addr.getHostAddress();
					Console.log(hostAddress);

					if (!hostAddress.contains("127.0.0.1") && !hostAddress.contains("localhost") && hostAddress.split("\\.").length == 4){
						localAddr = new IPAddress(hostAddress, Node.MONITOR_PORT);
						return localAddr;
					}
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		}

		return null;
	}
}
