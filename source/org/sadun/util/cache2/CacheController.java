package org.sadun.util.cache2;

/**
 * Objects implementing this interface control when or not an item in a
 * {@link org.sadun.util.cache2.Cache} can be inserted and/or has expired.
 * <p>
 * Typically a controller implements some kind of criteria in
 * {@link #getExpirationState(Object)} to determine wether or not an item is to
 * be considered expired.
 * 
 * @author Cristiano Sadun
 */
public interface CacheController extends CacheListener {

    /**
     * Queries whether or not a key has expired. If the key does not exists in
     * the cache, returns <b>null</b>.
     * 
     * @param key
     *            the key for which to retrieve the expiration state
     * @return
     */
    public CacheExpirationState getExpirationState(Object key);

    /**
     * Queries whether or not a key can be inserted in the cache.
     * 
     * @param key
     *            key the key for which to determine if insertion is possible
     * @param value
     *            the value for which to determine if insertion is possible
     * @param keyExists true if the key already exists in the cache
     */
    public boolean canInsert(Object key, Object value, boolean keyExists);

    /**
     * Notifies the controller that a given key has been queued for expiration.
     * An {@link CacheListener#expired(Object, Object, CacheExpirationState)
     * expired} notification will be sent later on.
     * 
     * @param key
     *            the key which is due to expire
     */
    public void queuedForExpiration(Object key);
}