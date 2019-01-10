package org.sadun.util.pool2.test;

import java.io.Serializable;

/**
 * A simple passivatable object for test purposes.
 * <p>
 * Each instances is automatically associated at construction an incremental id,
 * unique within the JVM.
 * 
 * @author cris
 */
public class PassivableObject implements Serializable, ObjectWithID {
	
	private static int c=0;
	private static Object lock = new Object();
	private String id;
	
	public PassivableObject() {
		synchronized(lock) {
			id=String.valueOf(c++);
		}
	}

	/**
	 * Returns the id.
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Passivable object #"+id;
	}

}
