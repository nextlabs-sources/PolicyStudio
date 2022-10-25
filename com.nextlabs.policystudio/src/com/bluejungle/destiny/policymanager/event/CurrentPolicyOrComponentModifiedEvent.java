/*
 * Created on Jan 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event;

/**
 * CurrentPolicyOrComponentModifiedEvent is an IEvent implementation utilized to
 * fire events of type {@see
 * com.bluejungle.destiny.policymanager.event.EventType#
 * CURRENT_POLICY_OR_COMPONENT_MODIFIED_EVENT}
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/event/
 *          CurrentPolicyOrComponentModifiedEvent.java#1 $
 */

public class CurrentPolicyOrComponentModifiedEvent implements IEvent {

	private IPolicyOrComponentData currentObject;

	/**
	 * Create an instance of CurrentObjectModifiedEvent
	 * 
	 * @param objectModifiedEventData
	 */
	public CurrentPolicyOrComponentModifiedEvent(
			IPolicyOrComponentData objectModifiedEventData) {
		if (objectModifiedEventData == null) {
			throw new NullPointerException(
					"objectModifiedEventData cannot be null.");
		}

		this.currentObject = objectModifiedEventData;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.event.IEvent#getEventType()
	 */
	public EventType getEventType() {
		return EventType.CURRENT_POLICY_OR_COMPONENT_MODIFIED_EVENT;
	}

	/**
	 * Retrieve the current object.
	 * 
	 * @return the current object.
	 */
	public IPolicyOrComponentData getCurrentObject() {
		return this.currentObject;
	}
}
