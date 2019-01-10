package com.deltax.util.listener;

import java.io.*;

/**
 * A listener which simply outputs signals on a given stream or writer.
 *
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano Sadun</a>
 * @version 1.0
 */
public class SimpleListener implements Listener {
	
	private PrintWriter out;
	private boolean autoFlushing=true;

	/**
	 * Create a listener which outputs to System.out
	 */
	public SimpleListener() {
		this(((OutputStream) (System.out)));
	}

	/**
	 * Create a listener which outputs to the given stream
	 * 
	 * @param outputstream the stream to output to
	 */
	public SimpleListener(OutputStream outputstream) {
		out = new PrintWriter(outputstream);
	}

	/**
	 * Create a listener which outputs to the given writer 
	 * @param writer the writer to output to
	 */
	public SimpleListener(Writer writer) {
		out = new PrintWriter(writer);
	}

	/**
	 * This implementation discriminates among {@link NamedSignal}s, {@link MsgSignal}s
	 * and {@link ExceptionSignal}s.
	 * 
	 * @param signal the received signal
	 */
	public void receive(Signal signal) {
		if (signal instanceof NamedSignal)
			out.println(((NamedSignal) signal).getName() + " received");
		else if (signal instanceof MsgSignal)
			out.println(((MsgSignal) signal).getMsg());
		else if (signal instanceof ExceptionSignal) {
			out.println("Exception signal raised by " + signal.getSource());
			((ExceptionSignal) signal).getException().printStackTrace(out);
		} else {
			out.println("Signal by " + signal.getSource());
		}
		if (autoFlushing) flush();
	}
	
	/**
	 * Flushes the stream.
	 */
	public void flush() {
		out.flush();
	}
	
	/**
	 * Closes the stream.
	 */
	public void close() {
		out.close();
	}
	
	/**
	 * Return the autoFlushing mode
	 * @return Return the autoFlushing mode
	 */
	public boolean isAutoFlushing() {
		return autoFlushing;
	}

	/**
	 * Set the autoFlushing mode (defaults to true)
	 * @param autoFlushing the autoFlushing mode
	 */
	public void setAutoFlushing(boolean autoFlushing) {
		this.autoFlushing = autoFlushing;
	}

}