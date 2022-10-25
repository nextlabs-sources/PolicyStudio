package com.bluejungle.destiny.policymanager.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

import com.bluejungle.destiny.policymanager.util.ResourceManager;

public class EditorElementHelper {
	private static List<EditorElement> elements;

	public static List<EditorElement> getElements() {
		if (elements == null) {
			elements = new ArrayList<EditorElement>();
			IConfigurationElement[] decls = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(
							"com.nextlabs.policystudio.editor");
			for (IConfigurationElement element : decls) {
				String type = element.getAttribute("type");
				String context = element.getAttribute("context");
				String displayName = element.getAttribute("displayName");
				String panelClass = element.getAttribute("panelClass");
				String editorClass = element.getAttribute("editorClass");
				String icon = element.getAttribute("icon");
				String bundleName = element.getContributor().getName();
				Bundle bundle = Platform.getBundle(bundleName);
				Image image = null;
				if (icon != null && icon.length() != 0) {
					try {
						URL urlEntry = bundle.getEntry(icon);
						String path = FileLocator.toFileURL(urlEntry).getPath();
						image = ResourceManager.getImage(path);
					} catch (Exception e) {
						Shell shell = Display.getCurrent().getActiveShell();
						MessageDialog.openError(shell, "Error",
								"Cannot load the image for " + context + ".");
					}
				}
				EditorElement editorElement = new EditorElement(type, context,
						displayName, image, panelClass, editorClass);
				elements.add(editorElement);
			}
		}
		return elements;
	}

	public static String[] getComponentContexts() {
		List<EditorElement> elements = getElements();
		Set<String> contexts = new HashSet<String>();
		for (EditorElement element : elements) {
			String type = element.getType();
			if (type.equals("COMPONENT")) {
				String context = element.getContext();
				contexts.add(context);
			}
		}

		return contexts.toArray(new String[contexts.size()]);
	}
	
	public static String[] getComponentDisplayNames() {
		List<EditorElement> elements = getElements();
		Set<String> contexts = new HashSet<String>();
		for (EditorElement element : elements) {
			String type = element.getType();
			if (type.equals("COMPONENT")) {
				String name = element.getDisplayName();
				contexts.add(name);
			}
		}

		return contexts.toArray(new String[contexts.size()]);
	}

	public static String getComponentContextByDisplayName(String name) {
		List<EditorElement> elements = getElements();
		for (EditorElement element : elements) {
			String type = element.getType();
			if (type.equals("COMPONENT") && element.getDisplayName().equals(name)) {
				String context = element.getContext();
				return context;
			}
		}

		return null;
	}

	public static Image getComponentImage(String object) {
		List<EditorElement> elements = getElements();
		for (EditorElement element : elements) {
			String type = element.getType();
			String name = element.getDisplayName();
			if (type.equals("COMPONENT") && object.equals(name)) {
				return element.getIcon();
			}
		}
		return null;
	}
}
