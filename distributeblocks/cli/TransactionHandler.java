package distributeblocks.cli;

import java.util.concurrent.Callable;

import distributeblocks.Node;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


// TODO: handler for making a transaction
// TODO: handler to move a transaction from onHold to funds. Maybe should be in the WalletHandler?
@Command(description = "",
		 name = "transaction", mixinStandardHelpOptions = true)
public class TransactionHandler implements Callable<Void> {
	private Node node;
	
	public TransactionHandler(Node node) {
		this.node = node;
	}
	
	@Override
	public Void call() throws Exception {		
		return null;
	}	
}
