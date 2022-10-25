/*
 * Created on Jan 23, 2006
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
 * SelectionChangedEvent is an {@see
 * com.bluejungle.destiny.policymanager.event.IEvent} implementation associated
 * with the event type, {@see EventType#SELECTION_CHANGED_EVENT}
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/event/SelectionChangedEvent.java#1
 *          $
 */

public class SelectionChangedEvent implements IEvent {

	private Set<IPolicyOrComponentData> selectedItems;

	/**
	 * Create an instance of SelectionChangedEvent
	 * 
	 * @param rawSelectedItems
	 *            the list of selected items as DomainObjectDescriptor instances
	 */
	public SelectionChangedEvent(Set<DomainObjectDescriptor> rawSelectedItems) {
		// FIX ME - This is copied in SelectedObjectsModifiedEvent. Can we reuse
		// this?!
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
		return EventType.SELECTION_CHANGED_EVENT;
	}

	/**
	 * Retrieve the list of selected items as PolicyOrComponentData instances
	 * 
	 * @return the list of selected items as PolicyOrComponentData instances
	 */
	public Set<IPolicyOrComponentData> getSelectedItems() {
		return selectedItems;
	}
}
