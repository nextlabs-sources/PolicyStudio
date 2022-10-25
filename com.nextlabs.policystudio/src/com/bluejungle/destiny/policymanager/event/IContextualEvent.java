/*
 * Created on Jan 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event;

/**
 * An instance of IContextualEvent is an event instance which is fired within a
 * particular context.
 * 
 * @see com.bluejungle.destiny.policymanager.event.ContextualEventType
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/event/IContextualEvent.java#1
 *          $
 */

public interface IContextualEvent {

	/**
	 * Retrieve the type of this event
	 * 
	 * @return the type of this event
	 */
	public ContextualEventType getContextualEventType();

	/**
	 * Retrieve the context in which this event was fired
	 * 
	 * @return the context in which this event was fired
	 */
	public Object getEventContext();
}
