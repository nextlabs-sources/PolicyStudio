/*
 * Created on May 10, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/IClipboardEnabled.java#1 $:
 */

public interface IClipboardEnabled {

	/**
	 * Copy selection to clipboard
	 */
	public void copy();

	/**
	 * Cut selection to clipboard
	 */
	public void cut();

	/**
	 * Paste selection from clipboard
	 */
	public void paste();
}
