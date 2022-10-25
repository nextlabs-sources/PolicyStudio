/*
 * Created on Apr 5, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;

import com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.DefaultLeafObjectBrowser;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.PortalLeafObjectBrowser;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.dictionary.DictionaryBasedLeafObjectBrowser;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * LeafObjectBrowserFactory is a factory class used to obtain a reference to a
 * Leaf Object Browser dialog, a dialog used to search for and select leaf
 * objects
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/browser/
 *          LeafObjectBrowserFactory.java#4 $
 */
@SuppressWarnings("deprecation")
public class LeafObjectBrowserFactory implements
		IHasComponentInfo<LeafObjectBrowserFactory> {

	private static final ComponentInfo<LeafObjectBrowserFactory> COMPONENT_INFO = new ComponentInfo<LeafObjectBrowserFactory>(
			LeafObjectBrowserFactory.class.getName(),
			LeafObjectBrowserFactory.class.getName(),
			LifestyleType.SINGLETON_TYPE);
	private static final Map<SpecType, List<LeafObjectType>> SPEC_TYPE_TO_LEAF_TYPE_LIST_MAP = new HashMap<SpecType, List<LeafObjectType>>();
	private static final Map<SpecType, String> SPEC_TYPE_TO_WINDOW_TITLE_MAP = new HashMap<SpecType, String>();
	private static final Set<SpecType> DICTIONARY_BASED_SPEC_TYPES = new HashSet<SpecType>();

	static {
		List<LeafObjectType> leafTypes = new ArrayList<LeafObjectType>(2);
		leafTypes.add(LeafObjectType.HOST);
		leafTypes.add(LeafObjectType.HOST_GROUP);
		SPEC_TYPE_TO_LEAF_TYPE_LIST_MAP.put(SpecType.HOST, leafTypes);
		leafTypes = new ArrayList<LeafObjectType>(1);
		leafTypes.add(LeafObjectType.APPLICATION);
		SPEC_TYPE_TO_LEAF_TYPE_LIST_MAP.put(SpecType.APPLICATION, leafTypes);
		leafTypes = new ArrayList<LeafObjectType>(2);
		leafTypes.add(LeafObjectType.APPUSER);
		leafTypes.add(LeafObjectType.ACCESSGROUP);
		SPEC_TYPE_TO_LEAF_TYPE_LIST_MAP.put(SpecType.APPUSER, leafTypes);
		leafTypes = new ArrayList<LeafObjectType>(2);
		leafTypes.add(LeafObjectType.USER);
		leafTypes.add(LeafObjectType.USER_GROUP);
		leafTypes.add(LeafObjectType.CONTACT);
		SPEC_TYPE_TO_LEAF_TYPE_LIST_MAP.put(SpecType.USER, leafTypes);

		SPEC_TYPE_TO_WINDOW_TITLE_MAP.put(SpecType.HOST,
				BrowserMessages.LEAFOBJECTBROWSERFACTORY_HOST);
		SPEC_TYPE_TO_WINDOW_TITLE_MAP.put(SpecType.APPLICATION,
				BrowserMessages.LEAFOBJECTBROWSERFACTORY_APPLICATION);
		SPEC_TYPE_TO_WINDOW_TITLE_MAP.put(SpecType.APPUSER,
				BrowserMessages.LEAFOBJECTBROWSERFACTORY_APPUSER);
		SPEC_TYPE_TO_WINDOW_TITLE_MAP.put(SpecType.USER,
				BrowserMessages.LEAFOBJECTBROWSERFACTORY_USER);
		SPEC_TYPE_TO_WINDOW_TITLE_MAP.put(SpecType.PORTAL,
				BrowserMessages.LEAFOBJECTBROWSERFACTORY_PORTAL);

		DICTIONARY_BASED_SPEC_TYPES.add(SpecType.USER);
		DICTIONARY_BASED_SPEC_TYPES.add(SpecType.HOST);
	}

	/**
	 * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
	 */
	public ComponentInfo<LeafObjectBrowserFactory> getComponentInfo() {
		return COMPONENT_INFO;
	}

	/**
	 * Retrieve a leaf object browser for the specified spec type
	 * 
	 * @param objectType
	 *            the spec type of the objects to be listed in the browser
	 * @param shell
	 *            the parent shell of the dialog
	 * @return the request leaf object browser
	 */
	public ILeafObjectBrowser getLeafObjectBrowser(SpecType objectType,
			Shell shell) {
		if (objectType == null) {
			throw new NullPointerException("leafObjectType cannot be null.");
		}

		if (shell == null) {
			throw new NullPointerException("shell cannot be null.");
		}

		List<LeafObjectType> leafObjectTypeList = SPEC_TYPE_TO_LEAF_TYPE_LIST_MAP
				.get(objectType);
		String windowTitle = SPEC_TYPE_TO_WINDOW_TITLE_MAP.get(objectType);

		ILeafObjectBrowser browserToReturn = null;

		if (objectType.equals(SpecType.PORTAL)) {
			browserToReturn = new PortalLeafObjectBrowser(shell, windowTitle);
		} else if (DICTIONARY_BASED_SPEC_TYPES.contains(objectType)) {
			browserToReturn = new DictionaryBasedLeafObjectBrowser(shell,
					windowTitle, leafObjectTypeList);
		} else {
			browserToReturn = new DefaultLeafObjectBrowser(shell, windowTitle,
					leafObjectTypeList);
		}

		return browserToReturn;
	}
}
