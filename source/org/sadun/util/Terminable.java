package org.sadun.util;

/**
 * This interface extends the standard Runnable by providing
 * methods to gracefully shut down a Thread.
 *
 * @author C. Sadun
 * @version 1.0
 */
public interface Terminable extends Runnable {

    /**
     * Unconditionally requests a shutdown. The runnable will complete
     * its last atomic operation, and gracefully exit the run() method.
     */
    public void shutdown();

    /**
     * Return <b>true</b> if a shutdown has been requested.
     * @return <b>true</b> if a shutdown has been requested
     */
    public boolean isShuttingDown();

}