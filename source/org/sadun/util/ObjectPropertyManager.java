/*
 * Created on Jan 11, 2003
 */
package org.sadun.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * This class allows to set and retrieve properties, by name, by any object using introspection and
 * JavaBeans conventions.
 * <p>
 * In particular, it supports automatic value conversion from String denotation.
 * @author Cristiano Sadun
 */
public class ObjectPropertyManager implements Serializable {
	
	/**
	 * An exception thrown when the {@link ObjectPropertyManager} cannot
	 * find a property in a bean.
	 * 
	 * @author Cristiano Sadun
	 */
	public static final class NoSuchPropertyException extends Exception {

		private NoSuchPropertyException(String msg) {
			super(msg);
		}
		
		private NoSuchPropertyException(Throwable e) {
			super(e);
		}
	}
	
	private transient Map pDescByName;
	private Object targetObject;
	private boolean shortForm = true;
	private boolean showType=false;

	/**
	 * Create an object manager which can set or get properties from
	 * the given target object 
	 * @param targetObject the managed object
	 */
	public ObjectPropertyManager(Object targetObject) {
		this.targetObject=targetObject;
	}
	
	public void setProperties(Properties properties) throws IntrospectionException, NoSuchPropertyException {
		for(Iterator i=properties.keySet().iterator();i.hasNext();) {
			String name=(String)i.next();
			String value=properties.getProperty(name);
			setProperty(name, value);
		}
	}
	
	/**
	 * Set the given property with the given value on the wrapped targetObject.
	 * <br>
	 * The property must be exposed by following JavaBeans conventions.
	 * @param name the name of the property to set
	 * @param value the value of the property
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 * @ if an exception occurs setting the property
	 */
	public void setProperty(String name, byte value) throws IntrospectionException, NoSuchPropertyException  {
		setProperty(name, new Byte(value));
	}
	
	/**
	 * Set the given property with the given value on the wrapped targetObject.
	 * <br>
	 * The property must be exposed by following JavaBeans conventions.
	 * @param name the name of the property to set
	 * @param value the value of the property
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 * @ if an exception occurs setting the property
	 */
	public void setProperty(String name, short value) throws IntrospectionException, NoSuchPropertyException  {
		setProperty(name, new Short(value));
	}

	/**
	 * Set the given property with the given value on the wrapped targetObject.
	 * <br>
	 * The property must be exposed by following JavaBeans conventions.
	 * @param name the name of the property to set
	 * @param value the value of the property
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 * @ if an exception occurs setting the property
	 */
	public void setProperty(String name, int value) throws IntrospectionException, NoSuchPropertyException  {
		setProperty(name, new Integer(value));
	}

	/**
	 * Set the given property with the given value on the wrapped targetObject.
	 * <br>
	 * The property must be exposed by following JavaBeans conventions.
	 * @param name the name of the property to set
	 * @param value the value of the property
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 * @ if an exception occurs setting the property
	 */
	public void setProperty(String name, long value) throws IntrospectionException, NoSuchPropertyException  {
		setProperty(name, new Long(value));
	}

	/**
	 * Set the given property with the given value on the wrapped targetObject.
	 * <br>
	 * The property must be exposed by following JavaBeans conventions.
	 * @param name the name of the property to set
	 * @param value the value of the property
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 * @ if an exception occurs setting the property
	 */
	public void setProperty(String name, char value) throws IntrospectionException, NoSuchPropertyException  {
		setProperty(name, new Character(value));
	}

	/**
	 * Set the given property with the given value on the wrapped targetObject.
	 * <br>
	 * The property must be exposed by following JavaBeans conventions.
	 * @param name the name of the property to set
	 * @param value the value of the property
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 * @ if an exception occurs setting the property
	 */
	public void setProperty(String name, float value) throws IntrospectionException, NoSuchPropertyException  {
		setProperty(name, new Float(value));
	}

	/**
	 * Set the given property with the given value on the wrapped targetObject.
	 * <br>
	 * The property must be exposed by following JavaBeans conventions.
	 * @param name the name of the property to set
	 * @param value the value of the property
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 * @ if an exception occurs setting the property
	 */
	public void setProperty(String name, double value) throws IntrospectionException, NoSuchPropertyException  {
		setProperty(name, new Double(value));
	}

	/**
	 * Set the given property with the given value on the wrapped targetObject.
	 * <br>
	 * The property must be exposed by following JavaBeans conventions.
	 * @param name the name of the property to set
	 * @param value the value of the property
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 * @ if an exception occurs setting the property
	 */
	public void setProperty(String name, boolean value) throws IntrospectionException, NoSuchPropertyException  {
		setProperty(name, new Boolean(value));
	}

	/**
	 * Set the given property with the given value on the wrapped targetObject.
	 * <br>
	 * The property must be exposed by following JavaBeans conventions.
	 * @param name the name of the property to set
	 * @param value the value of the property
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 * @ if an exception occurs setting the property
	 */
	public void setProperty(String name, Object value) throws IntrospectionException, NoSuchPropertyException  {
		Method m;
		try {
			m = getWriteMethod(name);
			Object adaptedValue = TypeWrapper.convertValue(value, m.getParameterTypes()[0]);
			m.invoke(targetObject, new Object[]{adaptedValue});
		} catch (IllegalAccessException e) {
			throw new NoSuchPropertyException(e);
		} catch (InvocationTargetException e) {
			throw new NoSuchPropertyException(e);
		}
	}

	/**
	 * Return the value of the given property on the wrapped targetObject.
	 * <br>
	 * The property must be exposed by following JavaBeans conventions.
	 * 
	 * @param name the name of the property to get
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 * @ if an exception occurs setting the property
	 */
	public Object getProperty(String name) throws IntrospectionException, NoSuchPropertyException  {
		Method m = getReadMethod(name);
		try {
			return m.invoke(targetObject, new Object[0]);
		} catch (IllegalAccessException e) {
			throw new IntrospectionException("(reading "+name+") "+e.getMessage());
		} catch (InvocationTargetException e) {
			throw new IntrospectionException("(reading "+name+") "+e.getMessage());
		}

	}

	private Method getWriteMethod(String name) throws IntrospectionException, NoSuchPropertyException {
		initPDMap();
		PropertyDescriptor pd = (PropertyDescriptor) pDescByName.get(name);
		if (pd == null)
			throw new NoSuchPropertyException("There is no writable property '" + name
					+ "' in this targetObject");
		return pd.getWriteMethod();
	}

	private Method getReadMethod(String name) throws IntrospectionException, NoSuchPropertyException  {
		initPDMap();
		PropertyDescriptor pd = (PropertyDescriptor) pDescByName.get(name);
		if (pd == null)
			throw new NoSuchPropertyException("There is no readable property '" + name
					+ "' in this targetObject");
		return pd.getReadMethod();
	}

	/**
	 * Return the names of the available properties of the wrapped targetObject
	 * 
	 * @param mustBeReadable excludes properties which have no read method
	 * @param mustBeWritable excludes properties which have no write method
	 * @return the array of property names exposed by the targetObject
	 * @throws IntrospectionException
	 * @ if a problem occurs while retrieving the property names
	 */
	public String[] getPropertyNames(boolean mustBeWritable,
			boolean mustBeReadable) throws IntrospectionException  {
		initPDMap();
		List l = new ArrayList();
		for (Iterator i = pDescByName.keySet().iterator(); i.hasNext();) {
			PropertyDescriptor pd = (PropertyDescriptor) pDescByName.get(i
					.next());
			if (((!mustBeWritable) || pd.getWriteMethod() != null)
					&& ((!mustBeReadable) || pd.getReadMethod() != null))
				l.add(pd.getName());
		}
		String[] result = new String[l.size()];
		l.toArray(result);
		return result;
	}

	/**
	 * Return the names of the available read/write properties of the wrapped targetObject.
	 * @return the names of the available read/write properties of the wrapped targetObject.
	 * @throws IntrospectionException
	 * @ if a problem occurs while retrieving the property names
	 */
	public String[] getPropertyNames() throws IntrospectionException  {
		return getPropertyNames(true, true);
	}
	
	private void initPDMap() throws IntrospectionException {
		if (pDescByName == null)
			pDescByName = new HashMap();
		if (pDescByName.size() > 0)
			return;
		
		BeanInfo info = Introspector.getBeanInfo(targetObject.getClass());
		PropertyDescriptor[] pd = info.getPropertyDescriptors();
		for (int i = 0; i < pd.length; i++) {
			pDescByName.put(pd[i].getName(), pd[i]);
		}
		
	}

	/**
	 * Return the value of the property as a boolean, operating promotions when possible
	 * @param string
	 * @param i
	 * @return
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 */
	public boolean getBooleanProperty(String name) throws IntrospectionException, NoSuchPropertyException {
		Object obj = getProperty(name);
		if (! TypeWrapper.isConvertible(obj.getClass(), Boolean.TYPE) )
			throw new NoSuchPropertyException("There is no boolean property '"+name+"' (but a property with type '"+obj.getClass().getName()+"' does exist)");
		if (TypeWrapper.isPrimitiveWrapper(obj.getClass())) return ((Boolean)obj).booleanValue();
		return Boolean.getBoolean(obj.toString());
	}
	
	/**
	 * Return the value of the property as a byte, operating promotions when possible
	 * @param string
	 * @param i
	 * @return
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 */
	public byte getByteProperty(String name) throws IntrospectionException, NoSuchPropertyException {
		Object obj = getProperty(name);
		if (! TypeWrapper.isConvertible(obj.getClass(), Byte.TYPE) )
			throw new NoSuchPropertyException("There is no byte property '"+name+"' (but a property with type '"+obj.getClass().getName()+"' does exist)");
		if (TypeWrapper.isPrimitiveWrapper(obj.getClass())) return ((Number)obj).byteValue();
		return Byte.decode(obj.toString()).byteValue();
	}
	
	/**
	 * Return the value of the property as a short, operating promotions when possible
	 * @param string
	 * @param i
	 * @return
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 */
	public short getShortProperty(String name) throws IntrospectionException, NoSuchPropertyException {
		Object obj = getProperty(name);
		if (! TypeWrapper.isConvertible(obj.getClass(), Short.TYPE) )
			throw new NoSuchPropertyException("There is no short property '"+name+"' (but a property with type '"+obj.getClass().getName()+"' does exist)");
		if (TypeWrapper.isPrimitiveWrapper(obj.getClass())) return ((Number)obj).shortValue();
		return Short.decode(obj.toString()).shortValue();
	}
	
	/**
	 * Return the value of the property as an integer, operating promotions when possible
	 * @param string
	 * @param i
	 * @return
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 */
	public int getIntProperty(String name) throws IntrospectionException, NoSuchPropertyException {
		Object obj = getProperty(name);
		if (! TypeWrapper.isConvertible(obj.getClass(), Integer.TYPE) )
			throw new NoSuchPropertyException("There is no int property '"+name+"' (but a property with type '"+obj.getClass().getName()+"' does exist)");
		if (TypeWrapper.isPrimitiveWrapper(obj.getClass())) return ((Number)obj).intValue();
		return Integer.decode(obj.toString()).intValue();
	}

	/**
	 * Return the value of the property as a long, operating promotions when possible
	 * @param string
	 * @return
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 */
	public long getLongProperty(String name) throws IntrospectionException, NoSuchPropertyException {
		Object obj = getProperty(name);
		if (! TypeWrapper.isConvertible(obj.getClass(), Long.TYPE) )
			throw new NoSuchPropertyException("There is no long property '"+name+"' (but a property with type '"+obj.getClass().getName()+"' does exist)");
		if (TypeWrapper.isPrimitiveWrapper(obj.getClass())) return ((Number)obj).longValue();
		return Long.decode(obj.toString()).longValue();
	}
	
	/**
	 * Return the value of the property as a float, operating promotions when possible
	 * @param string
	 * @return
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 */
	public float getFloatProperty(String name) throws IntrospectionException, NoSuchPropertyException {
		Object obj = getProperty(name);
		if (! TypeWrapper.isConvertible(obj.getClass(), Float.TYPE) )
			throw new NoSuchPropertyException("There is no float property '"+name+"' (but a property with type '"+obj.getClass().getName()+"' does exist)");
		if (TypeWrapper.isPrimitiveWrapper(obj.getClass())) return ((Number)obj).floatValue();
		return Float.valueOf(obj.toString()).floatValue();
	}

	/**
	 * Return the value of the property as a double, operating promotions when possible
	 * @param string
	 * @return
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 */
	public double getDoubleProperty(String name) throws IntrospectionException, NoSuchPropertyException {
		Object obj = getProperty(name);
		if (! TypeWrapper.isConvertible(obj.getClass(), Double.TYPE) )
			throw new NoSuchPropertyException("There is no double property '"+name+"' (but a property with type '"+obj.getClass().getName()+"' does exist)");
		if (TypeWrapper.isPrimitiveWrapper(obj.getClass())) return ((Number)obj).doubleValue();
		return Double.valueOf(obj.toString()).doubleValue();
	}
	
	/**
	 * Return the value of the property as a String
	 * @param string
	 * @return
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 */
	public String getStringProperty(String name) throws IntrospectionException, NoSuchPropertyException {
		Object obj = getProperty(name);
		if (! (obj instanceof String))
			throw new NoSuchPropertyException("There is no String property '"+name+"' (but a property with type '"+obj.getClass().getName()+"' does exist)");
		return (String)obj;
	}

	/**
	 * Return the value of the property as a char
	 * @param string
	 * @return
	 * @throws NoSuchPropertyException
	 * @throws IntrospectionException
	 */
	public char getCharProperty(String name) throws IntrospectionException, NoSuchPropertyException {
		Object obj = getProperty(name);
		if (! (obj instanceof Character))
			throw new NoSuchPropertyException("There is no char property '"+name+"' (but a property with type '"+obj.getClass().getName()+"' does exist)");
		return ((Character)obj).charValue();
	}
	
	/**
	 * Return the managed object
	 * @return the managed object
	 */
	public Object getTargetObject() {
		return targetObject;
	}
	
	/**
	 * Describe the current object.
	 * 
	 * @return aq description of the object 
	 * @throws IntrospectionException if introspection fails
	 */
	public String describe() throws IntrospectionException {
		return describe(0, false);
	}
	
	/**
	 * Describe the current object.
	 * 
	 * @return aq description of the object 
	 * @param recurse if <b>true</b>, member objects will be described as well
	 * @throws IntrospectionException if introspection fails
	 */
	public String describe(boolean recurse) throws IntrospectionException {
		return describe(0, recurse);
	}
	
	/**
	 * Describe the current object.
	 * 
	 * @param indent the indentation of the description
	 * @return aq description of the object 
	 * @throws IntrospectionException if introspection fails
	 */
	public String describe(int indent) throws IntrospectionException {
		return describe(indent, false);
	}
	
	/**
	 * Describe the current object.
	 * 
	 * @param indent the indentation of the description
	 * @param recurse if <b>true</b>, member objects will be described as well
	 * @return aq description of the object 
	 * @throws IntrospectionException if introspection fails
	 */
	public String describe(int indent, boolean recurse) throws IntrospectionException {
		String [] names = getPropertyNames();
		StringWriter sw = new StringWriter();
		IndentedPrintWriter pw = new IndentedPrintWriter(sw);
		pw.setIndentation(indent);
		for(int i=0;i<names.length;i++) {
			try {
				if (recurse) {
					Object value = getProperty(names[i]);
					String type ;
					if (value==null) type=((PropertyDescriptor)pDescByName.get(names[i])).getPropertyType().getName();
					else type=value.getClass().getName();
					if (shortForm) 
						if (type.lastIndexOf('.')!=-1) type=type.substring(type.lastIndexOf('.')+1);
					
					//pw.print((showType ? type+" " : "")+names[i]+" ("+value+"): ");
					pw.print((showType ? type+" " : "")+names[i]+": ");
					pw.incIndentation(3);
					
					if (value != null) {
						if (!TypeWrapper.isPrimitiveOrWrapper(value.getClass())) {
							if (value instanceof String)
								pw.print(describeString((String) value));
							else {
								pw.println();
								pw.incIndentation(1);
								pw.print(new ObjectPropertyManager(value)
										.describe(recurse));
								pw.decIndentation(1);
							}
						} else
							pw.print(value);
					} else
						pw.print("null");
					pw.decIndentation(3);
				} else {
					Object value = getProperty(names[i]);
					String type ;
					if (value==null) type=((PropertyDescriptor)pDescByName.get(names[i])).getPropertyType().getName();
					else type=value.getClass().getName();
					if (shortForm) 
						if (type.lastIndexOf('.')!=-1) type=type.substring(type.lastIndexOf('.')+1);
					
					pw.print((showType ? type+" " : "")+names[i]+": "+value);
				}
				if (i < names.length - 1) 
					pw.println();
			} catch(NoSuchPropertyException e) {
				// Shouldn't happen
				e.printStackTrace();
			}
		}
		return sw.toString();
	}
	/**
	 * @param string
	 * @return
	 */
	private String describeString(String s) {
		StringBuffer sb = new StringBuffer("\"");
		int l=s.length();
		synchronized(sb) {
			for(int i=0;i<l;i++) {
				if (s.charAt(i)=='\"') sb.append("\\\"");
				else sb.append(s.charAt(i));
			}
			sb.append("\"");
			return sb.toString();
		}
	}

	public boolean isShortForm() {
		return shortForm;
	}
	public void setShortForm(boolean shortForm) {
		this.shortForm = shortForm;
	}
	public boolean isShowType() {
		return showType;
	}
	public void setShowType(boolean showType) {
		this.showType = showType;
	}

	/**
	 * @param factory
	 * @param i
	 * @param b
	 * @return
	 * @throws IntrospectionException
	 */
	public static String getDescription(Object obj, int indent, boolean recurse, boolean shortForm, boolean showType) throws IntrospectionException {
		ObjectPropertyManager opm = new ObjectPropertyManager(obj);
		opm.setShortForm(shortForm);
		opm.setShowType(showType);
		return opm.describe(indent, recurse);
	}
}
