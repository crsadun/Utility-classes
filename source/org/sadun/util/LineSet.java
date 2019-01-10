package org.sadun.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * An ordered set of Strings, to which filters can be applied
 * 
 * @author cris
 */
public class LineSet {
			
	/**
	 * A Filter for matching certain lines in the set.
	 * 
	 * @author cris
	 */
	public interface Filter {
		
		/**
		 * Must return <b>true</b> if the given line (at the given position)
		 * is to be accepted.
		 * 		 * @param line the line to be checked		 * @param index the position of the line, from the top of the list
		 * @return boolean <b>true</b> if the given line (at the given position)
		 *                  is to be accepted.		 */
		public boolean match(String line, int index, Set info);
		
		public void onMatch(Set info);
	
	}
	
	public static Filter NULL_FILTER = new NullFilter();
	
	public abstract static class BaseFilter implements Filter {
		
		/**
		 * @see org.sadun.util.LineSet.Filter#onMatch(java.util.Set)
		 */
		public void onMatch(Set info) {
		}

	}
	
	/**
	 * A constant filter, which always matches or not matches
	 * 
	 * @author cris
	 */
	public static class ConstantFilter extends BaseFilter implements Filter {
		
		private boolean toMatch;
		
		
		public ConstantFilter(boolean toMatch, boolean toCut) {
			this.toMatch=toMatch;
		}

		/**
		 * @see org.sadun.util.LineSet.Filter#match(java.lang.String, int, java.util.Set)
		 */
		public boolean match(String line, int index, Set info) {
			return toMatch;
		}
	}
	
	/**
	 * A null line filter, which always matches.
	 * 
	 * @author cris
	 */
	private static final class NullFilter extends ConstantFilter{

		/**
		 * Constructor for NullFilter.
		 * @param toMatch
		 */
		private NullFilter() {
			super(true, false);
		}

	}
	
	/**
	 * A Filter which matches lines containing one or more
	 * substring.
	 * 
	 * @author cris	 */
	public static final class ContainsStringFilter extends BaseFilter implements Filter {
		
		private  boolean acceptIfYes;
		private boolean toCut;
		private String [] toCheck;
		private int lastMatchIndex=-1;
		
		/**
		 * Create a filter checking for one or more of the given strings
		 * being contained. 
		 * 		 * @param toCheck the strings to be used for matching
		 * @param toCut indicates whether further lines should be checked		 */
		public ContainsStringFilter(String [] toCheck, boolean acceptIfYes, boolean toCut) {
			this.acceptIfYes=acceptIfYes;
			this.toCheck=toCheck;
		}
		
		/**
		 * Create a filter checking for one or more of the given strings
		 * being contained; when a matches occurs, the check continues.
		 * 
		 * @param toCheck the strings to be used for matching
		 * @param toCut indicates whether further lines should be checked
		 */
		public ContainsStringFilter(String [] toCheck) {
			this(toCheck, true, false);
		}
		
		/**
		 * Create a filter checking for one given string
		 * being contained.
		 * 
		 * @param toCheck the string to be used for matching
		 */
		public ContainsStringFilter(String toCheck, boolean acceptIfYes, boolean toCut) {
			this(new String[] { toCheck}, acceptIfYes, toCut);
		}
		
		/**
		 * Create a filter checking for one given string
		 * being contained; when a matches occurs, the check continues.
		 * 
		 * @param toCheck the string to be used for matching
		 */
		public ContainsStringFilter(String toCheck) {
			this(toCheck, true, false);
		}

		
		/**
		 * @see org.sadun.util.LineSet.Filter#match(java.lang.String, int, java.util.Set)
		 */
		public boolean match(String line, int index, Set info) {
			if (toCut)
				if (info.contains(getClass())) return !acceptIfYes;
			for(int i=0;i<toCheck.length;i++) {
				if ((lastMatchIndex=line.indexOf(toCheck[i]))!=-1) return acceptIfYes;
			}
			return false;
		}

		/**
		 * Returns the last match index.
		 * @return int the last match index, or -1
		 */
		public int getLastMatchIndex() {
			return lastMatchIndex;
		}

	
		/**
		 * @see org.sadun.util.LineSet.Filter#onMatch(java.util.Set)
		 */
		public void onMatch(Set info) {
			info.add(getClass());
		}

	}
	
	private List lines=new ArrayList();

	/**
	 * Constructor for LineSet.
	 */
	public LineSet() {
		this(new String[0]);
	}
	
	/**
	 * Constructor for LineSet.
	 */
	public LineSet(String [] lines) {
		for(int i=0;i<lines.length;i++)
			add(lines[i]);
	}
	
	/**
	 * Add a line to the set.
	 * 	 * @param line	 */
	public void add(String line) {
		lines.add(line);
	}
	
	/**
	 * Count the lines matching the filter. A {@link #NULL_FILTER NULL_FILTER} 
	 * can be used.
	 * 	 * @param filter the filter to match	 * @return int the number of lines matching the filter	 */
	public int count(Filter filter) {
		if (filter==NULL_FILTER) return lines.size();
		else return countFiltered(filter);
	}
	
	private int countFiltered(Filter filter) {
		synchronized(lines) {
			Set info=new HashSet();
			int c=0;
			int mc=0;
			for(Iterator i=lines.iterator();i.hasNext();) {
				if (filter.match((String)i.next(), c++, info)) mc++;
			}
			return mc;
		}
	}
	
	private List applyFilter0(Filter filter) {
		if (filter==NULL_FILTER) return lines;
		synchronized(lines) {
			int c=0;
			List lines2=new ArrayList();
			Iterator i = lines.iterator();
			Set info=new HashSet();
			while(i.hasNext()) {
				String s=(String)i.next();
				if (filter.match(s, c++, info)) {
					lines2.add(s);
					filter.onMatch(info);
				} 
			}
			return lines2;
		}
	}
	
	public List getLinesList(Filter filter) {
		return applyFilter0(filter);
	}
	
	public String [] getLines(Filter filter) {
		List l = applyFilter0(filter);
		String [] result = new String[l.size()];
		l.toArray(result);
		return result;
	}
	
	public String toString(Filter filter) {
		synchronized(lines) {
			List l = applyFilter0(filter);
			StringWriter sw=new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			for(Iterator i=l.iterator();i.hasNext();) {
				pw.println(i.next());
			}
			return sw.toString();
		}
	}
	
	public String toString() {
		return toString(NULL_FILTER);
	}
	
	public static LineSet create(String text, String lineSeparator) {
		StringTokenizer st = new StringTokenizer(text, lineSeparator);
		LineSet ls = new LineSet();
		while(st.hasMoreTokens()) {
			ls.add(st.nextToken());
		}
		return ls;
	}
	
	public static LineSet create(String text) {
		return LineSet.create(text, System.getProperty("line.separator"));
	}

}
