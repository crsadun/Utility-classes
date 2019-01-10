package org.sadun.util;

/**
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 * 
 * @author Cristiano Sadun
 */
public class StringFinder {

	/**
	 * Constructor for StringFinder.
	 */
	public StringFinder() {
		super();
	}

	public static int[] findNotQuoted(
		String src,
		String[] substrings,
		boolean ignoreCase,
		int startPos) {
		return findNotQuoted(src, substrings, "\'\"", ignoreCase, startPos);
	}

	public static int[] findNotQuoted(
		String src,
		String[] substrings,
		boolean ignoreCase) {
		return findNotQuoted(src, substrings, "\'\"", ignoreCase, 0);
	}

	public static int[] findNotQuoted(
		String src,
		String[] substrings,
		String quoteChars,
		boolean ignoreCase) {
		return findNotQuoted(src, substrings, quoteChars, ignoreCase, 0);
	}

	public static int findNotQuotedIndex(
		String src,
		String target,
		String quoteChars,
		boolean ignoreCase) {
		return findNotQuotedIndex(src, target, quoteChars, ignoreCase, 0);
	}

	public static int findNotQuotedIndex(
		String src,
		String target,
		String quoteChars,
		boolean ignoreCase,
		int startPos) {
		return findNotQuoted(
			src,
			new String[] { target },
			quoteChars,
			ignoreCase,
			startPos)[0];
	}

	public static int findNotQuotedIndex(
		String src,
		String target,
		boolean ignoreCase) {
		return findNotQuotedIndex(src, target, ignoreCase, 0);
	}

	public static int findNotQuotedIndex(
		String src,
		String target,
		boolean ignoreCase,
		int startPos) {
		return findNotQuoted(
			src,
			new String[] { target },
			"\"\'",
			ignoreCase,
			startPos)[0];
	}

	/**
	 * Efficiently search for one of many substring within a string, ignoring quoted
	 * text.
	 */
	public static int[] findNotQuoted(
		String src,
		String[] substrings,
		String quoteChars,
		boolean ignoreCase,
		int startPos) {
		if (startPos < 0 || startPos >= src.length())
			throw new IllegalArgumentException(
				"start position ("
					+ startPos
					+ ") lower than zero or greater than string length ("
					+ src.length()
					+ ")");
		int currentQuoteChar = -1;
		int[] result = new int[2];
		main : for (int i = startPos; i < src.length(); i++) {
			char c = src.charAt(i);
			if (currentQuoteChar != -1) {
				if ((char) currentQuoteChar == c) {
					// Check either double quote or closed string
					if (i == src.length() - 1) {
						currentQuoteChar = -1; // Closed string
					} else {
						if ((char) currentQuoteChar == src.charAt(i + 1))
							i++; // Ignore double quote
						else {
							currentQuoteChar = -1; // Closed string
						}
					}
				}
			} else {
				if (isQuoteChar(c, quoteChars)) {
					currentQuoteChar = c;
				} else {
					// Check if one of the substrings is from here
					for (int j = 0; j < substrings.length; j++) {
						if (startsWith(src, i, substrings[j], ignoreCase)) {
							result[0] = i;
							result[1] = j;
							return result;
						}
					}
				}
			}
		}
		result[0] = result[1] = -1;
		return result;
	}

	private static boolean startsWith(
		String src,
		int startPos,
		String target,
		boolean ignoreCase) {
		int c = 0;
		for (int i = startPos; i < src.length() && c < target.length(); i++) {
			char srcChar, targetChar;
			if (ignoreCase) {
				srcChar = Character.toLowerCase(src.charAt(i));
				targetChar = Character.toLowerCase(target.charAt(c++));
			} else {
				srcChar = src.charAt(i);
				targetChar = target.charAt(c++);
			}
			if (srcChar != targetChar)
				return false;
		}
		return true;
	}

	private static boolean isQuoteChar(char c, String quoteChars) {
		return quoteChars.indexOf(c) != -1;
	}

	public static void main(String args[]) {
		String src = "Hello 'World' park' park";
		int i = findNotQuotedIndex(src, "Hello", true, 5);
		System.out.println(i);
		if (i != -1)
			System.out.println(src.substring(i));
	}

}
