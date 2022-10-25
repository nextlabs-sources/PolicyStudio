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
 *          bluejungle/destiny/policymanager/ui/CompositionUndoElementOp.java#5
 *          $:
 */

public class CompositionUndoElementOp extends EnumBase {
	private static final long serialVersionUID = -5975919834376643547L;

	public static final String ADD_NAME = "ADD";
	public static final int ADD_TYPE = 1;

	public static final String REMOVE_NAME = "REMOVE";
	public static final int REMOVE_TYPE = 2;

	public static final String CHANGE_OP_NAME = "CHANGE_OP";
	public static final int CHANGE_OP_TYPE = 3;

	public static final String ADD_REF_NAME = "ADD_REF";
	public static final int ADD_REF_TYPE = 4;

	public static final String REMOVE_REF_NAME = "REMOVE_REF";
	public static final int REMOVE_REF_TYPE = 5;

	public static final String REPLACE_NAME = "REPLACE";
	public static final int REPLACE_TYPE = 6;

	public static final CompositionUndoElementOp ADD = new CompositionUndoElementOp(
			ADD_NAME, ADD_TYPE);
	public static final CompositionUndoElementOp REMOVE = new CompositionUndoElementOp(
			REMOVE_NAME, REMOVE_TYPE);
	public static final CompositionUndoElementOp CHANGE_OP = new CompositionUndoElementOp(
			CHANGE_OP_NAME, CHANGE_OP_TYPE);
	public static final CompositionUndoElementOp ADD_REF = new CompositionUndoElementOp(
			ADD_REF_NAME, ADD_REF_TYPE);
	public static final CompositionUndoElementOp REMOVE_REF = new CompositionUndoElementOp(
			REMOVE_REF_NAME, REMOVE_REF_TYPE);
	public static final CompositionUndoElementOp REPLACE = new CompositionUndoElementOp(
			REPLACE_NAME, REPLACE_TYPE);

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param type
	 */
	private CompositionUndoElementOp(String name, int type) {
		super(name, type, CompositionUndoElementOp.class);
	}

}
