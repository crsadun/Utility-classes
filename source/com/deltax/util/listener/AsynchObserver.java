/*
 * Created on Aug 31, 2004
 */
package com.deltax.util.listener;

import java.util.Observable;
import java.util.Observer;

/**
 * A wrapper class which allows to user a {@link Listener} in contexts where an Observer
 * is required. Note that the synchoronous/asynchronous invocation will depend then on the specific Observer class,
 * outside the boundaries of the framework.
 * <p>
 * Upon invocation of <code>update()</code> the class simply forwards the notification
 * to the {@link Listener listener}, passing an {@link ObservableSignal} containing both source (an <code>Observable</code>) and
 * argument.
 * 
 * @author Cristiano Sadun
 */
public class AsynchObserver implements Observer {
	
	/**
	 * A class that can be used to cache {@link AsynchObserver} objects.
	 * 
	 * @author Cristiano Sadun
	 */
	public static class Cache {
		
		private org.sadun.util.Cache cache;
		
		private Cache(int n) {
			this.cache=new org.sadun.util.Cache(n);
		}

		public Observer wrap(Listener listener) {
			synchronized(this) {
				Observer observer = (Observer)cache.get(new Integer(listener.hashCode()));
				if (observer==null) {
					observer=new AsynchObserver(listener);
					cache.put(new Integer(listener.hashCode()), observer);
				}
				return observer;
			}
		}
	}
	
	private Listener listener;
	
	private AsynchObserver(Listener listener) {
		this.listener=listener;
	}

	public final void update(Observable o, Object arg) {
		listener.receive(new ObservableSignal(o, arg));
	}
	
	public static Cache createCache(int n) { return new Cache(n); }
	public static Cache createCache() { return createCache(5); }
}
