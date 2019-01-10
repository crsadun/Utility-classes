/*
 * Created on Aug 31, 2004
 */
package com.deltax.util.listener;

import java.util.Observable;

/**
 * A signal produced by an Observable object. Use {@link #getObservable()} to retrieve the observed object
 * and {@link com.deltax.util.listener.ObjectSignal#getArgument()} to retrieve the argument.
 * 
 * @author Cristiano Sadun
 * @version 1.0
 **/
public class ObservableSignal extends ObjectSignal {

	/**
	 * @param source
	 * @param arg
	 */
	public ObservableSignal(Observable source, Object arg) {
		super(source, arg);
	}
	
	/**
	 * Return the observable object which generated the signal.
	 * @return the observable object which generated the signal.
	 */
	public Observable getObservable() { return (Observable)getSource(); }

}
