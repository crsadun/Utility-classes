package org.sadun.util.cache2;

/**
 * A class embedding the minimum amount of information about
 * the expiration state of an item in a {@link org.sadun.util.cache2.Cache},
 * namely the fact that has expired or not according to the cache's 
 * {@link org.sadun.util.cache2.CacheController expiration controller}.
 *
 * @author Cristiano Sadun
 */
public class CacheExpirationState {

    /**
     * A convenience constant 
     */
    public static final CacheExpirationState EXPIRED = new CacheExpirationState(true);

    public static final CacheExpirationState NOT_EXPIRED = new CacheExpirationState(false);
    
    private boolean hasExpired;

    /**
     * Create a state object based on the given boolean value
     * @param hasExpired true if the item has expired
     */
    public CacheExpirationState(boolean hasExpired) {
        this.hasExpired=hasExpired;
    }

    /**
     * Return the expiration state as a boolean.
     * @return the expiration state as a boolean..
     */
    public boolean hasExpired() {
        return hasExpired;
    }
    
    public String toString() {
        return (hasExpired ? "" :"not ")+"expired";
    }

}
