/*
 * Created on Jan 20, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event;

import java.util.Set;

/**
 * Policy Author Event Manager
 * 
 * @author sgoldstein
 * @version $Id$
 */

public interface IEventManager {

    String COMPONENT_NAME = IEventManager.class.getName();

    /**
     * Register to listen for the specified event type
     * 
     * @param eventListener
     * @param eventType
     */
    void registerListener(IEventListener eventListener,
            EventType eventType);

    /**
     * Register to listen to a contextual event in the specified context.
     * 
     * @param eventListener
     * @param eventType
     * @param eventContext
     */
    void registerListener(IContextualEventListener eventListener,
            ContextualEventType eventType, Object eventContext);

    /**
     * Register to listen to a contextual event in any context. For example,
     * register to receive any event of type {@see
     * ContextualEventType#OBJECT_MODIFIED_EVENT}.
     * 
     * @param eventListener
     * @param eventType
     * @see IEventManager#fireEvent(Set)
     */
    void registerListener(IMultiContextualEventListener eventListener,
            ContextualEventType eventType);

    /**
     * Unregister an event listener for the specified event type
     * 
     * @param eventListener
     * @param eventType
     */
    void unregisterListener(IEventListener eventListener,
            EventType eventType);

    /**
     * Unregister a contextual event listener for the specified event type and
     * context
     * 
     * @param eventListener
     * @param eventType
     * @param eventContext
     */
    void unregisterListener(IContextualEventListener eventListener,
            ContextualEventType eventType, Object eventContext);

    /**
     * Unregister an event listener for the specified event type listening in
     * any context
     * 
     * @param eventListener
     * @param eventType
     */
    void unregisterListener(IMultiContextualEventListener eventListener,
            ContextualEventType eventType);

    /**
     * Fire the specified event
     * 
     * @param eventToFire
     */
    void fireEvent(IEvent eventToFire);

    /**
     * Fire the specified contextual event
     * 
     * @param contextualEventToFire
     */
    void fireEvent(IContextualEvent contextualEventToFire);

    /**
     * Fire a collection of contextual events which were the result of a single
     * user action. This is useful when contextual events have registered
     * listeners listening in the "any context". Events will be propogated as
     * follows:<br />
     * <br />
     * 1. Iterate through each event and fire it individually.<br />
     * 2. For each event type represented in the collection of events, fire a
     * single event for those listeners registered in the "any context" for the
     * associated event type. The event context for the events fired will be a
     * Set instance containing all context objects in the provided event
     * Collection of the associated type.<br />
     * <br />
     * To determine when you might use this mechanism, the following scenario
     * can be considered. Suppose a Policy referring to 4 other components is
     * approved (i.e. the user Submits it). This will result in 5 Object
     * Modified events, one for the policy and 4 others for the contained
     * components). If this mechanism is used, the DeployAction, which is
     * listening in the "any context" to refresh when any object is changed,
     * will only recieve 1 event rather than 5. Therefore, rather than
     * refreshing 5 times during a user request, it will only refresh once.
     * 
     * @param eventsToFire
     */
    void fireEvent(Set<? extends IContextualEvent> contextualEventsToFire);
}
