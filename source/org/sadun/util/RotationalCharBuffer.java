/*
 * Created on Jan 19, 2004
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.sadun.util;

import java.util.logging.Logger;

/**
 * A rotational character buffer.
 * 
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano
 *         Sadun</a>
 * @version 1.0
 */
public class RotationalCharBuffer {
	
	private char[] buffer;
	private int pos = 0; // The contents' start position in the buffer
	private boolean modified = true;
	private String lastImage = null;

	/**
	 * Create a buffer with the given lenght.
	 * 
	 * @param length the lenght of the buffer
	 */
	public RotationalCharBuffer(int length) {
		buffer = new char[length];
	}

	/**
	 * Add a character to the right of the current position.
	 * <p>
	 * The characters on the left are shifted one position and the leftmost
	 * discarded, if needed.
	 * 
	 * @param c the character to add
	 */
	public void addToRight(char c) {
		buffer[pos++] = c;
		if (pos == buffer.length)
			pos = 0;
		modified = true;
	}

	/**
	 * Invoke {@link #addToRight(char)}on an entire string, one character at a
	 * time, starting from the left.
	 * 
	 * @param s the string to add.
	 */
	public void addToRight(String s) {
		if (s.length() == 1)
			Logger.getAnonymousLogger().fine(
				"addToRight(String) invoked with 1-length string, use addToRight(char) for better performance");
		for (int i = 0; i < s.length(); i++)
			addToRight(s.charAt(i));
	}

	/**
	 * Add a character to the left of the current position.
	 * <p>
	 * The characters on the right are shifted one position and the rightmost
	 * discarded, if needed.
	 * 
	 * @param c the character to add
	 */
	public void addToLeft(char c) {
		pos--;
		if (pos == -1)
			pos = buffer.length - 1;
		buffer[pos] = c;
		//printBuffer();
		modified = true;
	}

	/**
	 * Invoke {@link #addToLeft(char)}on an entire string, one character at a
	 * time, starting from the left.
	 * 
	 * @param s the string to add.
	 */	
	public void addToLeft(String s) {
		if (s.length() == 1)
			Logger.getAnonymousLogger().fine(
				"addToRight(String) invoked with 1-length string, use addToRight(char) for better performance");
		for (int i = 0; i < s.length(); i++)
			addToLeft(s.charAt(i));
	}

	private void printBuffer() {
		System.out.print("BUFFER: ");
		for(int i=0;i<buffer.length;i++) {
			if (i==pos) System.out.print("*");
			if (buffer[i]==0) System.out.print("#");
			else System.out.print(buffer[i]);
			System.out.print(" ");
		}
		System.out.println();
	}
	
	
	/**
	 * Return the size of the buffer.
	 * @return the size of the buffer.
	 */
	public int size() { return buffer.length; }

	/**
	 * Return a string image of the buffer.
	 * @return a string image of the buffer.
	 */
	public String toString() {
		if (!modified)
			return lastImage;
		StringBuffer sb = new StringBuffer();
		for (int i = pos; i < buffer.length; i++)
			if (buffer[i]!=0) sb.append(buffer[i]);
		for (int i = 0; i < pos; i++)
			                    if (buffer[i]!=0) sb.append(buffer[i]);
		lastImage = sb.toString();
		modified = true;
		return lastImage;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof RotationalCharBuffer) {
			RotationalCharBuffer rcb=(RotationalCharBuffer)obj;
			return rcb.toString().equals(toString()) && rcb.pos==pos;
		} else if (obj instanceof char[]) return new String((char[])obj).equals(toString());
		else if (obj instanceof CharSequence) return ((CharSequence)obj).toString().equals(toString());
		return false;
	}

}
