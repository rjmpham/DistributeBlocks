package distributeblocks.io;

import java.io.PrintStream;

import distributeblocks.io.ConsoleWindow;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;


/*
 * Singleton class to run a windowed console application.
 * This class should be used by instantiating an object and calling
 * the run() method in a new thread. 
 */
// TODO: There is a memory leak in that old logs are never cleared
public class ConsoleWindow extends Application implements Runnable{
	
	// TODO: figure out how to set these through launch
	private int width = 600;
	private int height = 300;
	private String title = "Network Log";
	
	// output stream
	private static TextAreaOutputStream textAreaOutputStream;
	private PrintStream out;
	
	// Singleton object
	private static ConsoleWindow instance;
	public static int instantiations = 0;

	/*
	 * Constructor that ensure singleton is instantiated
	 */
    public ConsoleWindow() {
        instance=this;
        instantiations++;
        
	textAreaOutputStream = new TextAreaOutputStream();
	out = new PrintStream(textAreaOutputStream, true);
    }
    
    /*
     * Println to window
     */
    public synchronized void log(String s) {
    	out.println(s);
    }

    /*
     * Return the singleton object
     */
    public static synchronized ConsoleWindow getInstance() {
        return instance;
    }
    
	/*
	 * Creates the application window with a TextArea bound to a
	 * TextAreaOutputStream.
	 * 
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override 
	public void start(Stage stage) {  
		
		//Create a text area- must be past to the output stream
		TextArea textArea = new TextArea();
		textAreaOutputStream.setTextArea(textArea);
		textAreaOutputStream.setSize(width, height);
		
		//Creating a Group object  
		Group root = new Group(textAreaOutputStream.getTextArea());   
		//Creating a scene object 
		Scene scene = new Scene(root, width, height);  
  
		//Setting title to the Stage 
		stage.setTitle(title); 
		
		//Adding scene to the stage 
		stage.setScene(scene); 
     
		//Displaying the contents of the stage 
		stage.show(); 
	}

	/*
	 * Launches the console window. This method is blocking
	 * until the window is closed or exits prematurely.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		ConsoleWindow.launch();
		
	}
}
