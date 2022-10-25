package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.PluginUtil;

public class PreviewPanelFactory {
	public static Class<?> getPreviewPanelClass(String componentContext) {
		IConfigurationElement preview = PluginUtil
				.getPreviewPluginForContext(componentContext);
		if (preview == null) {
			return null;
		}
		String context = preview.getAttribute("context");
		if (context.equals(componentContext)) {
			String panel = preview.getAttribute("class");
			String contributor = preview.getContributor().getName();
			Bundle bundle = Platform.getBundle(contributor);
			try {
				Class<?> myClass = bundle.loadClass(panel);
				return myClass;
			} catch (IllegalArgumentException e) {
				LoggingUtil.logError(Activator.ID, "error", e);
				return null;
			} catch (ClassNotFoundException e) {
				LoggingUtil.logError(Activator.ID, "error", e);
				return null;
			}
		}
		return null;
	}
}
