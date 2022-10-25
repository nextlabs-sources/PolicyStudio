/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.ui.dialogs.DeploymentHistoryDialog;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/action/CheckDependenciesAction.
 *          java#2 $
 */

public class DeploymentHistoryAction extends Action {

	/**
	 * Constructor
	 * 
	 */
	public DeploymentHistoryAction() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 */
	public DeploymentHistoryAction(String text) {
		super(text);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param image
	 */
	public DeploymentHistoryAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param style
	 */
	public DeploymentHistoryAction(String text, int style) {
		super(text, style);
	}

	@Override
	public void run() {
		DeploymentHistoryDialog dlg = new DeploymentHistoryDialog(Display
				.getCurrent().getActiveShell());
		dlg.open();
	}
}