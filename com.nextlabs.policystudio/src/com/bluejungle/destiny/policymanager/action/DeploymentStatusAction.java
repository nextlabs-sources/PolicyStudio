/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.ui.dialogs.DeploymentStatusDialog;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/action/CheckDependenciesAction.
 *          java#2 $
 */

public class DeploymentStatusAction extends BaseDisableableAction {

	/**
	 * Constructor
	 * 
	 */
	public DeploymentStatusAction() {
		super();
		setEnabled(true);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 */
	public DeploymentStatusAction(String text) {
		super(text);
		setEnabled(true);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param image
	 */
	public DeploymentStatusAction(String text, ImageDescriptor image) {
		super(text, image);
		setEnabled(true);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param style
	 */
	public DeploymentStatusAction(String text, int style) {
		super(text, style);
		setEnabled(true);
	}

	@Override
	protected void refreshEnabledState(Set<IPolicyOrComponentData> selectedItems) {
		setEnabled(true);
	}

	@Override
	public void run() {
		DeploymentStatusDialog dlg = new DeploymentStatusDialog(Display
				.getCurrent().getActiveShell());
		dlg.open();
	}
}