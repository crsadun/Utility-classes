package org.sadun.util;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A symbol table.
 * <p>
 * The {@link #defineSymbol(java.lang.String, java.lang.Object) defineSymbol()}
 * method can be invoked to define symbols.
 * <p>
 * The {@link #evaluate(java.lang.String) evaluate()}method can be used to
 * evaluate a string containing references to the symbol. In this case, the
 * "string value" obtained by invoking the
 * {@link #getStringValue(java.lang.String)getStringValue()} method is used.
 * <p>
 * Such references have the format <tt>&lt;marker&gt;&lt;open bracket&gt;<i>name</i>&lt;close bracket&gt;</tt>.
 * The default marker is '$', and the default brackets are <tt>()</tt>, so a
 * default reference looks like <tt>$(<i>name</i>)</tt>.
 * <p>
 * By default, if the values contains other symbols, the translation occurs
 * recursively.
 * <p>
 * If a symbol is not defined, the table may either fail, substitute a blank or
 * leave the symbol itself.
 * 
 * @version 1.2
 * @author Cristiano Sadun
 */
public class SymbolTable {

	public class UndefinedSymbolException extends IllegalArgumentException {

		private String symbolName;

		UndefinedSymbolException(String symbolName, String msg) {
			super(msg);
			this.symbolName = symbolName;
		}

		/**
		 * Returns the symbolName.
		 * 
		 * @return String
		 */
		public String getSymbolName() {
			return symbolName;
		}

	}

	/**
	 * A <tt>String</tt> to <tt>String</tt> map containing associations
	 * between symbol names (without markers or brackets) and values.
	 */
	protected Map symbolTable;

	/**
	 * A BitSet for the options.
	 */
	protected BitSet options = new BitSet();

	/**
	 * The current symbol marker character. Defaults to <tt>$</tt> (dollar
	 * character).
	 */
	protected char symbolMarker = '$';

	/**
	 * A char[2] array containing the current bracket characters. Defaults to
	 * <tt>(</tt>,<tt>)</tt> (round braces).
	 */
	protected char[] bracketPair = new char[] { '(', ')' };

	private String symbolDefSequence =
		symbolMarker + String.valueOf(bracketPair[0]);
	private String symbolEndSequence = String.valueOf(bracketPair[1]);

	private static final int NORMAL = 0;
	private static final int MARKERFOUND = 1;
	private static final int SEARCHMATCHINGBRACE = 2;
	private Set undefinedSymbols = new HashSet();

	/**
	 * Options mask (0x00) for the "fail on undefined symbol" option.
	 */
	public static final int FAIL_ON_UNDEFINED_SYMBOL = 0;

	/**
	 * Options mask (0x01) for the "return blank on undefined symbol" option.
	 */
	public static final int RETURN_BLANK_ON_UNDEFINED_SYMBOL = 1;

	/**
	 * Options mask (0x02) for the "return symbol on undefined symbol" option.
	 */
	public static final int RETURN_SYMBOL_ON_UNDEFINED_SYMBOL = 2;

	/**
	 * Options mask (0x04) for the "evaluate symbol values " option.
	 */
	public static final int EVALUATE_SYMBOL_VALUES = 4;

	/**
	 * Create a symbol table with the given symbol map and the given failure
	 * policy.
	 * 
	 * @param symbolTable
	 *            the symbol table to use, mapping <tt>String</tt> objects to
	 *            <tt>String</tt> objects
	 * @param failOnUndefinedSymbol
	 *            if <b>true</b>, an <tt>IllegalArgumentException</tt>
	 *            will be raised if an undefined is found when
	 *            {@link #evaluate(java.lang.String) resolving}a String. If
	 *            <b>false</b> the symbol table will ignore the symbol.
	 */
	public SymbolTable(Map symbolTable, int failureBehaviour) {
		this.symbolTable = symbolTable;
		if (failureBehaviour < FAIL_ON_UNDEFINED_SYMBOL
			|| failureBehaviour > RETURN_SYMBOL_ON_UNDEFINED_SYMBOL)
			throw new IllegalArgumentException(
				"Internal error: failureBehaviour must be an integer comprised between "
					+ FAIL_ON_UNDEFINED_SYMBOL
					+ " and "
					+ RETURN_SYMBOL_ON_UNDEFINED_SYMBOL);
		options.clear(FAIL_ON_UNDEFINED_SYMBOL);
		options.clear(RETURN_BLANK_ON_UNDEFINED_SYMBOL);
		options.clear(RETURN_SYMBOL_ON_UNDEFINED_SYMBOL);
		options.set(EVALUATE_SYMBOL_VALUES);
		options.set(failureBehaviour);
	}

	/**
	 * Create a symbol table with the given symbol map, which fails on
	 * undefined symbols.
	 * 
	 * @see #SymbolTable(java.util.Map, int)
	 * 
	 * @param symbolTable
	 *            the symbol table to use, mapping <tt>String</tt> objects to
	 *            <tt>String</tt> objects
	 */
	public SymbolTable(Map symbolTable) {
		this(symbolTable, FAIL_ON_UNDEFINED_SYMBOL);
	}

	/**
	 * Create a symbol table with an empty symbol map, which fails on undefined
	 * symbols.
	 */
	public SymbolTable() {
		this(new HashMap());
	}

	/**
	 * Defines a symbol providing symbol name and value.
	 * 
	 * @param name
	 *            the name of the symbol, in the form <tt>$(<i>name</i>)</tt>
	 *            or <i>name</i>
	 * @param value
	 *            the value of the symbol
	 */
	public synchronized void defineSymbol(String name, Object value) {
		if (name.startsWith(symbolDefSequence)
			&& name.endsWith(symbolEndSequence))
			name = name.substring(2, name.length() - 1);
		symbolTable.put(name, value);
	}

	/**
	 * Return the value of the symbol with the given name, or <b>null</b> if
	 * the symbol is undefined.
	 * 
	 * @param name
	 *            the name of the symbol, in the form <tt>$(<i>name</i>)</tt>
	 *            or <i>name</i>
	 * @return the value of the symbol with the given name, or <b>null</b> if
	 *         the symbol is undefined.
	 */
	public synchronized Object getValue(String name) {
		return symbolTable.get(name);
	}

	/**
	 * Return the value of the symbol with the given name, translated to String
	 * by its <tt>toString</tt> method, or <b>null</b> if the symbol is
	 * undefined.
	 * 
	 * @param name
	 *            the name of the symbol, in the form <tt>$(<i>name</i>)</tt>
	 *            or <i>name</i>
	 * @return the value of the symbol with the given name, or <b>null</b> if
	 *         the symbol is undefined.
	 */
	public synchronized String getStringValue(String name) {
		Object obj = symbolTable.get(name);
		if (obj == null)
			return null;
		return obj.toString();
	}

	/**
	 * Return <b>true</b> if a symbol is defined.
	 * 
	 * @param name
	 *            the name of the symbol, in the form <tt>$(<i>name</i>)</tt>
	 *            or <i>name</i>
	 * @return <b>true</b> if a symbol is defined
	 */
	public boolean isDefined(String name) {
		if (name.startsWith(symbolDefSequence)
			&& name.endsWith(symbolEndSequence))
			name = name.substring(2, name.length() - 1);
		return symbolTable.containsKey(name);
	}

	/**
	 * Evaluates a String, replacing occurences of each symbol (see the class
	 * comment for the format) with the corresponding value.
	 * <p>
	 * Depending on the current failure policy, will either ignore undefined
	 * symbols, or fail with an <tt>IllegalArgumentException</tt>, and the
	 * evaluation will or will not recursively apply to the symbol values.
	 * 
	 * @param s
	 *            the string to translate
	 * @return the evaluated string
	 */
	public synchronized String evaluate(String s) {
		undefinedSymbols.clear();
		return evaluate0(s, new ArrayList());
	}

	private String evaluate0(String s, List symbolChain) {

		StringBuffer sb = new StringBuffer();
		int state = NORMAL;
		int brace_1 = -1, brace_2 = -1;
		int brace_count = -1;
		synchronized (sb) {
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				switch (state) {
					case NORMAL :
						if (c == symbolMarker)
							state = MARKERFOUND;
						else
							sb.append(c);
						break;
					case MARKERFOUND :
						if (c == symbolMarker) {
							// Double dollar found, append '$'
							sb.append(symbolMarker);
							state = NORMAL;
						} else if (c == bracketPair[0]) {
							brace_1 = i;
							brace_count = 1;
							state = SEARCHMATCHINGBRACE;
						} else
							throw new IllegalArgumentException(
								"'"
									+ symbolMarker
									+ "' can be followed only by '"
									+ symbolMarker
									+ "' or '"
									+ bracketPair[0]
									+ "' : "
									+ s.substring(i - 1));
						break;
					case SEARCHMATCHINGBRACE :
						if (s.charAt(i) == bracketPair[0])
							brace_count++;
						else if (s.charAt(i) == bracketPair[1]) {
							brace_count--;
							if (brace_count == 0) {
								brace_2 = i;
								String symbolName =
									s.substring(brace_1 + 1, brace_2);

								if (symbolChain.contains(symbolName))
									throw new RuntimeException(
										"Recursive definition: symbol "
											+ symbolName
											+ " is defined in terms of itself");
								else
									symbolChain.add(symbolName);

								String symbolValue = getStringValue(symbolName);
								if (symbolValue == null) {
									if (options
										.get(FAIL_ON_UNDEFINED_SYMBOL)) {
										throw new UndefinedSymbolException(
											symbolName,
											"Symbol "
												+ symbolDefSequence
												+ symbolName
												+ symbolEndSequence
												+ " is not defined");
									} else if (
										options.get(
											RETURN_SYMBOL_ON_UNDEFINED_SYMBOL)) {
										symbolValue =
											symbolDefSequence
												+ symbolName
												+ symbolEndSequence;
										undefinedSymbols.add(symbolName);
									} else if (
										options.get(
											RETURN_BLANK_ON_UNDEFINED_SYMBOL)) {
										symbolValue = "";
									}
									sb.append(symbolValue);
								} else {
									if (options.get(EVALUATE_SYMBOL_VALUES))
										sb.append(
											evaluate0(
												symbolValue,
												symbolChain));
									else
										sb.append(symbolValue);
								}

								if (symbolChain.size() > 0)
									symbolChain.remove(symbolChain.size() - 1);

								i = brace_2;
								state = NORMAL;
							}
						}
						break;
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Sets the behaviour in case of symbol not found.
	 * 
	 * @param failureBehaviour
	 *            one of the failure behaviour constants
	 */
	public synchronized void setBehaviourOnUndefinedSymbol(int failureBehaviour) {
		if (failureBehaviour < FAIL_ON_UNDEFINED_SYMBOL
			|| failureBehaviour > RETURN_SYMBOL_ON_UNDEFINED_SYMBOL)
			throw new IllegalArgumentException(
				"Internal error: failureBehaviour must be an integer comprised between "
					+ FAIL_ON_UNDEFINED_SYMBOL
					+ " and "
					+ RETURN_SYMBOL_ON_UNDEFINED_SYMBOL);
		options.clear(FAIL_ON_UNDEFINED_SYMBOL);
		options.clear(RETURN_BLANK_ON_UNDEFINED_SYMBOL);
		options.clear(RETURN_SYMBOL_ON_UNDEFINED_SYMBOL);
		options.set(failureBehaviour);
	}

	/**
	 * Returns the failure behaviour constant denoting the current failure
	 * policy.
	 * 
	 * @param failureBehaviour
	 *            one of the failure behaviour constants
	 * @return the failure behaviour constant denoting the current failure
	 *         policy
	 * @param failureBehaviour
	 *            one of the failure behaviour constants
	 */
	public int setBehaviourOnUndefinedSymbol() {
		int behaviour = -1;
		if (options.get(FAIL_ON_UNDEFINED_SYMBOL))
			behaviour = FAIL_ON_UNDEFINED_SYMBOL;
		else if (options.get(RETURN_BLANK_ON_UNDEFINED_SYMBOL))
			behaviour = RETURN_BLANK_ON_UNDEFINED_SYMBOL;
		else if (options.get(RETURN_SYMBOL_ON_UNDEFINED_SYMBOL))
			behaviour = RETURN_SYMBOL_ON_UNDEFINED_SYMBOL;
		return behaviour;
	}

	/**
	 * Returns the symbolMarker.
	 * 
	 * @return char
	 */
	public char getSymbolMarker() {
		return symbolMarker;
	}

	/**
	 * Sets the symbolMarker.
	 * 
	 * @param symbolMarker
	 *            The symbolMarker to set
	 */
	public void setSymbolMarker(char symbolMarker) {
		this.symbolMarker = symbolMarker;
	}

	/**
	 * Returns the bracketPair.
	 * 
	 * @return char[]
	 */
	public synchronized char[] getBracketPair() {
		return new char[] { bracketPair[0], bracketPair[1] };
	}

	/**
	 * Sets the bracketPair. Only char[2] arrays are allowed.
	 * 
	 * @param bracketPair
	 *            The bracketPair to set
	 */
	public synchronized void setBracketPair(char[] bracketPair) {
		if (bracketPair.length != 2)
			throw new IllegalArgumentException("bracket pair must be a char[2] array");
		if (bracketPair[0] == bracketPair[1])
			throw new IllegalArgumentException("bracket pair array must contain 2 different characters");
		this.bracketPair = bracketPair;
		this.symbolDefSequence = symbolMarker + String.valueOf(bracketPair[0]);
		this.symbolEndSequence = String.valueOf(bracketPair[1]);
	}

	/**
	 * Return <b>true</b> if the map recursively evaluates symbol values.
	 * 
	 * @return <b>true</b> if the map recursively evaluates symbol values.
	 */
	public boolean isEvaluateSymbolValues() {
		return options.get(EVALUATE_SYMBOL_VALUES);
	}

	/**
	 * Set whether or not the map recursively evaluates symbol values.
	 * 
	 * @param v
	 *            if <b>true</b>,
	 *            {@link #evaluate(java.lang.String) evaluate()}will
	 *            recursively evaluate symbol values.
	 */
	public void setEvaluateSymbolValues(boolean v) {
		if (v)
			options.set(EVALUATE_SYMBOL_VALUES);
		else
			options.clear(EVALUATE_SYMBOL_VALUES);
	}

	/**
	 * If
	 * {@link #RETURN_SYMBOL_ON_UNDEFINED_SYMBOL RETURN_SYMBOL_ON_UNDEFINED_SYMBOL}
	 * is the failure behaviour, return the undefined symbols found during the
	 * last evaluation; else return <n>null</b>.
	 * <p>
	 * 
	 * @return Set
	 */
	public Set getUndefinedSymbolsForLastEvaluation() {
		if (!options.get(RETURN_SYMBOL_ON_UNDEFINED_SYMBOL))
			return null;
		return undefinedSymbols;
	}

	/**
	 * Return an iterator over the defined symbol names.
	 * 
	 * @return an iterator over the defined symbol names.
	 */
	public Iterator definedSymbols() {
		return symbolTable.keySet().iterator();
	}
	
	public String toString() {
	    PrintStringWriter pw = new PrintStringWriter();
	    for(Iterator i=definedSymbols();i.hasNext();) {
	        String symbolName=(String)i.next();
	        String symbolValue=(String)symbolTable.get(symbolName);
	        pw.print(symbolName);
	        pw.print(": ");
	        pw.print(symbolValue);
	        if (i.hasNext()) pw.print(", ");
	    }
	    return pw.toString();
	}

	/*
	 * A test method
	 * 
	 * public static void main(String args[]) throws Exception { SymbolTable st =
	 * new SymbolTable(); st.setSymbolMarker('#'); st.defineSymbol("s1",
	 * "life"); st.defineSymbol("s2", "is"); st.defineSymbol("s3", "#(s1)");
	 * System.out.println(st.evaluate("#(s1) #(s2) #(s3)"));
	 */

}
