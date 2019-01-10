package org.sadun.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This class handles typical Unix shell login sequences via telnet.
 * <p>
 * It is useful to programmatic conversations between a java client and a telnet
 * host.
 * <p>
 * Login is performed by invoking {@link #doLogin(String, String)}which
 * consumes characters from a socket stream (polling it with a
 * {@link org.sadun.util.TelnetInputStreamConsumer}) and recognizes both a
 * {@link #getLoginPromptSequence() login prompt character sequence}(defaults
 * to <tt>"ogin:"</tt>) and a
 * {@link #getLoginIncorrectSequence() login incorrect character sequence}
 * (defaults to <tt>"ogin incorrect"</tt>).
 * <p>
 * Recognition occurs by looking at the received input every n milliseconds,
 * where n is the value of
 * {@link org.sadun.util.TelnetInputStreamConsumer#BASIC_CRITERIA_CHECK_TIME}.
 * <p>
 * By default, if the {@link #getLoginIncorrectSequence() "login incorrect" sequence} is not found within a
 * {@link #getLoginIncorrectVerificationTime() login incorrect verification 
 * time} (defaulting to twice the
 * {@link org.sadun.util.TelnetInputStreamConsumer#BASIC_CRITERIA_CHECK_TIME}),
 * the login is assumed to be successful and control is relinquished to the
 * caller, which receives a {@link org.sadun.util.TelnetInputStream} ready for
 * use. Such stream can be passed to a
 * {@link org.sadun.util.TelnetInputStreamConsumer} for further
 * consumption-based processing.
 * <p>
 * If the {@link #getLoginIncorrectSequence() "login incorrect" sequence} is found,
 * a {@link UnixLoginHandler.LoginIncorrectException} is raised.
 * <p>
 * However, if the {@link #isCheckForIncorrectLogin() CheckForIncorrectLogin}
 * property is set to false (defaults to true), no check for incorrect login is
 * made and the stream is returned to the caller immediately after having send
 * the login sequence, saving the time to wait for a possible
 * {@link #getLoginIncorrectSequence() "login incorrect" sequence}.
 * <p>
 * Note that in this case the {@link UnixLoginHandler.LoginIncorrectException} is never raised.
 * <p>
 * A typical usage sequence is
 * 
 * <pre>
 * 
 *     Socket s = new Socket(telnetHost, 23);
 *     {@link UnixLoginHandler UnixLoginHandler} handler = new {@link #UnixLoginHandler(Socket) UnixLoginHandler} UnixLoginHandler(s);
 *     {@link org.sadun.util.TelnetInputStreamConsumer TelnetInputStreamConsumer} is = new {@link org.sadun.util.TelnetInputStreamConsumer#TelnetInputStreamConsumer(TelnetInputStream) TelnetInputStreamConsumer}(handler.{@link #doLogin(String, String) doLogin}(&quot;user&quot;,&quot;password&quot;));
 *  
 * </pre>
 * 
 * @version 1.0
 * @author Cristiano Sadun
 */
public class UnixLoginHandler {

    /**
     * A class to signal an incorrect login by an {@link UnixLoginHandler}.
     * 
     * @author Cristiano Sadun
     */
    public final static class LoginIncorrectException extends Exception {
    }

    private TelnetInputStreamConsumer is;
    private PrintStream os;

    private boolean sendInitialCRLF = false;
    private Socket socket;
    private long timeout = 0;

    private String loginPromptSequence = "ogin:";
    private String loginIncorrectSequence = "ogin incorrect";
    private long loginIncorrectVerificationTime = TelnetInputStreamConsumer.BASIC_CRITERIA_CHECK_TIME * 2;
    private boolean checkForIncorrectLogin = true;

    /**
     * Construct a handler on a connected socket.
     * <p>
     * The handler will own the socket and attempt to close it on
     * {@link #doLogout() logout}.
     * <p>
     * An initial CRLF sequence will be sent to the host to trigger a login
     * prompt (see {@link #setSendInitialCRLF(boolean)}.
     * 
     * @param s
     *            the socket
     * @throws IOException
     *             if an exception occurs obtaining the input/output streams
     *             from the socket
     */
    public UnixLoginHandler(Socket s) throws IOException {
        this(s.getInputStream(), s.getOutputStream(), true);
        this.socket = s;
    }

    /**
     * Construct an handler on the given input/output stream pair.
     * <p>
     * An initial CRLF sequence will be sent to the host to trigger a login
     * prompt (see {@link #setSendInitialCRLF(boolean)}.
     * 
     * @param is
     *            the inputstream
     * @param os
     *            the outputstream
     */
    public UnixLoginHandler(InputStream is, OutputStream os) {
        this(is, os, true);
    }
    
    /**
     * Construct an handler on a connected socket, optionally sending an initial
     * CRLF sequence will be sent to the host to trigger a login prompt (see
     * {@link #setSendInitialCRLF(boolean)}.
     * 
     * @param s
     *            the socket
     * @param sendInitialCRLF
     *            if true, an initial CRLF sequence will be sent to the host to
     *            trigger a login prompt
     * @throws IOException
     *             if an exception occurs obtaining the input/output streams
     *             from the socket
     */
    public UnixLoginHandler(Socket s, boolean sendInitialCRLF)
            throws IOException {
        this(s.getInputStream(), s.getOutputStream());
    }

    /**
     * Construct an handler on the given input/output stream pair, optionally
     * sending an initial CRLF sequence will be sent to the host to trigger a
     * login prompt (see {@link #setSendInitialCRLF(boolean)}.
     * 
     * @param is
     *            the inputstream
     * @param os
     *            the outputstream
     * @param sendInitialCRLF
     *            if true, an initial CRLF sequence will be sent to the host to
     *            trigger a login prompt
     * @throws IOException
     *             if an exception occurs obtaining the input/output streams
     *             from the socket
     */
    public UnixLoginHandler(InputStream is, OutputStream os,
            boolean sendInitialCRLF) {
        this.is = new TelnetInputStreamConsumer(new TelnetNVTChannel(
                new BufferedInputStream(is), os));
        this.os = new PrintStream(os, true);
        this.sendInitialCRLF = sendInitialCRLF;
    }

    /**
     * Construct an handler towards the given host/port. A socket is created and
     * connected to the host/port.
     * 
     * @param host
     *            the telnet host name
     * @param port
     *            the port to connect to
     * @throws IOException
     *             if an exception occurs obtaining the input/output streams
     *             from the socket
     * @throws UnknownHostException
     *             if the host name cannot be resolved
     */
    public UnixLoginHandler(String host, int port) throws UnknownHostException,
            IOException {
        this(new Socket(host, port));
    }

    /**
     * Register a {@link TelnetInputStream.TelnetCommandListener}to handle
     * telnet commands.
     * 
     * @param tlh
     *            the {@link TelnetInputStream.TelnetCommandListener}to
     *            register.
     */
    public void registerTelnetCommandListener(
            TelnetInputStream.TelnetCommandListener tlh) {
        this.is.registerTelnetCommandListener(tlh);
    }

    /**
     * Unregister a {@link TelnetInputStream.TelnetCommandListener}to handle
     * telnet commands.
     * 
     * @param tlh
     *            the {@link TelnetInputStream.TelnetCommandListener}to
     *            unregister.
     */
    public void removeTelnetCommandListener(
            TelnetInputStream.TelnetCommandListener tlh) {
        this.is.removeTelnetCommandListener(tlh);
    }

    /**
     * Do the login. An initial CRLF is sent if {@link #isSendInitialCRLF()}is
     * true (as set at construction or by using
     * {@link #setSendInitialCRLF(boolean)}.
     * 
     * @param user
     * @param pwd
     * @throws IOException
     * @throws OperationTimedoutException
     */
    public TelnetInputStreamConsumer doLogin(String user, String pwd)
            throws IOException, OperationTimedoutException,
            LoginIncorrectException {
        String line;
        // Send CRLF if required
        if (sendInitialCRLF)
            sendCRLF();

        // Wait for login prompt
        //is.consumeInput(loginPromptRegExpPattern, false);
        is.setConsumptionOperationsTimeout(timeout);
        is.consumeInputUntilStringFound(loginPromptSequence);

        // Supply username
        os.print(user + "\r\n");
        os.flush();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new IOException("Login sequence interrupted");
        }
        // Supply password
        os.print(pwd + "\r\n");
        os.flush();
        if (checkForIncorrectLogin) {
            // Wait for input
            TelnetInputStreamConsumer.CompositeCriterium criterium = new TelnetInputStreamConsumer.OrCriterium(
                    new TelnetInputStreamConsumer.ContainsStringCriterium(
                            loginIncorrectSequence),
                    new TelnetInputStreamConsumer.NoMoreInputAvailableCriterium(
                            true, loginIncorrectVerificationTime));
            is.consumeByCriteria(criterium);
            if (criterium.getHoldingCriterium() instanceof TelnetInputStreamConsumer.ContainsStringCriterium)
                throw new LoginIncorrectException();
        }
        return is;
    }

    /**
     * Close input and output stream. If the originating socket is owned by this
     * handler, it is closed as well.
     * <P>
     * The logout sequence used is CRLF <tt>exit</tt> CRLF.
     */
    public void doLogout() {
        doLogout("\r\nexit", true);

    }

    /**
     * Send a logout sequence and {@link #disconnect()}.
     * <P>
     * The given logout sequence used to attempt a graceful logout.
     * 
     * @param logoutSequence
     *            the logout sequence to be sent to the host
     */
    public void doLogout(String logoutSequence) {
        doLogout(logoutSequence, false);
    }

    /**
     * Send a logout sequence and {@link #disconnect()}.
     * <P>
     * The given logout sequence used to attempt a graceful logout.
     * 
     * @param logoutSequence
     *            the logout sequence to be sent to the host
     * @param appendCR
     *            if true, a CRLF is appended to the logout sequence.
     */
    public void doLogout(String logoutSequence, boolean appendCR) {
        if (os != null) {
            os.print(logoutSequence);
            if (appendCR)
                os.print("\r\n");
            os.flush();
        }
        disconnect();
    }

    /**
     * Disconnect the stream, if the stream is connected. No specific logout is
     * performed, i.e. no logout sequence is sent to the connected host.
     * <p>
     * The input/output streams are closed. If the stream owns the socket (one
     * of the {@link #UnixLoginHandler(Socket), 
     * {@link #UnixLoginHandler(Socket, boolean)}or
     * {@link #UnixLoginHandler(String, int)}constructors has been used to
     * build the object), an attempt is made to close the socket object as well.
     */
    public void disconnect() {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                is = null;
            }
        }

        if (os != null) {
            os.close();
        }
        os = null;
        if (socket != null)
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Return the input stream used by this handler.
     * <p>
     * This method is <i>not </i> threadsafe, and it should be called only after
     * {@link #doLogin(String, String)}.
     * 
     * @return the input stream used by this handler.
     */
    public TelnetInputStreamConsumer getInputStream() {
        return is;
    }

    /**
     * Return the output stream used by this handler.
     * <p>
     * This method is <i>not </i> threadsafe, and it should be called only after
     * {@link #doLogin(String, String)}.
     * 
     * @return the output stream used by this handler.
     */
    public PrintStream getOutputStream() {
        return os;
    }

    private void sendCRLF() throws IOException {
        os.write('\r');
        os.write('\n');
        os.flush();
    }

    /**
     * Return true if an initial CRLF will be sent by
     * {@link #doLogin(String, String)}before initiating the login sequence
     * proper.
     * 
     * @return true if an initial CRLF will be sent by
     *         {@link #doLogin(String, String)}before initiating the login
     *         sequence proper.
     */
    public boolean isSendInitialCRLF() {
        return sendInitialCRLF;
    }

    /**
     * Set whether or not an initial CRLF will be sent by
     * {@link #doLogin(String, String)}before initiating the login sequence
     * proper.
     * 
     * @param sendInitialCRLF
     *            if true, an initial CRLF will be sent by
     *            {@link #doLogin(String, String)}before initiating the login
     *            sequence proper.
     */
    public void setSendInitialCRLF(boolean sendInitialCRLF) {
        this.sendInitialCRLF = sendInitialCRLF;
    }

    /**
     * Get the timeout when waiting for prompt/results on login operation. Zero
     * means indefinite wait.
     * 
     * @return the timeout when waiting for prompt/results on login operation.
     *         Zero means indefinite wait.
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Set the timeout when waiting for prompt/results on login operation. Zero
     * means indefinite wait.
     * 
     * @param timeout
     *            the timeout when waiting for prompt/results on login
     *            operation. Zero means indefinite wait.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Return the character sequence currently used to recognize incorrect logins. This is used only if the
     * {@link #isCheckForIncorrectLogin() CheckForIncorrectLogin} property is true.
     * @return the character sequence currently used to recognize incorrect logins.
     */
    public String getLoginIncorrectSequence() {
        return loginIncorrectSequence;
    }

    /**
     * Setthe character sequence to use to recognize incorrect logins. This is used only if the
     * {@link #isCheckForIncorrectLogin() CheckForIncorrectLogin} property is true.
     * @param loginIncorrectSequence the character sequence currently to use to recognize incorrect logins.
     */
    public void setLoginIncorrectSequence(String loginIncorrectSequence) {
        this.loginIncorrectSequence = loginIncorrectSequence;
    }

    /**
     * Return the character sequence currently used to recognize the login prompt.
     * 
     * @return the character sequence currently used to recognize the login prompt.
     */
    public String getLoginPromptSequence() {
        return loginPromptSequence;
    }

    /**
     * Set the character sequence to use to recognize the login prompt.
     * 
     * @param loginPromptSequence the character sequence currently to use to recognize the login prompt.
     */
    public void setLoginPromptSequence(String loginPromptSequence) {
        this.loginPromptSequence = loginPromptSequence;
    }

    /**
     * Return the time the handler will wait for a {@link #getLoginIncorrectSequence() "login incorrect" character sequence}
     * before assuming the login has been successful. This is used only if the
     * {@link #isCheckForIncorrectLogin() CheckForIncorrectLogin} property is true.
     * 
     * @return the time the handler will wait for a {@link #getLoginIncorrectSequence() "login incorrect" character sequence}
     * before assuming the login has been successful. 
     */
    public long getLoginIncorrectVerificationTime() {
        return loginIncorrectVerificationTime;
    }

    /**
     * Set the time the handler will wait for a {@link #getLoginIncorrectSequence() "login incorrect" character sequence}
     * before assuming the login has been successful. This is used only if the
     * {@link #isCheckForIncorrectLogin() CheckForIncorrectLogin} property is true.
     * 
     * @param loginIncorrectVerificationTime the time the handler will wait for a {@link #getLoginIncorrectSequence() "login 
     * incorrect" character sequence} before assuming the login has been successful. 
     */
    public void setLoginIncorrectVerificationTime(
            long loginIncorrectVerificationTime) {
        this.loginIncorrectVerificationTime = loginIncorrectVerificationTime;
    }

    /**
     * Return true if the handler will wait for the {@link #getLoginIncorrectSequence() "login incorrect" character sequence}
     * when executing {@link #doLogin(String, String)}.
     * 
     * @return true if the handler will wait for the {@link #getLoginIncorrectSequence() "login incorrect" character sequence}
     * when executing {@link #doLogin(String, String)}.
     */
    public boolean isCheckForIncorrectLogin() {
        return checkForIncorrectLogin;
    }

    /**
     * Set whether or not the handler will wait for the {@link #getLoginIncorrectSequence() "login incorrect" character sequence}
     * when executing {@link #doLogin(String, String)}.
     * 
     * @param checkForIncorrectLogin if true , the handler will wait for the {@link #getLoginIncorrectSequence() "login incorrect" character sequence}
     * when executing {@link #doLogin(String, String)}.
     */
    public void setCheckForIncorrectLogin(boolean checkForIncorrectLogin) {
        this.checkForIncorrectLogin = checkForIncorrectLogin;
    }

    /**
     * @return
     */
    public boolean areYouThere(long timeout) {
        // Send an AYT
        try {
	        OutputStream os = socket.getOutputStream();
	        os.flush();
	        os.write(TelnetInputStream.IAC);
	        os.write(TelnetInputStream.AYT);
	        
	        // Read until end of input
	        String received = is.consumeInput(timeout);
	        // Something's been received
	        return true;
        } catch(IOException e) {
            return false;
        }
        
        
    }
}