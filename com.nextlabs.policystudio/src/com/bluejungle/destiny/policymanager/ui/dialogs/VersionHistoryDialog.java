/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.bluejungle.destiny.policymanager.editor.ReadOnlyPanelFactory;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.pf.destiny.lib.AgentStatusDescriptor;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;

/**
 * @author fuad
 * @version $Id$
 */

public class VersionHistoryDialog extends Dialog {
    private static final Point SIZE = new Point(900, 600);

    private TableViewer tableViewer = null;
    private TabFolder tabFolder = null;
    private Composite objectViewerComposite = null;
    private TableViewer agentTableViewer = null;
    private IHasId domainObject = null;
    protected DomainObjectDescriptor descriptor = null;

    private List<DeploymentHistory> timeRelationList = new ArrayList<DeploymentHistory>();

    private class TableContentProvider implements IStructuredContentProvider {

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            if (timeRelationList != null) {
                return timeRelationList.toArray();
            } else {
                return new Object[0];
            }
        }
    }
    
    private static abstract class Column {
        final String name;
        final int width;
        final boolean sortable;
        
        Column(String name, int width, boolean sortable) {
            super();
            this.name = name;
            this.width = width;
            this.sortable = sortable;
        }
        
        abstract String getColumnText(DeploymentHistory deploymentHistory);
        
        TableSorter getTableSorter(int direction) {
            return null;
        }
        
        int compare(TimeRelation timeRelation1, TimeRelation timeRelation2) {
            return 0;
        }
    }
    
    private final Column COLUMN_VERSIONHISTORYDIALOG_DEPLOYMENT_TIME = 
        new Column(DialogMessages.VERSIONHISTORYDIALOG_DEPLOYMENT_TIME, 140, true){

            @Override
            String getColumnText(DeploymentHistory deploymentHistory) {
                return SimpleDateFormat.getDateTimeInstance().format(deploymentHistory.getDeployTime());
            }
            
            @Override
            int compare(TimeRelation timeRelation1, TimeRelation timeRelation2) {
                return timeRelation1.getActiveFrom().compareTo(timeRelation2.getActiveFrom());
            }
    };
    
    private final Column COLUMN_VERSIONHISTORYDIALOG_DEPLOYMENT_STATUS = 
        new Column(DialogMessages.VERSIONHISTORYDIALOG_DEPLOYMENT_STATUS, 140, true){
        
            private Date cutoff = new Date();

            @Override
            String getColumnText(DeploymentHistory deploymentHistory) {
                Date asof = deploymentHistory.getTimeRelation().getActiveFrom();
                if (asof.after(cutoff)) {
                    return DialogMessages.VERSIONHISTORYDIALOG_SCHEDULED;
                } else if (deploymentHistory.getTimeRelation().getActiveTo().after(cutoff)) {
                    if (PolicyServerProxy.getDeployedObject(descriptor
                            , deploymentHistory.getTimeRelation().getActiveFrom()) != null) {
                        return DialogMessages.VERSIONHISTORYDIALOG_ACTIVE;
                    } else {
                        return DialogMessages.VERSIONHISTORYDIALOG_INACTIVE;
                    }
                } else {
                    return DialogMessages.VERSIONHISTORYDIALOG_OBSOLETE;
                }
            }
            
            @Override
            int compare(TimeRelation timeRelation1, TimeRelation timeRelation2) {
                String status1 = getStatus(timeRelation1);
                String status2 = getStatus(timeRelation2);
                return status1.compareTo(status2);
            }
            
            String getStatus(TimeRelation timeRelation){
                Date asOf = timeRelation.getActiveFrom();
                if (asOf.after(cutoff)) {
                    return DialogMessages.VERSIONHISTORYDIALOG_SCHEDULED;
                } else if (timeRelation.getActiveTo().after(cutoff)) {
                    if (PolicyServerProxy.getDeployedObject(descriptor, timeRelation.getActiveFrom()) != null) {
                        return DialogMessages.VERSIONHISTORYDIALOG_ACTIVE;
                    } else {
                        return DialogMessages.VERSIONHISTORYDIALOG_INACTIVE;
                    }
                } else {
                    return DialogMessages.VERSIONHISTORYDIALOG_OBSOLETE;
                }
            }
    };
    
    private final Column COLUMN_VERSIONHISTORYDIALOG_LAST_MODIFIED_BY = 
        new Column("Last modified by", 90, false){

            @Override
            String getColumnText(DeploymentHistory deploymentHistory) {
                try {
                    return PolicyServerProxy.getUserName(deploymentHistory.getModifier());
                } catch (PolicyEditorException e) {
                    throw new RuntimeException(e);
                }
            }
    };
    
    private final Column COLUMN_VERSIONHISTORYDIALOG_SUBMITTED_BY = 
        new Column("Submitted by", 90, false){

            @Override
            String getColumnText(DeploymentHistory deploymentHistory) {
                try {
                    return PolicyServerProxy.getUserName(deploymentHistory.getSubmitter());
                } catch (PolicyEditorException e) {
                    throw new RuntimeException(e);
                }
            }
    };
    
    private final Column COLUMN_VERSIONHISTORYDIALOG_DEPLOYED_BY = 
        new Column("Deployed by", 90, false){

            @Override
            String getColumnText(DeploymentHistory deploymentHistory) {
                try {
                    return PolicyServerProxy.getUserName(deploymentHistory.getDeployer());
                } catch (PolicyEditorException e) {
                    throw new RuntimeException(e);
                }
            }
    };
   

    private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        
        final List<Column> columns;
        
        public TableLabelProvider(List<Column> columns) {
            this.columns = columns;
        }

        public String getColumnText(Object obj, int index) {
            DeploymentHistory deploymentHistory = (DeploymentHistory) obj;
            return columns.get(index).getColumnText(deploymentHistory);
        }

        public Image getColumnImage(Object obj, int index) {
            return null;
        }

    }

    private abstract class TableSorter extends ViewerSorter {
        private int direction;

        public TableSorter(int direction) {
            this.direction = direction;
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            DeploymentHistory d1 = (DeploymentHistory)e1;
            DeploymentHistory d2 = (DeploymentHistory)e2;

            TimeRelation timeRelation1 = direction == SWT.UP ? d1.getTimeRelation() : d2.getTimeRelation();
            TimeRelation timeRelation2 = direction == SWT.UP ? d2.getTimeRelation() : d1.getTimeRelation();

            return compare(timeRelation1, timeRelation2);
        }
        
        abstract int compare(TimeRelation timeRelation1, TimeRelation timeRelation2);
    }

    private class AgentTableContentProvider implements IStructuredContentProvider {

    	private Object[] input = null;
	
		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			if (newInput != null) {
				input = ((Collection<AgentStatusDescriptor>) newInput).toArray();
		    } else {
		        input = null;
		    }
		
		}
	
		public void dispose() {
		}
	
		public Object[] getElements(Object parent) {
		    if (input != null) {
		        return input;
		    } else {
		        return new Object[0];
		    }
		}
	}

    private class AgentTableLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        public String getColumnText(Object obj, int index) {
            AgentStatusDescriptor agentStatusDescriptor = (AgentStatusDescriptor) obj;
            switch (index) {

            case 0:
                return (agentStatusDescriptor.getHostName());
            case 1:
                if (agentStatusDescriptor.getAgentType() == AgentTypeEnumType.FILE_SERVER) {
                    return DialogMessages.VERSIONHISTORYDIALOG_FILE_SERVER_ENFORCER;
                } else if (agentStatusDescriptor.getAgentType() == AgentTypeEnumType.DESKTOP) {
                    return DialogMessages.VERSIONHISTORYDIALOG_DESKTOP_ENFORCER;
                } else if (agentStatusDescriptor.getAgentType() == AgentTypeEnumType.PORTAL) {
                    return DialogMessages.VERSIONHISTORYDIALOG_PORTAL_ENFORCER;
                } else if (agentStatusDescriptor.getAgentType() == AgentTypeEnumType.ACTIVE_DIRECTORY) {
                    return DialogMessages.VERSIONHISTORYDIALOG_ACTIVE_DIRECTORY_ENFORCER;
                } else {
                    return "Unknown";
                }
            }
            return null;
        }

        public Image getColumnImage(Object obj, int index) {
            return null;
        }

    }

    /**
     * Constructor
     */
    public VersionHistoryDialog(Shell parent, IHasId domainObject) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.domainObject = domainObject;
        EntityType entityType = DomainObjectHelper.getEntityType(domainObject);
        String name = DomainObjectHelper.getName(domainObject);
        DODDigest digest = null;
        if (entityType == EntityType.POLICY || entityType == EntityType.FOLDER) {
            digest = EntityInfoProvider.getPolicyDescriptor(name);
        } else {
            digest = EntityInfoProvider.getComponentDescriptor(name);
        }
        try {
            descriptor = PolicyServerProxy.getDescriptorById(digest.getId());
        } catch (PolicyEditorException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(DialogMessages.VERSIONHISTORYDIALOG_TITLE 
                + DomainObjectHelper.getName(domainObject));
        newShell.setSize(SIZE);
        newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);

        getTimeRelations();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite root = (Composite) super.createDialogArea(parent);
        initialize(root);

        return parent;
    }

    /**
     * Gets the deployment history of the domainObject
     */
    private void getTimeRelations() {
        DODDigest digest;
        EntityType entityType = DomainObjectHelper.getEntityType(domainObject);
        String name = DomainObjectHelper.getName(domainObject);
        if (entityType == EntityType.POLICY) {
            digest = EntityInfoProvider.getPolicyDescriptor(name);
        } else {
            digest = EntityInfoProvider.getComponentDescriptor(name);
        }
        DomainObjectDescriptor descriptor;
        try {
            descriptor = PolicyServerProxy.getDescriptorById(digest.getId());
        } catch (PolicyEditorException e) {
            e.printStackTrace();
            return;
        }
        Collection<DeploymentHistory> records = PolicyServerProxy.getDeploymentRecords(descriptor);

        if (records != null) {
            timeRelationList = new ArrayList<DeploymentHistory>(records);
        }
    }

    /**
     * 
     */
    private void initialize(Composite root) {
        GridLayout layout = new GridLayout(2, false);
        root.setLayout(layout);

        tableViewer = new TableViewer(root, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION 
                | SWT.V_SCROLL | SWT.H_SCROLL);
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        GridData data = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(data);

        
        
        List<Column> columnDatas = Arrays.asList(
                COLUMN_VERSIONHISTORYDIALOG_DEPLOYMENT_TIME
              , COLUMN_VERSIONHISTORYDIALOG_DEPLOYMENT_STATUS
              , COLUMN_VERSIONHISTORYDIALOG_LAST_MODIFIED_BY
              , COLUMN_VERSIONHISTORYDIALOG_SUBMITTED_BY
              , COLUMN_VERSIONHISTORYDIALOG_DEPLOYED_BY
        );
        
        for(final Column columnData : columnDatas) {
            TableColumn column = new TableColumn(table, SWT.LEFT);
            column.setText(columnData.name);
            column.setWidth(columnData.width);
            if(columnData.sortable){
                SelectionAdapter selectionAdapter = new SelectionAdapter() {
                    /**
                     * sets the sort column to the clicked column if the current sort
                     * column is clicked, toggle the sort order
                     * 
                     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                     */
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        int direction = SWT.DOWN;
                        Table table = tableViewer.getTable();
                        TableColumn column = (TableColumn) e.widget;
                        if (column == table.getSortColumn()) {
                            switch (table.getSortDirection()) {
                            case SWT.DOWN:
                                direction = SWT.UP;
                                break;
                            case SWT.UP:
                                direction = SWT.DOWN;
                                break;
                            }
                        } else {
                            table.setSortColumn(column);
                        }
                        table.setSortDirection(direction);
                        tableViewer.setSorter(new TableSorter(direction){
                            @Override
                            int compare(TimeRelation timeRelation1, TimeRelation timeRelation2) {
                                return columnData.compare(timeRelation1, timeRelation2);
                            }
                        });
                        tableViewer.refresh();
                    }
                };
                column.addSelectionListener(selectionAdapter);
            }
        }

        tableViewer.setContentProvider(new TableContentProvider());
        tableViewer.setLabelProvider(new TableLabelProvider(columnDatas));
        tableViewer.setInput(timeRelationList);

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                    IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                    if (selection != null) {
                        DeploymentHistory history = (DeploymentHistory) selection.getFirstElement();
                        IHasId entity = (IHasId) PolicyServerProxy.getDeployedObject(descriptor, history.getTimeRelation().getActiveFrom());

                        // remove previous panel before adding new one.
                        Control[] controls = objectViewerComposite.getChildren();
                        for (int i = 0; i < controls.length; i++) {
                            controls[i].dispose();
                        }

                        if (entity != null) {
                            ScrolledComposite componentViewerComposite = new ScrolledComposite(
                                    objectViewerComposite
                                  , SWT.V_SCROLL | SWT.H_SCROLL);
                            componentViewerComposite.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
                            componentViewerComposite.setLayout(new GridLayout());
                            GridData data = new GridData(GridData.FILL_BOTH);
                            componentViewerComposite.setLayoutData(data);
                            componentViewerComposite.setExpandHorizontal(true);
                            componentViewerComposite.setExpandVertical(true);
                            componentViewerComposite.getVerticalBar().setIncrement(10);
                            componentViewerComposite.getVerticalBar().setPageIncrement(100);
                            componentViewerComposite.getHorizontalBar().setIncrement(10);
                            componentViewerComposite.getHorizontalBar().setPageIncrement(100);

                            Composite detail = ReadOnlyPanelFactory.getEditorPanel(
                                            componentViewerComposite
                                          , SWT.NONE
                                          , entity);
                            data = new GridData(GridData.FILL_BOTH);
                            detail.setLayoutData(data);

                            componentViewerComposite.setContent(detail);
                            componentViewerComposite.setMinSize(detail.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                        } else {
                            Label emptyData = new Label(objectViewerComposite, SWT.NONE);
                            String typeStr;
                            if (descriptor.getType() == EntityType.POLICY) {
                                typeStr = DialogMessages.VERSIONHISTORYDIALOG_POLICY;
                            } else {
                                typeStr = DialogMessages.VERSIONHISTORYDIALOG_COMPONENT;
                            }
                            emptyData.setText(DialogMessages.VERSIONHISTORYDIALOG_NO_ACTIVE_VERSION
                                            + typeStr
                                            + DialogMessages.VERSIONHISTORYDIALOG_IS_DEFINED);
                            emptyData.setAlignment(SWT.CENTER);
                            GridData data = new GridData(GridData.FILL_HORIZONTAL);
                            data.verticalAlignment = GridData.CENTER;
                            emptyData.setLayoutData(data);
                        }
                        objectViewerComposite.layout(true, true);

                        Collection<AgentStatusDescriptor> agentStatusList = PolicyServerProxy.getAgentsForDeployedObject(descriptor, history.getTimeRelation().getActiveFrom());
                        if (agentStatusList != null) {
                            agentTableViewer.setInput(agentStatusList);
                        }
                    }
                }
            });

        addTabs(root);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, DialogMessages.LABEL_CLOSE, true);
    }

    /**
     * 
     */
    private void addTabs(Composite root) {
        tabFolder = new TabFolder(root, SWT.NONE);
        GridLayout layout = new GridLayout();
        tabFolder.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        tabFolder.setLayoutData(data);

        TabItem definitionTabItem = new TabItem(tabFolder, SWT.NONE);
        definitionTabItem.setText(DialogMessages.VERSIONHISTORYDIALOG_DEFINITION);
        objectViewerComposite = new Composite(tabFolder, SWT.BORDER);
        objectViewerComposite.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        objectViewerComposite.setLayout(new GridLayout());
        definitionTabItem.setControl(objectViewerComposite);

        TabItem agentTabItem = new TabItem(tabFolder, SWT.NONE);
        agentTabItem.setText(DialogMessages.VERSIONHISTORYDIALOG_DEPLOYED_POLICY_ENFORCERS);
        Composite c = new Composite(tabFolder, SWT.NONE);
        c.setLayout(new FillLayout());
        agentTabItem.setControl(c);

        agentTableViewer = new TableViewer(c, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        Table table = agentTableViewer.getTable();
        table.setHeaderVisible(true);
        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.VERSIONHISTORYDIALOG_HOST_NAME);
        column.setWidth(150);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.VERSIONHISTORYDIALOG_TYPE);
        column.setWidth(100);

        agentTableViewer.setContentProvider(new AgentTableContentProvider());
        agentTableViewer.setLabelProvider(new AgentTableLabelProvider());
    }
}
