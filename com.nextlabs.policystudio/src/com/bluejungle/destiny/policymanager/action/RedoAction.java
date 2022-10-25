/*
 * Created on Mar 3, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;

import com.bluejungle.destiny.policymanager.event.CurrentPolicyOrComponentChangedEvent;
import com.bluejungle.destiny.policymanager.event.CurrentPolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.EventType;
import com.bluejungle.destiny.policymanager.event.IEvent;
import com.bluejungle.destiny.policymanager.event.IEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.UndoInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;

/**
 * FIX ME - Move undo info outside of GlobalState!
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates
 *          .xml#2 $:
 */

@SuppressWarnings("restriction")
public class RedoAction extends BaseDisableableAction {

	/**
	 * Constructor
	 * 
	 */
	public RedoAction() {
		super(WorkbenchMessages.Workbench_redo);
		// Add a listeners for each event to refresh state
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		IEventManager eventManager = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
		eventManager.registerListener(new CurrentObjectChangedListener(),
				EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT);
		eventManager.registerListener(new CurrentObjectModifiedListener(),
				EventType.CURRENT_POLICY_OR_COMPONENT_MODIFIED_EVENT);
	}

	@Override
	public String getToolTipText() {
		return WorkbenchMessages.Workbench_redoToolTip;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_TOOL_REDO);
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_TOOL_REDO_DISABLED);
	}

	@Override
	public void run() {
		GlobalState gs = GlobalState.getInstance();
		IHasId currentObject = gs.getCurrentObject();
		if (this.canRedo(currentObject)) {
			UndoInfo info = (UndoInfo) gs.undoInfoMap.get((currentObject)
					.getId());
			info.redo(currentObject);
		}
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.action.BaseDisableableAction#refreshEnabledState()
	 */
	public void refreshEnabledState(IHasId currentObject) {
		boolean enabled = canRedo(currentObject);

		setEnabled(enabled);
	}

	/**
	 * @return
	 */
	private boolean canRedo(IHasId currentObject) {
		boolean enabled = false;

		GlobalState globalState = GlobalState.getInstance();
		if (currentObject != null) {
			UndoInfo undoInfo = (UndoInfo) globalState.undoInfoMap
					.get(currentObject.getId());
			if (undoInfo != null) {
				enabled = undoInfo.canRedo();
			}
		}
		return enabled;
	}

	private class CurrentObjectChangedListener implements IEventListener {

		/**
		 * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
		 */
		public void onEvent(IEvent event) {
			CurrentPolicyOrComponentChangedEvent currentObjectChangedEvent = (CurrentPolicyOrComponentChangedEvent) event;
			if (currentObjectChangedEvent.currentObjectExists()) {
				IHasId currentObject = currentObjectChangedEvent
						.getNewCurrentObject().getEntity();
				RedoAction.this.refreshEnabledState(currentObject);
			} else {
				RedoAction.this.setEnabled(false);
			}
		}
	}

	private class CurrentObjectModifiedListener implements IEventListener {

		/**
		 * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
		 */
		public void onEvent(IEvent event) {
			CurrentPolicyOrComponentModifiedEvent currentObjectModifiedEvent = (CurrentPolicyOrComponentModifiedEvent) event;
			IHasId currentObject = currentObjectModifiedEvent
					.getCurrentObject().getEntity();
			RedoAction.this.refreshEnabledState(currentObject);
		}

	}
}
