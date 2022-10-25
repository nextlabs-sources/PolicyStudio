package com.bluejungle.destiny.policymanager.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.PlugInConstant;
import com.bluejungle.destiny.policymanager.model.PolicyServerHelper;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;

public class PluginUtil {
	/**
	 * get the component context type list
	 * 
	 * @return list of string, for example, [USER, ACTION]
	 */
	public static List<String> getComponentTypeList() {
		IConfigurationElement[] editors = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(PlugInConstant.EDITOR_PLUGIN);
		List<String> result = new ArrayList<String>();
		for (IConfigurationElement editor : editors) {
			String context = editor.getAttribute(PlugInConstant.CONTEXT);
			String type = editor.getAttribute(PlugInConstant.TYPE);
			if (type.equals("COMPONENT")) {
				result.add(context);
			}
		}
		return result;
	}

	/**
	 * get the image for component
	 * 
	 * @param context
	 * @return default image if nothing found, or the image defined in the
	 *         plug-in
	 */
	public static Image getContextImageForContext(String context) {
		IConfigurationElement pluginEditor = getEditorPluginForContext(context);
		if (pluginEditor == null) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		}
		String contributor = pluginEditor.getContributor().getName();
		Bundle bundle = Platform.getBundle(contributor);
		String icon = pluginEditor.getAttribute(PlugInConstant.ICON);
		URL urlEntry = bundle.getEntry(icon);
		String path;
		try {
			path = FileLocator.toFileURL(urlEntry).getPath();
			return ResourceManager.getImage(path);
		} catch (IOException e) {
			LoggingUtil.logError(Activator.ID, "error", e);
		}
		return PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJ_ELEMENT);
	}

	/**
	 * get the default policy editor definition
	 * 
	 * @return null if nothing matches, or the IConfigurationElement
	 */
	public static IConfigurationElement getDefaultEditorPluginForPolicy() {
		IConfigurationElement result = null;
		IConfigurationElement[] editors = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(PlugInConstant.EDITOR_PLUGIN);
		for (IConfigurationElement editor : editors) {
			String pluginContext = editor.getAttribute(PlugInConstant.DEFAULT);
			if (pluginContext != null) {
				result = editor;
				break;
			}
		}
		return result;
	}

	/**
	 * get the display name for the context
	 * 
	 * @param context
	 * @return unknown if the definition is not found
	 */
	public static String getDisplayNameForContext(String context) {
		IConfigurationElement pluginEditor = getEditorPluginForContext(context);
		if (pluginEditor == null) {
			return PlugInConstant.UNKNOWN;
		}
		String displayName = pluginEditor
				.getAttribute(PlugInConstant.DISPLAY_NAME);
		if (displayName == null) {
			return PlugInConstant.UNKNOWN;
		}
		return displayName;
	}

	/**
	 * get the IConfigurationElement for the domain object
	 * 
	 * @param object
	 * @return null if nothing found, or the element
	 */
	public static IConfigurationElement getEditorPluginForDomainObject(
			Object object) {
		IConfigurationElement[] editors = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(PlugInConstant.EDITOR_PLUGIN);
		for (IConfigurationElement editor : editors) {
			String context = editor.getAttribute(PlugInConstant.CONTEXT);
			String type = editor.getAttribute(PlugInConstant.TYPE);
			if (object instanceof IDPolicy
					&& type.equals(PlugInConstant.POLICY)) {
				IDPolicy policy = (IDPolicy) object;
				if (policy.hasAttribute(context)) {
					return editor;
				}
			}
			if (object instanceof IDSpec
					&& type.equals(PlugInConstant.COMPONENT)) {
				IDSpec spec = (IDSpec) object;
				String componentType = PolicyServerHelper
						.getTypeFromComponentName(spec.getName());
				if (componentType.equals(context)) {
					return editor;
				}
			}
		}
		return null;
	}

	/**
	 * get the editor plug-in element for the context
	 * 
	 * @param context
	 * @return null if nothing found, or the element
	 */
	public static IConfigurationElement getEditorPluginForContext(String context) {
		return getPluginForContext(context, PlugInConstant.EDITOR_PLUGIN);
	}

	/**
	 * get the element for the specific type, POLICY or COMPONENT
	 * 
	 * @param type
	 * @return IConfigurationElement list
	 */
	public static List<IConfigurationElement> getEditorPluginForType(String type) {
		IConfigurationElement[] editors = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(PlugInConstant.EDITOR_PLUGIN);
		List<IConfigurationElement> result = new ArrayList<IConfigurationElement>();
		for (IConfigurationElement editor : editors) {
			String editorType = editor.getAttribute(PlugInConstant.TYPE);
			if (editorType.equals(type)) {
				result.add(editor);
			}
		}
		return result;
	}

	/**
	 * get the PDF plug-in definition based on context
	 * 
	 * @param context
	 * @return null if nothing found, or the element
	 */
	public static IConfigurationElement getPdfPluginForContext(String context) {
		return getPluginForContext(context, PlugInConstant.PDF_PLUGIN);
	}

	/**
	 * get the plug-in definition based on context and plug-in id
	 * 
	 * @param context
	 * @param pluginId
	 * @return null if nothing found, or the element
	 */
	public static IConfigurationElement getPluginForContext(String context,
			String pluginId) {
		IConfigurationElement[] editors = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(pluginId);
		IConfigurationElement pluginEditor = null;
		for (IConfigurationElement editor : editors) {
			String pluginContext = editor.getAttribute(PlugInConstant.CONTEXT);
			if (pluginContext.equals(context)) {
				pluginEditor = editor;
				break;
			}
		}
		return pluginEditor;
	}

	/**
	 * get the read-only plug-in definition based on context
	 * 
	 * @param context
	 * @return null if nothing found, or the element
	 */
	public static IConfigurationElement getReadOnlyPluginForContext(
			String context) {
		return getPluginForContext(context, PlugInConstant.READONLY_PLUGIN);
	}

	/**
	 * get the preview plug-in definition based on context
	 * 
	 * @param context
	 * @return null if nothing found, or the element
	 */
	public static IConfigurationElement getPreviewPluginForContext(
			String context) {
		return getPluginForContext(context, PlugInConstant.PREVIEW_PLUGIN);
	}
}
