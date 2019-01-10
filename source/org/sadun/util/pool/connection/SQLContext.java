/*
 * Created on Sep 5, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.pool.connection;

import java.io.StringWriter;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.sadun.util.DynamicClassLoader;
import org.sadun.util.IndentedPrintWriter;
import org.sadun.util.ObjectLister;


/**
 * An SQL Context declares a set of jdbc driver names
 * and a classpath where to load them from.
 * 
 * @author Cristiano Sadun
 */
public class SQLContext {
	
	private DynamicClassLoader dcl;
	private String classPath;
	private String []jdbcDriverNames;
	private Set loadedJdbcDrivers;
	private Set failedToLoadJdbcDrivers;

	/**
	 * Create an SQL Context with the given class path and
	 * jdbc drivers set.
	 * 
	 * @param classpath the classpath to use
	 * @param jdbcDriverNames the names of the jdbc driver classes
	 */
	public SQLContext(String classPath, String []jdbcDriverNames) {
		this.classPath=classPath;
		this.jdbcDriverNames=jdbcDriverNames;
		setClassPath(classPath);
	}
		
		
	public void setJdbcDriverNames(String []jdbcDriverNames) {
		this.jdbcDriverNames=jdbcDriverNames;
		this.loadedJdbcDrivers=new HashSet();
		this.failedToLoadJdbcDrivers=new HashSet();
		for (int i=0;i<jdbcDriverNames.length;i++) {
			try {
				System.out.print("Loading "+jdbcDriverNames[i]+": ");
				
				DriverManager.registerDriver((Driver)dcl.loadClass(jdbcDriverNames[i]).newInstance());
				loadedJdbcDrivers.add(jdbcDriverNames[i]);
				System.out.println("ok.");
			} catch (Exception e) {
				failedToLoadJdbcDrivers.add(jdbcDriverNames[i]);
				System.out.println("failed.");
			}
		}
	}
	
	public SQLContext(String []jdbcDriverNames) {
		this(System.getProperty("java.class.path"), jdbcDriverNames);
	}
	
	public SQLContext(String jdbcDriverNamesList) {
		this(split(jdbcDriverNamesList, ":,; "));
	}
	
	public SQLContext() {
		this(System.getProperty("jdbc.drivers"));
	}

	/**
	 * @param string
	 * @return
	 */
	private static String[] split(String s, String separator) {
		if (s==null) return new String[0];
		StringTokenizer st = new StringTokenizer(s, separator);
		String [] result = new String[st.countTokens()];
		int c=0;
		while(st.hasMoreTokens()) {
			result[c++]=st.nextToken();
		}
		return result;
	}
	
	public String toString() {
		StringWriter sw = new StringWriter();
		IndentedPrintWriter pw = new IndentedPrintWriter(sw);
		pw.print("SQL Context, classpath: ");
		pw.println(classPath);
		pw.println();
		pw.print(jdbcDriverNames.length+" JDBC Drivers registered");
		if (jdbcDriverNames.length>0) pw.print(":");
		pw.println();
		pw.incIndentation(3);
		for(int i=0;i<jdbcDriverNames.length;i++) {
			pw.print(jdbcDriverNames[i]);
			pw.print(" (");
			if (loadedJdbcDrivers.contains(jdbcDriverNames[i])) pw.print("loaded");
			else pw.print("failed");
			pw.println(")");
		}
		return sw.toString();
	}


	/**
	 * @return
	 */
	public String getClassPath() {
		return classPath;
	}

	/**
	 * @param classPath
	 */
	public void setClassPath(String classPath) {
		this.classPath = classPath;
		this.dcl=new DynamicClassLoader();
		this.dcl.setClassPath(classPath);
		setJdbcDriverNames(jdbcDriverNames);
	}

	/**
	 * @return
	 */
	public String [] getFailedToLoadJdbcDrivers() {
		String [] result = new String[failedToLoadJdbcDrivers.size()];
		failedToLoadJdbcDrivers.toArray(result);
		return result;
	}

	/**
	 * @return
	 */
	public String[] getJdbcDriverNames() {
		return jdbcDriverNames;
	}

	/**
	 * @return
	 */
	public String[] getLoadedJdbcDrivers() {
		String [] result = new String[loadedJdbcDrivers.size()];
		loadedJdbcDrivers.toArray(result);
		return result;
	}
	
	public static void main(String args[]) {
		System.out.println(new SQLContext("com.microsoft.jdbc.sqlserver.SQLServerDriver"));
	}


	/**
	 * @return
	 */
	public String getJdbcDriverNamesAsString() {
		String s = new ObjectLister(":", (char)0).list(jdbcDriverNames);
		return s; 
	}

}
