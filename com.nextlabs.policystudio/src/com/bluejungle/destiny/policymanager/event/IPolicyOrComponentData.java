/*
 * Created on Jan 30, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event;

import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;

/**
 * IPolicyOrComponentData is used to hold information about a policy or
 * component during the lifetime of event propogation. It's being created so
 * that different event handlers do not have to individually received the same
 * data from the server about objects relevant to the event.
 * 
 * In the future, this information should most likely be moved to standard PF
 * objects. It's a little out of place here
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/event/IPolicyOrComponentData.java#1
 *          $
 */

public interface IPolicyOrComponentData {

	/**
	 * Retrieve the descriptor for this policy or component. Note that the
	 * information in the descriptor may be stale. For the latest data, please
	 * retrieve the entity through {@see #getEntity()} instead
	 * 
	 * @return
	 */
	public abstract DomainObjectDescriptor getDescriptor();

	/**
	 * Retrieve the policy or component entity.
	 * 
	 * @return the entity.
	 */
	public abstract IHasId getEntity();

	/**
	 * Retrieve usage information about the policy or component.
	 * 
	 * @return the entityUsage.
	 * @throws PolicyEditorException
	 */
	public abstract DomainObjectUsage getEntityUsage()
			throws PolicyEditorException;

}