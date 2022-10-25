/*
 * Created on Mar 16, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.model.IClientComponent;
import com.bluejungle.destiny.policymanager.model.IComponentEditor;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.pf.destiny.lifecycle.EntityType;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/editor/ComponentEditor.java#1
 *          $:
 */

public class ComponentEditorPanel extends EditorPanel implements IEditorPanel {
	private IComponentEditor editor;

	public ComponentEditorPanel(Composite parent, int style,
			IClientComponent domainObject, String displayName) {
		super(parent, style, domainObject.getComponent());
		setDisplayName(displayName);
	}

	public IComponentEditor getEditor() {
		return editor;
	}

	public void setEditor(IComponentEditor editor) {
		this.editor = editor;
	}

	@Override
	public CompositePredicate getControlDomainObject(int controlId,
			IHasId domainObject) {
		return getControlDomainObject(controlId, domainObject);
	}

	@Override
	public String getDescription() {
		return editor.getDescription();
	}

	@Override
	protected EntityType getEntityType() {
		return editor.getEntityType();
	}

	@Override
	public String getObjectName() {
		return editor.getObjectName();
	}

	@Override
	public String getObjectTypeLabelText() {
		return editor.getObjectTypeLabelText();
	}

	@Override
	public void initializeContents() {
		editor.initializeContents();
	}

	@Override
	protected void relayoutContents() {
		editor.relayoutContents();
	}

	@Override
	public void setDescription(String description) {
		editor.setDescription(description);
	}
	@Override
	protected Class<?> getPreviewClass() {
		return editor.getPreviewClass();
	}

	@Override
	public boolean hasCustomProperties() {
		return editor.hasCustomProperties();
	}
}
