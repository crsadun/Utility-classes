package org.sadun.util.tp;


/**
 * A thread which acts as a pool for
 * other threads. Its run methods simply waits if there aren't
 * runnable start requests.
 * <p>
 * Albeit it is not type-compatible with {@link ThreadPool ThreadPool} (since
 * it extends Thread) this class exposes the same methods.
 *
 * @author Cris Sadun
 * @version 1.0
 */
public class ThreadPoolThread extends Thread implements org.sadun.util.Terminable {

    private ThreadPool tp;
    private boolean shutdown=false;

    /**
     * Create a pool with the given size using the given queue object.
     * @param size the size of the pool
     * @param daemon if <b>true</b> the pools will be daemon
     * @param queue the queue to use for determining the next thread to
     *        instantiate when there are pending start requests.
     *
     */
    public ThreadPoolThread(int size, boolean daemon, Queue queue) {
        tp=new ThreadPool(size, daemon, queue);
    }

    /**
     * Create a pool with the given size with a FIFO queue
     * @param size the size of the pool
     * @param daemon if <b>true</b> the pools will be daemon
     */
    public ThreadPoolThread(int size, boolean daemon) {
        tp = new ThreadPool(size, daemon);
    }

    /**
     * Create a pool of daemon threads with the given size and a FIFO waiting queue
     * @param size the size of the pool
     */
    public ThreadPoolThread(int size) {
        tp = new ThreadPool(size);
    }

    /**
     * Return the size of the pool
     * @return the size of the pool
     */
    public int size() { return tp.size(); }

    /**
     * Return the number of thread currently queued
     * @return the number of thread currently queued
     */
    public int getQueueSize() { return tp.getQueueSize(); }

    /**
     * Adds a runnable object to the pool.
     * If there's a thread available, the runnable is associated to the thread
     * and started. Else, it is queued, and will run as soon as one thread becomes
     * available.
     * @param runnable the Runnable object to execute
     * @return <b>true</b> if the runnable is started, </b>false</b> if it's queued.
     */
    public boolean start(Runnable runnable) {
        return tp.start(runnable);
    }

    public void shutdown() { shutdown=true; }

    public boolean isShuttingDown() { return shutdown; }

    public synchronized void run() {
        while(!shutdown) {
        }
    }
}