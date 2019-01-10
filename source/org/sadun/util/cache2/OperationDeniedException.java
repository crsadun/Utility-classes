package org.sadun.util.cache2;

/**
 * This exception indicates that a {@link org.sadun.util.cache2.CacheController}
 * has denied the insertion of an item in a cache. 
 *
 * @author Cristiano Sadun
 */
public class OperationDeniedException extends RuntimeException {

    private Object key;
    private Object value;
    
    public OperationDeniedException(Object key, Object value) {
        this.key=key;
        this.value=value;
    }

    /**
     * Return the key that was denied insertion
     * @return the key that was denied insertion
     */
    public Object getKey() {
        return key;
    }

    /**
     * Return the value that was denied insertion
     * @return the value that was denied insertion
     */
    public Object getValue() {
        return value;
    }

}
