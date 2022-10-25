package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.SharedImages;

import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author ichiang
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/
 *          DACDeployCheckDependenciesDialog.java #1 $
 */

public class DACDeployCheckDependenciesDialog extends DeployCheckDependenciesDialog {

	public DACDeployCheckDependenciesDialog(Shell parent,
			List<DomainObjectDescriptor> objectList) {
		super(parent, objectList);
	}

	
	@Override
	protected void createDialogHeaderBody(CLabel iconLabel, Label labelHeader, Label labelBody){
			
		if ((this.missingComponents.size() > 0) && PolicyHelpers.isSelectedADEnforcer ()) {
			iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_ERROR));
			labelHeader.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MISSING_COMPONENT_HEADER);
			labelBody.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MISSING_COMPONENT_BODY);
		} else if ((this.missingComponents.size() > 0) && (!(PolicyHelpers.isSelectedADEnforcer ()))) {
			iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_ERROR));
			labelHeader.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MISSING_COMPONENT_HEADER);
			//The object(s) require components which are either currently in development or are not deployable by the current user. These object(s) may not be deployed until these required components are available for deployment.
			//The object(s) has not selected a deployment target
			labelBody.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MISSING_COMPONENT_AUTO_BODY);
		} else if ((this.requiredComponents.size() > 0 || this.modifiedComponents.size() > 0) && PolicyHelpers.isSelectedADEnforcer ()) {
			iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
			labelHeader.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_NEW_MODIFIED_HEADER);
			labelBody.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_NEW_MODIFIED_BODY);
		} else if ((this.requiredComponents.size() > 0 || this.modifiedComponents.size() > 0)&& (!(PolicyHelpers.isSelectedADEnforcer ()))) {
			iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_ERROR));
			labelHeader.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_NEW_MODIFIED_AUTO_HEADER);
			labelBody.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_NEW_MODIFIED_AUTO_BODY);			
		} else {
			if (PolicyHelpers.isSelectedADEnforcer ()){
				iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_INFORMATION));
				labelHeader.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_SAFE_HEADER);
				labelBody.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_SAFE_BODY);
			}else{
				iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_ERROR));
				labelHeader.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_SAFE_AUTO_HEADER);
				labelBody.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_SAFE_AUTO_BODY);
			}
		}
	}
	
	@Override
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
		
		if (!(PolicyHelpers.isSelectedADEnforcer ())){
			dc.addSection(
					DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_REQUIRED_ACTIVE_DIRECTORIES,
					PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_ERROR_TSK),
					DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_REQUIRED_ACTIVE_DIRECTORIES_INFO,missingDeploymentTarget);
		}else{
			dc.addSection(
					DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_REQUIRED_ACTIVE_DIRECTORIES,
					PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
					DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_REQUIRED_ACTIVE_DIRECTORIES_INFO,missingDeploymentTarget);
		}
	}
}
