/************************************************************************************
* Copyright (C) 2002  Cristiano Sadun crsadun@tin.it
*
* You can redistribute this program and/or
* modify it under the terms of the GNU Lesser General Public License
* as published by the Free Software Foundation-
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

package org.sadun.util.ant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.sadun.util.ClassPackageExplorer;
import org.sadun.util.SimpleClassPackageExplorer;

import com.deltax.util.CPoolReader;
import com.deltax.util.DynamicClassFileFinder;
import com.deltax.util.DynamicJDK12ClassFileFinder;
import com.deltax.util.DynamicResourceFileFinder;

/**
 * An ant task to pack dependencies on a given set of classes.
 * <p>
 * The available attributes are:
 * <ul>
 * <li><b>classes</b>: a comma-separated list of classes to pack
 * <li><b>packages</b>: a comma-separated list of packages to pack. Each
 * class in the package will be included, together with its dependents.
 * <li><b>classpath</b>: additional classpath to use when packing classes
 * (optional)
 * <li><b>targetJar</b>: the name of the jar file to produce
 * <li><b>mainfestClasspath</b>: the (optional) manifest Class-Path entry
 * <li><b>mainfestMainclass</b>: the (optional) manifest Main-Class entry
 * <li><b>excludePkg</b>: a comma-separated list of package prefixes to
 * exclude. Defaults to <b>java,javax,sun</b>
 * <li><b>includePkg</b>: a comma-separated list of package prefixes to
 * include. Only classes in matching packages will be included. Has lower
 * precedence than excludePkg
 * <li><b>resolveFiltered</b>: if <b>true</b>, allows resolution of
 * classes which are filtered out. Defaults to <b>false</b>.
 * <li><b>cacheClassFiles</b>: if <b>false</b>, disables classfile
 * caching - slower packing but saving memory
 * </ul>
 * <p>
 * Additional classpath can be also specified by a nested <code>&lt;classpath&gt;</code>
 * element.
 * <p>
 * &lt;pack&gt; also supports inclusion of explicitly named additional classes
 * and/or files in the jar. Dependencies for such additional classes will be
 * computed and added too. This is done by declaring internal <b>
 * &lt;additionalclass&gt;</b> and <b>&lt;additionalfileset&gt;</b>
 * elements.
 * <p>
 * <b>&lt;additionalclass&gt;</b> has a single <b>name</b> attribute which
 * contains the fully qualified name of the class to include. The class must:
 * <ul>
 * <li>be in classpath;
 * <li>not conflict with the filter established by <b>excludePkg/includePkg
 * </b>.
 * </ul>
 * <p>
 * For example,
 * 
 * <pre>
 *  &lt;additionalclass name="javax.transaction.TransactionManager"/&gt;
 * </pre>
 * 
 * will add the <code>javax.transaction.TransactionManager</code> class and
 * all its dependent classes to the produced jar.
 * <p>
 * <b>&lt;additionalfileset&gt;</b> is a standard Ant <code>FileSet</code>
 * structure which specifies a set of files to unconditionally add to the
 * produced jar.
 * <p>
 * For example,
 * 
 * <pre>
 *  &lt;additionalfileset dir="${basedir}"&gt; &lt;include name="META-INF/services/*"/&gt; &lt;/additionalfileset&gt;
 * </pre>
 * 
 * <p>
 * will add any file under the <code>META-INF/service</code> subdirectory of
 * the current <code>${basedir}</code> directory.
 * <p>
 * 
 * $Revision$
 * 
 * @version 1.6
 * 
 * @author Cristiano Sadun
 */
public class Pack extends Task {

	interface ClassFilter {
		/**
		 * Return true if the given classfile is accepted
		 * 
		 * @param pkgname
		 *            the package name of the class
		 * @param clsName
		 *            the class name
		 * @param classfile
		 *            the
		 *            {@link com.deltax.util.CPoolReader.classfile classfile object}
		 *            for the class
		 * @return boolean <b>true</b> if the class is to be accepted
		 */
		public boolean accept(
			String pkgname,
			String clsName,
			CPoolReader.classfile classfile);
	}

	static class PackagePrefixClassFilter implements ClassFilter {

		private String[] prefixes;
		private boolean accept;

		/**
		 * Creates a filter which will accept or refuse (depending on the <tt>accept</tt>
		 * parameter) classes whose package matches a given prefix
		 * 
		 * @param prefixes
		 * @param accept
		 */
		public PackagePrefixClassFilter(String[] prefixes, boolean accept) {
			this.prefixes = prefixes;
			this.accept = accept;
		}

		/**
		 * Accepts the classes with or without one of the prefixes given at
		 * construction
		 * 
		 * @see org.sadun.ant.tasks.Pack.ClassFilter#accept(java.lang.String,
		 *      java.lang.String, null)
		 */
		public boolean accept(
			String pkgName,
			String clsName,
			CPoolReader.classfile classfile) {
			for (int i = 0; i < prefixes.length; i++)
				if (pkgName.startsWith(prefixes[i]))
					return accept;
			return !accept;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer("Filter ");
			sb.append(accept ? "includes" : "excludes");
			sb.append(
				" classes in packages whose name is prefixed with one of { ");
			for (int i = 0; i < prefixes.length; i++) {
				sb.append(prefixes[i]);
				if (i < prefixes.length - 1)
					sb.append(", ");
			}
			sb.append(" }");
			return sb.toString();
		}
	}

	private String classes;
	private String packages;
	private String targetJar;
	private boolean resolveFiltered = false;
	private String excludePkg;
	private String includePkg;
	private String classpath;
	private Path cPath;
	private String manifestClassPath;
	private String manifestMainClass;
	private boolean cacheClassFiles = true;
	private boolean detectrmi = false;

	private HashMap clsMap;
	private DynamicClassFileFinder cff;
	private DynamicResourceFileFinder rff;
	private CPoolReader cpoolreader = new CPoolReader(cff);
	private ClassFilter filter;
	private HashSet refusedNames;
	private Set additionalFiles = new HashSet();
	private Set additionalClasses = new HashSet();
	private Set resources = new HashSet();

	private Set ignorableClasses = new HashSet();
	
	public Pack() {
	    
	}

	/**
	 * Execute the task.
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException {
	    
	    String finalClassPath=null;
	    String bpv = getProject().getProperty("build.sysclasspath");
	    if (bpv==null) bpv="first"; // Backwards compatible behaviour
	    else log("build.systemclasspath is '"+bpv+"'", Project.MSG_VERBOSE);
	    if ("ignore".equals(bpv)) finalClassPath=classpath;
	    else if ("only".equals(bpv)) finalClassPath=System.getProperty("java.class.path");
	    else if ("last".equals(bpv)) finalClassPath=classpath+File.pathSeparator+System.getProperty("java.class.path");
	    else if ("first".equals(bpv)) finalClassPath=System.getProperty("java.class.path")+File.pathSeparator+classpath;
	    else throw new BuildException("Invalid value for build.sysclasspath (must be one of 'ignore','only','last','first'");
	    classpath=finalClassPath;
	    
	    cff = new DynamicJDK12ClassFileFinder(null);
		rff = (DynamicJDK12ClassFileFinder) cff;

		Set ignorableClasses2 = new HashSet();
		for (Iterator i = ignorableClasses.iterator(); i.hasNext();) {
			ignorableClasses2.add(((ClassSpec) i.next()).name);
		}

		ignorableClasses = ignorableClasses2;

		if (cacheClassFiles) {
			((DynamicJDK12ClassFileFinder) cff).setClassCacheOn(true);
		}

		if (targetJar == null)
			throw new BuildException("Missing mandatory \"targetJar\" attribute");
		if (classes == null && packages == null)
			throw new BuildException("Missing mandatory \"classes\" or \"packages\" attribute");
		if (classes != null && packages != null)
			throw new BuildException("Only one of \"classes\" or \"packages\" can be specified");
		if (classpath != null)
			cff.addClassPathEntry(classpath);
		if (cPath != null)
			cff.addClassPathEntry(cPath.toString());
		String[] clsNames;
		if (classes != null) {
			// "classes" is specified (it has precedence)
			clsNames = getStringArray(classes);
		} else {
			// "package" is specified
			// Explore the packages to find the relevant classes
			String[] pkgNames = getStringArray(packages);
			String cp2 = classpath;
			//String cp2=System.getProperty("java.class.path");
			/*
			 * if (classpath!=null)
			 */
			ClassPackageExplorer pkgExplorer =
				new SimpleClassPackageExplorer(cp2);
			Set cls = new HashSet();
			for (int i = 0; i < pkgNames.length; i++) {
				log(
					"Looking for classes in package "
						+ pkgNames[i]
						+ " using "
						+ cp2);
				String[] tmp = pkgExplorer.listPackage(pkgNames[i]);
				for (int j = 0; j < tmp.length; j++)
					cls.add(tmp[j]);
			}
			clsNames = new String[cls.size()];
			cls.toArray(clsNames);
			log("Classes to pack computed from given packages list");
		}
		clsMap = new HashMap();
		refusedNames = new HashSet();
		if (includePkg == null) {
			if (excludePkg == null)
				excludePkg = "java,javax,sun";
			filter =
				new PackagePrefixClassFilter(getStringArray(excludePkg), false);
		} else {
			filter =
				new PackagePrefixClassFilter(getStringArray(includePkg), true);
		}

		if (excludePkg != null)
			log("Excluding packages prefixed with " + excludePkg);
		if (includePkg != null)
			log("Including only packages prefixed with " + includePkg);

		for (int i = 0; i < clsNames.length; i++)
			try {
				log("Calculating dependencies for " + clsNames[i]);
				log("Classpath is " + cff.getClassPath(), Project.MSG_VERBOSE);
				// Find the dependecies for each class
				findDependencies(clsNames[i], clsMap);
			} catch (ClassNotFoundException e) {
				log(
					"The current class path is " + cff.getClassPath(),
					Project.MSG_ERR);
				throw new BuildException(e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new BuildException(e);
			}

		// Add any additional class
		try {
			for (Iterator i = additionalClasses.iterator(); i.hasNext();) {
				ClassSpec cls = (ClassSpec) i.next();
				log(
					"Finding dependencies for additional class " + cls.name,
					Project.MSG_VERBOSE);
				// Find the dependencies for each additional class
				findDependencies(cls.name, clsMap, true);
			}
		} catch (ClassNotFoundException e) {
			log(
				"The current class path is " + cff.getClassPath(),
				Project.MSG_ERR);
			throw new BuildException(e);
		} catch (IOException e) {
			throw new BuildException(e);
		}

		// Write the resulting target jar
		try {
			JarOutputStream jos =
				new JarOutputStream(
					new BufferedOutputStream(new FileOutputStream(targetJar)));
			if (manifestClassPath != null | manifestMainClass != null) {
				log("Creating manifest");
				Manifest manifest = new Manifest();
				manifest.getMainAttributes().put(
					Attributes.Name.MANIFEST_VERSION,
					"1.0");
				if (manifestClassPath != null) {
					manifest.getMainAttributes().put(
						Attributes.Name.CLASS_PATH,
						manifestClassPath);
				}
				if (manifestMainClass != null) {
					manifest.getMainAttributes().put(
						Attributes.Name.MAIN_CLASS,
						manifestMainClass);
				}
				JarEntry entry = new JarEntry("META-INF/MANIFEST.MF");
				jos.putNextEntry(entry);
				manifest.write(jos);
			}

			log("Packing " + targetJar);

			for (Iterator i = clsMap.keySet().iterator(); i.hasNext();) {
				String clsName = (String) i.next();
				String entryName = clsName.replace('.', '/') + ".class";
				JarEntry entry = new JarEntry(entryName);
				jos.putNextEntry(entry);
				byte[] bytecode = (byte[]) clsMap.get(clsName);
				ByteArrayInputStream is = new ByteArrayInputStream(bytecode);
				int c;
				while ((c = is.read()) != -1)
					jos.write(c);
			}

			// Also, determine and add additional files
			if (additionalFiles.size() > 0) {
				for (Iterator i = additionalFiles.iterator(); i.hasNext();) {
					FileSet fs = (FileSet) i.next();
					DirectoryScanner ds = fs.getDirectoryScanner(this.getProject());
					ds.scan();
					String[] includedFiles = ds.getIncludedFiles();
					for (int j = 0; j < includedFiles.length; j++) {
						File f =
							new File(
								ds.getBasedir()
									+ File.separator
									+ includedFiles[j]);
						log(
							"Adding file " + includedFiles[j],
							Project.MSG_VERBOSE);
						// Let's the jar entry have the same name as the file,
						// minus the base directory
						String fl =
							replaceAll(includedFiles[j], File.separator, "/");
						JarEntry entry = new JarEntry(fl);
						jos.putNextEntry(entry);
						InputStream is =
							new BufferedInputStream(new FileInputStream(f));
						int c;
						while ((c = is.read()) != -1)
							jos.write(c);
					}
				}
			}

			// And resources
			for (Iterator i = resources.iterator(); i.hasNext();) {
				Resource res = (Resource) i.next();
				log("Adding resource " + res, Project.MSG_VERBOSE);
				InputStream is = rff.openResource(res.name);
				if (is == null)
					throw new BuildException(
						"resource "
							+ res.name
							+ " not found. ClassPath is "
							+ rff.getClassPath());
				JarEntry entry = new JarEntry(res.name);
				jos.putNextEntry(entry);
				int c;
				while ((c = is.read()) != -1)
					jos.write(c);
			}

			jos.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new BuildException(e);
		}
	}

	/**
	 * Method replaceAll.
	 * 
	 * @param string
	 * @param string1
	 * @param string11
	 * @return String
	 */
	private static String replaceAll(String s, String orig, String dest) {
		StringBuffer sb = new StringBuffer();
		synchronized (sb) {
			int pos = 0, lastpos;
			do {
				lastpos = pos;
				pos = s.indexOf(orig, pos);
				if (pos == -1)
					continue;
				sb.append(s.substring(lastpos, pos));
				sb.append(dest);
				pos += orig.length();
			} while (pos != -1);
			sb.append(s.substring(lastpos));
			return sb.toString();
		}
	}

	private void findDependencies(String clsName, Map clsMap)
		throws IOException, ClassNotFoundException {
		findDependencies(clsName, clsMap, false);
	}

	private void findDependencies(
		String clsName,
		Map clsMap,
		boolean failOnUnaccepted)
		throws IOException, ClassNotFoundException {

		if (this.ignorableClasses.contains(clsName)) {
			log(clsName + " ignored as configured", Project.MSG_VERBOSE);
			return;
		}

		if (clsMap.keySet().contains(clsName)) {
			//log(clsName+" already accepted.", project.MSG_VERBOSE);
			return;
		}
		if (refusedNames.contains(clsName)) {
			//log(clsName+" already refused.", project.MSG_VERBOSE);
			return;
		}
		/*
		 * if (failOnUnaccepted) throw new BuildException("The class
		 * "+clsName+" is not acceptable with the current includePkg/excludePkg
		 * settings ("+filter+")");
		 */
		// Is the name an array? Try to find the component class and return
		if (clsName.startsWith("[L")) {
			String clsName2 = clsName.substring(2, clsName.length() - 1);
			findDependencies(clsName2, clsMap);
			return;
		}
		if (clsName.startsWith("[")) {
			String clsName2 = clsName.substring(1);
			if ("B".equals(clsName2))
				return;
			else if ("C".equals(clsName2))
				return;
			else if ("D".equals(clsName2))
				return;
			else if ("F".equals(clsName2))
				return;
			else if ("I".equals(clsName2))
				return;
			else if ("J".equals(clsName2))
				return;
			else if ("S".equals(clsName2))
				return;
			else if ("Z".equals(clsName2))
				return;
			else if ("V".equals(clsName2))
				return;
			findDependencies(clsName2, clsMap);
			return;
		}

		// Load the class
		byte[] bytecode = cff.getClassBytes(clsName);
		CPoolReader.classfile cf = cpoolreader.readClassData(bytecode);
		// Add it to the set, if not filtered out
		String[] tmp = splitClassName(clsName);
		boolean accepted = filter.accept(tmp[0], tmp[1], cf);
		if (failOnUnaccepted && !accepted)
			throw new BuildException(
				"The class "
					+ tmp[0]
					+ "."
					+ tmp[1]
					+ " is not acceptable with the current includePkg/excludePkg settings ("
					+ filter
					+ ")");
		if (accepted) {
			clsMap.put(clsName, bytecode);
			log(clsName + " accepted.", Project.MSG_VERBOSE);
		} else {
			refusedNames.add(clsName);
			log(clsName + " refused.", Project.MSG_VERBOSE);
		}
		if (accepted || resolveFiltered) {
			// If RMI detection is active and the class implements
			// UnicastRemoteObject,
			// try to find the stubs
			if (detectrmi && cf.isInterface()) {
				//System.out.println("Checking if "+clsName+" implements
				// Remote...");
				String superClass = cf.getSuperClass();
				if (superClass.equals("java.rmi.Remote")) {
					String stubClsName = clsName + "_Stub";
					byte[] stubBytecode = cff.getClassBytes(stubClsName);
					clsMap.put(stubClsName, stubBytecode);
				}
			}

			// Browse trhu all the class names mentioned in the constant pool,
			// and find all their dependencies
			String[] usedClasses = cf.getUsedClasses();
			for (int i = 0; i < usedClasses.length; i++) {
				String usedClassName = usedClasses[i].replace('/', '.');
				findDependencies(usedClassName, clsMap);
			}
		}
	}

	private static String[] splitClassName(String clsName) {
		int i = clsName.lastIndexOf('.');
		String[] result = new String[2];
		if (i == -1) {
			result[0] = "";
			result[1] = clsName;
		} else {
			result[0] = clsName.substring(0, i);
			result[1] = clsName.substring(i + 1);
		}
		return result;
	}

	private static String[] getStringArray(String classes) {
		StringTokenizer st = new StringTokenizer(classes, ";, ");
		String[] array = new String[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens())
			array[i++] = st.nextToken();
		return array;
	}

	/**
	 * Returns the classes.
	 * 
	 * @return String
	 */
	public String getClasses() {
		return classes;
	}

	/**
	 * Sets the classes.
	 * 
	 * @param classes
	 *            The classes to set
	 */
	public void setClasses(String classes) {
		this.classes = classes;
	}

	/**
	 * Returns the targetJar.
	 * 
	 * @return String
	 */
	public String getTargetJar() {
		return targetJar;
	}

	/**
	 * Sets the targetJar.
	 * 
	 * @param targetJar
	 *            The targetJar to set
	 */
	public void setTargetJar(String targetJar) {
		this.targetJar = targetJar;
	}

	/**
	 * Returns the resolveFiltered.
	 * 
	 * @return boolean
	 */
	public boolean getResolveFiltered() {
		return resolveFiltered;
	}

	/**
	 * Sets the resolveFiltered.
	 * 
	 * @param resolveFiltered
	 *            The resolveFiltered to set
	 */
	public void setResolveFiltered(boolean resolveFiltered) {
		this.resolveFiltered = resolveFiltered;
	}

	/**
	 * Returns the excludePkg.
	 * 
	 * @return String
	 */
	public String getExcludePkg() {
		return excludePkg;
	}

	/**
	 * Sets the excludePkg.
	 * 
	 * @param excludePkg
	 *            The excludePkg to set
	 */
	public void setExcludePkg(String excludePkg) {
		this.excludePkg = excludePkg;
	}


	/**
	 * Ant entry point for <code>classpath</code> subelements.
	 */
	public Path createClassPath() {
		if (cPath == null) {
			cPath = new Path(getProject());
		}
		return cPath;
	}

	/**
	 * Returns the classpath.
	 * 
	 * @return String
	 */
	public String getClasspath() {
		return classpath;
	}

	/**
	 * Sets the classpath.
	 * 
	 * @param classpath
	 *            The classpath to set
	 */
	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	/**
	 * Returns the manifestClassPath.
	 * 
	 * @return String
	 */
	public String getManifestClassPath() {
		return manifestClassPath;
	}

	/**
	 * Returns the manifestMainClass.
	 * 
	 * @return String
	 */
	public String getManifestMainClass() {
		return manifestMainClass;
	}

	/**
	 * Sets the manifestClassPath.
	 * 
	 * @param manifestClassPath
	 *            The manifestClassPath to set
	 */
	public void setManifestClassPath(String manifestClassPath) {
		this.manifestClassPath = manifestClassPath;
	}

	/**
	 * Sets the manifestMainClass.
	 * 
	 * @param manifestMainClass
	 *            The manifestMainClass to set
	 */
	public void setManifestMainClass(String manifestMainClass) {
		this.manifestMainClass = manifestMainClass;
	}

	/**
	 * Returns the includePkg.
	 * 
	 * @return String
	 */
	public String getIncludePkg() {
		return includePkg;
	}

	/**
	 * Sets the includePkg.
	 * 
	 * @param includePkg
	 *            The includePkg to set
	 */
	public void setIncludePkg(String includePkg) {
		this.includePkg = includePkg;
	}

	/**
	 * Ant entry point for <code>additionalfileset</code> subelements.
	 * <p>
	 * 
	 * @param fs
	 *            the fileset object to add, created by Ant engine
	 */
	public void addAdditionalFileSet(FileSet fs) {
		additionalFiles.add(fs);
	}

	/**
	 * Ant entry point for <code>additionalClass</code> subelements.
	 * <p>
	 * 
	 * @return AdditionalClass an object containing info about the class to add
	 */
	public ClassSpec createAdditionalClass() {
		ClassSpec cls = new ClassSpec();
		additionalClasses.add(cls);
		return cls;
	}

	/**
	 * Ant entry point for <code>ignoreClass</code> subelements.
	 * <p>
	 * 
	 * @return AdditionalClass an object containing info about the class to add
	 */
	public ClassSpec createIgnoreClass() {
		ClassSpec cls = new ClassSpec();
		ignorableClasses.add(cls);
		return cls;
	}

	/**
	 * Ant entry point for <code>additionalClass</code> subelements.
	 * <p>
	 * 
	 * @return AdditionalClass an object containing info about the class to add
	 */
	public Resource createResource() {
		Resource res = new Resource();
		resources.add(res);
		return res;
	}
	/**
	 * Returns the cacheClassFiles.
	 * 
	 * @return boolean
	 */
	public boolean isCacheClassFiles() {
		return cacheClassFiles;
	}

	/**
	 * Sets the cacheClassFiles.
	 * 
	 * @param cacheClassFiles
	 *            The cacheClassFiles to set
	 */
	public void setCacheClassFiles(boolean cacheClassFiles) {
		this.cacheClassFiles = cacheClassFiles;
	}

	/**
	 * Returns the packages.
	 * 
	 * @return String
	 */
	public String getPackages() {
		return packages;
	}

	/**
	 * Sets the packages.
	 * 
	 * @param packages
	 *            The packages to set
	 */
	public void setPackages(String packages) {
		this.packages = packages;
	}

}
