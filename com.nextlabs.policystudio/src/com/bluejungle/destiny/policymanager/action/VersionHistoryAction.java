package com.bluejungle.destiny.policymanager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.VersionHistoryDialog;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

public class VersionHistoryAction extends Action {
	private DomainObjectDescriptor descriptor;

	/**
	 * Constructor
	 * 
	 * @param text
	 */
	public VersionHistoryAction(String text) {
		super(text);
	}

	@Override
	public void run() {
		IHasId hasId = (IHasId) PolicyServerProxy
				.getEntityForDescriptor(descriptor);
		if (hasId == null) {
			return;
		}

		VersionHistoryDialog dlg = new VersionHistoryDialog(Display
				.getCurrent().getActiveShell(), hasId);
		dlg.open();
	}

	public void setDomainObjectDescriptor(DomainObjectDescriptor descriptor) {
		this.descriptor = descriptor;
	}
}