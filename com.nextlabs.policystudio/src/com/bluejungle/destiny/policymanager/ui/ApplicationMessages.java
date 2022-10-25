/*
 * Created on Jan 17, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author bmeng
 */

public class ApplicationMessages extends NLS {

	private static final String BUNDLE_NAME = "com.bluejungle.destiny.policymanager.ui.messages";//$NON-NLS-1$

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ApplicationMessages.class);
	}

	private ApplicationMessages() {
	}

    public static String 
            ABOUTPART_ABOUT
          , ABOUTPART_TITLE
          , ABOUTPART_LOGGED_IN
          , ABOUTPART_AS
          , ABOUTPART_UNKNOWN_VERSION
    ;

    public static String 
            CLASSLISTCONTROL_ERROR
          , CLASSLISTCONTROL_ERROR_LENGTH
          , CLASSLISTCONTROL_ERROR_ENTRY_NOT_FOUND
          , CLASSLISTCONTROL_CREATE
          , CLASSLISTCONTROL_CREATE_EXCEPTION
          , CLASSLISTCONTROL_DELETE_EXCEPTION
          , CLASSLISTCONTROL_CREATE_MSG1
          , CLASSLISTCONTROL_CREATE_MSG2
          , CLASSLISTCONTROL_CREATE_EXCEPTION_MSG1
          , CLASSLISTCONTROL_CREATE_EXCEPTION_MSG2
          , CLASSLISTCONTROL_DELETE_EXCEPTION_MSG
          , CLASSLISTCONTROL_DUPLICATED_EXCEPTION_MSG1
          , CLASSLISTCONTROL_DUPLICATED_EXCEPTION_MSG2
          , CLASSLISTCONTROL_NOT_FOUND
          , CLASSLISTCONTROL_EXCEPTION_NAME_EXISTED
          , CLASSLISTCONTROL_EXCEPTION_DELETION_ERROR_TITLE
          , CLASSLISTCONTROL_NOT_FOUND_MSG1
          , CLASSLISTCONTROL_NOT_FOUND_MSG2
          , CLASSLISTCONTROL_OPEN
          , CLASSLISTCONTROL_CANNOT_DELETE_EXCEPTION_MSG
    ;
    
    public static String
    		TAG_ERROR
    	  , TAG_ERROR_EMPTY
    	  , TAG_ERROR_DUPLICATE; 

    public static String 
            COMPOSITIONCONTROL_LOOKUP
          , COMPOSITIONCONTROL_BROWSE_FOR_DIRECTORY
          , COMPOSITIONCONTROL_CHOOSE_DIRECTORY
          , COMPOSITIONCONTROL_REMOVE
          , COMPOSITIONCONTROL_REMOVE_CONDITION
          , COMPOSITIONCONTROL_AND
          , COMPOSITIONCONTROL_ADD
          , COMPOSITIONCONTROL_ADD_CONDITION
    ;

    public static String 
            DOMAINOBJECTHELPER_POLICY_TYPE
          , DOMAINOBJECTHELPER_POLICY_FOLDER_TYPE
          , DOMAINOBJECTHELPER_USER_TYPE
          , DOMAINOBJECTHELPER_DESKTOP_TYPE
          , DOMAINOBJECTHELPER_RESOURCE_TYPE
          , DOMAINOBJECTHELPER_RESOURCE_DEVICE_TYPE
          , DOMAINOBJECTHELPER_RESOURCE_SERVER_TYPE
          , DOMAINOBJECTHELPER_APPLICATION_TYPE
          , DOMAINOBJECTHELPER_ACTION_TYPE
          , DOMAINOBJECTHELPER_PORTAL_TYPE
    ;

    public static String 
            FILTERCONTROL_SEARCH
          , FILTERCONTROL_CANCEL_SEARCH
          , FILTERCONTROL_CANEL
    ;

    public static String 
            MENU_FILE
          , MENU_EDIT
          , MENU_TOOLS
          , MENU_ACTIONS
          , MENU_WINDOW
          , MENU_HELP
    ;

    public static String 
            POLICYSERVERPROXY_ERROR
          , POLICYSERVERPROXY_ERROR_MSG
          , POLICYSERVERPROXY_LOGIN_MSG
    ;

	public static String PREVIEWVIEW_NO_PREVIEW;

    public static String 
            PREVIEWPANEL_PREVIEW
          , PREVIEWPANEL_CANCEL
          , PREVIEWPANEL_GO
          , PREVIEWPANEL_NO_RESULT
          , PREVIEWPANEL_CANNOT_PREVIEW
          , PREVIEWPANEL_ERROR_SAVING
          , PREVIEWPANEL_ERROR_SAVING_MSG
    ;

    public static String 
            PROPERTYEXPRESSIONCONTROL_ADD
          , PROPERTYEXPRESSIONCONTROL_ADD_CONDITION
          , PROPERTYEXPRESSIONCONTROL_REMOVE
          , PROPERTYEXPRESSIONCONTROL_REMOVE_CONDITION
    ;

	public static String TITLE_PREVIEW;

    public static String 
            STATUSPANEL_STATUS
          , STATUSPANEL_MODIFY
          , STATUSPANEL_SUBMIT
          , STATUSPANEL_DEPLOY
    ;

    public static String 
            DEPLOYEDTAB_DEPLOYED
          , DEPLOYEDTAB_HOST_NAME
          , DEPLOYEDTAB_ACTIVATED_BY
          , DEPLOYEDTAB_TYPE
          , DEPLOYEDTAB_HOSTS
          , DEPLOYEDTAB_SCHEDULE
          , DEPLOYEDTAB_REMOVE_TITLE
          , DEPLOYEDTAB_REMOVE_MSG
          , DEPLOYEDTAB_DEACTIVATE
          , DEACTIVATEDTAB_DEACTIVATED
          , DRAFTTAB_DRAFT
          , PENDINGTAB_PENDING
          , PENDINGTAB_DEPLOY
          , ALLTAB_ALL
    ;

    public static String 
            ABSTRACTTAB_PROCESSING
          , ABSTRACTTAB_DOTS_1
          , ABSTRACTTAB_DOTS_2
          , ABSTRACTTAB_DOTS_3
          , ABSTRACTTAB_POLICIES
          , ABSTRACTTAB_COMPONENTS
          , ABSTRACTTAB_OBJECT
          , ABSTRACTTAB_VERSION
          , ABSTRACTTAB_SUBMITTED_TIME
          , ABSTRACTTAB_OWNED_BY
          , ABSTRACTTAB_STATUS
          , ABSTRACTTAB_SEARCH
          , ABSTRACTTAB_GENERATE_PDF
          , ABSTRACTTAB_SAVE_AS_PDF
          , ABSTRACTTAB_CONFIRM
          , ABSTRACTTAB_CONFIRM_MSG
          , ABSTRACTTAB_REFRESH
          , ABSTRACTTAB_DELETE_FOLDER
          , ABSTRACTTAB_DEFINITION
          , ABSTRACTTAB_PREVIEW
          , ABSTRACTTAB_PROPERTIES
          , ABSTRACTTAB_IMPORT
          , ABSTRACTTAB_EXPORT
          , ABSTRACTTAB_ENTER_TERM
          , ABSTRACTTAB_COLUMN_MODIFY_BY
          , ABSTRACTTAB_COLUMN_SUBMIT_BY
          , ABSTRACTTAB_EXPORT_AS_XACML
    ;

    public static String 
            INTERACTIVE_FILE_TAGGING
          , AUTOMATIC_FILE_TAGGING
    ;
}
