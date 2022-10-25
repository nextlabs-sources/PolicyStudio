/*
 * Created on May 20, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;

import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyAuthorView;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.PolicyFolder;

/**
 * @author dstarke
 * 
 */
public class ShowPolicyUsageAction extends BaseDisableableAction {

	/**
	 * Constructor
	 * 
	 */
	public ShowPolicyUsageAction() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 */
	public ShowPolicyUsageAction(String text) {
		super(text);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param image
	 */
	public ShowPolicyUsageAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param style
	 */
	public ShowPolicyUsageAction(String text, int style) {
		super(text, style);
	}

	@Override
	public void run() {
		GlobalState gs = GlobalState.getInstance();
		Boolean inPolicyAuthorView = PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION.isEnabled();
		IHasId domainObject = null;
		if (inPolicyAuthorView){
			domainObject = (IHasId) gs.getCurrentObject();
		}else{
			Set<DomainObjectDescriptor> selectedItems = gs.getCurrentSelection();
			Iterator<DomainObjectDescriptor> selectedItemsIterator = selectedItems.iterator();
			DomainObjectDescriptor item = (DomainObjectDescriptor) selectedItemsIterator.next();
			domainObject = (IHasId) PolicyServerProxy.getEntityForDescriptor(item);
		}
		
		String name = ((IDSpec) domainObject).getName();
		EntityType entityType = EntityType.COMPONENT;

		((PolicyAuthorView) gs.getView()).setListView(EntityType.POLICY);
		gs.getPolicyListPanel().setupPolicyUsageFilterControl(name, entityType);
	}

	@Override
	public void refreshEnabledState(Set<IPolicyOrComponentData> selectedItems) {
		boolean newState = false;
		boolean inPolicyAuthorView = PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION.isEnabled();
		if (selectedItems.size() == 1 && inPolicyAuthorView) {
			IPolicyOrComponentData selectedItem = (IPolicyOrComponentData) selectedItems
					.iterator().next();
			IHasId selectedEntity = selectedItem.getEntity();
			newState = (!(selectedEntity instanceof IDPolicy) && !(selectedEntity instanceof PolicyFolder));
		}

		setEnabled(newState);
	}

}
