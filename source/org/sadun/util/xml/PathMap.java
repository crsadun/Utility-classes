package org.sadun.util.xml;

/**
 * An object that can be used to retrieve node values of an XML document via a path language. Note that
 * the path language need not be as complex as XPath.
 *  @version 1.0
 **/
public interface PathMap {
  
    /**
     * Return the textual value of the note indicated by the path.
     * @param path the path, in a given path language
     * @return the textual value of the note indicated by the path.
     */
    public String getValue(String path);
}
