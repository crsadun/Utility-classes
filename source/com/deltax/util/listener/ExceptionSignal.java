package com.deltax.util.listener;

/**
 * A signal carrying an exception.
 * 
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano
 *         Sadun</a>
 * @version 1.0
 */
public class ExceptionSignal extends Signal {

	protected Exception e;

	/**
	 * Create a signal carrying the given exception from the given source
	 * 
	 * @param exception
	 * @param obj
	 */
	public ExceptionSignal(Exception exception, Object source) {
		super(source);
		e = exception;
	}

	/**
	 * Return the carried exception.
	 * 
	 * @return the carried exception.
	 */
	public Exception getException() {
		return e;
	}

}