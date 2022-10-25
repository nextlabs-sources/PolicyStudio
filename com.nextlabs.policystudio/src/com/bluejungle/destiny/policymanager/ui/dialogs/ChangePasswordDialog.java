/*
 * Created on Sep 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.framework.utils.PasswordUtils;
import com.bluejungle.pf.destiny.services.InvalidPasswordException;
import com.bluejungle.pf.destiny.services.PolicyEditorException;

/**
 * @author sasha
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/ui/dialogs/ChangePasswordDialog
 *          .java#4 $:
 */

public class ChangePasswordDialog extends TitleAreaDialog {

	private Text oldPassText;
	private Text passText;
	private Text confPassText;

	/**
	 * Constructor
	 * 
	 * @param arg0
	 */
	public ChangePasswordDialog(Shell parent) {
		super(parent);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(DialogMessages.CHANGEPASSWORDDIALOG_CHANGE_PASSWORD);
		newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
	}

	@Override
	public void create() {
		super.create();
		setTitle(DialogMessages.CHANGEPASSWORDDIALOG_CHANGE_PASSWORD);
		setTitleImage(ImageBundle.TITLE_IMAGE);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				DialogMessages.LABEL_CHANGE, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite root = (Composite) super.createDialogArea(parent);

		Composite bottom = new Composite(root, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		bottom.setLayoutData(data);

		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 20;
		layout.horizontalSpacing = 10;
		bottom.setLayout(layout);

		initialize(bottom);

		return parent;
	}

	@Override
	protected void okPressed() {
		setMessage("", IMessageProvider.NONE);

		String password = passText.getText();
		if (!password.equals(confPassText.getText())) {
			setMessage(DialogMessages.CHANGEPASSWORDDIALOG_ERROR_MATCH,
					IMessageProvider.ERROR);
			passText.setFocus();
			return;
		}
		if (!PasswordUtils.isValidPasswordDefault(password)) {
			setMessage(DialogMessages.CHANGEPASSWORDDIALOG_ERROR_MINIMUM,
					IMessageProvider.ERROR);
			passText.setFocus();
			return;
		}

		try {
			PolicyServerProxy.changePassword(oldPassText.getText(), password);
		} catch (InvalidPasswordException e) {
			setMessage(DialogMessages.CHANGEPASSWORDDIALOG_ERROR_OLD,
					IMessageProvider.ERROR);
			oldPassText.setFocus();
			return;
		} catch (PolicyEditorException e) {
			setMessage(DialogMessages.CHANGEPASSWORDDIALOG_ERROR_INTERNAL,
					IMessageProvider.ERROR);
			return;
		}

		MessageDialog.openInformation(getShell(),
				DialogMessages.CHANGEPASSWORDDIALOG_CHANGE_PASSWORD,
				DialogMessages.CHANGEPASSWORDDIALOG_SUCCESSFULLY_CHANGED);
		super.okPressed();
	}

	private void initialize(Composite root) {
		Label userLabel = new Label(root, SWT.NONE);
		userLabel.setText(DialogMessages.CHANGEPASSWORDDIALOG_USER);

		Label userNameLabel = new Label(root, SWT.NONE);
		userNameLabel.setFont(FontBundle.ARIAL_9_BOLD);
		userNameLabel.setText(GlobalState.user);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		userNameLabel.setLayoutData(data);

		Label oldPassLabel = new Label(root, SWT.NONE);
		oldPassLabel.setText(DialogMessages.CHANGEPASSWORDDIALOG_OLD_PASSWORD);

		oldPassText = new Text(root, SWT.PASSWORD | SWT.BORDER);
		oldPassText.setTextLimit(PasswordUtils.DEFAULT_PASSWORD_MAX_LENGTH);
		data = new GridData(GridData.FILL_HORIZONTAL);
		oldPassText.setLayoutData(data);
		oldPassText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				setMessage("", IMessageProvider.NONE);
			}
		});

		Label passLabel = new Label(root, SWT.NONE);
		passLabel.setText(DialogMessages.CHANGEPASSWORDDIALOG_NEW_PASSWORD);

		passText = new Text(root, SWT.PASSWORD | SWT.BORDER);
		passText.setTextLimit(PasswordUtils.DEFAULT_PASSWORD_MAX_LENGTH);
		data = new GridData(GridData.FILL_HORIZONTAL);
		passText.setLayoutData(data);
		passText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				setMessage("", IMessageProvider.NONE);
			}
		});

		Label confPassLabel = new Label(root, SWT.NONE);
		confPassLabel
				.setText(DialogMessages.CHANGEPASSWORDDIALOG_CONFIRM_PASSWORD);

		confPassText = new Text(root, SWT.PASSWORD | SWT.BORDER);
		confPassText.setTextLimit(PasswordUtils.DEFAULT_PASSWORD_MAX_LENGTH);
		data = new GridData(GridData.FILL_HORIZONTAL);
		confPassText.setLayoutData(data);
		confPassText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				setMessage("", IMessageProvider.NONE);
			}
		});
	}
}
