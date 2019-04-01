package distributeblocks.cli;

import java.util.concurrent.Callable;

import distributeblocks.Node;
import distributeblocks.net.IPAddress;
import distributeblocks.net.NetworkConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(description = "Starts a node in the Coin^2 network",
		 name = "start", mixinStandardHelpOptions = true)
public class Start implements Callable<Void> {
	private Node node;
	
	public Start(Node node) {
		this.node = node;
	}
	
	@Option(names = {"-minp", "--minpeers"}, 
			description = "The minimum number of peers to connecto to")
	private int minPeers = 3;
	
	@Option(names = {"-maxp", "--maxpeers"}, 
			description = "The maximum number of peers to connecto to")
	private int maxPeers = 10;
	
	@Option(names = {"-p", "--port"}, 
			description = "The port to open on")
	private int port = 5832;
	
	@Option(names = {"-sAddr", "--seedAddress"}, 
			description = "The IP address of a seed node")
	private String seedAddress = "localhost";
	
	@Option(names = {"-sPort", "--seedPort"}, 
			description = "The IP port of the seed node")
	private int seedPort = 5832;

	@Option(names = {"-s", "--seed"}, 
			description = "This node is a seed")
	private boolean seed = false;
	
	@Option(names = {"-m", "--mining"}, 
			description = "This node is mining")
	private boolean mining = false;
	
	@Override
	public Void call() throws Exception {
		NetworkConfig config = new NetworkConfig();
		config.maxPeers = maxPeers;
		config.minPeers = minPeers;
		config.port = port;
		config.seed = seed;
		config.seedNode = new IPAddress(seedAddress, seedPort);
		config.mining = mining;
		node.initializeNetworkService(config);
		
		return null;
	}	
}
