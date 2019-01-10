/*
 * Created on Sep 14, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util;


/**
 * Objects implementing this interface have a string identifier.
 * <p>
 * It depends on the specific object if the identifier be unique or
 * not within a certain domain.
 * 
 * @author Cristiano Sadun
 */
public interface ObjectWithId {
	
	/**
	 * Return an id for an object.
	 * @return an id for an object.
	 */
	public String getId();
	
	/**
	 * Return the {@link ObjectWithIdMetadata metadata} for the object.
	 * @return the {@link ObjectWithIdMetadata metadata}for the object.
 	 */
	public ObjectWithIdMetadata getMetadata();

}
