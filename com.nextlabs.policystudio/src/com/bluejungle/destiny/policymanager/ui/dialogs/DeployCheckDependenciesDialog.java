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
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
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

import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.controls.DependencyControl;
import com.bluejungle.destiny.policymanager.ui.controls.DependencyControl.Dependency;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/
 *          DeployCheckDependenciesDialog.java#3 $
 */

@SuppressWarnings("restriction")
public class DeployCheckDependenciesDialog extends Dialog {

	private static final Point SIZE = new Point(600, 500);
	protected List<DomainObjectDescriptor> objectList = null;
	protected List<Dependency> missingComponents = new ArrayList<Dependency>();
	protected List<Dependency> missingExceptions = new ArrayList<Dependency>();
	protected List<Dependency> requiredComponents = new ArrayList<Dependency>();
	protected List<Dependency> requiredExceptions = new ArrayList<Dependency>();	
	protected List<Dependency> modifiedComponents = new ArrayList<Dependency>();
	protected List<Dependency> modifiedExceptions = new ArrayList<Dependency>();
	protected List<Dependency> ongoingModificationsComponents = new ArrayList<Dependency>();
	protected List<Dependency> ongoingModificationsExceptions = new ArrayList<Dependency>();
	protected List<Dependency> missingDeploymentTarget = new ArrayList<Dependency>();
	protected DependencyControl dc;

	/**
	 * Constructor
	 * 
	 * @param parent shell
	 * @param objectList
	 *            list of objects to check dependencies for
	 * 
	 */
	public DeployCheckDependenciesDialog(Shell parent,List<DomainObjectDescriptor> objectList) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.objectList = objectList;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_TITLE);
		newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
		newShell.setSize(SIZE);
		checkDependencies();
	}

	/**
	 * checks dependencies of the list objects and generate lists to show in the
	 * different sections of the dialog
	 */
	private void checkDependencies() {
		List<DomainObjectDescriptor> obsoleteObjects = new ArrayList<DomainObjectDescriptor>();
		List<DomainObjectDescriptor> approvedObjects = new ArrayList<DomainObjectDescriptor>();

		for (int i = 0, n = objectList.size(); i < n; i++) {
			DomainObjectDescriptor descriptor = objectList.get(i);
			if (descriptor.getStatus() == DevelopmentStatus.APPROVED) {
				approvedObjects.add(descriptor);
			} else if (descriptor.getStatus() == DevelopmentStatus.OBSOLETE) {
				obsoleteObjects.add(descriptor);
			}
		}

		/*
		 * Only add components to this because policies do not have referring objects
		 */
		List<DomainObjectDescriptor> obsoletePolicies = new ArrayList<DomainObjectDescriptor>(); 
		List<DomainObjectDescriptor> obsoleteComponents = new ArrayList<DomainObjectDescriptor>(); 
		for (int i = 0, n = obsoleteObjects.size(); i < n; i++) {
			DomainObjectDescriptor descriptor = (DomainObjectDescriptor) obsoleteObjects.get(i);
			if (descriptor.getType() == EntityType.POLICY) {
				obsoletePolicies.add(descriptor);
			}else{
				obsoleteComponents.add(descriptor);
			}
		}

		if (obsoleteComponents.size() > 0) {
			Collection<DomainObjectDescriptor> referringObjects 
				= PolicyServerProxy.getAllReferringObjectsAsOf(obsoleteComponents,new GregorianCalendar().getTime(), true);
			approvedObjects.addAll(referringObjects);

			/*
			 * When marking an object obsolete, all policies/components which
			 * refer to it are modified to remove that object. Therefore, when
			 * this obsolete object is "deployed" to un-deploy it, the other,
			 * modified objects, are then deployed. Therefore, these
			 * policies/component must all be in the approved state and the
			 * current user must have rights to deploy them
			 */
			Collection<DomainObjectDescriptor> permissableReferringObjects = PolicyServerProxy.filterByAllowedAction(referringObjects, DAction.DEPLOY);
			Iterator<DomainObjectDescriptor> referringObjectIterator = referringObjects.iterator();
			while (referringObjectIterator.hasNext()) {
				DomainObjectDescriptor descriptor = (DomainObjectDescriptor) referringObjectIterator.next();
				if ((!permissableReferringObjects.contains(descriptor))	|| (descriptor.getStatus() != DevelopmentStatus.APPROVED)) {
					if (descriptor.getType()== EntityType.POLICY){
						missingExceptions.add(new Dependency(descriptor, false,	false));
					}else{
						missingComponents.add(new Dependency(descriptor, false,	false));
					}
				} else {
					if (descriptor.getType()== EntityType.POLICY){
						requiredExceptions.add(new Dependency(descriptor, false,	false));
					}else{
						requiredComponents.add(new Dependency(descriptor, false,false));
					}
				}
			}
		}
		Set<DomainObjectDescriptor> dsp = new HashSet<DomainObjectDescriptor> ();
		dsp.addAll(obsoletePolicies);
		Collection<DomainObjectDescriptor> obsoletePolicyDependencies = PolicyHelpers.filterExceptionDependencies(dsp);
		
		Collection<DomainObjectDescriptor> dependencies = PolicyServerProxy.getAllDependencies(approvedObjects);
		Collection<DomainObjectDescriptor> dependenciesDeployedDescriptors = PolicyServerProxy.getDeployedDescriptors(dependencies);
		dependencies.addAll(obsoletePolicyDependencies);
		Collection<DomainObjectDescriptor> permissableDependencies = PolicyServerProxy.filterByAllowedAction(dependencies, DAction.DEPLOY);

		Iterator<DomainObjectDescriptor> iterator = dependencies.iterator();
		while (iterator.hasNext()) {
			DomainObjectDescriptor descriptor = (DomainObjectDescriptor) iterator.next();
			Iterator<DomainObjectDescriptor> deployedIterator = dependenciesDeployedDescriptors.iterator();

			DomainObjectDescriptor deployedDescriptor = null;
			while (deployedIterator.hasNext()) {
				DomainObjectDescriptor tempDescriptor = (DomainObjectDescriptor) deployedIterator.next();
				if (tempDescriptor.getId().equals(descriptor.getId())) {
					deployedDescriptor = tempDescriptor;
					break;
				}
			}

			if (deployedDescriptor == null) {
				if (!(isAcceptableStatus(descriptor))|| (!permissableDependencies.contains(descriptor))) {
					if (descriptor.getType()== EntityType.POLICY){
						missingExceptions.add(new Dependency(descriptor, false,	false));
					}else{
						missingComponents.add(new Dependency(descriptor, false,	false));
					}
				} else {
					if (descriptor.getType()== EntityType.POLICY){
						requiredExceptions.add(new Dependency(descriptor, false, false));
					}else{
						requiredComponents.add(new Dependency(descriptor, false, false));
					}
				}
			} else {
				if ((isAcceptableStatus(descriptor)) && deployedDescriptor.getVersion() < descriptor.getVersion()) {
					if (descriptor.getType()== EntityType.POLICY){
						modifiedExceptions.add(new Dependency(descriptor, false,false));
					}else{
						modifiedComponents.add(new Dependency(descriptor, false,false));
					}

				} else if (descriptor.getStatus() != DevelopmentStatus.APPROVED) {
					if (descriptor.getType()== EntityType.POLICY){
						ongoingModificationsExceptions.add(new Dependency(descriptor, false,false));
					}else{
						ongoingModificationsComponents.add(new Dependency(descriptor, false, false));
					}
				}
			}
		}
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
	
	protected void createDialogHeaderBody(CLabel iconLabel, Label labelHeader, Label labelBody){
		if (this.missingComponents.size() > 0) {
			iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_ERROR));
			labelHeader.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MISSING_COMPONENT_HEADER);
			labelBody.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MISSING_COMPONENT_BODY);
		} else if (this.requiredComponents.size() > 0 || this.modifiedComponents.size() > 0) {
			iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
			labelHeader.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_NEW_MODIFIED_HEADER);
			labelBody.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_NEW_MODIFIED_BODY);
		} else {
			iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_INFORMATION));
			labelHeader.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_SAFE_HEADER);
			labelBody.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_SAFE_BODY);
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

		dc = new DependencyControl(container, SWT.NONE, false);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		dc.setLayoutData(data);
		dc.setLabel(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_CHECKED_FOR);
		List<String> nameList = new ArrayList<String>();
		for (int i = 0; i < objectList.size(); i++) {
			DomainObjectDescriptor descriptor = (DomainObjectDescriptor) this.objectList.get(i);
			nameList.add(DomainObjectHelper.getDisplayName(descriptor));
		}
		dc.setNames(nameList);
		createDialogText();
		dc.initialize();

		return parent;
	}
	
	protected void createDialogText(){
		dc.addSection(
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MISSING_COMPONENTS,
				PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_ERROR_TSK),
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MISSING_COMPONENT_INFO,missingComponents);
		dc.addSection(
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MISSING_EXCEPTIONS,
				PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_ERROR_TSK),
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MISSING_EXCEPTION_INFO,missingExceptions);
		dc.addSection(
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_REQUIRED_COMPONENTS,
				PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_NEW_INFO,requiredComponents);
		dc.addSection(
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_REQUIRED_EXCEPTIONS,
				PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_NEW_INFO,requiredExceptions);
		dc.addSection(
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MODIFIED_COMPONENTS,
				PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MODIFIED_INFO,modifiedComponents);
		dc.addSection(
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MODIFIED_EXCEPTIONS,
				PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MODIFIED_INFO,modifiedExceptions);
		dc.addSection(
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_ONGOING_MODIFICATIONS,
				PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_INFO_TSK),
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_ONGOING_MODIFICATION_INFO,ongoingModificationsComponents);
		dc.addSection(
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_ONGOING_MODIFICATIONS_EXCEPTIONS,
				PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_INFO_TSK),
				DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_ONGOING_MODIFICATION_INFO,ongoingModificationsExceptions);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
	
}