/*
 * Created on Jun 28, 2004
 */
package org.sadun.util.ant;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.deltax.util.JDK12ClassFileFinder;

/**
 * @author Cristiano Sadun
 */
public class BaseVersion extends Task {

	protected class VClassLoader extends ClassLoader {

		private String classPath;

		VClassLoader(String classPath) {
			this.classPath = classPath;
		}

		VClassLoader() {
			this(System.getProperty("java.class.path"));
		}

		public Class loadClass(String name) throws ClassNotFoundException {
			if (!name.equals(versionFQClsName))
				return super.loadClass(name);
			log("Looking for " + name + " with class path " + classPath);
			JDK12ClassFileFinder cff = new JDK12ClassFileFinder(classPath);
			byte[] bytes;
			try {
				bytes = cff.getBytes(name);
			} catch (IOException e) {
				throw new ClassNotFoundException("Could not load " + name, e);
			} catch (ClassNotFoundException e) {
				throw e;
			}
			return defineClass(name, bytes, 0, bytes.length);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException {
		if (packageName == null)
			throw new BuildException("PackageName attribute missing");

		if (versionInfoPath == null)
			versionInfoPath = new File("." + packageName + ".versionInfo");

		versionFQClsName = mkVersionClsName(packageName);
		versionClsPackageName = frontName(versionFQClsName);
		versionSimpleClsName = baseName(versionFQClsName);
		
		log("Version info path is "+versionInfoPath.getAbsolutePath());

	}

	protected String packageName = null;
	protected String versionClsPackageName;
	protected String versionFQClsName;
	protected File versionInfoPath = null;
	protected String versionSimpleClsName;
	protected static final String BUILD_NUMBER_PROPERTY_NAME = "last.build.number";
	protected boolean useExistingVersionClass = false;

	/**
	 * @param versionFQClsName
	 * @return
	 */
	protected String baseName(String s) {
		int i = s.lastIndexOf('.');
		if (i == -1)
			return s;
		return s.substring(i + 1);
	}
	/**
	 * @param versionFQClsName
	 * @return
	 */
	protected String frontName(String s) {
		int i = s.lastIndexOf('.');
		if (i == -1)
			return "";
		return s.substring(0, i);
	}
	/**
	 * Composte the version class name.
	 * 
	 * @param packageName
	 * @return
	 */
	protected String mkVersionClsName(String packageName) {
		return packageName + ".version.Version";
	}
	/**
	 * Find out the last build number, either by looking in the versionInfo file
	 * or looking for an existing object in class path
	 */
	protected long computeLastBuildNumber() throws BuildException {

		Properties p = getExistingBuildNumber();

		// Pick up the last build from the properties
		String lastBuildNumber = p.getProperty(BUILD_NUMBER_PROPERTY_NAME);
		if (lastBuildNumber == null)
			throw new BuildException("Missing " + BUILD_NUMBER_PROPERTY_NAME
					+ " property in " + versionInfoPath.getAbsolutePath());

		if (lastBuildNumber.endsWith("L") && lastBuildNumber.length() > 1)
			lastBuildNumber = lastBuildNumber.substring(0, lastBuildNumber
					.length() - 1);
		try {
			return Long.parseLong(lastBuildNumber);
		} catch (NumberFormatException e) {
			throw new BuildException(lastBuildNumber
					+ " is not a valid value for the "
					+ BUILD_NUMBER_PROPERTY_NAME + " property in "
					+ versionInfoPath);
		}
	}

	protected org.sadun.util.Version getExistingVersionObject()
			throws ClassNotFoundException {
		log("Attempting to load " + versionFQClsName);
		try {
			VClassLoader cl = new VClassLoader();
			org.sadun.util.Version v = (org.sadun.util.Version) cl.loadClass(
					versionFQClsName).newInstance();
			log("Existing version is [" + v.getFullDescription() + "]");
			return v;
		} catch (InstantiationException e2) {
			throw new BuildException("Could not instantiate existing "
					+ versionFQClsName, e2);
		} catch (IllegalAccessException e2) {
			throw new BuildException("Could not access existing "
					+ versionFQClsName, e2);
		}

	}

	/**
	 * @return
	 */
	protected Properties getExistingBuildNumber() {
		Properties p = new Properties();
		if (useExistingVersionClass) {
			// Load the existing class
			try {
				org.sadun.util.Version v = getExistingVersionObject();
				p.put(BUILD_NUMBER_PROPERTY_NAME, "" + v.getBuildNumber());

			} catch (ClassNotFoundException e1) {
				log(versionFQClsName
						+ " not found, using 0 as last build number");
				p.put(BUILD_NUMBER_PROPERTY_NAME, "0");
			}
			return p;
		} else {
			// Look into the version info file
			if (versionInfoPath.exists()) {
				log("Reading " + versionInfoPath.getAbsolutePath());
				InputStream is = null;
				try {

					p.load(is = new BufferedInputStream(new FileInputStream(
							versionInfoPath)));
				} catch (IOException e) {
					throw new BuildException("Cannot read "
							+ versionInfoPath.getAbsolutePath(), e);
				} finally {
					if (is != null)
						try {
							is.close();
						} catch (IOException e) {
							log("Could not close input stream for "
									+ versionInfoPath.getAbsolutePath(),
									Project.MSG_WARN);
						}
				}
			} else {
				// Version info file doesn't exist - create it and set
				// last.build.number to 0
				log(versionInfoPath.getAbsolutePath()
						+ " not found, using 0 as last build number");
				p.put(BUILD_NUMBER_PROPERTY_NAME, "0");
			}
		}
		return p;
	}
	/**
	 * @return Returns the packageName.
	 */
	public String getPackageName() {
		return packageName;
	}
	/**
	 * @param packageName
	 *            The packageName to set.
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	/**
	 * @return Returns the versionInfoPath.
	 */
	public File getVersionInfoPath() {
		return versionInfoPath;
	}
	/**
	 * @return Returns the versionObjectPath.
	 */
	public File getVersionObjectBinaryPath() {
		return versionObjectBinaryPath;
	}
	/**
	 * @return Returns the versionObjectSourcePath.
	 */
	public File getVersionObjectSourcePath() {
		return versionObjectSourcePath;
	}
	/**
	 * @param versionInfoPath
	 *            The versionInfoPath to set.
	 */
	public void setVersionInfoPath(File versionInfoPath) {
		this.versionInfoPath = versionInfoPath;
	}
	/**
	 * @param versionObjectPath
	 *            The versionObjectPath to set.
	 */
	public void setVersionObjectBinaryPath(File versionObjectPath) {
		this.versionObjectBinaryPath = versionObjectPath;
	}
	/**
	 * @param versionObjectSourcePath
	 *            The versionObjectSourcePath to set.
	 */
	public void setVersionObjectSourcePath(File versionObjectSourcePath) {
		this.versionObjectSourcePath = versionObjectSourcePath;
	}

	protected File versionObjectBinaryPath = new File(".");
	protected File versionObjectSourcePath = new File(".");

}