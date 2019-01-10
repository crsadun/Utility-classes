/*
 * Created on Aug 31, 2004
 */
package com.deltax.util.listener;

/**
 * A signal carryin an object argument.
 * 
 * @version 1.0
 * @author Cristiano Sadun
 */
public class ObjectSignal extends Signal {

	protected Object argument;
	
	/**
	 * @param source
	 */
	public ObjectSignal(Object source, Object argument) {
		super(source);
		this.argument=argument;
	}
	
	public Object getArgument() { return argument; }

}
