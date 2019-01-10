package org.sadun.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import com.deltax.util.DynamicClassFileFinder;
import com.deltax.util.DynamicJDK12ClassFileFinder;
import com.deltax.util.DynamicResourceFileFinder;

/**
 * A classloader whose classpath can be set dynamically.
 * <p>
 * Version 1.2 adds resource support capability, but for the
 * {@link #findResources(java.lang.String) findResources()} which 
 * is not implemented.
 * <p>
 * By default, the classLoader attempts to use the parent class loader,
 * if any, to find a class. By setting the {@link #setForceDynamicLoading(boolean) 
 * forceDynamicLoading} property, the classLoader will always find
 * classes by using the embedded {@link com.deltax.util.DynamicClassFileFinder
 * DynamicClassFileFinder}.
 * 
 * $Revision$
 * 
 * @version 1.3
 * @author cris
 */
public class DynamicClassLoader extends ClassLoader {
	
	private DynamicClassFileFinder cff;
	private DynamicResourceFileFinder rff;
	private boolean forceDynamicLoading;
	
	private final static boolean debug = false;

	/**
	 * Constructor for DynamicClassLoader.
	 * @param arg0
	 */
	public DynamicClassLoader(ClassLoader arg0, DynamicClassFileFinder cff, DynamicResourceFileFinder rff) {
		super(arg0);
		this.cff=cff;
		this.rff=rff;
	}

	/**
	 * Constructor for DynamicClassLoader.
	 */
	public DynamicClassLoader(DynamicClassFileFinder cff, DynamicResourceFileFinder rff) {
		super();
		this.cff=cff;
		this.rff=rff;
	}
	
	private static DynamicJDK12ClassFileFinder dynamicJDK12ClassFileFinder = new DynamicJDK12ClassFileFinder();
	
	public DynamicClassFileFinder getClassFileFinder() { return cff; }
	public DynamicResourceFileFinder getResourceFileFinder() { return rff; }
		
	
	public DynamicClassLoader(ClassLoader arg0) {
		this(arg0, dynamicJDK12ClassFileFinder, dynamicJDK12ClassFileFinder);
	}
	
	/**
	 * Add a class path entry.	 * @param entry	 */
	public void addClassPathEntry(String entry) {
		if (debug) System.out.println("DCL: added classpath entry "+entry);
		synchronized(this) {
			cff.addClassPathEntry(entry);
			rff.addClassPathEntry(entry);
		}
	}
	
	/**
	 * Set the class path.	 * @param classPath	 */
	public void setClassPath(String classPath) {
		if (debug) System.out.println("DCL: set classpath to "+classPath);
		synchronized(this) {
			cff.setClassPath(classPath);
			rff.setClassPath(classPath);
		}
	}
	
	/**
	 * Returnt the current class path
	 * @return the current class path
	 */
	public String getClassPath() {
	    return cff.getClassPath();
	}

	/**
	 * Constructor for DynamicClassLoader.
	 */
	public DynamicClassLoader() {
		this(dynamicJDK12ClassFileFinder, dynamicJDK12ClassFileFinder);
	}


	public static void main(String[] args) {
		DynamicClassLoader dcl = new DynamicClassLoader();
		dynamicJDK12ClassFileFinder.addClassPathEntry("C:/mc4j/lib/core.jar");
		InputStream is = dcl.getResourceAsStream("org/netbeans/core/resources/action.gif");
		System.out.println(is);
	}
	
	/**
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	protected Class findClass(String arg0) throws ClassNotFoundException {
		if (debug) 
			System.out.println("DCL: finding class "+arg0);
		try {
			if (forceDynamicLoading) throw new ClassNotFoundException();
			return super.findClass(arg0);
		} catch(ClassNotFoundException e) {
			try {
				//System.out.println("Searching for class "+ arg0 + "; classpath is "+cff.getClassPath());
				byte [] data = cff.getClassBytes(arg0);
				return this.defineClass(arg0, data,0, data.length);
			} catch(IOException e2) {
				e2.printStackTrace();
				throw new ClassNotFoundException("IOException while reading definition for class "+arg0, e2);
			}
		} 
	}
	
	

	/**
	 * @see java.lang.ClassLoader#findResource(java.lang.String)
	 */
	protected URL findResource(String name) {
		if (debug) 
					System.out.println("DCL: finding resource "+name);
		try {
			File f = rff.findResourceFile(name);
			if (f==null) return null;
			URL fileURL = new URL("file",
								null,
								rff.findResourceFile(name).getCanonicalPath()
								);
			if (f.equals(new File(name))) { // It's the resource itself
				if (debug) 
						System.out.println("DCL: found resource "+name+" in "+fileURL);
				return fileURL;
			} else {
				String jarFileURL="jar:"+fileURL+"!/"+name;
				if (debug) 
					System.out.println("DCL: found resource "+name+" in "+jarFileURL);
				return new URL(jarFileURL);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @see java.lang.ClassLoader#findResources(java.lang.String)
	 */
	protected Enumeration findResources(String name) throws IOException {
		throw new RuntimeException("findResources is not implemented by this ClassLoader");
	}

	/**
	 * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsStream(String name) {
		InputStream is;
		if ((is=super.getResourceAsStream(name))!=null) return is;
		try {
			return rff.openResource(name);
		} catch (IOException e) {
			return null;
		}
	}

	/*
	 * @see java.lang.ClassLoader#getResource(java.lang.String)
	 *
	public URL getResource(String name) {
		URL url=null;
		if (getParent() != null) {
		    url = getParent().getResource(name);
		} else {
		    url = findResource(name);
		}
		return url;
	}
	*/


	/**
	 * Returns the forceDynamicLoading.
	 * @return boolean
	 */
	public boolean isForceDynamicLoading() {
		return forceDynamicLoading;
	}

	/**
	 * Sets the forceDynamicLoading.
	 * @param forceDynamicLoading The forceDynamicLoading to set
	 */
	public void setForceDynamicLoading(boolean forceDynamicLoading) {
		this.forceDynamicLoading = forceDynamicLoading;
	}

	/**
	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
	 */
	protected synchronized Class loadClass(String name, boolean resolve)
		throws ClassNotFoundException {
		if (forceDynamicLoading  && ! name.startsWith("java.")) {
			Class c;
			if ((c=super.findLoadedClass(name))!=null) {
				//System.out.println(name+" already loaded");
				 return c;
			}
			//System.out.println(name+" not loaded yet");
			Class cls = findClass(name);
			if (cls!=null && resolve) resolveClass(cls);
			return cls;
		} else return super.loadClass(name, resolve);
	}
	
	/** 
	 * Looks for a library in classpath and load it if found.
	 * @see java.lang.ClassLoader#findLibrary(java.lang.String)
	 * @author GePs
	 */
	protected String findLibrary(String libname) {
	    String cp = rff.getClassPath();
	    String[] paths = cp.split(";");
	    for (int i = 0; i < paths.length; i++) {
	        if (!paths[i].endsWith(".jar") && !paths[i].endsWith("zip")) {
	            java.io.File dir = new File(paths[i]);
	            if (dir.isDirectory()) {
	                java.io.File lib = new java.io.File(dir, libname);
	                if (lib.exists()) 
	                    return lib.getAbsolutePath();
	                lib = new File(dir, libname + ".dll");
	                if (lib.exists())
	                    return lib.getAbsolutePath();
	                lib = new File(dir, libname + ".so");
	                if (lib.exists())
	                    return lib.getAbsolutePath();
	            }
	        }
	    }
	    return null;
	}
}
