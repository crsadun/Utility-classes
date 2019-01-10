/*
 * Created on Mar 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.sadun.util.PushbackReader;

import junit.framework.TestCase;

/**
 *
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano Sadun</a>
 * @version 1.0
 */
public class PushbackReaderTest extends TestCase {
	
	private Reader reader;
	private PushbackReader pr;

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(PushbackReaderTest.class);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		reader= new StringReader("Hello\r\nworld");
		pr = new PushbackReader(reader);
	}
	
	public void testUnread() throws IOException {
		int c=pr.read();
		pr.unread(c);
		int c2=pr.read();
		assertEquals(c, c2);
	}
	
	public void testReadNext() throws IOException {
		String s = pr.readNext(5);
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<5;i++) {
			sb.append((char)pr.read());
		}
		assertEquals(s, sb.toString());
	}
	public void testNextEquals() throws IOException {
		boolean nextIsHello = pr.nextEquals("Hello");
		assertTrue(nextIsHello);
		boolean nextIsWorld = pr.nextEquals("World");
		assertFalse(nextIsWorld);
		nextIsHello = pr.nextEquals("Hello", false);
		assertTrue(nextIsHello);
		nextIsWorld = pr.nextEquals("\r\nworld");
		assertTrue(nextIsWorld);
	}

}
