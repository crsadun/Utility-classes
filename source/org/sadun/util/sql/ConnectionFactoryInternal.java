package org.sadun.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This class is for internal use and does not need instantiation.
 * 
 * @author Cristiano Sadun
 */
class ConnectionFactoryInternal {
	
	ConnectionFactoryInternal() {
		
	}

	 public Connection getConnection(String jdbcUrl, Properties p) throws SQLException {
		return DriverManager.getConnection(jdbcUrl, p);
	}

	 public Connection getConnection(String jdbcUrl) throws SQLException {
		//DriverManager.setLogStream(System.out);
		
		System.out.println(getClass().getClassLoader());
		
	 	return DriverManager.getConnection(jdbcUrl);
	}
	
	 public Connection getConnection(String jdbcUrl, String user, String pwd) throws SQLException {
		return DriverManager.getConnection(jdbcUrl, user, pwd);
	}
	
}
