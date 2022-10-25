/*
 * Created on Mar 2, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.swt.widgets.Control;

import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/ui/CompositionUndoElement.java#1 $:
 */

public class PropertyUndoElement extends BaseUndoElement {

	private static final IEventManager EVENT_MANAGER;
	static {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		EVENT_MANAGER = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
	}

	private ObjectProperty prop = null;
	private Object oldValue = null;
	private Object newValue = null;

	/**
	 * Constructor
	 * 
	 */
	public PropertyUndoElement() {
		super();
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.IUndoElement#undo()
	 */
	public boolean undo(Object spec, Control control) {
		if (this.prop == ObjectProperty.DESCRIPTION) {
			setDescription(spec, (String) this.oldValue);
		}

		PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent(
				(IHasId) spec);
		EVENT_MANAGER.fireEvent(objectModifiedEvent);

		return true;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.IUndoElement#redo()
	 */
	public boolean redo(Object spec, Control control) {
		if (this.prop == ObjectProperty.DESCRIPTION) {
			setDescription(spec, (String) this.newValue);
		}

		PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent(
				(IHasId) spec);
		EVENT_MANAGER.fireEvent(objectModifiedEvent);

		return true;
	}

	private void setDescription(Object spec, String description) {
		if (spec instanceof IDSpec) {
			((IDSpec) spec).setDescription(description);
		} else if (spec instanceof IDPolicy) {
			((IDPolicy) spec).setDescription(description);
		}
	}

	public Object getNewValue() {
		return this.newValue;
	}

	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}

	public Object getOldValue() {
		return this.oldValue;
	}

	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

	public ObjectProperty getProp() {
		return this.prop;
	}

	public void setProp(ObjectProperty prop) {
		this.prop = prop;
	}
}