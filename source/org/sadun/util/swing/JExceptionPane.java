package org.sadun.util.swing;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.JOptionPane;

/**
 * An extension of JOptionPane to show exception-related messages
 * 
 * @author cris
 */
public class JExceptionPane extends JOptionPane {
	
	/**
	 * Shows the exception in a dialog
	 */
	public static void showExceptionDialog(
		Component parent,
		String title,
		Throwable e) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(os);
			e.printStackTrace(ps);
			showMessageDialog(
				parent,
				os.toString(),
				title,
				JOptionPane.ERROR_MESSAGE
			);
	}
	
	public static void showExceptionDialog(
		Component parent,
		Throwable e) {
			showExceptionDialog(parent, "Exception occurred", e);
	}

}
