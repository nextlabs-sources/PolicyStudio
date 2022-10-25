/*
 * Created on Mar 3, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import org.eclipse.jface.action.IAction;

/**
 * @author fuad
 */

public class PolicyStudioActionFactory {

	private static final UndoAction UNDO_ACTION = new UndoAction();
	private static final RedoAction REDO_ACTION = new RedoAction();
	private static final CopyAction COPY_ACTION = new CopyAction(
			ActionMessages.ACTION_COPY);
	private static final CutAction CUT_ACTION = new CutAction();
	private static final PasteAction PASTE_ACTION = new PasteAction(
			ActionMessages.ACTION_PASTE);
	private static final SaveAction SAVE_ACTION = new SaveAction(
			ActionMessages.ACTION_SAVE);
	private static final SetTargetsAction SET_TARGETS_ACTION = new SetTargetsAction(
			ActionMessages.ACTION_SET_DEPLOYMENT_TARGET);
	private static final CheckDependenciesAction CHECK_DEPENDENCIES_ACTION = new CheckDependenciesAction(
			ActionMessages.ACTION_CHECK_DEPENDENCIES);
	private static final SubmitForDeploymentAction SUBMIT_ACTION = new SubmitForDeploymentAction(
			ActionMessages.ACTION_SUBMIT);
	private static final ScheduleDeploymentAction SCHEDULE_ACTION = new ScheduleDeploymentAction(
			ActionMessages.ACTION_DEPLOY);
	private static final DeploymentHistoryAction HISTORY_ACTION = new DeploymentHistoryAction(
			ActionMessages.ACTION_DEPLOYMENT_HISTORY);
	private static final ObjectPropertiesAction OBJECT_PROPERTIES_ACTION = new ObjectPropertiesAction(
			ActionMessages.ACTION_PROPERTIES);
	private static final ModifyAction MODIFY_ACTION = new ModifyAction(
			ActionMessages.ACTION_MODIFY);
	private static final DeactivateAction DEACTIVATE_ACTION = new DeactivateAction(
			ActionMessages.ACTION_DEACTIVATE);
	private static final DeleteAction DELETE_ACTION = new DeleteAction(
			ActionMessages.ACTION_DELETE);
	private static final ShowDeployedVersionAction SHOW_DEPLOYED_VERSION_ACTION = new ShowDeployedVersionAction(
			ActionMessages.ACTION_SHOW_DEPLOYED_VERSION);
	private static final ShowPolicyUsageAction SHOW_POLICY_USAGE_ACTION = new ShowPolicyUsageAction(
			ActionMessages.ACTION_SHOW_POLICY_USAGE);
	private static final DeployAllAction DEPLOY_ALL_ACTION = new DeployAllAction(
			ActionMessages.ACTION_DEPLOY_ALL);
	private static final DeploymentStatusAction DEPLOY_STATUS_ACTION = new DeploymentStatusAction(
			ActionMessages.ACTION_DEPLOYMENT_STATUS);
	private static final ShowVersionHistoryAction SHOW_VERSION_HISTORY_ACTION = new ShowVersionHistoryAction(
			ActionMessages.ACTION_VERSION_HISTORY);
	private static final VersionHistoryAction VERSION_HISTORY_ACTION = new VersionHistoryAction(
			ActionMessages.ACTION_VERSION_HISTORY);
	private static final ShowAboutAction SHOW_ABOUT_ACTION = new ShowAboutAction(
			ActionMessages.ACTION_ABOUT);
	private static final ChangePasswordAction CHANGE_PASSWORD_ACTION = new ChangePasswordAction(
			ActionMessages.ACTION_CHANGE_PASSWORD);
	private static final ShowHelpAction SHOW_HELP_ACTION = new ShowHelpAction(
			ActionMessages.ACTION_DISPLAY_HELP);
	private static final IAction UPDATE_COMPUTERS_WITH_AGENTS_ACTION = new UpdateComputersWithAgentsAction(
			ActionMessages.ACTION_UPDATE_COMPUTERS);
	private static final SwitchAndOpenAction SWITCH_AND_OPEN_ACTION = new SwitchAndOpenAction(
			ActionMessages.ACTION_OPEN);
	public static PreviewAction PREVIEW_ACTION;
	public static SwitchPerspectiveAction SWITCH_TO_POLICY_AUTHOR_ACTION;
	public static SwitchPerspectiveAction SWITCH_TO_POLICY_MANAGER_ACTION;
	private static final ImportAction IMPORT_ACTION = new ImportAction();
	private static final AbstractExportAction EXPORT_ACTION = new ExportAction();
	private static final AbstractExportAction EXPORT_XACML_ACTION = new ExportXacmlAction();
	private static final DuplicateAction DUPLICATE_ACTION = new DuplicateAction();

	public static IAction getUndoAction() {
		return UNDO_ACTION;
	}

	public static IAction getRedoAction() {
		return REDO_ACTION;
	}

	public static IAction getCopyAction() {
		return COPY_ACTION;
	}

	public static IAction getCutAction() {
		return CUT_ACTION;
	}

	public static IAction getPasteAction() {
		return PASTE_ACTION;
	}

	public static IAction getSaveAction() {
		return SAVE_ACTION;
	}

	public static IAction getSetTargetsAction() {
		return SET_TARGETS_ACTION;
	}

	public static IAction getCheckDependenciesAction() {
		return CHECK_DEPENDENCIES_ACTION;
	}

	public static IAction getSubmitForDeploymentAction() {
		return SUBMIT_ACTION;
	}

	public static IAction getScheduleDeploymentAction() {
		return SCHEDULE_ACTION;
	}

	public static IAction getDeploymentHistoryAction() {
		return HISTORY_ACTION;
	}

	public static IAction getObjectPropertiesAction() {
		return OBJECT_PROPERTIES_ACTION;
	}

	public static IAction getModifyAction() {
		return MODIFY_ACTION;
	}

	public static IAction getDeactivateAction() {
		return DEACTIVATE_ACTION;
	}

	public static IAction getDeleteAction() {
		return DELETE_ACTION;
	}

	public static IAction getShowDeployedVersionAction() {
		return SHOW_DEPLOYED_VERSION_ACTION;
	}

	public static VersionHistoryAction getVersionHistoryAction() {
		return VERSION_HISTORY_ACTION;
	}

	public static IAction getShowPolicyUsageAction() {
		return SHOW_POLICY_USAGE_ACTION;
	}

	public static IAction getDeployAllAction() {
		return DEPLOY_ALL_ACTION;
	}

	public static IAction getDeploymentStatusAction() {
		return DEPLOY_STATUS_ACTION;
	}

	public static IAction getShowVersionHistoryAction() {
		return SHOW_VERSION_HISTORY_ACTION;
	}

	public static IAction getShowAboutAction() {
		return SHOW_ABOUT_ACTION;
	}

	public static IAction getChangePasswordAction() {
		return CHANGE_PASSWORD_ACTION;
	}

	public static SwitchAndOpenAction getSwitchAndOpenAction() {
		return SWITCH_AND_OPEN_ACTION;
	}

	public static IAction getShowHelpAction() {
		return SHOW_HELP_ACTION;
	}

	public static IAction getUpdateComputersWithAgentsAction() {
		return UPDATE_COMPUTERS_WITH_AGENTS_ACTION;
	}

	public static IAction getImportAction() {
		return IMPORT_ACTION;
	}

	public static AbstractExportAction getExportAction() {
		return EXPORT_ACTION;
	}

	public static AbstractExportAction getExportXacmlAction() {
		return EXPORT_XACML_ACTION;
	}

	public static IAction getDuplicateAction() {
		return DUPLICATE_ACTION;
	}
}
