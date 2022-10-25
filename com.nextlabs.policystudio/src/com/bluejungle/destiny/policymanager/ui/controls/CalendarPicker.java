package com.bluejungle.destiny.policymanager.ui.controls;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TypedListener;

import com.bluejungle.destiny.policymanager.util.ResourceManager;

public final class CalendarPicker extends Composite {
	private Label text;
	private DateTime dateCtl;
	private Shell popup;
	private Button arrow;
	private boolean hasFocus;
	private Listener listener, filter;
	private Color foreground, background;
	private Font font;
	private Format format = new SimpleDateFormat("MM/dd/yyyy"); //$NON-NLS-1$
	private Calendar calendar;

	/**
	 * Constructs a new instance of this class given its parent and a style
	 * value describing its behavior and appearance.
	 * 
	 * @param parent
	 *            a widget which will be the parent of the new instance (cannot
	 *            be null)
	 * @param style
	 *            the style of widget to construct
	 */
	public CalendarPicker(Composite parent, int style) {
		super(parent, style = checkStyle(style));
		int textStyle = SWT.SINGLE;// | SWT.READ_ONLY;
		if ((style & SWT.FLAT) != 0) {
			textStyle |= SWT.FLAT;
		}
		text = new Label(this, textStyle | SWT.READ_ONLY);
		text.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));

		int arrowStyle = SWT.ARROW | SWT.DOWN;
		if ((style & SWT.FLAT) != 0) {
			arrowStyle |= SWT.FLAT;
		}
		arrow = new Button(this, arrowStyle);

		listener = new Listener() {
			public void handleEvent(Event event) {
				if (popup == event.widget) {
					popupEvent(event);
					return;
				}
				if (dateCtl == event.widget) {
					dateEvent(event);
					return;
				}
				if (arrow == event.widget) {
					arrowEvent(event);
					return;
				}
				if (CalendarPicker.this == event.widget) {
					comboEvent(event);
					return;
				}
				if (getShell() == event.widget) {
					handleFocus(SWT.FocusOut);
				}
			}
		};
		filter = new Listener() {
			public void handleEvent(Event event) {
				Shell shell = ((Control) event.widget).getShell();
				if (shell == CalendarPicker.this.getShell()) {
					handleFocus(SWT.FocusOut);
				}
			}
		};

		int[] comboEvents = { SWT.Dispose, SWT.Move, SWT.Resize };
		for (int i = 0; i < comboEvents.length; i++)
			this.addListener(comboEvents[i], listener);

		int[] arrowEvents = { SWT.Selection, SWT.FocusIn };
		for (int i = 0; i < arrowEvents.length; i++)
			arrow.addListener(arrowEvents[i], listener);

		createPopup();
	}

	static int checkStyle(int style) {
		int mask = SWT.BORDER | SWT.READ_ONLY | SWT.FLAT;
		return style & mask;
	}

	public void setFormat(Format format) {
		this.format = format;
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the user changes the receiver's selection, by sending it one of the
	 * messages defined in the <code>SelectionListener</code> interface.
	 * <p>
	 * <code>widgetSelected</code> is called when the combo's list selection
	 * changes. <code>widgetDefaultSelected</code> is typically called when
	 * ENTER is pressed the combo's area.
	 * </p>
	 * 
	 * @param listener
	 *            the listener which should be notified when the user changes
	 *            the receiver's selection
	 */
	public void addSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
		addListener(SWT.DefaultSelection, typedListener);
	}

	void arrowEvent(Event event) {
		switch (event.type) {
		case SWT.FocusIn: {
			handleFocus(SWT.FocusIn);
			break;
		}
		case SWT.Selection: {
			dropDown(!isDropped());
			if (text.getText().length() == 0) {
				if (calendar == null) {
					calendar = new GregorianCalendar();
				}
				calendar.set(Calendar.YEAR, dateCtl.getYear());
				calendar.set(Calendar.MONTH, dateCtl.getMonth());
				calendar.set(Calendar.DAY_OF_MONTH, dateCtl.getDay());
				text.setText(format.format(calendar));
				Event e = new Event();
				e.time = event.time;
				e.stateMask = event.stateMask;
				e.doit = event.doit;
				notifyListeners(SWT.Selection, e);
			}
			break;
		}
		}
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
		if (text.isDisposed()) {
			return;
		}
		if (calendar == null) {
			text.setText("");
		} else {
			text.setText(format.format(calendar.getTime()));
		}
	}

	public Calendar getCalendar() {
		return calendar;
	}

	void comboEvent(Event event) {
		switch (event.type) {
		case SWT.Dispose:
			if (popup != null && !popup.isDisposed()) {
				dateCtl.removeListener(SWT.Dispose, listener);
				popup.dispose();
			}
			Shell shell = getShell();
			shell.removeListener(SWT.Deactivate, listener);
			Display display = getDisplay();
			display.removeFilter(SWT.FocusIn, filter);
			popup = null;
			text = null;
			dateCtl = null;
			arrow = null;
			break;
		case SWT.Move:
			dropDown(false);
			break;
		case SWT.Resize:
			internalLayout(false);
			break;
		}
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		int width = 0, height = 0;

		Point textSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
		Point arrowSize = arrow.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
		Point listSize = dateCtl.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
		int borderWidth = getBorderWidth();

		height = Math.max(textSize.y, arrowSize.y);
		width = listSize.x;
		if (wHint != SWT.DEFAULT)
			width = wHint;
		if (hHint != SWT.DEFAULT)
			height = hHint;
		return new Point(width + 2 * borderWidth, height + 2 * borderWidth);
	}

	void createPopup() {
		// create shell and dateTime control
		popup = new Shell(getShell(), SWT.NO_TRIM | SWT.ON_TOP);
		int style = getStyle();
		int dateStyle = SWT.CALENDAR;
		if ((style & SWT.FLAT) != 0)
			dateStyle |= SWT.FLAT;
		dateCtl = new DateTime(popup, dateStyle);
		if (font != null)
			dateCtl.setFont(font);
		if (foreground != null)
			dateCtl.setForeground(foreground);
		if (background != null)
			dateCtl.setBackground(background);

		int[] popupEvents = { SWT.Close, SWT.Paint, SWT.Deactivate };
		for (int i = 0; i < popupEvents.length; i++) {
			popup.addListener(popupEvents[i], listener);
		}
		int[] dateEvents = { SWT.Selection, SWT.KeyDown, SWT.FocusIn,
				SWT.Dispose };
		for (int i = 0; i < dateEvents.length; i++) {
			dateCtl.addListener(dateEvents[i], listener);
		}
	}

	void dropDown(boolean drop) {
		if (drop == isDropped()) {
			return;
		}
		if (!drop) {
			popup.setVisible(false);
			if (!isDisposed() && arrow.isFocusControl()) {
				text.setFocus();
			}
			return;
		}

		if (getShell() != popup.getParent()) {
			dateCtl.removeListener(SWT.Dispose, listener);
			popup.dispose();
			popup = null;
			dateCtl = null;
			createPopup();
		}

		Point dateSize = dateCtl.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
		dateCtl.setBounds(1, 1, dateSize.x, dateSize.y);
		if (calendar != null) {
			dateCtl.setYear(calendar.get(Calendar.YEAR));
			dateCtl.setMonth(calendar.get(Calendar.MONTH));
			dateCtl.setDay(calendar.get(Calendar.DAY_OF_MONTH));
		}

		Display display = getDisplay();
		Rectangle dateRect = dateCtl.getBounds();
		Rectangle parentRect = display.map(getParent(), null, getBounds());
		Point comboSize = getSize();
		Rectangle displayRect = getMonitor().getClientArea();
		int width = Math.max(comboSize.x, dateRect.width + 2);
		int height = dateRect.height + 2;
		int x = parentRect.x;
		int y = parentRect.y + comboSize.y;
		if (y + height > displayRect.y + displayRect.height) {
			y = parentRect.y - height;
		}
		if (x + width > displayRect.x + displayRect.width) {
			x = displayRect.x + displayRect.width - dateRect.width;
		}
		popup.setBounds(x, y, width, height);
		popup.setVisible(true);
		dateCtl.setFocus();
	}

	@Override
	public Control[] getChildren() {
		checkWidget();
		return new Control[0];
	}

	/**
	 * Returns a string containing a copy of the contents of the receiver's text
	 * field.
	 * 
	 * @return the receiver's text
	 */
	public String getText() {
		checkWidget();
		return text.getText();
	}

	/**
	 * Returns the height of the receivers's text field.
	 * 
	 * @return the text height
	 */
	public int getTextHeight() {
		checkWidget();
		return text.getBounds().height;
	}

	void handleFocus(int type) {
		if (isDisposed()) {
			return;
		}
		switch (type) {
		case SWT.FocusIn: {
			if (hasFocus) {
				return;
			}
			hasFocus = true;
			Shell shell = getShell();
			shell.removeListener(SWT.Deactivate, listener);
			shell.addListener(SWT.Deactivate, listener);
			Display display = getDisplay();
			display.removeFilter(SWT.FocusIn, filter);
			display.addFilter(SWT.FocusIn, filter);
			Event e = new Event();
			notifyListeners(SWT.FocusIn, e);
			break;
		}
		case SWT.FocusOut: {
			if (!hasFocus) {
				return;
			}
			Control focusControl = getDisplay().getFocusControl();
			if (focusControl == arrow || focusControl == dateCtl
					|| focusControl == text) {
				return;
			}
			hasFocus = false;
			Shell shell = getShell();
			shell.removeListener(SWT.Deactivate, listener);
			Display display = getDisplay();
			display.removeFilter(SWT.FocusIn, filter);
			Event e = new Event();
			notifyListeners(SWT.FocusOut, e);
			break;
		}
		}
	}

	boolean isDropped() {
		return popup.getVisible();
	}

	@Override
	public boolean isFocusControl() {
		checkWidget();
		if (text.isFocusControl() || arrow.isFocusControl()
				|| dateCtl.isFocusControl() || popup.isFocusControl()) {
			return true;
		}
		return super.isFocusControl();
	}

	void internalLayout(boolean changed) {
		if (isDropped()) {
			dropDown(false);
		}
		Rectangle rect = getClientArea();
		int width = rect.width;
		int height = rect.height;
		Point arrowSize = arrow.computeSize(SWT.DEFAULT, height, changed);
		text.setBounds(0, 0, width - arrowSize.x, height);
		arrow.setBounds(width - arrowSize.x, 0, arrowSize.x, arrowSize.y);
	}

	void dateEvent(Event event) {
		switch (event.type) {
		case SWT.Dispose:
			if (getShell() != popup.getParent()) {
				popup = null;
				dateCtl = null;
				createPopup();
			}
			break;
		case SWT.FocusIn: {
			handleFocus(SWT.FocusIn);
			break;
		}
		case SWT.Selection: {
			if (calendar == null) {
				calendar = new GregorianCalendar();
			}
			calendar.set(Calendar.YEAR, dateCtl.getYear());
			calendar.set(Calendar.MONTH, dateCtl.getMonth());
			calendar.set(Calendar.DAY_OF_MONTH, dateCtl.getDay());

			text.setText(format.format(calendar.getTime()));

			Event e = new Event();
			e.time = event.time;
			e.stateMask = event.stateMask;
			e.doit = event.doit;
			notifyListeners(SWT.Selection, e);
			break;
		}

		case SWT.KeyDown: {
			if (event.character == SWT.ESC) {
				// Escape key cancels popup list
				dropDown(false);
			}
			if ((event.stateMask & SWT.ALT) != 0
					&& (event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN)) {
				dropDown(false);
			}
			if (event.character == SWT.CR) {
				// Enter causes default selection
				dropDown(false);
				Event e = new Event();
				e.time = event.time;
				e.stateMask = event.stateMask;
				notifyListeners(SWT.DefaultSelection, e);
			}

			if (isDisposed()) {
				break;
			}
			Event e = new Event();
			e.time = event.time;
			e.character = event.character;
			e.keyCode = event.keyCode;
			e.stateMask = event.stateMask;
			notifyListeners(SWT.KeyDown, e);
			break;
		}
		}
	}

	void popupEvent(Event event) {
		switch (event.type) {
		case SWT.Paint:
			// draw black rectangle around list
			Rectangle dateRect = dateCtl.getBounds();
			Color black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
			event.gc.setForeground(black);
			event.gc.drawRectangle(0, 0, dateRect.width + 1,
					dateRect.height + 1);
			break;
		case SWT.Close:
			event.doit = false;
			dropDown(false);
			break;
		case SWT.Deactivate:
			dropDown(false);
			break;
		}
	}

	@Override
	public void redraw() {
		super.redraw();
		text.redraw();
		arrow.redraw();
		if (popup.isVisible()) {
			dateCtl.redraw();
		}
	}

	@Override
	public void redraw(int x, int y, int width, int height, boolean all) {
		super.redraw(x, y, width, height, true);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the user changes the receiver's selection.
	 * 
	 * @param listener
	 *            the listener which should no longer be notified
	 */
	public void removeSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection, listener);
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		background = color;
		if (text != null) {
			text.setBackground(color);
		}
		if (dateCtl != null) {
			dateCtl.setBackground(color);
		}
		if (arrow != null) {
			arrow.setBackground(color);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (popup != null) {
			popup.setVisible(false);
		}
		if (text != null) {
			text.setEnabled(enabled);
		}
		if (arrow != null) {
			arrow.setEnabled(enabled);
		}
	}

	@Override
	public boolean setFocus() {
		checkWidget();
		if (isFocusControl()) {
			return true;
		}
		return text.setFocus();
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);
		this.font = font;
		text.setFont(font);
		dateCtl.setFont(font);
		internalLayout(true);
	}

	@Override
	public void setForeground(Color color) {
		super.setForeground(color);
		foreground = color;
		if (text != null) {
			text.setForeground(color);
		}
		if (dateCtl != null) {
			dateCtl.setForeground(color);
		}
		if (arrow != null) {
			arrow.setForeground(color);
		}
	}

	/**
	 * Sets the layout which is associated with the receiver to be the argument
	 * which may be null.
	 * <p>
	 * Note: No Layout can be set on this Control because it already manages the
	 * size and position of its children.
	 * </p>
	 * 
	 * @param layout
	 *            the receiver's new layout or null
	 */
	@Override
	public void setLayout(Layout layout) {
		checkWidget();
		return;
	}

	@Override
	public void setToolTipText(String string) {
		checkWidget();
		super.setToolTipText(string);
		arrow.setToolTipText(string);
		text.setToolTipText(string);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (isDisposed()) {
			return;
		}
		if (!visible) {
			popup.setVisible(false);
		}
	}
}