/*
 * Created on Apr 7, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl;

import com.bluejungle.pf.destiny.lib.LeafObject;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/browser/impl/
 *          LeafObjectSelectionEvent.java#1 $
 */

public class LeafObjectSelectionEvent {

	private LeafObject leafObjectSelected;

	/**
	 * Create an instance of LeafObjectSelectionEvent
	 * 
	 * @param leafObjectSelected
	 */
	public LeafObjectSelectionEvent(LeafObject leafObjectSelected) {
		super();
		// TODO Auto-generated constructor stub
		this.leafObjectSelected = leafObjectSelected;
	}

	/**
	 * Retrieve the leafObjectSelected.
	 * 
	 * @return the leafObjectSelected.
	 */
	public LeafObject getLeafObjectSelected() {
		return this.leafObjectSelected;
	}
}
