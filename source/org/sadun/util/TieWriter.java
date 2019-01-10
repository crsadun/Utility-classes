package org.sadun.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * An Writer which ties output to two underlying streams. Any character written to the stream
 * is replicated on both.
 *
 * @author C. Sadun
 * @version 1.2
 */
public class TieWriter extends Writer
{
   private Writer w1;
   private Writer w2;

   /**
    * Build a Writer that ties togheter the two given streams.
    * Every output operation on the Writer will be replicated on both.
    * @param w1 the first stream
    * @param w2 the second stream
    */
   public TieWriter(Writer w1, Writer w2)
   { this.w1=w1;
     this.w2=w2;
   }

   /**
    * Write a byte to the stream, and thus to the two underlying streams.
    *
    * @param b the byte to write
    * @exception IOException if something fails during writing
    */
   public void write(char[] buf, int off, int len) throws IOException
   {
   	w1.write(buf, off, len);
   	doCheckError(w1);
   	w2.write(buf, off, len);
   	doCheckError(w2);
   }

   /**
    * Close both underlying streams
    *
    * @param b the byte to write
    * @exception IOException if something fails during closing
    */
   public void close() throws IOException {
    w1.close();
    doCheckError(w1);
    w2.close();
    doCheckError(w2);
   }

   /**
    * Flush both underlying streams
    *
    * @param b the byte to write
    * @exception IOException if something fails during flushing
    */
   public void flush() throws IOException {
    w1.flush();
    doCheckError(w1);
    w2.flush();
    doCheckError(w2);
   }

   /**
    * Return the first of the two tied writers
    * @return the first of the two tied writers
    */
   public Writer getFirstWriter() { return w1; }

   /**
    * Return the second of the two tied writers
    * @return the second of the two tied writers
    */
   public Writer getSecondWriter() { return w2; }

   // If the writer's of PrintWriter flavor, do an explicit
   // error check and raise an IOException in case of error
   private void doCheckError(Writer w) throws IOException {
    if (w instanceof PrintWriter)
        if (((PrintWriter)w).checkError())
            throw new IOException("Operation on printwriter failed");
   }

   public static void main(String args[]) throws Exception {
    String lineSep = System.getProperty("line.separator");
    PrintWriter w = new PrintWriter(new TieWriter(new java.io.FileWriter("test.txt"), new org.sadun.util.AutoCRWriter(System.out)));
    w.print("Hello");
    w.println();
    w.print("World");
    w.println();

    w.close();
   }


}
