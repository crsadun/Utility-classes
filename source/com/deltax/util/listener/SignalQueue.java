package com.deltax.util.listener;

import java.util.ArrayList;
import java.util.List;

import com.deltax.util.FIFOQueue;
import com.deltax.util.QueueEmptyException;

class SignalQueue extends Thread implements Listener {

	private FIFOQueue queue;
	private Listener listener;
	private boolean shutDown;
    private volatile boolean ignoreSignals; 
	public static final int SLEEPING_INTERVAL = 1000;
    
    private static final boolean debug = false;

	public SignalQueue(Listener listener) {
		queue = new FIFOQueue();
		shutDown = false;
		this.listener = listener;
		setDaemon(true);
	}

	public void doStop() {
		shutDown = true;
		interrupt();
		
	}

	public void receive(Signal signal) {
		synchronized (queue) {
            if (debug) 
                System.err.println("["+Thread.currentThread().getName()+"] Receiving "+signal);
			queue.put(signal);
			queue.notify();
		}
	}

	public String[] getQueueState() {
		List l = new ArrayList();
		synchronized (queue) {
			for (int i = 0; i < queue.size(); i++) {
				l.add(queue.peek(i).toString());
			}
		}
		String[] result = new String[l.size()];
		l.toArray(result);
		return result;
	}

	public void run() {
		while (!shutDown) {
			if (queue.isEmpty()) {
				synchronized (queue) {
					try {
						queue.wait();
					} catch (InterruptedException e) {
						if (!shutDown)
							e.printStackTrace(System.err);
						interrupted(); // Reset interruption status
					}
				}
			} else {
				Signal signal = null;
				synchronized (queue) {
					try {
						signal = (Signal) queue.get();
					} catch (QueueEmptyException _ex) {
						continue;
					}
				}
                
                if (!ignoreSignals) {
    				if ((signal instanceof ExceptionSignal)
    					&& (listener instanceof ExceptionListener))
    					((ExceptionListener) listener).receiveException(
    						(ExceptionSignal) signal);
    				else
    					listener.receive(signal);
                }
			}
		}
	}

	/**
	 * @return Returns the listener.
	 */
	public Listener getListener() {
		return listener;
	}

    /**
     * Disables the queue, so that events are consumed but not passed on
     * to the listener object. This method should be used carefully, as
     * avoiding altogether to send signals prevents unnecessary resources
     * consumption.
     * @param value if true, the queue will ignore any signal it receives, i.e.
     *               will not pass it to the listener.
     */
    public void setIgnoreSignals(boolean value) {
        ignoreSignals=value;
    }

}