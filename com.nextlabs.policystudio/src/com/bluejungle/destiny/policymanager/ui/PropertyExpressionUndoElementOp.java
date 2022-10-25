/*
 * Created on Mar 2, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/ui/PropertyExpressionUndoElementOp
 *          .java#2 $:
 */

public class PropertyExpressionUndoElementOp extends EnumBase {
	private static final long serialVersionUID = 3539380063013325616L;

	public static final String ADD_NAME = "ADD";
	public static final int ADD_TYPE = 1;

	public static final String REMOVE_NAME = "REMOVE";
	public static final int REMOVE_TYPE = 2;

	public static final String CHANGE_ATTRIBUTE_NAME = "CHANGE_ATTRIBUTE";
	public static final int CHANGE_ATTRIBUTE_TYPE = 3;

	public static final String CHANGE_OP_NAME = "CHANGE_OP";
	public static final int CHANGE_OP_TYPE = 4;

	public static final String CHANGE_VALUE_NAME = "CHANGE_VALUE";
	public static final int CHANGE_VALUE_TYPE = 5;

	public static final PropertyExpressionUndoElementOp ADD = new PropertyExpressionUndoElementOp(
			ADD_NAME, ADD_TYPE);
	public static final PropertyExpressionUndoElementOp REMOVE = new PropertyExpressionUndoElementOp(
			REMOVE_NAME, REMOVE_TYPE);
	public static final PropertyExpressionUndoElementOp CHANGE_ATTRIBUTE = new PropertyExpressionUndoElementOp(
			CHANGE_ATTRIBUTE_NAME, CHANGE_ATTRIBUTE_TYPE);
	public static final PropertyExpressionUndoElementOp CHANGE_OP = new PropertyExpressionUndoElementOp(
			CHANGE_OP_NAME, CHANGE_OP_TYPE);
	public static final PropertyExpressionUndoElementOp CHANGE_VALUE = new PropertyExpressionUndoElementOp(
			CHANGE_VALUE_NAME, CHANGE_VALUE_TYPE);

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param type
	 */
	private PropertyExpressionUndoElementOp(String name, int type) {
		super(name, type, PropertyExpressionUndoElementOp.class);
	}
}
