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

public class Console extends Application {
	
	private int width = 600;
	private int height = 300;
	
	@Override 
	public void start(Stage stage) {  
		TextArea textArea = new TextArea();
		textArea.setPrefHeight(height);  
		textArea.setPrefWidth(width);
		
		ConsoleController consoleController = new ConsoleController(textArea);
		
		PrintStream printer = new PrintStream(consoleController, true);
		
		System.setOut(printer);
		System.setErr(printer);
		
		//Creating a Group object  
		Group root = new Group(consoleController.getTextArea());   
           
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}});
        object.start(); 
          
        launch(args); 
	}
	
	
	public class ConsoleController extends OutputStream {

		@FXML
		public TextArea textArea = new TextArea();	// default size
		
		public ConsoleController() {}
		
		public ConsoleController(TextArea textArea) {
			this.textArea = textArea;
		}
		    	
        @Override
        public void write(int b) throws IOException {
            appendText(String.valueOf((char) b));
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            appendText(new String(b, off, len));
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }
       
        public void appendText(String str) {
	        Platform.runLater(() -> textArea.appendText(str));
	    }
        
        public TextArea getTextArea() {
        	return textArea;
        }
	}
}