package org.sadun.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A special lineset with services for exception stack traces.
 * 
 * @author cris
 */
public class StackTraceLineSet extends LineSet {
	
	/**
	 * Constructor for StackTraceLineSet.
	 */
	public StackTraceLineSet(Throwable e) {
		this(extractTraceLines(e));
	}
	
	public StackTraceLineSet(String stackTrace) {
		this(extractTraceLines(stackTrace));
	}
	
	public StackTraceLineSet(String[] stackTraceLines) {
		for(int i=0;i<stackTraceLines.length;i++)
			add(stackTraceLines[i]);
	}
	
	private static String[] extractTraceLines(Throwable e) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		e.printStackTrace(ps);
		return extractTraceLines(os.toString());
		
	}
	
	private static String[] extractTraceLines(String stackTrace) {
		BufferedReader br=new BufferedReader(new StringReader(stackTrace));
		String line;
		List l=new ArrayList();
		try {		
			while((line=br.readLine())!=null) l.add(line);
		} catch(IOException e) {
			// Can't happen
		}
		String []result=new String[l.size()];
		l.toArray(result);
		return result;
	}
	
}
