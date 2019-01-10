package com.deltax.util.listener;

import java.io.IOException;
import java.io.Writer;

/**
 * A listener which distributes signals to a writer and to another listener
 *
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano Sadun</a>
 * @version 1.0
 */
public class BridgeListener extends SimpleListener {

	SimpleListener sl;
	Listener l;

	public BridgeListener(Writer writer, Listener listener)
		throws IOException {
		l = listener;
		sl = new SimpleListener(writer);
	}

	public void receive(Signal signal) {
		sl.receive(signal);
		l.receive(signal);
	}

}