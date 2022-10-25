/*
 * Created on Mar 14, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.controls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.bluejungle.destiny.policymanager.editor.EditorMessages;
import com.bluejungle.destiny.policymanager.editor.IEditorPanel;
import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.model.PolicyServerHelper;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.PropertyExpressionUndoElement;
import com.bluejungle.destiny.policymanager.ui.PropertyExpressionUndoElementOp;
import com.bluejungle.destiny.policymanager.util.PlatformUtils;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IExpressionReference;
import com.bluejungle.framework.expressions.IExpressionVisitor;
import com.bluejungle.framework.expressions.IFunctionApplication;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lifecycle.AttributeDescriptor;
import com.bluejungle.pf.destiny.lifecycle.AttributeType;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.LocationReference;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates
 *          .xml#2 $:
 */

public class PropertyExpressionControl extends Composite {

    private final String ALL_USERS = "All Users";
    private final String OWNER_USER_COMPONENT = "Owner User Component";
    private IEditorPanel editorPanel = null;
    private List<Combo> propControlArray = new ArrayList<Combo>();
    private List<Combo> opControlArray = new ArrayList<Combo>();
    private List<Widget> valueControlArray = new ArrayList<Widget>();
    private List<Button> removeButtonArray = new ArrayList<Button>();
    private List<Control> tabListArray = new ArrayList<Control>();
    private List<Label> propNameLabelArray = new ArrayList<Label>();
    private List<Label> propOpLabelArray = new ArrayList<Label>();
    private List<Label> propValueArray = new ArrayList<Label>();

    private EntityType entityType;

    private boolean editable = true;
    private boolean hasCustomProperties;
    private int controlId;

    private Button addButton = null;

    private CompositePredicate domainObject = null;

    private String lastBufferedValue;
    private Control lastBufferedSource;

    private boolean ignoreFocusLost = false;

    private static final int CONTROL_HEIGHT = 21;
    private static final int VALUE_TEXT_WIDTH = 300;
    private static final int DATE_PICKER_WIDTH = 150;
    private static final int REMOVE_BUTTON_WIDTH = 15;

    private static final Map<EntityType, Collection<AttributeDescriptor>> attributeMaps = new HashMap<EntityType, Collection<AttributeDescriptor>>();
    private static final Map<EntityType, Map<SpecAttribute, String>> attributeNameMaps = new HashMap<EntityType, Map<SpecAttribute, String>>();
    private static final Map<EntityType, Map<String, SpecAttribute>> nameAttributeMaps = new HashMap<EntityType, Map<String, SpecAttribute>>();
    private static final Map<EntityType, Map<SpecAttribute, AttributeType>> attributeTypeMaps = new HashMap<EntityType, Map<SpecAttribute, AttributeType>>();
    private static final Map<EntityType, Set<String>> attributeLists = new HashMap<EntityType, Set<String>>();
    private static final Constant CONST_ZERO = Constant.build(0);

    @SuppressWarnings("deprecation")
        private static final EntityType[] entityTypes = { EntityType.USER,
                                                          EntityType.HOST, EntityType.RESOURCE, EntityType.PORTAL,
                                                          EntityType.APPLICATION };
    private static final Object[] attributeTypes = { SubjectType.USER,
                                                     SubjectType.HOST, ResourceAttribute.FILE_SYSTEM_SUBTYPE,
                                                     ResourceAttribute.PORTAL_SUBTYPE, SubjectType.APP };

    private static class OperatorInfo {

        private Map<RelationOp, String> operatorNameMap = new HashMap<RelationOp, String>();
        private Map<String, RelationOp> nameOperatorMap = new HashMap<String, RelationOp>();
        private List<String> opList = new ArrayList<String>();

        private void storeOperatorInfo(RelationOp op, String name) {
            operatorNameMap.put(op, name);
            nameOperatorMap.put(name, op);
            opList.add(name);
        }

        public RelationOp getOperatorForName(String name) {
            return (RelationOp) nameOperatorMap.get(name);
        }

        public String getNameForOperator(RelationOp op) {
            return (String) operatorNameMap.get(op);
        }

        public List<String> getOperators() {
            return opList;
        }
    }

    private static final OperatorInfo dateOperators = new OperatorInfo();
    private static final OperatorInfo stringOperators = new OperatorInfo();
    private static final OperatorInfo numberOperators = new OperatorInfo();
    private static final OperatorInfo yesNoStates = new OperatorInfo();
    private static final OperatorInfo enumOperators = new OperatorInfo();

    private final PredicateModifiedListener predicateModifiedListner = new PredicateModifiedListener();

    static {
        for (int i = 0, n = entityTypes.length; i < n; i++) {
            Collection<AttributeDescriptor> attributes = PolicyServerProxy
                                                         .getAttributes(entityTypes[i]);
            attributeMaps.put(entityTypes[i], attributes);
            for (AttributeDescriptor attr : attributes) {
                if (attributeTypes[i] instanceof String) {
                    storeAttributeInfo(ResourceAttribute.forNameAndType(attr
                                                                        .getPqlName(), (String) attributeTypes[i]), attr
                                       .getDisplayName(), entityTypes[i], attr.getType());
                } else {
                    storeAttributeInfo(SubjectAttribute.forNameAndType(attr
                                                                       .getPqlName(), (SubjectType) attributeTypes[i]),
                                       attr.getDisplayName(), entityTypes[i], attr
                                       .getType());
                }
            }
        }
        stringOperators.storeOperatorInfo(RelationOp.EQUALS, "is");
        stringOperators.storeOperatorInfo(RelationOp.NOT_EQUALS, "is not");

        enumOperators.storeOperatorInfo(RelationOp.EQUALS, "is");
        enumOperators.storeOperatorInfo(RelationOp.NOT_EQUALS, "is not");

        dateOperators.storeOperatorInfo(RelationOp.LESS_THAN, "before");
        dateOperators.storeOperatorInfo(RelationOp.GREATER_THAN_EQUALS, "on or after");

        numberOperators.storeOperatorInfo(RelationOp.EQUALS, "=");
        numberOperators.storeOperatorInfo(RelationOp.GREATER_THAN, ">");
        numberOperators.storeOperatorInfo(RelationOp.GREATER_THAN_EQUALS, ">=");
        numberOperators.storeOperatorInfo(RelationOp.LESS_THAN, "<");
        numberOperators.storeOperatorInfo(RelationOp.LESS_THAN_EQUALS, "<=");
        numberOperators.storeOperatorInfo(RelationOp.NOT_EQUALS, "!=");

        // The operand in boolean expressions is hidden, and is set to zero.
        // Therefore, x>0 means "Yes", while x==0 means "No".
        yesNoStates.storeOperatorInfo(RelationOp.GREATER_THAN, "Yes");
        yesNoStates.storeOperatorInfo(RelationOp.EQUALS, "No");
    }

    private static void storeAttributeInfo(SpecAttribute attribute,
                                           String name, EntityType entityType, AttributeType type) {
        Map<SpecAttribute, String> attributeNameMap = attributeNameMaps
                                                      .get(entityType);
        if (attributeNameMap == null) {
            attributeNameMap = new HashMap<SpecAttribute, String>();
            attributeNameMaps.put(entityType, attributeNameMap);
        }
        Map<String, SpecAttribute> nameAttributeMap = nameAttributeMaps
                                                      .get(entityType);
        if (nameAttributeMap == null) {
            nameAttributeMap = new HashMap<String, SpecAttribute>();
            nameAttributeMaps.put(entityType, nameAttributeMap);
        }
        Map<SpecAttribute, AttributeType> attributeTypeMap = attributeTypeMaps
                                                             .get(entityType);
        if (attributeTypeMap == null) {
            attributeTypeMap = new HashMap<SpecAttribute, AttributeType>();
            attributeTypeMaps.put(entityType, attributeTypeMap);
        }

        attributeNameMap.put(attribute, name);
        nameAttributeMap.put(name, attribute);
        attributeTypeMap.put(attribute, type);

        Set<String> attrList = attributeLists.get(entityType);
        if (attrList == null) {
            attrList = new TreeSet<String>();
            attributeLists.put(entityType, attrList);
        }
        attrList.add(name);
    }

    @SuppressWarnings("deprecation")
        private static Set<String> getAttributeList(EntityType entityType) {
        Set<String> list = attributeLists.get(entityType);
        if (entityType.equals(EntityType.RESOURCE)  || entityType.equals(EntityType.PORTAL) ) {
            Collection<AttributeDescriptor> attributes = attributeMaps
                                                         .get(EntityType.RESOURCE);
            for (AttributeDescriptor attribute : attributes) {
                String group = attribute.getGroupName();
                if (group != null
                    && attribute.getGroupName().equals("With Content")) {
                    list.remove(attribute.getDisplayName());
                }
            }
        }
        return list;
    }

    @SuppressWarnings("deprecation")
        private static SpecAttribute getAttributeForName(String name,
                                                         EntityType entityType) {
        synchronized (nameAttributeMaps) {
            SpecAttribute r = nameAttributeMaps.get(entityType).get(name);
            if (r == null) {
                String subtype;
                if (entityType == EntityType.RESOURCE) {
                    r = ResourceAttribute.forNameAndType(name, ResourceAttribute.FILE_SYSTEM_SUBTYPE, false);
                } else if (entityType == EntityType.PORTAL) {
                    r = ResourceAttribute.forNameAndType(name, ResourceAttribute.PORTAL_SUBTYPE, false);
                } else if (entityType == EntityType.USER) {
                    r = SubjectAttribute.forNameAndType(name, SubjectType.USER);
                } else if (entityType == EntityType.APPLICATION) {
                    r = SubjectAttribute.forNameAndType(name, SubjectType.APP);
                } else if (entityType == EntityType.HOST) {
                    r = SubjectAttribute.forNameAndType(name, SubjectType.HOST);
                } else {
                    return null;
                }
                nameAttributeMaps.get(entityType).put(name, r);
                attributeNameMaps.get(entityType).put(r, name);
            }
            return r;
        }
    }

    private static String getNameForAttribute(SpecAttribute attribute,
                                              EntityType entityType) {
        String result = attributeNameMaps.get(entityType).get(attribute);
        if (result == null)
            return attribute.getName();
        return result;
    }

    private static AttributeType getTypeForAttribute(SpecAttribute attribute,
                                                     EntityType entityType) {
        return attributeTypeMaps.get(entityType).get(attribute);
    }

    /**
     * Constructor
     * 
     * @param parent
     * @param style
     */
    public PropertyExpressionControl(Composite parent, int style,
                                     CompositePredicate domainObj, IEditorPanel editorPanel,
                                     EntityType entityType, int controlId, boolean editable,
                                     boolean hasCustomProperties) {
        super(parent, style);
        this.domainObject = domainObj;
        this.controlId = controlId;
        this.hasCustomProperties = hasCustomProperties;
        setEditable(editable);

        IComponentManager componentManager = ComponentManagerFactory
                                             .getComponentManager();
        final IEventManager eventManager = componentManager
                                           .getComponent(EventManagerImpl.COMPONENT_INFO);
        eventManager.registerListener(predicateModifiedListner,
                                      ContextualEventType.PREDICATE_MODIFIED_EVENT, domainObject);

        addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                eventManager
                    .unregisterListener(
                        PropertyExpressionControl.this.predicateModifiedListner,
                    ContextualEventType.PREDICATE_MODIFIED_EVENT,
                    PropertyExpressionControl.this.domainObject);
            }
        });

        this.editorPanel = editorPanel;
        this.entityType = entityType;

        initialize();
        relayout();
    }

    /*
     * AW Note: refresh disposes UI widget and recreate them. If you call this
     * from a widget event handler, call refreshLater instead of refresh. See
     * bug 585
     */
    protected void refreshLater() {
        getDisplay().asyncExec(new Runnable() {

            public void run() {
                refresh();
            }
        });
    }

    /**
     * 
     */
    protected void refresh() {
        disposeWidgetArray(propControlArray);
        disposeWidgetArray(opControlArray);
        disposeWidgetArray(valueControlArray);
        disposeWidgetArray(removeButtonArray);
        disposeWidgetArray(propNameLabelArray);
        disposeWidgetArray(propOpLabelArray);
        disposeWidgetArray(propValueArray);

        propControlArray.clear();
        opControlArray.clear();
        valueControlArray.clear();
        removeButtonArray.clear();
        propNameLabelArray.clear();
        propOpLabelArray.clear();
        propValueArray.clear();
        tabListArray.clear();

        initialize();
        relayoutParent();
    }

    private void disposeWidgetArray(List<? extends Widget> list) {
        for (Widget widget : list) {
            widget.dispose();
        }
    }

    private int findIndex(int index) {
        int internal_local = 0;
        int internal_all = 0;
        Collection<AttributeDescriptor> attributes = attributeMaps
                                                     .get(entityType);
        for (int i = 0; i < domainObject.predicateCount(); i++) {
            IPredicate spec = (IPredicate) domainObject.predicateAt(i);
            if (spec instanceof Relation) {
                Relation relation = (Relation) spec;
                final IExpression lhs = relation.getLHS();
                String propertyName = ((SpecAttribute) lhs).getName();
                boolean found = false;
                for (AttributeDescriptor descriptor : attributes) {
                    if (descriptor.getPqlName().equals(propertyName)
                        && descriptor.getGroupName() != null
                        && descriptor
                        .getGroupName()
                        .equals(
                            EditorMessages.COMPONENTEDITOR_WITH_CONTENT)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (internal_local == index) {
                        return internal_all;
                    } else {
                        internal_local++;
                    }
                }
                internal_all++;
            }
        }
        return 0;
    }

    public void initialize() {
        IComponentManager componentManager = ComponentManagerFactory
                                             .getComponentManager();
        IEventManager eventManager = componentManager
                                     .getComponent(EventManagerImpl.COMPONENT_INFO);

        IHasId hasId = editorPanel.getDomainObject();
        Collection<AttributeDescriptor> attributes = null;
        if (hasId instanceof SpecBase) {
            SpecBase base = (SpecBase) hasId;
            String name = base.getName();
            String componentType = PolicyServerHelper
                                   .getTypeFromComponentName(name);
            if (componentType.equals("SERVER")) {
            } else {
                EntityType type = PolicyServerHelper
                                  .getEntityType(componentType);
                attributes = attributeMaps.get(type);
            }
        }

        for (int i = 0; i < domainObject.predicateCount(); i++) {
            IPredicate spec = (IPredicate) domainObject.predicateAt(i);
            if (spec instanceof Relation) {
                Relation relation = (Relation) spec;
                IExpression lhs = relation.getLHS();
                boolean found = false;
                if (lhs instanceof SpecAttribute) {
                    SpecAttribute attribute = (SpecAttribute) lhs;
                    found = false;
                    Collection<AttributeDescriptor> resourceAttributes = attributeMaps.get(EntityType.RESOURCE);
                    for (AttributeDescriptor descriptor : resourceAttributes) {
                        String group = descriptor.getGroupName();
                        if (group != null
                            && group
                            .equals(EditorMessages.COMPONENTEDITOR_WITH_CONTENT)
                            && descriptor.getPqlName().equals(
                                attribute.getName())) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    eventManager.registerListener(predicateModifiedListner,
                                                  ContextualEventType.PREDICATE_MODIFIED_EVENT, spec);
                    addExpressionControls((Relation) spec);
                }
            }
        }
        if (addButton == null && isEditable()) {
            addAddButton();
        }
    }

    /**
     * 
     */
    private void addAddButton() {
        addButton = new Button(this, SWT.FLAT | SWT.CENTER);
        addButton.setText(ApplicationMessages.PROPERTYEXPRESSIONCONTROL_ADD);
        addButton
            .setToolTipText(ApplicationMessages.PROPERTYEXPRESSIONCONTROL_ADD_CONDITION);
        addButton.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // FIXME: create the right type of object and guard it properly
                String name = getAttributeList(getEntityType()).iterator()
                              .next();
                SpecAttribute attribute = getAttributeForName(name,
                                                              getEntityType());
                OperatorInfo opers = getOperatorsForAttribute(attribute);
                Relation prop = new Relation(opers.getOperatorForName(opers
                                                                      .getOperators().get(0).toString()), attribute, Constant
                                             .build(""));
                if (isDateAttribute(attribute)) {
                    prop.setRHS(getDefaultDateExpression());
                } else if (isBooleanAttribute(attribute)) {
                    prop.setRHS(CONST_ZERO);
                } else if (isEnumAttribute(attribute)) {
                    String value = getAttributeList(getEntityType()).iterator()
                                   .next();
                    prop.setRHS(Constant.build(value));
                }
                PredicateHelpers.addPredicate(domainObject, prop);
                addExpressionControls(prop);
                addUndoElement(PropertyExpressionUndoElementOp.ADD,
                               domainObject.predicateCount() - 1, prop, null);
                relayoutParent();
            }
        });

    }

    /**
     * @param spec
     */
    private void addExpressionControls(Relation spec) {
        final IExpression lhs = spec.getLHS();
        final IExpression rhs = spec.getRHS();
        final RelationOp op = spec.getOp();
        if (isEditable()) {
            OperatorInfo operatorInfo = getOperatorsForAttribute(lhs);
            Button removeButton = new Button(this, SWT.FLAT | SWT.CENTER);
            removeButton
                .setText(ApplicationMessages.PROPERTYEXPRESSIONCONTROL_REMOVE);
            removeButton
                .setToolTipText(ApplicationMessages.PROPERTYEXPRESSIONCONTROL_REMOVE_CONDITION);
            removeButton.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
            removeButtonArray.add(removeButton);
            removeButton.addSelectionListener(new SelectionAdapter() {
                @Override
                    public void widgetSelected(SelectionEvent e) {
                    Button b = (Button) e.getSource();
                    int index = removeButtonArray.indexOf(b);
                    removeExpressionControls(index);
                    IPredicate oldValue = PredicateHelpers.removePredicateAt(
                        domainObject, findIndex(index));
                    addUndoElement(PropertyExpressionUndoElementOp.REMOVE,
                                   findIndex(index), null, oldValue);
                    relayoutParent();
                }
            });

            Combo combo;
            if (hasCustomProperties) {
                combo = new Combo(this, SWT.SINGLE | SWT.BORDER);
                // bug 14439, free limit
                // combo.setTextLimit(128);
            } else {
                combo = new Combo(this, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
            }
            propControlArray.add(combo);

            Set<String> attributeList = getAttributeList(getEntityType());
            List<String> sortedAttributeList = new ArrayList<String>(
                attributeList);
            Collections
                .sort(sortedAttributeList, String.CASE_INSENSITIVE_ORDER);
            for (String attribute : sortedAttributeList) {
                combo.add(attribute);
            }

            combo.setText(getNameForAttribute((SpecAttribute) lhs,
                                              getEntityType()));
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                    public void widgetSelected(SelectionEvent e) {
                    Combo source = (Combo) e.getSource();
                    int index = propControlArray.indexOf(source);
                    String newText = source.getText();
                    Relation spec = (Relation) PredicateHelpers.getPredicateAt(
                        domainObject, findIndex(index));
                    SpecAttribute newValue = getAttributeForName(newText,
                                                                 getEntityType());
                    Object oldValue = spec.getLHS();
                    if (!oldValue.equals(newValue)) {
                        addUndoElement(
                            PropertyExpressionUndoElementOp.CHANGE_ATTRIBUTE,
                        findIndex(index), newValue, oldValue);
                        IExpression newRHS;
                        RelationOp newOp;
                        if (isNumberAttribute(newValue)) {
                            newRHS = newValue.build("0");
                            newOp = RelationOp.EQUALS;
                        } else if (isDateAttribute(newValue)) {
                            newRHS = getDefaultDateExpression();
                            newOp = RelationOp.GREATER_THAN_EQUALS;
                        } else if (isBooleanAttribute(newValue)) {
                            newRHS = CONST_ZERO;
                            newOp = RelationOp.EQUALS;
                        } else if (isEnumAttribute(newValue)) {
                            String value = getDefaultEnumValue(newText);
                            newRHS = Constant.build(value);
                            newOp = RelationOp.EQUALS;
                        } else if (newText.equals(OWNER_USER_COMPONENT)) {
                            newRHS = Constant.build("USER"
                                                    + PQLParser.SEPARATOR + ALL_USERS);
                            newOp = RelationOp.EQUALS;
                        } else {
                            if (isOptionalAttribute(lhs, newText)) {
                                AttributeDescriptor descriptor = getAttributeDescriptorForDisplayName(newText);
                                List<AttributeDescriptor> allows = descriptor
                                                                   .getAllowedAttributes();
                                if (allows.size() > 0) {
                                    String name = allows.get(0)
                                                  .getDisplayName();
                                    newRHS = getSpecAttributeForName(name);
                                } else {
                                    newRHS = newValue.build("");
                                }
                            } else {
                                newRHS = newValue.build("");
                            }
                            newOp = RelationOp.EQUALS;
                        }

                        addUndoElement(
                            PropertyExpressionUndoElementOp.CHANGE_VALUE,
                        findIndex(index), newRHS, spec.getRHS(), true);
                        addUndoElement(
                            PropertyExpressionUndoElementOp.CHANGE_OP,
                        findIndex(index), newOp, spec.getOp(), true);
                        spec.setLHS(newValue);
                        spec.setOp(newOp);
                        spec.setRHS(newRHS);
                        refreshLater();
                    }
                }
            });
            combo.addFocusListener(new FocusAdapter() {

                @Override
                    public void focusLost(FocusEvent e) {
                    Combo source = (Combo) e.getSource();
                    int index = propControlArray.indexOf(source);
                    String newText = source.getText();
                    Relation spec = (Relation) PredicateHelpers.getPredicateAt(
                        domainObject, findIndex(index));
                    //
                    SpecAttribute newValue = getAttributeForName(newText,
                                                                 getEntityType());
                    Object oldValue = spec.getLHS();
                    if (!oldValue.equals(newValue)) {
                        addUndoElement(
                            PropertyExpressionUndoElementOp.CHANGE_ATTRIBUTE,
                        findIndex(index), newValue, oldValue);
                        IExpression newRHS;
                        RelationOp newOp;
                        if (isNumberAttribute(newValue)) {
                            newRHS = newValue.build("0");
                            newOp = RelationOp.EQUALS;
                        } else if (isDateAttribute(newValue)) {
                            newRHS = getDefaultDateExpression();
                            newOp = RelationOp.GREATER_THAN_EQUALS;
                        } else if (isBooleanAttribute(newValue)) {
                            newRHS = CONST_ZERO;
                            newOp = RelationOp.EQUALS;
                        } else if (isEnumAttribute(newValue)) {
                            String value = getDefaultEnumValue(newText);
                            newRHS = Constant.build(value);
                            newOp = RelationOp.EQUALS;
                        } else if (newText.equals(OWNER_USER_COMPONENT)) {
                            newRHS = Constant.build("USER"
                                                    + PQLParser.SEPARATOR + ALL_USERS);
                            newOp = RelationOp.EQUALS;
                        } else {
                            if (isOptionalAttribute(lhs, newText)) {
                                AttributeDescriptor descriptor = getAttributeDescriptorForDisplayName(newText);
                                List<AttributeDescriptor> allows = descriptor
                                                                   .getAllowedAttributes();
                                if (allows.size() > 0) {
                                    String name = allows.get(0)
                                                  .getDisplayName();
                                    newRHS = getSpecAttributeForName(name);
                                } else {
                                    newRHS = newValue.build("");
                                }
                            } else {
                                newRHS = newValue.build("");
                            }
                            newOp = RelationOp.EQUALS;
                        }

                        addUndoElement(
                            PropertyExpressionUndoElementOp.CHANGE_VALUE,
                        findIndex(index), newRHS, spec.getRHS(), true);
                        addUndoElement(
                            PropertyExpressionUndoElementOp.CHANGE_OP,
                        findIndex(index), newOp, spec.getOp(), true);
                        spec.setLHS(newValue);
                        spec.setOp(newOp);
                        spec.setRHS(newRHS);
                        refreshLater();
                    }
                }
            });

            combo = new Combo(this, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
            for (int i = 0; i < operatorInfo.getOperators().size(); i++) {
                combo.add((String) operatorInfo.getOperators().get(i));
            }
            if (isOptionalAttribute(lhs) && !isEnumAttribute(lhs)) {
                combo.add("is set");
                combo.add("is not set");
            }

            if (isOptionalAttribute(lhs) && rhs.equals(Constant.NULL)) {
                if (op.equals(RelationOp.NOT_EQUALS)) {
                    combo.setText("is set");
                } else if (op.equals(RelationOp.EQUALS)) {
                    combo.setText("is not set");
                }
            } else {
                combo.setText(operatorInfo.getNameForOperator(op));
            }

            opControlArray.add(combo);

            combo.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    Combo source = (Combo) e.getSource();
                    int index = opControlArray.indexOf(source);
                    String newOpText = source.getText();
                    Relation spec = (Relation) PredicateHelpers.getPredicateAt(
                        domainObject, findIndex(index));
                    IExpression oldLHS = spec.getLHS();
                    RelationOp oldOp = spec.getOp();
                    IExpression oldRHS = spec.getRHS();
                    if (newOpText.equals("is set")) {
                        if (!oldOp.equals(RelationOp.NOT_EQUALS)
                            || !oldRHS.equals(Constant.NULL)) {
                            Control control = (Control) valueControlArray
                                              .get(index);
                            control.setVisible(false);
                            RelationOp newOp = RelationOp.NOT_EQUALS;
                            IExpression newRHS = Constant.NULL;
                            addUndoElement(
                                PropertyExpressionUndoElementOp.CHANGE_VALUE,
                            findIndex(index), newRHS, spec.getRHS(),
                            true);
                            spec.setRHS(newRHS);
                            addUndoElement(
                                PropertyExpressionUndoElementOp.CHANGE_OP,
                            findIndex(index), newOp, oldOp);
                            spec.setOp(newOp);
                        }
                    } else if (newOpText.equals("is not set")) {
                        if (!oldOp.equals(RelationOp.EQUALS)
                            || !oldRHS.equals(Constant.NULL)) {
                            Control control = (Control) valueControlArray
                                              .get(index);
                            control.setVisible(false);
                            RelationOp newOp = RelationOp.EQUALS;
                            IExpression newRHS = Constant.NULL;
                            addUndoElement(
                                PropertyExpressionUndoElementOp.CHANGE_VALUE,
                            findIndex(index), newRHS, spec.getRHS(),
                            true);
                            spec.setRHS(newRHS);
                            addUndoElement(
                                PropertyExpressionUndoElementOp.CHANGE_OP,
                            findIndex(index), newOp, oldOp);
                            spec.setOp(newOp);
                        }
                    } else {
                        Control control = (Control) valueControlArray
                                          .get(index);
                        if (!isBooleanAttribute(oldLHS)) {
                            control.setVisible(true);
                        }
                        RelationOp newOp = getOperatorsForAttribute(
                            spec.getLHS()).getOperatorForName(newOpText);
                        if (!oldOp.equals(newOp)
                            || oldRHS.equals(Constant.NULL)) {
                            addUndoElement(
                                PropertyExpressionUndoElementOp.CHANGE_OP,
                            findIndex(index), newOp, oldOp);
                            spec.setOp(newOp);

                            IExpression newRHS;
                            if (isNumberAttribute(oldLHS)) {
                                newRHS = Constant.build("0");
                            } else if (isDateAttribute(oldLHS)) {
                                newRHS = getDefaultDateExpression();
                            } else if (isBooleanAttribute(oldLHS)) {
                                newRHS = CONST_ZERO;
                            } else if (isEnumAttribute(oldLHS)) {
                                IAttribute attribute = (IAttribute) lhs;
                                AttributeDescriptor descriptor = getAttributeDescriptorForName(attribute
                                                                                               .getName());
                                String value = descriptor.getEnumeratedValues()
                                               .get(0);
                                newRHS = Constant.build(value);
                            } else {
                                if (isOptionalAttribute(lhs)) {
                                    IAttribute attribute = (IAttribute) lhs;
                                    AttributeDescriptor descriptor = getAttributeDescriptorForName(attribute
                                                                                                   .getName());
                                    List<AttributeDescriptor> allows = descriptor
                                                                       .getAllowedAttributes();
                                    if (allows.size() > 0) {
                                        String name = allows.get(0)
                                                      .getDisplayName();
                                        newRHS = getSpecAttributeForName(name);
                                    } else {
                                        newRHS = Constant.build("");
                                    }
                                } else {
                                    newRHS = Constant.build("");
                                }
                            }
                            addUndoElement(
                                PropertyExpressionUndoElementOp.CHANGE_VALUE,
                            findIndex(index), newRHS, spec.getRHS(),
                            true);
                            spec.setRHS(newRHS);

                            if (control instanceof Text) {
                                ((Text) control).setText("");
                            }
                            if (control instanceof Combo) {
                                ((Combo) control).select(0);
                            }
                            if (control instanceof CalendarPicker) {
                                Calendar d = new GregorianCalendar();
                                d.set(Calendar.HOUR_OF_DAY, 0);
                                d.set(Calendar.MINUTE, 0);
                                d.set(Calendar.SECOND, 0);
                                ((CalendarPicker) control).setCalendar(d);
                            }
                        }
                    }
                }
            });

            if (isDateAttribute(lhs)) {
                addDateEditor(spec.getRHS());
            } else if (lhs == ResourceAttribute.OWNER_GROUP) {
                addComponentNameEditor("USER", stringFromExpression(spec
                                                                    .getRHS()));
            } else if (isEnumAttribute(lhs)) {
                IAttribute attribute = (IAttribute) lhs;
                AttributeDescriptor descriptor = getAttributeDescriptorForName(attribute
                                                                               .getName());
                addComboEnumEditor(descriptor, rhs);
            } else {
                Control t = null;
                if (isOptionalAttribute(lhs)) {
                    IAttribute attribute = (IAttribute) lhs;
                    AttributeDescriptor descriptor = getAttributeDescriptorForName(attribute
                                                                                   .getName());
                    List<AttributeDescriptor> allows = descriptor
                                                       .getAllowedAttributes();
                    if (allows.size() == 0) {
                        t = addStringEditor(
                            stringFromExpression(spec.getRHS()),
                        !isBooleanAttribute(lhs));
                    } else {
                        t = addComboEditor(descriptor, spec.getRHS(),
                                           !isBooleanAttribute(lhs));
                    }
                    if (rhs.equals(Constant.NULL)) {
                        t.setVisible(false);
                    }
                } else {
                    t = addStringEditor(stringFromExpression(spec.getRHS()),
                                        !isBooleanAttribute(lhs));
                }
            }
        } else { // if not editable
            OperatorInfo operatorInfo = getOperatorsForAttribute(lhs);
            Label propName = new Label(this, SWT.NONE);
            propName.setEnabled(false);
            if (lhs instanceof SpecAttribute) {
                propName.setText(getNameForAttribute((SpecAttribute) lhs,
                                                     getEntityType()));
            } else {
                // we really shouldn't get here, but just in case a new
                // type of property comes along, display something:
                propName.setText(lhs.evaluate(null).getValue().toString());
            }
            propName.setBackground(ColorBundle.LIGHT_GRAY);
            propNameLabelArray.add(propName);

            Label propOp = new Label(this, SWT.NONE);
            propOp.setEnabled(false);
            if (isOptionalAttribute(lhs) && rhs.equals(Constant.NULL)) {
                if (op.equals(RelationOp.NOT_EQUALS)) {
                    propOp.setText("is set");
                } else if (op.equals(RelationOp.EQUALS)) {
                    propOp.setText("is not set");
                }
            } else {
                propOp.setText(operatorInfo.getNameForOperator(spec.getOp()));
            }
            propOp.setBackground(ColorBundle.LIGHT_GRAY);
            propOpLabelArray.add(propOp);

            Label propValue = new Label(this, SWT.NONE);
            propValue.setEnabled(false);
            if (isDateAttribute(lhs)) {
                Long dateValue = (Long) ((Constant) rhs).getValue().getValue();
                if (dateValue != null) {
                    Date d = new Date(dateValue.longValue());
                    propValue.setText(SimpleDateFormat.getDateInstance()
                                      .format(d));
                }
            } else if (lhs == ResourceAttribute.OWNER_GROUP) {
                String text = stringFromExpression(rhs);
                if (text.startsWith("USER" + PQLParser.SEPARATOR)) {
                    text = text.substring(5);
                }
                propValue.setText(text);
            } else if (!isBooleanAttribute(lhs)) {
                if (rhs.equals(Constant.NULL)) {
                    propValue.setVisible(false);
                } else if (rhs instanceof SpecAttribute) {
                    SpecAttribute attribute = (SpecAttribute) rhs;
                    AttributeDescriptor descriptor = getAttributeDescriptorForName(attribute
                                                                                   .getName());
                    List<AttributeDescriptor> result = descriptor
                                                       .getAllowedAttributes();
                    if (result.size() > 0) {
                        IAttribute attr = (IAttribute) rhs;
                        for (AttributeDescriptor desc : result) {
                            if (desc.getPqlName().equalsIgnoreCase(
                                    attr.getName())) {
                                propValue.setText(desc.getDisplayName());
                                break;
                            }
                        }
                    } else {
                        propValue.setText(descriptor.getDisplayName());
                    }
                } else {
                    propValue.setText(stringFromExpression(rhs));
                }
            }
            propValue.setBackground(ColorBundle.LIGHT_GRAY);
            propValueArray.add(propValue);
        }
    }

    private String getDefaultEnumValue(String text) {
        AttributeDescriptor descriptor = getAttributeDescriptorForDisplayName(text);
        String value = descriptor.getEnumeratedValues().get(0);

        return value;
    }

    private String stringFromExpression(IExpression exp) {
        final String[] res = new String[] { "" };
        if (exp.equals(Constant.NULL)) {
            return "";
        }
        if (exp != null) {
            exp.acceptVisitor(new IExpressionVisitor() {

                public void visit(IAttribute attribute) {
                    // we should not get here
                }

                public void visit(Constant constant) {
                    res[0] = constant.getRepresentation();
                }

                public void visit(IExpressionReference ref) {
                    if (ref.isReferenceByName()) {
                        res[0] = ref.getReferencedName();
                    } else {
                        res[0] = ref.getPrintableReference();
                    }
                }

                public void visit(IExpression expression) {
                    // we should not get here
                }

                public void visit(IFunctionApplication arg0) {
                    // TODO I think I am ok to do nothing
                    
                }
            }, IExpressionVisitor.PREORDER);
        }

        return res[0].replaceAll("/+$", "");
    }

    private Text addStringEditor(String text, boolean visible) {
        return new StringText(this, text, visible);
    }

    private interface IHasValueChecker {
        void setNewText(String newText);
    }

    private class StringText extends Text implements IHasValueChecker {
        public StringText(Composite parent, String text, boolean visible) {
            super(parent, SWT.SINGLE | SWT.BORDER);

            // bug 14439, free limit
            //setTextLimit(128);
            setText(text);
            valueControlArray.add(this);
            if (visible) {
                addFocusListener(new FocusAdapter() {
                    @Override
                        public void focusLost(FocusEvent e) {
                        if (ignoreFocusLost) {
                            ignoreFocusLost = false;
                            return;
                        }
                        String newText = getText();
                        setNewText(newText);
                    }
                });

                addModifyListener(new ModifyListener() {
                    public void modifyText(ModifyEvent e) {
                        lastBufferedValue = getText();
                        lastBufferedSource = (StringText) e.getSource();
                    }
                });
            } else {
                setVisible(false);
            }
        }

        public void setNewText(String newText) {
            int index = valueControlArray.indexOf(this);
            if (index == -1) {
                // the control is already removed. no need to set new text
                return;
            }
            Relation spec = (Relation) PredicateHelpers.getPredicateAt(
                domainObject, findIndex(index));
            Constant newValue = Constant.build(newText);
            IExpression oldValue = spec.getRHS();
            if (oldValue != null && newValue != null) {
                if (oldValue instanceof Constant) {
                    Constant oldValueConstant = (Constant) oldValue;
                    if (oldValueConstant.getValue() != null
                        && oldValueConstant.getValue().getValue() != null
                        && newValue.getValue() != null
                        && newValue.getValue().getValue() != null
                        && !oldValueConstant.getValue().getValue()
                        .toString().equals(newValue.getValue().getValue().toString())) {
                        if(spec.getLHS()==ResourceAttribute.SIZE){
                            try {
                                long val = Long.parseLong((String)newValue.getValue().getValue());
                            } catch (NumberFormatException e) {
                                MessageDialog.openError(
                            			Display.getCurrent().getActiveShell(),
                                        ApplicationMessages.POLICYSERVERPROXY_ERROR,
                                        "Format error for size. Only number is allowed");
                                newText="0";
                                newValue = Constant.build(newText);
                                spec.setRHS(newValue);
                                lastBufferedValue = getText();
                                lastBufferedSource = this;
                                setText(newText);
                            }
                        }
                        addUndoElement(PropertyExpressionUndoElementOp.CHANGE_VALUE,findIndex(index), newValue, oldValue);
                        spec.setRHS(newValue);
                    }
                } else if (oldValue instanceof LocationReference) {
                    LocationReference oldValueReference = (LocationReference) oldValue;
                    if (oldValueReference.getRefLocationName() != null
                        && newValue.getValue() != null
                        && newValue.getValue().getValue() != null
                        && !oldValueReference.getRefLocationName().equals(
                            newValue.getValue().getValue().toString())) {
                        Constant oldValueConstant = Constant
                                                    .build(oldValueReference.getRefLocationName());
                        addUndoElement(
                            PropertyExpressionUndoElementOp.CHANGE_VALUE,
                        findIndex(index), newValue, oldValueConstant);
                        spec.setRHS(newValue);
                    }
                }
            }

            lastBufferedValue = getText();
            lastBufferedSource = this;
        }

        @Override
            protected void checkSubclass() {
            // to nothing
        }
    }

    public void flushBuffer() {
        if (lastBufferedSource != null && lastBufferedValue != null) {
            if (lastBufferedSource instanceof IHasValueChecker) {
                ((IHasValueChecker) lastBufferedSource)
                    .setNewText(lastBufferedValue);
            }
        }
        lastBufferedValue = null;
        lastBufferedSource = null;
    }

    public void ignoreFocusLostOnce() {
        ignoreFocusLost = true;
    }

    private Combo addComboEnumEditor(AttributeDescriptor descriptor,
                                     IExpression rhs) {
        Combo t = new Combo(this, SWT.READ_ONLY | SWT.BORDER);
        List<String> attributes = descriptor.getEnumeratedValues();
        t.setData(attributes);
        String[] result = new String[attributes.size()];
        for (int i = 0, n = attributes.size(); i < n; i++) {
            result[i] = attributes.get(i);
        }
        t.setItems(result);
        if (rhs instanceof Constant) {
            for (int i = 0, n = result.length; i < n; i++) {
                if (((Constant) rhs).getValue().getValue().equals(
                        attributes.get(i))) {
                    t.select(i);
                    break;
                }
            }
        }
        valueControlArray.add(t);
        t.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                Combo source = (Combo) e.getSource();
                int index = valueControlArray.indexOf(source);
                String newText = source.getText();
                Constant newValue = Constant.build(newText);
                Relation spec = (Relation) PredicateHelpers.getPredicateAt(
                    domainObject, findIndex(index));
                Constant oldValue = (Constant) spec.getRHS();
                // add if here
                if (oldValue.toString() != newValue.toString()) {
                    addUndoElement(
                        PropertyExpressionUndoElementOp.CHANGE_VALUE,
                    findIndex(index), newValue, oldValue);
                    spec.setRHS(newValue);
                }
            }
        });

        return t;
    }

    private Combo addComboEditor(AttributeDescriptor descriptor,
                                 IExpression rhs, boolean visible) {
        Combo t = new Combo(this, SWT.READ_ONLY | SWT.BORDER);
        List<AttributeDescriptor> attributes = descriptor
                                               .getAllowedAttributes();
        t.setData(attributes);
        String[] result = new String[attributes.size()];
        for (int i = 0, n = attributes.size(); i < n; i++) {
            result[i] = attributes.get(i).getDisplayName();
        }
        t.setItems(result);
        if (rhs instanceof Constant) {
            t.select(0);
        } else {
            IAttribute attribute = (IAttribute) rhs;
            for (int i = 0, n = result.length; i < n; i++) {
                if (attributes.get(i).getPqlName().equalsIgnoreCase(
                        attribute.getName())) {
                    t.select(i);
                    break;
                }
            }
        }
        valueControlArray.add(t);
        if (visible) {
            t.addFocusListener(new FocusAdapter() {
                @Override
                    public void focusLost(FocusEvent e) {
                    Combo source = (Combo) e.getSource();
                    int index = valueControlArray.indexOf(source);
                    String newText = source.getText();
                    SpecAttribute newValue = getSpecAttributeForName(newText);
                    Relation spec = (Relation) PredicateHelpers.getPredicateAt(
                        domainObject, findIndex(index));
                    IAttribute oldValue = (IAttribute) spec.getRHS();
                    // add if here
                    if (oldValue.getName() != newValue.getName()) {
                        addUndoElement(
                            PropertyExpressionUndoElementOp.CHANGE_VALUE,
                        findIndex(index), newValue, oldValue);
                        spec.setRHS(newValue);
                    }
                }
            });
        } else {
            t.setVisible(false);
        }

        return t;
    }

    private void addComponentNameEditor(final String type, String text) {
        new ComponentNameEditorText(this, type, text);
    }

    private class ComponentNameEditorText extends Text implements
                                                           IHasValueChecker {
        private final String type;
        private final PropertyExpressionControl parent;

        public ComponentNameEditorText(PropertyExpressionControl parent,
                                       final String type, String text) {
            super(parent, SWT.SINGLE | SWT.BORDER);
            this.type = type;
            this.parent = parent;

            if (text.startsWith(type.toString() + PQLParser.SEPARATOR)) {
                text = text.substring(5);
            }
            setText(text);
            parent.valueControlArray.add(this);
            addFocusListener(new FocusAdapter() {
                @Override
                    public void focusLost(FocusEvent e) {
                    if (ignoreFocusLost) {
                        ignoreFocusLost = false;
                        return;
                    }
                    setNewText(getText());
                }
            });

            addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    lastBufferedValue = getText();
                    lastBufferedSource = (ComponentNameEditorText) e
                                         .getSource();
                }
            });
        }

        public void setNewText(String newText) {
            int index = parent.valueControlArray.indexOf(this);
            if (index == -1) {
                // the control is already removed. no need to set new text
                return;
            }

            if (newText.length() == 0) {
                newText = parent.ALL_USERS;
                setText(newText);
            }
            Relation spec = (Relation) PredicateHelpers.getPredicateAt(
                parent.domainObject, findIndex(index));
            Constant newValue = Constant.build(newText);
            Constant newUserValue = Constant.build(type.toString()
                                                   + PQLParser.SEPARATOR + newText);
            IExpression oldValue = spec.getRHS();
            if (!((oldValue instanceof Constant) && ((Constant) oldValue)
                  .getValue().getValue().toString().equals(
                      newValue.getValue().getValue().toString()))) {
                String name = newValue.getValue().getValue().toString();
                if (isNewComponentName(type, name)) {
                    if (!displayCreateComponentDialog(type, name)) {
                        newText = parent.ALL_USERS;
                        setText(newText);
                        newUserValue = Constant.build(type.toString()
                                                      + PQLParser.SEPARATOR + newText);
                    }
                } else {
                    // the user may type a name that is different case,
                    // since we do case-insensitive name in other places.
                    // In order not to break the user experience, we just
                    // change the case to match the server one
                    if (EntityInfoProvider.isValidComponentName(name)) {
                        String existingName = EntityInfoProvider
                                              .getExistingComponentName(name, type);
                        if (existingName != null) {
                            // remove the type and the PQLSeperator
                            existingName = existingName.substring(type
                                                                  .toString().length() + 1);

                            // reset new name
                            setText(existingName);
                            newValue = Constant.build(existingName);
                            newUserValue = Constant.build(type.toString()
                                                          + PQLParser.SEPARATOR + existingName);
                        }
                    }
                }

                addUndoElement(PropertyExpressionUndoElementOp.CHANGE_VALUE,
                               findIndex(index), newValue, oldValue);
                spec.setRHS(newUserValue);

                lastBufferedValue = getText();
                lastBufferedSource = this;
            }
        }

        @Override
            protected void checkSubclass() {
            // to nothing
        }
    }

    private boolean isNewComponentName(String type, String name) {
        return EntityInfoProvider.isValidComponentName(name)
            && (EntityInfoProvider.getExistingComponentName(name, type) == null)
            && PolicyServerProxy.getAllowedEntityTypes().contains(
                getEntityType());
    }

    /**
     * 
     * @param name
     * @param entityType
     * @return true if a new entity was created, false otherwise
     */
    private boolean displayCreateComponentDialog(String type, String name) {
        StringBuffer msg = new StringBuffer();
        msg.append("There is currently no component with the name '");
        msg.append(name);
        msg.append("'. Would you like to create a new component?");
        if (MessageDialog.openConfirm(getDisplay().getActiveShell(),
                                      "Create a New Component?", msg.toString())) {
            PolicyServerProxy.createBlankComponent(name, type);
            GlobalState.getInstance().getComponentListPanel(type)
                .populateList();
            return true;
        } else {
            return false;
        }
    }

    private void addDateEditor(IExpression exp) {
        CalendarPicker dp = new CalendarPicker(this, SWT.BORDER);
        String propValue = ((Constant) exp).getValue().getValue().toString();
        Calendar d = new GregorianCalendar();
        if ("".equals(propValue) || "0".equals(propValue)) {
            d.set(Calendar.HOUR, 0);
            d.set(Calendar.MINUTE, 0);
            d.set(Calendar.SECOND, 0);
        } else {
            d.setTime(new Date(Long.parseLong(propValue)));
        }
        dp.setCalendar(d);
        valueControlArray.add(dp);
        dp.addSelectionListener(new SelectionAdapter() {

            @Override
                public void widgetSelected(SelectionEvent e) {
                CalendarPicker source = (CalendarPicker) e.getSource();
                int index = valueControlArray.indexOf(source);
                Date controlDate = source.getCalendar().getTime();
                if (controlDate == null) {
                    return;
                }
                Date date = new Date(controlDate.getTime()); // copy to
                // prevent side effects
                Relation spec = (Relation) PredicateHelpers.getPredicateAt(
                    domainObject, findIndex(index));
                Constant newValue = Constant.build(date);
                Constant oldValue = (Constant) spec.getRHS();
                if (!oldValue.getValue().getValue().toString().equals(
                        newValue.getValue().getValue().toString())) {
                    addUndoElement(
                        PropertyExpressionUndoElementOp.CHANGE_VALUE,
                    findIndex(index), newValue, oldValue);
                    spec.setRHS(newValue);
                }
            }
        });
    }

    private static boolean isDateAttribute(IExpression exp) {
        if (exp instanceof SpecAttribute) {
            AttributeType type = getTypeForAttribute((SpecAttribute) exp,
                                                     EntityType.forSpecType(PredicateHelpers
                                                                            .getExpressionType(exp)));
            return type == AttributeType.DATE;
        }
        return false;
    }

    private static boolean isNumberAttribute(IExpression exp) {
        if (exp instanceof SpecAttribute) {
            AttributeType type = getTypeForAttribute((SpecAttribute) exp,
                                                     EntityType.forSpecType(PredicateHelpers
                                                                            .getExpressionType(exp)));
            return type == AttributeType.LONG;
        }
        return false;
    }

    private static boolean isBooleanAttribute(IExpression exp) {
        if (exp instanceof SpecAttribute) {
            AttributeType type = getTypeForAttribute((SpecAttribute) exp,
                                                     EntityType.forSpecType(PredicateHelpers
                                                                            .getExpressionType(exp)));
            return type == AttributeType.BOOLEAN;
        }
        return false;
    }

    private static boolean isEnumAttribute(IExpression exp) {
        if (exp instanceof SpecAttribute) {
            AttributeType type = getTypeForAttribute((SpecAttribute) exp,
                                                     EntityType.forSpecType(PredicateHelpers
                                                                            .getExpressionType(exp)));
            return type == AttributeType.ENUM;
        }
        return false;
    }

    private static boolean isOptionalAttribute(IExpression exp) {
        IAttribute attribute = (IAttribute) exp;
        EntityType type = EntityType.forSpecType(PredicateHelpers
                                                 .getExpressionType(exp));
        String name = attribute.getName();
        Collection<AttributeDescriptor> descriptors = attributeMaps.get(type);
        for (AttributeDescriptor descriptor : descriptors) {
            if (descriptor.getPqlName().equalsIgnoreCase(name)) {
                if (!descriptor.isRequired()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isOptionalAttribute(IExpression exp, String name) {
        EntityType type = EntityType.forSpecType(PredicateHelpers
                                                 .getExpressionType(exp));
        Collection<AttributeDescriptor> descriptors = attributeMaps.get(type);
        for (AttributeDescriptor descriptor : descriptors) {
            if (descriptor.getDisplayName().equalsIgnoreCase(name)) {
                if (!descriptor.isRequired()) {
                    return true;
                }
            }
        }
        return false;
    }

    private OperatorInfo getOperatorsForAttribute(IExpression exp) {
        OperatorInfo operatorInfo;
        if (isDateAttribute(exp)) {
            operatorInfo = dateOperators;
        } else if (isNumberAttribute(exp)) {
            operatorInfo = numberOperators;
        } else if (isBooleanAttribute(exp)) {
            operatorInfo = yesNoStates;
        } else if (isEnumAttribute(exp)) {
            operatorInfo = enumOperators;
        } else {
            operatorInfo = stringOperators;
        }
        return operatorInfo;
    }

    /**
     * remove controls corresponding to index
     * 
     * @param index
     */
    protected void removeExpressionControls(int index) {
        Control c = (Control) removeButtonArray.remove(index);
        c.dispose();
        c = (Control) propControlArray.remove(index);
        c.dispose();
        c = (Control) opControlArray.remove(index);
        c.dispose();
        c = (Control) valueControlArray.remove(index);
        c.dispose();
    }

    public void relayout() {
        if (isEditable()) {
            relayoutEditable();
        } else {
            relayoutNonEditable();
        }

        layout();
    }

    private void relayoutEditable() {
        int spacing = 5;
        int currentX = spacing;
        int currentY = spacing;

        tabListArray.clear();

        for (int i = 0; i < propControlArray.size(); i++) {
            Control t = (Control) removeButtonArray.get(i);
            t.setBounds(currentX, currentY + 2, REMOVE_BUTTON_WIDTH,
                        REMOVE_BUTTON_WIDTH);
            tabListArray.add(t);
            currentX += REMOVE_BUTTON_WIDTH;
            currentX += spacing;
            t = (Control) propControlArray.get(i);
            Point controlSize = t.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            controlSize.x = PlatformUtils.findProperControlWidth(controlSize.x,
                                                                 100, 50);
            t.setBounds(currentX, currentY, controlSize.x, CONTROL_HEIGHT);
            tabListArray.add(t);
            currentX += controlSize.x + spacing;
            t = (Control) opControlArray.get(i);
            controlSize = t.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            controlSize.x = PlatformUtils.findProperControlWidth(controlSize.x,
                                                                 100, 50);
            t.setBounds(currentX, currentY, controlSize.x, CONTROL_HEIGHT);
            tabListArray.add(t);
            currentX += controlSize.x;
            currentX += spacing;
            t = (Control) valueControlArray.get(i);
            if (t instanceof CalendarPicker) {
                t.setBounds(currentX, currentY, DATE_PICKER_WIDTH,
                            CONTROL_HEIGHT);
            } else {
                t.setBounds(currentX, currentY, VALUE_TEXT_WIDTH,
                            CONTROL_HEIGHT);
            }
            tabListArray.add(t);
            currentX = spacing;
            currentY += CONTROL_HEIGHT + spacing;
        }

        Point addButtonSize = addButton.computeSize(REMOVE_BUTTON_WIDTH,
                                                    REMOVE_BUTTON_WIDTH);
        addButton.setBounds(currentX, currentY, addButtonSize.x,
                            REMOVE_BUTTON_WIDTH);
        tabListArray.add(addButton);
        setTabList((Control[]) tabListArray.toArray(new Control[tabListArray
                                                                .size()]));
    }

    private void relayoutNonEditable() {
        int spacing = 5;
        int currentX;
        int currentY = spacing;

        int maxPropNameLabelLength = 0;
        for (Label nextLabel : propNameLabelArray) {
            Point nextLabelSize = nextLabel.computeSize(SWT.DEFAULT,
                                                        SWT.DEFAULT);
            maxPropNameLabelLength = nextLabelSize.x > maxPropNameLabelLength ? nextLabelSize.x
                                     : maxPropNameLabelLength;
        }

        int maxOperatorLength = 0;
        for (Control nextOperatorControl : propOpLabelArray) {
            int nextOperatorSize = nextOperatorControl.computeSize(SWT.DEFAULT,
                                                                   SWT.DEFAULT).x;
            maxOperatorLength = nextOperatorSize > maxOperatorLength ? nextOperatorSize
                                : maxOperatorLength;
        }

        for (int i = 0; i < propNameLabelArray.size(); i++) {
            currentX = spacing + REMOVE_BUTTON_WIDTH + spacing
                       + REMOVE_BUTTON_WIDTH + spacing;

            Control t = (Control) propNameLabelArray.get(i);
            t.setBounds(currentX, currentY, maxPropNameLabelLength + 2,
                        CONTROL_HEIGHT);
            currentX += maxPropNameLabelLength + 2 + spacing;
            t = (Control) propOpLabelArray.get(i);
            t.setBounds(currentX, currentY, maxOperatorLength, CONTROL_HEIGHT);
            currentX += maxOperatorLength + spacing;
            t = (Control) propValueArray.get(i);
            t.setBounds(currentX, currentY, VALUE_TEXT_WIDTH, CONTROL_HEIGHT);

            currentY += CONTROL_HEIGHT + spacing;
        }
    }

    /**
     * 
     */
    private void relayoutParent() {
        setRedraw(false);
        // relayout();
        editorPanel.relayout();
        setRedraw(true);
    }

    /**
     * @param undoOp
     * @param index
     * @param newValue
     * @param oldValue
     */
    private void addUndoElement(PropertyExpressionUndoElementOp undoOp,
                                int index, Object newValue, Object oldValue) {
        addUndoElement(undoOp, index, newValue, oldValue, false);
    }

    private void addUndoElement(PropertyExpressionUndoElementOp undoOp,
                                int index, Object newValue, Object oldValue, boolean continuation) {
        PropertyExpressionUndoElement undoElement = new PropertyExpressionUndoElement();
        undoElement.setIndex(index);
        undoElement.setOp(undoOp);
        undoElement.setOldValue(oldValue);
        undoElement.setNewValue(newValue);
        undoElement.setControlId(controlId);
        undoElement.setContinuation(continuation);
        GlobalState.getInstance().addUndoElement(undoElement);
    }

    /**
     * @return Returns the editable.
     */
    private boolean isEditable() {
        return editable;
    }

    /**
     * @param editable
     *            The editable to set.
     */
    private void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * @return Returns the entityType.
     */
    public EntityType getEntityType() {
        return entityType;
    }

    public AttributeDescriptor getAttributeDescriptorForDisplayName(String name) {
        Collection<AttributeDescriptor> descriptors = attributeMaps
                                                      .get(getEntityType());
        for (AttributeDescriptor descriptor : descriptors) {
            if (descriptor.getDisplayName().equalsIgnoreCase(name)) {
                return descriptor;
            }
        }
        return null;
    }

    public SpecAttribute getSpecAttributeForName(String name) {
        for (EntityType type : entityTypes) {
            Map<String, SpecAttribute> map = nameAttributeMaps.get(type);
            SpecAttribute attribute = map.get(name);
            if (attribute != null) {
                return attribute;
            }
        }
        return null;
    }

    public AttributeDescriptor getAttributeDescriptorForName(String name) {
        Collection<AttributeDescriptor> descriptors = attributeMaps
                                                      .get(getEntityType());
        for (AttributeDescriptor descriptor : descriptors) {
            if (descriptor.getPqlName().equalsIgnoreCase(name)) {
                return descriptor;
            }
        }
        return null;
    }

    /**
     * @return
     */
    protected IExpression getDefaultDateExpression() {
        Calendar d = new GregorianCalendar();
        d.set(Calendar.HOUR_OF_DAY, 0);
        d.set(Calendar.MINUTE, 0);
        d.set(Calendar.SECOND, 0);
        d.set(Calendar.MILLISECOND, 0);
        return Constant.build(d.getTime());
    }

    private class PredicateModifiedListener implements IContextualEventListener {

        /**
         * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
         */
        public void onEvent(IContextualEvent event) {
            if (PropertyExpressionControl.this.domainObject.predicateCount() != PropertyExpressionControl.this.propControlArray
                .size()) {
                // property was added or removed (probably by undo)
                refresh();
                relayout();
            } else {
                for (int i = 0; i < PropertyExpressionControl.this.domainObject
                             .predicateCount(); i++) {
                    IPredicate pred = PropertyExpressionControl.this.domainObject
                                      .predicateAt(i);
                    if (pred instanceof Relation) {
                        Relation spec = (Relation) pred;
                        Combo propControl = (Combo) PropertyExpressionControl.this.propControlArray
                                            .get(i);
                        SubjectAttribute lhs = (SubjectAttribute) spec.getLHS();
                        if (!lhs.getName().equals(propControl.getText())) {
                            propControl.setText((lhs).getName());
                        }
                        Combo opControl = (Combo) PropertyExpressionControl.this.opControlArray
                                          .get(i);
                        if (!spec.getOp().getName().equals(opControl.getText())) {
                            propControl.setText(spec.getOp().getName());
                        }
                        Text valueControl = (Text) PropertyExpressionControl.this.valueControlArray
                                            .get(i);
                        String newText = stringFromExpression(spec.getRHS());
                        if (!valueControl.getText().equals(newText)) {
                            valueControl.setText(newText);
                        }
                    }
                }
            }
        }
    }
}
