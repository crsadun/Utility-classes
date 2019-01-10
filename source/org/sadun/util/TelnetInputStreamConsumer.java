package org.sadun.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sadun.util.TelnetInputStream.TelnetCommandListener;

import com.deltax.util.TimeInterval;

/**
 * This class wraps over a {@link org.sadun.util.TelnetInputStream}to provide
 * stream consumption capabilities.
 * <p>
 * Input from the stream can be consumed until on a certain
 * {@link TelnetInputStreamConsumer.Criterium criterium}(i.e., a condition)
 * holds.
 * <p>
 * A {@link #setConsumptionOperationsTimeout(long) timeout}can be set on
 * consumption operations to handle situations where the telnet host does not
 * behave as expected.
 * <p>
 * Common criteria, based on looking for strings in the input, are already
 * implemented as methods.
 * <p>
 * A general {@link #consumeByCriteria(Criterium)}method allows the user to
 * provide customized criteria if necessary.
 * <p>
 * The class
 * {@link TelnetInputStreamConsumer.CompositeCriterium CompositeCriterium}
 * allows to combine a set of criteria into one, use it to drive input
 * consumption and then find out which one of the set did indeed hold.
 * 
 * @version 1.0
 * @author Cristiano Sadun
 */
public class TelnetInputStreamConsumer extends FilterInputStream {

    /**
     * A criterium for evaluating input from a {@link TelnetInputStream}.
     * 
     * @version 1.0
     * @author Cristiano Sadun
     */
    public interface Criterium {

        public boolean holds(String input);

    }

    /**
     * An abstract criterium based on other criteria.
     * <p>
     * The class offers services to compose criteria and to retrieve which
     * criterium or criteria hold, after that {@link #holds(String)}has been
     * invoked.
     * <p>
     * All composing criteria are initially in an undefined state. After
     * {@link #holds(String)}is called, and depending on the implementation of
     * that method by the subclass, some will have been verified and found
     * holding or not, while others won't. (For example, ANDing many criteria
     * requires that every one is checked, while ORing them doesn't).
     * <p>
     * Subclasses must implement the method {@link #holdsMultiple(String)}to
     * define the precise semantics of the criteria (the member
     * {@link #operands}contains the
     * {@link TelnetInputStreamConsumer.Criterium}to check).
     * <p>
     * If the {@link #isVerifyAllRequested() verifyAllRequested}property is
     * set, invoking {@link #holds(String)}will trigger the verification of all
     * the composing criteria, regardless of the implementation of
     * {@link #holdsMultiple(String).
     * 
     * 
     * @version 1.0
     * @author Cristiano Sadun
     */
    public static abstract class CompositeCriterium implements Criterium {

        private static final int NOT_VERIFIED = 0;
        private static final int HOLDS = 1;
        private static final int DOES_NOT_HOLD = 2;

        /**
         * The criteria to check.
         */
        protected OperandWrapper[] operands;

        /**
         * VerifyAll mode
         */
        protected boolean verifyAllRequested;

        private int[] checkedState;

        // Wrap an operand so that checkedState is set when holds() is invoked
        private class OperandWrapper implements Criterium {

            private Criterium c;
            private int position;

            OperandWrapper(Criterium c, int position) {
                this.c = c;
                this.position = position;
            }

            public boolean holds(String input) {
                boolean result = c.holds(input);
                checkedState[position] = result ? HOLDS : DOES_NOT_HOLD;
                return result;
            }

            /**
             * @return
             */
            Criterium getWrappedCriterium() {
                return c;
            }
            
            public String toString() { return c.toString(); }
        }

        /**
         * Create an instance using the given criteria as components, without
         * requesting that all be verified when {@link #holds(String)}is
         * invoked.
         * 
         * @param criteria
         *            the composing criteria
         */
        protected CompositeCriterium(Criterium[] criteria) {
            this(criteria, false);
        }

        /**
         * Create an instance using the given criteria as components, possibly
         * requesting that all be verified when {@link #holds(String)}is
         * invoked.
         * 
         * @param criteria
         *            the composing criteria
         */
        protected CompositeCriterium(Criterium[] criteria,
                boolean verifyAllRequested) {
            this.verifyAllRequested = verifyAllRequested;
            operands = new OperandWrapper[criteria.length];
            for (int i = 0; i < criteria.length; i++)
                operands[i] = new OperandWrapper(criteria[i], i);
            this.checkedState = new int[criteria.length];
            Arrays.fill(checkedState, NOT_VERIFIED);
        }

        public final boolean holds(String input) {
            if (verifyAllRequested)
                for (int i = 0; i < operands.length; i++) {
                    operands[i].holds(input);
                }
            return holdsMultiple(input);
        }

        /**
         * This method is to be implemented by subclasses to do the actual
         * verification of the multiple criterium.
         * <p>
         * It has the same role as
         * {@link TelnetInputStreamConsumer.Criterium#holds(java.lang.String)}
         * in {@link TelnetInputStreamConsumer.Criterium}
         * 
         * @param input
         *            the input to verify
         * @return true if the multiple criterium is verified.
         */
        protected abstract boolean holdsMultiple(String input);

        private Criterium[] getCriteriaByState(int state) {
            List l = new ArrayList();
            for (int i = 0; i < checkedState.length; i++) {
                if (checkedState[i] == state)
                    l.add(new Integer(i));
            }
            Criterium[] c = new Criterium[l.size()];
            int count = 0;
            for (Iterator i = l.iterator(); i.hasNext();) {
                int index = ((Integer) i.next()).intValue();
                c[count++] = ((OperandWrapper) operands[index])
                        .getWrappedCriterium();
            }
            return c;
        }

        private Criterium getFirstCriteriaByState(int state) {
            Criterium[] c = getCriteriaByState(state);
            if (c.length == 0)
                return null;
            return c[0];
        }

        /**
         * Return the component criteria the did hold, after
         * {@link TelnetInputStreamConsumer.Criterium#holds(java.lang.String)}
         * has bee called. Criteria that have not yet been verified (that is,
         * for which
         * {@link TelnetInputStreamConsumer.Criterium#holds(java.lang.String)}
         * has not been called) are excluded. Use
         * {@link #getUndefinedCriteria()}to know which they are).
         * 
         * @return the criteria the did hold.
         */
        public Criterium[] getHoldingCriteria() {
            return getCriteriaByState(HOLDS);
        }

        /**
         * Return the first criterium that holds, or <b>null </b> if no criteria
         * holds.
         * 
         * @return the first criterium that holds, or <b>null </b> if no
         *         criteria did hold.
         */
        public Criterium getHoldingCriterium() {
            return getFirstCriteriaByState(HOLDS);
        }

        /**
         * Return the component criteria the did not hold, after
         * {@link TelnetInputStreamConsumer.Criterium#holds(java.lang.String)}
         * has bee called. Criteria that have not yet been verified (that is,
         * for which
         * {@link TelnetInputStreamConsumer.Criterium#holds(java.lang.String)}
         * has not been called) are excluded. Use
         * {@link #getUndefinedCriteria()}to know which they are).
         * 
         * @return the criteria the did not hold.
         */
        public Criterium[] getNotHoldingCriteria() {
            return getCriteriaByState(DOES_NOT_HOLD);
        }

        private Criterium getNotHoldingCriterium() {
            return getFirstCriteriaByState(DOES_NOT_HOLD);
        }

        /**
         * Return the component criteria which have not been verified after
         * {@link TelnetInputStreamConsumer.Criterium#holds(java.lang.String)}
         * has bee called.
         * 
         * @return the criteria the did not hold.
         */
        public Criterium[] getUndefinedCriteria() {
            return getCriteriaByState(NOT_VERIFIED);
        }

        private Criterium getUndefinedCriterium() {
            return getFirstCriteriaByState(NOT_VERIFIED);
        }

        /**
         * Return true if the given criteria has been verified and did hold.
         * 
         * @param criteria
         *            the criteria to verify, which must be part of this
         *            composite criterium
         * @return true if the given criteria has been verified and holds.
         * @exception IllegalArgumentException
         *                if the criteria is not part of this composite
         */
        public boolean isVerifiedAndHolds(Criterium criteria) {
            return verifiedAndInAGivenState(criteria, HOLDS);
        }

        /**
         * Return true if the given criteria has been verified and did not hold.
         * 
         * @param criteria
         *            the criteria to verify, which must be part of this
         *            composite criterium
         * @return true if the given criteria has been verified and holds.
         * @exception IllegalArgumentException
         *                if the criteria is not part of this composite
         */
        public boolean isVerifiedAndDoesNotHold(Criterium criteria) {
            return verifiedAndInAGivenState(criteria, DOES_NOT_HOLD);
        }

        /**
         * Return true if the given criteria has not verified.
         * 
         * @param criteria
         *            the criteria to verify, which must be part of this
         *            composite criterium
         * @return true if the given criteria has been verified and holds.
         * @exception IllegalArgumentException
         *                if the criteria is not part of this composite
         */
        public boolean isVerified(Criterium criteria) {
            return !verifiedAndInAGivenState(criteria, NOT_VERIFIED);
        }

        private boolean verifiedAndInAGivenState(Criterium criteria, int state) {
            for (int i = 0; i < operands.length; i++) {
                if (operands[i].getWrappedCriterium() == criteria) {
                    return checkedState[i] == state;
                }
            }
            throw new IllegalArgumentException(criteria
                    + " is not a criteria of this "
                    + CompositeCriterium.class.getName());
        }

        /**
         * Return true if <i>all </i> the composing criteria are to be checked
         * when {@link #holds(String)}is invoked.
         * 
         * @return
         */
        public boolean isVerifyAllRequested() {
            return verifyAllRequested;
        }

        public void setVerifyAllRequested(boolean verifyAllRequested) {
            this.verifyAllRequested = verifyAllRequested;
        }
    }

    /**
     * A criterium which holds if all its composing criteria hold.
     * 
     * @version 1.0
     * @author Cristiano Sadun
     */
    public static class AndCriterium extends CompositeCriterium {

        public AndCriterium(Criterium c1, Criterium c2) {
            this(new Criterium[] { c1, c2 });
        }

        public AndCriterium(Criterium[] criteria) {
            super(criteria);
        }

        public boolean holdsMultiple(String input) {
            for (int i = 0; i < operands.length; i++) {
                if (!operands[i].holds(input))
                    return false;
            }
            return true;
        }
        
        public String toString() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            for(int i=0;i<operands.length;i++) {
                pw.print(operands[i]);
                if (i<operands.length-1) pw.print(" and ");
            }
            return sw.toString(); 
        }

    }

    /**
     * A criterium which holds if one of its composing criteria hold.
     * 
     * @version 1.0
     * @author Cristiano Sadun
     */
    public static class OrCriterium extends CompositeCriterium {

        public OrCriterium(Criterium c1, Criterium c2) {
            this(new Criterium[] { c1, c2 });
        }

        public OrCriterium(Criterium[] criteria) {
            super(criteria);
        }

        public boolean holdsMultiple(String input) {
            for (int i = 0; i < operands.length; i++) {
                if (operands[i].holds(input))
                    return true;
            }
            return false;
        }
        
        public String toString() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            for(int i=0;i<operands.length;i++) {
                pw.print(operands[i]);
                if (i<operands.length-1) pw.print(" or ");
            }
            return sw.toString(); 
        }

    }

    /**
     * A criterium based on regular expression matching.
     * 
     * @version 1.0
     * @author Cristiano Sadun
     */
    public static class PatternBasedCriterium implements Criterium {

        private Pattern toMatch;
        private boolean onlyAtEnd;

        PatternBasedCriterium(Pattern toMatch, boolean onlyAtEnd) {
            this.toMatch = toMatch;
            this.onlyAtEnd = onlyAtEnd;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.sadun.util.TelnetInputStreamConsumer.Criterium#holds(java.lang.String)
         */
        public boolean holds(String input) {
            Matcher matcher = toMatch.matcher(input);
            if (matcher.lookingAt()) {
                if (onlyAtEnd) {
                    return (matcher.end() == input.length());
                } else
                    return true;
            } else
                return false;
        }
        
        public String toString() {
            return "input matches "+toMatch;
        }

    }

    /**
     * A criterium which holds if a certain string is found at the end of the
     * input.
     * 
     * @version 1.0
     * @author Cristiano Sadun
     */
    public static class StringEndBasedCriterium implements Criterium {

        private String endString;

        public StringEndBasedCriterium(String endString) {
            this.endString = endString;
        }

        public boolean holds(String input) {
            return input.endsWith(endString);
        }
        
        public String toString() {
            return "input ends with "+endString;
        }

    }

    /**
     * A criterium which holds if a certain string is found in the input.
     * 
     * @version 1.0
     * @author Cristiano Sadun
     */
    public static class ContainsStringCriterium implements Criterium {

        private String s;

        public ContainsStringCriterium(String s) {
            this.s = s;
        }

        public boolean holds(String input) {
            return input.indexOf(s) != -1;
        }
        
        public String toString() {
            return "input contains "+s;
        }
    }

    /**
     * A criterium which holds if no more input is available within a certain
     * amount of time (i.e. we can suppose that the host has nothing more to
     * say).
     * <p>
     * The minimum granularity of the timeout is
     * {@link TelnetInputStreamConsumer#BASIC_CRITERIA_CHECK_TIME}, that is,
     * timeouts lower than that value won't be enforced.
     * 
     * @author Cristiano Sadun
     */
    public static class NoMoreInputAvailableCriterium implements Criterium {

        private long timeout;
        private long lastInvocationTime = -1;
        private boolean cumulative;

        /**
         * Create a critierium which will hold when no input is received for a
         * certain waiting time.
         * 
         * @param cumulative
         *            if true, the criterium will consider the time from the
         *            first nonempty input. Otherwise, it will consider the time
         *            from the first empty input (that is, receiving input rests
         *            the time count).
         * @param waitingTime
         *            the time to wait until this criterium holds
         */
        public NoMoreInputAvailableCriterium(boolean cumulative,
                long waitingTime) {
            this.timeout = waitingTime;
            this.cumulative = cumulative;
        }

        /**
         * Create a critierium which will hold when no input is received.
         * <p>
         * The waiting timeis set as double the
         * {@link TelnetInputStreamConsumer#BASIC_CRITERIA_CHECK_TIME}.
         * 
         * @param cumulative
         *            if true, the criterium will consider the time from the
         *            first nonempty input. Otherwise, it will consider the time
         *            from the first empty input (that is, receiving input rests
         *            the time count).
         */
        public NoMoreInputAvailableCriterium(boolean cumulative) {
            this(cumulative,
                    TelnetInputStreamConsumer.BASIC_CRITERIA_CHECK_TIME * 2);
        }

        /**
         * Return true when no input has been received during the time interval
         * defined at construction.
         * 
         * @see org.sadun.util.TelnetInputStreamConsumer.Criterium#holds(java.lang.String)
         */
        public boolean holds(String input) {
            if (lastInvocationTime == -1) {
                lastInvocationTime = System.currentTimeMillis();
                return false;
            } else {
                if (input.length() > 0) {
                    if (!cumulative) {// Reset
                        lastInvocationTime = System.currentTimeMillis();
                        return false;
                    } else {
                        // Check timeout
                        long now = System.currentTimeMillis();
                        return now - lastInvocationTime > timeout;
                    }
                } else {
                    // Check timeout
                    long now = System.currentTimeMillis();
                    return now - lastInvocationTime > timeout;
                }
            }
        }
        
        public String toString() {
            return "no more input is available";
        }
    }

    private long consumptionOperationsTimeout;
    private PrintStream debugStream;

    /**
     * The minimum granularity (in milliseconds) with which consumption criteria
     * are checked. Its value is 1000L.
     */
    public static final long BASIC_CRITERIA_CHECK_TIME = 100L;

    public TelnetInputStreamConsumer(TelnetInputStream is) {
        super(is);
        this.debugStream = is.getDebugStream();
    }
    
    /**
     * Register a listener to Telnet commands.
     * 
     * @param tcl
     *            the listener to register
     */
    public void registerTelnetCommandListener(TelnetCommandListener tcl) {
        ((TelnetInputStream) in).registerTelnetCommandListener(tcl);
    }

    /**
     * Remove a listener to Telnet commands.
     * 
     * @param tcl
     *            the listener to remove
     */
    public void removeTelnetCommandListener(TelnetCommandListener tcl) {
        ((TelnetInputStream) in).removeTelnetCommandListener(tcl);
    }

    /**
     * Consumes all input that appears within a certain timeout. This is useful
     * to get ride of post-login babble.
     * 
     * @throws IOException
     */
    public String consumeInput(long timeout) throws IOException {
        boolean doContinue = true;
        long lastTime = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();
        while (doContinue) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return sb.toString();
            }

            int c = -1;
            while (in.available() > 0) {
                c = in.read();
                if (debugStream != null)
                    debugStream.print((char) c);
                sb.append((char) c);
            }

            long now = System.currentTimeMillis();
            if (now - lastTime > timeout)
                doContinue = false; // throw new RuntimeException("TIMEOUT");
            if (c != -1) {
                lastTime = now;
            } // else System.err.println("No reads for
            // "+TimeInterval.describe(now-lastTime));

        }
        return sb.toString();
    }

    /**
     * Consume all the input until a pattern is matched - possibly only at the
     * end of the input sequence
     * 
     * @param toMatch
     * @param onlyAtEnd
     * @return
     * @throws IOException
     * @throws OperationTimedoutException
     */
    public String consumeInput(Pattern toMatch, boolean onlyAtEnd)
            throws IOException, OperationTimedoutException {

        return consumeByCriteria(new PatternBasedCriterium(toMatch, onlyAtEnd));
    }

    /**
     * Consume all the input until a pattern is matched at the end of the input
     * sequence
     * 
     * @param toMatch
     * @param onlyAtEnd
     * @return
     * @throws IOException
     * @throws OperationTimedoutException
     */
    public String consumeInput(Pattern toMatch) throws IOException,
            OperationTimedoutException {
        return consumeInput(toMatch, true);
    }

    /**
     * Consume all the input until a given string is found at the end of it
     * (faster than {@link #consumeInput(Pattern) for constant patterns).
     * 
     * @param s
     * @return
     * @throws IOException
     * @throws OperationTimedoutException
     */
    public String consumeInputUntilStringFoundAtEnd(String s)
            throws IOException, OperationTimedoutException {
        return consumeByCriteria(new StringEndBasedCriterium(s));
    }

    /**
     * Consume all the input until one of a set of given strings is found at the
     * end of it (faster than
     * {@link #consumeInput(Pattern) for constant patterns).
     * 
     * @param s
     * @return
     * @throws IOException
     * @throws OperationTimedoutException
     */
    public String consumeInputUntilStringFoundAtEnd(String[] s)
            throws IOException, OperationTimedoutException {
        Criterium[] c = new Criterium[s.length];
        for (int i = 0; i < s.length; i++)
            c[i] = new StringEndBasedCriterium(s[i]);
        return consumeByCriteria(new OrCriterium(c));
    }

    /**
     * Consume all the input until a string is found in it (may be faster than
     * {@link #consumeInput(Pattern) for constant patterns).
     * 
     * @param s
     * @return
     * @throws IOException
     * @throws OperationTimedoutException
     */
    public String consumeInputUntilStringFound(String s) throws IOException,
            OperationTimedoutException {
        return consumeByCriteria(new ContainsStringCriterium(s));
    }

    /**
     * Consume all the input until one of a set strings is found in it (may be
     * faster than {@link #consumeInput(Pattern) for constant patterns).
     * 
     * @param s
     * @return
     * @throws IOException
     * @throws OperationTimedoutException
     */
    public String consumeInputUntilStringFound(String[] s) throws IOException,
            OperationTimedoutException {
        Criterium[] c = new Criterium[s.length];
        for (int i = 0; i < s.length; i++)
            c[i] = new ContainsStringCriterium(s[i]);
        return consumeByCriteria(new OrCriterium(c));
    }

    /**
     * Consumes the input based on some
     * {@link TelnetInputStreamConsumer.Criterium criterium}.
     * <p>
     * The
     * {@link #setConsumptionOperationsTimeout(long) consumption operations timeout}
     * is used for checking timeouts.
     * 
     * 
     * @param criteria
     * @return
     * @throws IOException
     * @throws OperationTimedoutException
     */
    public String consumeByCriteria(Criterium criteria) throws IOException,
            OperationTimedoutException {
        StringBuffer sb = new StringBuffer();
        String input;
        boolean doContinue = true;
        long lastReceptionTime = System.currentTimeMillis();
        do {
            input = consumeInput(BASIC_CRITERIA_CHECK_TIME);
            doContinue = !criteria.holds(input);
            if (input.length() > 0) {
                sb.append(input);

                lastReceptionTime = System.currentTimeMillis();
            } else { // No input received, check timeout
                if (consumptionOperationsTimeout == 0)
                    continue; // Wait indefinitely
                long now = System.currentTimeMillis();
                if (now - lastReceptionTime > consumptionOperationsTimeout)
                    throw new OperationTimedoutException(
                            "Consumption operation timed out: no input from telnet host in "
                                    + TimeInterval
                                            .describe(consumptionOperationsTimeout)+". Received input follows:"
                                    +System.getProperty("line.separator")+sb.toString());

            }
        } while (doContinue);
        return sb.toString();
    }

    /**
     * This method implements a typical consumption pattern when a command is
     * sent programmaticly over a telnet channel, and one of two results may
     * incur as a result in the host output (that is, this inputstream).
     * <p>
     * {@link #setConsumptionOperationsTimeout(long)}can be used to impose a
     * timeout on the result.
     * 
     * @param result
     * @param failure
     * @return
     * @throws OperationTimedoutException
     * @throws IOException
     */
    public boolean consumeUntilResultOrFailure(String successString,
            String failureString, String[] result) throws IOException,
            OperationTimedoutException {
        if (result.length != 1)
            throw new IllegalArgumentException(
                    "Invalid argument: the result array parameter is used"
                            + " to contain the string read from the telnet stream, so it must"
                            + " be of length 1. The passed array has length "
                            + result.length);
        ContainsStringCriterium successCriterium = new ContainsStringCriterium(
                successString);
        ContainsStringCriterium failureCriterium = new ContainsStringCriterium(
                failureString);
        OrCriterium c = new OrCriterium(successCriterium, failureCriterium);
        result[0] = consumeByCriteria(c);
        if (c.isVerifiedAndHolds(successCriterium)) {
            if (c.isVerifiedAndHolds(failureCriterium))
                throw new RuntimeException(
                        "Internal error: both failure and success criteria"
                                + " hold - i.e. both strings are found in the telnet result."
                                + " Please adjust the strings so that they indicates either success or failure");
            return true;
        } else if (c.isVerifiedAndHolds(failureCriterium))
            return false;
        else
            throw new RuntimeException(
                    "Internal error: neither success nor failure criteria"
                            + " hold, but consumeByCriteria() has exited without an exception."
                            + " This looks like a bug in consumeByCriteria()");
    }

    /**
     * Get the timeout on all consumption operations. A zero timeout indicates
     * indefinite wait.
     * 
     * @return the timeout on all consumption operations.
     */
    public long getConsumptionOperationsTimeout() {
        return consumptionOperationsTimeout;
    }

    /**
     * Set the timeout on all consumption operations. A zero timeout indicates
     * indefinite wait.
     * 
     * @param consumptionOperationsTimeout
     *            the timeout on all consumption operations.
     */
    public void setConsumptionOperationsTimeout(
            long consumptionOperationsTimeout) {
        this.consumptionOperationsTimeout = consumptionOperationsTimeout;
    }

    /**
     * @return
     */
    TelnetInputStream getTelnetInputStream() {
        return (TelnetInputStream)in;
    }

    /*
     * public PrintStream getDebugStream() { return debugStream; } public void
     * setDebugStream(PrintStream debugStream) { this.debugStream = debugStream; }
     */
}