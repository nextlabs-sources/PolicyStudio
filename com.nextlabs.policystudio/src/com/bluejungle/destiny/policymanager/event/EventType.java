/*
 * Created on Jan 20, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * Enumeration of event types
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/event/EventType.java#1 $
 */

public class EventType extends EnumBase {

	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Events of the type {@see #CURRENT_POLICY_OR_COMPONENT_MODIFIED_EVENT} are
	 * fired when the content of the current Policy or Component is modified
	 */
	public static final EventType CURRENT_POLICY_OR_COMPONENT_MODIFIED_EVENT = new EventType(
			"CURRENT_POLICY_OR_COMPONENT_MODIFIED_EVENT_TYPE");

	/**
	 * Events of the type {@see #CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT} are
	 * fired when the current Policy or Component is replaced by another Policy
	 * or Component
	 */
	public static final EventType CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT = new EventType(
			"CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT_TYPE");

	/**
	 * Events of the type {@see #SELECTION_CHANGED_EVENT} are fired when the
	 * item selection in the Policy or Component navigation panes is replaced by
	 * another item selection
	 */
	public static final EventType SELECTION_CHANGED_EVENT = new EventType(
			"SELECTION_CHANGED_EVENT_TYPE");

	/**
	 * Events of the type {@see #SELECTED_ITEMS_MODIFIED_EVENT} are fired when
	 * one or more of the items selected in the Policy or Component navigation
	 * panes are modified
	 */
	public static final EventType SELECTED_ITEMS_MODIFIED_EVENT = new EventType(
			"SELECTED_ITEMS_MODIFIED_EVENT_TYPE");

	/**
	 * Create an instance of EventTypeEnum. Private to support fixed size Enum
	 * 
	 * @param name
	 *            the name of the Enum item
	 */
	private EventType(String name) {
		super(name);
	}
}
