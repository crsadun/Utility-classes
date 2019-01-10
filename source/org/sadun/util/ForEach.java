package org.sadun.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * {@link #execute() Execute} the same action for each object in a collection, optionally filtering the objects by the given filter.
 * <p>
 * By default, ForEach doesn't do anything, so the typical way to employ ForEach is by subclassing anonymously
 * overriding the method <a href="#action(java.lang.Object)>action()</a>.  For example
 * <p><pre>
 * List list;
 *  ...<i>create and populate list</i>...
 * new ForEach(l) {
 *   public void action(Object obj) {
 *      System.out.println(obj);
 *	  }
 * }.execute();
 * </pre>
 * prints all the elements of a List collection (the default implementation of <a href="#action(java.lang.Object)>action()</a> does exactly this).
 * <p>
 * The class also carries a <I>state</i>, initially null (or zero). The state can be set with any of the {@link #setState(Object) setState()} overloads
 * and is returned by any one of the {@link #execute()},{@link #executeBoolean()},{@link #executeByte()},{@link #executeChar()},{@link #executeDouble()},
 * {@link #executeFloat()},{@link #executeInt()},{@link #executeLong()},{@link #executeShort()},{@link #executeString()} methods.
 * <p>
 * For example
 * <p><pre>
 * List list;
 *  ...<i>create and populate a list of lists</i>...
 * if(
 *    new ForEach(l) {
 *      public void action(Object obj) {
 *         if ( ((List)obj).contains("mystring")) setState(true);
 *	    }
 *    }.executeBoolean() == true) System.out.println("'mystring' found");
 * </pre>
 * prints out 'mystring found' if a given list in a list of lists contains the string "mystring".
 * 
 */
public class ForEach {

	/**
	 * Users can implement this interface (anonymously or via a named class) to apply the action
	 * only to a subset of the objects in the collection.
	 */
	public interface Filter {
		/**
		 * Determines if the action is to be applied to the object or not
		 * @param obj the object to verify
		 * @return <b>true</b> if the action must be applied to the object
		 */
		public boolean accept(Object obj);
	}

	/**
	 * A Null filter, which doesn't filter any object
	 */
	private static class NullFilter implements Filter {
		/**
		 * Returns always true
		 * @param obj the object to verify
		 * @return <b>true</b> always
		 */
		public boolean accept(Object obj) { return true; }
	}

	private Iterator i;
	private Filter filter;
	private boolean atStart=true;
	private Object state;

	/**
	 * Creates a ForEach object applying to the elements of the given collection satisfying the given filter
	 * @param c the collection on whose elements which the action will be executed
	 * @param filter the filter to apply
	 */
	public ForEach(Collection c, Filter filter) {
		this(c.iterator(), filter);
	}

	/**
	 * Creates a ForEach object applying to all the elements of the given collection
	 * @param c the collection on whose elements which the action will be executed
	 */
	public ForEach(Collection c) {
		this(c, null);
	}

	/**
	 * Creates a ForEach object applying to the elements of the given array satisfying the given filter
	 * @param array the array on whose elements which the action will be executed
	 * @param filter the filter to apply
	 */
	public ForEach(Object [] array, Filter filter) {
		this(Arrays.asList(array), filter);
	}

	/**
	 * Creates a ForEach object applying to all the elements of the given array
	 * @param array the array on whose elements which the action will be executed
	 */
	public ForEach(Object [] array) {
		this(array, null);
	}

	/**
	 * Creates a ForEach object applying to the elements of the given iterator satisfying the given filter
	 * @param i an iterator, on whose elements which the action will be executed
	 * @param filter the filter to apply
	 */
	public ForEach(Iterator i, Filter filter) {
		if (i==null) throw new IllegalArgumentException("Collection can't be 'null'");
		if (filter==null) filter=new NullFilter();
		this.i=i;
		this.filter=filter;
	}

	/**
	 * Creates a ForEach object applying to all the elements of the given iterator
	 * @param i an iterator, on whose elements which the action will be executed
	 */
	public ForEach(Iterator i) {
		this(i, null);
	}


	/**
	 * Set the object state. This method can be invoked within {@link #action(Object)} to set the global state of the
	 * cycle.
	 * 
	 * @param state the value to which to set the state
	 */
	public void setState(Object state) {
		this.state=state;
	}
	
	/**
	 * Set the object state. This method can be invoked within {@link #action(Object)} to set the global state of the
	 * cycle.
	 * 
	 * @param state the value to which to set the state
	 */
	public void setState(int state) {
		this.state=new Integer(state);
	}
	
	/**
	 * Set the object state. This method can be invoked within {@link #action(Object)} to set the global state of the
	 * cycle.
	 * 
	 * @param state the value to which to set the state
	 */
	public void setState(byte state) {
		this.state=new Byte(state);
	}
	
	/**
	 * Set the object state. This method can be invoked within {@link #action(Object)} to set the global state of the
	 * cycle.
	 * 
	 * @param state the value to which to set the state
	 */
	public void setState(char state) {
		this.state=new Character(state);
	}
	
	/**
	 * Set the object state. This method can be invoked within {@link #action(Object)} to set the global state of the
	 * cycle.
	 * 
	 * @param state the value to which to set the state
	 */
	public void setState(long state) {
		this.state=new Long(state);
	}
	
	/**
	 * Set the object state. This method can be invoked within {@link #action(Object)} to set the global state of the
	 * cycle.
	 * 
	 * @param state the value to which to set the state
	 */
	public void setState(float state) {
		this.state=new Float(state);
	}
	
	/**
	 * Set the object state. This method can be invoked within {@link #action(Object)} to set the global state of the
	 * cycle.
	 * 
	 * @param state the value to which to set the state
	 */
	public void setState(double state) {
		this.state=new Double(state);
	}
	
	/**
	 * Set the object state. This method can be invoked within {@link #action(Object)} to set the global state of the
	 * cycle.
	 * 
	 * @param state the value to which to set the state
	 */
	public void setState(boolean state) {
		this.state=new Boolean(state);
	}
	
	/**
	 * Executes the action of the elements of the collection/iterator. If a filter has been defined,
	 * the action is executed only on the elements satisfying the filter.
	 * <p>
	 * An action can set the state of the object by using one of the {@link #setState(Object) setState()} overloads.
	 * 
	 * @return the resulting state object
	 * @exception IllegalStateException if execute has already been invoked
	 */
	public Object execute() {
		if (! atStart) throw new IllegalStateException("execute() already invoked: use reInit() before reinvoking");
		for(;i.hasNext();) {
			Object obj = i.next();
			if (filter.accept(obj)) action(obj);
		}
		atStart=false;
		return state;
	}
		
	/**
	 * Executes the action of the elements of the collection/iterator. If a filter has been defined,
	 * the action is executed only on the elements satisfying the filter.
	 * <p>
	 * An action can set the state of the object by using one of the {@link #setState(Object) setState()} overloads.
	 * 
	 * @return the resulting state 
	 * @exception IllegalStateException if execute has already been invoked
	 */
	public byte executeByte() {
		execute();
		if (state==null) return 0;
		return ((Byte)TypeWrapper.convertValue(state, Byte.TYPE)).byteValue();
	}
	
	/**
	 * Executes the action of the elements of the collection/iterator. If a filter has been defined,
	 * the action is executed only on the elements satisfying the filter.
	 * <p>
	 * An action can set the state of the object by using one of the {@link #setState(Object) setState()} overloads.
	 * 
	 * @return the resulting state 
	 * @exception IllegalStateException if execute has already been invoked
	 */
	public int executeInt() {
		execute();
		if (state==null) return 0;
		return ((Integer)TypeWrapper.convertValue(state, Integer.TYPE)).intValue();
	}
	
	/**
	 * Executes the action of the elements of the collection/iterator. If a filter has been defined,
	 * the action is executed only on the elements satisfying the filter.
	 * <p>
	 * An action can set the state of the object by using one of the {@link #setState(Object) setState()} overloads.
	 * 
	 * @return the resulting state 
	 * @exception IllegalStateException if execute has already been invoked
	 */
	public short executeShort() {
		execute();
		if (state==null) return 0;
		return ((Short)TypeWrapper.convertValue(state, Short.TYPE)).shortValue();
	}
	
	/**
	 * Executes the action of the elements of the collection/iterator. If a filter has been defined,
	 * the action is executed only on the elements satisfying the filter.
	 * <p>
	 * An action can set the state of the object by using one of the {@link #setState(Object) setState()} overloads.
	 * 
	 * @return the resulting state 
	 * @exception IllegalStateException if execute has already been invoked
	 */
	public long executeLong() {
		execute();
		if (state==null) return 0;
		return ((Long)TypeWrapper.convertValue(state, Long.TYPE)).longValue();
	}
	
	/**
	 * Executes the action of the elements of the collection/iterator. If a filter has been defined,
	 * the action is executed only on the elements satisfying the filter.
	 * <p>
	 * An action can set the state of the object by using one of the {@link #setState(Object) setState()} overloads.
	 * 
	 * @return the resulting state 
	 * @exception IllegalStateException if execute has already been invoked
	 */
	public char executeChar() {
		execute();
		if (state==null) return 0;
		return ((Character)TypeWrapper.convertValue(state, Character.TYPE)).charValue();
	}
	
	/**
	 * Executes the action of the elements of the collection/iterator. If a filter has been defined,
	 * the action is executed only on the elements satisfying the filter.
	 * <p>
	 * An action can set the state of the object by using one of the {@link #setState(Object) setState()} overloads.
	 * 
	 * @return the resulting state 
	 * @exception IllegalStateException if execute has already been invoked
	 */
	public boolean executeBoolean() {
		execute();
		if (state==null) return false;
		return ((Boolean)TypeWrapper.convertValue(state, Boolean.TYPE)).booleanValue();
	}
	
	/**
	 * Executes the action of the elements of the collection/iterator. If a filter has been defined,
	 * the action is executed only on the elements satisfying the filter.
	 * <p>
	 * An action can set the state of the object by using one of the {@link #setState(Object) setState()} overloads.
	 * 
	 * @return the resulting state 
	 * @exception IllegalStateException if execute has already been invoked
	 */
	public float executeFloat() {
		execute();
		if (state==null) return 0;
		return ((Float)TypeWrapper.convertValue(state, Float.TYPE)).floatValue();
	}
	
	/**
	 * Executes the action of the elements of the collection/iterator. If a filter has been defined,
	 * the action is executed only on the elements satisfying the filter.
	 * <p>
	 * An action can set the state of the object by using one of the {@link #setState(Object) setState()} overloads.
	 * 
	 * @return the resulting state 
	 * @exception IllegalStateException if execute has already been invoked
	 */
	public double executeDouble() {
		execute();
		if (state==null) return 0;
		return ((Double)TypeWrapper.convertValue(state, Double.TYPE)).doubleValue();
	}
	
	/**
	 * Executes the action of the elements of the collection/iterator. If a filter has been defined,
	 * the action is executed only on the elements satisfying the filter.
	 * <p>
	 * An action can set the state of the object by using one of the {@link #setState(Object) setState()} overloads.
	 * 
	 * @return the resulting state 
	 * @exception IllegalStateException if execute has already been invoked
	 */
	public String executeString() {
		execute();
		if (state==null) return null;
		return ((String)TypeWrapper.convertValue(state, String.class));
	}

	/**
	 * Re-initializes the ForEach object on the given collection
	 * @param Collection c the collection on whose elements which the action will be executed
	 */
	public void reInit(Collection c) {
		reInit(c.iterator());
	}

	/**
	 * Re-initializes the ForEach object on the given iterator
	 * @param Iterator i an iterator, on whose elements which the action will be executed
	 */
	public void reInit(Iterator i) {
		this.i=i;
		atStart=true;
	}


	/**
	 * This method must be overridden by a subclass to define the actual action.
	 * <p>By default, the object's toString() method is invoked and the result
	 * printed on System.out.
	 */
	public void action(Object obj) {
		System.out.println(obj);
	}


}