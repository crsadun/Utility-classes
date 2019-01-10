package org.sadun.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * An Enumeration for a <tt>java.util.List</tt> object.
 * 
 * @author Cristiano Sadun
 */
public class ListEnumeration implements Enumeration {
	
	private Iterator iterator;
	
	public ListEnumeration(List list) {
		this.iterator=list.iterator();
	}

	/**
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	public boolean hasMoreElements() {
		return iterator.hasNext();
	}

	/**
	 * @see java.util.Enumeration#nextElement()
	 */
	public Object nextElement() {
		return iterator.next();
	}
	
	public static List asList(Enumeration e) {
		List l=new ArrayList();
		while(e.hasMoreElements())
			l.add(e.nextElement());
		return l;
	}

}
