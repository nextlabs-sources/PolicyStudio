package com.nextlabs.policystudio.editor.SAP;

import org.eclipse.osgi.util.NLS;

public class EditorMessages extends NLS {

	private EditorMessages() {
	}

	static {
		initializeMessages(
				"com.nextlabs.policystudio.editor.SAP.messages",
				EditorMessages.class);
	}

	public static String FIND_STRING;

	public static String FIND_INSTRUCTIONS;
	
	public static String 
		COMPONENTEDITOR_MEMBERS,
		OBJECTCOMPONENTEDITOR_OBJECTS, 
		OBJECTCOMPONENTEDITOR_LOOKUP,
		OBJECTCOMPONENTEDITOR_OBJECT_COMPONENTS,
		OBJECTLISTPANEL_OBJECT_TITLE,
		OBJECTLISTPANEL_OBJECT_MSG,
		OBJECTLISTPANEL_OBJECT_NAME;
}