/*
 * Created on Mar 2, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.swt.widgets.Control;

/**
 * @author fuad
 * @version $Id$:
 */

public interface IUndoElement {

	public boolean undo(Object spec, Control control);

	public boolean redo(Object spec, Control control);

	/**
	 * @return true if the undo element is a continuation of the previous undo
	 *         element. if it is true the previous element will be undone along
	 *         with this one.
	 * 
	 */
	public boolean isContinuation();

	/**
	 * @param continuation
	 *            specifies that this undo element is a continuation of the
	 *            previous undo element. if it is true the previous element will
	 *            be undone along with this one.
	 */
	public void setContinuation(boolean continuation);
}