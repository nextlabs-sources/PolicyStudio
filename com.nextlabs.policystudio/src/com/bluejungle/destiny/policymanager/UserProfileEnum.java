/*
 * Created on Feb 5, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/PolicyEnum.java#1 $
 */

public enum UserProfileEnum {
	CORPORATE, FILESYSTEM, PORTAL;
	@Override
	public String toString() {
		switch (this) {
		case CORPORATE:
			return "Corporate";
		case FILESYSTEM:
			return "FileSystem";
		case PORTAL:
			return "Portal";
		}
		return null;
	}
}
