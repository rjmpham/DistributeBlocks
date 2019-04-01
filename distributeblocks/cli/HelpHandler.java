package distributeblocks.cli;

import java.util.concurrent.Callable;

import distributeblocks.Node;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(description = "Lists available commands",
		 name = "help", mixinStandardHelpOptions = true)
public class HelpHandler implements Callable<Void> {
	private Node node;
	
	public HelpHandler(Node node) {
		this.node = node;
	}
	
	@Override
	public Void call() throws Exception {	
		System.out.println("Available commands:\n"
				+ "\texit\t\tTerminates the node processes\n"
				+ "\thelp\t\tLists available commands\n"
				+ "\tmine\t\tEnable or disable mining\n"
				+ "\tstart\t\tStart network connection\n"
				+ "\ttransaction\tCreate a new transaction\n"
				+ "\twallet\t\tCreate, load and access a wallet\n");
		
		return null;
	}	
}
