/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.IMultiContextualEventListener;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.ObjectSelectionDialog;
import com.bluejungle.destiny.policymanager.ui.dialogs.ScheduleDeploymentDialog;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/action/CheckDependenciesAction.
 *          java#2 $
 */

public class DeployAllAction extends Action {

	/**
	 * Constructor
	 * 
	 */
	public DeployAllAction() {
		super();
		setupListeners();
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 */
	public DeployAllAction(String text) {
		super(text);
		setupListeners();
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param image
	 */
	public DeployAllAction(String text, ImageDescriptor image) {
		super(text, image);
		setupListeners();
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param style
	 */
	public DeployAllAction(String text, int style) {
		super(text, style);
		setupListeners();
	}

	@Override
	public void run() {
		Collection<DomainObjectDescriptor> objectsToDeploy = PolicyServerProxy
				.filterByAllowedAction(PolicyServerProxy.getObjectsToDeploy(),
						DAction.DEPLOY);
		if (objectsToDeploy == null || objectsToDeploy.size() == 0) {
			MessageDialog.openInformation(
					Display.getCurrent().getActiveShell(),
					ActionMessages.DEPLOYALLACTION_DEPLOY_ALL,
					ActionMessages.DEPLOYALLACTION_NO_POLICIES);
		} else {
			ObjectSelectionDialog objectSelectionDialog = new ObjectSelectionDialog(
					Display.getCurrent().getActiveShell(), objectsToDeploy,
					ActionMessages.DEPLOYALLACTION_DEPLOY_ALL);
			if (objectSelectionDialog.open() == Window.OK) {
				ScheduleDeploymentDialog dlg = new ScheduleDeploymentDialog(
						Display.getCurrent().getActiveShell(),
						objectSelectionDialog.getSelectedObjects(),
						PolicyServerProxy.getNextDeploymentTime());
				dlg.open();
			}
		}
	}

	/**
     * 
     */
	private void setupListeners() {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		IEventManager eventManager = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
		eventManager.registerListener(new ObjectModifiedEventListener(),
				ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT);
	}

	/**
     * 
     */
	private void refreshEnabledState() {
		boolean hasObjectToDeploy = false;
		hasObjectToDeploy = PolicyServerProxy.hasObjectsToDeploy();
		setEnabled(hasObjectToDeploy);

	}

	private class ObjectModifiedEventListener implements
			IMultiContextualEventListener {

		/**
		 * @see com.bluejungle.destiny.policymanager.event.IMultiContextualEventListener#onEvents(java.util.Set)
		 */
		public void onEvents(Set<IContextualEvent> events) {
			refreshEnabledState();
		}
	}
}
