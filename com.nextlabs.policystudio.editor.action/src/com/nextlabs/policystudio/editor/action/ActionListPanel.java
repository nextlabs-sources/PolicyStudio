/*
 * Created on May 25, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.policystudio.editor.action;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.dialogs.TitleAreaDialogEx;
import com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel;
import com.bluejungle.destiny.policymanager.ui.usergroup.Messages;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.pf.domain.destiny.common.IDSpec;

/**
 * @author bmeng
 * 
 */
public class ActionListPanel extends ComponentListPanel {

	public ActionListPanel(Composite parent, int style) {
		super(parent, style);
	}
	
	@Override
    public void initialize() {
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.verticalSpacing = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout(layout);

        Composite top = new Composite(this, SWT.BORDER);
        top.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        layout = new GridLayout(2, false);
        top.setLayout(layout);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        top.setLayoutData(data);

        buttonNew = new Button(top, SWT.PUSH | SWT.FLAT | SWT.CENTER);
        buttonNew.setText(Messages.COMPONENTLISTPANEL_NEW);
        buttonNew.setToolTipText(Messages.COMPONENTLISTPANEL_NEW);
        data = new GridData();
        buttonNew.setLayoutData(data);

        buttonNew.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TitleAreaDialogEx dlg = getInputDialog();
                if (dlg.open() == Window.OK) {
                    IDSpec spec = createComponent(dlg.getValue().trim());
                    GlobalState.getInstance().loadObjectInEditorPanel(spec);
                    populateList();
                }
            }
        });

        setupFilterControl(top);
        setupTableViewer();
        
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
				Messages.ACTIONLISTPANEL_ACTION_TITLE,
				Messages.ACTIONLISTPANEL_ACTION_MSG,
				Messages.ACTIONLISTPANEL_ACTION_NAME,
				getNewComponentNameValidator());
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

	@Override
	public String getComponentType() {
		return "ACTION";
	}
}
