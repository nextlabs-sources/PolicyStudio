/*
 * Created on Aug 21, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.tab;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.action.AbstractExportAction;
import com.bluejungle.destiny.policymanager.action.ActionMessages;
import com.bluejungle.destiny.policymanager.action.DeleteAction;
import com.bluejungle.destiny.policymanager.action.DuplicateAction;
import com.bluejungle.destiny.policymanager.action.ExportXacmlAction;
import com.bluejungle.destiny.policymanager.action.PolicyStudioActionFactory;
import com.bluejungle.destiny.policymanager.action.RefreshAction;
import com.bluejungle.destiny.policymanager.action.SwitchAndOpenAction;
import com.bluejungle.destiny.policymanager.action.VersionHistoryAction;
import com.bluejungle.destiny.policymanager.action.tab.CreateFolderAction;
import com.bluejungle.destiny.policymanager.action.tab.DeleteFolderAction;
import com.bluejungle.destiny.policymanager.action.tab.PropertiesAction;
import com.bluejungle.destiny.policymanager.editor.PdfGenerator;
import com.bluejungle.destiny.policymanager.editor.ReadOnlyPanelFactory;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.model.EditorElementHelper;
import com.bluejungle.destiny.policymanager.model.EntityInformation;
import com.bluejungle.destiny.policymanager.model.PolicyServerHelper;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyManagerView;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PreviewPanelFactory;
import com.bluejungle.destiny.policymanager.ui.PreviewView.IPreviewPanel;
import com.bluejungle.destiny.policymanager.ui.dialogs.DialogMessages;
import com.bluejungle.destiny.policymanager.ui.dialogs.ExportDialog;
import com.bluejungle.destiny.policymanager.ui.dialogs.ObjectPropertiesDialog;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.PlatformUtils;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.policy.PolicyFolder;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

/**
 * @author bmeng
 * @version $Id$
 */

@SuppressWarnings("restriction")
public abstract class AbstractTab {
    private DomainObjectDescriptor currentComponentSelection;
    private ISelection selection;

    private class FolderExportAction extends Action {
        @Override
        public void run() {
            folderExport(false);
        }
    }

    private class FolderExportXacmlAction extends Action {
        @Override
        public void run() {
            folderExport(true);
        }
    }

    private class PolicyExportAction extends Action {
        @Override
        public void run() {
            policyExport(false);
        }
    }

    private class PolicyExportXacmlAction extends Action {
        @Override
        public void run() {
            policyExport(true);
        }
    }

    protected static final String[] DOTS = {
            ApplicationMessages.ABSTRACTTAB_DOTS_1,
            ApplicationMessages.ABSTRACTTAB_DOTS_2,
            ApplicationMessages.ABSTRACTTAB_DOTS_3 };
    protected static final IEventManager EVENT_MANAGER;
    
    private static final String POLICIES = ApplicationMessages.ABSTRACTTAB_POLICIES;
    private static final String COMPONENTS = ApplicationMessages.ABSTRACTTAB_COMPONENTS;
    static {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        EVENT_MANAGER = componentManager.getComponent(EventManagerImpl.COMPONENT_INFO);
    }
    
    
    protected final List<TabColumn> columns;
    
    private TreeViewer navigationTreeViewer;
    private TreeViewer componentTreeViewer;
    private TreeEditor navigationTreeEditor;

    private TreeItem lastItem, currentItem;
    private Composite compositeEdit;

    private CTabFolder tabFolder, detailTabFolder;
    private CTabItem tabItem, definitionTabItem, previewTabItem;

    protected Label progressLabel;
    protected ProgressBar progressBar;

    private Text textSearch;
    private String textVal = "";
    private Button buttonSearch;

    private ToolItem refreshItem, propertiesItem, newFolderItem,
        deleteFolderItem, generatePdfItem, folderExportItem, folderExportXacmlItem,
        policyExportItem, policyExportXacmlItem;

    private RefreshAction refreshAction;
    private PropertiesAction propertiesAction;
    private CreateFolderAction newFolderAction;
    private DeleteFolderAction deleteFolderAction;
    private VersionHistoryAction versionHistoryAction;
    private SwitchAndOpenAction switchAndOpenAction;
    private DuplicateAction duplicateAction;

    protected List<DODDigest> printComponentList = new ArrayList<DODDigest>();
    
    private class ColumnSelectionAdapter extends SelectionAdapter {
        
        final TabColumn column;
        
        ColumnSelectionAdapter(TabColumn column){
            this.column = column;
        }
        
        @Override
        public void widgetSelected(SelectionEvent e) {
            int direction = SWT.DOWN;
            Tree tree = componentTreeViewer.getTree();
            TreeColumn treeColumn = (TreeColumn) e.widget;
            if (treeColumn == tree.getSortColumn()) {
                switch (tree.getSortDirection()) {
                case SWT.DOWN:
                    direction = SWT.UP;
                    break;
                case SWT.UP:
                    direction = SWT.DOWN;
                    break;
                }
            } else {
                tree.setSortColumn(treeColumn);
            }
            tree.setSortDirection(direction);
            componentTreeViewer.setSorter(new ComponentTreeSorter(column, direction));
            componentTreeViewer.refresh();
        }
    }

    private class ComponentTreeSorter extends ViewerSorter {
        private TabColumn column;
        private int direction;

        public ComponentTreeSorter(TabColumn column, int direction) {
            this.column = column;
            this.direction = direction;
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            DODDigest d1 = (DODDigest)(direction == SWT.DOWN ? e1 : e2);
            DODDigest d2 = (DODDigest)(direction == SWT.DOWN ? e2 : e1);
            
            return column.compare(d1, d2);
        }
    }

    private class NavigationTreeContentProvder implements ITreeContentProvider {
        public Object[] getChildren(Object parentElement) {
            if (parentElement.equals("root")) {
                return new String[] { POLICIES, COMPONENTS };
            }
            if (parentElement.equals(POLICIES)) {
                List<DODDigest> elements = new ArrayList<DODDigest>();
                for (DODDigest descriptor : PolicyManagerView.ALL_FOLDER_LIST) {
                    String name = descriptor.getName();
                    int index = name.indexOf(PQLParser.SEPARATOR);
                    if (index == -1) {
                        elements.add(descriptor);
                    }
                }
                Collections.sort(elements, new Comparator<DODDigest>() {

                    public int compare(DODDigest o1, DODDigest o2) {
                        return EntityInformation.getDisplayName(o1)
                                .compareToIgnoreCase(
                                        EntityInformation.getDisplayName(o2));
                    }
                });
                return elements.toArray(new DODDigest[elements.size()]);
            } else if (parentElement.equals(COMPONENTS)) {
                List<String> elements = new ArrayList<String>();
                for (String type : EditorElementHelper
                        .getComponentDisplayNames()) {
                    elements.add(type.toString());
                }
                Collections.sort(elements, String.CASE_INSENSITIVE_ORDER);
                return elements.toArray(new String[elements.size()]);
            } else if (parentElement instanceof DODDigest) {
                List<DODDigest> elements = new ArrayList<DODDigest>();
                DODDigest parentDescriptor = (DODDigest) parentElement;
                if (!parentDescriptor.isAccessible()) {
                    return new Object[0];
                }
                String parentName = parentDescriptor.getName()
                        + PQLParser.SEPARATOR;

                for (DODDigest descriptor : PolicyManagerView.ALL_FOLDER_LIST) {
                    String name = descriptor.getName();
                    // children names start with the name of the parent
                    if (name.startsWith(parentName)) {
                        String nameEnding = name.substring(parentName.length(),
                                name.length());
                        int index = nameEnding.indexOf(PQLParser.SEPARATOR);
                        // child objects have no further separators
                        if (index == -1) {
                            elements.add(descriptor);
                        }
                    }
                }
                Collections.sort(elements, new Comparator<DODDigest>() {

                    public int compare(DODDigest o1, DODDigest o2) {
                        return EntityInformation.getDisplayName(o1)
                                .compareToIgnoreCase(
                                        EntityInformation.getDisplayName(o2));
                    }
                });
                return elements.toArray(new DODDigest[elements.size()]);
            }

            return new Object[0];
        }

        public Object getParent(Object element) {
            if (element instanceof String) {
                return null;
            }

            DODDigest descriptor = (DODDigest) element;
            String name = descriptor.getName();
            int index = name.lastIndexOf(PQLParser.SEPARATOR);
            if (index == -1) {
                return POLICIES;
            }
            String parentName = name.substring(0, index);
            for (DODDigest folder : PolicyManagerView.ALL_FOLDER_LIST) {
                if (folder.getName().equals(parentName)) {
                    return folder;
                }
            }
            return null;
        }

        public boolean hasChildren(Object element) {
            if (element.equals(POLICIES)) {
                return PolicyManagerView.ALL_FOLDER_LIST.size() > 0;
            }
            if (element.equals(COMPONENTS)) {
                return true;
            }

            if (element instanceof DODDigest) {
                DODDigest dod = (DODDigest) element;
                if (!dod.isAccessible()) {
                    return false;
                }
                String elemName = dod.getName();
                for (DODDigest desc : PolicyManagerView.ALL_FOLDER_LIST) {
                    if (desc.getName().startsWith(elemName)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    private class NavigationTreeLabelProvider extends LabelProvider {

        @Override
        public Image getImage(Object element) {
            if (element instanceof String) {
                String object = (String) element;
                Image image = EditorElementHelper.getComponentImage(object);
                if (image != null) {
                    return image;
                }
            }

            boolean expanded = navigationTreeViewer.getExpandedState(element);
            if (expanded) {
                return ImageBundle.FOLDER_OPEN_IMG;
            } else {
                return ImageBundle.FOLDER_IMG;
            }
        }

        @Override
        public String getText(Object element) {
            if (element instanceof String) {
                return (String) element;
            }
            if (element instanceof DODDigest) {
                EntityInformation info = PolicyManagerView.ENTITY_INFO_MAP
                        .get((DODDigest) element);
                return info.getDisplayName();
            }
            return "";
        }
    }

    private class ComponentTreeContentProvider implements ITreeContentProvider {
    	
    	public List<DODDigest> getTopPoliciesList (){
    		List<DODDigest> topPoliciesList = new ArrayList<DODDigest>();
    		for(DODDigest dgst: PolicyManagerView.ALL_POLICY_LIST){
    			if(!(dgst.isSubPolicy())){
    				topPoliciesList.add(dgst);
    			}
    		}
    		return topPoliciesList;
    	}

        public Object[] getChildren(Object parentElement) {
            String search = getSearchText();
            List<DODDigest> topPoliciesList = getTopPoliciesList();
            if (parentElement instanceof String) {
                List<DODDigest> elements = new ArrayList<DODDigest>();
                if (parentElement.equals(POLICIES)) {
                    for (DODDigest digest : topPoliciesList) {
                        if (EntityInformation.getDisplayName(digest).toUpperCase().indexOf(
                                search.toUpperCase()) != -1
                               && hasCorrectStatus(digest)) {      
                    		elements.add(digest);
                    	}
                    }
                    return elements.toArray(new DODDigest[elements.size()]);                 
                } else if (parentElement.equals(COMPONENTS)) {
                    for (DODDigest digest : PolicyManagerView.ALL_COMPONENT_LIST) {
                        if (EntityInformation.getDisplayName(digest).toUpperCase().indexOf(
                                search.toUpperCase()) != -1
                                && hasCorrectStatus(digest)) {
                            elements.add(digest);
                        }
                    }
                } else {
                    String type = EditorElementHelper
                            .getComponentContextByDisplayName((String) parentElement);
                    if (type != null) {
                        getComponentForType(elements, type);
                    }
                }
                Collections.sort(elements, new Comparator<DODDigest>() {

                    public int compare(DODDigest o1, DODDigest o2) {
                        return EntityInformation.getDisplayName(o1)
                                .compareToIgnoreCase(
                                        EntityInformation.getDisplayName(o2));
                    }
                });                
                return elements.toArray(new DODDigest[elements.size()]);
            } else if (parentElement instanceof DODDigest) {
                DODDigest parentDescriptor = (DODDigest) parentElement;
                if (!parentDescriptor.isAccessible()) {
                    return new Object[0];
                }
                if (parentDescriptor.getType().equals("FOLDER")) {
                    List<DODDigest> elements = new ArrayList<DODDigest>();
                    String parentName = parentDescriptor.getName()
                            + PQLParser.SEPARATOR;

                    for (DODDigest digest : topPoliciesList) {
                        String name = digest.getName();
                        // children names start with the name of the parent
                        if (name.startsWith(parentName)
                                && hasCorrectStatus(digest)) {
                            elements.add(digest);
                        }
                    }
                    Collections.sort(elements, new Comparator<DODDigest>() {

                        public int compare(DODDigest o1, DODDigest o2) {
                            return EntityInformation.getDisplayName(o1)
                                    .compareToIgnoreCase(
                                            EntityInformation
                                                    .getDisplayName(o2));
                        }
                    });
                    return elements.toArray(new DODDigest[elements.size()]);
                }

                List<DODDigest> elements = new ArrayList<DODDigest>();
                List<DODDigest> list = new ArrayList<DODDigest>();
                list.add(parentDescriptor);
                Collection<DODDigest> dependencies = PolicyServerProxy.getDirectDependenciesDigest(list);
                elements.addAll(dependencies);
                Collections.sort(elements, new Comparator<DODDigest>() {

                    public int compare(DODDigest o1, DODDigest o2) {
                        return EntityInformation.getDisplayName(o1)
                                .compareToIgnoreCase(
                                        EntityInformation.getDisplayName(o2));
                    }
                });
                return elements.toArray(new DODDigest[elements.size()]);
            }
            return new Object[0];
        }

        public Object getParent(Object element) {
            return null;
        }

        public boolean hasChildren(Object element) {
            if (element instanceof DODDigest) {
                DODDigest descriptor = (DODDigest) element;
                return descriptor.hasDependencies();
            }
            return false;
        }

        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    public abstract boolean hasCorrectStatus(DODDigest info);

    private class ComponentTreeLabelProvider extends LabelProvider implements
            ITableLabelProvider {
        public Image getColumnImage(Object element, int columnIndex) {
            return columns.get(columnIndex).getColumnImage(element);
        }

        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof DODDigest) {
                return columns.get(columnIndex).getColumnText((DODDigest)element);
            }
            //FIXME, why the element is not DODDigest
            return "";
        }
    }

    protected AbstractTab(CTabFolder folder, PolicyManagerView view) {
        this.tabFolder = folder;
        
        columns = new ArrayList<TabColumn>();
        addColumns(columns);
        ((ArrayList<TabColumn>)columns).trimToSize();
        
        create();
    }
    
    protected void addColumns(List<TabColumn> columns) {
        columns.add(TabColumn.DISPLAY_NAME_COLUMN);
        columns.add(TabColumn.VERSION_COLUMN);
        columns.add(TabColumn.SUBMITTED_TIME_COLUMN);
        columns.add(TabColumn.OWNED_BY_COLUMN);
        columns.add(TabColumn.STATUS_COLUMN);
    }

    private void create() {
        tabItem = new CTabItem(tabFolder, SWT.NONE);
        tabItem.setImage(getTabImage());
        tabItem.setText(getTabTitle());
        createContentForTab(tabItem);
    }

    protected abstract String getTabTitle();

    protected abstract Image getTabImage();

    private void createContentForTab(CTabItem item) {
        // initialize the actions
        refreshAction = new RefreshAction();
        propertiesAction = new PropertiesAction(this);
        newFolderAction = new CreateFolderAction(this);
        deleteFolderAction = new DeleteFolderAction(this);
        versionHistoryAction = PolicyStudioActionFactory.getVersionHistoryAction();
        switchAndOpenAction = PolicyStudioActionFactory.getSwitchAndOpenAction();
        duplicateAction = (DuplicateAction) PolicyStudioActionFactory.getDuplicateAction();

        Composite root = new Composite(item.getParent(), SWT.NONE);
        root.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        item.setControl(root);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        root.setLayout(layout);

        SashForm formTree = new SashForm(root, SWT.HORIZONTAL);
        GridData data = new GridData(GridData.FILL_BOTH);
        formTree.setLayoutData(data);

        Composite treeComposite = new Composite(formTree, SWT.BORDER);
        layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        treeComposite.setLayout(layout);
        data = new GridData(GridData.FILL_BOTH);
        treeComposite.setLayoutData(data);

        progressLabel = new Label(treeComposite, SWT.NONE);
        progressLabel.setText(ApplicationMessages.ABSTRACTTAB_PROCESSING + DOTS[2]);
        progressLabel.setVisible(false);
        data = new GridData();
        progressLabel.setLayoutData(data);

        progressBar = new ProgressBar(treeComposite, SWT.NONE);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setVisible(false);
        data = new GridData(GridData.FILL_HORIZONTAL);
        progressBar.setLayoutData(data);

        createNavigationTreeToolBar(treeComposite);

        navigationTreeViewer = new TreeViewer(treeComposite, SWT.BORDER);
        navigationTreeViewer.setContentProvider(new NavigationTreeContentProvder());
        navigationTreeViewer.setLabelProvider(new NavigationTreeLabelProvider());
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 3;
        navigationTreeViewer.getTree().setLayoutData(data);
        Tree navigationTree = navigationTreeViewer.getTree();
        navigationTree.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateNavigationTreeSelection();
            }
        });
        navigationTree.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
            	if (e.keyCode == SWT.F5) {
            		PolicyManagerView.refreshCurrentTab();
            	}
                super.keyPressed(e);
            }
        });

        navigationTreeEditor = new TreeEditor(navigationTreeViewer.getTree());

        navigationTreeViewer.addTreeListener(new ITreeViewerListener() {

            public void treeCollapsed(TreeExpansionEvent event) {
                Object obj = event.getElement();
                postUpdate(obj);
            }

            public void treeExpanded(TreeExpansionEvent event) {
                Object obj = event.getElement();
                postUpdate(obj);
            }
        });

        final MenuManager mgr = new MenuManager();
        mgr.setRemoveAllWhenShown(true);
        mgr.addMenuListener(new IMenuListener() {

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse
             * .jface.action.IMenuManager)
             */
            public void menuAboutToShow(IMenuManager manager) {
                IStructuredSelection selection = (IStructuredSelection) navigationTreeViewer
                        .getSelection();
                if (!selection.isEmpty()) {
                    Object object = selection.getFirstElement();
                    if (object instanceof DODDigest || object.equals(POLICIES)) {
                        FolderExportAction action = new FolderExportAction();
                        action.setText(PolicyStudioActionFactory.getExportAction() .getText());
                        action.setImageDescriptor(PolicyStudioActionFactory.getExportAction().getImageDescriptor());
                        mgr.add(action);
                        
                        FolderExportXacmlAction xacmlAction = new FolderExportXacmlAction();
                        xacmlAction.setText(new ExportXacmlAction().getText());
                        xacmlAction.setImageDescriptor(PolicyStudioActionFactory.getExportXacmlAction().getImageDescriptor());
                        mgr.add(xacmlAction);

                    }
                    mgr.add(refreshAction);
                    mgr.add(propertiesAction);
                    mgr.add(new Separator());
                    mgr.add(newFolderAction);
                    mgr.add(deleteFolderAction);
                    mgr.add(duplicateAction);
                    updateActionStatus();
                }
            }
        });
        navigationTreeViewer.getControl().setMenu(
                mgr.createContextMenu(navigationTreeViewer.getControl()));

        SashForm formDetail = new SashForm(formTree, SWT.VERTICAL);
        data = new GridData(GridData.FILL_BOTH);
        formDetail.setLayoutData(data);

        Composite tableComposite = new Composite(formDetail, SWT.BORDER);
        layout = new GridLayout(4, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        tableComposite.setLayout(layout);
        data = new GridData(GridData.FILL_BOTH);
        tableComposite.setLayoutData(data);

        Label label = new Label(tableComposite, SWT.NONE);
        label.setText("  ");

        textSearch = new Text(tableComposite, SWT.BORDER);
        textSearch.setEnabled(false);
        data = new GridData();
        data.widthHint = 200;
        textSearch.setLayoutData(data);
        textSearch.setText(ApplicationMessages.ABSTRACTTAB_ENTER_TERM);
        textSearch.setForeground(ResourceManager.getColor(SWT.COLOR_DARK_GRAY));
        textSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textSearch.setForeground(ResourceManager.getColor(SWT.COLOR_BLACK));
                if (!textSearch.getText().equals(textVal)) {
                    textSearch.setText(textVal);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                textVal = textSearch.getText();
                if (textVal.length() == 0) {
                    textSearch.setForeground(ResourceManager.getColor(SWT.COLOR_DARK_GRAY));
                    textSearch.setText(ApplicationMessages.ABSTRACTTAB_ENTER_TERM);
                }
            }
        });

        buttonSearch = new Button(tableComposite, SWT.PUSH);
        buttonSearch.setEnabled(false);
        buttonSearch.setText(ApplicationMessages.ABSTRACTTAB_SEARCH);
        data = new GridData();
        buttonSearch.setLayoutData(data);
        buttonSearch.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                componentTreeViewer.refresh();

                // pack the column
                for (TreeColumn column : componentTreeViewer.getTree().getColumns()) {
                    column.pack();
                }
            }
        });

        createComponentTreeToolBar(tableComposite);

        componentTreeViewer = new TreeViewer(tableComposite, SWT.BORDER
                | SWT.FULL_SELECTION | SWT.MULTI);
        componentTreeViewer
                .setContentProvider(new ComponentTreeContentProvider());
        componentTreeViewer.setLabelProvider(new ComponentTreeLabelProvider());
        Tree componentTree = componentTreeViewer.getTree();
        componentTree.setHeaderVisible(true);
        componentTree.setLinesVisible(true);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 4;
        componentTree.setLayoutData(data);
        
        
        componentTree.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateComponentTreeSelection();
                setDuplicateAction();
            }
            
        });
        componentTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                updateComponentTreeSelection();
                setDuplicateAction();
            }
        });

        componentTreeViewer.addTreeListener(new ITreeViewerListener() {

            public void treeCollapsed(TreeExpansionEvent event) {
                updateComponentTreeSelection();
            }

            public void treeExpanded(TreeExpansionEvent event) {
                updateComponentTreeSelection();
            }
        });

        final MenuManager menuManager = new MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new IMenuListener() {

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse
             * .jface.action.IMenuManager)
             */
            @SuppressWarnings("unchecked")
            public void menuAboutToShow(IMenuManager manager) {
                IStructuredSelection selection = (IStructuredSelection) componentTreeViewer
                        .getSelection();
                if (!selection.isEmpty()) {
                    boolean result = true;
                    IHasId hasId = null;
                    Iterator<DODDigest> iterator = selection.iterator();
                    printComponentList.clear();
                    while (iterator.hasNext()) {
                        DODDigest descriptor = iterator.next();
                        try {
        					hasId= (IHasId) PolicyServerProxy.getEntityForDescriptor(PolicyServerProxy.getDescriptorById(descriptor.getId()));
        				} catch (PolicyEditorException e) {
        					e.printStackTrace();
        				}
        				if (descriptor.getType().equals("POLICY")){
        					IPolicy policy = (IPolicy) hasId;
        					if(PolicyHelpers.isSubPolicy(policy)){
        						result = false;
        					}
        				}else{
                            result = false;
                        }
                        printComponentList.add(descriptor);
                    }
                    if (!printComponentList.isEmpty()) {
                        generatePdfItem.setEnabled(true);
                    }
                    if (result) {
                        policyExportItem.setEnabled(true);
                        policyExportXacmlItem.setEnabled(true);
                    }
                    if (componentTreeViewer.getTree().getSelection().length != 1) {
                        versionHistoryAction.setEnabled(false);
                        switchAndOpenAction.setEnabled(false);
                        duplicateAction.setEnabled(false);
                    } else {
                        versionHistoryAction.setEnabled(true);
                        switchAndOpenAction.setEnabled(true);
                        duplicateAction.setEnabled(true);
                    }
                    DODDigest digest = (DODDigest) selection.getFirstElement();
                    DomainObjectDescriptor descriptor;
                    try {
                        descriptor = PolicyServerProxy.getDescriptorById(digest.getId());
                    } catch (PolicyEditorException e) {
                        e.printStackTrace();
                        return;
                    }
                    versionHistoryAction.setDomainObjectDescriptor(descriptor);
                    PolicyExportAction action = new PolicyExportAction();
                    action.setText(PolicyStudioActionFactory.getExportAction().getText());
                    action.setImageDescriptor(PolicyStudioActionFactory.getExportAction().getImageDescriptor());
                    action.setEnabled(result);
                    menuManager.add(action);				

                    PolicyExportXacmlAction xacmlAction = new PolicyExportXacmlAction();
                    xacmlAction.setText(new ExportXacmlAction().getText());
                    xacmlAction.setImageDescriptor(PolicyStudioActionFactory.getExportXacmlAction().getImageDescriptor());
                    xacmlAction.setEnabled(result);
                    menuManager.add(xacmlAction);

                    menuManager.add(new Separator());
                    menuManager.add(versionHistoryAction);
                    switchAndOpenAction.setDomainObjectDescriptor(descriptor);
                    menuManager.add(switchAndOpenAction);
                    menuManager.add(duplicateAction);
                }
            }
        });
        componentTreeViewer.getControl().setMenu(
                        menuManager.createContextMenu(componentTreeViewer.getControl()));

        for (TabColumn column : columns) {
            TreeColumn treeColumn = new TreeColumn(componentTree, column.aligments);
            treeColumn.setWidth(column.widths);
            treeColumn.setText(column.title);
            treeColumn.addSelectionListener(new ColumnSelectionAdapter(column));
        }

        detailTabFolder = new CTabFolder(formDetail, SWT.BORDER);
        data = new GridData(GridData.FILL_BOTH);
        detailTabFolder.setLayoutData(data);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        detailTabFolder.setLayout(layout);

        definitionTabItem = new CTabItem(detailTabFolder, SWT.NONE);
        definitionTabItem.setText(ApplicationMessages.ABSTRACTTAB_DEFINITION);

        createAdditionalTab(detailTabFolder);

        previewTabItem = new CTabItem(detailTabFolder, SWT.NONE);
        previewTabItem.setText(ApplicationMessages.ABSTRACTTAB_PREVIEW);

        detailTabFolder.setSelection(definitionTabItem);

        formTree.setWeights(new int[] { 1, 2 });
        formDetail.setWeights(new int[] { 1, 1 });
    }

    protected void createAdditionalTab(CTabFolder folder) {
    }

    protected void updateAdditionalTab() {
    }

    private void createComponentTreeToolBar(Composite tableComposite) {
        GridData data;
        ToolBar toolBar = new ToolBar(tableComposite, SWT.FLAT);

        policyExportItem = new ToolItem(toolBar, SWT.NONE);
        policyExportItem.setToolTipText(ApplicationMessages.ABSTRACTTAB_EXPORT);
        policyExportItem.setEnabled(false);
        policyExportItem.setImage(PolicyStudioActionFactory.getExportAction().getImageDescriptor().createImage());
        policyExportItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                policyExport(false);
            }
        });


        policyExportXacmlItem = new ToolItem(toolBar, SWT.NONE);
        policyExportXacmlItem.setToolTipText(ApplicationMessages.ABSTRACTTAB_EXPORT_AS_XACML);
        policyExportXacmlItem.setEnabled(false);
        policyExportXacmlItem.setImage(PolicyStudioActionFactory.getExportXacmlAction().getImageDescriptor().createImage());
        policyExportXacmlItem.setDisabledImage(ImageBundle.XACML_EXPORT_DISABLED);
        policyExportXacmlItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                policyExport(true);
            }
        });

        generatePdfItem = new ToolItem(toolBar, SWT.NONE);
        generatePdfItem.setToolTipText(ApplicationMessages.ABSTRACTTAB_GENERATE_PDF);
        generatePdfItem.setEnabled(false);
        generatePdfItem.setImage(ImageBundle.PRINT_IMG);
        generatePdfItem.setDisabledImage(ImageBundle.PRINT_DISABLED_IMG);
        generatePdfItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = componentTreeViewer.getTree().getShell();
                FileDialog dialog = new FileDialog(shell, SWT.SAVE);
                dialog.setText(ApplicationMessages.ABSTRACTTAB_SAVE_AS_PDF);
                dialog.setFilterExtensions(new String[] { "*.pdf", "*.*" });
                dialog.setFileName("report");
                String path = dialog.open();
                if (path == null) {
                    return;
                }
                if (!path.toLowerCase().endsWith(".pdf")) {
                    path += ".pdf";
                }
                File file = new File(path);
                if (file.exists()
                        && !MessageDialog.openConfirm(shell,
                                ApplicationMessages.ABSTRACTTAB_CONFIRM,
                                ApplicationMessages.ABSTRACTTAB_CONFIRM_MSG)) {
                    return;
                }
                
                //convert DODDigest to DomainObjectDescriptor
                List<DomainObjectDescriptor> dods = new ArrayList<DomainObjectDescriptor>(printComponentList.size());
                for (DODDigest d : printComponentList) {
                    try {
                        DomainObjectDescriptor descriptor = PolicyServerProxy.getDescriptorById(d.getId());
                        dods.add(descriptor);
                        boolean test = false;
                        
                        if(test){
                            throw new PolicyEditorException("Sometyhing something");
                        }
                    } catch (PolicyEditorException pee) {
                        LoggingUtil.logError(Activator.ID, "Can't generate the PDF file", pee);
                        return;
                    }
                }

                PdfGenerator generator = new PdfGenerator(path, dods);
                generator.run();
                Program.launch(path);
            }
        });

        createAdditonalComponentTreeToolItem(toolBar);

        toolBar.pack();
        data = new GridData(GridData.HORIZONTAL_ALIGN_END
                | GridData.VERTICAL_ALIGN_CENTER);
        toolBar.setLayoutData(data);
    }

    protected void createAdditonalComponentTreeToolItem(ToolBar toolBar) {
    }

    private void postUpdate(final Object obj) {
        navigationTreeViewer.getControl().getDisplay().asyncExec(
                new Runnable() {

                    public void run() {
                        navigationTreeViewer.update(obj, null);
                    }
                });
    }

    private void updateActionStatus() {
        propertiesAction.setEnabled(false);
        newFolderAction.setEnabled(false);
        deleteFolderAction.setEnabled(false);
        IStructuredSelection selection = (IStructuredSelection) navigationTreeViewer
                .getSelection();
        if (selection.isEmpty()) {
            return;
        }

        Object element = selection.getFirstElement();
        if (element instanceof DODDigest) {
            propertiesAction.setEnabled(true);
        }
        if (element.equals(POLICIES) || element instanceof DODDigest) {
            newFolderAction.setEnabled(true);
        }
        if (element instanceof DODDigest && refereshDeleteFolderAction((DODDigest)element)) {
            deleteFolderAction.setEnabled(true);
        }
    }
    
    private boolean refereshDeleteFolderAction(DODDigest element){
		String folderName = element.getName();
		Collection<DomainObjectDescriptor> entitiesInFolder = PolicyServerProxy
				.getEntityList(PolicyServerProxy.escape(folderName)
						+ PQLParser.SEPARATOR + "%",
						DeleteAction.POLICY_FOLDER_CONTAINED_ENTITY_TYPES);

		return ((entitiesInFolder == null) || (entitiesInFolder.isEmpty()));
    }

    private void enterEditingMode() {
        if (currentItem != null && currentItem == lastItem) {
            Object data = currentItem.getData();
            if (data instanceof String) {
                return;
            }

            if (data instanceof DODDigest) {
                DODDigest descriptor = (DODDigest) data;
                if (!canRename(descriptor)) {
                    return;
                }
            }

            boolean showBorder = true;
            compositeEdit = new Composite(navigationTreeViewer.getTree(),
                    SWT.NONE);
            if (showBorder)
                compositeEdit.setBackground(ResourceManager
                        .getColor(SWT.COLOR_BLACK));
            final Text text = new Text(compositeEdit, SWT.NONE);
            final int inset = showBorder ? 1 : 0;
            compositeEdit.addListener(SWT.Resize, new Listener() {

                public void handleEvent(Event e) {
                    Rectangle rect = compositeEdit.getClientArea();
                    text.setBounds(rect.x + inset, rect.y + inset, rect.width
                            - inset * 2, rect.height - inset * 2);
                }
            });
            Listener textListener = new Listener() {

                public void handleEvent(final Event e) {
                    switch (e.type) {
                    case SWT.FocusOut:
                        setNewValue(currentItem, text.getText());
                        compositeEdit.dispose();
                        break;
                    case SWT.Verify:
                        String newText = text.getText();
                        /*
                         * allow backspace and delete characters to pass through so that 
                         * user will not be stuck at 128 characters
                         */
                        if (newText.length() >= 128 && (e.character != SWT.BS && e.character != SWT.DEL)) {
                            e.doit = false;
                        	return;
                        }
                        String leftText = newText.substring(0, e.start);
                        String rightText = newText.substring(e.end, newText
                                .length());
                        GC gc = new GC(text);
                        Point size = gc.textExtent(leftText + e.text
                                + rightText);
                        gc.dispose();
                        size = text.computeSize(size.x, SWT.DEFAULT);
                        navigationTreeEditor.horizontalAlignment = SWT.LEFT;
                        Rectangle itemRect = currentItem.getBounds(),
                        rect = navigationTreeViewer.getTree().getClientArea();
                        navigationTreeEditor.minimumWidth = Math.max(size.x,
                                itemRect.width)
                                + inset * 2;
                        int left = itemRect.x,
                        right = rect.x + rect.width;
                        navigationTreeEditor.minimumWidth = Math
                                .min(navigationTreeEditor.minimumWidth, right
                                        - left);
                        navigationTreeEditor.minimumHeight = size.y + inset * 2;
                        navigationTreeEditor.layout();
                        break;
                    case SWT.Traverse:
                        switch (e.detail) {
                        case SWT.TRAVERSE_RETURN:
                            setNewValue(currentItem, text.getText());
                            // FALL THROUGH
                        case SWT.TRAVERSE_ESCAPE:
                            compositeEdit.dispose();
                            e.doit = false;
                        }
                        break;
                    }
                }
            };
            
            /*
             * Listener to ignore characters that are not permitted 
             */
            text.addKeyListener(new KeyAdapter() {
    			@Override
    			public void keyPressed(KeyEvent e) {
    				PlatformUtils.validCharForName(e);
    			}
    		});
            text.addListener(SWT.FocusOut, textListener);
            text.addListener(SWT.Traverse, textListener);
            text.addListener(SWT.Verify, textListener);
            navigationTreeEditor.setEditor(compositeEdit, currentItem);
            text.setText(currentItem.getText());
            text.selectAll();
            text.setFocus();
        }
        lastItem = currentItem;
    }

    public void disposeEdit() {
        if (compositeEdit != null && !compositeEdit.isDisposed()) {
            compositeEdit.dispose();
        }
    }

    private void setNewValue(TreeItem item, Object value) {
        disposeEdit();

        Object data = item.getData();
        if (!(data instanceof DODDigest)) {
            return;
        }
        DODDigest digest = (DODDigest) data;
        if (getDisplayName(digest).equals(value)) {
            return;
        }
        DomainObjectDescriptor descriptor = null;
        try {
            descriptor = PolicyServerProxy.getDescriptorById(digest.getId());
        } catch (PolicyEditorException e) {
            e.printStackTrace();
            return;
        }
        PolicyFolder folder = (PolicyFolder) PolicyServerProxy
                .getEntityForDescriptor(descriptor);
        String currentName = folder.getName();
        int index = currentName.lastIndexOf(PQLParser.SEPARATOR);
        String newName = "";
        if (index >= 0) {
            newName = currentName.substring(0, index + 1);
        }
        newName = newName + (String) value;
        folder.setName(newName);
        Collection<DODDigest> list = PolicyServerProxy.saveEntityDigest(folder);
        if (list != null) {
            PolicyManagerView.ALL_FOLDER_LIST.remove(digest);
            PolicyManagerView.ENTITY_INFO_MAP.remove(digest);
            PolicyManagerView.ALL_FOLDER_LIST.addAll(list);
            for (DODDigest des : list) {
                PolicyManagerView.extractEntityInfo(des);
            }
        }
        navigationTreeViewer.refresh();

        EntityInfoProvider.updatePolicyTreeAsync();
    }

    private void createNavigationTreeToolBar(Composite treeComposite) {
        GridData data;
        ToolBar toolBar = new ToolBar(treeComposite, SWT.FLAT);

        folderExportItem = new ToolItem(toolBar, SWT.NONE);
        folderExportItem.setToolTipText(ApplicationMessages.ABSTRACTTAB_EXPORT);
        folderExportItem.setImage(PolicyStudioActionFactory.getExportAction().getImageDescriptor().createImage());
        folderExportItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                folderExport(false);
            }
        });

        folderExportXacmlItem = new ToolItem(toolBar, SWT.NONE);
        folderExportXacmlItem.setToolTipText(ApplicationMessages.ABSTRACTTAB_EXPORT_AS_XACML);
        folderExportXacmlItem.setImage(PolicyStudioActionFactory.getExportXacmlAction().getImageDescriptor().createImage());
        folderExportXacmlItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                folderExport(true);
            }
        });

        refreshItem = new ToolItem(toolBar, SWT.NONE);
        refreshItem.setToolTipText(ApplicationMessages.ABSTRACTTAB_REFRESH);
        refreshItem.setImage(ImageBundle.REFRESH_IMG);
        refreshItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PolicyManagerView.refreshCurrentTab();
            }
        });

        propertiesItem = new ToolItem(toolBar, SWT.NONE);
        propertiesItem
                .setToolTipText(ApplicationMessages.ABSTRACTTAB_PROPERTIES);
        propertiesItem.setEnabled(false);
        propertiesItem.setImage(ImageBundle.PROPERTIES_IMG);
        propertiesItem.setDisabledImage(ImageBundle.PROPERTIES_DISABLED_IMG);
        propertiesItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setFolderProperties();
            }
        });

        new ToolItem(toolBar, SWT.SEPARATOR);

        newFolderItem = new ToolItem(toolBar, SWT.NONE);
        newFolderItem.setToolTipText(ActionMessages.ACTION_NEW_FOLDER);
        newFolderItem.setEnabled(false);
        newFolderItem.setImage(ImageBundle.CREATE_FOLDER_IMG);
        newFolderItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                createFolder();
            }
        });

        deleteFolderItem = new ToolItem(toolBar, SWT.NONE);
        deleteFolderItem
                .setToolTipText(ApplicationMessages.ABSTRACTTAB_DELETE_FOLDER);
        deleteFolderItem.setEnabled(false);
        deleteFolderItem.setImage(ImageBundle.DELETE_FOLDER_IMG);
        deleteFolderItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteFolder();
            }
        });

        toolBar.pack();
        data = new GridData(GridData.HORIZONTAL_ALIGN_END
                | GridData.VERTICAL_ALIGN_CENTER);
        toolBar.setLayoutData(data);
    }

    private void folderExport(boolean isXacml) {
        AbstractExportAction action;
        String extension;
        if (isXacml) {
            action = PolicyStudioActionFactory.getExportXacmlAction();
            extension = ".xacml";
        } else {
            action = PolicyStudioActionFactory.getExportAction();
            extension = ".xml";
        }

        String defaultExtension = action.getDefaultExtension();

        IStructuredSelection selection = (IStructuredSelection) navigationTreeViewer
                .getSelection();
        Object object = selection.getFirstElement();
        Shell shell = Display.getCurrent().getActiveShell();
        List<DODDigest> exportList = new ArrayList<DODDigest>();
        File file = null;
        if (object instanceof DODDigest) {
            DODDigest descriptor = (DODDigest) object;
            String prefix = descriptor.getName()+"/";
            ExportDialog dialog = new ExportDialog(shell, extension);
            if (dialog.open() == Window.CANCEL) {
                return;
            }
            file = new File(dialog.getPath());
            action.setFile(file);
            int index = dialog.getSelection();
            for (DODDigest digest : PolicyManagerView.ALL_POLICY_LIST) {
                String name = digest.getName();
                int pos = name.indexOf(prefix);
                if (pos !=0 || !hasCorrectStatus(digest)) {
                    continue;
                }
                if (index == 0) {
                    exportList.add(digest);
                } else if (index == 1) {
                    String rest = name.substring(prefix.length() + 1);
                    if (rest.indexOf(PQLParser.SEPARATOR) == -1) {
                        exportList.add(digest);
                    }
                }
            }
        } else if (object.equals(POLICIES)) {
            FileDialog dialog = new FileDialog(shell, SWT.SAVE);
            dialog.setText(DialogMessages.EXPORTDIALOG_TITLE);
            dialog.setFilterExtensions(new String[] { "*" + defaultExtension, "*.*" });
            String path = dialog.open();
            if (path == null) {
                return;
            }
            if (!path.toLowerCase().endsWith(defaultExtension)) {
                path += defaultExtension;
            }
            file = new File(path);
            if (file.exists()) {
                if (!MessageDialog
                        .openConfirm(
                                shell,
                                "Confirm to Overwrite",
                                path
                                        + " already exists.\nDo you want to overwirte it?")) {
                    return;
                }
            }
            for (DODDigest digest : PolicyManagerView.ALL_POLICY_LIST) {
                if (hasCorrectStatus(digest)) {
                    exportList.add(digest);
                }
            }
        }
        action.setFile(file);
        List<Long> ids = new ArrayList<Long>();
        for (DODDigest digest : exportList) {
            ids.add(digest.getId());
        }
        List<DomainObjectDescriptor> des = null;
        try {
            des = PolicyServerProxy.getDescriptorsByIds(ids);
        } catch (PolicyEditorException e) {
            e.printStackTrace();
            return;
        }
        action.setPolicyList(des);
        action.run();
    }

    @SuppressWarnings("unchecked")
    private void policyExport(boolean isXacml) {
        AbstractExportAction action;

        if (isXacml) {
            action = PolicyStudioActionFactory.getExportXacmlAction();
        } else {
            action = PolicyStudioActionFactory.getExportAction();
        }

        String defaultExtension = action.getDefaultExtension();

        Shell shell = Display.getCurrent().getActiveShell();
        List<DomainObjectDescriptor> exportList = new ArrayList<DomainObjectDescriptor>();
        List<Long> digests = new ArrayList<Long>();
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setText(DialogMessages.EXPORTDIALOG_TITLE);
        dialog.setFilterExtensions(new String[] { "*" + defaultExtension, "*.*" });
        String path = dialog.open();
        if (path == null) {
            return;
        }
        if (!path.toLowerCase().endsWith(defaultExtension)) {
            path += defaultExtension;
        }
        File file = new File(path);
        if (file.exists()) {
            if (!MessageDialog.openConfirm(shell, "Confirm to Overwrite", path
                    + " already exists.\nDo you want to overwirte it?")) {
                return;
            }
        }

        IStructuredSelection selection = (IStructuredSelection) componentTreeViewer
                .getSelection();
        if (!selection.isEmpty()) {
            Iterator<DODDigest> iterator = selection.iterator();
            while (iterator.hasNext()) {
                DODDigest digest = iterator.next();
                digests.add(digest.getId());
            }
        }
        try {
            exportList = PolicyServerProxy.getDescriptorsByIds(digests);
        } catch (PolicyEditorException e) {
            e.printStackTrace();
            return;
        }
        action.setFile(file);
        action.setPolicyList(exportList);
        action.run();
    }

    public CTabItem getTabItem() {
        return tabItem;
    }

    protected String getDisplayName(DODDigest descriptor) {
        String fullName = descriptor.getName();
        int index = fullName.lastIndexOf(PQLParser.SEPARATOR);
        if (index == -1) {
            return fullName;
        } else {
            return fullName.substring(index + 1, fullName.length());
        }
    }

    private void updateNavigationTreeToolBarStatus() {
        duplicateAction.setEnabled(false);
        folderExportItem.setEnabled(false);
        folderExportXacmlItem.setEnabled(false);
        propertiesItem.setEnabled(false);
        newFolderItem.setEnabled(false);
        deleteFolderItem.setEnabled(false);

        textSearch.setEnabled(false);
        textVal = "";
        textSearch.setForeground(ResourceManager.getColor(SWT.COLOR_DARK_GRAY));
        textSearch.setText(ApplicationMessages.ABSTRACTTAB_ENTER_TERM);
        buttonSearch.setEnabled(false);
        IStructuredSelection selection = (IStructuredSelection) navigationTreeViewer
                .getSelection();
        if (selection.isEmpty()) {
            return;
        }

        Object element = selection.getFirstElement();
        if (element instanceof DODDigest) {
            DODDigest descriptor = (DODDigest) element;
            List<DODDigest> result = new ArrayList<DODDigest>();
            String folderName = descriptor.getName();
            for (DODDigest digest : PolicyManagerView.ENTITY_INFO_MAP.keySet()) {
                String fullName = digest.getName();
                if (fullName.startsWith(folderName)) {
                    String subName = fullName.substring(folderName.length());
                    if (subName.length() > 0
                            && !subName.startsWith(PQLParser.SEPARATOR + "")) {
                        continue;
                    }
                    if (digest.getType().equals("FOLDER")) {
                        result.add(digest);
                    } else if (hasCorrectStatus(digest)) {
                        result.add(digest);
                    }
                }
            }
            DuplicateAction.VIEW = "manager";
            DuplicateAction.TYPE = EntityType.FOLDER;
            List<DomainObjectDescriptor> descriptors = null;
            try {
                descriptors = PolicyServerProxy.getDescriptorsByDigests(result);
            } catch (PolicyEditorException e) {
                e.printStackTrace();
            }
            DuplicateAction.SOURCES.clear();
            DuplicateAction.SOURCES.addAll(descriptors);
            duplicateAction.setEnabled(true);
        }
        if (element instanceof DODDigest) {
            propertiesItem.setEnabled(true);
        }
        if (element.equals(POLICIES) || element instanceof DODDigest) {
            folderExportItem.setEnabled(true);
            folderExportXacmlItem.setEnabled(true);
            newFolderItem.setEnabled(true);
        }
        if (element.equals(POLICIES) || element.equals(COMPONENTS)) {
            textSearch.setEnabled(true);
            buttonSearch.setEnabled(true);
        }
        if (element instanceof DODDigest && refereshDeleteFolderAction((DODDigest)element)) {
            deleteFolderItem.setEnabled(true);
        }
    }

    private boolean canRename(Object element) {
        if (element instanceof String) {
            return false;
        } else if (element instanceof DODDigest) {
            DODDigest descriptor = (DODDigest) element;
            String name = descriptor.getName() + PQLParser.SEPARATOR;
            for (DODDigest folder : PolicyManagerView.ALL_FOLDER_LIST) {
                if (folder.getName().startsWith(name)) {
                    return false;
                }
            }
            for (DODDigest policy : PolicyManagerView.ALL_POLICY_LIST) {
                if (policy.getName().startsWith(name)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void updateNavigationTreeSelection() {
        updateNavigationTreeToolBarStatus();

        IStructuredSelection selection = (IStructuredSelection) navigationTreeViewer
                .getSelection();
        if (selection.isEmpty()) {
            return;
        }

        Object element = selection.getFirstElement();
        componentTreeViewer.setInput(element);
        selection = (IStructuredSelection) componentTreeViewer.getSelection();
        DODDigest entity = null;
        if(!selection.isEmpty()){
        	entity = (DODDigest) selection.getFirstElement();
        }else{
        	GlobalState.getInstance().setCurrentlySelection(Arrays.asList(new DomainObjectDescriptor[]{}));
        }
        if (currentComponentSelection != null
                && currentComponentSelection.equals(entity)) {
            return;
        }

        updateComponentTreeToolBarStatus();
        updateDefinitionTab();
        updateAdditionalTab();
        updatePreviewTab();

        // pack the column
        for (TreeColumn column : componentTreeViewer.getTree().getColumns()) {
            column.pack();
        }

        // unselect the folder
        Tree tree = navigationTreeViewer.getTree();
        highlightFolder(tree.getItems(), null);

        currentItem = (TreeItem) navigationTreeViewer.getTree().getSelection()[0];
    }

    private void highlightFolder(TreeItem[] items, DODDigest descriptor) {
        for (TreeItem item : items) {
            Object data = item.getData();
            if (data != null && data.equals(descriptor)) {
                item.setFont(FontBundle.ARIAL_9_UNDERLINE);
            } else {
                item.setFont(null);
            }
            TreeItem[] subitems = item.getItems();
            highlightFolder(subitems, descriptor);
        }
    }

    private void getComponentForType(List<DODDigest> elements, String type) {
        String search = getSearchText();
        for (DODDigest digest : PolicyManagerView.ALL_COMPONENT_LIST) {
            String componentType = PolicyServerHelper.getTypeFromComponentName(digest.getName());
            if (componentType.equals(type)
                    && getDisplayName(digest).indexOf(search) != -1
                    && hasCorrectStatus(digest)) {
                elements.add(digest);
            }
        }
    }

    private String getSearchText() {
        String search = textSearch.getText();
        if (search.equals(ApplicationMessages.ABSTRACTTAB_ENTER_TERM)) {
            search = "";
        }
        return search;
    }

    private void updateComponentTreeSelection() {
        IStructuredSelection selection = (IStructuredSelection) componentTreeViewer.getSelection();
        List<DomainObjectDescriptor> selectionsList = new ArrayList <DomainObjectDescriptor>();
        if (selection.isEmpty()) {
            return;
        }
        for(Object object: selection.toArray()){
            DomainObjectDescriptor element = null;
            if (object instanceof DODDigest) {
                DODDigest digest = (DODDigest) object;
                try {
                	element = PolicyServerProxy.getDescriptorById(digest.getId());
                } catch (PolicyEditorException e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                element = (DomainObjectDescriptor) object;
            }
            selectionsList.add(element);
        }
		GlobalState.getInstance().setCurrentlySelection(selectionsList);
		
        updateComponentTreeToolBarStatus();
        updateDefinitionTab();
        updateAdditionalTab();
        updatePreviewTab();      
        
        if (selectionsList.size()==1){
        	String policy = selectionsList.get(0).getName();
            for (DODDigest descriptor : PolicyManagerView.ALL_FOLDER_LIST) {
                String folder = descriptor.getName();
                if (policy.startsWith(folder)) {
                    String name = policy.substring(folder.length() + 1);
                    if (name.indexOf(PQLParser.SEPARATOR) == -1) {
                        navigationTreeViewer.expandToLevel(descriptor, 1);
                        Tree tree = navigationTreeViewer.getTree();
                        TreeItem[] items = tree.getItems();
                        highlightFolder(items, descriptor);
                        break;
                    }
                }
            }
        }
        
        // adjust the column
        for (TreeColumn column : componentTreeViewer.getTree().getColumns()) {
            column.pack();
        }
    }

    @SuppressWarnings("unchecked")
    private void updateComponentTreeToolBarStatus() {
        generatePdfItem.setEnabled(false);
        policyExportItem.setEnabled(false);
        policyExportXacmlItem.setEnabled(false);
        boolean result = true;
        IHasId hasId = null;
        		
        IStructuredSelection selection = (IStructuredSelection) componentTreeViewer
                .getSelection();
        if (!selection.isEmpty()) {
            Iterator<DODDigest> iterator = selection.iterator();
            printComponentList.clear();
            while (iterator.hasNext()) {
                DODDigest digest = iterator.next();
                try {
					hasId= (IHasId) PolicyServerProxy.getEntityForDescriptor(PolicyServerProxy.getDescriptorById(digest.getId()));
				} catch (PolicyEditorException e) {
					e.printStackTrace();
				}
				if (digest.getType().equals("POLICY")){
					IPolicy policy = (IPolicy) hasId;
					if(PolicyHelpers.isSubPolicy(policy)){
						result = false;
					}
				}else{
					result = false;
				}
                printComponentList.add(digest);
            }
            if (!printComponentList.isEmpty()) {
                generatePdfItem.setEnabled(true);
            }
            if (result) {
                policyExportItem.setEnabled(true);
                policyExportXacmlItem.setEnabled(true);
            }
        }

        updateAdditionalComponentTreeToolBarStatus();
    }

    public void updateAdditionalComponentTreeToolBarStatus() {
    }

    @SuppressWarnings("unchecked")
    private void updatePreviewTab() {
        Composite parent = new Composite(detailTabFolder, SWT.NONE);
        GridData data = new GridData(GridData.FILL_BOTH);
        parent.setLayoutData(data);
        StackLayout rootLayout = new StackLayout();
        parent.setLayout(rootLayout);

        Composite noPreview = new Composite(parent, SWT.NONE);
        noPreview.setLayout(new FillLayout(SWT.VERTICAL));

        new Label(noPreview, SWT.NONE);
        Label noPreviewLabel = new Label(noPreview, SWT.CENTER);
        noPreviewLabel.setFont(FontBundle.ARIAL_16_NORMAL);
        noPreviewLabel.setText(ApplicationMessages.PREVIEWVIEW_NO_PREVIEW);
        new Label(noPreview, SWT.NONE);

        IStructuredSelection selection = (IStructuredSelection) componentTreeViewer
                .getSelection();
        if (selection.isEmpty()) {
            rootLayout.topControl = noPreview;
        } else {
            TreeItem[] treeItems = componentTreeViewer.getTree().getSelection();
            TreeItem item = treeItems[0];
            DODDigest digest = (DODDigest) item.getData();
            DomainObjectDescriptor descriptor;
            try {
                descriptor = PolicyServerProxy.getDescriptorById(digest.getId());
            } catch (PolicyEditorException e) {
                e.printStackTrace();
                return;
            }
            IHasId hasId = (IHasId) PolicyServerProxy
                    .getEntityForDescriptor(descriptor);
            EntityType type = descriptor.getType();
            if (type == EntityType.POLICY || type == EntityType.FOLDER) {
                rootLayout.topControl = noPreview;
            } else if (type.equals(EntityType.COMPONENT)) {
                String componentType = PolicyServerHelper
                        .getTypeFromComponentName(descriptor.getName());
                IPreviewPanel panel = null;
                Class panelClass = PreviewPanelFactory
                        .getPreviewPanelClass(componentType);
                if (panelClass != null) {
                    Constructor constructor[] = panelClass.getConstructors();
                    try {
                        panel = (IPreviewPanel) constructor[0].newInstance();
                        panel.createControls(parent);
                        panel.setHasId(hasId);
                        rootLayout.topControl = panel.getRootControl();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        rootLayout.topControl = noPreview;
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                        rootLayout.topControl = noPreview;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        rootLayout.topControl = noPreview;
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                        rootLayout.topControl = noPreview;
                    }
                } else {
                    rootLayout.topControl = noPreview;
                }
            } else {
                rootLayout.topControl = noPreview;
            }
        }
        previewTabItem.setControl(parent);
    }

    private void updateDefinitionTab() {
        IStructuredSelection selection = (IStructuredSelection) componentTreeViewer
                .getSelection();
        if (selection.isEmpty()) {
            definitionTabItem.setControl(null);
            return;
        }

        TreeItem[] treeItems = componentTreeViewer.getTree().getSelection();
        TreeItem item = treeItems[0];
        TreeItem rootItem = item;
        while (rootItem.getParentItem() != null) {
            rootItem = rootItem.getParentItem();
        }

        Composite parent = new Composite(detailTabFolder, SWT.NONE);
        FormLayout layout = new FormLayout();
        parent.setLayout(layout);

        DODDigest itemDigest = (DODDigest) item.getData();
        DODDigest rootDigest = (DODDigest) rootItem.getData();
        DomainObjectDescriptor itemDescriptor;
        DomainObjectDescriptor rootDescriptor;
        try {
            itemDescriptor = PolicyServerProxy.getDescriptorById(itemDigest.getId());
            rootDescriptor = PolicyServerProxy.getDescriptorById(rootDigest.getId());
        } catch (PolicyEditorException e) {
            e.printStackTrace();
            return;
        }

        if (rootItem.equals(item)) {
            IHasId hasId = (IHasId) PolicyServerProxy.getEntityForDescriptor(rootDescriptor);

            ScrolledComposite scrolledComposite = new ScrolledComposite(parent,
                    SWT.V_SCROLL | SWT.H_SCROLL);
            Composite detail = ReadOnlyPanelFactory.getEditorPanel(
                    scrolledComposite, SWT.NONE, hasId);
            FormData data = new FormData();
            data.top = new FormAttachment(0, 0);
            data.bottom = new FormAttachment(100, 0);
            data.left = new FormAttachment(0, 0);
            data.right = new FormAttachment(100, 0);
            scrolledComposite.setLayoutData(data);

            scrolledComposite.setExpandHorizontal(true);
            scrolledComposite.setExpandVertical(true);
            scrolledComposite.setMinSize(detail.computeSize(SWT.DEFAULT,
                    SWT.DEFAULT));
            scrolledComposite.setContent(detail);
        } else {
            IHasId itemId = (IHasId) PolicyServerProxy.getEntityForDescriptor(itemDescriptor);
            IHasId rootId = (IHasId) PolicyServerProxy.getEntityForDescriptor(rootDescriptor);

            ScrolledComposite rootScroll = new ScrolledComposite(parent,
                    SWT.V_SCROLL | SWT.H_SCROLL);
            Composite root = ReadOnlyPanelFactory.getEditorPanel(rootScroll,
                    SWT.NONE, rootId);
            FormData data = new FormData();
            data.top = new FormAttachment(0, 0);
            data.bottom = new FormAttachment(100, 0);
            data.left = new FormAttachment(0, 0);
            data.right = new FormAttachment(50, 0);
            rootScroll.setLayoutData(data);

            rootScroll.setExpandHorizontal(true);
            rootScroll.setExpandVertical(true);
            rootScroll.setMinSize(root.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            rootScroll.setContent(root);

            ScrolledComposite childScroll = new ScrolledComposite(parent,
                    SWT.V_SCROLL | SWT.H_SCROLL);
            Composite child = ReadOnlyPanelFactory.getEditorPanel(childScroll,
                    SWT.NONE, itemId);
            data = new FormData();
            data.top = new FormAttachment(0, 15);
            data.bottom = new FormAttachment(100, 0);
            data.left = new FormAttachment(50, 15);
            data.right = new FormAttachment(100, -15);
            childScroll.setLayoutData(data);

            childScroll.setExpandHorizontal(true);
            childScroll.setExpandVertical(true);
            childScroll.setMinSize(child.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            childScroll.setContent(child);
        }

        definitionTabItem.setControl(parent);
    }

    public void refreshDataAsync() {
        disposeEdit();
        componentTreeViewer.refresh();
        updateComponentTreeToolBarStatus();
        updateDefinitionTab();
        updateAdditionalTab();
        updatePreviewTab();
        componentTreeViewer.refresh();
    }

    public void createFolder() {
        disposeEdit();
        if (!newFolderItem.isEnabled()) {
            return;
        }

        IStructuredSelection selection = (IStructuredSelection) navigationTreeViewer
                .getSelection();
        if (selection.isEmpty()) {
            return;
        }
        String name = "New Folder";
        String prefix = "";
        Object element = selection.getFirstElement();
        if (element instanceof DODDigest) {
            DODDigest descriptor = (DODDigest) element;
            prefix = descriptor.getName() + PQLParser.SEPARATOR;
        }
        name = prefix + name;

        int index = 2;
        String suggest = name;
        boolean notfound;
        do {
            notfound = true;
            for (DODDigest descriptor : PolicyManagerView.ALL_FOLDER_LIST) {
                if (descriptor.getName().equals(suggest)) {
                    suggest = name + " (" + index + ")";
                    index++;
                    notfound = false;
                    break;
                }
            }
        } while (!notfound);

        Collection<DODDigest> list = PolicyServerProxy.createBlankPolicyFolder(suggest);
        PolicyManagerView.ALL_FOLDER_LIST.addAll(list);
        for (DODDigest descriptor : PolicyManagerView.ALL_FOLDER_LIST) {
            PolicyManagerView.extractEntityInfo(descriptor);
        }
        navigationTreeViewer.refresh();

        // expand to that newly-created item
        for (DODDigest descriptor : PolicyManagerView.ALL_FOLDER_LIST) {
            if (descriptor.getName().equals(suggest)) {
                navigationTreeViewer.expandToLevel(descriptor, 1);

                // set selection to that item
                navigationTreeViewer.setSelection(new StructuredSelection(
                        descriptor), true);
                break;
            }
        }

        EntityInfoProvider.updatePolicyTreeAsync();
        updateNavigationTreeToolBarStatus();

        // entering the edit mode
        TreeItem[] selections = navigationTreeViewer.getTree().getSelection();
        currentItem = selections[0];
        lastItem = currentItem;
        enterEditingMode();
    }

    public void deleteFolder() {
        disposeEdit();
        if (!deleteFolderItem.isEnabled()) {
            return;
        }

        currentItem = null;
        lastItem = null;

        IStructuredSelection selection = (IStructuredSelection) navigationTreeViewer
                .getSelection();
        if (selection.isEmpty()) {
            return;
        }

        Object element = selection.getFirstElement();
        if (element instanceof String) {
            return;
        }

        if (element instanceof DODDigest) {
            DODDigest digest = (DODDigest) element;
            DomainObjectDescriptor descriptor;
            try {
                descriptor = PolicyServerProxy.getDescriptorById(digest.getId());
            } catch (PolicyEditorException e) {
                e.printStackTrace();
                return;
            }
            IHasId hasId = (IHasId) PolicyServerProxy.getEntityForDescriptor(descriptor);
            DomainObjectHelper.setStatus(hasId, DevelopmentStatus.DELETED);
            PolicyServerProxy.saveEntity(hasId);
            EntityInfoProvider.updatePolicyTreeAsync();
            PolicyManagerView.ALL_FOLDER_LIST.remove(digest);
            PolicyManagerView.ENTITY_INFO_MAP.remove(digest);
            navigationTreeViewer.refresh();
            updateNavigationTreeToolBarStatus();
        }
    }

    public TreeViewer getNavigationTreeViewer() {
        return navigationTreeViewer;
    }

    public TreeViewer getComponentTreeViewer() {
        return componentTreeViewer;
    }

    public void setFolderProperties() {
        IStructuredSelection selection = (IStructuredSelection) navigationTreeViewer
                .getSelection();
        if (selection.isEmpty()) {
            return;
        }

        DODDigest digest = (DODDigest) selection.getFirstElement();
        DomainObjectDescriptor descriptor;
        try {
            descriptor = PolicyServerProxy.getDescriptorById(digest.getId());
        } catch (PolicyEditorException e) {
            e.printStackTrace();
            return;
        }
        IHasId domainObject = (IHasId) PolicyServerProxy.getEntityForDescriptor(descriptor);
        if (domainObject != null) {
            ObjectPropertiesDialog dlg = new ObjectPropertiesDialog(Display
                    .getCurrent().getActiveShell(), domainObject);
            if (dlg.open() == Window.OK) {
                PolicyManagerView.refreshCurrentTab();
            }
        }
    }

    public CTabFolder getDetailTabFolder() {
        return detailTabFolder;
    }

    public ISelection getSelection() {
        return selection;
    }

    public void setSelection(ISelection selection) {
        this.selection = selection;
    }

    private void setDuplicateAction() {
        if (componentTreeViewer.getTree().getSelection().length == 1) {
            IStructuredSelection selection = (IStructuredSelection) componentTreeViewer.getSelection();
            Object object = selection.getFirstElement();
            DODDigest digest = (DODDigest) object;
            List<DODDigest> result = new ArrayList<DODDigest>();
            String type = digest.getType();
            if (type.equals("POLICY")) {
                String policyName = digest.getName();
                for (DODDigest desc : EntityInfoProvider.getPolicyList()) {
                    String fullName = desc.getName();
                    if (fullName.startsWith(policyName) && desc.isAccessible()) {
                        String subName = fullName.substring(policyName.length());
                        if (subName.length() == 0
                            || subName.startsWith(PQLParser.SEPARATOR + "")) {
                            result.add(desc);
                        }
                    }
                }
            } else if (type.equals("FOLDER")) {
                String folderName = digest.getName();
                for (DODDigest desc : EntityInfoProvider.getPolicyList()) {
                    String fullName = desc.getName();
                    if (fullName.startsWith(folderName) && desc.isAccessible()) {
                        String subName = fullName.substring(folderName.length());
                        if (subName.length() == 0
                            || subName.startsWith(PQLParser.SEPARATOR + "")) {
                            result.add(desc);
                        }
                    }
                }
            }else{
            	result.add(digest);
            }
            List<DomainObjectDescriptor> descriptors = null;
            try {
                descriptors = PolicyServerProxy.getDescriptorsByDigests(result);
            } catch (PolicyEditorException e) {
                e.printStackTrace();
            }
            DuplicateAction.SOURCES.clear();
            DuplicateAction.SOURCES.addAll(descriptors);
            if (digest.getType().equals("POLICY")) {
                DuplicateAction.TYPE = EntityType.POLICY;
            } else if (digest.getType().equals("FOLDER")) {
                DuplicateAction.TYPE = EntityType.FOLDER;
            } else{
            	DuplicateAction.TYPE = descriptors.get(0).getType();
            }
            DuplicateAction.VIEW = "manager";
            duplicateAction.setEnabled(true);
        } else {
            duplicateAction.setEnabled(false);
        }
    }
}
