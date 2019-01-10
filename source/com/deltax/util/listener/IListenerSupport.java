/*
 * Created on Nov 24, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.deltax.util.listener;

/**
 * An interface for the {@link ListenerSupport} class (experimental).
 *
 * @author Cristiano Sadun
 * @version 1.0
 */
public interface IListenerSupport {
	
	/**
	 * Add a listener to the listeners set.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addListener(Listener listener);
	
	/**
	 * Remove a listener from the listeners set.
	 * 
	 * @param listener
	 *            the listener to remove
	 * @exception BlockedListenerException if the listener is blocked and cannot be removed
	 */
	public void removeListener(Listener listener) throws BlockedListenerException;
	
	/**
	 * Unconditionally unregister all the listeners.
	 */
	public void removeAllListeners();
	
	/**
	 * Check whether a listener is registered or not.
	 * 
	 * @param listener
	 *            the listener to check
	 * @return true if the listener is registered
	 */
	public boolean isRegistered(Listener listener);
	
	/**
	 * Broadcast a signal. The order of invocation is unspecified.
	 */
	public void notify(Signal signal);
	
	/**
	 * Count the registered listeners.
	 * 
	 * @return the number of currently registered listeners
	 * 
	 * @see com.deltax.util.listener.IListenerSupport#countListeners()
	 */
	public int countListeners();
	
	/**
	 * Return the the amount of {@link #getReceiveTimeout() time}(in ms.) a
	 * {@link Listener}is given to exit gracefully from the
	 * {@link Listener#receive(Signal)}method when it's
	 * {@link #removeListener(Listener)}.
	 * <p>
	 * Note that this timeout does not apply to normal operations (i.e. a
	 * listener can normally block for how long it wants) but only when a
	 * listener is removed.
	 * 
	 * @return the timeout in milliseconds
	 */
	public long getReceiveTimeout();

	/**
	 * Set the the amount of {@link #getReceiveTimeout() time}(in ms.) a
	 * {@link Listener}is given to exit gracefully from the
	 * {@link Listener#receive(Signal)}method when it's
	 * {@link #removeListener(Listener)}.
	 * <p>
	 * Note that this timeout does not apply to normal operations (i.e. a
	 * listener can normally block for how long it wants) but only when a
	 * listener is removed.
	 * 
	 * @param receiveTimeout
	 *            the timeout in milliseconds
	 */
	public void setReceiveTimeout(long receiveTimeout);

}
