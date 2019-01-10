package com.deltax.util;

/**
 * A {@link ResourceFileFinder ResourceFileFinder} which allows to dynamically
 * redefine the class path.
 * <p>
 * The redefinition may or may not affect reloading of already linked classes.
 * 
 * @author cris
 */
public interface DynamicResourceFileFinder extends ResourceFileFinder {
	
	public void addClassPathEntry(String entry);
	
	public void setClassPath(String classPath);
	
	public String getClassPath();

}
