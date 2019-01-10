package org.sadun.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A telnet input stream to implement a Telnet NVT (Network Virtual Terminal)
 * communication over a couple of paired input and output streams.
 * <p>
 * The class simply registers a {@link org.sadun.util.TelnetInputStream.BasicNVTListner}
 * into a {@link org.sadun.util.TelnetInputStream} to handle DO and WILL commands
 * rejecting every option.
 * <p>
 * Further handling of specific Telnet commands can be added by registering additional
 * {@link org.sadun.util.TelnetInputStream.TelnetCommandListener telnet command 
 * listeners} with {@link org.sadun.util.TelnetInputStream#registerTelnetCommandListener(TelnetCommandListener) 
 * registerCommandListener()}.
 * <p>
 * A {@link TelnetNVTChannel} can be wrapped into a {@link org.sadun.util.TelnetInputStreamConsumer} to
 * add input-consumption functionality.
 * 
 * @version 1.0
 * @author Cristiano Sadun
 */
public class TelnetNVTChannel extends TelnetInputStream {

    /**
     * Create an instance over a socket connected to a TELNET host.
     * @param s the connected socket
     * @throws IOException if a problem arises obtaining the input/output streams of
     *                      the socket
     */
    public TelnetNVTChannel(Socket s) throws IOException {
        this(s.getInputStream(), s.getOutputStream());
        
    }
    
    /**
     * Create an instance on a couple of paired streams.
     * 
     * @param is the stream on which the telnet input is read
     * @param os the stream on which the telnet output is written
     */
    public TelnetNVTChannel(InputStream is, OutputStream os) {
        super(is);
        registerTelnetCommandListener(new TelnetInputStream.BasicNVTListener(
                os));
    }

}
