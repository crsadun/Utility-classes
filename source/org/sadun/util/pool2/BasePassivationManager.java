package org.sadun.util.pool2;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.sadun.util.Terminable;

/**
 * A Passivation thread is associated to one or more {@link ObjectPool ObjectPool}s
 * and triggers the activation/passivation state of a Pooled object.
 * <p>
 * This class is abstract. Each subclass may define alternative passivation strategies.
 * 
 * @author Cristiano Sadun
 *
 */
public abstract class BasePassivationManager
	extends Thread
	implements Terminable {
		
	private volatile boolean shutdown = false;
	private Iterator currentPool;
	protected PrintStream logStream;
	
	/**
	 * The set of pools monitored by this thread (contained org.sadun.util.pool2.ObjectPool).
	 */
	protected Set monitoredPools = new HashSet();
	
	/**
	 * The sleep time 
	 */
	protected int sleepTime;
	
	/**
	 * Controls which pools are checked on a service cycle
	 */
	protected boolean rotatePools=false;
	protected int rotationRate=1;
	

	/**
	 * Build a passivation thread monitoring the given pool, with the given thread name
	 * and having the given sleep time.
	 * 
	 * @param pool the pool to monitor. Additional pools can be monitored by the same
	 *              passivation thread by using {@link #addPool(org.sadun.util.pool2.ObjectPool) 
	 *              addPool()}
	 * @param name the thread name
	 */
	protected BasePassivationManager(
		ObjectPool pool,
		String name,
		int sleepTime) {
		super(name);
		this.sleepTime = sleepTime;
		addPool(pool);
	}

	protected BasePassivationManager(String name, int sleepTime) {
		super(name);
		this.sleepTime = sleepTime;
	}

	protected BasePassivationManager(String name) {
		this(name, 3600);
	}
	
	/**
	 * Add an {@link ObjectPool ObjectPool} to monitor.
	 * @param pool the pool to add
	 */
	public synchronized void addPool(ObjectPool pool) {
		monitoredPools.add(pool);
		Object [] objs = pool.createObjectsArray();

		if (objs.length>0)		
			if ( !(canPassivate(((PooledObject)objs[0])._getOriginal().getClass()))) {
				if (logStream != null) {
					logStream.println("Warning: class \""+objs.getClass().getComponentType()+"\" is not passivable.");
				}
			}
		
		// Create the initial state map
		for(int i=0;i<objs.length;i++) {
			PooledObject obj = (PooledObject)objs[i];
			createState(pool, obj);
		}
		if (logStream!=null)
			logStream.println("Pool \""+pool.getName()+"\" managed by "+getName());
	}

	/**
	 * Method canPassivate.
	 * @param class
	 * @return boolean
	 */
	public boolean canPassivate(Class cls) {
		return Serializable.class.isAssignableFrom(cls);
	}


	/**
	 * Used by a subclass to define an state information for a certain object belonging to
	 * a certain pool when a pool is added to the monitored set
	 * @param obj the object 
	 * @param pool the pool the object belongs to
	 */
	protected abstract void createState(ObjectPool pool, PooledObject obj);
	
	/**
	 * Used by a subclass to remove the state information for a certain object belonging to
	 * a certain pool when a pool is removed from the monitored set
	 * @param obj the object 
	 * @param pool the pool the object belongs to
	 */
	protected abstract void removeState(ObjectPool pool, PooledObject obj);


	/**
	 * Remove an {@link ObjectPool ObjectPool} from monitoring.
	 * @param pool the pool to remove
	 */
	public void removePool(ObjectPool pool) {
		monitoredPools.remove(pool);
	}

	/**
	 * Count the monitored pools
	 * @param pool the pool to add
	 */
	public int countPools() {
		return monitoredPools.size();
	}

	/**
	 * The passivation thread main loop.
	 * <p>
	 * The thread awakes any {@link #sleepTime sleepTime} milliseconds
	 * and runs a service cycle, by verifying the passivability of all
	 * the objects in all or some of the monitored pools (see {@link #rotatePools 
	 * rotatePools}).
	 * <p>
	 * The method {@link #isToPassivate(org.sadun.util.pool2.ObjectPool, org.sadun.util.pool2.PooledObject, java.lang.Object) isToPassivate()}
	 * is invoked on every object. If the method returns <b>true</b> the corresponding object
	 * is passivated.
	 */
	public final void run() {
		if (logStream!=null)
			logStream.println(getName()+" started");
		while (!shutdown) {
			doPassivationCheck();
			try {
				sleep(sleepTime);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		if (logStream!=null)
			logStream.println(getName()+" terminated");
	}

	/**
	 * Method doPassivationCheck. If pools are rotated, only a subset are actually
	 * checked on any active cycle. Else, all pools are checked.
	 */
	private void doPassivationCheck() {
		if (rotatePools) {
			for(int i=0;i<rotationRate;i++) {
				if (currentPool==null) currentPool=monitoredPools.iterator();
				else if (! currentPool.hasNext()) currentPool=monitoredPools.iterator();
				
				ObjectPool pool = (ObjectPool)currentPool.next();
				doPassivationCheck(pool);
			}
		} else {
			for(currentPool=monitoredPools.iterator(); currentPool.hasNext();) {
				ObjectPool pool = (ObjectPool)currentPool.next();
				doPassivationCheck(pool);	
			}
		}
		
	}

	/**
	 * Method doPassivationCheck. Checks an entire pool.
	 * @param pool
	 */
	private void doPassivationCheck(ObjectPool pool) {
		if (logStream!=null)
			logStream.println(getName()+" checking \""+pool.getName()+"\"");
		synchronized(pool) {
			Object preparationResult = prepareForPassivationCheck(pool);
			Object [] objs = pool.createObjectsArray();
			for(int i=0;i<objs.length;i++) {
				BasePooledObject obj = (BasePooledObject)objs[i];
				if (! obj._isPassivableNow()) continue;
				if (isToPassivate(pool, obj, preparationResult)) {
					obj._passivate();
				}
			}
		}
	}

	/**
	 * This method is invoked just before a pool is checked for 
	 * passivation, and therefore before any 
	 * {@link #isToPassivate(org.sadun.util.pool2.ObjectPool, org.sadun.util.pool2.PooledObject, java.lang.Object) isToPassivate()}
	 * is invoked. 
	 * <p>
	 * A subclass may execute some preparation before the pool's 
	 * object are actually scanned, and return an object as result.
	 * Such object is the passed to each subsequent {@link #isToPassivate(org.sadun.util.pool2.ObjectPool, org.sadun.util.pool2.PooledObject, java.lang.Object) isToPassivate()} invocation.
	 * <p>
	 * @param pool
	 */
	protected abstract Object prepareForPassivationCheck(ObjectPool pool);



	/**
	 * This method is invoked to check whether an object is to passivate 
	 * or not.
	 */
	protected abstract boolean isToPassivate(ObjectPool pool, PooledObject obj, Object preparationResult);
	
	/**
	 * This method is invoked by the pool when a pooled object 
	 * is aquired.
	 * @param obj the PooledObject which is being acquired
	 */
	void invoked(ObjectPool pool, PooledObject obj) {
		handleInvoked(pool, obj);
	}
	
	/**
	 * This method is invoked by the pool when a pooled object 
	 * is aquired.
	 * @param obj the PooledObject which is being acquired
	 */
	void acquired(ObjectPool pool, PooledObject obj) {
		handleAcquired(pool, obj);
	}
	
	/**
	 * This method is invoked by the pool when a pooled object 
	 * is released.
	 * @param obj the PooledObject which is being released
	 */
	void released(ObjectPool pool, PooledObject obj) {
		handleReleased(pool, obj);
	}
	

	/**
	 * This method is is used by the subclass to determine 
	 * what to do when a pooled object is actually invoked. 
	 * <p>
	 * The passivation thread may collect this information
	 * to implements its passivation policy.
	 * 
	 * @param obj the PooledObject which is being invoked
	 */
	protected abstract void handleInvoked(ObjectPool pool, PooledObject obj);
	
	/**
	 * This method is used by the subclass to determine 
	 * what to do when a pooled object is aquired.
 	 * <p>
	 * The passivation thread may collect this information
	 * to implements its passivation policy.
	 * 
	 * @param obj the PooledObject which is being acquired
	 */
	protected abstract void handleAcquired(ObjectPool pool, PooledObject obj);
	
	/**
	 * This method is invoked by the pool when a pooled object 
	 * is released.
	 * <p>
	 * The passivation thread may collect this information
	 * to implements its passivation policy.
	 * 
	 * @param obj the PooledObject which is being released
	 */
	protected abstract void handleReleased(ObjectPool pool, PooledObject obj);

	/**
	 * @see org.sadun.util.Terminable#isShuttingDown()
	 */
	public boolean isShuttingDown() {
		return shutdown;
	}

	/**
	 * @see org.sadun.util.Terminable#shutdown()
	 */
	public void shutdown() {
		if (logStream!=null)
			logStream.println("shutting down "+getName());
		shutdown = true;
		interrupt();
	}

	/**
	 * Returns the sleepTime.
	 * @return int
	 */
	public int getSleepTime() {
		return sleepTime;
	}

	/**
	 * Sets the sleepTime.
	 * @param sleepTime The sleepTime to set
	 */
	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}

	/**
	 * Returns the rotatePools.
	 * @return boolean
	 */
	public boolean isRotatePools() {
		return rotatePools;
	}

	/**
	 * Sets the rotatePools.
	 * @param rotatePools The rotatePools to set
	 */
	public void setRotatePools(boolean rotatePools) {
		this.rotatePools = rotatePools;
	}

	/**
	 * Returns the rotationRate.
	 * @return int
	 */
	public int getRotationRate() {
		return rotationRate;
	}

	/**
	 * Sets the rotationRate.
	 * @param rotationRate The rotationRate to set
	 */
	public void setRotationRate(int rotationRate) {
		this.rotationRate = rotationRate;
	}

	/**
	 * Method setLogStream.
	 * @param logStream
	 */
	public void setLogStream(PrintStream logStream) {
		this.logStream=logStream;
	}

}
