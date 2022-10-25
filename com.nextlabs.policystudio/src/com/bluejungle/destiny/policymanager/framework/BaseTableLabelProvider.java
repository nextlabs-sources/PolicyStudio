/*
 * Created on Mar 27, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.framework;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

/**
 * A base table label provider that implements the listener related methods of
 * ITableLabelProvider
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/branch/Destiny_Beta4_Stable/main/src/client/policymanager
 *          /src/java/main/com/bluejungle/destiny/policymanager/framework/
 *          BaseTableLabelProvider.java#1 $
 */

public abstract class BaseTableLabelProvider implements ITableLabelProvider {

	// Defined as HashSet to allow clone(). See event firing
	private final HashSet<ILabelProviderListener> listeners = new HashSet<ILabelProviderListener>();

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener cannot be null.");
		}

		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {

	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener cannot be null.");
		}

		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Fires a label provider changed event to all registered listeners Only
	 * listeners registered at the time this method is called are notified.
	 * 
	 * @param event
	 *            a label provider changed event
	 * 
	 * @see ILabelProviderListener#labelProviderChanged
	 */
	@SuppressWarnings("unchecked")
	protected void fireLabelProviderChanged(
			final LabelProviderChangedEvent event) {
		/*
		 * Clone list to allow listeners to be added/removed during event
		 * propogation
		 */
		HashSet<ILabelProviderListener> clonedListeners = null;
		synchronized (listeners) {
			clonedListeners = (HashSet<ILabelProviderListener>) listeners
					.clone();
		}

		Iterator<ILabelProviderListener> listenersIterator = clonedListeners
				.iterator();
		while (listenersIterator.hasNext()) {
			ILabelProviderListener nextListener = (ILabelProviderListener) listenersIterator
					.next();
			nextListener.labelProviderChanged(event);
		}
	}
}
