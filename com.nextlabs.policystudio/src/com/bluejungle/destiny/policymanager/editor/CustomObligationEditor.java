/*
 * Created on Aug 22, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElement;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElementOp;
import com.bluejungle.destiny.policymanager.ui.controls.CalendarPicker;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.pf.destiny.lifecycle.AttributeDescriptor;
import com.bluejungle.pf.destiny.lifecycle.AttributeType;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.ObligationArgument;
import com.bluejungle.pf.destiny.lifecycle.ObligationDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.obligation.CustomObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;

/**
 * @author bmeng
 * @version $Id$
 */

public class CustomObligationEditor extends BaseObligationEditor {
    private final static int BUTTON_SIZE = 15;
    private Button check;
    private Composite container;
    private DateFormat formatter;
    private Collection<AttributeDescriptor> attributes = null;
    private static Collection<ObligationDescriptor> obligationDescriptors;

    private static String ATTRIBUTE_TOKEN = "$ResourceAttribute.Name";
    private static String VALUE_TOKEN = "$ResourceAttribute.Value";
    private static String VALUES_TOKEN = "$ResourceAttribute.Values";

    static {
        try {
            obligationDescriptors = PolicyServerProxy.client.getObligationDescriptors();
        } catch (PolicyEditorException e) {
            //TODO what should I say?
            e.printStackTrace();
        }
    }

    public CustomObligationEditor(Composite parent, IDPolicy policy,
            IDEffectType effectType, boolean enabled) {
        super(parent, policy, effectType, enabled);
    }

    @Override
    protected void init() {
        formatter = new SimpleDateFormat("MM/dd/yyyy");

        super.init();

        obligationCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IDPolicy policy = getPolicy();
                IDEffectType effectType = getEffectType();
                boolean checkBoxIsSelected = obligationCheckBox.getSelection();
                if (checkBoxIsSelected) {
                    IObligation obligationToAdd = createObligation();
                    policy.addObligation(obligationToAdd, effectType);
                    
                    PolicyUndoElement.add(
                            PolicyUndoElementOp.ADD_OBLIGATION
                          , null
                          , new PolicyUndoElement.ObligationRecord(effectType, obligationToAdd)
                    );

                    createDetail();
                } else {
                    IObligation obligationToRemove = null;
                        while (!findObligations().isEmpty()) {
                        obligationToRemove = getObligation();
                        policy.deleteObligation(obligationToRemove, effectType);
                    }

                    // TODO something wrong here! why we only undo the last
                    // obligation
                    // either 1. there is only one, (then we should not use while loop)
                    // 2. We only care the last one, (then we should not create the element into loop)
                    // also what happen if there is no obligation?

                    if (obligationToRemove != null) {
                        PolicyUndoElement.add(PolicyUndoElementOp.REMOVE_OBLIGATION, null, new PolicyUndoElement.ObligationRecord(effectType, obligationToRemove));
                    }

                    disposeDetail();
                }

                CustomObligationEditor.this.layout();
                GlobalState.getInstance().getEditorPanel().relayout();
            }
        });

        if (obligationExists()) {
            createDetail();
        }
    }

    private void createDetail() {
        check = new Button(this, SWT.CHECK);
        check.setVisible(false);

        container = new Composite(this, SWT.NONE);
        container.setEnabled(getEnabled());
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        container.setLayoutData(data);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        container.setLayout(layout);

        List<IObligation> customObligations = findObligations();
        for (IObligation obligation : customObligations) {
            createLines((CustomObligation) obligation);
        }

        setBackground(getBackground());
    }

    private void showHideButton() {
        int count = 0;
        Control[] controls = container.getChildren();
        Button firstMinusButton = null;
        boolean foundLastPlusButton = false;
        for (int i = controls.length - 1; i >= 0; i--) {
            if (controls[i] instanceof Button) {
                Button button = (Button) controls[i];
                if (button.getText().equals(EditorMessages.POLICYEDITOR_PLUS)) {
                    count++;
                    if (!foundLastPlusButton) {
                        button.setVisible(true);
                        foundLastPlusButton = true;
                    } else {
                        button.setVisible(false);
                    }
                }
                if (button.getText().equals(EditorMessages.POLICYEDITOR_MINUS)) {
                    firstMinusButton = button;
                }
            }
        }
        if (count == 1) {
            firstMinusButton.setVisible(false);
        } else {
            firstMinusButton.setVisible(true);
        }

        // if is disabled, all the buttons should be invisible
        if (!getEnabled()) {
            for (int i = controls.length - 1; i >= 0; i--) {
                if (controls[i] instanceof Button) {
                    controls[i].setVisible(getEnabled());
                }
            }
        }
    }

    private CustomObligationComposite createLines(CustomObligation obligation) {
        GridData data;
        Button minusButton = new Button(container, SWT.FLAT);
        minusButton.setEnabled(getEnabled());
        minusButton.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        minusButton.setText(EditorMessages.POLICYEDITOR_MINUS);
        minusButton
                .setToolTipText(EditorMessages.POLICYEDITOR_CUSTOM_OBLIGATION_REMOVE_BUTTON_TOOLTIP);
        data = new GridData(GridData.VERTICAL_ALIGN_END);
        data.widthHint = BUTTON_SIZE;
        data.heightHint = BUTTON_SIZE;
        minusButton.setLayoutData(data);
        minusButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Button internalMinusButton = (Button) e.getSource();
                Button internalPlusButton = (Button) internalMinusButton
                        .getData();
                CustomObligationComposite internalComposite = (CustomObligationComposite) internalPlusButton
                        .getData();
                IDEffectType effectType = getEffectType();
                CustomObligation obligation = internalComposite.getObligation();
                getPolicy().deleteObligation(obligation, effectType);

                internalComposite.dispose();
                internalPlusButton.dispose();
                internalMinusButton.dispose();

                showHideButton();
                CustomObligationEditor.this.layout();
                GlobalState.getInstance().getEditorPanel().relayout();
            }
        });

        Button plusButton = new Button(container, SWT.FLAT);
        plusButton.setEnabled(getEnabled() && obligationDescriptors.size() > 0);
        plusButton.setText(EditorMessages.POLICYEDITOR_PLUS);
        plusButton.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        plusButton
                .setToolTipText(EditorMessages.POLICYEDITOR_CUSTOM_OBLIGATION_ADD_BUTTON_TOOLTIP);
        data = new GridData(GridData.VERTICAL_ALIGN_END);
        data.widthHint = BUTTON_SIZE;
        data.heightHint = BUTTON_SIZE;
        plusButton.setLayoutData(data);
        plusButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CustomObligation newObligation = (CustomObligation) createObligation();
                IDEffectType effectType = getEffectType();
                getPolicy().addObligation(newObligation, effectType);

                createLines(newObligation);

                showHideButton();
                CustomObligationEditor.this.layout();
                GlobalState.getInstance().getEditorPanel().relayout();
            }
        });

        CustomObligationComposite customObligationComposite = new CustomObligationComposite(
                container, obligation, getEnabled());
        data = new GridData(GridData.FILL_HORIZONTAL);
        customObligationComposite.setLayoutData(data);

        minusButton.setData(plusButton);
        plusButton.setData(customObligationComposite);

        showHideButton();
        CustomObligationEditor.this.layout();

        setBackground(getBackground());
        return customObligationComposite;
    }

    private void disposeDetail() {
        check.dispose();
        check = null;
        container.dispose();
        container = null;
    }

    @Override
    protected void initData() {
        super.initData();

        obligationCheckBox.setEnabled(getEnabled()
                && obligationDescriptors.size() > 0);
    }

    @Override
    protected String getTitle() {
        return EditorMessages.POLICYEDITOR_CUSTOM_OBLIGATION;
    }

    @Override
    protected String getObligationType() {
        return CustomObligation.OBLIGATION_NAME;
    }

    protected IObligation createObligation() {
        CustomObligation obligation = getObligationManager()
                .createCustomObligation("", Collections.EMPTY_LIST);

        if (obligationDescriptors.isEmpty()) {
            return obligation;
        }
        ObligationDescriptor descriptor = obligationDescriptors.iterator()
                .next();
        obligation.setCustomObligationName(descriptor.getDisplayName());
        List<String> arguments = new ArrayList<String>();
        for (ObligationArgument argument : descriptor.getObligationArguments()) {
            String name = argument.getDisplayName();
            String defaultValue = argument.getDefaultValue();
            String[] values = argument.getValues();
            arguments.add(name);

            if (defaultValue != null) {
                arguments.add(argument.getDefaultValue());
            } else if (values == null || values.length == 0) {
                arguments.add("");
            } else if (values[0].equals(ATTRIBUTE_TOKEN)) {
                arguments.add(getDefaultAttribute());
            } else if (values[0].equals(VALUE_TOKEN)
                    || values[0].equals(VALUES_TOKEN)) {
                String tagName = getDefaultAttribute();
                AttributeDescriptor des = getAttributeDescriptor(tagName);
                AttributeType type = (des != null) ? des.getType() : AttributeType.STRING;

                if (values[0].equals(VALUE_TOKEN)) {
                    if (type == AttributeType.BOOLEAN) {
                        arguments.add("Yes");
                    } else if (type == AttributeType.DATE) {
                        arguments.add(formatter
                                .format(getDefaultDateExpression().getTime()));
                    } else if (type == AttributeType.ENUM) {
                        List<String> attributes = getEnumAttribute(tagName);
                        arguments.add(attributes.get(0));
                    } else if (type == AttributeType.LONG) {
                        arguments.add("0");
                    } else if (type == AttributeType.STRING) {
                        arguments.add("");
                    }
                } else if (values[0].equals(VALUES_TOKEN)) {
                    if (type == AttributeType.ENUM) {
                        arguments.add(getAttributes(tagName));
                    } else {
                        arguments.add("");
                    }
                }
            } else {
                arguments.add(values[0]);
            }
        }
        obligation.setCustomArgs(arguments);
        return obligation;
    }

    public class CustomObligationComposite extends Composite {
        private CustomObligation obligation;
        private Combo comboObligationName;
        private Composite subContainer;

        private SelectionAdapter selectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                List<String> args = new ArrayList<String>();
                for (Control control : subContainer.getChildren()) {
                    if (control instanceof Label) {
                        Label label = (Label) control;
                        args.add(label.getText());
                    } else if (control instanceof Text) {
                        Text text = (Text) control;
                        args.add(text.getText());
                    } else if (control instanceof Combo) {
                        Combo combo = (Combo) control;
                        args.add(combo.getText());
                    } else if (control instanceof CalendarPicker) {
                        CalendarPicker picker = (CalendarPicker) control;
                        args.add(formatter.format(picker.getCalendar()
                                .getTime()));
                    }
                }

                ObligationDescriptor descriptor = (ObligationDescriptor) subContainer
                        .getData();
                int controlIndex = getSubControlPosition(subContainer,
                        (Control) e.getSource());
                int tagNameIndex = getTagNamePosition(descriptor);
                if (controlIndex == tagNameIndex) {
                    String tagName = ((Combo) e.getSource()).getText();
                    AttributeDescriptor des = getAttributeDescriptor(tagName);
                    AttributeType type = (des != null) ? des.getType() : AttributeType.STRING;
                    int tagValueIndex = getTagValuePosition(descriptor);
                    if (isAutomaticTagging(descriptor)) {
                        Control control = null;
                        GridData data = new GridData(GridData.FILL_HORIZONTAL);
                        if (type == AttributeType.BOOLEAN) {
                            args.add(tagValueIndex, "Yes");
                            Combo combo = new Combo(subContainer, SWT.READ_ONLY
                                    | SWT.BORDER);
                            combo.setItems(new String[] { "Yes", "No" });
                            combo.setText("Yes");
                            combo.addModifyListener(modifyListener);
                            control = combo;
                            control.setLayoutData(data);
                        } else if (type == AttributeType.DATE) {
                            args.add(3, formatter
                                    .format(getDefaultDateExpression()
                                            .getTime()));
                            CalendarPicker picker = new CalendarPicker(
                                    subContainer, SWT.BORDER);
                            picker.setCalendar(getDefaultDateExpression());
                            picker.addSelectionListener(selectionListener);
                            control = picker;
                            control.setLayoutData(data);
                        } else if (type == AttributeType.ENUM) {
                            List<String> values = getEnumAttribute(tagName);
                            args.add(tagValueIndex, values.get(0));
                            Combo combo = new Combo(subContainer, SWT.READ_ONLY
                                    | SWT.BORDER);
                            combo.setItems(values.toArray(new String[values
                                    .size()]));
                            combo.setText(values.get(0));
                            combo.addModifyListener(modifyListener);
                            control = combo;
                            control.setLayoutData(data);
                        } else if (type == AttributeType.LONG) {
                            args.add(tagValueIndex, "0");
                            Text text = new Text(subContainer, SWT.BORDER);
                            text.setText("0");
                            text.addModifyListener(modifyListener);
                            control = text;
                            control.setLayoutData(data);
                        } else if (type == AttributeType.STRING) {
                            args.add(tagValueIndex, "");
                            Text text = new Text(subContainer, SWT.BORDER);
                            text.setText("");
                            text.addModifyListener(modifyListener);
                            control = text;
                            control.setLayoutData(data);
                        }
                        args.remove(tagValueIndex + 1);
                        subContainer.getChildren()[tagValueIndex].dispose();
                        control
                                .moveBelow(subContainer.getChildren()[tagValueIndex - 1]);
                        subContainer.layout(true, true);
                    }
                }
                addHiddenArguments(args, obligation);
                obligation.setCustomArgs(args);
            }
        };

        private ModifyListener modifyListener = new ModifyListener() {

            public void modifyText(ModifyEvent e) {

                List<String> args = new ArrayList<String>();
                for (Control control : subContainer.getChildren()) {
                    if (control instanceof Label) {
                        Label label = (Label) control;
                        args.add(label.getText());
                    } else if (control instanceof Text) {
                        Text text = (Text) control;
                        args.add(text.getText());
                    } else if (control instanceof Combo) {
                        Combo combo = (Combo) control;
                        args.add(combo.getText());
                    } else if (control instanceof CalendarPicker) {
                        CalendarPicker picker = (CalendarPicker) control;
                        args.add(formatter.format(picker.getCalendar()
                                .getTime()));
                    }
                }

                ObligationDescriptor descriptor = (ObligationDescriptor) subContainer
                        .getData();
                int controlIndex = getSubControlPosition(subContainer,
                        (Control) e.getSource());
                int tagNameIndex = getTagNamePosition(descriptor);
                if (controlIndex == tagNameIndex) {
                    String tagName = ((Combo) e.getSource()).getText();
                    AttributeDescriptor des = getAttributeDescriptor(tagName);
                    AttributeType type = (des != null) ? des.getType() : AttributeType.STRING;
                    int tagValueIndex = getTagValuePosition(descriptor);
                    if (isAutomaticTagging(descriptor)) {
                        Control control = null;
                        GridData data = new GridData(GridData.FILL_HORIZONTAL);
                        if (type == AttributeType.BOOLEAN) {
                            args.add(tagValueIndex, "Yes");
                            Combo combo = new Combo(subContainer, SWT.READ_ONLY
                                    | SWT.BORDER);
                            combo.setItems(new String[] { "Yes", "No" });
                            combo.setText("Yes");
                            combo.addModifyListener(modifyListener);
                            control = combo;
                            control.setLayoutData(data);
                        } else if (type == AttributeType.DATE) {
                            args.add(3, formatter
                                    .format(getDefaultDateExpression()
                                            .getTime()));
                            CalendarPicker picker = new CalendarPicker(
                                    subContainer, SWT.BORDER);
                            picker.setCalendar(getDefaultDateExpression());
                            picker.addSelectionListener(selectionListener);
                            control = picker;
                            control.setLayoutData(data);
                        } else if (type == AttributeType.ENUM) {
                            List<String> values = getEnumAttribute(tagName);
                            args.add(tagValueIndex, values.get(0));
                            Combo combo = new Combo(subContainer, SWT.READ_ONLY
                                    | SWT.BORDER);
                            combo.setItems(values.toArray(new String[values
                                    .size()]));
                            combo.setText(values.get(0));
                            combo.addModifyListener(modifyListener);
                            control = combo;
                            control.setLayoutData(data);
                        } else if (type == AttributeType.LONG) {
                            args.add(tagValueIndex, "0");
                            Text text = new Text(subContainer, SWT.BORDER);
                            text.setText("0");
                            text.addModifyListener(modifyListener);
                            control = text;
                            control.setLayoutData(data);
                        } else if (type == AttributeType.STRING) {
                            args.add(tagValueIndex, "");
                            Text text = new Text(subContainer, SWT.BORDER);
                            text.setText("");
                            text.addModifyListener(modifyListener);
                            control = text;
                            control.setLayoutData(data);
                        }
                        args.remove(tagValueIndex + 1);
                        subContainer.getChildren()[tagValueIndex].dispose();
                        control
                                .moveBelow(subContainer.getChildren()[tagValueIndex - 1]);
                    } else {
                        String values = "";
                        if (type == AttributeType.ENUM) {
                            values = getAttributes(tagName);
                        }
                        args.add(tagValueIndex, values);
                        args.remove(tagValueIndex + 1);
                        Text text = (Text) subContainer.getChildren()[tagValueIndex];
                        String convertedValue = convertStringToComma(values);
                        text.setText(convertedValue);
                    }
                }
                addHiddenArguments(args, obligation);
                obligation.setCustomArgs(args);
                subContainer.layout(true, true);
            }
        };

        /**
         * Add hidden arguments to the obligation, also adjust the order the
         * arguments
         * 
         * @param arguments
         * @param obligation
         */
        private void addHiddenArguments(List<String> arguments,
                CustomObligation obligation) {
            for (ObligationDescriptor descriptor : obligationDescriptors) {
                if (descriptor.getDisplayName().equals(
                        obligation.getCustomObligationName())) {
                    ObligationArgument[] args = descriptor
                            .getObligationArguments();
                    String[] result = new String[args.length * 2];
                    for (int i = 0, n = args.length; i < n; i++) {
                        ObligationArgument arg = args[i];
                        String display = arg.getDisplayName();
                        result[2 * i] = display;
                        if (arguments.contains(display)) {
                            int index = arguments.indexOf(display);
                            result[2 * i + 1] = arguments.get(index + 1);
                        } else if (arg.isHidden()) {
                            String value = arg.getDefaultValue();
                            if (value == null) {
                                value = arg.getValues()[0];
                            }
                            result[2 * i + 1] = value;
                        } else {
                            result[2 * i + 1] = "";
                        }
                    }
                    arguments.clear();
                    for (String item : result) {
                        arguments.add(item);
                    }
                }
            }
        }

        /**
         * Create an instance of CustomObligationCommandComposite
         * 
         * @param editor
         * @param customObligation
         */
        public CustomObligationComposite(Composite parent,
                IObligation customObligation, boolean isEnabled) {
            super(parent, SWT.None);
            setEnabled(isEnabled);
            if (customObligation == null) {
                throw new NullPointerException(
                        "nextCustomObligation cannot be null.");
            }
            obligation = (CustomObligation) customObligation;
            init();
        }

        private void init() {
            GridLayout layout = new GridLayout();
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            setLayout(layout);
            Group group = new Group(this, SWT.NONE);
            GridData data = new GridData(GridData.FILL_BOTH);
            group.setLayoutData(data);
            layout = new GridLayout(2, false);
            layout.marginHeight = 5;
            layout.marginWidth = 5;
            group.setLayout(layout);

            Label label = new Label(group, SWT.NONE);
            label.setEnabled(getEnabled());
            label
                    .setText(EditorMessages.POLICYEDITOR_CUSTOM_OBLIGATION_COMMAND);
            data = new GridData();
            label.setLayoutData(data);

            comboObligationName = new Combo(group, SWT.BORDER | SWT.READ_ONLY);
            comboObligationName.setEnabled(getEnabled());
            data = new GridData(GridData.FILL_HORIZONTAL);
            comboObligationName.setLayoutData(data);
            for (ObligationDescriptor descriptor : obligationDescriptors) {
                comboObligationName.add(descriptor.getDisplayName());
            }

            comboObligationName.addModifyListener(new ModifyListener() {

                /**
                 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
                 */
                public void modifyText(ModifyEvent e) {
                    String newText = comboObligationName.getText();
                    ObligationDescriptor obligationDescriptor = null;
                    for (ObligationDescriptor descriptor : obligationDescriptors) {
                        if (descriptor.getDisplayName().equals(newText)) {
                            obligationDescriptor = descriptor;
                            break;
                        }
                    }
                    if (!obligation.getCustomObligationName().equals(newText)) {
                        obligation.setCustomObligationName(newText);

                        ObligationArgument[] arguments = obligationDescriptor
                                .getObligationArguments();
                        List<String> presetArguments = new ArrayList<String>();
                        if (arguments == null) {
                            arguments = new ObligationArgument[0];
                        }
                        for (int i = 0, n = arguments.length; i < n; i++) {
                            ObligationArgument argument = arguments[i];
                            if (argument.isHidden()) {
                                continue;
                            }
                            String name = argument.getDisplayName();
                            String defaultValue = argument.getDefaultValue();
                            String values[] = argument.getValues();
                            presetArguments.add(name);

                            if (defaultValue != null) {
                                presetArguments.add(argument.getDefaultValue());
                            } else if (values == null || values.length == 0) {
                                presetArguments.add("");
                            } else if (values[0].equals(ATTRIBUTE_TOKEN)) {
                                presetArguments.add(getDefaultAttribute());
                            } else if (values[0].equals(VALUE_TOKEN)
                                    || values[0].equals(VALUES_TOKEN)) {
                                String tagName = getDefaultAttribute();
                                AttributeDescriptor des = getAttributeDescriptor(tagName);
                                AttributeType type = (des != null) ? des.getType() : AttributeType.STRING;
                                if (values[0].equals(VALUE_TOKEN)) {
                                    if (type == AttributeType.BOOLEAN) {
                                        presetArguments.add("Yes");
                                    } else if (type == AttributeType.DATE) {
                                        presetArguments
                                                .add(formatter
                                                        .format(getDefaultDateExpression()
                                                                .getTime()));
                                    } else if (type == AttributeType.ENUM) {
                                        List<String> attributes = getEnumAttribute(tagName);
                                        presetArguments.add(attributes.get(0));
                                    } else if (type == AttributeType.LONG) {
                                        presetArguments.add("0");
                                    } else if (type == AttributeType.STRING) {
                                        presetArguments.add("");
                                    }
                                } else if (values[0].equals(VALUES_TOKEN)) {
                                    if (type == AttributeType.ENUM) {
                                        presetArguments
                                                .add(getAttributes(tagName));
                                    } else {
                                        presetArguments.add("");
                                    }
                                }
                            } else {
                                presetArguments.add(values[0]);
                            }
                        }
                        addHiddenArguments(presetArguments, obligation);
                        obligation.setCustomArgs(presetArguments);
                    }

                    for (Control control : subContainer.getChildren()) {
                        control.dispose();
                    }

                    for (String item : comboObligationName.getItems()) {
                        if (item.equals(newText)) {
                            try {
                                addArgumentsControl(subContainer,
                                        obligationDescriptor);
                            } catch (Exception ex) {
                                MessageDialog
                                        .openError(
                                                subContainer.getShell(),
                                                EditorMessages.ACTIONCOMPONENTEDITOR_ERROR,
                                                EditorMessages.CUSTOMOBLIGATIONEDITOR_ERROR_MSG);
                                return;
                            }
                            GridData data = new GridData(
                                    GridData.FILL_HORIZONTAL);
                            data.horizontalSpan = 2;
                            data.heightHint = subContainer.computeSize(
                                    SWT.DEFAULT, SWT.DEFAULT).y;
                            ObligationArgument[] arguments = obligationDescriptor
                                    .getObligationArguments();
                            if (arguments == null || arguments.length == 0) {
                                data.heightHint = 0;
                            }
                            subContainer.setLayoutData(data);
                            subContainer.layout(true, true);
                        }
                    }
                    try {
                        GlobalState.getInstance().getEditorPanel().relayout();
                    } catch (Exception ex) {
                    }
                }
            });

            subContainer = new Composite(group, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 2;
            data.heightHint = 0;
            subContainer.setLayoutData(data);
            layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            subContainer.setLayout(layout);

            if (obligation != null) {
                comboObligationName.setText(obligation
                        .getCustomObligationName());
            }
        }

        @SuppressWarnings("unchecked")
        private void addArgumentsControl(final Composite root,
                ObligationDescriptor descriptor) {
            root.setData(descriptor);
            ObligationArgument[] arguments = descriptor
                    .getObligationArguments();
            if (arguments == null) {
                arguments = new ObligationArgument[0];
            }
            for (int i = 0, n = arguments.length; i < n; i++) {
                ObligationArgument argument = arguments[i];
                boolean isUserEditable = argument.isUserEditable();
                boolean isHidden = argument.isHidden();
                if (isHidden) {
                    continue;
                }
                Label label = new Label(root, SWT.NONE);
                label.setEnabled(getEnabled());
                String display = argument.getDisplayName();
                label.setText(display);
                String[] values = argument.getValues();
                List<String> args = (List<String>) obligation
                        .getCustomObligationArgs();
                int index = -1;
                for (int j = 0, m = args.size(); j < m; j += 2) {
                    if (args.get(j).equals(display)) {
                        index = j;
                        break;
                    }
                }
                String value = "";
                if (index != -1 && index + 1 < args.size()) {
                    value = (String) args.get(index + 1);
                }
                GridData data = new GridData(GridData.FILL_HORIZONTAL);
                if (value.length() > 100) {
                    data.widthHint = 400;
                }
                if (values == null || values.length == 0) {
                    Text text = new Text(root, SWT.BORDER);
                    text.setEnabled(getEnabled());
                    text.setText(value);
                    text.setLayoutData(data);
                    text.addModifyListener(modifyListener);
                } else {
                    if (values[0].equals(VALUE_TOKEN)) {
                        Control controls[] = root.getChildren();
                        String tagName = ((Combo) controls[1]).getText();
                        AttributeDescriptor des = getAttributeDescriptor(tagName);
                        
                        AttributeType type = (des != null) ? des.getType() : AttributeType.STRING;

                        if (type == AttributeType.BOOLEAN) {
                            Combo combo = new Combo(root, SWT.BORDER
                                    | SWT.READ_ONLY);
                            combo.setEnabled(getEnabled());
                            combo.setItems(new String[] { "Yes", "No" });
                            combo.setLayoutData(data);
                            combo.setText(value);
                            combo.addModifyListener(modifyListener);
                        } else if (type == AttributeType.DATE) {
                            CalendarPicker picker = new CalendarPicker(root,
                                    SWT.BORDER);
                            picker.setEnabled(getEnabled());
                            picker.setLayoutData(data);
                            Calendar calendar = GregorianCalendar.getInstance();
                            Date date = calendar.getTime();
                            try {
                                date = formatter.parse(value);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            calendar.setTime(date);
                            picker.setCalendar(calendar);
                            picker.addSelectionListener(selectionListener);
                        } else if (type == AttributeType.ENUM) {
                            List<String> options = getEnumAttribute(tagName);
                            Combo combo = new Combo(root, SWT.BORDER
                                    | SWT.READ_ONLY);
                            combo.setEnabled(getEnabled());
                            combo.setItems(options.toArray(new String[options
                                    .size()]));
                            combo.setLayoutData(data);
                            combo.setText(value);
                            combo.addModifyListener(modifyListener);
                        } else if (type == AttributeType.LONG) {
                            Text textValue = new Text(root, SWT.BORDER);
                            textValue.setEnabled(getEnabled());
                            textValue.setText(value);
                            textValue.setLayoutData(data);
                            textValue.addModifyListener(modifyListener);
                        } else if (type == AttributeType.STRING) {
                            Text textValue = new Text(root, SWT.BORDER);
                            textValue.setEnabled(getEnabled());
                            textValue.setText(value);
                            textValue.setLayoutData(data);
                            textValue.addModifyListener(modifyListener);
                        }
                    } else if (values[0].equals(VALUES_TOKEN)) {
                        String convertedValue = convertStringToComma(value);
                        Text textValue = new Text(root, SWT.BORDER);
                        textValue.setEnabled(false);
                        textValue.setText(convertedValue);
                        textValue.setLayoutData(data);
                    } else {
                        Combo combo = null;
                        if (isUserEditable) {
                            combo = new Combo(root, SWT.BORDER);
                        } else {
                            combo = new Combo(root, SWT.BORDER | SWT.READ_ONLY);
                        }
                        combo.setEnabled(getEnabled());
                        combo.setLayoutData(data);
                        if (values[0].equals(ATTRIBUTE_TOKEN)) {
                            List<AttributeDescriptor> list = new ArrayList<AttributeDescriptor>(
                                    getAttributes());
                            values = new String[list.size()];
                            for (int j = 0, m = list.size(); j < m; j++) {
                                values[j] = list.get(j).getDisplayName();
                            }
                        }
                        combo.setItems(values);
                        combo.setText(value);
                        combo.addModifyListener(modifyListener);
                    }
                }
            }
            setBackground(getBackground());
        }

        public CustomObligation getObligation() {
            return obligation;
        }

        /**
         * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
         */
        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
        }

        @Override
        public void setBackground(Color color) {
            super.setBackground(color);
            for (Control child : getChildren()) {
                setControlBackground(child, color);
            }
        }

        private void setControlBackground(Control control, Color color) {
            control.setBackground(color);
            if (control instanceof Composite) {
                Composite composite = (Composite) control;
                for (Control child : composite.getChildren()) {
                    setControlBackground(child, color);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private Collection<AttributeDescriptor> getAttributes() {
        if (attributes == null) {
            attributes = PolicyServerProxy
                    .getCustomAttributes(EntityType.RESOURCE);
        }
        List<AttributeDescriptor> descriptors = new ArrayList<AttributeDescriptor>();
        for (AttributeDescriptor descriptor : attributes) {
            String group = descriptor.getGroupName();
            if (group == null || !group.equals("With Content")) {
                descriptors.add(descriptor);
            }

        }
        return descriptors;
    }

    private String getDefaultAttribute() {
        List<AttributeDescriptor> list = new ArrayList<AttributeDescriptor>(
                getAttributes());
        if (list.size() == 0) {
            return "";
        }
        return list.get(0).getDisplayName();
    }

    private List<String> getEnumAttribute(String name) {
        List<AttributeDescriptor> list = new ArrayList<AttributeDescriptor>(
                getAttributes());
        for (int i = 0, n = list.size(); i < n; i++) {
            AttributeDescriptor descriptor = list.get(i);
            if (descriptor.getDisplayName().equals(name)) {
                if (descriptor.getType() == AttributeType.ENUM) {
                    return descriptor.getEnumeratedValues();
                }
            }
        }
        return null;
    }

    private String getAttributes(String name) {
        List<String> attributes = getEnumAttribute(name);
        if (attributes == null) {
            return "";
        } else {
            String result = "";
            for (int i = 0, n = attributes.size(); i < n; i++) {
                String item = attributes.get(i);
                result += item;
                if (i + 1 != n) {
                    result += ";";
                }
            }
            return result;
        }
    }

    private String convertStringToComma(String name) {
        // return name.replace(';', ',');
        return name;
    }

    private AttributeDescriptor getAttributeDescriptor(String name) {
        List<AttributeDescriptor> list = new ArrayList<AttributeDescriptor>(
                getAttributes());
        for (int i = 0, n = list.size(); i < n; i++) {
            AttributeDescriptor descriptor = list.get(i);
            if (descriptor.getDisplayName().equals(name)) {
                return descriptor;
            }
        }
        return null;
    }

    private Calendar getDefaultDateExpression() {
        Calendar d = new GregorianCalendar();
        d.set(Calendar.HOUR_OF_DAY, 0);
        d.set(Calendar.MINUTE, 0);
        d.set(Calendar.SECOND, 0);
        return d;
    }

    /**
     * get the position of tag name in the argument list
     * 
     * @param descriptor
     * @return -1 if not found, otherwise the position
     */
    private int getTagNamePosition(ObligationDescriptor descriptor) {
        int index = 0;
        for (ObligationArgument argument : descriptor.getObligationArguments()) {
            if (argument.isHidden()) {
                continue;
            }
            String[] values = argument.getValues();
            if (values != null && values.length >= 1
                    && values[0].equals(ATTRIBUTE_TOKEN)) {
                return index + 1;
            } else {
                index += 2;
            }
        }
        return -1;
    }

    /**
     * get the position of tag value in the argument list
     * 
     * @param descriptor
     * @return -1 if not found, otherwise the position
     */
    private int getTagValuePosition(ObligationDescriptor descriptor) {
        int index = 0;
        for (ObligationArgument argument : descriptor.getObligationArguments()) {
            if (argument.isHidden()) {
                continue;
            }
            String[] values = argument.getValues();
            if (values != null
                    && values.length >= 1
                    && (values[0].equals(VALUE_TOKEN) || values[0]
                            .equals(VALUES_TOKEN))) {
                return index + 1;
            } else {
                index += 2;
            }
        }
        return -1;
    }

    private boolean isAutomaticTagging(ObligationDescriptor descriptor) {
        for (ObligationArgument argument : descriptor.getObligationArguments()) {
            if (argument.isHidden()) {
                continue;
            }
            String[] values = argument.getValues();
            if (values != null && values.length >= 1
                    && values[0].equals(VALUE_TOKEN)) {
                return true;
            }
        }
        return false;

    }

    /**
     * find the position of control in the container
     * 
     * @param container
     * @param sub
     * @return -1 if not found, otherwise the position
     */
    private int getSubControlPosition(Composite container, Control sub) {
        int index = 0;
        for (Control control : container.getChildren()) {
            if (control.equals(sub)) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
