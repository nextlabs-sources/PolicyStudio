package com.bluejungle.destiny.policymanager.ui.usergroup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.util.ResourceManager;

/**
 * @author bmeng
 */

public class TabFolderPanel extends Composite {

	private CTabFolder folder;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param style
	 */
	public TabFolderPanel(Composite parent, int style) {
		super(parent, style);
		setBackground(ResourceManager.getColor(123, 134, 154));

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		folder = new CTabFolder(this, SWT.NONE);
		folder.setBackground(ResourceManager.getColor("TABFOLDER_BACKGROUND",
				Activator.getDefault().getPluginPreferences().getString(
						"TABFOLDER_BACKGROUND")));
		folder.setForeground(ResourceManager.getColor("TABFOLDER_FOREGROUND",
				Activator.getDefault().getPluginPreferences().getString(
						"TABFOLDER_FOREGROUND")));
		folder.setSelectionBackground(new Color[] {
				ResourceManager.getColor("TABFOLDER_SELECTION_BACKGROUND",
						Activator.getDefault().getPluginPreferences()
								.getString("TABFOLDER_SELECTION_BACKGROUND")),
				ResourceManager.getColor("TABFOLDER_BACKGROUND", Activator
						.getDefault().getPluginPreferences().getString(
								"TABFOLDER_BACKGROUND")) }, new int[] { 100 },
				true);
		folder.setSelectionForeground(ResourceManager.getColor(
				"TABFOLDER_SELECTION_FOREGROUND", Activator.getDefault()
						.getPluginPreferences().getString(
								"TABFOLDER_SELECTION_FOREGROUND")));

		folder.setSimple(false);
		folder.setUnselectedImageVisible(false);
		folder.setUnselectedCloseVisible(false);

		GridData data = new GridData(GridData.FILL_BOTH);
		folder.setLayoutData(data);
	}

	public CTabItem add(String name) {
		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(name);

		return item;
	}

	public CTabFolder getFolder() {
		return folder;
	}
}