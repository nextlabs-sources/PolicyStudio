/*
 * Created on Mar 14, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.model;

/**
 * Enrollment Type Enumeration
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/model/EnrollmentType.java#1 $
 */

public enum EnrollmentType {
	ACTIVE_DIRECTORY(
			"com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.ActiveDirectoryEnroller"), LDIF(
			"com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.LdifEnroller"), SHAREPOINT(
			"com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.SharePointEnroller"), JAVA_PROPERTY_FILE(
			"com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.text.JavaPropertiesFileEnroller"), UNKNOWN(
			"com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.unknown.UnknownEnroller");

	private String name;

	private EnrollmentType(String name) {
		this.name = name;
	}

	public static EnrollmentType fromName(String name) {
		EnrollmentType typeToReturn = null;
		for (EnrollmentType nextType : EnrollmentType.values()) {
			if (nextType.name.equals(name)) {
				typeToReturn = nextType;
			}
		}

		if (typeToReturn == null) {
			throw new IllegalArgumentException("No type for name, " + name);
		}

		return typeToReturn;
	}
}
