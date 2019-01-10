package com.deltax.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A class to use a JPanel as an output stream.
 * <p>
 * The class extends <tt>OutputStream</tt> and outputs to a JPanel,
 * which can be obtained by invoking {@link #getPanel() getPanel()}.
 * <p>
 * The {@link #flush() flush()} method can be called to actually
 * update the associated panel.
 * 
 * @version 1.2
 */
public class JPanelOutputStream extends OutputStream {

	/**
	 * The number of maximum allowed lines, or -1.
	 */
	protected int maxLines;
	
	/**
	 * The associated JPanel object.
	 */
	protected JPanel panel;
	
	/**
	 * The JList used to display the output.
	 */
	protected JList list;
	
	/**
	 * The current line buffer.
	 */
	protected StringBuffer sb;
	
	/**
	 * The platform line separator
	 */
	protected String lineSep;
	
	private JScrollPane listScrollPane;

	/**
	 * Create JPanelOutputStream.
	 */
	public JPanelOutputStream() {
		this(-1);
	}

	/**
	 * Create JPanelOutputStream whose JPanel will display the given
	 * maximum number of lines.
	 * @param maxLines tha maximum number of lines of the associated JPanel.
	 */
	public JPanelOutputStream(int maxLines) {
		sb = new StringBuffer();
		lineSep = System.getProperty("line.separator");
		panel = new JPanel();
		this.maxLines = maxLines;
		panel.setLayout(new BorderLayout());
		//list = new JTextArea(10, 1);
		list = new JList();
		list.setModel(new DefaultListModel());
		//list.setEditable(false);
		panel.add("Center", listScrollPane = new JScrollPane(list));
		list.setForeground(Color.red);
		setFont(new Font("Monospaced", Font.PLAIN, 12));
	}

	/**
	 * Set the font with which the panel displays the text.
	 * @param font the font to use
	 */
	public void setFont(Font font) {
		list.setFont(font);
	}

	/**
	 * Get the font with which the panel displays the text.
	 */
	public Font getFont() {
		return list.getFont();
	}

	/**
	 * Create a panel which displays the given lines.
	 * @param data an array of lines
	 */
	public JPanelOutputStream(String data[]) {
		this(-1);
		DefaultListModel dlm = new DefaultListModel();
		for (int i = 0; i < data.length; i++)
			dlm.addElement(data[i]);
	}

	/**
	 * Clear the panel
	 */
	public void clear() {
		//list.setText("");
		list.setSelectionInterval(0, list.getModel().getSize()-1);
		list.clearSelection();
		list.updateUI();
	}

	/**
	 * Return the panel contents as a String. The platform default line separator
	 * is used to divide the lines.
	 * @return the panel contents as a String
	 */
	public String getContents() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		int size=list.getModel().getSize();
		DefaultListModel dlm = (DefaultListModel)list.getModel();
		for(int i=0;i<size;i++) 
			pw.println( dlm.get(i));
		
		return sw.toString();
		//return list.getText();
	}

	/**
	 * Return the panel associated to the panel.
	 */
	public JPanel getPanel() {
		return panel;
	}

	/*
	public static void main(String args[]) {
		JFrame f = new JFrame();
		f.setSize(300, 300);
		JPanelOutputStream pos = new JPanelOutputStream();
		PrintStream pr = new PrintStream(pos);
		
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(pos.getPanel());
		f.addWindowListener(new WindowAdapter() {

			public void WindowClosing(WindowEvent e) {
				System.exit(0);
			}

		});
		f.setVisible(true);
		
		pr.print("Hello\r\n");
		pr.print("world\r\n");
	}
	*/
	
	/**
	 * Implements the <tt>OutputStream</tt>'s write method.
	 * @param b the character to write
	 */
	public void write(int b) throws IOException {
		sb.append((char) b);
		//System.out.print((char)b);
		if (sb.toString().endsWith(lineSep)) {
			String s=sb.toString();
			DefaultListModel dlm = (DefaultListModel)list.getModel();
			dlm.addElement(s.substring(0, s.length()-lineSep.length()));
			/*list.append(s.substring(0, s.length()-lineSep.length()));
			list.append("\n");
			*/
			sb.delete(0, sb.length());
		}
	}

	/**
	 * Completes the current line by writing a line separator.
	 */
	public void flush() {
		for (int i = 0; i < lineSep.length(); i++)
			try {
				write(lineSep.charAt(i));
			} catch (IOException e) {
			}
	}

}