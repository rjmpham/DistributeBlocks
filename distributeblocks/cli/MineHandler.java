package distributeblocks.cli;

import java.util.concurrent.Callable;

import distributeblocks.Node;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(description = "Enable or disable mining",
		 name = "mine", mixinStandardHelpOptions = true)
public class MineHandler implements Callable<Void> {
	private Node node;
	
	@Parameters(index = "0", description = "true or false")
    private boolean enable;
	
	public MineHandler(Node node) {
		this.node = node;
	}
	
	@Override
	public Void call() throws Exception {		
		if (enable)
			node.enableMining();
		else
			node.disableMining();
			
		return null;
	}	
}
