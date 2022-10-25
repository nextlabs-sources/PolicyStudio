/*
 * Created on Jan 25, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.bluejungle.destiny.policymanager.Activator;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/util/LoggingUtil.java#1 $
 */

public class LoggingUtil {

	public static void logWarning(String pluginId, String message,
			Throwable exception) {
		log(IStatus.WARNING, pluginId, IStatus.WARNING, message, exception);
	}

	/**
	 * Log the specified information.
	 * 
	 * @param message
	 *            , a human-readable message, localized to the current locale.
	 */
	public static void logInfo(String pluginId, String message,
			Throwable exception) {
		log(IStatus.INFO, pluginId, IStatus.INFO, message, exception);
	}

	/**
	 * Log the specified error.
	 * 
	 * @param message
	 *            , a human-readable message, localized to the current locale.
	 * @param exception
	 *            , a low-level exception, or <code>null</code> if not
	 *            applicable.
	 */
	public static void logError(String pluginId, String message,
			Throwable exception) {
		log(IStatus.ERROR, pluginId, IStatus.ERROR, message, exception);
	}

	/**
	 * Log the specified information.
	 * 
	 * @param severity
	 *            , the severity; one of the following: <code>IStatus.OK</code>,
	 *            <code>IStatus.ERROR</code>, <code>IStatus.INFO</code>, or
	 *            <code>IStatus.WARNING</code>.
	 * @param pluginId
	 *            . the unique identifier of the relevant plug-in.
	 * @param code
	 *            , the plug-in-specific status code, or <code>OK</code>.
	 * @param message
	 *            , a human-readable message, localized to the current locale.
	 * @param exception
	 *            , a low-level exception, or <code>null</code> if not
	 *            applicable.
	 */
	public static void log(int severity, String pluginId, int code,
			String message, Throwable exception) {
		log(createStatus(severity, pluginId, code, message, exception));
	}

	/**
	 * Create a status object representing the specified information.
	 * 
	 * @param severity
	 *            , the severity; one of the following: <code>IStatus.OK</code>,
	 *            <code>IStatus.ERROR</code>, <code>IStatus.INFO</code>, or
	 *            <code>IStatus.WARNING</code>.
	 * @param pluginId
	 *            , the unique identifier of the relevant plug-in.
	 * @param code
	 *            , the plug-in-specific status code, or <code>OK</code>.
	 * @param message
	 *            , a human-readable message, localized to the current locale.
	 * @param exception
	 *            , a low-level exception, or <code>null</code> if not
	 *            applicable.
	 * @return, the status object (not <code>null</code>).
	 */
	public static IStatus createStatus(int severity, String pluginId, int code,
			String message, Throwable exception) {
		return new Status(severity, pluginId, code, message, exception);
	}

	/**
	 * Log the given status.
	 * 
	 * @param status
	 *            , the status to log.
	 */
	public static void log(IStatus status) {
		Activator.getDefault().getLog().log(status);
	}

	public static void log(String message) {
		logInfo(Activator.ID, message, null);
	}
}
