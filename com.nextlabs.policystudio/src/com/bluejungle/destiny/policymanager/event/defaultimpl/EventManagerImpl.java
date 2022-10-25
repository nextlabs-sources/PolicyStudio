/*
 * Created on Jan 20, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event.defaultimpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.EventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEvent;
import com.bluejungle.destiny.policymanager.event.IEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.IMultiContextualEventListener;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * Default implementation of an event manager
 * Note that this class IS thread safe. Listeners may be added/remove and events
 * may be fired concurrently
 * 
 * @author sgoldstein
 * @version $Id$
 */

public class EventManagerImpl implements IEventManager, IHasComponentInfo<IEventManager> {
    
    public static final ComponentInfo<IEventManager> COMPONENT_INFO = 
        new ComponentInfo<IEventManager>(
            IEventManager.COMPONENT_NAME
          , EventManagerImpl.class
          , IEventManager.class
          , LifestyleType.SINGLETON_TYPE);
    
    private static abstract class EventManagerInternal<_EVENT, _EVENT_TYPE, _LISTENER_TYPE> {
        
        @SuppressWarnings("unchecked")
        private final Map<_EVENT_TYPE, Set<_LISTENER_TYPE>> EVENT_LISTENER_REGISTRY = 
            Collections.synchronizedMap(new HashMap());
        
        
       
        @SuppressWarnings("unchecked")
        private void registerListenerImpl(
                _EVENT_TYPE registrationKey
              , _LISTENER_TYPE eventListener
        ) {
            synchronized (EVENT_LISTENER_REGISTRY) {
                Set<_LISTENER_TYPE> registeredListeners = EVENT_LISTENER_REGISTRY.get(registrationKey);
                if (registeredListeners == null) {
                    registeredListeners = new HashSet();
                    EVENT_LISTENER_REGISTRY.put(registrationKey, registeredListeners);
                }
                registeredListeners.add(eventListener);
            }
        }

        private void unregisterListenerImpl(
                _EVENT_TYPE eventType
              , _LISTENER_TYPE eventListener
        ) {
            synchronized (EVENT_LISTENER_REGISTRY) {
                Set<_LISTENER_TYPE> registeredListeners = EVENT_LISTENER_REGISTRY.get(eventType);
                if (registeredListeners != null) {
                    registeredListeners.remove(eventListener);
                }
            }
        }
        
        void unregisterListener(_EVENT_TYPE eventType, _LISTENER_TYPE eventListener) {
            checkNull(eventListener, "eventListener");
            checkNull(eventType, "eventType");

            unregisterListenerImpl(eventType, eventListener);
        }
        
        private void fireEvent(_EVENT eventToFire) {
            checkNull(eventToFire, "eventsToFire");
            fireEvents(Collections.singleton(eventToFire));
        }
        
        
        void fireEvents(final Set<_EVENT> eventsToFire) {
            checkNull(eventsToFire, "eventsToFire");
            
            synchronized (EVENT_LISTENER_REGISTRY) {
                /**
                 * We clone the listener Set because listeners may unregsiter during
                 * event handling and Iteratos are fail fast. HashSet is not thread
                 * safe, so we synchronize here during the clone. We could put a
                 * wrapper around it instead, but since we require more coarse
                 * synchronization in the register and unregsiter methods, we must
                 * follow the same approach here
                 */
                Set<_LISTENER_TYPE> registeredListeners = EVENT_LISTENER_REGISTRY.get(getEventType(eventsToFire));
                if (registeredListeners != null) {
                    
                    @SuppressWarnings("unchecked")
                    final Set<_LISTENER_TYPE> listenersToNotify = new HashSet(registeredListeners); 

                    /*
                     * Ideally, this would be outside the synchronized, but it's not
                     * a huge deal because it's async(). Can't be outside due to the
                     * anonymous class
                     */
                    Display currentDisplay = Display.getCurrent();
                    currentDisplay.asyncExec(new Runnable() {

                        public void run() {
                            for (_LISTENER_TYPE nextListener : listenersToNotify) {
                                try {
                                    runEvent(nextListener, eventsToFire);
                                } catch (RuntimeException e) {
                                    LoggingUtil.logError(Activator.ID, "", e);
                                }
                            }
                        }
                    });
                }
            }
        }
        
        _EVENT_TYPE getEventType(Set<_EVENT> events) {
            return getEventType(events.iterator().next());
        }
        
        abstract _EVENT_TYPE getEventType(_EVENT event);
        
        abstract void runEvent(_LISTENER_TYPE listener, Set<_EVENT> event);
            
    }
    
    private static final EventManagerInternal<
            IEvent
          , EventType
          , IEventListener
    > EVENT_MGR 
        = new EventManagerInternal<IEvent, EventType, IEventListener>() {

            @Override
            EventType getEventType(IEvent event) {
                return event.getEventType();
            }

            @Override
            void runEvent(IEventListener listener, Set<IEvent> events) {
                if(events != null) {
                    for(IEvent event : events) {
                        listener.onEvent(event);
                    }
                }
            }
        };
    
    private static final EventManagerInternal<
            IContextualEvent
          , ContextualEventListenerRegistryMapKey
          , IContextualEventListener
    > CONTEXTUAL_EVENT_MGR 
        = new EventManagerInternal<IContextualEvent, ContextualEventListenerRegistryMapKey, IContextualEventListener>() {

            @Override
            ContextualEventListenerRegistryMapKey getEventType(IContextualEvent contextualEventToFire) {
                Object eventContext = contextualEventToFire.getEventContext();
                ContextualEventType contextualEventType = contextualEventToFire.getContextualEventType();
                ContextualEventListenerRegistryMapKey registryKey = new ContextualEventListenerRegistryMapKey(
                        contextualEventType, eventContext);
                return registryKey;
            }

            @Override
            void runEvent(IContextualEventListener listener, Set<IContextualEvent> events) {
                if(events != null) {
                    for(IContextualEvent event : events) {
                        listener.onEvent(event);
                    }
                }
            }
        };
        
    private static final EventManagerInternal<
            IContextualEvent
          , ContextualEventType
          , IMultiContextualEventListener
    > ANY_CONTEXT_CONTEXTUAL_MGR 
        = new EventManagerInternal<IContextualEvent, ContextualEventType, IMultiContextualEventListener>() {

            @Override
            ContextualEventType getEventType(IContextualEvent event) {
                return event.getContextualEventType();
            }
            
            @Override
            void runEvent(IMultiContextualEventListener listener, Set<IContextualEvent> events) {
                listener.onEvents(events);
            }
        };

    public ComponentInfo<IEventManager> getComponentInfo() {
        return COMPONENT_INFO;
    }
    
    private static void checkNull(Object object, String name) {
        if (object == null) {
            throw new NullPointerException(name + " cannot be null.");
        }
    }

    public void registerListener(IEventListener eventListener, EventType eventType) {
        checkNull(eventListener, "eventListener");
        checkNull(eventType, "eventType");
        EVENT_MGR.registerListenerImpl(eventType, eventListener);
    }
    

    public void unregisterListener(IEventListener eventListener, EventType eventType) {
        EVENT_MGR.unregisterListener(eventType, eventListener);
    }

    public void registerListener(IContextualEventListener eventListener,
            ContextualEventType eventType, Object eventContext) {
        checkNull(eventListener, "eventListener");
        checkNull(eventType, "eventType");
        checkNull(eventContext, "eventContext");

        ContextualEventListenerRegistryMapKey registryKey = new ContextualEventListenerRegistryMapKey(
                eventType, eventContext);
        CONTEXTUAL_EVENT_MGR.registerListenerImpl(registryKey, eventListener);
    }
    
    public void unregisterListener(IContextualEventListener eventListener,
            ContextualEventType eventType, Object eventContext) {
        checkNull(eventListener, "eventListener");
        checkNull(eventType, "eventType");
        checkNull(eventContext, "eventContext");


        ContextualEventListenerRegistryMapKey registryKey = new ContextualEventListenerRegistryMapKey(
                eventType, eventContext);
        CONTEXTUAL_EVENT_MGR.unregisterListenerImpl(registryKey, eventListener);
    }

    public void registerListener(IMultiContextualEventListener eventListener,
            ContextualEventType eventType) {
        checkNull(eventListener, "eventListener");
        checkNull(eventType, "eventType");
        
        ANY_CONTEXT_CONTEXTUAL_MGR.registerListenerImpl(eventType, eventListener);
    }

    public void unregisterListener(IMultiContextualEventListener eventListener,
            ContextualEventType eventType) {
        ANY_CONTEXT_CONTEXTUAL_MGR.unregisterListener(eventType, eventListener);
    }


    public void fireEvent(final IEvent eventToFire) {
        EVENT_MGR.fireEvent(eventToFire);
    }

    public void fireEvent(IContextualEvent contextualEventToFire) {
        fireEvent(Collections.singleton(contextualEventToFire));
    }

    @SuppressWarnings("unchecked")
    public void fireEvent(Set<? extends IContextualEvent> contextualEventsToFire) {
        if (contextualEventsToFire == null) {
            throw new NullPointerException("contextualEventsToFire cannot be null.");
        }

        for (IContextualEvent nextEvent : contextualEventsToFire) {
            CONTEXTUAL_EVENT_MGR.fireEvent(nextEvent);
        }

        fireAnyContextContextualEvents((Set<IContextualEvent>)contextualEventsToFire);
    }

    /**
     * Fire a Set of contextual events to the "any context" listeners
     * 
     * @param eventsToFire
     */
    private void fireAnyContextContextualEvents(Set<IContextualEvent> eventsToFire) {
        if (!eventsToFire.isEmpty()) {
            ANY_CONTEXT_CONTEXTUAL_MGR.fireEvents(eventsToFire);
        }
    }


    private static class ContextualEventListenerRegistryMapKey {

        private ContextualEventType eventType;
        private Object eventContext;

        /**
         * Create an instance of EventListenerRegistryMapKey
         * 
         * @param eventType
         * @param eventContext
         */
        public ContextualEventListenerRegistryMapKey(
                ContextualEventType eventType, Object eventContext) {
            if (eventType == null) {
                throw new NullPointerException("eventType cannot be null.");
            }

            if (eventContext == null) {
                throw new NullPointerException("eventContext cannot be null.");
            }

            this.eventType = eventType;
            this.eventContext = eventContext;
        }

        @Override
        public boolean equals(Object obj) {
            boolean valueToReturn = false;

            if (obj == this) {
                valueToReturn = true;
            } else if ((obj != null) && (obj instanceof ContextualEventListenerRegistryMapKey)) {
                ContextualEventListenerRegistryMapKey objectToTest = (ContextualEventListenerRegistryMapKey) obj;
                if ((this.getContextualEventType().equals(objectToTest.getContextualEventType()))
                        && (this.getEventContext().equals(objectToTest.getEventContext()))) {
                    valueToReturn = true;
                }
            }

            return valueToReturn;
        }

        @Override
        public int hashCode() {
            return this.getContextualEventType().hashCode()
                    + this.getEventContext().hashCode();
        }

        /**
         * Retrieve the eventContext.
         * 
         * @return the eventContext.
         */
        private Object getEventContext() {
            return this.eventContext;
        }

        /**
         * Retrieve the eventType.
         * 
         * @return the eventType.
         */
        private ContextualEventType getContextualEventType() {
            return this.eventType;
        }

    }
}
