package org.sadun.util;

/**
 * Classes implementing this interface can provide a prefix for a {@link PrefixPrintWriter
 * PrefixPrintWriter} object.
 *
 * @author C. Sadun
 * @version 1.0
 */
public interface PrefixProvider {

    /**
     * Return a string to prefix to the stream lines.
     * @return a string to prefix to the stream lines
     */
    public String getPrefix();
}