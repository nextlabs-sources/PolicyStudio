package com.nextlabs.policystudio.editor.server;

import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.ui.dialogs.TitleAreaDialogEx;
import com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel;

/**
 * @author bmeng
 */
public class ServerListPanel extends ComponentListPanel {

	public ServerListPanel(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getInputDialog()
	 */
	@Override
	protected TitleAreaDialogEx getInputDialog() {
		return new TitleAreaDialogEx(getShell(),
				Messages.SERVERLISTPANEL_SERVER_TITLE,
				Messages.SERVERLISTPANEL_SERVER_MSG,
				Messages.SERVERLISTPANEL_SERVER_NAME,
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
		return "SERVER";
	}
}
