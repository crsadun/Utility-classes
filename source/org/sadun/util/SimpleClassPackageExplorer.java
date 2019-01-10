package org.sadun.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;

import org.sadun.util.sis.StateInfoSupport;

import com.deltax.util.CPoolReader;

/**
 * A package explorer implementation.
 * <p>
 * Version 2.0 optionally supports nested jars. If nested jar exploration is activated when
 * constructing an instance, any jar file contained within an explored jar file will be examined and 
 * an attempt will be made to list any .class file therein.
 * <p>
 * For example, if a jar contains
 * <pre>
 *  /A.class
 *  /lib/myjar.jar</pre>
 * and <tt>myjar.jar</tt> in turn contains
 * <pre>
 *  /B.class</pre>
 * the explorer will list <i>both</i> classes <tt>A</tt> and <tt>B</tt> if nested jar exploration is 
 * activated, but only <tt>A</tt> otherwise. 
 *   
 * <p>
 * Note that in general classes contained into nesteded jars will 
 * <i>not</i> be readily available to a Java virtual machine; rather, a separate mechanism (for example, see
 * <a target="_blank" href="http://one-jar.sourceforge.net/">http://one-jar.sourceforge.net/</a>) will be needed to load such 
 * classes.
 * <p>
 * WARNING: Sealed Jars aren't supported yet.
 * 
 * @version 2.0
 * @author Cristiano Sadun
 */
public class SimpleClassPackageExplorer implements ClassPackageExplorer {
	
	/**
	 * Classes implementing this interface receive notifications of the explorer's activity.
	 * 
	 * @author Cristiano Sadun
	 * @since v2.0
	 */
	public interface PackageExplorationListener {
		/**
		 * Notify that a jar/directory is being scanned.  
		 * 
		 * @param entry the jar/directory path
		 * @param isJar <b>true</b> if the entry is a jar file, <b>false</b> if it is a directory
		 */
		public void scanStart(File entry, boolean isJar);
		
		/**
		 * Notify that a jar/directory scanning is terminated.  
		 * 
		 * @param entry the jar/directory path
		 * @param isJar <b>true</b> if the entry is a jar file, <b>false</b> if it is a directory
		 */
		public void scanEnd(File entry, boolean isJar);
		
		/**
		 * Notify that a specific file is being scanned.
		 * 
		 * @param classFile the file path (may be .class file or a jar)
		 * @param className the class name of the contained class
		 */
		public void processingFile(File classFile, String className);

        /**
         * @param entry
         * @param nestedJarName
         */
        public void nestedScanStart(File entry, String nestedJarName);
        
        /**
         * @param entry
         * @param nestedJarName
         */
        public void nestedScanEnd(File entry, String nestedJarName);

        /**
         * @param entry
         * @param nestedJarName
         * @param className
         */
        public void processingNestedFile(File entry, String nestedJarName, String className);

        public void processingClassFile(File entry, boolean isJar, String className, InputStream classdata);
	}
	
	/**
	 * An empty implementation of {@link PackageExplorationListener}.
	 * 
	 * @author Cristiano Sadun
	 */
	public static class PackageExplorationAdapter implements PackageExplorationListener {
		
		/**
		 * This method does nothing.
		 */
		public void processingFile(File classFile, String className) {
		}
		/**
		 * This method does nothing.
		 */
		public void scanEnd(File entry, boolean isJar) {
		}
		
		/**
		 * This method does nothing.
		 */
		public void scanStart(File entry, boolean isJar) {
		}
		
		/**
		 * This method does nothing.
		 */
        public void nestedScanStart(File entry, String nestedJarName) {
        }
        
        /**
		 * This method does nothing.
		 */
        public void nestedScanEnd(File entry, String nestedJarName) {
        }

        /**
		 * This method does nothing.
		 */
        public void processingNestedFile(File entry, String nestedJarName, String className) {
        }
        
        /**
         * This method does nothing.
         */
        public void processingClassFile(File entry, boolean isJar, String className, InputStream classdata) {
        }
	}
	
	private String classPath;
	private Pattern [] dirPatterns;
	private StateInfoSupport sis;
	private CPoolReader cpr;
	private boolean errorOccurred;
	private Set listeners = new HashSet();
	
	/*
	 * A map (package name -> List(File files containing classes in that package)).
	 */
	private Map filesPerPackage;
	
	/* 
	 * A map (package name -> Integer ).
	 */
	private Map packageStatus;
	
	/*
	 * A map (package name -> List(class names).
	 */
	private Map classesPerPackage;
	
	/*
	 * A map (class name -> File containing that class)
	 */
	private Map filesPerClass; // NOT IMPLEMENTED YET 

	private static FileFilter classFileFilter = new ExtensionFileFilter("class");
	private static FileFilter dirFileFilter = new DirectoryFileFilter();
    private boolean supportNestedJars=false;
	
	/**
	 * Create a SimpleClassPackageExplorer on the system class path.
	 */
	public SimpleClassPackageExplorer() {
	    this(false);
	}
	
	/**
	 * Create a SimpleClassPackageExplorer on the system class path, optionally
	 * supporting nested jars.
	 * 
	 * @param supportNestedJars if true, consider jars into jars as valid source for .class
	 */
	public SimpleClassPackageExplorer(boolean supportNestedJars) {
		this(System.getProperty("java.class.path"), supportNestedJars);
	}
	
	/**
	 * Create a SimpleClassPackageExplorer on the given class path.
	 * 
	 * @param the classpath string to iterate on
	 */	
	public SimpleClassPackageExplorer(String classPath) {
	    this(classPath, false);
	}
	   
	/**
	 * Create a SimpleClassPackageExplorer on the given class path, optionally
	 * supporting nested jars.
	 * 
	 * @param supportNestedJars if true, consider jars into jars as valid source for .class
	 * @param the classpath string to iterate on
	 */
	public SimpleClassPackageExplorer(String classPath, boolean supportNestedJars) {
		this(classPath, new String[] { ".*" }, supportNestedJars);
		this.cpr=new CPoolReader(classPath);
		
	}

	/**
	 * Create a SimpleClassPackageExplorer on the given class path.
	 * 
	 * @param the classpath string to iterate on
	 * @param classDirs an array of regular expression for the names of subdirectories to search for;
	 *         these patterns are not considered for JARs.
	 */	
	public SimpleClassPackageExplorer(String classPath, String[] classDirs) {
	    this(classPath, classDirs, false);
	}
	
	/**
	 * Create a SimpleClassPackageExplorer on the given class path, optionally
	 * supporting nested jars.
	 * 
	 * @param supportNestedJars if true, consider jars into jars as valid source for .class
	 * @param the classpath string to iterate on
	 * @param classDirs an array of regular expression for the names of subdirectories to search for;
	 *         these patterns are not considered for JARs.
	 */	
	public SimpleClassPackageExplorer(String classPath, String[] classDirs, boolean supportNestedJars) {
		this.classPath=classPath;
		this.supportNestedJars=supportNestedJars;
		dirPatterns=new Pattern[classDirs.length];
		for(int i=0;i<classDirs.length;i++) {
			dirPatterns[i]=Pattern.compile(classDirs[i]);
		}
	}

	/**
	 * @see org.sadun.util.ClassPackageExplorer#listPackage(String)
	 */
	public String[] listPackage(String packageName) {
		return listPackage(packageName, IN_DIRECTORY+IN_JAR+IN_JAR_SEALED);
	}

	/**
	 * @see org.sadun.util.ClassPackageExplorer#listPackageNames()
	 */
	public String[] listPackageNames() {
		return listPackageNames(false);
	}

	/**
	 * Method buildInfo.
	 */
	private void buildInfo() {
		sis = new StateInfoSupport();
		filesPerPackage = new HashMap();
		packageStatus = new HashMap();
		classesPerPackage = new HashMap();
		errorOccurred=false;
		
		for(ClassPathIterator i=new ClassPathIterator(classPath);i.hasNext();) {
			File entry=i.nextEntryFile();
			if (i.isJar())
				scanJarFile(entry);
			else 
				scanDirectory(entry);
		}
	}

	/**
	 * Method scanDirectory.
	 * @param entry
	 */
	private void scanDirectory(File entry) {
		// Is the entry matching one of the patterns?
		boolean matchedOne=false;
		for(int i=0;i<dirPatterns.length;i++) {
			if (dirPatterns[i].matcher(entry.getName()).matches()) {
				matchedOne=true;
				break;
			}
		}
		if (!matchedOne) return; // Skip the entry
		
		// Fetch all the .class files
		File [] classFiles = entry.listFiles(classFileFilter);
		if (classFiles==null) return; // No class files
		
		notifyScanStart(entry, false);
		
		for(int i=0;i<classFiles.length;i++) {
			try {
				//System.out.println("processing "+classFiles[i]);
				processClassFile(entry, false, new BufferedInputStream(new FileInputStream(classFiles[i])));
			} catch (IOException e) {
				sis.addEntry("Could not open/process class file "+classFiles[i].getAbsolutePath());
				errorOccurred=true;
			}
		}
		
		// Fetch all the subdirectories
		File [] subDirs = entry.listFiles(dirFileFilter);
		
		// Recurse
		for(int i=0;i<subDirs.length;i++) {
			scanDirectory(subDirs[i]);
		}
		
		notifyScanEnd(entry, false);
	}

	/**
	 * Method scanJarFile.
	 * @param entry
	 */
	private void scanJarFile(File entry) {
		try {
			JarFile jf = new JarFile(entry);
			Enumeration e = jf.entries();
			
			notifyScanStart(entry, true);
			while(e.hasMoreElements()) {
				JarEntry jarEntry = (JarEntry)e.nextElement();
				if (jarEntry.isDirectory()) continue;
				String jarEntryName=jarEntry.getName();
				if (jarEntryName.endsWith(".class")) {
					processClassFile(entry, true, new BufferedInputStream(jf.getInputStream(jarEntry)));
				} else if (supportNestedJars && jarEntryName.endsWith(".jar")) {
				    InputStream jis=jf.getInputStream(jarEntry);
				    scanNestedJarFile(entry, jf, jarEntry.getName(), jis);
				}
			}
			
			notifyScanEnd(entry, true);
		} catch (IOException e) {
			sis.addEntry("Scanning Jar file", "Could not open/process jar file \""+entry+"\"", e);
			errorOccurred=true;
		}
	}
	
	/** 
     * @param jf
     * @param jarEntry
	 * @param nestedJarStream
     */
    private void scanNestedJarFile(File entry, JarFile jf, String nestedJarName, InputStream nestedJarStream) {
        try {
            JarInputStream jis = new JarInputStream(nestedJarStream);
            JarEntry jarEntry = jis.getNextJarEntry();
            //System.out.println("Scanning nested jar file: "+nestedJarName+", first entry is "+jarEntry.getName());
            if (jarEntry!=null) notifyNestedScanStart(entry, nestedJarName);
            while(jarEntry!=null) {
                if (! jarEntry.isDirectory()) {
	                String jarEntryName=jarEntry.getName();
					if (jarEntryName.endsWith(".class")) {
					    
					    
					    long size = jarEntry.getSize();
					    ByteArrayOutputStream os = new ByteArrayOutputStream();
					    for(long i=0;i<size;i++)
					        os.write(jis.read());
					    byte[]extra=os.toByteArray();
					    ByteArrayInputStream is = new ByteArrayInputStream(extra);
					    
						//System.out.println("processing "+jarEntry.getName());
						processNestedJarClassFile(entry, nestedJarName, is);
					} else if (supportNestedJars && jarEntryName.endsWith(".jar")) {
					    System.out.println("Scanning nested jar "+jarEntryName+" in nested jar "+nestedJarName+" in "+jarEntryName);
					    InputStream jis2=jf.getInputStream(jarEntry);
					    scanNestedJarFile(entry, jf, jarEntry.getName(), jis2);
					}
                }
				//jis.closeEntry();
				jarEntry=jis.getNextJarEntry();
            }
            notifyNestedScanEnd(entry, nestedJarName);
        } catch (IOException e) {
            sis.addEntry("Scanning Jar file", "Could not open/process nested jar \""+nestedJarName+"\" in jar file \""+entry+"\"", e);
			errorOccurred=true;
        }
        
    }

    /**
     * @param entry
     * @param nestedJarName
     * @param jis
     * @throws IOException
     */
    private void processNestedJarClassFile(File entry, String nestedJarName, InputStream classDataInputStream) throws IOException {
        CPoolReader.classfile c = cpr.readClassData(classDataInputStream, false);
		
		String className = c.getCPClassName(true);
		String pkgName = getPkgName(className);
		
		Integer status = (Integer)packageStatus.get(pkgName);
		if (status==null) {
			status=new Integer(0);
			packageStatus.put(pkgName, status);
		}
		
		Integer newStatus = new Integer(status.intValue() | IN_NESTED_JAR);
		packageStatus.put(pkgName, status);
		
		List classes = (List)classesPerPackage.get(pkgName);
		if (classes==null) {
			classes=new ArrayList();
			classesPerPackage.put(pkgName, classes);
		}
		classes.add(className);
		
		notifyNestedFileProcessing(entry, nestedJarName, className);
        
    }

    /**
	 * Method processFiles.
	 * @param entry
	 * @param classFiles
	 */
	private void processClassFile(File entry, boolean isJar, InputStream classDataInputStream) throws IOException {
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int ch;
        while((ch=classDataInputStream.read())!=-1) bos.write(ch);
        
        ByteArrayInputStream bis1 = new ByteArrayInputStream(bos.toByteArray());
        ByteArrayInputStream bis2 = new ByteArrayInputStream(bos.toByteArray());
		
		CPoolReader.classfile c = cpr.readClassData(bis1);
		
		String className = c.getCPClassName(true);
        
        notifyProcessingClassFile(entry, isJar, className, bis2);
        
		String pkgName = getPkgName(className);
		
		List files = (List)filesPerPackage.get(pkgName);
		if (files==null) {
			files=new ArrayList();
			filesPerPackage.put(pkgName, files);
		}
		files.add(entry);
		
		Integer status = (Integer)packageStatus.get(pkgName);
		if (status==null) {
			status=new Integer(0);
			packageStatus.put(pkgName, status);
		}
		
		Integer newStatus = new Integer(status.intValue() | (isJar ? IN_JAR : IN_DIRECTORY));
		packageStatus.put(pkgName, status);
		
		List classes = (List)classesPerPackage.get(pkgName);
		if (classes==null) {
			classes=new ArrayList();
			classesPerPackage.put(pkgName, classes);
		}
		classes.add(className);
		
		notifyFileProcessing(entry, className);
	}

	

    /**
	 * Method getPkgName.
	 * @param className
	 * @return String
	 */
	private String getPkgName(String className) {
		int i=className.lastIndexOf('.');
		if (i==-1) return "";
		return className.substring(0,i);
	}


	/**
	 * Returns the classPath.
	 * @return String
	 */
	public String getClassPath() {
		return classPath;
	}

	/**
	 * @see org.sadun.util.ClassPackageExplorer#getPackageFiles(String)
	 */
	public synchronized File[] getPackageFiles(String packageName) {
		List l = (List)filesPerPackage.get(packageName);
		if (l==null) return new File[0];
		File [] files = new File[l.size()];
		l.toArray(files);
		return files;
	}

	/**
	 * @see org.sadun.util.ClassPackageExplorer#getStatus(String)
	 */
	public synchronized int getStatus(String packageName) {
		Integer i = (Integer)packageStatus.get(packageName);
		if (i==null) return -1;
		return i.intValue();
	}

	/**
	 * @see org.sadun.util.ClassPackageExplorer#listPackage(String, int)
	 */
	public synchronized String[] listPackage(String packageName, int status) {
		if (status != IN_DIRECTORY + IN_JAR + IN_JAR_SEALED) 
			throw new UnsupportedOperationException("Disaggregation by status not supported yet");
		if (classesPerPackage==null) buildInfo();
		
		List l;
		if ((l=(List)classesPerPackage.get(packageName))==null) return new String[0];
		String [] classes = new String[l.size()];
		l.toArray(classes);
		return classes;
	}

	/**
	 * @see org.sadun.util.ClassPackageExplorer#listPackageNames(boolean)
	 */
	public synchronized String[] listPackageNames(boolean rescan) {
		if (rescan || classesPerPackage==null) buildInfo();
		String [] names = new String[classesPerPackage.keySet().size()];
		classesPerPackage.keySet().toArray(names);
		return names;
	}
	
	/**
	 * Sets the classPath.
	 * @param classPath The classPath to set
	 */
	public synchronized void setClassPath(String classPath) {
			this.classPath = classPath;
			buildInfo();
	}

	/**
	 * @see org.sadun.util.ClassPackageExplorer#getErrorLog()
	 */
	public synchronized String getErrorLog() {
		if (sis==null) buildInfo();
		return sis.getStateDescription(null);
	}

	/**
	 * @see org.sadun.util.ClassPackageExplorer#hasErrorOccurred()
	 */
	public synchronized boolean hasErrorOccurred() {
		if (sis==null) buildInfo();
		return errorOccurred;
	}

	/**
	 * Add a {@link PackageExplorationListener} to this object.
	 * @param l the {@link PackageExplorationListener} to add
	 */
	public void addExplorationListener(PackageExplorationListener l) {
		listeners.add(l);
	}
	
	/**
	 * Remove a {@link PackageExplorationListener} from this object.
	 * @param l the {@link PackageExplorationListener} to remove
	 */
	public void removeExplorationListener(PackageExplorationListener l) {
		listeners.remove(l);
	}

	/**
	 * @param entry
	 */
	private void notifyScanStart(File entry, boolean isJar) {
		for (Iterator i = listeners.iterator();i.hasNext();) {
			PackageExplorationListener listener = (PackageExplorationListener)i.next();
			listener.scanStart(entry, isJar); 
		}
	}
    
    private void notifyProcessingClassFile(File entry, boolean isJar, String className, InputStream classdata) {
        for (Iterator i = listeners.iterator();i.hasNext();) {
            PackageExplorationListener listener = (PackageExplorationListener)i.next();
            listener.processingClassFile(entry, isJar, className, classdata); 
        }
        
    }
	
	/**
	 * @param entry
	 */
	private void notifyScanEnd(File entry, boolean isJar) {
		for (Iterator i = listeners.iterator();i.hasNext();) {
			PackageExplorationListener listener = (PackageExplorationListener)i.next();
			listener.scanEnd(entry, isJar); 
		}
	}
	
	/**
	 * @param entry
	 */
	private void notifyFileProcessing(File entry, String className) {
		for (Iterator i = listeners.iterator();i.hasNext();) {
			PackageExplorationListener listener = (PackageExplorationListener)i.next();
			listener.processingFile(entry, className); 
		}
	}
	
    /**
     * @param entry
     * @param nestedJarName
     */
    private void notifyNestedScanStart(File entry, String nestedJarName) {
        for (Iterator i = listeners.iterator();i.hasNext();) {
			PackageExplorationListener listener = (PackageExplorationListener)i.next();
			listener.nestedScanStart(entry, nestedJarName); 
		}
    }
    
    /**
     * @param entry
     * @param nestedJarName
     */
    private void notifyNestedScanEnd(File entry, String nestedJarName) {
        for (Iterator i = listeners.iterator();i.hasNext();) {
			PackageExplorationListener listener = (PackageExplorationListener)i.next();
			listener.nestedScanEnd(entry, nestedJarName); 
		}
    }
    
    /**
     * @param entry
     * @param nestedJarName
     * @param className
     */
    private void notifyNestedFileProcessing(File entry, String nestedJarName, String className) {
        for (Iterator i = listeners.iterator();i.hasNext();) {
			PackageExplorationListener listener = (PackageExplorationListener)i.next();
			listener.processingNestedFile(entry, nestedJarName, className); 
		}
    }
    
	/**
	 * A test method
	 */
	public static void main(String args[]) throws Exception {
		SimpleClassPackageExplorer explorer = new SimpleClassPackageExplorer(
                "c:\\Temp\\kickoff.jar", true);
		PackageExplorationListener l = new PackageExplorationListener() {
		    
            public void nestedScanStart(File entry, String nestedJarName) {
                System.out.println("Starting scanning nested jar "+nestedJarName+" in "+entry);
            }
            
            
            public void nestedScanEnd(File entry, String nestedJarName) {
                System.out.println("Finished scanning nested jar "+nestedJarName+" in "+entry);
            }
            
            
            public void processingNestedFile(File entry, String nestedJarName,
                    String className) {
                System.out.println("Processing class " + className
                        + " in nested jar " + nestedJarName + " in " + entry
                        + "(" + Runtime.getRuntime().totalMemory()/(1024*1024) + "Mb)");
                
            }
            
            public void processingFile(File classFile, String className) {
                System.out.println("Processing class "+className+" in "+classFile);
            }
            
            public void scanStart(File entry, boolean isJar) {
                System.out.println("Processing "+(isJar ? "jar file " : "")+entry);
            }
            
            public void scanEnd(File entry, boolean isJar) {
                System.out.println("Finished Processing "+(isJar ? "jar file " : "")+entry);
            }
            
            public void processingClassFile(File entry, boolean isJar, String className, InputStream classdata) {
                System.out.println("Processing class "+className);
            }
            
		};
		
		//explorer.addExplorationListener(l);
		
		String [] pkgs = explorer.listPackageNames();
		System.out.println("Total of "+pkgs.length+" packages");
		for(int i=0;i<pkgs.length;i++) {
		    System.out.println();
		    System.out.println("Package "+pkgs[i]);
		    System.out.println();
		    String [] classes = explorer.listPackage(pkgs[i]);
		    for(int j=0;j<classes.length;j++) {
		        System.out.println("  "+classes[j]);
		    }
		}
		
		System.out.println(explorer.sis.getStateDescription(null));
	}


}
