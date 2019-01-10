package org.sadun.util.pool2;

import java.io.File;

/**
 * An object definining the configuration for a given {@link ObjectPool object pool}.
 * <p>
 * 
 * @author Cristiano Sadun
 */
public class Configuration {

	private File storageDirectory;
	private int poolSize;
	private ObjectPool.PassivationManagerFactory passivationManagerFactory;

	/**
	 * Create a configuration object with the given parameters.
	 * 
	 * @param storageDirectory the directory where to store passivated object
	 * @param poolSize the desired size of the pool
	 * @param a {@link ObjectPool.PassivationManagerFactory passivation manager factory} to use
	 *         for determining the passivation policy.
	 */	
	public Configuration(File storageDirectory, int poolSize, ObjectPool.PassivationManagerFactory passivationManagerFactory) {
		this.storageDirectory = storageDirectory;
		this.poolSize = poolSize;
		this.passivationManagerFactory=passivationManagerFactory;
	}
	
	/**
	 * Create a configuration object with the given parameters but associating an
	 * {@link DefaultPassivationManager default passivation manager} to
	 * the configured pool.
	 * 
	 * @param storageDirectory the directory where to store passivated object
	 * @param poolSize the desired size of the pool
	 */	
	public Configuration(File storageDirectory, int poolSize) {
		this(storageDirectory, poolSize, new ObjectPool.DefaultPassivationManagerFactory());
	}
	
	/**
	 * Create a configuration object with the given parameters but usin the 
	 * system's temporary directory as storage directory and associating an
	 * {@link DefaultPassivationManager default passivation 
	 * manager} to the configured pool.
	 * 
	 * @param poolSize the desired size of the pool
	 */	
	public Configuration(int poolSize) {
		this(new File(System.getProperty("java.io.tmpdir")), poolSize, new ObjectPool.DefaultPassivationManagerFactory());
	}

	/**
	 * Returns the storageDirectory.
	 * @return File
	 */
	public File getStorageDirectory() {
		return storageDirectory;
	}

	/**
	 * Sets the storageDirectory.
	 * @param storageDirectory The storageDirectory to set
	 */
	public void setStorageDirectory(File storageDirectory) {
		this.storageDirectory = storageDirectory;
	}

	/**
	 * Returns the poolSize.
	 * @return int
	 */
	public int getPoolSize() {
		return poolSize;
	}

	/**
	 * Sets the poolSize.
	 * @param poolSize The poolSize to set
	 */
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	/**
	 * Returns the passivationManagerFactory.
	 * @return ExtendedObjectPool.PassivationManagerFactory
	 */
	public ObjectPool.PassivationManagerFactory getPassivationManagerFactory() {
		return passivationManagerFactory;
	}

	/**
	 * Sets the passivationManagerFactory.
	 * @param passivationManagerFactory The passivationManagerFactory to set
	 */
	public void setPassivationManagerFactory(
		ObjectPool.PassivationManagerFactory passivationThreadFactory) {
		this.passivationManagerFactory = passivationThreadFactory;
	}

}
