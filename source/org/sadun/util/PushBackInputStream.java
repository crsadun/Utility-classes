package org.sadun.util;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.deltax.util.TimeInterval;

/**
 * A stream that allows to push back an arbitrary number of characters.
 * <p>
 * It also offers some commonly used lookahead comparison operations (based on
 * reading/unreading characters). These should really be in a separate class in
 * the future.
 * 
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano
 *         Sadun</a>
 * @version 1.0
 */
public class PushBackInputStream extends FilterInputStream {

    //private static final boolean debug = false;
    private int[] buf;
    private int currentPos;
    private int initialSize;

    /**
     * Create a pushback reader over the given source of characters, allowing to
     * push
     * 
     * @param r
     *            the source of characters
     */
    public PushBackInputStream(InputStream r) {
        this(r, 300);
    }

    public PushBackInputStream(InputStream r, int initialSize) {
        super(r);
        this.buf = new int[initialSize];
        this.initialSize = initialSize;
    }

    /**
     * Read the next character.
     * 
     * @return the next character on the stream, or -1 at EOF
     * @throws IOException
     *             if there is a problem reading the stream.
     */
    public int read() throws IOException {
        if (currentPos == 0)
            return super.read();
        else {
            int c = buf[--currentPos];
            if (currentPos > initialSize && currentPos % initialSize == 0) {
                int[] buf2 = new int[initialSize
                        + (initialSize * (currentPos / initialSize))];
                System.arraycopy(buf, 0, buf2, 0, currentPos);
                // System.err.println("buffer shrank to "+buf.length);
                buf = buf2;
            }
            return c;
        }
    }

    /**
     * Unread the given character. The character will be returned as result of
     * the next {@link #read()} operation.
     * 
     * @param c
     *            the charater to unread
     * @throws IOException
     *             if there is a problem unreading the character.
     */
    public void unread(int c) throws IOException {
        if (currentPos == buf.length) {
            int[] buf2 = new int[buf.length + initialSize];
            System.arraycopy(buf, 0, buf2, 0, buf.length);
            buf = buf2;

            // if (buf.length > 2 * initialSize) System.err.println("Warning:
            // buffer size is "+ buf.length+ ", more than double its initial
            // size,evaluate increasing the initial size "+ initialSize);

        }
        buf[currentPos++] = c;
    }

    /**
     * Unread the given String. The string character will be returned, in
     * reverse order, as result of the next {@link #read()} operation.
     * 
     * @param s
     *            the string to unread
     * @throws IOException
     *             if there is a problem unreading the character.
     */
    public void unread(String s) throws IOException {
        for (int i = s.length() - 1; i > 0; i--)
            unread(s.charAt(i));
    }

    private static void readAll(InputStream r) throws IOException {
        int c;
        while ((c = r.read()) != -1)
            System.out.print((char) c);
        System.out.println();
    }

    /**
     * Attempt to read the next <i>n</i> characters in the stream, then unread
     * them.
     * 
     * @param n
     *            the number of character to read.
     * @return a string with the read characters. Note that the string may
     *         contain less than n characters if EOF was reached.
     * @throws IOException
     *             in case of problems reading the stream
     */
    public String readNext(int n) throws IOException {
        return readNext(n, true);
    }

    private synchronized String readNext(int n, boolean unread)
            throws IOException {
        String result;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < n; i++) {
            int c = read();
            if (c == -1)
                break;
            sb.append((char) c);
        }
        result = sb.toString();

        if (unread)
            for (int i = result.length() - 1; i >= 0; i--) {
                unread(result.charAt(i));
            }
        return result;
    }

    /**
     * Looks ahead to check whether the next characters on the stream equal the
     * given String. The reader stream position is unchanged.
     * 
     * @param s
     *            the string to compare
     * @return true if the stream contains the string at the current position
     * @throws IOException
     *             in case of problems reading the stream
     */
    public boolean nextEquals(String s) throws IOException {
        return nextEquals(s, true);

    }

    /**
     * Looks ahead to check whether the next characters on the stream equal the
     * given String. The reader stream position is unchanged if there is no
     * match. If a match exists, it is changed or not depending on the value of
     * the <tt>unreadIfTrue</tt> parameter.
     * 
     * @param s
     *            the string to compare
     * @param unreadIfTrue
     *            if <b>false</b> and the comparison is successful, the stream
     *            position will be after the successfully compared string.
     *            Otherwise, it will be unchanged.
     * @return true if the stream contains the string at the current position
     * @throws IOException
     *             in case of problems reading the stream
     */
    public synchronized boolean nextEquals(String s, boolean unreadIfTrue)
            throws IOException {
        return nextEquals0(s, unreadIfTrue, false);
    }

    /**
     * Looks ahead to check whether the next characters on the stream equal the
     * given String (ignoring letter case). The reader stream position is
     * unchanged if there is no match. If a match exists, it is changed or not
     * depending on the value of the <tt>unreadIfTrue</tt> parameter.
     * 
     * @param s
     *            the string to compare
     * @param unreadIfTrue
     *            if <b>false</b> and the comparison is successful, the stream
     *            position will be after the successfully compared string.
     *            Otherwise, it will be unchanged.
     * @return true if the stream contains the string at the current position
     * @throws IOException
     *             in case of problems reading the stream
     */
    public synchronized boolean nextEqualsIgnoreCase(String s,
            boolean unreadIfTrue) throws IOException {
        return nextEquals0(s, unreadIfTrue, true);
    }

    private boolean nextEquals0(String s, boolean unreadIfTrue,
            boolean ignoreCase) throws IOException {
        assert s != null;
        int c = -1, readCount = s.length();
        boolean match = true;
        for (int i = 0; i < s.length(); i++) {
            c = read();

            boolean noMatch;
            if (ignoreCase)
                noMatch = Character.toLowerCase((char) c) != Character
                        .toLowerCase(s.charAt(i));
            else
                noMatch = (char) c != s.charAt(i);

            if (noMatch) {
                match = false;
                readCount = i + 1;
                break;
            }
        }
        if (unreadIfTrue || !match) {
            if (!match)
                unread(c);
            for (int i = readCount - (match ? 1 : 2); i >= 0; i--) {
                unread(s.charAt(i));
            }
        }

        return match;
    }

    public static void main(String args[]) throws Exception {

        
        StringBuffer veryLongString = new StringBuffer();

        for (int i = 0; i < 500; i++)
            veryLongString.append("y");
        
        PushBackInputStream r = new PushBackInputStream(
                new ByteArrayInputStream(veryLongString.toString()
                        .getBytes()), 80);

        long now = System.currentTimeMillis();
        long runTime = TimeInterval.SECOND * 30;
        long elapsed;
        double avgElapsed=0;
        long count=0;
        do {
            r.unread('\n');

            for (int i = 0; i < 500; i++)
                r.unread('x');
            
            for (int i = 0; i < 500; i++)
                r.read();

            elapsed = System.currentTimeMillis()-now;
            Thread.yield();
            
            avgElapsed+=elapsed/++count;

        } while (elapsed < runTime);

        readAll(r);

        System.out.println("Final cycles: "+count);
        System.out.println("Avg. elapsed time per cycle: "+TimeInterval.describe((long)avgElapsed));
        System.out.println("Buffer size: " + r.buf.length);
    }
}