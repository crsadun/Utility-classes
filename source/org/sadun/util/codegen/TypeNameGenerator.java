/*
 * Created on Aug 29, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.codegen;

/**
 * A class to produce source-level names for java types.
 * 
 * @author Cristiano Sadun
 */
public class TypeNameGenerator {
	
	/**
	 * Return the correct source level name for a given type
	 * 
	 * @param type
	 * @return
	 */
	public static String getName(Class type) {
		if (type.isArray()) {
			return getName(type.getComponentType())+"[]";
		}
		else if (type.getName().indexOf('$')!=-1) {
		    return type.getName().replaceAll("\\$",".");
		}
		else return type.getName();
	}

}
