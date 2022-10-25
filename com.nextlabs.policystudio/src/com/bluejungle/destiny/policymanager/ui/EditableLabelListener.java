/*
 * Created on Feb 25, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.swt.internal.SWTEventListener;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates
 *          .xml#2 $:
 */

@SuppressWarnings("restriction")
public interface EditableLabelListener extends SWTEventListener {

	public void mouseUp(EditableLabelEvent e);

	public void mouseDown(EditableLabelEvent e);

	public void mouseRightClick(EditableLabelEvent e);

	public void textChanged(EditableLabelEvent e);

	public boolean textSaved(EditableLabelEvent e);

	public boolean startEditing(EditableLabelEvent e);

	public void upArrow(EditableLabelEvent e);

	public void downArrow(EditableLabelEvent e);

	public void delete(EditableLabelEvent e);

	/**
	 * @param e
	 */
	public void fireCancelEditing(EditableLabelEvent e);
}