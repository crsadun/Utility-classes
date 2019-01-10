package org.sadun.util.xml.configuration;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A base {@link Configurator Configurator}.
 * <p>
 * This class extends SAX's <tt>DefaultHandler</tt> and provides support
 * for:
 * <p>
 * <ul>
 * <li> collecting text (via the {@link #characters(char[], int, int) 
 *      characters()} method);
 * <li> ensuring that {@link #getConfiguredObject() getConfiguredObject()}
 *      throws an exception if parsing hasn't completed yet (via the
 *      {@link #checkCompleted() checkCompleted()} method).
 * </ul>
 * <p>
 * Subclasses which override {@link #endElement(java.lang.String, 
 * java.lang.String, java.lang.String) endElement()} should either invoke
 * this class' implementation or set to <b>true</b> the protected 
 * <b>{@link #completed completed}</b> field when the configured object
 * is complete.
 * 
 * @author Cristiano Sadun
 */
public abstract class BaseConfigurator extends DefaultHandler implements Configurator {
 
	/**
	 * The buffer collecting text received by {@link #characters(char[], int, int) 
	 * characters()}.
	 */
	protected StringBuffer buffer;
	
	/**
	 * Indicate that the object built from the XML is ready.
	 */
	protected boolean completed;

	/**
	 * Constructor for BaseConfigurator.
	 */
	public BaseConfigurator() {
		this.buffer = new StringBuffer();
		this.completed = false;
	}

	/**
	 * Set {@link #completed completed} to true.
	 */
	public void endElement(
		String namespaceURI,
		String localName,
		String qName) throws SAXException {
		completed = true;
	}

	public final Object getConfiguredObject() throws SAXException {
		checkCompleted();
		return doGetConfiguredObject();
	}

	/**
	 * Throw an IllegalStateException if {@link #completed completed} is not <b>true</b>.
	 */
	protected void checkCompleted() {
		if (!completed)
			throw new IllegalStateException("The object(s) under configuration are not completed yet (possibly endElement has been overriden without invoking its super implementation)");
	}

	protected abstract Object doGetConfiguredObject() throws SAXException;

	
	public void characters(char[] ch, int start, int length)
		throws SAXException {
		buffer.append(ch, start, length);
	}

	/**
	 * Returns the completed.
	 * @return boolean
	 */
	public boolean isCompleted() {
		return completed;
	}

	public final String getText() {
		return buffer.toString();
	}
	
	public final String getTextAndResetBuffer() {
		return getTextAndResetBuffer(true);
	}
	
	public final String getTextAndResetBuffer(boolean trim) {
		String result = buffer.toString();
		if (trim) result=result.trim();
		buffer.delete(0, buffer.length());
		return result;
	}
	
	public String toString() {
		return DispatcherHandler.baseName(getClass().getName());
	}
	
	protected boolean getBooleanText(String localName) throws SAXException {
		String txt = getTextAndResetBuffer().trim();
		if ("true".equals(txt) || "yes".equals(txt))
			return true;
		else if ("false".equals(txt) || "no".equals(txt))
			return false;
		else
			throw new SAXException("Invalid value for element " + localName
					+ " (boolean expected, found '" + txt + "')");
	}

	protected short getShortText(String localName) throws SAXException {
		String txt = getTextAndResetBuffer().trim();
		try {
			return Short.parseShort(txt);
		} catch (NumberFormatException e) {
			throw new SAXException("Invalid value for element " + localName
					+ " (short expected, found '" + txt + "')");
		}
	}
	
	protected int getIntText(String localName) throws SAXException {
		String txt = getTextAndResetBuffer().trim();
		try {
			return Integer.parseInt(txt);
		} catch (NumberFormatException e) {
			throw new SAXException("Invalid value for element " + localName
					+ " (int expected, found '" + txt + "')");
		}
	}
	
	protected long getLongText(String localName) throws SAXException {
		String txt = getTextAndResetBuffer().trim();
		try {
			return Long.parseLong(txt);
		} catch (NumberFormatException e) {
			throw new SAXException("Invalid value for element " + localName
					+ " (long expected, found '" + txt + "')");
		}
	}
	
	protected byte getByteText(String localName) throws SAXException {
		String txt = getTextAndResetBuffer().trim();
		try {
			return Byte.parseByte(txt);
		} catch (NumberFormatException e) {
			throw new SAXException("Invalid value for element " + localName
					+ " (byte expected, found '" + txt + "')");
		}
	}
	protected float getFloatText(String localName) throws SAXException {
		String txt = getTextAndResetBuffer().trim();
		try {
			return Float.parseFloat(txt);
		} catch (NumberFormatException e) {
			throw new SAXException("Invalid value for element " + localName
					+ " (float expected, found '" + txt + "')");
		}
	}
	protected double getDoubleText(String localName) throws SAXException {
		String txt = getTextAndResetBuffer().trim();
		try {
			return Double.parseDouble(txt);
		} catch (NumberFormatException e) {
			throw new SAXException("Invalid value for element " + localName
					+ " (double expected, found '" + txt + "')");
		}
	}
	

}
