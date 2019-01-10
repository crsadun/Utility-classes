/*
 * Created on Sep 6, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.swt.widgets;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * A class to view/edit a map object and optionally edit its values.
 * 
 * @author Cristiano Sadun
 */
public class MapEditor extends Composite {

	private static int TABLE_INSET_HORIZONTAL = 2;
	private static int TABLE_INSET_VERTICAL = 2;

	/**
			 * This exception can be raised by a {@link TableItemChangeListener} when the changed
			 * value of a property is problematic.
			 * 
			 * @author Cristiano Sadun
			 */
	public static class TableItemChangeException extends Exception {

		/**
		 * Create  a {@link TableItemChangeException}.
		 */
		public TableItemChangeException() {
			super();
		}

		/**
		 * Create  a {@link TableItemChangeException} with the given message.
		 * @param message the message associated to the exception
		 */
		public TableItemChangeException(String message) {
			super(message);
		}

		/**
		 * Create  a {@link TableItemChangeException} with the given message and underlying cause.
		 * @param message the message associated to the exception
		 * @param cause the cause of the exception.
		 */
		public TableItemChangeException(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * Create  a {@link TableItemChangeException} with an underlying cause.
		 * @param cause the cause of the exception.
		 */
		public TableItemChangeException(Throwable cause) {
			super(cause);
		}

	}

	/**
	 * Classes implementing this interface can be notified
	 * of user-driven changes to the the properties displayed in
	 * a {@link MapEditor}. 
	 * 
	 * @author Cristiano Sadun
	 */
	public static interface TableItemChangeListener {

		/**
		 * Invoked to notify that an item in the Map editor table has changed.
		 * 
		 * @param item the item which is changed
		 * @param column
		 * @throws TableItemChangeException
		 */
		public void tableItemChanged(TableItem item, int column)
			throws TableItemChangeException;
		public void tableItemDeleted(TableItem item);
		public void tableItemAdded(TableItem item);
	}

	private Map map;
	private Group group;
	private Table table;
	private Menu rightMenu;
	private MenuItem deleteItem;
	private MenuItem addItem;
	private Color bgColor;
	private java.util.List tableItemChangeListeners = new ArrayList();

	private MouseAdapter rightMenuShowerMouseListener = new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			if (e.button == 3) {
				rightMenu.setVisible(true);
			}
		}
	};

	private EditableSelectionAdapter editableSelectionAdapter =
		new EditableSelectionAdapter(1);

	/**
	 * Create a map editor component.
	 *  
	 * @param parent the SWT parent
	 * @param groupName a descriptive name of the property set (e.g. "System Properties")
	 * @param map the map to display
	 * @param valuesEditable if <b>true</b> the user can change values via the UI
	 * @param listEditable if <b>true</b> the user can add or delete map via the UI
	 */
	public MapEditor(
		Composite parent,
		String groupName,
		Map properties,
		boolean valuesEditable,
		boolean listEditable) {
		super(parent, SWT.NONE);
		this.map = properties;

		setupUI(groupName, parent);
		setValueEditable(valuesEditable);
		setListEditable(listEditable);
		updatePropertiesTable(properties);
	}

	public MapEditor(Composite parent, String groupName, Map properties) {
		this(parent, groupName, properties, true, true);
	}

	public MapEditor(Composite parent, String groupName) {
		this(parent, groupName, new HashMap(), true, true);
	}

	/**
	 * @param properties2
	 */
	private void updatePropertiesTable(Map p) {
		table.removeAll();
		TableColumn c1 = new TableColumn(table, SWT.LEFT, 0);
		c1.setText("Name");
		TableColumn c2 = new TableColumn(table, SWT.LEFT, 1);
		c2.setText("Value");

		SortedSet ss = new TreeSet(p.keySet());

		for (Iterator i = ss.iterator(); i.hasNext();) {
			String propertyName = (String) i.next();
			String propertyValue = p.get(propertyName).toString();
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, propertyName);
			item.setText(1, propertyValue);
		}
	}

	/**
	 * 
	 */
	private void setupUI(String groupName, Composite parent) {

		bgColor = new Color(getDisplay(), 255, 255, 100);

		FillLayout fl = new FillLayout();
		fl.marginHeight = 5;
		fl.marginWidth = 5;
		setLayout(fl);
		group = new Group(this, SWT.NONE);
		group.setText(groupName);

		FillLayout fl2 = new FillLayout();

		fl2.marginHeight = TABLE_INSET_HORIZONTAL;
		fl2.marginWidth = TABLE_INSET_VERTICAL;
		group.setLayout(fl2);

		table = new Table(group, SWT.SINGLE | SWT.FULL_SELECTION);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		table.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {

				if (table.getColumnCount() < 2)
					return;

				int tblWidth = table.getBounds().width;
				int t0w = (int) (tblWidth * 0.3);
				int t1w = tblWidth - t0w;
				table.getColumn(0).setWidth(t0w);
				table.getColumn(1).setWidth(t1w);
			}
		});

		rightMenu = new Menu(this);
		deleteItem = new MenuItem(rightMenu, SWT.CASCADE);
		deleteItem.setText("&Delete property");
		addItem = new MenuItem(rightMenu, SWT.CASCADE);
		addItem.setText("&Add property");
		MenuItem copyItem = new MenuItem(rightMenu, SWT.CASCADE);
		copyItem.setText("&Copy");

		copyItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int x =
					table.getDisplay().getCursorLocation().x
						- getShell().getLocation().x;
				TableColumn[] cols = table.getColumns();
				int p = 0, i;
				String s = "";
				for (i = 0; i < cols.length; i++) {
					p += cols[i].getWidth();
					if (p >= x) {
						TableItem[] item = table.getSelection();
						if (item.length == 0)
							return;
						s = item[0].getText(i);
						break;
					}
				}
				Clipboard cb = new Clipboard(getDisplay());
				TextTransfer tt = TextTransfer.getInstance();
				cb.setContents(new Object[] { s }, new Transfer[] { tt });
				cb.dispose();
			}
		});

		deleteItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selected = table.getSelection();
				for (int i = 0; i < selected.length; i++) {
					notifyTableItemDeletion(selected[i]);
					selected[i].dispose();
				}
			}
		});

		addItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selected = table.getSelection();
				String name;
				if (selected.length == 0)
					name = InputBox.prompt(getShell(), "New property name");
				else
					name =
						InputBox.prompt(
							getShell(),
							getShell().getBounds().x
								+ selected[0].getBounds(0).x,
							getShell().getBounds().y
								+ selected[0].getBounds(0).y,
							"New property name");
				if (name == null)
					return;
				int pos = findIndexForNewItem(name, 0);

				TableItem item = new TableItem(table, SWT.NONE, pos);
				item.setText(0, name);
				table.select(pos);
				notifyTableItemAddition(item);
			}
		});

		addTableItemChangeListener(new TableItemChangeListener() {
			public void tableItemChanged(TableItem item, int column)
				throws TableItemChangeException {
				String name = item.getText(0);
				String value = item.getText(1);

				Object originalObj = map.get(name);
				if (originalObj == null || originalObj instanceof String)
					map.put(name, value);
				else {
					try {
						// Try to build an instance from the string
						Constructor ctor =
							originalObj.getClass().getConstructor(
								new Class[] { String.class });
					} catch (SecurityException e) {
						throw new TableItemChangeException(e);
					} catch (NoSuchMethodException e) {
						throw new TableItemChangeException(e);
					}
				}
				//System.out.println("Set " + name + " to " + value);
			}

			public void tableItemDeleted(TableItem item) {
				String name = item.getText(0);
				map.remove(name);
				//System.out.println("Removed " + name);
			}

			public void tableItemAdded(TableItem item) {
				String name = item.getText(0);
				String value = item.getText(1);
				map.put(name, value);
				//System.out.println("Added " + name);
			}

		});

		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				table.dispose();
				group.dispose();
				rightMenu.dispose();
				bgColor.dispose();
			}
		});
	}

	/**
	 * @param name
	 * @return
	 */
	private int findIndexForNewItem(String name, int column) {
		TableItem[] items = table.getItems();
		if (name == null)
			return items.length;
		for (int i = 0; i < items.length; i++)
			if (items[i].getText(column).compareTo(name) > 0)
				return i;
		return items.length;
	}

	/**
	 * Allow ore deny editing of property values.
	 * 
	 * @param editable if <b>true</b> property values will be editable. 
	 */
	public void setValueEditable(boolean editable) {
		if (editable)
			table.addSelectionListener(editableSelectionAdapter);
		else
			table.removeSelectionListener(editableSelectionAdapter);
	}

	/**
	 * Allow ore deny editing of property set - adding or deleting properties.
	 * 
	 * @param editable if <b>true</b> property set will be editable. 
	 */
	public void setListEditable(boolean editable) {
		if (editable) {
			table.addMouseListener(rightMenuShowerMouseListener);
		} else {
			table.removeMouseListener(rightMenuShowerMouseListener);
		}

	}

	/**
	 * Add a {@link TableItemChangeListener} to the listener set.
	 * @param l the {@link TableItemChangeListener} to add.
	 */
	public void addTableItemChangeListener(TableItemChangeListener l) {
		tableItemChangeListeners.add(l);
	}
	/**
	 * Remove a {@link TableItemChangeListener} from the listener set.
	 * @param l the {@link TableItemChangeListener} to remove.
	 */
	public void removeTableItemChangeListener(TableItemChangeListener l) {
		tableItemChangeListeners.remove(l);
	}

	private void notifyTableItemChange(TableItem item, int column)
		throws TableItemChangeException {
		for (Iterator i = tableItemChangeListeners.iterator(); i.hasNext();) {
			TableItemChangeListener l = (TableItemChangeListener) i.next();
			l.tableItemChanged(item, column);
		}
	}

	private void notifyTableItemDeletion(TableItem item) {
		for (Iterator i = tableItemChangeListeners.iterator(); i.hasNext();) {
			TableItemChangeListener l = (TableItemChangeListener) i.next();
			l.tableItemDeleted(item);
		}
	}

	private void notifyTableItemAddition(TableItem item) {
		for (Iterator i = tableItemChangeListeners.iterator(); i.hasNext();) {
			TableItemChangeListener l = (TableItemChangeListener) i.next();
			l.tableItemAdded(item);
		}
	}

	/**
	 * A selection adapter which allows editing of a table item
	 * via double click.
	 */
	class EditableSelectionAdapter extends SelectionAdapter {

		private int column;

		EditableSelectionAdapter(int column) {
			this.column = column;

		}

		class EditableListener
			extends FocusAdapter
			implements SelectionListener {

			private Text text;
			private TableItem itemToUpdate;
			private Shell tempShell;

			EditableListener(
				Shell tempShell,
				Text text,
				TableItem itemToUpdate) {
				this.text = text;
				this.itemToUpdate = itemToUpdate;
				this.tempShell = tempShell;
			}
			public void focusLost(FocusEvent e) {
				updateMapElement();
				tempShell.dispose();
			}

			private void updateMapElement() {
				try {
					itemToUpdate.setText(column, text.getText());
					notifyTableItemChange(itemToUpdate, column);
					text.dispose();
					text = null;
				} catch (TableItemChangeException e1) {
					MessageBox mb =
						new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
					mb.setMessage(
						"It is not possible to create a new object based on the given string ("
							+ e1.getCause().getMessage()
							+ ")");
				}

			}

			public void widgetDefaultSelected(SelectionEvent e) {
				updateMapElement();
				tempShell.dispose();
			}

			public void widgetSelected(SelectionEvent e) {
				// Do nothing
			}

		}

		public void widgetDefaultSelected(SelectionEvent e) {
			int selected = table.getSelectionIndex();
			String currentValue = table.getItem(selected).getText(1);

			int x = table.getColumn(0).getWidth() - 4;
			int y =
				2
					+ TABLE_INSET_VERTICAL
					+ (selected - table.getTopIndex()) * table.getItemHeight();
			int width = table.getColumn(1).getWidth();
			if (table.getVerticalBar() != null)
				width -= table.getVerticalBar().getSize().x;
			int height = table.getItemHeight();

			// PROBLEM: SWT apparently disposes of the parent shell when ESC is pressed
			// on a text box whose parent is the table. Dont know if it's bug or
			// I'm not understanding what happens well enough.
			// FIX: create a temporary shell
			Shell tempShell = new Shell(table.getShell(), SWT.NO_TRIM);

			TableItem item = table.getItem(selected);
			Point p =
				table.toDisplay(table.getLocation().x, table.getLocation().y);
			tempShell.setBounds(p.x + x, p.y + y, width, height);
			Text text = new Text(tempShell, SWT.SINGLE | SWT.LEFT);
			text.setBounds(0, 0, width, height);
			text.setBackground(bgColor);
			text.setText(currentValue);
			text.setSelection(0, currentValue.length());
			text.moveAbove(null);

			EditableListener el =
				new EditableListener(tempShell, text, table.getItem(selected));
			text.addFocusListener(el);
			text.addSelectionListener(el);

			tempShell.open();
			text.forceFocus();
		}
	}

	/**
	 * @return
	 */
	public Map getMap() {
		return map;
	}

	/**
	 * @param map
	 */
	public void setProperties(Properties properties) {
		this.map = properties;
		updatePropertiesTable(properties);
	}

	public static Properties open(
		Shell parent,
		int x,
		int y,
		Properties properties) {
		return new PropertiesEditorDialog(parent, x, y, properties).open();

	}

	public static Properties open(Shell parent, Properties properties) {
		return open(
			parent,
			parent.getLocation().x + 10,
			parent.getLocation().y + 10,
			properties);
	}

	static class PropertiesEditorDialog extends Dialog {

		private Properties properties;
		private int x, y;

		PropertiesEditorDialog(
			Shell parent,
			int x,
			int y,
			Properties properties) {
			super(parent);
			this.properties = properties;
			this.x = x;
			this.y = y;
		}

		public Properties open() {
			Shell parent = getParent();
			final Shell shell =
				new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			shell.setLocation(x, y);
			shell.setText(getText());
			shell.setLayout(new FillLayout());

			MapEditor editor = new MapEditor(shell, "Properties", properties);

			//shell.pack();
			//shell.setSize(shell.getSize().x + 40, shell.getSize().y);
			shell.open();
			Display display = parent.getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			return properties;
		}

	}

	public static void main(String[] args) {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Test of MapEditor with System properties");

		Properties p = MapEditor.open(shell, System.getProperties());

		p.list(System.out);
	}

}
