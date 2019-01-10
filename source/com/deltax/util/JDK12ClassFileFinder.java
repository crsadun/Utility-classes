/************************************************************************************
* Copyright (C) 1999-2002 Cristiano Sadun cristiano@xtractor.com
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
************************************************************************************/


package com.deltax.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class emulates JDK 1.2 behaviour for finding class,
 * providing direct access to the files / streams.
 * <p>
 * Of course, it works only when classes are actually loaded
 * from a filesystem.
 *
 * @version 1.4
 * @author Cristiano Sadun
 */
public class JDK12ClassFileFinder implements ClassFileFinder, ResourceFileFinder {

   /**
    * The application class path used by this classFileFinder
    */
   protected String classPath;
   
   /**
    * Caches results of {@link #getClassPathDirs() getClassPathDirs()}
    * If the classpath changes, it must be invalidated.    */
   protected Enumeration classPathDirs;
   
   protected HashSet missingFilesNotified = new HashSet();   	
   
   /**
    * Maps class names to Object[2] arrays containing file and classfile (File, byte[])    */
   protected HashMap classCache = new HashMap();
   protected boolean classCacheOn=false;
   
   /**
    * Jar files which couldn't be opened { HashSet(File) }    */
   private Set reportedProblematicJars = new HashSet();

   /**
    * Creates a classfinder which looks (besides system directories)
    * in the given class path
    * @param classPath the search class path
    */
   public JDK12ClassFileFinder(String classPath) {
      this.classPath = classPath;
   }

   /**
    * Creates a classfinder which looks (besides system directories)
    * in $java.class.path
    */
   public JDK12ClassFileFinder() {
      this(System.getProperties().getProperty("java.class.path"));
   }

   /**
    * Returns the supported loading scheme
    */
   public String getSupportedLoadingScheme() { return "1.2 (2)"; }


  /**
   * Open class data. The input stream reads exactly and only the
   * class byte data.
   *
   * @param className the name of the class to find
   * @exception ClassNotFoundException if the class is not found
   * @exception IOException if an I/O Exception occurs
   */
   public InputStream openClass(String className)
      throws IOException, ClassNotFoundException {
      File f = findClassFile(className);
      if (! isJar(f)) return new FileInputStream(f);
      else return openClassInJar(className,f);
   }

   /**
	 * This method emulates 1.2 behaviour for class finding - which means:
	 *    - it first searches into the $java.home/lib/ and $java.home/lib/*.jar;
	 *    - then in $java.ext.dirs/*.jar;
	 *    - eventually in $java.class.path
	 * @param className the name of the class to find
	 * @return the File object for the class file; this can be a .class
	           file, or a JAR file containing the class
	 * @exception ClassNotFoundException if the class is not found
	 * @exception IOException if an I/O Exception occurs
	 */
   public File findClassFile(String className)
         throws IOException, ClassNotFoundException {
      // Java home
      try {
         return searchInDirectory(
            new File(System.getProperties().getProperty("java.home")+File.separator+"lib"),
                     className, false
         );
      } catch (ClassNotFoundException e) { }

      // JARs in extension library
      try {
         return searchInDirectory(
            new File(System.getProperties().getProperty("java.ext.dirs")),
                     className, true
         );
      } catch (ClassNotFoundException e) { }

      // classes and jars in class path
      Enumeration en = getClassPathDirs();
      
      while (en.hasMoreElements())
         try {
            String dir = (String)en.nextElement();
            File dirFile = new File(dir);
            if (! (dir.endsWith(".jar") || dir.endsWith(".zip"))) {
               File f = getCandidateClassFile(dirFile, className);
               if (f.exists()) return f;
            } else  { // Search directly in JAR/ZIP
               File [] f2  = new File[1];
               f2[0] = dirFile;
               return searchJars(className, f2);
            }
         } catch (ClassNotFoundException e) { }

      // Not found
      throw new ClassNotFoundException(className);
   }

   /**
    * Return the byte array for the class
    *
    *
    * @exception ClassNotFoundException if the class is not found in the JAR file
    * @exception IOException if an I/O Exception occurs
    */
   public byte [] getBytes(String className)
      throws IOException, ClassNotFoundException {

         InputStream is = openClass(className);
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         int c;
         while ((c=is.read())!=-1) bos.write(c);
         is.close();
         return bos.toByteArray();
   }

   /**
    * Just checks if the file ends with .jar
    * @param f the file to check
    * @return true if the file name ends with .jar
    */
   public boolean isJar(File f) {
      return f.getName().endsWith(".jar");
   }

   /**
    * Open class data in a JAR file
    *
    * @return an input stream to the class byte data in JAR
    * @exception ClassNotFoundException if the class is not found in the JAR file
    * @exception IOException if an I/O Exception occurs
    * @exception FileNotFoundException if the JAR file does not exist
    */
   protected InputStream openClassInJar(String className, File jarFile)
         throws IOException, ClassNotFoundException {
      
      // Use a cache
      if (classCacheOn) {
	      Object []obj;
	      if ((obj=(Object [])classCache.get(className))!=null) {
	      	if (((File)obj[0]).equals(jarFile)) return new ByteArrayInputStream((byte[])obj[1]);
	      }
      }
      
      //System.out.println("Class "+className+": "+cc.intValue()+" times");

         	
      if (! jarFile.exists()) throw new FileNotFoundException(jarFile.getCanonicalPath().toString());
      JarFile jf = new JarFile(jarFile);
      String classFile = classNameToEntry(className);
      JarEntry entry = jf.getJarEntry(classFile);

      if (entry==null) throw new ClassNotFoundException(className);
      if (entry.getSize()==-1)
         throw new RuntimeException("Unknown entry size in JAR file "+jarFile);
      if ((long) ( (int)entry.getSize() ) != entry.getSize())
         throw new RuntimeException("Entry "+classFile+" too big in "+jarFile);

      InputStream jis = new BufferedInputStream(jf.getInputStream(entry));

      byte [] b = new byte [(int)entry.getSize()];
      for (int i=0;i<entry.getSize();i++)
         b[i]=(byte)jis.read();
      jis.close();
      
      if (classCacheOn) 
      	classCache.put(className, new Object[] { jarFile, b });

      return new ByteArrayInputStream(b);
   }

   /**
    * Searches jar files for a given class path
    *
    * @param className the name of the class to find
    * @param jars an array of JAR file objects
    * @return the jar file containing the class
	 * @exception ClassNotFoundException if the class is not found
	 * @exception IOException if an I/O Exception occurs
    */
   protected File searchJars(String className, File [] jars)
         throws IOException, ClassNotFoundException {
      for (int i=0; i<jars.length;i++)
         try {
         	if (reportedProblematicJars.contains(jars[i])) continue;
            openClassInJar(className, jars[i]).close();
            return jars[i];
         } catch (ClassNotFoundException e) { 
         } catch (FileNotFoundException e) {
         	if (! missingFilesNotified.contains(e.getMessage())) {
	         	System.err.println(e.getMessage()+" is in class path but cannot be located while searching jars");
	         	missingFilesNotified.add(e.getMessage());
         	}
         } catch(IOException e) {
         	System.err.println("Warning: problem with "+jars[i]+" while searching for class "+className+" ("+e.getClass().getName()+": "+e.getMessage()+
         	"). The file will be ignored.");
         	reportedProblematicJars.add(jars[i]);
         }
      throw new ClassNotFoundException(className);
   }



   /**
    * Find the JAR files in the given path
    * @param path the path to search in
    * @return an array (may be zero-length) with the JAR file names
    */
   protected File [] findJarsInPath(File path) {
      if (! path.isDirectory()) return new File[0];
      return path.listFiles( new FilenameFilter() {
         public boolean accept(File dir, String name) {
            return  name.endsWith(".jar");
         }
      });
   }

   /**
    * Converts a fully qualified externalized java class name into
    * a relative file path.
    * @param className the name of the class to find
    * @return the system-dependent path name for the class
    */
   protected String classNameToPath(String className) {
      if (className.endsWith(".class")) return className;
      return className.replace('.', File.separatorChar)+".class";
   }
   
   private HashMap classNameEntryCache = new HashMap();

   /**
    * Converts a fully qualified externalized java class name into
    * a zip file entry. (ZIP format uses always UNIX slash)
    * @param className the name of the class to find
    * @return the JAR entry name for the class
    */
   protected String classNameToEntry(String className) {
   	  String s=(String)classNameEntryCache.get(className);
   	  if (s==null) {
	      if (className.endsWith(".class")) return className;
	      s=className.replace('.', '/')+".class";
	      classNameEntryCache.put(className, s);
   	  }
      return s;
   }

   private File getCandidateClassFile(File dir, String className) {
      return new File(dir.getAbsolutePath()+File.separator+classNameToPath(className));
   }

   private File searchInDirectory(File dir, String className, boolean jarOnly)
         throws IOException, ClassNotFoundException {
      if (!jarOnly) {
         File f = getCandidateClassFile(dir,className);
         if (f.exists()) return f;
      }
      return searchJars(className, findJarsInPath(dir));
   }
   
   protected synchronized Enumeration getClassPathDirs() {
   		if (classPathDirs==null) {
	      StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
	      String[] dirs=new String[st.countTokens()];
		  int i=0;
	      while (st.hasMoreTokens()) dirs[i++]=st.nextToken();
	      classPathDirs=new ArrayEnumeration(dirs);
   		} else ((ArrayEnumeration)classPathDirs).reset();
		return classPathDirs;
   }

	/**
	 * @see com.deltax.util.ClassFileFinder#getClassBytes(java.lang.String)
	 */
	public byte[] getClassBytes(String className)
		throws IOException, ClassNotFoundException {
		if (classCacheOn) {
			Object [] obj = (Object [])classCache.get(className);
			if (obj != null)
				return (byte[])obj[1];
		}
		BufferedInputStream is = new BufferedInputStream(openClass(className));
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int c;
		while((c=is.read())!=-1) os.write(c);
		is.close();
		byte [] data = os.toByteArray();
		return data;
	}
	
	/**
	 * Return the stream associated to a resource in the filesystem, or <b>null</b>
	 * if such resource cannot be found.
	 * <p>
	 * The returned stream is not buffered.
	 * 
	 * @param resource the resource path
	 * @return a stream to the resource, or <b>null</b> if the resource cannot be found
	 * @exception IOException if a problem arises accessing the resource
	 * @see com.deltax.util.ResourceFileFinder#findResource(java.lang.String)
	 */
	public InputStream openResource(String resource) throws IOException {
		ResourceSearchResult result = findResourceFile0(resource);
		if (result!=null) return result.stream;
		return null;
	}

	/**
	 * Find a resource in the filesystem.
	 * <p>
	 * This class implements findResource by operating as {@link #findClassFile(java.lang.String) 
	 * findClassFile()} and attempting to locate the resource within directories or JAR 
	 * files therein.
	 * <p>
	 * Resource loading from other than filesystem is not supported.
	 * 
	 * @param resource the resource path
	 * @return a stream to the resource, or <b>null</b> if the resource cannot be found
	 * @exception IOException if a problem arises accessing the resource
	 * @see com.deltax.util.ResourceFileFinder#findResource(java.lang.String)
	 */
	public File findResourceFile(String resource) throws IOException {
		ResourceSearchResult r = findResourceFile0(resource);
		if (r!=null) return r.f;
		return null;
	}
	
	/**
	 * Return a byte array with the bytes for the resource, or <b>null</b> if the 
	 * resource cannot be found.
	 * 	 * @see com.deltax.util.ResourceFileFinder#getResourceBytes(String)	 */
	public byte [] getResourceBytes(String resource) throws IOException {
		InputStream is = openResource(resource);
		if (is==null) return null;
		if (is instanceof FileInputStream) is = new BufferedInputStream(is);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int c;
		while((c=is.read())!=-1) os.write(c);
		return os.toByteArray();
	}
		
		
	private ResourceSearchResult findResourceFile0(String resource) throws IOException {
		
		// No classes allowed
		if (resource.endsWith(".class")) return null;
		
		// Java home
	 	ResourceSearchResult result=null;
	    if ((result=searchResourceInDirectory(
    	    new File(System.getProperties().getProperty("java.home")+File.separator+"lib"),
        	         resource, false
	    ))!=null) return result;


        // JARs in extension library
		if ((result=searchResourceInDirectory(
		   new File(System.getProperties().getProperty("java.ext.dirs")),
		            resource, true
		))!=null) return result;


      // classes and jars in class path
      Enumeration en = getClassPathDirs();
      
      while (en.hasMoreElements()) {
        String dir = (String)en.nextElement();
        
		//System.out.println("JDK12CFF: looking for resource "+resource+" in "+dir);
        
        File dirFile = new File(dir);
        if (! (dir.endsWith(".jar") || dir.endsWith(".zip"))) {
           File f = new File(dirFile, resource);
           if (f.exists()) return new ResourceSearchResult(f);
        } else  { // Search directly in JAR/ZIP
           if (! dirFile.exists()) continue; // SKip missing classpath entries
           //System.out.println("Searching resource "+resource+" in "+dirFile);
           if ((result=searchResourceInJars(resource, new File[] { dirFile } )) != null) return result;
        }
      }
      
      // Not found
      return null;
	}
	
	private ResourceSearchResult searchResourceInDirectory(File dir, String resource, boolean jarOnly) throws IOException {
   	  if (!jarOnly) {
         File f = new File(resource);
         if (f.exists()) return new ResourceSearchResult(f);
      }
      return searchResourceInJars(resource, findJarsInPath(dir));
	}
	
	private class ResourceSearchResult {
		public File f;
		public InputStream stream;
		
		ResourceSearchResult() {
		}
		
		ResourceSearchResult(File f) throws IOException {
			this.f=f;
			this.stream=new FileInputStream(f);
		}

	}
	
	private ResourceSearchResult searchResourceInJars(String resource, File []jars) throws IOException {
		ResourceSearchResult result=new ResourceSearchResult();
		for(int i=0;i<jars.length;i++) {
			if (reportedProblematicJars.contains(jars[i])) continue;
			try {
				result.f=jars[i];
				if ((result.stream=openResourceInJar(resource, jars[i]))!=null) return result;
			} catch(IOException e) {
	         	System.err.println("Warning: problem with "+jars[i]+" while searching for resource "+resource+" ("+e.getClass().getName()+": "+e.getMessage()+
	         	"). The file will be ignored.");
	         	reportedProblematicJars.add(jars[i]);
	         }
		}
		// Not found
		return null;	
	}
	
	private InputStream openResourceInJar(String resource, File jarFile) throws IOException {
 		  if (! jarFile.exists()) throw new FileNotFoundException(jarFile.toString());
	      JarFile jf = new JarFile(jarFile);
	      
	      JarEntry entry=null;
	      
	      entry = jf.getJarEntry(resource);
	
	      if (entry==null) return null;
	      if (entry.getSize()==-1)
	         throw new RuntimeException("Unknown entry size in JAR file "+jarFile);
	      if ((long) ( (int)entry.getSize() ) != entry.getSize())
	         throw new RuntimeException("Entry "+resource+" too big in "+jarFile);
	
	      InputStream jis = jf.getInputStream(entry);
	
	      byte [] b = new byte [(int)entry.getSize()];
	      for (int i=0;i<entry.getSize();i++)
	         b[i]=(byte)jis.read();
	      jis.close();
	
	      return new ByteArrayInputStream(b);
	}
	
   public static void main(String args[]) throws Exception {
      if (args.length<1) return;

      DynamicResourceFileFinder rf = new DynamicJDK12ClassFileFinder();
      rf.addClassPathEntry("c:\\j2sdk1.4.1_01\\jre\\lib\\endorsed\\xercesImpl.jar");

	  InputStream is = rf.openResource("org.apache.xerces.impl.msg.XMLMessages.properties");
	  //InputStream is = rf.openResource(args[0]);
	  if (is!=null)
	      System.out.println("Resource found"); 
	  else
	      System.out.println("Resource not found"); 
   }	
   
	/**
	 * Returns the classCacheOn.
	 * @return boolean
	 */
	public boolean isClassCacheOn() {
		return classCacheOn;
	}
	
	/**
	 * Sets the classCacheOn. The class cache speeds up searching in jars
	 * by preserving classfile-classname-jar associations, but may lead
	 * to substantial growth of the amount of memory needed.
	 * 
	 * @param classCacheOn The classCacheOn to set
	 */
	public void setClassCacheOn(boolean classCacheOn) {
		this.classCacheOn = classCacheOn;
		if (classCacheOn==false) classCache=new HashMap();
	}
	
	/**
	 * Return the set, possibly empty, of files which couldn't be opened
	 * when the finder was searching for classes.	 * @return the set, possibly empty, of files which couldn't be opened.	 */
	public Set getCorruptFiles() {
		return reportedProblematicJars;
	}
	
	/**
	 * Empty the set of files considered corrupted since they failed to open
	 * when the finder was searching for classes, so that subsequent work will
	 * not ignore them.
	 */
	public void clearCorruptFiles() {
		reportedProblematicJars=new HashSet();
	}

}