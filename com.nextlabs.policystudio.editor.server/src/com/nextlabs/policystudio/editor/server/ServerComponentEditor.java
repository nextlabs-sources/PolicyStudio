/*
 * Created on Feb 15, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.nextlabs.policystudio.editor.server;

import java.util.List;

import com.bluejungle.destiny.policymanager.editor.BaseComponentEditor;
import com.bluejungle.destiny.policymanager.editor.EditorMessages;
import com.bluejungle.destiny.policymanager.model.IClientComponent;
import com.bluejungle.destiny.policymanager.model.IClientEditorPanel;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/editor/PortalComponentEditor.java#4
 *          $
 */

public class ServerComponentEditor extends BaseComponentEditor {
	public ServerComponentEditor(IClientEditorPanel panel,
			IClientComponent component) {
		super(panel, component);
		setShowPropertyExpressions(false);
	}

	@Override
	protected List<String> getPropertyOperatorList() {
		return null;
	}

	@Override
	protected List<String> getPropertyList() {
		return null;
	}

	@Override
	protected SpecType getSpecType() {
		return SpecType.RESOURCE;
	}

	@Override
	protected String getMemberLabel() {
		return EditorMessages.SERVERCOMPONENTEDITOR_SERVERS;
	}

	public EntityType getEntityType() {
		return EntityType.COMPONENT;
	}

	public String getObjectTypeLabelText() {
		return EditorMessages.SERVERCOMPONENTEDITOR_SERVER_COMPONENTS;
	}

	@Override
	public String getComponentType() {
		return "SERVER";
	}

	public boolean hasCustomProperties() {
		return true;
	}

	@Override
	protected String getLookupLabel() {
		return null;
	}
}
