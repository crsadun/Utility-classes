package org.sadun.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sadun.util.Broadcaster.BroadcastingException;
import org.sadun.util.SequentialUniqueIdGenerator.IdStorage.IdStorageException;

/**
 * An unique id generator which creates ids unique across different JVMs and
 * sequential.
 * 
 * @version 1.0
 * @author Cristiano Sadun
 */
public class SequentialUniqueIdGenerator {

    /**
     * 
     * @author Cristiano Sadun
     */
    private class CannotUnlockStorageException extends IdStorageException {

        /**
         * @param t
         */
        public CannotUnlockStorageException(Throwable t) {
            super(t);
            // TODO Auto-generated constructor stub
        }

    }

    /**
     * Classes implementing this interface allow access to a shared storage of
     * ids to use with a {@link SequentialUniqueIdGenerator}.
     * <p>
     * The storage can be slow, for example based on a remote object access or a
     * database.
     * 
     * @version 1.0
     * @author Cristiano Sadun
     */
    public interface IdStorage {

        public static class IdStorageException extends Exception {
            public IdStorageException(Throwable t) {
                super(t);
            }
        }

        /**
         * Lock the storage.
         * <p>
         * This method attempts to gain control of the storage and must block
         * until the control is obtained or a timeout is expired.
         * 
         * @exception IdStorageException
         *                if the lock cannot be obtained
         */
        public void lock() throws IdStorageException;

        /**
         * Return an iterator over the next block of ids. The
         * <code>next()</code> operation of the iterator may return any
         * object, but their string representation will be used. Therefore, the
         * iterator must return a sequence of objects whose string
         * representation is unique.
         * <p>
         * The storage must be left in a state such another process will not
         * retrieve the same block.
         * 
         * @exception IdStorageException
         *                if an exception occurs while using the
         *                {@link SequentialUniqueIdGenerator.IdStorage}
         * @exception IllegalStateException
         *                if the storage is unlocked
         * @return an iterator over the next block of ids.
         */
        public Iterator getNextIdBlock() throws IdStorageException,
                IllegalStateException;

        /**
         * Unlock the storage.
         * <p>
         * This method must release control of the storage.
         *  
         */
        public void unlock() throws IdStorageException;
    }

    private static Map instances = new HashMap();

    private IdStorage idStorage;
    private Iterator currentBlock = new NullIterator();
    private Broadcaster failureBroadcaster;

    /**
     * Create a sequential generator using the given {@link IdStorage}.
     * 
     * @param storage
     *            the id storage to use
     */
    public SequentialUniqueIdGenerator(IdStorage storage) {
        this.idStorage = storage;
    }

    /**
     * Create a sequential generator
     */
    protected SequentialUniqueIdGenerator() {

    }

    /**
     * Return the next id. This operation is potentially blocking, or may
     * trigger an {@link OperationTimedoutException}if the {@link IdStorage}
     * used by this {@link SequentialUniqueIdGenerator}imposes a timeout on the
     * {@link IdStorage#lock() storage locking operation}.
     * 
     * @return the next id.
     * @throws OperationTimedoutException
     *             if a timeout is raised when {@link IdStorage#lock()locking}
     *             the storage
     */
    public synchronized String getNextId() throws IdStorage.IdStorageException {
        if (idStorage == null)
            throw new IllegalStateException("No " + IdStorage.class.getName()
                    + " setup. Please invoke setIdStorage().");
        if (!currentBlock.hasNext()) {
            try {
                idStorage.lock();
                currentBlock = idStorage.getNextIdBlock();
            } catch (IdStorage.IdStorageException e) {
                currentBlock = new NullIterator(); // Ensure next call retries
                throw e;
            } finally {
                try {
                    idStorage.unlock();
                } catch (IdStorage.IdStorageException e) {
                    // An exception here may deadlock the system, so we need to
                    // broadcast the situation if possible
                    if (failureBroadcaster != null) {
                        try {
                            failureBroadcaster
                                    .broadCast(new CannotUnlockStorageException(
                                            e));
                        } catch (BroadcastingException e1) {
                            System.err
                                    .println("Warining! Could not broadcast an unlocking problem with the IdStorage."
                                            + " Deadlocks might occur in other processes.");
                        }
                    }

                }
            }
        }
        return currentBlock.next().toString();
    }

    /**
     * Set the idStorage.
     * 
     * @param idStorage
     */
    protected void setIdStorage(IdStorage idStorage) {
        if (idStorage != null)
            throw new IllegalStateException(
                    "The id storage can be initialized only once.");
        this.idStorage = idStorage;
    }

    /**
     * Return the current failure broadcaster. This broadcaster is used if the
     * {@link IdStorage}used by this generator cannot be unlocked, potentially
     * leaving other id generators in a deadlocked state.
     * 
     * @return the current failure broadcaster.
     */
    public Broadcaster getFailureBroadcaster() {
        return failureBroadcaster;
    }

    /**
     * Set the failure broadcaster. This broadcaster is used if the
     * {@link IdStorage}used by this generator cannot be unlocked, potentially
     * leaving other id generators in a deadlocked state.
     * <p>
     * If set to <b>null </b>, the current broadcaster (if any) is removed.
     * 
     * @param failureBroadcaster
     */
    public void setFailureBroadcaster(Broadcaster failureBroadcaster) {
        if (failureBroadcaster == null)
            if (this.failureBroadcaster != null)
                try {
                    this.failureBroadcaster.dispose();
                } catch (Broadcaster.BroadcastingException e) {
                    System.err
                            .println("Warning: the current broadcaster object "
                                    + failureBroadcaster
                                    + " could not be disposed");
                }
        this.failureBroadcaster = failureBroadcaster;
    }
}