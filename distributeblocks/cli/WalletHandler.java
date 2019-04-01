package distributeblocks.cli;

import java.util.concurrent.Callable;

import distributeblocks.Node;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


//TODO: handler for creating a new wallet (sk/pk)
//TODO: handler for connecting a wallet to the node
//TODO: handler for counting funds in the wallet
@Command(description = "",
		 name = "wallet", mixinStandardHelpOptions = true)
public class WalletHandler implements Callable<Void> {
	private Node node;
	
	public WalletHandler(Node node) {
		this.node = node;
	}
	
	@Override
	public Void call() throws Exception {		
		return null;
	}	
}
