/*
 * Created on Mar 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

import org.sadun.util.HTMLAnchorIterator;

import junit.framework.TestCase;

/**
 *
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano Sadun</a>
 * @version 1.0
 */
public class HTMLAnchorIteratorTest extends TestCase {
	
	private Reader html; 
	private HTMLAnchorIterator iterator;

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(HTMLAnchorIteratorTest.class);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		html = new BufferedReader(new FileReader("c:/projects/ffp/doc/index.html"));
		iterator=new HTMLAnchorIterator(html);
	}
	
	public void testNext() {
		while(iterator.hasNext())
			System.out.println(iterator.next());
	}

}
