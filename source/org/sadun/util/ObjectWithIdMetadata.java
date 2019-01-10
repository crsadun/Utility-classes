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
public interface ObjectWithIdMetadata {

	/**
	 * Return the id prefix for an object.
	 * <p>
	 * Typically, each object class has a class-dependent id prefix.
	 * An implementation may use the id prefix to veridy that a certain
	 * object has a proper id.   
	 * @return the id prefix for an object.
	 */
	public String getIdPrefix();
	
	/**
	 * Return the concrete class of the object.
	 * @return the concrete class of the object.
	 */
	public Class getType();

}
