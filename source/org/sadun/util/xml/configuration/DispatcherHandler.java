package org.sadun.util.xml.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sadun.util.Setup;
import org.xml.sax.Attributes;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <a name="desc">
 * A SAX handler which dispatches events to subclasses implementing the
 * {@link Configurator Configurator} interface. 
 * <p>
 * Dispatching can be set by tree depth level (using {@link #setDispatchLevel(int) 
 * setDispatchLevel()}) or by namespaceURI/tag combination (using 
 * {@link #setToDispatch(java.lang.String, java.lang.String)}.
 * <p>
 * {@link Configurator Configurator} classes are located automatically in a package
 * whose name is set at {@link #DispatcherHandler(java.lang.String) construction} 
 * and their name must match a transformation the element tag obtained by the 
 * following rules:
 * <p>
 * <ul>
 * <li> The tag name is altered so that 
 *      <p>
 *      <ul>
 *      <li>the first letter is capitalized;
 * 		<li>dashes  are eliminated and the following letter 
 *          capitalized.
 * 		</ul>
 *      <p>
 *      For example <tt><b>&lt;my-tag&gt;</b></tt> becomes <b>MyTag</b>.
 * 		<p>
 * <li> If the so transformed tag ends with <b>Configuration</b>, this part is stripped.
 *      <p>
 *      For example, <tt><b>MyTagConfiguration</b></tt> becomes also <b>MyTag</b>
 * 		<p>
 * <li> If the {@link #DispatcherHandler(java.lang.String, java.lang.String) two-parameters 
 *      constructor} is used to create the DispatcherHandler, the given postfix 
 *      is concatenated to the name.
 * 		<p>
 * 		For example, if the postfix is <b>Configurator</b>, the final result for
 *      a tag <tt><b>&lt;my-tag&gt;</b></tt> will be the class name <b>MyTagConfigurator</b>.
 * </ul>
 * <p>
 * The class works via a <i>current configurator</i> (initially null).
 * <p>
 * Whenever a dispatching occurs, the {@link com.xtractor.core.xspace.configurators.Configurator
 * Configurator} class found in the package name given at constructor is instantiated and 
 * control is sent to that instance, which becomes the current configurator.
 * <p>
 * When the level/element for which the dispatch was enabled is terminated, the method {@link #configuratorReady(java.lang.String, java.lang.String,
 * java.lang.String, com.xtractor.core.xspace.configurators.Configurator) configuratorReady()} 
 * (which must be implemented by the class extending this one) is invoked, and the previous configurator
 * is reinstated as "current".
 * <p>
 * Otherwise, the current configurator (if any) keeps receiving the SAX events.
 * 
 * @author Cristiano Sadun
 * </a>
 */
public abstract class DispatcherHandler extends DefaultHandler {

	private String packageName;
	private String postfix;
	private Stack elementsStack = new Stack();
	private Configurator currentConfigurator = null;

	private Map dispatcherMap = new HashMap();
	private Set dispatchLevels = new HashSet();

	private Stack configuratorsStack = new Stack();
	
	private Setup setupObject;

	/**
	 * Constructor for DispatcherHandler. Indicates that the {@link Configurator Configurator}
	 * classes will be searched for in the given package (see <a href="#desc">class description</a>).
	 * @param packageName the name of the package where to attempt to locate the
	 *         necessary {@link Configurator Configurator} classes.
	 * @param postfix a fixed postfix which will be added to the {@link Configurator Configurator} 
	 *         class name (see <a href="#desc">class description</a>).
	 */
	protected DispatcherHandler(Setup setup, String packageName, String postfix) {
		this.setupObject=setup;
		this.packageName = packageName;
		this.postfix = postfix;
	}
	
	protected DispatcherHandler(String packageName, String postfix) {
		this.setupObject=null;
		this.packageName = packageName;
		this.postfix = postfix;
	}

	/**
	 * Constructor for DispatcherHandler. Indicates that the {@link Configurator Configurator}
	 * classes will be searched for in the given package (see <a href="#desc">class description</a>).
	 * @param packageName the name of the package where to attempt to locate the
	 *         necessary {@link Configurator Configurator} classes.
	 */
	protected DispatcherHandler(String packageName) {
		this(packageName, "");
	}

	/**
	 * Indicate that the given namespace URI/local name combination
	 * is associated to a Configurator.
	 */
	protected void setToDispatch(String namespaceURI, String localName) {
		dispatcherMap.put(
			namespaceURI + ":" + localName,
			makeConfiguratorClassNameFromQName(localName));
	}

	/**
	 * Sets wether or not every element at a given depth level in the XML
	 * tree is associated to a Configurator.
	 * @param level the depth level in the XML tree
	 * @param toDispath, if <b>true</b>, every element at that level
	 *                    will require its own Configurator
	 */
	protected void setDispatchLevel(int level, boolean toDispatch) {
		if (toDispatch)
			dispatchLevels.add(new Integer(level));
		else
			dispatchLevels.remove(new Integer(level));
	}

	/**
	 * Indicates that every element at a given depth level in the XML
	 * tree is associated to a Configurator.
	 * @param level the depth level in the XML tree
	 */
	protected void setDispatchLevel(int level) {
		setDispatchLevel(level, true);
	}

	private String makeConfiguratorClassNameFromQName(String qName) {
		String tag = getTagFromQName(qName);
		StringBuffer sb = new StringBuffer();
		synchronized(sb) {
		boolean doChange = true;
		for (int i = 0; i < qName.length(); i++) {
			char c = tag.charAt(i);
			if (doChange) {
				sb.append(Character.toUpperCase(c));
				doChange = false;
			} else if (c == '-')
				doChange = true;
			else
				sb.append(c);
		}
		tag = sb.toString();
		}
		if (tag.endsWith("Configuration"))
			tag = tag.substring(0, tag.length() - 13);
		return packageName + "." + tag + postfix;
	}

	private String getTagFromQName(String qName) {
		int i = qName.indexOf(":");
		String tag;
		if (i == -1)
			tag = qName;
		else
			tag = qName.substring(i + 1);
		return tag;
	}

	/**
	 * Check whether the namespace/tag are associated to a configurator
	 * or we're at a dispatch level. 
	 * <p> 
	 * If yes, instantiate a new associated {@link Configurator Configurator}. 
	 * and invoke its {@link Configurator#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes) 
	 * startElement()} method.
	 * <p>
	 * If no, invoke the
	 * {@link Configurator#startElement(java.lang.String, java.lang.String,
	 * java.lang.String, org.xml.sax.Attributes) startElement()} method
	 * of the current {@link Configurator Configurator} (if any).
	 */
	public final void startElement(
		String namespaceURI,
		String localName,
		String qName,
		Attributes atts)
		throws SAXException {

		elementsStack.push(qName);

		if (dispatchLevels.contains(new Integer(elementsStack.size()))
			|| hasConfigurator(namespaceURI, localName)) {
			String clsName = makeConfiguratorClassNameFromQName(qName);

			try {
				Class configuratorCls = Class.forName(clsName);
				
				if (! Configurator.class.isAssignableFrom(configuratorCls))
					throw new SAXException("Internal error: the class "+clsName+" does not implement the Configurator interface");
				
				try {
					currentConfigurator =
						(Configurator) configuratorCls.newInstance();
					if (setupObject!=null) setupObject.setup(currentConfigurator);
					configuratorsStack.push(currentConfigurator);
				} catch (InstantiationException e) {
					throw new SAXException(
						"Could not instantiate XSpace configurator object",
						e);
				} catch (IllegalAccessException e) {
					throw new SAXException(
						"Could not access XSpace configurator object",
						e);
				}
			} catch (ClassNotFoundException e) {
				throw new SAXException(
					qName
						+ " configuration element not handled (class "
						+ clsName
						+ " not found");
			}

		}

		if (currentConfigurator != null)
			currentConfigurator.startElement(
				namespaceURI,
				localName,
				qName,
				atts);

	}

	public final void characters(char[] ch, int start, int length)
		throws SAXException {
		if (currentConfigurator != null)
			currentConfigurator.characters(ch, start, length);

	}

	public final void endElement(
		String namespaceURI,
		String localName,
		String qName)
		throws SAXException {

		if (elementsStack.size() > 0) 
			elementsStack.pop();
		if (currentConfigurator == null)
			return;
		currentConfigurator.endElement(namespaceURI, localName, qName);

		if (dispatchLevels.contains(new Integer(elementsStack.size()))
			|| hasConfigurator(namespaceURI, localName)) {
			configuratorReady(
				namespaceURI,
				localName,
				qName,
				currentConfigurator);
			if (configuratorsStack.size()==0) currentConfigurator=null;
			else currentConfigurator = (Configurator) configuratorsStack.pop();
		}
	}

	static String baseName(String clsName) {
		int i = clsName.lastIndexOf(".");
		if (i == -1)
			return clsName;
		return clsName.substring(i + 1);
	}

	/**
	 * Method hasConfigurator.
	 * @param namespaceURI
	 * @param localName
	 * @return boolean
	 */
	private boolean hasConfigurator(String namespaceURI, String localName) {
		return dispatcherMap.containsKey(namespaceURI + ":" + localName);
	}

	protected abstract void configuratorReady(
		String namespaceURI,
		String localName,
		String qName,
		Configurator configurator)
		throws SAXException;
	/**
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		if (currentConfigurator != null)
			currentConfigurator.endDocument();
	}

	/**
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(String)
	 */
	public void endPrefixMapping(String prefix) throws SAXException {
		if (currentConfigurator != null)
			currentConfigurator.endPrefixMapping(prefix);
	}

	/**
	 * @see org.xml.sax.ErrorHandler#error(SAXParseException)
	 */
	public void error(SAXParseException exception) throws SAXException {
		if (currentConfigurator != null
			&& currentConfigurator instanceof ErrorHandler)
			 ((ErrorHandler) currentConfigurator).error(exception);
	}

	/**
	 * @see org.xml.sax.ErrorHandler#fatalError(SAXParseException)
	 */
	public void fatalError(SAXParseException exception) throws SAXException {
		if (currentConfigurator != null
			&& currentConfigurator instanceof ErrorHandler)
			 ((ErrorHandler) currentConfigurator).fatalError(exception);
	}

	/**
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] ch, int start, int length)
		throws SAXException {
		if (currentConfigurator != null)
			currentConfigurator.ignorableWhitespace(ch, start, length);
	}

	/**
	 * @see org.xml.sax.DTDHandler#notationDecl(String, String, String)
	 */
	public void notationDecl(String name, String publicId, String systemId)
		throws SAXException {
		if (currentConfigurator != null
			&& currentConfigurator instanceof DTDHandler)
			((DTDHandler) currentConfigurator).notationDecl(
				name,
				publicId,
				systemId);
	}

	/**
	 * @see org.xml.sax.ContentHandler#processingInstruction(String, String)
	 */
	public void processingInstruction(String target, String data)
		throws SAXException {
		if (currentConfigurator != null)
			currentConfigurator.processingInstruction(target, data);
	}

	/**
	 * @see org.xml.sax.EntityResolver#resolveEntity(String, String)
	 */
	public InputSource resolveEntity(String publicId, String systemId)
		throws SAXException {
		if (currentConfigurator != null
			&& currentConfigurator instanceof EntityResolver)
			try {
				return ((EntityResolver) currentConfigurator).resolveEntity(
					publicId,
					systemId);
			} catch (IOException e) {
				e.printStackTrace();
				return super.resolveEntity(publicId, systemId);
			} else
			return super.resolveEntity(publicId, systemId);
	}

	/**
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
	 */
	public void setDocumentLocator(Locator locator) {
		if (currentConfigurator != null)
			currentConfigurator.setDocumentLocator(locator);
	}

	/**
	 * @see org.xml.sax.ContentHandler#skippedEntity(String)
	 */
	public void skippedEntity(String name) throws SAXException {
		if (currentConfigurator != null)
			currentConfigurator.skippedEntity(name);
	}

	/**
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		if (currentConfigurator != null)
			currentConfigurator.startDocument();
	}

	/**
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(String, String)
	 */
	public void startPrefixMapping(String prefix, String uri)
		throws SAXException {
		if (currentConfigurator != null)
			currentConfigurator.startPrefixMapping(prefix, uri);
	}

	/**
	 * @see org.xml.sax.DTDHandler#unparsedEntityDecl(String, String, String, String)
	 */
	public void unparsedEntityDecl(
		String name,
		String publicId,
		String systemId,
		String notationName)
		throws SAXException {
		if (currentConfigurator != null
			&& currentConfigurator instanceof DTDHandler)
			((DTDHandler) currentConfigurator).unparsedEntityDecl(
				name,
				publicId,
				systemId,
				notationName);
	}

	/**
	 * @see org.xml.sax.ErrorHandler#warning(SAXParseException)
	 */
	public void warning(SAXParseException exception) throws SAXException {
		if (currentConfigurator != null
			&& currentConfigurator instanceof ErrorHandler)
			 ((ErrorHandler) currentConfigurator).warning(exception);
	}

	/**
	 * Returns the setupObject.
	 * @return Setup
	 */
	public Setup getSetupObject() {
		return setupObject;
	}

	/**
	 * Sets the setupObject.
	 * @param setupObject The setupObject to set
	 */
	public void setSetupObject(Setup setupObject) {
		this.setupObject = setupObject;
	}

}
