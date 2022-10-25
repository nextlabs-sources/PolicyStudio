package com.bluejungle.destiny.policymanager.model;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.pf.domain.destiny.obligation.DObligationManager;
import com.bluejungle.pf.domain.destiny.obligation.IDObligationManager;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;

public class CreationExtension {
	public IDObligationManager oblMgr = (IDObligationManager) ComponentManagerFactory
			.getComponentManager().getComponent(DObligationManager.COMP_INFO);

	public boolean preCreation() {
		return true;
	}

	public void postCreation(IDPolicy policy) {
	}
}
