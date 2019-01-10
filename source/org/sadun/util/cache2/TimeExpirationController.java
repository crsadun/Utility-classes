package org.sadun.util.cache2;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link org.sadun.util.cache2.CacheController} which expires items
 * using time elapsed for insertion.
 * 
 * @author Cristiano Sadun
 */
public class TimeExpirationController extends CacheAdapter implements
        CacheController {

    /**
     * A subclass of {@link CacheExpirationState} that is created when using a
     * {@link Cache} with a {@link TimeExpirationController}. It carries
     * additional information about insertion time and elapsed time.
     * 
     * @author Cristiano Sadun
     */
    public class TimeExpirationState extends CacheExpirationState {

        private long insertionTime;
        private long elapsedTime;

        private TimeExpirationState(long insertionTime, long elapsedTime) {
            super(elapsedTime >= expirationTime);
            this.insertionTime = insertionTime;
            this.elapsedTime = elapsedTime;
        }

        /**
         * Return the time elapsed since insertion into the {@link Cache cache}
         * @return the time elapsed since insertion into the {@link Cache cache}
         */
        public long getElapsedTime() {
            return elapsedTime;
        }

        /**
         * Return the time of insertion into the {@link Cache cache}
         * @return the time of insertion into the {@link Cache cache}
         */
        public long getInsertionTime() {
            return insertionTime;
        }

        /**
         * Retrieve the lifetime interval at the moment of the call
         * @return
         */
        public long getLifetime() {
            return elapsedTime - insertionTime;
        }

    }

    private long expirationTime;
    private Map entryTimes = new HashMap();

    /**
     * Create a controller which will declare items expired after
     * a certain amount of time.
     * 
     * @param expirationTime the time after which the items will be reported
     * expired. 
     */
    public TimeExpirationController(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * Implementation the {@link CacheListener} interface.
     */
    public void inserted(Object key, Object value, boolean replaced) {
        entryTimes.put(key, new Long(System.currentTimeMillis()));
    }

    /**
     * Implementation the {@link CacheListener} interface.
     */
    public void expired(Object key, Object value,
            CacheExpirationState expirationData) {
        if (expirationData.hasExpired())
            entryTimes.remove(key);
    }

    /**
     * Return an {@link TimeExpirationState object} embedding information about the expiration 
     * state of the item associated with the given key.
     * <p>
     * If the key does not exist in the cache, return null.
     */
    public CacheExpirationState getExpirationState(Object key) {
        long now = System.currentTimeMillis();

        Long l = (Long) entryTimes.get(key);
        if (l == null)
            return null;

        long insertionTime = l.longValue();
        long elapsed = now - insertionTime;
        return new TimeExpirationState(insertionTime, elapsed);
    }
    
    /**
     * Always return true.
     */
    public boolean canInsert(Object key, Object value, boolean exists) {
        return true;
    }
    
    /**
     * This method does nothing
     */
    public void queuedForExpiration(Object key) {
        // Nothing to do
    }
    
    public String toString() {
        return "time-based cache controller";
    }

}
