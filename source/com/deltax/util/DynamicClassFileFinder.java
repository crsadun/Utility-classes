package com.deltax.util;

/**
 * A {@link ClassFileFinder ClassFileFinder} which allows to dynamically
 * redefine the class path.
 * <p>
 * The redefinition may or may not affect reloading of already linked classes.
 * 
 * @author cris
 */
public interface DynamicClassFileFinder extends ClassFileFinder {
	
	public void addClassPathEntry(String entry);
	
	public void setClassPath(String classPath);
	
	public String getClassPath();

}
