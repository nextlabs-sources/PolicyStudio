/*
 * Created on May 5, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.usergroup;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.action.DuplicateAction;
import com.bluejungle.destiny.policymanager.action.PolicyStudioActionFactory;
import com.bluejungle.destiny.policymanager.editor.IEditorPanel;
import com.bluejungle.destiny.policymanager.event.EventType;
import com.bluejungle.destiny.policymanager.event.IEvent;
import com.bluejungle.destiny.policymanager.event.IEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.model.CreationExtension;
import com.bluejungle.destiny.policymanager.model.EntityInformation;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoListener;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.IClipboardEnabled;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PolicyTransfer;
import com.bluejungle.destiny.policymanager.ui.controls.FilterControl;
import com.bluejungle.destiny.policymanager.ui.controls.FilterControlEvent;
import com.bluejungle.destiny.policymanager.ui.controls.FilterControlListener;
import com.bluejungle.destiny.policymanager.ui.dialogs.NewPolicyDialog;
import com.bluejungle.destiny.policymanager.ui.dialogs.TitleAreaDialogEx;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.exceptions.PolicyReference;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.destiny.policy.PolicyFolder;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

/**
 * @author bmeng
 * 
 */
public class PolicyListPanel extends Composite implements IClipboardEnabled {

    private static final boolean INCLUDE_TEXT_DRAG_SUPPORT = true;
    private static final List<EntityType> POLICIES_AND_FOLDERS = Arrays
        .asList(new EntityType[] { EntityType.POLICY, EntityType.FOLDER });
    private static final DomainObjectFormatter formatter = new DomainObjectFormatter();

    protected boolean ignoreSelection = false;
    protected boolean restoreSelection = true;

    private FilterControl filterControl;
    private TreeViewer treeViewer;
    private TableViewer filteredViewer;
    private Composite buttonRow;
    private Button addPolicyButton;
    private Button addFolderButton;
    private Object firstItem ;

    // private List<DomainObjectDescriptor> objectList = new
    // ArrayList<DomainObjectDescriptor>();
    private List<DODDigest> policyDigestList = new ArrayList<DODDigest>();
    private List<DomainObjectDescriptor> searchResults_byName = new ArrayList<DomainObjectDescriptor>();
    private List<DomainObjectDescriptor> searchResults_byComponent = new ArrayList<DomainObjectDescriptor>();
    private List<DomainObjectDescriptor> searchResults_bySingleComponent = new ArrayList<DomainObjectDescriptor>();
	private boolean policiesAreAllowed = PolicyServerProxy.getAllowedEntityTypes().contains(EntityType.POLICY);

    private boolean filtering = false;
    private boolean showingUsage = false;

    private List<Item> selectedItems = new ArrayList<Item>();
    private FilteredContentProvider filteredContentProvider;
    private boolean ignoreCurrentObjectChangedEvent = false;

    private DragSourceListener dragSourceListener = new DragSourceAdapter() {
        @Override
            @SuppressWarnings("unchecked")
            public void dragStart(DragSourceEvent e) {
            DragSource source = (DragSource) e.getSource();
            if (source.getControl() instanceof Tree) {
                for (Iterator iter = ((IStructuredSelection) treeViewer
                                      .getSelection()).iterator(); e.doit && iter.hasNext();) {
                    Object tmp = iter.next();
                    if (!(tmp instanceof DODDigest)) {
                        continue;
                    }
                    e.doit &= ((DODDigest) tmp).isAccessible();
                }

            } else {
                e.doit = false;
            }
        }

        @Override
            public void dragFinished(DragSourceEvent e) {
            getDisplay().readAndDispatch();

            DragSource source = (DragSource) e.getSource();
            if (source.getControl() instanceof Tree) {
                restoreTreeSelection();
            } else {
                restoreTableSelection();
            }
            ignoreSelection = false;
        }

        @Override
            @SuppressWarnings("unchecked")
            public void dragSetData(DragSourceEvent e) {
            DragSource source = (DragSource) e.getSource();
            if (source.getControl() instanceof Tree) {
                PolicyTransfer policyTransfer = PolicyTransfer.getInstance();
                if (policyTransfer.isSupportedType(e.dataType)) {
                    IStructuredSelection selection = null;
                    selection = (IStructuredSelection) treeViewer
                                .getSelection();
                    DODDigest[] descriptors = new DODDigest[selection.size()];
                    int i = 0;
                    for (Iterator iter = selection.iterator(); iter.hasNext();) {
                        Object tmp = iter.next();
                        if (!(tmp instanceof DODDigest)) {
                            continue;
                        }
                        descriptors[i++] = (DODDigest) tmp;
                    }
                    e.data = descriptors;
                    return;
                }

                TextTransfer transfer = TextTransfer.getInstance();
                if (transfer.isSupportedType(e.dataType)) {
                    String pql = getPQLForSelection();
                    e.data = pql;
                }
            }
        }
    };

    // private IDoubleClickListener doubleClickListener = new
    // IDoubleClickListener() {
    //
    // public void doubleClick(DoubleClickEvent event) {
    // IStructuredSelection selection;
    // if (event.getSource() instanceof TreeViewer) {
    // selection = (IStructuredSelection) treeViewer.getSelection();
    // } else {
    // selection = (IStructuredSelection) filteredViewer
    // .getSelection();
    // }
    // Object selected = selection.getFirstElement();
    // if (selected instanceof DomainObjectDescriptor) {
    // DomainObjectDescriptor descriptor = (DomainObjectDescriptor) selected;
    // loadPolicyInEditPanel(descriptor);
    // }
    // }
    // };

    private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {

        public void selectionChanged(SelectionChangedEvent event) {
            if (!ignoreSelection) {
                if (event.getSource() instanceof TreeViewer) {
                    setupNewTreeSelection();
                } else {
                    setupNewTableSelection();
                }
            }
        }
    };

    private class MouseAndTreeListener implements MouseListener, TreeListener {

        private Item clickedItem = null;
        private boolean treeChanged = false;

        public void mouseDoubleClick(MouseEvent e) {
        }

        public void mouseDown(MouseEvent e) {
            ignoreSelection = true;
            if (e.getSource() instanceof Tree) {
                clickedItem = treeViewer.getTree().getItem(new Point(e.x, e.y));
            } else {
                clickedItem = filteredViewer.getTable().getItem(
                    new Point(e.x, e.y));
            }

            // handle duplicate action status
            if (e.getSource() instanceof Tree) {
                TreeItem[] selections = treeViewer.getTree().getSelection();

                DuplicateAction action = (DuplicateAction) PolicyStudioActionFactory
                                         .getDuplicateAction();
                if (selections.length != 1) {
                    action.setEnabled(false);
//                    return;
                }else{
                    action.setEnabled(true);
                }
                prepareSelectionForDuplicate();
            }
        }

        public void mouseUp(MouseEvent e) {
            if (e.getSource() instanceof Tree) {
                TreeItem[] selections = treeViewer.getTree().getSelection();

                DuplicateAction action = (DuplicateAction) PolicyStudioActionFactory
                                         .getDuplicateAction();
                if (selections.length != 1) {
                    action.setEnabled(false);
//                    return;
                }else{
                    action.setEnabled(true);
                }
            }

            Item item = null;
            boolean treeEvent = false;
            if (e.getSource() instanceof Tree) {
                treeEvent = true;
                item = treeViewer.getTree().getItem(new Point(e.x, e.y));
            } else {
                item = filteredViewer.getTable().getItem(new Point(e.x, e.y));
            }
            if (clickedItem != null || treeChanged) {
                if (item == clickedItem) {
                    boolean enabled = true;
                    if (item != null) {
                        Object data = item.getData();
                        if (data instanceof DODDigest) {
                            DODDigest dod = (DODDigest) data;
                            enabled = dod.isAccessible();
                        }
                    }
                    if (enabled) {
                        if (treeEvent) {
                            setupNewTreeSelection();
                            if ((e.button == 3)
                                && (shouldShowContextMenu((TreeItem) item))) {
                                showContextMenu(e);
                            }
                        } else {
                            setupNewTableSelection();
                            if ((e.button == 3)
                                && (shouldShowContextMenu((TableItem) item))) {
                                showContextMenu(e);
                            }
                        }
                    } else {
                        if (treeEvent) {
                            restoreTreeSelection();
                        } else {
                            restoreTableSelection();
                        }
                    }
                } else {
                    if (treeEvent) {
                        restoreTreeSelection();
                    } else {
                        restoreTableSelection();
                    }
                }
            } else {
                if (treeEvent) {
                    clearTreeSelection();
                } else {
                    clearTableSelection();
                }
            }
            ignoreSelection = false;
            treeChanged = false;
        }

        public void treeCollapsed(TreeEvent e) {
            changed(e);
            TreeItem item = (TreeItem) e.item;
            DODDigest descriptor = (DODDigest)item.getData();
            String type = descriptor.getType();
            if (type.equals("POLICY")){
            	item.setImage(ImageBundle.POLICY_IMG);
            }else{
            	item.setImage(ImageBundle.FOLDER_IMG);
            }
        }

        public void treeExpanded(TreeEvent e) {
            changed(e);
            TreeItem item = (TreeItem) e.item;
            DODDigest descriptor = (DODDigest)item.getData();
            String type = descriptor.getType();
            if (type.equals("POLICY")){
            	item.setImage(ImageBundle.POLICY_IMG);
            }else{
            	item.setImage(ImageBundle.FOLDER_OPEN_IMG);
            }
            
        }

        private void changed(TreeEvent e) {
            setTreeItemColors(((Tree) e.getSource()).getItems());
            treeChanged = true;
        }
    };

    private MouseAndTreeListener mouseAndTreeListener = new MouseAndTreeListener();

    private DropTargetListener dropTargetListener = new DropTargetAdapter() {
        /**
         * Enable dropping only if dragging over a folder or an empty space.
         */
        @Override
            public void dragOver(DropTargetEvent event) {
            event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_EXPAND
                             | DND.FEEDBACK_SCROLL;
            Tree tree = treeViewer.getTree();
            TreeItem item = tree.getItem(tree.toControl(new Point(event.x,
                                                                  event.y)));
            if (item != null) {
                DODDigest descriptor = (DODDigest) item.getData();
                if (descriptor.isAccessible()){
                    event.detail = DND.DROP_MOVE;
                } else {
                    event.detail = DND.DROP_NONE;
                }
            } else {
                // Top level
                event.detail = DND.DROP_MOVE;
            }
        }

        /**
         * Renames all dropped policies to move them to the dropped folder
         * 
         * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
         */
        @Override
            public void drop(DropTargetEvent event) {
            GlobalState gs = GlobalState.getInstance();
            gs.saveEditorPanel();

            DODDigest[] digests = (DODDigest[]) PolicyTransfer.getInstance()
                                  .nativeToJava(event.currentDataType);
            Set <DomainObjectDescriptor> allMoved = new HashSet<DomainObjectDescriptor>();
            String oldParentName = null;
            for (DODDigest digest : digests) {
                if (digest == null) {
                    return;
                }
                DomainObjectDescriptor descriptor;
                try {
                    descriptor = PolicyServerProxy.getDescriptorById(digest
                                                                     .getId());
                } catch (PolicyEditorException e) {
                    e.printStackTrace();
                    return;
                }
                allMoved.add(descriptor);
                String objectName = digest.getName();
                // Add the parent name to movedFolders
                int fsIndex = objectName.lastIndexOf(PQLParser.SEPARATOR);
                String parentFolderOfThisObject = objectName.substring(0,fsIndex + 1);
                if (oldParentName == null) {
                    oldParentName = parentFolderOfThisObject;
                } else if (!oldParentName.equals(parentFolderOfThisObject)) {
                    // All moved objects must be from the same folder
                    event.detail = DND.DROP_NONE;
                    return;
                }
                    allMoved.addAll(PolicyServerProxy.getEntityList(
                                        PolicyServerProxy.escape(digest.getName())
                                    + PQLParser.SEPARATOR + "%",POLICIES_AND_FOLDERS));
            }

            /**
             * <Validation Check> for Drag and Drop feature
             */
            
            // Do not restore selection since objects are being moved around.
            restoreSelection = false;
            IHasId currentHasId = null;
            IHasId sourceHasId = null;
            Long currentObjId = null;
            DODDigest currentDescriptor = null;
            DODDigest newParentDescriptor = null;

            String newParentName;
            Tree tree = treeViewer.getTree();
            TreeItem currentItem = tree.getSelection()[0];
            TreeItem newParentItem = tree.getItem(tree.toControl(new Point(event.x,event.y)));

            if(currentItem != null) {
            	 currentDescriptor = (DODDigest) currentItem.getData();
            	 if(currentDescriptor.getType().equals("POLICY")){
            		 currentHasId = (IHasId)PolicyServerProxy.getEntityForDescriptor(PolicyServerProxy.getDescriptorByName(currentDescriptor.getName()));
            		 currentObjId = (currentHasId != null) ? currentHasId.getId(): null;
            	 }
            }
                   
            if (newParentItem != null) {
                newParentDescriptor = (DODDigest) newParentItem.getData();
                
                // We cannot move folder under policy
                if (newParentDescriptor.getType().equals("POLICY")&& currentDescriptor.getType().equals("FOLDER")) {
                	showError("A folder cannot be moved under a Policy.");
                    event.detail = DND.DROP_NONE;
                    return;
                }
                // Different types of policies cannot be moved as a policy set
                if(newParentDescriptor.getType().equals("POLICY")){
                	Set<String> attributes = new HashSet<String>();
	       		   	sourceHasId= (IHasId) PolicyServerProxy
	       		       		.getEntityForDescriptor(PolicyServerProxy.getDescriptorByName(newParentDescriptor.getName()));
		       		 if(sourceHasId instanceof Policy){
		         		IPolicy oldPolicy = (IPolicy)currentHasId;
		     	        attributes.addAll(oldPolicy.getAttributes());
		         		IPolicy sourcePolicy = (IPolicy)sourceHasId; 
		         		attributes.addAll(sourcePolicy.getAttributes());
		     	        if(attributes.contains(IPolicy.EXCEPTION_ATTRIBUTE)){
		     	        	attributes.remove(IPolicy.EXCEPTION_ATTRIBUTE);
		     	        }
	
		        		if(attributes.size()>1){
		        			showError("Cannot move different types of policy together.");
		        			event.detail = DND.DROP_NONE;
							return;
		        		}
		       		 }
            	}
                /* There's only 2 situations are allowed for drag and drop for DAC policies.
                 * 1. CAP to folder
                 * 2. CAR to CAP
                 * We should disable the rest of the behavior for DAC policies.
                 */      
                if(currentDescriptor.getType().equals("POLICY") && PolicyHelpers.isDACPolicyType(currentHasId)){
                	if(PolicyHelpers.isSubPolicy(currentHasId)){
                		if(newParentDescriptor.getType().equals("POLICY")){
                			if (!(PolicyHelpers.isDACPolicyType(sourceHasId)&& !PolicyHelpers.isSubPolicy(sourceHasId))){
                				showError("A CAR can only be moved underneath a CAP");
	            					return;
                			}
	        			}else{
	        				showError("A CAR can only be moved underneath a CAP");
	        				return;
	        			}
	        		}else{
	        			if(!newParentDescriptor.getType().equals("FOLDER")){
	        				showError("A CAP can only be moved underneath a folder");
	        				return;
	        			}
	        		}
	        	}
                newParentName = newParentDescriptor.getName()+ PQLParser.SEPARATOR;
            } else {
                newParentName = "";
            }

            // Dropping to the original location is a no-op
            if (oldParentName.equalsIgnoreCase(newParentName)) {
            	showError("Policies/ Policy Folders cannot be moved to the same location.");
                event.detail = DND.DROP_NONE;
                return;
            }         
            List <IHasId> policiesAndFolders = new ArrayList <IHasId>(PolicyServerProxy.getEntitiesForDescriptor(allMoved));
            Collections.sort(policiesAndFolders,  PolicyHelpers.CASE_INSENSITIVE_COMPARATOR);

            Map<String, String> policyMap = new HashMap<String, String>();
            Map<String, String> folderMap = new HashMap<String, String>();
            int oldParentLength = oldParentName.length();
            
            for (IHasId next : policiesAndFolders) {
                if (next instanceof IDPolicy) {
                	gs.closeEditorFor(next);
                    IDPolicy p = (IDPolicy) next;
                    String oldName = p.getName();
                    String newName = newParentName+ oldName.substring(oldParentLength);
                    Collection<DomainObjectDescriptor> existings = PolicyServerProxy
                                                                   .getEntityList(PolicyServerProxy.escape(newName),
                                                                                  POLICIES_AND_FOLDERS);
                    // Same name's of policy is already existed under the new Parent
                    for (DomainObjectDescriptor dod : existings) {
                        if (dod.getName().equalsIgnoreCase(newName)
                            && dod.getType() == EntityType.POLICY) {
                            showError(newName.replace(PQLParser.SEPARATOR, '/') + " already exists.");
                            return;
                        } else {
                            continue;
                        }
                    }
                    // Policies cannot be moved outside if folders
                    if (newName.indexOf(PQLParser.SEPARATOR) == -1) {
                        showError("Policies may not be moved outside of folders.");
                        return;
                    }
                    // A policy may not be moved to underneath itself.
                    if (newParentName.startsWith(oldName + PQLParser.SEPARATOR)) {
						showError("A policy may not be moved to one of its descendant policies.");
						return;
					}
                } else if (next instanceof PolicyFolder) {
                    PolicyFolder f = (PolicyFolder) next;
                    String oldName = f.getName();
                    String newName = newParentName+ oldName.substring(oldParentLength);
                    // A policy folder may not be moved to one of its descendant folders.
                    if (newParentName.startsWith(oldName + PQLParser.SEPARATOR)) {
                        showError("A policy folder may not be moved to one of its descendant folders.");
                        return;
                    }
                    Collection<DomainObjectDescriptor> existings = PolicyServerProxy.getEntityList(newName, POLICIES_AND_FOLDERS);
                    if (existings != null && !existings.isEmpty()) {
                        DomainObjectDescriptor dod = (DomainObjectDescriptor) existings
                                                     .iterator().next();
                        // Same name's of folder is already existed under the new Parent folder
                        if (!dod.getName().equalsIgnoreCase(f.getName())
                            || dod.getType() != EntityType.FOLDER) {
                            showError(newName.replace(PQLParser.SEPARATOR, '/') + " already exists.");
                            return;
                        } else {
                            continue;
                        }
                    }
                } else {
                    throw new IllegalStateException(
                        "The list contains items other than Policies/Policy Folders");
                }
            }
		/**
		 * </Validation Check>
		 */
            for (IHasId next : policiesAndFolders) {
                if (next == null) {
                    continue;
                }
                if (next instanceof Policy) {
                    Policy p = (Policy) next;
                    String oldName = p.getName();
                    String newName = newParentName + oldName.substring(oldParentLength);
                    policyMap.put(newName, oldName);
                    p.setName(newName);
                    p = changeAttribute(newName, p);
                    PolicyServerProxy.saveEntity(p);
                } else if (next instanceof PolicyFolder) {
                    PolicyFolder f = (PolicyFolder) next;
                    String oldName = f.getName();
                    String newName = newParentName + oldName.substring(oldParentLength);
                    folderMap.put(newName, oldName);
                    f.setName(newName);
                    PolicyServerProxy.saveEntity(f);
                }
            }
            Collection<DODDigest> newDescriptors = PolicyServerProxy.saveEntitiesDigest(policiesAndFolders);
            for (DODDigest descriptor : newDescriptors) {
                String oldName;
                if (descriptor.getType().equals("POLICY")) {
                    oldName = (String) policyMap.get(descriptor.getName());
                } else if (descriptor.getType().equals("FOLDER")) {
                    oldName = (String) folderMap.get(descriptor.getName());
                } else {
                    throw new IllegalStateException(
                        "The list contains items other than Policies/Policy Folders");
                }
                EntityInfoProvider.replacePolicyDescriptor(oldName, descriptor);    
                if(descriptor.getType().equals("POLICY")){
                	changeReferences(descriptor.getName(), oldName);
                } 
                if (descriptor.getId().equals(currentObjId)) {
                    treeViewer.setSelection(
                        new StructuredSelection(descriptor), true);
                    DomainObjectDescriptor des;
                    try {
                        des = PolicyServerProxy.getDescriptorById(descriptor.getId());
                        gs.setCurrentlySelection(Arrays.asList(new DomainObjectDescriptor[] { des }));
                        gs.forceLoadObjectInEditorPanel(des);
                    } catch (PolicyEditorException e) {
                        e.printStackTrace();
                    }
                } 
            }
        }

        @Override
            public void dropAccept(DropTargetEvent event) {
            DODDigest[] descriptors = (DODDigest[]) PolicyTransfer
                                      .getInstance().nativeToJava(event.currentDataType);

            // Verify that the collection of dropped items contains only
            // policies and policy folders
            for (DODDigest descriptor : descriptors) {
                if (descriptor != null
                    && !descriptor.getType().equals("POLICY")
                    && !descriptor.getType().equals("FOLDER")) {
                    event.detail = DND.DROP_NONE;
                    return;
                }
            }
        }

        private void showError(String error) {
            MessageDialog.openError(getShell(), "Error moving objects", error);
        }
    };
    
    public Policy changeAttribute (String newName, Policy p){
    	GlobalState gs = GlobalState.getInstance();
        int index = newName.lastIndexOf(PQLParser.SEPARATOR);
        String parentName = newName.substring(0,index);
    	DODDigest parentDigest = findParentDigest(parentName);
    	IHasId parentHasId = null;
    	String parentType = parentDigest.getType();
		if (parentType.equals("POLICY")){
			parentHasId = (IHasId) PolicyServerProxy.getEntityForDescriptor(PolicyServerProxy.getDescriptorByName(parentName));
		}
        if(parentType.equals("POLICY")){
            gs.closeEditorFor(parentHasId);
			if(!PolicyHelpers.isSubPolicy(p)){
				p.setAttribute(IPolicy.EXCEPTION_ATTRIBUTE, true);
			}
        }else if (parentType.equals("FOLDER")){
			if(PolicyHelpers.isSubPolicy(p)){
				p.setAttribute(IPolicy.EXCEPTION_ATTRIBUTE, false);
			}
        }
        return p;
    }
    
    public void changeReferences (String newName, String oldName){
        GlobalState gs = GlobalState.getInstance();
        List<IHasId> result = new ArrayList<IHasId>();
        int index = newName.lastIndexOf(PQLParser.SEPARATOR);
        int oldIndex = oldName.lastIndexOf(PQLParser.SEPARATOR);
        String newParentName = newName.substring(0,index);
        String oldParentName = oldName.substring(0,oldIndex);
        DODDigest newParentDigest = findParentDigest(newParentName);
        DODDigest oldParentDigest = findParentDigest(oldParentName);
        IHasId newParentHasId = null;
        IHasId oldParentHasId = null;
        IHasId hasId = null;
        String newParentType = newParentDigest.getType();
        if (oldParentDigest !=null){
	        String oldParentType = oldParentDigest.getType();
	        if (oldParentType.equals("POLICY")){
	        	oldParentHasId = (IHasId) PolicyServerProxy.getEntityForDescriptor(PolicyServerProxy.getDescriptorByName(oldParentName));
	        	gs.closeEditorFor(oldParentHasId);
	        	IPolicy oldparentPolicy = (IPolicy)oldParentHasId;
	        	List<IPolicyReference> policyExceptionRef = oldparentPolicy.getPolicyExceptions().getPolicies();
	           	policyExceptionRef.remove(PolicyHelpers.findRefByName(policyExceptionRef, oldName));
	           	oldparentPolicy.getPolicyExceptions().setPolicies(policyExceptionRef);
				result.add(oldParentHasId);
	        }
        }

		if (!newParentType.equals("FOLDER")){
			newParentHasId = (IHasId) PolicyServerProxy.getEntityForDescriptor(PolicyServerProxy.getDescriptorByName(newParentName));
		}
		hasId = (IHasId) PolicyServerProxy.getEntityForDescriptor(PolicyServerProxy.getDescriptorByName(newName));
        if(newParentType.equals("POLICY")){
            gs.closeEditorFor(newParentHasId);
            IPolicy parentPolicy = (IPolicy)newParentHasId;
            IPolicy currentPolicy = (IPolicy)hasId;
           	List<IPolicyReference> policyExceptionRef = parentPolicy.getPolicyExceptions().getPolicies();
           	policyExceptionRef.remove(PolicyHelpers.findRefByName(policyExceptionRef, oldName));
           	policyExceptionRef.add(new PolicyReference (newName));
           	parentPolicy.getPolicyExceptions().setPolicies(policyExceptionRef);
			result.add(newParentHasId);
        }
        try {
            PolicyServerProxy.client.saveEntities(result);
        } catch (PolicyEditorException e) {
        }
    }
    
    public DODDigest findParentDigest (String upperName){
    	EntityInfoProvider.updatePolicyTree();
        for (DODDigest desc : EntityInfoProvider.getPolicyList()) {
            String fullName = desc.getName();
            if (fullName.equals(upperName) && desc.isAccessible()) {
            	return desc;
            }
        }
        return null;
    }

    private class ViewContentProvider implements ITreeContentProvider {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang
         * .Object)
         */
        public Object[] getChildren(Object parentElement) {
            List<DODDigest> elements = new ArrayList<DODDigest>();
            if (parentElement instanceof String) {
                for (DODDigest descriptor : policyDigestList) {
                    String name = descriptor.getName();
                    int index = name.indexOf(PQLParser.SEPARATOR);
                    // root level objects either have no separators, or have
                    // their
                    // first separator at the end of their name
                    if (index < 0 || index == name.length() - 1) {
                        elements.add(descriptor);
                    }
                }
            } else {
                DODDigest parentDescriptor = (DODDigest) parentElement;
                if (!parentDescriptor.isAccessible()) {
                	// || parentDescriptor.getType().equals("POLICY")
                    return new Object[0];
                }
                String parentName = parentDescriptor.getName()
                                    + PQLParser.SEPARATOR;

                for (DODDigest descriptor : policyDigestList) {
                    String name = descriptor.getName();
                    // children names start with the name of the parent
                    if (name.startsWith(parentName)) {
                        String nameEnding = name.substring(parentName.length(),
                                                           name.length());

                        int index = nameEnding.indexOf(PQLParser.SEPARATOR);
                        // child objects have no further separators
                        if (index < 0) {
                            elements.add(descriptor);
                        }
                    }
                }
                
//            	List<DODDigest> exceptionDependencies = filterDependencies(parentDescriptor);      
//            	if (exceptionDependencies.size()>0){
//            		elements.addAll(exceptionDependencies);
//            	}
//                Collections.sort(elements, new Comparator<DODDigest>() {
//
//                    public int compare(DODDigest o1, DODDigest o2) {
//                        return EntityInformation.getDisplayName(o1)
//                                .compareToIgnoreCase(
//                                        EntityInformation.getDisplayName(o2));
//                    }
//                });
//                return elements.toArray(new DODDigest[elements.size()]);
            }
            

            
            DODDigest[] res = elements.toArray(new DODDigest[elements.size()]);
            Arrays.sort(res, DODDigest.CASE_INSENSITIVE_COMPARATOR);
            return res;
        }
        


        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang
         * .Object)
         */
        public Object getParent(Object element) {
            DODDigest descriptor = (DODDigest) element;
            String name = descriptor.getName();

            int index = name.lastIndexOf(PQLParser.SEPARATOR);
            if (index < 0) {
                return null;
            }
            if (index == name.length()) {
                index = name.substring(name.length() - 1).lastIndexOf(
                    PQLParser.SEPARATOR);
            }
            String parentName = name.substring(0, index);
            return EntityInfoProvider.getPolicyDescriptor(parentName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang
         * .Object)
         */
        public boolean hasChildren(Object element) {
            if (element instanceof DODDigest) {
                DODDigest dod = (DODDigest) element;
                if (!dod.isAccessible()) {
                    return false;
                }
                String type = dod.getType();
                String elemName = dod.getName();
                if (type.equals("FOLDER")) {
                    String childPrefix = elemName + PQLParser.SEPARATOR;
                    for (DODDigest desc : policyDigestList) {
                        if (desc.getName().startsWith(childPrefix)) {
                            return true;
                        }
                    }
                }else if (type.equals("POLICY")){
					//return true if the PolicyListPanel has children
                	if (this.getChildren(element).length>0)
                		return true;
                }
            }
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(
         * java.lang.Object)
         */
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse
         * .jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    private class FilteredContentProvider implements IStructuredContentProvider {

        private boolean inProgress = true;

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            String text = filterControl.getText();
            List<Object> list = new ArrayList<Object>();
            if (showingUsage) {
                list
                    .add(Messages.POLICYLISTPANEL_FILTER_POLICIES_USING_THE_COMPONENT
                         + filterControl.getBoxText()
                         + Messages.POLICYLISTPANEL_FILTER_END);
                if (searchResults_bySingleComponent.isEmpty()) {
                    list.add(emptyString());
                } else {
                    list.addAll(searchResults_bySingleComponent);
                }
            } else {
                list.add(Messages.POLICYLISTPANEL_FILTER_POLICIES_NAMED + text
                         + Messages.POLICYLISTPANEL_FILTER_END);
                if (searchResults_byName.isEmpty()) {
                    list.add(emptyString());
                } else {
                    list.addAll(searchResults_byName);
                }
                list
                    .add(Messages.POLICYLISTPANEL_FILTER_POLICIES_USING_THE_COMPONENT
                         + text + Messages.POLICYLISTPANEL_FILTER_END);
                if (searchResults_byComponent.isEmpty()) {
                    list.add(emptyString());
                } else {
                    list.addAll(searchResults_byComponent);
                }
            }
            return list.toArray();
        }

        private String emptyString() {
            return inProgress ? Messages.IN_PROGRESS_STRING
                : Messages.EMPTY_LIST_STRING;
        }

        public void setInProgress(boolean val) {
            inProgress = val;
        }

    }

    private class ViewLabelProvider extends LabelProvider {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
         */
        @Override
            public Image getImage(Object element) {
            if (element instanceof String) {
                return null;
            }
            return ObjectLabelImageProvider.getImage(element);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
         */
        @Override
            public String getText(Object element) {
            if (element instanceof String) {
                return (String) element;
            }
            if (element instanceof DODDigest) {
                DODDigest dod = (DODDigest) element;
                String fullName = dod.getName();
                int index = fullName.lastIndexOf(PQLParser.SEPARATOR);
                if (index < 0) {
                    return fullName;
                } else {
                    return fullName.substring(index + 1, fullName.length());
                }
            }
            return "";
        }
    }

    private class FilteredLabelProvider extends LabelProvider {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
         */
        @Override
            public Image getImage(Object element) {
            if (element instanceof String) {
                return null;
            }
            return ObjectLabelImageProvider.getImage(element);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
         */
        @Override
            public String getText(Object element) {
            if (element instanceof String) {
                return (String) element;
            }
            if (element instanceof DomainObjectDescriptor) {
                String fullName = ((DomainObjectDescriptor) element).getName();
                return fullName;
            }
            return "";
        }
    }

    private class FilterWorker implements Runnable {

        private String nameFilter;
        private FilterControlListener.EndOfSearch endOfSearch;

        public FilterWorker(String filter,
                            FilterControlListener.EndOfSearch endOfSearch) {
            this.nameFilter = filter;
            this.endOfSearch = endOfSearch;
        }

        public void run() {
            String filterStringPolicies = "%"
                                          + PolicyServerProxy.escape(nameFilter) + "%";
            Collection<DomainObjectDescriptor> entities = PolicyServerProxy
                                                          .getPolicyList(filterStringPolicies);
            searchResults_byName.clear();
            searchResults_byName.addAll(entities);

            searchResults_byComponent.clear();
            String filterStringComponents = "%" + PQLParser.SEPARATOR + "%"
                                            + PolicyServerProxy.escape(nameFilter) + "%";
            entities = PolicyServerProxy
                       .getPoliciesUsingComponent(filterStringComponents);
            searchResults_byComponent.addAll(entities);
            if (filteredContentProvider != null) {
                filteredContentProvider.setInProgress(false);
            }

            getDisplay().syncExec(new Runnable() {

                public void run() {
                    if (filteredViewer != null) {
                        filteredViewer.refresh();
                        resetTableItems();
                        // format special lines
                        int offset = 0;
                        formatTitleRow(offset++);
                        if (searchResults_byName.isEmpty()) {
                            formatEmptyMessage(offset++);
                        }
                        offset += searchResults_byName.size();
                        formatTitleRow(offset++);
                        if (searchResults_byComponent.isEmpty()) {
                            formatEmptyMessage(offset++);
                        }
                    }
                }
            });
            endOfSearch.endOfSearch();
        }
    }

    private class PolicyUsageWorker implements Runnable {

        private String nameFilter;
        private EntityType entityType;

        public PolicyUsageWorker(EntityType entityType, String filter) {
            this.entityType = entityType;
            this.nameFilter = filter;
        }

        public void run() {
            Collection<DomainObjectDescriptor> entities = PolicyServerProxy
                                                          .getPoliciesUsingComponent(nameFilter, entityType);
            searchResults_bySingleComponent.clear();
            searchResults_bySingleComponent.addAll(entities);

            getDisplay().syncExec(new Runnable() {

                public void run() {
                    if (filteredViewer != null) {
                        filteredViewer.refresh();
                        resetTableItems();
                        // format special lines
                        int offset = 0;
                        formatTitleRow(offset++);
                        if (searchResults_bySingleComponent.isEmpty()) {
                            formatEmptyMessage(offset++);
                        }
                    }
                }
            });
        }
    }

    private void formatTitleRow(int index) {
        TableItem item = filteredViewer.getTable().getItem(index);
        item.setBackground(ColorBundle.LIGHT_GRAY);
    }

    public void prepareSelectionForDuplicate() {
        TreeItem[] selections = treeViewer.getTree().getSelection();
        DuplicateAction action = (DuplicateAction) PolicyStudioActionFactory
                                 .getDuplicateAction();
        if (selections.length != 1) {
            action.setEnabled(false);
            return;
        }
        action.setEnabled(true);
        Object object = selections[0].getData();
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
        } else {
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
        } else {
            DuplicateAction.TYPE = EntityType.FOLDER;
        }
        DuplicateAction.VIEW = "author";
    }

    private void formatEmptyMessage(int index) {
        TableItem item = filteredViewer.getTable().getItem(index);
        item.setForeground(ColorBundle.LIGHT_GRAY);
    }

    private void resetTableItems() {
        TableItem[] items = filteredViewer.getTable().getItems();
        for (int i = 0; i < items.length; i++) {
            items[i].setBackground(null);
            items[i].setForeground(null);
        }
        setTableItemColors(items);
    }

    public PolicyListPanel(Composite parent, int style) {
        super(parent, style);

        initialize();
        final EntityInfoListener infoListener = new EntityInfoListener() {

            boolean firstTime = true;

            public void EntityInfoUpdated() {
                if (firstTime) {
                    firstTime = false;
                    addPolicyButton.setEnabled(policiesAreAllowed);
                    // addUsagePolicyButton.setEnabled(policiesAreAllowed);
                    addFolderButton.setEnabled(policiesAreAllowed);
                }
                if (treeViewer != null && !filtering && !showingUsage) {
                    // objectList.clear();
                    // objectList.addAll(EntityInfoProvider.getPolicyList());
                    policyDigestList.clear();
                    policyDigestList.addAll(EntityInfoProvider.getPolicyList());
                    treeViewer.refresh();
                    getDisplay().readAndDispatch();
                    setSelectionToEditorObject();
                    setTreeItemColors(treeViewer.getTree().getItems());
                } else if ((filteredViewer != null) & (filtering)
                           && (!showingUsage)) {
                    Thread t = new Thread(new FilterWorker(filterControl
                                                           .getText(),
                                                           FilterControlListener.EMPTY_END_OF_SEARCH));
                    t.start();
                }
            }
        };
        EntityInfoProvider.addPolicyInfoListener(infoListener);

        addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                EntityInfoProvider.removePolicyInfoListener(infoListener);
            }
        });

        IComponentManager componentManager = ComponentManagerFactory
                                             .getComponentManager();
        IEventManager eventManager = componentManager
                                     .getComponent(EventManagerImpl.COMPONENT_INFO);
        eventManager.registerListener(new CurrentObjectChangedListener(),
                                      EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT);
    }

    /**
     * select tree item corresponding to object in editor
     */
    private void setSelectionToEditorObject() {
        GlobalState gs = GlobalState.getInstance();
        IHasId currentObject = (IHasId) gs.getCurrentObject();
        EntityType entityType = DomainObjectHelper.getEntityType(currentObject);
        if (currentObject != null
            && (entityType == EntityType.POLICY || entityType == EntityType.FOLDER)) {

            DODDigest descriptor = EntityInfoProvider
                                   .getPolicyDescriptor(DomainObjectHelper
                                                        .getName(currentObject));
            if ((descriptor != null) && (treeViewer != null)) {
                treeViewer.setSelection(new StructuredSelection(descriptor),
                                        true);
                DomainObjectDescriptor des;
                try {
                    des = PolicyServerProxy.getDescriptorById(descriptor
                                                              .getId());
                } catch (PolicyEditorException e) {
                    e.printStackTrace();
                    return;
                }
                gs.setCurrentlySelection(Collections.singleton(des));
                memorizeTreeSelection();
            }
        }
    }

    protected void initialize() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.verticalSpacing = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);

        CLabel button = new CLabel(this, SWT.LEFT);
        button.setFont(FontBundle.DEFAULT_BOLD);
        Color color = ResourceManager.getColor(163, 178, 204);
        Color lightColor = ResourceManager.getColor(177, 190, 212);
        Color[] colorGradient = new Color[] {
            ResourceManager.getColor(SWT.COLOR_DARK_GRAY), color,
            lightColor, ResourceManager.getColor(SWT.COLOR_WHITE) };
        button.setText(Messages.POLICYLISTPANEL_POLICIES);
        button.setBackground(colorGradient, new int[] { 5, 95, 100 }, true);

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        button.setLayoutData(data);

        setupAddControl();

        setupTreeViewer();
    }

    private void setupFilterControl(Composite container) {
        filterControl = new FilterControl(container, SWT.NONE,
                                          Messages.FIND_STRING, Messages.FIND_INSTRUCTIONS);
        filterControl.addFilterControlListener(new FilterControlListener() {

            public void search(FilterControlEvent e,
                               FilterControlListener.EndOfSearch endOfSearch) {
                addPolicyButton.setEnabled(false);
                addFolderButton.setEnabled(false);
                filtering = true;
                if (filteredViewer == null) {
                    treeViewer.getTree().dispose();
                    treeViewer = null;
                    setupFilteredViewer();
                    filteredViewer.getTable().moveBelow(filterControl);
                    layout();
                } else {
                    if (filteredContentProvider != null) {
                        filteredContentProvider.setInProgress(true);
                    }
                    filteredViewer.refresh();
                }
                Thread t = new Thread(new FilterWorker(filterControl.getText(),
                                                       endOfSearch));
                t.start();
            }

            public void cancel(FilterControlEvent e) {
                addPolicyButton.setEnabled(policiesAreAllowed);
                addFolderButton.setEnabled(policiesAreAllowed);
                filtering = false;
                if (treeViewer == null) {
                    filteredViewer.getTable().dispose();
                    filteredViewer = null;
                    setupTreeViewer();
                    treeViewer.getTree().moveBelow(filterControl);
                    layout();
                }
                populateList();
            }
        });
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        filterControl.setLayoutData(data);
    }

    public void setupPolicyUsageFilterControl(String componentName,
                                              EntityType type) {
        addPolicyButton.setEnabled(false);
        addFolderButton.setEnabled(false);
        if (filterControl != null) {
            filterControl.dispose();
        }
        showingUsage = true;
        filterControl = new FilterControl(buttonRow, SWT.NONE, "",
                                          componentName);
        filterControl.setEditable(false);
        filterControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttonRow.layout(true);

        if (filteredViewer == null) {
            treeViewer.getTree().dispose();
            treeViewer = null;
            setupFilteredViewer();
            filteredViewer.getTable().moveBelow(filterControl);
        }
        filterControl.moveAbove(filteredViewer.getTable());

        layout();

        // populate filtered list here
        Thread t = new Thread(new PolicyUsageWorker(type, componentName));
        t.start();

        filterControl.addFilterControlListener(new FilterControlListener() {

            public void search(FilterControlEvent e,
                               FilterControlListener.EndOfSearch eos) {
            }

            public void cancel(FilterControlEvent e) {
                if (treeViewer == null) {
                    showingUsage = false;
                    filteredViewer.getTable().dispose();
                    filteredViewer = null;
                    setupTreeViewer();
                    treeViewer.getTree().moveBelow(filterControl);
                    filterControl.dispose();
                    setupFilterControl(buttonRow);
                    // setupFilterControl();
                    filterControl.moveBelow(addFolderButton);
                    layout(true, true);
                }
                addPolicyButton.setEnabled(policiesAreAllowed);
                addFolderButton.setEnabled(policiesAreAllowed);
                populateList();
            }
        });
    }

    private void setupTreeViewer() {
        treeViewer = new TreeViewer(this, SWT.MULTI | SWT.BORDER);

        /*
         * User hash table lookup of tree items. Increased performance at the
         * cost of increased memory usage
         */
        treeViewer.setUseHashlookup(true);

        /*
         * User custom comparer for hash lookup
         */
        treeViewer.setComparer(new TreeItemComparer());

        GridData gridData = new GridData(GridData.FILL_BOTH);
        treeViewer.getTree().setLayoutData(gridData);
        treeViewer.setContentProvider(new ViewContentProvider());
        treeViewer.setLabelProvider(new ViewLabelProvider());
        treeViewer.setInput("root");

        // treeViewer.addDoubleClickListener(doubleClickListener);
        treeViewer.getTree().addMouseListener(mouseAndTreeListener);
        treeViewer.getTree().addTreeListener(mouseAndTreeListener);
        treeViewer.addSelectionChangedListener(selectionChangedListener);

        if (INCLUDE_TEXT_DRAG_SUPPORT) {
            treeViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE,
                                      new Transfer[] { PolicyTransfer.getInstance(),
                                                       TextTransfer.getInstance() }, dragSourceListener);
        }

        int operations = DND.DROP_MOVE;
        Transfer[] types = new Transfer[] { PolicyTransfer.getInstance() };

        treeViewer.addDropSupport(operations, types, dropTargetListener);
        setTreeItemColors(treeViewer.getTree().getItems());
    }

    private void setTreeItemColors(TreeItem[] items) {
        if (items == null) {
            return;
        }
        for (int i = 0; i != items.length; i++) {
            Object data = items[i].getData();
            if (data instanceof DODDigest) {
                DODDigest dod = (DODDigest) data;
                if (dod.isAccessible()) {
                    items[i].setForeground(getShell().getDisplay()
                                           .getSystemColor(SWT.COLOR_BLACK));
                } else {
                    items[i].setForeground(getShell().getDisplay()
                                           .getSystemColor(SWT.COLOR_GRAY));
                }
                setTreeItemColors(items[i].getItems());
            }
        }
    }

    private void setTableItemColors(TableItem[] items) {
        if (items == null) {
            return;
        }
        for (int i = 0; i != items.length; i++) {
            Object data = items[i].getData();
            if (data instanceof DomainObjectDescriptor) {
                DomainObjectDescriptor dod = (DomainObjectDescriptor) data;
                if (dod.isAccessible()) {
                    items[i].setForeground(getShell().getDisplay()
                                           .getSystemColor(SWT.COLOR_BLACK));
                } else {
                    items[i].setForeground(getShell().getDisplay()
                                           .getSystemColor(SWT.COLOR_GRAY));
                }
            }
        }
    }

    private void setupFilteredViewer() {
        filteredViewer = new TableViewer(this, SWT.MULTI | SWT.H_SCROLL
                                         | SWT.V_SCROLL | SWT.FULL_SELECTION);
        Table table = filteredViewer.getTable();
        GridData gridData = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(gridData);

        TableColumn c1 = new TableColumn(table, SWT.LEFT);
        c1.setWidth(1000);
        table.setHeaderVisible(false);

        filteredViewer
            .setContentProvider(filteredContentProvider = new FilteredContentProvider());
        filteredViewer.setLabelProvider(new FilteredLabelProvider());
        filteredViewer.setInput(policyDigestList);
        setTableItemColors(filteredViewer.getTable().getItems());
        // filteredViewer.addDoubleClickListener(doubleClickListener);
        filteredViewer.addSelectionChangedListener(selectionChangedListener);
        filteredViewer.getTable().addMouseListener(mouseAndTreeListener);
        if (INCLUDE_TEXT_DRAG_SUPPORT) {
            filteredViewer.addDragSupport(DND.DROP_COPY,
                                          new Transfer[] { TextTransfer.getInstance() },
                                          dragSourceListener);
        }
    }

    private void setupAddControl() {
        buttonRow = new Composite(this, SWT.NONE);
        buttonRow.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        buttonRow.setLayoutData(data);

        GridLayout layout = new GridLayout(3, false);
        buttonRow.setLayout(layout);

        addPolicyButton = new Button(buttonRow, SWT.PUSH | SWT.FLAT
                                     | SWT.CENTER);
        addPolicyButton.setText(Messages.POLICYLISTPANEL_NEW_POLICY);
        addPolicyButton.setToolTipText(Messages.POLICYLISTPANEL_NEW_POLICY);
        data = new GridData();
        addPolicyButton.setLayoutData(data);
        addPolicyButton.addSelectionListener(new SelectionAdapter() {
            @SuppressWarnings("unchecked")
                @Override
                public void widgetSelected(SelectionEvent e) {
                String prefix = getNewEntityPrefix();
                if (prefix.equals("") || (!getSelectionType().equals("FOLDER"))) {
                    MessageDialog.openError(getShell(),
                                            Messages.POLICYLISTPANEL_NO_FOLDER_TITLE,
                                            Messages.POLICYLISTPANEL_NO_FOLDER_MSG);
                    return;
                }
                NewPolicyDialog dlg = new NewPolicyDialog(getShell(),
                                                          Messages.POLICYLISTPANEL_POLICY_TITLE,
                                                          Messages.POLICYLISTPANEL_POLICY_MSG,
                                                          Messages.POLICYLISTPANEL_POLICY_NAME,
                                                          getNewPolicyNameValidator());
                if (dlg.open() == Dialog.OK) {
                    String policyName = dlg.getValue();
                    String policyPurpose = dlg.getPolicyPurpose();

                    String context = "";
                    IConfigurationElement[] decls = Platform
                                                    .getExtensionRegistry()
                                                    .getConfigurationElementsFor(
                                                        "com.nextlabs.policystudio.editor");
                    for (IConfigurationElement element : decls) {
                        context = element.getAttribute("context");
                        String displayName = element
                                             .getAttribute("displayName");
                        if (displayName.equals(policyPurpose)) {
                            break;
                        }
                    }
                    decls = Platform.getExtensionRegistry()
                            .getConfigurationElementsFor(
                                "com.nextlabs.policystudio.creation");
                    IConfigurationElement foundElement = null;
                    for (IConfigurationElement element : decls) {
                        String extensionContext = element
                                                  .getAttribute("context");
                        if (extensionContext.equals(context)) {
                            foundElement = element;
                            break;
                        }
                    }
                    if (foundElement != null) {
                        String contributor = foundElement.getContributor()
                                             .getName();
                        Bundle bundle = Platform.getBundle(contributor);

                        try {
                            Class myClass = bundle.loadClass(foundElement
                                                             .getAttribute("class"));
                            Method preCreationMethod = myClass.getMethod(
                                "preCreation", (Class[]) null);
                            Constructor constructor[] = myClass
                                                        .getConstructors();
                            CreationExtension extension = (CreationExtension) constructor[0]
                                                          .newInstance();
                            Boolean result = (Boolean) preCreationMethod
                                             .invoke(extension, (Object[]) null);
                            if (!result) {
                                return;
                            }
                        } catch (IllegalArgumentException e1) {
                            LoggingUtil.logError(Activator.ID, "error", e1);
                        } catch (IllegalAccessException e1) {
                            LoggingUtil.logError(Activator.ID, "error", e1);
                        } catch (InvocationTargetException e1) {
                            LoggingUtil.logError(Activator.ID, "error", e1);
                        } catch (ClassNotFoundException e1) {
                            LoggingUtil.logError(Activator.ID, "error", e1);
                        } catch (SecurityException e1) {
                            LoggingUtil.logError(Activator.ID, "error", e1);
                        } catch (NoSuchMethodException e1) {
                            LoggingUtil.logError(Activator.ID, "error", e1);
                        } catch (InstantiationException e1) {
                            LoggingUtil.logError(Activator.ID, "error", e1);
                        }
                    }
                    createPolicy(prefix + policyName.trim(), policyPurpose);
                }
            }
        });

        addFolderButton = new Button(buttonRow, SWT.PUSH | SWT.FLAT
                                     | SWT.CENTER);
        addFolderButton.setText(Messages.POLICYLISTPANEL_NEW_FOLDER);
        addFolderButton.setToolTipText(Messages.POLICYLISTPANEL_NEW_FOLDER);
        data = new GridData();
        addFolderButton.setLayoutData(data);
        addFolderButton.addSelectionListener(new SelectionAdapter() {
            @Override
                public void widgetSelected(SelectionEvent e) {
            	String prefix = getNewEntityPrefix();
                if (!prefix.equals("")){
                	if (!getSelectionType().equals("FOLDER")) {
        				MessageDialog.openError(getShell(),
        				Messages.POLICYLISTPANEL_NO_FOLDER_TITLE,
        				Messages.POLICYLISTPANEL_NO_FOLDER_MSG_FOR_FOLDER);
        				return;
        			}
                }      
                TitleAreaDialogEx dlg = new TitleAreaDialogEx(getShell(),
                                                              Messages.POLICYLISTPANEL_FOLDER_TITLE,
                                                              Messages.POLICYLISTPANEL_FOLDER_MSG,
                                                              Messages.POLICYLISTPANEL_FOLDER_NAME,
                                                              getNewPolicyFolderNameValidator());
                if (dlg.open() == Window.OK) {
                    createFolder(prefix + dlg.getValue().trim());
                }
            }
        });

        setupFilterControl(buttonRow);
    }

    public void loadPolicyInEditPanel(DomainObjectDescriptor descriptor) {
        GlobalState.getInstance().loadObjectInEditorPanel(descriptor);
    }
    
    protected String getSelectionType (){
        IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
        DODDigest descriptor = (DODDigest) selection.getFirstElement();
        String type = descriptor.getType();
        return type;
    }

    protected String getNewEntityPrefix() {
        IStructuredSelection selection = (IStructuredSelection) treeViewer
                                         .getSelection();
        if (selection == null || selection.isEmpty()) {
            return "";
        }
        DODDigest descriptor = (DODDigest) selection.getFirstElement();
        String type = descriptor.getType();
        String name = descriptor.getName();
        if (type.equals("FOLDER")) {
            return name + PQLParser.SEPARATOR;
        }
        int index = name.lastIndexOf(PQLParser.SEPARATOR);
        if (index < 0) {
            return "";
        }
        return name.substring(0, index + 1);
    }

    protected void createPolicy(String name, String type) {
        IDPolicy policy = PolicyServerProxy.createBlankPolicy(name, type);
        GlobalState.getInstance().loadObjectInEditorPanel(policy);
        populateList();
    }
    
    protected void createPolicy(String name, String type, boolean isSubPolicy) {
        IDPolicy policy = PolicyServerProxy.createBlankPolicy(name, type, isSubPolicy);
        GlobalState.getInstance().loadObjectInEditorPanel(policy);
        populateList();
    }
    protected void createFolder(String name) {
        PolicyServerProxy.createBlankPolicyFolder(name);
        populateList();
    }

    public void populateList() {
        EntityInfoProvider.updatePolicyTreeAsync();
    }

    /**
     * @see com.bluejungle.destiny.policymanager.ui.IClipboardEnabled#copy()
     */
    public void copy() {
        Clipboard clipboard = new Clipboard(getDisplay());
        clipboard.setContents(new Object[] { getPQLForSelection() },
                              new Transfer[] { TextTransfer.getInstance() });
    }

    /**
     * @see com.bluejungle.destiny.policymanager.ui.IClipboardEnabled#cut()
     */
    public void cut() {
        copy();
    }

    /**
     * @see com.bluejungle.destiny.policymanager.ui.IClipboardEnabled#paste()
     */
    public void paste() {
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
        private String getPQLForSelection() {
        IStructuredSelection selection;
        if (!filtering && !showingUsage) {
            selection = (IStructuredSelection) treeViewer.getSelection();
        } else {
            selection = (IStructuredSelection) filteredViewer.getSelection();
        }
        StringBuffer output = new StringBuffer();
        Iterator iter = selection.iterator();
        while (iter.hasNext()) {
            Object tmp = iter.next();
            if (!(tmp instanceof DomainObjectDescriptor)) {
                continue;
            }
            DomainObjectDescriptor desc = (DomainObjectDescriptor) tmp;
            if (desc.getType() == EntityType.POLICY) {
                IEditorPanel panel = GlobalState.getInstance().getEditorPanel();
                IDPolicy policy;
                if (panel != null) {
                    IHasId domainObj = panel.getDomainObject();
                    if (domainObj instanceof IDPolicy
                        && domainObj.getId().equals(desc.getId())) {
                        policy = (IDPolicy) panel.getDomainObject();
                    } else {
                        policy = (IDPolicy) PolicyServerProxy
                                 .getEntityForDescriptor(desc);
                    }
                } else {
                    policy = (IDPolicy) PolicyServerProxy
                             .getEntityForDescriptor(desc);
                }
                formatter.reset();
                formatter.formatPolicyDef(policy.getId(), policy);
                output.append(formatter.getPQL());
            }
        }
        return output.toString();
    }

    public IInputValidator getNewPolicyNameValidator() {
        return new IInputValidator() {

            public String isValid(String s) {
                String stringToCheck = s.trim();
                if (EntityInfoProvider.isValidComponentName(stringToCheck)) {
                    String existingName = EntityInfoProvider
                                          .getExistingPolicyFolderName(getNewEntityPrefix()
                                                                 + stringToCheck);
                    if (existingName != null) {
                        return NLS.bind(
                            Messages.POLICYLISTPANEL_ERROR_POLICY_EXIST,
                        existingName);
                    } else {
                        return null;
                    }
                } else {
                    return Messages.POLICYLISTPANEL_ERROR_POLICY_INVALID;
                }
            }
        };
    }

    public IInputValidator getNewPolicyFolderNameValidator() {
        return new IInputValidator() {

            public String isValid(String s) {
                String stringToCheck = s.trim();
                if (EntityInfoProvider.isValidComponentName(stringToCheck)) {
                    String existingName = EntityInfoProvider
                                          .getExistingPolicyFolderName(getNewEntityPrefix()
                                                                       + stringToCheck);
                    if (existingName != null) {
                        return NLS.bind(
                            Messages.POLICYLISTPANEL_ERROR_FOLDER_EXIST,
                        existingName);
                    } else {
                        return null;
                    }
                }
                return Messages.POLICYLISTPANEL_ERROR_FOLDER_INVALID;
            }
        };
    }

    /**
     * Restore selection to previous known state. This is used to restore
     * selection back to the currently selected object after a drag is complete
     */
    private void restoreTreeSelection() {
        if (!restoreSelection) {
            restoreSelection = true;
            return;
        }

        Tree tree = treeViewer.getTree();
        boolean needSelection = selectedItems.size() != tree
                                .getSelectionCount();
        // boolean needDeselection = selectedItems.size() <
        // tree.getSelectionCount();
        if (!needSelection) {
            Set<Item> set = new HashSet<Item>(selectedItems);
            TreeItem[] selection = tree.getSelection();
            for (int i = 0; i < selection.length; i++) {
                if (!set.contains(selection[i])) {
                    needSelection = true;
                    break;
                }
            }
        }
        if (needSelection) {
            // tree.deselectAll();
            boolean valid = true;
            for (Item item : selectedItems) {
                if (item.isDisposed()) {
                    valid = false;
                    break;
                }
            }
            if (!valid) {
                tree.deselectAll();
            } else {
                tree.setSelection((TreeItem[]) selectedItems
                                  .toArray(new TreeItem[selectedItems.size()]));
            }
        }
    }

    /**
     * Restore selection to previous known state. This is used to restore
     * selection back to the currently selected object after a drag is complete
     */
    private void restoreTableSelection() {
        Table table = filteredViewer.getTable();
        int[] indices = new int[selectedItems.size()];
        boolean needSelection = selectedItems.size() != table
                                .getSelectionCount();

        for (int i = 0; i < indices.length; i++) {
            Item item = selectedItems.get(i);
            if ((item instanceof TableItem) && !item.isDisposed()) {
                int index = table.indexOf(((TableItem) selectedItems.get(i)));
                indices[i] = index;
                needSelection |= !table.isSelected(index);
            }
        }
        table.deselectAll();
        if (needSelection) {
            table.select(indices);
        }
    }

    /**
     * Setup new tree selection
     */

    private void setupNewTreeSelection() {
        memorizeTreeSelection();

        IStructuredSelection selection = (IStructuredSelection) treeViewer
                                         .getSelection();

        handleNewSelection(selection);
    }

    /**
     * Remeber the current selectation state for later restoration
     */
    private void memorizeTreeSelection() {
        Tree tree = treeViewer.getTree();
        selectedItems.clear();
        TreeItem[] items = tree.getSelection();
        for (int i = 0; i < items.length; i++) {
            selectedItems.add(items[i]);
        }
    }

    /**
     * remember the current selection state for later restoration. Open new
     * selection in an editor if not already open.
     */
    private void setupNewTableSelection() {
        Table table = filteredViewer.getTable();
        selectedItems.clear();
        TableItem[] items = table.getSelection();
        for (int i = 0; i < items.length; i++) {
            selectedItems.add(items[i]);
        }

        IStructuredSelection selection = (IStructuredSelection) filteredViewer
                                         .getSelection();
        handleNewSelection(selection);
    }

    /**
     * remember the current selection state for later restoration. Open new
     * selection in an editor if not already open.
     */
    private void clearTreeSelection() {
        Tree tree = treeViewer.getTree();
        selectedItems.clear();
        tree.deselectAll();
        IStructuredSelection selection = (IStructuredSelection) treeViewer
                                         .getSelection();
        handleNewSelection(selection);
    }

    /**
     * remember the current selection state for later restoration. Open new
     * selection in an editor if not already open.
     */
    private void clearTableSelection() {
        Table table = filteredViewer.getTable();
        selectedItems.clear();
        table.deselectAll();
        IStructuredSelection selection = (IStructuredSelection) filteredViewer
                                         .getSelection();
        handleNewSelection(selection);
    }

    /**
     * open the editor corresponding to the selected item.
     * 
     * @param selection
     */
	    @SuppressWarnings("unchecked")
	    private void handleNewSelection(IStructuredSelection selection) {
	    List<Object> selectedItems = null;
	    GlobalState gs = GlobalState.getInstance();
	    if (!selection.isEmpty()) {
	        selectedItems = selection.toList();
	        
	        if(selectedItems.size()==1){
	        	firstItem = selectedItems.get(0);
	        }      
	        if (firstItem instanceof DODDigest || firstItem instanceof DomainObjectDescriptor) {
	            DomainObjectDescriptor descriptor = null;
	            if (firstItem instanceof DODDigest) {
	                DODDigest digest = (DODDigest) firstItem;
	                try {
	                    descriptor = PolicyServerProxy.getDescriptorById(digest.getId());
	                } catch (PolicyEditorException e) {
	                    e.printStackTrace();
	                    return;
	                }
	            } else {
	                descriptor = (DomainObjectDescriptor) firstItem;
	            }

	            IHasId currentObject = (IHasId) gs.getCurrentObject();
	            if (currentObject == null
	                || currentObject.getId() != descriptor.getId()
	                || !DomainObjectHelper.getEntityType(currentObject).equals(descriptor.getType())) {
	                if (descriptor.isAccessible()) {
	                    ignoreCurrentObjectChangedEvent = true;
	                    gs.loadObjectInEditorPanel(descriptor);
	                    ignoreCurrentObjectChangedEvent = false;
	                }
	            }
	        } else {
	            selectedItems = Collections.EMPTY_LIST;
	        }
	    } else {
	        selectedItems = Collections.EMPTY_LIST;
	    }


        List<DomainObjectDescriptor> result = new ArrayList<DomainObjectDescriptor>();
        List<Long> ids = new ArrayList<Long>();

        // I think there is only ever one item in this list, but to be safe we'll check each
        // item for type.  It would be much eaiser if DODDigest and DomainObjectDescriptor both
        // implemented IHasId, but they don't.
        for (Object item : selectedItems) {
            if (item instanceof DODDigest) {
                ids.add(((DODDigest)item).getId());
            } else if (item instanceof DomainObjectDescriptor) {
                ids.add(((DomainObjectDescriptor)item).getId());
            }
        }
        if (ids.size() > 0) {
            try {
                result = PolicyServerProxy.getDescriptorsByIds(ids);
            } catch (PolicyEditorException e) {
                e.printStackTrace();
                return;
            }
            gs.setCurrentlySelection(result);
        } else {
            gs.setCurrentlySelection(Collections.EMPTY_LIST);
        }
    }

    /**
     * Determine if a context menu should be shown for the following menu item
     * 
     * @param item
     *            the item to test
     * @return true if a context menu should be shown; false otherwise
     */
    public boolean shouldShowContextMenu(TreeItem item) {
        Object data = item.getData();
        return shouldShowContextMenu(data);

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
        return shouldShowContextMenu(data);
    }

    /**
     * Determine if a context menu should be shown for the menu item with the
     * specified data
     * 
     * @param data
     *            the data of the menu item to test
     * @return true if a context menu should be shown; false otherwise
     */
    private boolean shouldShowContextMenu(Object data) {
        boolean valueToReturn = false;
        if (data instanceof DODDigest) {
            valueToReturn = true;
        }

        return valueToReturn;
    }

    /**
     * Display a context menu item for the menu item
     * 
     * @param me
     */
    private void showContextMenu(MouseEvent me) {
        DODDigest digest = null;
        Control control = (Control) me.getSource();
        Point l = control.toDisplay(me.x, me.y);
        Shell shell = getShell();
        Display display = shell.getDisplay();
        if (control instanceof Tree) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer
                                             .getSelection();
            digest = (DODDigest) selection.getFirstElement();
            if (digest.getType().equals("FOLDER")) {
                MenuManager menu = new MenuManager();
                menu.add(PolicyStudioActionFactory.getDeleteAction());
                menu.add(PolicyStudioActionFactory.getDuplicateAction());
                menu.add(new Separator());
                menu.add(PolicyStudioActionFactory.getObjectPropertiesAction());
                Menu contextMenu = menu.createContextMenu(shell);
                contextMenu.setLocation(l.x, l.y);
                contextMenu.setVisible(true);
                while (!contextMenu.isDisposed() && contextMenu.isVisible()) {
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                }
                contextMenu.dispose();

                return;
            }
        }

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

        MenuManager cmm = createContextMenu();
        Menu contextMenu = cmm.createContextMenu(getShell());

        contextMenu.setLocation(l.x, l.y);
        contextMenu.setVisible(true);
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

    private class TreeItemComparer implements IElementComparer {

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

    private final class CurrentObjectChangedListener implements IEventListener {

        /**
         * @see com.bluejungle.destiny.policymanager.ui.ObjectChangeListener#objectChanged()
         */
        public void onEvent(IEvent event) {
            if (!PolicyListPanel.this.ignoreCurrentObjectChangedEvent) {
                IHasId currentObject = (IHasId) GlobalState.getInstance()
                                       .getCurrentObject();
                if (DomainObjectHelper.getEntityType(currentObject) == EntityType.POLICY) {
                    PolicyListPanel.this.setSelectionToEditorObject();
                }
            }
        }
    }
}
