/*
 * Created on Nov 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.editor.DomainObjectEditor;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.common.IDSpec;

/**
 * Action to Update the list of computers with enforcers in the Policy Database
 * 
 * @author sgoldstein
 */
public class UpdateComputersWithAgentsDialog extends Dialog {
	private static final IEventManager EVENT_MANAGER;
	static {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		EVENT_MANAGER = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
	}

	private static final String COMPUTERS_WITH_AGENTS_COMPONENT_NAME = "HOST/Computers With Enforcers";
	private CLabel dialogLabel;
	private Throwable componentUpdateThrowable;

	/**
	 * Create an instance of UpdateComputersWithAgentsDialog
	 * 
	 * @param activeShell
	 */
	public UpdateComputersWithAgentsDialog(Shell activeShell) {
		super(activeShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	@Override
	public void create() {
		super.create();
		final Thread animationThread = new Thread(new DialogAnimationRunnable());
		animationThread.start();

		new Thread(new Runnable() {

			/**
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				try {
					performComponentUpdate();
				} catch (Exception exception) {
					// Catch all exceptions. Allows us to end animation and
					// display error. Is there a better design which would allow
					// runtime exceptiosn to automatically lead to error without
					// catching them explicitly?
					UpdateComputersWithAgentsDialog.this.componentUpdateThrowable = exception;
				}
				animationThread.interrupt();
			}
		}).start();
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		dialogLabel = new CLabel(composite, SWT.WRAP);
		dialogLabel
				.setText(DialogMessages.UPDATECOMPUTERWITHAGENTSDIALOG_STATUS_UPDATING);
		dialogLabel.setImage(ImageBundle.ANIMATED_BUSY_IMAGE[0]);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		dialogLabel.setLayoutData(data);

		applyDialogFont(composite);
		return composite;
	}

	@Override
	protected void okPressed() {
		/*
		 * Ideally, this would run when the animation in the dialog is running.
		 * But, unfortunately, SWT only has one UI thread, making it impossible
		 * to run the animated image and update the editor window (yes, not good
		 * desig on their part!). And, since this process takes some time,
		 * running it while the dialog is still open, will confuse end users.
		 * Therefore, althoug no ideal, we'll run it after the OK is pressed
		 */
		IDSpec computersWithAgentComponent = PolicyServerProxy.getEntityByName(
				COMPUTERS_WITH_AGENTS_COMPONENT_NAME, EntityType.COMPONENT);
		EntityInfoProvider.updateComponentList("HOST");

		PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent(
				computersWithAgentComponent);
		EVENT_MANAGER.fireEvent(objectModifiedEvent);

		/*
		 * Only run the next part if the current editor is editing this
		 * component. This saves a little time when not looking at the component
		 * and doesn't confuse the user when something new is placed before them
		 */
		GlobalState globalState = GlobalState.getInstance();
		DomainObjectEditor currentEditor = globalState.getActiveEditor();
		if ((currentEditor != null)
				&& (computersWithAgentComponent.equals(currentEditor
						.getDomainObject()))) {
			globalState
					.forceLoadObjectInEditorPanel(computersWithAgentComponent);
		}
		super.okPressed();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	/**
	 * Perform the component update
	 * 
	 * @throws PolicyEditorException
	 *             if an error occurs while performing the update
	 */
	private void performComponentUpdate() throws PolicyEditorException {
		PolicyServerProxy.updateComputersWithAgents();
	}

	/**
	 * Updates the dialog state after component update
	 */
	private void updateDialogAfterComponentUpdate() {
		if (componentUpdateThrowable == null) {
			getButton(IDialogConstants.OK_ID).setEnabled(true);
			dialogLabel.setImage(ImageBundle.STATIC_BUSY_IMAGE);
			dialogLabel
					.setText(DialogMessages.UPDATECOMPUTERWITHAGENTSDIALOG_STATUS_COMPLETE);
		} else {
			LoggingUtil
					.logError(
							Activator.ID,
							"Failed to update Computers With Enforcers desktop component",
							componentUpdateThrowable);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
			dialogLabel.setImage(ImageBundle.STATIC_BUSY_IMAGE);
			dialogLabel
					.setText(DialogMessages.UPDATECOMPUTERWITHAGENTSDIALOG_STATUS_ERROR);
			dialogLabel.setForeground(ResourceManager.getColor(255, 0, 0));
		}
	}

	/**
	 * A inner class which controls the dialog animation and update
	 * 
	 * @author sgoldstein
	 */
	private class DialogAnimationRunnable implements Runnable {

		private final Display display;
		private int imageNumber = 0;

		private DialogAnimationRunnable() {
			display = Display.getCurrent();
		}

		public void run() {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					Thread.sleep(150);
					imageNumber = (imageNumber != ImageBundle.ANIMATED_BUSY_IMAGE.length - 1) ? imageNumber + 1
							: 0;
					display.syncExec(new Runnable() {

						/**
						 * @see java.lang.Runnable#run()
						 */
						public void run() {
							UpdateComputersWithAgentsDialog.this.dialogLabel
									.setImage(ImageBundle.ANIMATED_BUSY_IMAGE[imageNumber]);
						}
					});
				}
			} catch (InterruptedException exception) {
				// Animation is complete
			}

			display.syncExec(new Runnable() {

				/**
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					updateDialogAfterComponentUpdate();
				}
			});
		}
	}
}
