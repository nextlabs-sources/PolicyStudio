/*
 * Created on Jan 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event;

import com.bluejungle.destiny.policymanager.event.defaultimpl.PolicyOrComponentData;
import com.bluejungle.framework.domain.IHasId;

/**
 * CurrentPolicyOrComponentChangedEvent is an IEvent implementation utilized to
 * fire events of type {@see
 * com.bluejungle.destiny.policymanager.event.EventType#
 * CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT}
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/event/
 *          CurrentPolicyOrComponentChangedEvent.java#1 $
 */
public class CurrentPolicyOrComponentChangedEvent implements IEvent {

	private IPolicyOrComponentData newCurrentObject;

	/**
	 * Create an instance of CurrentObjectChangedEvent, specifying the new
	 * current object
	 * 
	 * @param domainObject
	 */
	public CurrentPolicyOrComponentChangedEvent(IHasId domainObject) {
		if (domainObject == null) {
			throw new NullPointerException("domainObject cannot be null.");
		}

		this.newCurrentObject = new PolicyOrComponentData(domainObject);
	}

	/**
	 * Create an instance of CurrentPolicyOrComponentChangedEvent indicating
	 * that there is no object currently open
	 */
	public CurrentPolicyOrComponentChangedEvent() {
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.event.IEvent#getEventType()
	 */
	public EventType getEventType() {
		return EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT;
	}

	/**
	 * Retrieve the newCurrentObject.
	 * 
	 * @return the newCurrentObject.
	 */
	public IPolicyOrComponentData getNewCurrentObject() {
		return this.newCurrentObject;
	}

	/**
	 * Determine if Policy Author is in a state in which there is a current
	 * object (i.e. there is at least one policy or component editor open)
	 * 
	 * @return true if there is a current object; false otherwise
	 */
	public boolean currentObjectExists() {
		return this.newCurrentObject != null;
	}
}
