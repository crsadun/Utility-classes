/*
 * Created on Sep 16, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util;

/**
 * 
 * @author Cristiano Sadun
 */
public interface IdIterator {

	/**
	 * The constant for the "forwards" direction. Its value is 0.
	 */
	public static int FORWARD = 0;

	/**
	 * The constant for "backwards" direction. Its value is 1.
	 */
	public static int BACKWARD = 1;

	/**
	 * Return <b>true</b> if there exist an id in
	 * the given direction.
	 * 
	 * @param direction one of {@link #FORWARD} or {@link #BACKWARD} 
	 *         constants.
	 * @return <b>true</b> if there exist an id in
	 * the given direction.
	 */
	public boolean hasNext(int direction);

	/**
	 * Return the id on which the iterator is currently positioned.
	 * @return the id on which the iterator is currently positioned.
	 */
	public String getCurrentId();

	/**
	 * Moves the current id to the next in the given direction and returns it.
	 * 
	 * @param direction one of {@link #FORWARD} or {@link #BACKWARD} 
	 *         constants.
	 * @return the next id in the given direction.
	 */
	public String getNextId(int direction);
	
	/**
	 * Disposes of any resource that may be in use by the iterator. 
	 */
	public void close();

}
