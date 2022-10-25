/*
 * Created on Apr 7, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl;

/**
 * ILeafObjectSelectionListener is used to account for selection within a leaf
 * object table list. Currently, it's used only for the that the end user double
 * clicks on an item in the list, which must lead to a selection
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/browser/impl/
 *          ILeafObjectSelectionListener.java#1 $
 */

public interface ILeafObjectSelectionListener {

	/**
	 * Respond to the selection event
	 * 
	 * @param event
	 */
	public void onLeafObjectSelection(LeafObjectSelectionEvent event);
}
