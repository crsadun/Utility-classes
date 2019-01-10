package org.sadun.util.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sadun.util.ObjectLister;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This SAX handler can be used to create a map of simple paths to node values of an XML document.
 * <p>
 * After parsing, the method {@link #getValue(String) getValue(path)} returns the textual value
 * of the note indicated by the path. Paths are in the form
 * <pre>
 *  /tag1/tag2/...
 *  /tag1/tag2/@attribute
 * </pre>
 *
 * @version 1.0
 * @author Cristiano Sadun
 */
public class SimplePathMapCreatorHandler extends DefaultHandler implements PathMap {
    
    private Map pathsMap=new HashMap();
    private StringBuffer sb = new StringBuffer();
    private List tagsStack=new ArrayList();
    private boolean trimWhiteSpace;
    
    /**
     * Construct an instance that trims whitespace in values depending on the value
     * of the given parameter.
     * 
     * @param trimWhiteSpace if true, the instance will trim whitespace.
     */
    public SimplePathMapCreatorHandler(boolean trimWhiteSpace) {
        this.trimWhiteSpace=trimWhiteSpace;
    }
    
    /**
     * Construct an instance that trims whitespace in values.
     *
     */
    public SimplePathMapCreatorHandler() {
        this(true);
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        sb.append(ch, start,length);
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        String path=makePath();
        String value=sb.toString();
        if (trimWhiteSpace) value=value.trim();
        pathsMap.put(path, value);
        sb.delete(0, sb.length());
        tagsStack.remove(tagsStack.size()-1);
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String name, String qName,
            Attributes atts) throws SAXException {
        tagsStack.add(name);
        for(int i=0;i<atts.getLength();i++) {
            pathsMap.put(name+"/@"+atts.getLocalName(i), atts.getValue(i));
        }
    }
    
    private String makePath() {
        StringBuffer sb=new StringBuffer();
        for(Iterator i=tagsStack.iterator();i.hasNext();) {
            sb.append(i.next());
            if (i.hasNext()) sb.append('/');
        }
        return sb.toString();
    }
    
    public String getValue(String path) {
        if(path.endsWith("/")) path=path.substring(0, path.length()-1);
        return (String)pathsMap.get(path);
    }
}
