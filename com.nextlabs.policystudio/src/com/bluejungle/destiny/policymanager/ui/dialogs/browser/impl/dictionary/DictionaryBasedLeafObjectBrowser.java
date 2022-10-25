/*
 * Created on Mar 22, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.dictionary;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.DefaultLeafObjectBrowser;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectsSource;
import com.bluejungle.pf.destiny.lib.LeafObjectType;

/**
 * Implementation of the {@see
 * com.bluejungle.destiny.policymanager.ui.dialogs.browser.ILeafObjectBrowser}
 * interface to provide data browsing of User and User Group subjects.
 * Currently, the only functionality specific to User and User Group subjects is
 * the addition of the user realm search parameter
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/ui/dialogs/browser/impl/dictionary
 *          /DictionaryBasedLeafObjectBrowser.java#1 $
 */

public class DictionaryBasedLeafObjectBrowser extends DefaultLeafObjectBrowser {

	private static final Map<LeafObjectType, ISelectableLeafObjectTableModel> LEAF_OBJECT_TYPE_TO_TABLE_MODEL_MAP = new HashMap<LeafObjectType, ISelectableLeafObjectTableModel>();
	static {
		LEAF_OBJECT_TYPE_TO_TABLE_MODEL_MAP.put(LeafObjectType.USER,
				new SelectableUserTableModel());
		LEAF_OBJECT_TYPE_TO_TABLE_MODEL_MAP.put(LeafObjectType.USER_GROUP,
				new SelectableUserGroupTableModel());
		LEAF_OBJECT_TYPE_TO_TABLE_MODEL_MAP.put(LeafObjectType.HOST,
				new SelectableHostTableModel());
		LEAF_OBJECT_TYPE_TO_TABLE_MODEL_MAP.put(LeafObjectType.HOST_GROUP,
				new SelectableHostGroupTableModel());
		LEAF_OBJECT_TYPE_TO_TABLE_MODEL_MAP.put(LeafObjectType.CONTACT,
				new SelectableContactTableModel());
	}

	/**
	 * Create an instance of DictionaryBasedLeafObjectBrowser
	 * 
	 * @param parent
	 *            the containing shell
	 * @param windowTitle
	 * @param leafObjectTypes
	 */
	public DictionaryBasedLeafObjectBrowser(Shell parent, String windowTitle,
			List<LeafObjectType> leafObjectTypes) {
		super(parent, windowTitle, leafObjectTypes);
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.DefaultLeafObjectBrowser#createSelectableLeafObjectSource(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected ISelectableLeafObjectsSource createSelectableLeafObjectSource(
			Composite parent) {
		return new DictionarySelectableLeafObjectSourceImpl(parent);
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.DefaultLeafObjectBrowser#getSelectableLeafObjectTableModels()
	 */
	@Override
	protected List<ISelectableLeafObjectTableModel> getSelectableLeafObjectTableModels() {
		List<ISelectableLeafObjectTableModel> tableModelsToReturn = new LinkedList<ISelectableLeafObjectTableModel>();

		for (LeafObjectType nextLeafObjectType : getLeafObjectTypes()) {
			tableModelsToReturn.add(LEAF_OBJECT_TYPE_TO_TABLE_MODEL_MAP
					.get(nextLeafObjectType));
		}

		return tableModelsToReturn;
	}
}
