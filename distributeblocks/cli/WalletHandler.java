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
				 WalletHandler.FundsHandler.class,
				 WalletHandler.RescindHandler.class})
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
	
	
	@Command(description = "Create a new wallet",
			 name = "create", mixinStandardHelpOptions = true)
	class NewHandler implements Callable<Void> {
		
		@Parameters(index = "0", description = "File path to store wallet information")
	    private String walletPath;

		@Override
		public Void call() throws Exception {
			node.createWallet(walletPath);
			
			return null;
		}
	}
	
	
	@Command(description = "Load a previously saved wallet",
			 name = "load", mixinStandardHelpOptions = true)
	class LoadHandler implements Callable<Void> {
		
		@Parameters(index = "0", description = "File path to wallet information")
	    private String walletPath;

		@Override
		public Void call() throws Exception {
			node.loadWallet(walletPath);
			
			return null;
		}
	}
	
	
	@Command(description = "Check the funds available",
			 name = "funds", mixinStandardHelpOptions = true)
	class FundsHandler implements Callable<Void> {

		@Override
		public Void call() throws Exception {
			node.countFunds();
			
			return null;
		}
		
	}
	
	// TODO: allow the user to rescind a specific fund
	@Command(description = "Rescind all held funds",
			 name = "rescind", mixinStandardHelpOptions = true)
	class RescindHandler implements Callable<Void> {

		@Override
		public Void call() throws Exception {
			node.rescindHeldFunds();
			
			return null;
		}
		
	}
}
