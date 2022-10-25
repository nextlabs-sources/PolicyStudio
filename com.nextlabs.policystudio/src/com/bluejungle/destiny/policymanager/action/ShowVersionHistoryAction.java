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
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.VersionHistoryDialog;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/action/CheckDependenciesAction.
 *          java#2 $
 */

public class ShowVersionHistoryAction extends BaseDisableableAction {

	/**
	 * Constructor
	 * 
	 */
	public ShowVersionHistoryAction() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 */
	public ShowVersionHistoryAction(String text) {
		super(text);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param image
	 */
	public ShowVersionHistoryAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param style
	 */
	public ShowVersionHistoryAction(String text, int style) {
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

		if (domainObject == null) {
			return;
		}

		VersionHistoryDialog dlg = new VersionHistoryDialog(Display
				.getCurrent().getActiveShell(), domainObject);
		dlg.open();
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
			try {
				DomainObjectUsage domainObjectUsage = selectedItem
						.getEntityUsage();
				newState = domainObjectUsage.hasBeenDeployed();
			} catch (PolicyEditorException exception) {
				LoggingUtil
						.logWarning(
								Activator.ID,
								"Failed to load domain object usage.  ShowVersionHistory menu item may be improperly disabled.",
								exception);
			}
		}

		setEnabled(newState);
	}
}