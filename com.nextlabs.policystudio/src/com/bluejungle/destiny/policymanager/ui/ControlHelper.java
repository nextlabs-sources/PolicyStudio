/*
 * Created on May 10, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.swt.widgets.Control;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/ControlHelper.java#1 $:
 */

public class ControlHelper {

	/**
	 * Constructor
	 * 
	 */
	public ControlHelper() {
		super();
	}

	public static IClipboardEnabled isClipboardEnabled(Control control) {
		Control parent = control;
		while (!(parent instanceof IClipboardEnabled) && parent != null) {
			parent = parent.getParent();
		}
		return ((IClipboardEnabled) parent);
	}
}
