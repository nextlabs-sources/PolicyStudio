/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.SetDeploymentTargetDialog;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/action/SetTargetsAction.java#5
 *          $
 */

public class SetTargetsAction extends BaseDisableableAction {

	/**
	 * Constructor
	 * 
	 */
	public SetTargetsAction() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 */
	public SetTargetsAction(String text) {
		super(text);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param image
	 */
	public SetTargetsAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param style
	 */
	public SetTargetsAction(String text, int style) {
		super(text, style);
	}

	@Override
	public void run() {
		Boolean inPolicyAuthorView = PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION.isEnabled();
		IHasId current = null;
		if (inPolicyAuthorView){
			current = (IHasId) GlobalState.getInstance().getCurrentObject();
		}else{
			Set<DomainObjectDescriptor> selectedItems = GlobalState.getInstance().getCurrentSelection();
			Iterator<DomainObjectDescriptor> selectedItemsIterator = selectedItems.iterator();
			DomainObjectDescriptor item = (DomainObjectDescriptor) selectedItemsIterator.next();
			current = (IHasId) PolicyServerProxy.getEntityForDescriptor(item);
		}
		if (canSetTargets(current)) {
			List<IHasId> policyList = new ArrayList<IHasId>();
			policyList.add(current);
			SetDeploymentTargetDialog dlg = new SetDeploymentTargetDialog(
					Display.getCurrent().getActiveShell(), policyList);
			dlg.open();
		}
	}

	@Override
	public void refreshEnabledState(Set<IPolicyOrComponentData> selectedItems) {
		boolean newState = false;
		if (selectedItems.size() == 1) {
			IPolicyOrComponentData selectedItem = (IPolicyOrComponentData) selectedItems
					.iterator().next();
			IHasId selectedEntity = selectedItem.getEntity();
			newState = canSetTargets(selectedEntity);
		}

		setEnabled(newState);
	}

	/**
	 * Determine if targets can be set on the specified objects
	 * 
	 * @param current
	 *            the object to test
	 * @return true if targets can be set; false otherwise
	 */
	private boolean canSetTargets(IHasId current) {
		boolean valueToReturn = false;

		if ((current != null)
				&& (DomainObjectHelper.getObjectType(current) == ApplicationMessages.DOMAINOBJECTHELPER_POLICY_TYPE)
				&& (PolicyServerProxy.canPerformAction(current, DAction.DEPLOY))) {
			valueToReturn = true;
		}

		return valueToReturn;
	}
}