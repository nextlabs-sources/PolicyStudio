package com.bluejungle.destiny.policymanager.editor;

import java.util.HashMap;
import java.util.Map;

import com.bluejungle.destiny.policymanager.ui.PolicyHelpers.EffectTypeEnum;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;

public final class CommonPolicyConstants {
//    public static final String[] EFFECTS = {
//            EditorMessages.POLICYEDITOR_DENY
//          , EditorMessages.POLICYEDITOR_ALLOW
//          , EditorMessages.POLICYEDITOR_MONITOR 
//    };
//    
//    public static final Map<EffectTypeEnum, Integer> effectTypeToIndexMap;
//    public static final Map<Integer, EffectTypeEnum> indexToEffectTypeMap;
//    static {
//        effectTypeToIndexMap = new HashMap<EffectTypeEnum, Integer>();
//        indexToEffectTypeMap = new HashMap<Integer, EffectTypeEnum>();
//        
//        effectTypeToIndexMap.put(EffectTypeEnum.DENY, 0);
//        indexToEffectTypeMap.put(0, EffectTypeEnum.DENY);
//        
//        effectTypeToIndexMap.put(EffectTypeEnum.ALLOW, 1);
//        indexToEffectTypeMap.put(1, EffectTypeEnum.ALLOW);
//        
//        effectTypeToIndexMap.put(EffectTypeEnum.MONITOR, 2);
//        indexToEffectTypeMap.put(2, EffectTypeEnum.MONITOR);
//    }
    
    
    public static final String[] DAY_LABELS = { 
            "Sun"
          , "Mon"
          , "Tue"
          , "Wed"
          , "Thu"
          , "Fri"
          , "Sat" };
    
    public static final String[] DAY_NAMES = { 
            "Sunday"
          , "Monday"
          , "Tuesday"
          , "Wednesday"
          , "Thursday"
          , "Friday"
          , "Saturday" };
    
    public static final String[] DAY_COUNT_LABELS = { 
            "First"
          , "Second"
          , "Third"
          , "Last" };
    
    
    /**
     * copy from 
     *   - CommonPolicyDetailsComposite
     *   - ComponentDetailsComposite
     *   - ComponentPdfGenerator 
     *   - ClassListControl ( x3 !)
     *   
     *  There is a reverse mapping in PredicateHelpers and EntityInfoProvider but they are 
     *  not exactly same and they don't match this mapping.
     */
    public static Map<SubjectAttribute, LeafObjectType> SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP;
    static {
        SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP = new HashMap<SubjectAttribute, LeafObjectType>();

        SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.USER_ID,            LeafObjectType.USER);
        SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.HOST_ID,            LeafObjectType.HOST);
        SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.APP_ID,             LeafObjectType.APPLICATION);
        SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.USER_LDAP_GROUP_ID, LeafObjectType.USER_GROUP);
        SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.HOST_LDAP_GROUP_ID, LeafObjectType.HOST_GROUP);
        SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.CONTACT_ID,         LeafObjectType.CONTACT);
    }
    
    public static final String[] operators1 = new String[] { 
            " in "
          , " not in " };
    public static final String[] operators21 = new String[] { 
            " into "
          , " outside " };
    public static final String[] operators22 = new String[] { 
            " And into "
          , " And outside " };
    
}
