/*
 * Created on Nov 25, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.watchdog.mbean;

import javax.management.MBeanException;

import org.sadun.util.watchdog.WatchDog;
import org.sadun.util.watchdog.WatchDogException;

/**
 * <font color=red><b>NON COMPLETE YET</b></font>.
 * 
 * A JMX instrumentation of the {@link org.sadun.util.watchdog.WatchDog} class.
 * 
 * @author Cristiano Sadun
 * @version 1.0
 */
public interface ManagedWatchDogMBean {
	
	/**
	 * This interface must is implemented by objects which define a specific check
	 * semantics for the {@link WatchDog default} implementation of the
	 * {@link ManagedWatchDogMBean}.
	 * <p>
	 * Unless is used programmatically, a check action should have an empty constructor
	 * and accessors for its properties.
	 * 
	 * @author Cristiano Sadun
	 * @version 1.0
	 */
	public interface CheckAction {
		
		/**
		 * Performs a check on whether or not the action is ready, and returns
		 * a description of the problem if a problem exist, otherwise <b>null</b>.
		 * <p>
		 * For actions whose parameters may be checked at runtime, this is the 
		 * place where to put validation checks for such parameters.
		 *   
		 * @param running if <b>true</b>, the object using the action is declared as running.
		 * @return <b>null</b> if the action is ready. A description of the problem if it is not.
		 */
		public String isReady(boolean running);
		
		/**
		 * Perform the required check. This method must <i>return</i> a <tt>Throwable</tt>
		 * object if the check fails, and <i>raise</i> a
		 * {@link WatchDogException WatchDogException}if the check proves
		 * impossible.
		 * <p>
		 * In either case, the currently registered listeners will be notified
		 * of the failure.
		 * 
		 * @param obj
		 *            the object subject to the check, or <b>null</b> if the
		 *            check does not depend on any specific object.
		 * @return a Throwable object, if the check fails
		 * @throws WatchDogException
		 *             if the check is impossible
		 */
		public Throwable doCheck(Object obj) throws WatchDogException;
	}
	

	/**
	 * Add a watchdog listener to the listener set.
	 * 
	 * @param l
	 *            the listener to add
	 */
	public void addListener(WatchDog.Listener l);
	
	/**
	 * Remove a watchdog listener from the listeners set.
	 * 
	 * @param l
	 *            the listener to remove
	 */
	public void removeListener(WatchDog.Listener l);
	
	/**
	 * Return the amount of time the watchdog waits between each check.
	 * 
	 * @return the amount of time the watchdog waits between each check.
	 */
	public long getCheckPeriodTime();

	/**
	 * Set the amount of time the watchdog waits between each check.
	 * 
	 * @param checkPeriodTime
	 *            the amount of time the watchdog waits between each check.
	 */
	public void setCheckPeriodTime(long checkPeriodTime);
	
	/**
	 * Set the name of the {@link CheckAction check action class} to use.
	 * <p>
	 * The class must have a public default constructor.
	 * <p>
	 * This method attempts to create an instance of the class, and 
	 * a corresponding WatchDog object. An <tt>MBeanException</tt>
	 * is raised in case of problem doing so.
	 * <p>
	 * This method can be called only once.
	 * 
	 * 
	 * @param name the name of the {@link CheckAction check action class} to use.
	 * @param MBeanException if there is a problem instantiating the given class,
	 *        or a check action class has already been specified.
	 */
	public void setCheckActionClassName(String name) throws MBeanException;
	
	/**
	 * Get the name of the {@link CheckAction check action class} in use.
	 * @return the name of the {@link CheckAction check action class} in use.
	 */
	public String getCheckActionClassName();

	/**
	 * Return the synchronicity mode (see class comment).
	 * 
	 * @return the synchronicity mode (see class comment).
	 */
	public boolean isSynchronous();
	/**
	 * Return <b>true</b> if the watchdog goes immediatly to sleep on startup
	 * 
	 * @return <b>true</b> if the watchdog goes immediatly to sleep on startup
	 */
	public boolean isStartBySleeping();

	/**
	 * @param startBySleeping
	 *            The startBySleeping to set.
	 */
	public void setStartBySleeping(boolean startBySleeping);
	
	/**
	 * Set the name of the watchdog thread
	 * 
	 * @param name the name of the watchdog thread
	 */
	public void setName(String name);
	
	/**
	 * Set the name of the watchdog tGread
	 * 
	 * @return the name of the watchdog thread
	 */
	public String getName();
	
	/**
	 * Start the watchdog thread.
	 */
	public void startup();
	
	/**
	 * Unconditionally requests a shutdown. The runnable will complete
	 * its last atomic operation, and gracefully exit the run() method.
	 */
	public void shutdown();

	/**
	 * Return <b>true</b> if a shutdown has been requested.
	 * @return <b>true</b> if a shutdown has been requested
	 */
	public boolean isShuttingDown();
	
	/**
	 * Return <b>true</b> if the watchdog thread is currently running.
	 * @return <b>true</b> if the watchdog thread is currently running.
	 */
	public boolean isAlive();
	
	/**
	 * Set a parameter on the current check action object. The {@link #getCheckActionClassName() CheckActionClassName}
	 * attribute must be set to a valid, constructable class before this method is invoked.
	 * 
	 * @param name the name of the check action parameter.
	 * @param value the value to set
	 * 
	 * @throws MBeanException
	 */
	public void setCheckActionParameter(String name, Object value) throws MBeanException;
	
	/**
	 * Return the value of a parameter in the current check action object. The {@link #getCheckActionClassName() CheckActionClassName}
	 * attribute must be set to a valid, constructable class before this method is invoked.
	 * 
	 * @param name the name of the check action parameter.
	 * @return the parameter value 
	 * @throws MBeanException
	 */ 
	public Object getCheckActionParameter(String name)  throws MBeanException;
	
	

}
