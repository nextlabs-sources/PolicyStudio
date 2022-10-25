/*
 * Created on Nov 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.util;

import java.io.IOException;
import java.net.URL;

import org.apache.axis.AxisFault;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.UserProfileEnum;
import com.bluejungle.destiny.policymanager.ui.dialogs.DialogMessages;

/**
 * Utilites for interacting with the Eclipse Platform
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/PlatformUtils.java#1 $
 */

public class PlatformUtils {

	/**
	 * Find a resource relative to the plugin install directory
	 * 
	 * @param the
	 *            path of the resource to fine
	 * @throws IOException
	 *             if an error occurs while finding the resource
	 */
	public static URL getResource(String path) throws IOException {
		Bundle pluginBundle = Activator.getDefault().getBundle();
		URL foundURL = pluginBundle.getEntry(path);
		if (foundURL == null) {
			throw new IllegalArgumentException("Resource with path, " + path
					+ ", could not be found.");
		}

		return FileLocator.toFileURL(foundURL);
	}

	public static UserProfileEnum getProfile() {
		IConfigurationElement[] decls = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(
						"com.nextlabs.policystudio.profile");
		for (IConfigurationElement element : decls) {
			String name = element.getAttribute("name");
			if (name.equalsIgnoreCase(UserProfileEnum.CORPORATE.toString()))
				return UserProfileEnum.CORPORATE;
			else if (name.equalsIgnoreCase(UserProfileEnum.FILESYSTEM
					.toString()))
				return UserProfileEnum.FILESYSTEM;
			else if (name.equalsIgnoreCase(UserProfileEnum.PORTAL.toString()))
				return UserProfileEnum.PORTAL;
		}
		return null;
	}

	public static void validCharForName(KeyEvent e) {
		if (e.character == '&' || e.character == '$' || e.character == '*'
				|| e.character == '?' || e.character == '/'
				|| e.character == '\\')
			e.doit = false;
	}

	/**
	 * find the proper width hint for the widget, the minimal width is min and
	 * the increase step is step;
	 * 
	 * @param x
	 * @param min
	 * @param step
	 * @return the width hint
	 */
	public static int findProperControlWidth(int x, int min, int step) {
		int result = min;
		while (result < x) {
			result += step;
		}
		return result;
	}

	public static void exitOnNetworkError(Exception exception) {
		Display display = Display.getCurrent();
		Shell shell = display.getActiveShell();
		if (exception.getCause() instanceof AxisFault) {
			MessageDialog.openError(shell, DialogMessages.GENERAL_ERROR,
					DialogMessages.GENERAL_CONNECTION_ERROR);
			System.exit(-1);
		}
	}
}
