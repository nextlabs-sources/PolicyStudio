/*
 * Created on Mar 2, 2005 All sources, binaries and HTML pages (C) copyright
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
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/ui/CompositionUndoElement.java#1 $:
 */

public class PropertyExpressionUndoElement extends BaseUndoElement {

	private static final IEventManager EVENT_MANAGER;
	static {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		EVENT_MANAGER = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
	}

	private int index;
	private PropertyExpressionUndoElementOp op = null;
	private Object oldValue = null;
	private Object newValue = null;
	private int controlId = 0;

	/**
	 * Constructor
	 * 
	 */
	public PropertyExpressionUndoElement() {
		super();
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.IUndoElement#undo()
	 */
	public boolean undo(Object spec, Control control) {
		// The first spec is the one corresponding to the composition control.
		CompositePredicate compositionSpec = GlobalState.getInstance()
				.getEditorPanel().getControlDomainObject(controlId,
						(IDSpec) spec);
		// FIXME change to correct type
		if (op == PropertyExpressionUndoElementOp.CHANGE_ATTRIBUTE) {
			Relation prop = (Relation) PredicateHelpers.getPredicateAt(
					compositionSpec, this.index);
			prop.setLHS((SpecAttribute) this.oldValue);
		} else if (op == PropertyExpressionUndoElementOp.CHANGE_OP) {
			Relation prop = (Relation) PredicateHelpers.getPredicateAt(
					compositionSpec, this.index);
			prop.setOp((RelationOp) this.oldValue);
		} else if (op == PropertyExpressionUndoElementOp.CHANGE_VALUE) {
			Relation prop = (Relation) PredicateHelpers.getPredicateAt(
					compositionSpec, this.index);
			prop.setRHS((IExpression) this.oldValue);
		} else if (op == PropertyExpressionUndoElementOp.ADD) {
			PredicateHelpers.removePredicateAt(compositionSpec, this.index);
		} else if (op == PropertyExpressionUndoElementOp.REMOVE) {
			PredicateHelpers.insertPredicateAt(compositionSpec,
					(IPredicate) this.oldValue, index);
		}

		PredicateModifiedEvent predicateModifiedEvent = new PredicateModifiedEvent(
				compositionSpec);
		EVENT_MANAGER.fireEvent(predicateModifiedEvent);

		return true;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.IUndoElement#redo()
	 */
	public boolean redo(Object spec, Control control) {
		// The first spec is the one corresponding to the composition control.
		CompositePredicate compositionSpec = GlobalState.getInstance()
				.getEditorPanel().getControlDomainObject(controlId,
						(IDSpec) spec);
		// FIXME change to correct type
		if (op == PropertyExpressionUndoElementOp.CHANGE_ATTRIBUTE) {
			Relation prop = (Relation) PredicateHelpers.getPredicateAt(
					compositionSpec, this.index);
			prop.setLHS((SpecAttribute) this.newValue);
		} else if (op == PropertyExpressionUndoElementOp.CHANGE_OP) {
			Relation prop = (Relation) PredicateHelpers.getPredicateAt(
					compositionSpec, this.index);
			prop.setOp((RelationOp) this.newValue);
		} else if (op == PropertyExpressionUndoElementOp.CHANGE_VALUE) {
			Relation prop = (Relation) PredicateHelpers.getPredicateAt(
					compositionSpec, this.index);
			prop.setRHS((IExpression) this.newValue);
		} else if (op == PropertyExpressionUndoElementOp.ADD) {
			PredicateHelpers.insertPredicateAt(compositionSpec,
					(IPredicate) this.newValue, index);
		} else if (op == PropertyExpressionUndoElementOp.REMOVE) {
			PredicateHelpers.removePredicateAt(compositionSpec, this.index);
		}

		PredicateModifiedEvent predicateModifiedEvent = new PredicateModifiedEvent(
				compositionSpec);
		EVENT_MANAGER.fireEvent(predicateModifiedEvent);

		return true;
	}

	public PropertyExpressionUndoElementOp getOp() {
		return this.op;
	}

	public void setOp(PropertyExpressionUndoElementOp op) {
		this.op = op;
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
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

	/**
	 * @return Returns the controlId.
	 */
	public int getControlId() {
		return controlId;
	}

	/**
	 * @param controlId
	 *            The controlId to set.
	 */
	public void setControlId(int controlId) {
		this.controlId = controlId;
	}
}