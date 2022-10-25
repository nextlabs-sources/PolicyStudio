/*
 * Created on Mar 7, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.policystudio.editor.user;

import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.ui.dialogs.TitleAreaDialogEx;
import com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel;

/**
 * @author bmeng
 */

public class UserListPanel extends ComponentListPanel {

    /**
     * Constructor
     * 
     * @param parent
     * @param style
     */
    public UserListPanel(Composite parent, int style) {
        super(parent, style);
    }

    /**
     * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getInputDialog()
     */
    @Override
    protected TitleAreaDialogEx getInputDialog() {
        return new TitleAreaDialogEx(getShell(), Messages.USERLISTPANEL_USER_TITLE, Messages.USERLISTPANEL_USER_MSG, Messages.USERLISTPANEL_USER_NAME, getNewComponentNameValidator());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getFindInstructions()
     */
    @Override
    protected String getFindInstructions() {
        return Messages.FIND_INSTRUCTIONS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getFindString()
     */
    @Override
    protected String getFindString() {
        return Messages.FIND_STRING;
    }

    @Override
    public String getComponentType() {
        return "USER";
    }
}