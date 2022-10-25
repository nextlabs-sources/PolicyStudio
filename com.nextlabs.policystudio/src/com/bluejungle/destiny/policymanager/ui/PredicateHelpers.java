/*
 * Created on May 2, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.model.CreationExtension;
import com.bluejungle.destiny.policymanager.model.PolicyServerHelper;
import com.bluejungle.destiny.policymanager.ui.ConditionPredicateHelper.ConditionTypeEnum;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecManager;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.common.SpecReference;
import com.bluejungle.pf.domain.destiny.environment.TimeAttribute;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;

/**
 * @author dstarke
 * 
 */
public class PredicateHelpers {

    private static final IDSpecManager sm = ComponentManagerFactory
            .getComponentManager().getComponent(IDSpecManager.COMP_INFO);

    public static void fillInActionComponent(IDSpec spec) {
        CompositePredicate predicate = new CompositePredicate(BooleanOp.OR,
                buildConstantArrayList(false));
        spec.setPredicate(predicate);
    }
    
    public static void fillInStandardComponent(IDSpec spec) {
        if (spec == null) {
            return;
        }

        List<IPredicate> blankLineContents = new ArrayList<IPredicate>();
        blankLineContents.add(PredicateConstants.FALSE);
        blankLineContents.add(PredicateConstants.TRUE);
        CompositePredicate initialCompositionLine = new CompositePredicate(
                BooleanOp.OR, blankLineContents);

        List<IPredicate> initialCompositionList = new ArrayList<IPredicate>();
        initialCompositionList.add(initialCompositionLine);
        initialCompositionList.add(PredicateConstants.TRUE);
        CompositePredicate composition = new CompositePredicate(BooleanOp.AND,
                initialCompositionList);

        List<IPredicate> propList = new ArrayList<IPredicate>();
        propList.add(PredicateConstants.TRUE);
        propList.add(PredicateConstants.TRUE);

        CompositePredicate properties = new CompositePredicate(BooleanOp.AND,
                propList);

        List<IPredicate> parts = new ArrayList<IPredicate>();
        parts.add(composition);
        parts.add(properties);

        CompositePredicate predicate = new CompositePredicate(BooleanOp.AND,
                parts);

        spec.setPredicate(predicate);
    }

    @SuppressWarnings("unchecked")
    public static void fillInPolicy(IDPolicy policy, String type) {
        String context = "";
        IConfigurationElement[] decls = Platform
                .getExtensionRegistry()
                .getConfigurationElementsFor("com.nextlabs.policystudio.editor");
        for (IConfigurationElement element : decls) {
            context = element.getAttribute("context");
            String displayName = element.getAttribute("displayName");
            if (displayName.equals(type)) {
                break;
            }
        }
        decls = Platform.getExtensionRegistry().getConfigurationElementsFor(
                "com.nextlabs.policystudio.creation");
        IConfigurationElement foundElement = null;
        for (IConfigurationElement element : decls) {
            String extensionContext = element.getAttribute("context");
            if (extensionContext.equals(context)) {
                foundElement = element;
                break;
            }
        }
        
        if (foundElement != null) {
            String contributor = foundElement.getContributor().getName();
            Bundle bundle = Platform.getBundle(contributor);

            try {
                Class myClass = bundle.loadClass(foundElement
                        .getAttribute("class"));
                Method postCreationMethod = myClass.getMethod("postCreation",
                        new Class[] {IDPolicy.class});
                Constructor constructor[] = myClass.getConstructors();
                CreationExtension extension = (CreationExtension) constructor[0]
                        .newInstance();
                postCreationMethod.invoke(extension, new Object[] { policy });
            } catch (IllegalArgumentException e1) {
                LoggingUtil.logError(Activator.ID, "error", e1);
            } catch (IllegalAccessException e1) {
                LoggingUtil.logError(Activator.ID, "error", e1);
            } catch (InvocationTargetException e1) {
                LoggingUtil.logError(Activator.ID, "error", e1);
            } catch (ClassNotFoundException e1) {
                LoggingUtil.logError(Activator.ID, "error", e1);
            } catch (SecurityException e1) {
                LoggingUtil.logError(Activator.ID, "error", e1);
            } catch (NoSuchMethodException e1) {
                LoggingUtil.logError(Activator.ID, "error", e1);
            } catch (InstantiationException e1) {
                LoggingUtil.logError(Activator.ID, "error", e1);
            }
        }
    }

    public static List<IPredicate> buildConstantArrayList(boolean isAnd) {
        List<IPredicate> ret = new ArrayList<IPredicate>();
        ret.add(PredicateConstants.TRUE);
        ret.add(isAnd ? PredicateConstants.TRUE : PredicateConstants.FALSE);
        return ret;
    }

    // ------------------------
    // Object Balancing Helpers
    // ------------------------

    /**
     * Adds or removes padding constants from the domain object to ensure that
     * composite predicates remain valid and continue to be evaluated properly
     * when they contain too few real predicates.
     */
    public static void rebalanceDomainObject(CompositePredicate domainObject,
            BooleanOp type) {
        if (type == BooleanOp.AND) {
            rebalanceAndDomainObject(domainObject);
        } else if (type == BooleanOp.OR) {
            rebalanceOrDomainObject(domainObject);
        }
    }

    /**
     * Adds or removes "padding" constants from the domain object The composite
     * must contain at least two predicates Therefore, it is padded with extra
     * TRUE predicates
     */
    private static void rebalanceAndDomainObject(CompositePredicate domainObject) {
        // count the real predicates
        Iterator<IPredicate> iter = domainObject.predicates().iterator();
        int count = 0;
        int trueCount = 0;
        while (iter.hasNext()) {
            IPredicate pred = (IPredicate) iter.next();
            if (pred != PredicateConstants.TRUE) {
                count++;
            } else {
                trueCount++;
            }
        }

        int requiredTrues = 2 - count;
        if (requiredTrues < 0) {
            requiredTrues = 0;
        }

        if (trueCount == requiredTrues) {
            return;
        }

        if (trueCount > requiredTrues) {
            // remove extra TRUES
            int numToRemove = trueCount - requiredTrues;
            for (int i = 0; i < domainObject.predicateCount(); i++) {
                if ((numToRemove > 0)
                        && (domainObject.predicateAt(i) == PredicateConstants.TRUE)) {
                    domainObject.removePredicate(i);
                    numToRemove--;
                }
            }
        } else if (trueCount < requiredTrues) {
            // add additional TRUES
            int numToAdd = requiredTrues - trueCount;
            for (int i = 0; i < numToAdd; i++) {
                domainObject.addPredicate(PredicateConstants.TRUE);
            }
        }
    }

    /**
     * Adds or removes "padding" predicate constants to ensure that a predicate
     * is valid A blank predicate should be padded with a TRUE and a FALSE
     * constant A predicate with one real entry should be padded with a FALSE
     * constant A predicate with two or more entries should not be padded.
     */
    private static void rebalanceOrDomainObject(CompositePredicate domainObject) {
        if (domainObject.predicateCount() == 0) {
            // create the blank structure
            domainObject.addPredicate(PredicateConstants.FALSE);
            domainObject.addPredicate(PredicateConstants.TRUE);
            return;
        }
        if (domainObject.predicateCount() == 1) {
            // if the only predicate is a constant, add the counterpart
            IPredicate predicate = domainObject.predicateAt(0);
            if (predicate == PredicateConstants.FALSE) {
                domainObject.addPredicate(PredicateConstants.TRUE);
            } else if (predicate == PredicateConstants.TRUE) {
                domainObject.addPredicate(PredicateConstants.FALSE);
            } else {
                // otherwise, balance out the list with a FALSE predicate
                domainObject.addPredicate(PredicateConstants.FALSE);
            }
            return;
        }
        // at this point, we have 2 or more predicates
        // count the "real" predicates and what constants we have
        Iterator<IPredicate> iter = domainObject.predicates().iterator();
        int count = 0;
        boolean foundTrue = false;
        boolean foundFalse = false;
        while (iter.hasNext()) {
            IPredicate pred = (IPredicate) iter.next();
            if (pred instanceof PredicateConstants) {
                if (pred == PredicateConstants.TRUE) {
                    foundTrue = true;
                }
                if (pred == PredicateConstants.FALSE) {
                    foundFalse = true;
                }
            } else {
                count++;
            }
        }
        // if the number of real predicates is 0, make sure there is a TRUE and
        // a FALSE predicate
        if (count == 0) {
            if (foundTrue && foundFalse) {
                // structure is ok
                return;
            }
            // strip out everything and build a blank structure;
            // this really shouldn't ever happen- but just in case we're going
            // to assimilate the structure
            for (int i = 0; i < domainObject.predicateCount(); i++) {
                domainObject.removePredicate(i);
            }
            domainObject.addPredicate(PredicateConstants.FALSE);
            domainObject.addPredicate(PredicateConstants.TRUE);
            return;
        }
        // if the number of real predicates is 1, remove all TRUE constants,
        // make sure there is a FALSE constant
        if (count == 1) {
            if (foundTrue) {
                // remove any TRUE constants
                for (int i = 0; i < domainObject.predicateCount(); i++) {
                    if (domainObject.predicateAt(i) == PredicateConstants.TRUE) {
                        domainObject.removePredicate(i);
                    }
                }
                if (!foundFalse) {
                    // add the required FALSE constant
                    domainObject.addPredicate(PredicateConstants.FALSE);
                }
            }
            return;
        }
        // if the number of real predicates is 2 or more, remove all constants
        for (int i = domainObject.predicateCount() - 1; i >= 0; i--) {
            if (domainObject.predicateAt(i) instanceof PredicateConstants) {
                domainObject.removePredicate(i);
            }
        }
    }

    /**
     * Removes a predicate in a domain object at a given location. Ignores TRUE
     * and FALSE constants when calculating the remove index.
     * 
     * @param domainObject
     * @param index
     * @return
     */
    public static IPredicate removePredicateAt(CompositePredicate domainObject, int index) {
        IPredicate ret = null;
        int realPredicateIndex = 0;
        for (int i = 0; i < domainObject.predicateCount(); i++) {
            if (!(domainObject.predicateAt(i) instanceof PredicateConstants)) {
                if (index == realPredicateIndex) {
                    ret = domainObject.removePredicate(i);
                    break;
                }
                realPredicateIndex++;
            }
        }
        rebalanceDomainObject(domainObject, domainObject.getOp());
        return ret;
    }

    /**
     * Adds a predicate to a domain object and automatically manages any TRUE or
     * FALSE constants that may have been inserted to preserve the structure of
     * the composite.
     * 
     * @param domainObject
     * @param predicate
     */
    public static void addPredicate(CompositePredicate domainObject, IPredicate predicate) {
        domainObject.addPredicate(predicate);
        rebalanceDomainObject(domainObject, domainObject.getOp());
    }

    /**
     * Inserts a predicate in a domain object at a given location. Ignores TRUE
     * and FALSE constants when calculating the insertion index.
     * 
     * @param domainObject
     * @param toInsert
     * @param pos
     */
    public static void insertPredicateAt(CompositePredicate domainObject,
            IPredicate toInsert, int pos) {
        int realPredicateIndex = 0;
        boolean inserted = false;
        for (int i = 0; i < domainObject.predicateCount(); i++) {
            if (!(domainObject.predicateAt(i) instanceof PredicateConstants)) {
                if (pos == realPredicateIndex) {
                    domainObject.insertElement(toInsert, i);
                    inserted = true;
                    break;
                }
                realPredicateIndex++;
            }
        }
        if (!inserted) {
            domainObject.insertElement(toInsert, domainObject.predicateCount());
        }
        rebalanceDomainObject(domainObject, domainObject.getOp());
    }

    /**
     * 
     * @param domainObject
     * @param index
     * @return the predicate at the given index. Ignores TRUE and FALSE
     *         constants in calculating the index of a sub-predicate.
     */
    public static IPredicate getPredicateAt(CompositePredicate domainObject,
            int index) {
        int i = 0;
        int realPredicateIndex = 0;
        while (i < domainObject.predicateCount() && realPredicateIndex <= index) {
            if (!(domainObject.predicateAt(i) instanceof PredicateConstants)) {
                if (realPredicateIndex == index) {
                    return domainObject.predicateAt(i);
                }
                realPredicateIndex++;
            }
            i++;
        }
        return null;
    }

    /**
     * 
     * @param domainObject
     * @return the number of predicates that are not the constants TRUE or FALSE
     */
    public static int getRealPredicateCount(CompositePredicate domainObject) {
        Iterator<IPredicate> iter = domainObject.predicates().iterator();
        int count = 0;
        while (iter.hasNext()) {
            IPredicate pred = (IPredicate) iter.next();
            if (!(pred instanceof PredicateConstants)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 
     * @param domainObject
     * @param member
     * @return the index of the member in the domain object when TRUE and FALSE
     *         predicates are ignored
     */
    public static int getRealIndexOfObject(CompositePredicate domainObject,
            IPredicate member) {
        int i = 0;
        int realPredicateIndex = 0;
        while (i < domainObject.predicateCount()) {
            IPredicate obj = domainObject.predicateAt(i);
            if (!(obj instanceof PredicateConstants)) {
                if (obj == member) {
                    return realPredicateIndex;
                }
                realPredicateIndex++;
            }
            i++;
        }
        return -1;
    }

    /**
     * 
     * @param domainObject
     * @return true if the given predicate is a NOT predicate, false otherwise
     */
    public static boolean isNegationPredicate(CompositePredicate domainObject) {
        return domainObject.getOp() == BooleanOp.NOT;
    }

    /**
     * 
     * @param domainObject
     * @return a predicate representing the negation of the given predicate
     */
    public static IPredicate getNegationOfPredicate(CompositePredicate domainObject) {
        if (isNegationPredicate(domainObject)) {
            return domainObject.predicateAt(0);
        } else {
            CompositePredicate ret = new CompositePredicate(BooleanOp.NOT, domainObject);
            return ret;
        }
    }

    /**
     * Remove any references to object of the specified name and type from the
     * domain object
     * 
     * @param domainObject
     * @param name
     * @param entityType
     */
    @SuppressWarnings("deprecation")
    public static void removeReferences(IHasId domainObject, String name,
            EntityType entityType) {
        if (domainObject instanceof IDPolicy) {
            ITarget target = ((IDPolicy) domainObject).getTarget();
            if (entityType == EntityType.ACTION) {
                removeReferences(null, target.getActionPred(), name, entityType);
            } else if (entityType == EntityType.APPLICATION
                    || entityType == EntityType.USER
                    || entityType == EntityType.HOST) {
                removeReferences(null, target.getSubjectPred(), name,
                        entityType);
                removeReferences(null, target.getToSubjectPred(), name,
                        entityType);
            } else if (entityType == EntityType.RESOURCE
                    || entityType == EntityType.PORTAL) {
                removeReferences(null, target.getFromResourcePred(), name,
                        entityType);
                removeReferences(null, target.getToResourcePred(), name,
                        entityType);
            }
        } else if (domainObject instanceof IDSpec) {
            IPredicate pred = ((IDSpec) domainObject).getPredicate();
            removeReferences(null, pred, name, entityType);
        }
    }

    private static void removeReferences(CompositePredicate parent,
            IPredicate pred, String name, EntityType type) {
        if (pred instanceof CompositePredicate) {
            CompositePredicate comp = (CompositePredicate) pred;
            for (int i = comp.predicateCount() - 1; i >= 0; i--) {
                IPredicate subPred = comp.predicateAt(i);
                removeReferences(comp, subPred, name, type);
            }
        } else if (pred instanceof IDSpecRef) {
            IDSpecRef ref = (IDSpecRef) pred;
            if (name.equals(ref.getReferencedName())) {
                if (parent != null) {
                    parent.removePredicate(pred);
                    rebalanceDomainObject(parent, parent.getOp());
                }
            }
        }
    }

    /**
     * 
     * @param actionPred
     * @return a set representing the actions contained in the given composite
     *         predicate
     */
    public static Set<IDAction> getActionSet(CompositePredicate actionPred) {
        Set<IDAction> ret = new HashSet<IDAction>();
        Iterator<IPredicate> iter = actionPred.predicates().iterator();
        while (iter.hasNext()) {
            IPredicate pred = (IPredicate) iter.next();
            if (pred instanceof IDAction) {
                ret.add((IDAction) pred);
            }
        }
        return ret;
    }

    /**
     * Update a composite predicate to represent matching a given set of actions
     * 
     * @param pred
     * @param actionSet
     */
    public static void updateActionSet(CompositePredicate pred,
            Set<IDAction> actionSet) {
        Set<IDAction> oldSet = getActionSet(pred);

        // find out what needs to be added
        List<IDAction> toAdd = new ArrayList<IDAction>();
        Iterator<IDAction> addIter = actionSet.iterator();
        while (addIter.hasNext()) {
            IDAction obj = addIter.next();
            if (!oldSet.contains(obj)) {
                toAdd.add(obj);
            }
        }
        // add new things
        Iterator<IDAction> toAddIter = toAdd.iterator();
        while (toAddIter.hasNext()) {
            Object obj = toAddIter.next();
            pred.addPredicate((IPredicate) obj);
        }
        // remove extra stuff
        oldSet.removeAll(actionSet);
        Iterator<IDAction> iter = oldSet.iterator();
        while (iter.hasNext()) {
            pred.removePredicate((IPredicate) iter.next());
        }
        rebalanceDomainObject(pred, BooleanOp.OR);
    }
    
    // ------------------------
    // Reference Creation Helpers
    // ------------------------

    /**
     * Constructs a reference to a policy component suitable for use in a policy
     * or component definition.
     */
    public static IPredicate getComponentReference(String name,
            SpecType specType) {
        IDSpecRef classRef = (IDSpecRef) sm.getSpecReference(name);
        return classRef;
    }

    public static IPredicate getResourceReference(String name) {
        return ResourceAttribute.NAME.buildRelation(RelationOp.EQUALS, name);
    }

    
    private static final Map<LeafObjectType, SubjectAttribute> leafTypeToSubjectAttributeMap;
    static {
        leafTypeToSubjectAttributeMap = new HashMap<LeafObjectType, SubjectAttribute>();
        leafTypeToSubjectAttributeMap.put(LeafObjectType.USER,        SubjectAttribute.USER_ID);
        leafTypeToSubjectAttributeMap.put(LeafObjectType.USER_GROUP,  SubjectAttribute.USER_LDAP_GROUP_ID);
        leafTypeToSubjectAttributeMap.put(LeafObjectType.CONTACT,     SubjectAttribute.CONTACT_ID);
        leafTypeToSubjectAttributeMap.put(LeafObjectType.APPUSER,     SubjectAttribute.APPUSER_ID);
        leafTypeToSubjectAttributeMap.put(LeafObjectType.ACCESSGROUP, SubjectAttribute.APPUSER_ACCESSGROUP_ID);
        leafTypeToSubjectAttributeMap.put(LeafObjectType.HOST,        SubjectAttribute.HOST_ID);
        leafTypeToSubjectAttributeMap.put(LeafObjectType.HOST_GROUP,  SubjectAttribute.HOST_LDAP_GROUP_ID);
        leafTypeToSubjectAttributeMap.put(LeafObjectType.APPLICATION, SubjectAttribute.APP_ID);
    }
    
    
    public static IPredicate getLeafReference(LeafObject leaf) {
        SubjectAttribute subjectAttribute = leafTypeToSubjectAttributeMap.get(leaf.getType());
        if (subjectAttribute == null) {
            return null;
        }
        return new Relation(
                RelationOp.EQUALS
              , subjectAttribute
              , Constant.build(leaf.getId())
        );
    }

    
    private static final Map<EntityType, SpecType> entityToSpecTypeMap;
    static {
        entityToSpecTypeMap = new HashMap<EntityType, SpecType>();
        entityToSpecTypeMap.put(EntityType.ACTION,      SpecType.ACTION);
        entityToSpecTypeMap.put(EntityType.APPLICATION, SpecType.APPLICATION);
        entityToSpecTypeMap.put(EntityType.HOST,        SpecType.HOST);
        entityToSpecTypeMap.put(EntityType.RESOURCE,    SpecType.RESOURCE);
        entityToSpecTypeMap.put(EntityType.USER,        SpecType.USER);
    }
    
    /**
     * Converts an EntityType to a SpecType.
     * 
     * @param type
     * @return a SpecType for the given EntityType, or null if the type cannot
     *         be converted.
     */
    public static SpecType getSpecType(EntityType type) {
        return entityToSpecTypeMap.get(type);
    }

    /**
     * 
     * Returns the type of the specified predicate
     * 
     * @param predicate
     * @return the type of the predicate
     */
    public static SpecType getPredicateType(IPredicate predicate) {
        // FIXME (sergey) This requires some hardcodinf
        if (predicate instanceof SpecReference) {
            SpecReference ref = (SpecReference) predicate;
            if (ref.isReferenceByName()) {
                String name = ref.getReferencedName();
                return PolicyServerHelper.getSpecType(PolicyServerHelper
                        .getTypeFromComponentName(name));
            }
        } else if (predicate instanceof IRelation) {
            IExpression exp = ((IRelation) predicate).getLHS();
            return getExpressionType(exp);
        }
        return null;
    }

    public static SpecType getExpressionType(IExpression exp) {
        SpecType specType = null;
        if (exp instanceof SpecAttribute) {
            SpecAttribute sa = (SpecAttribute) exp;
            specType = sa.getSpecType();
            if (specType == SpecType.RESOURCE) {
                // Resources have subtypes
                String subtype = sa.getObjectSubTypeName();
                if (ResourceAttribute.PORTAL_SUBTYPE.equals(subtype)) {
                    specType = SpecType.PORTAL;
                }
            }
        }
        return specType;
    }
    
    
    
    
    /*
     * <conditions>
     * TODO merge all condition operations to ConditionTypeEnum
     */
    
    /**
     * @param condition
     * @return the predicate representing the enforcement start time
     */
    public static IPredicate getStartTime(IPredicate condition) {
        return ConditionTypeEnum.START_TIME.get(condition);
    }
    
    /**
     * Add or set the start time/date for a policy
     * 
     * @param policy
     * @param time
     */
    public static void setStartTime(IDPolicy policy, IExpression time) {
        ConditionTypeEnum.START_TIME.set(policy, time);
    }
    
    /**
     * Removes the start date condition from the policy
     * 
     * @param policy
     */
    public static void removeStartTime(IDPolicy policy) {
        ConditionTypeEnum.START_TIME.remove(policy);
    }
    

    /**
     * 
     * @param condition
     * @return the predicate representing the enforcement end time
     */
    public static IPredicate getEndTime(IPredicate condition) {
        return ConditionTypeEnum.END_TIME.get(condition);
    }
    
    /**
     * Add or set the end time/date for a policy
     * 
     * @param policy
     * @param time
     */
    public static void setEndTime(IDPolicy policy, IExpression time) {
        ConditionTypeEnum.END_TIME.set(policy, time);
    }
    
    /**
     * Remove the end date condition from the policy
     * 
     * @param policy
     */
    public static void removeEndTime(IDPolicy policy) {
        ConditionTypeEnum.END_TIME.remove(policy);
    }

    
    /**
     * 
     * @param condition
     * @return the predicate representing the start time for daily enforcement
     */
    public static IPredicate getDailyFromTime(IPredicate condition) {
        return ConditionTypeEnum.DAILY_FROM_TIME.get(condition);
    }
    
    /**
     * Add or set the end time for daily enforcement in a policy
     * 
     * @param policy
     * @param time
     */
    public static void setDailyFromTime(IDPolicy policy, IExpression time) {
        ConditionTypeEnum.DAILY_FROM_TIME.set(policy, time);
    }
    
    /**
     * Remove the beginning time for daily enforcement from the policy
     * 
     * @param policy
     */
    public static void removeDailyFromTime(IDPolicy policy) {
        ConditionTypeEnum.DAILY_FROM_TIME.remove(policy);
    }
    
    
    /**
     * 
     * @param condition
     * @return the predicate representing the end time for daily enforcement
     */
    public static IPredicate getDailyToTime(IPredicate condition) {
        return ConditionTypeEnum.DAILY_TO_TIME.get(condition);
    }
    
    /**
     * Add or set the beginning time for daily enforcement in a policy
     * 
     * @param policy
     * @param time
     */
    public static void setDailyToTime(IDPolicy policy, IExpression time) {
        ConditionTypeEnum.DAILY_TO_TIME.set(policy, time);
    }
    
    /**
     * Remove the end time for daily enforcement from the policy
     * 
     * @param policy
     */
    public static void removeDailyToTime(IDPolicy policy) {
        ConditionTypeEnum.DAILY_TO_TIME.remove(policy);
    }
    
    
    public static IPredicate getConnectionType(IPredicate condition) {
        return ConditionTypeEnum.CONNECTION_TYPE.get(condition);
    }
    
    public static void setConnectionType(IDPolicy policy, IExpression type) {
        ConditionTypeEnum.CONNECTION_TYPE.set(policy, type);
    }
    
    public static void removeConnectionType(IDPolicy policy) {
        ConditionTypeEnum.CONNECTION_TYPE.remove(policy);
    }

    
    public static IPredicate getHeartbeat(IPredicate condition) {
        return ConditionTypeEnum.HEARTBEAT.get(condition);
    }
    
    public static void setHeartbeat(IDPolicy policy, IExpression type) {
        ConditionTypeEnum.HEARTBEAT.set(policy, type);
    }
    
    public static void removeHeartbeat(IDPolicy policy) {
        ConditionTypeEnum.HEARTBEAT.remove(policy);
    }

    
    public static IPredicate getConnectionSite(IPredicate condition) {
        return ConditionTypeEnum.CONNECTION_SITE.get(condition);
    }
    
    public static void setConnectionSite(IDPolicy policy, IExpression type) {
        ConditionTypeEnum.CONNECTION_SITE.set(policy, type);
    }
    
    public static void removeConnectionSite(IDPolicy policy) {
        ConditionTypeEnum.CONNECTION_SITE.remove(policy);
    }

    /**
     * 
     * @param condition
     * @return the predicate representing the day of the week conditions
     */
    public static IPredicate getWeekDayPredicate(IPredicate condition) {
        return ConditionTypeEnum.WEEKDAY.get(condition);
    }
    
    /**
     * Add or set the predicate representing the enforcement days of the week in
     * a policy
     * 
     * @param policy
     * @param newWeekdays
     */
    public static void setWeekdayPredicate(IDPolicy policy, IPredicate newWeekdays) {
        ConditionTypeEnum.WEEKDAY.set(policy, newWeekdays); 
    }

    public static void addWeekdayExpressionToConditions(IDPolicy policy, String name) {
        IExpression expression = TimeAttribute.WEEKDAY.build(name);
        ConditionTypeEnum.WEEKDAY.add(policy, expression); 
    }
    
    /**
     * Remove the predicate representing the enforcement days of the week from
     * the policy
     * 
     * @param policy
     */
    public static void removeWeekdayPredicate(IDPolicy policy) {
        ConditionTypeEnum.WEEKDAY.remove(policy);
    }
    
    public static void removeWeekdayExpressionFromConditions(IDPolicy policy,
            String name) {
        IPredicate pred = getWeekDayPredicate(policy.getConditions());
        IExpression expToRemove = TimeAttribute.WEEKDAY.build(name);
        if (pred instanceof CompositePredicate) {
            CompositePredicate comp = (CompositePredicate) pred;
            Iterator<IPredicate> iter = comp.predicates().iterator();
            while (iter.hasNext()) {
                Relation rel = (Relation) iter.next();
                IExpression exp = rel.getRHS();
                if (exp.evaluate(null).equals(expToRemove.evaluate(null))) {
                    comp.removePredicate(rel);
                    break;
                }
            }
            if (comp.predicateCount() == 1) {
                // unrwap the single predicates
                PredicateHelpers.setWeekdayPredicate(policy, comp.predicateAt(0));
            }
        }
    }
    

    /**
     * 
     * @param condition
     * @return the predicate representing the day of the month conditions
     */
    public static IPredicate getDayOfMonthPredicate(IPredicate condition) {
        return ConditionTypeEnum.DAY_OF_MONTH.get(condition);
    }
    
    public static void setDailyOfMonthTime(IDPolicy policy, IExpression time) {
        ConditionTypeEnum.DAY_OF_MONTH.set(policy, time);
    }
    
    public static void removeDayOfMonthPredicate(IDPolicy policy) {
        ConditionTypeEnum.DAY_OF_MONTH.remove(policy);
    }

    /**
     * 
     * @param condition
     * @return the predicate representing the day of the week in the month
     *         conditions
     */
    public static IPredicate getDOWIMPredicate(IPredicate condition) {
        return ConditionTypeEnum.DOWIM.get(condition);
    }
    
    public static void setDOWIM(IDPolicy policy, IExpression expression) {
        ConditionTypeEnum.DOWIM.set(policy, expression);
    }
    
    public static void removeDOWIMPredicate(IDPolicy policy) {
        ConditionTypeEnum.DOWIM.remove(policy);
    }
    

    public static IPredicate getFullDOWIMInfoPredicates(IPredicate condition) {
        IPredicate dowim = getDOWIMPredicate(condition);
        IPredicate wdp = getWeekDayPredicate(condition);
        if (dowim == null || wdp == null) {
            return null;
        }
        List<IPredicate> list = new ArrayList<IPredicate>();
        list.add(dowim);
        list.add(wdp);
        return new CompositePredicate(BooleanOp.AND, list);
    }
    /*
     * </conditions>
     */
}
