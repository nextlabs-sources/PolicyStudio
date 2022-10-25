/*
 * Created on Jan 30, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event;

import com.bluejungle.framework.expressions.IPredicate;

/**
 * PredicateModifiedEvent is an {@see
 * com.bluejungle.destiny.policymanager.event.IContextualEvent} implementation
 * associated with the event type, {@see
 * com.bluejungle.destiny.policymanager.event.ContextualEventType#
 * PREDICATE_MODIFIED_EVENT}
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/event/PredicateModifiedEvent.java#1
 *          $
 */
public class PredicateModifiedEvent implements IContextualEvent {

	private IPredicate predicateModified;

	/**
	 * Create an instance of PredicateModifiedEvent
	 * 
	 * @param predicateModified
	 */
	public PredicateModifiedEvent(IPredicate predicateModified) {
		if (predicateModified == null) {
			throw new NullPointerException("predicateModified cannot be null.");
		}

		this.predicateModified = predicateModified;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.event.IContextualEvent#getContextualEventType()
	 */
	public ContextualEventType getContextualEventType() {
		return ContextualEventType.PREDICATE_MODIFIED_EVENT;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.event.IContextualEvent#getEventContext()
	 */
	public Object getEventContext() {
		return this.predicateModified;
	}

	/**
	 * Retrieve the event context of this event as a IPredicate instance
	 * 
	 * @return the event context of this event as a IPredicate instance
	 */
	public IPredicate getEventContextAsPredicate() {
		return this.predicateModified;
	}
}
