/*
 * Created on Jul 17, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.controls.CalendarPicker;
import com.bluejungle.destiny.policymanager.ui.controls.DependencyControl;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/D_Plugins/com.nextlabs.policystudio/src/com
 *          /bluejungle
 *          /destiny/policymanager/ui/dialogs/ScheduleDeploymentPage.java#1 $
 */

public class ScheduleDeploymentPage extends WizardPage {
	private Button defaultRadio, specifyRadio, pushRadio;
	private CalendarPicker dateControl;
	private DateTime timeControl;
	private Date defaultDeploymentTime = PolicyServerProxy.getNextDeploymentTime();
	private List<DomainObjectDescriptor> objectList;
	private List<DependencyControl.Dependency> accessPolicies = new ArrayList<DependencyControl.Dependency>();
	private List<DependencyControl.Dependency> usagePolicies = new ArrayList<DependencyControl.Dependency>();
	private List<DependencyControl.Dependency> selectedComponents = new ArrayList<DependencyControl.Dependency>();
	private List<DependencyControl.Dependency> requiredComponents = new ArrayList<DependencyControl.Dependency>();
	private List<DependencyControl.Dependency> modifiedComponents = new ArrayList<DependencyControl.Dependency>();
	private DependencyControl dependencyControl = null;

	public ScheduleDeploymentPage(List<DomainObjectDescriptor> descriptors) {
		super(DialogMessages.SCHEDULEDEPLOYMENTDIALOG_DEPLOY,
				DialogMessages.SCHEDULEDEPLOYMENTDIALOG_DEPLOY, 
				ImageDescriptor.createFromImage(ImageBundle.TITLE_IMAGE));
		this.objectList = descriptors;

		classifyObjects();
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);

		Group group = new Group(composite, SWT.NONE);
		group.setText(DialogMessages.SCHEDULEDEPLOYMENTDIALOG_GROUP_DEPLOYMENT_START_TIME);
		data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		layout = new GridLayout(3, false);
		group.setLayout(layout);

		defaultRadio = new Button(group, SWT.RADIO);
		String defaultTimeStr = DialogMessages.SCHEDULEDEPLOYMENTDIALOG_DEFAULTTIMESTR;
		if (defaultDeploymentTime != null) {
			defaultTimeStr = SimpleDateFormat.getDateTimeInstance(
					DateFormat.SHORT, DateFormat.SHORT).format(defaultDeploymentTime);
		}
		defaultRadio.setText(DialogMessages.SCHEDULEDEPLOYMENTDIALOG_DEFAULT+ defaultTimeStr);
		SelectionAdapter selectionAdaptor = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource() == specifyRadio) {
					dateControl.setEnabled(true);
					timeControl.setEnabled(true);
				} else {
					dateControl.setEnabled(false);
					timeControl.setEnabled(false);
				}
			}
		};
		defaultRadio.addSelectionListener(selectionAdaptor);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		defaultRadio.setLayoutData(data);

		specifyRadio = new Button(group, SWT.RADIO);
		specifyRadio.setText(DialogMessages.SCHEDULEDEPLOYMENTDIALOG_SPECIFY);
		specifyRadio.addSelectionListener(selectionAdaptor);
		data = new GridData();
		specifyRadio.setLayoutData(data);

		Calendar now = new GregorianCalendar();
		dateControl = new CalendarPicker(group, SWT.BORDER);
		dateControl.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
		dateControl.setCalendar(now);
		data = new GridData();
		dateControl.setLayoutData(data);

		timeControl = new DateTime(group, SWT.TIME | SWT.SHORT | SWT.BORDER);
		timeControl.setHours(now.get(Calendar.HOUR_OF_DAY));
		timeControl.setMinutes(now.get(Calendar.MINUTE));
		timeControl.setSeconds(now.get(Calendar.SECOND));
		data = new GridData();
		timeControl.setLayoutData(data);

		pushRadio = new Button(group, SWT.RADIO);
		pushRadio.setText(DialogMessages.SCHEDULEDEPLOYMENTDIALOG_DEPLOY_IMMEDIATELY);
		pushRadio.addSelectionListener(selectionAdaptor);
		data = new GridData();
		data.horizontalSpan = 3;
		pushRadio.setLayoutData(data);

		Label contentsLabel = new Label(composite, SWT.NONE);
		contentsLabel.setText(DialogMessages.SCHEDULEDEPLOYMENTDIALOG_DEPLOYMENT_CONTENTS);
		data = new GridData(GridData.FILL_HORIZONTAL);
		contentsLabel.setLayoutData(data);

		dependencyControl = new DependencyControl(composite, SWT.NONE, true);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 300;
		dependencyControl.setLayoutData(data);

		if (accessPolicies.size() > 0) {
			dependencyControl.addSection(
					DialogMessages.SCHEDULEDEPLOYMENTDIALOG_ACCESS_POLICIES,null, null, accessPolicies);
		}
		if (usagePolicies.size() > 0) {
			dependencyControl.addSection(
					DialogMessages.SCHEDULEDEPLOYMENTDIALOG_POLICIES, null,	null, usagePolicies);
		}
		if (selectedComponents.size() > 0) {
			dependencyControl.addSection(
							DialogMessages.SCHEDULEDEPLOYMENTDIALOG_SELECTED_COMPONENTS,null, null, selectedComponents);
		}
		if (requiredComponents.size() > 0) {
			dependencyControl.addSection(
							DialogMessages.SCHEDULEDEPLOYMENTDIALOG_REQUIRED_COMPONENTS,null, null, requiredComponents);
		}
		if (modifiedComponents.size() > 0) {
			dependencyControl.addSection(
							DialogMessages.SCHEDULEDEPLOYMENTDIALOG_MODIFIED_COMPONENTS,null, null, modifiedComponents);
		}
		dependencyControl.initialize();

		setControl(composite);
	}

	/**
	 * classify objects into three lists: Access policies, usage policies,
	 * selected components
	 * 
	 */
	private void classifyObjects() {
		accessPolicies.clear();
		usagePolicies.clear();
		selectedComponents.clear();
		modifiedComponents.clear();
		Collection<? extends IHasId> entities = PolicyServerProxy.getEntitiesForDescriptor(objectList);
		Map<Long, DomainObjectDescriptor> forId = new HashMap<Long, DomainObjectDescriptor>();
		for (DomainObjectDescriptor descriptor : objectList) {
			forId.put(descriptor.getId(), descriptor);
		}
		for (IHasId hasId : entities) {
			DomainObjectDescriptor descriptor = (DomainObjectDescriptor) forId.get(hasId.getId());
			assert descriptor != null; // There must be 1:1 mapping between the
			// two collections
			if (descriptor.getType() == EntityType.POLICY) {
				IDPolicy p = (IDPolicy) hasId;
				if (p.hasAttribute("access")) {
					accessPolicies.add(new DependencyControl.Dependency(descriptor, false, true));
				} else {
					usagePolicies.add(new DependencyControl.Dependency(	descriptor, false, true));
				}
			} else {
				selectedComponents.add(new DependencyControl.Dependency(descriptor, false, true));
			}
		}
	}

	/**
	 * checks dependencies of the list objects
	 * 
	 * @return false if the object cannot be deployed.
	 */
	public boolean checkDependencies() {
		List<DomainObjectDescriptor> obsoleteObjects = new ArrayList<DomainObjectDescriptor>();
		List<DomainObjectDescriptor> approvedObjects = new ArrayList<DomainObjectDescriptor>();

		for (DomainObjectDescriptor descriptor : objectList) {
			if (descriptor.getStatus() == DevelopmentStatus.APPROVED) {
				approvedObjects.add(descriptor);
			} else if (descriptor.getStatus() == DevelopmentStatus.OBSOLETE) {
				obsoleteObjects.add(descriptor);
			}
		}

		List<DomainObjectDescriptor> obsoleteComponents = new ArrayList<DomainObjectDescriptor>(); // add
		// only
		// components
		// (not policies) to
		// this because policies
		// do not have referring
		// objects
		for (DomainObjectDescriptor descriptor : obsoleteObjects) {
			if (descriptor.getType() != EntityType.POLICY) {
				obsoleteComponents.add(descriptor);
			}
		}

		if (obsoleteComponents.size() > 0) {
			Collection<DomainObjectDescriptor> referringObjects = PolicyServerProxy
					.getAllReferringObjectsAsOf(obsoleteComponents,	new GregorianCalendar().getTime(), true);

			/*
			 * When marking an object obsolete, all policies/components which
			 * refer to it are modified to remove that object. Therefore, when
			 * this obsolete object is "deployed" to undeploy it, the other,
			 * modified objects, are then deployed. Therefore, these
			 * policies/component must all be in the approved state and the
			 * current user must have rights to deploy them
			 */
			if (PolicyServerProxy.filterByAllowedAction(referringObjects,DAction.DEPLOY).size() != referringObjects.size()) {
				return false;
			}

			for (DomainObjectDescriptor descriptor : referringObjects) {
				if (descriptor.getStatus() != DevelopmentStatus.APPROVED) {
					return (false);
				}
			}

			approvedObjects.addAll(referringObjects);

			for (DomainObjectDescriptor descriptor : referringObjects) {
				requiredComponents.add(new DependencyControl.Dependency(descriptor, false, true));
			}
		}

		Collection<DomainObjectDescriptor> dependencies = PolicyServerProxy.getAllDependencies(approvedObjects);
		Collection<DomainObjectDescriptor> deployableDependencies = PolicyServerProxy.filterByAllowedAction(dependencies, DAction.DEPLOY);

		Collection<DomainObjectDescriptor> dependenciesDeployedDescriptors = PolicyServerProxy.getDeployedDescriptors(dependencies);
		for (DomainObjectDescriptor descriptor : dependencies) {
			DomainObjectDescriptor deployedDescriptor = null;
			for (DomainObjectDescriptor tempDescriptor : dependenciesDeployedDescriptors) {
				if (tempDescriptor.getId().equals(descriptor.getId())) {
					deployedDescriptor = tempDescriptor;
					break;
				}
			}

			if (deployedDescriptor == null) {
				if ((descriptor.getStatus() != DevelopmentStatus.APPROVED)|| (!deployableDependencies.contains(descriptor))) {
					return false;
				} else {
					requiredComponents.add(new DependencyControl.Dependency(descriptor, false, true));
				}
			} else {
				if (descriptor.getStatus() == DevelopmentStatus.APPROVED && deployedDescriptor.getVersion() < descriptor.getVersion()) {
					boolean isSelectable = deployableDependencies.contains(descriptor);
					modifiedComponents.add(new DependencyControl.Dependency(descriptor, isSelectable, false));
				}
			}
		}
		return true;
	}

	public List<DomainObjectDescriptor> getSelectedObjects() {
		List<DomainObjectDescriptor> selectedObjects = dependencyControl.getSelection();
		return selectedObjects;
	}

	public Date getDeploymentTime() {
		/*
		 * Removing push for this release if (pushRadio.getSelection()) {
		 * Calendar res = Calendar.getInstance(); res.clear( Calendar.SECOND );
		 * res.clear( Calendar.MILLISECOND ); res.add( Calendar.MINUTE, 1 );
		 * return res.getTime(); } else
		 */

		if (defaultRadio.getSelection()) {
			return defaultDeploymentTime;
		} else {
			Calendar res = new GregorianCalendar();
			res.setTime(dateControl.getCalendar().getTime());
			res.set(Calendar.HOUR_OF_DAY, timeControl.getHours());
			res.set(Calendar.MINUTE, timeControl.getMinutes());
			res.clear(Calendar.SECOND);
			res.clear(Calendar.MILLISECOND);
			return res.getTime();
		}
	}
}
