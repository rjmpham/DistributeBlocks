package distributeblocks.io;

/*
 * The classes in this file are based on those from:
 * 		http://www.java2s.com/Tutorials/Java/Swing_How_to/JFrame/Create_Console_JFrame.htm
 * Credit for the original code goes to the author of that post.
 */
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class Console extends JFrame {
	
	public static void main(String[] args) {
		ConsoleWindow consoleWindow = new ConsoleWindow();
		consoleWindow.init();
		Console launcher = new Console();
		launcher.setVisible(true);
		consoleWindow.getFrame().setLocation(
				launcher.getX() + launcher.getWidth() + launcher.getInsets().right,
				launcher.getY());
	}

	private Console() {
		super();
		setSize(600, 600);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
}

class ConsoleWindow {
	final JFrame frame = new JFrame();
	public ConsoleWindow() {
		JTextArea textArea = new JTextArea(24, 80);
		textArea.setBackground(Color.BLACK);
		textArea.setForeground(Color.LIGHT_GRAY);
		textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    
		System.setOut(new PrintStream(new OutputStream() {
      
			@Override
			public void write(int b) throws IOException {
				textArea.append(String.valueOf((char) b));
			}
		}));
		frame.add(textArea);
	}
	
	public void init() {
		frame.pack();
		frame.setVisible(true);
	}
	
	public JFrame getFrame() {
		return frame;
	}
}