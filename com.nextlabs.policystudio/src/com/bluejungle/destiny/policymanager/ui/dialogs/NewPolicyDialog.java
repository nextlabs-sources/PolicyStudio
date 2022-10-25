/*
 * Created on Dec 27, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bluejungle.destiny.policymanager.util.PluginUtil;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/ui/dialogs/NewPolicyDialog.java#5 $
 */

public class NewPolicyDialog extends TitleAreaDialogEx {

	private String policyPurpose;
	private Combo comboUsage;

	public NewPolicyDialog(Shell parentShell, String dialogTitle,
			String dialogMessage, String initialValue, IInputValidator validator) {
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(DialogMessages.NEWPOLICYDIALOG_TITLE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite root = (Composite) super.createDialogArea(parent);
		getTextControl().setTextLimit(128);

		Composite composite = new Composite(root, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(composite, SWT.NONE);
		label.setText(DialogMessages.NEWPOLICYDIALOG_TYPE);

		comboUsage = new Combo(composite, SWT.READ_ONLY);
		List<IConfigurationElement> editors = PluginUtil.getEditorPluginForType("POLICY"); 
		List<String> names = new ArrayList<String>();
		for (IConfigurationElement element : editors) {
			String name = element.getAttribute("displayName");
			names.add(name);
		}
		Collections.sort(names, String.CASE_INSENSITIVE_ORDER);

		comboUsage.setItems(names.toArray(new String[names.size()]));
		comboUsage.select(0);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		comboUsage.setLayoutData(data);

		return root;
	}

	@Override
	protected void okPressed() {
		policyPurpose = comboUsage.getText();

		super.okPressed();
	}

	public String getPolicyPurpose() {
		return policyPurpose;
	}
}
