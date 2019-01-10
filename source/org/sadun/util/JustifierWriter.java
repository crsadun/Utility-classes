/*
 * Created on Jul 5, 2004
 */
package org.sadun.util;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A Writer to justify text.
 * <p>
 * This writer right-justifies the characters sent to it to a given line length. It interprets the characters as
 * a stream of space-separated words and when a word doesn't fit in the given length, the line is justified and 
 * sent to the wrapped writer.
 * 
 * @version 1.03
 * @author Cristiano Sadun
 */
public class JustifierWriter extends FilterWriter {
	
	static class WordInfo {
		
		WordInfo(char[] cs, int start, int len) {
			this.cs=cs;
			this.start=start;
			this.length=len;
		}
		
		char [] cs;
		int start;
		int length;
		
		public String toString() {
			throw new RuntimeException();
		}
		
	}
	
	private int lineLength = 80;
	private StringBuffer buffer = new StringBuffer();
	private static String lineSep;
	private boolean justifyOnNewLine = false;
	private boolean preserveInitialSpaces = false;
	private boolean autoFlush = false;
	private boolean padOnNewLine = false;

	private boolean atStart = true;
	private List words = new ArrayList();
	private StringBuffer lastWordPiece = new StringBuffer();

	static {
		lineSep = System.getProperty("line.separator");
	}

	/**
	 * Create a justifier wrapping the given writer with the default linge length
	 * @param out the wrapped writer
	 */
	public JustifierWriter(Writer out) {
		this(out, 80);
	}

	/**
	 * Create a justifier wraping the given writer and using the given line length
	 * @param out the wrapped writer
	 * @param lineLenght the desired line length
	 */
	public JustifierWriter(Writer out, int lineLenght) {
		super(out);
		this.lineLength = lineLenght;
	}

	/**
	 * Write the given characters. If the characters contain a newline, the 
	 * line will be justified if {@link #isJustifyOnNewLine() justifyOnNewLine} is true
	 * and the embedded writer will be flushed if {@link #isAutoFlush() autoFlush} is true.
	 * <p>
	 * If the words exceed the given {@link #getLineLength() line lenght} the line is justified
	 * and sent to wrapped writer.
	 */
	public void write(char[] cbuf, int off, int len) throws IOException {
		synchronized(buffer) {
			addWords(cbuf, off, len);
		}
	}

	/**
	 * Write the given string. If the string contains a newline, the 
	 * line will be justified if {@link #isJustifyOnNewLine() justifyOnNewLine} is true
	 * and the embedded writer will be flushed if {@link #isAutoFlush() autoFlush} is true.
	 * <p>
	 * If the words exceed the given {@link #getLineLength() line lenght} the line is justified
	 * and sent to wrapped writer.
	 */
	public void write(String str, int off, int len) throws IOException {
		write(str.toCharArray(), off, len);
	}

	private void addWords(char[] cs, int off, int len) throws IOException {
		findWords(cs, off, len);
		for (Iterator i = words.iterator(); i.hasNext();) {
			WordInfo word = (WordInfo) i.next();
			i.remove();
			if (isLineSep(word.cs, word.start)) {
				outputBuffer(justifyOnNewLine);
			} else {
				if (buffer.length() + 1 + word.length > lineLength) {
					if (buffer.length()>0)
						// Justify
						outputBuffer(true);
				}
				if (buffer.length() > 0)
					buffer.append(" ");
				buffer.append(word.cs, word.start, word.length);
				if (atStart) atStart=false;
			}
		}

	}

	private void outputBuffer(boolean justify) throws IOException {
		if (justify)
			buffer = justify(buffer, lineLength, preserveInitialSpaces);
		if (padOnNewLine) {
			int n = lineLength - buffer.length();
			for (int i = 0; i < n; i++)
				buffer.append(' ');
		}
		out.write(buffer.toString());
		out.write(lineSep);
		atStart=true;
		if (autoFlush)
			out.flush();
		buffer.delete(0, buffer.length());
	}

	private void findWords(char[] cs, int off, int len) {
		int end = off + len;
		boolean wordFound = lastWordPiece.length() > 0;
		int currentStart = off;
		for (int i = off; i < end; i++) {
			if (Character.isSpace(cs[i])) {
				if (wordFound) {
					completeWord(cs,currentStart,i);
				} else if (atStart && preserveInitialSpaces) {
					// Jump to the first nonspace
					int j;
					for (j = i; j < end; j++) {
						if (!Character.isSpace(cs[j]))
							break;
					}
					addWord(cs, currentStart, j);
					i = j - 1;
				}
				wordFound = false;
				if (isLineSep(cs, i))
					addWord(lineSep.toCharArray(), 0, 2);

			} else {
				if (!wordFound) {
					wordFound = true;
					currentStart = i;
				} else if (lastWordPiece.length()+i-currentStart == lineLength) {
					// Word's longer than the line:add and reset currentstart
					completeWord(cs,currentStart,i);
					currentStart=i;
					addWord(lineSep.toCharArray(), 0, 2);
					
				} // else ignore non space
			}
		}

		if (wordFound && currentStart != end) {
			lastWordPiece.append(cs, currentStart, end - currentStart);
		}

	}

	private void completeWord(char[] cs, int currentStart, int i) {
		if (lastWordPiece.length() > 0) {
			lastWordPiece
					.append(cs, currentStart, i - currentStart);
			
			addWord(lastWordPiece.toString().toCharArray(), 0, lastWordPiece.length());
			lastWordPiece.delete(0, lastWordPiece.length());
		} else
			addWord(cs, currentStart, i
							- currentStart);
	}

	/**
	 * @param string
	 */
	private void addWord(char cs[], int start, int len) {
		words.add(new WordInfo(cs, start, len));
	}

	private boolean isLineSep(char[] cbuf, int pos) {
		for (int i = 0; i < lineSep.length(); i++) {
			if (i >= cbuf.length)
				return false;
			if (cbuf[pos + i] != lineSep.charAt(i))
				return false;
		}
		return true;
	}

	/**
	 * Return the given string, right-justified to the given line length, without preserving initial spaces.
	 * A single word will not be justified.
	 * 
	 * @param s the string to justified
	 * @param lineLength the desired line lenght.
	 * @return a string with the same contents as the given one, but right-justified
	 */
	public static String justify(String s, int lineLength) {
		return justify(new StringBuffer(s), lineLength, false).toString();
	}

	/**
	 * Return the given string, right-justified to the given line length, optionally preserving initial spaces.
	 * A single word will not be justified.
	 * 
	 * @param s the string to justified
	 * @param lineLength the desired line lenght.
	 * @param preserveInitialSpaces if true, initial spaces will be preserved
	 * @return a string with the same contents as the given one, but right-justified
	 */
	public static String justify(String s, int lineLength,
			boolean preserveInitialSpaces) {
		return justify(new StringBuffer(s), lineLength, preserveInitialSpaces).toString();
	}
	
	private static StringBuffer justify(StringBuffer buffer, int lineLength) {
		return justify(buffer, lineLength, false);
	}

	// Justify the current content of the buffer.
	private static StringBuffer justify(StringBuffer buffer, int lineLength,
			boolean preserveInitialSpaces) {
		StringBuffer sb = new StringBuffer();

		String words[] = new String[0];
		int initialSpaces = 0;
		if (preserveInitialSpaces) {
			for (int i = 0; i < buffer.length(); i++)
				if (!Character.isSpace(buffer.charAt(i))) {
					initialSpaces = i - 1;
					words = buffer.substring(i).split(" ");
					break;
				}
		} else
			words = buffer.toString().split(" ");
		
		if (words.length==0) return buffer;

		int nSpaces = lineLength;
		for (int i = 0; i < words.length; i++)
			nSpaces -= words[i].length();

		if (preserveInitialSpaces && initialSpaces > 0) {
			for (int i = 0; i < initialSpaces; i++)
				if (Character.isSpace(buffer.charAt(i)))
					sb.append(' ');
			nSpaces -= initialSpaces;
		}

		double ratio = ((double) nSpaces) / (words.length - 1);
		double total = 0, total0 = 0;
		for (int i = 0; i < words.length - 1; i++) {
			sb.append(words[i]);
			total += ratio;
			if (total - total0 >= 1) {
				int n = (int) (total - total0);
				for (int j = 0; j < n; j++) {
					sb.append(' ');
				}
				nSpaces -= n;
				total0 += n;
			}
		}
		if (words.length > 1) {
			for (int i = 0; i < nSpaces; i++)
				sb.append(' ');
		}
		sb.append(words[words.length - 1]);
		return sb;

	}

	/**
	 * Return the line length currently employed by this writer.
	 * @return the line length currently employed by this writer.
	 */
	public int getLineLength() {
		return lineLength;
	}
	
	/**
	 * Set the line length currently employed by this writer. This will affect only the 
	 * current line and the successive ones.
	 * @param lineLength the new line length
	 */
	public void setLineLength(int lineLength) {
		this.lineLength = lineLength;
	}
	
	/**
	 * Return true if the writer applies justification on explicit newlines.
	 * @return true if the writer applies justification on explicit newlines.
	 */
	public boolean isJustifyOnNewLine() {
		return justifyOnNewLine;
	}
	
	/**
	 * Set whether or not the writer applies justification on explicit newlines.
	 * @param justifyOnNewLine if true, the writer applies justification on explicit newlines.
	 */
	public void setJustifyOnNewLine(boolean justifyOnNewLine) {
		this.justifyOnNewLine = justifyOnNewLine;
	}
	
	/**
	 * Return true if the writer preserves intial spaces.
	 * @return true if the writer preserves intial spaces.
	 */
	public boolean isPreserveInitialSpaces() {
		return preserveInitialSpaces;
	}
	
	/**
	 * Set whether or not the writer preserves intial spaces.
	 * @param preserveInitialSpaces if true, the writer preserves intial spaces.
	 */
	public void setPreserveInitialSpaces(boolean preserveInitialSpaces) {
		this.preserveInitialSpaces = preserveInitialSpaces;
	}
	
	/**
	 * Return true if the writer pads output with space on a new line.
	 * @return true if the writer pads output with space on a new line.
	 */
	public boolean isPadOnNewLine() {
		return padOnNewLine;
	}
	
	/**
	 * Set whether or not the writer pads output with spaces on a new line.
	 * @param padOnNewLine if true, the writer pads output with spaces on a new line.
	 */
	public void setPadOnNewLine(boolean padOnNewLine) {
		this.padOnNewLine = padOnNewLine;
	}
	
	/**
	 * Return true if the writer automatically flushes the wrapped writer when a line is written.
	 * @return true if the writer automatically flushes the wrapped writer when a line is written.
	 */
	public boolean isAutoFlush() {
		return autoFlush;
	}
	
	/**
	 * Set whether or not the writer automatically flushes the wrapped writer when a line is written.
	 * @param autoFlush if true, the writer automatically flushes the wrapped writer when a line is written.
	 */
	public void setAutoFlush(boolean autoFlush) {
		this.autoFlush = autoFlush;
	}
	
	public static void main(String args[]) {
		StringWriter sw = new StringWriter();
		JustifierWriter jw = new JustifierWriter(sw);
		jw.setLineLength(20);
		PrintWriter pw = new PrintWriter(jw);
		while(true) {
			pw.println("Nel mezzo del cammin di nostra vita mi ritrovai per una selva oscura che la diretta via era smarrita.");
			System.out.println(sw.toString());
		}
	}
}