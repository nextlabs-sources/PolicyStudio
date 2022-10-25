package com.bluejungle.destiny.policymanager.model;

import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.pf.destiny.lifecycle.EntityType;

public interface IComponentEditor {
	void updateFromDomainObject();

	String getDescription();

	CompositePredicate getControlDomainObject();

	EntityType getEntityType();

	String getObjectName();

	String getObjectTypeLabelText();

	void initializeContents();

	void relayoutContents();

	void setDescription(String description);

	boolean isShowPropertyExpressions();

	void setShowPropertyExpressions(boolean showPropertyExpressions);

	Class<?> getPreviewClass();

	boolean hasCustomProperties();
}
