/*
 * Created on Apr 6, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.dictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.Image;

import com.bluejungle.destiny.policymanager.SharePointImageConstants;
import com.bluejungle.destiny.policymanager.UserProfileEnum;
import com.bluejungle.destiny.policymanager.model.EnrollmentType;
import com.bluejungle.destiny.policymanager.model.IRealm;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.BrowserMessages;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel;
import com.bluejungle.destiny.policymanager.util.PlatformUtils;
import com.bluejungle.destiny.services.policy.types.PolicyEditorRoles;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.services.PolicyEditorException;

/**
 * {@see
 * com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.userbrowser.
 * ISelectableDictionaryBasedLeafObjectTableModel} implementation for User Group
 * subjects
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/ui/dialogs/browser/impl/dictionary
 *          /SelectableUserGroupTableModel.java#2 $
 */

public class SelectableUserGroupTableModel implements
		ISelectableLeafObjectTableModel {

	private static final List<String> COLUMN_HEADERS = new ArrayList<String>(1);
	static {
		COLUMN_HEADERS.add(BrowserMessages.SELECTABLEUSERGROUPTABLEMODEL_NAME);
	}

	private final Map<String, EnrollmentType> domainToEnrollmentTypeMap = new HashMap<String, EnrollmentType>();

	SelectableUserGroupTableModel() {
		UserProfileEnum profile = PlatformUtils.getProfile();
		PolicyEditorRoles role = null;
		if (profile == UserProfileEnum.CORPORATE) {
			role = PolicyEditorRoles.CORPORATE;
		} else if (profile == UserProfileEnum.FILESYSTEM) {
			role = PolicyEditorRoles.FILESYSTEM;
		} else if (profile == UserProfileEnum.PORTAL) {
			role = PolicyEditorRoles.PORTAL;
		}

		try {
			Set<IRealm> dictionaryRealmsSet = PolicyServerProxy
					.getDictionaryRealms(role);
			for (IRealm nextRealm : dictionaryRealmsSet) {
				this.domainToEnrollmentTypeMap.put(nextRealm.getId(), nextRealm
						.getEnrollmentType());
			}
		} catch (PolicyEditorException exception) {
			// FIX ME
		}
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
		Image imageToReturn = null;

		String domainName = leafObject.getDomainName();
		if (domainName != null
				&& EnrollmentType.SHAREPOINT
						.equals(this.domainToEnrollmentTypeMap.get(domainName))) {
			imageToReturn = SharePointImageConstants.SHAREPOINT_USERGROUP;
		} else {
			imageToReturn = ImageBundle.IMPORTED_USER_GROUP_IMG;
		}

		return imageToReturn;
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
		return BrowserMessages.SELECTABLEUSERGROUPTABLEMODEL_USER_GROUPS;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel#supportsLeafObject(com.bluejungle.pf.destiny.lib.LeafObject)
	 */
	public boolean supportsLeafObject(LeafObject leafObject) {
		if (leafObject == null) {
			throw new NullPointerException("leafObject cannot be null.");
		}

		return leafObject.getType() == LeafObjectType.USER_GROUP;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectTableModel#getLeafObjectType()
	 */
	public LeafObjectType getLeafObjectType() {
		return LeafObjectType.USER_GROUP;
	}

}
