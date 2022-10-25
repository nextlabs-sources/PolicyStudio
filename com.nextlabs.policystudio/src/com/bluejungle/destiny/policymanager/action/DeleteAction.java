/*
 * Created on Jun 14, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.event.defaultimpl.PolicyOrComponentData;
import com.bluejungle.destiny.policymanager.model.EntityInformation;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.ExceptionClassListControl;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyManagerView;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;

/**
 * @author dstarke
 * 
 */
public class DeleteAction extends BaseDisableableAction {

	public static final Set<EntityType> POLICY_FOLDER_CONTAINED_ENTITY_TYPES = new HashSet<EntityType>();
	static {
		POLICY_FOLDER_CONTAINED_ENTITY_TYPES.add(EntityType.FOLDER);
		POLICY_FOLDER_CONTAINED_ENTITY_TYPES.add(EntityType.POLICY);
	}

	/**
     * 
     */
	public DeleteAction() {
		super();
	}

	/**
	 * @param text
	 */
	public DeleteAction(String text) {
		super(text);
	}

	/**
	 * @param text
	 * @param image
	 */
	public DeleteAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	/**
	 * @param text
	 * @param style
	 */
	public DeleteAction(String text, int style) {
		super(text, style);
	}

	public void doDelete(Set<DomainObjectDescriptor> selectedItems){
		Set<String> componentDeleted = new HashSet<String>();
		Set<IHasId> entitiesToUpdate = new HashSet<IHasId>();
		GlobalState gs = GlobalState.getInstance();
		for (DomainObjectDescriptor nextDescriptor : selectedItems) {
			EntityType currentType = nextDescriptor.getType();
			IHasId dj = (IHasId) PolicyServerProxy.getEntityForDescriptor(nextDescriptor);
			if (currentType == EntityType.POLICY && PolicyHelpers.isSubPolicy(dj)){
				String name = DomainObjectHelper.getName(dj);
				int index = name.lastIndexOf(PQLParser.SEPARATOR);
				String topDomainObjectDespName = name.substring(0, index);
				DomainObjectDescriptor dod = PolicyServerProxy.getDescriptorByName(topDomainObjectDespName);
				IHasId topId = (IHasId) PolicyServerProxy.getEntityForDescriptor(dod);
				gs.closeEditorFor(topId);
			}
			Set<DomainObjectDescriptor> selectedItem = new HashSet<DomainObjectDescriptor>();
			selectedItem.add(nextDescriptor);
			Set<DomainObjectDescriptor> selectedItemExceptions = PolicyHelpers.filterExceptionDependencies(selectedItem);
			selectedItemExceptions.add(nextDescriptor);
			for(DomainObjectDescriptor dsp : selectedItemExceptions){
				EntityType entityType = nextDescriptor.getType();

				String name = dsp.getName();
				// if object has not been deployed, or is permanently inactive, just delete it
				PolicyServerProxy.ObjectVersion version = PolicyServerProxy.getLastScheduledVersion(dsp);
				// also make sure there are no objects referring to this one
				Collection<DomainObjectDescriptor> c = PolicyServerProxy.getAllReferringObjects(name);

				if (((c == null) || (c.isEmpty()))
						&& (version == null || version.activeTo.before(new Date()))
						&& ((!entityType.equals(EntityType.FOLDER)) || (isPolicyFolderEmpty(dsp)))) {
					IHasId domainObject = (IHasId) PolicyServerProxy.getEntityForDescriptor(dsp);
					DomainObjectHelper.setStatus(domainObject,
							DevelopmentStatus.DELETED);
					if(dsp.getType()==EntityType.POLICY){
						removeExceptionRef(name, dsp);
					}
					entitiesToUpdate.add(domainObject);
					gs.closeEditorFor(domainObject);
					int pos = name.indexOf(PQLParser.SEPARATOR);
					if (pos != -1 && entityType != EntityType.POLICY
							&& entityType != EntityType.FOLDER) {
						componentDeleted.add(name.substring(0, pos).toUpperCase());
					}
				}
			}
		}
		PolicyServerProxy.saveEntities(entitiesToUpdate);
		// Update lists for deleted entity types
		for (String componentTypeName : componentDeleted) {
			EntityInfoProvider.updateComponentList(componentTypeName);
		}	
		Boolean inPolicyAuthorView = PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION.isEnabled();
		if (inPolicyAuthorView){
			EntityInfoProvider.updatePolicyTree();
		}else{
			PolicyManagerView.refreshCurrentTab();
		}
	}
	private void removeExceptionRef(String subName, DomainObjectDescriptor subDod){
		Set<IHasId> entitiesToUpdate = new HashSet<IHasId>();
		int index = subName.lastIndexOf(PQLParser.SEPARATOR);
		String topDomainObjectDespName = subName.substring(0, index);
		DODDigest digest = findParentDigest(topDomainObjectDespName);
		IHasId domainObject = null;
		if(digest.getType().equals("POLICY")){
			domainObject =(IHasId) PolicyServerProxy.getEntityForDescriptor(PolicyServerProxy.getDescriptorByName(topDomainObjectDespName));
			if (domainObject instanceof IDPolicy) {
				IDPolicy policy = (IDPolicy)domainObject;
				List<IPolicyReference> policyRef = policy.getPolicyExceptions().getPolicies();
				policyRef.remove(PolicyHelpers.findRefByName(policyRef, subName));
				entitiesToUpdate.add(domainObject);
				PolicyServerProxy.saveEntities(entitiesToUpdate);
	        }
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
	
	@Override
	public void run() {
		Set<DomainObjectDescriptor> selectedItems = getSelectedItems();
		Iterator iter = selectedItems.iterator();
		if(displayDeleteExceptionDialog(iter.next())){
			doDelete(selectedItems);
		}
	}

	/**
	 * @return true if user wants to delete the policy, false otherwise
	 */
	private boolean displayDeleteExceptionDialog(Object object) {
		if(showDeleteExceptionDialog(object)){
			StringBuffer msg = new StringBuffer();
			msg.append(ApplicationMessages.CLASSLISTCONTROL_DELETE_EXCEPTION_MSG);
			if (MessageDialog.openQuestion(
							Display.getCurrent().getActiveShell(),ApplicationMessages.CLASSLISTCONTROL_DELETE_EXCEPTION, msg.toString())) {
				return true;
			} else {
				return false;
			}
		}return true;
	}
	
	private boolean showDeleteExceptionDialog(Object object){
		boolean show = false;
		DomainObjectDescriptor dspr = (DomainObjectDescriptor)object;
		EntityType type = dspr.getType();
		if(object == null || type != EntityType.POLICY){
			show = false;
		}else{
			IHasId dj = (IHasId) PolicyServerProxy.getEntityForDescriptor(dspr);
			IDPolicy policy = (IDPolicy) dj;
			show = (policy.getPolicyExceptions().getPolicies().size())>0;
		}
		return show;
	}
	
	/**
	 * @param nextDescriptor
	 * @return
	 */
	private boolean isPolicyFolderEmpty(DomainObjectDescriptor nextDescriptor) {
		String folderName = nextDescriptor.getName();
		Collection<DomainObjectDescriptor> entitiesInFolder = PolicyServerProxy
				.getEntityList(PolicyServerProxy.escape(folderName)
						+ PQLParser.SEPARATOR + "%",
						POLICY_FOLDER_CONTAINED_ENTITY_TYPES);

		return ((entitiesInFolder == null) || (entitiesInFolder.isEmpty()));
	}
	
	public static boolean checkDomainObjectsStatus(DomainObjectDescriptor domainObjectDescriptor){
		Set <DomainObjectDescriptor> domainObjectList = new HashSet<DomainObjectDescriptor>();
		if(domainObjectDescriptor.getType()==EntityType.POLICY){
		Set <DomainObjectDescriptor> dmj = new HashSet<DomainObjectDescriptor>();
		dmj.add(domainObjectDescriptor);
			domainObjectList.addAll(PolicyHelpers.filterExceptionDependencies(dmj));
			domainObjectList.addAll(dmj);
		}else{
			domainObjectList.add(domainObjectDescriptor);
		}
		boolean canBeDelete = true;
		for (DomainObjectDescriptor descriptor : domainObjectList){
			IHasId domainObject = (IHasId) PolicyServerProxy.getEntityForDescriptor(descriptor);
			canBeDelete &= PolicyServerProxy.canPerformAction(domainObject,DAction.DELETE);
			DevelopmentStatus status = DomainObjectHelper.getStatus(domainObject);
			canBeDelete&= ( isInactive(descriptor)
					|| status == DevelopmentStatus.NEW
					|| status == DevelopmentStatus.EMPTY || status == DevelopmentStatus.DRAFT);	
			if(!canBeDelete){
				return false;
			}
		}
		return true;
	}
	
	private static boolean isInactive (DomainObjectDescriptor descriptor){
		return EntityInformation.getStatus(descriptor).equalsIgnoreCase("Inactive");
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.action.BaseDisableableAction#refreshEnabledState(java.util.Set)
	 */
	@Override
	protected void refreshEnabledState(Set<IPolicyOrComponentData> selectedItems) {
		boolean newState = !selectedItems.isEmpty();

		Iterator<IPolicyOrComponentData> selectedItemsIterator = selectedItems
				.iterator();
		while ((selectedItemsIterator.hasNext()) && (newState)) {
			PolicyOrComponentData nextSelectedItem = (PolicyOrComponentData) selectedItemsIterator
					.next();
			DomainObjectDescriptor domainObjectDescriptor = nextSelectedItem
					.getDescriptor();

			if (domainObjectDescriptor.getType().equals(EntityType.FOLDER)) {
				newState &= isPolicyFolderEmpty(domainObjectDescriptor);
			} else {				
				newState &= checkDomainObjectsStatus(domainObjectDescriptor);
				try {
					DomainObjectUsage domainObjectUsage = nextSelectedItem
							.getEntityUsage();
					newState &= (!domainObjectUsage.hasReferringObjects());
					newState &= (domainObjectUsage
							.getCurrentlydeployedvcersion() == null);
					newState &= (!domainObjectUsage.hasFuturedeployments());
				} catch (PolicyEditorException exception) {
					LoggingUtil
							.logWarning(
									Activator.ID,
									"Failed to load domain object usage.  Delete menu item may be improperly disabled.",
									exception);
					newState = false;
				}
			}
		}

		setEnabled(newState);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_TOOL_DELETE);
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_TOOL_DELETE_DISABLED);
	}
}
