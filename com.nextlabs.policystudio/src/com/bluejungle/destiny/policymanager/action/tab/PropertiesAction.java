/*
 * Created on Jun 22, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action.tab;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.bluejungle.destiny.policymanager.action.ActionMessages;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.tab.AbstractTab;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/D_Plugins/com.nextlabs.policystudio/src/com
 *          /bluejungle/destiny/policymanager/action/PropertiesAction.java#1 $
 */

public class PropertiesAction extends Action {

	private AbstractTab tab;

	public PropertiesAction(AbstractTab tab) {
		this.tab = tab;
	}

	@Override
	public String getText() {
		return ActionMessages.ACTION_PROPERTIES;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(ImageBundle.PROPERTIES_IMG);
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return ImageDescriptor
				.createFromImage(ImageBundle.PROPERTIES_DISABLED_IMG);
	}

	@Override
	public void run() {
		tab.setFolderProperties();
	}
}
