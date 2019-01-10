package org.sadun.util;

import java.io.IOException;
import java.io.Writer;

/**
 * A null writer.
 * 
 * @author Cristiano Sadun
 */
public class NullWriter extends Writer {
    
    public static final NullWriter INSTANCE = new NullWriter(); 
    
    private NullWriter() {
    }

    public void close() throws IOException {
    }
    public void flush() throws IOException {

    }
    public void write(char[] cbuf, int off, int len) throws IOException {
    }
    public void write(char[] cbuf) throws IOException {
    }
    public void write(int c) throws IOException {
    }
    public void write(String str, int off, int len) throws IOException {
    }
    public void write(String str) throws IOException {
    }
}