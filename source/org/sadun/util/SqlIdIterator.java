/*
 * Created on Sep 16, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sadun.util.pool.connection.ConnectionPool;

/**
 * An {@link org.sadun.util.IdIterator} based on an SQL statement
 * executed over a given Connection.
 * 
 * @author Cristiano Sadun
 */
public class SqlIdIterator implements IdIterator {

	private ConnectionPool connectionPool;
	private Connection conn;
	private Statement stmt;
	private ResultSet rs;
	private String current, previous, next;

	public SqlIdIterator(
		ConnectionPool connectionPool,
		String tableName,
		String idColumnName)
		throws SQLException {
		this(connectionPool, tableName, idColumnName, null, null);
	}

	public SqlIdIterator(
		ConnectionPool connectionPool,
		String tableName,
		String idColumnName,
		String upperLimit,
		String lowerLimit)
		throws SQLException {
		this.connectionPool = connectionPool;
		this.conn = connectionPool.getConnection();
		init(tableName, idColumnName, upperLimit, lowerLimit);
	}

	public SqlIdIterator(
		Connection conn,
		String tableName,
		String idColumnName)
		throws SQLException {
		this(conn, tableName, idColumnName, null, null);
	}

	public SqlIdIterator(
		Connection conn,
		String tableName,
		String idColumnName,
		String upperLimit,
		String lowerLimit)
		throws SQLException {
		this.conn = conn;
		init(tableName, idColumnName, upperLimit, lowerLimit);
	}

	private void init(
		String tableName,
		String idColumnName,
		String upperLimit,
		String lowerLimit)
		throws SQLException {

		String sql = "select " + idColumnName + " from " + tableName;
		int luc = (upperLimit == null ? 0 : 1) + (lowerLimit == null ? 0 : 1);
		if (luc > 0) {
			sql += " where ";
			if (upperLimit != null)
				sql += " "
					+ idColumnName
					+ " less than "
					+ toSqlString(upperLimit);
			if (lowerLimit != null) {
				if (upperLimit != null)
					sql += " and ";
				sql += " "
					+ idColumnName
					+ " greater than "
					+ toSqlString(lowerLimit);
			}
		}

		try {
			stmt =
				conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(sql);

			if (rs.next()) {
				current = rs.getString(1);
			}

		} catch (SQLException e) {
			close();
			throw e;
		}
	}

	/**
	 * @param upperLimit
	 * @return
	 */
	private static String toSqlString(String s) {
		int p = s.indexOf('\'');
		if (p == -1)
			return s;
		int p0 = 0;
		StringBuffer sb = new StringBuffer();
		synchronized(sb) {
		while (p != -1) {
			sb.append(s.substring(p0, p));
			sb.append("''");
			p0 = p + 1;
			p = s.indexOf('\'', p0);
		}
		sb.append(s.substring(p0, p));
		return sb.toString();
		}
	}

	/**
	 *  (non-Javadoc)
	 * @see org.sadun.util.IdIterator#hasNext(int)
	 */
	public boolean hasNext(int direction) {
		if (next == null)
			checkNext(direction, true);
		return next != null;

	}

	/**
	 * @return
	 */
	private void checkNext(int direction, boolean undo) {
		// assume the rs is on the 'current' row
		boolean valid;
		try {
			switch (direction) {

				case FORWARD :
					valid = rs.next();
					if (valid) {
						// There is a next id
						String s = rs.getString(1);
						if (undo) {
							next = s;
							rs.previous();
						} else {
							previous = current;
							current = s;
							next = null;
						}
					} else {
						if (undo)
							rs.previous();
						else {
							previous = current;
							current = null;
							next = null;
						}
					}

					break;
				case BACKWARD :
					valid = rs.previous();
					if (valid) {
						// There is a previous id
						String s = rs.getString(1);
						if (undo) {
							previous = s;
							rs.next();
						} else {
							next = current;
							current = s;
							previous = null;
						}
					} else {
						if (undo)
							rs.next();
						else {
							next = current;
							current = null;
							previous = null;
						}
					}
					break;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.IdIterator#getCurrentId()
	 */
	public String getCurrentId() {
		return current;
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.IdIterator#getNextId(int)
	 */
	public String getNextId(int direction) {
		checkNext(direction, false);
		return current;
	}

	/**
	 * Releases this {@link SqlIdIterator}'s result set and statement, but does not
	 * close the connection.
	 * <p>
	 * However, if the {@link SqlIdIterator} has been constructed via a {@link ConnectionPool} (see
	 * {@link #SqlIdIterator(ConnectionPool, String, String, String, String)} and 
	 * {@link #SqlIdIterator(ConnectionPool, String, String)}), the connection is released to the
	 * pool.
	 * 
	 */
	public void close() {
		try {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				if (connectionPool != null)
					connectionPool.releaseConnection(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
