/*
 * Created on Feb 6, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.nextlabs.policystudio.editor.device;

import org.eclipse.osgi.util.NLS;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/editor/EditorMessages.java#3 $
 */

public class EditorMessages extends NLS {

	private static final String BUNDLE_NAME = "com.nextlabs.policystudio.editor.device.messages";//$NON-NLS-1$

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, EditorMessages.class);
	}

	private EditorMessages() {
	}

	public static String DEVICES, DEVICE_COMPONENT,
			DEVICELISTPANEL_DEVICE_TITLE, DEVICELISTPANEL_DEVICE_MSG,
			DEVICELISTPANEL_DEVICE_NAME, FIND_INSTRUCTIONS, FIND_STRING,
			COMPONENTEDITOR_MEMBERS;
}
