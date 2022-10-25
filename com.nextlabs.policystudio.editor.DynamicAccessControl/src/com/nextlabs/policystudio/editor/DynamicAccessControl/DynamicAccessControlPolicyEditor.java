/**
 * Created on July, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 * @author ichiang
 * 
 */

package com.nextlabs.policystudio.editor.DynamicAccessControl;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.bluejungle.destiny.policymanager.editor.BasePolicyEditor;
import com.bluejungle.destiny.policymanager.editor.CustomObligationEditor;
import com.bluejungle.destiny.policymanager.editor.EditorMessages;
import com.bluejungle.destiny.policymanager.editor.EditorPanel;
import com.bluejungle.destiny.policymanager.editor.LogObligationEditor;
import com.bluejungle.destiny.policymanager.editor.SendEmailObligationEditor;
import com.bluejungle.destiny.policymanager.editor.SendMessageObligationEditor;
import com.bluejungle.destiny.policymanager.model.IClientEditorPanel;
import com.bluejungle.destiny.policymanager.model.IClientPolicy;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElement;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElementOp;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers.EffectTypeEnum;
import com.bluejungle.destiny.policymanager.ui.controls.CompositionControl;
import com.bluejungle.destiny.policymanager.ui.controls.ExceptionCompositionControl;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

public class DynamicAccessControlPolicyEditor extends BasePolicyEditor {

    private CompositionControl 
    		rsrcSrcComp
    	  , rsrcTgtComp
    	  , userComp
    	  , hostComp
    	  , appComp;
    	  

    private Label 
            separatorCondition
    	  , dateSectionLabel
    	  , subjectSectionLabel
    	  , onResourceLabel
    	  , separator;
  
    private Composite 
    		resourceSection
          , connectionTypeSection
          , enforcementDateSection
          , recurrentDateSection
          , subjectComposite;
    
    private Composite            
            connectionTypeLabel
          , enfDateLabel
          , recDateLabel;
       
    public DynamicAccessControlPolicyEditor(IClientEditorPanel clientPanel, IClientPolicy clientPolicy) {
        super(clientPanel, clientPolicy);
    }
    
    protected String getEnforcementSectionLabelText() {
        return EditorMessages.POLICYEDITOR_ENFORCEMENT;
    }
    @Override
    public void initializeContents() {
    	DACTypeEnum type = getDACtype ();
    	if(type == DACTypeEnum.CAP){
            initializeExceptions();
    	}else{
	        initializeSubject();
	        initializeBody();
	        initializeObligations();
    	}
    	panel.getScrolledComposite().setMinSize(panel.getLeftComposite().computeSize(SWT.DEFAULT,SWT.DEFAULT));
    }

    @Override
    public void updateExtraFromDomainObject() {
        startTime.setupRow();
        endTime.setupRow();
        dailySchedule.setupRow();
        recurringEnforcement.setupRow();
        updateConnectionType();
    }
  
    @Override
    protected void updateObligation() {
    	connectionTypeSection.setVisible(false);
        heartbeatLabel.setVisible(false);
        enforcementDateSection.setVisible(false);
        recurrentDateSection.setVisible(false);
        connectionTypeLabel.setVisible(false);
        enfDateLabel.setVisible(false);
        recDateLabel.setVisible(false);
        advancedConditionLabel.setVisible(true);
        advancedConditionSection.setVisible(true);
        denyLabel.setVisible(false);
        denyObligations.setVisible(false);
            
        FormData data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        data.top = new FormAttachment(resourceSection, EditorPanel.SPACING);
        data.height = 0;
        data.width = 0;

        connectionTypeSection.setLayoutData(data);
        heartbeatSection.setLayoutData(data);
        enforcementDateSection.setLayoutData(data);
        recurrentDateSection.setLayoutData(data);
        connectionTypeLabel.setLayoutData(data);
        heartbeatLabel.setLayoutData(data);
        enfDateLabel.setLayoutData(data);
        recDateLabel.setLayoutData(data);
        denyLabel.setLayoutData(data);
        denyObligations.setLayoutData(data);
           
        data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        data.top = new FormAttachment(resourceSection, EditorPanel.SPACING);
        dateSectionLabel.setLayoutData(data);
        dateSectionLabel.setVisible(true);
          
        data = new FormData();
        data.top = new FormAttachment(dateSectionLabel, 2);
        data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        data.right = new FormAttachment(100, -EditorPanel.SIDE_SPACING);
        separatorCondition.setLayoutData(data);
        separatorCondition.setVisible(true);
        data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        data.top = new FormAttachment(separatorCondition, EditorPanel.SPACING);
        data.width = LABEL_COLUMN_WIDTH;
        data.bottom = new FormAttachment(advancedConditionSection, 0, SWT.BOTTOM);
        advancedConditionLabel.setLayoutData(data);
        addSectionFormData(advancedConditionSection, advancedConditionLabel, separatorCondition);      
           
        data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        data.top = new FormAttachment(separatorObligation, EditorPanel.SPACING);
        data.width = LABEL_COLUMN_WIDTH;
        data.bottom = new FormAttachment(allowObligations, 0, SWT.BOTTOM);
        allowLabel.setLayoutData(data);
        addSectionFormData(allowObligations, allowLabel, separatorObligation);
    }
    
	/*
	 * <DAC Policy type>
	 * By knowing the policy level, we can seperate the DAC editor to two different layout
	 */
	protected static enum DACTypeEnum {
        CAP
      , CAR
    }
	
	protected DACTypeEnum getDACtype (){
		Policy policy = (Policy) (clientPolicy);
		boolean isCARtype = policy.hasAttribute(IPolicy.EXCEPTION_ATTRIBUTE);
		if(isCARtype){
			return DACTypeEnum.CAR;
		}
		return DACTypeEnum.CAP;
	}    
	/*
	 * </DAC Policy type>
	 */
 
	/*
	 * <Policy effects mapping for DAC policy>
	 */
    protected String[] EFFECTS = {
	        EditorMessages.POLICYEDITOR_ALLOW
  	    };
    
    public static final Map<EffectTypeEnum, Integer> effectTypeToIndexMap;
    public static final Map<Integer, EffectTypeEnum> indexToEffectTypeMap;
    public static final Map<EffectTypeEnum, String>  effectTypeToStringMap;
    
	static {
	    effectTypeToIndexMap = new HashMap<EffectTypeEnum, Integer>();
	    indexToEffectTypeMap = new HashMap<Integer, EffectTypeEnum>();
	    effectTypeToStringMap = new HashMap<EffectTypeEnum, String> ();
	    
	    effectTypeToIndexMap.put(EffectTypeEnum.ALLOW_ONLY, 0);
	    indexToEffectTypeMap.put(0, EffectTypeEnum.ALLOW_ONLY);
	    effectTypeToStringMap.put(EffectTypeEnum.ALLOW_ONLY, "ALLOW ONLY");
	}
	/*
	 * </Policy effects mapping for DAC policy>
	 */

    /*
     * <effect>
     */
    protected String getSubjectSectionLabelText() {
        return EditorMessages.POLICYEDITOR_SUBJECT;
    }
    
	public static int getIndexFromEffectTypeEnum (EffectTypeEnum effectTypeEnum){
		return effectTypeToIndexMap.get(effectTypeEnum);
	}
    @Override
	protected EffectTypeEnum getEffectTypeEnumFromIndex (int index){
		return indexToEffectTypeMap.get(index);
	}

    @Override
    protected void setComboForEffect(Combo combo) {
        EffectTypeEnum effectTypeEnum = getEffect();
        Integer index = getIndexFromEffectTypeEnum(effectTypeEnum);
        
        if (index != null) {
        	combo.select(index);
        }
    }	
    /*
     * </effect>
     */
    
    /*
     *<Exception> 
     */
    
    protected Composite exceptionsComposite;
    protected Label exceptionsSectionLabel, exceptionsSeparator;
    protected ExceptionCompositionControl exceptionComp;
  
    protected void initializeExceptions() {
    	exceptionsComposite = panel.addLeftEditorSectionComposite();
        FormLayout exceptionLayout = new FormLayout();
        exceptionsComposite.setLayout(exceptionLayout);

        exceptionsSectionLabel = new Label(exceptionsComposite, SWT.NONE);
        exceptionsSectionLabel.setText(getExceptionSectionLabelText());
        exceptionsSectionLabel.setBackground(panel.getBackground());
        exceptionsSectionLabel.setForeground(ColorBundle.CE_MED_BLUE);
        
        FormData sectionLabelData = new FormData();
        sectionLabelData.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        sectionLabelData.top = new FormAttachment(0, EditorPanel.TOP_SPACKING);
        exceptionsSectionLabel.setLayoutData(sectionLabelData);

        exceptionsSeparator = createSeparator(exceptionsComposite, exceptionsSectionLabel);
        String type = clientPolicy.getAttributes().toString();
                exceptionComp = new ExceptionCompositionControl(
        		exceptionsComposite
                , SWT.NONE
                , EditorMessages.POLICYEDITOR_EXCEPTION_POLICIES
                , ""
                , getException(ControlId.POLICY)
                , panel.getEditorPanel()
                , ControlId.POLICY.ordinal()
                , panel.isEditable()
                , false
                , true
                , new String[] {}
                , SpecType.ILLEGAL
                , "POLICY"
                , null
            );  
        exceptionComp.setBackground(panel.getBackground());
        
        Composite exceptionLabel = getPartLabel(
                EditorMessages.POLICYEDITOR_EXCEPTION_POLICIES_LABEL
              , exceptionsComposite
              , exceptionsSeparator
              , exceptionComp
        );
        addSectionFormData(exceptionComp, exceptionLabel, exceptionsSeparator);
        
    }
   
    
    /*
     *</Exception> 
     */ 
    
    @Override
    protected void initializeSubject() {
        subjectComposite = panel.addLeftEditorSectionComposite();
        FormLayout subjectLayout = new FormLayout();

        effectCombo = new Combo(subjectComposite, SWT.READ_ONLY);
        effectCombo.setItems(EFFECTS);
        effectCombo.setEnabled(panel.isEditable());
        effectCombo.setVisible(false);
        setComboForEffect(effectCombo);

        subjectSectionLabel = new Label(subjectComposite, SWT.NONE);
        subjectSectionLabel.setText(getSubjectSectionLabelText());
        subjectSectionLabel.setBackground(panel.getBackground());
        subjectSectionLabel.setForeground(ColorBundle.CE_MED_BLUE);
        FormData data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        data.top = new FormAttachment(0, EditorPanel.TOP_SPACKING);
        subjectSectionLabel.setLayoutData(data);

        Label subjectSectionSeparator = new Label(subjectComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        subjectSectionSeparator.setBackground(panel.getBackground());
        data= new FormData();
        data.top = new FormAttachment(subjectSectionLabel, 2);
        data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        data.right = new FormAttachment(100, -EditorPanel.SIDE_SPACING);
        subjectSectionSeparator.setLayoutData(data);

        userComp = new CompositionControl(
                subjectComposite
              , SWT.NONE
              , EditorMessages.POLICYEDITOR_USER_COMPONENT
              , ""
              , getControlDomainObject(ControlId.USERS, clientPolicy)
              , panel.getEditorPanel()
              , ControlId.USERS.ordinal()
              , panel.isEditable()
              , false
              , SpecType.USER
              , "USER"
              , null
        );
        userComp.setBackground(panel.getBackground());
        Composite userLabel = getPartLabel(
                EditorMessages.POLICYEDITOR_USER
              , subjectComposite
              , subjectSectionSeparator
              , userComp
        );
       
        data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        data.top = new FormAttachment(subjectSectionSeparator, EditorPanel.SPACING);
        data.width = LABEL_COLUMN_WIDTH;
        data.bottom = new FormAttachment(userComp, 0, SWT.BOTTOM);
        userLabel.setLayoutData(data);

        addSectionFormData(userComp, userLabel, subjectSectionSeparator);
      

        hostComp = new CompositionControl(
                subjectComposite
              , SWT.NONE
              , EditorMessages.POLICYEDITOR_COMPUTER_COMPONENT
              , ""
              , getControlDomainObject(ControlId.HOSTS, clientPolicy)
              , panel.getEditorPanel()
              , ControlId.HOSTS.ordinal()
              , panel.isEditable()
              , false
              , SpecType.HOST
              , "HOST"
              , null
        );
        hostComp.setBackground(panel.getBackground());
        Composite desktopLabel = getPartLabel(
                EditorMessages.POLICYEDITOR_COMPUTER
              , subjectComposite
              , userLabel
              , hostComp
        );
        
        data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        data.top = new FormAttachment(userLabel, EditorPanel.SPACING);
        data.width = LABEL_COLUMN_WIDTH;
        data.bottom = new FormAttachment(hostComp, 0, SWT.BOTTOM);
        desktopLabel.setLayoutData(data);
        addSectionFormData(hostComp, desktopLabel, userLabel);
  
        if (hasApplicationSubject() && !isAccess()) {
            // Applications are not used in access policies
            appComp = new CompositionControl(
                    subjectComposite
                  , SWT.NONE
                  , EditorMessages.POLICYEDITOR_APPLICATION_COMPONENT
                  , ""
                  , getControlDomainObject(ControlId.APPLICATIONS, clientPolicy)
                  , panel.getEditorPanel()
                  , ControlId.APPLICATIONS.ordinal()
                  , panel.isEditable()
                  , true
                  , SpecType.APPLICATION
                  , "APPLICATION"
                  , null
            );
            appComp.setBackground(panel.getBackground());
            appComp.setVisible(false);
            Composite appLabel = getPartLabel(
                    EditorMessages.POLICYEDITOR_APPLICATION
                  , subjectComposite
                  , desktopLabel
                  , appComp
            );
            appLabel.setVisible(false);
            
            data = new FormData();
            data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
            data.top = new FormAttachment(resourceSection, EditorPanel.SPACING);
            data.height = 0;
            data.width = 0;
            addSectionFormData(appComp, appLabel, desktopLabel);
            appLabel.setLayoutData(data);
            appComp.setLayoutData(data);

        }

        subjectComposite.setLayout(subjectLayout);
    }
    
    protected String getBodySectionLabelText() {
        return EditorMessages.POLICYEDITOR_PERFORM_THE_FOLLOWING;
    }
    

    protected Control initializeBody() {
        Control lastAttachment = super.initializeBody();
        lastAttachment = initializeAction(lastAttachment);
        
        
        // On Resources
        onResourceLabel = new Label(bodyComposite, SWT.NONE);
        onResourceLabel.setText(EditorMessages.POLICYEDITOR_ON_RESOURCES);
        onResourceLabel.setBackground(panel.getBackground());
        onResourceLabel.setForeground(ColorBundle.CE_MED_BLUE);
        FormData sectionLabelData = new FormData();
        sectionLabelData.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        sectionLabelData.top = new FormAttachment(lastAttachment, EditorPanel.SPACING);
        onResourceLabel.setLayoutData(sectionLabelData);    

        separator = createBodySeparator(onResourceLabel);

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
//        sectionLabelData.bottom = new FormAttachment(obligationsSectionLabel, EditorPanel.SPACING);
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
        heartbeatAttachment.setVisible(false);
        
        enforcementDateSection = new Composite(bodyComposite, SWT.NONE);
        enforcementDateSection.setVisible(false);
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
        lastAttachment = initializeAdvancedCondition(advancedConditionSection, separatorCondition);
        
        return lastAttachment;
    }
    
    protected String getExceptionSectionLabelText(){
    	return EditorMessages.POLICYEDITOR_EXCEPTIONS;
    }
    /*
     * <heartbeat>
     */
    private Button buttonAddHeartbeat, buttonRemoveHeartbeat;
    private Label labelTimeSinceLastHeartbeat;
    private Text textHeartbeatValue;
    
    protected Composite heartbeatSection;
    protected Composite heartbeatLabel;
    
    /**
     * return this attachment, so the next one can attach
     * @param parent
     * @param topAttachment
     * @return
     */
    protected Composite initializeHeartbeat(Composite parent, Control topAttachment) {
        heartbeatSection = new Composite(parent, SWT.NONE);
        heartbeatSection.setVisible(false);
        heartbeatLabel = getPartLabel(
                "Heartbeat"      // String labelName
              , parent           // Composite parent
              , topAttachment    // Control topAttachment
              , heartbeatSection // Control bottomAttachment
        );
        addSectionFormData(heartbeatSection, heartbeatLabel, topAttachment);
        heartbeatLabel.setVisible(false);
        initializeHeartbeatSection(heartbeatSection);
        return heartbeatLabel;
    }
    
    protected void initializeHeartbeatSection(Composite section) {
        Color background = panel.getBackground();
        section.setBackground(background);

        FormLayout layout = new FormLayout();
        section.setLayout(layout);

        buttonRemoveHeartbeat = new Button(section, SWT.FLAT);
        buttonRemoveHeartbeat.setText(EditorMessages.POLICYEDITOR_MINUS);
        buttonRemoveHeartbeat.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        buttonRemoveHeartbeat.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                labelTimeSinceLastHeartbeat.setVisible(false);
                textHeartbeatValue.setVisible(false);
                textHeartbeatValue.setText("");
                Relation rel = (Relation) PredicateHelpers.getHeartbeat(clientPolicy.getConditions());
                Long oldValue = null;
                if (rel != null) {
                    oldValue = (Long) rel.getRHS().evaluate(null).getValue();
                }
                PredicateHelpers.removeHeartbeat(clientPolicy);
                PolicyUndoElement.add(
                        PolicyUndoElementOp.CHANGE_HEARTBEAT_COND
                      , oldValue
                      , null
                );

                updateHeartbeat();
            }
        });

        buttonAddHeartbeat = new Button(section, SWT.FLAT);
        buttonAddHeartbeat.setVisible(false);
        buttonAddHeartbeat.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        buttonAddHeartbeat.setText(EditorMessages.POLICYEDITOR_PLUS);
        buttonAddHeartbeat.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                buttonRemoveHeartbeat.setVisible(true);
                labelTimeSinceLastHeartbeat.setVisible(true);
                textHeartbeatValue.setVisible(true);
                addHeartbeat();
                updateHeartbeat();
                textHeartbeatValue.forceFocus();

                PolicyUndoElement.add(
                        PolicyUndoElementOp.CHANGE_HEARTBEAT_COND
                      , null
                      , Long.valueOf(6000)
                );
            }
        });

        FormData data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        data.top = new FormAttachment(0, EditorPanel.SPACING);
        data.width = BUTTON_WIDTH;
        data.height = BUTTON_WIDTH;
        buttonAddHeartbeat.setLayoutData(data);
        data = new FormData();
        data.left = new FormAttachment(EditorPanel.SPACING, EditorPanel.SPACING);
        data.top = new FormAttachment(0, EditorPanel.SPACING);
        data.width = BUTTON_WIDTH;
        data.height = BUTTON_WIDTH;
        buttonRemoveHeartbeat.setLayoutData(data);
        labelTimeSinceLastHeartbeat = new Label(section, 0);
        labelTimeSinceLastHeartbeat.setEnabled(panel.isEditable());
        labelTimeSinceLastHeartbeat.setVisible(false);
        labelTimeSinceLastHeartbeat.setBackground(background);
        labelTimeSinceLastHeartbeat.setText("Time Since Last Heartbeat (min): ");
        data = new FormData();
        data.left = new FormAttachment(buttonRemoveHeartbeat, EditorPanel.SPACING);
        data.top = new FormAttachment(0, EditorPanel.SPACING);
        labelTimeSinceLastHeartbeat.setLayoutData(data);
        textHeartbeatValue = new Text(section, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
        textHeartbeatValue.setVisible(false);
        textHeartbeatValue.setEnabled(panel.isEditable());
        data = new FormData();
        data.left = new FormAttachment(labelTimeSinceLastHeartbeat, EditorPanel.SPACING);
        data.top = new FormAttachment(0, EditorPanel.SPACING);
        int columns = 6;
        GC gc = new GC(textHeartbeatValue);
        FontMetrics fm = gc.getFontMetrics();
        int width = columns * fm.getAverageCharWidth();
        int height = fm.getHeight();
        gc.dispose();
        textHeartbeatValue.setSize(textHeartbeatValue.computeSize(width, height));
        data.width = textHeartbeatValue.getSize().x;

        textHeartbeatValue.setLayoutData(data);

        textHeartbeatValue.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.CR) {
                    // Force focus to label
                    labelTimeSinceLastHeartbeat.forceFocus();
                }
            }
        });

        textHeartbeatValue.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                addHeartbeat();
            }
        });

        updateHeartbeat();
    }
    
    
    private void updateHeartbeat() {
        Relation rel = (Relation) PredicateHelpers.getHeartbeat((clientPolicy).getConditions());
        Long val = null;
        if (rel != null)
            val = (Long) rel.getRHS().evaluate(null).getValue();
        if (val == null) {
            buttonAddHeartbeat.setVisible(panel.isEditable());
            buttonRemoveHeartbeat.setVisible(false);
            labelTimeSinceLastHeartbeat.setVisible(false);
            textHeartbeatValue.setVisible(false);
        } else {
            buttonAddHeartbeat.setVisible(false);
            buttonRemoveHeartbeat.setVisible(panel.isEditable());
            labelTimeSinceLastHeartbeat.setVisible(true);
            textHeartbeatValue.setVisible(true);
            textHeartbeatValue.setText(String.valueOf(val.longValue() / 60));
        }
    }
    
    private void addHeartbeat() {
        int index = 0;
        try {
            index = Integer.valueOf(textHeartbeatValue.getText()).intValue() * 60;
        } catch (NumberFormatException _ex) {
            index = 6000;
        }
        IExpression exp = Constant.build(index);
        PredicateHelpers.setHeartbeat(clientPolicy, exp);
    }
    
    /*
     *</heartbeat> 
     */
    

    private void initializeResources(Composite resourceSection) {
        Color background = panel.getBackground();
        resourceSection.setBackground(background);
        FormLayout layout = new FormLayout();
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
        tgtLabel.setVisible(false);


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
        rsrcTgtComp.setVisible(false);
    }
    
    @Override
    protected void initializeOneObligation(Composite container, final IDEffectType type) {
        Color background = panel.getBackground();
        container.setBackground(background);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 2;
        container.setLayout(layout);

        LogObligationEditor editor = new LogObligationEditor(
                container
              , clientPolicy
              , type
              , panel.isEditable()
        );
        editor.setBackground(background);

        SendMessageObligationEditor editor1 = new SendMessageObligationEditor(
                container
              , clientPolicy
              , type
              , panel.isEditable()
        );
        editor1.setBackground(background);
        editor1.setVisible(false);

        SendEmailObligationEditor editor2 = new SendEmailObligationEditor(
                container
              , clientPolicy
              , type
              , panel.isEditable()
        );
        editor2.setBackground(background);
        editor2.setVisible(false);

        CustomObligationEditor editor3 = new CustomObligationEditor(
                container
              , clientPolicy
              , type
              , panel.isEditable()
        );
        editor3.setBackground(background);
        editor3.setVisible(false);
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
    	DACTypeEnum type = getDACtype ();
    	if(type == DACTypeEnum.CAP){
    		exceptionComp.relayout();
    	}else{
            userComp.relayout();
            hostComp.relayout();
            if (!isAccess()) {
                appComp.relayout();
            }
            actionComp.relayout();
            rsrcSrcComp.relayout();
            rsrcTgtComp.relayout();
            obligationsComposite.layout(true, true);
    	}
    }
    
}