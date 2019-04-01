package distributeblocks.cli;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;
import java.util.concurrent.Callable;
import picocli.CommandLine;

import distributeblocks.Node;

// TODO: handler to tell the user what commands are available
// TODO: handler for exiting the program
// TODO: handler for connecting a wallet to the node
// TODO: handler for making a transaction
// TODO: handler for enabling/ disabling mining
// TODO: handler for counting funds in the wallet

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
			// TODO: either handle quotes, or make sure no valid arg has whitespace
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
			Class handlerClass = Class.forName(qualifyName(command));
			Constructor constructor = handlerClass.getConstructor(Node.class);
			Callable commandHandler = (Callable) constructor.newInstance(node);
			
			//call the command parser on the hander
			CommandLine.call(commandHandler, args);
			
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
	
	/*
	 * Gets the object name corresponding to a user command.
	 * This method ensures that there is no naming conflict within
	 * this package and other packages of the project, since every
	 * class here has the "Handler" suffix.
	 */
	private static String qualifyName(String command) {
		String first = String.valueOf(command.charAt(0)).toUpperCase();
		String theRest = command.substring(1);
		return first + theRest + "Handler";
	}
}