package org.sadun.util;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A PrintWriter that unconditionally prefixes any new line of output
 * with a given prefix.
 * <p>
 * The prefix may be a constant string, or computed on each invocation
 * if a {@link PrefixProvider PrefixProvider} object is given at construction
 *
 * @version 1.1
 * @author C. Sadun
 *
 */
public class PrefixPrintWriter extends PrintWriter {

    private static char [] ls=System.getProperty("line.separator").toCharArray();
    private PrefixProvider pp;
    private int truncatedNL;
    private boolean start=true;
    private boolean autoFlush=true;

    private static final class TimePrefixProvider implements PrefixProvider {

        private TimePrefixProvider() { }
        public String getPrefix() {
            return "["+
                   DateFormat.getDateTimeInstance().format(new Date())+
                   "] "; }
    }

    /**
     * A prefix provider to show the current directory as a prefix.
     * <p>
     * Optionally, a certain directory can be indicated to be substituted
     * with a "tilde" (~) charachter in the prefix. The substitution occurs
     * only when the current directory is the given one ora a subdirectory of the
     * given one (i.e., the current directory path starts or equals the the given
     * one's path).
     *
     */
    public static final class DirectoryPrefixProvider implements PrefixProvider {

        private String tildeValue;

        /**
         * Create a prefix provider which shows the absolute path name
         * of the current directory
         */
        public DirectoryPrefixProvider() {
            this(null);
        }

        /**
         * Create a prefix provider which shows the absolute path name
         * of the current directory, substituting the given directory
         * with a 'tilde' (~) charachter.
         * @param tildeValue the directory that must be substituted with a tilde
         * @exception RuntimeException if the given File is not a directory
         */
         public DirectoryPrefixProvider(File tildeValue) {
            if (tildeValue != null) {
                if (! tildeValue.isDirectory())
                    throw new RuntimeException("tilde value can only be a directory, or null");
                this.tildeValue=PathNormalizer.normalize(tildeValue).getAbsolutePath();
            }
        }

        /**
         * Return a string to prefix to the stream lines.
         * @return a string to prefix to the stream lines.
         */
        public String getPrefix() {
            String dir = PathNormalizer.normalize(new File(".")).getAbsolutePath();
            if (tildeValue != null) {
                if (dir.equals(tildeValue)) dir="~"+File.separator;
                else if (dir.startsWith(tildeValue)) dir="~"+File.separator+dir.substring(tildeValue.length());
            }
            return dir;
        }

    }

    /**
     * A built-in provider which prefixes the output with time information
     */
    public static final TimePrefixProvider TIME_PREFIXPROVIDER = new TimePrefixProvider();

    // A utility provider to allow for static strings
    private static class ConstantStringPrefixProvider implements PrefixProvider {

        private String s;

        public ConstantStringPrefixProvider(String s) {
            this.s=s;
        }

        public String getPrefix() { return s; }
    }

    /**
     * Build a PrefixPrintWriter which wraps the given writer,
     * and use the given {@link PrefixProvider PrefixProvider}
     * to determine the prefix.
     *
     * @param w the writer to wrap on
     * @param pp the {@link PrefixProvider PrefixProvider} providing the prefix for each line
     */
    public PrefixPrintWriter(Writer w, PrefixProvider pp) {
        super(w, true);
        this.truncatedNL=0;
        this.pp=pp;
    }

    /**
     * Build a PrefixPrintWriter which wraps the given writer,
     * and prefixes lines with the given constant string
     *
     * @param w the writer to wrap on
     * @param prefix the prefix for each line
     */
    public PrefixPrintWriter(Writer w, String prefix) {
        this(w, new ConstantStringPrefixProvider(prefix));
    }

    /**
     * Build a PrefixPrintWriter which wraps the given output stream,
     * and use the given {@link PrefixProvider PrefixProvider}
     * to determine the prefix.
     *
     * @param os the output stream to wrap on
     * @param pp the {@link PrefixProvider PrefixProvider} providing the prefix for each line
     */
    public PrefixPrintWriter(OutputStream os, PrefixProvider pp) {
        this(new OutputStreamWriter(os), pp);
    }

    /**
     * Build a PrefixPrintWriter which wraps the given output stream,
     * and prefixes lines with the given constant string
     *
     * @param os the output stream to wrap on
     * @param prefix the prefix for each line
     */
    public PrefixPrintWriter(OutputStream os, String prefix) {
        this(new OutputStreamWriter(os), prefix);
    }

    /**
     * Write a character
     */
    public void write(int c) {
     write(new char[] { (char)c }, 0, 1);
    }

    /**
    * Write a substring of a string, of given length from a given offset
    */
   public void write(String s, int off, int len) {
	 write(s.toCharArray(), off, len);
   }

   /**
    * Write a portion of character array, of given length from a given offset
    */
   public void write(char buf[], int off, int len) {
    synchronized(out) {

     String prefix = pp.getPrefix();

     if (start) {
        super.write(prefix, 0, prefix.length());
        start=false;
     }

     List pos = new ArrayList();
     int truncated=truncatedNL; // Remember if we start in a truncated-newline situation
     for(int i=off;i<off+len;i++) {
 		if (isNL(buf, i, off+len)) pos.add(new Integer(i));
     }
	 int p1 = 0;
     String s;
     for(Iterator i=pos.iterator();i.hasNext(); ) {
       int p2 = ((Integer)i.next()).intValue();
	   super.write(buf, p1, p2-p1);
       super.write(ls, 0, ls.length);
       prefix = pp.getPrefix();
       super.write(prefix, 0, prefix.length());
       p1=p2+ls.length-truncated;
	   if (truncated!=0) truncated=0; // Just the first time
   	 }
  	 super.write(buf, p1, off+len-p1);
	 if (autoFlush) super.flush();
    }
   }

   /**
    * Checks if buf matches the line separator this.ls,
    * setting this.truncatedNL if a partial match exists
    * but the buffer portion is too short for a complete
    * match
    */
   private boolean isNL(char []buf, int start, int end) {
   for(int i=truncatedNL;i<ls.length && i<end;i++) {
        if (buf[start+i-truncatedNL]!=ls[i]) {
            if (truncatedNL !=0) truncatedNL=0;
            return false;
        }
    }
    if (end-start+truncatedNL < ls.length) {
        truncatedNL=end-start;
        return false;
    }
	if (truncatedNL !=0) truncatedNL=0;
    return true;
   }

   /**
    * Terminate the current line by writing the line separator string.  The
    * line separator string is defined by the system property
    * <code>line.separator</code>, and is not necessarily a single newline
    * character (<code>'\n'</code>).
    */
   public void println() {
	super.println();
	start=true;
   }


   public static void main(String []args) throws Exception {
     PrefixPrintWriter pw = new PrefixPrintWriter(System.out, ">");
     pw.write(ls[0]);
     pw.write(ls[1]);
     pw.print("This is a test... ");
     pw.println("Hello"+System.getProperty("line.separator")+"World");
   }

}