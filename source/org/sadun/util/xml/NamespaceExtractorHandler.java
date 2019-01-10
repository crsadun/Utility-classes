package org.sadun.util.xml;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This SAX handler collects the known namespace prefixes in
 * a document and make them available as a {@link #getNamespacesMap() map}. 
 *
 * @author Cristiano Sadun
 */
public class NamespaceExtractorHandler extends DefaultHandler {
    
    
    private Map namespacesMap = new HashMap();
    
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        namespacesMap.put(prefix, uri);
    }

    /**
     * @return Returns the namespacesMap.
     */
    public Map getNamespacesMap() {
        return namespacesMap;
    }
    
    /**
     * Return the prefix for the given URI, or null if
     * no such prefix is defined.
     * @param uri
     * @return
     */
    public String getPrefixForUri(String uri) {
        return (String)namespacesMap.get(uri);
    }
    
    public String getTagName(String uri, String localName) {
        String pfx = getPrefixForUri(uri);
        if (pfx==null) return localName;
        else return pfx+":"+localName;
    }
}
