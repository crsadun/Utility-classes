package com.deltax.util.listener;

/**
 * An anonymous signal class. This class doesn't carry any other information but the 
 * source of the signal, and can say wether or not the source supports {@link ExceptionSignal 
 * exception signals}.
 *
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano Sadun</a>
 * @version 1.0
 */
public class Signal {
	
	private Object source;

	/**
	 * Build a signal with the given source.
	 * 
	 * @param source the stated source of the signal.
	 */
	public Signal(Object source) {
		if (source == null) {
			throw new RuntimeException("Signal source cannot be null");
		} else {
			this.source = source;
			return;
		}
	}

	/**
	 * Return the stated source of the signal.
	 * @return the stated source of the signal.
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * Return <b>true</b> if the source supports {@link ExceptionSignal exception signals}.
	 * @return <b>true</b> if the source supports {@link ExceptionSignal exception signals}.
	 */
	public boolean sourceSupportsExceptionSignals() {
		if (getSource() instanceof SignalSource)
			return ((SignalSource) getSource()).supportsExceptionSignals();
		else
			return false;
	}
}