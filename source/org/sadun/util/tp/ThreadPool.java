package org.sadun.util.tp;


/**
 * A thread pooling class. Ensures that no more than <i>n</i> thread are alive
 * at the same time, while queueing incoming requests.
 * <p>
 * The pooled threads can be created as daemon or not - if they're daemon, the
 * JVM will exit when only pooled threads are running.
 * <p>
 * Note that a thread pool is <i>not</i> a thread in itself, i.e. is executed
 * in the thread of the caller.
 * <p>
 * Use the {@link ThreadPoolThread ThreadPoolThread} for a pool which runs in a
 * thread on its own and on emtpy queue just sleeps.
 *
 * @author Cris Sadun
 * @version 1.1
 */
public class ThreadPool {

    private boolean shutdown=false;
    private Queue queue;
    private PooledThread [] pool;
    public boolean verbose=( System.getProperty("org.sadun.verbose") != null);

    /**
     * A thread class that waits() undefinitely unless explicitly
     * notified. When notified, it attempts to run the associated
     * runnable. If no runnable exists, or when the runnable exits or
     * fails, it goes back to waiting.
     */
    public class PooledThread extends Thread {

        private Runnable runnable;
        private boolean waiting=true;
        int i;

        /**
         * Create a PooledThread on the given group, with the given
         * identifier and the given daemon property
         */
        PooledThread(ThreadGroup tg, int i, boolean daemon) {
            super(tg, "pooled-thread-"+i);
            //super(tg, "pooled-thread-"+i+" (free)");
            this.i=i;
            setDaemon(daemon);
        }

        private synchronized void setRunnable(Runnable r) {
            if (! isFree())
                throw new IllegalStateException("Thread "+getName()+" in the pool is not free");
            if (verbose) System.out.println("{"+Thread.currentThread()+"} "+"Thread "+this+" is free, associating "+r);
            //setName("pooled-thread-"+i+" hosting "+r);
            runnable=r;
            waiting=false;
            notify();
        }

        /**
         * Runs the available runnable object, or waits.
         */
        public final synchronized void run() {
            do {
                if (runnable != null) {
                    waiting=false;
                    try {
                        runnable.run();
                    } catch(Exception exc) {
                        // An exception must just set the pooled thread to free
						runnable=null;
						exc.printStackTrace();
                        //setName("pooled-thread-"+i+" (free)");
                    }
                }

                // Notify the pool the thread is free and ask for a new runnable,
                // if any
                runnable = getNextThread();
                if (runnable != null) {
                    if (verbose) System.out.println("{"+Thread.currentThread()+"} "+"Dequeueed "+runnable+" ("+queue.size()+" in queue)");
                    continue;
                }

                waiting=true;
                try {
                    if (verbose) System.out.println("{"+Thread.currentThread()+"} "+this+" going to wait.");
                    wait(0);
                    if (verbose) System.out.println("{"+Thread.currentThread()+"} "+this+" notified.");
                } catch(InterruptedException e) {
                    if (verbose) System.out.println("{"+Thread.currentThread()+"} "+this+" INTERRUPTED!");
                }
                waiting=false;
            } while(!shutdown);
        }

        /**
         * Return true if the thread is not associated to any runnable
         */
        protected boolean isFree() { return waiting; }

        /**
         * Return the associated Runnable, or <b>null</b>
         * @return the associated Runnable, or <b>null</b>
         */
        protected Runnable getRunnable() { return runnable; }

        public String toString() { return getName(); }

        /**
         * Finalizes terminates the controlled threads.
         */
        public void finalize() {
            terminate();
        }
    }

    /**
     * Return the next thread to run, as scheduled by the queue
     */
    private Runnable getNextThread() {
        if (queue.isEmpty()) return null;
        return (Runnable)queue.get();
    }

    /**
     * Create a pool with the given size using the given queue object.
     * @param size the size of the pool
     * @param daemon if <b>true</b> the pools will be daemon
     * @param queue the queue to use for determining the next thread to
     *        instantiate when there are pending start requests.
     *
     */
    public ThreadPool(int size, boolean daemon, Queue queue) {
        if (verbose) System.out.println("Creating thread pool of size "+size);
        this.queue=queue;
        this.pool=new PooledThread[size];
        ThreadGroup tg = new ThreadGroup("thread-pool");
        for(int i=0;i<size;i++) {
            pool[i]= new PooledThread(tg, i, daemon);
            pool[i].start();
        }

    }
    
    /**
     * Create a pool with the given size with a FIFO queue
     * @param size the size of the pool
     * @param daemon if <b>true</b> the pools will be daemon
     */
    public ThreadPool(int size, boolean daemon) {
        this(size, daemon, new FIFOQueue());
    }

    /**
     * Create a pool of daemon threads with the given size and a FIFO waiting queue
     * @param size the size of the pool
     */
    public ThreadPool(int size) {
        this(size, true);
    }
    
    /**
     * Resize the pool. 
     * <p>
     * The new size must be greater than 0.
     * If the new size is greater than the previous size, an attempt is made to add
     * threads to the pool.
     * <p>
     * If the old size is lower than the previous size, free threads are removed first.
     * If the new size requires that some busy thread is removed, the thread is interrupted as 
     * specified and removed from the pool, but it will continue running  until its natural 
     * termination until the executing code doesn't explicitly handle the interruption.
     *   
     * @param newSize
     * @param interrupt determines wether or not to invoke interrupt() on busy thread if necessary.
     */
    public void resize(int newSize, boolean interrupt) {
        throw new UnsupportedOperationException("Not implemented yet");
    }


    /**
     * Return the size of the pool
     * @return the size of the pool
     */
    public int size() { return pool.length; }

    /**
     * Return the number of thread currently queued
     * @return the number of thread currently queued
     */
    public int getQueueSize() { return queue.size(); }

    /**
     * Add a runnable object to the pool.
     * <p>
     * If there's a thread available, the runnable is associated to the thread
     * and started. Else, it is queued, and will run as soon as one thread becomes
     * available.
     * @param runnable the Runnable object to execute
     * @return <b>true</b> if the runnable is started, </b>false</b> if it's queued.
     */
    public boolean start(Runnable runnable) {
    	return start(runnable, false);
    }
    
    /**
     * Add a runnable object to the pool.
     * <p>
     * If there's a thread available, the runnable is associated to the thread
     * and started. Else, it is queued (unless failIfNoFree is <code>true</code>), and will run as soon as one thread becomes
     * available.
     * @param runnable the Runnable object to execute
     * @param failIfNoFree if <code>true</code>, returns false without queuing the request
     * @return <b>true</b> if the runnable is started, </b>false</b> if it's not started.
     */
    public boolean start(Runnable runnable, boolean failIfNoFree) {
        for(int i=0;i<size();i++) {
            if (pool[i].isFree()) {
				pool[i].setRunnable(runnable);
                return true;
            }
        }
        // Queue the thread
        if (!failIfNoFree) {
	        if (verbose) System.out.println("{"+Thread.currentThread()+"} "+"Queueing "+runnable);
	        queue.put(runnable);
        }
        return false;
    }

    /**
     * Return <b>true</b> if the Runnable is associated with any pooled thread.
     * @return <b>true</b> if the Runnable is associated with any pooled thread.
     */
    public synchronized boolean isAlive(Runnable r) {
        for(int i=0;i<pool.length;i++) {
            if (r == pool[i].getRunnable()) return true;
        }
        return false;
    }
    
    /**
     * Return an estimation of the number of threads in the pool currently associated to 
     * a runnable.
     * @return an estimation of the number of threads in the pool currently associated to 
     * a runnable.
     */
    public synchronized int getBusyCount() {
    	int c=0;
    	for(int i=0;i<size();i++) if (! pool[i].isFree()) c++;
    	return c;
    }

    /**
     * Unconditionally terminates all the threads in the pool.
     * Free threads simply exit. Threads associated to a Runnable
     * are interrupt()ed.
     */
    public void terminate() {
        shutdown=true;
        for(int i=0;i<pool.length;i++) {
            if (verbose) System.out.println("{"+Thread.currentThread()+"} "+"Interrupting "+pool[i].getName());
            pool[i].interrupt();
        }
    }
}  