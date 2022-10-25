/*
 * Created on May 31, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.swt.widgets.Control;

import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.PredicateModifiedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.pf.domain.destiny.common.IDSpec;

/**
 * @author dstarke
 * 
 */
public class ActionComponentUndoElement extends BaseUndoElement {

	private static final IEventManager EVENT_MANAGER;
	static {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		EVENT_MANAGER = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
	}

	private ActionComponentUndoElementOp op = null;
	private Object oldValue = null;
	private Object newValue = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bluejungle.destiny.policymanager.ui.IUndoElement#undo(java.lang.Object
	 * , org.eclipse.swt.widgets.Control)
	 */
	public boolean undo(Object spec, Control control) {
		CompositePredicate actions = (CompositePredicate) ((IDSpec) spec)
				.getPredicate();
		if (op == ActionComponentUndoElementOp.ADD_ACTION) {
			IPredicate[] actionList = (IPredicate[]) newValue;
			for (int i = 0; i < actionList.length; i++) {
				actions.removePredicate(actionList[i]);
			}
			PredicateHelpers.rebalanceDomainObject(actions, actions.getOp());
		} else if (op == ActionComponentUndoElementOp.REMOVE_ACTION) {
			IPredicate[] actionList = (IPredicate[]) oldValue;
			for (int i = 0; i < actionList.length; i++) {
				actions.addPredicate(actionList[i]);
			}
			PredicateHelpers.rebalanceDomainObject(actions, actions.getOp());
		}

		PredicateModifiedEvent predicateModifiedEvent = new PredicateModifiedEvent(
				(IPredicate) spec);
		EVENT_MANAGER.fireEvent(predicateModifiedEvent);

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bluejungle.destiny.policymanager.ui.IUndoElement#redo(java.lang.Object
	 * , org.eclipse.swt.widgets.Control)
	 */
	public boolean redo(Object spec, Control control) {
		CompositePredicate actions = (CompositePredicate) ((IDSpec) spec)
				.getPredicate();
		if (op == ActionComponentUndoElementOp.ADD_ACTION) {
			IPredicate[] actionList = (IPredicate[]) newValue;
			for (int i = 0; i < actionList.length; i++) {
				actions.addPredicate(actionList[i]);
			}
			PredicateHelpers.rebalanceDomainObject(actions, actions.getOp());
		} else if (op == ActionComponentUndoElementOp.REMOVE_ACTION) {
			IPredicate[] actionList = (IPredicate[]) oldValue;
			for (int i = 0; i < actionList.length; i++) {
				actions.removePredicate(actionList[i]);
			}
			PredicateHelpers.rebalanceDomainObject(actions, actions.getOp());
		}

		PredicateModifiedEvent predicateModifiedEvent = new PredicateModifiedEvent(
				(IPredicate) spec);
		EVENT_MANAGER.fireEvent(predicateModifiedEvent);

		return false;
	}

	/**
	 * @return Returns the newValue.
	 */
	public Object getNewValue() {
		return newValue;
	}

	/**
	 * @param newValue
	 *            The newValue to set.
	 */
	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}

	/**
	 * @return Returns the oldValue.
	 */
	public Object getOldValue() {
		return oldValue;
	}

	/**
	 * @param oldValue
	 *            The oldValue to set.
	 */
	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

	/**
	 * @return Returns the op.
	 */
	public ActionComponentUndoElementOp getOp() {
		return op;
	}

	/**
	 * @param op
	 *            The op to set.
	 */
	public void setOp(ActionComponentUndoElementOp op) {
		this.op = op;
	}
}
