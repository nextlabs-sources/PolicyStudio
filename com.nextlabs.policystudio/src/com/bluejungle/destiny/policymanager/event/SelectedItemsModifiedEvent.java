/*
 * Created on Jan 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event;

import java.util.HashSet;
import java.util.Set;

import com.bluejungle.destiny.policymanager.event.defaultimpl.PolicyOrComponentData;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * SelectedItemsModifiedEvent is an {@see
 * com.bluejungle.destiny.policymanager.event.IEvent} implementation associated
 * with the event type, {@see EventType#SELECTED_ITEMS_MODIFIED_EVENT}
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/event/SelectedItemsModifiedEvent
 *          .java#1 $
 */

public class SelectedItemsModifiedEvent implements IEvent {

	private Set<IPolicyOrComponentData> selectedItems;

	/**
	 * Create an instance of SelectedObjectsModifiedEvent
	 * 
	 * @param selectedItems
	 */
	public SelectedItemsModifiedEvent(
			Set<DomainObjectDescriptor> rawSelectedItems) {
		if (rawSelectedItems == null) {
			throw new NullPointerException("selectedItems cannot be null.");
		}

		selectedItems = new HashSet<IPolicyOrComponentData>(rawSelectedItems
				.size());
		for (DomainObjectDescriptor descriptor : rawSelectedItems) {
			IPolicyOrComponentData nextSelectedItemWrapper = new PolicyOrComponentData(
					descriptor);
			selectedItems.add(nextSelectedItemWrapper);
		}
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.event.IEvent#getEventType()
	 */
	public EventType getEventType() {
		return EventType.SELECTED_ITEMS_MODIFIED_EVENT;
	}

	/**
	 * Retrieve the list of selected items as PolicyOrComponentData instances
	 * 
	 * @return the list of selected items as PolicyOrComponentData instances
	 */
	public Set<IPolicyOrComponentData> getSelectedItems() {
		return this.selectedItems;
	}
}
