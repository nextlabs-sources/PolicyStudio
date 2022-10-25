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
 *          /bluejungle/destiny/policymanager/action/DeleteFolderAction.java#1 $
 */

public class DeleteFolderAction extends Action {

	private AbstractTab tab;

	public DeleteFolderAction(AbstractTab tab) {
		this.tab = tab;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(ImageBundle.DELETE_FOLDER_IMG);
	}

	@Override
	public String getText() {
		return ActionMessages.ACTION_DELETE_FOLDER;
	}

	@Override
	public void run() {
		tab.deleteFolder();
	}
}
