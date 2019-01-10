package org.sadun.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple cache object, which holds at most <i>n</i> references.
 *
 * @author Cristiano Sadun
 */
public class Cache {

	private HashMap map;

	/**
	 * List of keys, in last-accessed order (first element is the oldest)
	 */
	private List accessList;
	private int max;

	/**
	 * Constructor for Cache.
	 * @param max the maximum number of references to hold.
	 */
	public Cache(int max) {
		if (max<2) throw new IllegalArgumentException("A cache can't be less than size 2");
		this.max=max;
		clear();
	}

	/**
	 * Put an object in the cache. If the cache size exceed the maximum size,
	 * the least accessed object will be removed.
	 *
	 * @param the key of the object to put.
	 * @return the object just inserted.
	 */
	public synchronized Object put(Object key, Object value) {
		if (map.size() >= max) {
			if (! (accessList.size() > 0)) throw new IllegalStateException("AccessList size is 0");
			// Remove the first element in the access List
			Object obj = accessList.get(0);
			accessList.remove(0);
			map.remove(obj);
			removed(obj);
		}

		used(key);

		return map.put(key, value);
	}

	/**
	 * Invoked after an object is removed from the cache.
	 * 
     * @param obj the removed object
     */
    protected void removed(Object obj) { }

    /**
	 * Remove an object from the cache.
	 *
	 * @param the key of the object to remove.
	 * @return null or the object corresponding to the key.
	 */
	public synchronized Object remove(Object key) {
		Object obj;
		if ((obj=map.remove(key))==null) return null;
		accessList.remove(key);
		removed(obj);
		return obj;
	}

	/**
	 * Find an object into the cache.
	 *
	 * @param key the key of the object to get.
	 * @return null or the object corresponding to the key.
	 */
	public synchronized Object get(Object key) {
		Object obj;
		if ((obj=map.get(key))==null) return null;

		used(key);

		return obj;
	}
	
	/**
	 * Marks the object as used.
	 * <p>
	 * This method can be invoked after method calls that make use of
	 * the object.
	 * 
	 * @param key
	 */
	public synchronized void used(Object key) {
        // Add the key to the access list, on top
	    boolean empty=accessList.isEmpty();
	    if ((! empty) && accessList.get(0)!=key) {
			accessList.remove(key);
			accessList.add(key);
	    } else if (empty) {
	        accessList.add(key);
	    } // else the object is already on top
	    
	}

	/**
	 * Clear the cache.
	 */
	protected void clear() {
		map=new HashMap();
		accessList=new LinkedList();
	}
	
	/**
	 * Return true if the cache contains the given key.
	 * <p>
	 * Note that this not guarantees that the corresponding
	 * object will actually exist in the cache in a later call
	 * to {@link #get(Object)}.
	 * 
	 * @param key the value to verify
	 * @return <b>true</b> if the key exist in the cache
	 */
	public boolean containsKey(Object key)  {
		return map.containsKey(key);
	}
	

}
