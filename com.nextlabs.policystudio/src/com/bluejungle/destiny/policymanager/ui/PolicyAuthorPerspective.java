/*
 * Created on Sep 29, 2004
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

/**
 * @author fuad
 * 
 */
public class PolicyAuthorPerspective implements IPerspectiveFactory {

	public static final String ID = "com.bluejungle.destiny.policymanager.ui.PolicyAuthorPerspective"; //$NON-NLS-1$

	public PolicyAuthorPerspective() {
	}

	public void createInitialLayout(IPageLayout layout) {
		IViewLayout viewlayout;

		layout.setEditorAreaVisible(true);

		layout.addStandaloneView(PolicyAuthorView.ID_VIEW, false,
				IPageLayout.LEFT, .3f, layout.getEditorArea());
		if ((viewlayout = layout.getViewLayout(PolicyAuthorView.ID_VIEW)) != null) {
			viewlayout.setCloseable(false);
		}

		layout.addStandaloneView(
				"com.bluejungle.destiny.policymanager.ui.StatusPart", false,
				IPageLayout.BOTTOM, .94f, layout.getEditorArea());
		if ((viewlayout = layout
				.getViewLayout("com.bluejungle.destiny.policymanager.ui.StatusPart")) != null) {
			viewlayout.setCloseable(false);
		}

		layout.addPlaceholder(
				"com.bluejungle.destiny.policymanager.ui.PreviewView",
				IPageLayout.RIGHT, .70f, layout.getEditorArea());
	}
}
