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


public class Console implements Runnable{
	
	
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
        
        // TODO: get the instance from this
        ConsoleWindow.launch(args); 
	}
	
	@Override
	public void run() {
		ConsoleWindow.launch();
	}
	
	/*
	 * The Console class is used to open a new window and redirect
	 * standard out and standard error to the window. This may be done
	 * by calling the static Console.launch() method, or by instantiating
	 * a console and calling the console.run() method to start the window
	 * in a new thread. Note that due to the JavaFX architecture, the instantiated
	 * Console will be different than that actually used for the window
	 * (run() calls a static method!).
	 * 
	 */
	/* TODO: Is this really a good architecture? preferably we could do
	 * 			console.write(myString); 
	 * 		instead of redirecting ALL standard output.
	 */
	public class ConsoleWindow extends Application{
		
		// TODO: figure out how to set these through launch
		private int width = 600;
		private int height = 300;
		private String title = "Network Log";
		
		protected PrintStream out;
		
		@Override 
		public void start(Stage stage) {  
			// create text area for the output stream
			TextArea textArea = new TextArea();
			textArea.setPrefHeight(height);  
			textArea.setPrefWidth(width);
			
			// Set the window out to the text area output stream
			TextAreaOutputStream textAreaOutputStream = new TextAreaOutputStream(textArea);
			PrintStream out = new PrintStream(textAreaOutputStream, true);
			
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
	}
}