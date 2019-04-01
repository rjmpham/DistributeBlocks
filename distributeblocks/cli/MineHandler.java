package distributeblocks.cli;

import java.util.concurrent.Callable;

import distributeblocks.Node;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


//TODO: handler for enabling/ disabling mining
@Command(description = "",
		 name = "mine", mixinStandardHelpOptions = true)
public class MineHandler implements Callable<Void> {
	private Node node;
	
	public MineHandler(Node node) {
		this.node = node;
	}
	
	@Override
	public Void call() throws Exception {		
		return null;
	}	
}
