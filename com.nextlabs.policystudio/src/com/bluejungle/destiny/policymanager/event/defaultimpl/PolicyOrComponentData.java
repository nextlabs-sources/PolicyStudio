/*
 * Created on Jan 26, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event.defaultimpl;

import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;

/**
 * PolicyOrComponentData is used within events to carry information about
 * policies and components involved in the events. For example, an instance of
 * PolicyOrComponentData which represents the currently focused policy or
 * component editor within Policy Author can be retrieved from a {@see
 * com.bluejungle.destiny.policymanager.event.
 * CurrentPolicyOrComponentModifiedEvent} instance. Having this information
 * available on the event allows event listeners to share it, reducing the
 * number of web service calls which must be made to the server during the event
 * propogation lifecycle
 * 
 * @author sgoldstein
 */
public class PolicyOrComponentData implements IPolicyOrComponentData {

	private DomainObjectDescriptor descriptor;
	private IHasId entity;
	private DomainObjectUsage entityUsage;

	/**
	 * Create an instance of PolicyOrComponentData
	 * 
	 * @param nextSelectedItem
	 */
	public PolicyOrComponentData(DomainObjectDescriptor descriptor) {
		if (descriptor == null) {
			throw new NullPointerException("descriptor cannot be null.");
		}
		this.descriptor = descriptor;
	}

	/**
	 * Create an instance of PolicyOrComponentData
	 * 
	 * @param entity
	 */
	public PolicyOrComponentData(IHasId entity) {
		if (entity == null) {
			throw new NullPointerException("entity cannot be null.");
		}
		this.entity = entity;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData#getDescriptor()
	 */
	public DomainObjectDescriptor getDescriptor() {
		if (descriptor == null) {
			// try to create it lazily from the entity
			if (entity != null) {
				descriptor = DomainObjectHelper.getCachedDescriptor(entity);
			} else {
				throw new IllegalStateException(
						"Both descriptor and entity are null.");
			}
		}

		return descriptor;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData#getEntity()
	 */
	public IHasId getEntity() {
		if (entity == null) {
			// try to create it lazily from the entity
			if (descriptor != null) {
				entity = (IHasId) PolicyServerProxy
						.getEntityForDescriptor(descriptor);
			} else {
				throw new IllegalStateException(
						"Both descriptor and entity are null.");
			}
		}

		return entity;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData#getEntityUsage()
	 */
	public DomainObjectUsage getEntityUsage() throws PolicyEditorException {
		if (entityUsage == null) {
			DomainObjectDescriptor domainObjectDescriptor = getDescriptor();
			if (domainObjectDescriptor == null)
				return null;
			entityUsage = PolicyServerProxy.getUsage(domainObjectDescriptor);
		}

		return entityUsage;
	}
}
