/*
 * Created on Nov 25, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.sadun.util.watchdog.mbean;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.sadun.util.watchdog.WatchDog;
import org.sadun.util.watchdog.WatchDogException;
import org.sadun.util.watchdog.WatchDog.Listener;

import com.sun.tools.javac.v8.comp.Check;

/**
 * <font color=red><b>NON COMPLETE YET</b></font>.
 * 
 * An implementation of {@link ManagedWatchDogMBean}.
 * <p>
 * Unlike {@link org.sadun.util.watchdog.WatchDog}, this implementation is
 * concrete and can be directly instantiated. The specific check action to
 * perform can be specified by declaring the
 * {@link ManagedWatchDogMBean.CheckAction}class name via the
 * {@link #setCheckActionClassName(String)}method (or the equivalent <tt>CheckActionClassName</tt>
 * JMX attribute).
 * <p>
 * However, it is still possible to extend the class and construct a subclass
 * which
 * <ul>
 * <li>provides a concrete {@link org.sadun.util.watchdog.WatchDog}class
 * (typically, an already existing watchdog)
 * <li>provides a {@link WatchDog.CheckAction}object which specifies the
 * checking semantics.
 * </ul>
 * <p>
 * Therfore, to create a JMX-instrumented watchdog, a developer can:
 * <ul>
 * <li>define the checking semantics, by
 * <p>
 * <ul>
 * <li>creating <b>a concrete non-instrumented Watchdog class</b> (extending
 * {@link org.sadun.util.watchdog.WatchDog}and overriding
 * {@link org.sadun.util.watchdog.WatchDog#doCheck(Object)})
 * <p>
 * <li>or creating an <b>implementation of
 * {@link ManagedWatchDogMBean.CheckAction}</b>. For example,
 * <p>
 * 
 * <pre>
 *  class MyCheck implements ManagedWatchDogMBean.CheckAction { public Throwable doCheck(Object obj) throws WatchDogException { System.out.println("Fake check"); return null; } }
 * </pre>
 * 
 * 
 * 
 * <p>
 * </ul>
 * <p>
 * <li>then, either
 * <p>
 * <ul>
 * <li><b>extend the ManagedWatchdog class</b>, defining a default
 * constructor which invokes the appropriate <tt>protected</tt> constructor,
 * for example:
 * <p>
 * 
 * <pre>
 *  class MyManagedWatchDog extends ManagedWatchDog { public MyManagedWatchDog() { super("MyManagedWatchDog", new MyCheck()); } }
 * </pre>
 * 
 * 
 * <li>or simply configure the <tt>CheckActionClassName</tt> attribute in
 * the MBean server configuration, for example:
 * 
 * <pre>
 *  &lt;mbean code="org.sadun.util.watchdog.mbean.ManagedWatchDog"&gt; &lt;attribute name="CheckActionClassName"&gt;MyCheck&lt;attribute&gt; &lt;mbean&gt;
 * </pre>
 * 
 * 
 * </ul>
 * </ul>
 * <p>
 * 
 * @author Cristiano Sadun
 * @version 1.0
 */
public class ManagedWatchDog
	implements ManagedWatchDogMBean, MBeanRegistration {

	/**
	 * A watchdog implementation which implements doCheck so that an external
	 * CheckAction object concretely defines what to do. It's necessary since
	 * the original WatchDog was (sic) abstract.
	 * <p>
	 * It also implements methods to set/get parameters for the action, using
	 * bean introspection.
	 * 
	 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">
	 *         Cristiano Sadun</a>
	 * @version 1.0
	 */
	private static class DelegatedWatchDog extends WatchDog {

		private CheckAction action;
		private PropertyDescriptor[] descriptors;

		DelegatedWatchDog(CheckAction action) throws IntrospectionException {
			assert action != null;
			this.action = action;
			this.descriptors =
				Introspector
					.getBeanInfo(action.getClass())
					.getPropertyDescriptors();
		}

		public Throwable doCheck(Object obj) throws WatchDogException {
			return action.doCheck(obj);
		}
		
		public void run() {
			String s;
			if ((s=action.isReady(false))!=null) throw new IllegalStateException("Action is not ready: "+s);
			super.run();
		}

		public Object getCheckActionParameter(String name)
			throws
				IllegalStateException,
				IllegalArgumentException,
				IllegalAccessException,
				InvocationTargetException {
			if (action == null)
				throw new IllegalStateException("No action defined");
			PropertyDescriptor descriptor = findDescriptor(name);
			Method readerMethod = descriptor.getWriteMethod();
			if (readerMethod == null)
				throw new IllegalArgumentException(
					"The property \""
						+ name
						+ "\" is not readable in "
						+ action.getClass());
			return readerMethod.invoke(action, new Object[0]);

		}

		private PropertyDescriptor findDescriptor(String name) {
			for (int i = 0; i < descriptors.length; i++) {
				if (descriptors[i].getName().equals(name))
					return descriptors[i];
			}
			throw new IllegalArgumentException(
				"No property \"" + name + "\" in " + action.getClass());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#setCheckActionParameter(java.lang.String,
		 *      java.lang.Object)
		 */
		public void setCheckActionParameter(String name, Object value)
			throws
				IllegalStateException,
				IllegalArgumentException,
				IllegalAccessException,
				InvocationTargetException {
			if (action == null)
				throw new IllegalStateException("No action defined");
			PropertyDescriptor descriptor = findDescriptor(name);
			Method writerMethod = descriptor.getWriteMethod();
			if (writerMethod == null)
				throw new IllegalArgumentException(
					"The property \""
						+ name
						+ "\" is read-only in "
						+ action.getClass());
			writerMethod.invoke(action, new Object[] { value });
		}

	}

	/**
	 * The embedded watchdog object.
	 */
	protected WatchDog watchDog;

	/**
	 * The objectname with which this watchdog is registered.
	 */
	protected ObjectName mbeanName;

	private String checkActionClassName;

	/**
	 * Create a WatchDog MBean which wraps an existing concrete
	 * {@link org.sadun.util.watchdog.WatchDog}class.
	 * 
	 * @param watchDog
	 *            the concrete {@link org.sadun.util.watchdog.WatchDog}
	 *            instance to instrument.
	 * 
	 * public WatchDog(org.sadun.util.watchdog.WatchDog watchDog) {
	 * this.watchDog=watchDog; } * Create a WatchDog MBean with the given check
	 * period, in milliseconds, and the given associated action.
	 * <p>
	 * The watchdog is created in synchronus mode - i.e. a blocking listener
	 * will block the WatchDog thread itself.
	 * 
	 * @param name
	 *            the name of the WatchDog thread
	 * @param checkPeriodTime
	 *            the amount of time the watchdog waits between each check.
	 * @param action
	 *            the {@link CheckAction}to perform
	 */
	public ManagedWatchDog(
		String name,
		long checkPeriodTime,
		final CheckAction action) {
		this.checkActionClassName = action.getClass().getName();
		try {
			watchDog = new DelegatedWatchDog(action);
		} catch (IntrospectionException e) {
			throw new RuntimeException(
				"Could not introspect the action class "
					+ action.getClass().getName(),
				e);
		}
		watchDog.setName(name);
		watchDog.setCheckPeriodTime(checkPeriodTime);
	}

	/**
	 * Create a WatchDog MBean with the given check period, in milliseconds,
	 * and the given associated action.
	 * <p>
	 * The watchdog can be created in either synchronous or asynchronous mode.
	 * <p>
	 * In the former case, listeners are invoked in the WatchDog thread - and
	 * if they block, the WatchDog thread will block; in the latter case,
	 * listeners execution is decoupled from the WatchDog thread.
	 * <p>
	 * Asynchronous mode requires is heavier and requires more resources.
	 * 
	 * @param name
	 *            the name of the WatchDog thread
	 * @param checkPeriodTime
	 *            the amount of time the watchdog waits between each check.
	 * @param synchronous
	 *            if <b>true</b>, synchronous mode is selected, else
	 *            asynchronous.
	 * @param action
	 *            the {@link CheckAction}to perform
	 */
	public ManagedWatchDog(
		String name,
		long checkPeriodTime,
		boolean synchronous,
		final CheckAction action) {
		this.checkActionClassName = action.getClass().getName();
		try {
			watchDog = new DelegatedWatchDog(action);
		} catch (IntrospectionException e) {
			throw new RuntimeException(
				"Could not introspect the action class "
					+ action.getClass().getName(),
				e);
		}
		watchDog.setName(name);
		watchDog.setCheckPeriodTime(checkPeriodTime);
		watchDog.setSynchronous(synchronous);

	}
	/**
	 * Create a WatchDog MBean with the default check period time.
	 * 
	 * @param name
	 *            the name of the WatchDog thread asynchronous.
	 * @param action
	 *            the {@link CheckAction}to perform
	 */
	public ManagedWatchDog(String name, final CheckAction action) {
		this.checkActionClassName = action.getClass().getName();
		try {
			watchDog = new DelegatedWatchDog(action);
		} catch (IntrospectionException e) {
			throw new RuntimeException(
				"Could not introspect the action class "
					+ action.getClass().getName(),
				e);
		}
		watchDog.setName(name);
	}

	/**
	 * Constructor which leaves all definitions to subsequent set calls.
	 *  
	 */
	public ManagedWatchDog() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.management.MBeanRegistration#postDeregister()
	 */
	public void postDeregister() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
	 */
	public void postRegister(Boolean registrationDone) {
		if (registrationDone.booleanValue()) {
			if (watchDog == null)
				throw new IllegalStateException(
					"Please specify the CheckActionClassName attribute (the name of a class implementing the "
						+ Check.class.getName()
						+ " interface");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.management.MBeanRegistration#preDeregister()
	 */
	public void preDeregister() throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer,
	 *      javax.management.ObjectName)
	 */
	public ObjectName preRegister(MBeanServer server, ObjectName name)
		throws Exception {
		this.mbeanName = name;
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#addListener(org.sadun.util.watchdog.ManagedWatchDog.Listener)
	 */
	public void addListener(Listener l) {
		watchDog.addListener(l);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#getCheckPeriodTime()
	 */
	public long getCheckPeriodTime() {
		return watchDog.getCheckPeriodTime();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#getName()
	 */
	public String getName() {
		return watchDog.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#isAlive()
	 */
	public boolean isAlive() {
		return watchDog.isAlive();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#isShuttingDown()
	 */
	public boolean isShuttingDown() {
		return watchDog.isShuttingDown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#isStartBySleeping()
	 */
	public boolean isStartBySleeping() {
		return watchDog.isStartBySleeping();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#isSynchronous()
	 */
	public boolean isSynchronous() {
		return watchDog.isSynchronous();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#removeListener(org.sadun.util.watchdog.ManagedWatchDog.Listener)
	 */
	public void removeListener(Listener l) {
		watchDog.removeListener(l);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#setCheckPeriodTime(long)
	 */
	public void setCheckPeriodTime(long checkPeriodTime) {
		watchDog.setCheckPeriodTime(checkPeriodTime);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#setName(java.lang.String)
	 */
	public void setName(String name) {
		watchDog.setName(name);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#setStartBySleeping(boolean)
	 */
	public void setStartBySleeping(boolean startBySleeping) {
		watchDog.setStartBySleeping(startBySleeping);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#shutdown()
	 */
	public void shutdown() {
		watchDog.shutdown();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#startup()
	 */
	public void startup() {
		watchDog.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#getCheckActionClassName()
	 */
	public String getCheckActionClassName() {
		return checkActionClassName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.mbean.ManagedWatchDogMBean#setCheckActionClassName(java.lang.String)
	 */
	public void setCheckActionClassName(String name) throws MBeanException {
		try {
			/*if (checkActionClassName != null)
				throw new MBeanException(
					null,
					"The CheckActionClassName can be set only once");
					*/
			if (isAlive()) throw new MBeanException(new IllegalStateException("The watchdog is already running"));
			
			if (watchDog!=null) watchDog.dispose();
			Class cls = Class.forName(name);
			Object obj = cls.newInstance();
			if (!(obj instanceof CheckAction))
				throw new MBeanException(
					null,
					"The class specified by the CheckActioClassName attribute must implement the "
						+ CheckAction.class.getName()
						+ " interface");
			this.checkActionClassName = name;
			final CheckAction action = (CheckAction) obj;
			try {
				this.watchDog = new DelegatedWatchDog(action);
			} catch (IntrospectionException e) {
				throw new MBeanException(
					e,
					"Could not introspect the action class "
						+ action.getClass().getName());
			}
		} catch (ClassNotFoundException e) {
			throw new MBeanException(e, "Cannot find the class " + name);
		} catch (InstantiationException e) {
			throw new MBeanException(e, "Cannot instntiate the class " + name);
		} catch (IllegalAccessException e) {
			throw new MBeanException(e, "Cannot access the class " + name);
		}

	}

	public Object getCheckActionParameter(String name) throws MBeanException {
		try {
			return ((DelegatedWatchDog)watchDog).getCheckActionParameter(name);
		} catch (IllegalStateException e) {
			throw new MBeanException(e);
		} catch (IllegalArgumentException e) {
			throw new MBeanException(e);
		} catch (IllegalAccessException e) {
			throw new MBeanException(e);
		} catch (InvocationTargetException e) {
			throw new MBeanException(e);
		}
	}
	
	public void setCheckActionParameter(String name, Object value)
		throws MBeanException { 
		try {
			((DelegatedWatchDog)watchDog).setCheckActionParameter(name, value);
		} catch (IllegalStateException e) {
			throw new MBeanException(e);
		} catch (IllegalArgumentException e) {
			throw new MBeanException(e);
		} catch (IllegalAccessException e) {
			throw new MBeanException(e);
		} catch (InvocationTargetException e) {
			throw new MBeanException(e);
		}
	}

}
