/*
 * Created on Nov 25, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.watchdog.listeners;

import java.util.List;

import org.sadun.util.watchdog.WatchDogException;

/**
 * An exception raised when a wait/retry policy is abandoned after a certain number of retries.
 * 
 * @author Cristiano Sadun
 * @version 1.0
 */
public class MaxRetryExceededException extends Exception {
	
	private int attemptedRetriesCount;
	private WatchDogException [] outcomes;

	/**
	 * Create an exception
	 */
	public MaxRetryExceededException(int retries, List outcomes) {
		super("The maximum number of retries ("+retries+") has been run and failed.");
		this.outcomes=new WatchDogException [outcomes.size()];
		this.attemptedRetriesCount=retries;
		outcomes.toArray(this.outcomes);
	}

	
	/**
	 * Return a count of the attempted retries
	 * @return a count of the attempted retries
	 */
	public int getAttemptedRetriesCount() {
		return attemptedRetriesCount;
	}

	/**
	 * Return the ordered results of the failed retries.
	 * 
	 * @return the ordered results of the failed retries.
	 */
	public WatchDogException[] getOutcomes() {
		return outcomes;
	}

}
