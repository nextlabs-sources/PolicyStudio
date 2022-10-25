package com.nextlabs.policystudio.editor.host;

import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.ui.dialogs.TitleAreaDialogEx;
import com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel;

/**
 * @author bmeng
 * 
 */
public class DesktopListPanel extends ComponentListPanel {

	public DesktopListPanel(Composite parent, int style) {
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
				Messages.DESKTOPLISTPANEL_DESKTOP_TITLE,
				Messages.DESKTOPLISTPANEL_DESKTOP_MSG,
				Messages.DESKTOPLISTPANEL_DESKTOP_NAME,
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
		return "HOST";
	}
}
