/*
 * Created on May 11, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.swt.widgets.Control;

import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers.EffectTypeEnum;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;

/**
 * @author dstarke
 * 
 *         This class represents policy UNDO/REDO actions.
 */
public class PolicyUndoElement<T> extends BaseUndoElement {

    private static final IEventManager EVENT_MANAGER;
    static {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        EVENT_MANAGER = componentManager.getComponent(EventManagerImpl.COMPONENT_INFO);
    }

    private final PolicyUndoElementOp<T> op;
    private final T oldValue;
    private final T newValue;

    public static class EffectRecord {

        public EffectTypeEnum effectTypeEnum;

        public EffectRecord(EffectTypeEnum index) {
            this.effectTypeEnum = index;
        }
    }

    public static class ObligationRecord {

        public IDEffectType type;
        public IObligation obligation;
        public String message = "";

        public ObligationRecord(IDEffectType type, IObligation obligation) {
            this.type = type;
            this.obligation = obligation;
        }

        public ObligationRecord(IDEffectType type, String message) {
            this.type = type;
            this.message = message;
        }
    }
    
    @SuppressWarnings("deprecation")
    public static <T> void add(PolicyUndoElementOp<T> op, T oldValue, T newValue) {
        PolicyUndoElement<T> e = new PolicyUndoElement<T>(op, oldValue, newValue);
        GlobalState.getInstance().addUndoElement(e);
    }

    /**
     * I can't under why you want to create undo element but not adding it to the GlobalState.
     * That's why I mark this constructor as Deprecated. If you find a valid use case, remove the annotation
     * @param op
     * @param oldValue
     * @param newValue
     * @deprecated please use PolicyUndoElement.add(PolicyUndoElementOp<T> op, T oldValue, T newValue) instead 
     */
    @Deprecated 
    public PolicyUndoElement(PolicyUndoElementOp<T> op, T oldValue, T newValue) {
        this.op = op;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /*
     * (non-Javadoc)
     * b
     * @see
     * com.bluejungle.destiny.policymanager.ui.IUndoElement#undo(java.lang.Object
     * , org.eclipse.swt.widgets.Control)
     */
    public boolean undo(Object spec, Control control) {
        IDPolicy policy = (IDPolicy) spec;
        op.undo(policy, oldValue, newValue);
        PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent(policy);
        EVENT_MANAGER.fireEvent(objectModifiedEvent);

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bluejungle.destiny.policymanager.ui.IUndoElement#redo(java.lang.Object
     * , org.eclipse.swt.widgets.Control)
     */
    public boolean redo(Object spec, Control control) {
        IDPolicy policy = (IDPolicy) spec;
        op.redo(policy, oldValue, newValue);
        PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent(policy);
        EVENT_MANAGER.fireEvent(objectModifiedEvent);

        return true;
    }

//    /**
//     * @return Returns the newValue.
//     */
//    public Object getNewValue() {
//        return newValue;
//    }
//
//    /**
//     * @param newValue
//     *            The newValue to set.
//     */
//    public void setNewValue(Object newValue) {
//        this.newValue = newValue;
//    }
//
//    /**
//     * @return Returns the oldValue.
//     */
//    public Object getOldValue() {
//        return oldValue;
//    }
//
//    /**
//     * @param oldValue
//     *            The oldValue to set.
//     */
//    public void setOldValue(Object oldValue) {
//        this.oldValue = oldValue;
//    }

//    /**
//     * @return Returns the op.
//     */
//    public PolicyUndoElementOp getOp() {
//        return op;
//    }

//    /**
//     * @param op
//     *            The op to set.
//     */
//    public void setOp(PolicyUndoElementOp op) {
//        this.op = op;
//    }
}
