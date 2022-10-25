package com.bluejungle.destiny.policymanager.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.model.IClientComponent;
import com.bluejungle.destiny.policymanager.model.IClientEditorPanel;
import com.bluejungle.destiny.policymanager.model.IComponentEditor;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.GlobalState.SaveCause;
import com.bluejungle.destiny.policymanager.ui.controls.CompositionControl;
import com.bluejungle.destiny.policymanager.ui.controls.PropertyExpressionControl;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

public abstract class BaseComponentEditor implements IComponentEditor {
	protected static final Point SIZE_MEMBER_LABEL = new Point(100, 20);

	protected static final int CONTROL_ID_COMPOSITION = 0;
	protected static final int CONTROL_ID_PROPERTIES = 1;

	protected Composite membersComposite = null;
	protected Composite propertiesComposite = null;

	protected Label propertiesNameLabel = null;

	protected CompositionControl compControl = null;
	protected boolean showPropertyExpressions;
	protected PropertyExpressionControl propertyExpressionControl = null;

	protected IClientEditorPanel panel;
	protected IDSpec component;

	public BaseComponentEditor(IClientEditorPanel panel,
			IClientComponent component) {
		this.panel = panel;
		this.component = component.getComponent();
	}

	public boolean isShowPropertyExpressions() {
		return showPropertyExpressions;
	}

	public void setShowPropertyExpressions(boolean showPropertyExpressions) {
		this.showPropertyExpressions = showPropertyExpressions;
	}

	public void initializeContents() {
		membersComposite = panel.addLeftEditorSectionComposite();
		initializeMembers();
		if (showPropertyExpressions) {
			propertiesComposite = panel.addLeftEditorSectionComposite();
			initializePropertyExpressions();
		}
		panel.getScrolledComposite().setMinSize(panel.getLeftComposite().computeSize(SWT.DEFAULT,SWT.DEFAULT));
	}

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
		compControl = new CompositionControl(membersComposite, SWT.NONE,
				getObjectTypeLabelText(), getMemberLabel(),
				getControlDomainObject(CONTROL_ID_COMPOSITION, component),
				panel.getEditorPanel(), CONTROL_ID_COMPOSITION, panel.isEditable(), true,
				getSpecType(), getComponentType(), label);
		compControl.setBackground(panel.getBackground());
		data = new FormData();
		data.left = new FormAttachment(0,EditorPanel.SIDE_SPACING);
		data.top = new FormAttachment(membersLabel, EditorPanel.SPACING);
		compControl.setLayoutData(data);
	}

	protected void initializePropertyExpressions() {
		FormLayout layout = new FormLayout();
		propertiesComposite.setLayout(layout);

		Composite propertiesLabel = panel.initializeSectionHeading(
				propertiesComposite,
				EditorMessages.COMPONENTEDITOR_WITH_PROPERTIES);
		FormData data = new FormData();
		data.left = new FormAttachment(0,EditorPanel.SIDE_SPACING);
		data.top = new FormAttachment(0);
		data.right = new FormAttachment(100, -EditorPanel.SIDE_SPACING);
		propertiesLabel.setLayoutData(data);

		propertiesNameLabel = new Label(propertiesComposite, SWT.NONE);
		propertiesNameLabel
				.setText(EditorMessages.COMPONENTEDITOR_PROPERTY_NAME);
		propertiesNameLabel.setBackground(panel.getBackground());
		data = new FormData();
		data.left = new FormAttachment(0, 65);
		data.top = new FormAttachment(propertiesLabel, EditorPanel.SPACING);
		propertiesNameLabel.setLayoutData(data);

		propertyExpressionControl = new PropertyExpressionControl(
				propertiesComposite, SWT.NONE, getControlDomainObject(
						CONTROL_ID_PROPERTIES, component), panel.getEditorPanel(),
				getEntityType(), CONTROL_ID_PROPERTIES, panel.isEditable(),
				panel.hasCustomProperties());
		propertyExpressionControl.setBackground(panel.getBackground());
		data = new FormData();
		data.left = new FormAttachment(0,EditorPanel.SIDE_SPACING);
		data.top = new FormAttachment(propertiesNameLabel, EditorPanel.SPACING);
		propertyExpressionControl.setLayoutData(data);
	}

	public CompositePredicate getControlDomainObject(int controlId,
			IHasId domainObject) {
		switch (controlId) {
		case CONTROL_ID_COMPOSITION:
			return (CompositePredicate) ((ICompositePredicate) ((IDSpec) domainObject)
					.getPredicate()).predicateAt(0);
		case CONTROL_ID_PROPERTIES:
			IPredicate predicate = ((ICompositePredicate) ((IDSpec) domainObject)
					.getPredicate()).predicateAt(1);
			if (predicate instanceof PredicateConstants) {
				CompositePredicate result = new CompositePredicate(
						BooleanOp.AND, new ArrayList<IPredicate>());
				result.addPredicate(PredicateConstants.TRUE);
				result.addPredicate(PredicateConstants.TRUE);
				return result;
			}
			return (CompositePredicate) ((ICompositePredicate) ((IDSpec) domainObject)
					.getPredicate()).predicateAt(1);
		}
		return null;
	}

	public void saveContents(SaveCause cause) {
		// flush buffer first
		if (propertyExpressionControl != null) {
			switch (cause) {
			case DISPOSE:
			case PART_DEACTIVATED:
				propertyExpressionControl.ignoreFocusLostOnce();
				break;
			case USER_REQUEST:
			default:
				// do nothing
				break;
			}
			propertyExpressionControl.flushBuffer();

		}

		// then save
		panel.saveContents(cause);
	}

	/**
	 * @return
	 */
	protected abstract List<String> getPropertyOperatorList();

	/**
	 * @return
	 */
	protected abstract List<String> getPropertyList();

	public void relayout() {
		if (!panel.canRelayout()) {
			return;
		}

		panel.relayout();
		membersComposite.redraw();
		if (showPropertyExpressions) {
			propertiesComposite.redraw();
			propertiesComposite.layout(true);
		}
	}

	public void relayoutContents() {
		relayoutMembers();
		relayoutProperties();
	}

	protected void relayoutMembers() {
		compControl.relayout();
	}

	protected void relayoutProperties() {
		if (propertyExpressionControl != null) {
			propertyExpressionControl.relayout();
		}
	}

	public String getDescription() {
		return ((IDSpec) component).getDescription();
	}

	public String getObjectName() {
		return DomainObjectHelper.getDisplayName(component);
	}

	public void setDescription(String description) {
		((IDSpec) component).setDescription(description);
	}

	protected abstract SpecType getSpecType();

	protected abstract String getMemberLabel();

	protected abstract String getComponentType();

	protected String getLookupLabel() {
		return null;
	}
	
	public CompositePredicate getControlDomainObject() {
		return null;
	}

	public void updateFromDomainObject() {
	}
	
	public Class<?> getPreviewClass() {
		return null;
	}
}
