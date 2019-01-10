/*
 * Created on Aug 27, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.pool.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.sadun.util.watchdog.WatchDog;
import org.sadun.util.watchdog.WatchDogException;

/**
 * A {@link WatchDog WatchDog} class for {@link ConnectionPool ConnectionPool}.
 * <p>
 * The watchdog contends with the other accessors for acquiring a connection from the pool.
 *  
 * @author Cristiano Sadun
 */
final class ConnectionPoolWatchDog extends WatchDog {
	
	private ConnectionPool connectionPool;

	/** 
	 * Ask for a connection and uses it to ask for database metadata.
	 * Return a SQLException if problems arise, else <b>null</b>.
	 * 
	 * @param obj ignored 
	 * @see org.sadun.util.pool.connection.WatchDog#doCheck()
	 */
	public Throwable doCheck(Object obj) throws WatchDogException {
		
		//System.err.println("CHECKING CONNECTION ("+connectionPool.getFreeCount()+" free connections)");
		
		Connection conn = connectionPool.getConnection();
		try {
			ConnectionPool.doTest(conn);
			return null;
		} catch (SQLException e) {
			return e;
		} finally {
			if (conn!=null)
				connectionPool.releaseConnection(conn);
		}
		
	}
	
	ConnectionPoolWatchDog(ConnectionPool connectionPool, long sleepTime) {
		super("Watchdog for connection pool on "+connectionPool.getJdbcUrl(), sleepTime);
		this.connectionPool = connectionPool;
	}

	ConnectionPoolWatchDog(ConnectionPool connectionPool) {
		super("Watchdog for connection pool on "+connectionPool.getJdbcUrl());
		this.connectionPool=connectionPool;
	}
	
	public void run() {
		DriverManager.println("[Connection Pool WatchDog] Started, sleep period is "+getCheckPeriodTime());
		super.run();
		DriverManager.println("[Connection Pool WatchDog] Terminated");
	}

	/**
	 * The target object is the connection pool.
	 * 
	 * @see org.sadun.util.pool.connection.WatchDog#getObjectToCheck()
	 */
	protected Object getObjectToCheck() {
		return connectionPool;
	}
}
