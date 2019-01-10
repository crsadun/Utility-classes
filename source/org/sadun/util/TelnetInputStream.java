package org.sadun.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A stream to handle telnet-based communication.
 * <p>
 * The stream recognizes the standard TELNET command sequences (RFC854) in an
 * input stream and allows for either batch or immediate handling.
 * <p>
 * Batch handling can be realized by retrieving the list of
 * {@link TelnetInputStream.TelnetCommand commands} when the input is blocked, by
 * using {@link #getCommands()}.
 * <p>
 * Immediate handling can be realized by registering a
 * {@link TelnetInputStream.TelnetCommandListener} and responding to the command
 * (to the appropriate output stream) with the result, and invoking
 * {@link #commandHandled(TelnetCommand)} after the command has been properly handled.
 * <p>
 * {@link TelnetInputStream.BasicNVTListener} is a listener which automatically
 * denies any option request, limiting the interaction capabilities to the
 * Telnet NVT. 
 * <p>
 * See also the class {@link org.sadun.util.TelnetInputStreamConsumer} for a
 * wrapper which provides efficient consumption capabilities for machine-processing
 * TELNET conversations.
 * 
 * @version 0.2
 * @author Cristiano Sadun
 */
public class TelnetInputStream extends FilterInputStream {

    /**
     * A standard {@link TelnetInputStream.TelnetCommandListener}that refuses
     * any option and suggestions, limiting the functionality to the basic
     * Telnet NVT (network virtual terminal) functionality.
     * 
     * @author Cristiano Sadun
     */
    public static class BasicNVTListener implements TelnetCommandListener {

        protected OutputStream os;

        /**
         * Create a listener which answers Telnet commands on the given output
         * stream.
         * 
         * @param os
         */
        public BasicNVTListener(OutputStream os) {
            this.os = os;
        }

        /**
         * Reply WON'T to any DO command and DON'T to any WILL command. Other
         * commands are ignored.
         * 
         * @param is
         * @param command
         * @throws IOException
         */
        public void telnetCommandReceived(TelnetInputStream is, TelnetCommand command)
                throws IOException {
            if (command.getCommandCode() == TelnetInputStream.DO) {
                // Reply with WON'T
                os.write(TelnetInputStream.IAC);
                os.write(TelnetInputStream.WONT);
                os.write(command.getOptionCode());
                os.flush();
            } else if (command.getCommandCode() == TelnetInputStream.WILL) {
                // Reply with DON'T
                os.write(TelnetInputStream.IAC);
                os.write(TelnetInputStream.DONT);
                os.write(command.getOptionCode());
                os.flush();
            }
            is.commandHandled(command);
        }
    };

    private Object lock = new Object();

    /**
     * A listener to Telnet commands. Classes implementing this interface can be
     * registed into a {@link TelnetInputStream}and receive notifications of
     * commands.
     * 
     * @author Cristiano Sadun
     */
    public interface TelnetCommandListener {
        public void telnetCommandReceived(TelnetInputStream is, TelnetCommand cmd)
                throws IOException;
    }

    /**
     * The Telnet command code IAC (255)
     */
    public static final int IAC = 255;

    /**
     * The Telnet command code SE (240)
     */
    public static final int SE = 240;

    /**
     * The Telnet command code NOP (241)
     */
    public static final int NOP = 241;

    /**
     * The Telnet command code DM (242)
     */
    public static final int DM = 242;

    /**
     * The Telnet command code BREAK (243)
     */
    public static final int BREAK = 243;

    /**
     * The Telnet command code IP (244)
     */
    public static final int IP = 244;

    /**
     * The Telnet command code AO (245)
     */
    public static final int AO = 245;

    /**
     * The Telnet command code IAC (246)
     */
    public static final int AYT = 246;

    /**
     * The Telnet command code EC (247)
     */
    public static final int EC = 247;

    /**
     * The Telnet command code EL (248)
     */
    public static final int EL = 248;

    /**
     * The Telnet command code GA (249)
     */
    public static final int GA = 249;

    /**
     * The Telnet command code SB (250)
     */
    public static final int SB = 250;

    /**
     * The Telnet command code WILL (251)
     */
    public static final int WILL = 251;

    /**
     * The Telnet command code WONT (252)
     */
    public static final int WONT = 252;

    /**
     * The Telnet command code DO (253)
     */
    public static final int DO = 253;

    /**
     * The Telnet command code DONT (254)
     */
    public static final int DONT = 254;

    private static final int OPTION_TRANSMIT_BINARY = 0;
    private static final int OPTION_ECHO = 1;
    private static final int OPTION_SUPPRESS_GO_AHEAD = 3;
    private static final int OPTION_STATUS = 5;
    
    private static final String[] optNames = { 
      "TRANSMIT_BINARY", "ECHO", null, "SUPPRESS_GO_AHEAD", null, "STATUS"      
    };
    
    private static final String[] cmdNames = { "IAC", "DONT", "DO", "WONT",
            "WILL", "SB", "GA", "EL", "EC", "AYT", "AO", "IP", "BREAK", "DM",
            "NOP", "SE" };

    private long cmdCount = 0;
    private List cmdList = new LinkedList();
    private Set commandListeners = new HashSet();

    private boolean trimLinesEnabled = true;

    private PrintStream debugStream;

    /**
     * A Telnet command, associated to a specific {@link TelnetInputStream}.
     * <p>
     * Commands are produced by a {@link TelnetInputStream}itself on reading.
     * 
     * @version 1.0
     * @author Cristiano Sadun
     */
    public class TelnetCommand {

        private int code;
        private int option;
        private long sequenceNumber;

        TelnetCommand(long sequenceNumber, int code, int option) {
            this.code = code;
            this.option = option;
            this.sequenceNumber = sequenceNumber;
        }

        TelnetCommand(long sequenceNumber, int code) {
            this(sequenceNumber, code, -1);
        }

        /**
         * Return true if the command is has an option code (DO, DONT, WILL,
         * WONT)
         * 
         * @return true if the command is has an option code (DO, DONT, WILL,
         *         WONT)
         */
        public boolean hasOptionCode() {
            return option != -1;
        }

        /**
         * Return the option code for the command.
         * 
         * @return the option code for the command.
         * @exception IllegalStateException
         *                if the command has no options
         */
        public int getOptionCode() {
            if (!hasOptionCode())
                throw new IllegalStateException("The command "
                        + TelnetInputStream.getCommandName(code)
                        + " has not option code");
            return option;
        }

        /**
         * Return the telnet command code
         * 
         * @return the telnet command code
         */
        public int getCommandCode() {
            return code;
        }

        /**
         * Return an human-readable description of the command code.
         * 
         * @return an human-readable description of the command code.
         */
        public String getCommandName() {
            return TelnetInputStream.getCommandName(code);
        }

        /**
         * Return the order of the command as it has been read from the stream
         * 
         * @return the order of the command as it has been read from the stream
         */
        public long getSequenceNumber() {
            return sequenceNumber;
        }

        /**
         * Return a string representation of the command
         * 
         * @return a string representation of the command
         */
        public String toString() {
            return getCommandName() + " "
                    + (hasOptionCode() ? "" + getOptionCode() : "") + " (seq# "
                    + sequenceNumber + ")";
        }

        TelnetInputStream getTIS() {
            return TelnetInputStream.this;
        }
    }

    /**
     * Create a telnet input stream over the given input stream.
     */
    public TelnetInputStream(InputStream in) {
        super(in);
    }

    public int read() throws IOException {
        // Check for IAC
        int c = in.read();
        if (c == -1)
            return -1;
        if (c == IAC) { // Wait for the next character
            int c1 = in.read();
            if (c1 == -1)
                throw new IOException("Unterminated IAC telnet sequence");
            switch (c1) {
            case SE:
            case NOP:
            case DM:
            case BREAK:
            case IP:
            case AO:
            case AYT:
            case EC:
            case EL:
            case GA:
            case SB:
                commandFound(new TelnetCommand(cmdCount, c1));
                break;
            case WILL:
            case WONT:
            case DO:
            case DONT:
                // Option code expected
                int c2 = in.read();
                if (c2 == -1)
                    throw new IOException(
                            "Unterminated telnet option command sequence for command "
                                    + getCommandName(c1) + "(" + c1 + ")");
                commandFound((new TelnetCommand(cmdCount, c1, c2)));
                break;
            default:
                throw new IOException("Unrecognized telnet command int: " + c1);
            }
            cmdCount++;

            // Go read real data
            return read();
        } else
            return c;
    }

    /**
     * @param command
     * @throws IOException
     */
    private void commandFound(TelnetCommand command) throws IOException {
        synchronized (lock) {
            if (debugStream!=null) debugStream.println(command);
            cmdList.add(command);
            int toRemove = -1;
            for (Iterator i = commandListeners.iterator(); i.hasNext();) {
                TelnetCommandListener tcl = (TelnetCommandListener) i.next();
                tcl.telnetCommandReceived(this, command);
            }
        }

    }

    /**
     * Return the list of commands encountered so far, and not yet handled.
     * (Handling is notified by invoking {@link #commandHandled(TelnetCommand)}.
     * 
     * @return the list of commands encountered so far, and not yet handled.
     */
    public TelnetCommand[] getCommands() {
        TelnetCommand[] cmds = new TelnetCommand[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * Remove a command from the list of unhandled commands. This method is used
     * to notify the stream that a Telnet command has been handled, and is
     * typically invoked after either batch or immediate command processing.
     * 
     * @param command
     *            the command that has been handled.
     */
    public void commandHandled(TelnetCommand command) {
        if (command.getTIS() != this)
            throw new RuntimeException("The command " + command
                    + " is not been issued over this TelnetInputStream");
        cmdList.remove(command);

    }

    /**
     * Register a listener to Telnet commands.
     * 
     * @param tcl
     *            the listener to register
     */
    public void registerTelnetCommandListener(TelnetCommandListener tcl) {
        synchronized (lock) {
            commandListeners.add(tcl);
        }
    }

    /**
     * Remove a listener to Telnet commands.
     * 
     * @param tcl
     *            the listener to remove
     */
    public void removeTelnetCommandListener(TelnetCommandListener tcl) {
        synchronized (lock) {
            commandListeners.remove(tcl);
        }
    }

    /**
     * Return a human-readable description of the given Telnet command code.
     * 
     * @param telnetCommandCode
     *            the command code
     * @return a human-readable description of the given Telnet command code.
     */
    private static final String getCommandName(int telnetCommandCode) {
        if (telnetCommandCode > 255 || telnetCommandCode < 240)
            throw new IllegalArgumentException("Invalid command code "
                    + telnetCommandCode + " - range is 240-255 inclusive");
        return cmdNames[255 - telnetCommandCode];
    }

    /**
     * A Line mode helper. Converts both CR LF and CR NUL into a new line, and
     * strips it.
     * 
     * @return a line, interpreted basing on CR LF and CR NUL.
     * @throws IOException
     *
    public String readLine() throws IOException {
        StringBuffer sb = null;
        int c0 = -1, c1 = -1;

        while ((c1 = read()) != -1) {
            if (sb == null)
                sb = new StringBuffer();
            if ((c0 == '\r' && c1 == '\n') | // CRLF|
                    (c0 == '\r' && c1 == 0)) // CR NUL
                break;
            sb.append((char) (c0 = c1));
        }
        if (sb == null)
            return null;
        sb.deleteCharAt(sb.length() - 1);
        String result = sb.toString();
        if (trimLinesEnabled)
            result = result.trim();
        return result;
    }

    /**
     * Return whether or not the line mode helper {@link #readLine()}will trim
     * received lines.
     * 
     * @return whether or not the line mode helper {@link #readLine()}will trim
     *         received lines.
     *
    public boolean isTrimLinesEnabled() {
        return trimLinesEnabled;
    }

    /**
     * Set whether or not the line mode helper {@link #readLine()}will trim
     * received lines.
     * 
     * @param trimLinesEnabled
     *            define whether or not the line mode helper {@link #readLine()}
     *            will trim received lines.
     *
    public void setTrimLinesEnabled(boolean trimLinesEnabled) {
        this.trimLinesEnabled = trimLinesEnabled;
    }*/
    public PrintStream getDebugStream() {
        return debugStream;
    }
    public void setDebugStream(PrintStream debugStream) {
        this.debugStream = debugStream;
    }
}