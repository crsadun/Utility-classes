package org.sadun.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 *
 * @author Cristiano Sadun
 */
public class ArrayIterator implements Iterator {

    Object [] array;
    int current;
    
    /**
     * 
     */
    public ArrayIterator(Object [] array) {
       this.array=array;
       this.current=0;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return current<array.length;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next() {
        if (!hasNext()) throw new NoSuchElementException();
        return array[current++];
    }

}
