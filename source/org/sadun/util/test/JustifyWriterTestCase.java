/*
 * Created on Jul 5, 2004
 */
package org.sadun.util.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.sadun.util.JustifierWriter;
import org.sadun.util.TieWriter;

/**
 * @author Cristiano Sadun
 */
public class JustifyWriterTestCase extends TestCase {

	private StringWriter sw;
	private JustifierWriter writer;
	private PrintWriter printWriter;
	private int lineSepLength = System.getProperty("line.separator").length();
	
	private static final int TEST_LINE_LENGTH=20;

	protected void setUp() throws Exception {
		sw = new StringWriter();
		printWriter = new PrintWriter(writer = new JustifierWriter(
				new TieWriter(sw, new OutputStreamWriter(System.out)), TEST_LINE_LENGTH));
		writer.setAutoFlush(true);
		System.out.println();
		for (int i=0;i<TEST_LINE_LENGTH;i++) {
			System.out.print(i % 10);
		}
		System.out.println();
	}
	
	public void testWordLongerThanLine() {
		// Build a string longer than the line
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<TEST_LINE_LENGTH+1;i++)
			sb.append((char)('a'+(i%10)));
		String s=sb.toString();
		printWriter.println(s);
		printWriter.flush();
		assertEquals(TEST_LINE_LENGTH+2*lineSepLength+1, sw.toString().length());
	}
	
	public void testTwoWords() {
		writer.setJustifyOnNewLine(true);
		printWriter.println("Hello world!");
		printWriter.flush();
		assertEquals(TEST_LINE_LENGTH + lineSepLength, sw.toString().length());
	}
	
	/*
	public static void testIndentedPrintWriter() {
		JustifierWriter w;
		IndentedPrintWriter pw = new IndentedPrintWriter(w=new JustifierWriter(new OutputStreamWriter(System.out), TEST_LINE_LENGTH));
		w.setPreserveInitialSpaces(true);
		w.setJustifyOnNewLine(true);
		w.setAutoFlush(true);
		pw.println("Hello everybody");
		pw.incIndentation(3);
		pw.println("This is a test");
	}
	*/

	
	
	public void testParagraph() throws IOException {
		String testString = "Hello darkness my old friend, I've come to talk to you again; because a vision softly creeping, left its seeds while I was sleeping";
		writer.setJustifyOnNewLine(true);
		writer.setPadOnNewLine(true);
		printWriter.println(testString);
		printWriter.flush();
		BufferedReader reader=  new BufferedReader(new StringReader(sw.toString()));
		String line=null;
		do {
			line=reader.readLine();
			if (line==null) continue;
			assertEquals(TEST_LINE_LENGTH, line.length());
		} while(line!=null);
			
	}
	
	public void testJustification() {
		System.out.println();
		String s= "Hello world!";
		s = JustifierWriter.justify(s, TEST_LINE_LENGTH, true).toString();
		System.out.println(s);
		assertEquals(TEST_LINE_LENGTH, s.length());
	}

	public void testSingleWord() {
		String testString = "Hello!";
		printWriter.println(testString);
		printWriter.flush();
		assertEquals(testString.length() + lineSepLength, sw.toString()
				.length());
	}

	

	

	public void testPreserveSpace() {
		writer.setJustifyOnNewLine(true);
		writer.setPreserveInitialSpaces(true);
		printWriter.println("   Hello world!");
		printWriter.flush();
		assertTrue(sw.toString().startsWith("   "));
		assertEquals(TEST_LINE_LENGTH + lineSepLength, sw.toString().length());
	}
	
	

}