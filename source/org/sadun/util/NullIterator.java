package org.sadun.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator which has no elements.
 * 
 * @version 1.0
 * @author Cristiano Sadun
 */
public class NullIterator implements Iterator {

    /**
     * Always return false.
     */
    public boolean hasNext() {
        return false;
    }
    
    /**
     * Always throws a <code>NoSuchElementException</code>.
     */
    public Object next() {
        throw new NoSuchElementException();
    }
    
    /**
     * Not supported operation.
     */
    public void remove() {
        throw new UnsupportedOperationException();

    }
}
