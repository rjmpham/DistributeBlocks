package distributeblocks;

import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkService;

public class Node {


	private static int minPeers = 3;
	private static int maxPeers = 3;
	private static int port = 5832;
	private static IPAddress seedNode = new IPAddress("localhost", 5831); // TODO: Support multiple seed nodes.
	private static boolean seed = false;

	/**
	 *
	 *
	 * @param args
	 */
	public static void main (String[] args){


		// TODO proper type checking and crap.
		for (int i = 0; i < args.length; i ++){

			String a = args[i];

			System.out.println("got arg: " + args[i]);
			switch (a){
				case "minp":
					minPeers = Integer.parseInt(args[i+1]);
					break;
				case "maxp":
					maxPeers = Integer.parseInt(args[i+1]);
					break;
				case "port":
					port = Integer.parseInt(args[i+1]);
					System.out.println("Got port: " + port);
					break;
				case "seedAddr":
					String seedAddr = args[i+1];
					int seedPort = Integer.parseInt(args[i+2]);
					seedNode = new IPAddress(seedAddr, seedPort);
					break;
				case "seed":
					seed = true;
					break;
			}
		}

		// TODO: Maybe replace param list with config object.
		NetworkService.init(minPeers, maxPeers, port, seedNode, seed);

	}


}
