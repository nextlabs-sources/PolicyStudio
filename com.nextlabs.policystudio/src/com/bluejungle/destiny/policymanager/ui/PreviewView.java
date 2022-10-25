package com.bluejungle.destiny.policymanager.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.editor.DomainObjectEditor;
import com.bluejungle.destiny.policymanager.event.EventType;
import com.bluejungle.destiny.policymanager.event.IEvent;
import com.bluejungle.destiny.policymanager.event.IEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;

public class PreviewView extends ViewPart implements GlobalState.IPartObserver {

	public static String ID = "com.bluejungle.destiny.policymanager.ui.PreviewView";

	private Composite rootContainer;
	private StackLayout rootLayout;
	private Composite noPreview;
	private Map<DomainObjectEditor, IPreviewPanel> editorToPreview;
	private IPreviewPanel currentPreviewPanel;
	private CurrentObjectChangedListener currentObjectChangedListener = new CurrentObjectChangedListener();

	public PreviewView() {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		IEventManager eventManager = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
		eventManager.registerListener(currentObjectChangedListener,
				EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT);

		GlobalState.getInstance().addPartObserver(this);
		editorToPreview = new HashMap<DomainObjectEditor, IPreviewPanel>();
	}

	@Override
	public void dispose() {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		IEventManager eventManager = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
		eventManager.unregisterListener(currentObjectChangedListener,
				EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT);

		GlobalState.getInstance().removePartObserver(this);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		rootContainer = new Composite(parent, SWT.NONE);
		rootLayout = new StackLayout();
		rootContainer.setLayout(rootLayout);

		noPreview = new Composite(rootContainer, SWT.NONE);
		noPreview.setLayout(new FillLayout(SWT.VERTICAL));

		new Label(noPreview, SWT.NONE);
		Label noPreviewLabel = new Label(noPreview, SWT.CENTER);
		noPreviewLabel.setFont(FontBundle.ARIAL_16_NORMAL);
		noPreviewLabel.setText(ApplicationMessages.PREVIEWVIEW_NO_PREVIEW);
		new Label(noPreview, SWT.NONE);

		updateFromCurrentObject();
	}

	@Override
	public void setFocus() {
	}

	public void workbenchInitialized() {
	}

	public void partOpened(IWorkbenchPart aPart) {
	}

	public void partClosed(IWorkbenchPart aPart) {
		if (editorToPreview.containsKey(aPart)) {
			IPreviewPanel p = (IPreviewPanel) editorToPreview.get(aPart);
			p.getRootControl().dispose();
			editorToPreview.remove(aPart);
		}
	}

	/**
	 * 
	 */
	private void updateFromCurrentObject() {
		DomainObjectEditor gae = GlobalState.getInstance().getActiveEditor();
		Class<?> previewClass = null;

		if (gae == null || (previewClass = gae.getPreviewClass()) == null) {
			rootLayout.topControl = noPreview;
		} else {
			IPreviewPanel currentPanel = (IPreviewPanel) editorToPreview
					.get(gae);
			if (currentPanel == null) {
				try {
					currentPanel = (IPreviewPanel) previewClass.newInstance();
				} catch (Exception e) {
					LoggingUtil.logError(Activator.ID,
							"error update from current object", e);
					currentPanel = null;
				}
				if (currentPanel != null) {
					currentPanel.setParentEditor(gae);
					editorToPreview.put(gae, currentPanel);
					currentPanel.createControls(rootContainer);
				}
			}

			if (currentPreviewPanel != null) {
				if (currentPreviewPanel == currentPanel) {
					return;
				} else {
					rootLayout.topControl = null;
				}
			}

			if (currentPanel != null) {
				rootLayout.topControl = currentPanel.getRootControl();
			} else {
				rootLayout.topControl = noPreview;
			}
		}

		if (!rootContainer.isDisposed())
			rootContainer.layout(true);
	}

	public interface IPreviewPanel {

		public void setParentEditor(DomainObjectEditor anEditor);

		public void createControls(Composite parent);

		public Composite getRootControl();

		public void setHasId(IHasId hasId);
	}

	/**
	 * @author sgoldstein
	 */
	public class CurrentObjectChangedListener implements IEventListener {

		/**
		 * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
		 */
		public void onEvent(IEvent event) {
			PreviewView.this.updateFromCurrentObject();
		}
	}
}
