/*
 * Created on Jan 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * ContextualEventType is an enumeration of event types which have a context in
 * which they're generate. For instance, an event of type, {@see
 * #POLICY_OR_COMPONENT_MODIFIED_EVENT}, is fired in the context of a particular
 * Policy or Component being modified
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/event/ContextualEventType.java#1 $
 */

public class ContextualEventType extends EnumBase {

	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Events of the type {@see #POLICY_OR_COMPONENT_MODIFIED_EVENT} are fired
	 * when a particular Policy or Component is modified
	 */
	public static final ContextualEventType POLICY_OR_COMPONENT_MODIFIED_EVENT = new ContextualEventType(
			"POLICY_OR_COMPONENT_MODIFIED_EVENT_TYPE");

	/**
	 * Events of the type {@see #PREDICATE_MODIFIED_EVENT} are fired when a
	 * particular predicate within a Policy or Component is modified.
	 */
	public static final ContextualEventType PREDICATE_MODIFIED_EVENT = new ContextualEventType(
			"PREDICATE_MODIFIED_EVENT_TYPE");

	/**
	 * Create an instance of ContextualEventType. Private to support fixed size
	 * Enum
	 * 
	 * @param name
	 */
	private ContextualEventType(String name) {
		super(name);
	}
}
