/*
 * Created on Mar 3, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.bluejungle.destiny.policymanager.ui.ControlHelper;
import com.bluejungle.destiny.policymanager.ui.IClipboardEnabled;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates
 *          .xml#2 $:
 */

public class PasteAction extends Action {

	/**
	 * Constructor
	 * 
	 */
	public PasteAction(String text) {
		super(text);
	}

	@Override
	public void run() {
		Control focusControl = Display.getCurrent().getFocusControl();
		IClipboardEnabled clipboardControl = ControlHelper
				.isClipboardEnabled(focusControl);
		if (focusControl instanceof Text) {
			((Text) focusControl).paste();
		} else if (clipboardControl != null) {
			clipboardControl.paste();
		}
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_TOOL_PASTE);
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_TOOL_PASTE_DISABLED);
	}
}