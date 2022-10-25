/*
 * Created on May 20, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.bluejungle.destiny.policymanager.editor.ReadOnlyPanelFactory;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author bmeng
 * 
 */
public class ShowDeployedVersionDialog extends Dialog {

	private DomainObjectDescriptor descriptor;

	private Point WINDOW_SIZE = new Point(600, 400);

	public ShowDeployedVersionDialog(Shell parent,
			DomainObjectDescriptor descriptor) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.descriptor = descriptor;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setSize(WINDOW_SIZE);

		newShell
				.setText(DialogMessages.SHOWDEPLOYEDVERSIONDIALOG_DEPLOYED_VERSION_OF
						+ descriptor.getName());
		newShell.setImage(ObjectLabelImageProvider.getImage(descriptor));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite root = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(root, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(data);

		init(container);

		return parent;
	}

	private void init(Composite root) {
		PolicyServerProxy.ObjectVersion deployedObject = PolicyServerProxy
				.getDeployedVersion(descriptor);
		PolicyServerProxy.ObjectVersion scheduledObject = PolicyServerProxy
				.getLastScheduledVersion(descriptor);

		TabFolder tabFolder = new TabFolder(root, SWT.NONE);
		GridLayout layout = new GridLayout();
		tabFolder.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		tabFolder.setLayoutData(data);

		if (scheduledObject != null && scheduledObject.object != null) {
			TabItem scheduled = new TabItem(tabFolder, SWT.NONE);
			scheduled
					.setText(DialogMessages.SHOWDEPLOYEDVERSIONDIALOG_LAST_SCHEDULED_VERSION);
			Composite scheduledComp = setupViewPanel(tabFolder, scheduledObject);
			scheduled.setControl(scheduledComp);
		}
		if (deployedObject != null && deployedObject.object != null) {
			TabItem deployed = new TabItem(tabFolder, SWT.NONE);
			deployed
					.setText(DialogMessages.SHOWDEPLOYEDVERSIONDIALOG_DEPLOYED_VERSION);
			Composite deployedComp = setupViewPanel(tabFolder, deployedObject);
			deployed.setControl(deployedComp);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				DialogMessages.SHOWDEPLOYEDVERSIONDIALOG_CLOSE, true);
	}

	private Composite setupViewPanel(Composite parent,
			PolicyServerProxy.ObjectVersion objectVersion) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(data);

		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);

		ScrolledComposite componentViewerComposite = new ScrolledComposite(
				comp, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		componentViewerComposite.setBackground(ResourceManager
				.getColor(SWT.COLOR_WHITE));
		componentViewerComposite.setLayout(new GridLayout());
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		componentViewerComposite.setLayoutData(data);
		componentViewerComposite.setExpandHorizontal(true);
		componentViewerComposite.setExpandVertical(true);
		componentViewerComposite.getVerticalBar().setIncrement(10);
		componentViewerComposite.getVerticalBar().setPageIncrement(100);
		componentViewerComposite.getHorizontalBar().setIncrement(10);
		componentViewerComposite.getHorizontalBar().setPageIncrement(100);

		Composite detail = ReadOnlyPanelFactory.getEditorPanel(
				componentViewerComposite, SWT.NONE, objectVersion.object);
		data = new GridData(GridData.FILL_BOTH);
		detail.setLayoutData(data);

		componentViewerComposite.setContent(detail);
		componentViewerComposite.setMinSize(detail.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

		Label fromLabel = new Label(comp, SWT.NONE);
		fromLabel.setText(DialogMessages.SHOWDEPLOYEDVERSIONDIALOG_ACTIVE_FROM);
		data = new GridData();
		fromLabel.setLayoutData(data);

		Label fromValue = new Label(comp, SWT.WRAP);
		fromValue.setText(objectVersion.activeFrom.toString());
		data = new GridData(GridData.FILL_HORIZONTAL);
		fromLabel.setLayoutData(data);

		if (!UnmodifiableDate.END_OF_TIME.equals(objectVersion.activeTo)) {
			// only display if active to some real date
			Label toLabel = new Label(comp, SWT.NONE);
			toLabel.setText(DialogMessages.SHOWDEPLOYEDVERSIONDIALOG_ACTIVE_TO);
			data = new GridData();
			toLabel.setLayoutData(data);

			Label toValue = new Label(comp, SWT.WRAP);
			toValue.setText(objectVersion.activeTo.toString());
			data = new GridData(GridData.FILL_HORIZONTAL);
			toValue.setLayoutData(data);
		}

		return comp;
	}
}
