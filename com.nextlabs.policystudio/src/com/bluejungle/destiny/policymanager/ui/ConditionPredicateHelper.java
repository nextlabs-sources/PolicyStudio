package com.bluejungle.destiny.policymanager.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.environment.HeartbeatAttribute;
import com.bluejungle.pf.domain.destiny.environment.RemoteAccessAttribute;
import com.bluejungle.pf.domain.destiny.environment.TimeAttribute;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

public class ConditionPredicateHelper {
    
    /**
     * for v1
     *
     * Find the first matching predicate.
     */
    private static abstract class ConditionFinder extends DefaultPredicateVisitor{
        Stack<ICompositePredicate> parents = new Stack<ICompositePredicate>();;
        IPredicate matchingPredicate = null;
        
        @Override
        public void visit(IRelation relation) {
            if (matchingPredicate != null) {
                return;
            }

            if (isMatch(relation)) {
                matchingPredicate = relation;
            }
        }
        
        @Override
        public void visit(ICompositePredicate pred, boolean preorder) {
            if (matchingPredicate != null) {
                return;
            }

            if (isMatch(pred)) {
                matchingPredicate = pred;
                return;
            }
            
            if(preorder){
                parents.push(pred);
            } else {
                parents.pop();
            }
        }
        
        protected abstract boolean isMatch(IPredicate predicate);
    }
    
    /*
     *  this could be V1 or V2
     *  If V1, may looks like
     *       AND
     *      /   \
     *     C1   C2
     *    
     *  If V2, it is
     *       AND (cp1)
     *      /   \
     *   TRUE  AND (cp2)
     *         /  \
     *       ...  ...
     *  We can distinguish two by LHS 
     */
    private static class ConditionWalker {
        IPredicate v1Root= null;
        IPredicate freeTypeRoot = null;
        
        void walk(IPredicate predicate) {
            if (predicate instanceof ICompositePredicate){
                ICompositePredicate cp1 = (ICompositePredicate)predicate;
                if(cp1.getOp() == BooleanOp.AND 
                        && cp1.predicateCount() == 2) {
                    walkCp1(cp1);
                }
            }
            
            if (v1Root == null) {
                v1Root = predicate;
            }
        }
        
        void walkCp1(ICompositePredicate cp1) {
            IPredicate lhs = cp1.predicateAt(0);
            IPredicate rhs = cp1.predicateAt(1);
            if (lhs instanceof PredicateConstants 
                    && ((PredicateConstants) lhs) == PredicateConstants.TRUE) {
                if(rhs instanceof ICompositePredicate) {
                    ICompositePredicate cp2 = (ICompositePredicate)rhs;
                    walkCp2(cp2);
                }
            }
        }
        
        void walkCp2(ICompositePredicate cp2) {
            if(cp2.getOp() == BooleanOp.AND 
                    && cp2.predicateCount() == 2) {
                walkV1Root(cp2.predicateAt(0));
                walkFreeTypeRoot(cp2.predicateAt(1));
            }
        }
        
        void walkV1Root(IPredicate predicate){
            v1Root = predicate;
        }
        
        void walkFreeTypeRoot(IPredicate predicate){
            freeTypeRoot = predicate;
        }
        
    }
    
    /**
     * 
     * @param predicate could be v1 or v2 condition
     * @return the v1 structure
     */
    static IPredicate getV1Predicate(IPredicate predicate) {
        ConditionWalker walker = new ConditionWalker();
        walker.walk(predicate);
        return walker.v1Root;
    }
    
    /**
     * 
     * @param predicate could be v1 or v2 condition
     * @return the v1 structure
     */
    static IPredicate getFreeTypePredicate(IPredicate predicate){
        ConditionWalker walker = new ConditionWalker();
        walker.walk(predicate);
        return walker.freeTypeRoot; 
    }
    
    public static String getFreeTypeConditionString(IPredicate predicate) {
        IPredicate freeTypeRoot = getFreeTypePredicate(predicate);
        if(freeTypeRoot == null || freeTypeRoot == PredicateConstants.TRUE){
            return null;
        }
        
        DomainObjectFormatter dof = new DomainObjectFormatter();
        dof.formatRef(freeTypeRoot);
        
        return dof.getPQL();
    }
    
    public static void setFreeTypeConditionString(IPolicy policy, String freeTypeString) throws PQLException {
        IPredicate condition = policy.getConditions();
        
        if(freeTypeString == null || freeTypeString.trim().length() == 0){
            //this is remove
            
            if(condition == null){
                //do nothing because there is nothing
                return;
            }
            IPredicate v1Root = getV1Predicate(condition);
            
            policy.setConditions(buildV2PredicateFromV1(v1Root));
            return;
        }
        
        DomainObjectBuilder dob = new DomainObjectBuilder("POLICY \"dummy\" WHERE " + freeTypeString);
        
        final AtomicReference<IPredicate> aPredicate = new AtomicReference<IPredicate>();
        
        IPQLVisitor visitor = new DefaultPQLVisitor() {
            
            @Override
            public void visitPolicy(DomainObjectDescriptor descriptor,
                    IDPolicy policy) {
                // I only care the first one
                if (aPredicate.get() == null) {
                    aPredicate.set(policy.getConditions());
                }
            }
        };
        
        dob.processInternalPQL(visitor);
        IPredicate predicate = aPredicate.get();
        if (predicate != null) {
            if(condition == null){
                policy.setConditions(buildV2PredicateFromFreeType(predicate));
            } else {
                setFreeTypePredicate(condition, predicate);
            }
        } else {
            throw new PQLException(new IllegalArgumentException("Doesn't have a component"));
        }
    }
    
    /**
     * 
     * @param condition must be v2 strucutrue
     */
    private static void setV1Predicate(IPredicate predicate, final IPredicate v1condition){
        ConditionWalker walker = new ConditionWalker(){
            @Override
            void walkCp2(ICompositePredicate cp2) {
                // replace the lhs
                ((CompositePredicate)cp2).removePredicate(0);
                ((CompositePredicate)cp2).insertElement(v1condition, 0);
            }
        };
        walker.walk(predicate);
    }
    
    /**
     * make sure this is v2 structure
     * @param predicate
     * @param newFreeTypeRoot
     */
    private static void setFreeTypePredicate(IPredicate predicate, final IPredicate newFreeTypeRoot){
        ConditionWalker walker = new ConditionWalker(){
            @Override
            void walkCp2(ICompositePredicate cp2) {
                // replace the lhs
                ((CompositePredicate)cp2).removePredicate(1);
                ((CompositePredicate)cp2).insertElement(newFreeTypeRoot, 1);
            }
        };
        walker.walk(predicate);
    }
    
    static IPredicate buildV2PredicateFromV1(IPredicate predicate) {
        return buildV2Structure(predicate, PredicateConstants.TRUE);
    }
    
    private static IPredicate buildV2PredicateFromFreeType(IPredicate freeTypePredicate) {
        return buildV2Structure(PredicateConstants.TRUE, freeTypePredicate);
    }
    
    private static IPredicate buildV2Structure(IPredicate v1Root, IPredicate freeTypeRoot) {
        /*
         * The new condition type will be
         *          AND (root)
         *        /     \ 
         *    TRUE      AND 
         *            /     \
         *      v1Root     freeTypeRoot
         */
        CompositePredicate root = new CompositePredicate(BooleanOp.AND, PredicateConstants.TRUE);
        root.addPredicate( new CompositePredicate(BooleanOp.AND, 
                Arrays.asList(v1Root, freeTypeRoot)
        ));
        
        return root;
    }
    
    
    /*
     * The new condition type will be
     *          AND
     *        /     \ 
     *    TRUE     AND
     *            /   \
     *          OLD   NEW
     *          
     *  
     *          
     * The condition is identified by Attribute and relation.
     * Such as START_TIME must be >= or > relation and TimeAttribute.IDENTITY
     * 
     * For WEEKDAY, DAY_OF_MONTH and DOWIM are bit tricky. Since they may have multiple predicates.
     * If they have more than one predicate, assume they all under one composite predicate!
     * This assumption may be changed in the future. Beware!!!
     */

    static enum ConditionTypeEnum {
        START_TIME(     true, RelationOp.GREATER_THAN_EQUALS, TimeAttribute.IDENTITY, RelationOp.GREATER_THAN)
      , END_TIME(       true, RelationOp.LESS_THAN_EQUALS,    TimeAttribute.IDENTITY, RelationOp.LESS_THAN)
      , DAILY_FROM_TIME(true, RelationOp.GREATER_THAN_EQUALS, TimeAttribute.TIME,     RelationOp.GREATER_THAN)
      , DAILY_TO_TIME(  true, RelationOp.LESS_THAN_EQUALS,    TimeAttribute.TIME,     RelationOp.LESS_THAN)
      , CONNECTION_TYPE(true, RelationOp.EQUALS,              RemoteAccessAttribute.REMOTE_ACCESS)
      , HEARTBEAT(      true, RelationOp.GREATER_THAN,        HeartbeatAttribute.TIME_SINCE_LAST_HEARTBEAT)
      , CONNECTION_SITE(true, RelationOp.EQUALS,              RemoteAccessAttribute.REMOTE_ADDRESS)
      , DAY_OF_MONTH   (true, RelationOp.EQUALS,              TimeAttribute.DATE)
      , DOWIM(          true, RelationOp.EQUALS,              TimeAttribute.DOWIM)
      
        /**
         * week day predicate is either
         * 1. A single relation on WEEKDAY attribute
         * 2. composite predicates containing only relations on WEEKDAY attributes
         */
        //TODO should include RelationOp.NOT_EQUALS?
      , WEEKDAY (     false, RelationOp.EQUALS, TimeAttribute.WEEKDAY)
      
        ;
        
        final boolean singleAttribute;
        
        /**
         * the relation used for building
         */
        final RelationOp buildRelationOp;
        
        /**
         * any possible matching relationOps
         * if null, match everything
         */
        final Set<RelationOp> matchedRelationOps;
        
        final IAttribute attribute;
        
        /**
         * this enum can build predicate
         * @param buildRelationOp
         * @param attribute
         * @param extraMatchingRelationOps
         */
        private ConditionTypeEnum(
                boolean singleAttribute
              ,  RelationOp buildRelationOp
              , IAttribute attribute
              , RelationOp... extraMatchingRelationOps
        ) {
            this.singleAttribute = singleAttribute;
            this.buildRelationOp = buildRelationOp;
            this.attribute = attribute;
            if (buildRelationOp != null
                    || (extraMatchingRelationOps != null 
                            && extraMatchingRelationOps.length > 0)) {
                matchedRelationOps = new HashSet<RelationOp>();
                if (buildRelationOp != null) {
                    matchedRelationOps.add(buildRelationOp);
                }
                if(extraMatchingRelationOps != null){
                    Collections.addAll(matchedRelationOps, extraMatchingRelationOps);
                }
            } else {
                matchedRelationOps = null;
            }
            
        }
        
        public boolean isContain(IPredicate predicate) {
            return get(predicate) != null;
        }
        
        public boolean isPredicateMatch(IPredicate predicate) {
            if(predicate instanceof IRelation){
                IRelation relation = (IRelation)predicate;
                if (relation.getLHS() == attribute) {
                    if(matchedRelationOps == null ){
                        //I don't need to check the relationOp, null means everything match
                        return true;
                    } else {
                        RelationOp op = relation.getOp();
                        if( matchedRelationOps.contains(op)){
                            return true;
                        }
                    }    
                }
            }
            
            if (!singleAttribute) {
                if (predicate instanceof ICompositePredicate) {
                    ICompositePredicate comp = (ICompositePredicate) predicate;
                    BooleanOp op = comp.getOp();
                    if (op == BooleanOp.OR) {
                        
                        //make sure all predicate are WEEKDAY attribute
                        for (IPredicate pred : comp.predicates()) {
                            if (!(pred instanceof IRelation)) {
                                return false;
                            }
                            IExpression exp = ((IRelation) pred).getLHS();
                            if (exp != attribute) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
            return false;
        }
        
        /**
         * find the first matching predicate in the <code>predicate</code>
         * @param predicate
         * @return
         */
        public IPredicate get(IPredicate predicate) {
            if(predicate == null){
                return null;
            }
            ConditionFinder finder = new ConditionFinder() {
                @Override
                protected boolean isMatch(IPredicate relation) {
                    return isPredicateMatch(relation);
                }
            };
            predicate.accept(finder, IPredicateVisitor.PREORDER);
            
            if(finder.matchingPredicate != null ){
                if(singleAttribute){
                     assert finder.matchingPredicate instanceof IRelation;
                }
            }
            
            return finder.matchingPredicate;
        }
        
        /**
         * Remove a predicate from the conditions of a policy. 
         * The predicate to remove is identified by the isMatch method
         * Only the first matched one is removed
         * @param policy
         */
        public boolean remove(IPolicy policy){
            IPredicate condition = policy.getConditions();
            if (condition == null) {
                return false;
            }
            
            IPredicate v1Condition = getV1Predicate(condition);
            
            ConditionFinder remover = new ConditionFinder(){
                @Override
                protected boolean isMatch(IPredicate relation) {
                    return isPredicateMatch(relation);
                }
            };
            
            v1Condition.accept(remover, IPredicateVisitor.PREPOSTORDER);
            if(remover.matchingPredicate == null){
                return false;
            }
            
            if(remover.parents.isEmpty()){
                if(v1Condition == condition){
                    //v1 structure
                    policy.setConditions(null);
                } else{
                    //v2 structure
                    
                    IPredicate freeTypeCondition = getFreeTypePredicate(condition);
                    if(freeTypeCondition == PredicateConstants.TRUE){
                        //remove all condition
                        policy.setConditions(null);
                    } else {
                        // put a dummy condition
                        setV1Predicate(condition, PredicateConstants.TRUE);
                    }
                }
            } else {
                CompositePredicate parent = (CompositePredicate)remover.parents.pop();
                parent.removePredicate(remover.matchingPredicate);
                if(parent.predicateCount() == 1 ){
                    IPredicate toPromote = parent.predicateAt(0);
                    
                    if(remover.parents.empty()){
                        // hit the root
                        setV1Predicate(condition, toPromote);
                    } else {
                        CompositePredicate grandpa = (CompositePredicate)remover.parents.pop();
                        grandpa.removePredicate(parent);
                        grandpa.addPredicate(toPromote);
                    }
                }
            }
            return true;
        }
        
        /**
         * add the condition. If the condition can only accept one expression, replace
         *   the existing one.
         * @param policy
         * @param expression
         */
        public void add(IPolicy policy, IExpression expression){
            IPredicate condition = policy.getConditions();
            
            // if condition is null, create a new condition root
            if (condition != null) {
                if (tryReplace(policy, expression)) {
                    return;
                }
            }
            
            // if no replace, try add
            IPredicate newPredicate = buildRelation(expression);
            
            // the predicate doesn't exist, we need to add. Always add since it doesn't exist
            add(policy, newPredicate);
        }
        
        protected boolean tryReplace(IPolicy policy, IExpression expression) {
            IPredicate condition = policy.getConditions();
            assert condition != null;
            
            if (singleAttribute) {
               IPredicate matchingPredicate = get(condition);
               if (matchingPredicate instanceof Relation) {
                   ((Relation)matchingPredicate).setRHS(expression);
                   return true;
               } else if (matchingPredicate != null){
                   throw new UnsupportedOperationException("matchingPredicate=" + matchingPredicate.toString());
               }
               
               // no matching predicate
               
               return false;
            }else {
                IPredicate newPredicate = buildRelation(expression);
                return tryReplace(policy, newPredicate);
            }
        }
        
        /**
         * this will add a predicate to the condition.
         * By default, it doesn't know how to do a replace
         * @param policy
         * @param predicate
         */
        protected void add(IPolicy policy, IPredicate predicate){
            IPredicate condition = policy.getConditions();
            
            // if condition is null, create a new condition root
            if (condition == null) {
                IPredicate v2Root = buildV2PredicateFromV1(predicate);
                policy.setConditions(v2Root);
                return;
            }
            
            IPredicate oldV1Condition = getV1Predicate(condition);
            IPredicate freeTypeCondition = getFreeTypePredicate(condition);
            
            if(freeTypeCondition == null) {
                // v1 structure
                // convert to v2 first
                
                condition = buildV2Structure(condition, oldV1Condition);
                policy.setConditions(condition);
            }
            
            
            if (tryReplace(policy, predicate)) {
                return;
            }
            
            //v2
            if (oldV1Condition == PredicateConstants.TRUE) {
                setV1Predicate(condition, predicate);
            } else if (oldV1Condition instanceof IRelation) {
                predicate = new CompositePredicate(BooleanOp.AND,
                        Arrays.asList(oldV1Condition, predicate));
                
                setV1Predicate(condition, predicate);
            } else if (oldV1Condition instanceof CompositePredicate) {
                 ((CompositePredicate)oldV1Condition).addPredicate(predicate);
            } else {
                throw new IllegalArgumentException("I don't know how to add '" + oldV1Condition + "'.");
            }
        }
        
        protected boolean tryReplace(IPolicy policy, IPredicate predicate) {
            if(singleAttribute){
                assert predicate instanceof IRelation;
                return false;
            } else {
                IPredicate condition = policy.getConditions();
                if (getFreeTypePredicate(condition) == null) {
                    // v1 structure
                    // convert to v2 first

                    IPredicate oldV1Condition = getV1Predicate(condition);
                    condition = buildV2Structure(condition, oldV1Condition);
                    policy.setConditions(condition);
                }
                
                IPredicate v1Condition = getV1Predicate(condition);
                
                ConditionFinder finder = new ConditionFinder(){
                    @Override
                    protected boolean isMatch(IPredicate relation) {
                        return isPredicateMatch(relation);
                    }
                };
                
                v1Condition.accept(finder, IPredicateVisitor.PREPOSTORDER);
                if(finder.matchingPredicate == null){
                    return false;
                }
                
                if(finder.parents.isEmpty()){
                    // this is the root
                     if(v1Condition == PredicateConstants.TRUE){
                         setV1Predicate(condition, predicate);
                     } else if (v1Condition instanceof IRelation) {
                         setV1Predicate(condition, mergeNew((IRelation)v1Condition, predicate));
                     } else if (v1Condition instanceof CompositePredicate) {
                         mergeExisiting((CompositePredicate)v1Condition, predicate);
                     } else {
                         throw new IllegalArgumentException(
                                 "I don't know how to add '" + v1Condition + "'.");
                     }
                } else {
                    if (finder.matchingPredicate instanceof IRelation) {
                        //need to convert to CompositePredicate
                        
                        CompositePredicate parent = (CompositePredicate)finder.parents.pop();
                        parent.removePredicate(finder.matchingPredicate);

                        predicate = mergeNew((IRelation)finder.matchingPredicate, predicate);
                        
                        parent.addPredicate(predicate);
                    }else if (finder.matchingPredicate instanceof CompositePredicate) {
                        mergeExisiting((CompositePredicate) finder.matchingPredicate, predicate);
                    } else {
                        throw new IllegalArgumentException(
                                "I don't know how to add '" + finder.matchingPredicate + "'.");
                    }
                }
                return true;
            }
        }
        
        private void mergeExisiting(CompositePredicate existing, IPredicate predicate) {
            assert existing != null;
            assert predicate != null;

            CompositePredicate existingCp = (CompositePredicate) existing;
            if (predicate instanceof ICompositePredicate) {
                assert existingCp.getOp() == ((ICompositePredicate) predicate)
                        .getOp();

                for (IPredicate p : ((ICompositePredicate) predicate).predicates()) {
                    existingCp.addPredicate(p);
                }
            } else {
                existingCp.addPredicate(predicate);
            }
        }
        
        private CompositePredicate mergeNew(IRelation existing, IPredicate predicate) {
            assert existing != null;
            assert predicate != null;

            CompositePredicate toReturn;
            if (predicate instanceof CompositePredicate) {
                toReturn = ((CompositePredicate) predicate);
                toReturn.addPredicate(existing);
            } else {
                toReturn = new CompositePredicate(BooleanOp.OR,
                        Arrays.asList(existing, predicate));
            }
            return toReturn;
        }
       
        
        /**
         * set the condition. If the condition already exists, replace the value
         * @param policy
         * @param expression
         */
        public void set(IPolicy policy, IExpression expression) {
            remove(policy);
            if (expression != null) {
                add(policy, expression);
            }
        }
        
        
        public void set(IPolicy policy, IPredicate predicate) {
            assert isPredicateMatch(predicate);
            if(singleAttribute){
                assert predicate instanceof IRelation;
            }
            
            
            remove(policy);
            add(policy, predicate);
        }
        
        IRelation buildRelation(IExpression expression) {
            assert buildRelationOp != null;
            assert attribute != null;
            return new Relation(
                  buildRelationOp
                , attribute
                , expression);
        }
    }
    
}
