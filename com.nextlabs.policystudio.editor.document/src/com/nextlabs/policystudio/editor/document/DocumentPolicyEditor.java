package com.nextlabs.policystudio.editor.document;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.RowLayout;
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

public class DocumentPolicyEditor extends BasePolicyEditor {

    private CompositionControl rsrcSrcComp;
    private CompositionControl rsrcTgtComp;

    private Label dateSectionLabel, separatorCondition;
    
    private Composite 
            resourceSection
          , connectionTypeSection
          , enforcementDateSection
          , recurrentDateSection;
            
    private Composite            
            connectionTypeLabel
          , enfDateLabel
          , recDateLabel;

    public DocumentPolicyEditor(IClientEditorPanel clientPanel, IClientPolicy clientPolicy) {
        super(clientPanel, clientPolicy);
    }

    @Override
    public void updateExtraFromDomainObject() {
        startTime.setupRow();
        endTime.setupRow();
        dailySchedule.setupRow();
        recurringEnforcement.setupRow();
        updateConnectionType();
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
           data.top = new FormAttachment(separatorObligation,EditorPanel.SPACING);
           data.height = 0;
           data.width = 0;
           denyLabel.setLayoutData(data);
           denyObligations.setLayoutData(data);

           data = new FormData();
           data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
           data.top = new FormAttachment(separatorObligation,EditorPanel.SPACING);
           data.width = LABEL_COLUMN_WIDTH;
           data.bottom = new FormAttachment(allowObligations, 0, SWT.BOTTOM);
           allowLabel.setLayoutData(data);
           addSectionFormData(allowObligations, allowLabel,separatorObligation);
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
           data.top = new FormAttachment(resourceSection, EditorPanel.SPACING);
           dateSectionLabel.setLayoutData(data);

           data = new FormData();
           data.top = new FormAttachment(dateSectionLabel, 2);
           data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
           data.right = new FormAttachment(100, -EditorPanel.SIDE_SPACING);
           separatorCondition.setLayoutData(data);

           data = new FormData();
           data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
           data.top = new FormAttachment(separatorCondition,EditorPanel.SPACING);
           data.width = LABEL_COLUMN_WIDTH;
           data.bottom = new FormAttachment(connectionTypeSection, 0,SWT.BOTTOM);
           connectionTypeLabel.setLayoutData(data);
           addSectionFormData(connectionTypeSection, connectionTypeLabel,separatorCondition);

           data = new FormData();
           data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
           data.top = new FormAttachment(connectionTypeLabel, EditorPanel.SPACING);
           data.width = LABEL_COLUMN_WIDTH;
           data.bottom = new FormAttachment(heartbeatSection, 0, SWT.BOTTOM);
           heartbeatLabel.setLayoutData(data);
           addSectionFormData(heartbeatSection, heartbeatLabel,connectionTypeLabel);

           data = new FormData();
           data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
           data.top = new FormAttachment(heartbeatLabel, EditorPanel.SPACING);
           data.width = LABEL_COLUMN_WIDTH;
           data.bottom = new FormAttachment(enforcementDateSection, 0,SWT.BOTTOM);
           enfDateLabel.setLayoutData(data);
           addSectionFormData(enforcementDateSection, enfDateLabel,heartbeatLabel);

           data = new FormData();
           data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
           data.top = new FormAttachment(enfDateLabel, EditorPanel.SPACING);
           data.width = LABEL_COLUMN_WIDTH;
           data.bottom = new FormAttachment(recurrentDateSection, 0,SWT.BOTTOM);
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
           data.top = new FormAttachment(separatorObligation,EditorPanel.SPACING);
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
        return EditorMessages.POLICYEDITOR_PERFORM_THE_FOLLOWING;
    }

    protected Control initializeBody() {
        Control lastAttachment = super.initializeBody();

        lastAttachment = initializeAction(lastAttachment);

        // On Resources
        Label onResourceLabel = new Label(bodyComposite, SWT.NONE);
        onResourceLabel.setText(EditorMessages.POLICYEDITOR_ON_RESOURCES);
        onResourceLabel.setBackground(panel.getBackground());
        onResourceLabel.setForeground(ColorBundle.CE_MED_BLUE);
        FormData sectionLabelData = new FormData();
        sectionLabelData.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        sectionLabelData.top = new FormAttachment(lastAttachment, EditorPanel.SPACING);
        onResourceLabel.setLayoutData(sectionLabelData);

        Label separator = createBodySeparator(onResourceLabel);

        resourceSection = new Composite(bodyComposite, SWT.NONE);
        Composite resourceLabel = getPartLabel(
                EditorMessages.POLICYEDITOR_TARGET
              , bodyComposite
              , separator
              , resourceSection
        );
        addSectionFormData(resourceSection, resourceLabel, separator);

        initializeResources(resourceSection);

        // Conditions
        dateSectionLabel = new Label(bodyComposite, SWT.NONE);
        dateSectionLabel.setText(EditorMessages.POLICYEDITOR_CONDITIONS);
        dateSectionLabel.setBackground(panel.getBackground());
        dateSectionLabel.setForeground(ColorBundle.CE_MED_BLUE);
        sectionLabelData = new FormData();
        sectionLabelData.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        sectionLabelData.top = new FormAttachment(resourceSection, EditorPanel.SPACING);
        dateSectionLabel.setLayoutData(sectionLabelData);

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
              , "RESOURCE"
              , null
        );
        rsrcSrcComp.setBackground(background);

        Label tgtLabel = new Label(resourceSection, SWT.NONE);
        tgtLabel.setText(EditorMessages.POLICYEDITOR_MOVED_RENAMED_COPIED);
        tgtLabel.setEnabled(panel.isEditable());
        tgtLabel.setBackground(background);

        rsrcTgtComp = new CompositionControl(
                resourceSection
              , SWT.NONE
              , EditorMessages.POLICYEDITOR_RESOURCE_COMPONENT
              , ""
              , getControlDomainObject(ControlId.DOC_TARGET, clientPolicy)
              , panel.getEditorPanel()
              , ControlId.DOC_TARGET.ordinal()
              , panel.isEditable()
              , false
              , true
              , new String[] { EditorMessages.POLICYEDITOR_INTO, EditorMessages.POLICYEDITOR_OUTSIDE }
              , SpecType.RESOURCE
              , "RESOURCE"
              , null
        );
        rsrcTgtComp.setBackground(background);
    }

    

    protected void relayout() {
        if (!panel.canRelayout()) {
            return;
        }

        panel.relayout();
        subjectComposite.redraw();
        bodyComposite.redraw();
        exceptionsComposite.redraw();
        obligationsComposite.redraw();
        Control[] controls = bodyComposite.getChildren();
        for (Control control : controls) {
            control.redraw();
        }
    }

    @Override
    public void relayoutContents() {
        userComp.relayout();
        hostComp.relayout();
        if (!isAccess()) {
            appComp.relayout();
        }
        actionComp.relayout();
        rsrcSrcComp.relayout();
        rsrcTgtComp.relayout();
        exceptionComp.relayout();
        obligationsComposite.layout(true, true);
    }
    
}
