package com.deltax.util;

import java.io.PrintStream;

import javax.swing.JPanel;

/**
 * A PrintStream to embed a {@link JPanelOutputStream JPanelOutputStream}
 * 
 * @version 1.0
 * @author Cristiano Sadun
 */
public class JPanelPrintStream extends PrintStream {

	/**
	 * Constructor for JPanelPrintStream
	 * @param arg0
	 */
	public JPanelPrintStream(JPanelOutputStream arg0) {
		super(arg0);
	}
	
	/**
	 * Constructor for JPanelPrintStream.
	 */
	public JPanelPrintStream() {
		super(new JPanelOutputStream());
	}
	
	/**
	 * Return the JPanel underlying the stream
	 * 	 * @return JPanel	 */
	public JPanel getPanel() { return ((JPanelOutputStream)out).panel; }
	
	public String getContents() { return ((JPanelOutputStream)out).getContents(); }
	
	/**
	 * Clean up the contents of the JPanel underlying the stream	 */
    public void clear() { ((JPanelOutputStream)out).clear(); }
    
}
