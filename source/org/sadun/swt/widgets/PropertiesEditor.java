/*
 * Created on Sep 8, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.swt.widgets;

import java.util.Properties;

import org.eclipse.swt.widgets.Composite;

/**
 * 
 * @author Cristiano Sadun
 */
public class PropertiesEditor extends MapEditor {

	/**
	 * @param parent
	 * @param groupName
	 * @param properties
	 * @param valuesEditable
	 * @param listEditable
	 */
	public PropertiesEditor(
		Composite parent,
		String groupName,
		Properties properties,
		boolean valuesEditable,
		boolean listEditable) {
		super(parent, groupName, properties, valuesEditable, listEditable);
	}

	/**
	 * @param parent
	 * @param groupName
	 * @param properties
	 */
	public PropertiesEditor(
		Composite parent,
		String groupName,
		Properties properties) {
		super(parent, groupName, properties);
	}

	/**
	 * @param parent
	 * @param groupName
	 */
	public PropertiesEditor(Composite parent, String groupName) {
		super(parent, groupName);
	}
	
	public Properties getProperties() { return (Properties)super.getMap(); }

}
