/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.SharedImages;

import com.bluejungle.destiny.policymanager.action.PolicyStudioActionFactory;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyManagerView;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.controls.DependencyControl;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/
 *          SubmitCheckDependenciesDialog.java#1 $
 */

@SuppressWarnings("restriction")
public class SubmitCheckDependenciesDialog extends Dialog {

	public static final String CHECK_DEPENDENCIES = "CHECK_DEPENDENCIES";
	public static final String SUBMIT = "SUBMIT";

	private static final IEventManager EVENT_MANAGER;
	static {
		IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
		EVENT_MANAGER = componentManager.getComponent(EventManagerImpl.COMPONENT_INFO);
	}

	private static final Point SIZE = new Point(600, 500);
	protected List<DomainObjectDescriptor> objectList = null;
	protected List<DomainObjectDescriptor> requiredComponents = new ArrayList<DomainObjectDescriptor>();
	protected List<DomainObjectDescriptor> requiredExceptions = new ArrayList<DomainObjectDescriptor>();	
	protected List<DomainObjectDescriptor> modifiedComponents = new ArrayList<DomainObjectDescriptor>();
	protected List<DomainObjectDescriptor> modifiedExceptions= new ArrayList<DomainObjectDescriptor>();
	protected List<DomainObjectDescriptor> missingDeploymentTarget = new ArrayList <DomainObjectDescriptor>();
	protected String dialogType = null;
	protected DependencyControl dc;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent shell
	 * @param objectList
	 *            list of objects to check dependencies for
	 * 
	 */
	public SubmitCheckDependenciesDialog(Shell parent, List<DomainObjectDescriptor> objectList, String dialogType) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.objectList = objectList;
		this.dialogType = dialogType;
	}

	/**
	 * Two situations would bring out this window
	 * 1. check dependencies (without submit button on the window)
	 * 2. submit policy (with submit button on the window)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (dialogType.equals(CHECK_DEPENDENCIES))
			newShell.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_TITLE_CHECK);
		else
			newShell.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_TITLE_SUBMIT);
		newShell.setSize(SIZE);
		newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
		checkDependencies();
	}

	/**
	 * checks dependencies of the list objects
	 */
	private void checkDependencies() {
		Collection<DomainObjectDescriptor> dependencies = PolicyServerProxy.getAllDependencies(objectList);
		Collection<DomainObjectDescriptor> dependenciesDeployedDescriptors = PolicyServerProxy.getDeployedDescriptors(dependencies);
			
		for (DomainObjectDescriptor descriptor : dependencies) {
			DomainObjectDescriptor deployedDescriptor = null;
			for (DomainObjectDescriptor tempDescriptor : dependenciesDeployedDescriptors) {
				if (tempDescriptor.getId().equals(descriptor.getId())) {
					deployedDescriptor = tempDescriptor;
					break;
				}
			}

			if (descriptor.getStatus() != DevelopmentStatus.APPROVED&& deployedDescriptor == null) {
				if (descriptor.getType()== EntityType.POLICY){
					requiredExceptions.add(descriptor);
				}else{
					requiredComponents.add(descriptor);					
				}
			} else if (descriptor.getStatus() == DevelopmentStatus.DRAFT) {
				if (descriptor.getType()== EntityType.POLICY){
					modifiedExceptions.add(descriptor);
				}else{
					modifiedComponents.add(descriptor);
				}
			}
		}
	}
	
	protected void createDialogHeaderBody(CLabel iconLabel, Label labelHeader, Label labelBody){
		
		if (objectList.size() == 1) {
			if (dialogType.equals(CHECK_DEPENDENCIES)) {
				// user check dependencies and there has no new component
				if (modifiedComponents.size() == 0 && requiredComponents.size() == 0) {
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
					//The selected object is safe to submit.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_HEADER);
					//This object does not use any components that are currently in development.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_BODY);
				// user check dependencies and there has new component
				} else {
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
					//The selected object uses new or modified components.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_MODIFIED_HEADER);
					//This object uses components which are currently in development. You may still submit this object for deployment, but it may not behave as expected until the issues below are resolved.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_MODIFIED_BODY);
				}				
			} else { // else if (dialogType.equals(SUBMIT))
				//user submit policy and there has no new component
				if (modifiedComponents.size() == 0 && requiredComponents.size() == 0) {
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
					//The selected object is safe to submit.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_HEADER);
					//This object does not use any components that are currently in development.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_BODY);
				//user submit policy and there has new component
				} else {
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
					//The selected object uses new or modified components.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SUBMIT_SINGLE_HEADER);
					//The objects you are submitting use components which are currently in development.  You may still submit these objects for deployment, but they may not behave as expected until the issues below are resolved.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SUBMIT_BODY);
				}
			}
		// how to let objectList.size() (> 1) ||( < 1) ?
		} else {
			if (dialogType.equals(CHECK_DEPENDENCIES)) {
				if (modifiedComponents.size() == 0 && requiredComponents.size() == 0) {
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
					//The selected objects are safe to submit.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MULTIPLE_UNMODIFIED_HEADER);
					//These objects do not use any components that are currently in development.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MULTIPLE_UNMODIFIED_BODY);
				} else {
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
					//The selected objects use new or modified components.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MULTIPLE_MODIFIED_HEADER);
					//These object use components which are currently in development. You may still submit these objects for deployment, but they may not behave as expected until the issues below are resolved.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MULTIPLE_MODIFIED_BODY);
				}
			} else { // else if (dialogType.equals(SUBMIT))
				if (modifiedComponents.size() == 0 && requiredComponents.size() == 0) {
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
					//The selected objects are safe to submit.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MULTIPLE_UNMODIFIED_HEADER);
					//These objects do not use any components that are currently in development.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MULTIPLE_UNMODIFIED_BODY);
				} else {
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
					//Some selected objects use new or modified components.
					labelHeader	.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SUBMIT_MULTIPLE_HEADER);
					//The objects you are submitting use components which are currently in development.  You may still submit these objects for deployment, but they may not behave as expected until the issues below are resolved.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SUBMIT_BODY);
				}
			}
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite root = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(root, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(data);

		CLabel iconLabel = new CLabel(container, SWT.NONE);


		Label labelHeader = new Label(container, SWT.WRAP);
		labelHeader.setFont(FontBundle.ARIAL_12_NORMAL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		labelHeader.setLayoutData(data);

		Label labelBody = new Label(container, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		labelBody.setLayoutData(data);
		
		createDialogHeaderBody(iconLabel, labelHeader, labelBody);

		boolean isCheckDependenciesDialog = dialogType.equals(CHECK_DEPENDENCIES);
		dc = new DependencyControl(container, SWT.NONE, !isCheckDependenciesDialog);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		dc.setLayoutData(data);
		if (isCheckDependenciesDialog) {
			dc.setLabel(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_CHECKED_FOR);
		} else {
			dc.setLabel(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_OBJECTS_TO_SUBMIT);
		}

		List<String> nameList = new ArrayList<String>();
		for (DomainObjectDescriptor descriptor : objectList) {
			nameList.add(DomainObjectHelper.getDisplayName(descriptor));
		}
		dc.setNames(nameList);
		createDialogText();

		return parent;
	}
	
	protected void createDialogText(){
		List<DependencyControl.Dependency> requiredComponentsDependencies = buildDependenciesList(requiredComponents);
		dc.addSection(
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_REQUIRED_COMPONENTS,
						PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_REQUIRED_COMPONENTS_DESC,requiredComponentsDependencies);
		
		List<DependencyControl.Dependency> requiredExceptionsDependencies = buildDependenciesList(requiredExceptions);
		dc.addSection(
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_REQUIRED_EXCEPTIONS,
						PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_REQUIRED_EXCEPTIONS_DESC,requiredExceptionsDependencies);
		
		List<DependencyControl.Dependency> modifiedComponentsDependencies = buildDependenciesList(modifiedComponents);
		dc.addSection(
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MODIFIED_COMPONENTS,
						PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MODIFIED_COMPONENTS_DESC, modifiedComponentsDependencies);
		List<DependencyControl.Dependency> modifiedExceptionsDependencies = buildDependenciesList(modifiedExceptions);
		dc.addSection(
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MODIFIED_EXCEPTIONS,
						PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MODIFIED_EXCEPTIONS_DESC, modifiedExceptionsDependencies);
		dc.initialize();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		boolean isCheckDependenciesDialog = dialogType.equals(CHECK_DEPENDENCIES);

		if (isCheckDependenciesDialog) {
			createButton(parent, IDialogConstants.CANCEL_ID,IDialogConstants.OK_LABEL, true);
		} else {
			createButton(parent, IDialogConstants.OK_ID,DialogMessages.LABEL_SUBMIT, true);
			createButton(parent, IDialogConstants.CANCEL_ID,IDialogConstants.CANCEL_LABEL, false);
		}
	}

	@Override
	protected void okPressed() {
		submit();
		super.okPressed();
		Boolean inPolicyAuthorView = PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION.isEnabled();
		if (!inPolicyAuthorView){
			PolicyManagerView.refreshCurrentTab();
		}
	}

	/**
	 * submits objects along with the selected objects.
	 */
	protected void submit() {
		List<DomainObjectDescriptor> submitList = new ArrayList<DomainObjectDescriptor>(objectList);
		submitList.addAll(dc.getSelection());
		Collection<? extends IHasId> objectsToSubmit = PolicyServerProxy.getEntitiesForDescriptor(submitList);

		objectsToSubmit = PolicyServerProxy.getEditedEntitiesMatching(objectsToSubmit);

		for (IHasId item : objectsToSubmit) {
			DomainObjectHelper.setStatus(item, DevelopmentStatus.APPROVED);
		}

		List<DODDigest> digests = PolicyServerProxy.saveEntitiesDigest(objectsToSubmit);
		EntityInfoProvider.refreshDescriptors(digests);

		Set<PolicyOrComponentModifiedEvent> eventsToFire = new HashSet<PolicyOrComponentModifiedEvent>();
		for (IHasId item : objectsToSubmit) {
			PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent(item);
			eventsToFire.add(objectModifiedEvent);
		}

		EVENT_MANAGER.fireEvent(eventsToFire);
	}

	/**
	 * Build the dependency list to be passed to the Dependency Control
	 * 
	 * @return the dependency list to be passed to the Dependency Control
	 */
	protected List<DependencyControl.Dependency> buildDependenciesList(List<DomainObjectDescriptor> dependentComponents) {
		if (dependentComponents == null) {
			throw new NullPointerException("dependentComponents cannot be null.");
		}

		Collection<DomainObjectDescriptor> selectableDependentComponents;
		if (SUBMIT.equals(dialogType)) {
			selectableDependentComponents = PolicyServerProxy.filterByAllowedAction(dependentComponents, DAction.APPROVE);
		} else {
			selectableDependentComponents = Collections.emptySet();
		}

		List<DependencyControl.Dependency> dependenciesList = new ArrayList<DependencyControl.Dependency>();
		for (DomainObjectDescriptor nextDependentComponent : dependentComponents) {
			boolean isSelectable = selectableDependentComponents.contains(nextDependentComponent);
			DependencyControl.Dependency nextDependency = new DependencyControl.Dependency(
					nextDependentComponent, isSelectable& nextDependentComponent.isAccessible(),isSelectable);
			dependenciesList.add(nextDependency);
		}
		return dependenciesList;
	}
}
