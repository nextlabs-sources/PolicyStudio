/*
 * Created on May 20, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.model.ClientEditorPanel;
import com.bluejungle.destiny.policymanager.model.ComponentWrapper;
import com.bluejungle.destiny.policymanager.model.IClientComponent;
import com.bluejungle.destiny.policymanager.model.IClientPolicy;
import com.bluejungle.destiny.policymanager.model.IComponentEditor;
import com.bluejungle.destiny.policymanager.model.IPolicyEditor;
import com.bluejungle.destiny.policymanager.model.PolicyServerHelper;
import com.bluejungle.destiny.policymanager.model.PolicyWrapper;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.PluginUtil;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

/**
 * @author dstarke
 * 
 */
public class EditorPanelFactory {

	public static boolean hasEditorPanelFor(Object domainObject) {
		if (domainObject instanceof IDPolicy) {
			return true;
		} else if (domainObject instanceof IDSpec) {
			return true;
		}
		return false;
	}

	@SuppressWarnings( { "unchecked" })
	public static EditorPanel getEditorPanel(Composite parent, int style,
			Object domainObject) {
		if (parent.isDisposed()) {
			return null;
		}
		if (domainObject instanceof IDPolicy) {
			IPolicy policy = (IDPolicy) domainObject;
			IConfigurationElement defaultElement = PluginUtil
					.getDefaultEditorPluginForPolicy();

			IConfigurationElement[] editors = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(
							"com.nextlabs.policystudio.editor");
			IConfigurationElement editor = null;
			for (IConfigurationElement element : editors) {
				String type = element.getAttribute("type");
				if (!type.equals("POLICY")) {
					continue;
				}
				String name = element.getAttribute("context");
				if (policy.hasAttribute(name)) {
					editor = element;
				}
			}
			if (editor == null) {
				editor = defaultElement;
			}
			if (editor == null) {
				return null;
			}

			Bundle bundle = Platform.getBundle(editor.getContributor()
					.getName());
			try {
				Class panelClass = bundle.loadClass(editor
						.getAttribute("panelClass"));
				Constructor panelConstructor[] = panelClass.getConstructors();
				IClientPolicy clientPolicy = new PolicyWrapper(
						(IDPolicy) domainObject);
				String displayName = editor.getAttribute("displayName");
				PolicyEditorPanel editorPanel = (PolicyEditorPanel) panelConstructor[0]
						.newInstance(new Object[] { parent, style,
								clientPolicy, displayName });
				ClientEditorPanel clientEditorPanel = new ClientEditorPanel();
				clientEditorPanel.setEditorPanel(editorPanel);
				Class editorClass = bundle.loadClass(editor
						.getAttribute("editorClass"));
				Constructor editorConstructor[] = editorClass.getConstructors();
				IPolicyEditor policyEditor = (IPolicyEditor) editorConstructor[0]
						.newInstance(new Object[] { clientEditorPanel,
								clientPolicy });
				editorPanel.setEditor(policyEditor);
				return editorPanel;
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
			String name = spec.getName();
			String type = PolicyServerHelper.getTypeFromComponentName(name);
			List<IConfigurationElement> list = PluginUtil
					.getEditorPluginForType("COMPONENT");
			for (IConfigurationElement element : list) {
				String componentContext = element.getAttribute("context");
				if (componentContext.equals(type)) {
					String panel = element.getAttribute("panelClass");
					String editor = element.getAttribute("editorClass");
					String displayName = element.getAttribute("displayName");
					String contributor = element.getContributor().getName();
					Bundle bundle = Platform.getBundle(contributor);
					try {
						Class panelClass = bundle.loadClass(panel);
						Constructor panelConstructor[] = panelClass
								.getConstructors();
						IClientComponent component = new ComponentWrapper(spec);
						ComponentEditorPanel editorPanel = (ComponentEditorPanel) panelConstructor[0]
								.newInstance(new Object[] { parent, style,
										component, displayName });
						ClientEditorPanel clientEditorPanel = new ClientEditorPanel();
						clientEditorPanel.setEditorPanel(editorPanel);
						Class editorClass = bundle.loadClass(editor);
						Constructor editorConstructor[] = editorClass
								.getConstructors();
						IComponentEditor componentEditor = (IComponentEditor) editorConstructor[0]
								.newInstance(new Object[] { clientEditorPanel,
										component });
						editorPanel.setEditor(componentEditor);
						return editorPanel;
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
			}
		}
		return null;
	}
}
