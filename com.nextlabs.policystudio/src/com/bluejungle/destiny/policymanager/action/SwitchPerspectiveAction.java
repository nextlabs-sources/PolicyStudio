/*
 * Created on Jun 19, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import com.bluejungle.destiny.policymanager.ConfigurableMessages;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyAuthorPerspective;
import com.bluejungle.destiny.policymanager.ui.PolicyManagerPerspective;
import com.bluejungle.destiny.policymanager.ui.PolicyManagerView;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/main/com.nextlabs.policystudio/src/com/bluejungle
 *          /destiny/policymanager/action/SwitchPerspectiveAction.java#3 $
 */

public class SwitchPerspectiveAction extends Action {
	private String id;
	private IPerspectiveDescriptor desc;

	private static String lastId = PolicyAuthorPerspective.ID;
	private static boolean saveStatus, propertiesStatus;
	private static boolean undoStatus, redoStatus, deleteStatus;
	private static boolean historyStatus, deploymentStatus;
	private static boolean modifyStatus, submitStatus, scheduleStatus,
			deployallStatus, deactivateStatus, usageStatus, versionStatus,
			dependenciesStatus, targetStatus, versionHistoryStatus,
			updateStatus;
	private static boolean previewStatus;

	public SwitchPerspectiveAction(String id) {
		this.id = id;
		desc = PlatformUI.getWorkbench().getPerspectiveRegistry()
				.findPerspectiveWithId(id);
		if (desc != null) {
			setText(desc.getLabel());
			setImageDescriptor(desc.getImageDescriptor());
		}
	}

	@Override
	public void run() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		try {
			if (lastId.equals(id)) {
				return;
			}
			PlatformUI.getWorkbench().showPerspective(id, window);
			lastId = id;
			String title = "";
			if (id.equals(PolicyManagerPerspective.ID)) {
				GlobalState.getInstance().saveEditorPanel();
				title = ConfigurableMessages.POLICYMANAGER_TITLE;

				saveStatus = PolicyStudioActionFactory.getSaveAction()
						.isEnabled();
				propertiesStatus = PolicyStudioActionFactory
						.getObjectPropertiesAction().isEnabled();
				PolicyStudioActionFactory.getSaveAction().setEnabled(false);
				PolicyStudioActionFactory.getObjectPropertiesAction()
						.setEnabled(false);

				historyStatus = PolicyStudioActionFactory
						.getDeploymentHistoryAction().isEnabled();
				deploymentStatus = PolicyStudioActionFactory
						.getDeploymentStatusAction().isEnabled();
				PolicyStudioActionFactory.getDeploymentHistoryAction()
						.setEnabled(true);
				PolicyStudioActionFactory.getDeploymentStatusAction()
						.setEnabled(true);

				undoStatus = PolicyStudioActionFactory.getUndoAction()
						.isEnabled();
				redoStatus = PolicyStudioActionFactory.getRedoAction()
						.isEnabled();
				deleteStatus = PolicyStudioActionFactory.getDeleteAction()
						.isEnabled();
				PolicyStudioActionFactory.getUndoAction().setEnabled(false);
				PolicyStudioActionFactory.getRedoAction().setEnabled(false);
				PolicyStudioActionFactory.getDeleteAction().setEnabled(false);

				modifyStatus = PolicyStudioActionFactory.getModifyAction()
						.isEnabled();
				submitStatus = PolicyStudioActionFactory
						.getSubmitForDeploymentAction().isEnabled();
				scheduleStatus = PolicyStudioActionFactory
						.getScheduleDeploymentAction().isEnabled();
				deployallStatus = PolicyStudioActionFactory
						.getDeployAllAction().isEnabled();
				deactivateStatus = PolicyStudioActionFactory
						.getDeactivateAction().isEnabled();
				usageStatus = PolicyStudioActionFactory
						.getShowPolicyUsageAction().isEnabled();
				versionStatus = PolicyStudioActionFactory
						.getShowDeployedVersionAction().isEnabled();
				dependenciesStatus = PolicyStudioActionFactory
						.getCheckDependenciesAction().isEnabled();
				targetStatus = PolicyStudioActionFactory.getSetTargetsAction()
						.isEnabled();
				versionHistoryStatus = PolicyStudioActionFactory
						.getShowVersionHistoryAction().isEnabled();
				updateStatus = PolicyStudioActionFactory
						.getUpdateComputersWithAgentsAction().isEnabled();
				PolicyStudioActionFactory.getModifyAction().setEnabled(false);
				PolicyStudioActionFactory.getSubmitForDeploymentAction()
						.setEnabled(false);
				PolicyStudioActionFactory.getScheduleDeploymentAction()
						.setEnabled(false);
				PolicyStudioActionFactory.getDeployAllAction()
						.setEnabled(false);
				PolicyStudioActionFactory.getDeactivateAction().setEnabled(
						false);
				PolicyStudioActionFactory.getShowPolicyUsageAction()
						.setEnabled(false);
				PolicyStudioActionFactory.getShowDeployedVersionAction()
						.setEnabled(false);
				PolicyStudioActionFactory.getCheckDependenciesAction()
						.setEnabled(false);
				PolicyStudioActionFactory.getSetTargetsAction().setEnabled(
						false);
				PolicyStudioActionFactory.getShowVersionHistoryAction()
						.setEnabled(false);
				PolicyStudioActionFactory.getUpdateComputersWithAgentsAction()
						.setEnabled(true);

				previewStatus = PolicyStudioActionFactory.PREVIEW_ACTION
						.isEnabled();
				PolicyStudioActionFactory.PREVIEW_ACTION.setEnabled(false);
				PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION
						.setEnabled(false);
				PolicyStudioActionFactory.SWITCH_TO_POLICY_AUTHOR_ACTION
						.setEnabled(true);

				PolicyStudioActionFactory.getImportAction().setEnabled(true);
				PolicyStudioActionFactory.getExportAction().setEnabled(true);

				PolicyManagerView.refreshCurrentTab();
			} else if (id.equals(PolicyAuthorPerspective.ID)) {
				title = ConfigurableMessages.POLICYAUTHOR_TITLE;

				PolicyStudioActionFactory.getSaveAction()
						.setEnabled(saveStatus);
				PolicyStudioActionFactory.getObjectPropertiesAction()
						.setEnabled(propertiesStatus);

				PolicyStudioActionFactory.getUndoAction()
						.setEnabled(undoStatus);
				PolicyStudioActionFactory.getRedoAction()
						.setEnabled(redoStatus);
				PolicyStudioActionFactory.getDeleteAction().setEnabled(
						deleteStatus);

				PolicyStudioActionFactory.getDeploymentHistoryAction()
						.setEnabled(historyStatus);
				PolicyStudioActionFactory.getDeploymentStatusAction()
						.setEnabled(deploymentStatus);

				PolicyStudioActionFactory.getModifyAction().setEnabled(
						modifyStatus);
				PolicyStudioActionFactory.getSubmitForDeploymentAction()
						.setEnabled(submitStatus);
				PolicyStudioActionFactory.getScheduleDeploymentAction()
						.setEnabled(scheduleStatus);
				PolicyStudioActionFactory.getDeployAllAction().setEnabled(
						deployallStatus);
				PolicyStudioActionFactory.getDeactivateAction().setEnabled(
						deactivateStatus);
				PolicyStudioActionFactory.getShowPolicyUsageAction()
						.setEnabled(usageStatus);
				PolicyStudioActionFactory.getShowDeployedVersionAction()
						.setEnabled(versionStatus);
				PolicyStudioActionFactory.getCheckDependenciesAction()
						.setEnabled(dependenciesStatus);
				PolicyStudioActionFactory.getSetTargetsAction().setEnabled(
						targetStatus);
				PolicyStudioActionFactory.getShowVersionHistoryAction()
						.setEnabled(versionHistoryStatus);
				PolicyStudioActionFactory.getUpdateComputersWithAgentsAction()
						.setEnabled(updateStatus);

				PolicyStudioActionFactory.PREVIEW_ACTION
						.setEnabled(previewStatus);
				PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION
						.setEnabled(true);
				PolicyStudioActionFactory.SWITCH_TO_POLICY_AUTHOR_ACTION
						.setEnabled(false);

				PolicyStudioActionFactory.getImportAction().setEnabled(false);
				PolicyStudioActionFactory.getExportAction().setEnabled(false);
			}
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
					.setText(title);
			PolicyStudioActionFactory.getDuplicateAction().setEnabled(false);
		} catch (WorkbenchException e) {
			MessageDialog.openError(window.getShell(), "Error",
					"Error opening perspective:" + e.getMessage());
		}
	}
}
