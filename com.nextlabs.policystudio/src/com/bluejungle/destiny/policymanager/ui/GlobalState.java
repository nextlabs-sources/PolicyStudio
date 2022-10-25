/*
 * Created on Mar 2, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bluejungle.destiny.policymanager.action.PolicyStudioActionFactory;
import com.bluejungle.destiny.policymanager.editor.DomainObjectEditor;
import com.bluejungle.destiny.policymanager.editor.DomainObjectInput;
import com.bluejungle.destiny.policymanager.editor.EditorPanelFactory;
import com.bluejungle.destiny.policymanager.editor.IEditorPanel;
import com.bluejungle.destiny.policymanager.event.CurrentPolicyOrComponentChangedEvent;
import com.bluejungle.destiny.policymanager.event.IEvent;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.SelectionChangedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel;
import com.bluejungle.destiny.policymanager.ui.usergroup.PolicyListPanel;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates
 *          .xml#2 $:
 */

public class GlobalState implements IPartListener {

    private static GlobalState _instance = new GlobalState();

    public static String user, server;

    public static GlobalState getInstance() {
        return GlobalState._instance;
    }

    public Map<Long, UndoInfo> undoInfoMap = new HashMap<Long, UndoInfo>();

    private ViewPart view = null;
    private String viewID = null;
    // private IEditorPanel editorPanel = null;

    private Map<String, ComponentListPanel> componentListPanels = new HashMap<String, ComponentListPanel>();
    private PolicyListPanel policyPanel = null;
    private DomainObjectEditor activeEditor;
    private Map<Long, IHasId> openedEntities;
    private List<IPartObserver> partObservers;
    private Set<DomainObjectDescriptor> currentSelection = new HashSet<DomainObjectDescriptor>();
    private IEventManager eventManager;

    public enum SaveCause {
        PART_DEACTIVATED, USER_REQUEST, DISPOSE, UNKNOWN,
    }

    /**
     * Constructor
     * 
     */
    public GlobalState() {
        openedEntities = new HashMap<Long, IHasId>();
        partObservers = new ArrayList<IPartObserver>();

        eventManager = ComponentManagerFactory.getComponentManager()
                       .getComponent(EventManagerImpl.COMPONENT_INFO);
    }

    public void willBecomeVisible() {
        IWorkbench w = PlatformUI.getWorkbench();
        IWorkbenchWindow currentWindow = w.getActiveWorkbenchWindow();
        IWorkbenchPage currentPage = currentWindow.getActivePage();
        currentPage.addPartListener(this);

        // showAboutPage();

        for (IPartObserver observer : partObservers) {
            observer.workbenchInitialized();
        }
    }

    /**
     * @param undoElement
     */
    public void addUndoElement(IUndoElement undoElement) {
        IHasId currentObject = (IHasId) getCurrentObject();
        if (currentObject != null) {
            UndoInfo undoInfo = undoInfoMap.get(currentObject.getId());
            if (undoInfo == null) {
                undoInfo = new UndoInfo();
                undoInfoMap.put(currentObject.getId(), undoInfo);
            }
            undoInfo.addUndoElement(undoElement);
        }
    }

    public ViewPart getView() {
        return view;
    }

    public void setView(ViewPart view) {
        this.view = view;
    }

    public String getViewID() {
        return viewID;
    }
    public void setViewID(String viewID) {
        this.viewID = viewID;
    }
    public IEditorPanel getEditorPanel() {
        if (activeEditor != null) {
            return activeEditor.getEditorPanel();
        } else {
            return null;
        }
    }

    public void addComponentListPanel(ComponentListPanel panel) {
        componentListPanels.put(panel.getComponentType(), panel);
    }

    public ComponentListPanel getComponentListPanel(String componentType) {
        return componentListPanels.get(componentType);
    }

    public void setActiveEditor(DomainObjectEditor anEditor) {
        if (activeEditor != anEditor) {
            activeEditor = anEditor;

            IEvent currentObjectChangedEvent = null;
            if (activeEditor != null) {
                currentObjectChangedEvent = new CurrentPolicyOrComponentChangedEvent(
                    activeEditor.getDomainObject());
            } else {
                currentObjectChangedEvent = new CurrentPolicyOrComponentChangedEvent();
            }

            eventManager.fireEvent(currentObjectChangedEvent);
        }
    }

    public DomainObjectEditor getActiveEditor() {
        return activeEditor;
    }

    public IHasId getCurrentObject() {
        return (activeEditor != null) ? activeEditor.getDomainObject() : null;
    }

    /**
     * Return the set of currently selected items
     * 
     * @return a Set instance containing the DomainObjectDescriptor instance
     *         representing the currently selected items in the left hand side
     *         navigation
     */
    public Set<DomainObjectDescriptor> getCurrentSelection() {
        return currentSelection;
    }

    public void setCurrentlySelection(
        Collection<DomainObjectDescriptor> currentSelectionToSet) {		
        if (currentSelectionToSet == null) {
            throw new NullPointerException(
                "currentSelectionToSet cannot be null.");
        }
        currentSelection.clear();
        currentSelection.addAll(currentSelectionToSet);

        IEvent selectionChangedEvent = new SelectionChangedEvent(currentSelection);
        eventManager.fireEvent(selectionChangedEvent);
    }

    public PolicyListPanel getPolicyListPanel() {
        return policyPanel;
    }

    public void setPolicyListPanel(PolicyListPanel panel) {
        policyPanel = panel;
    }

    /**
     * Save contents of current editor
     */
    public void saveEditorPanel() {
        // Scott's fix for locking - begin
        // if (editorPanel != null) {
        // editorPanel.saveContents();
        // }
        // IEditorPanel ep = getEditorPanel();
        // if (ep != null) {
        // ep.saveContents();
        // }
        DomainObjectEditor activeEditor = getActiveEditor();
        if (activeEditor != null) {
            activeEditor.saveEditor(SaveCause.USER_REQUEST);
        }
        // Scott's fix for locking - end
    }

    // public void saveAndRefreshEditorPanel() {
    // IEditorPanel oldPanel = getEditorPanel();
    // if (oldPanel != null) {
    // IHasId domainObject = oldPanel.getDomainObject();
    // DevelopmentStatus status = DomainObjectHelper.getStatus(domainObject);
    // // only save and replace objects in appropriate state
    // if (status == DevelopmentStatus.NEW || status == DevelopmentStatus.EMPTY
    // || status == DevelopmentStatus.DRAFT) {
    // Collection c = PolicyServerProxy.saveEntity(domainObject);
    // DomainObjectDescriptor desc = (DomainObjectDescriptor)
    // c.iterator().next();
    // loadObjectInEditorPanel(desc);
    // }
    // }
    // }

    public void forceLoadObjectInEditorPanel(DomainObjectDescriptor descriptor) {
        IHasId domainObject = (IHasId) PolicyServerProxy
                              .getEntityForDescriptor(descriptor);
        loadObjectInEditorPanel(domainObject, true);
    }

    public void loadObjectInEditorPanel(DomainObjectDescriptor descriptor) {
        IHasId domainObject = (IHasId) PolicyServerProxy
                              .getEntityForDescriptor(descriptor);
        loadObjectInEditorPanel(domainObject);
    }

    public void loadObjectInEditorPanel(IHasId domainObject) {
        loadObjectInEditorPanel(domainObject, false);
    }

    public void forceLoadObjectInEditorPanel(IHasId domainObject) {
        loadObjectInEditorPanel(domainObject, true);
    }

    public void loadObjectInEditorPanel(IHasId domainObject,
                                        boolean forceNewEditor) {
        if (domainObject == null
            || DomainObjectHelper.getStatus(domainObject) == DevelopmentStatus.DELETED) {
            // special case: if domainObject has been deleted, don't load it
            return;
        }

        /*
         * If we don't have any editor for the object, bail.
         */
        if (!EditorPanelFactory.hasEditorPanelFor(domainObject)) {
            return;
        }

        IWorkbench w = PlatformUI.getWorkbench();
        IWorkbenchWindow currentWindow = w.getActiveWorkbenchWindow();
        IWorkbenchPage currentPage = currentWindow.getActivePage();

        DomainObjectInput doi = new DomainObjectInput(domainObject);

        if (forceNewEditor) {
            IEditorPart current = currentPage.findEditor(doi);
            if (current != null) {
                if (currentPage.getActivePart() instanceof DomainObjectEditor) {
                    DomainObjectEditor doe = (DomainObjectEditor) currentPage
                                             .getActivePart();
                    doe.stopAutoSaves();
                }
                currentPage.closeEditor(current, false);
                current.dispose();
            }
        }
        Boolean inPolicyAuthorView = PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION.isEnabled();
        if(inPolicyAuthorView){
            try {
                currentPage
                    .openEditor(
                        doi,
                        "com.bluejungle.destiny.policymanager.editor.DomainObjectEditor",
                        true);
            } catch (PartInitException pie) {
                System.err.println("" + pie);
            }
        }
    }

    public void closeEditorFor(IHasId aDomainObject) {
        IWorkbench w = PlatformUI.getWorkbench();
        IWorkbenchWindow currentWindow = w.getActiveWorkbenchWindow();
        IWorkbenchPage currentPage = currentWindow.getActivePage();

        DomainObjectInput doi = new DomainObjectInput(aDomainObject);
        IEditorPart ep = currentPage.findEditor(doi);
        if (ep != null) {
            currentPage.closeEditor(ep, false);
        }
    }

    /*
     * Part Listener
     */
    private void setActiveEditorFromPart(IWorkbenchPart part) {
        if (part instanceof DomainObjectEditor) {
            DomainObjectEditor doe = (DomainObjectEditor) part;
            setActiveEditor(doe);
        }
    }

    public void partActivated(IWorkbenchPart part) {
        setActiveEditorFromPart(part);
    }

    public void partBroughtToTop(IWorkbenchPart part) {
        setActiveEditorFromPart(part);
    }

    public void partClosed(IWorkbenchPart part) {
        if (part instanceof DomainObjectEditor) {
            DomainObjectEditor doe = (DomainObjectEditor) part;
            if (getActiveEditor() == doe) {
                setActiveEditor(null);
            }
            openedEntities.remove(doe.getDomainObject().getId());
        }

        for (IPartObserver observer : partObservers) {
            observer.partClosed(part);
        }
    }

    public void partDeactivated(IWorkbenchPart part) {
        if (part instanceof DomainObjectEditor) {
            DomainObjectEditor doe = (DomainObjectEditor) part;
            // Scott's fix for locking - begin
            // doe.autoSave();
            doe.saveEditor(SaveCause.PART_DEACTIVATED);
            // Scott's fix for locking - end
        }
    }

    public void partOpened(IWorkbenchPart part) {
        setActiveEditorFromPart(part);
        if (part instanceof DomainObjectEditor) {
            DomainObjectEditor doe = (DomainObjectEditor) part;
            IHasId dObj = doe.getDomainObject();
            if (!openedEntities.containsKey(dObj.getId())) {
                openedEntities.put(dObj.getId(), dObj);
            }
        }

        for (IPartObserver observer : partObservers) {
            observer.partOpened(part);
        }
    }

    public IHasId getOpenedEntityForEntity(Object anEntity) {
        if (anEntity instanceof IHasId) {
            IHasId o = (IHasId) anEntity;
            IHasId eo = (IHasId) openedEntities.get(o.getId());
            if (eo != null) {
                return eo;
            }
        }
        return null;
    }

    public IHasId getOpenedEntityForID(Long id) {
        return (IHasId) openedEntities.get(id);
    }

    public void setOpenedEntity(IHasId domainObject) {
        openedEntities.put(domainObject.getId(), domainObject);
    }

    public void addPartObserver(GlobalState.IPartObserver o) {
        partObservers.add(o);
    }

    public void removePartObserver(GlobalState.IPartObserver o) {
        partObservers.remove(o);
    }

    public static interface IPartObserver {

        public void workbenchInitialized();

        public void partOpened(IWorkbenchPart aPart);

        public void partClosed(IWorkbenchPart aPart);
    }
}
