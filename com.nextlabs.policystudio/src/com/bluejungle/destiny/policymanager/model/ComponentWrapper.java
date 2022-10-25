package com.bluejungle.destiny.policymanager.model;

import com.bluejungle.pf.domain.destiny.common.IDSpec;

public class ComponentWrapper implements IClientComponent {
	private IDSpec spec;

	public IDSpec getComponent() {
		return spec;
	}

	public ComponentWrapper(IDSpec spec) {
		this.spec = spec;
	}
}
