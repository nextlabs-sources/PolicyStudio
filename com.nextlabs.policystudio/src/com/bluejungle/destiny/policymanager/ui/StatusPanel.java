/*
 * Created on Mar 18, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.action.PolicyStudioActionFactory;
import com.bluejungle.destiny.policymanager.event.CurrentPolicyOrComponentChangedEvent;
import com.bluejungle.destiny.policymanager.event.CurrentPolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.EventType;
import com.bluejungle.destiny.policymanager.event.IEvent;
import com.bluejungle.destiny.policymanager.event.IEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author fuad
 */

public class StatusPanel extends Composite {

    private CLabel selectedLabel;
    
    private Label statusLabel;
    private Label statusValue;
    
    private Label lastModifiedLabel;
    private Label lastModifiedValue;
    
    private Label submitterLabel;
    private Label submitterValue;
    
    private Button modifyButton;
    private Button submitButton;
    private Button deployButton;
    private IHasId currentObject;

    /**
     * Constructor
     * 
     * @param parent
     * @param style
     */
    public StatusPanel(Composite parent, int style) {
        super(parent, style);
        setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        initialize();
        relayout();

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IEventManager eventManager = componentManager.getComponent(EventManagerImpl.COMPONENT_INFO);
        eventManager.registerListener(new CurrentObjectChangedListener(),
                EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT);
        eventManager.registerListener(new CurrentObjectModifiedListener(),
                EventType.CURRENT_POLICY_OR_COMPONENT_MODIFIED_EVENT);
    }

    /**
     * @param currentObject
     * 
     */
    protected void refresh(IPolicyOrComponentData currentObject) {
        updateStatus(currentObject);
    }

    /**
     * 
     */
    private void initialize() {
        /*
         * +--------------------------+ +-------------------------------------+
         * |buttonComposite           | |infoComposite                        |
         * |                          | |                                     |
         * |+------+ +------+ +------+| |       statusLabel statusValue       |
         * ||modify| |submit| |deploy|| | lastModifiedLabel lastModifiedValue |
         * |+------+ +------+ +------+| |    submitterLabel submitterValue    |
         * +--------------------------+ +-------------------------------------+
         * 
         */
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        layout.spacing = 10;
        setLayout(layout);

        Composite buttonComposite = new Composite(this, SWT.NONE);
        buttonComposite.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        FormData formData = new FormData();
        formData.top = new FormAttachment(null, 0, SWT.CENTER);
        formData.left = new FormAttachment(25);
        buttonComposite.setLayoutData(formData);
        buttonComposite.setLayout(new FormLayout());

        selectedLabel = new CLabel(this, SWT.LEFT);
        selectedLabel.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        FormData fd = new FormData();
        fd.top = new FormAttachment(buttonComposite, 0, SWT.CENTER);
        fd.left = new FormAttachment(0, 0);
        fd.right = new FormAttachment(buttonComposite);
        selectedLabel.setLayoutData(fd);

        Composite infoComposite = new Composite(this, SWT.NONE);
        infoComposite.setBackground(getBackground());
        GridLayout gridLayout = new GridLayout(2, false);
        infoComposite.setLayout(gridLayout);

        formData = new FormData();
//        formData.top = new FormAttachment(buttonComposite, 0, SWT.CENTER);
        formData.left = new FormAttachment(buttonComposite);
        infoComposite.setLayoutData(formData);
        
        // status
        
        statusLabel = new Label(infoComposite, SWT.NONE);
        statusLabel.setBackground(getBackground());
        statusLabel.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        statusLabel.setText(ApplicationMessages.STATUSPANEL_STATUS);
        GridData gridDate = new GridData();
        gridDate.horizontalAlignment = SWT.RIGHT;
        statusLabel.setLayoutData(gridDate);

        statusValue = new Label(infoComposite, SWT.NONE);
        statusValue.setBackground(getBackground());
        gridDate = new GridData();
        statusValue.setLayoutData(gridDate);

        // last modified
        lastModifiedLabel = new Label(infoComposite, SWT.NONE);
        lastModifiedLabel.setBackground(getBackground());
        lastModifiedLabel.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        lastModifiedLabel.setText("Last Modified:");
        gridDate = new GridData();
        gridDate.horizontalAlignment = SWT.RIGHT;
        lastModifiedLabel.setLayoutData(gridDate);

        lastModifiedValue = new Label(infoComposite, SWT.NONE);
        lastModifiedValue.setBackground(getBackground());
        gridDate = new GridData();
        
        lastModifiedValue.setLayoutData(gridDate);
        
        //submitter
        submitterLabel = new Label(infoComposite, SWT.NONE);
        submitterLabel.setBackground(getBackground());
        submitterLabel.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        submitterLabel.setText("Submitted by:");
        gridDate = new GridData();
        gridDate.horizontalAlignment = SWT.RIGHT;
        submitterLabel.setLayoutData(gridDate);

        submitterValue = new Label(infoComposite, SWT.NONE);
        submitterValue.setBackground(getBackground());
        gridDate = new GridData();
        submitterValue.setLayoutData(gridDate);

        modifyButton = new Button(buttonComposite, SWT.PUSH);
        modifyButton.setText(ApplicationMessages.STATUSPANEL_MODIFY);
        submitButton = new Button(buttonComposite, SWT.PUSH);
        submitButton.setText(ApplicationMessages.STATUSPANEL_SUBMIT);
        deployButton = new Button(buttonComposite, SWT.PUSH);
        deployButton.setText(ApplicationMessages.STATUSPANEL_DEPLOY);

        modifyButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	String name = DomainObjectHelper.getName(currentObject);
            	EntityType type= DomainObjectHelper.getEntityType(currentObject);
            	GlobalState.getInstance().setCurrentlySelection(PolicyServerProxy.getEntityList(name, (Arrays.asList(new EntityType[] { type }))));
                deployButton.setVisible(false);
                IAction action = PolicyStudioActionFactory.getModifyAction();
                action.run();
            }
        });
        submitButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	String name = DomainObjectHelper.getName(currentObject);
            	EntityType type= DomainObjectHelper.getEntityType(currentObject);
            	GlobalState.getInstance().setCurrentlySelection(PolicyServerProxy.getEntityList(name, (Arrays.asList(new EntityType[] { type }))));
                IAction action = PolicyStudioActionFactory.getSubmitForDeploymentAction();
                action.run();
            }
        });
        deployButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	String name = DomainObjectHelper.getName(currentObject);
            	EntityType type= DomainObjectHelper.getEntityType(currentObject);
            	GlobalState.getInstance().setCurrentlySelection(PolicyServerProxy.getEntityList(name, (Arrays.asList(new EntityType[] { type }))));
                IAction action = PolicyStudioActionFactory.getScheduleDeploymentAction();
                action.run();
            }
        });

        // form attachment for deploy button never needs to change
        formData = new FormData();
        formData.top = new FormAttachment(0, 0);
        formData.left = new FormAttachment(modifyButton, 5, SWT.RIGHT);
        deployButton.setLayoutData(formData);

        setNoObjectActive();
        layout();
    }

    private String getLastNameComponent(String aName) {
        int i = aName.lastIndexOf(PQLParser.SEPARATOR);
        if (i != -1) {
            return aName.substring(i + 1);
        } else {
            return aName;
        }
    }

    /**
     * Updates the state of the status panel based on the new object or the new
     * state of the current object
     * 
     * @param currentObjectEventData
     */
    private void updateStatus(IPolicyOrComponentData currentObjectEventData) {
        IHasId newObject = currentObjectEventData.getEntity();
        currentObject = newObject;

        
        String entityName;
        if (currentObject != null) {
            entityName = getLastNameComponent(DomainObjectHelper.getDisplayName(currentObject));
        } else {
            entityName = "";
        }
        selectedLabel.setText(entityName);
        
        DomainObjectDescriptor descriptor = currentObjectEventData.getDescriptor();
        
        /*
         * check if the lock on this object as already acquired by us. If not we disable
         * all the buttons 
         */
        boolean acquiredLock = false;
        try {
        	acquiredLock = PolicyServerProxy.hasLock(currentObjectEventData.getEntity());
        }
        catch (PolicyEditorException e) {
        	e.printStackTrace();
        }
        
        // FIX ME - Need case when there is no active editor!!!
        if (currentObject != null) {
            try {
                statusLabel.setVisible(true);
                DomainObjectUsage entityUsage = currentObjectEventData.getEntityUsage();
                String statusKey = DomainObjectHelper.getDeploymentStatusKey(
                        descriptor, entityUsage);
                String statusText = DomainObjectHelper.getStatusText(statusKey);
                String deploymentText = DomainObjectHelper.getDeploymentText(statusKey);
                if (deploymentText != null && deploymentText.length() != 0) {
                    statusValue.setText(statusText + " (" + deploymentText + ")");
                } else {
                    statusValue.setText(statusText);
                }
                statusValue.pack();
                
                if (descriptor != null) {
                    Long modifierId = descriptor.getModifier();
                    String modifierName = PolicyServerProxy.getUserName(modifierId);
                    lastModifiedLabel.setVisible(true);
                    lastModifiedValue.setText(descriptor.getLastModified() + " " + modifierName);
                    lastModifiedValue.pack();
                }

                DevelopmentStatus status = DomainObjectHelper.getStatus(currentObject);
                if ((status == DevelopmentStatus.APPROVED)
                        || ((status == DevelopmentStatus.OBSOLETE) 
                                && (entityUsage.getCurrentlydeployedvcersion() != null))) {
                    setSubmitInfo(descriptor);
                    
                    modifyButton.setVisible(true);
                    submitButton.setVisible(false);
                    deployButton.setVisible(true);
                    FormData formData = new FormData();
                    formData.top = new FormAttachment(0, 0);
                    formData.left = new FormAttachment(0, 0);
                    modifyButton.setLayoutData(formData);
                    formData = new FormData(0, 0);
                    submitButton.setLayoutData(formData);
                } else if ((status == DevelopmentStatus.OBSOLETE)
                        && (entityUsage.getCurrentlydeployedvcersion() == null)) {
                    setSubmitInfo(descriptor);
                    
                    modifyButton.setVisible(true);
                    submitButton.setVisible(false);
                    deployButton.setVisible(false);
                    FormData formData = new FormData();
                    formData.top = new FormAttachment(0, 0);
                    formData.left = new FormAttachment(0, 0);
                    modifyButton.setLayoutData(formData);
                } else {
                    submitterLabel.setVisible(false);
                    submitterValue.setText("");
                    
                    modifyButton.setVisible(false);
                    submitButton.setVisible(true);
                    deployButton.setVisible(false);
                    FormData formData = new FormData(0, 0);
                    modifyButton.setLayoutData(formData);
                    formData = new FormData();
                    formData.top = new FormAttachment(0, 0);
                    formData.left = new FormAttachment(0, 0);
                    submitButton.setLayoutData(formData);
                }
            } catch (PolicyEditorException exception) {
                LoggingUtil.logWarning(
                        Activator.ID
                      , "Failed to load object usage for current entity.  Status panel text will not be accurate."
                      , exception);

                statusValue.setText("");
                lastModifiedValue.setText("");
                modifyButton.setVisible(false);
                submitButton.setVisible(false);
                deployButton.setVisible(false);
                
                exception.printStackTrace();
            }

			Collection<? extends com.bluejungle.pf.domain.epicenter.action.IAction> allowedActions = null;
			if (acquiredLock) {
				allowedActions = PolicyServerProxy.allowedActions(currentObject);
			}
			modifyButton.setEnabled((allowedActions != null && allowedActions
					.contains(DAction.WRITE)) && acquiredLock);
			submitButton.setEnabled((allowedActions != null && allowedActions
					.contains(DAction.APPROVE)) && acquiredLock);
			deployButton.setEnabled((allowedActions != null && allowedActions
					.contains(DAction.DEPLOY)) && acquiredLock);

        } else {
            setNoObjectActive();
        }

        layout();
    }
    
    private void setSubmitInfo(DomainObjectDescriptor descriptor) throws PolicyEditorException {
        if (descriptor != null) {
            submitterLabel.setVisible(true);
            if (descriptor.getLastSubmitted() != null) {
                submitterValue.setText(descriptor.getLastSubmitted() + " "
                        + PolicyServerProxy.getUserName(descriptor.getSubmitter()));
            } else {
                submitterValue.setText("");
            }
           
            submitterValue.pack();
        } else {
            submitterLabel.setVisible(false);
            submitterValue.setText("");
        }
    }

    /**
     * Sets the status panel to represent no active object
     */
    private void setNoObjectActive() {
        if (!selectedLabel.isDisposed()) {
            selectedLabel.setText("");
        }
        if (!statusLabel.isDisposed()) {
            statusLabel.setVisible(false);
        }
        if (!statusValue.isDisposed()) {
            statusValue.setText("");
        }
        
        if (!lastModifiedLabel.isDisposed()) {
            lastModifiedLabel.setVisible(false);
        }
        if (!lastModifiedValue.isDisposed()) {
            lastModifiedValue.setText("");
        }
        
        if (!submitterLabel.isDisposed()) {
            submitterLabel.setVisible(false);
        }
        if (!submitterValue.isDisposed()) {
            submitterValue.setText("");
        }
        
        if (!modifyButton.isDisposed()) {
            modifyButton.setVisible(false);
        }
        if (!submitButton.isDisposed()) {
            submitButton.setVisible(false);
        }
        if (!deployButton.isDisposed()) {
            deployButton.setVisible(false);
        }
    }
    
    /**
     * 
     */
    private void relayout() {
    }

    private class CurrentObjectChangedListener implements IEventListener {

        /**
         * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
         */
        public void onEvent(IEvent event) {
            CurrentPolicyOrComponentChangedEvent currentObjectChangedEvent 
                    = (CurrentPolicyOrComponentChangedEvent) event;
            if (currentObjectChangedEvent.currentObjectExists()) {
                IPolicyOrComponentData currentObject = currentObjectChangedEvent.getNewCurrentObject();
                StatusPanel.this.refresh(currentObject);
            } else {
                StatusPanel.this.currentObject = null;
                StatusPanel.this.setNoObjectActive();
            }
        }
    }

    private class CurrentObjectModifiedListener implements IEventListener {

        /**
         * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
         */
        public void onEvent(IEvent event) {
            CurrentPolicyOrComponentModifiedEvent currentObjectModifiedEvent 
                    = (CurrentPolicyOrComponentModifiedEvent) event;
            IPolicyOrComponentData currentObject = currentObjectModifiedEvent.getCurrentObject();
            StatusPanel.this.refresh(currentObject);
        }
    }
}
