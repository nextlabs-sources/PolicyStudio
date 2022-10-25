/*
 * Created on May 11, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.text.DateFormat;
import java.util.Date;

import com.bluejungle.destiny.policymanager.ui.PolicyUndoElement.EffectRecord;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElement.ObligationRecord;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.environment.TimeAttribute;
import com.bluejungle.pf.domain.destiny.obligation.NotifyObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;

/**
 * @author dstarke
 * 
 *         PolicyUndoElement merged to this file.
 * 
 *         TODO this class extended EnumBase but I removed it. There is no
 *         benefit to use enum.
 */
public abstract class PolicyUndoElementOp<T> {
    
    static abstract class PolicyUndoElementChangeOp<_T1> extends PolicyUndoElementOp<_T1> {
        
        protected PolicyUndoElementChangeOp(String name) {
            super(name);
        }
        
        @Override
        public boolean undo(IDPolicy policy, _T1 oldValue, _T1 newValue) {
            set(policy, oldValue);
            return true;
        }

        @Override
        public boolean redo(IDPolicy policy, _T1 oldValue, _T1 newValue) {
            set(policy, newValue);
            return true;
        }

        /**
         * if undo, the value is oldValue.
         * if redom the value is newValue
         * @param policy
         * @param value
         */
        protected abstract void set(IDPolicy policy, _T1 value);
    }

    public static final PolicyUndoElementOp<EffectRecord> CHANGE_EFFECT = 
            new PolicyUndoElementChangeOp<EffectRecord>("CHANGE_EFFECT") {

        protected void set(IDPolicy policy, EffectRecord rec) {
            PolicyHelpers.saveEffect(policy, rec.effectTypeEnum);
        }
    };

    // TODO merge ADD_OBLIGATION and REMOVE_OBLIGATION to CHANGE_OBLIGATION

    public static final PolicyUndoElementOp<ObligationRecord> ADD_OBLIGATION = 
            new PolicyUndoElementOp<ObligationRecord>("ADD_OBLIGATION") {

        @Override
        public boolean undo(IDPolicy policy, ObligationRecord oldValue, ObligationRecord newValue) {
            policy.deleteObligation(newValue.obligation, newValue.type);
            return true;
        }

        @Override
        public boolean redo(IDPolicy policy, ObligationRecord oldValue, ObligationRecord newValue) {
            policy.addObligation(newValue.obligation, newValue.type);
            return true;
        }
    };

    public static final PolicyUndoElementOp<ObligationRecord> REMOVE_OBLIGATION = 
            new PolicyUndoElementOp<ObligationRecord>("REMOVE_OBLIGATION") {

        @Override
        public boolean undo(IDPolicy policy, ObligationRecord oldValue, ObligationRecord newValue) {
            policy.addObligation(oldValue.obligation, oldValue.type);
            return true;
        }

        @Override
        public boolean redo(IDPolicy policy, ObligationRecord oldValue, ObligationRecord newValue) {
            policy.deleteObligation(oldValue.obligation, oldValue.type);
            return true;
        }
    };

    public static final PolicyUndoElementOp<ObligationRecord> CHANGE_NOTIFY_OBLIGATION_ADDRESS = 
            new PolicyUndoElementChangeOp<ObligationRecord>("CHANGE_NOTIFY_OBLIGATION_ADDRESS") {

        @Override
        protected void set(IDPolicy policy, ObligationRecord rec) {
            IObligation[] obligations = policy.getObligationArray(rec.type);
            for (IObligation obligation : obligations) {
                if (obligation instanceof NotifyObligation) {
                    ((NotifyObligation) obligation).setEmailAddresses(rec.message);
                    break;
                }
            }
        }
    };

    public static final PolicyUndoElementOp<ObligationRecord> CHANGE_NOTIFY_OBLIGATION_MSG = 
            new PolicyUndoElementChangeOp<ObligationRecord>("CHANGE_NOTIFY_OBLIGATION_MSGE") {

        @Override
        protected void set(IDPolicy policy, ObligationRecord rec) {
            IObligation[] obligations = policy.getObligationArray(rec.type);
            for (IObligation obligation : obligations) {
                if (obligation instanceof NotifyObligation) {
                    ((NotifyObligation) obligation).setBody(rec.message);
                    break;
                }
            }
        }
    };

    // TODO missing implementation
    // public static final PolicyUndoElementOp CHANGE_DISPLAY_OBLIGATION_MSG =
    // new PolicyUndoElementOp("CHANGE_DISPLAY_OBLIGATION_MSG");

    public static final PolicyUndoElementOp<Date> CHANGE_START_DATE = 
            new PolicyUndoElementOp<Date>("CHANGE_START_DATE") {
        
        @Override
        public boolean undo(IDPolicy policy, Date oldValue, Date newValue) {
            set(policy, oldValue);
            return true;
        }

        @Override
        public boolean redo(IDPolicy policy, Date oldValue, Date newValue) {
            set(policy, newValue);
            return true;
        }
        
        private void set(IDPolicy policy, Date d) {
            if (d == null) {
                PredicateHelpers.removeStartTime(policy);
            } else {
                String dateString = DateFormat.getDateTimeInstance().format(d);
                IExpression exp = TimeAttribute.IDENTITY.build(dateString);
                PredicateHelpers.setStartTime(policy, exp);
            }
        }

    };

    public static final PolicyUndoElementOp<Date> CHANGE_END_DATE = 
            new PolicyUndoElementChangeOp<Date>("CHANGE_END_DATE") {

        @Override
        protected void set(IDPolicy policy, Date d) {
            if (d == null) {
                PredicateHelpers.removeEndTime(policy);
            } else {
                String dateString = DateFormat.getDateTimeInstance().format(d);
                IExpression exp = TimeAttribute.IDENTITY.build(dateString);
                PredicateHelpers.setEndTime(policy, exp);
            }
        }
    };

    public static final PolicyUndoElementOp<IExpression[]> CHANGE_DAILY_SCHEDULE = 
            new PolicyUndoElementChangeOp<IExpression[]>("CHANGE_DAILY_SCHEDULE") {

        @Override
        protected void set(IDPolicy policy, IExpression[] times) {
            if (times == null) {
                PredicateHelpers.removeDailyFromTime(policy);
                PredicateHelpers.removeDailyToTime(policy);
            } else {
                PredicateHelpers.setDailyFromTime(policy, times[0]);
                PredicateHelpers.setDailyToTime(policy, times[1]);
            }
        }
    };

    public static final PolicyUndoElementOp<IExpression> CHANGE_DAILY_SCHEDULE_FROM = 
            new PolicyUndoElementChangeOp<IExpression>("CHANGE_DAILY_SCHEDULE_FROM") {

        @Override
        protected void set(IDPolicy policy, IExpression value) {
            PredicateHelpers.setDailyFromTime(policy, value);
        }
    };

    public static final PolicyUndoElementOp<IExpression> CHANGE_DAILY_SCHEDULE_TO = new PolicyUndoElementChangeOp<IExpression>("CHANGE_DAILY_SCHEDULE_TO") {

        @Override
        protected void set(IDPolicy policy, IExpression value) {
            PredicateHelpers.setDailyToTime(policy, value);
        }
    };

    public static final PolicyUndoElementOp<String> ADD_WEEKDAY = 
            new PolicyUndoElementOp<String>("ADD_WEEKDAY") {

        @Override
        public boolean undo(IDPolicy policy, String oldValue, String newValue) {
            PredicateHelpers.removeWeekdayExpressionFromConditions(policy, oldValue);
            return true;
        }

        @Override
        public boolean redo(IDPolicy policy, String oldValue, String newValue) {
            PredicateHelpers.addWeekdayExpressionToConditions(policy, newValue);
            return true;
        }

    };

    public static final PolicyUndoElementOp<String> REMOVE_WEEKDAY = 
            new PolicyUndoElementOp<String>("REMOVE_WEEKDAY") {

        @Override
        public boolean undo(IDPolicy policy, String oldValue, String newValue) {
            PredicateHelpers.addWeekdayExpressionToConditions(policy, oldValue);
            return true;
        }

        @Override
        public boolean redo(IDPolicy policy, String oldValue, String newValue) {
            PredicateHelpers.removeWeekdayExpressionFromConditions(policy, oldValue);
            return true;
        }

    };

    public static final PolicyUndoElementOp<IPredicate> CHANGE_RECURRENCE_PREDICATE = 
            new PolicyUndoElementOp<IPredicate>("CHANGE_RECURRENCE_PREDICATE") {

        @Override
        public boolean undo(IDPolicy policy, IPredicate oldValue, IPredicate newValue) {
            IPredicate oldPred = (IPredicate) oldValue;
            IPredicate newPred = (IPredicate) newValue;
            // first remove the new value
            if (null != PredicateHelpers.getDOWIMPredicate(newPred)) {
                PredicateHelpers.removeDOWIMPredicate(policy);
                PredicateHelpers.removeWeekdayPredicate(policy);
            } else if (null != PredicateHelpers.getWeekDayPredicate(newPred)) {
                PredicateHelpers.removeWeekdayPredicate(policy);
            } else if (null != PredicateHelpers.getDayOfMonthPredicate(newPred)) {
                PredicateHelpers.removeDayOfMonthPredicate(policy);
            }
            // then add the old value
            if (null != PredicateHelpers.getDOWIMPredicate(oldPred)) {
                // the old predicate will have a DOWIM and a Weekday
                // predicate... add both back

              //TODO didn't have time to finish
//              PredicateHelpers.addPredicateToConditions(policy,
//                    PredicateHelpers.getDOWIMPredicate(oldPred));
//              PredicateHelpers.addPredicateToConditions(policy,
//                    PredicateHelpers.getWeekDayPredicate(oldPred));
            } else if (null != PredicateHelpers.getWeekDayPredicate(oldPred)) {
                // TODO didn't have time to finish
                // PredicateHelpers.addPredicateToConditions(policy, oldPred);
            } else if (null != PredicateHelpers.getDayOfMonthPredicate(oldPred)) {
                // TODO didn't have time to finish
                // PredicateHelpers.addPredicateToConditions(policy, oldPred);
            }
            return true;
        }

        @Override
        public boolean redo(IDPolicy policy, IPredicate oldValue, IPredicate newValue) {
            IPredicate oldPred = (IPredicate) oldValue;
            IPredicate newPred = (IPredicate) newValue;
            // first remove the old value
            if (null != PredicateHelpers.getDOWIMPredicate(oldPred)) {
                PredicateHelpers.removeDOWIMPredicate(policy);
                PredicateHelpers.removeWeekdayPredicate(policy);
            } else if (null != PredicateHelpers.getWeekDayPredicate(oldPred)) {
                PredicateHelpers.removeWeekdayPredicate(policy);
            } else if (null != PredicateHelpers.getDayOfMonthPredicate(oldPred)) {
                PredicateHelpers.removeDayOfMonthPredicate(policy);
            }
            // then add the new value
            if (null != PredicateHelpers.getDOWIMPredicate(newPred)) {
                // the old predicate will have a DOWIM and a Weekday
                // predicate... add both back
                // TODO didn't have time to finish
                // PredicateHelpers.addPredicateToConditions(policy,
                // PredicateHelpers.getDOWIMPredicate(newPred));
                // PredicateHelpers.addPredicateToConditions(policy,
                // PredicateHelpers.getWeekDayPredicate(newPred));
            } else if (null != PredicateHelpers.getWeekDayPredicate(newPred)) {
                // TODO didn't have time to finish
                // PredicateHelpers.addPredicateToConditions(policy, newPred);
            } else if (null != PredicateHelpers.getDayOfMonthPredicate(newPred)) {
                // TODO didn't have time to finish
                // PredicateHelpers.addPredicateToConditions(policy, newPred);
            }
            return true;
        }

    };

    public static final PolicyUndoElementOp<IExpression> CHANGE_RECURRENCE_DATE = 
            new PolicyUndoElementChangeOp<IExpression>("CHANGE_RECURRENCE_DATE") {

        @Override
        protected void set(IDPolicy policy, IExpression value) {
            Relation rel = (Relation) PredicateHelpers.getDayOfMonthPredicate(policy.getConditions());
            IExpression actualExp = rel.getRHS();
            if (!actualExp.evaluate(null).equals(value.evaluate(null))) {
                rel.setRHS(value);
            }
        }
    };

    public static final PolicyUndoElementOp<IExpression> CHANGE_RECURRENCE_DOWIM = 
            new PolicyUndoElementChangeOp<IExpression>("CHANGE_RECURRENCE_DOWIM") {

        @Override
        protected void set(IDPolicy policy, IExpression value) {
            Relation rel = (Relation) PredicateHelpers.getDOWIMPredicate(policy.getConditions());
            IExpression actualExp = rel.getRHS();
            if (!actualExp.evaluate(null).equals(value.evaluate(null))) {
                rel.setRHS(value);
            }
        }
    };

    public static final PolicyUndoElementOp<IExpression> CHANGE_RECURRENCE_WEEKDAY = 
            new PolicyUndoElementChangeOp<IExpression>("CHANGE_RECURRENCE_WEEKDAY") {
        
        @Override
        protected void set(IDPolicy policy, IExpression value) {
            Relation rel = (Relation) PredicateHelpers.getWeekDayPredicate(policy.getConditions());
            IExpression actualExp = rel.getRHS();
            if (!actualExp.evaluate(null).equals(value.evaluate(null))) {
                rel.setRHS(value);
            }
        }
    };

    public static final PolicyUndoElementOp<Long> CHANGE_CONNECTION_TYPE = 
            new PolicyUndoElementChangeOp<Long>("CHANGE_CONNECTION_TYPE") {

        @Override
        protected void set(IDPolicy policy, Long value) {
            if (value == null) {
                PredicateHelpers.removeConnectionType(policy);
            } else {
                long index = value.longValue();
                IExpression exp = Constant.build(index);
                PredicateHelpers.setConnectionType(policy, exp);
            }
        }
    };

    public static final PolicyUndoElementOp<Long> CHANGE_HEARTBEAT_COND = 
            new PolicyUndoElementChangeOp<Long>("CHANGE_HEARTBEAT_COND") {

        @Override
        protected void set(IDPolicy policy, Long value) {
            if (value == null) {
                PredicateHelpers.removeHeartbeat(policy);
            } else {
                long index = value.longValue();
                IExpression exp = Constant.build(index);
                PredicateHelpers.setHeartbeat(policy, exp);
            }
        }
    };
    
    public static final PolicyUndoElementOp<String> CHANGE_ADVANCED_COND = 
           new PolicyUndoElementChangeOp<String>("CHANGE_ADVANCED_COND") {
   
       @Override
       protected void set(IDPolicy policy, String value) {
           try {
            ConditionPredicateHelper.setFreeTypeConditionString(policy, value);
        } catch (PQLException e) {
            throw new RuntimeException("Only parsed pql can be undo/redo.", e);
        }
       }
   };

    /**
     * the name is purely for internal debug only. It should not be show to the
     * user.
     */
    private final String name;

    private PolicyUndoElementOp(String name) {
        this.name = name;
    }

    /**
     * TODO what does the return value means?
     * 
     * @param policy
     * @param oldValue
     * @param newValue
     * @return
     */
    public abstract boolean undo(IDPolicy policy, T oldValue, T newValue);

    /**
     * TODO what does the return value means?
     * 
     * @param policy
     * @param oldValue
     * @param newValue
     * @return
     */
    public abstract boolean redo(IDPolicy policy, T oldValue, T newValue);

}
