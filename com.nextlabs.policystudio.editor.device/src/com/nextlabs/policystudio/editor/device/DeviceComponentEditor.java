/*
 * Created on Apr 19, 2005
 * 
 */
package com.nextlabs.policystudio.editor.device;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.editor.BaseComponentEditor;
import com.bluejungle.destiny.policymanager.editor.EditorPanel;
import com.bluejungle.destiny.policymanager.model.IClientComponent;
import com.bluejungle.destiny.policymanager.model.IClientEditorPanel;
import com.bluejungle.destiny.policymanager.ui.PreviewPanelFactory;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author dstarke
 * 
 */
public class DeviceComponentEditor extends BaseComponentEditor {
	protected CompositionControl myCompControl = null;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param style
	 */
	public DeviceComponentEditor(IClientEditorPanel panel,
			IClientComponent component) {
		super(panel, component);
		setShowPropertyExpressions(false);
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
		return EntityType.RESOURCE;
	}

	@Override
	protected SpecType getSpecType() {
		return SpecType.RESOURCE;
	}

	@Override
	protected String getMemberLabel() {
		return EditorMessages.DEVICES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.bluejungle.destiny.policymanager.editor.EditorPanel#
	 * getObjectTypeLabelText()
	 */
	public String getObjectTypeLabelText() {
		return EditorMessages.DEVICE_COMPONENT;
	}

	@Override
	public Class<?> getPreviewClass() {
		return PreviewPanelFactory.getPreviewPanelClass(getComponentType());
	}

	@Override
	protected String getComponentType() {
		return "DEVICE";
	}

	public boolean hasCustomProperties() {
		return true;
	}

	@Override
	protected void initializeMembers() {
		FormLayout layout = new FormLayout();
		membersComposite.setLayout(layout);

		Composite membersLabel = panel.initializeSectionHeading(
				membersComposite, EditorMessages.COMPONENTEDITOR_MEMBERS);
		FormData data = new FormData();
		data.left = new FormAttachment(0,EditorPanel.SIDE_SPACING);
		data.top = new FormAttachment(0,EditorPanel.TOP_SPACKING);
		data.right = new FormAttachment(100, -EditorPanel.SIDE_SPACING);
		membersLabel.setLayoutData(data);

		String label = getLookupLabel();
		myCompControl = new CompositionControl(membersComposite, SWT.NONE,
				getObjectTypeLabelText(), getMemberLabel(),
				getControlDomainObject(CONTROL_ID_COMPOSITION, component),
				panel.getEditorPanel(), CONTROL_ID_COMPOSITION, panel
						.isEditable(), true, getSpecType(), getComponentType(),
				label);
		myCompControl.setBackground(panel.getBackground());
		data = new FormData();
		data.left = new FormAttachment(0,EditorPanel.SIDE_SPACING);
		data.top = new FormAttachment(membersLabel, EditorPanel.SPACING);
		myCompControl.setLayoutData(data);
	}

	@Override
	protected void relayoutMembers() {
		myCompControl.relayout();
	}
}