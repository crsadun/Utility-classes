package org.sadun.util.xml.configuration;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A Configurator is a SAX Content handler which receives SAX events pertaining 
 * <i>only</i> to a specific element in an XML document (and typically reacts 
 * incrementally configuring an associated object, hence the name).
 * <p>
 * For example, when parsing the following XML
 * <pre>
 * &lt;?xml version=&quot;1.0&quot;?&gt;
 *  &lt;elem1&gt;
 *   &lt;sub-elem1&gt;
 *     ...
 *   &lt;/sub-elem1&gt;
 *   &lt;sub-elem2&gt;
 *     ...
 *   &lt;/sub-elem2&gt;
 *  &lt;/elem1&gt;
 * &gt;
 * </pre>
 * we might want to handle <tt>sub-elem1</tt> and <tt>sub-elem2</tt>
 * with specific classes rather than into a single big SAX ContentHandler.
 * <p>
 * A Configurator is usually associated to an XML tag via some name 
 * transformation. For example, parsing the XML via a SAX parser 
 * by using a {@link DispatcherHandler DispatcherHandler},
 * a Configurator class can be associated to each element, which will
 * receive only SAX events pertaining to that element.
 * </pre>
 * 
 * @author Cristiano Sadun
 */
public interface Configurator extends ContentHandler {
	
	/**
	 * Return the object resulting from the parsing of the specific XML element
	 * handled by this configurator, or <b>null</b>.
	 */
	public Object getConfiguredObject() throws SAXException;
	
	/**
	 * Return the text nodes resulting from the parsing of the specific XML element (if any)
	 * handled by this configurator, or <b>null</b>.
	 */
	public String getText();

	public void startElement(
		String namespaceURI,
		String localName,
		String qName,
		Attributes atts)
		throws SAXException;

	public void endElement(
		String namespaceURI,
		String localName,
		String qName) throws SAXException;
}
