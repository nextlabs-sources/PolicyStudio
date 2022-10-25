/*
 * Created on Feb 25, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.swt.internal.SWTEventObject;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/EditableLabelEvent.java#5
 *          $:
 */

@SuppressWarnings("restriction")
public class EditableLabelEvent extends SWTEventObject {
	private static final long serialVersionUID = -4808648737669818267L;

	public SWTEventObject originalEvent = null;
	public int x = 0;
	public int y = 0;
	public int stateMask;

	/**
	 * Constructor
	 * 
	 * @param source
	 */
	public EditableLabelEvent(Object source) {
		super(source);
	}

	public SWTEventObject getOriginalEvent() {
		return this.originalEvent;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public boolean isRightMouseButton() {
		return ((org.eclipse.swt.events.MouseEvent) originalEvent).button == 3;
	}
}
