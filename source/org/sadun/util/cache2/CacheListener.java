package org.sadun.util.cache2;

/**
 * Classes implementing this interface can receive notifications
 * by {@link org.sadun.util.cache2.Cache} objects (if they 
 * {@link org.sadun.util.cache2.Cache#addCacheListener(CacheListener) register}).

 *
 * @author Cristiano Sadun
 */
public interface CacheListener {
    
    /** 
     * Invoked when a key has been added to the cache
     * 
     * @param key  the added key
     * @param value the added value
     * @param replaced true if the same key already existed in the cache
     */
    public void inserted(Object key, Object value, boolean replaced);
    
    /**
     * invoked when a key has expired
     * 
     * @param key the expired key
     */
    public void expired(Object key, Object value, CacheExpirationState expirationData);
    
    /**
     * Notify that a certain number of entries have expired
     * at a given time.
     * 
     * @param expirationTime
     * @param total the total expired elements
     * @param remaining the total remaining elements
     */
    void totalExpired(long expirationTime, int total, int remaining);

}