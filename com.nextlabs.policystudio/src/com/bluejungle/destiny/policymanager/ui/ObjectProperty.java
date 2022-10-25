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
 *          /com/bluejungle/destiny/policymanager/ui/ObjectProperty.java#2 $:
 */

public class ObjectProperty extends EnumBase {
	private static final long serialVersionUID = 8640543513898445379L;

	public static final String DESCRIPTION_NAME = "Description";
	public static final int DESCRIPTION_TYPE = 1;

	public static final ObjectProperty DESCRIPTION = new ObjectProperty(
			DESCRIPTION_NAME, DESCRIPTION_TYPE);

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param type
	 */
	private ObjectProperty(String name, int type) {
		super(name, type, ObjectProperty.class);
	}

}
