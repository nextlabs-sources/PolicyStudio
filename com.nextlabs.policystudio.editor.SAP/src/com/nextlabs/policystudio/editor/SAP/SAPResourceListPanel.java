package com.nextlabs.policystudio.editor.SAP;

import com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel;
import com.bluejungle.destiny.policymanager.ui.dialogs.TitleAreaDialogEx;
import org.eclipse.swt.widgets.Composite;

public class SAPResourceListPanel extends ComponentListPanel {

	public SAPResourceListPanel(Composite parent, int style) {
		super(parent, style);
	}

	protected TitleAreaDialogEx getInputDialog(){
		return new TitleAreaDialogEx(getShell(), 
				EditorMessages.OBJECTLISTPANEL_OBJECT_TITLE, 
				EditorMessages.OBJECTLISTPANEL_OBJECT_MSG, 
				EditorMessages.OBJECTLISTPANEL_OBJECT_NAME, 
				getNewComponentNameValidator());
	}

	protected String getFindInstructions(){
		return EditorMessages.FIND_INSTRUCTIONS;
	}

	protected String getFindString() {
		return EditorMessages.FIND_STRING;
	}

	public String getComponentType() {
		return "OBJECT";
	}
}