package distributeblocks.cli;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import distributeblocks.Node;
import distributeblocks.net.NetworkConfig;
import picocli.CommandLine;

public class CommandLineInterface implements Runnable{
	private static Class classObj = CommandLineInterface.class;
	
	private Node node; 					// the node who ran this CLI
	
	public CommandLineInterface(Node node) {
		this.node = node;
	}
	
	@Override
	public void run() {
		// TODO: loop over user input here
	}
		
	/*
	 * Parses a list of command line arguments and
	 * performs the requested action.
	 */
	public void parseCommand(String[] args) {
		String command = "None";
		try {
			// read the command and call the requested method
			command = args[0];
			Method method = classObj.getDeclaredMethod(args[0], String[].class);
			method.invoke(this, args);
		
		// handle reflection exceptions
		} catch (NoSuchMethodException | SecurityException| IllegalAccessException | 
				 IllegalArgumentException | InvocationTargetException e) {
			System.out.println(String.format("Unrecognized command \'{}\'. Use \'help\' command", command));
		
		// handle any empty command
		} catch (IndexOutOfBoundsException e) {
			System.out.println("No command provided. Use \'help\' command");
		}
	}
	
	/*
	 * Starts a node's networking processes
	 */
	public void start(String[] args) {
		NetworkConfig config = CommandLine.call(new Start(), args);
		node.initializeNetworkService(config);
	}
	
}
