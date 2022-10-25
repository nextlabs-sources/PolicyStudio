package com.bluejungle.destiny.policymanager.editor;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.action.CheckDependenciesAction;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.model.IClientEditorPanel;
import com.bluejungle.destiny.policymanager.model.IClientPolicy;
import com.bluejungle.destiny.policymanager.model.IPolicyEditor;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.ConditionPredicateHelper;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElement;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElementOp;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers.EffectTypeEnum;
import com.bluejungle.destiny.policymanager.ui.controls.CalendarPicker;
import com.bluejungle.destiny.policymanager.ui.controls.CompositionControl;
import com.bluejungle.destiny.policymanager.ui.controls.ExceptionCompositionControl;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.environment.TimeAttribute;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.obligation.CustomObligation;
import com.bluejungle.pf.domain.destiny.obligation.DisplayObligation;
import com.bluejungle.pf.domain.destiny.obligation.NotifyObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyExceptions;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;
import com.bluejungle.pf.domain.epicenter.misc.IEffectType;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;

/**
 * TODO the code style is odd right now. such as the variable is in the middle of the file.
 * TODO but don't change the style. This class should break into multi classes instead.
 * 
 * This class may change more often than your thought. 
 * Every time you change its subclasses. You should think about changes the hierarchy. 
 * Anything the subclass can be shared should go into this file.
 * 
 */
public abstract class BasePolicyEditor implements IPolicyEditor {
	
	
    // --Positioning--------
    protected static final int LABEL_COLUMN_WIDTH = 130;
    protected static final int BUTTON_WIDTH = 15;
    
    
    protected final IClientEditorPanel panel;
    protected final IDPolicy clientPolicy;
    
    
    protected BasePolicyEditor(
            IClientEditorPanel clientPanel
          , IClientPolicy clientPolicy
          , Collection<ControlId> supportedControlIds
    ) {
        this.panel = clientPanel;
        this.clientPolicy = clientPolicy.getPolicy();
        this.supportedControlIds = new HashSet<ControlId>(supportedControlIds);
    }
    
    protected BasePolicyEditor(
            IClientEditorPanel clientPanel
          , IClientPolicy clientPolicy
    ) {
        this(
             clientPanel
           , clientPolicy
           , Arrays.asList(
                   ControlId.USERS
                 , ControlId.HOSTS
                 , ControlId.APPLICATIONS
                 , ControlId.ACTIONS
                 , ControlId.DOC_SRC
                 , ControlId.DOC_TARGET
                 , ControlId.DATE
                 , ControlId.POLICY)
        );
    }
    
    protected Combo effectCombo;
    protected Combo allowonly_effectCombo;

    public CompositePredicate getControlDomainObject() {
        return null;
    }

    public String getDescription() {
        return clientPolicy.getDescription();
    }

    public EntityType getEntityType() {
        return EntityType.POLICY;
    }

    public String getObjectName() {
        String name = clientPolicy.getName();
        int index = name.lastIndexOf(PQLParser.SEPARATOR);
        if (index < 0) {
            return name;
        }
        return name.substring(index + 1);
    }

    public String getObjectTypeLabelText() {
        if (isAccess()) {
            return EditorMessages.POLICYEDITOR_ACCESS_POLICY;
        } else {
            return EditorMessages.POLICYEDITOR_USAGE_POLICY;
        }
    }
    
    protected boolean isAccess() {
        return clientPolicy.hasAttribute("access");
     }

    public void initializeContents() {
        initializeSubject();
        initializeBody();
        initializeExceptions();
        initializeObligations();
		panel.getScrolledComposite().setMinSize(panel.getLeftComposite().computeSize(SWT.DEFAULT,SWT.DEFAULT));
    }
    
    
    protected Composite getPartLabel(
            String labelName
          , Composite parent
          , Control topAttachment
          , Control bottomAttachment
    ) {
        FormLayout labelLayout = new FormLayout();
        Composite labelBackground = new Composite(parent, SWT.NONE);
        labelBackground.setLayout(labelLayout);
        labelBackground.setBackground(ResourceManager.getColor(
                "EDITOR_PART_BACKGROUD"
              , Activator.getDefault().getPluginPreferences().getString("EDITOR_PART_BACKGROUD")
        ));

        Label label = new Label(labelBackground, SWT.RIGHT);
        label.setText(labelName);
        label.setBackground(ResourceManager.getColor(
                "EDITOR_PART_BACKGROUD"
              , Activator.getDefault().getPluginPreferences().getString("EDITOR_PART_BACKGROUD")
        ));
        label.setForeground(ColorBundle.DARK_GRAY);
        FormData labelData = new FormData();
        labelData.right = new FormAttachment(100, 100, -EditorPanel.SPACING);
        labelData.top = new FormAttachment(0, 100, EditorPanel.SPACING);
        label.setLayoutData(labelData);

        FormData compData = new FormData();
        compData.left = new FormAttachment(0,EditorPanel.SIDE_SPACING);
        compData.top = new FormAttachment(topAttachment, EditorPanel.SPACING);
        compData.width = LABEL_COLUMN_WIDTH;
        compData.bottom = new FormAttachment(bottomAttachment, 0, SWT.BOTTOM);
        labelBackground.setLayoutData(compData);

        return labelBackground;
    }
    
    protected void addSectionFormData(
            Control control
          , Control leftAttachment
          , Control topAttachment
    ) {
        FormData compData = new FormData();
        compData.left = new FormAttachment(leftAttachment, EditorPanel.SPACING);
        compData.top = new FormAttachment(topAttachment, EditorPanel.SPACING);
        control.setLayoutData(compData);
    }
    
    protected void addLabelSectionFormData(Composite control, Control topAttachment) {
        FormData compData = new FormData();
        compData.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        compData.top = new FormAttachment(topAttachment, EditorPanel.SPACING);
        control.setLayoutData(compData);
    }

    
    protected Label createSeparator(Composite parent, Control topAttachment) {
        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setBackground(panel.getBackground());
        FormData separatorData = new FormData();
        separatorData.top = new FormAttachment(topAttachment, 2);
        separatorData.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        separatorData.right = new FormAttachment(100, -EditorPanel.SIDE_SPACING);
        separatorData.width = 50;
        separator.setLayoutData(separatorData);
        
        return separator;
    }
    
    
    
    
    /*
     * <subject>
     */
    
    protected Composite subjectComposite;
    protected Label subjectSectionLabel;
    
    protected CompositionControl userComp, hostComp, appComp;
    protected Label enforcementLabel;
    /**
     * default value
     * @return
     */
    protected String getSubjectSectionLabelText() {
        return EditorMessages.POLICYEDITOR_SUBJECT;
    }
    
    /**
     * default value
     * @return
     */
    protected boolean hasApplicationSubject() {
        return true;
    }
    
    protected void updateEnforcementType(){
    	effectCombo.setVisible(false);

      	allowonly_effectCombo = new Combo(subjectComposite, SWT.READ_ONLY);
        allowonly_effectCombo.setItems(ALLOWONLY_EFFECTS);
        allowonly_effectCombo.select(0);
        allowonly_effectCombo.setEnabled(false);
        FormData comboData = new FormData();
        comboData.left = new FormAttachment(enforcementLabel, EditorPanel.SPACING);
        comboData.top = new FormAttachment(0, 100, EditorPanel.SPACING);
        comboData.width = LABEL_COLUMN_WIDTH - 20;
        allowonly_effectCombo.setLayoutData(comboData);
    }
	
    protected void initializeSubject() {
        subjectComposite = panel.addLeftEditorSectionComposite();
        FormLayout subjectLayout = new FormLayout();

        enforcementLabel = new Label(subjectComposite, SWT.NONE);
        enforcementLabel.setText(getEnforcementSectionLabelText());
        enforcementLabel.setBackground(panel.getBackground());
        enforcementLabel.setForeground(ColorBundle.CE_MED_BLUE);
        FormData compData = new FormData();
        compData.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        compData.top = new FormAttachment(0,EditorPanel.TOP_SPACKING);
        compData.width = LABEL_COLUMN_WIDTH;
        enforcementLabel.setLayoutData(compData);

        effectCombo = new Combo(subjectComposite, SWT.READ_ONLY);
        effectCombo.setItems(EFFECTS);
        effectCombo.setEnabled(panel.isEditable());
        FormData comboData = new FormData();
        comboData.left = new FormAttachment(enforcementLabel, EditorPanel.SPACING);
        comboData.top = new FormAttachment(0, EditorPanel.TOP_SPACKING);
        comboData.width = LABEL_COLUMN_WIDTH - 20;
        effectCombo.setLayoutData(comboData);
        EffectTypeEnum oldeffect = getEffect();
        if (oldeffect == EffectTypeEnum.ALLOW_ONLY){
            updateEnforcementType();
        }else{
            setComboForEffect(effectCombo);
        }

        effectCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Combo c = (Combo) e.getSource();            
                EffectTypeEnum oldEffectTypeEnum = getEffect();
                int newIndex = c.getSelectionIndex();
                EffectTypeEnum newEffectTypeEnum = getEffectTypeEnumFromIndex(newIndex);
                saveEffect(newEffectTypeEnum);
                PolicyHelpers.setCombiningAlgorithmByPolicy(clientPolicy);
          
                if (newIndex == getIndexFromEffectTypeEnum(EffectTypeEnum.ALLOW)) {
                    removeDenyObligation();
                }

                updateObligation();
                GlobalState.getInstance().getEditorPanel().relayout();

                PolicyUndoElement.add(
                        PolicyUndoElementOp.CHANGE_EFFECT
                      , new PolicyUndoElement.EffectRecord(oldEffectTypeEnum)
                      , new PolicyUndoElement.EffectRecord(newEffectTypeEnum)
                );
            }
        });

        subjectSectionLabel = new Label(subjectComposite, SWT.NONE);
        subjectSectionLabel.setText(getSubjectSectionLabelText());
        subjectSectionLabel.setBackground(panel.getBackground());
        subjectSectionLabel.setForeground(ColorBundle.CE_MED_BLUE);
        FormData sectionLabelData = new FormData();
        sectionLabelData.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        sectionLabelData.top = new FormAttachment(effectCombo, EditorPanel.SPACING);
        subjectSectionLabel.setLayoutData(sectionLabelData);

        Label subjectSectionSeparator = new Label(subjectComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        subjectSectionSeparator.setBackground(panel.getBackground());
        FormData separatorData = new FormData();
        separatorData.top = new FormAttachment(subjectSectionLabel, EditorPanel.SPACING);
        separatorData.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        separatorData.right = new FormAttachment(100, -EditorPanel.SIDE_SPACING);
        subjectSectionSeparator.setLayoutData(separatorData);

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
//        addLabelSectionFormData(userLabel, subjectSectionSeparator);
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
        addSectionFormData(hostComp, desktopLabel, userComp);

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
                  , false
                  , SpecType.APPLICATION
                  , "APPLICATION"
                  , null
            );
            appComp.setBackground(panel.getBackground());
            Composite appLabel = getPartLabel(
                    EditorMessages.POLICYEDITOR_APPLICATION
                  , subjectComposite
                  , desktopLabel
                  , appComp
            );
            addSectionFormData(appComp, appLabel, hostComp);
        }

        subjectComposite.setLayout(subjectLayout);
    }
    
    protected abstract void updateObligation();
    protected abstract String getEnforcementSectionLabelText();
    
    /*
     * </subject>
     */
    
    
    
    
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
        heartbeatLabel = getPartLabel(
                "Heartbeat"      // String labelName
              , parent           // Composite parent
              , topAttachment    // Control topAttachment
              , heartbeatSection // Control bottomAttachment
        );
        addSectionFormData(heartbeatSection, heartbeatLabel, topAttachment);
        
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
        data.left = new FormAttachment(buttonRemoveHeartbeat, EditorPanel.SPACING);
        data.top = new FormAttachment(0, EditorPanel.SPACING);
        data.width = BUTTON_WIDTH;
        data.height = BUTTON_WIDTH;
        buttonAddHeartbeat.setLayoutData(data);
        data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SPACING);
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
    
    
    
    
    
    
    
    /*
     * <body>
     */
    protected Composite bodyComposite;
    
    protected abstract String getBodySectionLabelText();
    
    /**
     * 
     * @return the last control for the others to attach
     */
    protected Control initializeBody() {
        bodyComposite = panel.addLeftEditorSectionComposite();
        FormLayout bodyLayout = new FormLayout();
        bodyComposite.setLayout(bodyLayout);
        
        
        Label bodySectionLabel = new Label(bodyComposite, SWT.NONE);
        bodySectionLabel.setText(getBodySectionLabelText());
        bodySectionLabel.setBackground(panel.getBackground());
        bodySectionLabel.setForeground(ColorBundle.CE_MED_BLUE);
        FormData sectionLabelData = new FormData();
        sectionLabelData.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        sectionLabelData.top = new FormAttachment(0, 100, EditorPanel.SPACING);
        bodySectionLabel.setLayoutData(sectionLabelData);

        return createBodySeparator(bodySectionLabel);
    }
    
    /**
     * 
     * @param topAttachment
     * @return the separator (It is a org.eclipse.swt.widget.Label object)
     */
    protected Label createBodySeparator(Control topAttachment) {
        return createSeparator(bodyComposite, topAttachment);
    }
    
    protected CompositionControl actionComp;
    
    protected Control initializeAction(Control lastAttachment) {
        actionComp = new CompositionControl(
                bodyComposite
              , SWT.NONE
              , EditorMessages.POLICYEDITOR_ACTION_COMPONENT
              , ""
              , getControlDomainObject(ControlId.ACTIONS, clientPolicy)
              , panel.getEditorPanel()
              , ControlId.ACTIONS.ordinal()
              , panel.isEditable()
              , false
              , true
              , new String[] {}
              , SpecType.ACTION
              , "ACTION"
              , null
        );
        actionComp.setBackground(panel.getBackground());
        Composite actionLabel = getPartLabel(
                EditorMessages.POLICYEDITOR_ACTION
              , bodyComposite
              , lastAttachment
              , actionComp
        );
        addSectionFormData(actionComp, actionLabel, lastAttachment);
        
        return actionComp;
    }
    
    /*
     * </body>
     */
    
    
    public void setDescription(String description) {
        clientPolicy.setDescription(description);
    }

    public void updateFromDomainObject() {
//        IDPolicy policy = clientPolicy;
        if (panel.isDisposed()) {
           return;
        }
//        IEffectType effect = policy.getMainEffect();
//        IEffectType otherwise = policy.getOtherwiseEffect();
        EffectTypeEnum effectTypeEnum = getEffect();
        Integer index = getIndexFromEffectTypeEnum(effectTypeEnum);
        
        if (index != null && index != effectCombo.getSelectionIndex()) {
        	effectCombo.select(index);
        }
        updateExtraFromDomainObject();
        relayout();
    }
    
    /**
     * very likely you have some extra stuff to update
     */
    protected abstract void updateExtraFromDomainObject();
    
    protected abstract void relayout();
    
	/*
	 * <Policy effects mapping>
	 */
    

    public static String[] EFFECTS = {
        EditorMessages.POLICYEDITOR_DENY
//      , EditorMessages.POLICYEDITOR_ALLOW
      , EditorMessages.POLICYEDITOR_MONITOR 
    };
    
    public static String[] ALLOWONLY_EFFECTS = {
    	EditorMessages.POLICYEDITOR_ALLOW
    };
    

    protected static final Map<EffectTypeEnum, Integer> effectTypeToIndexMap;
    protected static final Map<EffectTypeEnum, String>  effectTypeToStringMap;
    protected static final Map<Integer, EffectTypeEnum> indexToEffectTypeMap;
    
	static {
	    effectTypeToIndexMap  = new HashMap<EffectTypeEnum, Integer>();
	    indexToEffectTypeMap  = new HashMap<Integer, EffectTypeEnum>();
	    effectTypeToStringMap = new HashMap<EffectTypeEnum, String> ();
	    
	    effectTypeToIndexMap.put(EffectTypeEnum.DENY, 0);
	    indexToEffectTypeMap.put(0, EffectTypeEnum.DENY);
	    effectTypeToStringMap.put(EffectTypeEnum.DENY, "DENY");
	    
//	    effectTypeToIndexMap.put(EffectTypeEnum.ALLOW_ONLY, 1);
//	    indexToEffectTypeMap.put(1, EffectTypeEnum.ALLOW_ONLY);
//	    effectTypeToStringMap.put(EffectTypeEnum.ALLOW_ONLY, "ALLOW ONLY");
	    
	    effectTypeToIndexMap.put(EffectTypeEnum.ALLOW, 1);
	    indexToEffectTypeMap.put(1, EffectTypeEnum.ALLOW);
	    effectTypeToStringMap.put(EffectTypeEnum.ALLOW, "ALLOW");
	}
	/*
	 * </Policy effects mapping >
	 */

    /*
     * <effect>
     */

	protected String getStringFromEffectTypeEnum(EffectTypeEnum effectTypeEnum){	
		return EFFECTS[getIndexFromEffectTypeEnum(effectTypeEnum)];		
	}
	protected static int getIndexFromEffectTypeEnum (EffectTypeEnum effectTypeEnum){
		return effectTypeToIndexMap.get(effectTypeEnum);
	}
	protected EffectTypeEnum getEffectTypeEnumFromIndex (int index){
		return indexToEffectTypeMap.get(index);
	}
    
    protected EffectTypeEnum getEffect() {
        IDPolicy policy = (clientPolicy);
        IEffectType effect = policy.getMainEffect();
        IEffectType otherwise = policy.getOtherwiseEffect();
        if(PolicyHelpers.isDACPolicyType(policy)){
        	if (effect == EffectType.DENY && otherwise == EffectType.ALLOW){
        		effect = EffectType.ALLOW;
        		otherwise = EffectType.DENY;
        	}
    		policy.setMainEffect(effect);
    		policy.setOtherwiseEffect(otherwise);
        }
        EffectTypeEnum effectTypeEnum = PolicyHelpers.getIndexForEffect (effect, otherwise);
        return effectTypeEnum;
    }
        
    protected void setComboForEffect(Combo combo) {
        EffectTypeEnum effectTypeEnum = getEffect();
        Integer index = getIndexFromEffectTypeEnum(effectTypeEnum);
        
        if (index != null) {
        	combo.select(index);
        }
    }
    
    protected void saveEffect(EffectTypeEnum effectTypeEnum) {
        PolicyHelpers.saveEffect(clientPolicy, effectTypeEnum);
    }
    
    /*
     * </effect>
     */    
    
    
    
    
    
    /*
     * <ControlDomainObject>
     */
    
    private final Set<ControlId> supportedControlIds;
    
    protected static enum ControlId{
        USERS
      , HOSTS
      , APPLICATIONS
      , ACTIONS
      , DOC_SRC
      , DOC_TARGET
      , DATE
      , SUBJECT
      , POLICY
    }
    
    protected List<IPolicyReference> getException(ControlId controlId){
        if (!supportedControlIds.contains(controlId)) {
            return null;
        }
        IDPolicy policy = clientPolicy;
        IPolicyExceptions exception = policy.getPolicyExceptions();
        return exception.getPolicies();
    }
    
    protected CompositePredicate getControlDomainObject(ControlId controlId, IHasId domainObject) {

        if (!supportedControlIds.contains(controlId)) {
            return null;
        }
        
        IDPolicy policy = (IDPolicy) domainObject;
        ITarget target = policy.getTarget();
        CompositePredicate subject;

        switch (controlId) {
        case USERS:
            subject = (CompositePredicate) target.getSubjectPred();
            return (CompositePredicate) subject.predicateAt(0);
        case HOSTS:
            subject = (CompositePredicate) target.getSubjectPred();
            return (CompositePredicate) subject.predicateAt(1);
        case APPLICATIONS:
            subject = (CompositePredicate) target.getSubjectPred();
            return (CompositePredicate) subject.predicateAt(2);
        case ACTIONS:
            return (CompositePredicate) target.getActionPred();
        case DOC_SRC:
            return (CompositePredicate) target.getFromResourcePred();
        case DOC_TARGET:
            return (CompositePredicate) target.getToResourcePred();
        case DATE:
            return (CompositePredicate) policy.getConditions();
        case SUBJECT:
            return (CompositePredicate) target.getToSubjectPred();      	
        }

        return null;
    }
    
    /*
     * </ControlDomainObject>
     */
    
    
    
    
    
    
    /*
     * <obligation>
     */
    protected Composite obligationsComposite;
    protected Label obligationsSectionLabel
                , separatorObligation;
    protected Composite denyObligations
                , denyLabel
                , allowObligations
                , allowLabel;
    
    protected void initializeObligations() {
        obligationsComposite = panel.addLeftEditorSectionComposite();
        FormLayout oblLayout = new FormLayout();
        obligationsComposite.setLayout(oblLayout);

        obligationsSectionLabel = new Label(obligationsComposite, SWT.NONE);
        obligationsSectionLabel.setText(EditorMessages.POLICYEDITOR_OBLIGATIONS);
        obligationsSectionLabel.setBackground(panel.getBackground());
        obligationsSectionLabel.setForeground(ColorBundle.CE_MED_BLUE);
        FormData sectionLabelData = new FormData();
        sectionLabelData.left = new FormAttachment(0, EditorPanel.SIDE_SPACING);
        sectionLabelData.top = new FormAttachment(0, 100, EditorPanel.SPACING);
        obligationsSectionLabel.setLayoutData(sectionLabelData);

        separatorObligation = createSeparator(obligationsComposite, obligationsSectionLabel);

        denyObligations = new Composite(obligationsComposite, SWT.NONE);
        denyLabel = getPartLabel(
                EditorMessages.POLICYEDITOR_ON_DENY
              , obligationsComposite
              , separatorObligation
              , denyObligations
        );
        addSectionFormData(denyObligations, denyLabel, separatorObligation);

        initializeOneObligation(denyObligations, EffectType.DENY);

        allowObligations = new Composite(obligationsComposite, SWT.NONE);
        allowLabel = getPartLabel(
                EditorMessages.POLICYEDITOR_ON_ALLOW
              , obligationsComposite
              , denyLabel
              , allowObligations
        );
        addSectionFormData(allowObligations, allowLabel, denyLabel);

        initializeOneObligation(allowObligations, EffectType.ALLOW);

        updateObligation();
    }
    
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

        SendEmailObligationEditor editor2 = new SendEmailObligationEditor(
                container
              , clientPolicy
              , type
              , panel.isEditable()
        );
        editor2.setBackground(background);

        CustomObligationEditor editor3 = new CustomObligationEditor(
                container
              , clientPolicy
              , type
              , panel.isEditable()
        );
        editor3.setBackground(background);
    }
    
    protected void removeDenyObligation() {
        IDPolicy policy = clientPolicy;
        IDEffectType effectType = EffectType.DENY;
        Collection<IObligation> obligations = policy.getObligations(effectType);

        List<IObligation> obligationsToBeDeleted = new ArrayList<IObligation>();
        List<IObligation> obligationsToReturn = new ArrayList<IObligation>();
        for (IObligation nextObligation : obligations) {
            String nextObligationType = nextObligation.getType();
            if (nextObligationType.equals(DisplayObligation.OBLIGATION_NAME)) {
                obligationsToReturn.add(nextObligation);
            }
        }
        if (!obligationsToReturn.isEmpty()) {
            obligationsToBeDeleted.add(obligationsToReturn.get(0));
        }
        obligationsToReturn.clear();
        for (IObligation nextObligation : obligations) {
            String nextObligationType = nextObligation.getType();
            if (nextObligationType.equals(NotifyObligation.OBLIGATION_NAME)) {
                obligationsToReturn.add(nextObligation);
            }
        }
        if (!obligationsToReturn.isEmpty()) {
            obligationsToBeDeleted.add(obligationsToReturn.get(0));
        }
        obligationsToReturn.clear();
        for (IObligation nextObligation : obligations) {
            String nextObligationType = nextObligation.getType();
            if (nextObligationType.equals(CustomObligation.OBLIGATION_NAME)) {
                obligationsToReturn.add(nextObligation);
            }
        }
        if (!obligationsToReturn.isEmpty()) {
            obligationsToBeDeleted.add(obligationsToReturn.get(0));
        }

        for (IObligation nextObligation : obligationsToBeDeleted) {
            policy.deleteObligation(nextObligation, effectType);
        }
    }
    
    /*
     * </obligation>
     */
    
    /*
     *<Exception> 
     */
    
    protected abstract String getExceptionSectionLabelText();
    
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
        sectionLabelData.top = new FormAttachment(0, 100, EditorPanel.SPACING);
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

    
    /*
     * <connectionType> 
     */
    private Button connectionTypeButtonAdd, connectionTypeButtonRemove;
    private Combo connectionTypeComboType;
    private Label connectionTypeLabelSite;
    private Text connectionTypeTextSite;
    
    protected void initializeConnectionTypeSection(Composite section) {
        Color background = panel.getBackground();
        section.setBackground(background);

        FormLayout layout = new FormLayout();
        section.setLayout(layout);

        connectionTypeButtonRemove = new Button(section, SWT.FLAT);
        connectionTypeButtonRemove.setText(EditorMessages.POLICYEDITOR_MINUS);
        connectionTypeButtonRemove.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        connectionTypeButtonRemove.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // get the old value
                connectionTypeLabelSite.setVisible(false);
                connectionTypeTextSite.setVisible(false);
                connectionTypeTextSite.setText("");

                Relation rel = (Relation) PredicateHelpers.getConnectionType(clientPolicy.getConditions());
                Long oldValue = null;
                if (rel != null) {
                    oldValue = (Long) ((IRelation) rel).getRHS().evaluate(null).getValue();
                }
                // remove the old value
                PredicateHelpers.removeConnectionType(clientPolicy);
                PredicateHelpers.removeConnectionSite(clientPolicy);
                // create undo info
                PolicyUndoElement.add(
                        PolicyUndoElementOp.CHANGE_CONNECTION_TYPE
                      , oldValue
                      , null
                );

                updateConnectionType();
            }
        });

        connectionTypeButtonAdd = new Button(section, SWT.FLAT);
        connectionTypeButtonAdd.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        connectionTypeButtonAdd.setText(EditorMessages.POLICYEDITOR_PLUS);
        connectionTypeButtonAdd.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                connectionTypeButtonRemove.setVisible(true);
                connectionTypeComboType.setVisible(true);
                connectionTypeComboType.select(1);
                connectionTypeLabelSite.setVisible(false);
                connectionTypeTextSite.setVisible(false);
                connectionTypeTextSite.setText("");
                connectionTypeComboType.setFocus();
                connectionTypeButtonAdd.setVisible(false);
                addConnectionType();

                PolicyUndoElement.add(
                        PolicyUndoElementOp.CHANGE_CONNECTION_TYPE
                      , null
                      , Long.valueOf(1)
                );
            }
        });

        FormData data = new FormData();
        data.left = new FormAttachment(connectionTypeButtonRemove, EditorPanel.SPACING);
        data.top = new FormAttachment(0, EditorPanel.SPACING);
        data.width = BUTTON_WIDTH;
        data.height = BUTTON_WIDTH;
        connectionTypeButtonAdd.setLayoutData(data);

        data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SPACING);
        data.top = new FormAttachment(0, EditorPanel.SPACING);
        data.width = BUTTON_WIDTH;
        data.height = BUTTON_WIDTH;
        connectionTypeButtonRemove.setLayoutData(data);

        connectionTypeComboType = new Combo(section, SWT.READ_ONLY);
        connectionTypeComboType.setEnabled(panel.isEditable());
        connectionTypeComboType.add(EditorMessages.POLICYEDITOR_LOCAL);
        connectionTypeComboType.add(EditorMessages.POLICYEDITOR_REMOTE);
        connectionTypeComboType.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = connectionTypeComboType.getSelectionIndex();
                if (index == 1) {
                    connectionTypeLabelSite.setVisible(false);
                    connectionTypeTextSite.setVisible(false);
                } else {
                    connectionTypeLabelSite.setVisible(false);
                    connectionTypeTextSite.setVisible(false);
                }
                addConnectionType();
            }
        });

        connectionTypeLabelSite = new Label(section, SWT.NONE);
        connectionTypeLabelSite.setEnabled(panel.isEditable());
        connectionTypeLabelSite.setVisible(false);
        connectionTypeLabelSite.setBackground(background);
        connectionTypeLabelSite.setText(EditorMessages.POLICYEDITOR_SITE);
        data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SPACING);
        data.top = new FormAttachment(connectionTypeComboType, EditorPanel.SPACING);
        connectionTypeLabelSite.setLayoutData(data);

        data = new FormData();
        data.left = new FormAttachment(connectionTypeLabelSite, EditorPanel.SPACING);
        data.top = new FormAttachment(0, EditorPanel.SPACING);
        data.width = 100;
        connectionTypeComboType.setLayoutData(data);

        connectionTypeTextSite = new Text(section, SWT.BORDER);
        connectionTypeTextSite.setVisible(false);
        data = new FormData();
        data.left = new FormAttachment(connectionTypeLabelSite, EditorPanel.SPACING);
        data.top = new FormAttachment(connectionTypeComboType, EditorPanel.SPACING);
        data.right = new FormAttachment(connectionTypeComboType, 0, SWT.RIGHT);
        connectionTypeTextSite.setLayoutData(data);
        connectionTypeTextSite.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                addConnectionType();
            }
        });

        updateConnectionType();
    }

    private void addConnectionType() {
        int index = connectionTypeComboType.getSelectionIndex();
        IExpression exp = Constant.build(index);
        PredicateHelpers.setConnectionType(clientPolicy, exp);
        if (index == 1) {
            String text = connectionTypeTextSite.getText();
            if (text.trim().length() == 0) {
                PredicateHelpers.removeConnectionSite(clientPolicy);
            } else {
                IExpression exp1 = Constant.build(text);
                PredicateHelpers.setConnectionSite(clientPolicy, exp1);
            }
        } else {
            PredicateHelpers.removeConnectionSite(clientPolicy);
        }
    }

    protected void updateConnectionType() {
        Relation rel = (Relation) PredicateHelpers.getConnectionType(clientPolicy.getConditions());
        Long val = null;
        if (rel != null) {
            val = (Long) ((IRelation) rel).getRHS().evaluate(null).getValue();
        }

        if (val == null) {
            connectionTypeButtonAdd.setVisible(true && panel.isEditable());
            connectionTypeButtonRemove.setVisible(false);
            connectionTypeComboType.setVisible(false);
            connectionTypeLabelSite.setVisible(false);
            connectionTypeTextSite.setVisible(false);
        } else {
            connectionTypeButtonAdd.setVisible(false);
            connectionTypeButtonRemove.setVisible(true && panel.isEditable());
            connectionTypeComboType.setVisible(true);
            int index = val.intValue();
            connectionTypeComboType.select(index);
            if (index == 1) {
                connectionTypeLabelSite.setVisible(false);
                connectionTypeLabelSite.setEnabled(panel.isEditable());
                connectionTypeTextSite.setVisible(false);
                connectionTypeTextSite.setEditable(panel.isEditable());
                connectionTypeTextSite.setEnabled(panel.isEditable());

                Relation rel1 = (Relation) PredicateHelpers.getConnectionSite(clientPolicy.getConditions());
                String val1 = "";
                if (rel1 != null) {
                    val1 = (String) ((IRelation) rel1).getRHS().evaluate(null).getValue();
                }
                connectionTypeTextSite.setText(val1);
            } else {
                connectionTypeLabelSite.setVisible(false);
                connectionTypeTextSite.setVisible(false);
            }
        }
    }
    /*
     * </connectionType>
     */
    
    
    /*
     * <something>
     */
    
    protected EnforcementTimeRow startTime, endTime;
    
    protected void initializeEnforcementDateSection(Composite dateSection) {
        Color background = panel.getBackground();
        dateSection.setBackground(background);

        dateSection.setLayout(new RowLayout(SWT.VERTICAL));

        startTime = new EnforcementTimeRow(
                dateSection
              , EditorMessages.POLICYEDITOR_START
              , new StartTimeAccessor()
              , PolicyUndoElementOp.CHANGE_START_DATE);
        startTime.setupRow();
        endTime = new EnforcementTimeRow(
                dateSection
              , EditorMessages.POLICYEDITOR_END
              , new EndTimeAccessor()
              , PolicyUndoElementOp.CHANGE_END_DATE);
        endTime.setupRow();
    }
    
    protected DailyScheduleTimeRow dailySchedule;
    protected RecurringScheduleTimeRow recurringEnforcement;

    protected void initializeRecurrentDateSection(Composite dateSection) {
        Color background = panel.getBackground();
        dateSection.setBackground(background);

        dateSection.setLayout(new RowLayout(SWT.VERTICAL));

        dailySchedule = new DailyScheduleTimeRow(dateSection, EditorMessages.POLICYEDITOR_TIME);
        dailySchedule.setupRow();
        recurringEnforcement = new RecurringScheduleTimeRow(dateSection, EditorMessages.POLICYEDITOR_DAY);
        recurringEnforcement.setupRow();
    }
    
    /*
     * </something>
     */
    
    
    
    
    /*
     * <Condition Expression>
     */
    
    private Button advancedConditionButtonAdd, advancedConditionButtonRemove;
    private Text advancedConditionText;
    protected Composite advancedConditionLabel, advancedConditionSection;
    
    protected Control initializeAdvancedCondition(Composite section, Control topAttachment) {
        advancedConditionLabel = getPartLabel(
                "Condition\nExpression"
              , bodyComposite
              , topAttachment
              , section
        );
        addSectionFormData(section, advancedConditionLabel, topAttachment);

        Color background = panel.getBackground();
        section.setBackground(background);

        FormLayout layout = new FormLayout();
        section.setLayout(layout);

        advancedConditionButtonRemove = new Button(section, SWT.FLAT);
        advancedConditionButtonRemove.setText(EditorMessages.POLICYEDITOR_MINUS);
        advancedConditionButtonRemove.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        advancedConditionButtonRemove.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                advancedConditionButtonAdd.setVisible(true);
                advancedConditionButtonRemove.setVisible(false);
                advancedConditionText.setVisible(false);
                
                String oldValue = advancedConditionText.getText();
                advancedConditionText.setText("");

                try {
                    ConditionPredicateHelper.setFreeTypeConditionString(clientPolicy, null);
                } catch (PQLException e1) {
                    final Shell shell = Display.getCurrent().getActiveShell();
                    MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);

                    messageBox.setText("Can't remove condition");
                    messageBox.setMessage(String.valueOf(e1.getCause()));
                    messageBox.open();
                    return;
                }

                PolicyUndoElement.add(
                        PolicyUndoElementOp.CHANGE_ADVANCED_COND
                      , oldValue
                      , null
                );
            }
        });

        advancedConditionButtonAdd = new Button(section, SWT.FLAT);
        advancedConditionButtonAdd.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        advancedConditionButtonAdd.setText(EditorMessages.POLICYEDITOR_PLUS);
        advancedConditionButtonAdd.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                advancedConditionButtonAdd.setVisible(false);
                advancedConditionButtonRemove.setVisible(true);
                advancedConditionText.setVisible(true);

                String raw = ConditionPredicateHelper.getFreeTypeConditionString(clientPolicy.getConditions());
                advancedConditionText.setText(raw != null ? raw : "");

            }
        });

        FormData data = new FormData();
        data.left = new FormAttachment(advancedConditionButtonRemove, EditorPanel.SPACING);
        data.top = new FormAttachment(0, EditorPanel.SPACING);
        data.width = BUTTON_WIDTH;
        data.height = BUTTON_WIDTH;
        advancedConditionButtonAdd.setLayoutData(data);

        data = new FormData();
        data.left = new FormAttachment(0, EditorPanel.SPACING);
        data.top = new FormAttachment(0, EditorPanel.SPACING);
        data.width = BUTTON_WIDTH;
        data.height = BUTTON_WIDTH;
        advancedConditionButtonRemove.setLayoutData(data);

        advancedConditionText = new Text(section, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
        advancedConditionText.setVisible(false);
        advancedConditionText.setEnabled(panel.isEditable());
        data = new FormData();
        data.left = new FormAttachment(advancedConditionButtonAdd, EditorPanel.SPACING);
        data.top = new FormAttachment(0, EditorPanel.SPACING);
        data.width = 300;
        data.height = advancedConditionText.getLineHeight() * 4;
        advancedConditionText.setLayoutData(data);
        advancedConditionText.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                addAdvancedCondition();
            }
        });

        updateAdvancedCondition();
        
        return advancedConditionLabel;
    }

    private void addAdvancedCondition() {
        String oldValue = ConditionPredicateHelper.getFreeTypeConditionString(clientPolicy.getConditions());
        String newValue = advancedConditionText.getText();
        
        try {
            ConditionPredicateHelper.setFreeTypeConditionString(clientPolicy, newValue);
        } catch (PQLException e1) {
            final Shell shell = Display.getCurrent().getActiveShell();
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);

            messageBox.setText("Invalid Condition");
            messageBox.setMessage(String.valueOf(e1.getCause()));
            messageBox.open();
            return;
        }
        
        PolicyUndoElement.add(
                PolicyUndoElementOp.CHANGE_ADVANCED_COND
              , oldValue
              , advancedConditionText.getText()
        );
    }

    private void updateAdvancedCondition() {
        String raw = ConditionPredicateHelper.getFreeTypeConditionString(clientPolicy.getConditions());
        advancedConditionText.setText(raw != null ? raw : "");

        if (raw == null) {
            advancedConditionButtonAdd.setVisible(true && panel.isEditable());
            advancedConditionButtonRemove.setVisible(false);
            advancedConditionText.setVisible(false);
            advancedConditionText.setText("");
        } else {
            advancedConditionButtonAdd.setVisible(false);
            advancedConditionButtonRemove.setVisible(true && panel.isEditable());
            advancedConditionText.setVisible(true);
            advancedConditionText.setText(raw);
        }
    }
    
    /*
     * </Condition Expression>
     */
    
    

    
    
    /*
     * <TimePredicateAccessor>
     */
    
    /**
     * A class for abstracting access of Time information from the domain
     * object. This class is used with TimeRows which implement the control
     * logic for UI widgets in the Date and Time section.
     */
    protected static abstract class TimePredicateAccessor {

        public abstract IPredicate getTimePredicate();

        public abstract boolean hasTimePredicate();

        public abstract void addNewTimePredicate();

        public abstract void removeTimePredicate();

        public abstract void modifyTime(Object timeInfo);
    }
    
    
    protected class RecurringScheduleAccessor extends TimePredicateAccessor {

        public RecurringScheduleAccessor() {
        }
        
        @Override
        public void addNewTimePredicate() {
            addNewWeekdayPredicate();
        }

        public void addNewWeekdayPredicate() {
            IDPolicy policy = clientPolicy;
            List<Relation> days = new ArrayList<Relation>();
            for (String weekday : CommonPolicyConstants.DAY_NAMES) {
                Relation rel = new Relation(
                        RelationOp.EQUALS
                      , TimeAttribute.WEEKDAY
                      , TimeAttribute.WEEKDAY.build(weekday.toLowerCase())
                );
                days.add(rel);
            }
            CompositePredicate pred = new CompositePredicate(BooleanOp.OR, days);
            PredicateHelpers.setWeekdayPredicate(policy, pred);

            PolicyUndoElement.add(
                    PolicyUndoElementOp.CHANGE_RECURRENCE_PREDICATE
                  , null
                  , getTimePredicate()
            );
        }

        @Override
        public IPredicate getTimePredicate() {
            IDPolicy policy = clientPolicy;
            IPredicate conditions = policy.getConditions();
            IPredicate dowim = PredicateHelpers.getDOWIMPredicate(conditions);
            if (dowim != null) {
                return PredicateHelpers.getFullDOWIMInfoPredicates(conditions);
            }
            IPredicate wdp = PredicateHelpers.getWeekDayPredicate(conditions);
            if (wdp != null) {
                return wdp;
            }
            IPredicate dom = PredicateHelpers.getDayOfMonthPredicate(conditions);
            if (dom != null) {
                return dom;
            }
            return null;
        }

        @Override
        public boolean hasTimePredicate() {
            IDPolicy policy = clientPolicy;
            IPredicate conditions = policy.getConditions();
            IPredicate wdp = PredicateHelpers.getWeekDayPredicate(conditions);
            IPredicate dom = PredicateHelpers.getDayOfMonthPredicate(conditions);
            IPredicate dowim = PredicateHelpers.getDOWIMPredicate(conditions);
            return (dowim != null || dom != null || wdp != null);
        }

        @Override
        public void modifyTime(Object timeInfo) {
            // not using this method here, since all the modification code is
            // specified in the row class implementation
        }

        @Override
        public void removeTimePredicate() {

            IPredicate oldValue = getTimePredicate();
            doRemoveExistingTimePredicate();

            PolicyUndoElement.add(
                    PolicyUndoElementOp.CHANGE_RECURRENCE_PREDICATE
                  , oldValue
                  , null
            );

        }

        public void doRemoveExistingTimePredicate() {
            IDPolicy policy = clientPolicy;
            IPredicate conditions = policy.getConditions();
            IPredicate dowim = PredicateHelpers.getDOWIMPredicate(conditions);
            if (dowim != null) {
                // policy will have a day of week in month (ie. First) and a
                // weekday (ie. Monday)
                PredicateHelpers.removeDOWIMPredicate(policy);
                PredicateHelpers.removeWeekdayPredicate(policy);
            }
            IPredicate wdp = PredicateHelpers.getWeekDayPredicate(conditions);
            if (wdp != null) {
                PredicateHelpers.removeWeekdayPredicate(policy);
            }
            IPredicate dom = PredicateHelpers.getDayOfMonthPredicate(conditions);
            if (dom != null) {
                PredicateHelpers.removeDayOfMonthPredicate(policy);
            }
        }
    }
    
    protected class StartTimeAccessor extends TimePredicateAccessor {
        
        public StartTimeAccessor() {
        }
        
        @Override
        public IPredicate getTimePredicate() {
            return PredicateHelpers.getStartTime(clientPolicy.getConditions());
        }

        @Override
        public boolean hasTimePredicate() {
            return PredicateHelpers.getStartTime(clientPolicy.getConditions()) != null;
        }

        @Override
        public void addNewTimePredicate() {
            Calendar calendar = new GregorianCalendar();
            calendar.set(Calendar.SECOND, 0);
            String dateString=DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US).format(calendar.getTime());
            IExpression exp = TimeAttribute.IDENTITY.build(dateString);
            // set the value
            PredicateHelpers.setStartTime(clientPolicy, exp);
            // create undo info
            PolicyUndoElement.add(
                    PolicyUndoElementOp.CHANGE_START_DATE
                  , null
                  , calendar.getTime()
            );
        }

        @Override
        public void removeTimePredicate() {
            // get the old value
            Relation rel = (Relation) PredicateHelpers.getStartTime(clientPolicy.getConditions());
            Date oldDate = null;
            if (rel != null) {
                Long dateVal = (Long) ((IRelation) rel).getRHS().evaluate(null).getValue();
                oldDate = new Date(dateVal.longValue());
            }
            // remove the old value
            PredicateHelpers.removeStartTime(clientPolicy);
            // create undo info
            PolicyUndoElement.add(
                    PolicyUndoElementOp.CHANGE_START_DATE
                  , oldDate
                  , null
            );
        }

        @Override
        public void modifyTime(Object timeInfo) {
            // get the old value
            Relation rel = (Relation) PredicateHelpers.getStartTime(clientPolicy.getConditions());
            Date oldDate = null;
            if (rel != null) {
                Long dateVal = (Long) ((IRelation) rel).getRHS().evaluate(null).getValue();
                oldDate = new Date(dateVal.longValue());
            }

            Date newDate = (Date) timeInfo;
            if (!newDate.equals(oldDate)) {
                // set the new value
                String dateString =DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US).format(newDate);
                IExpression exp = TimeAttribute.IDENTITY.build(dateString);
                PredicateHelpers.setStartTime(clientPolicy, exp);
            }
        }
    }

    protected class EndTimeAccessor extends TimePredicateAccessor {
        
        public EndTimeAccessor() {
        }
        
        @Override
        public IPredicate getTimePredicate() {
            return PredicateHelpers.getEndTime(clientPolicy.getConditions());
        }

        @Override
        public boolean hasTimePredicate() {
            return PredicateHelpers.getEndTime(clientPolicy.getConditions()) != null;
        }

        @Override
        public void addNewTimePredicate() {
            Calendar calendar = new GregorianCalendar();
            calendar.set(Calendar.SECOND, 0);
            String dateString = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US).format(calendar.getTime());
            IExpression exp = TimeAttribute.IDENTITY.build(dateString);
            // set the new value
            PredicateHelpers.setEndTime(clientPolicy, exp);
            // create undo info
            PolicyUndoElement.add(
                    PolicyUndoElementOp.CHANGE_END_DATE
                  , null
                  , calendar.getTime()
            );
        }

        @Override
        public void removeTimePredicate() {
            // get the old value
            Relation rel = (Relation) PredicateHelpers.getStartTime(clientPolicy.getConditions());
            Date oldDate = null;
            if (rel != null) {
                Long dateVal = (Long) ((IRelation) rel).getRHS().evaluate(null).getValue();
                oldDate = new Date(dateVal.longValue());
            }

            // remove the old value
            PredicateHelpers.removeEndTime(clientPolicy);

            // create undo info
            PolicyUndoElement.add(
                    PolicyUndoElementOp.CHANGE_END_DATE
                  , oldDate
                  , null
            );
        }

        @Override
        public void modifyTime(Object timeInfo) {
            // get the old value
            Relation rel = (Relation) PredicateHelpers.getStartTime(clientPolicy.getConditions());
            Date oldDate = null;
            if (rel != null) {
                Long dateVal = (Long) ((IRelation) rel).getRHS().evaluate(null).getValue();
                oldDate = new Date(dateVal.longValue());
            }

            Date newDate = (Date) timeInfo;
            if (!newDate.equals(oldDate)) {
                // set the new value
            	String dateString  =DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US).format(newDate);
                IExpression exp = TimeAttribute.IDENTITY.build(dateString);
                PredicateHelpers.setEndTime(clientPolicy, exp);
            }
        }
    }


    protected class DailyScheduleAccessor extends TimePredicateAccessor {
        
        public DailyScheduleAccessor() {
        }
        
        @Override
        public IPredicate getTimePredicate() {
            IPredicate condition = clientPolicy.getConditions();
            IPredicate from = PredicateHelpers.getDailyFromTime(condition);
            IPredicate to = PredicateHelpers.getDailyToTime(condition);

            List<IPredicate> parts = new ArrayList<IPredicate>();
            parts.add(from);
            parts.add(to);
            return new CompositePredicate(BooleanOp.AND, parts);
        }

        @Override
        public boolean hasTimePredicate() {
            IPredicate condition = clientPolicy.getConditions();
            IPredicate from = PredicateHelpers.getDailyFromTime(condition);
            IPredicate to = PredicateHelpers.getDailyToTime(condition);
            return (from != null && to != null);
        }

        @Override
        public void addNewTimePredicate() {
            Calendar d = new GregorianCalendar();
            d.set(Calendar.SECOND, 0);
            String timeString=DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.US).format(d.getTime());
            IExpression fromExp = TimeAttribute.TIME.build(timeString);
            IExpression toExp = TimeAttribute.TIME.build(timeString);
            IDPolicy policy = clientPolicy;
            PredicateHelpers.setDailyFromTime(policy, fromExp);
            PredicateHelpers.setDailyToTime(policy, toExp);

            // create undo info
            PolicyUndoElement.add(
                    PolicyUndoElementOp.CHANGE_DAILY_SCHEDULE
                  , null
                  , new IExpression[] { fromExp, toExp }
            );
        }

        @Override
        public void removeTimePredicate() {
            IDPolicy policy = clientPolicy;
            Relation from = (Relation) PredicateHelpers.getDailyFromTime(policy.getConditions());
            Relation to = (Relation) PredicateHelpers.getDailyToTime(policy.getConditions());

            PredicateHelpers.removeDailyFromTime(policy);
            PredicateHelpers.removeDailyToTime(policy);

            // create undo info
            PolicyUndoElement.add(
                    PolicyUndoElementOp.CHANGE_DAILY_SCHEDULE
                  , new IExpression[] { from.getRHS(), to.getRHS() }
                  , null
            );
        }

        @Override
        public void modifyTime(Object timeInfo) {
            // get the old values
            IDPolicy policy = clientPolicy;
            Relation from = (Relation) PredicateHelpers.getDailyFromTime(policy.getConditions());
            Relation to = (Relation) PredicateHelpers.getDailyToTime(policy.getConditions());
            IExpression oldFrom = null;
            IExpression oldTo = null;
            if (from != null) {
                oldFrom = from.getRHS();
            }
            if (to != null) {
                oldTo = to.getRHS();
            }

            // prepare new values to set if we have them
            DateFormat format = DateFormat.getTimeInstance();
            Date[] times = (Date[]) timeInfo;
            IExpression fromExp = null;
            if (times[0] != null) {
                String fromString = format.format(times[0]);
                fromExp = TimeAttribute.TIME.build(fromString);
            }
            IExpression toExp = null;
            if (times[1] != null) {
                String toString = format.format(times[1]);
                toExp = TimeAttribute.TIME.build(toString);
            }

            if ((times[0] != null && !fromExp.equals(oldFrom)) 
             || (times[1] != null && !toExp.equals(oldTo))) 
            {
                PolicyUndoElement undo;
                // set the new values we have
                if (times[0] != null) {
                    PredicateHelpers.setDailyFromTime(policy, fromExp);
                    undo = new PolicyUndoElement(
                            PolicyUndoElementOp.CHANGE_DAILY_SCHEDULE_FROM
                          , oldFrom
                          , fromExp
                    );
                }
                if (times[1] != null) {
                    PredicateHelpers.setDailyToTime(policy, toExp);
                    undo = new PolicyUndoElement(
                            PolicyUndoElementOp.CHANGE_DAILY_SCHEDULE_TO
                          , oldTo
                          , toExp
                    );
                }
                //TODO why uncomment? It causes this whole method meaningless.
                // GlobalState.getInstance().addUndoElement(undo);
            }

        }
    }
    
    /*
     * </TimePredicateAccessor>
     */
    
    
    
    
    /*
     * <TimeRow>
     */
    
    protected abstract class TimeRow extends Composite {
        private static final int TIME_LABEL_WIDTH = 35;
        
        public TimePredicateAccessor accessor;
        public String labelString;
        public Label labelComp;
        public Button removeButton;
        public Button addButton;
        public Composite contents;

        protected TimeRow(Composite parent, String label, TimePredicateAccessor accessor) {
            super(parent, SWT.NONE);
            this.labelString = label;
            this.accessor = accessor;
            setLayout(new FormLayout());
            setBackground(parent.getBackground());
            addLabel();
        }

        /**
         * Initializes or refreshes the row.
         */
        public void setupRow() {
            if (!accessor.hasTimePredicate()) {
                if (panel.isEditable() && addButton == null) {
                    addAddButton();
                }
                if (removeButton != null) {
                    removeButton.dispose();
                    removeButton = null;
                }
                if (contents != null) {
                    contents.dispose();
                    contents = null;
                }
            } else {
                if (panel.isEditable() && removeButton == null) {
                    addRemoveButton();
                }
                if (contents == null) {
                    addContents();
                }
                if (addButton != null) {
                    addButton.dispose();
                    addButton = null;
                }
            }
            if (contents != null) {
                setStateFromDomainObject();
            }
        }

        public void addAddButton() {
            addButton = new Button(this, SWT.FLAT);
            addButton.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
            addButton.setText(EditorMessages.POLICYEDITOR_PLUS);
            addButton.setToolTipText(EditorMessages.POLICYEDITOR_ADD_CONDITION);

            FormData data = new FormData();
            data.left = new FormAttachment(0, 2 * EditorPanel.SPACING + TIME_LABEL_WIDTH);
            data.top = new FormAttachment(0, EditorPanel.SPACING);
            data.width = BUTTON_WIDTH;
            data.height = BUTTON_WIDTH;

            addButton.setLayoutData(data);

            addButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    accessor.addNewTimePredicate();
                    setupRow();
                    TimeRow.this.layout();
                    GlobalState.getInstance().getEditorPanel().relayout();
                }
            });
        }

        public void addRemoveButton() {
            removeButton = new Button(this, SWT.FLAT);
            removeButton.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
            removeButton.setText(EditorMessages.POLICYEDITOR_MINUS);
            removeButton.setToolTipText(EditorMessages.POLICYEDITOR_REMOVE_CONDITION);

            FormData data = new FormData();
            data.left = new FormAttachment(0, 2 * EditorPanel.SPACING + TIME_LABEL_WIDTH);
            data.top = new FormAttachment(0, EditorPanel.SPACING);
            data.width = BUTTON_WIDTH;
            data.height = BUTTON_WIDTH;

            removeButton.setLayoutData(data);

            removeButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    accessor.removeTimePredicate();
                    setupRow();
                    TimeRow.this.layout();
                    GlobalState.getInstance().getEditorPanel().relayout();
                }
            });
        }

        public void addLabel() {
            labelComp = new Label(this, SWT.NONE);
            labelComp.setText(labelString);
            labelComp.setEnabled(panel.isEditable());
            labelComp.setBackground(getParent().getBackground());
            FormData data = new FormData();
            data.left = new FormAttachment(0);
            data.top = new FormAttachment(0, EditorPanel.SPACING + 1);
            labelComp.setLayoutData(data);
        }

        public void addContents() {
            contents = new Composite(this, SWT.NONE);
            contents.setBackground(getParent().getBackground());
            populateContents(contents);
            FormData data = new FormData();
            data.left = new FormAttachment(0, TIME_LABEL_WIDTH + BUTTON_WIDTH + 3 * EditorPanel.SPACING);
            data.right = new FormAttachment(100);
            contents.setLayoutData(data);
        }

        /**
         * Subclasses should implement this to populate the contents composite.
         * This method is called whenever a new contents composite is created.
         * 
         * @param contents
         */
        public abstract void populateContents(Composite contents);

        /**
         * Subclasses should implement this to populate information from the
         * domain object into this control. This method will be called whenever
         * The object needs to be refreshed. It will not perform any necessary
         * setup, however, so if you want to refresh the row, you should call
         * SetupRow instead.
         */
        public abstract void setStateFromDomainObject();
    }
    
    protected class EnforcementTimeRow extends TimeRow {

        private CalendarPicker dateControl;
        private DateTime timeControl;
        private PolicyUndoElementOp<Date> modifyUndoOp;

        public EnforcementTimeRow(
                Composite parent
              , String label
              , TimePredicateAccessor accessor
              , PolicyUndoElementOp<Date> modifyUndoOp
        ) {
            super(parent, label, accessor);
            this.modifyUndoOp = modifyUndoOp;
        }

        @Override
        public void populateContents(Composite contents) {
            RowLayout layout = new RowLayout();
            contents.setLayout(layout);

            Calendar now = new GregorianCalendar();
            dateControl = new CalendarPicker(contents, SWT.BORDER);
            dateControl.setCalendar(now);
            timeControl = new DateTime(contents, SWT.TIME | SWT.SHORT | SWT.BORDER);
            timeControl.setHours(now.get(Calendar.HOUR_OF_DAY));
            timeControl.setMinutes(now.get(Calendar.MINUTE));
            timeControl.setSeconds(now.get(Calendar.SECOND));

            dateControl.setEnabled(panel.isEditable());
            timeControl.setEnabled(panel.isEditable());

            dateControl.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    Date controlDate = dateControl.getCalendar().getTime();
                    Calendar date = new GregorianCalendar();
                    date.setTimeInMillis(controlDate.getTime());
                    date.set(Calendar.HOUR_OF_DAY, timeControl.getHours());
                    date.set(Calendar.MINUTE, timeControl.getMinutes());
                    Relation rel = (Relation) accessor.getTimePredicate();
                    if (rel != null) {
                        Long dateVal = (Long) rel.getRHS().evaluate(null).getValue();
                        Calendar oldDate = new GregorianCalendar();
                        oldDate.setTimeInMillis(dateVal.longValue());
                        if (!date.equals(oldDate)) {
                            accessor.modifyTime(date.getTime());
                            createUndoModify(oldDate.getTime(), date.getTime());
                        }
                    }
                }
            });

            timeControl.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    Date controlDate = dateControl.getCalendar().getTime();
                    Calendar date = new GregorianCalendar();
                    date.setTimeInMillis(controlDate.getTime());
                    date.set(Calendar.HOUR_OF_DAY, timeControl.getHours());
                    date.set(Calendar.MINUTE, timeControl.getMinutes());
                    Relation rel = (Relation) accessor.getTimePredicate();
                    if (rel != null) {
                        Long dateVal = (Long) rel.getRHS().evaluate(null).getValue();
                        Calendar oldDate = new GregorianCalendar();
                        oldDate.setTimeInMillis(dateVal.longValue());
                        if (!date.equals(oldDate)) {
                            accessor.modifyTime(date.getTime());
                            createUndoModify(oldDate.getTime(), date.getTime());
                        }
                    }
                }
            });
        }

        @Override
        public void setStateFromDomainObject() {
            IPredicate pred = accessor.getTimePredicate();
            Long dateVal = (Long) ((IRelation) pred).getRHS().evaluate(null).getValue();
            Calendar date = new GregorianCalendar();
            date.setTimeInMillis(dateVal.longValue());
            Date oldDate = dateControl.getCalendar().getTime();
            Calendar oldCalendar = new GregorianCalendar();
            if (oldDate != null) {
                oldCalendar.setTime(oldDate);
            } else {
                oldCalendar = null;
            }

            if (dateControl != null && (oldCalendar == null 
                   || oldCalendar.get(Calendar.YEAR) != date.get(Calendar.YEAR) 
                   || oldCalendar.get(Calendar.MONTH) != date.get(Calendar.MONTH) 
                   || oldCalendar.get(Calendar.DAY_OF_MONTH) != date.get(Calendar.DAY_OF_MONTH)
                   )
            ) {
                dateControl.setCalendar(date);
            }
            if (timeControl != null 
                    && (timeControl.getHours() != date.get(Calendar.HOUR_OF_DAY) 
                     || timeControl.getMinutes() != date.get(Calendar.MINUTE)
            )) {
                timeControl.setHours(date.get(Calendar.HOUR_OF_DAY));
                timeControl.setMinutes(date.get(Calendar.MINUTE));
            }
        }

        public void createUndoModify(Date oldDate, Date newDate) {
            PolicyUndoElement undo = new PolicyUndoElement(modifyUndoOp, oldDate, newDate);
            
            //TODO why uncomment?
            // GlobalState.getInstance().addUndoElement(undo);
        }
    }
    
    protected class DailyScheduleTimeRow extends TimeRow {

        private DateTime from;
        private DateTime to;

        public DailyScheduleTimeRow(Composite parent, String label) {
            super(parent, label, new DailyScheduleAccessor());
        }

        @Override
        public void populateContents(Composite contents) {
            FormLayout layout = new FormLayout();
            contents.setLayout(layout);
            Label fromLabel = new Label(contents, SWT.NONE);
            fromLabel.setEnabled(panel.isEditable());
            fromLabel.setText(EditorMessages.POLICYEDITOR_FROM);
            fromLabel.setBackground(getBackground());
            FormData fromData = new FormData();
            fromData.left = new FormAttachment(0, 2 * EditorPanel.SPACING);
            fromData.top = new FormAttachment(0, EditorPanel.SPACING + 1);
            fromLabel.setLayoutData(fromData);

            from = new DateTime(contents, SWT.TIME | SWT.SHORT | SWT.BORDER);
            FormData fcData = new FormData();
            fcData.left = new FormAttachment(fromLabel);
            fcData.top = new FormAttachment(fromLabel, 0, SWT.CENTER);
            from.setLayoutData(fcData);
            from.setEnabled(panel.isEditable());

            from.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    int hours = from.getHours();
                    int minutes = from.getMinutes();

                    Relation rel = (Relation) ((CompositePredicate) accessor.getTimePredicate()).predicateAt(0);
                    if (rel != null) {
                        Long oldVal = (Long) rel.getRHS().evaluate(null).getValue();
                        Calendar oldDate = new GregorianCalendar();
                        oldDate.setTimeInMillis(oldVal.longValue());

                        if (oldDate.get(Calendar.HOUR_OF_DAY) != hours 
                                || oldDate.get(Calendar.MINUTE) != minutes) 
                        {
                            Calendar fd = new GregorianCalendar();
                            fd.set(Calendar.YEAR, 0);
                            fd.set(Calendar.MONTH, 0);
                            fd.set(Calendar.DAY_OF_MONTH, 0);
                            fd.set(Calendar.HOUR_OF_DAY, hours);
                            fd.set(Calendar.MINUTE, minutes);

                            Date td = null;
                            accessor.modifyTime(new Date[] { fd.getTime(), td });
                        }
                    }
                }
            });

            Label toLabel = new Label(contents, SWT.NONE);
            toLabel.setEnabled(panel.isEditable());
            toLabel.setText(EditorMessages.POLICYEDITOR_TO);
            toLabel.setBackground(getBackground());
            FormData tlData = new FormData();
            tlData.left = new FormAttachment(from, EditorPanel.SPACING);
            tlData.top = new FormAttachment(0, EditorPanel.SPACING + 1);
            toLabel.setLayoutData(tlData);

            to = new DateTime(contents, SWT.TIME | SWT.SHORT | SWT.BORDER);
            FormData tcData = new FormData();
            tcData.left = new FormAttachment(toLabel);
            tcData.top = new FormAttachment(toLabel, 0, SWT.CENTER);
            to.setLayoutData(tcData);
            to.setEnabled(panel.isEditable());

            to.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    int hours = to.getHours();
                    int minutes = to.getMinutes();

                    Relation rel = (Relation) ((CompositePredicate) accessor.getTimePredicate()).predicateAt(1);
                    if (rel != null) {
                        Long oldVal = (Long) rel.getRHS().evaluate(null).getValue();
                        Calendar oldDate = new GregorianCalendar();
                        oldDate.setTimeInMillis(oldVal.longValue());

                        if (oldDate.get(Calendar.HOUR_OF_DAY) != hours 
                  || oldDate.get(Calendar.MINUTE) != minutes) {
                            Date fd = null;
                            Calendar td = new GregorianCalendar();
                            td.set(Calendar.YEAR, 0);
                            td.set(Calendar.MONTH, 0);
                            td.set(Calendar.DAY_OF_MONTH, 0);
                            td.set(Calendar.HOUR_OF_DAY, hours);
                            td.set(Calendar.MINUTE, minutes);
                            accessor.modifyTime(new Date[] { fd, td.getTime() });
                        }
                    }
                }
            });
        }

        @Override
        public void setStateFromDomainObject() {
            // The Time attribute uses the local time. This code parses the
            // representation instead of relying on the Long value stored
            // in the internal representation to avoid correcting for time zone
            // twice.
            try {
                IPredicate pred = accessor.getTimePredicate();
                Relation fromRel = (Relation) ((CompositePredicate) pred).predicateAt(0);
                Constant fromConst = (Constant) fromRel.getRHS();
                Calendar fromDate = new GregorianCalendar();
                fromDate.setTime(DateFormat.getTimeInstance().parse(unquote(fromConst.getRepresentation())));

                if (fromDate.get(Calendar.HOUR_OF_DAY) != from.getHours() 
                        || fromDate.get(Calendar.MINUTE) != from.getMinutes()) 
                  {
                    from.setHours(fromDate.get(Calendar.HOUR_OF_DAY));
                    from.setMinutes(fromDate.get(Calendar.MINUTE));
                }
                Relation toRel = (Relation) ((CompositePredicate) pred).predicateAt(1);
                Constant toConst = (Constant) toRel.getRHS();
                Calendar toDate = new GregorianCalendar();
                toDate.setTime(DateFormat.getTimeInstance().parse(unquote(toConst.getRepresentation())));
                if (toDate.get(Calendar.HOUR_OF_DAY) != to.getHours() 
                        || toDate.get(Calendar.MINUTE) != to.getMinutes()) {
                    to.setHours(toDate.get(Calendar.HOUR_OF_DAY));
                    to.setMinutes(toDate.get(Calendar.MINUTE));
                }
            } catch (ParseException pe) {
                // This will not happen because PQL parsed this successfully
            }
        }

        private String unquote(String s) {
            if (s == null) {
                return null;
            }
            if (s.length() < 2 || s.charAt(0) != '"' || s.charAt(s.length() - 1) != '"') {
                return s;
            } else {
                return s.substring(1, s.length() - 1);
            }
        }
    }

    protected class RecurringScheduleTimeRow extends TimeRow {
   
        private Button weekRadio;
        private Button dayNumRadio;
        private Button dowimRadio;
        private Combo dayNum;
        private Combo dayCountCombo;
        private Combo daysCombo;
        private List<Button> weekdayButtonList;

        public RecurringScheduleTimeRow(Composite parent, String label) {
            super(parent, label, new RecurringScheduleAccessor());
        }

        @Override
        public void populateContents(Composite contents) {
            Color background = getBackground();
            contents.setLayout(new FillLayout());

            Group group = new Group(contents, SWT.NONE);
            group.setBackground(background);

            GridLayout layout = new GridLayout();
            layout.numColumns = 2;
            group.setLayout(layout);

            weekRadio = new Button(group, SWT.RADIO);
            weekRadio.setText("");
            weekRadio.setBackground(background);
            weekRadio.setEnabled(panel.isEditable());
            weekRadio.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (weekRadio.getSelection()) {
                        IDPolicy policy = clientPolicy;
                        // get the old time predicate
                        IPredicate oldValue = accessor.getTimePredicate();
                        // remove it
                        if (oldValue != null) {
                            PredicateHelpers.removeDayOfMonthPredicate(policy);
                            PredicateHelpers.removeWeekdayPredicate(policy);
                            PredicateHelpers.removeDOWIMPredicate(policy);
                        }
                        // add a new weekday predicate
                        ((RecurringScheduleAccessor) accessor).addNewWeekdayPredicate();
                        // save undo information
                        addConditionUndoElement(
                                PolicyUndoElementOp.CHANGE_RECURRENCE_PREDICATE
                              , oldValue
                              , accessor.getTimePredicate()
                        );
                        // refresh the control
                        setStateFromDomainObject();
                    }
                }
            });

            Composite weekPanel = new Composite(group, SWT.NONE);

            dayNumRadio = new Button(group, SWT.RADIO);
            dayNumRadio.setText("");
            dayNumRadio.setBackground(background);
            dayNumRadio.setEnabled(panel.isEditable());
            dayNumRadio.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (dayNumRadio.getSelection()) {
                        IDPolicy policy = clientPolicy;
                        // get the old time predicate
                        IPredicate oldValue = accessor.getTimePredicate();
                        // remove it
                        if (oldValue != null) {
                            PredicateHelpers.removeDayOfMonthPredicate(policy);
                            PredicateHelpers.removeWeekdayPredicate(policy);
                            PredicateHelpers.removeDOWIMPredicate(policy);
                        }
                        // add a new day number predicate
                        String dayNumText = dayNum.getText();
                        if ("".equals(dayNumText)) {
                            dayNumText = "1";
                        }
                        PredicateHelpers.setDailyOfMonthTime(policy, TimeAttribute.DATE.build(dayNumText));
                        // add undo info
                        addConditionUndoElement(
                                PolicyUndoElementOp.CHANGE_RECURRENCE_PREDICATE
                              , oldValue
                              , accessor.getTimePredicate()
                        );
                        // refresh the control
                        setStateFromDomainObject();
                    }
                }
            });

            Composite dayPanel = new Composite(group, SWT.NONE);
            dayPanel.setBackground(background);

            dowimRadio = new Button(group, SWT.RADIO);
            dowimRadio.setText("");
            dowimRadio.setBackground(background);
            dowimRadio.setEnabled(panel.isEditable());
            dowimRadio.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (dowimRadio.getSelection()) {
                        IDPolicy policy = clientPolicy;
                        // get the old time predicate
                        IPredicate oldValue = accessor.getTimePredicate();
                        // remove it
                        if (oldValue != null) {
                            PredicateHelpers.removeDayOfMonthPredicate(policy);
                            PredicateHelpers.removeWeekdayPredicate(policy);
                            PredicateHelpers.removeDOWIMPredicate(policy);
                        }
                        // add a new dowim predicate
                        int count = dayCountCombo.getSelectionIndex();
                        if (count == -1) {
                            count = 1;
                        } else if (count == CommonPolicyConstants.DAY_COUNT_LABELS.length - 1) {
                            count = -1;
                        } else {
                            count++;
                        }

                        int dayIndex = daysCombo.getSelectionIndex();
                        if (dayIndex == -1) {
                            dayIndex = 0;
                        }
                        Relation dayRel = new Relation(
                                RelationOp.EQUALS
                              , TimeAttribute.WEEKDAY
                              , TimeAttribute.WEEKDAY.build(CommonPolicyConstants.DAY_NAMES[dayIndex])
                       );

                        PredicateHelpers.setDOWIM(policy, TimeAttribute.DOWIM.build("" + count));
                        PredicateHelpers.setWeekdayPredicate(policy, dayRel);
                        
                        // add undo info
                        addConditionUndoElement(
                                PolicyUndoElementOp.CHANGE_RECURRENCE_PREDICATE
                              , oldValue
                              , accessor.getTimePredicate()
                        );
                        // refresh the control
                        setStateFromDomainObject();
                    }
                }
            });

            Composite dowimPanel = new Composite(group, SWT.NONE);
            dowimPanel.setBackground(background);

            populateWeekPanel(weekPanel);
            populateDayPanel(dayPanel);
            populateDowimPanel(dowimPanel);
        }

        private void populateWeekPanel(Composite weekPanel) {
            Color background = getBackground();

            GridLayout layout = new GridLayout(7, true);
            weekPanel.setLayout(layout);
            weekPanel.setBackground(background);

            for (String label : CommonPolicyConstants.DAY_LABELS) {
                Label l = new Label(weekPanel, SWT.NONE);
                l.setEnabled(panel.isEditable());
                GridData data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
                l.setLayoutData(data);
                l.setText(label);
                l.setBackground(background);
            }

            if (weekdayButtonList == null) {
                weekdayButtonList = new ArrayList<Button>();
            } else {
                weekdayButtonList.clear();
            }
            for (int i = 0; i < 7; i++) {
                Button b = new Button(weekPanel, SWT.CHECK);
                GridData data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
                b.setLayoutData(data);
                weekdayButtonList.add(b);
                b.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Button b = (Button) e.getSource();
                        int index = weekdayButtonList.indexOf(b);
                        IDPolicy policy = clientPolicy;
                        String name = CommonPolicyConstants.DAY_NAMES[index].toLowerCase();
                        if (b.getSelection()) {
                            // add a new weekday relation
                            PredicateHelpers.addWeekdayExpressionToConditions(policy, name);
                            PolicyUndoElement.add(PolicyUndoElementOp.ADD_WEEKDAY, null, name);
                        } else {
                            // remove a weekday relation and resave
                            PredicateHelpers.removeWeekdayExpressionFromConditions(policy, name);
                            PolicyUndoElement.add(PolicyUndoElementOp.REMOVE_WEEKDAY, name, null);
                        }
                        setStateFromDomainObject();
                    }
                });
            }
        }

        private void populateDayPanel(Composite dayPanel) {
            Color background = getBackground();
            FormLayout layout = new FormLayout();
            dayPanel.setLayout(layout);

            Label t1 = new Label(dayPanel, SWT.NONE);
            t1.setEnabled(panel.isEditable());
            t1.setText(EditorMessages.POLICYEDITOR_DAYS);
            t1.setBackground(background);
            FormData t1Data = new FormData();
            t1Data.left = new FormAttachment(0, EditorPanel.SPACING);
            t1.setLayoutData(t1Data);

            dayNum = new Combo(dayPanel, SWT.READ_ONLY);
            String[] list = new String[31];
            for (int i = 0; i < 31; i++) {
                list[i] = String.valueOf(i + 1);
            }
            dayNum.setItems(list);
            dayNum.select(0);
            FormData dnData = new FormData();
            dnData.left = new FormAttachment(t1, EditorPanel.SPACING);
            dayNum.setLayoutData(dnData);
            dayNum.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    IDPolicy policy = clientPolicy;
                    Relation pred = (Relation) PredicateHelpers.getDayOfMonthPredicate(policy.getConditions());
                    if (!pred.getRHS().toString().equals(dayNum.getText())) {
                        IExpression oldValue = pred.getRHS();
                        IExpression newValue = TimeAttribute.DATE.build(dayNum.getText());
                        pred.setRHS(newValue);
                        addConditionUndoElement(PolicyUndoElementOp.CHANGE_RECURRENCE_DATE, oldValue, newValue);
                    }
                }
            });

            Label t2 = new Label(dayPanel, SWT.NONE);
            t2.setEnabled(panel.isEditable());
            t2.setText(EditorMessages.POLICYEDITOR_OF_EVERY_MONTH);
            t2.setBackground(background);
            FormData t2Data = new FormData();
            t2Data.left = new FormAttachment(dayNum, EditorPanel.SPACING);
            t2.setLayoutData(t2Data);
        }

        private void populateDowimPanel(Composite dowimPanel) {
            Color background = getBackground();
            FormLayout layout = new FormLayout();
            dowimPanel.setLayout(layout);

            Label t1 = new Label(dowimPanel, SWT.NONE);
            t1.setEnabled(panel.isEditable());
            t1.setText(EditorMessages.POLICYEDITOR_THE);
            t1.setBackground(background);
            FormData t1Data = new FormData();
            t1Data.left = new FormAttachment(0, EditorPanel.SPACING);
            t1.setLayoutData(t1Data);

            dayCountCombo = new Combo(dowimPanel, SWT.READ_ONLY);
            dayCountCombo.setItems(CommonPolicyConstants.DAY_COUNT_LABELS);
            dayCountCombo.select(0);
            FormData dcData = new FormData();
            dcData.left = new FormAttachment(t1, EditorPanel.SPACING);
            dayCountCombo.setLayoutData(dcData);
            dayCountCombo.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    IDPolicy policy = clientPolicy;
                    Relation rel = (Relation) PredicateHelpers.getDOWIMPredicate(policy.getConditions());
                    int count = dayCountCombo.getSelectionIndex();
                    if (count == -1) {
                        count = 1;
                    } else if (count == CommonPolicyConstants.DAY_COUNT_LABELS.length - 1) {
                        count = -1;
                    } else {
                        count++;
                    }
                    if (!rel.getRHS().toString().equals("" + count)) {
                        IExpression oldValue = rel.getRHS();
                        IExpression newValue = TimeAttribute.DOWIM.build("" + count);
                        rel.setRHS(newValue);
                        addConditionUndoElement(
                                PolicyUndoElementOp.CHANGE_RECURRENCE_DOWIM
                              , oldValue
                              , newValue
                        );
                    }
                }
            });

            daysCombo = new Combo(dowimPanel, SWT.READ_ONLY);
            daysCombo.setItems(CommonPolicyConstants.DAY_NAMES);
            daysCombo.select(0);
            FormData dData = new FormData();
            dData.left = new FormAttachment(dayCountCombo, EditorPanel.SPACING);
            daysCombo.setLayoutData(dData);
            daysCombo.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    IDPolicy policy = clientPolicy;
                    Relation rel = (Relation) PredicateHelpers.getWeekDayPredicate(policy.getConditions());
                    String oldValue = rel.getRHS().toString();
                    // remove the quotes that this always seems to have:
                    oldValue = oldValue.substring(1, oldValue.length() - 1);
                    if (!oldValue.toLowerCase().equals(daysCombo.getText().toLowerCase())) {
                        IExpression oldExp = rel.getRHS();
                        IExpression newExp = TimeAttribute.WEEKDAY.build(daysCombo.getText());
                        rel.setRHS(newExp);
                        addConditionUndoElement(
                                PolicyUndoElementOp.CHANGE_RECURRENCE_WEEKDAY
                              , oldExp
                              , newExp
                        );
                    }
                }
            });

            Label t2 = new Label(dowimPanel, SWT.NONE);
            t2.setEnabled(panel.isEditable());
            t2.setText(EditorMessages.POLICYEDITOR_OF_EVERY_MONTH);
            t2.setBackground(background);
            FormData t2Data = new FormData();
            t2Data.left = new FormAttachment(daysCombo, EditorPanel.SPACING);
            t2.setLayoutData(t2Data);
        }

        @Override
        public void setStateFromDomainObject() {
            for (Button button : weekdayButtonList) {
                button.setSelection(false);
            }

         
            IDPolicy policy = clientPolicy;
            IPredicate dowim = PredicateHelpers.getDOWIMPredicate(policy.getConditions());
            IPredicate weekday = PredicateHelpers.getWeekDayPredicate(policy.getConditions());
            if (dowim != null) {
                // setup dowim section
                weekRadio.setSelection(false);
                dayNumRadio.setSelection(false);
                dowimRadio.setSelection(true);

                IExpression exp = ((Relation) dowim).getRHS();
                Long storedCount = (Long) exp.evaluate(null).getValue();
                int count = storedCount.intValue();
                if (count == -1) {
                    // assuming the last index is "last"
                    count = CommonPolicyConstants.DAY_COUNT_LABELS.length;
                }
                count--; // subtract 1 to get the array index;

                dayCountCombo.select(count);

                IExpression weekdayExp = ((Relation) weekday).getRHS();
                String savedValue = weekdayExp.toString();
                // remove the quotes that this always seems to have:
                savedValue = savedValue.substring(1, savedValue.length() - 1);

                for (int i = 0; i < CommonPolicyConstants.DAY_NAMES.length; i++) {
                    if (CommonPolicyConstants.DAY_NAMES[i].toLowerCase().equals(savedValue.toLowerCase())) {
                        daysCombo.select(i);
                        break;
                    }
                }

                daysCombo.setEnabled(true && panel.isEditable());
                dayCountCombo.setEnabled(true && panel.isEditable());
                dayNum.setEnabled(false);
                for (Button button : weekdayButtonList) {
                    button.setEnabled(false);
                }

            } else if (weekday != null) {
                // setup weekday section
                weekRadio.setSelection(true);
                dayNumRadio.setSelection(false);
                dowimRadio.setSelection(false);

                for (Button button : weekdayButtonList) {
                    button.setEnabled(true);
                }

                if (weekday instanceof Relation) {
                    Button b = getButtonForExpression(((Relation) weekday).getRHS());
                    b.setSelection(true);
                    b.setEnabled(false); // don't let people remove the last
                    // day
                    // because we have no way to track an empty weekday section.
                } else if (weekday instanceof CompositePredicate) {
                    List<IPredicate> list = ((CompositePredicate) weekday).predicates();
                    for (IPredicate predicate : list) {
                        IExpression exp = ((Relation) predicate).getRHS();
                        Button b = getButtonForExpression(exp);
                        b.setSelection(true);
                        b.setEnabled(true && panel.isEditable());
                    }
                }

                daysCombo.setEnabled(false);
                dayCountCombo.setEnabled(false);
                dayNum.setEnabled(false);

            } else {
                IPredicate day = PredicateHelpers.getDayOfMonthPredicate(policy.getConditions());
                if (day != null) {
                    // setup day section
                    weekRadio.setSelection(false);
                    dayNumRadio.setSelection(true);
                    dowimRadio.setSelection(false);

                    String dayText = ((Relation) day).getRHS().toString();
                    dayNum.setText(dayText);
                }

                daysCombo.setEnabled(false);
                dayCountCombo.setEnabled(false);
                dayNum.setEnabled(true && panel.isEditable());
                for (Button button : weekdayButtonList) {
                    button.setEnabled(false);
                }
            }
        }

        private Button getButtonForExpression(IExpression exp) {
            Long i = (Long) exp.evaluate(null).getValue();
            return (Button) weekdayButtonList.get(i.intValue() - 1);
        }

        private <T> void addConditionUndoElement(
                PolicyUndoElementOp<T> op
              , T oldCondition
              , T newCondition
        ) {
            PolicyUndoElement.add(op, oldCondition, newCondition);
        }
    }
    
    /*
     * </TimeRow>
     */
    
    
    
    
    
    /**
     * @author sgoldstein
     */
    public class ObjectModifiedListner implements IContextualEventListener {

        /**
         * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
         */
        public void onEvent(IContextualEvent event) {
            updateFromDomainObject();
        }
    }
    
}
