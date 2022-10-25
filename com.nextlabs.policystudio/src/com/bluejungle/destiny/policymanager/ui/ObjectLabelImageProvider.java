/*
 * Created on May 17, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.bluejungle.destiny.policymanager.SharePointImageConstants;
import com.bluejungle.destiny.policymanager.model.PolicyServerHelper;
import com.bluejungle.destiny.policymanager.util.PluginUtil;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;

/**
 * @author dstarke
 * 
 */
public class ObjectLabelImageProvider {

	public static Image getImage(Object obj) {
		if (obj instanceof IDSpec || obj instanceof IDSpecRef) {
			String componentType = "";
			if (obj instanceof IDSpec) {
				IDSpec spec = (IDSpec) obj;
				componentType = PolicyServerHelper
						.getTypeFromComponentName(spec.getName());
			}
			if (obj instanceof IDSpecRef) {
				IDSpecRef spec = (IDSpecRef) obj;
				componentType = PolicyServerHelper
						.getTypeFromComponentName(spec.getReferencedName());
			}
			return PluginUtil.getContextImageForContext(componentType);
		}
		if (obj instanceof IDPolicy) {
			return ImageBundle.POLICY_IMG;
		}
		if (obj instanceof DomainObjectDescriptor) {
			DomainObjectDescriptor desc = (DomainObjectDescriptor) obj;
			EntityType type = desc.getType();
			String name = desc.getName();
			if (type == EntityType.LOCATION) {
				return getTemporaryFillerImage();
			} else if (type == EntityType.POLICY) {
				return ImageBundle.POLICY_IMG;
			} else if (type == EntityType.FOLDER) {
				return ImageBundle.FOLDER_IMG;
			} else if (type == EntityType.COMPONENT) {
				String componentType = PolicyServerHelper
						.getTypeFromComponentName(name);
				return PluginUtil.getContextImageForContext(componentType);
			}
		}
		if (obj instanceof DODDigest) {
			DODDigest desc = (DODDigest) obj;
			String type = desc.getType();
			if (type.equals("LOCATION")) {
				return getTemporaryFillerImage();
			} else if (type.equals("POLICY")) {
				return ImageBundle.POLICY_IMG;
			} else if (type.equals("FOLDER")) {
				return ImageBundle.FOLDER_IMG;
			}
			return PluginUtil.getContextImageForContext(type);
		}
		if (obj instanceof Relation) {
			Relation rel = (Relation) obj;
			IExpression exp = rel.getLHS();
			if (exp instanceof ResourceAttribute) {
				ResourceAttribute attribute = (ResourceAttribute) exp;
				String subtype = attribute.getObjectSubTypeName();
				if (ResourceAttribute.PORTAL_SUBTYPE.equals(subtype)) {
					return SharePointImageConstants.SHAREPOINT;
				} else {
					return ImageBundle.FILE_IMG;
				}
			} else if (exp instanceof SubjectAttribute) {
				if (exp == SubjectAttribute.USER_UID
						|| exp == SubjectAttribute.USER_NAME
						|| exp == SubjectAttribute.USER_ID) {
					return ImageBundle.USER_IMG;
				} else if (exp == SubjectAttribute.CONTACT_ID) {
					return ImageBundle.CONTACT_IMG;
				} else if (exp == SubjectAttribute.HOST_UID
						|| exp == SubjectAttribute.HOST_NAME
						|| exp == SubjectAttribute.HOST_ID) {
					return ImageBundle.DESKTOP_IMG;
				} else if (exp == SubjectAttribute.APP_UID
						|| exp == SubjectAttribute.APP_NAME
						|| exp == SubjectAttribute.APP_ID) {
					return ImageBundle.APPLICATION_IMG;
				} else if (exp == SubjectAttribute.USER_LDAP_GROUP
						|| exp == SubjectAttribute.USER_LDAP_GROUP_ID) {
					return ImageBundle.IMPORTED_USER_GROUP_IMG;
				} else if (exp == SubjectAttribute.HOST_LDAP_GROUP
						|| exp == SubjectAttribute.HOST_LDAP_GROUP_ID) {
					return ImageBundle.IMPORTED_HOST_GROUP_IMG;
				}
			}
		}
		if (obj instanceof LeafObject) {
			LeafObjectType type = ((LeafObject) obj).getType();
			if (type == LeafObjectType.APPLICATION) {
				return ImageBundle.APPLICATION_IMG;
			} else if (type == LeafObjectType.HOST
					|| type == LeafObjectType.DESKTOP_AGENT) {
				return ImageBundle.DESKTOP_IMG;
			} else if (type == LeafObjectType.HOST_GROUP
					|| type == LeafObjectType.FILE_SERVER_AGENT
					|| type == LeafObjectType.ACTIVE_DIRECTORY_AGENT) {
				return ImageBundle.IMPORTED_HOST_GROUP_IMG;
			} else if (type == LeafObjectType.PORTAL_AGENT) {
				return SharePointImageConstants.SHAREPOINT;
			} else if (type == LeafObjectType.RESOURCE) {
				return ImageBundle.FILE_IMG;
			} else if (type == LeafObjectType.USER) {
				return ImageBundle.USER_IMG;
			} else if (type == LeafObjectType.CONTACT) {
				return ImageBundle.CONTACT_IMG;
			} else if (type == LeafObjectType.USER_GROUP) {
				return ImageBundle.IMPORTED_USER_GROUP_IMG;
			} else if (type == LeafObjectType.ACTION) {
				return PluginUtil.getContextImageForContext("ACTION");
			} else if (type == LeafObjectType.APPUSER) {
				return ImageBundle.APP_USER_IMG;
			} else if (type == LeafObjectType.ACCESSGROUP) {
				return ImageBundle.APP_USER_GROUP_IMG;
			}
		}
		return getTemporaryFillerImage();
	}

	private static Image getTemporaryFillerImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJ_ELEMENT);
	}
}
