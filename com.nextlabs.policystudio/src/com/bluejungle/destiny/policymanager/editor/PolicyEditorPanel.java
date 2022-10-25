package com.bluejungle.destiny.policymanager.editor;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.model.IClientPolicy;
import com.bluejungle.destiny.policymanager.model.IPolicyEditor;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.pf.destiny.lifecycle.EntityType;

public class PolicyEditorPanel extends EditorPanel {
	private IPolicyEditor editor;

	public class ObjectModifiedListner implements IContextualEventListener {
		/**
		 * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
		 */
		public void onEvent(IContextualEvent event) {
			editor.updateFromDomainObject();
		}
	}

	public PolicyEditorPanel(Composite parent, int style,
			IClientPolicy domainObject, String displayName) {
		super(parent, style, domainObject.getPolicy());
		setDisplayName(displayName);
		final ObjectModifiedListner objectModifiedListner = new ObjectModifiedListner();

		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		final IEventManager eventManager = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
		eventManager.registerListener(objectModifiedListner,
				ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT,
				domainObject);

		addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				eventManager.unregisterListener(objectModifiedListner,
						ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT,
						PolicyEditorPanel.this.domainObject);
			}
		});
	}

	public IPolicyEditor getEditor() {
		return editor;
	}

	public void setEditor(IPolicyEditor editor) {
		this.editor = editor;
	}

	@Override
	public CompositePredicate getControlDomainObject(int controlId,
			IHasId domainObject) {
		return editor.getControlDomainObject();
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
		return null;
	}

	@Override
	public boolean hasCustomProperties() {
		return false;
	}
}
