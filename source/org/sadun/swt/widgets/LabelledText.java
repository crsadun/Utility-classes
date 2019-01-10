/*
 * Created on Sep 5, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * A text with a label attached
 * 
 * @author Cristiano Sadun
 */
public class LabelledText extends Composite {
	
	private Label label;
	private Text text;
	
	public LabelledText(Composite parent, String labelText) {
		this(parent, labelText, SWT.NONE);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public LabelledText(Composite parent, String labelText, int style) {
		super(parent, style);
		
		label = new Label(this, SWT.LEFT|SWT.SHADOW_OUT);
		label.setText(labelText);
		
		text = new Text(this, SWT.SINGLE|SWT.BORDER);
		
		FormLayout fm = new FormLayout();
		fm.marginHeight=2;
		fm.marginWidth=2;
		setLayout(fm);
		FormData fd0 = new FormData();
		fd0.left=new FormAttachment(0,0);
		fd0.right=new FormAttachment(text,0, SWT.DEFAULT);
		fd0.top=new FormAttachment(0 ,0);
		fd0.bottom=new FormAttachment(text, 0, SWT.BOTTOM);
		label.setLayoutData(fd0);
		
		FormData fd1 = new FormData();
		fd1.left=new FormAttachment(label,5, SWT.DEFAULT);
		fd1.right=new FormAttachment(100,0);
		fd1.top=new FormAttachment(0 ,0);
		
		text.setLayoutData(fd1);
		pack();
	}
	
	public Text getTextWidget() { return text; }
	
	public String getText() { return text.getText(); }
	public void setText(String s) { text.setText(s); }
	
	public String getLabelText() { return label.getText(); }
	public void setLabelText(String text) { label.setText(text); }

}
