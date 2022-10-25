package com.nextlabs.policystudio.editor.devicecontrol;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.editor.BasePolicyEditor;
import com.nextlabs.policystudio.editor.devicecontrol.EditorMessages;
import com.bluejungle.destiny.policymanager.editor.EditorPanel;
import com.bluejungle.destiny.policymanager.model.IClientEditorPanel;
import com.bluejungle.destiny.policymanager.model.IClientPolicy;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers.EffectTypeEnum;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

public class DeviceControlPolicyEditor extends BasePolicyEditor {

    // --Widgets--------
    private CompositionControl rsrcSrcComp;

    private Label 
            dateSectionLabel
          , separatorCondition;
    private Composite 
            resourceSection
          , enforcementDateSection
          , recurrentDateSection
          , enfDateLabel
          , recDateLabel;

    public DeviceControlPolicyEditor(IClientEditorPanel clientPanel, IClientPolicy clientPolicy) {
        super(clientPanel, clientPolicy);
    }

    @Override
    public void updateExtraFromDomainObject() {
        startTime.setupRow();
        endTime.setupRow();
        dailySchedule.setupRow();
        recurringEnforcement.setupRow();
        relayout();
    }

    @Override
    protected boolean hasApplicationSubject() {
        return false;
    }

    
    protected void updateObligation() {
        EffectTypeEnum effect = getEffect();

        if (effect == EffectTypeEnum.ALLOW) {
            FormData data = new FormData();
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.top = new FormAttachment(resourceSection, EditorPanel.SPACING);
            data.height = 0;
            data.width = 0;
            denyLabel.setLayoutData(data);
            denyObligations.setLayoutData(data);
            
            data = new FormData();
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.top = new FormAttachment(separatorObligation, EditorPanel.SPACING);
            data.height = 0;
            data.width = 0;
            denyLabel.setLayoutData(data);
            denyObligations.setLayoutData(data);

            data = new FormData();
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.top = new FormAttachment(separatorObligation, EditorPanel.SPACING);
            data.width = LABEL_COLUMN_WIDTH;
            data.bottom = new FormAttachment(allowObligations, 0, SWT.BOTTOM);
            allowLabel.setLayoutData(data);
            addSectionFormData(allowObligations, allowLabel, separatorObligation);
        } else {
            dateSectionLabel.setVisible(true);
            separatorCondition.setVisible(true);
            // connectionTypeSection.setVisible(true);
            heartbeatLabel.setVisible(true);
            enforcementDateSection.setVisible(true);
            recurrentDateSection.setVisible(true);
            // connectionTypeLabel.setVisible(true);
            enfDateLabel.setVisible(true);
            recDateLabel.setVisible(true);
            advancedConditionLabel.setVisible(true);
            advancedConditionSection.setVisible(true);

            FormData data = new FormData();
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.top = new FormAttachment(resourceSection, EditorPanel.SPACING);
            dateSectionLabel.setLayoutData(data);

            data = new FormData();
            data.top = new FormAttachment(dateSectionLabel, 2);
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.right = new FormAttachment(100, -EditorPanel.SIDE_SPACING);
            separatorCondition.setLayoutData(data);

            data = new FormData();
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.top = new FormAttachment(separatorCondition, EditorPanel.SPACING);
            data.width = LABEL_COLUMN_WIDTH;
            data.bottom = new FormAttachment(heartbeatSection, 0, SWT.BOTTOM);
            heartbeatLabel.setLayoutData(data);
            addSectionFormData(heartbeatSection, heartbeatLabel, separatorCondition);

            data = new FormData();
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.top = new FormAttachment(heartbeatLabel, EditorPanel.SPACING);
            data.width = LABEL_COLUMN_WIDTH;
            data.bottom = new FormAttachment(enforcementDateSection, 0, SWT.BOTTOM);
            enfDateLabel.setLayoutData(data);
            addSectionFormData(enforcementDateSection, enfDateLabel, heartbeatLabel);

            data = new FormData();
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.top = new FormAttachment(enfDateLabel, EditorPanel.SPACING);
            data.width = LABEL_COLUMN_WIDTH;
            data.bottom = new FormAttachment(recurrentDateSection, 0, SWT.BOTTOM);
            recDateLabel.setLayoutData(data);
            addSectionFormData(recurrentDateSection, recDateLabel, enfDateLabel);

            // extendedCondition
            data = new FormData();
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.top = new FormAttachment(recurrentDateSection, EditorPanel.SPACING);
            data.width = LABEL_COLUMN_WIDTH;
            data.bottom = new FormAttachment(advancedConditionSection, 0, SWT.BOTTOM);
            advancedConditionLabel.setLayoutData(data);
            addSectionFormData(advancedConditionSection, advancedConditionLabel, recurrentDateSection);
            
            denyLabel.setVisible(true);
            denyObligations.setVisible(true);

            data = new FormData();
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.top = new FormAttachment(separatorObligation, EditorPanel.SPACING);
            data.width = LABEL_COLUMN_WIDTH;
            data.bottom = new FormAttachment(denyObligations, 0, SWT.BOTTOM);
            denyLabel.setLayoutData(data);
            addSectionFormData(denyObligations, denyLabel, separatorObligation);

            data = new FormData();
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.top = new FormAttachment(denyLabel, EditorPanel.SPACING);
            data.width = LABEL_COLUMN_WIDTH;
            data.bottom = new FormAttachment(allowObligations, 0, SWT.BOTTOM);
            allowLabel.setLayoutData(data);
            addSectionFormData(allowObligations, allowLabel, denyLabel);
        }
    }
    
    protected String getEnforcementSectionLabelText() {
        return EditorMessages.POLICYEDITOR_ENFORCEMENT;
    }
    
    protected String getBodySectionLabelText() {
        return EditorMessages.ATTACH_FOLLOWING_RESOURCE;
    }

    protected Control initializeBody() {
        Control lastAttachment = super.initializeBody();

        
        resourceSection = new Composite(bodyComposite, SWT.NONE);
        Composite resourceLabel = getPartLabel(EditorMessages.DEVICE, bodyComposite, lastAttachment, resourceSection);
        addSectionFormData(resourceSection, resourceLabel, lastAttachment);

        initializeResources(resourceSection);

        // Conditions
        dateSectionLabel = new Label(bodyComposite, SWT.NONE);
        dateSectionLabel.setText(EditorMessages.POLICYEDITOR_CONDITIONS);
        dateSectionLabel.setBackground(panel.getBackground());
        dateSectionLabel.setForeground(ColorBundle.CE_MED_BLUE);
        FormData sectionLabelData = new FormData();
        sectionLabelData.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        sectionLabelData.top = new FormAttachment(resourceSection, EditorPanel.SPACING);
        dateSectionLabel.setLayoutData(sectionLabelData);

        separatorCondition = createBodySeparator(dateSectionLabel);

        Composite heartbeatAttachment = initializeHeartbeat(bodyComposite, separatorCondition);
        
        enforcementDateSection = new Composite(bodyComposite, SWT.NONE);
        enfDateLabel = getPartLabel(EditorMessages.POLICYEDITOR_DATETIME, bodyComposite, heartbeatAttachment, enforcementDateSection);
        addSectionFormData(enforcementDateSection, enfDateLabel, heartbeatAttachment);

        initializeEnforcementDateSection(enforcementDateSection);

        recurrentDateSection = new Composite(bodyComposite, SWT.NONE);
        recDateLabel = getPartLabel(EditorMessages.POLICYEDITOR_RECURRENCE, bodyComposite, enfDateLabel, recurrentDateSection);
        addSectionFormData(recurrentDateSection, recDateLabel, enfDateLabel);

        initializeRecurrentDateSection(recurrentDateSection);
        
        advancedConditionSection = new Composite(bodyComposite, SWT.NONE);
        lastAttachment = initializeAdvancedCondition(advancedConditionSection, recDateLabel);
        
        return lastAttachment;
    }

    private void initializeResources(Composite resourceSection) {
        Color background = panel.getBackground();
        resourceSection.setBackground(background);
        RowLayout layout = new RowLayout(SWT.VERTICAL);
        layout.marginLeft = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        resourceSection.setLayout(layout);

        rsrcSrcComp = new CompositionControl(
                resourceSection
              , SWT.NONE
              , EditorMessages.POLICYEDITOR_RESOURCE_COMPONENT
              , ""
              , getControlDomainObject(ControlId.DOC_SRC, clientPolicy)
              , panel.getEditorPanel()
              , ControlId.DOC_SRC.ordinal()
              , panel.isEditable()
              , false
              , SpecType.RESOURCE
              , "DEVICE"
              , null
        );
        rsrcSrcComp.setBackground(background);
    }

    protected String getExceptionSectionLabelText(){
    	return EditorMessages.POLICYEDITOR_EXCEPTIONS;
    }
    

    protected void relayout() {
        if (!panel.canRelayout()) {
            return;
        }

        panel.relayout();
        subjectComposite.redraw();
        bodyComposite.redraw();
        obligationsComposite.redraw();
        Control[] controls = bodyComposite.getChildren();
        for (Control control : controls) {
            control.redraw();
        }
        exceptionsComposite.redraw();
    }

    @Override
    public void relayoutContents() {
        userComp.relayout();
        hostComp.relayout();

        // actionComp.relayout();
        rsrcSrcComp.relayout();
        // rsrcTgtComp.relayout();
        exceptionComp.relayout();
        obligationsComposite.layout(true, true);
    }

    protected String getPolicyType() {
        return "Document Policy";
    }

}
