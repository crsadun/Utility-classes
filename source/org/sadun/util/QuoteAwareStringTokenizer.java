/*
 * Created on Jun 3, 2004
 */
package org.sadun.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A StringTokenizer-compatible class which takes quotes into account.
 * <p>
 * The tokenizer behaves like StringTokenizer, except that it accepts a set of quote pairs indicating opening and closing quote character
 * (denoted, at construction, by a String containing the pairs consecutively: <tt>()[]</tt> for example) and returns quoted portions
 * of the string as single tokens.
 * 
 * @author Cristiano Sadun
 */
public class QuoteAwareStringTokenizer implements Enumeration {

	private String str;
	private String delim;
	private String quotePairs;
	private boolean returnDelims;
	private char[][] quotes;
	private static String DEFAULT_QUOTEPAIRS = "\"\"''";
	private Object[][] entries;
	private int currEntryIndex;

	public QuoteAwareStringTokenizer(String str, String delim,
			String quotePairs, boolean returnDelims) {
		this.str = str;
		this.returnDelims = returnDelims;
		init(delim, quotePairs);
		tokenize();
	}
		
	private void init(String delim, String quotePairs) {
		this.delim = delim;
		this.quotePairs=quotePairs;
		if (quotePairs.length() % 2 != 0)
			throw new IllegalArgumentException(
					"Please specify quotes in pairs (e.g. \"\\\"\\\"''()\")");
		quotes = new char[quotePairs.length() / 2][2];
		for (int i = 0; i < quotePairs.length() / 2; i++) {
			quotes[i][0] = quotePairs.charAt(i * 2);
			quotes[i][1] = quotePairs.charAt(i * 2 + 1);
			if (delim.indexOf(quotes[i][0]) != -1)
				throw new IllegalArgumentException(
						"'"
								+ quotes[i][0]
								+ "' cannot be used as a delimiter since it has been passed as a quote character");
			if (delim.indexOf(quotes[i][1]) != -1)
				throw new IllegalArgumentException(
						"'"
								+ quotes[i][1]
								+ "' cannot be used as a delimiter since it has been passed as a quote character");
		}

	}

	private void tokenize() {
		// Tokenize
		List entries = new ArrayList();
		int currentQuoteIndex = -1;
		int curr = 0;
		int i = 0;
outer:		while (i < str.length()) {
			char c = str.charAt(i);
			if (currentQuoteIndex == -1) {
				int p;
				if ((p = delim.indexOf(c)) != -1) { // Check for delimiter
					Object[] entry;
					if (curr != i) {
						entry = new Object[]{str.substring(curr, i),
								new Integer(curr)};
						entries.add(entry);
					}

					do {
						if (returnDelims) {
							entry = new Object[]{delim.substring(p, p + 1),
									new Integer(i)};
							entries.add(entry);
						}
						i++;
						if (i>=str.length()) break;
					} while ((p = delim.indexOf(str.charAt(i))) != -1);

					curr = i;
				} else { // Check for quote
					for (int j = 0; j < quotes.length; j++) {
						if (c == quotes[j][0]) {

							if (i > 1 && str.charAt(i - 1) == '\\') { // Handle
								// escaped
								// quotes
								// Do nothing
							} else {
								currentQuoteIndex = j;
								if (curr != i) {
									Object[] entry = new Object[]{
											str.substring(curr, i),
											new Integer(curr)};
									entries.add(entry);

								}
								curr = i + 1;
							}
							break;
						}
					}

					// Both for quote or not, increment current char index
					i++;
				}
			} else {
				// Check for closing quote
				if (c == quotes[currentQuoteIndex][1]) {
					if (i > 1 && str.charAt(i - 1) == '\\') { // Handle escaped
						// quotes
						// Do nothing
					} else {
						Object[] entry = new Object[]{str.substring(curr, i),
								new Integer(curr)};
						entries.add(entry);
						currentQuoteIndex = -1;
						curr = i + 1;
					}
				}
				i++;
			}
		}
		if (currentQuoteIndex != -1)
			throw new NoSuchElementException(
					"Unterminated quotation (started with '"
							+ quotes[currentQuoteIndex][0] + "', expected '"
							+ quotes[currentQuoteIndex][1] + "')");
		
		if (curr<str.length()) {
			Object[] entry = new Object[]{str.substring(curr, str.length()),
					new Integer(curr)};
			entries.add(entry);
		}

		this.entries = new Object[entries.size()][2];
		entries.toArray(this.entries);

		for (i = 0; i < this.entries.length; i++) {
			this.entries[i][0] = unescapeQuotes((String) this.entries[i][0]);
			//System.out.println("{" + this.entries[i][0] + "} at position " + this.entries[i][1]);
		}

	}

	/**
	 * @param object
	 * @return
	 */
	private String unescapeQuotes(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\\' && i < s.length() - 1) {
				char c1 = s.charAt(i + 1);
				boolean found=false;	
				for (int j = 0; j < quotes.length; j++) {
					if (c1 == quotes[j][0] || c1 == quotes[j][1]) {
						sb.append(c1);
						found=true;
						i++;
						break;
					}
				}
				if (!found) sb.append(c);
			} else
				sb.append(c);
		}
		return sb.toString();
	}

	public QuoteAwareStringTokenizer(String str, String delim, String quotePairs) {
		this(str, delim, quotePairs, false);
	}

	public QuoteAwareStringTokenizer(String str, String delim,
			boolean returnDelims) {
		this(str, delim, DEFAULT_QUOTEPAIRS, returnDelims);
	}

	public QuoteAwareStringTokenizer(String str, String delim) {
		this(str, delim, false);
	}

	public QuoteAwareStringTokenizer(String str) {
		this(str, " ");
	}

	/**
	 * @return
	 */
	public int countTokens() {
		return entries.length - currEntryIndex;
	}

	/**
	 * @return
	 */
	public String nextToken() {
		if (!hasMoreTokens()) throw new NoSuchElementException("No more tokens");
		return (String) entries[currEntryIndex++][0];
	}

	/* (non-Javadoc)
	 * @see com.sun.tools.javac.v8.util.Enumeration#nextElement()
	 */
	public Object nextElement() {
		return nextToken();
	}
	
	/* (non-Javadoc)
	 * @see com.sun.tools.javac.v8.util.Enumeration#hasMoreElements()
	 */
	public boolean hasMoreElements() {
		return hasMoreTokens();
	}

	/**
	 * @return
	 */
	public boolean hasMoreTokens() {
		return currEntryIndex < entries.length;
	}
	
	public String nextToken(String delim, String quotePairs) {
		if (!hasMoreTokens()) throw new NoSuchElementException("No more tokens");
		int curPos=((Integer)entries[currEntryIndex][1]).intValue();
		str=str.substring(curPos);
		init(delim, quotePairs);
		tokenize();
		currEntryIndex=0;
		return nextToken();
	}
	
	public String nextToken(String delim) {
		return nextToken(delim, quotePairs);
	}
}