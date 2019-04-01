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
				+ "\t\texit\t\t\tTerminates the node processes\n"
				+ "\t\thelp\t\t\tLists available commands\n"
				+ "\t\tmine\t\t\tEnable or disable mining\n"
				+ "\t\tstart\t\t\tStart network connection\n"
				+ "\t\ttransaction\tCreate a new transaction\n"
				+ "\t\twallet\t\t\tCreate, load and access a wallet\n");
		
		return null;
	}	
}
