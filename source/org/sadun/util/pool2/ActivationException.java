package org.sadun.util.pool2;

/**
 * An exception raised when an object cannot be activated for some reason.
 * 
 * @author Cristiano Sadun
 */
public class ActivationException extends RuntimeException {
	
	public ActivationException(String msg) {
		super(msg);
	}
	
	public ActivationException(String msg, Exception e) {
		super(msg,e);
	}

}
