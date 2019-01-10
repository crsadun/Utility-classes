/*
 * Created on Aug 25, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.pool.connection;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.sadun.util.ObjectLister;
import org.sadun.util.pool.ObjectPool;
import org.sadun.util.watchdog.WatchDog;

/**
 * A test class. The main method produces 100 threads asking resources
 * to a shared connection and running the same query, each delayed by
 * a random time interval.
 * 
 * @author Cristiano Sadun
 */
public class Test extends Thread {

	private volatile boolean shutdown;
	private ConnectionPool pool;

	public static void main(String args[])
		throws ClassNotFoundException, SQLException, FileNotFoundException {
			
		System.out.println("STARTING");	

		Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
		
		//DriverManager.setLogStream(new PrintStream(new FileOutputStream("c:\\temp\\log.txt")));
		
		/*
		String jdbcURL= "jdbc:microsoft:sqlserver://localhost:1433;User=sa;Password=;DatabaseName=ipm";
		ConnectionPool pool = new ConnectionPool(100, jdbcURL);
		*/

		String encodedDataSource =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<java version=\"1.4.1_03\" class=\"java.beans.XMLDecoder\">"
				+ " <object class=\"org.sadun.util.pool.connection.ConfigurableDataSource\">"
				+ "  <void property=\"connectionProperties\"> "
				+ "   <object class=\"java.util.Properties\"/> "
				+ "  </void> "
				+ "  <void property=\"jdbcUrl\">"
				+ "   <string>jdbc:microsoft:sqlserver://localhost:1433;User=sa;Password=;DatabaseName=ipm</string>"
				+ "  </void> "
				+ " </object> "
				+ "</java>";
				
		System.out.println("CREATING CONNECTION POOL");

		ConnectionPool pool =
			new ConnectionPool(
				10,
				new ConfigurableDataSource(
					new ByteArrayInputStream(encodedDataSource.getBytes())));
					
		pool.addListener(new WatchDog.BaseListener() {

			public void checkFailed(Object obj, Throwable e) {
				System.err.println("Connection pool failure detected: "+e.getMessage());
			}
			
		});
		
		pool.setWatchDogCheckPeriodTime(10);
		pool.setWatchDogEnabled(true);

		Test[] t = new Test[10];
		
		System.out.println("CREATING TEST THREADS");
		
		for (int i = 0; i < t.length; i++)
			t[i] = new Test(pool);
			
		System.out.println("STARTING TEST THREADS");
			
		for (int i = 0; i < t.length; i++)
			t[i].start();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("TERMINATING TEST THREADS");
		
		for (int i = 0; i < t.length; i++)
			t[i].shutdown();
			
		System.out.println("DESTROYING POOL");
		
		pool.destroy(true);
			
		
	}

	public Test(ConnectionPool pool) {
		this.pool = pool;
	}

	public void run() {
		this.shutdown = false;
		Random random = new Random();
		do {

			long sleepTime = 100 * (random.nextInt(10)) + 1;
			try {
				sleep(sleepTime);
			} catch (InterruptedException e1) {
				if (shutdown)
					break;
				else
					e1.printStackTrace();
			}

			Connection conn = null;
			try {
				conn = pool.getConnection();
			} catch (ObjectPool.ObjectPoolException e2) {
				if (shutdown) {
					System.out.println(e2.getMessage());
					break;
				} else
					e2.printStackTrace();
			}

			String sql = "select id, role from roles";

			Statement stmt = null;
			ResultSet rs = null;

			try {
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				List l = new ArrayList();
				while (rs.next()) {
					l.add(rs.getString(2));
				}
				String s =
					getName() + ": " + ObjectLister.getInstance().list(l);
				System.out.println(s);
				//conn.close();
			} catch (SQLException e) {
				e.printStackTrace(System.err);
			} finally {
				pool.releaseConnection(conn);
			}
		}
		while (!shutdown);
	}

	public void shutdown() {
		shutdown = true;
		interrupt();
	}
	
}
