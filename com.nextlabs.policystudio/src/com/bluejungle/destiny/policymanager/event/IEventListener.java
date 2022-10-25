/*
 * Created on Jan 20, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event;

/**
 * Implentations of IEventListener register to recieve events of a type declared
 * in the enumeration, {@see
 * com.bluejungle.destiny.policymanager.event.EventType}.
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/event/IEventListener.java#1 $
 */

public interface IEventListener {

	/**
	 * Response to the provided event
	 * 
	 * @param event
	 *            an event which was fired
	 */
	public void onEvent(IEvent event);
}
