/*
 * Created on Mar 18, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.policystudio.editor.host;

import org.eclipse.osgi.util.NLS;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/usergroup/Messages.java#2
 *          $:
 */

public class Messages extends NLS {

	private Messages() {
	}

	static {
		initializeMessages(
				"com.nextlabs.policystudio.editor.host.messages",
				Messages.class);
	}

	public static String FIND_STRING;

	public static String FIND_INSTRUCTIONS;

	public static String EMPTY_LIST_STRING, IN_PROGRESS_STRING;

	public static String ACTIONLISTPANEL_ACTION_TITLE,
			ACTIONLISTPANEL_ACTION_MSG, ACTIONLISTPANEL_ACTION_NAME;

	public static String APPLICATIONLISTPANEL_APPLICATION_TITLE,
			APPLICATIONLISTPANEL_APPLICATION_MSG,
			APPLICATIONLISTPANEL_APPLICATION_NAME;

	public static String COMPONENTLISTPANEL_NEW,
			COMPONENTLISTPANEL_FILTER_OBJECT_NAME,
			COMPONENTLISTPANEL_FILTER_USING_COMPONENT,
			COMPONENTLISTPANEL_FILTER_USED_IN_POLICY;
	public static String COMPONENTLISTPANEL_ERROR_COMPONENT_EXIST,
			COMPONENTLISTPANEL_ERROR_COMPONENT_INVALID;

	public static String PORTALLISTPANEL_PORTAL_TITLE,
			PORTALLISTPANEL_PORTAL_MSG, PORTALLISTPANEL_PORTAL_NAME;
	public static String SERVERLISTPANEL_SERVER_TITLE, SERVERLISTPANEL_SERVER_MSG,
			SERVERLISTPANEL_SERVER_NAME;
	public static String DESKTOPLISTPANEL_DESKTOP_TITLE,
			DESKTOPLISTPANEL_DESKTOP_MSG, DESKTOPLISTPANEL_DESKTOP_NAME;

	public static String POLICYLISTPANEL_NEW_POLICY,
			POLICYLISTPANEL_NEW_FOLDER, POLICYLISTPANEL_POLICIES;
	public static String POLICYLISTPANEL_NO_FOLDER_TITLE,
			POLICYLISTPANEL_NO_FOLDER_MSG;
	public static String POLICYLISTPANEL_POLICY_TITLE,
			POLICYLISTPANEL_POLICY_MSG, POLICYLISTPANEL_POLICY_NAME;
	public static String POLICYLISTPANEL_FOLDER_TITLE,
			POLICYLISTPANEL_FOLDER_MSG, POLICYLISTPANEL_FOLDER_NAME;
	public static String POLICYLISTPANEL_ERROR_POLICY_EXIST,
			POLICYLISTPANEL_ERROR_POLICY_INVALID;
	public static String POLICYLISTPANEL_ERROR_FOLDER_EXIST,
			POLICYLISTPANEL_ERROR_FOLDER_INVALID,
			POLICYLISTPANEL_FILTER_POLICIES_USING_THE_COMPONENT,
			POLICYLISTPANEL_FILTER_POLICIES_NAMED, POLICYLISTPANEL_FILTER_END;

	public static String RESOURCELISTPANEL_RESOURCE_TITLE,
			RESOURCELISTPANEL_RESOURCE_MSG, RESOURCELISTPANEL_RESOURCE_NAME;

	public static String USERLISTPANEL_USER_TITLE, USERLISTPANEL_USER_MSG,
			USERLISTPANEL_USER_NAME;

	public static String STATUS_DRAFT_NONE_NONE, STATUS_DRAFT_NONE_PRIOR,
			STATUS_DRAFT_NONE_CURRENT, STATUS_DRAFT_PRIOR_NONE,
			STATUS_DRAFT_PRIOR_PRIOR, STATUS_DRAFT_PRIOR_CURRENT,
			STATUS_DRAFT_CURRENT_NONE, STATUS_DRAFT_CURRENT_PRIOR,
			STATUS_DRAFT_CURRENT_CURRENT, STATUS_APPROVED_NONE_NONE,
			STATUS_APPROVED_NONE_PRIOR, STATUS_APPROVED_NONE_CURRENT,
			STATUS_APPROVED_PRIOR_NONE, STATUS_APPROVED_PRIOR_PRIOR,
			STATUS_APPROVED_PRIOR_CURRENT, STATUS_APPROVED_CURRENT_NONE,
			STATUS_APPROVED_CURRENT_PRIOR, STATUS_APPROVED_CURRENT_CURRENT,
			STATUS_OBSOLETE_NONE_NONE, STATUS_OBSOLETE_NONE_PRIOR,
			STATUS_OBSOLETE_NONE_CURRENT, STATUS_OBSOLETE_PRIOR_NONE,
			STATUS_OBSOLETE_PRIOR_PRIOR, STATUS_OBSOLETE_PRIOR_CURRENT,
			STATUS_OBSOLETE_CURRENT_NONE, STATUS_OBSOLETE_CURRENT_PRIOR,
			STATUS_OBSOLETE_CURRENT_CURRENT;

	public static String DEPLOYMENT_DRAFT_NONE_NONE,
			DEPLOYMENT_DRAFT_NONE_PRIOR, DEPLOYMENT_DRAFT_NONE_CURRENT,
			DEPLOYMENT_DRAFT_PRIOR_NONE, DEPLOYMENT_DRAFT_PRIOR_PRIOR,
			DEPLOYMENT_DRAFT_PRIOR_CURRENT, DEPLOYMENT_DRAFT_CURRENT_NONE,
			DEPLOYMENT_DRAFT_CURRENT_PRIOR, DEPLOYMENT_DRAFT_CURRENT_CURRENT,
			DEPLOYMENT_APPROVED_NONE_NONE, DEPLOYMENT_APPROVED_NONE_PRIOR,
			DEPLOYMENT_APPROVED_NONE_CURRENT, DEPLOYMENT_APPROVED_PRIOR_NONE,
			DEPLOYMENT_APPROVED_PRIOR_PRIOR, DEPLOYMENT_APPROVED_PRIOR_CURRENT,
			DEPLOYMENT_APPROVED_CURRENT_NONE,
			DEPLOYMENT_APPROVED_CURRENT_PRIOR,
			DEPLOYMENT_APPROVED_CURRENT_CURRENT, DEPLOYMENT_OBSOLETE_NONE_NONE,
			DEPLOYMENT_OBSOLETE_NONE_PRIOR, DEPLOYMENT_OBSOLETE_NONE_CURRENT,
			DEPLOYMENT_OBSOLETE_PRIOR_NONE, DEPLOYMENT_OBSOLETE_PRIOR_PRIOR,
			DEPLOYMENT_OBSOLETE_PRIOR_CURRENT,
			DEPLOYMENT_OBSOLETE_CURRENT_NONE,
			DEPLOYMENT_OBSOLETE_CURRENT_PRIOR,
			DEPLOYMENT_OBSOLETE_CURRENT_CURRENT;
	
	public static String COMPOSITIONCONTROL_LOOKUP;
}