/*
 * Created on Mar 10, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.usergroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.action.DuplicateAction;
import com.bluejungle.destiny.policymanager.action.PolicyStudioActionFactory;
import com.bluejungle.destiny.policymanager.event.EventType;
import com.bluejungle.destiny.policymanager.event.IEvent;
import com.bluejungle.destiny.policymanager.event.IEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoListener;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.IClipboardEnabled;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyObjectTransfer;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.controls.FilterControl;
import com.bluejungle.destiny.policymanager.ui.controls.FilterControlEvent;
import com.bluejungle.destiny.policymanager.ui.controls.FilterControlListener;
import com.bluejungle.destiny.policymanager.ui.dialogs.TitleAreaDialogEx;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecManager;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;

/**
 * @author bmeng
 */

public abstract class ComponentListPanel extends Composite implements
        IClipboardEnabled {

    protected FilterControl filterControl;
    protected Button buttonNew;
    protected TableViewer tableViewer;

    private List<DODDigest> objectList = new ArrayList<DODDigest>();
    private List<DomainObjectDescriptor> searchResults_byName = new ArrayList<DomainObjectDescriptor>();
    private List<DomainObjectDescriptor> searchResults_byContainedObject = new ArrayList<DomainObjectDescriptor>();
    private List<DomainObjectDescriptor> searchResults_byPolicy = new ArrayList<DomainObjectDescriptor>();
    private List<TableItem> selectedItems = new ArrayList<TableItem>();
    private boolean ignoreSelection;
    private boolean ignoreCurrentObjectChangedEvent;
    private boolean filtering;
    private ViewContentProvider viewContentProvider;
    private final IDSpecManager sm = (IDSpecManager) ComponentManagerFactory
            .getComponentManager().getComponent(IDSpecManager.COMP_INFO);

    private class ViewContentProvider implements IStructuredContentProvider {

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            if (filtering) {
                String text = filterControl.getText();
                List<Object> list = new ArrayList<Object>();
                list.add(NLS.bind(
                        Messages.COMPONENTLISTPANEL_FILTER_OBJECT_NAME, text));
                if (searchResults_byName.isEmpty()) {
                    list.add(emptyString());
                } else {
                    list.addAll(searchResults_byName);
                }
                list.add(NLS.bind(
                        Messages.COMPONENTLISTPANEL_FILTER_USING_COMPONENT,
                        text));
                if (searchResults_byContainedObject.isEmpty()) {
                    list.add(emptyString());
                } else {
                    list.addAll(searchResults_byContainedObject);
                }
                list
                        .add(NLS
                                .bind(
                                        Messages.COMPONENTLISTPANEL_FILTER_USED_IN_POLICY,
                                        text));
                if (searchResults_byPolicy.isEmpty()) {
                    list.add(emptyString());
                } else {
                    list.addAll(searchResults_byPolicy);
                }
                return list.toArray();
            } else {
                return objectList.toArray();
            }
        }

        private boolean inProgress = true;

        public void setInProgress(boolean val) {
            inProgress = val;
        }

        private String emptyString() {
            return inProgress ? Messages.IN_PROGRESS_STRING
                    : Messages.EMPTY_LIST_STRING;
        }
    }

    private class ViewLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        public String getColumnText(Object obj, int index) {
            if (obj instanceof String) {
                return (String) obj;
            }
            String name = "";
            if (obj instanceof DODDigest) {
                name = ((DODDigest) obj).getName();
            } else {
                name = ((DomainObjectDescriptor) obj).getName();
            }
            if (name != null) {
                int pos = name.indexOf(PQLParser.SEPARATOR);
                return (pos != -1) ? name.substring(pos + 1) : name;
            } else {
                return null;
            }
        }

        public Image getColumnImage(Object obj, int index) {
            if (obj instanceof String) {
                return null;
            }
            return getImage(obj);
        }

        @Override
        public Image getImage(Object obj) {
            return ObjectLabelImageProvider.getImage(obj);
        }
    }

    private class FilterWorker implements Runnable {

        private String componentNameFilter;

        private String componentType;

        private FilterControlListener.EndOfSearch endOfSearch;

        public FilterWorker(String entityType, String filter,
                FilterControlListener.EndOfSearch endOfSearch) {
            this.componentType = entityType;
            this.componentNameFilter = filter;
            this.endOfSearch = endOfSearch;
        }

        public void run() {
            String filterString = "%"
                    + PolicyServerProxy.escape(componentNameFilter) + "%";
            Collection<DomainObjectDescriptor> entities = PolicyServerProxy
                    .getEntityList(filterString, componentType);
            searchResults_byName.clear();
            searchResults_byName.addAll(entities);
            entities = PolicyServerProxy.getReferringComponents(filterString);
            searchResults_byContainedObject.clear();
            searchResults_byContainedObject.addAll(entities);
            String policyFilterString = "%" + PQLParser.SEPARATOR
                    + filterString;
            entities = PolicyServerProxy.getReferredEntitiesForPolicy(
                    policyFilterString, componentType);
            searchResults_byPolicy.clear();
            searchResults_byPolicy.addAll(entities);

            if (viewContentProvider != null) {
                viewContentProvider.setInProgress(false);
            }

            getDisplay().syncExec(new Runnable() {

                public void run() {
                    if (tableViewer != null) {
                        tableViewer.refresh();
                        resetTableItems();
                        // format special lines
                        int offset = 0;
                        formatTitleRow(offset++);
                        if (searchResults_byName.isEmpty()) {
                            formatEmptyMessage(offset++);
                        }
                        offset += searchResults_byName.size();
                        formatTitleRow(offset++);
                        if (searchResults_byContainedObject.isEmpty()) {
                            formatEmptyMessage(offset++);
                        }
                        offset += searchResults_byContainedObject.size();
                        formatTitleRow(offset++);
                        if (searchResults_byPolicy.isEmpty()) {
                            formatEmptyMessage(offset++);
                        }
                    }
                }
            });

            endOfSearch.endOfSearch();
        }

        private void formatTitleRow(int index) {
            TableItem item = tableViewer.getTable().getItem(index);
            item.setBackground(ColorBundle.LIGHT_GRAY);
        }

        private void formatEmptyMessage(int index) {
            TableItem item = tableViewer.getTable().getItem(index);
            item.setForeground(ColorBundle.LIGHT_GRAY);
        }
    }

    private void resetTableItems() {
        TableItem[] items = tableViewer.getTable().getItems();
        for (int i = 0; i < items.length; i++) {
            items[i].setBackground(null);
            Object data = items[i].getData();
            if ((data instanceof DODDigest && !((DODDigest) data).isAccessible())
             || (data instanceof DomainObjectDescriptor) && !((DomainObjectDescriptor) data).isAccessible()) {
                items[i].setForeground(getShell().getDisplay().getSystemColor(
                        SWT.COLOR_GRAY));
            } else {
                items[i].setForeground(null);
            }
        }
    }

    /**
     * Constructor
     * 
     * @param parent
     * @param style
     */
    public ComponentListPanel(Composite parent, int style) {
        super(parent, style);
        initialize();
        final EntityInfoListener entityInfoListener = new EntityInfoListener() {

            boolean firstTime = true;

            @SuppressWarnings("deprecation")
            public void EntityInfoUpdated() {
                EntityType entityType = DomainObjectHelper
                        .componentToEntityType(getComponentType());
                if (firstTime) {
                    firstTime = false;
                    // If the user cannot modify components, disable the
                    // addButton
                    if (getComponentType().equals("USER")) {
                        entityType = EntityType.USER;
                    } else if (getComponentType().equals("HOST")) {
                        entityType = EntityType.HOST;
                    } else if (getComponentType().equals("APPLICATION")) {
                        entityType = EntityType.APPLICATION;
                    } else if (getComponentType().equals("ACTION")) {
                        entityType = EntityType.ACTION;
                    } else if (getComponentType().equals("RESOURCE")) {
                        entityType = EntityType.RESOURCE;
                    } else if (getComponentType().equals("PORTAL")) {
                        entityType = EntityType.PORTAL;
                    } else if (getComponentType().equals("SERVER")) {
                        entityType = EntityType.RESOURCE;
                    } else if (getComponentType().equals("DEVICE")) {
                        entityType = EntityType.DEVICE;
                    } else if (getComponentType().equals("ENOVIA")) {
                        entityType = EntityType.ENOVIA;
                    } else if (getComponentType().equals("OBJECT")) {
                        entityType = EntityType.SAP;
                    } else {
                        entityType = EntityType.COMPONENT;
                    }
                    buttonNew.setEnabled(PolicyServerProxy
                            .getAllowedEntityTypes().contains(entityType));
                }
                if (tableViewer != null && !filtering) {
                    objectList.clear();
                    objectList.addAll(EntityInfoProvider
                            .getComponentList(getComponentType()));
                    tableViewer.refresh();
                    resetTableItems();
                    // ComponentListPanel.this.setSelectionToEditorObject();
                }
            }
        };
        EntityInfoProvider.addEntityInfoListener(entityInfoListener,
                getComponentType());

        addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                EntityInfoProvider.removeEntityInfoListener(entityInfoListener,
                        getComponentType());
            }
        });

        IComponentManager componentManager = ComponentManagerFactory
                .getComponentManager();
        IEventManager eventManager = componentManager
                .getComponent(EventManagerImpl.COMPONENT_INFO);
        eventManager.registerListener(new CurrentObjectChangedListener(),
                EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT);

        GlobalState.getInstance().addComponentListPanel(this);
    }

    /**
     * 
     */
    public void populateList() {
        EntityInfoProvider.updateComponentList(getComponentType());
    }

    public void filterList(String filter,
            FilterControlListener.EndOfSearch endOfSearch) {
        Thread t = new Thread(new FilterWorker(getComponentType(), filter,
                endOfSearch));
        t.start();
    }

    public abstract String getComponentType();

    protected IDSpec createComponent(String name) {
        return PolicyServerProxy.createBlankComponent(name, getComponentType());
    }

    protected void saveGroup(IDSpec group) {
        PolicyServerProxy.saveEntity(group);
    }

    public void initialize() {
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.verticalSpacing = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout(layout);

        Composite top = new Composite(this, SWT.BORDER);
        top.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        layout = new GridLayout(2, false);
        top.setLayout(layout);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        top.setLayoutData(data);

        buttonNew = new Button(top, SWT.PUSH | SWT.FLAT | SWT.CENTER);
        buttonNew.setText(Messages.COMPONENTLISTPANEL_NEW);
        buttonNew.setToolTipText(Messages.COMPONENTLISTPANEL_NEW);
        data = new GridData();
        buttonNew.setLayoutData(data);

        buttonNew.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TitleAreaDialogEx dlg = getInputDialog();
                if (dlg.open() == Window.OK) {
                    IDSpec spec = createComponent(dlg.getValue().trim());
                    GlobalState.getInstance().loadObjectInEditorPanel(spec);
                    populateList();
                }
            }
        });

        setupFilterControl(top);

        setupTableViewer();
    }

    /**
     * @return
     */
    protected abstract TitleAreaDialogEx getInputDialog();

    /**
     * 
     */
    protected void setupTableViewer() {
        tableViewer = new TableViewer(this, SWT.BORDER | SWT.MULTI
                | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

        /*
         * User hash table lookup of tree items. Increased performance at the
         * cost of increased memory usage
         */
        tableViewer.setUseHashlookup(true);

        /*
         * User custom comparer for hash lookup
         */
        tableViewer.setComparer(new TableItemComparer());

        final Table table = tableViewer.getTable();
        GridData gridData = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(gridData);
        table.setHeaderVisible(false);

        tableViewer
                .setContentProvider(viewContentProvider = new ViewContentProvider());
        tableViewer.setLabelProvider(new ViewLabelProvider());
        tableViewer.setInput(objectList);

        tableViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE,
                new Transfer[] { PolicyObjectTransfer.getInstance() },
                new DragSourceAdapter() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public void dragStart(DragSourceEvent event) {
                        IStructuredSelection selection = (IStructuredSelection) tableViewer
                                .getSelection();
                        Iterator iterator = selection.iterator();
                        while (iterator.hasNext()) {
                            Object selected = iterator.next();
                            if ( !( selected instanceof DODDigest && ((DODDigest) selected).isAccessible())
                              && !( selected instanceof DomainObjectDescriptor && ((DomainObjectDescriptor) selected).isAccessible())      
                            ) {
                                event.doit = false;
                                return;
                            }
                        }
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void dragSetData(DragSourceEvent event) {
                        PolicyObjectTransfer transfer = PolicyObjectTransfer
                                .getInstance();
                        // Provide the data of the requested type.
                        if (transfer.isSupportedType(event.dataType)) {
                            IStructuredSelection selection = (IStructuredSelection) tableViewer
                                    .getSelection();
                            IDSpecRef[] specRefArray = new IDSpecRef[selection
                                    .size()];
                            Iterator iterator = selection.iterator();
                            int index = 0;
                            while (iterator.hasNext()) {
                                Object next = iterator.next();
                                String name;
                                if(next instanceof DODDigest){
                                    DODDigest spec = (DODDigest) next;
                                    name = spec.getName();
                                } else {
                                    DomainObjectDescriptor spec = (DomainObjectDescriptor) next;
                                    name = spec.getName();
                                }
                                
                                IDSpecRef ref = (IDSpecRef) sm.getSpecReference(name);
                                specRefArray[index++] = ref;
                                
                            }

                            event.data = specRefArray;
                        }
                    }

                    @Override
                    public void dragFinished(DragSourceEvent event) {
                        // In the normal case, we get dragFinished after
                        // selectionChanged. When the item being dragged is
                        // already
                        // selected, we get the dragFinished before
                        // selectionChanged. We
                        // flush all events in the windows message queue to
                        // force the
                        // selectionChanged to fire.
                        getDisplay().readAndDispatch();

                        restoreSelection();
                        ComponentListPanel.this.ignoreSelection = false;
                    }
                });

        tableViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) tableViewer
                        .getSelection();
                Object selected = selection.getFirstElement();
                if (selected instanceof DODDigest) {
                    DODDigest digest = (DODDigest) selected;
                    DomainObjectDescriptor descriptor;
                    try {
                        descriptor = PolicyServerProxy.getDescriptorById(digest
                                .getId());
                    } catch (PolicyEditorException e) {
                        e.printStackTrace();
                        return;
                    }
                    GlobalState.getInstance().loadObjectInEditorPanel(
                            descriptor);
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            TableItem clickedItem = null;

            @Override
            public void mouseDown(MouseEvent e) {
                Table table = tableViewer.getTable();
                TableItem[] items = table.getSelection();
                DuplicateAction action = (DuplicateAction) PolicyStudioActionFactory
                        .getDuplicateAction();
                if (items.length == 1) {
                    DomainObjectDescriptor descriptor = null;
                    if (items[0].getData() instanceof DODDigest) {
                        DODDigest digest = (DODDigest) items[0].getData();
                        try {
                            descriptor = PolicyServerProxy
                                    .getDescriptorById(digest.getId());
                        } catch (PolicyEditorException e1) {
                            e1.printStackTrace();
                        }
                    } else if (items[0].getData() instanceof DomainObjectDescriptor) {
                        descriptor = (DomainObjectDescriptor) items[0]
                                .getData();
                    } else {
                        ComponentListPanel.this.ignoreSelection = true;
                        return;
                    }
                    DuplicateAction.VIEW = "author";
                    DuplicateAction.TYPE = descriptor.getType();
                    DuplicateAction.SOURCES.clear();
                    DuplicateAction.SOURCES.add(descriptor);
                    action.setEnabled(true);
                } else {
                    action.setEnabled(false);
                }
                ComponentListPanel.this.ignoreSelection = true;
                clickedItem = table.getItem(new Point(e.x, e.y));
            }

            @Override
            public void mouseUp(MouseEvent e) {
                TableItem item = table.getItem(new Point(e.x, e.y));
                boolean needNewSelection = false;
                if (item != null && item == clickedItem) {
                    Object data = item.getData();
                    if (data instanceof DODDigest) {
                        needNewSelection = ((DODDigest) data).isAccessible();
                    } else if (data instanceof DomainObjectDescriptor) {
                        needNewSelection = ((DomainObjectDescriptor) data)
                                .isAccessible();
                    }
                }
                if (needNewSelection) {
                    setupNewSelection();
                } else {
                    restoreSelection();
                }
                ComponentListPanel.this.ignoreSelection = false;
                if ((e.button == 3) && (item != null)
                        && (shouldShowContextMenu(item))) {
                    showContextMenu(e, item);
                }
            }
        });

        tableViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        if (!ComponentListPanel.this.ignoreSelection) {
                            setupNewSelection();
                        }
                    }
                });
    }

    /**
     * Determine if a context menu should be shown for the following menu item
     * 
     * @param item
     *            the item to test
     * @return true if a context menu should be shown; false otherwise
     */
    public boolean shouldShowContextMenu(TableItem item) {
        Object data = item.getData();

        boolean valueToReturn = false;
        if (data instanceof DODDigest || data instanceof DomainObjectDescriptor) {
            valueToReturn = true;
        }

        return valueToReturn;
    }

    private void showContextMenu(MouseEvent me, TableItem selectedItem) {
        IWorkbenchWindow iww = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        /*
         * The following should not happen
         */
        if (iww == null) {
            LoggingUtil
                    .logWarning(
                            Activator.ID,
                            "Failed to display context menu.  Active Workbench is null.",
                            null);
            return;
        }
        Shell shell = getShell();
        MenuManager cmm = createContextMenu();
        Menu contextMenu = cmm.createContextMenu(shell);

        Point l = ((Control) (me.getSource())).toDisplay(me.x, me.y);
        contextMenu.setLocation(l.x, l.y);
        contextMenu.setVisible(true);
        Display display = getShell().getDisplay();
        while (!contextMenu.isDisposed() && contextMenu.isVisible()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        contextMenu.dispose();
    }

    private MenuManager createContextMenu() {
        MenuManager menu = new MenuManager();
        menu.add(PolicyStudioActionFactory.getShowPolicyUsageAction());
        menu.add(PolicyStudioActionFactory.getShowDeployedVersionAction());
        menu.add(PolicyStudioActionFactory.getShowVersionHistoryAction());
        menu.add(new Separator());
        menu.add(PolicyStudioActionFactory.getCheckDependenciesAction());
        menu.add(PolicyStudioActionFactory.getSetTargetsAction());
        menu.add(new Separator());
        menu.add(PolicyStudioActionFactory.getModifyAction());
        menu.add(PolicyStudioActionFactory.getSubmitForDeploymentAction());
        menu.add(PolicyStudioActionFactory.getScheduleDeploymentAction());
        menu.add(PolicyStudioActionFactory.getDeactivateAction());
        menu.add(PolicyStudioActionFactory.getDeleteAction());
        menu.add(PolicyStudioActionFactory.getDuplicateAction());
        menu.add(new Separator());
        menu.add(PolicyStudioActionFactory.getObjectPropertiesAction());
        return menu;
    }

    /**
     * 
     */
    protected void setupFilterControl(Composite container) {
        filterControl = new FilterControl(container, SWT.NONE, getFindString(),
                getFindInstructions());
        filterControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        filterControl.addFilterControlListener(new FilterControlListener() {

            public void search(FilterControlEvent e,
                    FilterControlListener.EndOfSearch endOfSearch) {
                filtering = true;
                if (viewContentProvider != null) {
                    viewContentProvider.setInProgress(true);
                }
                tableViewer.refresh();
                filterList(filterControl.getText(), endOfSearch);
            }

            public void cancel(FilterControlEvent e) {
                filtering = false;
                populateList();
            }
        });
    }

    protected abstract String getFindString();

    protected abstract String getFindInstructions();

    /**
     * copy selection to clipboard
     * 
     * @see com.bluejungle.destiny.policymanager.ui.IClipboardEnabled#copy()
     */
    @SuppressWarnings("unchecked")
    public void copy() {
        IStructuredSelection selection = (IStructuredSelection) tableViewer
                .getSelection();
        IDSpecRef[] specRefArray = new IDSpecRef[selection.size()];
        if (selection.size() > 0) {
            Iterator iterator = selection.iterator();
            int index = 0;
            while (iterator.hasNext()) {
                Object tmp = iterator.next();
                if (!(tmp instanceof DomainObjectDescriptor)) {
                    continue;
                }
                DomainObjectDescriptor spec = (DomainObjectDescriptor) tmp;
                IDSpecRef ref = (IDSpecRef) sm.getSpecReference(spec.getName());
                specRefArray[index++] = ref;
            }
        }

        Clipboard clipboard = new Clipboard(getDisplay());
        clipboard.setContents(new Object[] { specRefArray },
                new Transfer[] { PolicyObjectTransfer.getInstance() });
    }

    public void cut() {
        copy();
    }

    public void paste() {
        return;
    }

    public IInputValidator getNewComponentNameValidator() {
        return new IInputValidator() {

            public String isValid(String s) {
                String stringToCheck = s.trim();
                if (EntityInfoProvider.isValidComponentName(stringToCheck)) {
                    String existingName = EntityInfoProvider
                            .getExistingComponentName(stringToCheck,
                                    getComponentType());
                    if (existingName != null) {
                        int index = existingName.indexOf(PQLParser.SEPARATOR);
                        if (index != -1) {
                            existingName = existingName.substring(index + 1);
                        }
                        return NLS
                                .bind(
                                        Messages.COMPONENTLISTPANEL_ERROR_COMPONENT_EXIST,
                                        existingName);
                    } else {
                        return null;
                    }
                } else {
                    return Messages.COMPONENTLISTPANEL_ERROR_COMPONENT_INVALID;
                }
            }
        };
    }

    /**
     * Restore selection to previous known state. This is used to restore
     * selection back to the currently selected object after a drag is complete
     */
    private void restoreSelection() {
        Table table = tableViewer.getTable();
        int[] indices = new int[selectedItems.size()];
        boolean needSelection = selectedItems.size() != table
                .getSelectionCount();

        for (int i = 0; i < indices.length; i++) {
            int index = table.indexOf(((TableItem) selectedItems.get(i)));
            indices[i] = index;
            needSelection |= !table.isSelected(index);
        }
        if (needSelection) {
            table.deselectAll();
            table.select(indices);
        }
    }

    /**
     * Handle a new table item selection
     */
    @SuppressWarnings("unchecked")
    private void setupNewSelection() {
        List<DomainObjectDescriptor> selectedItems;
        GlobalState gs = GlobalState.getInstance();
        IStructuredSelection selection = (IStructuredSelection) tableViewer
                .getSelection();
        if (selection.isEmpty()) {
            selectedItems = Collections.EMPTY_LIST;
        } else {
            selectedItems = new ArrayList<DomainObjectDescriptor>();
        }
        for (Object object : selection.toList()) {
            DomainObjectDescriptor descriptor = null;
            if (object instanceof DomainObjectDescriptor) {
                descriptor = (DomainObjectDescriptor) object;
            } else if (object instanceof DODDigest) {
                DODDigest digest = (DODDigest) object;
                try {
                    descriptor = PolicyServerProxy.getDescriptorById(digest
                            .getId());
                } catch (PolicyEditorException e) {
                    e.printStackTrace();
                    return;
                }
            }
            selectedItems.add(descriptor);
        }

        if (!selectedItems.isEmpty()) {
            DomainObjectDescriptor descriptor = selectedItems.get(0);
            IHasId currentObject = (IHasId) gs.getCurrentObject();
            if (descriptor != null
                    && (currentObject == null || currentObject.getId() != descriptor
                            .getId())) {
                ignoreCurrentObjectChangedEvent = true;
                gs.loadObjectInEditorPanel(descriptor);
                ignoreCurrentObjectChangedEvent = false;
            }
            gs.setCurrentlySelection(selectedItems);
        } else {
            gs.setCurrentlySelection(Collections.EMPTY_LIST);
        }
        memorizeSelection();

        // if (!selection.isEmpty()) {
        // selectedItems = selection.toList();
        // Object selected = selectedItems.get(0);
        // if (!(selected instanceof DODDigest)
        // && !(selected instanceof DomainObjectDescriptor)) {
        // selectedItems = Collections.EMPTY_LIST;
        // } else {
        // DomainObjectDescriptor descriptor = null;
        // if (selected instanceof DODDigest) {
        // DODDigest digest = (DODDigest) selected;
        // try {
        // descriptor = PolicyServerProxy.getDescriptorById(digest
        // .getId());
        // } catch (PolicyEditorException e) {
        // e.printStackTrace();
        // return;
        // }
        // } else if (selected instanceof DomainObjectDescriptor) {
        // descriptor = (DomainObjectDescriptor) selected;
        // }
        // IHasId currentObject = (IHasId) gs.getCurrentObject();
        // if (descriptor != null
        // && (currentObject == null || currentObject.getId() != descriptor
        // .getId())) {
        // ignoreCurrentObjectChangedEvent = true;
        // gs.loadObjectInEditorPanel(descriptor);
        // ignoreCurrentObjectChangedEvent = false;
        // }
        // }
        // } else {
        // selectedItems = Collections.EMPTY_LIST;
        // }

        // List<DomainObjectDescriptor> result = new
        // ArrayList<DomainObjectDescriptor>();
        // List<Long> ids = new ArrayList<Long>();
        //
        // for (DODDigest digest : selectedItems) {
        // ids.add(digest.getId());
        // }

        memorizeSelection();
    }

    /**
     * Remember the current selection state for later restoration.
     */
    private void memorizeSelection() {
        Table table = tableViewer.getTable();
        selectedItems.clear();
        TableItem[] items = table.getSelection();
        for (int i = 0; i < items.length; i++) {
            selectedItems.add(items[i]);
        }
    }

    /**
     * select tree item corresponding to object in editor
     */
    private void setSelectionToEditorObject() {
        GlobalState gs = GlobalState.getInstance();
        IHasId currentObject = (IHasId) gs.getCurrentObject();
        if (currentObject != null) {
            DODDigest dod = EntityInfoProvider
                    .getComponentDescriptor(DomainObjectHelper
                            .getName(currentObject));
            DomainObjectDescriptor descriptor = null;
            try {
                descriptor = PolicyServerProxy.getDescriptorById(dod.getId());
            } catch (PolicyEditorException e) {
                e.printStackTrace();
            }
            if ((descriptor != null) && (tableViewer != null)) {
                ComponentListPanel.this.ignoreSelection = true;
                tableViewer.setSelection(new StructuredSelection(descriptor),
                        true);
                gs.setCurrentlySelection(Collections.singleton(descriptor));
                memorizeSelection();
            }
        }
    }

    private class TableItemComparer implements IElementComparer {

        /**
         * @see org.eclipse.jface.viewers.IElementComparer#equals(java.lang.Object,
         *      java.lang.Object)
         */
        public boolean equals(Object a, Object b) {
            boolean valueToReturn = false;

            if ((a instanceof DomainObjectDescriptor)
                    && (b instanceof DomainObjectDescriptor)) {
                DomainObjectDescriptor elementA = (DomainObjectDescriptor) a;
                DomainObjectDescriptor elementB = (DomainObjectDescriptor) b;
                valueToReturn = elementA.getId().equals(elementB.getId());
            } else {
                /*
                 * For some strange reason which I haven't looked deep enough to
                 * determine, this method is sometimes passed ArrayList
                 * instances. Perhaps for folder? In any case, rather than
                 * spending a lot of time looking into it, simply using the
                 * default behavior
                 */
                valueToReturn = a.equals(b);
            }

            return valueToReturn;
        }

        /**
         * @see org.eclipse.jface.viewers.IElementComparer#hashCode(java.lang.Object)
         */
        public int hashCode(Object element) {
            int valueToReturn = 0;

            if (element instanceof DomainObjectDescriptor) {
                DomainObjectDescriptor descriptorElement = (DomainObjectDescriptor) element;
                valueToReturn = descriptorElement.getId().hashCode();
            } else {
                /*
                 * For some strange reason which I haven't looked deep enough to
                 * determine, this method is sometimes passed ArrayList
                 * instances. Perhaps for folder? In any case, rather than
                 * spending a lot of time looking into it, simply using the
                 * default behavior
                 */
                valueToReturn = element.hashCode();
            }

            return valueToReturn;
        }

    }

    private class CurrentObjectChangedListener implements IEventListener {

        /**
         * @see com.bluejungle.destiny.policymanager.ui.ObjectChangeListener#objectChanged()
         */
        public void onEvent(IEvent event) {
            if (!ComponentListPanel.this.ignoreCurrentObjectChangedEvent) {
                IHasId currentObject = GlobalState.getInstance()
                        .getCurrentObject();
                if (DomainObjectHelper.getComponentType(currentObject) == getComponentType()) {
                    ComponentListPanel.this.setSelectionToEditorObject();
                }
            }
        }
    }

}
