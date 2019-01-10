package org.sadun.util.sql;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.sadun.util.DynamicClassLoader;
import org.sadun.util.ObjectLister;

/**
 * This class allows to load JDBC drivers by specifiying their location at runtime, 
 * and to successively obtain connections.
 * <p>
 * Note that method {@link DynamicConnectionFactory#loadDriver(String)} must be used instead of
 * <tt>Class.forName</tt>, and one of the {@link #getConnection(String)} overloads to 
 * obtain a connection.
 * <p>
 * Using <tt>DriverManager</tt> directly will result in a "No suitable driver".
 * <p>
 * Example code follows:
 * <pre>
 *  DynamicConnectionFactory factory = new DynamicConnectionFactory();
 *  factory.addClassPathEntry(..class path of the jdbc driver(s)...);
 *  factory.loadDriver(..driver class name...);
 *  Connection conn = factory.getConnection(..jdbc URL...);
 * </pre>
 * @author Cristiano Sadun
 */
public class DynamicConnectionFactory {
	 
	private Class cls;
	private Object factoryInternal;
	private DynamicClassLoader loader;
	
	/**
	 * Create a factory which uses the same class path as the system.
	 * {@link #addClassPathEntry(String)} can be successively used to extend the class path}.
	 */
	public DynamicConnectionFactory() {
		this(new DynamicClassLoader());
	}

	private DynamicConnectionFactory(DynamicClassLoader loader) {
		this.loader=loader;
		loader.setForceDynamicLoading(true);
		try {
			cls = loader.loadClass("org.sadun.util.sql.ConnectionFactoryInternal");
			try {
				Constructor ctor = cls.getDeclaredConstructor(new Class[0]);
				ctor.setAccessible(true);
				factoryInternal = ctor.newInstance(new Object[0]);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Unexpectedly can't find the default constructor in the utility class org.sadun.util.sql.ConnectionFactoryInternal",e);
			}  catch (InvocationTargetException e) {
				throw new RuntimeException("Unexpectedly can't invoke the default constructor in the utility class org.sadun.util.sql.ConnectionFactoryInternal",e);
			}
			//factoryInternal = cls.newInstance();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unexpectedly can't find the utility class org.sadun.util.sql.ConnectionFactoryInternal",e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Unexpectedly can't instantiate the utility class org.sadun.util.sql.ConnectionFactoryInternal",e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unexpectedly can't access the utility class org.sadun.util.sql.ConnectionFactoryInternal",e);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	/**
	 * Add a class path entry.
	 * @param entry the entry to add
	 */
	public void addClassPathEntry(String entry) {
		loader.addClassPathEntry(entry);
	}
	
	/**
	 * Set the class path.
	 * @param cp the class path to set.
	 */
	public void setClassPath(String cp) { loader.setClassPath(cp); }
	
	/**
	 * Load a JDBC driver dynamically.
	 * @param driverClassName the name of the driver
	 * @return the driver Class object
	 * @throws ClassNotFoundException if the driver class does not exist
	 */
	public Class loadDriver(String driverClassName) throws ClassNotFoundException {
		return Class.forName(driverClassName, true, loader);
	}
	
	private Connection invoke(Class [] argTypes, Object [] args) throws SQLException {
		try {
			Method m = cls.getMethod("getConnection", argTypes);
			m.setAccessible(true);
			try {
				return (Connection)m.invoke(factoryInternal, args);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				if (e.getCause() instanceof SQLException) throw (SQLException)e.getCause();
				throw new RuntimeException(e);
			}
		} catch (SecurityException e1) {
			throw new RuntimeException(e1);
		} catch (NoSuchMethodException e1) {
			
			new ObjectLister("\r\n").println(cls.getMethods());
			
			throw new RuntimeException("Could not find method getConnection() with args ("+new ObjectLister((char)0).list(argTypes)+")", e1);
		}
	 }

	/**
	 * Return a connection by using the given jdbcURL and properties (as DriverManager)
	 * @param jdbcUrl the JDBC url to connect to
	 * @param p the connection properties
	 * @return a JDBC Connection
	 * @throws SQLException if a connection cannot be established
	 */
	public Connection getConnection(String jdbcUrl, Properties p) throws SQLException {
		return invoke(new Class[] { String.class, Properties.class }, new Object[] {jdbcUrl, p});
	}

	/**
	 * Return a connection by using the given jdbc URL (as DriverManager)
	 * @param jdbcUrl the JDBC url to connect to
	 * @return a JDBC Connection
	 * @throws SQLException if a connection cannot be established
	 */
	public Connection getConnection(String jdbcUrl) throws SQLException {
		return invoke(new Class[] { String.class }, new Object[] {jdbcUrl});
	}
	
	/**
	 * Return a connection by using the given jdbcURL and username/password (as DriverManager)
	 * @param jdbcUrl the JDBC url to connect to
	 * @param user the user
	 * @param password the password
	 * @return a JDBC Connection
	 * @throws SQLException if a connection cannot be established
	 */
	public Connection getConnection(String jdbcUrl, String user, String pwd) throws SQLException {
		return invoke(new Class[] { String.class, String.class, String.class }, new Object[] {jdbcUrl, user, pwd});
	}
	
}
