/*
 * Created on Mar 8, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.controls;

import org.eclipse.swt.internal.SWTEventObject;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/ui/controls/FilterControlEvent.
 *          java#4 $:
 */

@SuppressWarnings("restriction")
public class FilterControlEvent extends SWTEventObject {
	private static final long serialVersionUID = 5283470198554600706L;

	public SWTEventObject originalEvent = null;

	/**
	 * Constructor
	 * 
	 * @param source
	 */
	public FilterControlEvent(Object source) {
		super(source);
	}

	public SWTEventObject getOriginalEvent() {
		return this.originalEvent;
	}

	public void setOriginalEvent(SWTEventObject originalEvent) {
		this.originalEvent = originalEvent;
	}
}
