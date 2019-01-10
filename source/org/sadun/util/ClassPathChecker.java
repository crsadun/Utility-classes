/*
 * Created on Jun 7, 2004
 */
package org.sadun.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies wether or not all the entries in classpath are available
 * 
 * @author Cristiano Sadun
 */
public class ClassPathChecker {
	
	private String classPath;
	private static final int MISSING=0;
	private static final int EXISTING=1;
	
	private static final ClassPathChecker checker = new ClassPathChecker();
	
	public ClassPathChecker(String classPath) {
		this.classPath=classPath;
	}
	
	public ClassPathChecker() {
		this(System.getProperty("java.class.path"));
	}
	
	public List findMissingSet() {
		return find(MISSING);
	}
	
	public List findExistingSet() {
		return find(EXISTING);
	}

	
	public static ClassPathChecker getInstance() { return checker; }
	
	/**
	 * @param existing2
	 * @return
	 */
	private List find(int mode) {
		assert mode==MISSING || mode==EXISTING;
		ClassPathIterator i = new ClassPathIterator(classPath);
		List l=new ArrayList();
		while(i.hasNext()) {
			File f = i.nextEntryFile();
			if ( (mode==MISSING && ! f.exists()) || (mode==EXISTING && f.exists()) ) {
				try {
					l.add(f.getCanonicalPath());
				} catch (IOException e) {
					l.add(f.getAbsolutePath());
				}
			}
		}
		return l;
	}
	
	public String listMissingEntries() {
		List l = findMissingSet();
		if (l.size()==0) return "(all entries in classpath exist)";
		StringBuffer sb = new StringBuffer("The following entries cannot be opened:");
		sb.append(System.getProperty("line.separator"));
		ObjectLister ol = new ObjectLister(System.getProperty("line.separator"));
		sb.append(ol.list(l));
		return sb.toString();
	}
	
	public String listExistingEntries() {
		List l = findExistingSet();
		if (l.size()==0) return "(no entry in classpath exist)";
		StringBuffer sb = new StringBuffer("The following entries can be opened:");
		sb.append(System.getProperty("line.separator"));
		ObjectLister ol = new ObjectLister(System.getProperty("line.separator"));
		sb.append(ol.list(l));
		return sb.toString();
	}
	
	public String getCheckResult() {
		StringBuffer sb = new StringBuffer("Classpath is "+classPath);
		sb.append(System.getProperty("line.separator"));
		sb.append(listMissingEntries());
		return sb.toString();
	}
	
}
