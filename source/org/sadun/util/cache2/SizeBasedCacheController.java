package org.sadun.util.cache2;

/**
 * A size-based controller.
 * 
 * 
 * @author Cristiano Sadun
 */
public class SizeBasedCacheController implements CacheController {

    private static final boolean debug = true;
    private int maxSize;
    private int size;
    private int queuedForExpiration;

    public SizeBasedCacheController(int maxSize) {
        this.size = 0;
        this.queuedForExpiration = 0;
        this.maxSize = maxSize;
    }

    public CacheExpirationState getExpirationState(Object key) {
        if (size - queuedForExpiration >= maxSize)
            return CacheExpirationState.EXPIRED;
        else
            return CacheExpirationState.NOT_EXPIRED;
    }

    public void expired(Object key, Object value,
            CacheExpirationState expirationData) {
        size--;
        queuedForExpiration--;
        if (debug)
            System.err.println("Removed: " + key + " (size: " + size
                    + ", queued for expiration: " + queuedForExpiration + ")");
    }

    public void queuedForExpiration(Object key) {
        if (debug)
            System.err.println("Queued for expiration: " + key + " (size: "
                    + size + ", queued for expiration: " + queuedForExpiration
                    + ")");
        queuedForExpiration++;

    }

    public void inserted(Object key, Object value, boolean replaced) {
        if (!replaced) size++;
        if (debug)
            System.err.println("Inserted: " + key + " (size: " + size
                    + ", queued for expiration: " + queuedForExpiration + ")");

    }

    public void totalExpired(long expirationTime, int total, int remaining) {
        // Nothing to do
    }

    /**
     * Return the maximum size
     * 
     * @return the maximum size
     */
    public int getMaxSize() {
        return maxSize;
    }

    public boolean canInsert(Object key, Object value, boolean keyExists) {
        if (debug)
            System.err.println("Checking if can be inserted: "
                    + key
                    + ".. "
                    + (keyExists ? "yes since key already exists" : (size
                            - queuedForExpiration < maxSize ? "yes" : "no")));

        return keyExists ? true : size - queuedForExpiration < maxSize;
    }

    public static void main(String[] args) {
        Cache cache = new Cache(new SizeBasedCacheController(2));

        cache.put("Hello", "World");
        cache.put("Hello", "World2");
        cache.put("Hello2", "World3");
    }

}
