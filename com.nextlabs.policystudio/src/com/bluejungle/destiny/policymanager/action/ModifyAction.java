/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;

import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyManagerView;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/action/CheckDependenciesAction.
 *          java#2 $
 */

public class ModifyAction extends BaseDisableableAction {

	private static final IEventManager EVENT_MANAGER;
	static {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		EVENT_MANAGER = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
	}

	/**
	 * Constructor
	 * 
	 */
	public ModifyAction() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 */
	public ModifyAction(String text) {
		super(text);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param image
	 */
	public ModifyAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param style
	 */
	public ModifyAction(String text, int style) {
		super(text, style);
	}

	@Override
	public void run() {
		GlobalState gs = GlobalState.getInstance();
		Boolean inPolicyAuthorView = PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION.isEnabled();
		IHasId domainObject = null;
		Set<DomainObjectDescriptor> selectedItems = gs.getCurrentSelection();
		Iterator<DomainObjectDescriptor> selectedItemsIterator = selectedItems.iterator();
		DomainObjectDescriptor item = (DomainObjectDescriptor) selectedItemsIterator.next();
		domainObject = (IHasId) PolicyServerProxy.getEntityForDescriptor(item);
		DomainObjectHelper.setStatus(domainObject, DevelopmentStatus.DRAFT);
		PolicyServerProxy.saveEntity(domainObject);

		PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent(domainObject);
		EVENT_MANAGER.fireEvent(objectModifiedEvent);
		if (!inPolicyAuthorView){
			PolicyManagerView.refreshCurrentTab();
		}
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.action.BaseDisableableAction#refreshEnabledState()
	 */
	@Override
	protected void refreshEnabledState(Set<IPolicyOrComponentData> selectedItems) {
		boolean newState = false;

		if (selectedItems.size() == 1) {
			IPolicyOrComponentData selectedItem = (IPolicyOrComponentData) selectedItems
					.iterator().next();
			IHasId current = selectedItem.getEntity();
			DevelopmentStatus status = DomainObjectHelper.getStatus(current);
			if (((status == DevelopmentStatus.APPROVED) || (status == DevelopmentStatus.OBSOLETE))
					&& (PolicyServerProxy.canPerformAction(current,
							DAction.WRITE))) {
				newState = true;
			}
		}

		setEnabled(newState);
	}
}