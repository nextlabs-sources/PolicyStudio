package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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

public class ExportDialog extends TitleAreaDialog {
    private Text textPath;
    private String path;
    private Button buttonFolder, buttonSubFolder;
    private int selection;
    private final String extension; // .xml or .xacml

    public ExportDialog(Shell parentShell, String extension) {
        super(parentShell);
        this.extension = extension;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(DialogMessages.EXPORTDIALOG_TITLE);
        newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
    }

    @Override
    public void create() {
        super.create();
        setTitle(DialogMessages.EXPORTDIALOG_DESCRIPTION);
        setTitleImage(ImageBundle.TITLE_IMAGE);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID,
                     DialogMessages.EXPORTDIALOG_EXPORT, true);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        createButton(parent, IDialogConstants.CANCEL_ID,
                     IDialogConstants.CANCEL_LABEL, false);
    }

    private void changeOKButtonStatus(String path) {
        Button b = getButton(IDialogConstants.OK_ID);

        boolean enabled = b.getEnabled();

        if (enabled && (path == null || path.length() == 0)) {
            b.setEnabled(false);
        } else if (!enabled && (path != null && path.length () > 0)) {
            b.setEnabled(true);
        }
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
        label.setText(DialogMessages.EXPORTDIALOG_EXPORT_FILE_NAME);

        textPath = new Text(bottom, SWT.BORDER);
        textPath.setEditable(true);
        data = new GridData(GridData.FILL_HORIZONTAL);
        textPath.setLayoutData(data);
        textPath.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                path = textPath.getText();
                changeOKButtonStatus(path);
            }
        });

        Button buttonBrowse = new Button(bottom, SWT.PUSH);
        buttonBrowse.setText(DialogMessages.EXPORTDIALOG_BROWSE);
        buttonBrowse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = getParentShell();
                FileDialog dialog = new FileDialog(shell, SWT.SAVE);
                dialog.setText(DialogMessages.EXPORTDIALOG_TITLE);
                dialog.setFilterExtensions(new String[] { "*" + extension, "*.*" });
                path = dialog.open();
                if (path == null) {
                    return;
                }
                if (!path.toLowerCase().endsWith(extension)) {
                    path += extension;
                }
                File file = new File(path);
                if (file.exists()) {
                    if (!MessageDialog
                        .openConfirm(
                            shell,
                        "Confirm to Overwrite",
                        path
                        + " already exists.\nDo you want to overwirte it?")) {
                        return;
                    }
                }
                textPath.setText(path);
                changeOKButtonStatus(path);
            }
        });

        Group group = new Group(bottom, SWT.NONE);
        group.setText(DialogMessages.EXPORTDIALOG_EXPORT_TARGET);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        group.setLayoutData(data);
        layout = new GridLayout(2, false);
        group.setLayout(layout);

        buttonSubFolder = new Button(group, SWT.RADIO);
        buttonSubFolder.setText(DialogMessages.EXPORTDIALOG_SUBFOLDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        buttonSubFolder.setLayoutData(data);
        buttonSubFolder.setSelection(true);

        buttonFolder = new Button(group, SWT.RADIO);
        buttonFolder.setText(DialogMessages.EXPORTDIALOG_FOLDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        buttonFolder.setLayoutData(data);

        return parent;
    }

    private void getChoice() {
        if (buttonSubFolder.getSelection()) {
            selection = 0;
        }
        if (buttonFolder.getSelection()) {
            selection = 1;
        }
    }

    public int getSelection() {
        return selection;
    }

    @Override
    protected void okPressed() {
        getChoice();
        super.okPressed();
    }

    public String getPath() {
        return path;
    }
}
