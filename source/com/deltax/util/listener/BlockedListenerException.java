/*
 * Created on Dec 2, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.deltax.util.listener;

/**
 * An exception thrown when the {@link com.deltax.util.listener.ListenerSupport} cannot
 * terminate gracefully a listener which is being {@link com.deltax.util.listener.ListenerSupport#removeListener(Listener) 
 * removed}, since the listener is blocked.
 * 
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano Sadun</a>
 * @version 1.0
 */
public class BlockedListenerException extends RuntimeException {

	/**
	 * @param string
	 */
	BlockedListenerException(String msg) {
		super(msg);
	}

}
