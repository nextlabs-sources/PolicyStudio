/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.controls.CalendarPicker;
import com.bluejungle.destiny.policymanager.ui.controls.DependencyControl;
import com.bluejungle.destiny.policymanager.ui.dialogs.SetDeploymentTargetDialog.Deploy;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/
 *          SubmitCheckDependenciesDialog.java#1 $
 */

public class ScheduleDeploymentDialog extends Dialog {

	private static final IEventManager EVENT_MANAGER;
	static {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		EVENT_MANAGER = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
	}

	private static final Point SIZE = new Point(600, 500);

	private List<DomainObjectDescriptor> objectList = null;
	private List<DependencyControl.Dependency> accessPolicies = new ArrayList<DependencyControl.Dependency>();
	private List<DependencyControl.Dependency> usagePolicies = new ArrayList<DependencyControl.Dependency>();
	private List<DependencyControl.Dependency> selectedComponents = new ArrayList<DependencyControl.Dependency>();
	private List<DependencyControl.Dependency> selectedExceptions = new ArrayList<DependencyControl.Dependency>();	
	private List<DependencyControl.Dependency> requiredComponents = new ArrayList<DependencyControl.Dependency>();
	private List<DependencyControl.Dependency> requiredExceptions = new ArrayList<DependencyControl.Dependency>();
	private List<DependencyControl.Dependency> modifiedComponents = new ArrayList<DependencyControl.Dependency>();
	private List<DependencyControl.Dependency> modifiedExceptions = new ArrayList<DependencyControl.Dependency>();
	private DependencyControl dependencyControl = null;

	private Button defaultRadio, specifyRadio, pushRadio;
	private CalendarPicker dateControl;
	private DateTime timeControl;
	private final Date defaultDeploymentTime;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent shell
	 * @param objectList
	 *            list of objects to deploy
	 * 
	 */
	public ScheduleDeploymentDialog(Shell parent,
			List<DomainObjectDescriptor> objectList, Date defaultDeploymentTime) {
		super(parent);
		this.objectList = objectList;
		this.defaultDeploymentTime = defaultDeploymentTime;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(DialogMessages.SCHEDULEDEPLOYMENTDIALOG_DEPLOY);
		newShell.setSize(SIZE);
		newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
	}

	@Override
	public int open() {
		classifyObjects();
		GlobalState gs = GlobalState.getInstance();
		IHasId domainObject = (IHasId) gs.getCurrentObject();
		
		if (!checkDependencies()) {
			// Should the dialog be opened here directly?
			if ((domainObject instanceof Policy)){
				if (PolicyHelpers.isDACPolicyType(domainObject)){
					DACDeployCheckDependenciesDialog DACdlg = new DACDeployCheckDependenciesDialog(
							getShell(), objectList);
					DACdlg.open();	
				}else{
					DeployCheckDependenciesDialog dlg = new DeployCheckDependenciesDialog(
							getShell(), objectList);
					dlg.open();
				}
			}else{
				DeployCheckDependenciesDialog dlg = new DeployCheckDependenciesDialog(
						getShell(), objectList);
				dlg.open();
			}
			return Window.OK;
		}
		return super.open();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite root = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(root, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(data);
		initialize(container);

		return parent;
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
		selectedExceptions.clear();
		modifiedComponents.clear();
		modifiedExceptions.clear();
		Collection<? extends IHasId> entities = PolicyServerProxy
				.getEntitiesForDescriptor(objectList);
		Map<Long, DomainObjectDescriptor> forId = new HashMap<Long, DomainObjectDescriptor>();
		for (DomainObjectDescriptor descriptor : objectList) {
			forId.put(descriptor.getId(), descriptor);
		}
		for (IHasId hasId : entities) {
			DomainObjectDescriptor descriptor = (DomainObjectDescriptor) forId
					.get(hasId.getId());
			assert descriptor != null; // There must be 1:1 mapping between the
			// two collections
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
				if (descriptor.getType() == EntityType.POLICY){
					selectedExceptions.add(new DependencyControl.Dependency(
							descriptor, false, true));
				}else{
					selectedComponents.add(new DependencyControl.Dependency(
							descriptor, false, true));
				}
			}
		}
	}

	/**
	 * checks dependencies of the list objects
	 * 
	 * @return false if the object cannot be deployed.
	 */
	private boolean checkDependencies() {
		List<DomainObjectDescriptor> obsoleteObjects = new ArrayList<DomainObjectDescriptor>();
		List<DomainObjectDescriptor> approvedObjects = new ArrayList<DomainObjectDescriptor>();

		for (DomainObjectDescriptor descriptor : objectList) {
			if (descriptor.getStatus() == DevelopmentStatus.APPROVED) {
				approvedObjects.add(descriptor);
			} else if (descriptor.getStatus() == DevelopmentStatus.OBSOLETE) {
				obsoleteObjects.add(descriptor);
			}
		}
		List<DomainObjectDescriptor> obsoletePolicies = new ArrayList<DomainObjectDescriptor>(); 
		List<DomainObjectDescriptor> obsoleteComponents = new ArrayList<DomainObjectDescriptor>(); // add
		// only
		// components
		// (not policies) to
		// this because policies
		// do not have referring
		// objects
		for (DomainObjectDescriptor descriptor : obsoleteObjects) {
			if (descriptor.getType() == EntityType.POLICY) {
				obsoletePolicies.add(descriptor);
			}else{
				obsoleteComponents.add(descriptor);
			}
		}
		
		GlobalState gs = GlobalState.getInstance();
		IHasId domainObject = (IHasId) gs.getCurrentObject();
		
		if ((domainObject instanceof Policy)){
			if (PolicyHelpers.isDACPolicyType(domainObject)){
				if (!PolicyHelpers.isSelectedADEnforcer()){
					return false;
				}
			}
		}


		if (obsoleteComponents.size() > 0 ) {
			Collection<DomainObjectDescriptor> referringObjects = PolicyServerProxy
					.getAllReferringObjectsAsOf(obsoleteComponents,
							new GregorianCalendar().getTime(), true);

			/*
			 * When marking an object obsolete, all policies/components which
			 * refer to it are modified to remove that object. Therefore, when
			 * this obsolete object is "deployed" to undeploy it, the other,
			 * modified objects, are then deployed. Therefore, these
			 * policies/component must all be in the approved state and the
			 * current user must have rights to deploy them
			 */
			if (PolicyServerProxy.filterByAllowedAction(referringObjects,
					DAction.DEPLOY).size() != referringObjects.size()) {
				return false;
			}

			for (DomainObjectDescriptor descriptor : referringObjects) {
				if (descriptor.getStatus() != DevelopmentStatus.APPROVED) {
					return (false);
				}
			}
			approvedObjects.addAll(referringObjects);

			for (DomainObjectDescriptor descriptor : referringObjects) {
				if (descriptor.getType() == EntityType.POLICY){
					requiredExceptions.add(new DependencyControl.Dependency(
							descriptor, false, true));
				}else{
					requiredComponents.add(new DependencyControl.Dependency(
							descriptor, false, true));
				}
			}
		}
		Set<DomainObjectDescriptor> dsp = new HashSet<DomainObjectDescriptor> ();
		dsp.addAll(obsoletePolicies);
		Collection<DomainObjectDescriptor> obsoletePolicyDependencies = PolicyHelpers.filterExceptionDependencies(dsp);

		Collection<DomainObjectDescriptor> dependencies = PolicyServerProxy
				.getAllDependencies(approvedObjects);
		Collection<DomainObjectDescriptor> dependenciesDeployedDescriptors = PolicyServerProxy
		.getDeployedDescriptors(dependencies);
		dependencies.addAll(obsoletePolicyDependencies);
		Collection<DomainObjectDescriptor> deployableDependencies = PolicyServerProxy
				.filterByAllowedAction(dependencies, DAction.DEPLOY);


		for (DomainObjectDescriptor descriptor : dependencies) {
			DomainObjectDescriptor deployedDescriptor = null;
			for (DomainObjectDescriptor tempDescriptor : dependenciesDeployedDescriptors) {
				if (tempDescriptor.getId().equals(descriptor.getId())) {
					deployedDescriptor = tempDescriptor;
					break;
				}
			}

			if (deployedDescriptor == null) {
				if (!(isAcceptableStatus(descriptor))
						|| (!deployableDependencies.contains(descriptor))) {
					return false;
				} else {
					if (descriptor.getType() == EntityType.POLICY){
						requiredExceptions.add(new DependencyControl.Dependency(
								descriptor, false, true));
					}else{
						requiredComponents.add(new DependencyControl.Dependency(
								descriptor, false, true));
					}
				}
			} else {
				if ((isAcceptableStatus(descriptor))
						&& deployedDescriptor.getVersion() < descriptor
								.getVersion()) {
					boolean isSelectable = deployableDependencies
							.contains(descriptor);
					if(descriptor.getType() == EntityType.POLICY){
						modifiedExceptions.add(new DependencyControl.Dependency(
								descriptor, isSelectable, false));
					}else{
						modifiedComponents.add(new DependencyControl.Dependency(
								descriptor, isSelectable, false));
					}
				}
			}
		}
		return (true);
	}
	
	protected boolean isAcceptableStatus (DomainObjectDescriptor descriptor){
		if (descriptor.getStatus()==DevelopmentStatus.APPROVED){
			return true;
		}else if (descriptor.getStatus()==DevelopmentStatus.OBSOLETE){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 
	 */
	private void initialize(Composite root) {
		GridLayout layout = new GridLayout();
		root.setLayout(layout);

		Group group = new Group(root, SWT.NONE);
		group
				.setText(DialogMessages.SCHEDULEDEPLOYMENTDIALOG_GROUP_DEPLOYMENT_START_TIME);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		layout = new GridLayout(3, false);
		group.setLayout(layout);

		pushRadio = new Button(group, SWT.RADIO);
		pushRadio
				.setText(DialogMessages.SCHEDULEDEPLOYMENTDIALOG_DEPLOY_IMMEDIATELY);
		data = new GridData();
		data.horizontalSpan = 3;
		pushRadio.setLayoutData(data);

		defaultRadio = new Button(group, SWT.RADIO);
		String defaultTimeStr = DialogMessages.SCHEDULEDEPLOYMENTDIALOG_DEFAULTTIMESTR;
		if (defaultDeploymentTime != null) {
			defaultTimeStr = SimpleDateFormat.getDateTimeInstance(
					DateFormat.SHORT, DateFormat.SHORT).format(
					defaultDeploymentTime);
		}
		defaultRadio.setText(DialogMessages.SCHEDULEDEPLOYMENTDIALOG_DEFAULT
				+ defaultTimeStr);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		defaultRadio.setLayoutData(data);

		specifyRadio = new Button(group, SWT.RADIO);
		specifyRadio.setText(DialogMessages.SCHEDULEDEPLOYMENTDIALOG_SPECIFY);
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

		Label contentsLabel = new Label(root, SWT.NONE);
		contentsLabel
				.setText(DialogMessages.SCHEDULEDEPLOYMENTDIALOG_DEPLOYMENT_CONTENTS);
		data = new GridData(GridData.FILL_HORIZONTAL);
		contentsLabel.setLayoutData(data);

		dependencyControl = new DependencyControl(root, SWT.NONE, true);
		data = new GridData(GridData.FILL_BOTH);
		dependencyControl.setLayoutData(data);

		if (accessPolicies.size() > 0) {
			dependencyControl.addSection(
					DialogMessages.SCHEDULEDEPLOYMENTDIALOG_ACCESS_POLICIES,
					null, null, accessPolicies);
		}
		if (usagePolicies.size() > 0) {
			dependencyControl.addSection(
					DialogMessages.SCHEDULEDEPLOYMENTDIALOG_POLICIES, null,
					null, usagePolicies);
		}
		if (selectedComponents.size() > 0) {
			dependencyControl
					.addSection(
							DialogMessages.SCHEDULEDEPLOYMENTDIALOG_SELECTED_COMPONENTS,
							null, null, selectedComponents);
		}
		if (selectedExceptions.size() > 0) {
			dependencyControl
					.addSection(
							DialogMessages.SCHEDULEDEPLOYMENTDIALOG_SELECTED_EXCEPTIONS,
							null, null, selectedExceptions);
		}
		if (requiredComponents.size() > 0) {
			dependencyControl
					.addSection(
							DialogMessages.SCHEDULEDEPLOYMENTDIALOG_REQUIRED_COMPONENTS,
							null, null, requiredComponents);
		}
		if (requiredExceptions.size() > 0) {
				dependencyControl
						.addSection(
								DialogMessages.SCHEDULEDEPLOYMENTDIALOG_REQUIRED_EXCEPTIONS,
								null, null, requiredExceptions);	
		}
		if (modifiedComponents.size() > 0) {
			dependencyControl
					.addSection(
							DialogMessages.SCHEDULEDEPLOYMENTDIALOG_MODIFIED_COMPONENTS,
							null, null, modifiedComponents);
		}
		if (modifiedExceptions.size() > 0) {
			dependencyControl
					.addSection(
							DialogMessages.SCHEDULEDEPLOYMENTDIALOG_MODIFIED_EXCEPTIONS,
							null, null, modifiedExceptions);
		}
		dependencyControl.initialize();
	}

	@Override
	protected void okPressed() {
		deploy();
		super.okPressed();
	}

	/**
	 * deploy the selected objects
	 */
	protected void deploy() {
		List<DomainObjectDescriptor> selectedObjects = dependencyControl.getSelection();

		Date deploymentTime = PolicyServerProxy.deployObjects(selectedObjects,	getDeploymentTime());
		
		if (pushRadio.getSelection() && deploymentTime != null ) {
        	//if deploymentTime is null, push can't execute
        	//getDeploymentTime always return non-null time (if push).
        	//the only chance to get null is deployOjbects() failed 
        	// or server return a deploymentRecord with asOf time is null but which is not quite possible.
        	// because DeploymentTime object checks if asOf is not null. 
			try {
				PolicyServerProxy.client.executePush(deploymentTime);
			} catch (PolicyEditorException e) {
				e.printStackTrace();
			}
		}

		Set<PolicyOrComponentModifiedEvent> eventsToFire = new HashSet<PolicyOrComponentModifiedEvent>(
				selectedObjects.size());
		for (DomainObjectDescriptor nextObjectDeployed : selectedObjects) {
			PolicyOrComponentModifiedEvent nextEvent = new PolicyOrComponentModifiedEvent(
					nextObjectDeployed);
			eventsToFire.add(nextEvent);
		}

		EVENT_MANAGER.fireEvent(eventsToFire);
	}

	/**
	 * @return time from the date/time controls or current time if push is
	 *         selected
	 */
	protected Date getDeploymentTime() {
		if (pushRadio.getSelection()) {
			Calendar res = new GregorianCalendar();
			return res.getTime();
		}
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