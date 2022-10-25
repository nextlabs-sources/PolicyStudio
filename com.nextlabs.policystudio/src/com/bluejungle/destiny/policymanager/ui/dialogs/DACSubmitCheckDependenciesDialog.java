package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.util.List;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.SharedImages;

import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.controls.DependencyControl;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author ichiang
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/
 *          DACSubmitCheckDependenciesDialog.java #1 $
 */

public class DACSubmitCheckDependenciesDialog extends SubmitCheckDependenciesDialog {
	
	
	public DACSubmitCheckDependenciesDialog(Shell parent, List<DomainObjectDescriptor> objectList, String dialogType) {
		super(parent, objectList, dialogType);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		boolean isCheckDependenciesDialog = dialogType.equals(CHECK_DEPENDENCIES);

		if ((isCheckDependenciesDialog)
				||((!isCheckDependenciesDialog)&& !(PolicyHelpers.isSelectedADEnforcer ()))){
			createButton(parent, IDialogConstants.CANCEL_ID,IDialogConstants.OK_LABEL, true);
		} else if(PolicyHelpers.isSelectedADEnforcer ()) {
			createButton(parent, IDialogConstants.OK_ID,DialogMessages.LABEL_SUBMIT, true);
			createButton(parent, IDialogConstants.CANCEL_ID,IDialogConstants.CANCEL_LABEL, false);
		}
	}
	
	@Override
	protected void createDialogHeaderBody(CLabel iconLabel, Label labelHeader, Label labelBody){
		
		if (objectList.size() == 1) {
			if (dialogType.equals(CHECK_DEPENDENCIES)) {
				if ((modifiedComponents.size() == 0 && requiredComponents.size() == 0) && (PolicyHelpers.isSelectedADEnforcer ())) {
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
					//The selected object is safe to submit.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_MANUAL_HEADER);
					//This object does not use any components that are currently in development and the object has selected the deployment target.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_MANUAL_BODY);
				} else if ((modifiedComponents.size() == 0 && requiredComponents.size() == 0) && !(PolicyHelpers.isSelectedADEnforcer ())){
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_ERROR));
					//The selected object has not selected a deployment target.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_AUTO_HEADER);
					//This object has not selected a deployment target. You can not submit this object for deployment.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_AUTO_BODY);
				} else if ((modifiedComponents.size() != 0 || requiredComponents.size() != 0) && PolicyHelpers.isSelectedADEnforcer ()){
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
					//The selected object uses new or modified components.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_MODIFIED_MANUAL_HEADER);
					//This object uses components which are currently in development. You may still submit this object for deployment, but it may not behave as expected until the issues below are resolved.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_MODIFIED_MANUAL_BODY);
				} else if ((modifiedComponents.size() != 0 || requiredComponents.size() != 0) && !(PolicyHelpers.isSelectedADEnforcer ())){
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_ERROR));
					//The selected object uses new or modified components and the object has not selected the deployment target.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_MODIFIED_AUTO_HEADER);
					//This object uses components which are currently in development and the object has not selected a deployment target. You can not submit this object for deployment.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_MODIFIED_AUTO_BODY);
				}				
			} 
			else { // else if (dialogType.equals(SUBMIT))
				if ((modifiedComponents.size() == 0 && requiredComponents.size() == 0) && PolicyHelpers.isSelectedADEnforcer ()) {
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
					//The selected object is safe to submit.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_MANUAL_HEADER);
					//This object does not use any components that are currently in development and the object has selected the deployment target.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_MANUAL_BODY);
				} else if ((modifiedComponents.size() == 0 && requiredComponents.size() == 0) && !(PolicyHelpers.isSelectedADEnforcer ())){
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_ERROR));
					//The selected object has not selected a deployment target.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_AUTO_HEADER);
					//This object has not selected a deployment target. You can not submit this object for deployment.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_AUTO_BODY);
				} else if ((modifiedComponents.size() != 0 || requiredComponents.size() != 0) && PolicyHelpers.isSelectedADEnforcer ()){
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
					//The selected object uses new or modified components.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_MODIFIED_MANUAL_HEADER);
					//This object uses components which are currently in development. You may still submit this object for deployment, but it may not behave as expected until the issues below are resolved.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_MODIFIED_MANUAL_BODY);
				} else if ((modifiedComponents.size() != 0 || requiredComponents.size() != 0) && !(PolicyHelpers.isSelectedADEnforcer ())){
					iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_ERROR));
					//The selected object uses new or modified components and the object has not selected the deployment target.
					labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_MODIFIED_AUTO_HEADER);
					//This object uses components which are currently in development and the object has not selected a deployment target. You can not submit this object for deployment.
					labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_MODIFIED_AUTO_BODY);
				}		
			}
		} 
	}
	
	@Override
	protected void createDialogText(){
		List<DependencyControl.Dependency> requiredDependencies = buildDependenciesList(requiredComponents);
		dc.addSection(
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_REQUIRED_COMPONENTS,
						PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_REQUIRED_COMPONENTS_DESC,requiredDependencies);
		List<DependencyControl.Dependency> requiredExceptionsDependencies = buildDependenciesList(requiredExceptions);
		dc.addSection(
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_REQUIRED_EXCEPTIONS,
						PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_REQUIRED_EXCEPTIONS_DESC,requiredExceptionsDependencies);
		List<DependencyControl.Dependency> modifiedDependencies = buildDependenciesList(modifiedComponents);
		dc.addSection(
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MODIFIED_COMPONENTS,
						PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MODIFIED_COMPONENTS_DESC, modifiedDependencies);
		List<DependencyControl.Dependency> modifiedExceptionsDependencies = buildDependenciesList(modifiedExceptions);
		dc.addSection(
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MODIFIED_EXCEPTIONS,
						PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
						DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MODIFIED_EXCEPTIONS_DESC, modifiedExceptionsDependencies);
		
		if (!(PolicyHelpers.isSelectedADEnforcer ())){
			List<DependencyControl.Dependency>  deploymentTargetDependencies= buildDependenciesList(missingDeploymentTarget);
			dc.addSection(
							DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_REQUIRED_ACTIVE_DIRECTORIES,
							PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_ERROR_TSK),
							DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_REQUIRED_ACTIVE_DIRECTORIES_DESC, deploymentTargetDependencies);	
			dc.initialize();
		}else{
			List<DependencyControl.Dependency>  deploymentTargetDependencies= buildDependenciesList(missingDeploymentTarget);
			dc.addSection(
							DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_REQUIRED_ACTIVE_DIRECTORIES,
							PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
							DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_REQUIRED_ACTIVE_DIRECTORIES_DESC, deploymentTargetDependencies);	
			dc.initialize();
		}


	}
}

