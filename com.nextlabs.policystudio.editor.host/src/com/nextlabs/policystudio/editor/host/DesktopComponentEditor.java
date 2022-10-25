/*
 * Created on Apr 19, 2005
 * 
 */
package com.nextlabs.policystudio.editor.host;

import java.util.List;

import com.bluejungle.destiny.policymanager.editor.BaseComponentEditor;
import com.bluejungle.destiny.policymanager.editor.EditorMessages;
import com.bluejungle.destiny.policymanager.model.IClientComponent;
import com.bluejungle.destiny.policymanager.model.IClientEditorPanel;
import com.bluejungle.destiny.policymanager.ui.PreviewPanelFactory;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author dstarke
 * 
 */
public class DesktopComponentEditor extends BaseComponentEditor {

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param style
	 */
	public DesktopComponentEditor(IClientEditorPanel panel,
			IClientComponent component) {
		super(panel, component);
		setShowPropertyExpressions(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.bluejungle.destiny.policymanager.editor.ComponentEditor#
	 * getPropertyOperatorList()
	 */
	@Override
	protected List<String> getPropertyOperatorList() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bluejungle.destiny.policymanager.editor.ComponentEditor#getPropertyList
	 * ()
	 */
	@Override
	protected List<String> getPropertyList() {
		return null;
	}

	@SuppressWarnings("deprecation")
	public EntityType getEntityType() {
		return EntityType.HOST;
	}

	@Override
	protected SpecType getSpecType() {
		return SpecType.HOST;
	}

	@Override
	protected String getMemberLabel() {
		return EditorMessages.DESKTOPCOMPONENTEDITOR_COMPUTERS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.bluejungle.destiny.policymanager.editor.EditorPanel#
	 * getObjectTypeLabelText()
	 */
	@Override
	public String getObjectTypeLabelText() {
		return EditorMessages.DESKTOPCOMPONENTEDITOR_COMPUTER_COMPONENT;
	}

	@Override
	public Class<?> getPreviewClass() {
		return PreviewPanelFactory.getPreviewPanelClass(getComponentType());
	}

	@Override
	protected String getComponentType() {
		return "HOST";
	}

	@Override
	public boolean hasCustomProperties() {
		return false;
	}
	
	@Override
	protected String getLookupLabel() {
		return Messages.COMPOSITIONCONTROL_LOOKUP;
	}
}