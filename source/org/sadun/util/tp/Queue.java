package org.sadun.util.tp;

/**
 * A simple queue interface for Runnable objects.
 * <p>
 * This queue may decide the order with which a certain Runnable object
 * may be selected by implementing appropriately the {@link Queue#get() get()} method.
 *
 * @author C. Sadun
 * @version 1.0
 */
public interface Queue {

    /**
     * Add a runnable object to the queue
     * @param obj the runnable to be added to the queue
     */
    public void put(Runnable obj);

    /**
     * Fetch a runnable object from the queue
     * @param obj the runnable to be fetched from o the queue
     */
    public Runnable get();

    /**
     * Return the current size of the queue
     * @return the current size of the queue
     */
    public int size();

    /**
     * Return <b>true</b> if the queue is empty
     * @return <b>true</b> if the queue is empty
     */
    public boolean isEmpty();

}