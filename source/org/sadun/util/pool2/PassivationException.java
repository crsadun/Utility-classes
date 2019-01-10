package org.sadun.util.pool2;

/**
 * An exception raised when an object cannot be passivated for some reason.
 * 
 * @author Cristiano Sadun
 */
public class PassivationException extends RuntimeException {
	
	public PassivationException(String msg) {
		super(msg);
	}
	
	public PassivationException(String msg, Exception e) {
		super(msg,e);
	}

}
