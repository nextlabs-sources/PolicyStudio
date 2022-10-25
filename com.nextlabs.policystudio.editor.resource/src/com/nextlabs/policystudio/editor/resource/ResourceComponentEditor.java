/*
 * Created on Apr 21, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.policystudio.editor.resource;

import com.bluejungle.destiny.policymanager.editor.BasePropertyComponentEditor;
import com.bluejungle.destiny.policymanager.model.IClientComponent;
import com.bluejungle.destiny.policymanager.model.IClientEditorPanel;
import com.bluejungle.destiny.policymanager.ui.PreviewPanelFactory;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author dstarke, bo meng
 * 
 */
public class ResourceComponentEditor extends BasePropertyComponentEditor {

	public ResourceComponentEditor(IClientEditorPanel panel,
			IClientComponent component) {
		super(panel, component);
		setShowPropertyExpressions(true);
	}

	@SuppressWarnings("deprecation")
	public EntityType getEntityType() {
		return EntityType.RESOURCE;
	}

	@Override
	protected SpecType getSpecType() {
		return SpecType.RESOURCE;
	}

	@Override
	protected String getMemberLabel() {
		return Messages.RESOURCECOMPONENTEDITOR_DOCUMENTS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.bluejungle.destiny.policymanager.editor.EditorPanel#
	 * getObjectTypeLabelText()
	 */
	public String getObjectTypeLabelText() {
		return Messages.RESOURCECOMPONENTEDITOR_DOCUMENT_COMPONENT;
	}

	@Override
	public Class<?> getPreviewClass() {
		return PreviewPanelFactory.getPreviewPanelClass(getComponentType());
	}

	@Override
	public String getComponentType() {
		return "RESOURCE";
	}
}
