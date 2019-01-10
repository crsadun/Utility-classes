package org.sadun.util.sis;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sadun.util.IndentedPrintWriter;
import org.sadun.util.ListMapIterator;

/**
 * Plug-in class for adding state information support to an mbean.
 * <p>
 * The Mbean owning an instance can record state information entries related
 * to a specific key and obtain a String to provide the console user with a log.
 * 
 * @author cris
 */
public class StateInfoSupport {
	
	private Map entries = new HashMap();
	private List keys = new ArrayList();
	private String lastKey;
	private int level;
	
	private synchronized void addEntry(String key, InfoEntry entry) { 
	
		if (! key.equals(lastKey)) {
			keys.add(key);
			lastKey=key;
		}
		
		List l = (List)entries.get(key);
		if (l==null)  { 
			entries.put(key, l=new ArrayList()); 
		}
		l.add(entry); 
	}

	/**
	 * Add a entry for the given key, containing a textual description.
	 * 	 * @param key	 * @param description	 */	
	public void addEntry(String key, String description) {  
		addEntry(key, new InfoEntry(description, level)); 
	}
	
	/**
	 * Add a entry for the given key, containing a textual description and
	 * a given exception object.
	 * 	 * @param key	 * @param description	 * @param e	 */
	public void addEntry(String key, String description, Throwable e) { 
		addEntry(key, new InfoEntry(description, level, e)); 
	}
	
	/**
	 * Add a entry for the same key as the last <tt>addEntry</tt> operation, 
	 * containing a textual description.
	 * <p>
	 * This method can be invoked only after one of the <tt>addEntry</tt> overloads
	 * with <tt>key</tt> parameters have been invoked.
	 * 	 * @param description	 */
	public void addEntry(String description) {  
		checkLastKey();
		addEntry(lastKey, new InfoEntry(description, level)); 
	}
	
	/**
 	 * Add a entry for the same key as the last <tt>addEntry</tt> operation, 
	 * containing a textual description anda given exception object.
	 * <p>
	 * This method can be invoked only after one of the <tt>addEntry</tt> overloads
	 * with <tt>key</tt> parameters have been invoked.
	 * 	 * @param description	 * @param e	 */
	public void addEntry(String description, Throwable e) { 
		checkLastKey();
		addEntry(lastKey, new InfoEntry(description, level, e)); 
	}
	
	private void checkLastKey() {
		if (lastKey==null) {
			IllegalStateException e = new IllegalStateException("StateInfoSupport: no last key set. Please use the addEntry(String key, ...) overload on the first entry.");
			throw e;
		}
	}
	
	/**
	 * Return a state log for the given key. If key is <b>null</b> or
	 * the empty string, all entries are descripted.
	 * <p>
	 * If the key does not exist, <b>null</b> is returned.
	 * 	 * @param key the key of interest.	 * @return a state log for the given key, or <b>null</b>. If key is <b>null</b> or
	 * the empty string, all entries are descripted.	 */
	public synchronized String getStateDescription(String key) {
		StringWriter sw = new StringWriter();
		IndentedPrintWriter pw = new IndentedPrintWriter(sw);
		Iterator i;
		
		if (key==null || "".equals(key))
			i = new ListMapIterator(entries, keys);
		else {
			List l = (List)entries.get(key);
			if (l==null) return null;
			i = l.iterator();
		}
		
		String currentKey=null;
		
		while(i.hasNext()) {
			String oldKey=currentKey;
			if (i instanceof ListMapIterator) {
				currentKey=(String)((ListMapIterator)i).getCurrentKey();
			} else currentKey=key;
			
			if (! currentKey.equals(oldKey)) {
				pw.println();
				pw.println("{"+currentKey+"} ");
				pw.println();
			}
			
			InfoEntry entry = (InfoEntry)i.next();
			pw.incIndentation(5+entry.getLevel());
			pw.println(entry);
			pw.decIndentation(5);
		}
		
		return sw.toString();
	}
	
	// Test method
	public static void main(String args[]) throws Exception {
		StateInfoSupport sis =new StateInfoSupport();
		sis.addEntry("intialization", "all is right");
		sis.addEntry("something going wrong", new IllegalStateException("illegal state"));
		sis.addEntry("doing something", "all ok");
		sis.addEntry("ops!", new RuntimeException("something didnt work"));
		
		System.out.println(sis.getStateDescription("intialization"));
		
	}
	

	/**
	 * Returns the level.
	 * @return int
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Sets the level.
	 * @param level The level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

}
