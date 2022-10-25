package com.nextlabs.policystudio.editor.communication;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.editor.BasePolicyEditor;
import com.bluejungle.destiny.policymanager.editor.EditorMessages;
import com.bluejungle.destiny.policymanager.editor.EditorPanel;
import com.bluejungle.destiny.policymanager.model.IClientEditorPanel;
import com.bluejungle.destiny.policymanager.model.IClientPolicy;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers.EffectTypeEnum;
import com.bluejungle.destiny.policymanager.ui.controls.CompositionControl;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

public class CommunicationPolicyEditor extends BasePolicyEditor {

    private CompositionControl 
            rsrcSrcComp
          , subjectComp;

    private Label 
            dateSectionLabel
          , separatorCondition;
    
    private Composite 
            connectionTypeSection
          , enforcementDateSection
          , recurrentDateSection;
    
    private Composite
            connectionTypeLabel
          , enfDateLabel
          , recDateLabel;

    public CommunicationPolicyEditor(IClientEditorPanel clientPanel, IClientPolicy clientPolicy) {
        super(clientPanel
              , clientPolicy
              , Arrays.asList(
                   ControlId.USERS
                 , ControlId.HOSTS
                 , ControlId.APPLICATIONS
                 , ControlId.ACTIONS
                 , ControlId.DOC_SRC
                 , ControlId.DATE
                 , ControlId.SUBJECT
                 , ControlId.POLICY)
        );
    }


    @Override
    public void updateExtraFromDomainObject() {
        startTime.setupRow();
        endTime.setupRow();
        dailySchedule.setupRow();
        recurringEnforcement.setupRow();
        updateConnectionType();
    }
    
    protected String getSubjectSectionLabelText() {
        return EditorMessages.COMMUNICATIONPOLICYEDITOR_COMMUNICATION_BETWEEN_SENDER;
    }
    
    protected String getEnforcementSectionLabelText() {
        return EditorMessages.POLICYEDITOR_ENFORCEMENT;
    }
    
    protected String getBodySectionLabelText() {
        return EditorMessages.COMMUNICATIONPOLICYEDITOR_AND_RECIPIENTS;
    }

    protected Control initializeBody() {
        Control lastAttachment = super.initializeBody();

        // And Recipients

        subjectComp = new CompositionControl(
                bodyComposite
              , SWT.NONE
              , EditorMessages.POLICYEDITOR_USER_COMPONENT
              , ""
              , getControlDomainObject(ControlId.SUBJECT, clientPolicy)
              , panel.getEditorPanel()
              , ControlId.SUBJECT.ordinal()
              , panel.isEditable()
              , false
              , SpecType.USER
              , "USER"
              , null
        );
        subjectComp.setBackground(panel.getBackground());
        Composite subjectLabel = getPartLabel(
                EditorMessages.POLICYEDITOR_USER
              , bodyComposite
              , lastAttachment
              , subjectComp
        );
        addSectionFormData(subjectComp, subjectLabel, lastAttachment);

        // Using Channel
        Label label = new Label(bodyComposite, SWT.NONE);
        label.setText(EditorMessages.COMMUNICATIONPOLICYEDITOR_USING_CHANNEL);
        label.setBackground(panel.getBackground());
        label.setForeground(ColorBundle.CE_MED_BLUE);
        FormData data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        data.top = new FormAttachment(subjectComp, EditorPanel.SPACING);
        label.setLayoutData(data);

        Label separator = createBodySeparator(label);
        
        lastAttachment = initializeAction(separator);

        // With Message or Attachment
        label = new Label(bodyComposite, SWT.NONE);
        label.setText(EditorMessages.COMMUNICATIONPOLICYEDITOR_WITH_MESSAGE_OR_ATTACHMENT);
        label.setBackground(panel.getBackground());
        label.setForeground(ColorBundle.CE_MED_BLUE);
        data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        data.top = new FormAttachment(actionComp, EditorPanel.SPACING);
        label.setLayoutData(data);

        separator = createBodySeparator(label);

        rsrcSrcComp = new CompositionControl(
                bodyComposite
              , SWT.NONE
              , EditorMessages.POLICYEDITOR_RESOURCE_COMPONENT
              , ""
              , getControlDomainObject(ControlId.DOC_SRC, clientPolicy)
              , panel.getEditorPanel()
              , ControlId.DOC_SRC.ordinal()
              , panel.isEditable()
              , false
              , SpecType.RESOURCE
              , "RESOURCE"
              , null
        );
        rsrcSrcComp.setBackground(panel.getBackground());
        Composite resourceLabel = getPartLabel(
                EditorMessages.COMMUNICATIONPOLICYEDITOR_DOCUMENT
              , bodyComposite
              , separator
              , rsrcSrcComp
        );
        addSectionFormData(rsrcSrcComp, resourceLabel, separator);

        // Conditions
        dateSectionLabel = new Label(bodyComposite, SWT.NONE);
        dateSectionLabel.setText(EditorMessages.POLICYEDITOR_CONDITIONS);
        dateSectionLabel.setBackground(panel.getBackground());
        dateSectionLabel.setForeground(ColorBundle.CE_MED_BLUE);
        data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        data.top = new FormAttachment(rsrcSrcComp, EditorPanel.SPACING);
        dateSectionLabel.setLayoutData(data);

        separatorCondition = createBodySeparator(dateSectionLabel);

        connectionTypeSection = new Composite(bodyComposite, SWT.NONE);
        connectionTypeLabel = getPartLabel(
                EditorMessages.POLICYEDITOR_CONNECTION_TYPE
              , bodyComposite
              , separatorCondition
              , connectionTypeSection
        );
        addSectionFormData(connectionTypeSection, connectionTypeLabel, separatorCondition);

        initializeConnectionTypeSection(connectionTypeSection);

        Composite heartbeatAttachment = initializeHeartbeat(bodyComposite, connectionTypeLabel);

        enforcementDateSection = new Composite(bodyComposite, SWT.NONE);
        enfDateLabel = getPartLabel(
                EditorMessages.POLICYEDITOR_DATETIME
              , bodyComposite
              , heartbeatAttachment
              , enforcementDateSection
        );
        addSectionFormData(enforcementDateSection, enfDateLabel, heartbeatAttachment);

        initializeEnforcementDateSection(enforcementDateSection);

        recurrentDateSection = new Composite(bodyComposite, SWT.NONE);
        recDateLabel = getPartLabel(
                EditorMessages.POLICYEDITOR_RECURRENCE
              , bodyComposite
              , enfDateLabel
              , recurrentDateSection
        );
        addSectionFormData(recurrentDateSection, recDateLabel, enfDateLabel);

        initializeRecurrentDateSection(recurrentDateSection);
        
        advancedConditionSection = new Composite(bodyComposite, SWT.NONE);
        lastAttachment = initializeAdvancedCondition(advancedConditionSection, recDateLabel);
        
        return lastAttachment;
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
        Control[] controls = bodyComposite.getChildren();
        for (Control control : controls) {
            control.redraw();
        }
        exceptionsComposite.redraw();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bluejungle.destiny.policymanager.editor.EditorPanel#relayoutContents
     * ()
     */
    @Override
    public void relayoutContents() {
        userComp.relayout();
        hostComp.relayout();
        if (!isAccess()) {
            appComp.relayout();
        }
        actionComp.relayout();
        rsrcSrcComp.relayout();
        // rsrcTgtComp.relayout();
        subjectComp.relayout();        
        exceptionComp.relayout();
    }

    protected String getPolicyType() {
        return "Communication Policy";
    }

    protected void updateObligation() {
        EffectTypeEnum effect = getEffect();

        if (effect == EffectTypeEnum.ALLOW) {
            FormData data = new FormData();
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.top = new FormAttachment(rsrcSrcComp, EditorPanel.SPACING);
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
            connectionTypeSection.setVisible(true);
            heartbeatLabel.setVisible(true);
            enforcementDateSection.setVisible(true);
            recurrentDateSection.setVisible(true);
            connectionTypeLabel.setVisible(true);
            enfDateLabel.setVisible(true);
            recDateLabel.setVisible(true);
            advancedConditionLabel.setVisible(true);
            advancedConditionSection.setVisible(true);

            FormData data = new FormData();
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.top = new FormAttachment(rsrcSrcComp, EditorPanel.SPACING);
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
            data.bottom = new FormAttachment(connectionTypeSection, 0, SWT.BOTTOM);
            connectionTypeLabel.setLayoutData(data);
            addSectionFormData(connectionTypeSection, connectionTypeLabel, separatorCondition);

            data = new FormData();
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.top = new FormAttachment(connectionTypeLabel, EditorPanel.SPACING);
            data.width = LABEL_COLUMN_WIDTH;
            data.bottom = new FormAttachment(heartbeatSection, 0, SWT.BOTTOM);
            heartbeatLabel.setLayoutData(data);
            addSectionFormData(heartbeatSection, heartbeatLabel, connectionTypeLabel);

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
}
