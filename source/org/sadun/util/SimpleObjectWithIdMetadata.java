/*
 * Created on Sep 16, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.sadun.util;

import java.io.Serializable;
import java.io.StringWriter;

/**
 * @author Cristiano Sadun
 */
public class SimpleObjectWithIdMetadata
	implements ObjectWithIdMetadata, Serializable {

	private Class type;
	private String idPrefix;

	/**
	 *  
	 */
	public SimpleObjectWithIdMetadata(Class type, String idPrefix) {
		this.type = type;
		this.idPrefix = idPrefix;
	}

	public SimpleObjectWithIdMetadata(Class type) {
		this(type, getDefaultPrefix(type));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.ObjectWithIdMetadata#getIdPrefix()
	 */
	public String getIdPrefix() {
		return idPrefix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.ObjectWithIdMetadata#getType()
	 */
	public Class getType() {
		return type;
	}

	public String toString() {
		StringWriter sw = new StringWriter();
		IndentedPrintWriter pw = new IndentedPrintWriter(sw);
		pw.println("ObjectWithId metadata for class " + type.getName() + ":");
		pw.println();
		pw.incIndentation(5);
		pw.println("id starts with " + getIdPrefix());
		return sw.toString();
	}

	private static String getDefaultPrefix(Class cls) {
		String clsName = cls.getName();
		int i = clsName.lastIndexOf('.');
		if (i != -1)
			clsName = clsName.substring(i + 1);
		StringBuffer sb = new StringBuffer();
		synchronized (sb) {
			for (i = 0; i < clsName.length(); i++) {
				char c = clsName.charAt(i);
				if (Character.isUpperCase(c))
					sb.append(Character.toLowerCase(c));
			}
		}
		String acronym = sb.toString();
		if (acronym.length() == 0)
			acronym = clsName.substring(0, Math.min(clsName.length(), 4));
		return acronym;

	}

}
