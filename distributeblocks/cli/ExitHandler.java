package distributeblocks.cli;

import java.util.concurrent.Callable;

import distributeblocks.Node;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


//TODO: handler for exiting the program
@Command(description = "",
		 name = "exit", mixinStandardHelpOptions = true)
public class ExitHandler implements Callable<Void> {
	private Node node;
	
	public ExitHandler(Node node) {
		this.node = node;
	}
	
	@Override
	public Void call() throws Exception {		
		return null;
	}	
}
