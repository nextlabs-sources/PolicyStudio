/*
 * Created on Jan 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event;

import java.util.Set;

/**
 * Implementations of IMutliContextualEventListener listen for contextual events
 * fired in any context. For more information, see {@see
 * com.bluejungle.destiny.policymanager.event.IEventManager#fireEvent(Set)}
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/event/
 *          IMultiContextualEventListener.java#1 $
 */

public interface IMultiContextualEventListener {

	/**
	 * Respond to the provided events
	 * 
	 * @param events
	 */
	public void onEvents(Set<IContextualEvent> events);
}
