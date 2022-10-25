/*
 * Created on Mar 18, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/ui/dialogs/SubjectComponentBrowser
 *          .java#1 $
 */

public class SubjectComponentBrowser extends Dialog {

	private Shell dialogShell = null;

	/**
	 * Create an instance of SubjectComponentBrowser
	 * 
	 * @param parent
	 */
	public SubjectComponentBrowser(Shell parent) {
		super(parent);
		init();
		// TODO Auto-generated constructor stub
	}

	/**
     * 
     */
	private void init() {
		createShell();
		this.dialogShell.open();
	}

	/**
	 * This method initializes sShell
	 */
	private void createShell() {
		dialogShell = new Shell();
		dialogShell.setText("Shell");
		dialogShell.setSize(new Point(300, 200));

	}

}
