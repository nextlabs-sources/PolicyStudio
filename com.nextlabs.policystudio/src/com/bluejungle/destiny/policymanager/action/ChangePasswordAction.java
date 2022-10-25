/*
 * Created on Sep 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;

import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.ChangePasswordDialog;

/**
 * @author sasha
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/action/ChangePasswordAction.java#1
 *          $:
 */

public class ChangePasswordAction extends Action {

	/**
	 * Constructor
	 * 
	 */
	public ChangePasswordAction() {
		super();
		setupListeners();
	}

	/**
	 * Constructor
	 * 
	 * @param arg0
	 */
	public ChangePasswordAction(String arg0) {
		super(arg0);
		setupListeners();
	}

	/**
	 * Constructor
	 * 
	 * @param arg0
	 * @param arg1
	 */
	public ChangePasswordAction(String arg0, int arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructor
	 * 
	 * @param arg0
	 * @param arg1
	 */
	public ChangePasswordAction(String arg0, ImageDescriptor arg1) {
		super(arg0, arg1);
	}

	@Override
	public void run() {
		ChangePasswordDialog dialog = new ChangePasswordDialog(Display
				.getCurrent().getActiveShell());
		dialog.open();
	}

	/**
	 * Set up event listeners for this action
	 */
	private void setupListeners() {
		GlobalState.getInstance().addPartObserver(
				new GlobalState.IPartObserver() {

					/**
					 * @see com.bluejungle.destiny.policymanager.ui.GlobalState.IPartObserver#partClosed(org.eclipse.ui.IWorkbenchPart)
					 */
					public void partClosed(IWorkbenchPart aPart) {

					}

					/**
					 * @see com.bluejungle.destiny.policymanager.ui.GlobalState.IPartObserver#partOpened(org.eclipse.ui.IWorkbenchPart)
					 */
					public void partOpened(IWorkbenchPart aPart) {
					}

					/**
					 * @see com.bluejungle.destiny.policymanager.ui.GlobalState.IPartObserver#workbenchInitialized()
					 */
					public void workbenchInitialized() {
						if (!PolicyServerProxy.canChangePassword()) {
							ChangePasswordAction.this.setEnabled(false);
						} else {
							ChangePasswordAction.this.setEnabled(true);
						}

					}
				});
	}
}
