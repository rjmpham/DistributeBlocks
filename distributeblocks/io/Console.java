package distributeblocks.io;

/*
 * The Console class is used to open a new window for printing messages.
 * This may be done instantiating the singleton ConsoleWindow on a new
 * thread.
 * 
 * The Console should be started with start(), and then used by calling log(). 
 */
public class Console {
	
	private static ConsoleWindow consoleWindow;
	private static boolean useStdOut = false;
	
	public static void start(){ 
		// start a new thread to run the window application
		Thread thread = new Thread(new ConsoleWindow());
        thread.start(); 
        
        do {
        	try {
				Thread.sleep(100);
				consoleWindow = ConsoleWindow.getInstance();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        // wait until second instantiation (created by  ConsoleWindow.launch())
        } while(ConsoleWindow.instantiations <= 1);  
	}
	
	/*
	 * set's whether log statements should go to standard out or not
	 */
	public static void redirectToStdOut() {
		useStdOut = true;
	}
	
	/*
	 * Log a string to the console.
	 */
	public static synchronized void log(String s) {
		if(useStdOut)
			System.out.println(s);
		else if (consoleWindow != null)
			consoleWindow.log(s);
		else
			return;
	}
	
	// overrided log methods for primitive
	public static void log(byte b) {
		log(String.valueOf(b));
	}
	
	public static void log(short s) {
		log(String.valueOf(s));
	}
	
	public static void log(int i) {
		log(String.valueOf(i));
	}
	
	public static void log(long l) {
		log(String.valueOf(l));
	}
	
	public static void log(float f) {
		log(String.valueOf(f));
	}
	
	public static void log(double d) {
		log(String.valueOf(d));
	}
	
	public static void log(char c) {
		log(String.valueOf(c));
	}
	
	public static void log(boolean b) {
		log(String.valueOf(b));
	}
	
	// catch-all for logging objects
	public static void log(Object o) {
		log(o.toString());
	}
	
}