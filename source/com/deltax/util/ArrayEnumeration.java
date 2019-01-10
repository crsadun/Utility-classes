package com.deltax.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * A resettable Enumeration over an array.
 * 
 * @author cris
 */
public class ArrayEnumeration implements Enumeration {
	
	private Object []array;
	private int curPos;

	/**
	 * Constructor for ArrayEnumeration.
	 */
	public ArrayEnumeration(Object []array) {
		this.array=array;
		reset();
	}
	
	/**
	 * Reset the enumeration. 	 */
	public void reset() {
		curPos=0;
	}

	/**
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	public boolean hasMoreElements() {
		return curPos<array.length;
	}

	/**
	 * @see java.util.Enumeration#nextElement()
	 */
	public Object nextElement() {
		if (! hasMoreElements()) throw new NoSuchElementException();
		return array[curPos++];
	}

	/**
	 * Returns the array.
	 * @return Object[]
	 */
	public Object[] getArray() {
		return array;
	}

}
