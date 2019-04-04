package distributeblocks.io;

import java.io.IOException;
import java.io.OutputStream;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

/*
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
	public TextArea textArea = new TextArea();	// default TextArea
	
	public TextAreaOutputStream() {} // use default TextArea
	
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
   
    public void appendText(String str) {
        Platform.runLater(() -> textArea.appendText(str));
    }
    
    public TextArea getTextArea() {
    	return textArea;
    }
}