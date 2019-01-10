/*
 * Created on Aug 28, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.pool;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sadun.util.IndentedPrintWriter;
import org.sadun.util.ListMapIterator;
import org.sadun.util.Setup;
import org.sadun.util.pool.ObjectPool.Factory;

/**
 * An object pool identical to {@link ObjectPool ObjectPool} but which requires
 * the acquiring object to identify itself. 
 * 
 * @author Cristiano Sadun
 */
public class ExtendedObjectPool {

	private ObjectPool pool;

	/**
	 * A map of owner -> List(owned)
	 */
	private Map allocationTable = new HashMap();

	/**
	* Create a pool of <i>n</i> objects using the given factory
	*/
	public ExtendedObjectPool(int n, Factory factory) {
		pool = new ObjectPool(n, factory);
	}

	/**
	 * Create a pool of <i>n</i> object of the given class (by name)
	 * using the given construction parameters.
	 * <p>
	 * If some post-construction setup is needed, it can
	 * be provided as an Setup object.
	 *
	 * @param n the size of the pool
	 * @param clsName the name of the class of the objects to pool
	 * @param params the construction parameters, or <b>null</b>
	 * @param the post-construction setup object, or <b>null</b>
	 * @exception ClassNotFoundException if the given class name cannot be resolved
	 */
	public ExtendedObjectPool(int n, String clsName, Object[] params, Setup ps)
		throws ClassNotFoundException {
		this.pool = new ObjectPool(n, clsName, params, ps);
	}

	/**
	 * Create a pool of <i>n</i> object of the given class (by name)
	 * using the given construction parameters.
	 * <p>
	 * @param n the size of the pool
	 * @param clsName the name of the class of the objects to pool
	 * @param params the construction parameters, or <b>null</b>
	 * @exception ClassNotFoundException if the given class name cannot be resolved
	 */
	public ExtendedObjectPool(int n, String clsName, Object[] params)
		throws ClassNotFoundException {
		this.pool = new ObjectPool(n, clsName, params);
	}

	/**
	 * Create a pool of <i>n</i> object of the given class (by name)
	 * using the default constructor.
	 * @param n the size of the pool
	 * @param clsName the name of the class of the objects to pool
	 * @exception ClassNotFoundException if the given class name cannot be resolved
	 */
	public ExtendedObjectPool(int n, String clsName)
		throws ClassNotFoundException {
		this.pool = new ObjectPool(n, clsName);
	}

	/**
	 * Create a pool of <i>n</i> object of the given class
	 * using the given construction parameters.
	 * <p>
	 * If some post-construction setup is needed, it can
	 * be provided as an Setup object.
	 *
	 * @param n the size of the pool
	 * @param cls the class of the objects to pool
	 * @param params the construction parameters, or <b>null</b>
	 * @param the post-construction setup object, or <b>null</b>
	 */
	public ExtendedObjectPool(int n, Class cls, Object[] params, Setup ps) {
		this.pool = new ObjectPool(n, cls, params, ps);
	}

	/**
	 * Create a pool of <i>n</i> object of the given class
	 * using the given construction parameters.
	 * <p>
	 * @param n the size of the pool
	 * @param cls the class of the objects to pool
	 * @param params the construction parameters, or <b>null</b>
	 */
	public ExtendedObjectPool(int n, Class cls, Object[] params) {
		this.pool = new ObjectPool(n, cls, params);
	}

	/**
	 * Create a pool of <i>n</i> object of the given class
	 * using the default constructor.
	 * @param n the size of the pool
	 * @param cls the class of the objects to pool
	 */
	public ExtendedObjectPool(int n, Class cls) {
		this.pool = new ObjectPool(n, cls);
	}

	/**
	 * Return the number of available objects in the pool
	 * @return the number of available objects in the pool
	 */
	public int getFreeCount() {
		return pool.getFreeCount();
	}

	/**
	 * Return the number of used objects in the pool
	 * @return the number of used objects in the pool
	 */
	public int getUsedCount() {
		return pool.getUsedCount();
	}

	/**
	 * Return the size of the pool
	 * @return the size of the pool
	 */
	public int getSize() {
		return pool.getSize();
	}

	/**
	 * Attempt to acquire an object.
	 * @param requester the object performing the attempt
	 * @param waitIfUnavailable if <b>true</b>, in case all the pooled objects
	 *        are used, the call will block until an object is released.
	 *        If <b>false</b>, in the same condition the method returns <b>null</b>.
	 * @return a pooled object
	 */
	public synchronized Object acquire(
		Object requester,
		boolean waitIfUnavailable) {
		Object obj = pool.acquire(waitIfUnavailable);
		if (obj != null)
			allocate(requester, obj);
		return obj;
	}

	/**
	 * Attempt to acquire an object.
	 * <p>
	 * If there aren't any objects available, this method blocks until
	 * one becomes available.
	 * @param requester the object performing the attempt
	 * @return a pooled object
	 */
	public Object acquire(Object requester) {
		return acquire(requester, true);
	}

	/**
	 * Attempt to acquire the i-th object.
	 * <p>
	 * If the object is not available, this method blocks until
	 * the object becomes available.
	 * @param requester the object performing the attempt
	 * @return a pooled object
	 */
	public Object acquire(Object requester, int i) {
		Object obj = pool.acquire(i);
		if (obj != null)
			allocate(requester, obj);
		return obj;
	}

	private void allocate(Object requester, Object obj) {
		List l = (List) allocationTable.get(requester);
		if (l == null) {
			l = new ArrayList();
			allocationTable.put(requester, l);
		}
		l.add(obj);
	}

	/**
	 * Release an object.
	 * <p>
	 * @param releaseWaitingCalls if <b>true</b>, the method notifies waiting objects that
	 *                            one pooled object has become available.
	 * @param the pooled object to release
	 */
	public synchronized void release(Object obj, boolean releaseWaitingCalls) {
		deallocate(obj);
		pool.release(obj, releaseWaitingCalls);
	}

	/**
	 * Release an object, notifying waiting thread (if any) that
	 * one pooled object has become available.
	 * @param the pooled object to release
	 */
	public void release(Object obj) {
		release(obj, true);
	}

	private void deallocate(Object obj) {
		ListMapIterator lm = new ListMapIterator(allocationTable);
		while (lm.hasNext()) {
			Object allocatedObj = lm.next();
			if (allocatedObj == obj) {
				List l = (List) (allocationTable.get(lm.getCurrentKey()));
				l.remove(obj);
				if (l.size() == 0)
					allocationTable.remove(lm.getCurrentKey());
				break;
			}
		}
	}

	/**
	 * Renew one object in the pool.
	 * <p>
	 * A new instance is created substituting the passed object in
	 * the pool, and the new instance is returned. The object is released() 
	 * but any thread waiting on that object shifts waiting for another object.
	 */
	public synchronized Object renew(Object owner, Object obj) {
		Object newObj = pool.renew(obj);
		deallocate(obj);
		allocate(owner, newObj);
		return newObj;
	}

	/**
	 * Return a string description of the pool
	 * @return a string description of the pool
	 */
	public synchronized String toString() {
		StringWriter sw = new StringWriter();
		IndentedPrintWriter pw = new IndentedPrintWriter(sw);
		pw.println(pool.toString());
		pw.println("[Allocation table]");
		pw.println();
		pw.incIndentation(3);
		ListMapIterator i = new ListMapIterator(allocationTable);
		while (i.hasNext()) {
			Object allocatedObj = i.next();
			Object owner = i.getCurrentKey();
			pw.println(
				allocatedObj.getClass().getName()
					+ " (\""
					+ allocatedObj.toString()
					+ "\"): \t"
					+ owner.getClass().getName()
					+ " (\""
					+ owner.toString()
					+ "\")");
		}

		return sw.toString();
	}

	public static void main(String args[]) throws Exception {
		ExtendedObjectPool pool = new ExtendedObjectPool(2, Thread.class);
		Object owner = new Object();
		Thread t = (Thread) pool.acquire(owner);

		System.out.println(pool);

		pool.release(t);

	}

}
