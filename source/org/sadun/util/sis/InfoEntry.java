package org.sadun.util.sis;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;

import org.sadun.util.IndentedPrintWriter;

/**
 * A class to collect entries about the state of an MBean.
 * 
 * @author cris
 */
class InfoEntry {
	
	private String description;
	private Throwable e;
	private Date timestamp;
	private int level;
	
	public InfoEntry(String description, int level) {
		this(description, level, null);
	}
	
	public InfoEntry(String description, int level, Throwable e) {
		this.timestamp=new Date();
		this.description=description;
		this.e=e;
	}
	
	public synchronized String toString() {
		StringWriter sw=new StringWriter();
		IndentedPrintWriter pw = new IndentedPrintWriter(sw);
		pw.print("[");
		pw.print(DateFormat.getDateTimeInstance().format(timestamp));
		pw.print("] ");
		pw.print(description);
		if (e!=null) {
			pw.println(" (stack trace follows):");
			pw.println();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(os));
			pw.incIndentation(5);
			pw.print(os.toString());
			pw.decIndentation(5);
		}
		return sw.toString();
	}
	
	/**
	 * Returns the level.
	 * @return int
	 */
	public int getLevel() {
		return level;
	}

}
