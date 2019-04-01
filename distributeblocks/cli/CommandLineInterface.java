package distributeblocks.cli;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;
import java.util.concurrent.Callable;

import distributeblocks.Node;
import distributeblocks.net.NetworkConfig;
import picocli.CommandLine;

public class CommandLineInterface implements Runnable{
	private static Scanner keyboard = new Scanner(System.in);
	
	private Node node; 					// the node who ran this CLI
	
	public CommandLineInterface(Node node) {
		this.node = node;
	}
	
	/*
	 * Repeatedly gets user input and attempt to parse it
	 * as a recognized command.
	 */
	@Override
	public void run() {
		String input;
		while(true) {
			input = keyboard.nextLine();
			// TODO: either handle quotes, or make sure no valid arg as whitespace
			parseCommand(input.split("\\s+"));
		}
	}
		
	/*
	 * Parses a list of command line arguments and
	 * performs the requested action.
	 */
	public void parseCommand(String[] args) {
		String command = "None";
		try {
			// read the command and instantiate a handler
			command = args[0];
			Class classObj = Class.forName(command);
			Constructor constructor = classObj.getConstructor(Node.class);
			Callable commandObj = (Callable) constructor.newInstance(node);
			
			//call the command parser on the hander
			CommandLine.call(commandObj, args);
			
		// handle reflection exceptions
		} catch (ClassNotFoundException | SecurityException | IllegalAccessException | 
				 NoSuchMethodException | IllegalArgumentException | InstantiationException |
				 InvocationTargetException e) {
			System.out.println(String.format("Unrecognized command \'{}\'. Use \'help\' command", command));
		
		// handle any empty command
		} catch (IndexOutOfBoundsException e) {
			System.out.println("No command provided. Use \'help\' command");
		}
	}	
}
