package org.sadun.util;

import java.io.File;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * An iterator over a classpath declaration.
 * 
 * @author Cristiano Sadun
 */
public class ClassPathIterator implements Iterator {

	private StringTokenizer st;
	private String currentEntry;
	private File currentFile;

	/**
	 * Create a ClassPathIterator iterating on the given class path.
	 * 
	 * @param the classpath string to iterate on
	 */
	public ClassPathIterator(String classpath) {
		st = new StringTokenizer(classpath, File.pathSeparator);
	}
	
	/**
	 * Create a ClassPathIterator iterating on the system class path.
	 */
	public ClassPathIterator() {
		this(System.getProperty("java.class.path"));
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return st.hasMoreTokens();
	}

	/**
	 * Return the next entry as a String
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		synchronized (this) {
			currentEntry = st.nextToken();
			currentFile = new File(currentEntry);
			return currentEntry;
		}
	}

	/**
	 * Return the next entry in the class path as a String
	 */
	public String nextEntryString() {
		return (String) next();
	}

	/**
	 * Return the next entry in the class path as a File
	 */
	public File nextEntryFile() {
		next();
		return currentFile;
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns whether the entry where the iterator is positioned is a JAR or not
	 * 
	 * @param checkExistence also checks if the JAR exists.
	 */
	public boolean isJar(boolean checkExistence) {
		if (currentEntry == null)
			throw new IllegalStateException("Please invoke next() first");
		return currentEntry.endsWith(".jar")
			&& ((!checkExistence) || (checkExistence && currentFile.exists()));
	}

	/**
	 * Returns whether the entry where the iterator is positioned is a JAR or not, regardless
	 * of its existence.
	 */
	public boolean isJar() {
		return isJar(false);
	}


	/**
	 * Returns whether the entry where the iterator is positioned is a JAR or not
	 * 
	 * @param checkExistence also checks if the JAR exists.
	 */
	public boolean isDirectory(boolean checkExistence) {
		if (currentEntry == null)
			throw new IllegalStateException("Please invoke next() first");
		return currentFile.isDirectory()
			&& ((!checkExistence) || (checkExistence && currentFile.exists()));
	}

	/**
	 * Returns whether the entry where the iterator is positioned is a JAR or not, regardless
	 * of its existence.
	 */
	public boolean isDirectory() {
		return isDirectory(false);
	}

}
