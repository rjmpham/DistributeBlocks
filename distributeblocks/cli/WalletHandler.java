package distributeblocks.cli;

import java.util.concurrent.Callable;

import distributeblocks.Node;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(description = "Create, load and access a wallet",
		 name = "wallet", mixinStandardHelpOptions = true,
		 subcommands = {
				 WalletHandler.NewHandler.class,
				 WalletHandler.LoadHandler.class,
				 WalletHandler.FundsHandler.class})
public class WalletHandler implements Callable<Void> {
	private Node node;
	
	public WalletHandler(Node node) {
		this.node = node;
	}
	
	@Override
	public Void call() throws Exception {
		System.out.println("subcommand required");
		
		return null;
	}
	
	
	//TODO: handler for creating a new wallet (sk/pk)
	@Command(description = "Create a new wallet",
			 name = "create", mixinStandardHelpOptions = true)
	class NewHandler implements Callable<Void> {

		@Override
		public Void call() throws Exception {
			return null;
		}
	}
	
	
	//TODO: handler for connecting a wallet to the node
	@Command(description = "Load a previously saved wallet",
			 name = "load", mixinStandardHelpOptions = true)
	class LoadHandler implements Callable<Void> {

		@Override
		public Void call() throws Exception {
			return null;
		}
	}
	
	
	@Command(description = "Check the funds available",
			 name = "funds", mixinStandardHelpOptions = true)
	class FundsHandler	implements Callable<Void> {

		@Override
		public Void call() throws Exception {
			node.countFunds();
			
			return null;
		}
		
	}
}
