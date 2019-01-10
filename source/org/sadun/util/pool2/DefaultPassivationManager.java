package org.sadun.util.pool2;

import java.util.HashMap;
import java.util.Map;

/**
 * The default passivation manager.
 * <p>
 * This passivation manager:
 * <ul>
 * <li> requires that the object be <tt>Serializable</tt> for considering them
 *      passivable.
 * <li> uses the {@link Configuration Configuration} {@link Configuration#getStorageDirectory() 
 *      storage directory} to store passivated instances.
 * <li> passivates instances when they have been inactive for over one minute.
 * </ul>
 * <p>
 * 
 * @author Cristiano Sadun
 */
public class DefaultPassivationManager extends BasePassivationManager {
	
	private static DefaultPassivationManager instance;
	private static long defaultPassivationThreshold=36000L;
	
	private class PooledObjectState {
		private long inserted;
		private long lastAccessed;
		
		public PooledObjectState(boolean acquired, long inserted) {
			this.inserted=inserted;
			this.lastAccessed=inserted;
		}
	}
	
	/**
	 * A Map to Maps of PooledObjects to States.
	 * There exists a map for each monitored pool, mapping PooledObjects to PooledObjectStates.
	 */
	private Map statesMap = new HashMap();

	private long passivationThreshold;
	
		/**
	 * Constructor for DefaultPassivationManager.
	 * @param name
	 */
	public DefaultPassivationManager() {
		super("Default passivation thread");
		commonInit();
	}


	/**
	 * Constructor for DefaultPassivationManager.
	 * @param name
	 */
	public DefaultPassivationManager(String name) {
		super(name);
		commonInit();
	}
	
	/**
	 * Constructor for DefaultPassivationManager.
	 * @param name
	 */
	public DefaultPassivationManager(String name, int sleepTime) {
		super(name, sleepTime);
		commonInit();
	}

	/**
	 * Constructor for DefaultPassivationManager.
	 * @param pool
	 * @param name
	 * @param sleepTime
	 */
	public DefaultPassivationManager(
		ObjectPool pool,
		String name,
		int sleepTime) {
		super(pool, name, sleepTime);
		commonInit();
	}
	
	/**
	 * Method commonInit.
	 */
	private void commonInit() {
		this.passivationThreshold=getDefaultPassivationThreshold();
	}
	
	protected void createState(ObjectPool pool, PooledObject obj) {
		Map poolStatesMap = (Map)statesMap.get(pool);
		if (poolStatesMap == null) {
			poolStatesMap =new HashMap();
			statesMap.put(pool, poolStatesMap);
		}
		poolStatesMap.put(obj, new PooledObjectState(pool.isAcquired(obj), System.currentTimeMillis()));
	}
	
	/**
	 * @see org.sadun.util.pool2.BasePassivationManager#removeState(ObjectPool, PooledObject)
	 */
	protected void removeState(ObjectPool pool, PooledObject obj) {
		Map poolStatesMap = (Map)statesMap.get(pool);
		if (poolStatesMap == null) return;
		poolStatesMap.remove(obj);
	}


	/**
	 * @see org.sadun.util.pool2.BasePassivationManager#handleInvoked(org.sadun.util.pool2.ObjectPool, org.sadun.util.pool2.PooledObject)
	 */
	protected void handleInvoked(ObjectPool pool, PooledObject obj) {
		// Reset the lastAccessed field object of the invoked object
		PooledObjectState state = (PooledObjectState)((Map)statesMap.get(pool)).get(obj);
		state.lastAccessed=System.currentTimeMillis();
	}

	/**
	 * @see org.sadun.util.pool2.BasePassivationManager#isToPassivate(org.sadun.util.pool2.ObjectPool, org.sadun.util.pool2.PooledObject, java.lang.Object) 
	 */
	protected boolean isToPassivate(ObjectPool pool, PooledObject obj, Object preparationResult) {
		PooledObjectState state = (PooledObjectState)((Map)statesMap.get(pool)).get(obj);
		long currentTime=System.currentTimeMillis();
		if (currentTime - state.lastAccessed > passivationThreshold) return true;
		if (pool.getLogStream()!=null)
			pool.getLogStream().println(obj+" accessed "+(currentTime - state.lastAccessed)+"ms ago, not to passivate");
		return false;
	}

	/**
	 * @see org.sadun.util.pool2.BasePassivationManager#handleAcquired(ObjectPool, PooledObject)
	 */
	protected void handleAcquired(ObjectPool pool, PooledObject obj) {
	}

	/**
	 * @see org.sadun.util.pool2.BasePassivationManager#handleReleased(ObjectPool, PooledObject)
	 */
	protected void handleReleased(ObjectPool pool, PooledObject obj) {
	}
	
	/**
	 * @see org.sadun.util.pool2.BasePassivationManager#prepareForPassivationCheck(ObjectPool)
	 */
	protected Object prepareForPassivationCheck(ObjectPool pool) {
		return null;
	}

	/**
	 * Returns the instance.
	 * @return DefaultPassivationManager
	 */
	public static DefaultPassivationManager getInstance() {
		if (instance==null) instance=new DefaultPassivationManager();
		return instance;
	}

	/**
	 * Returns the passivation threshold.
	 * @return long
	 */
	public long getPassivationThreshold() {
		return passivationThreshold;
	}

	/**
	 * Sets the passivation threshold.
	 * @param passivationThreshold The passivationThreshold to set
	 */
	public void setPassivationThreshold(long passivationThreshold) {
		this.passivationThreshold = passivationThreshold;
	}

	/**
	 * Returns the defaultPassivationThreshold.
	 * @return long
	 */
	public static long getDefaultPassivationThreshold() {
		return defaultPassivationThreshold;
	}

	/**
	 * Sets the defaultPassivationThreshold.
	 * @param defaultPassivationThreshold The defaultPassivationThreshold to set
	 */
	public static void setDefaultPassivationThreshold(long defaultPassivationThreshold) {
		DefaultPassivationManager.defaultPassivationThreshold =
			defaultPassivationThreshold;
	}

}
