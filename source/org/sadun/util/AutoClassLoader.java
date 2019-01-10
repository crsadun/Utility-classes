/*
 * AutoClassLoader.java
 *
 * Created on 15. august 2001, 16:39
 */

package org.sadun.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A ClassLoader implementation which looks up the JARs in classpath and loads them
 * individually, without the need of specifying one by one. The JARs are loaded
 * in the same order they appear in the directory listing.
 *
 * @author  Cristiano Sadun
 * @version 1.0.1
 */
public class AutoClassLoader extends java.lang.ClassLoader {

    /**
     * The classpath resulting by looking up directories for JARs.
     * Set up by expandClasspath()
     */
    private static String cpath = null;
    
    /**
     * The path separator. Set up at construction.     */
    private static String pathSep = null;
    

    /** Creates new AutoClassLoader */
    public AutoClassLoader() {
        super();
        if (pathSep == null) {
            pathSep = System.getProperty("path.separator");
        }
    }

    /** Creates new AutoClassLoader */
    public AutoClassLoader(ClassLoader parent) {
        super(parent);
        if (pathSep == null) {
            pathSep = System.getProperty("path.separator");
        }
    }

    private String expandClassPath() {
        String cp = System.getProperty("java.class.path");
        StringTokenizer st = new StringTokenizer(cp,pathSep);
        StringBuffer sb = new StringBuffer();
        boolean finished=! st.hasMoreTokens();
		synchronized(sb) {
        while(! finished) {
            String cpElem = st.nextToken();
            sb.append(cp+pathSep);
            File f = new File(cpElem);
            if (f.isDirectory() && f.exists())
                sb.append(getJarsInDirectory(f, pathSep));
            else sb.append(cpElem);
            if (st.hasMoreTokens()) sb.append(pathSep);
            else finished=true;
        }
		}
        return sb.toString();
    }

    private String getJarsInDirectory(File f, String separator) {
     String [] files = f.list(new FilenameFilter() {
         public boolean accept(File dir, String name) {
             return name.endsWith(".jar");
         }
     });
     StringBuffer sb=new StringBuffer();
	 synchronized(sb) {
     for(int i=0;i<files.length;i++) {
         sb.append(f.getAbsolutePath()+File.separator+files[i]);
         if (i<files.length) sb.append(separator);
     }
     return sb.toString();
	 }
    }

    /**
     * Finds a class in the expanded class path
     * @exception ClassNotFoundException if the class cannot be found
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        if (cpath==null) cpath=expandClassPath();

        // Look in the expanded classpath, using the standard classloader
        StringTokenizer st = new StringTokenizer(cpath,pathSep);
        while(st.hasMoreTokens()) {
        	String entry = st.nextToken();
        	if (!entry.endsWith(".jar")) continue;

        	Class cls;
        	if ((cls=lookInJar(name, entry))!=null) return cls;
        }
        throw new ClassNotFoundException(name);
    }

    private Class lookInJar(String clsName, String jarFileName) throws ClassNotFoundException {
    		try {
	    		JarFile jarFile = new JarFile(jarFileName);
	    		String entryName=clsName.replace('.','/')+".class";
	    		JarEntry entry = jarFile.getJarEntry(entryName);
	    		if(entry==null) return null;

	    		System.out.println(clsName+" found in "+jarFileName);

	    		BufferedInputStream is = new BufferedInputStream(jarFile.getInputStream(entry));
	    		ByteArrayOutputStream os = new ByteArrayOutputStream();
	    		for(long i=0;i<entry.getSize();i++)
	    		   os.write(is.read());

            byte [] b = os.toByteArray();

	    		return defineClass(clsName, b, 0, b.length);
	    	} catch(IOException e) {
	    		throw new ClassNotFoundException("Can't load "+clsName+" from "+jarFileName, e);
	    	}
    }

   public static void main(String args[]) throws Exception {

      System.setProperty("java.class.path","c:\\xtractor\\dev\\core\\lib;c:\\xtractor\\dev\\core\\lib\\3rd");
      AutoClassLoader cl = new AutoClassLoader(ClassLoader.getSystemClassLoader());
      Class cls = cl.loadClass("com.metamata.util.ZipPath");
   }


}
