/*
 * Created on Sep 2, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util;

/**
 * An interface indicating that an object is wrapping another one
 * (see also {@link org.sadun.util.codegen.ObjectWrapperGenerator
 * ObjectWrapperGenerator}).
 * 
 * @author Cristiano Sadun
 */
public interface Wrapper {
	
	/**
	 * Return the wrapped object.
	 * @return the wrapped object.
	 */
	public Object getWrappedObject();
	
	/**
	 * Return the class of the wrapped object.
	 * @return
	 */
	public Class getWrappedObjectClass();

}
