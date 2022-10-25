package com.nextlabs.policystudio.editor.portal;

import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.ui.dialogs.TitleAreaDialogEx;
import com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel;

/**
 * @author bmeng
 */
public class PortalListPanel extends ComponentListPanel {

	public PortalListPanel(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getInputDialog()
	 */
	@Override
	protected TitleAreaDialogEx getInputDialog() {
		return new TitleAreaDialogEx(getShell(),
				Messages.PORTALLISTPANEL_PORTAL_TITLE,
				Messages.PORTALLISTPANEL_PORTAL_MSG,
				Messages.PORTALLISTPANEL_PORTAL_NAME,
				getNewComponentNameValidator());
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getFindInstructions()
	 */
	@Override
	protected String getFindInstructions() {
		return Messages.FIND_INSTRUCTIONS;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getFindString()
	 */
	@Override
	protected String getFindString() {
		return Messages.FIND_STRING;
	}

	@Override
	public String getComponentType() {
		return "PORTAL";
	}
}
