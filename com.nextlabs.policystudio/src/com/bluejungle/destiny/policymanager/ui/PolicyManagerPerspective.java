/*
 * Created on Jun 19, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/D_Plugins/com.nextlabs.policystudio/src/com
 *          /bluejungle/destiny/policymanager/ui/PolicyManagerPerspective.java#1
 *          $
 */

public class PolicyManagerPerspective implements IPerspectiveFactory {

	public static final String ID = "com.bluejungle.destiny.policymanager.ui.PolicyManagerPerspective"; //$NON-NLS-1$

	public PolicyManagerPerspective() {
	}

	public void createInitialLayout(IPageLayout layout) {
		IViewLayout viewlayout;

		layout.setEditorAreaVisible(false);

		layout.addStandaloneView(PolicyManagerView.ID, false, IPageLayout.LEFT,
				1f, layout.getEditorArea());
		if ((viewlayout = layout.getViewLayout(PolicyManagerView.ID)) != null) {
			viewlayout.setCloseable(false);
		}
	}
}
