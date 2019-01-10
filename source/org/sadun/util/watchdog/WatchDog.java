/*
 * Created on Aug 27, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.sadun.util.watchdog;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.sadun.util.Terminable;

import com.deltax.util.TimeInterval;
import com.deltax.util.listener.IListenerSupport;
import com.deltax.util.listener.ListenerSupport;
import com.deltax.util.listener.Signal;

/**
 * A WatchDog thread, which periodically executes some checking action.
 * <p>
 * The thread is a daemon, therefore will exit when there are no more
 * non-daemon threads running.
 * <p>
 * Event listeners (implementing the
 * {@link WatchDog.Listener WatchDog.Listener}interface can be registered and
 * will be notfied of failures or the impossibility to execute a check.
 * <p>
 * The check is executed synchronously, so if one listener blocks, the others
 * will be blocked as well.
 * <p>
 * The default check period is set to 5 minutes. The property <tt>org.sadun.pool.connection.watchdog.sleeptime</tt>
 * can be set to a numeric (long) value to change this.
 * <p>
 * <b>Synchronous vs. Asynchronous mode</b>
 * <p>
 * Version 1.0 notified all the listeners synchronously, that is, the method
 * calls of the {@link WatchDog.Listener}interface happened in the same thread
 * as the thread running WatchDog itself. This implied that if a listener
 * performed a heavy operation or crashed, the following listeners and the
 * watchdog itself where blocked.
 * <p>
 * Version 1.1 is retrofitted with an aysnchronous listening mode (optional) to
 * allow for complex or unreliable listeners. No changes are required to
 * external code.
 * <p>
 * The default mode is still synchronous, so existing code behaviour is
 * unchanged. To change the synchronicity mode, either use the
 * {@link #WatchDog(String, long, boolean) new constructor}or invoke
 * {@link #setSynchronous(boolean)}before starting the watchdog.
 * <p>
 * If the asynchronous mode is selected at
 * {@link #WatchDog(String, long, boolean) construction}, listeners
 * notifications will be decoupled from the WatchDog thread itself. Therefore,
 * if a listener blocks, the watchdog thread will not block (see
 * <a href="http://space.tin.it/computer/csadun/software/lf">Aysnchronous
 * listening framework</a> for further information).
 * <p>
 * Asynchronous mode uses more system resources.
 * 
 * @version 1.1
 * @author Cristiano Sadun
 */
public abstract class WatchDog extends Thread implements Terminable {

	/**
	 * The event listening interface for {@link WatchDog WatchDog}events.
	 * 
	 * @author Cristiano Sadun
	 */
	public interface Listener {

		/**
		 * Notifies the listener that a check has been successful
		 * 
		 * @param e
		 */
		public void checkOk(Object obj);

		/**
		 * Notifies the listener that a check has failed.
		 * 
		 * @param e
		 */
		public void checkFailed(Object obj, Throwable e);

		/**
		 * Notifies the listener that a check could not be performed.
		 * 
		 * @param e
		 */
		public void checkImpossible(Object obj, WatchDogException e);

	}

	/**
	 * A signal class for the asynchronous listening framework retrofitting.
	 * The signal type and info is carried in the members.
	 * 
	 * @author Cristiano Sadun
	 * @version 1.0
	 */
	private class WatchDogSignal extends Signal {

		static final int SIGNAL_CHECK_OK = 0;
		static final int SIGNAL_CHECK_FAILED = 1;
		static final int SIGNAL_CHECK_IMPOSSIBLE = 2;

		Object param;
		private int type;
		private Throwable throwable;

		/**
		 * @param arg0
		 */
		WatchDogSignal(Object param, int type, Throwable throwable) {
			super(WatchDog.this);
			this.param = param;
			this.type = type;
			this.throwable = throwable;
		}

		boolean isOk() {
			return type == SIGNAL_CHECK_OK;
		}
		boolean isImpossible() {
			return type == SIGNAL_CHECK_IMPOSSIBLE;
		}
		boolean isFailed() {
			return type == SIGNAL_CHECK_FAILED;
		}

		public WatchDogException getImpossibilityCause() {
			assert isImpossible() && throwable != null;
			return (WatchDogException) throwable;
		}
		public Throwable getFailureCause() {
			assert isFailed() && throwable != null;
			return throwable;
		}

		/**
		 * @param originalListener
		 */
		public void dispatch(Listener originalListener) {
			if (isOk())
				originalListener.checkOk(param);
			else if (isFailed())
				originalListener.checkFailed(param, getFailureCause());
			else
				originalListener.checkImpossible(
					param,
					getImpossibilityCause());
		}
	}

	/**
	 * An internal wrapper to decouple event notification. The original
	 * watchdog listeners are wrapped and invoked in an asynchronous Listener
	 * (from the listening framework).
	 * <p>
	 * The implementation of notify() has therefore changed as well.
	 */
	private class ListenerWrapper
		implements com.deltax.util.listener.Listener {

		private Listener originalListener;

		ListenerWrapper(Listener originalListener) {
			assert originalListener != null;
			this.originalListener = originalListener;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.deltax.util.listener.Listener#receive(com.deltax.util.listener.Signal)
		 */
		public void receive(Signal signal) {
			WatchDogSignal s = (WatchDogSignal) signal;
			s.dispatch(originalListener);

		}

		public boolean equals(Object obj) {
			if (obj instanceof ListenerWrapper) {
				return originalListener.equals(
					((ListenerWrapper) obj).originalListener);
			}
			return false;
		}

		public int hashCode() {
			return originalListener.hashCode();
		}

	}

	/**
	 * A base implementation of {@link WatchDog.Listener WatchDog.Listener}
	 * which invokes
	 * {@link WatchDog.Listener#checkFailed(Object, Exception) checkFailed()}
	 * also when a check is impossible.
	 * <p>
	 * Listeners extending this class will need to do a type check against the
	 * received Throwable to verify wether or not is a
	 * {@link WatchDogException WatchDogException}(and thus represents the
	 * impossiblity of checking rather than a failed check).
	 * 
	 * @author Cristiano Sadun
	 *  
	 */
	public static abstract class BaseListener implements Listener {

		/**
		 * Does not do anything.
		 */
		public void checkOk(Object obj) {
		}

		/**
		 * Receive a notification of any problem during the check.
		 * <p>
		 * The implementation will need to do a type check against the received
		 * Throwable to verify wether or not is a
		 * {@link WatchDogException WatchDogException}(and thus represents the
		 * impossiblity of checking rather than a failed check).
		 * 
		 * @param obj
		 * @param e
		 */
		public abstract void checkFailed(Object obj, Throwable e);

		/**
		 * Invoke {@link #checkFailed(Object, Exception) checkFailed()}passing
		 * the received {@link WatchDogException WatchDogException}.
		 */
		public void checkImpossible(Object obj, WatchDogException e) {
			checkFailed(obj, e);
		}

	}

	private volatile boolean shutdown;
	private long checkPeriodTime;
	Set listeners = new HashSet();
	// This is used only for the synchronous notification
	IListenerSupport ls;
	private boolean startBySleeping = false;
	private boolean firstCycle = false;

	public final static long DEFAULT_SLEEP_TIME = 300000L; // 5 minutes

	private boolean synchronous;
	private boolean removeFailedListeners=true;

	/**
	 * Create a watchdog with the given check period, in milliseconds.
	 * <p>
	 * The watchdog is created in synchronus mode - i.e. a blocking listener
	 * will block the WatchDog thread itself.
	 * 
	 * @param name
	 *            the name of the WatchDog thread
	 * @param checkPeriodTime
	 *            the amount of time the watchdog waits between each check.
	 */
	protected WatchDog(String name, long checkPeriodTime) {
		this(name, checkPeriodTime, false);
	}

	/**
	 * Create a watchdog with the given check period, in milliseconds.
	 * <p>
	 * The watchdog can be created in either synchronous or asynchronous mode.
	 * <p>
	 * In the former case, listeners are invoked in the WatchDog thread - and
	 * if they block, the WatchDog thread will block; in the latter case,
	 * listeners execution is decoupled from the WatchDog thread.
	 * <p>
	 * Asynchronous mode requires is heavier and requires more resources.
	 * 
	 * @param name
	 *            the name of the WatchDog thread
	 * @param checkPeriodTime
	 *            the amount of time the watchdog waits between each check.
	 * @param synchronous
	 *            if <b>true</b>, synchronous mode is selected, else
	 *            asynchronous.
	 */
	protected WatchDog(
		String name,
		long checkPeriodTime,
		boolean synchronous) {
		super(name);
		ls = createListenerSupport();
		this.checkPeriodTime = checkPeriodTime;
		this.synchronous = synchronous;
		setDaemon(true);
	}

	/**
	 * Create a watchdog with the default check period time.
	 */
	protected WatchDog(String name) {
		this(name, getDefaultSleepTime());
	}

	/**
	 * Create a watchdog with the default check period time.
	 */
	protected WatchDog() {
		this("");
		setName(getClass().getName());
	}

	/**
	 * @return
	 */
	private IListenerSupport createListenerSupport() {
		/*
		 * // Attempt to find a mbean server which has a listrner support whose
		 * name matches the // WatchdogName ArrayList serversList =
		 * MBeanServerFactory.findMBeanServer(null); ObjectName candidateName=
		 * new ObjectName("user:name="+getName()+"WDListenerSupport"); for(int
		 * i=0;i <serversList.size();i++) { MBeanServer mbs =
		 * (MBeanServer)serversList.get(i); Set
		 * instances=mbs.queryMBeans(candidateName, null); if
		 * (instances.size()>0) { // Pick up the first ObjectInstance instance =
		 * (ObjectInstance)instances.iterator().next(); instance. } }
		 */
		return new ListenerSupport();
	}

	private static long getDefaultSleepTime() {
		long defaultSleepTime = DEFAULT_SLEEP_TIME;
		String s =
			System.getProperty("org.sadun.pool.connection.watchdog.sleeptime");
		if (s != null)
			try {
				defaultSleepTime = Long.parseLong(s);
			} catch (NumberFormatException e) {
				System.err.println(
					"Warning: value of property org.sadun.pool.connection.watchdog.sleeptime must be a number, \""
						+ s
						+ "\" ignored, using "
						+ new TimeInterval(defaultSleepTime));
			}
		return defaultSleepTime;
	}

	/**
	 * Run the watching loop, until the thread is either
	 * {@link #shutdown() shut down}or the last running thread.
	 */
	public void run() {
		shutdown = false;
		firstCycle=true;
		while (!shutdown) {
			try {
				if (firstCycle) {
					if (startBySleeping)
						Thread.sleep(checkPeriodTime);
					firstCycle = false;
				} else
					Thread.sleep(checkPeriodTime);
				Object obj = getObjectToCheck();
				try {
					Throwable t = doCheck(obj);
					notify(obj, t);
				} catch (WatchDogException e) {
					notify(obj, e);
				}
			} catch (InterruptedException e) {
				// Ignore, will exit on shutdown
			}
		}
	}

	/**
	 * This method can be implemented by subclasses if the checking action
	 * implies one or more objects. The default implemntation returns <n>null
	 * </b>.
	 * 
	 * @return the object subject to the checking action.
	 */
	protected Object getObjectToCheck() {
		return null;
	}

	/**
	 * The implementation of notify now can use the LF listner support
	 * 
	 * @param t
	 */
	void notify(Object obj, Throwable t) {
		if (synchronous) {
			notifySynchronous(obj, t);
		} else {
			notifyAsynchronous(obj, t);
		}
	}

	void notifySynchronous(Object obj, Throwable t) {
		Set failed=new HashSet();
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			Listener listener = ((Listener) i.next());
			try {
				if (t == null)
					listener.checkOk(obj);
				else if (t instanceof WatchDogException)
					listener.checkImpossible(obj, (WatchDogException) t);
				else
					listener.checkFailed(obj, t);
			} catch (Throwable e) {
				System.err.println(
					"Warning: the listener "
						+ listener.toString()
						+ " has generated a "
						+ e + (removeFailedListeners ? 
						 " and is removed from the listeners set" : ""));
				if (removeFailedListeners) failed.add(listener);
			}
		}
		if (failed.size()>0 && removeFailedListeners)
			for (Iterator i = failed.iterator(); i.hasNext();) {
				Listener listener = ((Listener) i.next());
				removeListener(listener);
			}
	}

	void notifyAsynchronous(Object obj, Throwable t) {
		if (t == null)
			ls.notify(
				new WatchDogSignal(obj, WatchDogSignal.SIGNAL_CHECK_OK, t));
		else if (t instanceof WatchDogException)
			ls.notify(
				new WatchDogSignal(
					obj,
					WatchDogSignal.SIGNAL_CHECK_IMPOSSIBLE,
					t));
		else
			ls.notify(
				new WatchDogSignal(obj, WatchDogSignal.SIGNAL_CHECK_FAILED, t));

	}

	/**
	 * Perform the required check. This method must <i>return</i> a <tt>Throwable</tt>
	 * object if the check fails, and <i>raise</i> a
	 * {@link WatchDogException WatchDogException}if the check proves
	 * impossible.
	 * <p>
	 * In either case, the currently registered listeners will be notified of
	 * the failure.
	 * 
	 * @param obj
	 *            the object subject to the check, or <b>null</b> if the
	 *            check does not depend on any specific object.
	 * @return a Throwable object, if the check fails
	 * @throws WatchDogException
	 *             if the check is impossible
	 */
	protected abstract Throwable doCheck(Object obj) throws WatchDogException;

	/**
	 * See
	 * {@link org.sadun.util.Terminable#isShuttingDown() org.sadun.util.Terminable.isShuttingDown()}.
	 * 
	 * @see org.sadun.util.Terminable#isShuttingDown()
	 */
	public boolean isShuttingDown() {
		return shutdown;
	}

	/**
	 * Shut down the watchdog thread. See
	 * {@link org.sadun.util.Terminable#shutdown() org.sadun.util.Terminable.shutdown()}.
	 * 
	 * @see org.sadun.util.Terminable#shutdown()
	 */
	public synchronized void shutdown() {
		shutdown = true;
		interrupt();

	}

	/**
	 * Add a watchdog listener to the listener set.
	 * 
	 * @param l
	 *            the listener to add
	 */
	public void addListener(Listener l) {
		synchronized (ls) {
			ls.addListener(new ListenerWrapper(l));
		}
		synchronized (listeners) {
			listeners.add(l);
		}
	}

	/**
	 * Remove a watchdog listener from the listeners set.
	 * 
	 * @param l
	 *            the listener to remove
	 */
	public void removeListener(Listener l) {
		synchronized (ls) {
			ls.removeListener(new ListenerWrapper(l));
		}
		synchronized (listeners) {
			listeners.remove(l);
		}
	}
	
	public void removeaAllListeners() {
		synchronized (ls) {
			ls.removeAllListeners();
		}
		synchronized (listeners) {
			listeners.clear();
		}
	}

	/**
	 * Return the amount of time the watchdog waits between each check.
	 * 
	 * @return the amount of time the watchdog waits between each check.
	 */
	public long getCheckPeriodTime() {
		return checkPeriodTime;
	}

	/**
	 * Set the amount of time the watchdog waits between each check.
	 * 
	 * @param checkPeriodTime
	 *            the amount of time the watchdog waits between each check.
	 */
	public void setCheckPeriodTime(long checkPeriodTime) {
		this.checkPeriodTime = checkPeriodTime;
	}

	/**
	 * Return the synchronicity mode (see class comment).
	 * 
	 * @return the synchronicity mode (see class comment).
	 */
	public boolean isSynchronous() {
		return synchronous;
	}

	/**
	 * Set the synchronicity mode (see class comment).
	 * 
	 * @param v
	 *            if <b>true</b>, the mode is "synchronous" (see class
	 *            comment).
	 */
	public void setSynchronous(boolean v) {
		if (isAlive())
			throw new IllegalStateException("The watchdog is already running");
		this.synchronous = v;

	}

	/**
	 * Return <b>true</b> if the watchdog goes immediatly to sleep on startup
	 * 
	 * @return <b>true</b> if the watchdog goes immediatly to sleep on
	 *         startup
	 */
	public boolean isStartBySleeping() {
		return startBySleeping;
	}

	/**
	 * @param startBySleeping
	 *            The startBySleeping to set.
	 */
	public void setStartBySleeping(boolean startBySleeping) {
		if (isAlive())
			throw new IllegalStateException("The watchdog is already running");
		this.startBySleeping = startBySleeping;
	}

	/**
	 * Return <b>true</b> if listeners that fail are removed from the listener set (in synchronous mode).
	 * @return <b>true</b> if listeners that fail are removed from the listener set (in synchronous mode).
	 */
	public boolean isRemoveFailedListeners() {
		return removeFailedListeners;
	}

	/**
	 * Set wether or not listeners that fail are removed from the listener set (in synchronous mode).
	 * @param removeFailedListeners if <b>true</b>, listeners that fail are removed from the listener set (in synchronous mode).
	 */
	public void setRemoveFailedListeners(boolean removeFailedListeners) {
		this.removeFailedListeners = removeFailedListeners;
	}

	/**
	 * Disposes of any resource allocated by the watchdog.
	 * This implementation does not do anything.
	 */
	public void dispose() {
	}
	
	/**
	 * The class finalizer. This implementation invokes {@link #dispose()}.
	 */
	public void finalize() {
		dispose();
	}
}
