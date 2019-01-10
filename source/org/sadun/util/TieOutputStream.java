package org.sadun.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * An OutputStream which ties output to two underlying streams. Any character written to the stream
 * is replicated on both.
 *
 * @author C. Sadun
 * @version 1.2
 */
public class TieOutputStream extends OutputStream
{
   private OutputStream os1;
   private OutputStream os2;

   /**
    * Build a OutputStream that ties togheter the two given streams.
    * Every output operation on the OutputStream will be replicated on both.
    * @param os1 the first stream
    * @param os2 the second stream
    */
   public TieOutputStream(OutputStream os1, OutputStream os2)
   { this.os1=os1;
     this.os2=os2;
   }

   /**
    * Write a byte to the stream, and thus to the two underlying streams.
    *
    * @param b the byte to write
    * @exception IOException if something fails during writing
    */
   public void write(int b) throws IOException
   {
   	os1.write(b);
   	doCheckError(os1);
   	os2.write(b);
   	doCheckError(os2);
   }

   /**
    * Close both underlying streams
    *
    * @param b the byte to write
    * @exception IOException if something fails during closing
    */
   public void close() throws IOException {
    os1.close();
    doCheckError(os1);
    os2.close();
    doCheckError(os2);
   }

   /**
    * Flush both underlying streams
    *
    * @param b the byte to write
    * @exception IOException if something fails during flushing
    */
   public void flush() throws IOException {
    os1.flush();
    doCheckError(os1);
    os2.flush();
    doCheckError(os2);
   }

   /**
    * Return the first of the two tied writers
    * @return the first of the two tied writers
    */
   public OutputStream getFirstWriter() { return os1; }

   /**
    * Return the second of the two tied writers
    * @return the second of the two tied writers
    */
   public OutputStream getSecondWriter() { return os2; }

   // If the writer's of PrintStream flavor, do an explicit
   // error check and raise an IOException in case of error
   private void doCheckError(OutputStream w) throws IOException {
    if (w instanceof PrintStream)
        if (((PrintStream)w).checkError())
            throw new IOException("Operation on printwriter failed");
   }


}
