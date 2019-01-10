package com.deltax.util.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sadun.util.tp.ThreadPool;

/**
 * <b>This class is the pluggable support for asynchronous listening.</b>
 * <p>
 * It exposes registration methods for
 * {@link com.deltax.util.listener.Listener signal listeners} and the
 * {@link #notify(Signal)} method to be called to actually send signals.
 * <p>
 * It also allows to enable or disable a specific listener.
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano
 *         Sadun</a>
 * @version 3.0
 */
public class ListenerSupport implements IListenerSupport {
    
	/**
	 * The default {@link #getReceiveTimeout() time}a {@link Listener} is
	 * given to exit gracefully from the {@link Listener#receive(Signal)} 
	 * method when it's {@link #removeListener(Listener) removed}.
	 * <p>
	 * It can be set by using {@link #setReceiveTimeout(long)}after creating
	 * the listener support.
	 */
	public final static long DEFAULT_RECEIVE_TIMEOUT = 10000L;
	// Wait at most 10 secs for a thread to die when stopped.

	/**
	 * A lock used in accessing the listeners/queue registry.
	 */
	protected Object lock;
	private HashMap queueTable;
	private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;
	
	private ThreadPool threadPool;
    private Set disabledListeners = new HashSet();

	/**
	 * Create a listener support which uses at most the given number of threads
	 */
	public ListenerSupport(int maxThreads) {
		lock = new Object();
		queueTable = new HashMap();
		if (maxThreads>0) threadPool=new ThreadPool(maxThreads);
	}
	
	/**
	 * Create a listener support.
	 */
	public ListenerSupport() {
	    this(-1);
	}
	

	/**
	 * Add a listener to the listeners set.
	 * <p>
	 * If the support has a maximum thread count, the method will block if there are no free threads for the listener,
	 * until another listener releases a thread.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addListener(Listener listener) {
		synchronized (lock) {
			SignalQueue signalqueue = new SignalQueue(listener);
			
			if (threadPool==null) {
			    signalqueue.start();
			}
			else {
			    if (!threadPool.start(signalqueue))
                    throw new RuntimeException(
                            "Could not start signal queue thread - "+(threadPool.size()-threadPool.getBusyCount())+" threads free");
			}
			queueTable.put(listener, signalqueue);
            
            while(! signalqueue.isAlive()) 
                Thread.yield();
            
		}
	}
    
    public Listener [] getListeners() {
        Set s = queueTable.keySet();
        Listener [] listeners = new Listener[s.size()];
        s.toArray(listeners);
        return listeners;
    }

	/**
	 * Unconditionally unregister all the listeners.
	 */
	public void removeAllListeners() {
		synchronized (lock) {
			for (Iterator i = queueTable.keySet().iterator();
				i.hasNext();
				((SignalQueue) queueTable.get(i.next())).doStop());
			queueTable.clear();
		}
	}

	/**
	 * Check whether a listener is registered or not.
	 * 
	 * @param listener
	 *            the listener to check
	 * @return true if the listener is registered
	 */
	public boolean isRegistered(Listener listener) {
		return queueTable.get(listener) != null;
	}

	/**
	 * Broadcast a signal. The order of invocation is unspecified.
	 */
	public void notify(Signal signal) {
		synchronized (lock) {
			for (Iterator i = queueTable.keySet().iterator();
				i.hasNext();) {
                Listener l = (Listener)i.next();
                if (!isEnabled(l)) continue;
				((SignalQueue)queueTable.get(l)).receive(signal);
            }
		}
	}

    /**
	 * Remove a listener from the listeners set.
	 * 
	 * @param listener
	 *            the listener to remove
	 * @exception BlockedListenerException
	 *                if the listener is blocked and cannot be removed
	 */
	public void removeListener(Listener listener)
		throws BlockedListenerException {
		synchronized (lock) {
			SignalQueue signalqueue = (SignalQueue) queueTable.get(listener);
			if (signalqueue == null)
				return;
			signalqueue.doStop();
			try {
				signalqueue.join(receiveTimeout);
				// Check if the thread's really terminated
				if (signalqueue.isAlive()) {
					throw new BlockedListenerException(
						"The listener "
							+ listener
							+ " did not return from receive() in time and appears to be blocked");
				}
			} catch (InterruptedException interruptedexception) {
				interruptedexception.printStackTrace();
			}
			queueTable.remove(listener);
            disabledListeners.remove(listener);
		}
	}

	/**
	 * Allows subclasses to retrieve the queue threads (experimental)
	 * 
	 * @return
	 */
	protected SignalQueue[] getQueues() {
		synchronized (lock) {
			List l = new ArrayList();
			for (Iterator i = queueTable.keySet().iterator();
				i.hasNext();
				l.add(queueTable.get(i.next())));

			SignalQueue[] result = new SignalQueue[l.size()];
			l.toArray(result);
			return result;
		}

	}

	/**
	 * Count the registered listeners.
	 * 
	 * @return the number of currently registered listeners
	 * 
	 * @see com.deltax.util.listener.IListenerSupport#countListeners()
	 */
	public int countListeners() {
		return queueTable.keySet().size();
	}

	/**
	 * Return the the amount of {@link #getReceiveTimeout() time} (in ms.) a
	 * {@link Listener}is given to exit gracefully from the
	 * {@link Listener#receive(Signal)} method when it's
	 * {@link #removeListener(Listener) removed}.
	 * <p>
	 * Note that this timeout does not apply to normal operations (i.e. a
	 * listener can normally block for how long it wants) but only when a
	 * listener is removed.
	 * 
	 * @return the timeout in milliseconds
	 */
	public long getReceiveTimeout() {
		return receiveTimeout;
	}

	/**
	 * Set the the amount of {@link #getReceiveTimeout() time} (in ms.) a
	 * {@link Listener}is given to exit gracefully from the
	 * {@link Listener#receive(Signal)} method when it's
	 * {@link #removeListener(Listener) removed}.
	 * <p>
	 * Note that this timeout does not apply to normal operations (i.e. a
	 * listener can normally block for how long it wants) but only when a
	 * listener is removed.
	 * 
	 * @param receiveTimeout
	 *            the timeout in milliseconds
	 */
	public void setReceiveTimeout(long receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}
    
    /**
     * Enables or disable one of the contained listeners.
     * <p>
     * If a listener has not been {@link #addListener(Listener) added} to 
     * the support, disabling it does nothing. Similarly, enabling a
     * listener which is not disabled does nothing.
     * 
     * @param listener the listener to disable or enable
     * @param enabled if false, the listener will not be notified events.
     */
    public void setEnabled(Listener listener, boolean enabled) {
        if (enabled) 
            disabledListeners.remove(listener);
        else
            if (queueTable.containsKey(listener))
                disabledListeners.add(listener);
    }
    
    private boolean isEnabled(Listener l) {
        return ! disabledListeners.contains(l);
    }
    
    

}