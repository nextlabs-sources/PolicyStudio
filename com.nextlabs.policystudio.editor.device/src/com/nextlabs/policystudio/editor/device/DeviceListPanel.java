package com.nextlabs.policystudio.editor.device;


import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.ui.dialogs.TitleAreaDialogEx;
import com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel;

/**
 * @author bmeng
 * 
 */
public class DeviceListPanel extends ComponentListPanel {

    public DeviceListPanel(Composite parent, int style) {
        super(parent, style);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getInputDialog()
     */
    @Override
    protected TitleAreaDialogEx getInputDialog() {
        return new TitleAreaDialogEx(getShell(), EditorMessages.DEVICELISTPANEL_DEVICE_TITLE, EditorMessages.DEVICELISTPANEL_DEVICE_MSG, EditorMessages.DEVICELISTPANEL_DEVICE_NAME, getNewComponentNameValidator());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getFindInstructions()
     */
    @Override
    protected String getFindInstructions() {
        return EditorMessages.FIND_INSTRUCTIONS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getFindString()
     */
    @Override
    protected String getFindString() {
        return EditorMessages.FIND_STRING;
    }

    @Override
    public String getComponentType() {
        return "DEVICE";
    }
}
