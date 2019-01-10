/*
 * Created on Jun 3, 2004
 */
package org.sadun.util;

import java.util.Enumeration;

import junit.framework.TestCase;

/**
 * @author Cristiano Sadun
 */
public class QuoteAwareStringTokenizerTest extends TestCase {

	private QuoteAwareStringTokenizer createTokenizer(boolean returnDelim) {
		String s = "'q0\\''  t (q1)";
		QuoteAwareStringTokenizer st= new QuoteAwareStringTokenizer(s, " ", "''()", returnDelim);
		return st;
	}
	
	private QuoteAwareStringTokenizer createTokenizer() {
		return createTokenizer(false);
	}

	public void testCountTokens() {
		QuoteAwareStringTokenizer st = createTokenizer();
		assertEquals(3, st.countTokens());
	}

	public void testUseNoDelim() {
		QuoteAwareStringTokenizer st = createTokenizer();
		assertTrue(st.nextToken().equals("q0'"));
		assertTrue(st.nextToken().equals("t"));
		assertTrue(st.nextToken().equals("q1"));
	}
	
	public void testUseDelim() {
		QuoteAwareStringTokenizer st = createTokenizer(true);
		assertTrue(st.nextToken().equals("q0'"));
		assertTrue(st.nextToken().equals(" "));
		assertTrue(st.nextToken().equals(" "));
		assertTrue(st.nextToken().equals("t"));
		assertTrue(st.nextToken().equals(" "));
		assertTrue(st.nextToken().equals("q1"));
	}
	
	public void testNoQuotes1() {
		String s = "'q0\\''  t (q1)";
		QuoteAwareStringTokenizer st= new QuoteAwareStringTokenizer(s, " ", "");
		assertTrue(st.nextToken().equals("'q0\\''"));
		assertTrue(st.nextToken().equals("t"));
		assertTrue(st.nextToken().equals("(q1)"));
	}
	
	public void testNoQuotes2() {
		String s = "a,b,c";
		QuoteAwareStringTokenizer st= new QuoteAwareStringTokenizer(s, ",","");
		assertTrue(st.nextToken().equals("a"));
		assertTrue(st.nextToken().equals("b"));
		assertTrue(st.nextToken().equals("c"));
	}
	
	public void testNoQuotes3() {
		String s = "a,b,c,";
		QuoteAwareStringTokenizer st= new QuoteAwareStringTokenizer(s, ",","");
		assertTrue(st.nextToken().equals("a"));
		assertTrue(st.nextToken().equals("b"));
		assertTrue(st.nextToken().equals("c"));
	}
	
	public void testEnumeration() {
		String s = "a,b,c";
		Enumeration enum = new QuoteAwareStringTokenizer(s, ",","");
		assertTrue(enum.nextElement().equals("a"));
		assertTrue(enum.nextElement().equals("b"));
		assertTrue(enum.nextElement().equals("c"));
	}
	
	public void testNextToken() {
		String s="a,b 'c'";
		QuoteAwareStringTokenizer st= new QuoteAwareStringTokenizer(s, ",");
		assertEquals("a", st.nextToken());
		assertEquals("b", st.nextToken(" "));
	}

}