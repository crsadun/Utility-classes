package com.deltax.util.listener;

/**
 * Classes implementing this interface can receive signals notified via a 
 * {@link com.deltax.util.listener.ListenerSupport}.
 *
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano Sadun</a>
 * @version 1.0
 */
public interface Listener {

	/**
	 * Invoked when a signal is received.
	 * 
	 * @param signal the received signal.
	 */
    public abstract void receive(Signal signal);
}