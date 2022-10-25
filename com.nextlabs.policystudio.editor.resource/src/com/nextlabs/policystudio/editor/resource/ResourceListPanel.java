/*
 * Created on Apr 20, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.policystudio.editor.resource;

import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.ui.dialogs.TitleAreaDialogEx;
import com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel;

/**
 * @author bmeng
 */
public class ResourceListPanel extends ComponentListPanel {

	public ResourceListPanel(Composite parent, int style) {
		super(parent, style);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#
	 * getInputDialog()
	 */
	@Override
	protected TitleAreaDialogEx getInputDialog() {
		return new TitleAreaDialogEx(getShell(),
				Messages.RESOURCELISTPANEL_RESOURCE_TITLE,
				Messages.RESOURCELISTPANEL_RESOURCE_MSG,
				Messages.RESOURCELISTPANEL_RESOURCE_NAME,
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
		return "RESOURCE";
	}

}
