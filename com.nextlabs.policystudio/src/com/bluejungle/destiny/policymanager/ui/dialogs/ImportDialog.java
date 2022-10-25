package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.util.PlatformUtils;

public class ImportDialog extends TitleAreaDialog {
	private Text textFileName;
	private Text textSuffix;
	private Button buttonOptionO, buttonOptionR, buttonOptionD, buttonOptionX;
	private Button buttonShallow;

	private int selection;
	private String path;
	private String suffix;
	private boolean isShallowImport;

	public ImportDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(DialogMessages.IMPORTDIALOG_TITLE);
		newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
	}

	@Override
	public void create() {
		super.create();
		setTitle(DialogMessages.IMPORTDIALOG_DESCRIPTION);
		setTitleImage(ImageBundle.TITLE_IMAGE);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				DialogMessages.IMPORTDIALOG_IMPORT, true);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite root = (Composite) super.createDialogArea(parent);

		Composite bottom = new Composite(root, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		bottom.setLayoutData(data);

		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 20;
		bottom.setLayout(layout);

		Label label = new Label(bottom, SWT.NONE);
		label.setText(DialogMessages.IMPORTDIALOG_IMPORT_FILE_NAME);

		textFileName = new Text(bottom, SWT.BORDER);
		textFileName.setEnabled(false);
		data = new GridData(GridData.FILL_HORIZONTAL);
		textFileName.setLayoutData(data);

		Button buttonBrowse = new Button(bottom, SWT.PUSH);
		buttonBrowse.setText(DialogMessages.EXPORTDIALOG_BROWSE);
		buttonBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = getParentShell();
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });
				path = dialog.open();
				if (path == null) {
					return;
				}
				File file = new File(path);
				if (!file.exists()) {
					MessageDialog.openError(shell,
							DialogMessages.IMPORTDIALOG_FILE_NOT_FOUND,
							DialogMessages.IMPORTDIALOG_CANNOT_FIND + path);
					return;
				}
				textFileName.setText(path);
				getButton(IDialogConstants.OK_ID).setEnabled(true);
			}
		});

		Group group = new Group(bottom, SWT.NONE);
		group.setText(DialogMessages.IMPORTDIALOG_IMPORT_OPTIONS);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		group.setLayoutData(data);
		layout = new GridLayout(2, false);
		group.setLayout(layout);

		buttonOptionO = new Button(group, SWT.RADIO);
		buttonOptionO.setText(DialogMessages.IMPORTDIALOG_OVERWRITING);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		buttonOptionO.setLayoutData(data);

		buttonOptionR = new Button(group, SWT.RADIO);
		buttonOptionR.setText(DialogMessages.IMPORTDIALOG_RENAMING);
		data = new GridData(GridData.FILL);
		buttonOptionR.setLayoutData(data);
		buttonOptionR.setSelection(true);

		textSuffix = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		textSuffix.setLayoutData(data);
		textSuffix.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				PlatformUtils.validCharForName(e);
			}
		});
		Calendar today = new GregorianCalendar();
		SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
		textSuffix.setText(format.format(today.getTime()));

		buttonOptionD = new Button(group, SWT.RADIO);
		buttonOptionD.setText(DialogMessages.IMPORTDIALOG_KEEPING);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		buttonOptionD.setLayoutData(data);

		buttonOptionX = new Button(group, SWT.RADIO);
		buttonOptionX.setText(DialogMessages.IMPORTDIALOG_CANCELLING);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		buttonOptionX.setLayoutData(data);

		buttonShallow = new Button(bottom, SWT.CHECK);
		buttonShallow.setText(DialogMessages.IMPORTDIALOG_SHALLOW_IMPORT);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		buttonShallow.setLayoutData(data);

		return parent;
	}

	public boolean isShallowImport() {
		return isShallowImport;
	}

	private void getChoice() {
		if (buttonOptionO.getSelection()) {
			selection = 0;
		} else if (buttonOptionR.getSelection()) {
			selection = 1;
		} else if (buttonOptionD.getSelection()) {
			selection = 2;
		} else if (buttonOptionX.getSelection()) {
			selection = 3;
		}
		if (buttonShallow.getSelection()) {
			isShallowImport = true;
		} else {
			isShallowImport = false;
		}
		suffix = textSuffix.getText();
	}

	public int getSelection() {
		return selection;
	}

	@Override
	protected void okPressed() {
		getChoice();
		super.okPressed();
	}

	public String getSuffix() {
		return suffix;
	}

	public String getPath() {
		return path;
	}
}