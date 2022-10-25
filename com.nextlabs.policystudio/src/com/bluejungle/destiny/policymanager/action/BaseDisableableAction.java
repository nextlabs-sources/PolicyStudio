/*
 * Created on Nov 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.bluejungle.destiny.policymanager.event.EventType;
import com.bluejungle.destiny.policymanager.event.IEvent;
import com.bluejungle.destiny.policymanager.event.IEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.event.SelectedItemsModifiedEvent;
import com.bluejungle.destiny.policymanager.event.SelectionChangedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * Base class for actions which can be disabled based on the items that are
 * selected in the policy author
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/action/BaseDisableableAction.java#1
 *          $
 */

public class BaseDisableableAction extends Action {

	/**
	 * 
	 * Create an instance of BaseDisableableAction which refreshes active state
	 * on the {@see EventType#SELECTION_CHANGED_EVENT} only
	 */
	protected BaseDisableableAction() {
		setupListeners();
	}

	/**
	 * Create an instance of BaseDisableableAction which refreshes active state
	 * on the {@see EventType#SELECTION_CHANGED_EVENT} only
	 * 
	 * @param text
	 */
	protected BaseDisableableAction(String text) {
		super(text);
		setupListeners();
	}

	/**
	 * Create an instance of BaseDisableableAction which refreshes active state
	 * on the {@see EventType#SELECTION_CHANGED_EVENT} only
	 * 
	 * @param text
	 * @param image
	 */
	public BaseDisableableAction(String text, ImageDescriptor image) {
		super(text, image);
		setupListeners();
	}

	/**
	 * Create an instance of BaseDisableableAction which refreshes active state
	 * on the {@see EventType#SELECTION_CHANGED_EVENT} only
	 * 
	 * @param text
	 * @param style
	 */
	public BaseDisableableAction(String text, int style) {
		super(text, style);
		setupListeners();
	}

	private void setupListeners() {
		// action is not available by default
		setEnabled(false);

		// add a listeners for each event to refresh state
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		IEventManager eventManager = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);

		StateChangeListener stateChangeListener = new StateChangeListener();
		eventManager.registerListener(stateChangeListener,
				EventType.SELECTED_ITEMS_MODIFIED_EVENT);
		eventManager.registerListener(stateChangeListener,
				EventType.SELECTION_CHANGED_EVENT);
	}

	/**
	 * Refresh the enabled state after item selection has changed. By default,
	 * enables action when only one item is selected. Disabled otherwise
	 * 
	 * @param selectedItems
	 *            the Set of selected items as {@see
	 *            com.bluejungle.destiny.policymanager.event.PolicyOrComponentData
	 *            * } instances
	 * 
	 */
	protected void refreshEnabledState(Set<IPolicyOrComponentData> selectedItems) {
		setEnabled(selectedItems.size() == 1);
	}

	/**
	 * Retrieve the currently selected items
	 * 
	 * @return the currently selected items
	 */
	protected final Set<DomainObjectDescriptor> getSelectedItems() {
		return GlobalState.getInstance().getCurrentSelection();
	}

	/**
	 * @author sgoldstein
	 */
	private class StateChangeListener implements IEventListener {

		/**
		 * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
		 */
		public void onEvent(IEvent event) {
			Set<IPolicyOrComponentData> selectedItems = null;
			if (event instanceof SelectionChangedEvent) {
				selectedItems = ((SelectionChangedEvent) event)
						.getSelectedItems();
			} else if (event instanceof SelectedItemsModifiedEvent) {
				selectedItems = ((SelectedItemsModifiedEvent) event)
						.getSelectedItems();
			} else {
				throw new IllegalArgumentException("Unknown event class, "
						+ event.getClass());
			}

			BaseDisableableAction.this.refreshEnabledState(selectedItems);
		}
	}
}
