package com.deltax.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple FIFO queue.
 *
 * @author Cris Sadun
 * @version 1.2
 */
public class FIFOQueue {

    private List v;

    /**
     * Create an empty queue
     */
    public FIFOQueue() {
        v = new ArrayList();
    }

    /**
     * Clear the queue
     */
    public void clear() {
        v = new ArrayList();
    }

    /**
     * Return the queue head
     * @return the queue head
     * @exception QueueEmptyException if the queue is empty
     */
    public Object get()
        throws QueueEmptyException {
        //synchronized(v) {
            if(isEmpty())
                throw new QueueEmptyException();
            Object obj1 = v.get(0);
            v.remove(0);
            Object obj = obj1;
            return obj;
        //}
    }
    
    public Object peek(int i) {
    	return v.get(i);
    }
    
	public Object peek() {
		return peek(0);
	}

    /**
     * Return the queue head, as a byte
     * @return the queue head
     * @exception QueueEmptyException if the queue is empty
     * @exception ClassCastException if the queue head is not a Byte object
     */
    public byte getByte()
        throws QueueEmptyException {
        return ((Byte)get()).byteValue();
    }

    /**
     * Return the queue head, as a char
     * @return the queue head
     * @exception QueueEmptyException if the queue is empty
     * @exception ClassCastException if the queue head is not a Character object
     */
    public char getChar()
        throws QueueEmptyException {
        return ((Character)get()).charValue();
    }

    /**
     * Return the queue head, as a double
     * @return the queue head
     * @exception QueueEmptyException if the queue is empty
     * @exception ClassCastException if the queue head is not a Double object
     */
    public double getDouble()
        throws QueueEmptyException {
        return ((Double)get()).doubleValue();
    }

    /**
     * Return the queue head, as a float
     * @return the queue head
     * @exception QueueEmptyException if the queue is empty
     * @exception ClassCastException if the queue head is not a Float object
     */
    public float getFloat()
        throws QueueEmptyException {
        return ((Float)get()).floatValue();
    }

    /**
     * Return the queue head, as a int
     * @return the queue head
     * @exception QueueEmptyException if the queue is empty
     * @exception ClassCastException if the queue head is not an Integer object
     */
    public int getInteger()
        throws QueueEmptyException {
        return ((Integer)get()).intValue();
    }

    /**
     * Return the queue head, as a long
     * @return the queue head
     * @exception QueueEmptyException if the queue is empty
     * @exception ClassCastException if the queue head is not a Long object
     */
    public long getLong()
        throws QueueEmptyException {
        return ((Long)get()).longValue();
    }

    /**
     * Return the queue head, as a String
     * @return the queue head
     * @exception QueueEmptyException if the queue is empty
     * @exception ClassCastException if the queue head is not a String object
     */
    public String getString()
        throws QueueEmptyException {
        return (String)get();
    }

    /**
     * Return the queue head, as a boolean
     * @return the queue head
     * @exception QueueEmptyException if the queue is empty
     * @exception ClassCastException if the queue head is not a Boolean object
     */
    public boolean getBoolean()
        throws QueueEmptyException {
        return ((Boolean)get()).booleanValue();
    }

    /**
     * Return <b>true</b> if the queue is empty.
     * @return <b>true</b> if the queue is empty
     */
    public boolean isEmpty() {
        return v.size() == 0;
    }

    /**
     * Return the size of the queue
     */
    public int size() { return v.size(); }

    /**
     * Insert a byte in the queue
     * @param x the element to be inserted
     */
    public void put(byte x) {
        put(new Byte(x));
    }

    /**
     * Insert a char in the queue
     * @param x the element to be inserted
     */
    public void put(char x) {
        put(new Character(x));
    }

    /**
     * Insert a double in the queue
     * @param x the element to be inserted
     */
    public void put(double x) {
        put(new Double(x));
    }

    /**
     * Insert a float in the queue
     * @param x the element to be inserted
     */
    public void put(float x) {
        put(new Float(x));
    }

    /**
     * Insert an integer in the queue
     * @param x the element to be inserted
     */
    public void put(int x) {
        put(new Integer(x));
    }

    /**
     * Insert a long in the queue
     * @param x the element to be inserted
     */
    public void put(long x) {
        put(new Long(x));
    }

    /**
     * Insert a boolean in the queue
     * @param x the element to be inserted
     */
    public void put(boolean x) {
        put(new Boolean(x));
    }

    /**
     * Insert an Object in the queue
     * @param obj the object to be inserted
     */
    public void put(Object obj) {
        v.add(obj);
    }
}