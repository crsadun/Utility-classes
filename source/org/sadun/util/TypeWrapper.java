package org.sadun.util;

import java.lang.reflect.Array;

import com.deltax.util.SignatureAnalyzer;

/**
 * An helper class to automatically wrap primitive types
 * into their corresponding object types.
 * 
 * @author Cristiano Sadun
 */
public class TypeWrapper {

	public static Object wrap(byte v) {
		return new Byte(v);
	}
	public static Object wrap(short v) {
		return new Short(v);
	}
	public static Object wrap(int v) {
		return new Integer(v);
	}
	public static Object wrap(long v) {
		return new Long(v);
	}
	public static Object wrap(float v) {
		return new Float(v);
	}
	public static Object wrap(double v) {
		return new Double(v);
	}
	public static Object wrap(char v) {
		return new Character(v);
	}
	public static Object wrap(boolean v) {
		return new Boolean(v);
	}

	public static Object wrap(Object obj) {

		if (!obj.getClass().isArray())
			throw new IllegalArgumentException("wrap(Object) can be invoked only on arrays of primitive types");

		Object[] array = (Object[]) obj;
		Class componentType = obj.getClass().getComponentType();
		Class baseComponentType = componentType;

		int dim = 1;

		while (baseComponentType.isArray()) {
			baseComponentType = baseComponentType.getComponentType();
			dim++;
		}

		if (!baseComponentType.isPrimitive())
			throw new IllegalArgumentException("wrap(Object) can be invoked only on arrays of primitive types");

		return wrap0(array, componentType, baseComponentType, dim);
	}

	private static Object wrap0(
		Object[] array,
		Class componentType,
		Class baseComponentType,
		int dim) {
		Object[] result = null;
		for (int i = 0; i < array.length; i++) {
			if (componentType.isArray()) {
				// Component type is an array. If the value of the ith element is
				// not null, go creating the subarray and assign it
				if (array[i] == null) {
					result[i] = null;
					continue;
				} else {
					Object[] elementArray = (Object[]) array[i];
					result[i] =
						wrap0(
							elementArray,
							componentType.getComponentType(),
							baseComponentType,
							dim - 1);
				}
			} else {
				// Component type is not an array. Since we're dealing with primitive
				// types, array elements are never null.

				// Let's create a corresponding array of wrappers		
				result =
					(Object[]) Array.newInstance(
						TypeWrapper.getWrapperClass(baseComponentType),
						1);
				// Now, let's fill the values with wrapper objects
				for (int j = 0; j < result.length; j++) {

				}
			}
		}
		return result;
	}

	/*
	private static Object [] wrap0(Object obj, int level, Class [] baseType) { 
		if (!obj.getClass().isArray()) 
			throw new IllegalArgumentException("wrap(Object) can be invoked only on arrays of primitive types");
			
		Class componentType = obj.getClass().getComponentType();
		if (componentType.isArray()) {
			// For each element, we need to obtain the wrapped array
			Object [] result=null;
			for(int i=0;i<((Object[])obj).length;i++) {
				Object elem = ((Object[])obj)[i];
				Object [] wrapped = wrap0(elem, level+1, baseType);
				// In "basetype", we've recorded the primitive type,
				// so we create an appropriate wrapper type array 
				if (result==null) {
					try {
						result = Array.n.newInstance()
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					System.exit(0);
				}
			/*
			if (componentType==byte[].class) wrappedArray=wrap0((byte)
			else if (componentType==Short.TYPE) wrappedValue=wrap(((short[])obj)[i]);
			else if (componentType==Integer.TYPE) wrappedValue=wrap(((int[])obj)[i]);
			else if (componentType==Long.TYPE) wrappedValue=wrap(((long[])obj)[i]);
			else if (componentType==Float.TYPE) wrappedValue=wrap(((float[])obj)[i]);
			else if (componentType==Double.TYPE) wrappedValue=wrap(((double[])obj)[i]);
			else if (componentType==Character.TYPE) wrappedValue=wrap(((char[])obj)[i]);
			else if (componentType==Boolean.TYPE) wrappedValue=wrap(((boolean[])obj)[i]);
			*
			}
			return result;
		}
		
		if (!componentType.isPrimitive())
			throw new IllegalArgumentException("wrap(Object) can be invoked only on arrays of primitive types");
		
		// Create an array of the wrapper type, and assign it
		// with wraps of the original value
		int length = getArrayLength(obj);
		baseType[0]=componentType;
		Object [] wrapperArray = createWrapperArray(componentType, length);
		for(int i=0;i<length;i++) {
			Object wrappedValue=null;
			if (componentType==Byte.TYPE) wrappedValue=wrap(((byte[])obj)[i]);
			else if (componentType==Short.TYPE) wrappedValue=wrap(((short[])obj)[i]);
			else if (componentType==Integer.TYPE) wrappedValue=wrap(((int[])obj)[i]);
			else if (componentType==Long.TYPE) wrappedValue=wrap(((long[])obj)[i]);
			else if (componentType==Float.TYPE) wrappedValue=wrap(((float[])obj)[i]);
			else if (componentType==Double.TYPE) wrappedValue=wrap(((double[])obj)[i]);
			else if (componentType==Character.TYPE) wrappedValue=wrap(((char[])obj)[i]);
			else if (componentType==Boolean.TYPE) wrappedValue=wrap(((boolean[])obj)[i]);
			wrapperArray[i]=wrappedValue;
		}
		return wrapperArray;
	}
	*/

	private static int getArrayLength(Object array) {
		Class componentType = array.getClass().getComponentType();
		if (componentType == Byte.TYPE)
			return ((byte[]) array).length;
		if (componentType == Short.TYPE)
			return ((short[]) array).length;
		if (componentType == Integer.TYPE)
			return ((int[]) array).length;
		if (componentType == Long.TYPE)
			return ((long[]) array).length;
		if (componentType == Float.TYPE)
			return ((float[]) array).length;
		if (componentType == Double.TYPE)
			return ((double[]) array).length;
		if (componentType == Character.TYPE)
			return ((char[]) array).length;
		if (componentType == Boolean.TYPE)
			return ((boolean[]) array).length;
		throw new IllegalArgumentException("getArrayLength() called over an array of nonprimitive types");

	}

	private static Object[] createWrapperArray(
		Class componentType,
		int length) {
		if (componentType == Byte.TYPE)
			return new Byte[length];
		if (componentType == Short.TYPE)
			return new Short[length];
		if (componentType == Integer.TYPE)
			return new Integer[length];
		if (componentType == Long.TYPE)
			return new Long[length];
		if (componentType == Float.TYPE)
			return new Float[length];
		if (componentType == Double.TYPE)
			return new Double[length];
		if (componentType == Character.TYPE)
			return new Character[length];
		if (componentType == Boolean.TYPE)
			return new Boolean[length];
		throw new IllegalArgumentException("createWrapperArray() called over a nonprimitive type");

	}

	/**
	 * Return the class of the wrapper object for the given
	 * primitive type.
	 * 
	 * @param primitiveCls the Class of the primitive type
	 * @return Class the wrapper class
	 */
	public static Class getWrapperClass(Class primitiveCls) {
		if (primitiveCls == Byte.TYPE)
			return Byte.class;
		if (primitiveCls == Short.TYPE)
			return Short.class;
		if (primitiveCls == Integer.TYPE)
			return Integer.class;
		if (primitiveCls == Long.TYPE)
			return Long.class;
		if (primitiveCls == Float.TYPE)
			return Float.class;
		if (primitiveCls == Double.TYPE)
			return Double.class;
		if (primitiveCls == Character.TYPE)
			return Character.class;
		if (primitiveCls == Boolean.TYPE)
			return Boolean.class;
		throw new IllegalArgumentException("getWrapper() called over a nonprimitive type");
	}

	/**
	 * Return a wrapper object corresponding to passed value, interpreted
	 * according to the given primitive (or primtitive wrapper) class .
	 * 
	 * @param primitiveCls the Class of the primitive (or wrapper) type
	 * @param value the value to intepret
	 */
	public static Object wrapFromString(Class primitiveCls, String value) {
		try {
			if (primitiveCls == Byte.TYPE || primitiveCls==Byte.class)
				return new Byte(value);
			if (primitiveCls == Short.TYPE  || primitiveCls==Short.class)
				return new Short(value);
			if (primitiveCls == Integer.TYPE || primitiveCls==Integer.class)
				return new Integer(value);
			if (primitiveCls == Long.TYPE || primitiveCls==Long.class)
				return new Long(value);
			if (primitiveCls == Float.TYPE || primitiveCls==Float.class)
				return new Float(value);
			if (primitiveCls == Double.TYPE || primitiveCls==Double.class)
				return new Double(value);
			if (primitiveCls == Character.TYPE || primitiveCls==Character.class) {
				if (value.length() < 1)
					//throw new IllegalArgumentException("Invalid value for character type");
					return null;
				return new Character(value.charAt(0));
			}
			if (primitiveCls == Boolean.TYPE || primitiveCls==Boolean.class)
				return new Boolean(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(
				"'"
					+ value
					+ "' is not a correct representation for the type "
					+ SignatureAnalyzer.getJavaTypeName(primitiveCls));
		}
		throw new IllegalArgumentException("getWrapper() called over a nonprimitive type");
	}
	
	/**
	 * Return the primitive type class for a wrapper type. If the given type is
	 * not a wrapper, an IllegalArgumentExcepion is thrown.
	 * @param wrapperCls
	 * @return
	 */
	public static Class getUnwrappedPrimitiveType(Class wrapperCls) {
		if (wrapperCls==Byte.class) return Byte.TYPE;
		else if (wrapperCls==Short.class) return Short.TYPE;
		else if (wrapperCls==Integer.class) return Integer.TYPE;
		else if (wrapperCls==Long.class) return Long.TYPE;
		else if (wrapperCls==Float.class) return Float.TYPE;
		else if (wrapperCls==Double.class) return Double.TYPE;
		else if (wrapperCls==Character.class) return Character.TYPE;
		else if (wrapperCls==Boolean.class) return Boolean.TYPE;
		throw new IllegalArgumentException(wrapperCls+" is not a wrapper type");
	}
	
	public static boolean isPrimitiveWrapper(Class cls) {
		try {
			getUnwrappedPrimitiveType(cls);
			return true;
		} catch(IllegalArgumentException e) {
			return false;
		}
	}
	
	public static boolean isPrimitiveOrWrapper(Class cls) {
		if (cls.isPrimitive()) return true;
		return isPrimitiveWrapper(cls);
	}
	
	/*public Object interpretPrimitiveString(String value, Class cls) {
		Class primitiveCls=getWrapperClass(cls);
		return primitiveCls.
	}*/

	public static void main(String args[]) {

		Integer[][] wrapped =
			(
				Integer[][]) TypeWrapper
					.wrap(new int[][] { new int[] { 2, 5 }, new int[] { 3, 6 }
		});
	}
	/**
	 * Verifies if a source type can be legally promoted to a destination type.
	 * For example, an int can be promoted to long, but not vice versa.
	 * @param wrapperClass
	 * @param class1
	 * @return
	 */
	public static boolean isConvertible(Class srcType, Class destType) {
		// Reduce to primtiive types whenever possible
		if (isPrimitiveWrapper(srcType)) 
			srcType=getUnwrappedPrimitiveType(srcType);
		
		if (isPrimitiveWrapper(destType))
			destType=getUnwrappedPrimitiveType(destType);
		
		// Every type's compatible with itself
		if (srcType==destType) return true;
		
		if (destType==Short.TYPE) {
			if (srcType==Character.TYPE) return true;
			if (srcType==Byte.TYPE) return true;
			return false;
		} else
		if (destType==Integer.TYPE) {
			return isConvertible(srcType, Short.TYPE);
		} else
		if (destType==Long.TYPE) {
			return isConvertible(srcType, Integer.TYPE);
		} else
		if (destType==Float.TYPE) {
			return isConvertible(srcType, Integer.TYPE);
		} else
		if (destType==Double.TYPE) {
			if (isConvertible(srcType, Long.TYPE)) return true;
			return isConvertible(srcType, Float.TYPE);
		} 
		
		return false;
	}
	/**
		 *  Convert the given value to the given target type, or throws a IllegalArgumentException
		 *  if no conversion is possible
	     *  @param value the value to convert
	     *  @param targetType the type to convert to	
		 */
		public static Object convertValue(Object value, Class targetType) {
			if (value==null) return null;
			if (value.getClass() == targetType)
				return value;
			if (value instanceof String)
				return wrapFromString(targetType, (String) value);
			if (targetType.isPrimitive()) {
				if (isConvertible(value.getClass(), getWrapperClass(targetType))) 
					return value;
			} else { // Nonprimitive
				if (isPrimitiveWrapper(targetType))
					if (isConvertible(value.getClass(), getUnwrappedPrimitiveType(targetType)))
						return value;
			}
			throw new IllegalArgumentException("Don't know how to convert " + value + " ("
					+ value.getClass().getName() + " to type " + targetType.getName());
	}
	
}
