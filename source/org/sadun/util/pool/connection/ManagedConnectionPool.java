/*
 * Created on Aug 27, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.sadun.util.pool.connection;

import java.sql.Connection;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.management.MBeanException;
import javax.management.ObjectName;

import org.sadun.util.ObjectLister;
import org.sadun.util.pool.ObjectPool;
import org.sadun.util.watchdog.WatchDog;
import org.sadun.util.watchdog.WatchDogException;

/**
 * <font face=RED>NOT COMPLETE YET</font>
 * 
 * @author Cristiano Sadun
 */
public class ManagedConnectionPool
	implements ManagedConnectionPoolMBean, WatchDog.Listener {

	private ConnectionPool pool;
	private int size = 1;
	private String jdbcURL = "";
	private String connectionProperties = "";
	private String jdbcDrivers = "";
	private String currentState = "Watchdog not enabled";

	private ObjectName objectName;

	private static final boolean debug = true;

	/**
	 * @param n
	 * @param factory
	 */
	public ManagedConnectionPool() {
	}

	/**
	 * @param factoryClassName
	 * @return
	 */
	private static ObjectPool.Factory createFactory(String factoryClassName)
		throws MBeanException {
		if (debug)
			System.out.println(
				"[ManagedConnectionPool] Creating factory " + factoryClassName);
		try {
			Object obj = Class.forName(factoryClassName).newInstance();
			if (!(obj instanceof ObjectPool.Factory))
				throw new MBeanException(
					new IllegalArgumentException("FactoryClassName must be the name of a class implementing the ObjectPool.Factory interface"));
			return (ObjectPool.Factory) obj;
		} catch (InstantiationException e) {
			throw new MBeanException(e);
		} catch (IllegalAccessException e) {
			throw new MBeanException(e);
		} catch (ClassNotFoundException e) {
			throw new MBeanException(e);
		}
	}

	/**
	 * Set the pool size. If the specified size is different than the current
	 * size, the pool is reset to the given size.
	 * <p>
	 * This property cannot be changed if some client has already acquired
	 * pooled objects.
	 * 
	 * @param size
	 *            the new size
	 */
	public void setSize(int size) throws MBeanException {

		if (debug)
			System.out.println(
				"[ManagedConnectionPool] Setting size to " + size);

		if (size == getSize())
			return;
		checkPoolStateForReset();

		int oldSize = this.size;

		this.size = size;
		this.pool = createPool(jdbcURL, connectionProperties, oldSize);
	}

	/**
	 * Set the jdbc URL of the pool.
	 * <p>
	 * This property cannot be changed if some client has already acquired
	 * pooled objects.
	 * 
	 * @param jdbcURL
	 *            the new jdbcURL
	 */
	public void setJdbcURL(String jdbcURL) throws MBeanException {

		if (debug)
			System.out.println(
				"[ManagedConnectionPool] Setting JDBC url to " + jdbcURL);

		checkPoolStateForReset();

		String oldURL = this.jdbcURL;

		this.jdbcURL = jdbcURL;
		this.pool = createPool(oldURL, connectionProperties, size);
	}

	/**
	 * Set the connection properties of the pool.
	 * <p>
	 * This property cannot be changed if some client has already acquired
	 * pooled objects.
	 * 
	 * @param jdbcURL
	 *            the new jdbcURL
	 */
	public void setConnectionProperties(String connectionProperties)
		throws MBeanException {
		if (debug)
			System.out.println(
				"[ManagedConnectionPool] Setting connection properties to "
					+ connectionProperties);

		checkPoolStateForReset();
		String oldProperties = connectionProperties;
		this.connectionProperties = connectionProperties;
		this.pool = createPool(jdbcURL, oldProperties, size);
	}

	/**
	 * Set the list of JDBC driver classes to load, separated by
	 * colon/semicolon or comma.
	 * 
	 * @param jdbcDrivers
	 *            the list of JDBC driver classes to load, separated by
	 *            colon/semicolon or comma.
	 */
	public void setJdbcDrivers(String jdbcDrivers) throws MBeanException {
		if (debug)
			System.out.println(
				"[ManagedConnectionPool] Loading drivers " + jdbcDrivers);
		StringTokenizer st = new StringTokenizer(jdbcDrivers, ":,; ");
		while (st.hasMoreTokens()) {
			String clsName = st.nextToken();
			try {
				Class.forName(clsName);
			} catch (ClassNotFoundException e) {
				throw new MBeanException(
					e,
					"Could not find JDBC driver class " + clsName);
			}
		}
		this.jdbcDrivers = jdbcDrivers;
		this.pool = createPool(jdbcURL, connectionProperties, size);
	}

	private void checkPoolStateForReset() throws MBeanException {
		if (pool != null) {
			if (pool.getFreeCount() != pool.getSize())
				throw new MBeanException(
					new IllegalStateException(
						"Cannot reset the pool when there are "
							+ getUsedCount()
							+ " used objects"));
		}
	}

	/**
	 * @see org.sadun.util.pool.ManagedObjectPoolMBean#getFreeCount()
	 */
	public int getFreeCount() {
		if (pool == null)
			this.pool = createPool(jdbcURL, connectionProperties, size);
		return pool.getFreeCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.pool.ManagedObjectPoolMBean#getObjectType()
	 */
	public Class getObjectType() {
		return Connection.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.pool.ManagedObjectPoolMBean#getSize()
	 */
	public int getSize() {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.pool.ManagedObjectPoolMBean#getUsedCount()
	 */
	public int getUsedCount() {
		if (pool == null)
			this.pool = createPool(jdbcURL, connectionProperties, size);
		return pool.getUsedCount();
	}

	/**
	 * Create the pool with the current parameters. If the creation fails in
	 * any way, reset the parameters to their previous values
	 * 
	 * @return
	 */
	private ConnectionPool createPool(
		String oldJDBCUrl,
		String oldConnectionProperties,
		int oldSize) {

		if (debug)
			System.out.println(
				"[ManagedConnectionPool] Creating pool of "
					+ size
					+ " connections to "
					+ jdbcURL
					+ " with properties "
					+ ObjectLister.getInstance().list(
						makeProperties(connectionProperties)));

		try {
			return new ConnectionPool(
				size,
				jdbcURL,
				makeProperties(connectionProperties));
		} catch (RuntimeException e) {
			jdbcURL = oldJDBCUrl;
			connectionProperties = oldConnectionProperties;
			size = oldSize;
			return null;
		}
	}

	private static Properties makeProperties(String properties) {
		char quoteChar = 0;
		StringBuffer sb = new StringBuffer();
		String name = null;
		Properties prop = new Properties();
		synchronized (sb) {
			for (int i = 0; i < properties.length(); i++) {
				char c = properties.charAt(i);
				if (quoteChar != 0) { // In quoted text
					if (c == '\\') {
						if (i < properties.length() - 1) {
							char nextChar = properties.charAt(i + 1);
							if (nextChar == '\"' || nextChar == '\'') {
								sb.append(nextChar);
								i += 1;
								continue;
							}
						} else
							throw new RuntimeException("Illegal trailing \\ in properties specification");
					} else if (c == quoteChar) {
						quoteChar = 0;
						continue;
					}
					sb.append(c);
				} else {
					if (c == '\"' || c == '\'')
						quoteChar = c;
					else if (name == null) {
						if (c == '=' || i == properties.length() - 1) {
							name = sb.toString().trim();
							sb.delete(0, sb.length());
						} else
							sb.append(c);
					} else {
						if (c == ',' || i == properties.length() - 1) {
							String value = sb.toString().trim();
							sb.delete(0, sb.length());
							prop.setProperty(name, value);
							name = null;
						} else
							sb.append(c);
					}
				}
			}

			if (name != null) {
				String value = sb.toString().trim();
				prop.setProperty(name, value);
			}
		}
		return prop;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.pool.connection.ManagedConnectionPoolMBean#getJdbcURL()
	 */
	public String getJdbcURL() {
		return jdbcURL;
	}

	/**
	 * @return
	 */
	public String getConnectionProperties() {
		return connectionProperties;
	}

	/**
	 * Return a list of JDBC driver classes to load, separated by
	 * colon/semicolon or comma.
	 * 
	 * @return a list of JDBC driver classes to load, separated by
	 *         colon/semicolon or comma.
	 */
	public String getJdbcDrivers() {
		return jdbcDrivers;
	}

	/**
	 * Enables the watchdog thread for the pool, registering this mbean as a
	 * listener
	 *  
	 */
	public synchronized void enableWatchDog() {
		if (pool == null)
			createPool(jdbcURL, connectionProperties, size);
		if (!pool.isWatchDogEnabled()) {
			pool.addListener(this);
			pool.setWatchDogEnabled(true);
		}
	}

	public synchronized void disableWatchDog() {
		if (pool == null)
			createPool(jdbcURL, connectionProperties, size);
		if (pool.isWatchDogEnabled()) {
			currentState = "Watchdog not enabled";
			pool.removeListener(this);
			pool.setWatchDogEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.ManagedWatchDog.Listener#checkFailed(java.lang.Object,
	 *      java.lang.Throwable)
	 */
	public void checkFailed(Object obj, Throwable e) {
		currentState =
			"Connection check failed ("
				+ e.getClass().getName()
				+ ": "
				+ e.getMessage()
				+ ")";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.ManagedWatchDog.Listener#checkImpossible(java.lang.Object,
	 *      org.sadun.util.watchdog.WatchDogException)
	 */
	public void checkImpossible(Object obj, WatchDogException e) {
		currentState = "Connection check impossible " + e.getMessage();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.ManagedWatchDog.Listener#checkOk(java.lang.Object)
	 */
	public void checkOk(Object obj) {
		currentState = "Connection check successful";
	}

}
