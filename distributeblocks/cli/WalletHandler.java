package distributeblocks.cli;

import java.util.concurrent.Callable;

import distributeblocks.Node;
import distributeblocks.Wallet;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(description = "Create, load and access a wallet",
		 name = "wallet", mixinStandardHelpOptions = true,
		 subcommands = {
				 NewHandler.class,
				 LoadHandler.class,
				 FundsHandler.class,
				 RescindHandler.class})
public class WalletHandler implements Callable<Void> {
	protected static Node node; 	// Must be protected static so sub commands can access it
	
	public WalletHandler(Node node) {
		WalletHandler.node = node;
	}
	
	@Override
	public Void call() throws Exception {
		System.out.println("subcommand required");
		new CommandLine(this).usage(System.out);
		
		return null;
	}
}

@Command(description = "Create a new wallet",
name = "create", mixinStandardHelpOptions = true)
class NewHandler implements Callable<Void> {

	public NewHandler() {}
	
	@Parameters(index = "0", description = "File path to store wallet information")
	private String walletPath;
	
	@Override
	public Void call() throws Exception {
		WalletHandler.node.createWallet(walletPath);
		
		return null;
	}
}

@Command(description = "Load a previously saved wallet",
name = "load", mixinStandardHelpOptions = true)
class LoadHandler implements Callable<Void> {

	public LoadHandler() {}
	
	@Parameters(index = "0", description = "File path to wallet information")
	private String walletPath;
	
	@Override
	public Void call() throws Exception {
		WalletHandler.node.loadWallet(walletPath);
		
		return null;
	}
}

@Command(description = "Check the funds available",
name = "funds", mixinStandardHelpOptions = true)
class FundsHandler implements Callable<Void> {
	
	public FundsHandler() {}
	
	@Override
	public Void call() throws Exception {
		System.out.println(Wallet.COIN_BASE_KEYS);
		WalletHandler.node.countFunds();
		
		return null;
	}
}

// TODO: allow the user to rescind a specific fund
@Command(description = "Rescind all held funds",
		 name = "rescind", mixinStandardHelpOptions = true)
class RescindHandler implements Callable<Void> {

	public RescindHandler() {}
	
	@Override
	public Void call() throws Exception {
		WalletHandler.node.rescindHeldFunds();
		
		return null;
	}	
}
