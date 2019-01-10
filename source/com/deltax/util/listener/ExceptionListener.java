package com.deltax.util.listener;

/**
 * Classes implementing this interface will explicitly receive
 * {@link com.deltax.util.listener.ExceptionSignal exceptions signals} separately from normal
 * signals.
 * 
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano
 *         Sadun</a>
 * @version 1.0
 */
public interface ExceptionListener extends Listener {

	/**
	 * Receives an exception-related signal
	 * 
	 * @param exceptionsignal the received signal 
	 */
	public abstract void receiveException(ExceptionSignal exceptionsignal);
}