/*
 * Created on Sep 6, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author Cristiano Sadun
 */
public class InputBox extends Composite {

	private Text text;

	/**
	 * @param parent
	 * @param style
	 */
	public InputBox(Composite parent, String prompt, String defaultText) {
		super(parent, SWT.NONE);
		setupUI(prompt, defaultText, parent);

	}

	public InputBox(Composite parent, String prompt) {
		this(parent, prompt, "");
	}

	private void setupUI(String prompt, String defaultText, Composite parent) {
		FillLayout fl = new FillLayout();
		fl.marginHeight = 5;
		fl.marginWidth = 5;
		setLayout(fl);
		Group group = new Group(this, SWT.NONE);
		group.setText(prompt);

		FillLayout fl2 = new FillLayout();

		fl2.marginHeight = 2;
		fl2.marginWidth = 2;
		group.setLayout(fl2);

		text = new Text(group, SWT.HORIZONTAL | SWT.SINGLE | SWT.BORDER);
		text.setText(defaultText);
	}

	public String getText() {
		return text.getText();
	}

	public static String prompt(
		Shell parent,
		int x,
		int y,
		String prompt,
		String defaultText) {
		InputBoxDialog dlg = new InputBoxDialog(parent, x, y, prompt, defaultText);
		return dlg.open();
	}

	public static String prompt(Shell parent, int x, int y, String prompt) {
		return prompt(parent, x, y, prompt, "");
	}

	public static String prompt(
		Shell parent,
		String prompt,
		String defaultText) {
		return prompt(
			parent,
			parent.getSize().x / 2,
			parent.getSize().y / 2,
			prompt,
			defaultText);
	}

	public static String prompt(Shell parent, String prompt) {
		return prompt(parent, prompt, "");
	}

	private static class InputBoxDialog extends Dialog {

		private String prompt;
		private String defaultText;
		private int x,y;

		InputBoxDialog(Shell parent, int x, int y, String prompt, String defaultText) {
			super(parent);
			this.prompt = prompt;
			this.defaultText = defaultText;
			this.x=x;
			this.y=y;
		}

		public String open() {
			Shell parent = getParent();
			final Shell shell = new Shell(parent, SWT.PRIMARY_MODAL);
			shell.setLocation(x, y);
			shell.setText(getText());
			shell.setLayout(new FillLayout());
			final InputBox ib = new InputBox(shell, prompt, defaultText);

			final String[] result = new String[1];

			ib.text.addSelectionListener(new SelectionAdapter() {
				public void widgetDefaultSelected(SelectionEvent e) {
					result[0] = ib.getText();
					shell.dispose();
				}
			});

			shell.pack();
			shell.setSize(shell.getSize().x + 40, shell.getSize().y);
			shell.open();
			Display display = parent.getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			return result[0];
		}

	}

}
