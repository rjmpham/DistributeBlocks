package distributeblocks.cli;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Callable;
import picocli.CommandLine;

import distributeblocks.Node;

/**
 * Interface to parse and process user commands.
 * This is capable of handing commands received at run time,
 * as well as which the node is running.
 */
public class CommandLineInterface implements Runnable{
	private static Scanner keyboard = new Scanner(System.in);
	
	private Node node; 					// the node who ran this CLI
	
	public CommandLineInterface(Node node) {
		this.node = node;
	}
	
	/**
	 * Repeatedly gets user input and attempt to parse it
	 * as a recognized command.
	 */
	@Override
	public void run() {
		String input;
		while(true) {
			input = keyboard.nextLine();
			parseCommand(input.split("\\s+"));
		}
	}
		
	/**
	 * Parses a list of command line arguments and
	 * performs the requested action.
	 * 
	 * @param args	command line args
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
			CommandLine.call(commandHandler, Arrays.copyOfRange(args, 1, args.length));
			
		// handle reflection exceptions
		} catch (ClassNotFoundException | SecurityException | IllegalAccessException | 
				 NoSuchMethodException | IllegalArgumentException | InstantiationException |
				 InvocationTargetException e) {
			System.out.println(String.format("Unrecognized command \'%s\'. Use \'help\' command", command));
		
		// handle any empty command
		} catch (IndexOutOfBoundsException e) {
			System.out.println("No command provided. Use \'help\' command");
		}
	}
	
	/**
	 * Gets the object name corresponding to a user command.
	 * This method ensures that there is no naming conflict within
	 * this package and other packages of the project, since every
	 * class here has the "Handler" suffix.
	 * 
	 * @param command	user inputed command
	 * 
	 * @return "distributeblocks.cli." + sentenceCase(command) + "Handler"
	 */
	private static String qualifyName(String command) {
		String first = String.valueOf(command.charAt(0)).toUpperCase();
		String theRest = command.substring(1);
		return "distributeblocks.cli." + first + theRest + "Handler";
	}
}
