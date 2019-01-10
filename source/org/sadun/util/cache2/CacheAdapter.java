package org.sadun.util.cache2;

/**
 * An implementation of {@link org.sadun.util.cache2.CacheListener}
 * which does nothing on every method.
 *
 * @author Cristiano Sadun
 */
public class CacheAdapter implements CacheListener {

    /**
     * This method does nothing.
     */
    public void expired(Object key, Object value, CacheExpirationState expirationData) {
    }
    
    /**
     * This method does nothing.
     */
    public void inserted(Object key, Object value, boolean replaced) {
    }
    
    /**
     * This method does nothing.
     */
    public void totalExpired(long expirationTime, int total, int remaining) {
    }

}
