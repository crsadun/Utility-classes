package org.sadun.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * A class that automatically intercepts and prints line separators
 * without explicitly needing for println() to be called (behaves
 * as PrintStream rather than PrintWriter.
 */
public class AutoCRWriter extends Writer {

    private static char[] lineSep;
    private PrintWriter out;
    private int index=0;

    public AutoCRWriter(Writer out) {
        if (out instanceof PrintWriter) this.out=(PrintWriter)out;
        else this.out=new PrintWriter(out);
    }

    public AutoCRWriter(OutputStream out) {
        this.out=new PrintWriter(new OutputStreamWriter(out));
    }

    public void write(int c) throws IOException {

        //System.out.println("C="+c);

        if (c==lineSep[index]) {
            if (++index == lineSep.length) {
                out.flush();
                out.println();
                index=0;
                return;
            }
        } else {
            if (index != 0) {
                for(int i=0; i < index; i++)
                    out.write(lineSep[i]);
                index=0;
            }
            out.write(c);
        }
    }

    public void close() throws IOException {
        if (index != 0) {
            for(int i=0; i < index; i++)
                out.write(lineSep[i]);
            index=0;
        }
        out.close();
    }

    public void flush() throws IOException {
        if (index != 0) {
            for(int i=0; i < index; i++)
                out.write(lineSep[i]);
            index=0;
        }
        out.flush();
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        for(int i=off;i<off+len;i++)
            write((int)cbuf[i]);
    }

    static {
         String s = System.getProperty("line.separator");
         lineSep = new char[s.length()];
         for(int i=0;i<lineSep.length;i++) lineSep[i]=s.charAt(i);
    }


    public static void main(String args[]) throws IOException {
        Writer w = new PrintWriter(new AutoCRWriter(System.out));
        w.write("Hello");
        w.write(new String(lineSep));
        w.write("World");
        w.write(new String(lineSep));
        //w.flush();

        ((PrintWriter)w).print("Hello2");
        ((PrintWriter)w).println();
        ((PrintWriter)w).print("World2");
        ((PrintWriter)w).println();
    }

}

