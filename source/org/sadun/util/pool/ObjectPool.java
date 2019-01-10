package org.sadun.util.pool;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.sadun.util.ObjectLister;
import org.sadun.util.Setup;

/**
 * An object pool, which holds <i>n</i> identical copies of an
 * object and allows access and release of each of them.
 * <p>
 * An object can be created by providing its class - in which case
 * the default constructor will be used to create the instances;
 * or by providing the construction parameters as an object array.
 * <p>
 * Optionally, after creation, a {@link org.sadun.util.Setup Setup object} can
 * be used to differentiate or further set up the created object.
 * <p>
 * This version supports Enterprise Java Beans creation via a JNDI name and home interface
 *
 * @author C. Sadun
 * @version 2.2
 */
public class ObjectPool implements Serializable {

	protected Set free;
	protected Set used;
	protected PooledObjectWrapper[] pool;
	private Factory factory;
	private boolean verbose = (System.getProperty("org.sadun.verbose") != null);

	/**
	 * An exception thrown in case of pooling operation errors.
	 */
	public static final class ObjectPoolException extends RuntimeException {

		private Throwable e;

		public ObjectPoolException(String msg) {
			super(msg);
		}

		public ObjectPoolException(Throwable e) {
			this("", e);
		}

		public ObjectPoolException(String msg, Throwable e) {
			super(
				msg
					+ "(caused by "
					+ e.getClass().getName()
					+ ": "
					+ e.getMessage()
					+ ")");
			this.e = e;
		}

		public Throwable getRootException() {
			return e;
		}

	}

	// This is just to have another object monitor for index-based acquire/released
	protected static final class PooledObjectWrapper implements Serializable {
		private Object object;
		PooledObjectWrapper(Object object) {
			this.object = object;
		}
		public Object getObject() {
			return object;
		}
	}

	/**
	 * If an object to be pooled can't be constructed directly by
	 * invoking a constructor, a Factory can be provided
	 * to do the construction job.
	 */
	public static interface Factory extends Serializable {

		/**
		 * This method is invoked by the pool when a pooled instance
		 * has to be created.
		 * @return the newly created object
		 * @exception ObjectPoolException if there is a problem creating the object
		 */
		public Object create() throws ObjectPoolException;

		/**
		 * This method is invoked by the pool when a pooled instance
		 * has to be destroyed.
		 * @param obj the object to destroy
		 * @exception ObjectPoolException if there is a problem destroying the object
		 */
		public void destroy(Object obj) throws ObjectPoolException;

		/**
		 * This method is invoked when validating the created class.
		 *
		 * @return the class object for the type of object this factory will create
		 */
		public Class getProducedClass();
	}

	/**
	 * A base implementation of {@link ExtendedObjectPool.Factory ExtendedObjectPool.Factory}
	 * relying on reflection, holding a Class object, an optional parameter array
	 * and an optional {@link org.sadun.util.Setup Setup} object.
	 * <p>
	 * These objects are made available to subclasses by the members {@link #cls cls},
	 * {@link #params params} and {@link #ps ps}.
	 *
	 * @author Cristiano Sadun
	 * @version 1.0
	 */
	public static abstract class BaseFactory implements Factory {

		/**
		 * The class of the objects produced by the factory.
		 */
		protected Class cls;

		/**
		 * The parameters to use for constructing the object. It is never <b>null</b>, but may have size zero.
		 */
		protected Object[] params;

		/**
		 * The setup object to use for post-construction initialization. Can be <b>null</b>.
		 */
		protected Setup ps;

		/**
		 * The types of the parameters to use for constructing the object. It is never <b>null</b>, but may have size zero.
		 */
		protected Class[] paramCls;

		/**
		 * Create a BaseFactory whose member {@link #cls cls} will hold the class of the objects to produce
		 * @param cls the class of the objects to produce
		 * @param params the construction parameters, or <b>null</b>
		 * @param ps the {@link org.sadun.util.Setup Setup} object to be used for post-construction setup, or <b>null</b>
		 */
		public BaseFactory(Class cls, Object[] params, Setup ps) {
			if (params == null)
				params = new Object[0];
			this.cls = cls;
			this.params = params;
			this.ps = ps;
			this.paramCls = new Class[params.length];
			for (int i = 0; i < params.length; i++)
				paramCls[i] = params[i].getClass();
		}

		/**
		 * Create a BaseFactory whose member {@link #cls cls} will hold the class of the objects to produce
		 * @param clsName the name of the class of the objects to produce
		 * @param params the construction parameters, or <b>null</b>
		 * @param ps the {@link org.sadun.util.Setup Setup} object to be used for post-construction setup, or <b>null</b>
		 */
		public BaseFactory(String clsName, Object[] params, Setup ps) {
			this(findClass(clsName), params, ps);
		}

		/**
		 * Subclasses must implement the code which actually creates a new instance of the class
		 * to pool.
		 */
		public abstract Object create();

		/**
		 * A null implementation of {@link ObjectPool.Factory#destroy(Object) ObjectPool.Factory.destroy()}.
		 */
		public void destroy(Object obj) {
		}

		/**
		 * Return the class object for the type of object this factory creates
		 *
		 * @return the class object for the type of object this factory creates
		 */
		public Class getProducedClass() {
			return cls;
		}

	}

	private static Class findClass(String clsName) throws ObjectPoolException {
		try {
			return Class.forName(clsName);
		} catch (ClassNotFoundException e) {
			throw new ObjectPoolException(e);
		}
	}

	/**
	 * An {@link ExtendedObjectPool.Factory ExtendedObjectPool.Factory} implementation which uses reflection
	 * to create instances of a certain class.
	 * <p>
	 * After creation, objects can be optionally setup by an user-provided class implementing
	 * the {@link org.sadun.util.Setup Setup} interface.
	 *
	 * @author Cristiano Sadun
	 * @version 1.0
	 */
	public static class ObjectFactory extends BaseFactory {

		/**
		 * Create a factory which will construct object of the given class, with the given parameters,
		 * using the given {@link org.sadun.util.Setup Setup object}.
		 * <p>
		 * The construction parameter types are deduced from the types of the passed parameter objects,
		 * and a compatible constructor is searched for.
		 *
		 * @param cls the class of the object to build
		 * @param params the construction parameters to use, or <b>null</b>
		 * @param ps the {@link org.sadun.util.Setup Setup} object to be used for post-construction setup, or <b>null</b>
		 */
		public ObjectFactory(Class cls, Object[] params, Setup ps) {
			super(cls, params, ps);
		}

		/**
		 * Create a factory which will construct object of the given class, with the given parameters,
		 * using the given {@link org.sadun.util.Setup Setup object}.
		 * <p>
		 * The construction parameter types are deduced from the types of the passed parameter objects,
		 * and a compatible constructor is searched for.
		 *
		 * @param clsName the name of class of the object to build
		 * @param params the construction parameters to use, or <b>null</b>
		 * @param ps the {@link org.sadun.util.Setup Setup} object to be used for post-construction setup, or <b>null</b>
		 */
		public ObjectFactory(String clsName, Object[] params, Setup ps) {
			super(clsName, params, ps);
		}

		/**
		 * Create an instance of the class defined at construction, using the (optional) parameters
		 * and the (optional) {@link org.sadun.util.Setup Setup} object defined at construction for construction
		 * and post-construction initialization.
		 * @return the newly created object
		 * @exception ObjectPoolException if there is a problem creating the object
		 */
		public Object create() {
			try {
				Constructor ctor = this.cls.getConstructor(paramCls);
				Object obj = ctor.newInstance(params);
				if (ps != null)
					ps.setup(obj);
				return obj;
			} catch (NoSuchMethodException e) {
				throw new ObjectPoolException(
					"The class "
						+ this.cls.getName()
						+ " does not have a constructor matching the passed parameter objects ("+ObjectLister.getInstance().list(params));
			} catch (InstantiationException e) {
				throw new ObjectPoolException(
					"Could not instantiate " + this.cls.getName());
			} catch (IllegalAccessException e) {
				throw new ObjectPoolException(
					"Could not access proper constructor in "
						+ this.cls.getName());
			} catch (InvocationTargetException e) {
				throw new ObjectPoolException(
					"Object construction failed with exception "
						+ e.getTargetException());
			}
		}

		/**
		 * This implementation does not do anything.
		 */
		public void destroy(Object obj) {

		}
	}

	/**
	 * Create a pool of <i>n</i> objects using the given factory
	 */
	public ObjectPool(int n, Factory factory) {
		if (n == 0)
			throw new IllegalArgumentException("Can't build a pool of 0 objects");
		this.used = new HashSet();
		this.free = new HashSet();
		this.pool = new PooledObjectWrapper[n];
		this.factory = factory;
		Class cls = factory.getProducedClass();
		if (verbose)
			System.out.println(
				"Creating " + n + " objects of type " + cls.getName());
		for (int i = 0; i < n; i++) {
			pool[i] = new PooledObjectWrapper(factory.create());
			if (!cls.isAssignableFrom(pool[i].object.getClass()))
				throw new ObjectPoolException(
					"The provided factory "
						+ factory
						+ " must create only objects of type "
						+ cls.getName()
						+ ". The produced object has type "
						+ pool[i].object.getClass().getName()
						+ " instead");
			if (free.contains(pool[i].object))
				throw new ObjectPoolException(
					"Attempting to add the same object (\""
						+ ((Object) pool[i].object).toString()
						+ "\") twice to the pool");
			free.add(pool[i].object);
		}
		if (verbose)
			System.out.println("Object pool created");
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
	public ObjectPool(int n, String clsName, Object[] params, Setup ps)
		throws ClassNotFoundException {
		this(n, new ObjectFactory(Class.forName(clsName), params, ps));
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
	public ObjectPool(int n, String clsName, Object[] params)
		throws ClassNotFoundException {
		this(n, Class.forName(clsName), params, null);
	}

	/**
	 * Create a pool of <i>n</i> object of the given class (by name)
	 * using the default constructor.
	 * @param n the size of the pool
	 * @param clsName the name of the class of the objects to pool
	 * @exception ClassNotFoundException if the given class name cannot be resolved
	 */
	public ObjectPool(int n, String clsName) throws ClassNotFoundException {
		this(n, Class.forName(clsName), null);
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
	public ObjectPool(int n, Class cls, Object[] params, Setup ps) {
		this(n, new ObjectFactory(cls, params, ps));
	}

	/**
	 * Create a pool of <i>n</i> object of the given class
	 * using the given construction parameters.
	 * <p>
	 * @param n the size of the pool
	 * @param cls the class of the objects to pool
	 * @param params the construction parameters, or <b>null</b>
	 */
	public ObjectPool(int n, Class cls, Object[] params) {
		this(n, cls, params, null);
	}

	/**
	 * Create a pool of <i>n</i> object of the given class
	 * using the default constructor.
	 * @param n the size of the pool
	 * @param cls the class of the objects to pool
	 */
	public ObjectPool(int n, Class cls) {
		this(n, cls, null);
	}

	/**
	 * Return the number of available objects in the pool
	 * @return the number of available objects in the pool
	 */
	public synchronized int getFreeCount() {
		return free.size();
	}

	/**
	 * Return the number of used objects in the pool
	 * @return the number of used objects in the pool
	 */
	public synchronized int getUsedCount() {
		return used.size();
	}

	/**
	 * Return the size of the pool
	 * @return the size of the pool
	 */
	public synchronized int getSize() {
		return free.size() + used.size();
	}

	/**
	 * Attempt to acquire an object.
	 * @param waitIfUnavailable if <b>true</b>, in case all the pooled objects
	 *        are used, the call will block until an object is released.
	 *        If <b>false</b>, in the same condition the method returns <b>null</b>.
	 * @return a pooled object
	 */
	public synchronized Object acquire(boolean waitIfUnavailable) {
		if (free.isEmpty())
			if (waitIfUnavailable) {
				try {
					wait(0);
				} catch (InterruptedException e) {
					//e.printStackTrace();
					return null;
				}
			} else
				return null;
		// The object may have been released because has been renewed,
		// and taken out of the pool; in this case, we just have to
		// re-perform the emptyness check by recursively calling acquire()
		if (free.isEmpty())
			return acquire(waitIfUnavailable);
		Object obj = free.iterator().next();
		acquire0(obj);
		return obj;
	}

	private void acquire0(Object obj) {
		free.remove(obj);
		used.add(obj);
	}

	/**
	 * Attempt to acquire an object.
	 * <p>
	 * If there aren't any objects available, this method blocks until
	 * one becomes available.
	 * @return a pooled object
	 */
	public Object acquire() {
		return acquire(true);
	}

	/**
	 * Attempt to acquire the i-th object.
	 * <p>
	 * If the object is not available, this method blocks until
	 * the object becomes available.
	 * @return a pooled object
	 */
	public synchronized Object acquire(int i) {
		PooledObjectWrapper pObj = pool[i];
		if (used.contains(pObj.object)) {
			try {
				pObj.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}
		acquire0(pObj.object);
		return pObj.object;
	}

	/**
	 * Releases the i-th object, notifying the waiting thread (if any) that
	 * one pooled object has become available.
	 * @param the pooled object to release
	 */
	public synchronized void release(int i) {
		PooledObjectWrapper pObj = pool[i];
		release0(pObj.object);
		pObj.notify();
	}

	/**
	 * Release an object.
	 * <p>
	 * @param releaseWaitingCalls if <b>true</b>, the method notifies waiting objects that
	 *                            one pooled object has become available.
	 * @param the pooled object to release
	 */
	public synchronized void release(Object obj, boolean releaseWaitingCalls) {
		if (!used.contains(obj))
			throw new IllegalArgumentException(
				"The object "
					+ obj
					+ " is not a pooled object or has been renewed");
		release0(obj);
		if (releaseWaitingCalls)
			notify();
	}

	private void release0(Object obj) {
		used.remove(obj);
		free.add(obj);
	}

	/**
	 * Release an object, notifying waiting thread (if any) that
	 * one pooled object has become available.
	 * @param the pooled object to release
	 */
	public void release(Object obj) {
		release(obj, true);
	}

	/**
	 * Renew one object in the pool.
	 * <p>
	 * A new instance is created substituting the passed object in
	 * the pool, and the new instance is returned. The object is released() 
	 * but any thread waiting on that object shifts waiting for another object.
	 */
	public synchronized Object renew(Object obj) {
		// Create a new instance
		Object obj2 = factory.create();

		// Find the object in the pool
		int c = -1;
		for (int i = 0; i < pool.length; i++)
			if (pool[i].object == obj) {
				c = i;
				break;
			}
		if (c == -1) {
			if (verbose)
				System.out.println(this);
			throw new IllegalArgumentException(
				"The object <" + obj + "> is not a pooled object");
		}

		// Replace the object
		if (used.contains(obj)) {
			synchronized (obj) {
				release(obj);
				free.remove(obj);
				used.add(obj2);
				if (verbose)
					System.out.println(
						"Used object " + obj + " renewed by " + obj2);
			}
		} else if (free.contains(obj)) {
			synchronized (obj) {
				free.remove(obj);
				free.add(obj2);
				if (verbose)
					System.out.println(
						"Free object " + obj + " renewed by " + obj2);
			}
		}
		pool[c] = new PooledObjectWrapper(obj2);
		return obj2;
	}

	/**
	 * Return a string description of the pool
	 * @return a string description of the pool
	 */
	public synchronized String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println(
			"Pool of "
				+ pool.length
				+ " objects of type "
				+ factory.getProducedClass().getName());
		for (Iterator i = used.iterator(); i.hasNext();) {
			pw.println("[Used] \"" + i.next() + "\"");
		}
		for (Iterator i = free.iterator(); i.hasNext();) {
			pw.println("[Free] \"" + i.next() + "\"");
		}
		return sw.toString();
	}

	/**
	 * A convenience method to create a new pool of objects of a given class, by name,
	 * using their default constructor.
	 * @param poolSize the size of the pool
	 * @param clsName the name of the class of the pooled objects
	 * @exception ObjectPoolException if the pool cannot be initialized
	 */
	public static ObjectPool newPool(int poolSize, String clsName)
		throws ObjectPoolException {
		return new ObjectPool(poolSize, new ObjectFactory(clsName, null, null));
	}

	/**
	 * A convenience method to create a new pool of objects of a given class, by name,
	 * using the given parameters.
	 * @param poolSize the size of the pool
	 * @param clsName the name of the class of the pooled objects
	 * @param params the parameters used to initialize the objects of the pool
	 * @exception ObjectPoolException if the pool cannot be initialized
	 */
	public static ObjectPool newPool(
		int poolSize,
		String clsName,
		Object[] params)
		throws ObjectPoolException {
		return new ObjectPool(
			poolSize,
			new ObjectFactory(clsName, params, null));
	}

	/**
	 * Return the object type pooled by this pool
	 */
	public Class getObjectType() {
		return factory.getProducedClass();
	}

	/**
	* Returns the factory.
	* @return Factory
	*/
	protected Factory getFactory() {
		return factory;
	}

	/**
	 * Sets the factory.
	 * @param factory The factory to set
	 */
	protected void setFactory(Factory factory) {
		this.factory = factory;
	}

	/**
	 * Destroys all the objects in the pool by invoking
	 * the {@link Factory#destroy(Object) Factory.destroy()} method
	 * on the pool's object factory.
	 * 
	 * @param waitForReleasedObjects waits until all the objects are released before
	 *                                destroying them
	 */
	public void destroy(boolean waitForReleasedObjects) {
		int destroyed = 0;
		if (verbose)
			System.out.println(
				"Destroying object pool, "
					+ (waitForReleasedObjects ? "" : "not ")
					+ "waiting for objects in use to be released");
		do {
			for (int i = 0; i < pool.length; i++) {
				if (pool[i] == null)
					continue;

				synchronized (pool[i]) {
					boolean doDestroy = true;
					if (waitForReleasedObjects) {
						if (used.contains(pool[i].object)) {
							doDestroy = false;
							if (verbose)
								System.out.println(
									"(Waiting for "
										+ pool[i].object
										+ " to be freed)");
						}
					}

					if (doDestroy) {
						factory.destroy(pool[i].object);
						free.remove(pool[i].object);
						used.remove(pool[i].object);
						pool[i] = null;
						destroyed++;
					}
				}
			}
		} while (waitForReleasedObjects && destroyed < pool.length);

		if (verbose)
			System.out.println("Object pool destroyed");

	}



	/*
	 public static void main(String args[]) throws Exception {
	    final ExtendedObjectPool pool = new ExtendedObjectPool(2, String.class, new Object [] { "Hello world" });
	    final java.util.Random random = new java.util.Random();
	    final Set acquired = new HashSet();
	
	    Thread t1 = new Thread("acquirer") {
	        public void run() {
	            while(true) {
	                if (random.nextInt(3) < 2) {
	                    System.out.println("Aquiring - "+pool.getFreeCount()+" free objects in pool");
	                    acquired.add(pool.acquire());
	                } else {
	                    System.out.println("Consumer sleeping");
	                    try {
	                        sleep(1000);
	                    } catch(InterruptedException e) {
	                    }
	                    System.out.println("Consumer awakening");
	                }
	            }
	        }
	    };
	
	    Thread t2 = new Thread("remover") {
	            public void run() {
	                while(true) {
	                    if (random.nextInt(3) < 1)
	                        if (! acquired.isEmpty()) {
	                            Object obj = acquired.iterator().next();
	                            acquired.remove(obj);
	                            pool.release(obj);
	                            System.out.println("Removed  - "+pool.getFreeCount()+" free objects in pool");
	                        }
	                }
	            }
	        };
	
	    t2.start();
	    t1.start();
	    }
	
	*/

}