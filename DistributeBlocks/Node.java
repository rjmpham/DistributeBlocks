package distributeblocks;

import distributeblocks.net.NetworkService;

public class Node {


	private static int minPeers = 3;
	private static int maxPeers = 3;

	/**
	 *
	 *
	 * @param args
	 */
	public static void main (String[] args){


		// TODO proper type checking and crap.
		for (int i = 0; i < args.length - 1; i ++){

			String a = args[i];

			switch (a){
				case "minp":
					minPeers = Integer.parseInt(args[i+1]);
					break;
				case "maxp":
					maxPeers = Integer.parseInt(args[i+1]);
					break;
			}
		}

		NetworkService.init(minPeers, maxPeers);

	}


}
