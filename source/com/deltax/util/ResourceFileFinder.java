package com.deltax.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * This interface defines services for publicly load resource files.
 * <p>
 * The {@link JDK12ClassFileFinder JDK12ClassFileFinder} class implements this 
 * interface emulating the Java 2 system class loader behaviour.
 *
 * @version 1.0
 * @author Cristiano Sadun
 */
public interface ResourceFileFinder {
	
	/**
     * Returns the supported loading scheme
     * @return a description string
     */
    public String getSupportedLoadingScheme();
	
	/**
	 * Find a resource in the filesystem. The returned file either is the resource itself,
	 * or a JAR/ZIP file containing the resource.
	 * 
	 * @param resource the resource path
	 * @return a stream to the resource, or <b>null</b> if the resource cannot be found
	 * @exception IOException if a problem arises accessing the resource
	 */
	public File findResourceFile(String resource) throws IOException;
	
	/**
	 * Return a byte array with the bytes for the resource, or <b>null</b> if the 
	 * resource cannot be found.	 * @param resource the resource name	 * @return byte[] the bytes of the resource	 * @throws IOException if a problem arises accessing the resource	 */
	public byte [] getResourceBytes(String resource) throws IOException;
	
	/**
	 * Return the stream associated to a resource in the filesystem, or <b>null</b>
	 * if such resource cannot be found.
	 * <p>
	 * The returned stream is not buffered.
	 * 
	 * @param resource the resource path
	 * @return a stream to the resource, or <b>null</b> if the resource cannot be found
	 * @exception IOException if a problem arises accessing the resource
	 */
	public InputStream openResource(String resource) throws IOException;

}
