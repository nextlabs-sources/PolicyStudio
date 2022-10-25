/*
 * Created on Mar 16, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.policystudio.editor.user;

import java.util.List;

import com.bluejungle.destiny.policymanager.editor.BaseComponentEditor;
import com.bluejungle.destiny.policymanager.editor.EditorMessages;
import com.bluejungle.destiny.policymanager.model.IClientComponent;
import com.bluejungle.destiny.policymanager.model.IClientEditorPanel;
import com.bluejungle.destiny.policymanager.ui.PreviewPanelFactory;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/editor/UserComponentEditor.java#1
 *          $:
 */

public class UserComponentEditor extends BaseComponentEditor {

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param style
	 */
	public UserComponentEditor(IClientEditorPanel panel,
			IClientComponent component) {
		super(panel, component);
		setShowPropertyExpressions(true);
	}

	/**
	 * 
	 * returns the list of valid property attributes. this list is used to
	 * populate the combo
	 * 
	 * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getPropertyList()
	 */
	@Override
	protected List<String> getPropertyList() {
		return null;
	}

	/**
	 * returns the list of valid property attribute operators. this list is used
	 * to populate the combo
	 * 
	 * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getPropertyOperatorList()
	 */
	@Override
	protected List<String> getPropertyOperatorList() {
		return null;
	}

	@SuppressWarnings("deprecation")
	public EntityType getEntityType() {
		return EntityType.USER;
	}

	@Override
	protected SpecType getSpecType() {
		return SpecType.USER;
	}

	@Override
	protected String getMemberLabel() {
		return EditorMessages.USERCOMPONENTEDITOR_USERS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.bluejungle.destiny.policymanager.editor.EditorPanel#
	 * getObjectTypeLabelText()
	 */
	public String getObjectTypeLabelText() {
		return EditorMessages.USERCOMPONENTEDITOR_USER_COMPONENT;
	}

	@Override
	public Class<?> getPreviewClass() {
		return PreviewPanelFactory.getPreviewPanelClass(getComponentType());
	}

	@Override
	protected String getComponentType() {
		return "USER";
	}

	@Override
	protected String getLookupLabel() {
		return Messages.COMPOSITIONCONTROL_LOOKUP;
	}

	@Override
	public boolean hasCustomProperties() {
		return true;
	}
}
