/*
 * Created on Sep 1, 2005 All sources, binaries and HTML pages (C) copyright
 * 2005 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @author aweber
 */
public class StatusPart extends ViewPart {

	private StatusPanel statusPanel;

	public StatusPart() {
	}

	@Override
	public void createPartControl(Composite parent) {
		statusPanel = new StatusPanel(parent, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(100, -40);
		formData.bottom = new FormAttachment(100, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		statusPanel.setLayoutData(formData);
	}

	@Override
	public void setFocus() {
	}
}
