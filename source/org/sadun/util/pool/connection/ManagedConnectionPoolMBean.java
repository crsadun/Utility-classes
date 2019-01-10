/*
 * Created on Aug 27, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.pool.connection;

import javax.management.MBeanException;

/**
 * 
 * @author Cristiano Sadun
 */
public interface ManagedConnectionPoolMBean {

	/**
 	 * Return the number of available objects in the pool
	 * @return the number of available objects in the pool
	 */
	public int getFreeCount();

	/**
	 * Return the number of used objects in the pool
	 * @return the number of used objects in the pool
	 */
	public int getUsedCount();

	/**
	 * Return the size of the pool
	 * @return the size of the pool
	 */
	public int getSize();
	
	/**
	 * Set the size of the pool
	 * @param size the size of the pool
	 */
	public void setSize(int size) throws MBeanException;
	
	/**
	 * Return the jdbcURL accessed by this pool
	 * @return the jdbcURL accessed by this pool
	 */
	public String getJdbcURL();
	
	/**
	 * Set the jdbcURL accessed by this pool
	 * @param size the jdbcURL accessed by this pool
	 */
	public void setJdbcURL(String jdbcURL) throws MBeanException;

	/**
	 * Set the connection properties of the pool. The passed string is
     * interpreted as a sequence of name=value tokens separated by spaces
     * or commas. Values can be quoted if they contain spaces or commans,
     * and the quotes are discarded.
     * 
     * @param connectionProperties the String defining the properties
	 */
	public void setConnectionProperties(String connectionProperties) throws MBeanException;

	/**
	 * Return the connection properties of the pool (see {@link #setConnectionProperties(String)
	 * setConnectionProperties()} for details.
	 * 
	 * @return the connection properties of the pool
     */
	public String getConnectionProperties();
	
	/**
	 * Return a list of JDBC driver classes to load, 
	 * separated by colon/semicolon or comma.
	 * 
	 * @return a list of JDBC driver classes to load, 
	 * separated by colon/semicolon or comma.
	 */
	public String getJdbcDrivers();

	/**
	 * Set the list of JDBC driver classes to load, 
	 * separated by colon/semicolon or comma.
	 * 
	 * @param jdbcDrivers the list of JDBC driver classes to load, 
	 *                     separated by colon/semicolon or comma.
	 */
	public void setJdbcDrivers(String jdbcDrivers) throws MBeanException;

}
