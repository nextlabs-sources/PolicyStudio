/*
 * Created on Jul 2, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchImages;

import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author dstarke
 * 
 */
@SuppressWarnings("restriction")
public class SaveAction extends BaseDisableableAction {

	/**
     * 
     */
	public SaveAction() {
		super();
		setup();
	}

	/**
	 * @param text
	 */
	public SaveAction(String text) {
		super(text);
		setup();
	}

	/**
	 * @param text
	 * @param image
	 */
	public SaveAction(String text, ImageDescriptor image) {
		super(text, image);
		setup();
	}

	/**
	 * @param text
	 * @param style
	 */
	public SaveAction(String text, int style) {
		super(text, style);
		setup();
	}

	private void setup() {
		setText(ActionMessages.ACTION_SAVE);
		setToolTipText(ActionMessages.ACTION_SAVE);
		setId("save"); //$NON-NLS-1$
		setImageDescriptor(WorkbenchImages
				.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
		setDisabledImageDescriptor(WorkbenchImages
				.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT_DISABLED));
		setActionDefinitionId("org.eclipse.ui.file.save");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		GlobalState gs = GlobalState.getInstance();
		// Scott's fix for locking - start
		// gs.saveAndRefreshEditorPanel();
		gs.saveEditorPanel();
		// Scott's fix for locking - end
	}

	@Override
	public void refreshEnabledState(Set<IPolicyOrComponentData> selectedItems) {
		boolean newState = false;
		if (selectedItems.size() == 1) {
			IPolicyOrComponentData nextSelectedItem = (IPolicyOrComponentData) selectedItems
					.iterator().next();
			IHasId entity = nextSelectedItem.getEntity();

			DevelopmentStatus status = DomainObjectHelper.getStatus(entity);
			newState = (status == DevelopmentStatus.DRAFT
					|| status == DevelopmentStatus.NEW || status == DevelopmentStatus.EMPTY);
			newState &= PolicyServerProxy.canPerformAction(entity,
					DAction.WRITE);
		}

		setEnabled(newState);
	}
}
