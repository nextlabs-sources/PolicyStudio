/*
 * Created on Jan 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event;

/**
 * Implentations of IContextualEventListener register to recieve events of a
 * contextual event type ({@see
 * com.bluejungle.destiny.policymanager.event.ContextualEventType})
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/event/IContextualEventListener.
 *          java#1 $
 */

public interface IContextualEventListener {

	/**
	 * Response to the provided event
	 * 
	 * @param event
	 *            an event which was fired
	 */
	public void onEvent(IContextualEvent event);
}
