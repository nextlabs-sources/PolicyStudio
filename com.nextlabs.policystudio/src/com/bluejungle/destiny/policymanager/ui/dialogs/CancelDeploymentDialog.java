/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.controls.DependencyControl;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lifecycle.DeploymentRecord;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/
 *          SubmitCheckDependenciesDialog.java#1 $
 */

public class CancelDeploymentDialog extends Dialog {

	private static final IEventManager EVENT_MANAGER;
	static {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		EVENT_MANAGER = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
	}

	private static final Point SIZE = new Point(600, 500);

	private List<DependencyControl.Dependency> accessPolicies = new ArrayList<DependencyControl.Dependency>();
	private List<DependencyControl.Dependency> usagePolicies = new ArrayList<DependencyControl.Dependency>();
	private List<DependencyControl.Dependency> components = new ArrayList<DependencyControl.Dependency>();
	private DependencyControl dependencyControl = null;
	private DeploymentRecord deploymentRecord = null;
	boolean canCancelDeployment = false;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent shell
	 * @param canCancel
	 * @param objectList
	 *            list of objects to deploy
	 * 
	 */
	public CancelDeploymentDialog(Shell parent,
			DeploymentRecord deploymentRecord, boolean canCancel) {
		super(parent);
		this.deploymentRecord = deploymentRecord;
		this.canCancelDeployment = canCancel;
	}

	@Override
	public int open() {
		if (!classifyObjects()) {
			return CANCEL;
		}
		return super.open();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setSize(SIZE);
		newShell.setText(DialogMessages.CANCELDEPLOYMENTDIALOG_TITLE);
		newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite root = (Composite) super.createDialogArea(parent);

		Composite bottom = new Composite(root, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		bottom.setLayoutData(data);

		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 15;
		layout.marginHeight = 15;
		layout.horizontalSpacing = 10;
		bottom.setLayout(layout);

		initialize(bottom);

		return parent;
	}

	/**
	 * classify objects into three lists: Access policies, usage policies,
	 * selected components
	 * 
	 * @return true if objects are classified successfully. false otherwise.
	 */
	private boolean classifyObjects() {
		Collection<DomainObjectDescriptor> objectList = PolicyServerProxy
				.getObjectsInDeploymentRecord(deploymentRecord);
		if (objectList == null || objectList.size() == 0) {
			MessageDialog
					.openError(getShell(), "Error",
							"Deployments without policies or components cannot be cancelled.");
			return false;
		}
		Collection<? extends IHasId> entities = PolicyServerProxy
				.getEntitiesForDescriptor(objectList);
		Map<Long, DomainObjectDescriptor> forId = new HashMap<Long, DomainObjectDescriptor>();
		for (DomainObjectDescriptor descriptor : objectList) {
			forId.put(descriptor.getId(), descriptor);
		}
		for (IHasId hasId : entities) {
			DomainObjectDescriptor descriptor = (DomainObjectDescriptor) forId
					.get(hasId.getId());
			if (descriptor.getType() == EntityType.POLICY) {
				IDPolicy p = (IDPolicy) hasId;
				if (p.hasAttribute("access")) {
					accessPolicies.add(new DependencyControl.Dependency(
							descriptor, false, true));
				} else {
					usagePolicies.add(new DependencyControl.Dependency(
							descriptor, false, true));
				}
			} else {
				components.add(new DependencyControl.Dependency(descriptor,
						false, true));
			}
		}
		return true;
	}

	/**
     * 
     */
	private void initialize(Composite root) {
		Label deploymentTimeLabel = new Label(root, SWT.NONE);
		deploymentTimeLabel
				.setText(DialogMessages.CANCELDEPLOYMENTDIALOG_SCHEDULED_DEPLOYMENT_TIME);
		GridData data = new GridData();
		deploymentTimeLabel.setLayoutData(data);

		Label deploymentTimeValue = new Label(root, SWT.NONE);
		deploymentTimeValue.setText(SimpleDateFormat.getDateTimeInstance()
				.format(deploymentRecord.getAsOf()));
		data = new GridData();
		deploymentTimeValue.setLayoutData(data);

		Label contentsLabel = new Label(root, SWT.NONE);
		contentsLabel
				.setText(DialogMessages.CANCELDEPLOYMENTDIALOG_DEPLOYMENT_CONTENTS);
		data = new GridData();
		data.horizontalSpan = 2;
		contentsLabel.setLayoutData(data);

		dependencyControl = new DependencyControl(root, SWT.NONE, false);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		dependencyControl.setLayoutData(data);

		if (accessPolicies.size() > 0) {
			dependencyControl.addSection(
					DialogMessages.CANCELDEPLOYMENTDIALOG_ACCESS_POLICIES,
					null, null, accessPolicies);
		}
		if (usagePolicies.size() > 0) {
			dependencyControl.addSection(
					DialogMessages.CANCELDEPLOYMENTDIALOG_USAGE_POLICIES, null,
					null, usagePolicies);
		}
		if (components.size() > 0) {
			dependencyControl.addSection(
					DialogMessages.CANCELDEPLOYMENTDIALOG_COMPONENTS, null,
					null, components);
		}

		dependencyControl.initialize();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				DialogMessages.CANCELDEPLOYMENTDIALOG_CANCEL_THIS_DEPLOYMENT,
				false);
		getButton(IDialogConstants.OK_ID).setEnabled(canCancelDeployment);
		createButton(parent, IDialogConstants.CANCEL_ID,
				DialogMessages.LABEL_CLOSE, false);
	}

	@Override
	protected void okPressed() {
		cancelDeployment();
	}

	/**
	 * calls the server proxy to cancel the deployment
	 */
	private void cancelDeployment() {
		Calendar cutoff = new GregorianCalendar();
		cutoff.add(Calendar.MINUTE, 1);
		// Check if it is still OK to cancel this deployment
		if (!deploymentRecord.getAsOf().after(cutoff.getTime())) {
			String state = deploymentRecord.getAsOf().after(new Date()) ? " about to become"
					: "";
			MessageDialog.openError(getShell(), "Error", "This deployment is"
					+ state + " active.\nIt can no longer be cancelled.");
			return;
		}
		PolicyServerProxy.cancelDeployment(deploymentRecord);

		// deployment of current object may have been cancelled.
		// we could optimize to do this only if current object is really
		// affected.
		GlobalState gs = GlobalState.getInstance();
		IHasId currentObject = gs.getCurrentObject();
		if (currentObject != null) {
			PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent(
					currentObject);
			EVENT_MANAGER.fireEvent(objectModifiedEvent);
		}

		super.okPressed();
	}
}