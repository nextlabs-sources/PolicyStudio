/*
 * Created on Mar 2, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.util.List;

import org.eclipse.swt.widgets.Control;

import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.PredicateModifiedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/ui/CompositionUndoElement.java#1 $:
 */

public class CompositionUndoElement extends BaseUndoElement {

	private static final IEventManager EVENT_MANAGER;
	static {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		EVENT_MANAGER = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
	}

	private int index;
	private CompositionUndoElementOp op = null;
	private Object oldValue = null;
	private Object newValue = null;
	private List<Integer> indexArray = null;
	private List<IPredicate> refArray = null;
	private int controlId = 0;

	/**
	 * Constructor
	 * 
	 */
	public CompositionUndoElement() {
		super();
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.IUndoElement#undo()
	 */
	public boolean undo(Object spec, Control control) {
		// The first spec is the one corresponding to the composition control.
		CompositePredicate compositionSpec = GlobalState.getInstance()
				.getEditorPanel().getControlDomainObject(controlId,
						(IHasId) spec);
		if (op == CompositionUndoElementOp.REMOVE_REF) {
			CompositePredicate compSpec = (CompositePredicate) PredicateHelpers
					.getPredicateAt(compositionSpec, index);
			for (int i = indexArray.size() - 1; i >= 0; i--) {
				int insertPos = indexArray.get(i).intValue();
				IPredicate insertSpec = (IPredicate) refArray.get(i);
				PredicateHelpers.insertPredicateAt(compSpec, insertSpec,
						insertPos);
			}

			PredicateModifiedEvent predicateModifiedEvent = new PredicateModifiedEvent(
					compSpec);
			EVENT_MANAGER.fireEvent(predicateModifiedEvent);

		} else if (op == CompositionUndoElementOp.ADD_REF) {
			CompositePredicate compSpec = (CompositePredicate) PredicateHelpers
					.getPredicateAt(compositionSpec, index);
			for (int i = indexArray.size() - 1; i >= 0; i--) {
				int insertPos = ((Integer) indexArray.get(i)).intValue();
				PredicateHelpers.removePredicateAt(compSpec, insertPos);
			}

			PredicateModifiedEvent predicateModifiedEvent = new PredicateModifiedEvent(
					compSpec);
			EVENT_MANAGER.fireEvent(predicateModifiedEvent);
		} else if (op == CompositionUndoElementOp.ADD) {
			PredicateHelpers.removePredicateAt(compositionSpec, index);

			PredicateModifiedEvent predicateModifiedEvent = new PredicateModifiedEvent(
					compositionSpec);
			EVENT_MANAGER.fireEvent(predicateModifiedEvent);
		} else if (op == CompositionUndoElementOp.REMOVE) {
			PredicateHelpers.insertPredicateAt(compositionSpec,
					(IPredicate) oldValue, index);

			PredicateModifiedEvent predicateModifiedEvent = new PredicateModifiedEvent(
					compositionSpec);
			EVENT_MANAGER.fireEvent(predicateModifiedEvent);
		} else if (op == CompositionUndoElementOp.REPLACE) {
			PredicateHelpers.removePredicateAt(compositionSpec, index);
			PredicateHelpers.insertPredicateAt(compositionSpec,
					(IPredicate) oldValue, index);

			PredicateModifiedEvent predicateModifiedEvent = new PredicateModifiedEvent(
					compositionSpec);
			EVENT_MANAGER.fireEvent(predicateModifiedEvent);
		}
		return true;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.IUndoElement#redo()
	 */
	public boolean redo(Object spec, Control control) {
		// The first spec is the one corresponding to the composition control.
		CompositePredicate compositionSpec = GlobalState.getInstance()
				.getEditorPanel().getControlDomainObject(controlId,
						(IHasId) spec);
		if (op == CompositionUndoElementOp.REMOVE_REF) {

			CompositePredicate compSpec = (CompositePredicate) PredicateHelpers
					.getPredicateAt(compositionSpec, index);
			for (int i = 0; i < indexArray.size(); i++) {
				int insertPos = indexArray.get(i).intValue();
				PredicateHelpers.removePredicateAt(compSpec, insertPos);
			}

			PredicateModifiedEvent predicateModifiedEvent = new PredicateModifiedEvent(
					compSpec);
			EVENT_MANAGER.fireEvent(predicateModifiedEvent);
		} else if (op == CompositionUndoElementOp.ADD_REF) {
			CompositePredicate compSpec = (CompositePredicate) PredicateHelpers
					.getPredicateAt(compositionSpec, index);
			for (int i = 0; i < indexArray.size(); i++) {
				int insertPos = indexArray.get(i).intValue();
				IPredicate insertSpec = refArray.get(i);
				PredicateHelpers.insertPredicateAt(compSpec, insertSpec,
						insertPos);
			}

			PredicateModifiedEvent predicateModifiedEvent = new PredicateModifiedEvent(
					compSpec);
			EVENT_MANAGER.fireEvent(predicateModifiedEvent);
		} else if (op == CompositionUndoElementOp.ADD) {
			PredicateHelpers.insertPredicateAt(compositionSpec,
					(IPredicate) newValue, index);

			PredicateModifiedEvent predicateModifiedEvent = new PredicateModifiedEvent(
					compositionSpec);
			EVENT_MANAGER.fireEvent(predicateModifiedEvent);
		} else if (op == CompositionUndoElementOp.REMOVE) {
			PredicateHelpers.removePredicateAt(compositionSpec, index);

			PredicateModifiedEvent predicateModifiedEvent = new PredicateModifiedEvent(
					compositionSpec);
			EVENT_MANAGER.fireEvent(predicateModifiedEvent);
		} else if (op == CompositionUndoElementOp.REPLACE) {
			PredicateHelpers.removePredicateAt(compositionSpec, index);
			PredicateHelpers.insertPredicateAt(compositionSpec,
					(IPredicate) newValue, index);

			PredicateModifiedEvent predicateModifiedEvent = new PredicateModifiedEvent(
					compositionSpec);
			EVENT_MANAGER.fireEvent(predicateModifiedEvent);
		}
		return true;
	}

	public CompositionUndoElementOp getOp() {
		return op;
	}

	public void setOp(CompositionUndoElementOp op) {
		this.op = op;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<Integer> getIndexArray() {
		return indexArray;
	}

	public void setIndexArray(List<Integer> indexArray) {
		this.indexArray = indexArray;
	}

	public Object getNewValue() {
		return newValue;
	}

	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

	public List<IPredicate> getRefArray() {
		return refArray;
	}

	public void setRefArray(List<IPredicate> refArray) {
		this.refArray = refArray;
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