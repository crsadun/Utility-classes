package org.sadun.util.pool2;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.sadun.util.Setup;
import org.sadun.util.pool2.test.ObjectWithID;

/**
 * <font color=red>NOT COMPLETE YET</font>.
 * <p>
 * A pool of object which handles transparent passivation/activation.
 * <p>
 * This pool create and allocate pooled objects referencing the original objects
 * and implementing the same interface(s). 
 * <p>
 * The pool can be used either accessing objects by interface or by class.
 * <p>
 * The given object class should implement one or more specific interface(s). The instances
 * returned by the {@link org.sadun.util.pool.ObjectPool#acquire() acquire()} method
 * can be directly cast to that interface(s). For example, if a class 
 * <tt>MyTestObject</tt> implements the <tt>javax.swing.Action</tt> interface,
 * the following code can be used:
 * <pre>
 *  ObjectPool pool = new ObjectPool("test pool", 10, MyTestObject.class);
 *  ...
 *  Action action = (Action)pool.acquire();
 *  Object obj = action.getValue();
 *  ...
 *  action.release();
 *  ...
 *  pool.dispose();
 * </pre> 
 * <p>
 * If the given object is used directly as a class (rather than interface), the
 * following conditions hold:
 * <p>
 * <ul>
 * <li> The object returned by the pool's {@link org.sadun.util.pool.ObjectPool#acquire() acquire()} 
 *      method cannot be cast to the original class directly, but to the {@link PooledObject
 *      PooledObject} interface.
 * <li> The returned object implements all the methods of the original object, but since it's
 *      type is possibly generated at runtime, the methods are only available trhough reflection.
 * <li> The original object can be obtained by invoking {@link PooledObject#_getOriginal() _getOriginal()}
 *      on the returned object, and must be released by {@link PooledObject#_getOriginal() _releaseOriginal()}.
 * </ul>
 * <p>
 * For example:
 * <pre>
 *   ObjectPool pool = new ObjectPool("test pool", 10, MyTestObject.class);
 *   ...
 *   PooledObject pObj = (PooledObject)pool.acquire();
 *   MyTestObject obj = (MyTestObject)pObj._getOriginal();
 *   obj.myTestMethod();
 *   pObj._releaseOriginal();
 * </pre>.
 *   
 * @version 1.0
 * @author Cristiano Sadun
 */
public class ObjectPool extends org.sadun.util.pool.ObjectPool {

	private String name;
	private PrintStream logStream;
	private static PrintStream defaultLogStream;
	private Configuration configuration;
	private BasePassivationManager passivationManager;

	/**
	 * A base class for a factory of {@link BasePassivationManager BasePassivationManager}s.
	 * It can be extended to produce a specific subclass of {@link BasePassivationManager 
	 * BasePassivationManager}.
	 */
	protected abstract static class PassivationManagerFactory {
		/**
		 * Return a specific subclass of {@link BasePassivationManager 
		 * BasePassivationManager} implementing a certain passivation
		 * policy.
		 * 
		 * @return a concrete subclass of {@link BasePassivationManager 
		 * BasePassivationManager}
		 */
		protected abstract BasePassivationManager createPassivationThread();
	}

	/**
	 * An {@link ObjectPool.PassivationManagerFactory ObjectPool.PassivationManagerFactory} which
	 * produces {@link DefaultPassivationManager DefaultPassivationManager}s
	 */
	public final static class DefaultPassivationManagerFactory
		extends PassivationManagerFactory {
		/**
		 * Return the single instance of {@link DefaultPassivationManager DefaultPassivationManager}.
		 * @return the single instance of {@link DefaultPassivationManager DefaultPassivationManager}.
		 */
		protected BasePassivationManager createPassivationThread() {
			return DefaultPassivationManager.getInstance();
		}
	}

	/**
	 * A {@link org.sadun.util.pool.ObjectPool.Factory object factory} which produces {@link BasePooledObject BasePooledObject}
	 * wrappers for other classes.
	 * <p>
	 * In detail, this class uses an existing 
	 * {@link org.sadun.util.pool.ObjectPool.Factory object factory}
	 * to produce objects of class C, obtains an appropriate <tt>Pooled<i>C</i></tt> subclass of 
	 * {@link BasePooledObject BasePooledObject} and then wraps the objects in instances of <tt>Pooled<i>C</i></tt>.
	 */
	protected static class PooledObjectFactory
		implements org.sadun.util.pool.ObjectPool.Factory {

		private Class pooledClass;
		private Factory factory;
		private Constructor pooledClassConstructor;
		private ObjectPool pool;
		private static StubClassLoader scl;
		private static Object lock = new Object();

		/**
		 * Create a factory which is used by the given {@link ObjectPool ObjectPool} and employs the given 
		 * {@link org.sadun.util.pool.ObjectPool.Factory org.sadun.util.pool.ObjectPool.Factory}
		 * @param pool the {@link ObjectPool ObjectPool} using this factory to create pooled objects
		 * @param factory the {@link org.sadun.util.pool.ObjectPool.Factory org.sadun.util.pool.ObjectPool.Factory} to produce
		 *         the original instances
		 */
		protected PooledObjectFactory(ObjectPool pool, Factory factory)
			throws ObjectPoolException {

			this.factory = factory;
			this.pool = pool;

			// Create the stub class loader
			createStubClassLoader();

			// Load the pooled class			
			try {
				pooledClass =
					scl.loadClass(
						scl.getPooledClassName(factory.getProducedClass()));

				try {
					pooledClassConstructor =
						pooledClass.getConstructor(
							new Class[] {
								ObjectPool.class,
								factory.getProducedClass()});
				} catch (NoSuchMethodException e) {
					// This shouldn't happen, since the wrapper is generated on purpose...
					throw new RuntimeException(
						"Corrupted class " + pooledClass.getName(),
						e);
				}

			} catch (ClassNotFoundException e) {
				throw new ObjectPoolException(
					"Could not generate/load the pooled class for "
						+ factory.getProducedClass().getName(),
					e);
			}
		}

		private static void createStubClassLoader()
			throws ObjectPoolException {
			if (scl == null)
				synchronized (lock) {

					Class cls = BasePooledObject.class;
					try {
						scl = new StubClassLoader(cls);
						scl.setLogStream(getDefaultLogStream());
					} catch (IOException e) {
						e.printStackTrace();
						throw new IllegalStateException(
							"Cannot create StubClassLoader using "
								+ cls.getName());
					}
				}
		}

		/**
		 * @see org.sadun.util.pool.ObjectPool.Factory#create()
		 */
		public Object create() throws ObjectPoolException {
			// Use the original factory to create the object
			try {
				BasePooledObject obj =
					(BasePooledObject) pooledClassConstructor.newInstance(
						new Object[] { null, factory.create()});
				obj.setObjectPool(pool);
				return obj;
			} catch (InstantiationException e) {
				throw new ObjectPoolException(
					"Cannot instantiate pooled object for "
						+ factory.getProducedClass().getName(),
					e);
			} catch (IllegalAccessException e) {
				throw new ObjectPoolException(
					"Cannot instantiate pooled object for "
						+ factory.getProducedClass().getName(),
					e);
			} catch (InvocationTargetException e) {
				throw new ObjectPoolException(
					"Cannot instantiate pooled object for "
						+ factory.getProducedClass().getName(),
					e);
			}

		}

		/**
		 * @see org.sadun.util.pool.ObjectPool.Factory#getProducedClass()
		 */
		public Class getProducedClass() {
			return pooledClass;
		}

		/* (non-Javadoc)
		 * @see org.sadun.util.pool.ObjectPool.Factory#destroy(java.lang.Object)
		 */
		public void destroy(Object obj) throws ObjectPoolException {
			// NOT IMPLEMENTED YET

		}

	}

	/**
	 * Create a named object pool of objects of the given class and
	 * the given {@link Configuration Configuration} (which also provides
	 * the size of the pool).
	 * <p>
	 * The objects are created by invoking their default constructor.
	 * @param poolName the name of the pool
	 * @param configuration a {@link Configuration Configuration} object
	 * @param objectType the class of objects to create
	 */
	public ObjectPool(
		String poolName,
		Configuration configuration,
		Class objectType)
		throws ObjectPoolException {
		this(poolName, configuration, objectType, (Setup) null);
	}

	/**
	 * Create a named object pool of objects of the given class and
	 * the given {@link Configuration Configuration} (which also provides
	 * the size of the pool).
	 * <p>
	 * The objects are created by invoking their default constructor.
	 * <p>
	 * After the creation, each object is passed by the given {@link 
	 * org.sadun.util.Setup Setup object}.
	 * 
	 * @param poolName the name of the pool
	 * @param configuration a {@link Configuration Configuration} object
	 * @param objectType the class of objects to create
	 * @param setupObject the {@link org.sadun.util.Setup Setup object} used 
	 *         for post-construction setup
	 */
	public ObjectPool(
		String poolName,
		Configuration configuration,
		Class objectType,
		Setup setupObject)
		throws ObjectPoolException {
		this(poolName, configuration, objectType, new Object[] {
		}, setupObject);
	}

	/**
	 * Create a named object pool of objects of the given class and
	 * the given {@link Configuration Configuration} (which also provides
	 * the size of the pool).
	 * <p>
	 * The objects are created by invoking the constructor matching the types
	 * of the given parameter objects.
	 * 
	 * @param poolName the name of the pool
	 * @param configuration a {@link Configuration Configuration} object
	 * @param objectType the class of objects to create
	 * @param params the array of parameters used for constructing the object
	 */
	public ObjectPool(
		String poolName,
		Configuration configuration,
		Class objectType,
		Object[] params) {
		this(poolName, configuration, objectType, params, null);
	}

	/**
	 * Create a named object pool of objects of the given class and
	 * the given {@link Configuration Configuration} (which also provides
	 * the size of the pool).
	 * <p>
	 * The objects are created by invoking the constructor matching the types
	 * of the given parameter objects.
	 * <p>
	 * After the creation, each object is passed by the given {@link 
	 * org.sadun.util.Setup Setup object}.
	 * 
	 * @param poolName the name of the pool
	 * @param configuration a {@link Configuration Configuration} object
	 * @param objectType the class of objects to create
	 * @param params the array of parameters used for constructing the object
	 * @param setupObject the {@link org.sadun.util.Setup Setup object} used 
	 *         for post-construction setup
	 */
	public ObjectPool(
		String poolName,
		Configuration configuration,
		Class objectType,
		Object[] params,
		Setup setupObject)
		throws ObjectPoolException {
		this(
			poolName,
			configuration,
			new ObjectFactory(objectType, params, setupObject));
	}

	/**
	 * Create a named object pool of <tt>n</tt> objects of the given class and
	 * a default {@link Configuration Configuration}.
	 * <p>
	 * The objects are created by invoking the constructor matching the types
	 * of the given parameter objects.
	 * 
	 * @param n the number of objects to create
	 * @param poolName the name of the pool
	 * @param objectType the class of objects to create
	 * @param params the array of parameters used for constructing the object
	 */
	public ObjectPool(
		String poolName,
		int n,
		Class objectType,
		Object[] params) {
		this(poolName, new Configuration(n), objectType, params);
	}

	/**
	 * Create a named object pool of <tt>n</tt> objects of the given class and
	 * a default {@link Configuration Configuration}.
	 * <p>
	 * The objects are created by invoking their default constructor.
	 * 
	 * @param n the number of objects to create
	 * @param poolName the name of the pool
	 * @param objectType the class of objects to create
	 * @param params the array of parameters used for constructing the object
	 */
	public ObjectPool(String poolName, int n, Class objectType) {
		this(poolName, new Configuration(n), objectType, new Object[0]);
	}

	/**
	 * Create a named object pool of objects of the given class and
	 * the given {@link Configuration Configuration} (which also provides
	 * the size of the pool).
	 * <p>
	 * The objects are created by using the given {@link org.sadun.util.pool.ObjectPool.Factory 
	 * object factory}.
	 * <p>
	 * @param poolName the name of the pool
	 * @param configuration a {@link Configuration Configuration} object
	 * @param factory the {@link org.sadun.util.pool.ObjectPool.Factory  object factory} which produces the instances to pool.
	 */
	public ObjectPool(
		String poolName,
		Configuration configuration,
		Factory factory)
		throws ObjectPoolException {
		super(
			configuration.getPoolSize(),
			new PooledObjectFactory(null, factory));
		setPool(createObjectsArray(), this);

		this.configuration = configuration;
		this.name = poolName;
		this.logStream = getDefaultLogStream();

		passivationManager =
			configuration
				.getPassivationManagerFactory()
				.createPassivationThread();
		passivationManager.setLogStream(logStream);

		passivationManager.addPool(this);

		// Initialize
		if (logStream != null)
			logStream.println(
				"Pool of "
					+ getSize()
					+ " instances of "
					+ factory.getProducedClass()
					+ " created.");

		passivationManager.start();
	}

	/**
	 * Returns the defaultLogStream.
	 * @return PrintStream
	 */
	public static PrintStream getDefaultLogStream() {
		return defaultLogStream;
	}

	/**
	 * Sets the defaultLogStream.
	 * @param defaultLogStream The defaultLogStream to set
	 */
	public static void setDefaultLogStream(PrintStream defaultLogStream) {
		ObjectPool.defaultLogStream = defaultLogStream;
	}

	/**
	 * Returns the logStream.
	 * @return PrintStream
	 */
	public PrintStream getLogStream() {
		return logStream;
	}

	/**
	 * Sets the logStream.
	 * @param logStream The logStream to set
	 */
	public void setLogStream(PrintStream logStream) {
		this.logStream = logStream;
		PooledObjectFactory.scl.setLogStream(logStream);
	}

	/**
	 * Returns the configuration.
	 * @return Configuration
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	Object[] createObjectsArray() {
		Object[] pool2 = new Object[pool.length];
		for (int i = 0; i < pool.length; i++) {
			pool2[i] = pool[i].getObject();
		}
		return pool2;
	}

	synchronized boolean isAcquired(PooledObject obj) {
		//assert(used.contains(obj) || free.contains(obj));
		return used.contains(obj);
	}

	/**
	 * A convenience method which already casts the result of {@link org.sadun.util.pool.ObjectPool#acquire() acquire()}
	 * to the {@link PooledObject PooledObject} type.
	 * @return one object in the pool, obtained invoking {@link org.sadun.util.pool.ObjectPool#acquire() acquire()}
	 */
	public PooledObject acquireInstance() {
		return (PooledObject) acquire();
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	public void dispose() {
		passivationManager.shutdown();
	}

	/**
	 * Returns the passivationManager.
	 * @return BasePassivationManager
	 */
	public BasePassivationManager getPassivationManager() {
		return passivationManager;
	}

	/**
	 * Method setPool. Set the given pool as the pool refrenced by each BasePooledObject.
	 * @param objects
	 * @param objectPool
	 */
	private void setPool(Object[] objects, ObjectPool objectPool) {
		for (int i = 0; i < objects.length; i++) {
			((BasePooledObject) objects[i]).setObjectPool(objectPool);
		}
	}

	/**
	 * Test method
	 */
	public static void main(String[] args) throws Exception {
		//ObjectPool.setDefaultLogStream(System.out);
		DefaultPassivationManager.setDefaultPassivationThreshold(1000L);
		ObjectPool pool =
			new ObjectPool(
				"Test pool",
				new Configuration(new File(File.separator + "temp"), 10),
				org.sadun.util.pool2.test.PassivableObject.class);
	//	Thread.sleep(10000);
		// Pick an object
		ObjectWithID obj = (ObjectWithID) pool.acquire();
		System.out.println("Object ID: "+obj.getId());
		pool.release(obj);
	//	Thread.sleep(5000);
		pool.dispose();
	}

}
