package org.sadun.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A PrintWriter which embeds a StringWriter.
 * 
 * @author Cristiano Sadun
 */
public class PrintStringWriter extends PrintWriter {

    private StringWriter sw;
    private PrintWriter pw;

    /**
     * @param out
     */
    public PrintStringWriter() {
        super(NullWriter.INSTANCE);
        sw = new StringWriter();
        pw = new PrintWriter(sw);
    }
    
    public String toString() {
        return sw.toString();
    }
    
    public int length() {
        return sw.getBuffer().length();
    }

    public boolean checkError() {
        return pw.checkError();
    }

    public void close() {

        pw.close();
    }

    public void flush() {

        pw.flush();
    }

    public void print(boolean b) {

        pw.print(b);
    }

    public void print(char c) {

        pw.print(c);
    }

    public void print(char[] s) {

        pw.print(s);
    }

    public void print(double d) {

        pw.print(d);
    }

    public void print(float f) {

        pw.print(f);
    }

    public void print(int i) {

        pw.print(i);
    }

    public void print(long l) {

        pw.print(l);
    }

    public void print(Object obj) {

        pw.print(obj);
    }

    public void print(String s) {

        pw.print(s);
    }

    public void println() {

        pw.println();
    }

    public void println(boolean x) {

        pw.println(x);
    }

    public void println(char x) {

        pw.println(x);
    }

    public void println(char[] x) {

        pw.println(x);
    }

    public void println(double x) {

        pw.println(x);
    }

    public void println(float x) {

        pw.println(x);
    }

    public void println(int x) {

        pw.println(x);
    }

    public void println(long x) {

        pw.println(x);
    }

    public void println(Object x) {

        pw.println(x);
    }

    public void println(String x) {

        pw.println(x);
    }

    public void write(char[] buf, int off, int len) {

        pw.write(buf, off, len);
    }

    public void write(char[] buf) {

        pw.write(buf);
    }

    public void write(int c) {

        pw.write(c);
    }

    public void write(String s, int off, int len) {

        pw.write(s, off, len);
    }

    public void write(String s) {

        pw.write(s);
    }

}