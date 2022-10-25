/*
 * Created on Apr 10, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl;

/**
 * SelectableLeafObjectSourceException indicates an exception during information
 * requests from a Selectable Leaf Object Source
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/browser/impl/
 *          SelectableLeafObjectTableModelException.java#1 $
 */

public class SelectableLeafObjectSourceException extends Exception {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -6174940176785793855L;

	/**
	 * 
	 * Create an instance of SelectableLeafObjectSourceException
	 * 
	 * @param message
	 * @param cause
	 */
	public SelectableLeafObjectSourceException(String message, Throwable cause) {
		super(message, cause);
	}

}
