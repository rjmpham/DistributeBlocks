package distributeblocks.cli;

import java.util.concurrent.Callable;

import distributeblocks.Node;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


//TODO: handler to tell the user what commands are available
@Command(description = "",
		 name = "help", mixinStandardHelpOptions = true)
public class HelpHandler implements Callable<Void> {
	private Node node;
	
	public HelpHandler(Node node) {
		this.node = node;
	}
	
	@Override
	public Void call() throws Exception {		
		return null;
	}	
}
