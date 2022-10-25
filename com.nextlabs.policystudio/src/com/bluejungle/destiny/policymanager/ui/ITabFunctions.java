/*
 * Created on Jul 5, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.CTabItem;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/D_Plugins/com.nextlabs.policystudio/src/com
 *          /bluejungle/destiny/policymanager/ui/ITabFunctions.java#1 $
 */

public interface ITabFunctions {

	public void createFolder();

	public void deleteFolder();

	public void refreshActiveDataAsync();

	public CTabItem getTabItem();

	public TreeViewer getTreeViewer();

	public void setFolderPermission();
}
