/*
 * Created on Jun 19, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.bluejungle.destiny.policymanager.model.EditorElementHelper;
import com.bluejungle.destiny.policymanager.model.EntityInformation;
import com.bluejungle.destiny.policymanager.ui.tab.AbstractTab;
import com.bluejungle.destiny.policymanager.ui.tab.AllTab;
import com.bluejungle.destiny.policymanager.ui.tab.DeactivatedTab;
import com.bluejungle.destiny.policymanager.ui.tab.DeployedTab;
import com.bluejungle.destiny.policymanager.ui.tab.DraftTab;
import com.bluejungle.destiny.policymanager.ui.tab.PendingTab;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.pf.destiny.lib.DODDigest;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/main/com.nextlabs.policystudio/src/com/bluejungle
 *          /destiny/policymanager/ui/PolicyManagerView.java#2 $
 */

public class PolicyManagerView extends ViewPart {

	public static List<DODDigest> ALL_FOLDER_LIST = new ArrayList<DODDigest>();
	public static List<DODDigest> ALL_POLICY_LIST = new ArrayList<DODDigest>();
	public static List<DODDigest> ALL_COMPONENT_LIST = new ArrayList<DODDigest>();
	public static Map<DODDigest, EntityInformation> ENTITY_INFO_MAP = new HashMap<DODDigest, EntityInformation>();
	public static AbstractTab CURRENT_TAB;

	public static final String ID = "com.bluejungle.destiny.policymanager.ui.PolicyManagerView";
	private CTabFolder tabFolder;
	private AbstractTab draftTab, pendingTab, deployedTab, deactivatedTab,
			allTab;

	public static Timer autoRefresh;

	public IStatusLineManager getStatusLineManager() {
		return getViewSite().getActionBars().getStatusLineManager();
	}

	@Override
	public void createPartControl(Composite parent) {
		createTabsControl(parent);
                
		GlobalState.getInstance().setView(this);
		GlobalState.getInstance().setViewID(ID);
	}

	private void createTabsControl(Composite root) {
		tabFolder = new CTabFolder(root, SWT.TOP);
		tabFolder.setBorderVisible(true);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Set up a gradient background for the selected tab
		tabFolder.setSelectionBackground(new Color[] {
				ResourceManager.getColor(SWT.COLOR_LIST_SELECTION),
				ResourceManager.getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW),
				ResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW) },
				new int[] { 50, 100 });
		tabFolder.setSimple(false);
		tabFolder.setUnselectedImageVisible(true);
		tabFolder.setSelectionForeground(ResourceManager
				.getColor(SWT.COLOR_WHITE));
		tabFolder.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshTab();
			}
		});

		draftTab = new DraftTab(tabFolder, this);

		pendingTab = new PendingTab(tabFolder, this);

		deployedTab = new DeployedTab(tabFolder, this);

		deactivatedTab = new DeactivatedTab(tabFolder, this);

		allTab = new AllTab(tabFolder, this);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		tabFolder.setLayout(layout);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 3;
		tabFolder.setLayoutData(data);

		CURRENT_TAB = draftTab;
		tabFolder.setSelection(draftTab.getTabItem());
	}

	@Override
	public void setFocus() {
	}

	private void refreshTab() {
		CURRENT_TAB.setSelection(CURRENT_TAB.getNavigationTreeViewer()
				.getSelection());
		switch (tabFolder.getSelectionIndex()) {
		case 0:
			CURRENT_TAB = draftTab;
			break;
		case 1:
			CURRENT_TAB = pendingTab;
			break;
		case 2:
			CURRENT_TAB = deployedTab;
			break;
		case 3:
			CURRENT_TAB = deactivatedTab;
			break;
		case 4:
			CURRENT_TAB = allTab;
			break;
		default:
			break;
		}
		CURRENT_TAB.getNavigationTreeViewer().setInput("root");
		CURRENT_TAB.refreshDataAsync();
		ISelection selection = CURRENT_TAB.getSelection();
		CURRENT_TAB.getNavigationTreeViewer().setSelection(selection);
	}

	public static void refreshCurrentTab() {
		EntityInfoProvider.updatePolicyTree();
		EntityInfoProvider.updateComponents();

		ALL_FOLDER_LIST.clear();
		ALL_POLICY_LIST.clear();
		ALL_COMPONENT_LIST.clear();
		ENTITY_INFO_MAP.clear();

		loadData();
		CURRENT_TAB.getNavigationTreeViewer().setInput("root");
		CURRENT_TAB.refreshDataAsync();
	}

	private static void loadData() {
		// if (PolicyManagerView.ALL_FOLDER_LIST.size() == 0
		// || PolicyManagerView.ALL_POLICY_LIST.size() == 0
		// || PolicyManagerView.ALL_COMPONENT_LIST.size() == 0) {
		List<DODDigest> policies = EntityInfoProvider.getPolicyList();
		for (DODDigest descriptor : policies) {
			if (!descriptor.isAccessible()) {
				continue;
			}
			String type = descriptor.getType();
			if (type.equals("FOLDER")) {
				PolicyManagerView.ALL_FOLDER_LIST.add(descriptor);
			} else if (type.equals("POLICY")) {
				PolicyManagerView.ALL_POLICY_LIST.add(descriptor);
			}
			extractEntityInfo(descriptor);
		}

		for (String type : EditorElementHelper.getComponentContexts()) {
			PolicyManagerView.ALL_COMPONENT_LIST.addAll(EntityInfoProvider
					.getComponentList(type));
		}
		for (DODDigest descriptor : PolicyManagerView.ALL_COMPONENT_LIST) {
			extractEntityInfo(descriptor);
		}
		// }
	}

	public static void extractEntityInfo(DODDigest descriptor) {
		EntityInformation info = new EntityInformation();

		info.setFullName(descriptor.getName());
		info.setDisplayName(EntityInformation.getDisplayName(descriptor));
		Date date = descriptor.getLastUpdated();
		info.setSubmittedTime(date);
		String time = "";
		if (date != null) {
			Format formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm aaa");
			time = formatter.format(date);
		}
		info.setFormatedTime(time);
		info.setHasChildren(descriptor.hasDependencies());
		info.setSubmittedBy(EntityInformation.getActivatedBy(descriptor));
		info.setVersion(descriptor.getVersion());
		info.setStatus(EntityInformation.getStatus(descriptor));
		PolicyManagerView.ENTITY_INFO_MAP.put(descriptor, info);
	}
}
