package org.sadun.util.cache2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * This class implements a key/value cache where the expiration criterium for
 * the cached values is determined by an external
 * {@link org.sadun.util.cache2.CacheController controller}.
 * <p>
 * Upon expiration of one or more entries, it produces expiration events which
 * can be intercepted by {@link #addSubcacheListener(ExpirationListener)
 * registering} an {@link org.sadun.util.TimeBasedCache.ExpirationListener}
 * object.
 * 
 * @author Cristiano Sadun
 */
public class Cache {

    public class CleanerThread extends Thread {

        private long checkInterval;

        public CleanerThread(long checkInterval) {
            super("Cleaner thread for "+(name == null ? "anonymous cache" : "cache \""+name+"\""));
            setDaemon(true);
            this.checkInterval=checkInterval;
        }

        public void run() {
            while(true) {
                try {
                    Thread.sleep(checkInterval);
                } catch (InterruptedException e) {
                }
                expire();
            }
        }

    }

    private Map map;
    private Object lock = new Object();
    private CacheController controller;
    private Set cacheListeners = new HashSet();
    private boolean expiring;
    private String name;
    
    /**
     * Create a cache using the given map object as underlying storage and the
     * given controller to determine wether or not an entry is expired.
     * 
     * @param name the (optional) name of the cache - can be null
     * @param map
     * @param controller
     */
    public Cache(String name, Map map, CacheController controller) {
        this.name=name;
        this.map = map;
        this.controller = controller;
    }

    /**
     * Create a cache using the given controller to determine wether or not an
     * entry is expired.
     * 
     * @param name the (optional) name of the cache - can be null
     * @param criterium
     */
    public Cache(String name, CacheController criterium) {
        this(name, new HashMap(), criterium);
    }
    
    /**
     * Create a cache using the given map object as underlying storage and the
     * given controller to determine wether or not an entry is expired.
     * 
     * @param map
     * @param controller
     */
    public Cache(Map map, CacheController controller) {
        this(null, map,controller);
    }

    /**
     * Create a cache using the given controller to determine wether or not an
     * entry is expired.
     * 
     * @param criterium
     */
    public Cache(CacheController criterium) {
        this(null, new HashMap(), criterium);
    }

    /**
     * Places a key/value pair in the cache.
     * <p>
     * If the {@link CacheController controller} indicates that the
     * pair be inserted, an {@link OperationDeniedException} is thrown.
     * <p>
     * This method triggers entry expirations as necessary.
     * 
     * @param key
     * @param value
     * @return the replaced object, or null if no object was replaced
     */
    public Object put(Object key, Object value) throws OperationDeniedException {
        Object replaced;
        synchronized (lock) {
            expire0();
            if (!controller.canInsert(key, value, map.containsKey(key))) 
                throw new OperationDeniedException(key, value);
            replaced = map.put(key, value);
            controller.inserted(key, value, replaced!=null);
        }
        notifyInsertion(key, value, replaced!=null);
        return replaced; 
    }

    /**
     * Retrieves a value given a key.
     * <p>
     * This method triggers entry expirations as necessary.
     * 
     * @param key
     * @return
     */
    public Object get(Object key) {
        return get(key, true);
    }

    /**
     * Retrieves a value given a key, wit the possiblity of expiring entries as
     * necessary
     * 
     * @param performExpiration
     * @param key
     * @return
     */
    public Object get(Object key, boolean performExpiration) {
        if (performExpiration)
            synchronized (lock) {
                expire0();
            }
        return map.get(key);
    }

    /**
     * Removes any expired key/value pair.
     * 
     * @return the number of expired keys.
     */
    public int expire() {
        synchronized (lock) {
            return expire0();
        }
    }

    /**
     * Return true if the given key does not exist or has expired
     * 
     * @param key
     * @return true if the given key does not exist or has expired
     */
    public boolean hasExpired(Object key) {
        CacheExpirationState data = controller.getExpirationState(key);
        if (data == null)
            return true;
        return data.hasExpired();
    }

    // Must be called in a synchronized block
    private int expire0() {
        if (expiring) {
            return 0;
        }
        
        expiring = true;
        long now = System.currentTimeMillis();

        Iterator i = map.keySet().iterator();
        Set toRemove = new HashSet();
        while (i.hasNext()) {
            Object key = i.next();
            CacheExpirationState data = controller.getExpirationState(key);
            if (data==null) 
                throw new RuntimeException("No expiration data for etnry "+key+" in "+controller+" on "+toString());
            if (data.hasExpired()) {
                toRemove.add(new Object[] { key, data });
                controller.queuedForExpiration(key);
            }
        }

        i = toRemove.iterator();
        while (i.hasNext()) {
            Object obj[] = (Object[]) i.next();
            Object key = obj[0];
            CacheExpirationState data = (CacheExpirationState) obj[1];
            Object value = map.remove(key);
            controller.expired(key, value, data);
            notifyRemoval(key, value, data);
        }
        notifyTotalExpired(now, toRemove.size(), map.size());
        expiring = false;
        return toRemove.size();
    }

    private void notifyTotalExpired(long expirationTime, int total,
            int remaining) {
        Iterator i = new ArrayList(cacheListeners).iterator();
        while (i.hasNext()) {
            ((CacheListener) i.next()).totalExpired(expirationTime, total,
                    remaining);
        }
    }

    private void notifyRemoval(Object key, Object value,
            CacheExpirationState data) {
        Iterator i = new ArrayList(cacheListeners).iterator();
        while (i.hasNext()) {
            ((CacheListener) i.next()).expired(key, value, data);
        }
    }

    private void notifyInsertion(Object key, Object value, boolean replaced) {
        Iterator i = new ArrayList(cacheListeners).iterator();
        while (i.hasNext()) {
            ((CacheListener) i.next()).inserted(key, value, replaced);
        }
    }

    /**
     * Return the current size of the cache.
     * <p>
     * This method triggers entry expirations as necessary.
     * 
     * @return
     */
    public int size() {
        synchronized (lock) {
            expire0();
            return map.size();
        }
    }

    /**
     * Remove an entry from the cache.
     * 
     * @param key
     */
    public void remove(Object key) {
        synchronized (lock) {
            map.remove(key);
        }
    }

    /**
     * Returns the key set of the cache.
     * <p>
     * Note that the keys contained in the set may be associate to entries which
     * will be expired upon later reading, so null may be returned when
     * {@link #get(Object) retrieving objects  by key}.
     * 
     * @return the key set of the cache.
     */
    public Set keySet() {
        return new HashSet(map.keySet());
    }

    /**
     * Add a listener to expiration events.
     * 
     * @param listener
     */
    public void addCacheListener(CacheListener listener) {
        cacheListeners.add(listener);
    }

    /**
     * Remove a listener to expiration events.
     * 
     * @param listener
     */
    public void removeCacheListener(CacheListener listener) {
        cacheListeners.remove(listener);
    }

    /**
     * Return the size of the cache at the moment of the call. Unlike
     * {@link #size()} no expiration is performed, so expired entries still
     * present in the cache do count.
     * <p>
     * Note that such size may subsequently change if some entries expire.
     * 
     * @return the size of the cache at the moment of the call.
     */
    public int currentSize() {
        synchronized (lock) {
            return map.size();
        }
    }
    
    public String toString() {
        if (name==null) return super.toString();
        else return name;
    }
    
    public Thread createCleanerThread(long checkInterval) {
        return new CleanerThread(checkInterval);
    }

    public CacheController getController() {
        return controller;
    }

    public Set contentsSet() {
        Set s = new HashSet();
        Iterator i = map.entrySet().iterator();
        while(i.hasNext()) {
            Map.Entry entry = (Entry) i.next();
            s.add(entry.getValue());
        }
        return s;
    }

}
