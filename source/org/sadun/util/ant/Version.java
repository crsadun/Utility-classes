/*******************************************************************************
 * Copyright (C) 2002 Cristiano Sadun crsadun@tin.it
 * 
 * You can redistribute this program and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software
 * Foundation-
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 ******************************************************************************/
package org.sadun.util.ant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.sadun.util.IndentedPrintWriter;
import org.sadun.util.VersionImpl;

import com.deltax.util.JDK12ClassFileFinder;
import com.sun.tools.javac.v8.JavaCompiler;
import com.sun.tools.javac.v8.util.Context;
import com.sun.tools.javac.v8.util.Hashtable;
import com.sun.tools.javac.v8.util.Options;

/**
 * A task to generate {@link org.sadun.util.Version}objects when compiling.
 * <p>
 * <b>Note: </b> <tt>tools.jar</tt> must be in classpath for the task to work.
 * 
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano
 *         Sadun </a>
 * @version 1.1
 */
public class Version extends BaseVersion {

	private int major = org.sadun.util.Version.NOT_VALUED;
	private int minor = org.sadun.util.Version.NOT_VALUED;
	private int micro = org.sadun.util.Version.NOT_VALUED;
	private String level = null;
	private boolean deleteGeneratedCode = false;
	private boolean copyStdClasses = true;

	private String propertyName;
	private boolean override;

	private long lastBuildNumber = org.sadun.util.Version.NOT_VALUED;

	private boolean verbose = false;

	/**
	 * Create a subclass of {@link org.sadun.util.VersionImpl}with the
	 * appropriate values.
	 */
	public void execute() throws BuildException {

		super.execute();

		lastBuildNumber = computeLastBuildNumber();
		log("Build number will be " + (lastBuildNumber + 1));
		long buildTime = System.currentTimeMillis();
		VersionImpl v = new VersionImpl(major, minor, micro, level,
				lastBuildNumber, buildTime);
		if (!v.hasInfoEnough())
			throw new BuildException(
					"Not enough version information provided. At least give major number, or build number, or level");

		
		log("Setting properties...");
		if (propertyName == null)
			propertyName = "version";

		String vrs = (major == org.sadun.util.Version.NOT_VALUED ? v
				.getMajorNumber() : major)
				+ "."
				+ (minor == org.sadun.util.Version.NOT_VALUED ? v
						.getMajorNumber() : minor)
				+ "."
				+ (micro == org.sadun.util.Version.NOT_VALUED ? v
						.getMicroNumber() : micro);

		safeSet(propertyName, vrs);
		safeSet(propertyName + ".major", "" + v.getMajorNumber());
		safeSet(propertyName + ".minor", "" + v.getMinorNumber());
		safeSet(propertyName + ".micro", "" + v.getMicroNumber());
		safeSet(propertyName + ".build", "" + v.getBuildNumber());

		log("Generating version object source in "
				+ versionObjectSourcePath.getAbsolutePath());
		File classCode = generateVersionClassCode(buildTime);
		log("Compiling " + classCode.getAbsolutePath() + " in "
				+ versionObjectBinaryPath.getAbsolutePath());
		compileVersionCode(classCode);

		if (copyStdClasses)
			copyStdClasses(versionObjectBinaryPath);

		// Create the new .versioninfo if necessary
		if (!useExistingVersionClass) {
			log("Updating " + versionInfoPath.getAbsolutePath());
			mkVersionInfoFile(versionInfoPath, lastBuildNumber + 1);
		}

		if (deleteGeneratedCode) {
			log("Deleting " + classCode.getAbsolutePath());
			if (!classCode.delete())
				throw new BuildException("Cannot delete " + classCode);
		}
		log("Version class generation done");
	}

	private void safeSet(String propertyName, String value) {
		if (getProject().getProperty(propertyName) != null && !override)
			throw new BuildException("The property '" + propertyName
					+ "' already exists. User override='yes' to allow override");
		getProject().setProperty(propertyName, value);
		log("Property " + propertyName + " set to "
				+ getProject().getProperty(propertyName));
	}

	private void copyStdClasses(File binPath) throws BuildException {
		log("Copying standard library classes to " + binPath.getAbsolutePath());
		JDK12ClassFileFinder cff = new JDK12ClassFileFinder(getTaskClassPath());

		// Interface
		try {
			writeClassFile(Version.class.getName(), binPath, cff
					.getBytes(Version.class.getName()));
			// Version interface
		} catch (IOException e) {
			throw new BuildException("I/O error attempting to locate "
					+ Version.class.getName() + " in " + getTaskClassPath(), e);
		} catch (ClassNotFoundException e) {
			throw new BuildException("Could not locate "
					+ Version.class.getName() + " in " + getTaskClassPath());
		}

		// Base implementation
		try {
			writeClassFile(VersionImpl.class.getName(), binPath, cff
					.getBytes(VersionImpl.class.getName()));
			// Version interface
		} catch (IOException e) {
			throw new BuildException(
					"I/O error attempting to locate "
							+ VersionImpl.class.getName() + " in "
							+ getTaskClassPath(), e);
		} catch (ClassNotFoundException e) {
			throw new BuildException("Could not locate "
					+ VersionImpl.class.getName() + " in " + getTaskClassPath());
		}

	}

	private void writeClassFile(String name, File binPath, byte[] bytecode)
			throws BuildException {
		File filePath = mkFilePath(binPath, frontName(name), baseName(name),
				"class");
		OutputStream os;
		try {
			os = new BufferedOutputStream(new FileOutputStream(filePath));
			ByteArrayInputStream is = new ByteArrayInputStream(bytecode);
			int c;
			while ((c = is.read()) != -1)
				os.write(c);
			os.close();
		} catch (FileNotFoundException e) {
			throw new BuildException(
					"Could not write "
							+ filePath.getAbsolutePath()
							+ ". Use copyStdClasses=no to disable library class creation.");
		} catch (IOException e) {
			throw new BuildException(
					"Error writing "
							+ filePath.getAbsolutePath()
							+ ". Use copyStdClasses=no to disable library class creation.");
		}

	}

	private String taskClassPath;

	private String getTaskClassPath() {
		if (taskClassPath == null) {
			String classPath;
			if (getClass().getClassLoader() instanceof AntClassLoader) {
				AntClassLoader cl = (AntClassLoader) getClass()
						.getClassLoader();
				classPath = cl.getClasspath();
			} else {
				log(
						"Version task ClassLoader is not the expected "
								+ AntClassLoader.class.getName()
								+ " but "
								+ getClass().getClassLoader().getClass()
										.getName()
								+ " instead. Setting classpath by using java.class.path property - required interfaces might not be found",
						Project.MSG_WARN);
				classPath = System.getProperty("java.class.path");
			}
			log("Class path is " + classPath);
			taskClassPath = classPath;
		}
		return taskClassPath;
	}

	/**
	 * @param classCode
	 */
	private void compileVersionCode(File classCode) {
		// Compile - tools.jar must be in classpath, besides the required
		// org.sadun.util.classes

		Hashtable options = new Hashtable();
		options.put("-classpath", System.getProperty("java.class.path"));

		com.sun.tools.javac.v8.util.List list = com.sun.tools.javac.v8.util.List
				.make(classCode.getAbsolutePath());
		Context ctx = new Context();
		Options opt = Options.instance(ctx);

		opt.put("-classpath", getTaskClassPath());
		JavaCompiler jc = new JavaCompiler(ctx);

		try {
			jc.verbose = verbose;
			jc.classOutput = true;
			com.sun.tools.javac.v8.util.List l = jc.compile(list);
			jc.close();
		} catch (Throwable e) {
			throw new BuildException("Code compilation has failed", e);
		}
		if (jc.errorCount() != 0)
			throw new BuildException("Code compilation has failed");
	}

	/**
	 * Create the version class source code in the given file
	 * 
	 * @return
	 */
	private File generateVersionClassCode(long buildTime) {
		StringWriter sw = new StringWriter();
		IndentedPrintWriter pw = new IndentedPrintWriter(sw);

		pw.println("// GENERATED CODE - MODIFICATIONS WILL BE LOST");
		pw
				.println("// Version class generated by Version ant task - (C) Copyright 2002 dr. Cristiano Sadun; licensed under GPL");
		if (versionClsPackageName.length() > 0) {
			pw.println("package " + versionClsPackageName + ";");
			pw.println();
		}

		pw.println("/**");
		pw.println(" * This class is automatically generated by the "
				+ Version.class.getName() + " ANT task");
		pw.println(" * to encapsulate version information. ");
		pw.println(" */");
		pw.println("public final class " + versionSimpleClsName + " extends "
				+ VersionImpl.class.getName() + " {");
		pw.incIndentation(3);
		pw.println("public " + versionSimpleClsName + "() {");
		pw.println("  super(" + getRepr(major) + "," + getRepr(minor) + ","
				+ getRepr(micro) + "," + getRepr(level) + ","
				+ getRepr(lastBuildNumber + 1) + "L," + getRepr(buildTime)
				+ "L);");
		pw.println("}");
		pw.println();

		pw.println("public " + versionSimpleClsName
				+ "(int major, int minor, int micro) {");
		pw.println("  super(major, minor, micro," + getRepr(level) + ","
				+ getRepr(lastBuildNumber + 1) + "L," + getRepr(buildTime)
				+ "L);");
		pw.println("}");
		pw.println();

		pw.println("public static void main(String args[]) { ");
		pw.println(" System.out.println(new " + versionSimpleClsName
				+ "().getFullDescription());");
		pw.println("}");

		pw.decIndentation(3);
		pw.println("}");

		// Create a file and write the code into it
		File file = mkFilePath(versionObjectSourcePath, versionClsPackageName,
				versionSimpleClsName, "java");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file));
			StringReader sr = new StringReader(sw.toString());
			int c;
			while ((c = sr.read()) != -1)
				bw.write(c);
			return file;
		} catch (IOException e) {
			throw new BuildException("Could not write version soruce code in "
					+ file);
		} finally {
			if (bw != null)
				try {
					bw.close();
				} catch (IOException e) {
					log("Could not close output stream for " + file,
							Project.MSG_WARN);
				}
		}
	}

	private File mkFilePath(File basePath, String pkgName, String clsName,
			String extension) {

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < pkgName.length(); i++)
			if (pkgName.charAt(i) == '.')
				sb.append(File.separator);
			else
				sb.append(pkgName.charAt(i));
		String pkgDir = sb.toString();
		File dir = new File(basePath, pkgDir);

		if (dir.exists()) {
			if (!dir.isDirectory())
				throw new BuildException("The path " + dir
						+ " is not a directory");
		} else {
			if (!dir.mkdirs())
				throw new BuildException(
						"Could not create directory branch for " + dir);
		}

		return new File(dir, clsName + "." + extension);
	}

	/**
	 * @param level2
	 * @return
	 */
	private String getRepr(String s) {
		if (s == null)
			return "null";
		else {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < s.length(); i++) {
				if (s.charAt(i) == '\"')
					sb.append("\\\"");
				else
					sb.append(s.charAt(i));
			}
			return sb.toString();
		}
	}

	private String getRepr(long l) {
		if (l == org.sadun.util.Version.NOT_VALUED)
			return org.sadun.util.Version.class.getName() + ".NOT_VALUED";
		return String.valueOf(l);
	}

	private void mkVersionInfoFile(File versionInfoFile, long newBuildNumber) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(
					versionInfoPath)));
			pw.println(BUILD_NUMBER_PROPERTY_NAME + "=" + newBuildNumber + "L");

		} catch (IOException e) {
			throw new BuildException("Could not write "
					+ versionInfoPath.getAbsolutePath(), e);
		} finally {
			if (pw != null)
				pw.close();
		}
	}

	/**
	 * @return Returns the major.
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * @param major
	 *            The major to set.
	 */
	public void setMajor(int major) {
		this.major = major;
	}

	/**
	 * @return Returns the micro.
	 */
	public int getMicro() {
		return micro;
	}

	/**
	 * @param micro
	 *            The micro to set.
	 */
	public void setMicro(int micro) {
		this.micro = micro;
	}

	/**
	 * @return Returns the minor.
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * @param minor
	 *            The minor to set.
	 */
	public void setMinor(int minor) {
		this.minor = minor;
	}

	/**
	 * @return Returns the useExistingVersionClass.
	 */
	public boolean isUseExistingVersionClass() {
		return useExistingVersionClass;
	}

	/**
	 * @param useExistingVersionClass
	 *            The useExistingVersionClass to set.
	 */
	public void setUseExistingVersionClass(boolean useExistingVersionClass) {
		this.useExistingVersionClass = useExistingVersionClass;
	}

	/**
	 * @return Returns the level.
	 */
	public String getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            The level to set.
	 */
	public void setLevel(String level) {
		this.level = level;
	}

	/**
	 * @return Returns the deleteGeneratedCode.
	 */
	public boolean isDeleteGeneratedCode() {
		return deleteGeneratedCode;
	}

	/**
	 * @param deleteGeneratedCode
	 *            The deleteGeneratedCode to set.
	 */
	public void setDeleteGeneratedCode(boolean deleteGeneratedCode) {
		this.deleteGeneratedCode = deleteGeneratedCode;
	}

	public static void main(String args[]) {
		Project prj = new Project();
		prj.addBuildListener(new BuildListener() {
			public void buildStarted(BuildEvent event) {
				System.out.println(event);
			}
			public void buildFinished(BuildEvent event) {
				System.out.println(event);
			}
			public void messageLogged(BuildEvent event) {
				System.out.println(event.getMessage());
			}
			public void targetFinished(BuildEvent event) {
				System.out.println(event);
			}
			public void targetStarted(BuildEvent event) {
				System.out.println(event);
			}
			public void taskFinished(BuildEvent event) {
				System.out.println(event);
			}
			public void taskStarted(BuildEvent event) {
				System.out.println(event);
			}
		});

		Version version = new Version();
		version.setProject(prj);
		//version.setUseExistingVersionClass(true);
		version.setPackageName("org.sadun.util");
		version.setVersionObjectSourcePath(new File("./source"));
		version.setVersionObjectBinaryPath(new File("./classes"));

		version.execute();
	}

	/**
	 * @return Returns the verbose.
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * @param verbose
	 *            The verbose to set.
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isOverride() {
		return override;
	}
	public void setOverride(boolean override) {
		this.override = override;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
}