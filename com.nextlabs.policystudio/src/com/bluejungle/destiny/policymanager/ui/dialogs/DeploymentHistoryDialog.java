package com.bluejungle.destiny.policymanager.ui.dialogs;

/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.bluejungle.destiny.policymanager.framework.standardlisteners.TableColumnResizeListener;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.pf.destiny.lifecycle.DeploymentRecord;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author fuad
 */

public class DeploymentHistoryDialog extends Dialog {
    private static final Point SIZE = new Point(600, 500);

    private Timer autoRefresh;
    private List<DeploymentRecord> deploymentRecords;
    private TableViewer tableViewer;
    private Button cancelDeploymentButton;

    private class TableContentProvider implements IStructuredContentProvider {

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            if (deploymentRecords != null) {
                return deploymentRecords.toArray();
            } else {
                return new Object[0];
            }
        }
    }

    private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

        public String getColumnText(Object obj, int index) {
            Date cutoff = new Date();
            DeploymentRecord record = (DeploymentRecord) obj;
            switch (index) {
            case 0:
                return SimpleDateFormat.getDateTimeInstance().format(record.getWhenRequested());
            case 1:
                return SimpleDateFormat.getDateTimeInstance().format(record.getAsOf());
            case 2:
                return NLS.bind(DialogMessages.DEPLOYMENTHISTORYDIALOG_OBJECTS
                        , record.getNumberOfDeployedEntities());
            case 3:
                if (record.getAsOf().after(cutoff)) {
                    return DialogMessages.DEPLOYMENTHISTORYDIALOG_SCHEDULED;
                } else {
                    return DialogMessages.DEPLOYMENTHISTORYDIALOG_DEPLOYED;
                }
            case 4:
                try {
                        return PolicyServerProxy.getUserName(record.getDeployer());
                    } catch (PolicyEditorException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            }
            return null;
        }

        public Image getColumnImage(Object obj, int index) {
            return null;
        }

        @Override
        public Image getImage(Object obj) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
        }
    }
    
    private static final Comparator<DeploymentRecord> requestTimeComparator = 
            new Comparator<DeploymentRecord>() {
        public int compare(DeploymentRecord o1, DeploymentRecord o2) {
            return o1.getWhenRequested().compareTo(o2.getWhenRequested());
        }
    };
    
    private static final Comparator<DeploymentRecord> asOfTimeComparator = 
            new Comparator<DeploymentRecord>() {
        public int compare(DeploymentRecord o1, DeploymentRecord o2) {
            return o1.getAsOf().compareTo(o2.getAsOf());
        }
    };
    
    private static final Comparator<DeploymentRecord> numberOfDeployedEntitiesComparator = 
            new Comparator<DeploymentRecord>() {
        public int compare(DeploymentRecord o1, DeploymentRecord o2) {
            int number1 = o1.getNumberOfDeployedEntities();
            int number2 = o2.getNumberOfDeployedEntities();
            return number1 - number2;
        }
    };
    
    private static final Comparator<DeploymentRecord> statusComparator = 
            new Comparator<DeploymentRecord>() {
        public int compare(DeploymentRecord o1, DeploymentRecord o2) {
            //TODO looks like this can be better
            String status1, status2;
            Date cutoff = new Date();
            if (o1.getAsOf().after(cutoff)) {
                status1 = DialogMessages.DEPLOYMENTHISTORYDIALOG_SCHEDULED;
            } else {
                status1 = DialogMessages.DEPLOYMENTHISTORYDIALOG_DEPLOYED;
            }
            if (o2.getAsOf().after(cutoff)) {
                status2 = DialogMessages.DEPLOYMENTHISTORYDIALOG_SCHEDULED;
            } else {
                status2 = DialogMessages.DEPLOYMENTHISTORYDIALOG_DEPLOYED;
            }
            
            return status1.compareTo(status2);
        }
    };
    
    private static final Map<String, Comparator<DeploymentRecord>> COLUMN_STRING_TO_COMPARATOR_MAP;
    static{
        COLUMN_STRING_TO_COMPARATOR_MAP = new HashMap<String, Comparator<DeploymentRecord>>();
        COLUMN_STRING_TO_COMPARATOR_MAP.put(
                DialogMessages.DEPLOYMENTHISTORYDIALOG_SCHEDULED_TIME
              , requestTimeComparator);
        COLUMN_STRING_TO_COMPARATOR_MAP.put(
                DialogMessages.DEPLOYMENTHISTORYDIALOG_DEPLOYMENT_TIME
              , asOfTimeComparator);
        COLUMN_STRING_TO_COMPARATOR_MAP.put(
                DialogMessages.DEPLOYMENTHISTORYDIALOG_CONTENTS
              , numberOfDeployedEntitiesComparator);
        COLUMN_STRING_TO_COMPARATOR_MAP.put(
                DialogMessages.DEPLOYMENTHISTORYDIALOG_STATUS
              , statusComparator);
    }
    

    private class TableSorter extends ViewerSorter {
        private String column;
        private int direction;

        public TableSorter(String column, int direction) {
            this.column = column;
            this.direction = direction;
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            Comparator<DeploymentRecord> comparator = COLUMN_STRING_TO_COMPARATOR_MAP.get(column);
            if(comparator == null) {
                return 0;
            }
            
            DeploymentRecord deploymentRecord1 = (DeploymentRecord) (direction == SWT.UP ? e1 : e2);
            DeploymentRecord deploymentRecord2 = (DeploymentRecord) (direction == SWT.UP ? e2 : e1);
            
            return comparator.compare(deploymentRecord1, deploymentRecord2);
        }
        
    }

    /**
     * Constructor
     */
    public DeploymentHistoryDialog(Shell parent) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setSize(SIZE);
        newShell.setText(DialogMessages.DEPLOYMENTHISTORYDIALOG_TITLE);
        newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);

        autoRefresh = new Timer();
        final Display display = newShell.getDisplay();
        autoRefresh.schedule(new TimerTask() {
            @Override
            public void run() {
                display.asyncExec(new Runnable() {

                    public void run() {
                        updateCancelButtonState();
                        tableViewer.refresh();
                        setTableColors();
                        display.update();
                    }
                });
            }
        }, 0, 10000);
    }

    @Override
    public boolean close() {
        autoRefresh.cancel();
        return super.close();
    }

    /**
     * Gets all deployment records from eight day earlier up to the year 2020
     */
    private void getDeploymentRecords() {
        Calendar start = new GregorianCalendar();
        // Get the history for the last 8 days
        start.add(Calendar.DAY_OF_MONTH, -8);

        Calendar end = new GregorianCalendar();
        end.set(Calendar.YEAR, 2020);

        deploymentRecords = new ArrayList<DeploymentRecord>(
                PolicyServerProxy.getDeploymentRecords(start.getTime(), end.getTime()));
        for (int i = 0; i < deploymentRecords.size(); i++) {
            DeploymentRecord record = (DeploymentRecord) deploymentRecords.get(i);
            if (record.isCancelled() || record.isHidden()) {
                deploymentRecords.remove(i--);
            }
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite root = (Composite) super.createDialogArea(parent);

        tableViewer = new TableViewer(root, SWT.BORDER | SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        SelectionListener selectionListener = new SelectionAdapter() {

            /**
             * sets the sort column to the clicked column if the current sort
             * column is clicked, toggle the sort order
             * 
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
                tableViewer.setSorter(new TableSorter(column.getText(),
                        direction));
                tableViewer.refresh();
                setTableColors();
            }
        };

        TableColumn column;
        
        column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.DEPLOYMENTHISTORYDIALOG_SCHEDULED_TIME);
        column.setWidth(180);
        column.addSelectionListener(selectionListener);
        
        column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.DEPLOYMENTHISTORYDIALOG_DEPLOYMENT_TIME);
        column.setWidth(180);
        column.addSelectionListener(selectionListener);
        
        column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.DEPLOYMENTHISTORYDIALOG_CONTENTS);
        column.addSelectionListener(selectionListener);
        column.setWidth(100);
        
        column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.DEPLOYMENTHISTORYDIALOG_STATUS);
        column.setWidth(100);
        column.addSelectionListener(selectionListener);
        
        column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.DEPLOYMENTHISTORYDIALOG_DEPLOYED_BY);
        column.setWidth(100);

        table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDoubleClick(MouseEvent e) {
                    cancelDeployment();
                }
        });

        GridData data = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(data);

        tableViewer.setContentProvider(new TableContentProvider());
        tableViewer.setLabelProvider(new TableLabelProvider());
        getDeploymentRecords();
        tableViewer.setInput(deploymentRecords);
        setTableColors();

        cancelDeploymentButton = new Button(root, SWT.PUSH);
        cancelDeploymentButton.setText(DialogMessages.DEPLOYMENTHISTORYDIALOG_CANCEL_SELECTED_DEPLOYMENT);

        cancelDeploymentButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    cancelDeployment();
                }
        });

        // Enable/disable cancel button based on state.
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event) {
                    updateCancelButtonState();
                }
        });

        updateCancelButtonState();

        root.addListener(SWT.Resize, new TableColumnResizeListener(table, root));

        return parent;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, DialogMessages.LABEL_CLOSE, false);
    }

    protected void setTableColors() {
        Date cutoff = new Date();
        Table table = tableViewer.getTable();
        TableItem items[] = table.getItems();
        for (TableItem item : items) {
            DeploymentRecord record = (DeploymentRecord) item.getData();
            if (!record.getAsOf().after(cutoff)) {
                item.setForeground(ResourceManager
                        .getColor(SWT.COLOR_DARK_GRAY));
            } else {
                item.setForeground(ResourceManager.getColor(SWT.COLOR_BLACK));
            }
        }
    }

    /**
     * Opens the cancel deployment dialog for the selected deployment record
     */
    protected void cancelDeployment() {
        IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
        if (selection != null) {
            DeploymentRecord dr = (DeploymentRecord) selection.getFirstElement();
            boolean canCancel = cancelDeploymentButton.getEnabled();
            CancelDeploymentDialog dlg = new CancelDeploymentDialog(getShell(), dr, canCancel);
            if (dlg.open() == Window.OK) {
                deploymentRecords.remove(dr);
                tableViewer.refresh();
                setTableColors();
            }
        }
    }

    /**
     * enable/disable cancel button depending on the current selected deployment
     * record.
     */
    private void updateCancelButtonState() {
        Calendar cutoffCal = new GregorianCalendar();
        cutoffCal.add(Calendar.MINUTE, 1);
        Date cutoff = cutoffCal.getTime();
        IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
        if (selection != null) {
            DeploymentRecord dr = (DeploymentRecord) selection.getFirstElement();
            if (dr != null && dr.getAsOf().after(cutoff)) {
                // See if the current user is authorized to cancel the deployment
                Collection<DomainObjectDescriptor> deployed = 
                    PolicyServerProxy.getObjectsInDeploymentRecord(dr);
                Collection<DomainObjectDescriptor> canUndeploy = 
                        PolicyServerProxy.filterByAllowedAction(deployed, DAction.DEPLOY);
                // The button should be enabled only when the user has
                // deployment rights for all the deployed objects:
                cancelDeploymentButton.setEnabled(deployed != null
                        && canUndeploy != null
                        && deployed.size() == canUndeploy.size());
            } else {
                cancelDeploymentButton.setEnabled(false);
            }
        }
    }
}