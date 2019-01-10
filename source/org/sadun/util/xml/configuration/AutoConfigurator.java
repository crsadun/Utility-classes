package org.sadun.util.xml.configuration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sadun.util.FieldResolver;
import org.sadun.util.ObjectLister;
import org.sadun.util.TypeWrapper;
import org.xml.sax.SAXException;

/**
 * This configurator automatically binds fields to tags in the HTML.
 * 
 * @author Cristiano Sadun
 */
public abstract class AutoConfigurator extends BaseConfigurator {

	private Set setFields = new HashSet();

	private Set encounteredTags = new HashSet();

	private Set ignorableTags = new HashSet();

	private Set requiredFields = new HashSet();
	private Set requiredFieldsCopy = new HashSet();
	
	protected Field set(Object obj, String tagName) throws SAXException {
		List attemptsList = new ArrayList();

		encounteredTags.add(tagName);

		try {
			Field field = locateField(obj, tagName, attemptsList);
			Object value = TypeWrapper.convertValue(getTextAndResetBuffer(),
					field.getType());
			try {
				boolean accessible = field.isAccessible();
				field.setAccessible(true);
				field.set(obj, value);
				setFields.add(field.getName());
				field.setAccessible(accessible);
				return field;
			} catch (IllegalArgumentException e1) {
				throw new SAXException("Cannot set field " + field.getName()
						+ " of type " + field.getClass() + " in class "
						+ obj.getClass() + " with value " + obj, e1);
			} catch (IllegalAccessException e1) {
				throw new SAXException("Cannot access field " + field.getName()
						+ " of type " + field.getClass() + " in class "
						+ obj.getClass(), e1);
			}
		} catch (NoSuchFieldException e) {
			throw new SAXException(
					"Cannot automatically locate field for tag '"
							+ tagName
							+ " in "
							+ obj.getClass().getName()
							+ "' (attempted "
							+ ObjectLister.getInstance().list(attemptsList)
							+ ") please use setOnTag() or setIgnore() to set esplicitly which field is associated to the tag",
					e);
		}
	}

	public boolean hasFieldBeenSet(String fieldName) {
		return setFields.contains(fieldName);
	}

	public boolean hasTagBeenEncountered(String tag) {
		return encounteredTags.contains(tag);
	}

	public void setMandatory(String fieldName) {
		requiredFields.add(fieldName);
	}
	
	public boolean isMandatory(String fieldName) {
		return requiredFields.contains(fieldName);
	}
	
	public void startDocument() throws SAXException {
		super.startDocument();
		requiredFieldsCopy=new HashSet();
		if (requiredFields.size()>0) {
			requiredFieldsCopy.addAll(requiredFields);
			completed=false;
		} else completed=true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.xml.configuration.BaseConfigurator#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (isIgnorableTag(localName))
			return;
		String fieldName = set(localName).getName();
		if (!completed) {
		 requiredFieldsCopy.remove(fieldName);
		 if (requiredFields.size()==0) completed=true;
		}
	}
	
	public void setIgnore(String tag) {
		ignorableTags.add(tag);
	}

	/**
	 * @param localName
	 * @return
	 */
	public boolean isIgnorableTag(String tag) {
		return ignorableTags.contains(tag);

	}

	/**
	 * @param obj
	 * @param tagName
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	private Field locateField(Object obj, String tagName, List attemptList)
			throws NoSuchFieldException {
		// Simple name transform

		String attempt = tagNameToFieldName1(tagName);
		attemptList.add(attempt);

		return FieldResolver.findField(obj.getClass(), attempt);
	}

	private String tagNameToFieldName1(String tagName) {
		StringBuffer sb = new StringBuffer();

		synchronized (sb) {
			sb.append(Character.toLowerCase(tagName.charAt(0)));
			boolean capitalize = false;
			for (int i = 1; i < tagName.length(); i++) {
				char c = tagName.charAt(i);
				if (c == '-')
					capitalize = true;
				else {
					if (capitalize) {
						capitalize = false;
						sb.append(Character.toUpperCase(c));
					} else {
						sb.append(Character.toLowerCase(c));

					}
				}

			}
		}
		return sb.toString();

	}

	protected Field set(String tagName) throws SAXException {
		return set(this, tagName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.xml.configuration.BaseConfigurator#doGetConfiguredObject()
	 */
	protected abstract Object doGetConfiguredObject() throws SAXException;

}