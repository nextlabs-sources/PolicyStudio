/*
 * Created on Apr 7, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.SharePointImageConstants;
import com.bluejungle.destiny.policymanager.UserProfileEnum;
import com.bluejungle.destiny.policymanager.framework.BaseTableLabelProvider;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.PlatformUtils;
import com.bluejungle.destiny.policymanager.util.PluginUtil;
import com.bluejungle.destiny.services.policy.types.EnrollmentType;
import com.bluejungle.destiny.services.policy.types.PolicyEditorRoles;
import com.bluejungle.destiny.services.policy.types.Realm;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.services.PolicyEditorException;

/**
 * 
 * LefaObjectNameLabelProvider is an {@see
 * org.eclipse.jface.viewers.ITableLabelProvider} which provides the leaf object
 * name as the text of the label and the appropriate leaf object image as the
 * image of the label
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/branch/Destiny_Beta4_Stable/main/src/client/policymanager
 *          /src
 *          /java/main/com/bluejungle/destiny/policymanager/ui/dialogs/browser
 *          /impl/LeafObjectNameLabelProvider.java#1 $
 */

public class LeafObjectNameLabelProvider extends BaseTableLabelProvider
		implements ITableLabelProvider {

	private Map<String, EnrollmentType> typeMap;

	public LeafObjectNameLabelProvider() {
		updateDictionaryRealmsMap();
	}

	private void updateDictionaryRealmsMap() {
		typeMap = new HashMap<String, EnrollmentType>();
		Set<Realm> enrollmentNames = new HashSet<Realm>();
		UserProfileEnum profile = PlatformUtils.getProfile();
		PolicyEditorRoles role = null;
		if (profile == UserProfileEnum.CORPORATE)
			role = PolicyEditorRoles.CORPORATE;
		else if (profile == UserProfileEnum.FILESYSTEM)
			role = PolicyEditorRoles.FILESYSTEM;
		else if (profile == UserProfileEnum.PORTAL)
			role = PolicyEditorRoles.PORTAL;
		try {
			enrollmentNames = PolicyServerProxy.client
					.getDictionaryEnrollmentRealms(role);
		} catch (PolicyEditorException exception) {
			LoggingUtil.logWarning(Activator.ID,
					"Failed to retrieve dictionary realms.", exception);
		}

		for (Realm realm : enrollmentNames) {
			typeMap.put(realm.getName(), realm.getType());
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
	 *      int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof LeafObject) {
			LeafObject object = (LeafObject) element;
			LeafObjectType type = object.getType();
			String domainName = object.getDomainName();
			if (type == LeafObjectType.APPLICATION) {
				return ImageBundle.APPLICATION_IMG;
			} else if (type == LeafObjectType.HOST) {
				return ImageBundle.DESKTOP_IMG;
			} else if (type == LeafObjectType.HOST_GROUP) {
				return ImageBundle.IMPORTED_HOST_GROUP_IMG;
			} else if (type == LeafObjectType.RESOURCE) {
				return ImageBundle.FILE_IMG;
			} else if (type == LeafObjectType.USER) {
				if (domainName != null
						&& EnrollmentType.value3
								.equals(typeMap.get(domainName)))
					return SharePointImageConstants.SHAREPOINT_USER;
				return ImageBundle.USER_IMG;
			} else if (type == LeafObjectType.USER_GROUP) {
				if (domainName != null
						&& EnrollmentType.value3
								.equals(typeMap.get(domainName)))
					return SharePointImageConstants.SHAREPOINT_USERGROUP;
				return ImageBundle.IMPORTED_USER_GROUP_IMG;
			} else if (type == LeafObjectType.CONTACT) {
				return ImageBundle.CONTACT_IMG;
			} else if (type == LeafObjectType.ACTION) {
				return PluginUtil.getContextImageForContext("ACTION");
			} else if (type == LeafObjectType.APPUSER) {
				return ImageBundle.APP_USER_IMG;
			} else if (type == LeafObjectType.ACCESSGROUP) {
				return ImageBundle.APP_USER_GROUP_IMG;
			}
		}

		return ObjectLabelImageProvider.getImage(element);
	}

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
	 *      int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		return ((LeafObject) element).getName();
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
	 *      java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

}
