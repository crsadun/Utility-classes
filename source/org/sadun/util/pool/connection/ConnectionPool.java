/*
 * Created on Aug 15, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.pool.connection;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.sadun.util.pool.ObjectPool;
import org.sadun.util.pool.ObjectPool.Factory;
import org.sadun.util.pool.ObjectPool.ObjectPoolException;
import org.sadun.util.watchdog.WatchDog;

/**
 * A pool of SQL connections. The connections must not be closed before being returned
 * to the pool, unless {@link #isTestBeforeAcquiring() #isTestBeforeAcquiring()} is
 * <b>true</b>. 
 * <p>
 * If that is the case, a test is performed on the connection before releasing it
 * to the client code (when it invokes {@link #getConnection() getConnection()}).
 * Note that this can impair performance if connections are requested often.
 * <p>
 * The pool can also be associated to a watchdog thread (by using {@link #setWatchDogEnabled(boolean) 
 * setWatchDogEnabled(()} and {@link #setWatchDogCheckPeriodTime(long) setWatchDogCheckPeriodTime()}, 
 * which periodically checks the state of the pool by picking up a connection and testing it.
 * <p>
 * On termination, the pool should be destroyed by invoking {@link #destroy(Object) destroy()}
 * which terminates the watchdog thread and closes the connections (see also 
 * {@link org.sadun.util.pool.ObjectPool#destroy(boolean) org.sadun.util.pool.ObjectPool.destroy()}).
 * 
 * 
 * @author Cristiano Sadun
 */
public class ConnectionPool {

	private final class ConnectionFactory implements Factory {
		
		/* (non-Javadoc)
		* @see org.sadun.util.pool.ObjectPool.Factory#create()
		*/
		public Object create() throws ObjectPoolException {
			try {
				assert jdbcUrl != null || dataSource != null;
				if (jdbcUrl != null) 
					return DriverManager.getConnection(
						jdbcUrl,
						connectionProperties);
				else 
					return dataSource.getConnection();
			} catch (SQLException e) {
				throw new ObjectPoolException(e);
			}
		}

		/* (non-Javadoc)
		 * @see org.sadun.util.pool.ObjectPool.Factory#getProducedClass()
		 */
		public Class getProducedClass() {
			return Connection.class;
		}
		
		

		/**
		 * If the passed object is a pooled connection, close it.
		 * 
		 * @see org.sadun.util.pool.ObjectPool.Factory#destroy(java.lang.Object)
		 */
		public void destroy(Object obj) throws ObjectPoolException {
			if (obj instanceof Connection)
				try {
					((Connection)obj).close();
				} catch (SQLException e) {
					throw new ObjectPoolException(e);
				}
		}

	}

	private String jdbcUrl;
	private Properties connectionProperties;
	private DataSource dataSource;
	private ObjectPool pool;
	private boolean testBeforeAcquiring;
	private int maxGetRetry;
	
	private ConnectionPoolWatchDog watchDog=null;
	private long watchDogCheckPeriodTime=300000L;
	private Set listeners = new HashSet();

	/**
	 * Create a pool of <i>n</i> connections to the given
	 * JDBC URL using the given connection properties.
	 * 
	 * @param n the size of the pool
	 * @param jdbcUrl the JDBC URL for the connection
	 * @param connectionProperties the connection properties to use
	 * @exception {@link ObjectPool.ObjectPoolException ObjectPoolException} if the connections cannot be created
	 */
	public ConnectionPool(
		int n,
		String jdbcUrl,
		Properties connectionProperties) {
		init(n, jdbcUrl, connectionProperties, null);
	}

	/**
	 * Create a pool of <i>n</i> connections to the given
	 * JDBC URL.
	 * @param n the size of the pool
	 * @param jdbcUrl the JDBC URL for the connection
	 * @exception {@link ObjectPool.ObjectPoolException ObjectPoolException} if the connections cannot be created
	 */
	public ConnectionPool(int n, String jdbcUrl) {
		this(n, jdbcUrl, new Properties());
	}
	
	/**
	 * Create a pool of <i>n</i> connections to the given
	 * DataSource.
	 * <p>
	 * Note that DataSource implementations often already pool connections.
	 * 
	 * @param n the size of the pool
	 * @param dataSource the data source to use
	 * @exception {@link ObjectPool.ObjectPoolException ObjectPoolException} if the connections cannot be created
	 */
	public ConnectionPool(int n, DataSource dataSource) {
		init(n, null, null, dataSource);
	}
	
	private void init(int n, String jdbcUrl, Properties connectionProperties, DataSource dataSource) {
		this.jdbcUrl = jdbcUrl;
		this.connectionProperties = connectionProperties;
		this.dataSource = dataSource;
		this.pool = new ObjectPool(n, new ConnectionFactory());
		this.maxGetRetry = Math.min(3, maxGetRetry);
	}

	/**
	 * Retrieves a connection from the pool.
	 * <p>
	 * If {@link #isTestBeforeAcquiring() isTestBeforeAcquiring()} is <b>true</b>
	 * {@link #doTest(Connection) doTest()} is invoked before actually returning the
	 * connection object. 
	 * 
	 * @return a connection, or <b>null</b> if a working connection cannot be
	 *          obtained.
	 */
	public Connection getConnection() throws ObjectPool.ObjectPoolException {
		int nTrials = 0;
		Connection conn = (Connection) pool.acquire();
		// It may be null if the thread has been interrupted
		if (conn == null) {
			/*DriverManager.println(
				"[Connection pool] thread "
					+ Thread.currentThread().getName()
					+ " has been interrupted - returning null connection");
			return null;
			*/
			//System.out.println(Thread.currentThread().getName()+" INTERRUPTED");
			throw new ObjectPool.ObjectPoolException("The thread "
					+ Thread.currentThread().getName()
					+ " accessing the connection pool "+toString()+" has been interrupted while was waiting for a connection to free");
		}
		do {

			if (testBeforeAcquiring) {
				try {
					doTest(conn);
					return conn;
				} catch (SQLException e) {
					conn = (Connection) pool.renew(conn);
				}
			} else
				return conn;
		} while (++nTrials < maxGetRetry);
		return null;
	}

	/**
	 * This method can be overridden to implement any desired connection
	 * test technique.
	 * <p>
	 * The test shall raise an SQLException if the connection is
	 * not valid.
	 * <p>
	 * The base implementation attempts to retrieve the database metadata
	 * and retrieve the catalogs. This might not be sufficient 
	 * for certain driver/RDBMs.
	 * 
	 * @exception SQLException if the connection is not valid
	 * @param conn the connection to test
	 */
	static void doTest(Connection conn) throws SQLException {
		ResultSet rs = null;
		try {
			assert conn != null;
			DatabaseMetaData dbmd = conn.getMetaData();
			rs = dbmd.getCatalogs();
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	/**
	 * Return the given connection to the pool.
	 * 
	 * @param conn the connection to return.
	 */
	public void releaseConnection(Connection conn) {
		synchronized (pool) {
			pool.release(conn);
			try {
				if (conn.isClosed())
					pool.renew(conn);
			} catch (SQLException e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				DriverManager.println(sw.toString());
				System.err.println(
					"Warning: org.sadun.util.ConnectionPool.releaseConnection() could not check wether a"
						+ File.separator
						+ "pooled connection had been closed or not (set the JDBC DriverManager"
						+ File.separator
						+ "logstream to see the causing SQLException)");

			}
		}
	}

	/**
	 * Return <b>true</b> if {@link #getConnection() getConnection()} tests
	 * the connection before returning it, else <b>false</b>.
	 * 
	 * @return <b>true</b> if {@link #getConnection() getConnection()} tests
	 * the connection before returning it, else <b>false</b>.
	 */
	public boolean isTestBeforeAcquiring() {
		return testBeforeAcquiring;
	}

	/**
	 * Set wether or not {@link #getConnection() getConnection()} tests
	 * the connection before returning it.
	 * 
	 * @param testBeforeAcquiring <b>true</b> if the connection has to be tested.
	 */
	public void setTestBeforeAcquiring(boolean testBeforeAcquiring) {
		this.testBeforeAcquiring = testBeforeAcquiring;
	}

	/**
	 * Return the maximum number of retry attempts before {@link #getConnection()
	 * getConnection()} stops and returns null (applies only if {@link #isTestBeforeAcquiring()
	 * isTestBeforeAcquiring()} is <b>true</b>).
	 * 
	 * @return the maximum number of retry attempts before {@link #getConnection()
	 * getConnection()} stops and returns null (applies only if {@link #isTestBeforeAcquiring()
	 * isTestBeforeAcquiring()} is <b>true</b>).
	 */
	public int getMaxGetRetry() {
		return maxGetRetry;
	}

	/**
	 * Set the maximum number of retry attempts before {@link #getConnection()
	 * getConnection()} stops and returns null (applies only if {@link #isTestBeforeAcquiring()
	 * isTestBeforeAcquiring()} is <b>true</b>).
	 * 
	 * @param maxGetRetry the maximum number of retry attempts before {@link #getConnection()
	 * getConnection()} stops and returns null (applies only if {@link #isTestBeforeAcquiring()
	 * isTestBeforeAcquiring()} is <b>true</b>).
	 */
	public void setMaxGetRetry(int maxGetRetry) {
		if (maxGetRetry <= 1)
			throw new IllegalArgumentException("maxGetRetry cannot be set to values less than 2");
		this.maxGetRetry = maxGetRetry;
	}
	
	
	/**
	 * Register a listener for the watchdog thread, if {@link #setWatchDogEnabled(boolean) enabled}. 
	 * 
	 * @param l the {@link WatchDog.Listener WatchDog.Listener} to register.
	 */
	public void addListener(WatchDog.Listener l) {
		synchronized(listeners) {
			listeners.add(l);
		}
	}
	
	/**
	 * Deregister a listener for the watchdog thread, if {@link #setWatchDogEnabled(boolean) enabled}.
	 * 
	 * @param l the {@link WatchDog.Listener WatchDog.Listener} to deregister.
	 */
	public void removeListener(WatchDog.Listener l) {
		synchronized(listeners) {
			listeners.remove(l);
		}
	}
	
	/**
	 * Enable or disable the {@link ConnectionPoolWatchDog watchdog} thread associated to this 
	 * connection pool, which periodically checks the state of the pool.
	 * 
	 * @param v if <b>true</b> enables and starts the {@link ConnectionPoolWatchDog watchdog},
	 *           else, disables it.
	 */
	public synchronized void setWatchDogEnabled(boolean v) {
		if (v) {
			// enable
			if (watchDog!=null) return;
			watchDog=new ConnectionPoolWatchDog(this, watchDogCheckPeriodTime);
			for(Iterator i=listeners.iterator();i.hasNext();) {
				watchDog.addListener((WatchDog.Listener)i.next());
			}
			watchDog.start();
		} else {
			// disable
			if (watchDog==null) return;
			watchDog.shutdown();
			watchDog=null;
		}
	}
	
	public boolean isWatchDogEnabled() {
		return watchDog!=null;
	}

	/**
	 * Return the amount of time the watchdog waits between each check (if the watchdog is enabled).
	 * 
	 * @return the amount of time the watchdog waits between each check (if the watchdog is enabled).
	 */
	public long getWatchDogCheckPeriodTime() {
		return watchDogCheckPeriodTime;
	}

	/**
	 * Set the amount of time the watchdog waits between each check (if the watchdog is enabled).
	 * 
	 * @param watchDogCheckPeriodTime the amount of time the watchdog waits between each check (if the watchdog is enabled).
	 */
	public void setWatchDogCheckPeriodTime(long watchDogCheckPeriodTime) {
		this.watchDogCheckPeriodTime = watchDogCheckPeriodTime;
		if (watchDog!=null) watchDog.setCheckPeriodTime(watchDogCheckPeriodTime);
	}

	/**
	 * Return the JDBC url to which this pool connects.
	 * 
	 * @return the JDBC url to which this pool connects.
	 */
	public String getJdbcUrl() {
		return jdbcUrl;
	}
	
	/**
	 * Destroy any resource associated to the pool, closing all the pooled connections.
	 * @param waitForReleasedObject if <b>true</b> the pool will wait until
	 *                               all connections are released before closing them.
	 */
	public void destroy(boolean waitForReleasedObject) {
		setWatchDogEnabled(false);
		pool.destroy(waitForReleasedObject);
	}
	
	/**
	 * Return the number of available connections in the pool
	 * @return the number of available connections in the pool
	 */
	public int getFreeCount() { return pool.getFreeCount(); }
	
	/**
	 * Return the number of used objects in the pool
	 * @return the number of used objects in the pool
	 */
	public int getUsedCount() { return pool.getUsedCount(); }

	 /**
	  * Return the size of the pool
	  * @return the size of the pool
	  */
	 public synchronized int getSize() { return pool.getSize(); }

}
