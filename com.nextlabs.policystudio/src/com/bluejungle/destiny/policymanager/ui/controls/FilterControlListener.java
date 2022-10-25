/*
 * Created on Mar 8, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.controls;

import org.eclipse.swt.internal.SWTEventListener;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/ui/controls/FilterControlListener
 *          .java#5 $:
 */

@SuppressWarnings("restriction")
public interface FilterControlListener extends SWTEventListener {

	public interface EndOfSearch {

		void endOfSearch();
	}

	public static final EndOfSearch EMPTY_END_OF_SEARCH = new EndOfSearch() {

		public void endOfSearch() {
		}
	};

	void search(FilterControlEvent e, EndOfSearch eos);

	void cancel(FilterControlEvent e);

}
