/*
 * Created on Sep 1, 2005 All sources, binaries and HTML pages (C) copyright
 * 2005 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.parser.PQLParser;

/**
 * @author aweber
 */
public class DomainObjectInput extends PlatformObject implements IEditorInput,
		IPersistableElement {

	private IHasId domainObject;

	public DomainObjectInput(IHasId anObject) {
		this.domainObject = anObject;
	}

	public IHasId getDomainObject() {
		return domainObject;
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.getMissingImageDescriptor();
	}

	public String getName() {
		String n = DomainObjectHelper.getDisplayName(domainObject);
		int i = n.lastIndexOf(PQLParser.SEPARATOR);
		if (i != -1) {
			return n.substring(i + 1);
		} else {
			return n;
		}
	}

	public IPersistableElement getPersistable() {
		return this;
	}

	public String getToolTipText() {
		return DomainObjectHelper.getDisplayName(domainObject);
	}

	@Override
	public int hashCode() {
		return getDomainObject().hashCode();
	}

	@Override
	public boolean equals(Object anotherObject) {
		if (anotherObject == null) {
			return false;
		} else if (anotherObject instanceof DomainObjectInput) {
			if (((DomainObjectInput) anotherObject).getDomainObject() == null
					|| getDomainObject() == null) {
				return false;
			}
			return DomainObjectHelper.isSame(
					((DomainObjectInput) anotherObject).getDomainObject(),
					getDomainObject());
		} else {
			return false;
		}
	}

	/*
	 * IPersistableElement
	 */
	public void saveState(IMemento memento) {
	}

	public String getFactoryId() {
		return "some id";
	}
}
