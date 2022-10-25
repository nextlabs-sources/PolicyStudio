package com.bluejungle.destiny.policymanager.model;

import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.pf.destiny.lifecycle.EntityType;

public interface IPolicyEditor {
	void updateFromDomainObject();

	String getDescription();

	CompositePredicate getControlDomainObject();

	EntityType getEntityType();

	String getObjectName();

	String getObjectTypeLabelText();

	public void initializeContents();

	void relayoutContents();

	void setDescription(String description);
}
