/*
 * Created on Aug 26, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.pool.connection;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.sadun.util.IndentedPrintWriter;

/**
 * A non-pooled data source (use {@link org.sadun.util.pool.connection.ConnectionPool ConnectionPool} to pool it
 * if needed) which can be externally configured via standard XML serialization.
 * <p>
 * An example of XML encoding follows:
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;java version="1.4.1_03" class="java.beans.XMLDecoder"&gt;
 *  &lt;object class="org.sadun.util.pool.connection.ConfigurableDataSource"&gt;
 *   &lt;void property="connectionProperties"&gt;
 *    &lt;void method="put"&gt;
 *     &lt;string&gt;fooProperty&lt;/string&gt;
 *     &lt;string&gt;fooValue&lt;/string&gt;
 *    &lt;/void&gt;
 *   &lt;/void&gt;
 *   &lt;void property="jdbcUrl"&gt;
 *    &lt;string&gt;jdbc:microsoft:sqlserver://localhost:1433;User=sa;Password=;DatabaseName=ipm&lt;/string&gt;
 *   &lt;/void&gt;
 *  &lt;/object&gt;
 * &lt;/java&gt;
 * </pre>
 * @author Cristiano Sadun
 */
public class ConfigurableDataSource implements DataSource, Serializable {

	private String jdbcUrl;
	private Properties connectionProperties;
	private PrintWriter logWriter;

	/**
	 * Create an unitialized ConfigurableDataSource.
	 * {@link #setJdbcUrl(String) setJdbcUrl()} must be invoked before invoking
	 * {@link #getConnection() getConnection()}.
	 *
	 */
	public ConfigurableDataSource() {
		connectionProperties=new Properties();
	}

	/**
	 * Create an datasource using the given JDBC url.
	 *
	 * @param jdbcUrl the JDBC url to use.
	 */
	public ConfigurableDataSource(String jdbcUrl) {
		this(jdbcUrl, new Properties());
	}

	/**
	 * Create an datasource using the given JDBC url and connection properties.
	 *
	 * @param jdbcUrl the JDBC url to use.
	 * @param connectionProperties the connection properties to use.
	 */
	public ConfigurableDataSource(
		String jdbcUrl,
		Properties connectionProperties) {
		this.jdbcUrl = jdbcUrl;
		this.connectionProperties = connectionProperties;
	}

	/**
	 * Create an datasource from an XMLEncoded stream.
	 *
	 * @param xml the XMLEncoded stream to create the datasource from.
	 */
	public ConfigurableDataSource(InputStream xml) {
		this(fromXMLStream(xml));
	}

	/**
	 * Create an datasource from an XMLEncoded file.
	 *
	 * @param xml the file containing the XMLEncoding of the datasource
	 */
	public ConfigurableDataSource(File xmlPath) throws FileNotFoundException {
		this(new FileInputStream(xmlPath));
	}

	private static ConfigurableDataSource fromXMLStream(InputStream xml) {
		try {
			XMLDecoder e = new XMLDecoder(new BufferedInputStream(xml));
			return (ConfigurableDataSource) e.readObject();
		} finally {
			try {
				xml.close();
			} catch (IOException e) {
				DriverManager.println(
					"[ConfigurableDataSource] Could not close XML configuration stream");
			}
		}
	}

	private ConfigurableDataSource(ConfigurableDataSource ds) {
		this.jdbcUrl = ds.jdbcUrl;
		this.connectionProperties = ds.connectionProperties;
	}

	/**
	 * See {@link javax.sql.DataSource DataSource}.
	 * @see javax.sql.DataSource#getConnection()
	 */
	public Connection getConnection() throws SQLException {
		if (jdbcUrl==null) throw new SQLException("Please set the data source JDBC url via setJdbcUrl()");
		return DriverManager.getConnection(jdbcUrl, connectionProperties);
	}

	/**
	 * See {@link javax.sql.DataSource DataSource}.
	 * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
	 */
	public Connection getConnection(String username, String password)
		throws SQLException {
		if (jdbcUrl==null) throw new SQLException("Please set the data source JDBC url via setJdbcUrl()");
		Properties p = new Properties();
		p.put("user", username);
		p.put("password", password);
		return DriverManager.getConnection(jdbcUrl, p);
	}

	/**
	 * See {@link javax.sql.DataSource DataSource}.
	 * @see javax.sql.DataSource#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws SQLException {
		return logWriter;
	}

	/**
	 * See {@link javax.sql.DataSource DataSource}.
	 * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter(PrintWriter out) throws SQLException {
		this.logWriter = out;
	}

	/**
	 * See {@link javax.sql.DataSource DataSource}.
	 * @see javax.sql.DataSource#setLoginTimeout(int)
	 */
	public void setLoginTimeout(int seconds) throws SQLException {
		DriverManager.setLoginTimeout(seconds);
	}

	/**
	 * See {@link javax.sql.DataSource DataSource}.
	 * @see javax.sql.DataSource#getLoginTimeout()
	 */
	public int getLoginTimeout() throws SQLException {
		return DriverManager.getLoginTimeout();
	}

	/**
	 * Return the connection properties for this data source.
	 * @return the connection properties for this data source.
	 */
	public Properties getConnectionProperties() {
		return connectionProperties;
	}

	/**
	 * Return the JDBC url for this data source.
	 * @return the JDBC url for this data source.
	 */
	public String getJdbcUrl() {
		return jdbcUrl;
	}

	/**
	 * Set the connection properties for this data source.
	 * @param connectionProperties
	 */
	public void setConnectionProperties(Properties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	/**
	 * Set the JDBC url for this data source.
	 * @param jdbcUrl
	 */
	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	/**
	 * Return a description of the data source.
	 */
	public String toString() {
		StringWriter sw = new StringWriter();
		IndentedPrintWriter pw = new IndentedPrintWriter(sw);
		pw.println(jdbcUrl);

		if (connectionProperties.size() > 0) {
			pw.setIndentation(5);
			connectionProperties.list(pw);
		}

		pw.close();
		return sw.toString();
	}

	/**
	 * Return <b>true</b> if the given object is a ConfigurableDataSource with
	 * identical JDBC url and connection properties, else <b>false</b>.
	 *
	 * @return <b>true</b> if the given object is a ConfigurableDataSource with
	 * identical JDBC url and connection properties, else <b>false</b>.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ConfigurableDataSource) {
			ConfigurableDataSource obj2 = (ConfigurableDataSource) obj;
			return obj2.jdbcUrl.equals(jdbcUrl)
				&& obj2.connectionProperties.equals(connectionProperties);
		}
		return false;
	}

	/**
	 * Return an hash code for the object.
	 * @return an hash code for the object.
	 */
	public int hashCode() {
		return jdbcUrl.hashCode();
	}

	public static void main(String args[]) throws Exception {

		XMLEncoder e = new XMLEncoder(
						  new BufferedOutputStream(
							  new FileOutputStream("/temp/dsTest.xml")));

		String jdbcURL= "jdbc:microsoft:sqlserver://localhost:1433;User=sa;Password=;DatabaseName=ipm";
				//Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");

		Properties prop=new Properties();
		prop.put("fooProperty", "fooValue");

		DataSource ds = new ConfigurableDataSource(jdbcURL, prop);

		 e.writeObject(ds);
		e.close();

		ConfigurableDataSource ds2 =
			new ConfigurableDataSource(new File("/temp/dsTest.xml"));
		System.out.println(ds2);

	}

}
