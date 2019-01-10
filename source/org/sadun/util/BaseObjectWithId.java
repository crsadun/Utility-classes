/*
 * Created on Sep 14, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A base implementation of {@link org.sadun.util.ObjectWithId}.
 * <p> 
 * This partial implementation stores the object id in the protected
 * field {@link #id}, and can be used both for classes requiring id based equality only
 * or requring also type-equality.
 * <p>
 * It also provides a default {@link org.sadun.util.ObjectWithIdMetadata} in {@link #metadata}, defininig:
 * <p>
 * <ul>
 * <li> the {@link org.sadun.util.ObjectWithIdMetadata#getIdPrefix() metadata}
 *      (that is, a string with which every id must start) by
 * <p>
 * <ul>
 * <li> creating an acronym of the unqualified class name, keeping only the uppercase letters;
 *      <br> for example, <tt>foo.MyClass</tt> has <tt>MC</tt> as acronym;
 * <p>
 * <li> if the acronym built with the rule above is the empty string, the first three letters (or
 *      less if there are less available) of the unqualified className are used.
 * </ul>
  * <p>
 * Subclasses may set {@link #metadata} independently in a static intializer; if the id prefix is set
 * to <b>null</b>, no check is made on the id pattern when a new object is constructed. 
 * 
 * @author Cristiano Sadun
 */
public abstract class BaseObjectWithId implements Serializable, ObjectWithId {

	private ObjectWithIdMetadata metadata;
	protected transient static Map objectWithIdMetadataMap = new HashMap();
	private transient boolean modified;
	// By definition, this must be false when an object is just deserialized

	/**
	 * The id of the object. 
	 */
	protected String objectId;
	private boolean requireSameTypeForEquality;

	/**
	 * Create an instance of the class with the given id
	 * which will or will not perform a type comparison in the
	 * {@link #equals(Object)} method, depending on the value of the
	 * <tt>requireSameTypeForEquality</tt> parameter. 
	 * <p>
	 * Unless {@link #metadata} returns a <tt>null</tt> {@link ObjectWithIdMetadata#getIdPrefix() id prefix}, 
	 * the constructor also checks that the given id matches the class (see class comment).
	 * 
	 * @param id the unique id of the object
	 * @param requireSameTypeForEquality if <b>true</b>, {@link #equals(Object)}
	 *         will perform also a type comparsino besides an id comparison.  
	 */
	protected BaseObjectWithId(String id, boolean requireSameTypeForEquality) {
		this.objectId = id;
		this.requireSameTypeForEquality = requireSameTypeForEquality;
		this.metadata = getMetadata();
		clearModified();
		if (metadata == null) {
			metadata =
				(ObjectWithIdMetadata) Collections.synchronizedMap(
					objectWithIdMetadataMap).get(
					getClass());
			if (metadata == null) {
				Collections.synchronizedMap(objectWithIdMetadataMap).put(
					getClass(),
					metadata = mkDefaultMetdata(getClass()));
			}
		}
		checkPrefix(id, metadata);
	}

	/**
	 * This method is used at construction to verify whether the
	 * given id is suitable for the object, and shoud throw an
	 * <tt>IllegalArgumentException</tt> if it not.
	 * <p>
	 * This method can be overridden by subclasses.
	 * <p> 
	 * This implementation accepts only prefixes which start
	 * with the id prefix specified by the metadata.
	 * @param metadata the metadata for the object
	 * @exception IllegalArgumentException if the id is invalid.
	 */
	protected void checkPrefix(String id, ObjectWithIdMetadata metadata) {
		String idPrefix = metadata.getIdPrefix();
		if (idPrefix != null)
			if (!id.startsWith(idPrefix))
				throw new IllegalArgumentException(
					"The given ID '"
						+ id
						+ "' for instance of "
						+ getClass()
						+ " does not match the required id prefix '"
						+ idPrefix
						+ "'");
	}

	/**
	 * Create an instance with the given id and which will {@link #equals(Object) equate} only
	 * to object of the same type.
	 * <p>
	 * Unless {@link #metadata} is <tt>null</tt>, the constructor also checks that the given 
	 * id matches the class {@link #metadata id prefix} (see class comment).
	 * 
	 * @param id the unique id of the object
	 */
	protected BaseObjectWithId(String id) {
		this(id, true);
	}

	/**
	 * @return
	 */
	private static ObjectWithIdMetadata mkDefaultMetdata(Class cls) {
		String clsName = cls.getName();
		int i = clsName.lastIndexOf('.');
		if (i != -1)
			clsName = clsName.substring(i + 1);
		StringBuffer sb = new StringBuffer();
		synchronized(sb) {
		for (i = 0; i < clsName.length(); i++) {
			char c = clsName.charAt(i);
			if (Character.isUpperCase(c))
				sb.append(Character.toLowerCase(c));
		}
		}
		String acronym = sb.toString();
		
		if (acronym.length() == 0)
			acronym = clsName.substring(0, Math.min(clsName.length(), 4));
		ObjectWithIdMetadata result =
			new SimpleObjectWithIdMetadata(cls, acronym);
		return result;
	}

	/** 
	 * Return the unique id of the object.
	 * @see org.sadun.util.ObjectWithId#getId()
	 */
	public String getId() {
		return objectId;
	}

	/**
	 * Verifies if two object are the same, by comparing their ids.
	 * <o>
	 * Depending on the at {@link #BaseObjectWithId(String, boolean) construction} parameter
	 * <tt>requireSameTypeForEquality</tt>, the comparison may also require that the
	 * compared object has the same class as this object.
	 * 
	 * @param obj the object to compare
	 * @return <btrue</b> if the given object is equalt to this object  
	 */
	public final boolean equals(Object obj) {
		if (obj instanceof ObjectWithId) {
			if (!requireSameTypeForEquality) {
				if (!(obj.getClass() == getClass()))
					return false;
				return objectId.equals(((ObjectWithId) obj).getId());
			}
		}
		return equalWithNoObjectWithId(obj);
	}

	/**
	 * Subclasses may override this method to implement an equality check
	 * with some other type than {@link ObjectWithId}.
	 * <p>
	 * This implementation returns <b>false</b>.
	 * 
	 * @param obj the object to compare (which is <i>not</i> an {@link ObjectWithId}. 
	 * @return <b>false</b>.
	 */
	protected boolean equalWithNoObjectWithId(Object obj) {
		return false;
	}

	/**
	 * Return an hash code for the object, obtained by its id.
	 * @return an hash code for the object, obtained by its id.
	 */
	public final int hashCode() {
		return objectId.hashCode();
	}

	/**
	 * Return the value of the {@link #metadata} field.
	 * The metadata is set on a per-class basis.
	 * 
	 * @see org.sadun.util.ObjectWithId#getMetadata()
	 */
	public ObjectWithIdMetadata getMetadata() {
		if (metadata == null) {
			metadata =
				(ObjectWithIdMetadata) Collections.synchronizedMap(
					objectWithIdMetadataMap).get(
					getClass());
			if (metadata == null) {
				Collections.synchronizedMap(objectWithIdMetadataMap).put(
					getClass(),
					metadata = mkDefaultMetdata(getClass()));
			}
		}
		return metadata;
	}

	/**
	 * Subclasses may use this method to set the "modified" status of the object to <b>true</b>.
	 * <p>
	 * Typically this is done in setters.
	 */
	protected final void setModified() {
		modified = true;
	}

	/**
	 * Subclasses may use this method to set the "modified" status of the object to <b>true</b>.
	 */
	protected final void clearModified() {
		modified = false;
	}

	/**
	 * This method return <b>true</b> if the object "modified" status has been set by {@link #setModified()}.
	 *  
	 * @return
	 */
	public final boolean isModified() {
		return modified;
	}
}
