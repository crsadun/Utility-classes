package org.sadun.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A class to list the elements of an array or a collection/enumeration into a
 * string, each element divided by a separtor, by invoking either the <tt>toString()</tt>
 * method or a given method with no parameters (and returning a String).
 * <p>
 * The default {@link #list(java.lang.Object[]) list(array)}method uses <tt>toString</tt>
 * to describe each object; the
 * {@link #list(java.lang.Object[], java.lang.String) list(array, method
 * name)} method attempts to locate a method with signature
 * 
 * <pre>
 *  public String <i>&lt;method name&gt;</i>()
 * </pre>
 * 
 * in each object in the array, and invokes it to describe each object.
 * <p>
 * For example, for the array <code>String [] array { "Hello", "World" }</code>
 * invoking <code>new ObjectLister().list(array);</code> will produce <code>"Hello, World"</code>.
 * <p>
 * For the array <code>Thread [] array = { new Thread("Thread 1"), 
 * new Thread("Thread 2") }</code>,
 * invoking <code>new ObjectLister().list(array, "getName");</code> will
 * produce <code>"Thread 1, Thread 2"</code>.
 * <p>
 * By default, the method to invoke is made accessible to the client code
 * regardless of its protection status. Use {@link #setSetAccessible(boolean)
 * setSetAccessible()} to disable such behaviour. If disabled, the
 * {@link #setFailOnUnaccessible(boolean) failOnUnaccessible}property may be
 * used to cause the list to raise an excepion when a method is not accessible
 * (by default, an explicit mention of the fact will appear in the list).
 * 
 * @author Cristiano Sadun
 */
public class ObjectLister {

	private static ObjectLister defaultInstance = new ObjectLister();
	private static ObjectLister defaultHTMLInstance =
		new ObjectLister("<br>", (char) 0);

	private String separator;
	private boolean useToStringIfNotFound;
	private boolean failOnUnaccessible = false;
	private boolean setAccessible = true;
	private char quoteChar = DEFAULT_QUOTE_CHAR;

	/**
	 * The default quote char, '&quote;'
	 */
	public static final char DEFAULT_QUOTE_CHAR = '\"';

	/**
	 * The default separator sequence ", "
	 */
	public static final String DEFAULT_SEPARATOR = ", ";

	// Chache of Class -> Map(name, Method)
	private Cache clsToMethodsCache = new Cache(3);

	/**
	 * Constructor for ObjectLister.
	 * 
	 * @param separator
	 *            the separator to use
	 * @param quoteChar
	 *            the quotation charachter to use. If 0, no quotes are used.
	 * @param useToStringIfNotFound
	 *            if <b>true</b>, when
	 *            {@link #list(java.lang.Object[], java.lang.String)
	 * @param failIfUnaccessible
	 *            if <b>true</b> an exception is thrown if the method to use
	 *            for listing is not accessible. list(array, method name)} is
	 *            invoked, <tt>toString()</tt> will be used if the given
	 *            method is not found in an object in the array to list. If <b>
	 *            false</b> an exception will be raised.
	 */
	public ObjectLister(
		String separator,
		char quoteChar,
		boolean useToStringIfNotFound,
		boolean failIfUnaccessible) {
		this.separator = separator;
		this.quoteChar = quoteChar;
		this.useToStringIfNotFound = useToStringIfNotFound;
		this.failOnUnaccessible = failIfUnaccessible;
	}

	/**
	 * Constructor for ObjectLister.
	 * 
	 * @param separator
	 *            the separator to use
	 * @param useToStringIfNotFound
	 *            if <b>true</b>, when
	 *            {@link #list(java.lang.Object[], java.lang.String)
	 * @param failIfUnaccessible
	 *            if <b>true</b> an exception is thrown if the method to use
	 *            for listing is not accessible. list(array, method name)} is
	 *            invoked, <tt>toString()</tt> will be used if the given
	 *            method is not found in an object in the array to list. If <b>
	 *            false</b> an exception will be raised.
	 */
	public ObjectLister(
		String separator,
		boolean useToStringIfNotFound,
		boolean failIfUnaccessible) {
		this(
			separator,
			DEFAULT_QUOTE_CHAR,
			useToStringIfNotFound,
			failIfUnaccessible);
	}

	/**
	 * Constructor for ObjectLister, which does not fail if the method to use
	 * is not accessible.
	 * 
	 * @param separator
	 *            the separator to use
	 * @param useToStringIfNotFound
	 *            if <b>true</b>, when
	 *            {@link #list(java.lang.Object[], java.lang.String) 
	 *            list(array, method name)} is invoked, <tt>toString()</tt>
	 *            will be used if the given method is not found in an object in
	 *            the array to list. If <b>false</b> an exception will be
	 *            raised.
	 */
	public ObjectLister(String separator, boolean useToStringIfNotFound) {
		this(separator, useToStringIfNotFound, false);
	}

	/**
	 * Constructor for ObjectLister, which does not fail if the method to use
	 * is not accessible.
	 * 
	 * @param separator
	 *            the separator to use
	 * @param quoteChar
	 *            the quotation charachter to use. If 0, no quotes are used.
	 * @param useToStringIfNotFound
	 *            if <b>true</b>, when
	 *            {@link #list(java.lang.Object[], java.lang.String) 
	 *            list(array, method name)} is invoked, <tt>toString()</tt>
	 *            will be used if the given method is not found in an object in
	 *            the array to list. If <b>false</b> an exception will be
	 *            raised.
	 */
	public ObjectLister(
		String separator,
		char quoteChar,
		boolean useToStringIfNotFound) {
		this(separator, quoteChar, useToStringIfNotFound, false);
	}

	/**
	 * Constructor for ObjectLister, which uses the default sequence
	 * {@link #DEFAULT_SEPARATOR}as separator.
	 * 
	 * @param useToStringIfNotFound
	 *            if <b>true</b>, when
	 *            {@link #list(java.lang.Object[], java.lang.String) 
	 *            list(array, method name)} is invoked, <tt>toString()</tt>
	 *            will be used if the given method is not found in an object in
	 *            the array to list. If <b>false</b> an exception will be
	 *            raised.
	 */
	public ObjectLister(boolean useToStringIfNotFound) {
		this(DEFAULT_SEPARATOR, useToStringIfNotFound);
	}

	/**
	 * Constructor for ObjectLister. When
	 * {@link #list(java.lang.Object[], java.lang.String) list(array, method
	 * name)} is invoked, <tt>toString()</tt> will be used if the given
	 * method is not found in an object in the array to list.
	 * 
	 * @param separator
	 *            the separator to use
	 */
	public ObjectLister(String separator) {
		this(separator, true);
	}

	/**
	 * Constructor for ObjectLister. When
	 * {@link #list(java.lang.Object[], java.lang.String) list(array, method
	 * name)} is invoked, <tt>toString()</tt> will be used if the given
	 * method is not found in an object in the array to list.
	 * 
	 * @param separator
	 *            the separator to use
	 * @param quoteChar
	 *            the quotation charachter to use. If 0, no quotes are used.
	 */
	public ObjectLister(String separator, char quoteChar) {
		this(separator, quoteChar, true);
	}

	/**
	 * Constructor for ObjectLister, which uses the default sequence
	 * {@link #DEFAULT_SEPARATOR}as separator. When
	 * {@link #list(java.lang.Object[], java.lang.String) list(array, method
	 * name)} is invoked, <tt>toString()</tt> will be used if the given
	 * method is not found in an object in the array to list.
	 */
	public ObjectLister() {
		this(DEFAULT_SEPARATOR);
	}

	/**
	 * Constructor for ObjectLister, which uses the default sequence
	 * {@link #DEFAULT_SEPARATOR}as separator. When
	 * {@link #list(java.lang.Object[], java.lang.String) list(array, method
	 * name)} is invoked, <tt>toString()</tt> will be used if the given
	 * method is not found in an object in the array to list.
	 * 
	 * @param quoteChar
	 *            the quotation charachter to use. If 0, no quotes are used.
	 */
	public ObjectLister(char quoteChar) {
		this(DEFAULT_SEPARATOR, quoteChar);
	}

	/**
	 * Return a String containing a list of objects in the array, obtained
	 * invoking the given method name on each element in the array.
	 * <p>
	 * The method must return a String and have no parameters.
	 * 
	 * @return a String containing a list of objects in the array
	 * @param array
	 *            the array to list
	 * @param methodToUse
	 *            the name of the method to use
	 * @exception RuntimeException
	 *                if a method with the given name, which returns a String
	 *                and has no parameter is not available in the objects of
	 *                the array.
	 */
	public String list(Object[] array, String methodToUse) {
		StringBuffer sb = new StringBuffer();
		synchronized (sb) {
			Object[] params = new Object[0];
			for (int i = 0; i < array.length; i++) {
				if (array[i] == null)
					continue;
				try {
					Method m = findMethod(array[i], methodToUse);
					if (setAccessible)
						m.setAccessible(true);
					String s = (String) m.invoke(array[i], params);
					if (quoteChar != 0)
						sb.append(quoteChar);
					sb.append(s);
					if (quoteChar != 0)
						sb.append(quoteChar);

				} catch (NoSuchMethodException e) {
					if (useToStringIfNotFound)
						sb.append(array[i].toString());
					else
						throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					if (useToStringIfNotFound)
						sb.append(array[i].toString());
					else
						throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					if (failOnUnaccessible)
						throw new RuntimeException(
							"\""
								+ methodToUse
								+ "()\" exists in "
								+ array[i].getClass().getName()
								+ ", but can't be accessed",
							e);
					else
						sb.append(
							"(unaccessible "
								+ methodToUse
								+ " in "
								+ array[i].getClass().getName());
				}
				if (i < array.length - 1)
					sb.append(separator);
			}
			return sb.toString();
		}
	}

	/**
	 * Find a public String <name>() method for the given object.
	 * 
	 * @param object
	 *            the object in whose class to look for
	 * @param methodName
	 * @return Method the found method
	 */
	private Method findMethod(Object object, String methodName)
		throws NoSuchMethodException {

		Class cls = object.getClass();
		synchronized (this) {
			Cache methodsCache;
			if ((methodsCache = (Cache) clsToMethodsCache.get(cls)) == null) {
				methodsCache = new Cache(3);
				clsToMethodsCache.put(cls, methodsCache);
			}
			Method method;
			if ((method = (Method) methodsCache.get(methodName)) == null) {
				method = cls.getMethod(methodName, new Class[0]);
				if (method.getReturnType() != String.class)
					throw new NoSuchMethodException(
						"\""
							+ methodName
							+ "()\" exists, but has not String return type");
				methodsCache.put(methodName, method);
			}
			return method;
		}
	}

	/**
	 * Invoke {@link #list(java.lang.Object[], java.lang.String) list()}using
	 * the <tt>toString()</tt> method.
	 * 
	 * @param the
	 *            array to be listed
	 * @return a string listing the array
	 */
	public String list(Object[] array) {
		return list(array, "toString");
	}

	/**
	 * Return a String containing a list of objects in the collection, obtained
	 * invoking the given method name on each element in the collection.
	 * <p>
	 * The method must return a String and have no parameters.
	 * 
	 * @return a String containing a list of objects in the collection
	 * @param coll
	 *            the collection to list
	 * @param methodToUse
	 *            the name of the method to use
	 * @exception RuntimeException
	 *                if a method with the given name, which returns a String
	 *                and has no parameter is not available in the objects of
	 *                the collection.
	 */
	public String list(Collection coll, String methodToUse) {
		Object[] array = new Object[coll.size()];
		coll.toArray(array);
		return list(array, methodToUse);
	}

	/**
	 * Invoke {@link #list(java.util.Collection, java.lang.String) list()}
	 * using the <tt>toString()</tt> method.
	 * 
	 * @param the
	 *            collection to be listed
	 * @return a string listing the collection
	 */
	public String list(Collection coll) {
		return list(coll, "toString");
	}
	
	/**
	 * Return a String containing a list of objects in the iterator, obtained
	 * invoking the given method name on each element.
	 * <p>
	 * The method must return a String and have no parameters.
	 * 
	 * @return a String containing a list of objects in the collection
	 * @param iterator the iterator to be listed
	 * @param methodToUse
	 *            the name of the method to use
	 * @exception RuntimeException
	 *                if a method with the given name, which returns a String
	 *                and has no parameter is not available in the objects of
	 *                the collection.
	 */
	public String list(Iterator iterator, String methodToUse) {
		List l=new LinkedList();
		while(iterator.hasNext()) l.add(iterator.next());
		Object[] array = new Object[l.size()];
		l.toArray(array);
		return list(array, methodToUse);
	}

	/**
	 * Invoke {@link #list(java.util.Iterator, java.lang.String) list()}
	 * using the <tt>toString()</tt> method.
	 * 
	 * @param iterator the iterator to be listed
	 * @return a string listing the collection
	 */
	public String list(Iterator iterator) {
		return list(iterator, "toString");
	}
	

	/**
	 * Return a String containing a list of objects in the enumeration,
	 * obtained invoking the given method name on each element in the
	 * enumeration.
	 * <p>
	 * The method must return a String and have no parameters.
	 * 
	 * @return a String containing a list of objects in the enumeration
	 * @param enum
	 *            the enumeration to list
	 * @param methodToUse
	 *            the name of the method to use
	 * @exception RuntimeException
	 *                if a method with the given name, which returns a String
	 *                and has no parameter is not available in the objects of
	 *                the enumeration.
	 */
	public String list(Enumeration enum, String methodToUse) {
		Object[] array = enumerate(enum);
		return list(array, methodToUse);
	}

	/**
	 * Invoke {@link #list(java.util.Enumeration, java.lang.String) list()}
	 * using the <tt>toString()</tt> method.
	 * 
	 * @param enum
	 *            the enumeration to be listed
	 * @return a string listing the enumeration
	 */
	public String list(Enumeration enum) {
		return list(enum, "toString");
	}

	/**
	 * Invoke {@link #list(Object[], String) list()}and print the result on
	 * System.out.
	 * 
	 * @param array
	 *            see {@link #list(Object[], String) list()}
	 * @param methodToUse
	 *            see {@link #list(Object[], String) list()}
	 */
	public void println(Object[] array, String methodToUse) {
		System.out.println(list(array, methodToUse));
	}

	/**
	 * Invoke {@link #list(Object[]) list()}and print the result on
	 * System.out.
	 * 
	 * @param array
	 *            see {@link #list(Object[]) list()}
	 */
	public void println(Object[] array) {
		System.out.println(list(array));
	}

	/**
	 * Invoke {@link #list(Collection, String) list()}and print the result on
	 * System.out.
	 * 
	 * @param coll
	 *            see {@link #list(Collection, String) list()}
	 * @param methodToUse
	 *            see {@link #list(Collection, String) list()}
	 */
	public void println(Collection coll, String methodToUse) {
		System.out.println(list(coll, methodToUse));
	}

	/**
	 * Invoke {@link #list(Collection) list()}and print the result on
	 * System.out.
	 * 
	 * @param coll
	 *            see {@link #list(Collection) list()}
	 */
	public void println(Collection coll) {
		System.out.println(list(coll));
	}

	/**
	 * Invoke {@link #list(Enumeration, String) list()}and print the result on
	 * System.out.
	 * 
	 * @param enum
	 *            see {@link #list(Enumeration, String) list()}
	 * @param methodToUse
	 *            see {@link #list(Enumeration, String) list()}
	 */
	public void println(Enumeration enum, String methodToUse) {
		System.out.println(list(enum, methodToUse));
	}

	/**
	 * Invoke {@link #list(Enumeration) list()}and print the result on
	 * System.out.
	 * 
	 * @param enum
	 *            see {@link #list(Enumeration) list()}
	 */
	public void println(Enumeration enum) {
		System.out.println(list(enum));
	}

	/**
	 * Invoke {@link #list(Map, String) list()}and print the result on
	 * System.out.
	 * 
	 * @param map
	 *            see {@link #list(Map, String) list()}
	 * @param methodToUse
	 *            see {@link #list(Map, String) list()}
	 */
	public void println(Map map, String methodToUse) {
		System.out.println(list(map, methodToUse));
	}

	/**
	 * Invoke {@link #list(Map) list()}and print the result on System.out.
	 * 
	 * @param array
	 *            see {@link #list(Map) list()}
	 */
	public void println(Map map) {
		System.out.println(list(map));
	}

	private class Entry {
		Object key;
		Object value;
		String methodToUse;

		public Entry(Object key, Object value, String methodToUse) {
			this.key = key;
			this.value = value;
			this.methodToUse = methodToUse;
		}

		public String toString() {
			return ((key == null) ? "<null>" : key.toString())
				+ " = "
				+ list(new Object[] { value }, methodToUse);
		}

	}

	/**
	 * Return a String containing a list of values in the map, obtained
	 * invoking the given method name on each element in the map.
	 * <p>
	 * The method must return a String and have no parameters.
	 * 
	 * @return a String containing a list of values in the map
	 * @param map
	 *            the map to list
	 * @param methodToUse
	 *            the name of the method to use
	 * @exception RuntimeException
	 *                if a method with the given name, which returns a String
	 *                and has no parameter is not available in the objects of
	 *                the map.
	 */
	public String list(Map map, String methodToUse) {
		Entry[] entries = new Entry[map.keySet().size()];
		int c = 0;
		for (Iterator i = map.keySet().iterator(); i.hasNext();) {
			Object key = i.next();
			Object value = map.get(key);
			entries[c++] = new Entry(key, value, methodToUse);
		}
		return list(entries, methodToUse);
	}

	/**
	 * Invoke {@link #list(java.util.Map, java.lang.String) list()}using the
	 * <tt>toString()</tt> method.
	 * 
	 * @param the
	 *            map to be listed
	 * @return a string listing the map
	 */
	public String list(Map map) {
		return list(map, "toString");
	}

	/**
	 * Returns the useToStringIfNotFound.
	 * 
	 * @return boolean
	 */
	public boolean isUseToStringIfNotFound() {
		return useToStringIfNotFound;
	}

	/**
	 * Sets the useToStringIfNotFound.
	 * 
	 * @param useToStringIfNotFound
	 *            The useToStringIfNotFound to set
	 */
	public void setUseToStringIfNotFound(boolean useToStringIfNotFound) {
		this.useToStringIfNotFound = useToStringIfNotFound;
	}

	/**
	 * Returns the default instance, which uses {@link #DEFAULT_SEPARATOR}.
	 * 
	 * @return ObjectLister the default object lister.
	 */
	public static ObjectLister getInstance() {
		return defaultInstance;
	}

	private static Object[] enumerate(Enumeration e) {
		List l = new ArrayList();
		while (e.hasMoreElements())
			l.add(e.nextElement());
		return l.toArray();
	}

	/**
	 * A test method
	 */
	public static void main(String args[])
		throws NoSuchMethodException, InvocationTargetException {
		Object[] array = new Object[3];
		array[0] = "Hello world";
		array[1] = new Thread("Test Thread 2");
		array[2] = new Object[0];

		Map map = new HashMap();
		map.put("Key 1", array[0]);
		map.put("Key 2", array[1]);
		map.put("Key 3", array[2]);
		map.put("bingo", null);

		System.out.println(new ObjectLister().list(map));
	}

	/**
	 * @return
	 */
	public boolean isSetAccessible() {
		return setAccessible;
	}

	/**
	 * @param setAccessible
	 */
	public void setSetAccessible(boolean setAccessible) {
		this.setAccessible = setAccessible;
	}

	/**
	 * @return
	 */
	public boolean isFailOnUnaccessible() {
		return failOnUnaccessible;
	}

	/**
	 * @param failOnUnaccessible
	 */
	public void setFailOnUnaccessible(boolean failOnUnaccessible) {
		this.failOnUnaccessible = failOnUnaccessible;
	}

	/**
	 * Return the default instance separating listed elements with a <tt>&ølt;br&gt;</tt>
	 * HTML element
	 * 
	 * @return
	 */
	public static ObjectLister getDefaultHTMLInstance() {
		return defaultHTMLInstance;
	}

}
