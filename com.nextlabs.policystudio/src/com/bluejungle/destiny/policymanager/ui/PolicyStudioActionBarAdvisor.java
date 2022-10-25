/*
 * Created on Jan 10, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import com.bluejungle.destiny.policymanager.action.PolicyStudioActionFactory;
import com.bluejungle.destiny.policymanager.action.PreviewAction;
import com.bluejungle.destiny.policymanager.action.RedoAction;
import com.bluejungle.destiny.policymanager.action.SwitchPerspectiveAction;
import com.bluejungle.destiny.policymanager.action.UndoAction;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author bmeng
 */

public class PolicyStudioActionBarAdvisor extends ActionBarAdvisor {
	public static final String M_ACTION = IWorkbenchActionConstants.MENU_PREFIX
			+ ApplicationMessages.MENU_ACTIONS;
	public static final String M_TOOLS = IWorkbenchActionConstants.MENU_PREFIX
			+ ApplicationMessages.MENU_TOOLS;

	private IWorkbenchAction quitAction;

	public PolicyStudioActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		menuBar.add(createFileMenu());
		menuBar.add(createEditMenu());
		menuBar.add(createToolsMenu());
		menuBar.add(createActionMenu());
		menuBar.add(createWindowMenu());
		menuBar.add(createHelpMenu());
	}

	private MenuManager createToolsMenu() {
		MenuManager menu = new MenuManager(ApplicationMessages.MENU_TOOLS,
				M_TOOLS);
		menu.add(PolicyStudioActionFactory.getDeploymentHistoryAction());
		menu.add(PolicyStudioActionFactory.getDeploymentStatusAction());
		return menu;
	}

	private MenuManager createWindowMenu() {
		MenuManager menu = new MenuManager(ApplicationMessages.MENU_WINDOW,
				IWorkbenchActionConstants.M_WINDOW);
		menu.add(PolicyStudioActionFactory.PREVIEW_ACTION);

		menu.add(new Separator());

		menu.add(PolicyStudioActionFactory.SWITCH_TO_POLICY_AUTHOR_ACTION);
		menu.add(PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION);

		return menu;
	}

	private MenuManager createHelpMenu() {
		MenuManager menu = new MenuManager(ApplicationMessages.MENU_HELP,
				IWorkbenchActionConstants.M_HELP); //$NON-NLS-1$
		menu.add(PolicyStudioActionFactory.getShowHelpAction());
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(PolicyStudioActionFactory.getShowAboutAction());
		return menu;
	}

	private MenuManager createFileMenu() {
		MenuManager menu = new MenuManager(ApplicationMessages.MENU_FILE,
				IWorkbenchActionConstants.M_FILE);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		menu.add(PolicyStudioActionFactory.getImportAction());
		menu.add(new Separator());
		// menu.add(PolicyStudioActionFactory.getSaveAction());
		menu.add(PolicyStudioActionFactory.getChangePasswordAction());
		menu.add(PolicyStudioActionFactory.getObjectPropertiesAction());
		menu.add(new Separator());
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(quitAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
		return menu;
	}

	private MenuManager createActionMenu() {
		MenuManager menu = new MenuManager(ApplicationMessages.MENU_ACTIONS,
				M_ACTION);
		menu.add(PolicyStudioActionFactory.getModifyAction());
		menu.add(PolicyStudioActionFactory.getSubmitForDeploymentAction());
		menu.add(PolicyStudioActionFactory.getScheduleDeploymentAction());
		menu.add(PolicyStudioActionFactory.getDeployAllAction());
		menu.add(PolicyStudioActionFactory.getDeactivateAction());
		menu.add(new Separator());
		menu.add(PolicyStudioActionFactory.getShowPolicyUsageAction());
		menu.add(PolicyStudioActionFactory.getShowDeployedVersionAction());
		menu.add(PolicyStudioActionFactory.getCheckDependenciesAction());
		menu.add(PolicyStudioActionFactory.getSetTargetsAction());
		menu.add(PolicyStudioActionFactory.getShowVersionHistoryAction());
		menu.add(new Separator());
		menu
				.add(PolicyStudioActionFactory
						.getUpdateComputersWithAgentsAction());
		return menu;
	}

	private MenuManager createEditMenu() {
		MenuManager menu = new MenuManager(ApplicationMessages.MENU_EDIT,
				IWorkbenchActionConstants.M_EDIT);
		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));

		menu.add(PolicyStudioActionFactory.getUndoAction());
		menu.add(PolicyStudioActionFactory.getRedoAction());
		menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));

		// menu.add(PolicyManagerActionFactory.getCopyAction());
		// menu.add(PolicyManagerActionFactory.getPasteAction());
		menu.add(PolicyStudioActionFactory.getDeleteAction());
		menu.add(new GroupMarker(IWorkbenchActionConstants.ADD_EXT));

		menu.add(new Separator());
		menu.add(PolicyStudioActionFactory.getDuplicateAction());
		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		menu.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				UndoAction undoAction = (UndoAction) PolicyStudioActionFactory
						.getUndoAction();
				RedoAction redoAction = (RedoAction) PolicyStudioActionFactory
						.getRedoAction();
				Boolean inPolicyAuthorView = PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION.isEnabled();
				IHasId current = null;
				if(inPolicyAuthorView){
					current = GlobalState.getInstance().getCurrentObject();
				}else{
					Set<DomainObjectDescriptor> selectedItems = GlobalState.getInstance().getCurrentSelection();
					if (!selectedItems.isEmpty()){
						Iterator<DomainObjectDescriptor> selectedItemsIterator = selectedItems.iterator();
						DomainObjectDescriptor item = (DomainObjectDescriptor) selectedItemsIterator.next();
						current = (IHasId) PolicyServerProxy.getEntityForDescriptor(item);
					}
				}
				if (current != null) {
					undoAction.refreshEnabledState(current);
					redoAction.refreshEnabledState(current);
				} else {
					undoAction.setEnabled(false);
					redoAction.setEnabled(false);
				}
			}

		});
		return menu;
	}

	@Override
	protected void makeActions(IWorkbenchWindow window) {
		quitAction = ActionFactory.QUIT.create(window);
		PolicyStudioActionFactory.PREVIEW_ACTION = new PreviewAction(window,
				ApplicationMessages.TITLE_PREVIEW, PreviewView.ID);

		PolicyStudioActionFactory.SWITCH_TO_POLICY_AUTHOR_ACTION = new SwitchPerspectiveAction(
				PolicyAuthorPerspective.ID);
		PolicyStudioActionFactory.SWITCH_TO_POLICY_AUTHOR_ACTION
				.setEnabled(false);
		PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION = new SwitchPerspectiveAction(
				PolicyManagerPerspective.ID);
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		// ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		// toolBarManager.add(PolicyManagerActionFactory.getSaveAction());
		// toolBarManager.add(new Separator());
		// toolBarManager.add(undoAction);
		// toolBarManager.add(redoAction);
		// toolBarManager.add(new Separator());
		// coolBar.add(toolBarManager);
	}
}
