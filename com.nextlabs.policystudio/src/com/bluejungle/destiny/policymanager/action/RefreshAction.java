/*
 * Created on Jun 22, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyManagerView;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/D_Plugins/com.nextlabs.policystudio/src/com
 *          /bluejungle/destiny/policymanager/action/RefreshAction.java#1 $
 */

public class RefreshAction extends Action {
	public RefreshAction() {
	}

	@Override
	public String getText() {
		return ActionMessages.ACTION_REFRESH;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(ImageBundle.REFRESH_IMG);
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return ImageDescriptor.createFromImage(ImageBundle.REFRESH_IMG);
	}

	@Override
	public void run() {
		PolicyManagerView.refreshCurrentTab();
	}
}
