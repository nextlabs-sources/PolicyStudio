/*
 * Created on Apr 6, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.dictionary;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.BrowserMessages;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;

/**
 * {@see
 * com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.userbrowser.
 * ISelectableDictionaryBasedLeafObjectTableModel} implementation for HostGroup
 * Subjects
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/browser/impl/
 *          userbrowser/SelectableHostGroupTableModel.java#3 $
 */

public class SelectableHostGroupTableModel implements
		ISelectableLeafObjectTableModel {

	private static final List<String> COLUMN_HEADERS = new ArrayList<String>(1);
	static {
		COLUMN_HEADERS.add(BrowserMessages.SELECTABLEHOSTGROUPTABLEMODEL_NAME);
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel#getColumnHeaders()
	 */
	public List<String> getColumnHeaders() {
		return COLUMN_HEADERS;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel#getImage(com.bluejungle.pf.destiny.lib.LeafObject)
	 */
	public Image getImage(LeafObject leafObject) {
		return ImageBundle.IMPORTED_HOST_GROUP_IMG;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel#getText(com.bluejungle.pf.destiny.lib.LeafObject,
	 *      int)
	 */
	public String getText(LeafObject leafObject, int columnIndex) {
		String textToReturn = null;
		switch (columnIndex) {
		case 0:
			textToReturn = leafObject.getName();
			break;
		default:
			break;
		}

		return textToReturn;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel#getTitle()
	 */
	public String getTitle() {
		return BrowserMessages.SELECTABLEHOSTGROUPTABLEMODEL_HOST_GROUPS;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel#supportsLeafObject(com.bluejungle.pf.destiny.lib.LeafObject)
	 */
	public boolean supportsLeafObject(LeafObject leafObject) {
		if (leafObject == null) {
			throw new NullPointerException("leafObject cannot be null.");
		}

		return leafObject.getType() == LeafObjectType.HOST_GROUP;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel#getLeafObjectType()
	 */
	public LeafObjectType getLeafObjectType() {
		return LeafObjectType.HOST_GROUP;
	}

}
