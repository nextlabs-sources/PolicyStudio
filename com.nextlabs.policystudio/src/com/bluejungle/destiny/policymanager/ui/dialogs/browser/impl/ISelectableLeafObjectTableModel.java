/*
 * Created on Apr 6, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl;

import java.util.List;

import org.eclipse.swt.graphics.Image;

import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;

/**
 * Instances of {@see ISelectableLeafObjectTableModel} are responsible for
 * providing table descriptor information for Selectable Leaf Object tables used
 * with a leaf object data browser
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/browser/impl/
 *          ISelectableLeafObjectTableModel.java#2 $
 */
public interface ISelectableLeafObjectTableModel {

	/**
	 * Retrieve the title representing the leaf object data list. If multiple
	 * lists are shown within a single browser, this title will appear on the
	 * table's tab
	 * 
	 * @return the title representing the leaf object data list
	 */
	public String getTitle();

	/**
	 * Retrieve the columns headers of the columns to display in the table
	 * 
	 * @return the columns headers of the columns to display in the table
	 */
	public List<String> getColumnHeaders();

	/**
	 * Retrieve the label text to display for the specified leaf object in the
	 * specified column (0-based index)
	 * 
	 * @param leafObject
	 *            the data representing the row in the table in which the
	 *            resulting text will be displayed
	 * @param columnIndex
	 *            the index of the column in which the resulting text will be
	 *            displayed
	 * @return the label text to display for the specified leaf object in the
	 *         specified column (0-based index)
	 */
	public String getText(LeafObject leafObject, int columnIndex);

	/**
	 * The icon associated with the specified leaf object
	 * 
	 * @param leafObject
	 * @return ihe icon associated with the specified leaf object
	 */
	public Image getImage(LeafObject leafObject);

	/**
	 * Determine if this table model supports the specified leaf object
	 * 
	 * @param leafObject
	 * @return true if the table model supports the leaf object; false otherwise
	 */
	public boolean supportsLeafObject(LeafObject leafObject);

	/**
	 * Retrieve the type of leaf object that this model is associated with
	 * (Developer Note - In the future, if necessary, this can return a Set if
	 * one table must display multiple leaf object types
	 * 
	 * @return the type of leaf object that this model is associated with
	 */
	public LeafObjectType getLeafObjectType();
}
