/*
 * Created on Mar 27, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.model;

/**
 * Represents a single realm as provided by the Destiny Information Directory
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/model/IRealm.java#1 $
 */

public interface IRealm {

	/**
	 * A realm which contains all other realms. Useful for searching for
	 * elements in all realms
	 */
	public static final IRealm ALL_REALMS_REALM = new IRealm() {

		/**
		 * @see com.bluejungle.destiny.policymanager.model.IRealm#getId()
		 */
		public String getId() {
			throw new UnsupportedOperationException(
					"ID does not exists on All Realms Realm");
		}

		/**
		 * @see com.bluejungle.destiny.policymanager.model.IRealm#getTitle()
		 */
		public String getTitle() {
			return "All"; // FIX ME - I18N;
		}

		/**
		 * @see com.bluejungle.destiny.policymanager.model.IRealm#getEnrollmentType()
		 */
		public EnrollmentType getEnrollmentType() {
			return EnrollmentType.UNKNOWN;
		}
	};

	/*
	 * Retrieve the title (the display name) of this realm
	 */
	public String getTitle();

	/**
	 * Retrieve the ID of this realm
	 * 
	 * @return the ID of this realm
	 */
	public String getId();

	/**
	 * Retrieve the enrollment type for this realm
	 * 
	 * @return the enrollment type for this realm
	 */
	public EnrollmentType getEnrollmentType();
}
