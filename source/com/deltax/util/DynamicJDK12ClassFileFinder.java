package com.deltax.util;

/**
 * A {@link JDK12ClassFileFinder JDK12ClassFileFinder} whose classpath definition
 * can be changed at runtime.
 *
 * @author cris
 * @version 1.0.1
 */
public class DynamicJDK12ClassFileFinder extends JDK12ClassFileFinder implements DynamicClassFileFinder, DynamicResourceFileFinder {

    private static String pathSep = null;

	/**
	 * Constructor for DynamicJDK12ClassFileFinder.
	 * @param classPath
	 */
	public DynamicJDK12ClassFileFinder(String classPath) {
		super(classPath);
		if (pathSep == null) {
			pathSep = System.getProperty("path.separator");
		}
	}

	/**
	 * Constructor for DynamicJDK12ClassFileFinder.
	 */
	public DynamicJDK12ClassFileFinder() {
		super();
		if (pathSep == null) {
		    pathSep = System.getProperty("path.separator");
		}
	}

	public void addClassPathEntry(String entry) {
	    if (classPath==null) classPath=entry;
		else classPath+=pathSep+entry;
		this.classPathDirs=null; // Invalidate
	}

	public void setClassPath(String classPath) {
		this.classPath=classPath;
		this.classPathDirs=null; // Invalidate
	}

	/**
	 * @see com.deltax.util.DynamicClassFileFinder#getClassPath()
	 */
	public String getClassPath() {
		return classPath;
	}

}
