package com.deltax.util.listener;

/**
 * A base implementation of signal source, which is a thread in itself.
 *
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano Sadun</a>
 * @version 1.0
 */
public abstract class BaseSignalSourceThread extends Thread {
    
    
    private static class LFThreadGroup extends ThreadGroup {
        
        private long threadCount=0;
        
        LFThreadGroup() {
            super("Signal thread group");
        }

        /* (non-Javadoc)
         * @see java.lang.ThreadGroup#uncaughtException(java.lang.Thread, java.lang.Throwable)
         */
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace(System.err);
            System.err.flush();
        }

        public synchronized long getNextCount() {
            return threadCount++;
        }
    }
    
    private static LFThreadGroup lfThreadGroup = new LFThreadGroup();
    
	protected BaseSignalSource bs;

	public BaseSignalSourceThread() {
	    super(lfThreadGroup, "signal source "+(lfThreadGroup.getNextCount()));
		bs = new BaseSignalSource() {
		};
	}
	
	protected BaseSignalSourceThread(BaseSignalSource bs) {
	    super(lfThreadGroup, "signal source "+(lfThreadGroup.getNextCount()));
		this.bs=bs;
	}

	public void addListener(Listener listener) {
		bs.addListener(listener);
	}

	public boolean isRegistered(Listener listener) {
		return bs.isRegistered(listener);
	}

	protected void notify(Signal signal) {
		bs.notify(signal);
	}

	protected void notifyException(ExceptionSignal exceptionsignal) {
		bs.notifyException(exceptionsignal);
	}

	public void removeListener(Listener listener) {
		bs.removeListener(listener);
	}
	
	public void removeAllListeners() {
		bs.removeAllListeners();
	}
    
    public void setEnabled(Listener listener, boolean enabled) {
        bs.setEnabled(listener, enabled);
    }
    
    /**
     * Retrieve an array with the currently registered listeners
     */
    public Listener [] getListeners() { 
            return bs.getListeners();
    }
	
}