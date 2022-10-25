/*
 * Created on Sep 29, 2004
 * 
 */
package com.bluejungle.destiny.policymanager.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.CurrentPolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.IMultiContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.SelectedItemsModifiedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.dialogs.LoginDialog;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author fuad
 * 
 */
public class PolicyStudioApplication implements IApplication {
	/**
	 * Set up the services used by policy author
	 */
	private void setupServices() {
		/*
		 * Setup the Event Manager
		 */
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		IEventManager eventManager = (IEventManager) componentManager
				.getComponent(EventManagerImpl.class);

		// Setup event framework event listener
		eventManager.registerListener(
				new ObjectModifiedEventPropogationListener(eventManager),
				ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT);
	}

	/**
	 * @author sgoldstein
	 */
	public class ObjectModifiedEventPropogationListener implements
			IMultiContextualEventListener {

		private IEventManager eventManager;

		/**
		 * Create an instance of ObjectModifiedEventPropogationListener
		 * 
		 * @param eventManager
		 */
		public ObjectModifiedEventPropogationListener(IEventManager eventManager) {
			if (eventManager == null) {
				throw new NullPointerException("eventManager cannot be null.");
			}

			this.eventManager = eventManager;
		}

		/**
		 * @see com.bluejungle.destiny.policymanager.event.IMultiContextualEventListener#onEvents(java.util.Set)
		 */
		public void onEvents(Set<IContextualEvent> events) {
			Map<Long, IContextualEvent> contextObjectIdsToEventsMap = new HashMap<Long, IContextualEvent>();

			for (IContextualEvent event : events) {
				Long nextEventContextId = ((IHasId) event.getEventContext())
						.getId();
				contextObjectIdsToEventsMap.put(nextEventContextId, event);
			}

			GlobalState globalState = GlobalState.getInstance();
			IHasId currentObject = globalState.getCurrentObject();
			if (currentObject != null) {
				Long currentObjectId = currentObject.getId();
				if (contextObjectIdsToEventsMap.containsKey(currentObjectId)) {
					PolicyOrComponentModifiedEvent objectModifiedEvent = (PolicyOrComponentModifiedEvent) contextObjectIdsToEventsMap
							.get(currentObjectId);
					IPolicyOrComponentData objectModifiedEventData = objectModifiedEvent
							.getEventContextAsPolicyOrComponentData();
					CurrentPolicyOrComponentModifiedEvent currentObjectModifiedEvent = new CurrentPolicyOrComponentModifiedEvent(
							objectModifiedEventData);
					this.eventManager.fireEvent(currentObjectModifiedEvent);
				}
			}

			Set<DomainObjectDescriptor> selectedItems = globalState
					.getCurrentSelection();
			Iterator<DomainObjectDescriptor> selectedItemsIterator = selectedItems
					.iterator();
			boolean selectedItemFound = false;
			while ((selectedItemsIterator.hasNext()) && (!selectedItemFound)) {
				DomainObjectDescriptor nextSelectedItem = (DomainObjectDescriptor) selectedItemsIterator
						.next();
				if (contextObjectIdsToEventsMap.containsKey(nextSelectedItem
						.getId())) {
					SelectedItemsModifiedEvent selectedObjectsModifiedEvent = new SelectedItemsModifiedEvent(
							selectedItems);
					this.eventManager.fireEvent(selectedObjectsModifiedEvent);
					selectedItemFound = true;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.
	 * IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		setupServices();

		Display display = PlatformUI.createDisplay();
		Shell shell = display.getActiveShell();
		Platform.endSplash();
		try {
			LoginDialog loginDialog = new LoginDialog(shell);
			if (loginDialog.open() != Window.OK) {
				return IApplication.EXIT_OK;
			}

			// store the user name and server
			GlobalState.user = loginDialog.getUsername();
			GlobalState.server = loginDialog.getPolicyServer();

			int returnCode = PlatformUI.createAndRunWorkbench(display,
					new PolicyStudioWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed()) {
					workbench.close();
					ResourceManager.dispose();
				}
			}
		});
	}
}
