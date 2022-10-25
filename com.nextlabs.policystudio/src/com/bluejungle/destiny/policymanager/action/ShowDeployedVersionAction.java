/*
 * Created on May 20, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.ShowDeployedVersionDialog;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;

/**
 * @author dstarke
 * 
 */
public class ShowDeployedVersionAction extends BaseDisableableAction {

	/**
	 * Constructor
	 * 
	 */
	public ShowDeployedVersionAction() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 */
	public ShowDeployedVersionAction(String text) {
		super(text);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param image
	 */
	public ShowDeployedVersionAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param style
	 */
	public ShowDeployedVersionAction(String text, int style) {
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

		String name;
		DODDigest desc;
		if (domainObject instanceof IDPolicy) {
			name = ((IDPolicy) domainObject).getName();
			desc = EntityInfoProvider.getPolicyDescriptor(name);
		} else {
			name = ((IDSpec) domainObject).getName();
			desc = EntityInfoProvider.getComponentDescriptor(name);
		}

		DomainObjectDescriptor descriptor = null;
		try {
			descriptor = PolicyServerProxy.getDescriptorById(desc.getId());
		} catch (PolicyEditorException e) {
			e.printStackTrace();
			return;
		}
		Object tmp = PolicyServerProxy.getDeployedVersion(descriptor);
		if (tmp == null) {
			MessageDialog.openInformation(
					Display.getCurrent().getActiveShell(),
					ActionMessages.SHOWDEPLOYEDVERSIONACTION_NO_DEPLOYED,
					ActionMessages.SHOWDEPLOYEDVERSIONACTION_NO_DEPLOYED_MSG);
			return;
		}
		ShowDeployedVersionDialog window = new ShowDeployedVersionDialog(
				Display.getCurrent().getActiveShell(), descriptor);
		window.open();
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.action.BaseDisableableAction#refreshEnabledState()
	 */
	@Override
	protected void refreshEnabledState(Set<IPolicyOrComponentData> selectedItems) {
		IHasId currentObject = (IHasId) GlobalState.getInstance()
				.getCurrentObject();
		if (currentObject == null) {
			setEnabled(false);
			return;
		}
		DODDigest descriptor;
		if (currentObject instanceof IDPolicy) {
			descriptor = EntityInfoProvider
					.getPolicyDescriptor(((IDPolicy) currentObject).getName());
		} else {
			descriptor = EntityInfoProvider
					.getComponentDescriptor(((IDSpec) currentObject).getName());
		}
		DomainObjectDescriptor des = null;
		try {
			des = PolicyServerProxy.getDescriptorById(descriptor.getId());
		} catch (PolicyEditorException e) {
			e.printStackTrace();
			return;
		}
		boolean enabled = descriptor != null
				&& PolicyServerProxy.getDeployedVersion(des) != null;
		setEnabled(enabled);
	}
}
