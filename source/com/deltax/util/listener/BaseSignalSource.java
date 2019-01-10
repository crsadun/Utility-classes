package com.deltax.util.listener;

/**
 * A base implementation of SignalSource. The {@link #notify(Signal)} and {@link #notifyException(ExceptionSignal)} methods
 * are protected.  
 *
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano Sadun</a>
 * @version 1.0
 */
public abstract class BaseSignalSource implements SignalSource {
	
	/**
	 * The {@link ListenerSupport listener support} object
	 */
	protected ListenerSupport ls;

	/**
	 * Initialize the {@link ListenerSupport listener support} object.
	 */
	protected BaseSignalSource() {
		ls = new ListenerSupport();
	}

	/**
	 * Allow source clients to add listeners.
	 * 
	 * @param listener the listener to add.
	 */
	public void addListener(Listener listener) {
		ls.addListener(listener);
	}

	/**
	 * Return <b>true</b> if the given object is registered as a listener.
	 * 
	 * @param listener the listner to check
	 * @return <b>true</b> if the given object is registered as a listener, else <b>false</b>.
	 */
	public boolean isRegistered(Listener listener) {
		return ls.isRegistered(listener);
	}

	
	/**
	 * Notify a {@link Signal signal} to the listners.
	 * 
	 * @param signal the signal to be notified.
	 */
	protected void notify(Signal signal) {
		ls.notify(signal);
	}
	/**
	 * Notify a {@link ExceptionSignal exception signal} to the listners.
	 * 
	 * @param signal the signal to be notified.
	 */
	protected void notifyException(ExceptionSignal exceptionsignal) {
		ls.notify(exceptionsignal);
	}
	
	/**
	 * Return the number of registered listeners.
	 * @return the number of registered listeners.
	 */
	public int countListeners() {
		return ls.countListeners();
	}
    
    /**
     * Retrieve an array with the currently registered listeners
     */
    public Listener [] getListeners() { 
            return ls.getListeners();
    }

	/**
	 * Allow source clients to remove listeners.
	 * 
	 * @param listener the listener to remove.
	 */
	public void removeListener(Listener listener) {
		ls.removeListener(listener);
	}
	
	public void removeAllListeners() {
	    ls.removeAllListeners();
	}

	/**
	 * Always return true, since this implementation does support {@link ExceptionSignal exception signals}.
	 * 
	 * @return true
	 */
	public boolean supportsExceptionSignals() {
		return true;
	}
    
    public void setEnabled(Listener listener, boolean enabled) {
        ls.setEnabled(listener, enabled);
    }

	
}