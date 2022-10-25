/*
 * Created on Apr 6, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.framework.standardlisteners;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * A listner which will auto resize table columns for a specified to table. The
 * columns are resized to have an equal amount of white space and take up the
 * entire table width
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/branch/Destiny_Beta4_Stable/main/src/client/policymanager
 *          /src/java/main/com/bluejungle/destiny/policymanager/framework/
 *          standardlisteners/TableColumnResizeListener.java#1 $
 */

public class TableColumnResizeListener implements Listener {

	private final Table tableToResize;
	private final Composite boundingComposite;

	/**
	 * Create an instance of TableColumnResizeListener
	 * 
	 * @param tableToResize
	 */
	public TableColumnResizeListener(Table tableToResize) {
		if (tableToResize == null) {
			throw new NullPointerException("tableToResize cannot be null.");
		}

		this.tableToResize = tableToResize;
		this.boundingComposite = tableToResize;
	}

	/**
	 * Create an instance of TableColumnResizeListener
	 * 
	 * @param tableToResize
	 * @param boundingComposite
	 */
	public TableColumnResizeListener(Table tableToResize,
			Composite boundingComposite) {
		if (tableToResize == null) {
			throw new NullPointerException(
					"tableToResize, Composite cannot be null.");
		}

		if (boundingComposite == null) {
			throw new NullPointerException("boundingComposite cannot be null.");
		}

		this.tableToResize = tableToResize;
		this.boundingComposite = boundingComposite;
	}

	/**
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		GridLayout layout = new GridLayout();

		int width = 0;
		TableColumn[] tableColumns = tableToResize.getColumns();
		for (TableColumn column : tableColumns) {
			column.pack();
			width += column.getWidth();
		}

		/*
		 * If the actual width is larger than total column width, disperse extra
		 * space in columns
		 */
		int actualWidth = boundingComposite.getBounds().width
				- layout.marginWidth * 2 - tableColumns.length
				* tableToResize.getBorderWidth() - tableColumns.length
				* tableToResize.getGridLineWidth() - 1;
		// Account for scrollbar, if one exists
		ScrollBar veriticalScrollBar = tableToResize.getVerticalBar();
		if (veriticalScrollBar.getMaximum() > 0) {
			actualWidth -= veriticalScrollBar.getSize().x;
		}

		if ((actualWidth > width)) {
			int widthDifference = actualWidth - width;
			int baseColumnWidthDifference = widthDifference
					/ tableColumns.length; // int
			/* arithmetic will truncate remainder */
			int remainingDifference = widthDifference % tableColumns.length;
			for (int i = 0; i < tableColumns.length; i++) {
				int newColumnWidth = tableColumns[i].getWidth()
						+ baseColumnWidthDifference;
				if (i < remainingDifference) {
					newColumnWidth++;
				}

				tableColumns[i].setWidth(newColumnWidth);
			}
		}
	}
}