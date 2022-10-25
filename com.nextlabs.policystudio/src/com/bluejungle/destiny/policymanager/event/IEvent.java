/*
 * Created on Jan 20, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event;

/**
 * An instance of an event
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/event/IEvent.java#1 $
 */
public interface IEvent {

	/**
	 * Retrieve the type of this event
	 * 
	 * @return the type of this event
	 */
	public EventType getEventType();
}
