/*
 * Created on Sep 5, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.swt.widgets;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


/**
 * A text with a label attached
 * 
 * @author Cristiano Sadun
 */
public class LabelledCombo extends Composite {
	
	private String []items;
	private Label label;
	private Combo combo;
	
	public LabelledCombo(Composite parent, String labelText, Collection items) {
		this(parent, labelText, SWT.NONE);
		this.items=new String[items.size()];
		items.toArray(this.items);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public LabelledCombo(Composite parent, String labelText, int style) {
		super(parent, style);
		
		label = new Label(this, SWT.LEFT|SWT.SHADOW_OUT);
		label.setText(labelText);
		
		combo = new Combo(this, SWT.SINGLE|SWT.BORDER);
		
		FormLayout fm = new FormLayout();
		fm.marginHeight=2;
		fm.marginWidth=2;
		setLayout(fm);
		FormData fd0 = new FormData();
		fd0.left=new FormAttachment(0,0);
		fd0.right=new FormAttachment(combo,0, SWT.DEFAULT);
		fd0.top=new FormAttachment(0 ,0);
		fd0.bottom=new FormAttachment(combo, 0, SWT.BOTTOM);
		label.setLayoutData(fd0);
		
		FormData fd1 = new FormData();
		fd1.left=new FormAttachment(label,5, SWT.DEFAULT);
		fd1.right=new FormAttachment(100,0);
		fd1.top=new FormAttachment(0 ,0);
		
		combo.setLayoutData(fd1);
		combo.setItems(items);
		pack();
	}
	
	public Combo getComboWidget() { return combo; }
	
	public String getLabelText() { return label.getText(); }
	public void setLabelText(String text) { label.setText(text); }

	/**
	 * @return
	 */
	public String[] getItems() {
		return combo.getItems();
	}

	/**
	 * @param items
	 */
	public void setItems(String[] items) {
		this.items=items;
		if (combo!=null) combo.setItems(items);
	}

}
