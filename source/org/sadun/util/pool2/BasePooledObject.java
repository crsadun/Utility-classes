package org.sadun.util.pool2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;



/**
 * A base implementation of a pooled object employing file serialization 
 * for passivation purposes.
 * <p> 
 * This class offer support for handling the passivability and passivation state, 
 * and for object passivation/activation. 
 *
 * @version 1.0
 * @author Cristiano Sadun
 * 
 */
public abstract class BasePooledObject implements PooledObject {

	private static Random random = new Random();
	private Object original;
	private boolean passivated;
	private boolean originalAcquired;
	private Object passivationLock = new Object();
	private ObjectPool objectPool;

	private File serializedFile;

	protected BasePooledObject(ObjectPool objectPool, Object original) {
		this.objectPool = objectPool;
		this.original = original;
		this.passivated = false;
		this.originalAcquired = false;
	}

	/**
	 * @see org.sadun.util.pool2.PooledObject#_getOriginal()
	 */
	public final Object _getOriginal() throws ActivationException {
		synchronized (passivationLock) {
			if (original == null)
				_activate();
			originalAcquired = true;
		}
		return original;

	}

	/**
	 * @see org.sadun.util.pool2.PooledObject#_releaseOriginal()
	 */
	public final void _releaseOriginal() {
		originalAcquired = false;
	}

	/**
	 * Activate the object. If the object has not been passivated,
	 * this method does nothing.
	 * 
	 * @see org.sadun.util.pool2.PooledObject#_activate()
	 */
	public final void _activate() throws ActivationException {
		synchronized (passivationLock) {
			if (!passivated)
				return;
				
			if (objectPool.getLogStream()!=null) {
				objectPool.getLogStream().println("Activating passivated object from "+serializedFile);
			}	
				
			ObjectInputStream ois = null;
			try {
				try {
					ois =
						new ObjectInputStream(
							new BufferedInputStream(
								new FileInputStream(serializedFile)));
				} catch (IOException e) {
					throw new ActivationException(
						"Could not open passivated file " + serializedFile, e);
				}
				original = ois.readObject();
				serializedFile = null;
				passivated = false;

			} catch (ClassNotFoundException e) {
				throw new ActivationException(
					"Could not find the class with which the pooled object has been serialized",
					e);
			} catch (IOException e) {
				throw new ActivationException(
					"Could not read the serialized object from file "
						+ serializedFile,
					e);
			} finally {
				if (ois != null)
					try {
						ois.close();
					} catch (IOException e) {
						throw new ActivationException(
							"Could not close the serialized object file "
								+ serializedFile,
							e);
					}
			}

		}
	}

	/**
	 * This method passivates objects via serialization.
	 * <p>
	 * The original object must be Serializable.
	 * <p>
	 * The object is assigned an unique file name in a directory
	 * whose name is the class name of the original object; it is
	 * then serialized using an <tt>ObjectOutputStream</tt>.
	 * 
	 * @see org.sadun.util.pool2.PooledObject#_passivate()
	 */
	public final void _passivate() throws PassivationException {
		// Serialize the object to a file
		synchronized (passivationLock) {
			
			if (passivated) throw new PassivationException("Already passivated");
			
			if (!objectPool.getPassivationManager().canPassivate(original.getClass()))
				throw new PassivationException(
					"Object " + original.toString() + " is intrinsecally not passivable");
					
			if (originalAcquired) throw new PassivationException("Original object acquired");
					
			File objDir =
				new File(
					objectPool.getConfiguration().getStorageDirectory(),
					original.getClass().getName());
			if (!objDir.exists())
				if (!objDir.mkdirs())
					throw new PassivationException(
						"Cannot create passivation directory " + objDir);
			serializedFile = new File(objDir, mkTmpFileName(original));
			
			if (objectPool.getLogStream()!=null) {
				objectPool.getLogStream().println("Passivating "+this+" to "+serializedFile);
			}		
			
			ObjectOutputStream oos = null;
			try {
				try {
					oos =
						new ObjectOutputStream(
							new BufferedOutputStream(
								new FileOutputStream(serializedFile)));
				} catch (IOException e) {
					throw new PassivationException(
						"Could not create file for passivated object",
						e);
				}

				oos.writeObject(original);
				original = null;
				passivated = true;
			} catch (NotSerializableException e) {
				throw new PassivationException(
					"The pooled object is not Serializable",
					e);
			} catch (IOException e) {
				throw new PassivationException(
					"Could not write passivated object to file",
					e);
			} finally {
				if (oos != null)
					try {
						oos.close();
					} catch (IOException e) {
						throw new PassivationException(
							"Could not close passivated object file",
							e);
					}
			}
		}

	}

	/**
	 * Method mkTmpFileName.
	 */
	private String mkTmpFileName(Object obj) {
		return System.currentTimeMillis()
			+ "_"
			+ obj.getClass().getName()
			+ random.nextInt(1000);
	}

	/**
	 * Return the current passivable.state.
	 * @return boolean
	 */
	public boolean _isPassivableNow() {
		return _isPassivable() && passivated==false && originalAcquired==false;
	}

	/**
	 * Return the invariant passivable.state, as determined by the passivation manager 
     * associated to this pooled object.
     * <p>
     * For example, a passivation manager may require that an object be serializable
     * or implement a specific interface.
     * @see BasePassivationManager#canPassivate(java.lang.Class)
	 * @return boolean
	 */
	public boolean _isPassivable() {
		//return  Serializable.class.isAssignableFrom(objectPool.getObjectType());
		return objectPool.getPassivationManager().canPassivate(objectPool.getObjectType());
	}
	
	/**
	 * Returns the passivated state.
	 * @return boolean
	 */
	public boolean _isPassivated() {
		return passivated;
	}

	/**
	 * Return the serialized File, or <b>null</b> if the object is not
	 * passivated.
	 * @return File
	 */
	public File getSerializedFile() {
		synchronized (passivationLock) {
			return serializedFile;
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(
			"Pooled ");
		sb.append(objectPool.getObjectType().getName());
		sb.append(" object");
		if (passivated) {
			sb.append("(passivated)");
		} else {
			
			if (original!=null) {
				sb.append(" [");
				sb.append(original.toString());
				sb.append("] ");
			}
			
			if (_isPassivable())
				sb.append("(active)");
			else
				sb.append("(not passivable)");
		}
		return sb.toString();
	}
	/**
	 * Returns the original.
	 * @return Object
	 */
	protected Object getOriginal() {
		return original;
	}

	/**
	 * Returns the objectPool.
	 * @return ExtendedObjectPool
	 */
	public final ObjectPool getObjectPool() {
		return objectPool;
	}
	
	final void setObjectPool(ObjectPool pool) {
		if (objectPool!=null) throw new IllegalStateException("Object pool can be set only once");
		this.objectPool=pool;
	}
}
