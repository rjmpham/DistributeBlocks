package distributeblocks.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group; 
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage; 

/*
 * The Console class is used to open a new window and redirect
 * standard out and standard error to the window. This may be done
 * by calling the static Console.launch() method, or by instantiating
 * a console and calling the console.run() method to start the window
 * in a new thread. Note that due to the JavaFX architecture, the instantiated
 * Console will be different than that actually used for the window
 * (run() calls a static method!).
 * 
 * This class also keeps a copy of System.out and System.err in the
 * public systemOut and systemErr fields. These may be used in case
 * it is desired for the original console to be used.
 */
/* TODO: Is this really a good architecture? preferably we could do
 * 			console.write(myString); 
 * 		instead of redirecting ALL standard output.
 */
public class Console extends Application implements Runnable{
	
	public static PrintStream systemOut = System.out;
	public static PrintStream systemErr = System.err;
	
	private int width = 600;
	private int height = 300;
	
	@Override 
	public void start(Stage stage) {  
		TextArea textArea = new TextArea();
		textArea.setPrefHeight(height);  
		textArea.setPrefWidth(width);
		
		// Redirect Standard out to the window output stream
		TextAreaOutputStream textAreaOutputStream = new TextAreaOutputStream(textArea);
		PrintStream printer = new PrintStream(textAreaOutputStream, true);
		System.setOut(printer);
		System.setErr(printer);
		
		//Creating a Group object  
		Group root = new Group(textAreaOutputStream.getTextArea());   
           
		//Creating a scene object 
		Scene scene = new Scene(root, width, height);  
  
		//Setting title to the Stage 
		stage.setTitle("Network Log"); 
		
		//Adding scene to the stage 
		stage.setScene(scene); 
     
		//Displaying the contents of the stage 
		stage.show(); 
	}      
	   
	public static void main(String args[]){ 
	      
		Thread object = new Thread(new Runnable() {

		@Override
		public void run() {
			while(true) {
				System.out.println("it works!"); 
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}});
        object.start(); 
          
        launch(args); 
	}
	
	@Override
	public void run() {
		Console.launch();
	}
}