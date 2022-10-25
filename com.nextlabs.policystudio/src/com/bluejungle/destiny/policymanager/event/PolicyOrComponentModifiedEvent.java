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
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * PolicyOrComponentModifiedEvent is an {@see
 * com.bluejungle.destiny.policymanager.event.IContextualEvent} implementation
 * associated with the event type, {@see
 * com.bluejungle.destiny.policymanager.event.ContextualEventType#
 * POLICY_OR_COMPONENT_MODIFIED_EVENT}
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/event/
 *          PolicyOrComponentModifiedEvent.java#1 $
 */

public class PolicyOrComponentModifiedEvent implements IContextualEvent {

	private final Object eventContext;
	private final IPolicyOrComponentData eventContextAsPolicyOrComponentData;

	/**
	 * Create an instance of PolicyOrComponentModifiedEvent
	 * 
	 * @param eventContext
	 */
	public PolicyOrComponentModifiedEvent(IHasId eventContext) {
		if (eventContext == null) {
			throw new NullPointerException("eventContext cannot be null.");
		}

		this.eventContext = eventContext;
		this.eventContextAsPolicyOrComponentData = new PolicyOrComponentData(
				eventContext);
	}

	/**
	 * Create an instance of PolicyOrComponentModifiedEvent
	 * 
	 * @param eventContext
	 */
	public PolicyOrComponentModifiedEvent(DomainObjectDescriptor eventContext) {
		if (eventContext == null) {
			throw new NullPointerException("eventContext cannot be null.");
		}

		this.eventContextAsPolicyOrComponentData = new PolicyOrComponentData(
				eventContext);
		this.eventContext = this.eventContextAsPolicyOrComponentData
				.getEntity();
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.event.IContextualEvent#getContextualEventType()
	 */
	public ContextualEventType getContextualEventType() {
		return ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.event.IEvent#getEventContext()
	 */
	public Object getEventContext() {
		return this.eventContext;
	}

	/**
	 * Retrieve the event context of this event as a PolicyOrComponentData
	 * instance
	 * 
	 * @return the event context of this event as a PolicyOrComponentData
	 *         instance
	 */
	public IPolicyOrComponentData getEventContextAsPolicyOrComponentData() {
		return this.eventContextAsPolicyOrComponentData;
	}

}
