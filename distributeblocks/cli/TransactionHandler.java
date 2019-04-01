package distributeblocks.cli;

import java.util.concurrent.Callable;

import distributeblocks.Node;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(description = "Create a new transaction",
		 name = "transaction", mixinStandardHelpOptions = true)
public class TransactionHandler implements Callable<Void> {
	private Node node;
	
	@Parameters(index = "0", description = "The path to the recipient's public key")
    private String publicKeyPath;
	
	@Parameters(index = "1", description = "The funds to send")
    private float amount;
	
	public TransactionHandler(Node node) {
		this.node = node;
	}
	
	@Override
	public Void call() throws Exception {	
		node.createTransaction(System.getProperty("user.dir") + publicKeyPath, amount);
		
		return null;
	}	
}
