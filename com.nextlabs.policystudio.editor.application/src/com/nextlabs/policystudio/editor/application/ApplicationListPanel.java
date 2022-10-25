/*
 * Created on Mar 7, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.policystudio.editor.application;

import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.ui.dialogs.TitleAreaDialogEx;
import com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel;

/**
 * @author bmeng
 */

public class ApplicationListPanel extends ComponentListPanel {

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param style
	 */
	public ApplicationListPanel(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getInputDialog()
	 */
	@Override
	protected TitleAreaDialogEx getInputDialog() {
		return new TitleAreaDialogEx(getShell(),
				Messages.APPLICATIONLISTPANEL_APPLICATION_TITLE,
				Messages.APPLICATIONLISTPANEL_APPLICATION_MSG,
				Messages.APPLICATIONLISTPANEL_APPLICATION_NAME,
				getNewComponentNameValidator());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#
	 * getFindInstructions()
	 */
	@Override
	protected String getFindInstructions() {
		return Messages.FIND_INSTRUCTIONS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#
	 * getFindString()
	 */
	@Override
	protected String getFindString() {
		return Messages.FIND_STRING;
	}

	@Override
	public String getComponentType() {
		return "APPLICATION";
	}
}