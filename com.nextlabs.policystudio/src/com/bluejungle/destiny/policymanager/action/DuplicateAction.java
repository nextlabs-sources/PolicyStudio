/*
 * Created on Mar 3, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.DuplicateDialog;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author bmeng
 */

public class DuplicateAction extends BaseDisableableAction {
	public static String VIEW;
	public static EntityType TYPE;
	public static List<DomainObjectDescriptor> SOURCES = new ArrayList<DomainObjectDescriptor>();
	private boolean policiesAreAllowed = PolicyServerProxy.getAllowedEntityTypes().contains(EntityType.POLICY);
	
	/**
	 * Constructor
	 * 
	 */
	public DuplicateAction() {
		setEnabled(false);
	}

	@Override
	public String getText() {
		return ActionMessages.ACTION_DUPLICATE;
	}

	@Override
	public void run() {
		if (VIEW == null) {
			return;
		}
		if (SOURCES.size() == 0) {
			return;
		}
		// validation check
		if (PolicyStudioActionFactory.SWITCH_TO_POLICY_AUTHOR_ACTION
				.isEnabled()
				&& VIEW.equals("author")) {
			return;
		}
		if (PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION
				.isEnabled()
				&& VIEW.equals("manager")) {
			return;
		}

		if (PolicyStudioActionFactory.getDuplicateAction().isEnabled()) {
			Shell shell = Display.getCurrent().getActiveShell();

			DuplicateDialog dialog = new DuplicateDialog(shell);
			dialog.setView(VIEW);
			dialog.setType(TYPE);
			dialog.setSources(SOURCES);
			dialog.open();
		}
	}
	
	@Override
	public void refreshEnabledState(Set<IPolicyOrComponentData> selectedItems) {
		boolean newState = !selectedItems.isEmpty();
		newState = policiesAreAllowed;
		setEnabled(newState);
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return ImageDescriptor
				.createFromImage(ImageBundle.DUPLICATE_DISABLED_IMG);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(ImageBundle.DUPLICATE_IMG);
	}
}