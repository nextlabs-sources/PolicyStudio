/*
 * Created on Mar 7, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.SWTEventObject;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.util.ResourceManager;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates
 *          .xml#2 $:
 */

@SuppressWarnings("restriction")
public class FilterControl extends Composite {

	private Text text;
	private Button goButton;
	private Button cancelButton;
	private String textVal = "";
	private String findString = "";
	private String boxText = "";
	private List<FilterControlListener> filterControlListenerArray = new ArrayList<FilterControlListener>();

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param style
	 */
	public FilterControl(Composite parent, int style, String findString,
			String boxText) {
		super(parent, style);
		this.findString = findString;
		this.boxText = boxText;
		setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
		initialize();
	}

	private void initialize() {
		FormLayout layout = new FormLayout();
		setLayout(layout);

		text = new Text(this, SWT.SINGLE | SWT.BORDER);
		text.setToolTipText(boxText);
		text.setTextLimit(128);
		text.setForeground(ResourceManager.getColor(SWT.COLOR_DARK_GRAY));
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (!text.getEditable()) {
					return;
				}
				text.setForeground(ResourceManager.getColor(SWT.COLOR_BLACK));
				if (!text.getText().equals(textVal)) {
					text.setText(textVal);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				textVal = text.getText();
				if (textVal.length() == 0) {
					text.setForeground(ResourceManager
							.getColor(SWT.COLOR_DARK_GRAY));
					text.setText(boxText);
				} else {
					cancelButton.setVisible(true);
				}
			}
		});

		text.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				textVal = text.getText();
				if (textVal.length() == 0) {
					cancelButton.setVisible(false);
					fireCancelEvent(e);
				} else {
					cancelButton.setVisible(true);
					fireSearchEvent(e);
				}
			}
		});

		goButton = new Button(this, SWT.PUSH | SWT.FLAT | SWT.CENTER);
		goButton.setToolTipText(ApplicationMessages.FILTERCONTROL_SEARCH);
		goButton.setText(ApplicationMessages.FILTERCONTROL_SEARCH);
		goButton.setAlignment(SWT.CENTER);

		goButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				textVal = text.getText();
				if (textVal.equals(boxText)) {
					textVal = "";
				}
				if (textVal.length() == 0) {
					cancelButton.setVisible(false);
					fireCancelEvent(e);
				} else {
					cancelButton.setVisible(true);
					fireSearchEvent(e);
				}
			}
		});

		cancelButton = new Button(this, SWT.PUSH | SWT.FLAT | SWT.CENTER);
		cancelButton
				.setToolTipText(ApplicationMessages.FILTERCONTROL_CANCEL_SEARCH);
		cancelButton.setText(ApplicationMessages.FILTERCONTROL_CANEL);
		cancelButton.setAlignment(SWT.CENTER);
		cancelButton.setVisible(false);

		cancelButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (textVal.length() != 0) {
					textVal = "";
					text.setText("");
				}
				fireCancelEvent(e);
				if (!cancelButton.isDisposed()) {
					cancelButton.setVisible(false);
				}
			}
		});

		CLabel findLabel = new CLabel(this, SWT.NONE);
		findLabel.setText(findString);
		findLabel.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));

		FormData formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 0);
		findLabel.setLayoutData(formData);

		formData = new FormData();
		formData.width = 100;
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		formData.left = new FormAttachment(findLabel);
		formData.right = new FormAttachment(goButton);
		text.setLayoutData(formData);

		Point size = goButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		formData = new FormData(size.x, size.y);
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100, 0);
		formData.right = new FormAttachment(cancelButton, -1);
		goButton.setLayoutData(formData);

		size = cancelButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		formData = new FormData(size.x, size.y);
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100, 0);
		formData.right = new FormAttachment(100);
		cancelButton.setLayoutData(formData);

		relayout();
	}

	void relayout() {
		if (textVal.length() == 0) {
			text.setText(boxText);
			text.setForeground(ResourceManager.getColor(SWT.COLOR_DARK_GRAY));
		}

		layout();
	}

	public void addFilterControlListener(FilterControlListener listener) {
		filterControlListenerArray.add(listener);
	}

	public void removeFilterControlListener(FilterControlListener listener) {
		filterControlListenerArray.remove(listener);
	}

	private void fireSearchEvent(SWTEventObject event) {
		goButton.setEnabled(false);
		FilterControlEvent e = new FilterControlEvent(this);
		e.originalEvent = event;
		for (FilterControlListener listener : filterControlListenerArray) {
			listener.search(e, new FilterControlListener.EndOfSearch() {

				public void endOfSearch() {
					getDisplay().syncExec(new Runnable() {

						public void run() {
							goButton.setEnabled(true);
						}
					});
				}
			});
		}
	}

	private void fireCancelEvent(SWTEventObject event) {
		goButton.setEnabled(true);
		FilterControlEvent e = new FilterControlEvent(this);
		e.originalEvent = event;
		for (FilterControlListener listener : filterControlListenerArray) {
			listener.cancel(e);
		}
	}

	public void setEditable(boolean editable) {
		text.setEditable(editable);
		text.setEnabled(editable);
		goButton.setVisible(editable);
		cancelButton.setVisible(!editable);
	}

	/**
	 * @return
	 */
	public String getText() {
		return textVal;
	}

	public String getBoxText() {
		return boxText;
	}
}