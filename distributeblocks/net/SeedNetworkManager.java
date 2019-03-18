package distributeblocks.net;

public class SeedNetworkManager extends NetworkManager {




	/**
	 * @param minPeers     Minimum number of connected peers. Will periodicaly attempt to discover new peers if
	 *                     number of connected peers is less than this value.
	 * @param maxPeers
	 * @param port
	 * @param seedNodeAddr
	 * @param seed
	 */
	public SeedNetworkManager(int minPeers, int maxPeers, int port, IPAddress seedNodeAddr, boolean seed) {
		super(minPeers, maxPeers, port, seedNodeAddr, seed);
	}
}
