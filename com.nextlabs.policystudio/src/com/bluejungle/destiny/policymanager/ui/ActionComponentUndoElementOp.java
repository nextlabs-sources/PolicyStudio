/*
 * Created on May 31, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * @author dstarke
 */
public class ActionComponentUndoElementOp extends EnumBase {
	private static final long serialVersionUID = -4245234596484314518L;

	public static final String ADD_ACTION_NAME = "ADD_ACTION";
	public static final int ADD_ACTION_TYPE = 0;

	public static final String REMOVE_ACTION_NAME = "REMOVE_ACTION";
	public static final int REMOVE_ACTION_TYPE = 1;

	public static final ActionComponentUndoElementOp ADD_ACTION = new ActionComponentUndoElementOp(
			ADD_ACTION_NAME, ADD_ACTION_TYPE);
	public static final ActionComponentUndoElementOp REMOVE_ACTION = new ActionComponentUndoElementOp(
			REMOVE_ACTION_NAME, REMOVE_ACTION_TYPE);

	private ActionComponentUndoElementOp(String name, int type) {
		super(name, type, ActionComponentUndoElementOp.class);
	}

}
