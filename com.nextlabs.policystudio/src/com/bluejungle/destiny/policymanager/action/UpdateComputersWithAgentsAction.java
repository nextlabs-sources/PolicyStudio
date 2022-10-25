/*
 * Created on Nov 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.ui.dialogs.UpdateComputersWithAgentsDialog;

/**
 * Action to Update the list of computers with agents in the Policy Database
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/action/
 *          UpdateComputersWithAgentsAction.java#2 $
 */
public class UpdateComputersWithAgentsAction extends Action {

	/**
	 * Create an instance of UpdateComputersWithAgentsAction
	 * 
	 * @param text
	 */
	public UpdateComputersWithAgentsAction(String text) {
		super(text);
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		UpdateComputersWithAgentsDialog dlg = new UpdateComputersWithAgentsDialog(
				Display.getCurrent().getActiveShell());
		dlg.open();
	}
}
