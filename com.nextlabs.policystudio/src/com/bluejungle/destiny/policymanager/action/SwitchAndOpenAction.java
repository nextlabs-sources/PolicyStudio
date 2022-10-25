/*
 * Created on Jun 19, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import org.eclipse.jface.action.Action;

import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/D_Plugins/com.nextlabs.policystudio/src/com
 *          /bluejungle/destiny/policymanager/action/SwitchAndOpenAction.java#1
 *          $
 */

public class SwitchAndOpenAction extends Action {
	private DomainObjectDescriptor descriptor;

	/**
	 * Constructor
	 * 
	 * @param text
	 */
	public SwitchAndOpenAction(String text) {
		super(text);
	}

	@Override
	public void run() {
		SwitchPerspectiveAction action = PolicyStudioActionFactory.SWITCH_TO_POLICY_AUTHOR_ACTION;
		action.run();

		GlobalState.getInstance().forceLoadObjectInEditorPanel(descriptor);
	}

	public void setDomainObjectDescriptor(DomainObjectDescriptor descriptor) {
		this.descriptor = descriptor;
	}
}
