/*
 * Created on Aug 27, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.pool;

import javax.management.MBeanException;

/**
 * 
 * @author Cristiano Sadun
 */
public interface ManagedObjectPoolMBean {

	/**
 	 * Return the number of available objects in the pool
	 * @return the number of available objects in the pool
	 */
	public int getFreeCount();

	/**
	 * Return the number of used objects in the pool
	 * @return the number of used objects in the pool
	 */
	public int getUsedCount();

	/**
	 * Return the size of the pool
	 * @return the size of the pool
	 */
	public int getSize();
	
	/**
	 * Set the size of the pool
	 * @param size the size of the pool
	 */
	public void setSize(int size) throws MBeanException;
	
	/**
	 * Return the object type pooled by this pool
	 */
	public Class getObjectType();

	/**
	 * Set the name of the factory class to use. 
	 * 
	 * @param factoryClassName
	 * @throws MBeanException
	 */
	public void setFactoryClassName(String factoryClassName)
		throws MBeanException;

	/**
	 * Return the name of the factory class in use
	 * @return
	 */
	public String getFactoryClassName();

	//public String getClassName();

	
}
