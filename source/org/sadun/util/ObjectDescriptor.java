/*
 * Created on Sep 8, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * This object exposes methods which use bean introspection
 * to print out descriptions of objects using a resource bundle.
 * <p>
 * In general, for each attribute in the object, a corresponding label is searched
 * for in the resource bundle, with a key name equal to the attribute
 * name.
 * 
 * @author Cristiano Sadun
 */
public class ObjectDescriptor {

	private static class DefaultResourceBundle extends ResourceBundle {

		/* (non-Javadoc)
		 * @see java.util.ResourceBundle#getKeys()
		 */
		public Enumeration getKeys() {
			throw new RuntimeException("This method is not implemented");
		}

		/* (non-Javadoc)
		 * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
		 */
		protected Object handleGetObject(String key) {
			if (key.length() == 1)
				return key.toUpperCase();
			return Character.toUpperCase(key.charAt(0)) + key.substring(1);
		}

	}

	private ObjectDescriptor() {
	}

	private static DefaultResourceBundle drb = new DefaultResourceBundle();

	/**
	 * Produces a description of the object, using attribute names
	 * as labels.
	 * 
	 * @param obj the object to describe.
	 * @return a description of the object.
	 */
	public static String describe(Object obj) {
		return describe(obj, drb);
	}

	/**
	 * Produces a description of the object, with labels found in
	 * the given bundle by using attribute names as keys.
	 * 
	 * @param obj the object to describe.
	 * @param resources the resource bundle where to look for labels
	 * @return a description of the object.
	 */
	public static String describe(Object obj, ResourceBundle resources) {
		return describe(obj, resources, null);
	}

	/**
	 * Produces a description of the object, with labels found in
	 * the given bundle by using attribute names (with an optional prefix) as keys.
	 * 
	 * @param obj the object to describe.
	 * @param resources the resource bundle where to look for labels
	 * @param prefix an optional prefix prepended to the resource name when looking in the bundle (may be null) 
	 * @return a description of the object.
	 */
	public static String describe(
		Object obj,
		ResourceBundle resources,
		String prefix) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		BeanInfo bi;
		try {
			bi = Introspector.getBeanInfo(obj.getClass());
			PropertyDescriptor[] pd = bi.getPropertyDescriptors();
			for (int i = 0; i < pd.length; i++) {
				if (pd[i].getPropertyType() == Class.class)
					continue;
                
                if (pd[i].getReadMethod()==null) continue;
                
				Object value = pd[i].getReadMethod().invoke(obj, new Object[0]);
				String labelKey =
					(prefix == null)
						? pd[i].getName()
						: prefix + pd[i].getName();
				if (value != null) {
                    if (value.getClass().isPrimitive())
    					pw.println(
    						resources.getString(labelKey)
    							+ ": "
    							+ value.toString());
                    else {
                        pw.println(resources.getString(labelKey) + ": ");
                        IndentedPrintWriter ipw = new IndentedPrintWriter(pw);
                        ipw.incIndentation();
                        ipw.print(describe(value));
                        ipw.decIndentation();
                        ipw.flush();
                        pw.println();
                    }
                }
			}
			return sw.toString();
		} catch (IntrospectionException e) {
			throw new RuntimeException("Could not introspect the object ", e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Could not retrieve field value", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Could not retrieve field value", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Could not retrieve field value", e);
		}
	}

}
