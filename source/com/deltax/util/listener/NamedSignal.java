package com.deltax.util.listener;

/**
 * A signal with a name.
 * 
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano
 *         Sadun</a>
 * @version 1.0
 */
public class NamedSignal extends Signal {

	private String name;

	/**
	 * Create a signal with the given name.
	 * 
	 * @param name the name of the signal
	 * @param source the stated source of the signal
	 */
	public NamedSignal(String name, Object source) {
		super(source);
		this.name = name;
	}

	/**
	 * Return the name of the signal
	 * @return the name of the signal
	 */
	public String getName() {
		return name;
	}

}