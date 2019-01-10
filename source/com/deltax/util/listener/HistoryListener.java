package com.deltax.util.listener;

import java.io.FileWriter;
import java.io.IOException;


/**
 * A listener which logs an history of received signals to a file. 
 *
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano Sadun</a>
 * @version 1.0
 */
public class HistoryListener extends BridgeListener
{

	/**
	 * Create a listener attached to the given path.
	 * 
	 * @param s
	 * @param listener
	 * @throws IOException
	 */
    public HistoryListener(String path, Listener listener)
        throws IOException
    {
        super(new FileWriter(path), listener);
        filename = path;
    }

    public String getFilename()
    {
        return filename;
    }

    String filename;
}