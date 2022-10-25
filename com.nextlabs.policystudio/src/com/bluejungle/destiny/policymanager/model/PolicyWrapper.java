package com.bluejungle.destiny.policymanager.model;

import com.bluejungle.pf.domain.destiny.policy.IDPolicy;

public class PolicyWrapper implements IClientPolicy {
	private IDPolicy policy;

	public PolicyWrapper(IDPolicy policy) {
		this.policy = policy;
	}

	public IDPolicy getPolicy() {
		return policy;
	}
}
