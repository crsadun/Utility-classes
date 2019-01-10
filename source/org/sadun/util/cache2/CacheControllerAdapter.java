package org.sadun.util.cache2;

public class CacheControllerAdapter implements CacheController {

    /**
     * This method always return null
     */
    public CacheExpirationState getExpirationState(Object key) {
        return null;
    }

    /**
     * This method always return true
     */
    public boolean canInsert(Object key, Object value, boolean keyExists) {
        return true;
    }

    /**
     * This method does nothing.
     */
    public void queuedForExpiration(Object key) {
    }

    /**
     * This method does nothing.
     */
    public void inserted(Object key, Object value, boolean replaced) {
    }

    /**
     * This method does nothing.
     */
    public void expired(Object key, Object value,
            CacheExpirationState expirationData) {
    }

    /**
     * This method does nothing.
     */
    public void totalExpired(long expirationTime, int total, int remaining) {
    }

}
