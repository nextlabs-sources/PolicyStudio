/*
 * Created on Mar 2, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

/**
 * @author fuad
 * @version $Id$:
 */

public abstract class BaseUndoElement implements IUndoElement {

	protected Long domainObjectId = null;
	boolean continuation = false;

	/**
	 * Constructor
	 * 
	 */
	public BaseUndoElement() {
		super();
	}

	public Long getDomainObjectId() {
		return this.domainObjectId;
	}

	public void setDomainObjectId(Long domainObjectId) {
		this.domainObjectId = domainObjectId;
	}

	/**
	 * @return true if the undo element is a continuation of the previous undo
	 *         element. if it is true the previous element will be undone along
	 *         with this one.
	 * 
	 */
	public boolean isContinuation() {
		return this.continuation;
	}

	/**
	 * @param continuation
	 *            specifies that this undo element is a continuation of the
	 *            previous undo element. if it is true the previous element will
	 *            be undone along with this one.
	 */
	public void setContinuation(boolean continuation) {
		this.continuation = continuation;
	}
}