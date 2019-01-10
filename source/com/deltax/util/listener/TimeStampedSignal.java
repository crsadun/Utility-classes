package com.deltax.util.listener;

import java.util.Date;

/**
 * A signal with a time stamp.
 * 
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano
 *         Sadun</a>
 * @version 1.0
 */
public class TimeStampedSignal extends Signal {

	public TimeStampedSignal(Object obj) {
		super(obj);
		time = (new Date()).getTime();
	}

	public long getTime() {
		return time;
	}

	protected long time;
}