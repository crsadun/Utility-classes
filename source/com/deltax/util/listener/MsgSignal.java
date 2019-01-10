package com.deltax.util.listener;

/**
 * A signal class carrying a message.
 * 
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano
 *         Sadun</a>
 * @version 1.0
 */
public class MsgSignal extends Signal {

	private String msg;

	/**
	 * Create a signal with the given message.
	 * 
	 * @param msg the message
	 * @param source the stated signal source.
	 */
	public MsgSignal(String msg, Object source) {
		super(source);
		this.msg = msg;
	}

	/**
	 * Return the signal message.
	 * @return the signal message.
	 */
	public String getMsg() {
		return msg;
	}
	
	public String toString() {
	    return getMsg();
	}

}