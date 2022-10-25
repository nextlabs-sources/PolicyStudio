/*
 * Created on Jun 17, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.bluejungle.destiny.policymanager.model.EntityInformation;
import com.bluejungle.destiny.policymanager.model.PolicyServerHelper;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyManagerView;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.common.AccessPolicy;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.exceptions.PolicyReference;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.destiny.policy.PolicyFolder;
import com.bluejungle.pf.domain.destiny.policy.PolicyObject;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

/**
 * @author bmeng
 * 
 */
public class DuplicateDialog extends TitleAreaDialog {
    final private static String POLICIES = ApplicationMessages.ABSTRACTTAB_POLICIES;
    private TreeViewer treeViewer;
    private Text textName;
    private EntityType type;
    private List<DODDigest> sources = new ArrayList<DODDigest>();
    private String prefix;
    private String view;

    private class NavigationTreeContentProvder implements ITreeContentProvider {
        public Object[] getChildren(Object parentElement) {
        	List<DODDigest> elements = new ArrayList<DODDigest>();
            if (parentElement.equals("root")) {
                return new String[] { POLICIES };
            }
            if (parentElement.equals(POLICIES)) {
//                List<DODDigest> elements = new ArrayList<DODDigest>();
                List<DODDigest> descriptors = null;
                descriptors = EntityInfoProvider.getPolicyList();
                for (DODDigest descriptor : descriptors) {
//                    if (!descriptor.getType().equals("FOLDER")) {
//                        continue;
//                    }
                    String name = descriptor.getName();
                    int index = name.indexOf(PQLParser.SEPARATOR);
                    if (index < 0 || index == name.length() - 1) {
                        elements.add(descriptor);
                    }
                }
//                Collections.sort(elements, new Comparator<DODDigest>() {
//
//                    public int compare(DODDigest o1, DODDigest o2) {
//                        return EntityInformation.getDisplayName(o1)
//                            .compareToIgnoreCase(
//                                EntityInformation.getDisplayName(o2));
//                    }
//                });
//                return elements.toArray(new DODDigest[elements.size()]);
                
                DODDigest[] res = elements.toArray(new DODDigest[elements.size()]);
                Arrays.sort(res, DODDigest.CASE_INSENSITIVE_COMPARATOR);
                return res;

            } else if (parentElement instanceof DODDigest) {
//                List<DODDigest> elements = new ArrayList<DODDigest>();
                DODDigest parentDescriptor = (DODDigest) parentElement;
                if (!parentDescriptor.isAccessible()) {
                	// || parentDescriptor.getType().equals("POLICY")
                    return new Object[0];
                }
                String parentName = parentDescriptor.getName()
                                    + PQLParser.SEPARATOR;

                List<DODDigest> descriptors = null;
                descriptors = EntityInfoProvider.getPolicyList();
                for (DODDigest descriptor : descriptors) {
//                    if (!descriptor.getType().equals("FOLDER")) {
//                        continue;
//                    }
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
//                Collections.sort(elements, new Comparator<DODDigest>() {
//
//                    public int compare(DODDigest o1, DODDigest o2) {
//                        return EntityInformation.getDisplayName(o1)
//                            .compareToIgnoreCase(
//                                EntityInformation.getDisplayName(o2));
//                    }
//                });
//                return elements.toArray(new DODDigest[elements.size()]);
                DODDigest[] res = elements.toArray(new DODDigest[elements.size()]);
                Arrays.sort(res, DODDigest.CASE_INSENSITIVE_COMPARATOR);
                return res;
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
            if (index < 0) {
                return POLICIES;
            }
            if (index == name.length()) {
                index = name.substring(name.length() - 1).lastIndexOf(
                    PQLParser.SEPARATOR);
            }
            String parentName = name.substring(0, index);
//            List<DODDigest> descriptors = null;
//            descriptors = EntityInfoProvider.getPolicyList();
//            for (DODDigest folder : descriptors) {
//                if (!descriptor.getType().equals("FOLDER")) {
//                    continue;
//                }
//                if (folder.getName().equals(parentName)) {
//                    return folder;
//                }
//            }
//            return null;
            return EntityInfoProvider.getPolicyDescriptor(parentName);
            }

        public boolean hasChildren(Object element) {
            if (element.equals(POLICIES)) {
                List<DODDigest> descriptors = null;
                descriptors = EntityInfoProvider.getPolicyList();
                for (DODDigest descriptor : descriptors) {
                    if (descriptor.getType().equals("FOLDER")) {
                        return true;
                    }
                }
                return false;
            }
            if (element instanceof DODDigest) {
                DODDigest dod = (DODDigest) element;
                if (!dod.isAccessible()) {
                    return false;
                }
                String elemName = dod.getName();
                String type = dod.getType();
//                String childPrefix = elemName + PQLParser.SEPARATOR;
                List<DODDigest> descriptors = null;
                descriptors = EntityInfoProvider.getPolicyList();
//                for (DODDigest desc : descriptors) {
//                    if (!desc.getType().equals("FOLDER")) {
//                        continue;
//                    }
//                    if (desc.getName().startsWith(childPrefix)) {
//                        return true;
//                    }
//                }
                if (type.equals("FOLDER")) {
                    String childPrefix = elemName + PQLParser.SEPARATOR;
                for (DODDigest desc : descriptors) {
                        if (desc.getName().startsWith(childPrefix)) {
                            return true;
                        }
                    }
                }else if (type.equals("POLICY")){  
                	if (PolicyHelpers.filterExceptionDependencies(dod).size()>0){
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
//            boolean expanded = treeViewer.getExpandedState(element);
            if (element instanceof String) {
                return ImageBundle.FOLDER_OPEN_IMG;
            } else {
            	return ObjectLabelImageProvider.getImage(element);
            }
//            if (expanded) {
//                return ImageBundle.FOLDER_OPEN_IMG;
//            } else {
//                return ImageBundle.FOLDER_IMG;
//            }
        }

        @Override
            public String getText(Object element) {
            if (element instanceof String) {
                return (String) element;
            }
            if (element instanceof DODDigest) {
                return EntityInformation.getDisplayName((DODDigest) element);
            }
            return "";
        }
    }

    /**
     * @param parent
     */
    public DuplicateDialog(Shell parent) {
        super(parent);
    }

    @Override
        protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        newShell.setText(DialogMessages.DUPLICATEDIALOG_DUPLICATE);
        newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
    }

    @Override
        public void create() {
        super.create();
        if (type == EntityType.FOLDER || type == EntityType.POLICY) {
            setTitle(DialogMessages.DUPLICATEDIALOG_TITLE_FOLDER);
        } else {
            setTitle(DialogMessages.DUPLICATEDIALOG_TILTE_COMPONENT);
        }
        setTitleImage(ImageBundle.TITLE_IMAGE);
    }

    @Override
        protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        final Composite root = new Composite(composite, SWT.NONE);
        GridData data = new GridData(GridData.FILL_BOTH);
        root.setLayoutData(data);

        GridLayout layout = new GridLayout();
        root.setLayout(layout);

        Group sourceGroup = new Group(root, SWT.NONE);
        sourceGroup.setText(DialogMessages.DUPLICATEDIALOG_SOURCE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        sourceGroup.setLayoutData(data);
        layout = new GridLayout(2, false);
        sourceGroup.setLayout(layout);

        Label label = new Label(sourceGroup, SWT.NONE);
        label.setText(DialogMessages.DUPLICATEDIALOG_TYPE);

        Text text = new Text(sourceGroup, SWT.BORDER);
        text.setEnabled(false);
        data = new GridData(GridData.FILL_HORIZONTAL);
        text.setLayoutData(data);
        text.setText(type.toString());

        label = new Label(sourceGroup, SWT.NONE);
        label.setText(DialogMessages.DUPLICATEDIALOG_NAME);

        text = new Text(sourceGroup, SWT.BORDER);
        text.setEnabled(false);
        data = new GridData(GridData.FILL_HORIZONTAL);
        text.setLayoutData(data);
        text.setText(getBaseObject().getName());

        Group targetGroup = new Group(root, SWT.NONE);
        targetGroup.setText(DialogMessages.DUPLICATEDIALOG_TARGET);
        data = new GridData(GridData.FILL_HORIZONTAL);
        targetGroup.setLayoutData(data);
        layout = new GridLayout(2, false);
        targetGroup.setLayout(layout);

        if (type == EntityType.POLICY || type == EntityType.FOLDER) {
            treeViewer = new TreeViewer(targetGroup, SWT.BORDER);
            Tree tree = treeViewer.getTree();
            data = new GridData(GridData.FILL_BOTH);
            data.horizontalSpan = 2;
            data.heightHint = 300;
            tree.setLayoutData(data);

            treeViewer.setContentProvider(new NavigationTreeContentProvder());
            treeViewer.setLabelProvider(new NavigationTreeLabelProvider());
            treeViewer.setInput("root");
//            treeViewer.getTree().addTreeListener(new TreeListener() {
//                public void treeCollapsed(TreeEvent e) {
//                    TreeItem item = (TreeItem) e.item;
//                    item.setImage(ImageBundle.FOLDER_IMG);
//                }
//
//                public void treeExpanded(TreeEvent e) {
//                    TreeItem item = (TreeItem) e.item;
//                    item.setImage(ImageBundle.FOLDER_OPEN_IMG);
//                }
//            });
//            treeViewer.expandAll();
            treeViewer.refresh();
            treeViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        validationCheck();
                    }
                });
        }

        label = new Label(targetGroup, SWT.NONE);
        label.setText(DialogMessages.DUPLICATEDIALOG_NAME);

        textName = new Text(targetGroup, SWT.BORDER);
        textName.setTextLimit(128);
        data = new GridData(GridData.FILL_HORIZONTAL);
        textName.setLayoutData(data);
        textName.setText(getSuggestedName(EntityInformation
                                          .getDisplayName(getBaseObject())));
        textName.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validationCheck();
            }
        });

        return composite;
    }

    private String getSuggestedName(String name) {
        String supposedName = name;
        if (type == EntityType.FOLDER || type == EntityType.POLICY) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer
                                             .getSelection();
            Object object = selection.getFirstElement();

            if (object instanceof DODDigest) {
                DODDigest descriptor = (DODDigest) object;
                supposedName = descriptor.getName() + PQLParser.SEPARATOR
                               + name;
            }

            if (object == null && type == EntityType.POLICY) {
                return supposedName;
            }

            if (supposedName.indexOf(PQLParser.SEPARATOR) == -1
                && type == EntityType.POLICY) {
                return getSuggestedName("Copy of " + name);
            }
            List<DODDigest> descriptors = null;
            descriptors = EntityInfoProvider.getPolicyList();
            for (DODDigest descriptor : descriptors) {
                if (descriptor.getName().equals(supposedName)) {
                    return getSuggestedName("Copy of " + name);
                }
            }
        } else {
            String source = sources.get(0).getName();
            supposedName = source.substring(0, source
                                            .indexOf(PQLParser.SEPARATOR))
                           + PQLParser.SEPARATOR + name;
            DODDigest digest = EntityInfoProvider
                               .getComponentDescriptor(supposedName);
            if (digest != null) {
                return getSuggestedName("Copy of " + name);
            }
        }
        return name;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public void setSources(List<DomainObjectDescriptor> sources) {
    	ArrayList <DomainObjectDescriptor> list = new ArrayList <DomainObjectDescriptor>(sources);
        Collections.sort(list,  DomainObjectDescriptor.CASE_INSENSITIVE_COMPARATOR);
        
        for (DomainObjectDescriptor descriptor : list) {
            if (descriptor.getType().equals(EntityType.POLICY)
                || descriptor.getType().equals(EntityType.FOLDER)) {
                for (DODDigest digest : EntityInfoProvider.getPolicyList()) {
                    if (digest.getId().longValue() == descriptor.getId()
                        .longValue()) {
                        this.sources.add(digest);
                    }
                }
            } else if (descriptor.getType().equals(EntityType.COMPONENT)) {
                String type = PolicyServerHelper
                              .getTypeFromComponentName(descriptor.getName());
                for (DODDigest digest : EntityInfoProvider
                         .getComponentList(type)) {
                    if (digest.getId().longValue() == descriptor.getId()
                        .longValue()) {
                        this.sources.add(digest);
                    }
                }
            }
        }
    }

    private DODDigest getBaseObject() {
        if (type == EntityType.FOLDER) {
            DODDigest result = null;
            for (DODDigest descriptor : sources) {
                if (!descriptor.getType().equals("FOLDER")) {
                    continue;
                }
                if (result == null) {
                    result = descriptor;
                } else if (result.getName().length() > descriptor.getName()
                           .length()) {
                    result = descriptor;
                }
            }
            return result;
        } else {
            return sources.get(0);
        }
    }

    @Override
        protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);

        getButton(IDialogConstants.OK_ID).setText(DialogMessages.LABEL_SAVE);

        validationCheck();
    }

    private void validationCheck() {
        String name = textName.getText().trim();
        String supposedName = name;

        setMessage("", IMessageProvider.NONE);
        if (name.length() == 0) {
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            setMessage(DialogMessages.DUPLICATEDIALOG_ERROR_EMPTY,
                       IMessageProvider.ERROR);
            return;
        }
        if (!EntityInfoProvider.isValidComponentName(name)) {
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            setMessage(DialogMessages.DUPLICATEDIALOG_ERROR_INVALID_CHAR,
                       IMessageProvider.ERROR);
            return;
        }
        if (type == EntityType.FOLDER || type == EntityType.POLICY) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer
                                             .getSelection();
            Object targetObject = selection.getFirstElement();
            IHasId hasId = null;
            IHasId targetHasId = null;
    		Set<String> attributes = new HashSet<String>();
            if (targetObject instanceof DODDigest) {
                DODDigest targetDescriptor = (DODDigest) targetObject;
                supposedName = targetDescriptor.getName() + PQLParser.SEPARATOR+ name;
                
        		if(type == EntityType.FOLDER && targetDescriptor.getType().equals("POLICY")){
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                    setMessage(DialogMessages.DUPLICATEDIALOG_ERROR_WRONG_SELECTION,
                            IMessageProvider.ERROR);
                 return;
        		}
        		
       			try {
       		       	targetHasId= (IHasId) PolicyServerProxy
       		       		.getEntityForDescriptor(PolicyServerProxy.getDescriptorById(targetDescriptor.getId()));
       		       	hasId= (IHasId) PolicyServerProxy
       		       		.getEntityForDescriptor(PolicyServerProxy.getDescriptorById(sources.get(0).getId()));

        		    }catch (PolicyEditorException e) {
        		    	e.printStackTrace();
        		    }
        		    if(!(targetHasId instanceof PolicyFolder)){
	            		IPolicy policy = (IPolicy)hasId;
	            		attributes.addAll(policy.getAttributes());
	            		IPolicy targetPolicy = (IPolicy)targetHasId; 
	                	attributes.addAll(targetPolicy.getAttributes());
	            		if(attributes.contains(IPolicy.EXCEPTION_ATTRIBUTE)){
	            			attributes.remove(IPolicy.EXCEPTION_ATTRIBUTE);
	            		}
            		}
            }

    		if(attributes.size()>1){
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                setMessage(DialogMessages.DUPLICATEDIALOG_ERROR_WRONG_TYPE,
                        IMessageProvider.ERROR);
             return;
    		}

			/**
			 * <Vlidation checks for DAC Rules>
			 * CAP can only be duplicated underneath a folder
			 * CAR can only be duplicated underneath a CAP
			 */
    		if(hasId!=null && !(hasId instanceof PolicyFolder) && PolicyHelpers.isDACPolicyType(hasId)){
    			if (PolicyHelpers.isSubPolicy(hasId)){
    				if(!(targetHasId instanceof Policy)||PolicyHelpers.isSubPolicy(targetHasId)){
    	                getButton(IDialogConstants.OK_ID).setEnabled(false);
    	                setMessage(DialogMessages.DUPLICATEDIALOG_ERROR_CAR,
    	                        IMessageProvider.ERROR);
    	                return;
    				}
    			}else{
    				if(!(targetHasId instanceof PolicyFolder)){
    	                getButton(IDialogConstants.OK_ID).setEnabled(false);
    	                setMessage(DialogMessages.DUPLICATEDIALOG_ERROR_CAP,
    	                        IMessageProvider.ERROR);
    	                return;
    				}
    			}
            }
			/**
			 * </Vlidation checks for DAC Rules>
			 */

            if (supposedName.indexOf(PQLParser.SEPARATOR) == -1
                && type == EntityType.POLICY) {
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                setMessage(DialogMessages.DUPLICATEDIALOG_ERROR_POLICY,
                           IMessageProvider.ERROR);
                return;
            }
            List<DODDigest> descriptors = null;
            descriptors = EntityInfoProvider.getPolicyList();
            for (DODDigest descriptor : descriptors) {
                if (descriptor.getName().equalsIgnoreCase(supposedName)) {
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                    setMessage(DialogMessages.DUPLICATEDIALOG_ERROR_NAME,
                               IMessageProvider.ERROR);
                    return;
                }
            }
            getButton(IDialogConstants.OK_ID).setEnabled(true);
            prefix = supposedName;
        } else {
            String source = sources.get(0).getName();
            supposedName = source.substring(0, source
                                            .indexOf(PQLParser.SEPARATOR))
                           + PQLParser.SEPARATOR + name;
            DODDigest digest = EntityInfoProvider
                               .getComponentDescriptor(supposedName);
            if (digest != null) {
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                setMessage(DialogMessages.DUPLICATEDIALOG_ERROR_NAME,
                           IMessageProvider.ERROR);
                return;
            }
            getButton(IDialogConstants.OK_ID).setEnabled(true);
            prefix = supposedName;
        }
    }

    @Override
    protected void okPressed() {
        if (type == EntityType.FOLDER) {
            DODDigest base = getBaseObject();
            String baseName = base.getName();
            for (DODDigest descriptor : sources) {
                String name = descriptor.getName();
                String newName = name.substring(baseName.length());
                newName = prefix + newName;
                duplicate(descriptor, newName);
            }
        } else if (type == EntityType.POLICY) {
            DODDigest base = getBaseObject();
            String baseName = base.getName();
            for (DODDigest descriptor : sources) {
                String name = descriptor.getName();
                String newName = name.substring(baseName.length());
                newName = prefix + newName;
                duplicate(descriptor, newName);
            }
        } else {
            DODDigest descriptor = sources.get(0);
            String newName = prefix;
            duplicate(descriptor, newName);
        }

        if (type == EntityType.FOLDER || type == EntityType.POLICY) {
            EntityInfoProvider.updatePolicyTree();
        } else {
            String name = prefix.substring(0, prefix
                                           .indexOf(PQLParser.SEPARATOR));
            EntityInfoProvider.updateComponentList(name);
        }

        if (view.equals("manager")) {
            PolicyManagerView.refreshCurrentTab();
        }

        super.okPressed();
    }

    private void duplicate(DODDigest descriptor, String newName) {
    	List<IHasId> result = new ArrayList<IHasId>();
        IDSubject currentUser = null;
        IHasId hasId = null;
        String oldName = descriptor.getName();
        String type = descriptor.getType();
        
        try {
	       	hasId= (IHasId) PolicyServerProxy.getEntityForDescriptor(PolicyServerProxy.getDescriptorById(descriptor.getId()));
            currentUser = PolicyServerProxy.getLoggedInUser();
        } catch (PolicyEditorException e) {
            e.printStackTrace();
        }

        if (hasId instanceof IDPolicy) {
            PolicyObject source = (PolicyObject) hasId;
            source.setId(null);
        } else if (hasId instanceof PolicyFolder) {
            PolicyFolder source = (PolicyFolder) hasId;
            source.setId(null);
        } else if (hasId instanceof SpecBase) {
            SpecBase source = (SpecBase) hasId;
            source.setId(null);
        }
        IDPolicy temp = PolicyServerProxy.createNewPolicy(newName);
        DomainObjectHelper.setName(hasId, newName);
        DomainObjectHelper.setStatus(hasId, DevelopmentStatus.DRAFT);
        DomainObjectHelper.setOwnerId(hasId, currentUser);
        DomainObjectHelper.setAccessPolicy(hasId, (AccessPolicy)temp.getAccessPolicy());
	        if(type.equals("POLICY")){
		        changeReferences(hasId, newName, oldName);
	        }
		    result.add(hasId);
	        try {
				PolicyServerProxy.client.saveEntities(result);
			} catch (PolicyEditorException e) {
				e.printStackTrace();
			}
    }

    public void changeReferences (IHasId hasId, String newName, String oldName){
        GlobalState gs = GlobalState.getInstance();
        List<IHasId> result = new ArrayList<IHasId>();
        int index = newName.lastIndexOf(PQLParser.SEPARATOR);
        String parentName = newName.substring(0,index);
        DODDigest parentDigest = findUpperDigest(parentName);
        IHasId parentHasId = null;
        String parentType = parentDigest.getType();
		if (!parentType.equals("FOLDER")){
			parentHasId = (IHasId) PolicyServerProxy.getEntityForDescriptor(PolicyServerProxy.getDescriptorByName(parentName));
		}
        if(parentType.equals("POLICY")){
            gs.closeEditorFor(parentHasId);
            IPolicy parentPolicy = (IPolicy)parentHasId;
            IPolicy currentPolicy = (IPolicy)hasId;
           	List<IPolicyReference> policyExceptionRef = parentPolicy.getPolicyExceptions().getPolicies();
           	if(!sameParent(newName, oldName)){
               	policyExceptionRef.remove(PolicyHelpers.findRefByName(policyExceptionRef, oldName));
           	}
           	policyExceptionRef.add(new PolicyReference (newName));
           	parentPolicy.getPolicyExceptions().setPolicies(policyExceptionRef);
           	currentPolicy.getPolicyExceptions().setPolicies( new ArrayList<IPolicyReference>());
			if(!PolicyHelpers.isSubPolicy(currentPolicy)){
				currentPolicy.setAttribute(IPolicy.EXCEPTION_ATTRIBUTE, true);
        result.add(hasId);
    }
			result.add(parentHasId);
        }else if (parentType.equals("FOLDER")){
        	if(hasId instanceof IDPolicy){
            	IPolicy currentPolicy = (IPolicy)hasId;
    			if(PolicyHelpers.isSubPolicy(currentPolicy)){
    				currentPolicy.setAttribute(IPolicy.EXCEPTION_ATTRIBUTE, false);
    				result.add(hasId);
    			}
        	}
        }
        try {
            PolicyServerProxy.client.saveEntities(result);
        } catch (PolicyEditorException e) {
        }
    }
    
    public DODDigest findUpperDigest (String upperName){
    	EntityInfoProvider.updatePolicyTree();
        for (DODDigest desc : EntityInfoProvider.getPolicyList()) {
            String fullName = desc.getName();
            if (fullName.equals(upperName) && desc.isAccessible()) {
            	return desc;
            }
        }
        return null;
    }
    
    public boolean sameParent (String newName, String oldName){
		int newIndex = newName.lastIndexOf(PQLParser.SEPARATOR);
		String newParentName = newName.substring(0, newIndex);
		int oldIndex = oldName.lastIndexOf(PQLParser.SEPARATOR);
		String oldParentName = oldName.substring(0, oldIndex);
    	if (newParentName.equalsIgnoreCase(oldParentName)){
    		return true;
    	}else{
    		return false;
    	}
    }

    public void setView(String view) {
        this.view = view;
    }
}
