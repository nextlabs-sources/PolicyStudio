package com.bluejungle.destiny.policymanager.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import com.bluejungle.destiny.policymanager.action.PolicyStudioActionFactory;
import com.bluejungle.destiny.policymanager.model.EditorElementHelper;
import com.bluejungle.destiny.policymanager.ui.controls.WindowShade;
import com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel;
import com.bluejungle.destiny.policymanager.ui.usergroup.PolicyListPanel;
import com.bluejungle.destiny.policymanager.ui.usergroup.TabFolderPanel;
import com.bluejungle.pf.destiny.lifecycle.EntityType;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class PolicyAuthorView extends ViewPart {

	public static final String ID_VIEW = "com.bluejungle.destiny.policymanager.ui.PolicyAuthorView"; //$NON-NLS-1$

	private SashForm vSash = null;

	private WindowShade windowShade = null;

	public PolicyAuthorView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		showUI(parent);

		GlobalState.getInstance().setView(this);
		GlobalState.getInstance().setViewID(ID_VIEW);
	}

	/**
	 * 
	 */
	private void showUI(Composite parentControl) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;

		parentControl.setLayout(gridLayout);

		initialize(parentControl);

		setupActions();

		Display.getCurrent().asyncExec(new Runnable() {

			public void run() {
				EntityInfoProvider.updatePolicyTreeAsync();
				for (String ev : EditorElementHelper.getComponentContexts()) {
					EntityInfoProvider.updateComponentListAsync(ev);
				}
				GlobalState.getInstance().willBecomeVisible();
			}
		});

		// showLoginWindow(parentControl.getShell());
	}

	/**
	 * sets up actions, key bindings for the view
	 */
	private void setupActions() {
		IActionBars actionBars = getViewSite().getActionBars();
		PolicyStudioActionFactory.getUndoAction().setEnabled(false);
		PolicyStudioActionFactory.getUndoAction().setActionDefinitionId(
				"org.eclipse.ui.edit.undo");
		// getSite().getKeyBindingService().registerAction(PolicyManagerActionFactory.getUndoAction());

		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
				PolicyStudioActionFactory.getUndoAction());

		PolicyStudioActionFactory.getRedoAction().setEnabled(false);
		PolicyStudioActionFactory.getRedoAction().setActionDefinitionId(
				"org.eclipse.ui.edit.redo");
		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
				PolicyStudioActionFactory.getRedoAction());

		PolicyStudioActionFactory.getCopyAction().setEnabled(true);
		PolicyStudioActionFactory.getCopyAction().setActionDefinitionId(
				"org.eclipse.ui.edit.copy");
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
				PolicyStudioActionFactory.getCopyAction());

		PolicyStudioActionFactory.getPasteAction().setEnabled(true);
		PolicyStudioActionFactory.getPasteAction().setActionDefinitionId(
				"org.eclipse.ui.edit.paste");
		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
				PolicyStudioActionFactory.getPasteAction());

		PolicyStudioActionFactory.getCutAction().setEnabled(true);
		PolicyStudioActionFactory.getCutAction().setActionDefinitionId(
				"org.eclipse.ui.edit.cut");
		actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(),
				PolicyStudioActionFactory.getCutAction());

		PolicyStudioActionFactory.getSaveAction().setActionDefinitionId(
				"org.eclipse.ui.file.save");
		actionBars.setGlobalActionHandler(ActionFactory.SAVE.getId(),
				PolicyStudioActionFactory.getSaveAction());
	}

	@SuppressWarnings("unchecked")
	private void initialize(Composite parent) {
		vSash = new SashForm(parent, SWT.VERTICAL);
		GridData data = new GridData(GridData.FILL_BOTH);
		vSash.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		vSash.setLayout(layout);

		PolicyListPanel policyPanel = new PolicyListPanel(vSash, SWT.BORDER);
		GlobalState.getInstance().setPolicyListPanel(policyPanel);

		windowShade = new WindowShade(vSash, SWT.BORDER);

		IConfigurationElement[] folders = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(
						"com.nextlabs.policystudio.tabfolder");
		IConfigurationElement[] tabs = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("com.nextlabs.policystudio.tab");
		for (IConfigurationElement folder : folders) {
			TabFolderPanel tabfolder = null;
			String folderName = folder.getAttribute("name");
			List<IConfigurationElement> children = new ArrayList<IConfigurationElement>();
			for (IConfigurationElement tab : tabs) {
				String tabFolderName = tab.getAttribute("folder");
				if (tabFolderName.equalsIgnoreCase(folderName)) {
					children.add(tab);
				}
			}
			int size = children.size();
			if (size == 0) {
				continue;
			}
			Collections.sort(children, new Comparator<IConfigurationElement>() {

				public int compare(IConfigurationElement o1,
						IConfigurationElement o2) {
					return o1.getAttribute("name").compareToIgnoreCase(
							o2.getAttribute("name"));
				}
			});
			if (size > 1) {
				tabfolder = new TabFolderPanel(windowShade, SWT.NONE);
				data = new GridData(GridData.FILL_BOTH);
				tabfolder.setLayoutData(data);
			}
			CTabItem defaultTab = null;
			ComponentListPanel panel = null;
			for (int j = 0, n = children.size(); j < n; j++) {
				IConfigurationElement child = children.get(j);
				String classname = child.getAttribute("class");
				String name = child.getAttribute("name");

				String contributor = child.getContributor().getName();
				Bundle bundle = Platform.getBundle(contributor);
				try {
					Class myClass = bundle.loadClass(classname);
					Constructor constructor[] = myClass.getConstructors();
					if (n > 1) {
						panel = (ComponentListPanel) constructor[0]
								.newInstance(new Object[] {
										tabfolder.getFolder(), SWT.NONE });
						CTabItem tab = tabfolder.add(name);
						tab.setControl(panel);
						if (j == 0) {
							defaultTab = tab;
						}
					} else
						panel = (ComponentListPanel) constructor[0]
								.newInstance(new Object[] { windowShade,
										SWT.NONE });
					data = new GridData(GridData.FILL_BOTH);
					panel.setLayoutData(data);
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (InvalidRegistryObjectException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			if (size > 1) {
				tabfolder.getFolder().setSelection(defaultTab);
				windowShade.addPanel(folderName, tabfolder);
			} else
				windowShade.addPanel(folderName, panel);
		}

		windowShade.relayout();
	}

	@SuppressWarnings("deprecation")
	public void setListView(EntityType type) {
		if (type != EntityType.POLICY) {
			if (type == EntityType.USER) {
				windowShade.setOpenShade(0);
			} else if (type == EntityType.HOST) {
				windowShade.setOpenShade(1);
			} else if (type == EntityType.APPLICATION) {
				windowShade.setOpenShade(2);
			} else if (type == EntityType.RESOURCE) {
				windowShade.setOpenShade(3);
			}
		}
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
	}
}
