package org.sadun.util;

import java.io.File;

/**
 * Classes implementing this interface can explore the classpath, retrieving the contents of Java
 * packages and enumerate the classes therein.
 * <p>
 * Multiple jars or directories containing different classes in the same package are supported.
 * <p>
 * However, different jars and directories containing the same class are not.
 * 
 * @author Cristiano Sadun
 */
public interface ClassPackageExplorer {
	
	/**
	 * Status constant mask, indicating that a certain package is found in a directory. 
	 * @see #getStatus(java.lang.String)
	 */
	public static final int IN_DIRECTORY = 0;
	
	/**
	 * Status constant mask, indicating that a certain package is found in a jar file.
	 * @see #getStatus(java.lang.String)
	 */	
	public static final int IN_JAR = 1;
	
	/**
	 * Status constant mask, indicating that a certain package is found in a jar file and is sealed.
	 * @see #getStatus(java.lang.String)
	 */	
	public static final int IN_JAR_SEALED = 2;
	
	
	/**
	 * Status constant mask, indicating that a certain package is found in a jar file nested in another jar file 
	 * @see #getStatus(java.lang.String)
	 */
	public static final int IN_NESTED_JAR = 3;

	/**
	 * List the available packages. The classpath is scanned for classes, and each package is reported.
	 * This can take a long time on first invocation.
	 * 
	 * @return a list of package names
	 */
	public String [] listPackageNames();

	/**
	 * List the available packages. The classpath is scanned for classes, and each package is reported.
	 * <p>
	 * This can take a long time on first invocation or if <tt>rescan</tt> is <b>true</b>.
	 * 
	 * @param rescan forces a re-scanning
	 * @return a list of package names
	 */	
	public String [] listPackageNames(boolean rescan);
	
	/**
	 * Return the names of the classes in the package.
	 * 
	 * @param packageName the name of the package
	 * @param a status bit mask (see status constant masks) indicating which files to list.
	 * @return the names of the classes in the package.
	 */
	public String [] listPackage(String packageName, int status);

	/**
	 * Return the names of all the classes in the package.
	 * This can take a long time on first invocation.
	 * <p>
	 * @param packageName the name of the package
	 * @return the names of all the classes in the package.
	 */	
	public String [] listPackage(String packageName);
	
	/**
	 * Return information on whether the package lives in a directory, a jar file, a sealed jar
	 * file or a combination.
	 * <p>
	 * This can take a long time on first invocation.
	 * 
	 * @param packageName the name of the package
	 * @return the distribution status for the given package, or -1 if the given package
	 *          does not exist.
	 */
	public int getStatus(String packageName);
	
	/**
	 * Return the one or more files or directories where a package lives.
	 * <p>
	 * This can take a long time on first invocation.
	 * 
	 * @param packageName the name of the package
	 * @return the files containing classes belonging to the given package.
	 */
	public File[] getPackageFiles(String packageName);
	
	/**
	 * Return an error status for the explorer.
	 * 
	 * @return <b>true</b> if an error has occurred while exploring the class path.
	 */
	public boolean hasErrorOccurred();
	
	/**
	 * Return an error status for the explorer.
	 */
	public String getErrorLog();

}
