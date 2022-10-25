/*
 * Created on Aug 21, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.tab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.model.EntityInformation;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyManagerView;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.dialogs.DialogMessages;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.pf.destiny.lib.AgentStatusDescriptor;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author bmeng
 * @version $Id$
 */

public class DeployedTab extends AbstractTab {
    private ToolItem submitItem;
    private CTabItem itemHosts;
    private Composite hostComposite;

    public DeployedTab(CTabFolder folder, PolicyManagerView view) {
        super(folder, view);
    }
    
    @Override
    protected void addColumns(List<TabColumn> columns) {
        super.addColumns(columns);
        
        int i = columns.indexOf(TabColumn.OWNED_BY_COLUMN);
        if (i == -1) {
            //should not happen but we can continue without lost the functionality
            
            LoggingUtil.logWarning(Activator.ID,
                    "Can't find owned by column, the new columns will added to the end", null);
            
            columns.add(TabColumn.MODIFIED_BY_COLUMN);
            columns.add(TabColumn.SUBMITTED_BY_COLUMN);
        }else {
            columns.add(i,    TabColumn.MODIFIED_BY_COLUMN);
            columns.add(i +1, TabColumn.SUBMITTED_BY_COLUMN);
        }
    }

    private class HostTableContenetProvider implements
            IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            DODDigest digest = (DODDigest) inputElement;
            DomainObjectDescriptor descriptor = null;
            try {
                descriptor = PolicyServerProxy.getDescriptorById(digest.getId());
            } catch (PolicyEditorException e) {
                e.printStackTrace();
                return new Object[0];
            }
            Collection<DeploymentHistory> records = PolicyServerProxy.getDeploymentRecords(descriptor);

            if (records == null || records.size() == 0) {
                return new Object[0];
            }
            
            List<TimeRelation> timeRelationList = new ArrayList<TimeRelation>(records.size());
            for (DeploymentHistory record : records) {
                timeRelationList.add(record.getTimeRelation());
            }
            
            Collections.sort(timeRelationList, new Comparator<TimeRelation>() {

                public int compare(TimeRelation o1, TimeRelation o2) {
                    return o2.getActiveFrom().compareTo(o1.getActiveFrom());
                }
            });
            TimeRelation tr = timeRelationList.get(0);
            Collection<AgentStatusDescriptor> agentStatusList = PolicyServerProxy
                    .getAgentsForDeployedObject(descriptor, tr.getActiveFrom());
            ArrayList<AgentStatusDescriptor> agents = new ArrayList<AgentStatusDescriptor>(
                    agentStatusList);
            return agents.toArray(new AgentStatusDescriptor[agents.size()]);
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    private class HostTableLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            AgentStatusDescriptor agentStatusDescriptor = (AgentStatusDescriptor) element;
            switch (columnIndex) {
            case 0:
                return (agentStatusDescriptor.getHostName());
            case 1:
                return "Unknown";
            case 2:
                return agentStatusDescriptor.getLastUpdated().toString();
            case 3:
                if (agentStatusDescriptor.getAgentType() == AgentTypeEnumType.FILE_SERVER) {
                    return DialogMessages.VERSIONHISTORYDIALOG_FILE_SERVER_ENFORCER;
                } else if (agentStatusDescriptor.getAgentType() == AgentTypeEnumType.DESKTOP) {
                    return DialogMessages.VERSIONHISTORYDIALOG_DESKTOP_ENFORCER;
                } else if (agentStatusDescriptor.getAgentType() == AgentTypeEnumType.PORTAL) {
                    return DialogMessages.VERSIONHISTORYDIALOG_PORTAL_ENFORCER;
                } else {
                    return "Unknown";
                }
            default:
                return "";
            }
        }
    }

    @Override
    public String getTabTitle() {
        return ApplicationMessages.DEPLOYEDTAB_DEPLOYED;
    }

    @Override
    public Image getTabImage() {
        return ImageBundle.TAB_DEPLOYED_IMG;
    }

    @Override
    public void createAdditonalComponentTreeToolItem(ToolBar toolBar) {
        submitItem = new ToolItem(toolBar, SWT.NONE);
        submitItem.setToolTipText(ApplicationMessages.DEPLOYEDTAB_DEACTIVATE);
        submitItem.setEnabled(false);
        submitItem.setImage(ImageBundle.DEACTIVATE_IMG);
        submitItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                submitDeactivation();
            }
        });
    }

    private void submitDeactivation() {
        IStructuredSelection selection = (IStructuredSelection) getComponentTreeViewer().getSelection();
        if (selection.isEmpty()) {
            return;
        }

        DODDigest digest = (DODDigest) selection.getFirstElement();
        DomainObjectDescriptor descriptor = null;
        try {
            descriptor = PolicyServerProxy.getDescriptorById(digest.getId());
        } catch (PolicyEditorException e) {
            e.printStackTrace();
            return;
        }
        Set <DomainObjectDescriptor> set = new HashSet <DomainObjectDescriptor>();
        set.add(descriptor);
		Collection<DomainObjectDescriptor> policiesDependencies = new HashSet<DomainObjectDescriptor>(PolicyHelpers.filterExceptionDependencies(set));
		policiesDependencies.addAll(set);
		for (DomainObjectDescriptor nextDescriptor : policiesDependencies) {
	        IHasId hasId = (IHasId) PolicyServerProxy.getEntityForDescriptor(nextDescriptor);
	        String name = DomainObjectHelper.getName(hasId);
	        EntityType entityType = DomainObjectHelper.getEntityType(hasId);

	        Collection<DomainObjectDescriptor> c = PolicyServerProxy.getAllReferringObjects(name);
	        if (c.isEmpty() || removeReferences(name, entityType, c)) {
	            DomainObjectHelper.setStatus(hasId, DevelopmentStatus.OBSOLETE);
	            PolicyServerProxy.saveEntity(hasId);

	            PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent(hasId);
	            EVENT_MANAGER.fireEvent(objectModifiedEvent);
	        }
	        PolicyManagerView.refreshCurrentTab();
		}
    }

    /**
     * Asks the user if they would like to remove all references to an object.
     * If the user so elects, actually performs the removal.
     * 
     * @return whether all the dependencies have been successfully removed
     */
    @SuppressWarnings("unchecked")
    private boolean removeReferences(String name, EntityType entityType,
            Collection<DomainObjectDescriptor> referringObjects) {
        boolean doRemove = MessageDialog.openConfirm(
                Display.getCurrent().getActiveShell()
              , ApplicationMessages.DEPLOYEDTAB_REMOVE_TITLE
              , NLS.bind(ApplicationMessages.DEPLOYEDTAB_REMOVE_MSG, name, name)
        );

        if (doRemove) {
            Collection<? extends IHasId> c = PolicyServerProxy.getEntitiesForDescriptor(referringObjects);
            Iterator<IHasId> iter = (Iterator<IHasId>) c.iterator();
            while (iter.hasNext()) {
                PredicateHelpers.removeReferences(iter.next(), name, entityType);
            }
            PolicyServerProxy.saveEntities(c);

            Set<PolicyOrComponentModifiedEvent> eventsToFire = new HashSet<PolicyOrComponentModifiedEvent>();
            Iterator<IHasId> changedEntities = (Iterator<IHasId>) c.iterator();
            while (changedEntities.hasNext()) {
                PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent(
                        changedEntities.next());
                eventsToFire.add(objectModifiedEvent);
            }
            EVENT_MANAGER.fireEvent(eventsToFire);
        }
        return doRemove;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateAdditionalComponentTreeToolBarStatus() {
        submitItem.setEnabled(false);

        IStructuredSelection selection = (IStructuredSelection) getComponentTreeViewer().getSelection();
        if (selection.isEmpty()) {
            return;
        }
        if (getComponentTreeViewer().getTree().getSelection().length == 1) {
            boolean newState = false;
            Object object = selection.getFirstElement();
            DomainObjectDescriptor descriptor = null;
            DODDigest digest = (DODDigest) object;
            try {
                descriptor = PolicyServerProxy.getDescriptorById(digest.getId());
            } catch (PolicyEditorException e) {
                e.printStackTrace();
                return;
            }
            IHasId hasId = (IHasId) PolicyServerProxy.getEntityForDescriptor(descriptor);

            // can only deactivate draft or approved objects
            DevelopmentStatus status = DomainObjectHelper.getStatus(hasId);
            newState = (status == DevelopmentStatus.DRAFT || status == DevelopmentStatus.APPROVED);
            newState &= PolicyServerProxy.canPerformAction(hasId,
                    DAction.APPROVE);

            String name = DomainObjectHelper.getName(hasId);
            Collection referringObjects = PolicyServerProxy.getAllReferringObjects(name);
            Iterator referringObjectsIterator = referringObjects.iterator();
            while ((referringObjectsIterator.hasNext()) && (newState)) {
                DomainObjectDescriptor nextReferringObject = (DomainObjectDescriptor) referringObjectsIterator.next();
                newState &= !nextReferringObject.isHidden();
            }

            try {
                DomainObjectUsage entityUsage = PolicyServerProxy.getUsage(descriptor);
                newState &= entityUsage.hasBeenDeployed();
            } catch (PolicyEditorException exception) {
                LoggingUtil.logWarning(Activator.ID
                        , "Failed to load deployment history for selected domain object." 
                          + "  Deactivate menu item may be improperly disabled."
                        , exception);
            }
            submitItem.setEnabled(newState);
        }
    }

    @Override
    public void createAdditionalTab(CTabFolder folder) {
        itemHosts = new CTabItem(folder, SWT.NONE);
        itemHosts.setText(ApplicationMessages.DEPLOYEDTAB_HOSTS);
    }

    @Override
    public void updateAdditionalTab() {
        IStructuredSelection selection = (IStructuredSelection) getComponentTreeViewer()
                .getSelection();
        if (selection.isEmpty()) {
            itemHosts.setControl(null);
            return;
        }

        Object element = selection.getFirstElement();
        DODDigest digest = (DODDigest) element;

        hostComposite = new Composite(getDetailTabFolder(), SWT.NONE);
        hostComposite.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        hostComposite.setLayout(layout);

        Label hostImage = new Label(hostComposite, SWT.NONE);
        hostImage.setImage(ObjectLabelImageProvider.getImage(digest));
        GridData data = new GridData(GridData.BEGINNING);
        hostImage.setLayoutData(data);

        Label hostName = new Label(hostComposite, SWT.WRAP);
        hostName.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        hostName.setText(getDisplayName(digest));
        data = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
        hostName.setLayoutData(data);

        TableViewer hostTableViewer = new TableViewer(hostComposite, SWT.BORDER);
        hostTableViewer.setContentProvider(new HostTableContenetProvider());
        hostTableViewer.setLabelProvider(new HostTableLabelProvider());
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        Table hostTable = hostTableViewer.getTable();
        hostTable.setLayoutData(data);
        hostTable.setLinesVisible(true);
        hostTable.setHeaderVisible(true);

        TableColumn tableColumn = new TableColumn(hostTableViewer.getTable(),
                SWT.V_SCROLL | SWT.H_SCROLL);
        tableColumn.setWidth(100);
        tableColumn.setText(ApplicationMessages.DEPLOYEDTAB_HOST_NAME);

        tableColumn = new TableColumn(hostTableViewer.getTable(), SWT.NONE);
        tableColumn.setWidth(100);
        tableColumn.setText(ApplicationMessages.DEPLOYEDTAB_ACTIVATED_BY);

        tableColumn = new TableColumn(hostTableViewer.getTable(), SWT.NONE);
        tableColumn.setWidth(100);
        tableColumn.setText(ApplicationMessages.DEPLOYEDTAB_SCHEDULE);

        tableColumn = new TableColumn(hostTableViewer.getTable(), SWT.NONE);
        tableColumn.setWidth(100);
        tableColumn.setText(ApplicationMessages.DEPLOYEDTAB_TYPE);

        hostTableViewer.setInput(digest);
        for (TableColumn column : hostTable.getColumns()) {
            column.pack();
        }

        itemHosts.setControl(hostComposite);
        data = new GridData(GridData.FILL_BOTH);
        hostComposite.setLayoutData(data);
    }

    @Override
    public boolean hasCorrectStatus(DODDigest info) {
        String result = EntityInformation.getStatus(info);
        if (result.equals("Deployed") || result.indexOf("is deployed") != -1) {
            return true;
        }
        return false;
    }
}
