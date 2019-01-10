/*
 * Created on Aug 27, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.pool;

import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.sadun.util.pool.ObjectPool.ObjectPoolException;

/**
 * 
 * @author Cristiano Sadun
 */
public class ManagedObjectPool 
	implements ManagedObjectPoolMBean, MBeanRegistration {

	private static class NullFactory implements ObjectPool.Factory {

		/* (non-Javadoc)
		 * @see org.sadun.util.pool.ObjectPool.Factory#create()
		 */
		public Object create() throws ObjectPoolException {
			return new Object();
		}

		/* (non-Javadoc)
		 * @see org.sadun.util.pool.ObjectPool.Factory#destroy(java.lang.Object)
		 */
		public void destroy(Object obj) throws ObjectPoolException {
		}

		/* (non-Javadoc)
		 * @see org.sadun.util.pool.ObjectPool.Factory#getProducedClass()
		 */
		public Class getProducedClass() {
			return Object.class;
		}

	}

	private ObjectPool pool;
	private String factoryClassName = NullFactory.class.getName();
	private int size = 1;
	private String className="";
	private boolean registered=false;

	private ObjectName objectName;

	/**
	 * @param n
	 * @param factory
	 */
	public ManagedObjectPool() {
	}
	
	/**
	 * Subclasses (which handles more specific object pools) may directly 
	 * specify default size and factory class name.
	 * 
	 * @param size
	 * @param factoryClassName
	 */
	protected ManagedObjectPool(int size, String factoryClassName) {
		this.size=size;
		this.factoryClassName=factoryClassName;
		this.className=null;
	}

	/**
	 * @param factoryClassName
	 * @return
	 */
	private static ObjectPool.Factory createFactory(String factoryClassName)
		throws MBeanException {
		try {
			Object obj =
				Class.forName(factoryClassName).newInstance();
			if (!(obj instanceof ObjectPool.Factory))
				throw new MBeanException(
					new IllegalArgumentException("FactoryClassName must be the name of a class implementing the ObjectPool.Factory interface"));
			return (ObjectPool.Factory) obj;
		} catch (InstantiationException e) {
			throw new MBeanException(e);
		} catch (IllegalAccessException e) {
			throw new MBeanException(e);
		} catch (ClassNotFoundException e) {
			throw new MBeanException(e);
		}
	}

	/**
	 * Return the name of the pool's factory class, or <b>null</b> if no factory class is employed.
	 * 
	 * @return the name of the pool's factory class, or <b>null</b> if no factory class is employed.
	 */
	public String getFactoryClassName() {
		return factoryClassName;
	}

	/**
	 * Set the name of the pool's factory class. The pool is reset, using the given factory
	 * to populate it.
	 * <p>
	 * This property cannot be changed if some client has already acquired pooled objects.
	 *  
	 * @param factoryClassName the new factory class name
	 */
	public void setFactoryClassName(String factoryClassName)
		throws MBeanException {
		checkPoolStateForReset();
		
		
		String oldFactoryClassName=this.factoryClassName;
		String oldClassName=this.className;
		int oldSize=this.size;
		
		this.factoryClassName = factoryClassName;
		this.className = pool.getFactory().getProducedClass().getName();
		
		if (registered) // Avoid unnecessary intializations at startup
			this.pool = createPool(oldFactoryClassName, oldClassName, oldSize);
	}
	
	/**
	 * Set the pool size. If the specified size is different than the current size, the pool is reset
	 * to the given size.
	 * <p>
	 * This property cannot be changed if some client has already acquired pooled objects.
	 *  
	 * @param size the new size
	 */
	public void setSize(int size) throws MBeanException {
		if (size == getSize())
			return;
		checkPoolStateForReset();
		
		String oldFactoryClassName=this.factoryClassName;
		String oldClassName=this.className;
		int oldSize=this.size;
		
		this.size=size;
		if (registered) // Avoid unnecessary intializations at startup
			this.pool = createPool(oldFactoryClassName, oldClassName, oldSize);
	}
	
	/**
	 * Set the name of the class to produce. This nullifies the factory class
	 * name, if defined.
	 * <p>
	 * This property cannot be changed if some client has already acquired pooled objects.
	 *  
	 * @param className the name of the class of the objects to pool.
	 */
	public void setClassName(String className) throws MBeanException {
		checkPoolStateForReset();
		
		String oldFactoryClassName=this.factoryClassName;
		String oldClassName=this.className;
		int oldSize=this.size;
		
		this.factoryClassName="";
		this.className = className;
		if (registered) // Avoid unnecessary intializations at startup
			this.pool = createPool(oldFactoryClassName, oldClassName, oldSize); 
	}


	private void checkPoolStateForReset() throws MBeanException {
		if (pool != null) {
			if (pool.getFreeCount() != pool.getSize())
				throw new MBeanException(
					new IllegalStateException(
						"Cannot reset the pool when there are "
							+ getUsedCount()
							+ " used objects"));
		}
	}

	/**
	 * @see org.sadun.util.pool.ManagedObjectPoolMBean#getFreeCount()
	 */
	public int getFreeCount() {
		return pool.getFreeCount();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.pool.ManagedObjectPoolMBean#getObjectType()
	 */
	public Class getObjectType() {
		return pool.getObjectType();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.pool.ManagedObjectPoolMBean#getSize()
	 */
	public int getSize() {
		return pool.getSize();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.pool.ManagedObjectPoolMBean#getUsedCount()
	 */
	public int getUsedCount() {
		return pool.getUsedCount();
	}

	/**
	 * @return
	 */
	public String getClassName() {
		return className;
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanRegistration#postDeregister()
	 */
	public void postDeregister() {
		registered=false;
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
	 */
	public void postRegister(Boolean registrationDone) {
		if (registrationDone.booleanValue()) {
			registered=true; 
			this.pool = createPool(factoryClassName, className, size);
		}
	}

	/**
	 * Create the pool with the current parameters. If the creation fails in any way,
	 * reset the parameters to their previous values
	 * @return
	 */
	private ObjectPool createPool(String oldFactoryClassName, String oldClassName, int oldSize) {
		try {
			if (factoryClassName.length()>0) {
				try {
					System.out.println("[ObjectPool] Creating pool of "+size+" objects using the factory class "+factoryClassName);
					return new ObjectPool(size, createFactory(factoryClassName));
				} catch (MBeanException e) {
					throw new RuntimeException(e.getTargetException());
				}
			} else if (className.length()>0) {
				try {
					System.out.println("[ObjectPool] Creating pool of "+size+" objects of class "+className);
					return new ObjectPool(size, className);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			} else
				throw new RuntimeException("Either FactoryClassName or ClassName attributes must be declared");
		} catch(RuntimeException e) {
			factoryClassName=oldFactoryClassName;
			className=oldClassName;
			size=oldSize;
			throw e;
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.management.MBeanRegistration#preDeregister()
	 */
	public void preDeregister() throws Exception {
		if (pool != null)
			pool.destroy(true);
	}

	/* (non-Javadoc)
	 * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
	 */
	public ObjectName preRegister(MBeanServer arg0, ObjectName arg1)
		throws Exception {
		this.objectName = arg1;
		return objectName;
	}
	
}
