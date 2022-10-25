/*
 * Created on Jan 17, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager;

import org.eclipse.osgi.util.NLS;

/**
 * @author bmeng
 */

public class ConfigurableMessages extends NLS {

	private static final String BUNDLE_NAME = "com.bluejungle.destiny.policymanager.messages";//$NON-NLS-1$

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ConfigurableMessages.class);
	}

	private ConfigurableMessages() {
	}

	// General message
	public static String LOGINDIALOG_WINDOW_TITLE, ABOUTPART_COPYRIGHT,ABOUTPART_COPYRIGHT_line2, ABOUTPART_COPYRIGHT_line3,
			ABOUTPART_APPLICATION_NAME;

	public static String POLICYAUTHOR_TITLE, POLICYMANAGER_TITLE;
}
