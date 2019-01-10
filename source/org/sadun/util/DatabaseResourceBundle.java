/*
 * Created on Sep 8, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.sadun.util.pool.connection.ConfigurableDataSource;

/**
 * A resource bundle which reads resources from a database.
 * <p> 
 * The default resource table is defined by the constant {@link #RESOURCETABLE_DEFAULT_NAME 
 * RESOURCETABLE_DEFAULT_NAME}.
 * <p>
 * A resource table must have the fields (KEY_NAME: VARCHAR,  LOCALE: VARCHAR, VALUE: VARCHAR)
 * which are queried when looking up for a key.
 * <p>
 * Different resource tables may be employed for different keys, by using the {@link #setResourceTable(Pattern, String)
 * setResourceTable()} methods, which associate a regular expression to a table name, and make so that 
 * a key which matches the regular expression queries the given name.
 * <p>
 * For example,
 * <pre> 
 *  setResourceTable("STEP1DIALOG_*", "STEP1_DIALOG_RESOURCES");
 * </pre>
 * will associate the table STEP1_DIALOG_RESOURCES to any key matching the STEP1DIALOG_* regular 
 * expression.
 * <p>
 * The bundle typically pre-loads and caches the keys (via its {@link #setAutoAdjustCache(boolean) 
 * autoAdjustCache}, {@link #setPreLoad(boolean) preLoad} and {@link #setCacheSize(int) cacheSize}
 * propreties) to avoid accessing the database for each key lookup.
 * 
 * @author Cristiano Sadun
 * @version 2.0
 */
public class DatabaseResourceBundle extends ResourceBundle {

	/**
	 * Define the default name of the default resource table to "APPLICATION_RESOURCES".
	 */
	public static final String RESOURCETABLE_DEFAULT_NAME =
		"APPLICATION_RESOURCES";

	/**
	 * Define the default key cache size to 100
	 */
	public static final int DEFAULT_CACHE_SIZE = 100;

	private DataSource ds;
	private String defaultResourceTable;
	private Map tablesByPattern = new HashMap();
	private boolean preLoad = true;
	private boolean autoAdjustCache = false;
	private int cacheSize;
	private Cache keysCache;
	private Locale localeToUse; 

	/**
	 * Create a database resource bundle for a given locale which will look for resourcess using the
	 * given default table name (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of the given size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param ds the data source to connect to. A {@link ConfigurableDataSource ConfigurableDataSource}
	 *            can be used to wrap a JDBC URL if necessary
	 * @param defaultResourceTable the name of the table where to look, by default, for keys. The table
	 *         must have a specific layout (see class comment) and is used unless a specific table
	 *         name is set for a specific key regular expression pattern by {@link #setResourceTable(String, String) 
	 *         setResourceTable()}.
	 * @param the locale to use
	 * @param cacheSize the size of the in-core key cache
	 */
	public DatabaseResourceBundle(
		DataSource ds,
		String defaultResourceTable,
		int cacheSize,
		Locale locale) {
		this.ds = ds;
		this.defaultResourceTable = defaultResourceTable;
		this.localeToUse=locale;
		createCache(cacheSize);
	}
	
	/**
	 * Create a database resource bundle which will look for resourcess using the
	 * given default table name (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of the given size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param ds the data source to connect to. A {@link ConfigurableDataSource ConfigurableDataSource}
	 *            can be used to wrap a JDBC URL if necessary
	 * @param defaultResourceTable the name of the table where to look, by default, for keys. The table
	 *         must have a specific layout (see class comment) and is used unless a specific table
	 *         name is set for a specific key regular expression pattern by {@link #setResourceTable(String, String) 
	 *         setResourceTable()}.
	 * @param cacheSize the size of the in-core key cache
	 */
	public DatabaseResourceBundle(
		DataSource ds,
		String defaultResourceTable,
		int cacheSize) {
		this(ds, defaultResourceTable, cacheSize, Locale.getDefault());
	}
	

	/**
	 * Create a database resource bundle which will look for resourcess using the
	 * given default table name (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of the {@link #DEFAULT_CACHE_SIZE DEFAULT_CACHE_SIZE} 
	 * size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param ds the data source to connect to. A {@link ConfigurableDataSource ConfigurableDataSource}
	 *            can be used to wrap a JDBC URL if necessary
	 * @param defaultResourceTable the name of the table where to look, by default, for keys. The table
	 *         must have a specific layout (see class comment) and is used unless a specific table
	 *         name is set for a specific key regular expression pattern by {@link #setResourceTable(String, String) 
	 *         setResourceTable()}.
	 */
	public DatabaseResourceBundle(DataSource ds, String defaultResourceTable) {
		this(ds, defaultResourceTable, DEFAULT_CACHE_SIZE);
	}
	
	/**
	 * Create a database resource bundle for a given locale which will look for resourcess using the
	 * given default table name (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of the {@link #DEFAULT_CACHE_SIZE DEFAULT_CACHE_SIZE} 
	 * size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param ds the data source to connect to. A {@link ConfigurableDataSource ConfigurableDataSource}
	 *            can be used to wrap a JDBC URL if necessary
	 * @param defaultResourceTable the name of the table where to look, by default, for keys. The table
	 *         must have a specific layout (see class comment) and is used unless a specific table
	 *         name is set for a specific key regular expression pattern by {@link #setResourceTable(String, String) 
	 *         setResourceTable()}.
	 * @param the locale to use
	 */
	public DatabaseResourceBundle(DataSource ds, String defaultResourceTable, Locale locale) {
		this(ds, defaultResourceTable, DEFAULT_CACHE_SIZE, locale);
	}

	/**
	 * Create a database resource bundle which will look for resources using the
	 * given {@link #RESOURCETABLE_DEFAULT_NAME RESOURCETABLE_DEFAULT_NAME} 
	 * (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of given size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param ds the data source to connect to. A {@link ConfigurableDataSource ConfigurableDataSource}
	 *            can be used to wrap a JDBC URL if necessary
	 * @param cacheSize the size of the in-core key cache
	 */
	public DatabaseResourceBundle(DataSource ds, int cacheSize) {
		this(ds, RESOURCETABLE_DEFAULT_NAME, DEFAULT_CACHE_SIZE);
	}
	
	/**
	 * Create a database resource bundle for a given locale which will look for resourcess using the
	 * given {@link #RESOURCETABLE_DEFAULT_NAME RESOURCETABLE_DEFAULT_NAME} 
	 * (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of given size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param ds the data source to connect to. A {@link ConfigurableDataSource ConfigurableDataSource}
	 *            can be used to wrap a JDBC URL if necessary
	 * @param cacheSize the size of the in-core key cache
	 * @param the locale to use
	 */
	public DatabaseResourceBundle(DataSource ds, int cacheSize, Locale locale) {
		this(ds, RESOURCETABLE_DEFAULT_NAME, DEFAULT_CACHE_SIZE, locale);
	}

	/**
	 * Create a database resource bundle which will look for resourcess using the
	 * given {@link #RESOURCETABLE_DEFAULT_NAME RESOURCETABLE_DEFAULT_NAME} 
	 * (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of the {@link #DEFAULT_CACHE_SIZE DEFAULT_CACHE_SIZE} 
	 * size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param ds the data source to connect to. A {@link ConfigurableDataSource ConfigurableDataSource}
	 *            can be used to wrap a JDBC URL if necessary
	 */
	public DatabaseResourceBundle(DataSource ds) {
		this(ds, RESOURCETABLE_DEFAULT_NAME, DEFAULT_CACHE_SIZE);
	}
	
	/**
	 * Create a database resource bundle for a given locale which will look for resourcess using the
	 * given {@link #RESOURCETABLE_DEFAULT_NAME RESOURCETABLE_DEFAULT_NAME} 
	 * (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of the {@link #DEFAULT_CACHE_SIZE DEFAULT_CACHE_SIZE} 
	 * size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param ds the data source to connect to. A {@link ConfigurableDataSource ConfigurableDataSource}
	 *            can be used to wrap a JDBC URL if necessary
	 * @param the locale to use
	 */
	public DatabaseResourceBundle(DataSource ds, Locale locale) {
		this(ds, RESOURCETABLE_DEFAULT_NAME, DEFAULT_CACHE_SIZE, locale);
	}

	/**
	 * Create a database resource bundle for a given locale which will look for resourcess using the
	 * given default table name (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of the given size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param jdbcUrl the JDBC URL of the Database to connect to
	 * @param defaultResourceTable the name of the table where to look, by default, for keys. The table
	 *         must have a specific layout (see class comment) and is used unless a specific table
	 *         name is set for a specific key regular expression pattern by {@link #setResourceTable(String, String) 
	 *         setResourceTable()}.
	 * @param cacheSize the size of the in-core key cache
	 * @param the locale to use 
	 */
	public DatabaseResourceBundle(
		String jdbcUrl,
		String defaultResourceTable,
		int cacheSize, 
		Locale locale) {
		this.ds = new ConfigurableDataSource(jdbcUrl);
		this.defaultResourceTable = defaultResourceTable;
		this.localeToUse=locale;
		createCache(cacheSize);
	}
	
	/**
	 * Create a database resource bundle which will look for resourcess using the
	 * given default table name (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of the given size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param jdbcUrl the JDBC URL of the Database to connect to
	 * @param defaultResourceTable the name of the table where to look, by default, for keys. The table
	 *         must have a specific layout (see class comment) and is used unless a specific table
	 *         name is set for a specific key regular expression pattern by {@link #setResourceTable(String, String) 
	 *         setResourceTable()}.
	 * @param cacheSize the size of the in-core key cache
	 */
	public DatabaseResourceBundle(
		String jdbcUrl,
		String defaultResourceTable,
		int cacheSize) {
		this(jdbcUrl, defaultResourceTable, cacheSize, Locale.getDefault());
	}

	/**
	 * Create a database resource bundle which will look for resourcess using the
	 * given default table name (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of the {@link #DEFAULT_CACHE_SIZE DEFAULT_CACHE_SIZE} 
	 * size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param jdbcUrl the JDBC URL of the Database to connect to
	 * @param defaultResourceTable the name of the table where to look, by default, for keys. The table
	 *         must have a specific layout (see class comment) and is used unless a specific table
	 *         name is set for a specific key regular expression pattern by {@link #setResourceTable(String, String) 
	 *         setResourceTable()}.
	 */
	public DatabaseResourceBundle(
		String jdbcUrl,
		String defaultResourceTable) {
		this(jdbcUrl, defaultResourceTable, DEFAULT_CACHE_SIZE);
	}
	
	/**
	 * Create a database resource bundle for a given locale which will look for resourcess using the
	 * given default table name (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of the {@link #DEFAULT_CACHE_SIZE DEFAULT_CACHE_SIZE} 
	 * size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param jdbcUrl the JDBC URL of the Database to connect to
	 * @param defaultResourceTable the name of the table where to look, by default, for keys. The table
	 *         must have a specific layout (see class comment) and is used unless a specific table
	 *         name is set for a specific key regular expression pattern by {@link #setResourceTable(String, String) 
	 *         setResourceTable()}.
	 * @param the locale to use
	 */
	public DatabaseResourceBundle(
		String jdbcUrl,
		String defaultResourceTable, Locale locale) {
		this(jdbcUrl, defaultResourceTable, DEFAULT_CACHE_SIZE, locale);
	}

	/**
	 * Create a database resource bundle which will look for resources using the
	 * given {@link #RESOURCETABLE_DEFAULT_NAME RESOURCETABLE_DEFAULT_NAME} 
	 * (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of given size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param jdbcUrl the JDBC URL of the Database to connect to
	 * @param cacheSize the size of the in-core key cache
	 */
	public DatabaseResourceBundle(String jdbcUrl, int cacheSize) {
		this(jdbcUrl, RESOURCETABLE_DEFAULT_NAME, DEFAULT_CACHE_SIZE);
	}
	
	/**
	 * Create a database resource bundle for a given locale which will look for resources using the
	 * given {@link #RESOURCETABLE_DEFAULT_NAME RESOURCETABLE_DEFAULT_NAME} 
	 * (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of given size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param jdbcUrl the JDBC URL of the Database to connect to
	 * @param cacheSize the size of the in-core key cache
	 * @param the locale to use
	 */
	public DatabaseResourceBundle(String jdbcUrl, int cacheSize, Locale locale) {
		this(jdbcUrl, RESOURCETABLE_DEFAULT_NAME, DEFAULT_CACHE_SIZE, locale);
	}

	/**
	 * Create a database resource bundle which will look for resources using the
	 * given {@link #RESOURCETABLE_DEFAULT_NAME RESOURCETABLE_DEFAULT_NAME} 
	 * (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of the {@link #DEFAULT_CACHE_SIZE DEFAULT_CACHE_SIZE} 
	 * size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param jdbcUrl the JDBC URL of the Database to connect to
	 */
	public DatabaseResourceBundle(String jdbcUrl) {
		this(jdbcUrl, RESOURCETABLE_DEFAULT_NAME, DEFAULT_CACHE_SIZE);
	}
	
	/**
	 * Create a database resource bundle for a given locale which will look for resourcess using the
	 * given {@link #RESOURCETABLE_DEFAULT_NAME RESOURCETABLE_DEFAULT_NAME} 
	 * (see {@link #setResourceTable(String, String) setResourceTable()})
	 * found at the given jdbc URL, using a cache of the {@link #DEFAULT_CACHE_SIZE DEFAULT_CACHE_SIZE} 
	 * size.
	 * <p>
	 * The bundle will pre-load the keys.
	 * 
	 * @param jdbcUrl the JDBC URL of the Database to connect to
	 * @param the locale to use
	 */
	public DatabaseResourceBundle(String jdbcUrl, Locale locale) {
		this(jdbcUrl, RESOURCETABLE_DEFAULT_NAME, DEFAULT_CACHE_SIZE, locale);
	}

	/**
	 * Clear the cache. The next invocation will reconnect and re-read to the database. 
	 */
	public void clearCache() {
		synchronized (this) {
			this.keysCache = new Cache(cacheSize);
		}
	}

	private void createCache(int cacheSize) {
		this.cacheSize = cacheSize;
		this.keysCache = new Cache(cacheSize);
	}

	/**
	 * Set the resource table associated to keys matching the given regular expression.
	 * <p>
	 * When queried with a key, the bundle will match the key with the regular
	 * expression, and use the given table rather than the default table if
	 * there is a match.
	 *  
	 * @param keyRegExpPattern the regular expression to match
	 * @param tableName the table where to lookup resources whose key matches the given regular 
	 *         expression
	 */
	public void setResourceTable(String keyRegExpPattern, String tableName) {
		setResourceTable(Pattern.compile(keyRegExpPattern), tableName);
	}

	/**
	 * Set the resource table associated to keys matching the given regular expression.
	 * <p>
	 * When queried with a key, the bundle will match the key with the regular
	 * expression, and use the given table rather than the default table if
	 * there is a match.
	 *  
	 * @param keyPattern the regular expression pattern to match
	 * @param tableName the table where to lookup resources whose key matches the given regular 
	 *         expression
	 */
	public void setResourceTable(Pattern keyPattern, String tableName) {
		tablesByPattern.put(keyPattern, tableName);
	}

	/**
	 * Finds the table matching the key (or uses the default table) and
	 * expects it to have a (KEY: VARCHAR[50],  LOCALE: VARCHAR[5], VALUE: VARCHAR[100])
	 * structure. 
	 * 
	 * (non-Javadoc)
	 * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
	 */
	protected Object handleGetObject(String key) {

		Object result;
		if ((result = keysCache.get(key)) != null)
			return result;

		String tableToQuery = defaultResourceTable;
		for (Iterator i = tablesByPattern.keySet().iterator(); i.hasNext();) {
			Pattern pattern = (Pattern) i.next();
			Matcher matcher = pattern.matcher(key);
			if (matcher.matches()) {
				tableToQuery = (String) tablesByPattern.get(pattern);
				break;
			}
		}

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = ds.getConnection();
			stmt = conn.createStatement();

			if (autoAdjustCache) {
				rs =
					stmt.executeQuery(
						"select count(*) from "
							+ tableToQuery
							+ " where locale = '"
							+ localeToUse.toString()
							+ "'");
				rs.next();
				int requiredCacheSize = Math.max(2, rs.getInt(1));
				rs.close();
				if (this.cacheSize != requiredCacheSize) {
					createCache(requiredCacheSize);
				}
			}

			if (preLoad) { // Go loading the entire locale
				rs =
					stmt.executeQuery(
						"select key_name, value from "
							+ tableToQuery
							+ " where locale= '"
							+ localeToUse.toString()
							+ "'");
				while (rs.next()) {
					String readKey = rs.getString(1);
					String readValue = rs.getString(2);
					keysCache.put(readKey, readValue);
					if (readKey.equals(key))
						result = readValue;
				}

			} else {
				rs =
					stmt.executeQuery(
						"select value from "
							+ tableToQuery
							+ " where key_name='"
							+ toSqlString(key)
							+ "' and locale= '"
							+ localeToUse.toString()
							+ "'");
				String readValue = rs.getString(1);
				keysCache.put(key, readValue);
				result = readValue;
			}
		} catch (SQLException e) {
			throw new MissingResourceException(
				"Could not get value for key '"
					+ key
					+ "' due to an SQL exception: "
					+ e.getMessage(),
				getClass().getName(),
				key);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * @param key
	 * @return
	 */
	private String toSqlString(String key) {
		return key.replaceAll("'", "''");
	}

	/** 
	 * Connect to all the resource tables and finds out the available keys.
	 * <p>
	 * This method can be time-consuming.
	 * @see java.util.ResourceBundle#getKeys()
	 */
	public Enumeration getKeys() {
		return getKeys(Locale.getDefault());
	}
	
	/** 
	 * Connect to all the resource tables and finds out the available keys for
	 * the given locale
	 * <p>
	 * This method can be time-consuming.
	 * @see java.util.ResourceBundle#getKeys()
	 */
	public Enumeration getKeys(Locale locale) {
	    List l = new LinkedList();
		Set readTables = new HashSet();
		addKeys(l, defaultResourceTable, locale);
		readTables.add(defaultResourceTable.toLowerCase());
		for (Iterator i = tablesByPattern.keySet().iterator(); i.hasNext();) {
			String table = (String) i.next();
			if (readTables.contains(table.toLowerCase()))
				continue;
			addKeys(l, table, locale);
		}
		return new ListEnumeration(l);
	}

	/**
	 * @param l
	 * @param locale
	 * @param defaultResourceTable2
	 */
	private void addKeys(List l, String table, Locale locale) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = ds.getConnection();
			stmt = conn.createStatement();

			String sql =
				"select key_name from "
					+ table
					+ " where locale = '"
					+ locale.toString()
					+ "'";

			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				l.add(rs.getString(1));
			}
		} catch (SQLException e) {
			throw new RuntimeException(
				"Could not read keys from resource table " + table,
				e);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	* Return the value of the auto-adjust-cache property (see {@link #setAutoAdjustCache(boolean) 
	* setAutoAdjustCache()}.
	* 
	* @return the value of the auto-adjust-cache property. 
	*/
	public boolean isAutoAdjustCache() {
		return autoAdjustCache;
	}

	/**
	 * Set the auto-adjust-cache property. Clear the cache if necessary. 
	 * 
	 * @param autoAdjustCache if <b>true</b>, the cache will adjust itself to the
	 *                         size necessary to hold all the keys for one resource table.
	 */
	public void setAutoAdjustCache(boolean autoAdjustCache) {
		if (autoAdjustCache != this.autoAdjustCache)
			clearCache();
		this.autoAdjustCache = autoAdjustCache;
	}

	/**
	 * Retur the current size of the cache.
	 * 
	 * @return the current size of the cache.
	 */
	public int getCacheSize() {
		return cacheSize;
	}

	/**
	 * Set the current size of the cache. The auto-adjust-cache property (see {@link #setAutoAdjustCache(boolean)
	 * is automatically set to <b>false</b>. Clear the cache if necessary.
	 * 
	 * @param cacheSize the current size of the cache.
	 */
	public void setCacheSize(int cacheSize) {
		if (cacheSize != this.cacheSize) {
			setAutoAdjustCache(false);
			createCache(cacheSize);
		}
	}

	/**
	 * Return the JDBC url of the database containing the resources.
	 * @return the JDBC url of the database containing the resources.
	 */
	public String getJdbcUrl() {
		if (ds instanceof ConfigurableDataSource)
			return ((ConfigurableDataSource) ds).getJdbcUrl();
		throw new IllegalStateException("The bundle has been constructed using a data source");
	}

	/**
	 * Set the JDBC url of the database containing the resources. Clear the cache if necessary.
	 * @param jdbcUrl the JDBC url of the database containing the resources.
	 */
	public void setJdbcUrl(String jdbcUrl) {
		if (!(ds instanceof ConfigurableDataSource))
			throw new IllegalStateException("The bundle has been constructed using a data source");

		if (((ConfigurableDataSource) ds).getJdbcUrl().equals(jdbcUrl))
			return;
		clearCache();
		this.ds = new ConfigurableDataSource(jdbcUrl);
	}

	/**
	 * Return the value of the pre-load property (see {@link #setPreLoad(boolean) setPreLoad()}).
	 * @return the value of the pre-load property.
	 */
	public boolean isPreLoad() {
		return preLoad;
	}

	/**
	 * Set the value of the pre-load property.
	 * @param preLoad if <b>true</b>, the bundle will pre-load all the key/value pairs on
	 * the first opportunity.
	 */
	public void setPreLoad(boolean preLoad) {
		this.preLoad = preLoad;
	}

	public static void main(String[] args) {
		String jdbcURL =
			"jdbc:microsoft:sqlserver://localhost:1433;User=sa;Password=;DatabaseName=ipm";
		ResourceBundle rb = new DatabaseResourceBundle(jdbcURL);
		ObjectLister.getInstance().println(rb.getKeys());
		System.out.println(rb.getString("name"));
	}

}
