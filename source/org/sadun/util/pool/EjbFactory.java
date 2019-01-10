package org.sadun.util.pool;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.sadun.util.Setup;
import org.sadun.util.pool.ObjectPool.ObjectPoolException;

  /**
   * An {@link ExtendedObjectPool.Factory ExtendedObjectPool.Factory} implementation which uses reflection
   * to create instances of enterprise java beans, looking up the home interface and invoking a
   * proper <tt>create()</tt> method.
   * <p>
   * The JNDI name and the home interface class of the EJB to pool must be provided at construction.
   * <p>
   * After creation, objects can be optionally setup by an user-provided class implementing
   * the {@link org.sadun.util.Setup Setup} interface.
   *
   * @author Cristiano Sadun
   * @version 1.0
   */
  public class EjbFactory extends ObjectPool.BaseFactory {

    private Method createMethod;
    private String jndiName;
    private EJBHome home;

    /**
     * Create a factory which makes use of a <tt>create()</tt> method on the home interface of
     * an EJB with the given JNDI name and expecting the given parameters; after creation,
     * the given {@link org.sadun.util.Setup Setup} object will be used for post initialization.
     * @param jndiName the JNDI name of the EJB's home interface
     * @param remoteInterface the remote interface of the EJB
     * @param params the types of the parameters expected by the EJB's create() method, or <b>null</b>
     * @param ps the {@link org.sadun.util.Setup Setup} object to be used for post-construction setup, or <b>null</b>
     */
    public EjbFactory(String jndiName, Class remoteInterface, Object []params, Setup ps) {
        super(remoteInterface, params, ps);
        this.jndiName=jndiName;
    }

    /*
     * Create a factory which makes use of a <tt>create()</tt> method on the home interface of
     * an EJB with the given JNDI name and expecting the given parameters
     * @param jndiName the JNDI name of the EJB's home interface
     * @param remoteInterface the remote interface of the EJB
     * @param params the types of the parameters expected by the EJB's create() method
     */
    public EjbFactory(String jndiName, Class remoteInterface, Object []params) {
        this(jndiName, remoteInterface, params, null);
    }

    /*
     * Create a factory which makes use of a <tt>create()</tt> method on the home interface of
     * an EJB with the given JNDI name with no parameters
     * @param jndiName the JNDI name of the EJB's home interface
     * @param remoteInterface the remote interface of the EJB
     */
    public EjbFactory(String jndiName, Class remoteInterface) {
        this(jndiName, remoteInterface, null, null);
    }

    public Object create() throws ObjectPool.ObjectPoolException {
        try {
            if (home==null) {
                Context ic = new InitialContext();
                Object obj = ic.lookup(jndiName);
                home = (EJBHome)PortableRemoteObject.narrow(obj, EJBHome.class);
                home = (EJBHome)PortableRemoteObject.narrow(obj, home.getEJBMetaData().getHomeInterfaceClass());
            }
            Object newObject = createMethod.invoke(home, params);
            if (ps != null) ps.setup(newObject);
            return newObject;
        } catch(Exception e) {
            throw new ObjectPool.ObjectPoolException(e);
        }
    }
    
	/**
	 * This implementation does not do anything.
	 */
	public void destroy(Object obj) {
		if (obj instanceof EJBObject)
			try {
				((EJBObject)obj).remove();
			} catch (RemoteException e) {
				throw new ObjectPoolException(e);
			} catch (RemoveException e) {
				throw new ObjectPoolException(e);
			}
	
	}

    /**
     * Return the JNDI name of the Enterprise Java Bean produced by this factory
     * @return the JNDI name of the Enterprise Java Bean produced by this factory
     */
    public String getJNDIName() { return jndiName; }

    /**
     * Get the home interface used by this factory to produce Enterprise Java Beans
     * @return the home interface used by this factory to produce Enterprise Java Beans
     */
    public EJBHome getEJBHome() { return home; }

  }
