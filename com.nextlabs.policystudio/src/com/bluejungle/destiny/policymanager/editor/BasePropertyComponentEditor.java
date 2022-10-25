package com.bluejungle.destiny.policymanager.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;


import com.bluejungle.destiny.policymanager.editor.BaseComponentEditor;
import com.bluejungle.destiny.policymanager.editor.EditorPanel;
import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.model.IClientComponent;
import com.bluejungle.destiny.policymanager.model.IClientEditorPanel;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.PropertyExpressionUndoElement;
import com.bluejungle.destiny.policymanager.ui.PropertyExpressionUndoElementOp;
import com.bluejungle.destiny.policymanager.util.PlatformUtils;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lifecycle.AttributeDescriptor;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
/** 
 * This abstract class is for any component that has properties (beyond the usual resource name)
 */
public abstract class BasePropertyComponentEditor extends BaseComponentEditor {
    private static int BUTTON_WIDTH = 15;
    private static int BUTTON_HEIGHT = 15;
    private static int CONTROL_HEIGHT = 21;
    private static int TEXT_WIDTH = 300;

    public BasePropertyComponentEditor(IClientEditorPanel panel, IClientComponent component) {
        super(panel, component);
    }

    private class PredicateModifiedListener implements IContextualEventListener {
        /**
         * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
         */
        public void onEvent(IContextualEvent event) {
            refreshLater();
        }
    }
    
    private final PredicateModifiedListener predicateModifiedListner = new PredicateModifiedListener();
    
    private Composite contentAnaysisContainer;
    
    private List<Button> buttonList = new ArrayList<Button>();
    private List<Combo> propertyNameList = new ArrayList<Combo>();
    private List<Combo> operatorList = new ArrayList<Combo>();
    private List<Composite> detailList = new ArrayList<Composite>();
    private Button buttonAdd;
    
    private Map<String, String> valueMap = new HashMap<String, String>();
    
    private List<AttributeDescriptor> propertiesList;
    private CompositePredicate relations;

    /*
     * (non-Javadoc)
     * 
     * @seecom.bluejungle.destiny.policymanager.editor.ComponentEditor#
     * getPropertyOperatorList()
     */
    @Override
	protected List<String> getPropertyOperatorList() {
        return null;
    }

    @Override
	protected void initializePropertyExpressions() {
        super.initializePropertyExpressions();

        preparePropertiesList();
        prepareData();

        Composite propertiesLabel = panel.initializeSectionHeading(
            propertiesComposite, EditorMessages.COMPONENTEDITOR_WITH_CONTENT);
        FormData data = new FormData();
        data.left = new FormAttachment(0,EditorPanel.SIDE_SPACING);
        data.top = new FormAttachment(propertyExpressionControl);
        data.right = new FormAttachment(100,-EditorPanel.SIDE_SPACING);
        propertiesLabel.setLayoutData(data);

        propertiesNameLabel = new Label(propertiesComposite, SWT.NONE);
        propertiesNameLabel.setText(EditorMessages.COMPONENTEDITOR_PROPERTY_NAME);
        propertiesNameLabel.setBackground(panel.getBackground());
        data = new FormData();
        data.left = new FormAttachment(0, 65);
        data.top = new FormAttachment(propertiesLabel, EditorPanel.SPACING);
        propertiesNameLabel.setLayoutData(data);

        contentAnaysisContainer = new Composite(propertiesLabel.getParent(),
                                                SWT.NONE);
        contentAnaysisContainer.setBackground(panel.getBackground());
        data = new FormData();
        data.left = new FormAttachment(0,EditorPanel.SIDE_SPACING);
        data.top = new FormAttachment(propertiesNameLabel, EditorPanel.SPACING);
        data.right = new FormAttachment(100,-EditorPanel.SIDE_SPACING);
        contentAnaysisContainer.setLayoutData(data);

        GridLayout layout = new GridLayout(4, false);
        contentAnaysisContainer.setLayout(layout);

        initializeLayout();
    }

    private void prepareData() {
        IPredicate predicate = ((ICompositePredicate) ((IDSpec) component)
				.getPredicate()).predicateAt(1);
        if (predicate instanceof PredicateConstants) {
            CompositePredicate result = new CompositePredicate(BooleanOp.AND,
                                                               new ArrayList<IPredicate>());
            result.addPredicate(PredicateConstants.TRUE);
            result.addPredicate(PredicateConstants.TRUE);
            relations = result;
        } else {
            relations = (CompositePredicate) ((ICompositePredicate) ((IDSpec) component)
                                              .getPredicate()).predicateAt(1);
        }
    }

    private void initializeLayout() {
        IComponentManager componentManager = ComponentManagerFactory
                                             .getComponentManager();
        IEventManager eventManager = componentManager
                                     .getComponent(EventManagerImpl.COMPONENT_INFO);
        eventManager.registerListener(predicateModifiedListner,
                                      ContextualEventType.PREDICATE_MODIFIED_EVENT, relations);

        for (int i = 0; i < relations.predicateCount(); i++) {
            IPredicate spec = (IPredicate) relations.predicateAt(i);
            if (spec instanceof Relation) {
                Relation relation = (Relation) spec;
                final IExpression lhs = relation.getLHS();
                String propertyName = ((SpecAttribute) lhs).getName();
                if (getPqlNameList().contains(propertyName)) {
                    createNewLine(relation);
                    eventManager.registerListener(predicateModifiedListner,
                                                  ContextualEventType.PREDICATE_MODIFIED_EVENT,
                                                  relation);
                }
            }
        }
        if (panel.isEditable()) {
            createNewButton();
        }
    }

    private void createNewButton() {
        buttonAdd = new Button(contentAnaysisContainer, SWT.FLAT);
        buttonAdd.setText(ApplicationMessages.PROPERTYEXPRESSIONCONTROL_ADD);
        buttonAdd
            .setToolTipText(ApplicationMessages.PROPERTYEXPRESSIONCONTROL_ADD_CONDITION);
        buttonAdd.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        GridData gridData = new GridData();
        gridData.widthHint = BUTTON_WIDTH;
        gridData.heightHint = BUTTON_HEIGHT;
        buttonAdd.setLayoutData(gridData);
        buttonAdd.addSelectionListener(new SelectionAdapter() {
            @Override
                public void widgetSelected(SelectionEvent e) {
                List<String> displays = getDisplayNameList();
                if (displays == null || displays.size() == 0) {
                    return;
                }
                String name = displays.get(0);
                AttributeDescriptor descriptor = getAttributeForDisplayName(name);
                String internal = descriptor.getPqlName();
                Relation relation = null;
                RelationOp op = RelationOp.EQUALS;
                String value = null;
                if (internal.equals(EditorMessages.COMPONENTEDITOR_CONTENT_TYPE)) {
                    value = getEnumDisplayValues(descriptor)[0];
                } else if (internal
                           .equals(EditorMessages.COMPONENTEDITOR_CONTAINS_CONTENT)) {
                    value = constructContent(
                        getEnumDisplayValues(descriptor)[0], "", 1);
                }
                relation = new Relation(op, descriptor.getAttribute(), Constant
                                        .build(value));

                PredicateHelpers.addPredicate(relations, relation);
                createNewLine(relation);
                addUndoElement(PropertyExpressionUndoElementOp.ADD, relations
                               .predicateCount() - 1, relation, null, false);
                refreshLater();
            }
        });

        if (propertiesList.size() == 0) {
            buttonAdd.setEnabled(false);
        }
    }

    private void removeLine(int index) {
        Control c = (Control) buttonList.remove(index);
        c.dispose();
        c = (Control) propertyNameList.remove(index);
        c.dispose();
        c = (Control) operatorList.remove(index);
        c.dispose();
        c = (Control) detailList.remove(index);
        c.dispose();
    }

    private void addUndoElement(PropertyExpressionUndoElementOp undoOp,
                                int index, Object newValue, Object oldValue, boolean continuation) {
        PropertyExpressionUndoElement undoElement = new PropertyExpressionUndoElement();
        undoElement.setIndex(index);
        undoElement.setOp(undoOp);
        undoElement.setOldValue(oldValue);
        undoElement.setNewValue(newValue);
        undoElement.setControlId(1);
        undoElement.setContinuation(continuation);
        GlobalState.getInstance().addUndoElement(undoElement);
    }

    private void relayoutParent() {
        panel.getComposite().setRedraw(false);
        contentAnaysisContainer.layout(true, true);
        relayout();
        panel.getComposite().setRedraw(true);
    }

    private void refreshLater() {
        Display.getCurrent().asyncExec(new Runnable() {

            public void run() {
                refresh();
            }
        });
    }

    private void disposeWidgetArray(List<? extends Widget> list) {
        for (Widget widget : list) {
            widget.dispose();
        }
    }

    private void refresh() {
        if (buttonAdd != null && !buttonAdd.isDisposed()) {
            buttonAdd.dispose();
        }
        disposeWidgetArray(buttonList);
        disposeWidgetArray(propertyNameList);
        disposeWidgetArray(operatorList);
        disposeWidgetArray(detailList);

        buttonList.clear();
        propertyNameList.clear();
        operatorList.clear();
        detailList.clear();

        initializeLayout();
        relayoutParent();
    }

    private int findIndex(int index) {
        int internal_ca = 0;
        int internal_all = 0;
        for (int i = 0; i < relations.predicateCount(); i++) {
            IPredicate spec = (IPredicate) relations.predicateAt(i);
            if (spec instanceof Relation) {
                Relation relation = (Relation) spec;
                final IExpression lhs = relation.getLHS();
                String propertyName = ((SpecAttribute) lhs).getName();
                if (getPqlNameList().contains(propertyName)) {
                    if (internal_ca == index) {
                        return internal_all;
                    } else {
                        internal_ca++;
                    }
                }
                internal_all++;
            }
        }
        return 0;
    }

    private void createNewLine(Relation relation) {
        final IExpression lhs = relation.getLHS();
        final RelationOp op = relation.getOp();
        final IExpression rhs = relation.getRHS();
        String propertyName = ((SpecAttribute) lhs).getName();
        AttributeDescriptor descriptor = getAttributeForPqlName(propertyName);

        if (panel.isEditable()) {
            Button buttonRemove = new Button(contentAnaysisContainer, SWT.FLAT);
            buttonRemove
                .setText(ApplicationMessages.PROPERTYEXPRESSIONCONTROL_REMOVE);
            buttonRemove
                .setToolTipText(ApplicationMessages.PROPERTYEXPRESSIONCONTROL_REMOVE_CONDITION);
            buttonRemove.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
            GridData data = new GridData();
            data.widthHint = BUTTON_WIDTH;
            data.heightHint = BUTTON_HEIGHT;
            buttonRemove.setLayoutData(data);
            buttonList.add(buttonRemove);
            buttonRemove.addSelectionListener(new SelectionAdapter() {
                @Override
                    public void widgetSelected(SelectionEvent e) {
                    Button b = (Button) e.getSource();
                    int index = buttonList.indexOf(b);
                    removeLine(index);
                    IPredicate oldValue = PredicateHelpers.removePredicateAt(
                        relations, findIndex(index));
                    addUndoElement(PropertyExpressionUndoElementOp.REMOVE,
                                   findIndex(index), null, oldValue, false);
                    relayoutParent();
                }
            });

            Combo comboPropertyName = new Combo(contentAnaysisContainer,
                                                SWT.DROP_DOWN | SWT.READ_ONLY);
            data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            comboPropertyName.setLayoutData(data);
            List<String> displayNameList = getDisplayNameList();
            comboPropertyName.setItems(displayNameList
                                       .toArray(new String[displayNameList.size()]));
            propertyNameList.add(comboPropertyName);
            Point controlSize = comboPropertyName.computeSize(SWT.DEFAULT,
                                                              SWT.DEFAULT);
            data.widthHint = PlatformUtils.findProperControlWidth(
                controlSize.x, 100, 50);
            int index = 0;
            String name = descriptor.getDisplayName();
            String internal = descriptor.getPqlName();
            for (int i = 0, n = displayNameList.size(); i < n; i++) {
                if (displayNameList.get(i).equals(name)) {
                    index = i;
                    break;
                }
            }
            comboPropertyName.select(index);
            comboPropertyName.addSelectionListener(new SelectionAdapter() {
                @Override
                    public void widgetSelected(SelectionEvent e) {
                    Combo source = (Combo) e.getSource();
                    int index = propertyNameList.indexOf(source);
                    String newText = source.getText();
                    Relation spec = (Relation) PredicateHelpers.getPredicateAt(
                        relations, findIndex(index));
                    AttributeDescriptor newDescriptor = getAttributeForDisplayName(newText);
                    SpecAttribute newValue = (SpecAttribute) newDescriptor
                                             .getAttribute();
                    Object oldValue = spec.getLHS();
                    if (!oldValue.equals(newValue)) {
                        addUndoElement(
                            PropertyExpressionUndoElementOp.CHANGE_ATTRIBUTE,
                        findIndex(index), newValue, oldValue, true);
                        IExpression newRHS = newValue.build("");
                        RelationOp newOp = RelationOp.EQUALS;
                        String internal = newDescriptor.getPqlName();
                        if (internal
                            .equals(EditorMessages.COMPONENTEDITOR_CONTENT_TYPE)) {
                            newRHS = newValue.build(getInternal(newDescriptor
                                                                .getEnumeratedValues().get(0)));
                        } else if (internal
                                   .equals(EditorMessages.COMPONENTEDITOR_CONTAINS_CONTENT)) {
                            newRHS = Constant.build(constructContent(
                                                        getDisplay(newDescriptor
                                                                   .getEnumeratedValues().get(0)), "",
                                                    1));
                        }

                        addUndoElement(
                            PropertyExpressionUndoElementOp.CHANGE_VALUE,
                        findIndex(index), newRHS, spec.getRHS(), true);
                        addUndoElement(
                            PropertyExpressionUndoElementOp.CHANGE_OP,
                        findIndex(index), newOp, spec.getOp(), false);
                        spec.setLHS(newValue);
                        spec.setOp(newOp);
                        spec.setRHS(newRHS);

                        refreshLater();
                    }
                }
            });

            Combo comboOperator = new Combo(contentAnaysisContainer,
                                            SWT.DROP_DOWN | SWT.READ_ONLY);
            data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            data.widthHint = 100;
            comboOperator.setLayoutData(data);
            operatorList.add(comboOperator);
            if (internal.equals(EditorMessages.COMPONENTEDITOR_CONTENT_TYPE)) {
                comboOperator.setItems(getOperators(descriptor));
                if (RelationOp.EQUALS == op) {
                    comboOperator.setText("is");
                } else {
                    comboOperator.setText("is not");
                }
            } else if (internal
                       .equals(EditorMessages.COMPONENTEDITOR_CONTAINS_CONTENT)) {
                comboOperator.setItems(getEnumDisplayValues(descriptor));
                Constant constant = (Constant) rhs;
                String value = (String) constant.getValue().getValue();
                String value1 = getValue1(value);
                comboOperator.setText(getDisplayFromInternal(value1));
            }
            controlSize = comboOperator.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            data.widthHint = PlatformUtils.findProperControlWidth(
                controlSize.x, 100, 50);
            comboOperator.addSelectionListener(new SelectionAdapter() {
                @Override
                    public void widgetSelected(SelectionEvent e) {
                    Combo source = (Combo) e.getSource();
                    int index = operatorList.indexOf(source);
                    String newOpText = source.getText();
                    Relation spec = (Relation) PredicateHelpers.getPredicateAt(
                        relations, findIndex(index));
                    IExpression oldLHS = spec.getLHS();
                    RelationOp oldOp = spec.getOp();
                    IExpression oldRHS = spec.getRHS();
                    SpecAttribute attribute = (SpecAttribute) oldLHS;
                    AttributeDescriptor newDescriptor = getAttributeForPqlName(attribute
                                                                               .getName());
                    String internal = newDescriptor.getPqlName();
                    if (internal.equals(EditorMessages.COMPONENTEDITOR_CONTENT_TYPE)) {
                        RelationOp newOp;
                        if (newOpText.equals("is")) {
                            newOp = RelationOp.EQUALS;
                        } else {
                            newOp = RelationOp.NOT_EQUALS;
                        }
                        if (!newOp.equals(oldOp)) {
                            addUndoElement(
                                PropertyExpressionUndoElementOp.CHANGE_OP,
                            findIndex(index), newOp, oldOp, false);
                            spec.setOp(newOp);

                            refreshLater();
                        }
                    } else if (internal
                               .equals(EditorMessages.COMPONENTEDITOR_CONTAINS_CONTENT)) {
                        String oldValue = (String) ((Constant) oldRHS)
                                          .getValue().getValue();
                        String newValue = constructContent(
                            getInternalFromDisplay(newOpText), "", 1);
                        if (!oldValue.equals(newValue)) {
                            IExpression newRHS = Constant.build(newValue);
                            addUndoElement(
                                PropertyExpressionUndoElementOp.CHANGE_VALUE,
                            findIndex(index), newRHS, spec.getRHS(),
                            false);
                            spec.setRHS(newRHS);

                            refreshLater();
                        }
                    }
                }
            });

            Composite compositeDetail = new Composite(contentAnaysisContainer,
                                                      SWT.NONE);
            compositeDetail.setBackground(panel.getBackground());
            data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            data.heightHint = CONTROL_HEIGHT;
            compositeDetail.setLayoutData(data);
            GridLayout layout = new GridLayout(3, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            compositeDetail.setLayout(layout);
            detailList.add(compositeDetail);
            Constant constant = (Constant) rhs;
            String value = (String) constant.getValue().getValue();
            String value1 = getValue1(value);
            String value2 = getValue2(value);
            String value3 = getValue3(value);
            if (internal.equals(EditorMessages.COMPONENTEDITOR_CONTENT_TYPE)) {
                Combo combo = new Combo(compositeDetail, SWT.DROP_DOWN
                                        | SWT.READ_ONLY);
                data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
                data.widthHint = 100;
                combo.setLayoutData(data);
                combo.setItems(getEnumDisplayValues(descriptor));
                combo.setText(value1);
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                        public void widgetSelected(SelectionEvent e) {
                        Combo source = (Combo) e.getSource();
                        int index = detailList.indexOf(source.getParent());
                        String newValueText = source.getText();
                        Relation spec = (Relation) PredicateHelpers
                                        .getPredicateAt(relations, findIndex(index));
                        IExpression oldRHS = spec.getRHS();
                        String newValue = getInternalFromDisplay(newValueText);
                        String oldValue = (String) ((Constant) oldRHS)
                                          .getValue().getValue();
                        if (!oldValue.equals(newValue)) {
                            IExpression newRHS = Constant.build(newValue);
                            addUndoElement(
                                PropertyExpressionUndoElementOp.CHANGE_VALUE,
                            findIndex(index), newRHS, oldRHS, false);
                            spec.setRHS(newRHS);
                        }
                    }
                });
            } else if (internal
                       .equals(EditorMessages.COMPONENTEDITOR_CONTAINS_CONTENT)) {
                if (value1.equals(EditorMessages.COMPONENTEDITOR_KEYWORDS)) {
                    Text text = new Text(compositeDetail, SWT.BORDER);
                    data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
                    data.widthHint = 200;
                    text.setLayoutData(data);
                    text.setText(value2);
                    text.addFocusListener(new FocusAdapter() {
                        @Override
                            public void focusLost(FocusEvent e) {
                            Text source = (Text) e.getSource();
                            int index = detailList.indexOf(source.getParent());
                            String newValueText = source.getText();
                            Relation spec = (Relation) PredicateHelpers
                                            .getPredicateAt(relations, findIndex(index));
                            IExpression oldRHS = spec.getRHS();
                            Combo combo = operatorList.get(index);
                            Spinner spinner = (Spinner) source.getParent()
                                              .getChildren()[2];
                            String newValue = constructContent(
                                getInternalFromDisplay(combo.getText()),
                            newValueText, spinner.getSelection());
                            String oldValue = (String) ((Constant) oldRHS)
                                              .getValue().getValue();
                            if (!oldValue.equals(newValue)) {
                                IExpression newRHS = Constant.build(newValue);
                                addUndoElement(
                                    PropertyExpressionUndoElementOp.CHANGE_VALUE,
                                findIndex(index), newRHS, oldRHS, false);
                                spec.setRHS(newRHS);
                            }
                        }
                    });
                }
                Label label = new Label(compositeDetail, SWT.NONE);
                label.setBackground(panel.getBackground());
                label.setText(EditorMessages.COMPONENTEDITOR_MATCH_COUNT);

                Spinner spinner = new Spinner(compositeDetail, SWT.BORDER);
                spinner.setMinimum(1);
                spinner.setMaximum(10000);
                spinner.setSelection(Integer.valueOf(value3).intValue());
                spinner.addSelectionListener(new SelectionAdapter() {
                    @Override
                        public void widgetSelected(SelectionEvent e) {
                        Spinner source = (Spinner) e.getSource();
                        int index = detailList.indexOf(source.getParent());
                        Combo combo = operatorList.get(index);
                        Control control = source.getParent().getChildren()[0];
                        String newValue = "";
                        Relation spec = (Relation) PredicateHelpers
                                        .getPredicateAt(relations, findIndex(index));
                        IExpression oldRHS = spec.getRHS();
                        if (control instanceof Text) {
                            Text text = (Text) control;
                            newValue = constructContent(
                                getInternalFromDisplay(combo.getText()),
                            text.getText(), source.getSelection());
                        } else {
                            newValue = constructContent(
                                getInternalFromDisplay(combo.getText()),
                            "", source.getSelection());
                        }
                        String oldValue = (String) ((Constant) oldRHS)
                                          .getValue().getValue();
                        if (!oldValue.equals(newValue)) {
                            IExpression newRHS = Constant.build(newValue);
                            addUndoElement(
                                PropertyExpressionUndoElementOp.CHANGE_VALUE,
                            findIndex(index), newRHS, oldRHS, false);
                            spec.setRHS(newRHS);
                        }
                    }
                });
            }
        } else { // not editable
            Label label = new Label(contentAnaysisContainer, SWT.NONE);
            label.setBackground(panel.getBackground());
            GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            data.widthHint = BUTTON_WIDTH + EditorPanel.SPACING + BUTTON_WIDTH;
            data.heightHint = BUTTON_HEIGHT;
            label.setLayoutData(data);
            Label labelName = new Label(contentAnaysisContainer, SWT.NONE);
            labelName.setEnabled(false);
            data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            data.heightHint = BUTTON_HEIGHT;
            labelName.setLayoutData(data);
            labelName.setBackground(ColorBundle.LIGHT_GRAY);
            labelName.setText(descriptor.getDisplayName());

            Label labelOp = new Label(contentAnaysisContainer, SWT.NONE);
            Label labelValue = new Label(contentAnaysisContainer, SWT.NONE);
            labelOp.setEnabled(false);
            labelValue.setEnabled(false);
            labelOp.setBackground(ColorBundle.LIGHT_GRAY);
            labelValue.setBackground(ColorBundle.LIGHT_GRAY);
            data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            data.heightHint = BUTTON_HEIGHT;
            labelOp.setLayoutData(data);
            data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            data.heightHint = BUTTON_HEIGHT;
            data.widthHint = TEXT_WIDTH;
            labelValue.setLayoutData(data);
            String value = (String) ((Constant) rhs).getValue().getValue();
            String internal = descriptor.getPqlName();
            if (internal.equals(EditorMessages.COMPONENTEDITOR_CONTENT_TYPE)) {
                if (op.equals(RelationOp.EQUALS)) {
                    labelOp.setText("is");
                } else {
                    labelOp.setText("is not");
                }
                labelValue.setText(value);
            } else if (internal
                       .equals(EditorMessages.COMPONENTEDITOR_CONTAINS_CONTENT)) {
                String value1 = getValue1(value);
                if (value1.equals(EditorMessages.COMPONENTEDITOR_KEYWORDS)) {
                    labelOp.setText(value1 + " " + getValue2(value));
                } else {
                    labelOp.setText(getDisplayFromInternal(value1));
                }
                labelValue.setText(EditorMessages.COMPONENTEDITOR_MATCH_COUNT
                                   + getValue3(value));
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bluejungle.destiny.policymanager.editor.ComponentEditor#getPropertyList
     * ()
     */
    @Override
	protected List<String> getPropertyList() {
        return null;
    }

    public boolean hasCustomProperties() {
        return true;
    }

    private void preparePropertiesList() {
        // Not sure why this is always RESOURCE.  It has something to do with making sure that the "content" section is
        // editable, but other than that it's a mystery
        Collection<AttributeDescriptor> attributes = PolicyServerProxy.getAttributes(EntityType.RESOURCE);
        propertiesList = new ArrayList<AttributeDescriptor>();
        for (AttributeDescriptor attribute : attributes) {
            String group = attribute.getGroupName();
            String internal = attribute.getPqlName();
            if (group != null
                && group.equals(EditorMessages.COMPONENTEDITOR_WITH_CONTENT)
                && (internal.equals(EditorMessages.COMPONENTEDITOR_CONTENT_TYPE) || internal
                    .equals(EditorMessages.COMPONENTEDITOR_CONTAINS_CONTENT))) {
                propertiesList.add(attribute);
                getEnumDisplayValues(attribute);
            }
        }
    }

    private List<String> getPqlNameList() {
        List<String> list = new ArrayList<String>();
        for (AttributeDescriptor descriptor : propertiesList) {
            list.add(descriptor.getPqlName());
        }
        return list;
    }

    private List<String> getDisplayNameList() {
        List<String> list = new ArrayList<String>();
        for (AttributeDescriptor descriptor : propertiesList) {
            list.add(descriptor.getDisplayName());
        }
        Collections.sort(list);
        return list;
    }

    private AttributeDescriptor getAttributeForDisplayName(String name) {
        for (AttributeDescriptor descriptor : propertiesList) {
            if (descriptor.getDisplayName().equals(name)) {
                return descriptor;
            }
        }
        return null;
    }

    private String[] getOperators(AttributeDescriptor descriptor) {
        if (descriptor.getPqlName().equals(
                EditorMessages.COMPONENTEDITOR_CONTENT_TYPE)) {
            return new String[] { "is", "is not" };
        }
        return null;
    }

    private String[] getEnumDisplayValues(AttributeDescriptor descriptor) {
        List<String> list = new ArrayList<String>();
        for (String value : descriptor.getEnumeratedValues()) {
            String internal = getInternal(value);
            String display = getDisplay(value);
            valueMap.put(internal, display);
            list.add(display);
        }
        Collections.sort(list);
        return list.toArray(new String[list.size()]);
    }

    private String getDisplayFromInternal(String str) {
        String result = valueMap.get(str);

        if (result == null) {
            return str;
        }
        return result;
    }

    private String getInternalFromDisplay(String str) {
        if (!valueMap.containsValue(str)) {
            return str;
        }
        for (String key : valueMap.keySet()) {
            if (valueMap.get(key).equals(str)) {
                return key;
            }
        }
        return str;
    }

    private String getInternal(String str) {
        int index = str.indexOf(':');
        if (index != -1) {
            return str.substring(0, index);
        }
        return str;
    }

    private String getDisplay(String str) {
        int index = str.indexOf(':');
        if (index != -1 && index < str.length() - 1) {
            return str.substring(index + 1);
        }
        return str;
    }

    private AttributeDescriptor getAttributeForPqlName(String name) {
        for (AttributeDescriptor descriptor : propertiesList) {
            if (descriptor.getPqlName().equals(name)) {
                return descriptor;
            }
        }
        return null;
    }

    /**
     * the format of str is: "*:REG:ccn>=5:*" or: "*:KEY:plutonium>=2:*"
     * 
     * @param str
     * @return Keywords(s) or other value name
     */
    private String getValue1(String str) {
        if (str.startsWith("*KEY:")) {
            return EditorMessages.COMPONENTEDITOR_KEYWORDS;
        } else if (str.startsWith("*REG:")) {
            int index1 = str.indexOf(":");
            int index2 = str.lastIndexOf(">=");
            return str.substring(index1 + 1, index2);
        }
        return str;
    }

    private String getValue2(String str) {
        if (str.startsWith("*KEY:")) {
            int index1 = str.indexOf(":");
            int index2 = str.lastIndexOf(">=");
            return str.substring(index1 + 1, index2);
        }
        return str;
    }

    private String getValue3(String str) {
        if (str.startsWith("*REG:") || str.startsWith("*KEY")) {
            int index1 = str.lastIndexOf(">=");
            int index2 = str.lastIndexOf(";*");
            return str.substring(index1 + 2, index2);
        }
        return str;
    }

    private String constructContent(String display, String key, int count) {
        String result = "*";
        if (display.equals(EditorMessages.COMPONENTEDITOR_KEYWORDS)) {
            result += "KEY:";
            result += key;
        } else {
            result += "REG:";
            result += getInternalFromDisplay(display);
        }
        result += ">=";
        result += count;
        result += ";*";
        return result;
    }

    @Override
	protected String getLookupLabel() {
        return EditorMessages.LOOK_UP;
    }

    @Override
	public boolean isShowPropertyExpressions() {
        return true;
    }
}





