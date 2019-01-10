/*
 * Created on Nov 25, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.watchdog.listeners;

import java.util.LinkedList;
import java.util.List;

import org.sadun.util.watchdog.WatchDogException;
import org.sadun.util.watchdog.WatchDog.Listener;

/**
 * A listener which implements a wait/retry policy on impossible checks and
 * sends an alarm when a failure occurs or the check couldn't be performed
 * for a certain number of times. 
 * @author Cristiano Sadun
 * @version 1.0
 */
public abstract class WaitAndRetryListener implements Listener {
	
	private int maxRetryCount;
	private int retryCount;
	private List retryOutcomes=new LinkedList();
	
	/**
	 * The default maximum number of retries to attempt. Its value is 3.
	 */
	public static final int DEFAULT_MAX_RETRY_COUNT=3;

	/**
	 * Create a listener which waits at most {@link #DEFAULT_MAX_RETRY_COUNT} impossible
	 * checks before declaring a failure.
	 */
	public WaitAndRetryListener() {
		this(DEFAULT_MAX_RETRY_COUNT);
	}
	
	/**
	 * Create a listener which waits the given number of impossible
	 * checks before declaring a failure.
	 * 
	 * @param maxRetryCount the max number of impossible checks before declaring a failure.
	 */
	public WaitAndRetryListener(int maxRetryCount) {
		this.maxRetryCount=maxRetryCount;
		this.retryCount=0;
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.watchdog.WatchDog.Listener#checkOk(java.lang.Object)
	 */
	public void checkOk(Object obj) {
		reset();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.watchdog.WatchDog.Listener#checkFailed(java.lang.Object, java.lang.Throwable)
	 */
	public abstract void checkFailed(Object obj, Throwable e);
	
	/* (non-Javadoc)
	 * @see org.sadun.util.watchdog.WatchDog.Listener#checkImpossible(java.lang.Object, org.sadun.util.watchdog.WatchDogException)
	 */
	public void checkImpossible(Object obj, WatchDogException e) {
		retryOutcomes.add(e);
		retryCount++;
		if (retryCount == maxRetryCount) {
			checkFailed(obj, new MaxRetryExceededException(maxRetryCount, retryOutcomes));
			reset();
		}
	}
	
	protected synchronized void reset() {
		retryCount=0;
		retryOutcomes.clear();
	}

	/**
	 * @return Returns the maxRetryCount.
	 */
	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	/**
	 * @param maxRetryCount The maxRetryCount to set.
	 */
	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

}
