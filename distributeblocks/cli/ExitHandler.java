package distributeblocks.cli;

import java.util.concurrent.Callable;

import distributeblocks.Node;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(description = "Terminates the node processes",
		 name = "exit", mixinStandardHelpOptions = true)
public class ExitHandler implements Callable<Void> {
	private Node node;
	
	public ExitHandler(Node node) {
		this.node = node;
	}
	
	@Override
	public Void call() throws Exception {	
		node.exit();
		
		return null;
	}	
}
