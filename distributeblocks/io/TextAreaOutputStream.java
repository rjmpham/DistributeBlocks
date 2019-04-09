package distributeblocks.io;

import java.io.IOException;
import java.io.OutputStream;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

/**
 * This class outputs all parameters of the write() method
 * to a JavaFX TextArea object. If the no-args constructor is called
 * A default TextArea will be created, which can be accessed with
 * the getTextArea() method.
 * 
 * If a TextAreaOutputStream is wrapped in a PrintStream, messages
 * can be redirected to a window wrapping the TextArea.
 */
public class TextAreaOutputStream extends OutputStream {
	
	@FXML
	public TextArea textArea;
	
	// textArea cannot be instantiated until the Application.launch() method is called
	public TextAreaOutputStream() {	}
	
	public TextAreaOutputStream(TextArea textArea) {
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
   
    /**
     * Puts text into the actual text area. This is called by
     * each of the overriden write() method.
     * 
     * @param s		String to log
     */
    public void appendText(String s) {
    	if(textArea != null)
    		Platform.runLater(() -> textArea.appendText(s));
    }
    
    /**
     * Set's the text area to log too
     * 
     * @param textArea
     */
    public void setTextArea(TextArea textArea) {
    	this.textArea = textArea;
    }
    
    /**
     * Set's the size of the local textArea
     * 
     * @param width		width of textArea
     * @param height	height of textArea
     */
    public void setSize(int width, int height) {
    	if (textArea != null) {
			textArea.setPrefHeight(height);  
			textArea.setPrefWidth(width);
    	}
    }
    
    // Getter methods
    public TextArea getTextArea() { return textArea; }
}