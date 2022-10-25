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
 *          /bluejungle/destiny/policymanager/action/CreateFolderAction.java#1 $
 */

public class CreateFolderAction extends Action {

	private AbstractTab tab;

	public CreateFolderAction(AbstractTab tab) {
		this.tab = tab;
	}

	@Override
	public String getText() {
		return ActionMessages.ACTION_NEW_FOLDER;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(ImageBundle.CREATE_FOLDER_IMG);
	}

	@Override
	public void run() {
		tab.createFolder();
	}
}
