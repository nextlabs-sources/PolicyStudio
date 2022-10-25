/*
 * Created on Mar 3, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.ui.ControlHelper;
import com.bluejungle.destiny.policymanager.ui.IClipboardEnabled;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates
 *          .xml#2 $:
 */

public class CutAction extends Action {

	/**
	 * Constructor
	 * 
	 */
	public CutAction() {
		super();
	}

	@Override
	public void run() {
		Control focusControl = Display.getCurrent().getFocusControl();
		IClipboardEnabled clipboardControl = ControlHelper
				.isClipboardEnabled(focusControl);
		if (focusControl instanceof Text) {
			((Text) focusControl).cut();
		} else if (clipboardControl != null) {
			clipboardControl.cut();
		}

	}

}