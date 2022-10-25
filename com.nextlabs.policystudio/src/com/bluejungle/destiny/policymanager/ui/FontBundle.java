/*
 * Created on Mar 17, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.util.ResourceManager;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/FontBundle.java#7 $:
 */

public class FontBundle {
	public static final Font DEFAULT_NORMAL = ResourceManager.getFont(Display
			.getDefault().getSystemFont().getFontData()[0], false, false);
	public static final Font DEFAULT_BOLD = ResourceManager
			.getBoldFont(DEFAULT_NORMAL);
	public static final Font DEFAULT_UNDERLINE = ResourceManager.getFont(
			Display.getDefault().getSystemFont().getFontData()[0], false, true);

	public static final Font ARIAL_9_NORMAL = ResourceManager.getFont("Arial",
			9, SWT.NONE);
	public static final Font ARIAL_9_ITALIC = ResourceManager.getFont("Arial",
			9, SWT.ITALIC);
	public static final Font ARIAL_9_BOLD = ResourceManager.getFont("Arial", 9,
			SWT.BOLD);
	public static final Font ARIAL_9_UNDERLINE = ResourceManager.getFont(
			"Arial", 9, SWT.NONE, false, true);

	public static final Font ARIAL_12_NORMAL = ResourceManager.getFont("Arial",
			12, SWT.NONE);
	public static final Font ARIAL_16_NORMAL = ResourceManager.getFont("Arial",
			16, SWT.BOLD);
	public static final Font ARIAL_18_NORMAL = ResourceManager.getFont("Arial",
			18, SWT.BOLD);
}
