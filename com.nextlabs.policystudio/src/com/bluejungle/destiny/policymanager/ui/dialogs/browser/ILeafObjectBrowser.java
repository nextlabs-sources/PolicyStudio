/*
 * Created on Apr 4, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser;

import java.util.List;

/**
 * Interface for a Leaf Object Browser. Mostly a marker interface. Includes two
 * methods to run the browser (one which specifies the location of the dialog
 * and one which doesn't
 * 
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/browser/
 *          ILeafObjectBrowser.java#2 $
 */

public interface ILeafObjectBrowser {

	/**
	 * @return a List of leaf objects selected through the browser
	 */
	@SuppressWarnings("unchecked")
	List getItemsToReturn();

	/**
	 * Run this leaf object browser, specifying the start position at which the
	 * browser should open on the client's display. The items selected in the
	 * browser will be returned from this method when the process is complete
	 */
	int open();
}
