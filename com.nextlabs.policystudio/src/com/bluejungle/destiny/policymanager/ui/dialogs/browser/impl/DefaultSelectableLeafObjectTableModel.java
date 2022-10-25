/*
 * Created on Apr 6, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.BrowserMessages;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;

/**
 * The default implementation of the {@see
 * com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.
 * ISelectableLeafObjectTableModel} interface. It supports all Leaf Objects in a
 * generic fashion
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/browser/impl/
 *          DefaultSelectableLeafObjectTableModel.java#2 $
 */

public class DefaultSelectableLeafObjectTableModel implements
		ISelectableLeafObjectTableModel {

	private static final List<String> COLUMN_HEADERS = Collections
			.singletonList(BrowserMessages.DEFAULTSELECTABLELEAFOBJECTTABLEMODEL_NAME);

	private static final Map<LeafObjectType, String> LEAF_OBJECT_TYPE_TO_TITLE_MAP = new HashMap<LeafObjectType, String>();

	static {
		LEAF_OBJECT_TYPE_TO_TITLE_MAP.put(LeafObjectType.HOST,
				BrowserMessages.DEFAULTSELECTABLELEAFOBJECTTABLEMODEL_HOSTS);
		LEAF_OBJECT_TYPE_TO_TITLE_MAP
				.put(
						LeafObjectType.HOST_GROUP,
						BrowserMessages.DEFAULTSELECTABLELEAFOBJECTTABLEMODEL_HOST_GROUPS);
		LEAF_OBJECT_TYPE_TO_TITLE_MAP.put(LeafObjectType.APPUSER,
				BrowserMessages.DEFAULTSELECTABLELEAFOBJECTTABLEMODEL_USERS);
		LEAF_OBJECT_TYPE_TO_TITLE_MAP
				.put(
						LeafObjectType.ACCESSGROUP,
						BrowserMessages.DEFAULTSELECTABLELEAFOBJECTTABLEMODEL_USER_GROUPS);
		LEAF_OBJECT_TYPE_TO_TITLE_MAP
				.put(
						LeafObjectType.APPLICATION,
						BrowserMessages.DEFAULTSELECTABLELEAFOBJECTTABLEMODEL_APPLICATIONS);
	}

	private static final Map<LeafObjectType, SubjectAttribute> LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP = new HashMap<LeafObjectType, SubjectAttribute>();

	static {
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.HOST,
				SubjectAttribute.HOST_NAME);
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.HOST_GROUP,
				SubjectAttribute.HOST_LDAP_GROUP);
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.APPUSER,
				SubjectAttribute.USER_NAME);
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.ACCESSGROUP,
				SubjectAttribute.USER_LDAP_GROUP);
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.APPLICATION,
				SubjectAttribute.APP_NAME);
	}

	private LeafObjectType leafObjectType;

	/**
	 * Create an instance of DefaultSelectableLeafObjectTableModel
	 * 
	 * @param nextLeafObjectType
	 */
	public DefaultSelectableLeafObjectTableModel(LeafObjectType leafObjectType) {
		if (leafObjectType == null) {
			throw new NullPointerException("leafObjectType cannot be null.");
		}

		this.leafObjectType = leafObjectType;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel#getTitle()
	 */
	public String getTitle() {
		return (String) LEAF_OBJECT_TYPE_TO_TITLE_MAP.get(this.leafObjectType);
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel#getColumnHeaders()
	 */
	public List<String> getColumnHeaders() {
		return COLUMN_HEADERS;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel#getText(com.bluejungle.pf.destiny.lib.LeafObject,
	 *      int)
	 */
	public String getText(LeafObject leafObject, int columnIndex) {
		return leafObject.getName();
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel#getImage()
	 */
	public Image getImage(LeafObject leafObject) {
		return ObjectLabelImageProvider.getImage(leafObject);
	}

	/**
	 * 
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel#getLeafObjectType()
	 */
	public LeafObjectType getLeafObjectType() {
		return this.leafObjectType;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel#supportsLeafObject(com.bluejungle.pf.destiny.lib.LeafObject)
	 */
	public boolean supportsLeafObject(LeafObject leafObject) {
		if (leafObject == null) {
			throw new NullPointerException("leafObject cannot be null.");
		}

		return leafObject.getType() == this.leafObjectType;
	}
}
