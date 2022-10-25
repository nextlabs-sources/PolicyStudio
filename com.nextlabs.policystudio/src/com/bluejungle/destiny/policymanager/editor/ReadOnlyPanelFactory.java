/*
 * Created on May 20, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.model.PolicyServerHelper;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.PluginUtil;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

/**
 * @author bmeng
 * 
 */
public class ReadOnlyPanelFactory {

	public static boolean hasEditorPanelFor(Object domainObject) {
		if (domainObject instanceof IDPolicy) {
			return true;
		} else if (domainObject instanceof IDSpec) {
			return true;
		}
		return false;
	}

	@SuppressWarnings( { "unchecked" })
	public static Composite getEditorPanel(Composite parent, int style,
			Object domainObject) {
		if (domainObject instanceof IDPolicy) {
			IConfigurationElement defaultElement = PluginUtil
					.getDefaultEditorPluginForPolicy();
			String defaultContext = defaultElement.getAttribute("context");
			IPolicy policy = (IDPolicy) domainObject;
			IConfigurationElement[] decls = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(
							"com.nextlabs.policystudio.readonly");
			IConfigurationElement find = null;
			for (IConfigurationElement element : decls) {
				String context = element.getAttribute("context");
				if (policy.hasAttribute(context)) {
					find = element;
					break;
				}
			}
			if (find == null) {
				find = PluginUtil.getReadOnlyPluginForContext(defaultContext);
			}
			if (find == null) {
				return null;
			}
			String contributor = find.getContributor().getName();
			Bundle bundle = Platform.getBundle(contributor);
			try {
				Class myClass = bundle.loadClass(find.getAttribute("class"));
				Constructor constructor[] = myClass.getConstructors();
				return (Composite) constructor[0].newInstance(new Object[] {
						parent, style, (IDPolicy) domainObject });
			} catch (IllegalArgumentException e) {
				LoggingUtil.logError(Activator.ID, "error", e);
			} catch (InstantiationException e) {
				LoggingUtil.logError(Activator.ID, "error", e);
			} catch (IllegalAccessException e) {
				LoggingUtil.logError(Activator.ID, "error", e);
			} catch (InvocationTargetException e) {
				LoggingUtil.logError(Activator.ID, "error", e);
			} catch (ClassNotFoundException e) {
				LoggingUtil.logError(Activator.ID, "error", e);
			}
		} else if (domainObject instanceof IDSpec) {
			IDSpec spec = (IDSpec) domainObject;
			String type = PolicyServerHelper.getTypeFromComponentName(spec
					.getName());
			IConfigurationElement result = null;
			IConfigurationElement[] decls = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(
							"com.nextlabs.policystudio.readonly");
			for (IConfigurationElement element : decls) {
				String context = element.getAttribute("context");
				if (type.equals(context)) {
					result = element;
					break;
				}
			}
			if (result == null) {
				return null;
			}
			IConfigurationElement editor = PluginUtil
					.getEditorPluginForContext(type);
			if (editor == null) {
				return null;
			}
			String contributor = result.getContributor().getName();
			Bundle bundle = Platform.getBundle(contributor);
			try {
				Class myClass = bundle.loadClass(result.getAttribute("class"));
				Constructor constructor[] = myClass.getConstructors();
				return (Composite) constructor[0].newInstance(new Object[] {
						parent, style, spec,
						PluginUtil.getContextImageForContext(type),
						editor.getAttribute("displayName"), type });
			} catch (IllegalArgumentException e) {
				LoggingUtil.logError(Activator.ID, "error", e);
			} catch (InstantiationException e) {
				LoggingUtil.logError(Activator.ID, "error", e);
			} catch (IllegalAccessException e) {
				LoggingUtil.logError(Activator.ID, "error", e);
			} catch (InvocationTargetException e) {
				LoggingUtil.logError(Activator.ID, "error", e);
			} catch (ClassNotFoundException e) {
				LoggingUtil.logError(Activator.ID, "error", e);
			}
		}
		return null;
	}
}
