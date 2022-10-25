/*
 * Created on May 10, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.util.Date;

import com.bluejungle.destiny.policymanager.model.PolicyServerHelper;
import com.bluejungle.destiny.policymanager.ui.usergroup.Messages;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.common.AccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.destiny.policy.PolicyFolder;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.Subject;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.misc.IAccessControlled;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/DomainObjectHelper.java#1
 *          $:
 */

public class DomainObjectHelper {

    /**
     * Constructor
     * 
     */
    public DomainObjectHelper() {
        super();
    }

    /**
     * returns the name of the specified object
     * 
     * @param domainObject
     *            instance of either a policy or a spec.
     * @return name of domainObject
     */
    public static String getName(IHasId domainObject) {
        if (domainObject instanceof Policy) {
            return ((Policy) domainObject).getName();
        } else if (domainObject instanceof IDSpec) {
            return ((IDSpec) domainObject).getName();
        } else if (domainObject instanceof PolicyFolder) {
            return ((PolicyFolder) domainObject).getName();
        } else if (domainObject instanceof DAction) {
            return ((DAction) domainObject).getName();
        } else if (domainObject instanceof SpecBase) {
            return ((SpecBase) domainObject).getName();
        } else if (domainObject instanceof SpecBase) {
            return ((SpecBase) domainObject).getName();
        }
        return null;
    }

    /**
     * returns the Status of the specified object
     * 
     * @param domainObject
     *            instance of either a policy or a spec.
     * @return Status of domainObject
     */
    public static DevelopmentStatus getStatus(IHasId domainObject) {
        if (domainObject instanceof Policy) {
            return ((Policy) domainObject).getStatus();
        } else if (domainObject instanceof IDSpec) {
            return ((IDSpec) domainObject).getStatus();
        } else if (domainObject instanceof PolicyFolder) {
            return ((PolicyFolder) domainObject).getStatus();
        }
        return null;
    }

    /**
     * sets the status of the specified object
     * 
     * @param domainObject
     *            instance of either a policy or a spec.
     * @param status
     *            new status for domainObject
     * 
     */
    public static void setStatus(IHasId domainObject, DevelopmentStatus status) {
        if (domainObject instanceof Policy) {
            ((Policy) domainObject).setStatus(status);
        } else if (domainObject instanceof IDSpec) {
            ((IDSpec) domainObject).setStatus(status);
        } else if (domainObject instanceof PolicyFolder) {
            ((PolicyFolder) domainObject).setStatus(status);
        }
    }

    /**
     * @param domainObject
     * @return the type of the object
     */
    public static String getObjectType(IHasId domainObject) {
        if (domainObject instanceof Policy) {
            return ApplicationMessages.DOMAINOBJECTHELPER_POLICY_TYPE;
        } else if (domainObject instanceof PolicyFolder) {
            return ApplicationMessages.DOMAINOBJECTHELPER_POLICY_FOLDER_TYPE;
        } else if (domainObject instanceof IDSpec) {
            SpecType type = ((IDSpec) domainObject).getSpecType();
            if (type == SpecType.USER) {
                return ApplicationMessages.DOMAINOBJECTHELPER_USER_TYPE;
            } else if (type == SpecType.RESOURCE) {
            	String resourceType = ((IDSpec) domainObject).getName();
            	String[] splits = resourceType.split("/");
            	if (splits[0].equalsIgnoreCase("DEVICE")){
            		return ApplicationMessages.DOMAINOBJECTHELPER_RESOURCE_DEVICE_TYPE;
            	}else if(splits[0].equalsIgnoreCase("SERVER")){
            		return ApplicationMessages.DOMAINOBJECTHELPER_RESOURCE_SERVER_TYPE;
            	}else{
            		 return ApplicationMessages.DOMAINOBJECTHELPER_RESOURCE_TYPE;
            	}	
            } else if (type == SpecType.HOST) {
                return ApplicationMessages.DOMAINOBJECTHELPER_DESKTOP_TYPE;
            } else if (type == SpecType.APPLICATION) {
                return ApplicationMessages.DOMAINOBJECTHELPER_APPLICATION_TYPE;
            } else if (type == SpecType.ACTION) {
                return ApplicationMessages.DOMAINOBJECTHELPER_ACTION_TYPE;
            } else if (type == SpecType.PORTAL) {
                return ApplicationMessages.DOMAINOBJECTHELPER_PORTAL_TYPE;
            }
        }
        return "";

    }

    /**
     * @param domainObject
     * @return the type of the object
     */
    public static EntityType getEntityType(IHasId domainObject) {
        if (domainObject instanceof Policy) {
            return EntityType.POLICY;
        } else if (domainObject instanceof PolicyFolder) {
            return EntityType.FOLDER;
        } else if (domainObject instanceof IDSpec) {
            IDSpec spec = (IDSpec) domainObject;
            return EntityType.forSpecType(spec.getSpecType());
        }
        return null;
    }

    /**
     * @param domainObject
     * @return the type of the object
     */
    public static String getComponentType(IHasId domainObject) {
        if (domainObject instanceof IDSpec) {
            IDSpec spec = (IDSpec) domainObject;
            return PolicyServerHelper.getComponentEnumFormName(spec.getName());
        } else {
            return null;
        }
    }

    /**
     * @param domainObject
     * @return the type of the object
     */
    public static Date getLastModifiedDate(IHasId domainObject) {
        // TODO fix when backend APIs are available
        if (domainObject instanceof Policy) {
        } else if (domainObject instanceof PolicyFolder) {
        } else if (domainObject instanceof IDSpec) {
        }
        return new Date();
    }

    /**
     * checks if object1 and object2 are the same.
     * 
     * @param object1
     * @param object2
     * @return true if object1 and object2 are the same object
     */
    public static boolean isSame(IHasId object1, IHasId object2) {
        if (object1 == null && object2 == null) {
            return true;
        } else if (object1 == null || object2 == null) {
            return false;
        }
        return (object1.getId().equals(object2.getId()));
    }

    /**
     * gets the cached descriptor of the specified domain object from
     * EntityInfoProvider and returns it.
     * 
     * @param domainObject
     *            instance of IHasId. Should be policy or spec object
     * @return cached descriptor for specified object
     */
    public static DomainObjectDescriptor getCachedDescriptor(IHasId domainObject) {
        DODDigest digest = null;
        if (domainObject instanceof Policy) {
            digest = EntityInfoProvider
                     .getPolicyDescriptor(((Policy) domainObject).getName());
        } else if (domainObject instanceof PolicyFolder) {
            digest = EntityInfoProvider
                     .getPolicyFolderDescriptor(((PolicyFolder) domainObject)
                                                .getName());
        } else if (domainObject instanceof IDSpec) {
            digest = EntityInfoProvider
                     .getComponentDescriptor(((IDSpec) domainObject).getName());
        }
        if (digest != null) {
            try {
                DomainObjectDescriptor desc = PolicyServerProxy
                                              .getDescriptorById(digest.getId());
                return desc;
            } catch (PolicyEditorException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;

    }

    /**
     * @param domainObject
     * @return accesspolicy associated with domainObject
     */
    public static IAccessPolicy getAccessPolicy(IHasId domainObject) {
        if (domainObject instanceof IAccessControlled) {
            IAccessPolicy accessPolicy = ((IAccessControlled) domainObject)
                                         .getAccessPolicy();
            if (accessPolicy == null) {
                DomainObjectHelper.setDefaultAccessPolicy(domainObject);
            }
            return ((IAccessControlled) domainObject).getAccessPolicy();
        }
        return null;
    }

    /**
     * sets a default access policy on domainObject TODO: this code is
     * placeholder. Default policy should be set by PF
     * 
     * @param domainObject
     * @return
     */
    public static void setDefaultAccessPolicy(IHasId domainObject) {
        if (domainObject instanceof IAccessControlled) {
            final IAccessControlled accessControlled = (IAccessControlled) domainObject;
            accessControlled.setOwner(new Subject("Administrator@local",
                                                  SubjectType.USER));
            accessControlled.setAccessPolicy((AccessPolicy) AccessPolicy
                                             .getDefaultAccessPolicy(new Long(0)));
        }
    }
    /**
     * Assigns an access policy to the domain object.  Usually this will be done by
     * the PF
     *
     * @param domainObject the object
     * @param accessPolicy the access policy
     */
    public static void setAccessPolicy(IHasId domainObject, AccessPolicy accessPolicy) {
        if (domainObject instanceof IAccessControlled) {
            final IAccessControlled accessControlled = (IAccessControlled) domainObject;
            accessControlled.setAccessPolicy(accessPolicy);
        }
    }

    /**
     * This method evaluates the editablility of an object solely based on its
     * current development state For general editablility tests, consider using
     * isEditable() instead, which checks for editability based on the object
     * state AND the permissions of the current user.
     * 
     * @param domainObject
     * @return true if the object is in a development status where it can be
     *         edited
     */
    public static boolean isStatusEditable(IHasId domainObject) {
        DevelopmentStatus status = getStatus(domainObject);
        return status == DevelopmentStatus.DRAFT
            || status == DevelopmentStatus.EMPTY
            || status == DevelopmentStatus.NEW;
    }

    /**
     * 
     * @param domainObject
     * @return true if the object can be edited by the current user
     */
    public static boolean isEditable(IHasId domainObject) {
        return isStatusEditable(domainObject)
            && PolicyServerProxy.canPerformAction(domainObject,
                                                  DAction.WRITE);
    }

    /**
     * For policies, replace folder separator $ with /
     * 
     * @param descriptor
     *            descriptor of domain object
     * @return display name for object
     */
    public static String getDisplayName(DomainObjectDescriptor descriptor) {
        String name = descriptor.getName();
        if (descriptor.getType() == EntityType.POLICY
            || descriptor.getType() == EntityType.FOLDER) {
            return PQLParser.SEPARATOR + name;
        } else {
            int pos = name.indexOf(PQLParser.SEPARATOR);
            if (pos != -1) {
                return name.substring(pos + 1);
            } else {
                return name;
            }
        }
    }

    /**
     * For policies, replace folder separator $ with /
     * 
     * @param domainObject
     * 
     * @return display name for object
     */
    public static String getDisplayName(IHasId domainObject) {
        String ret = "";

        if (domainObject instanceof Policy) {
            ret = PQLParser.SEPARATOR + ((Policy) domainObject).getName();
        } else if (domainObject instanceof IDSpec) {
            ret = ((IDSpec) domainObject).getName();
            int pos = ret.indexOf(PQLParser.SEPARATOR);
            if (pos != -1) {
                ret = ret.substring(pos + 1);
            }
        } else if (domainObject instanceof PolicyFolder) {
            ret = PQLParser.SEPARATOR + ((PolicyFolder) domainObject).getName();
        }

        return ret;
    }

    /**
     * @param domainObject
     * @param newName
     *            new name for object
     */
    public static void setName(IHasId domainObject, String newName) {
        if (domainObject instanceof Policy) {
            ((Policy) domainObject).setName(newName);
        } else if (domainObject instanceof PolicyFolder) {
            ((PolicyFolder) domainObject).setName(newName);
        } else if (domainObject instanceof SpecBase) {
            ((SpecBase) domainObject).setName(newName);
        }
    }

    /**
     * @param domainObject
     * @return name of owner
     */
    public static Long getOwnerId(IHasId domainObject) {
        if (domainObject == null) {
            throw new NullPointerException("domainObject");
        }
        IDSubject owner = null;
        if (domainObject instanceof Policy) {
            owner = ((Policy) domainObject).getOwner();
        } else if (domainObject instanceof PolicyFolder) {
            owner = ((PolicyFolder) domainObject).getOwner();
        } else if (domainObject instanceof SpecBase) {
            owner = ((SpecBase) domainObject).getOwner();
        }
        return owner.getId();
    }

    public static void setOwnerId(IHasId domainObject, IDSubject id) {
        if (domainObject == null) {
            throw new NullPointerException("domainObject");
        }

        if (domainObject instanceof Policy) {
            ((Policy)domainObject).setOwner(id);
        } else if (domainObject instanceof PolicyFolder) {
            ((PolicyFolder)domainObject).setOwner(id);
        } else if (domainObject instanceof SpecBase) {
            ((SpecBase)domainObject).setOwner(id);
        }
    }

 /**
  * Given an object, returns a string to look up
  * 
  * @param entityUsage
  * @param domainObject
  * @return
  */
 public static String getDeploymentStatusKey(
   DomainObjectDescriptor descriptor, DomainObjectUsage entityUsage) {
  if (descriptor == null) {
   return "DRAFT.None.None";
  }

  Long currentlyDeployedVersion = entityUsage
    .getCurrentlydeployedvcersion();
  Long pendingDeploymentVersion = entityUsage.getLatestDeployedVersion();
  if ((currentlyDeployedVersion != null)
    && (pendingDeploymentVersion != null)
    && (currentlyDeployedVersion.equals(pendingDeploymentVersion))) {
   pendingDeploymentVersion = null;
  }

  int descriptorVersion = descriptor.getVersion();
  DevelopmentStatus status = descriptor.getStatus();
  StringBuffer res = new StringBuffer(
    (status == DevelopmentStatus.APPROVED || status == DevelopmentStatus.OBSOLETE) ? status
      .getName()
      : DevelopmentStatus.DRAFT.getName());
  res.append('.');
  res.append(getDeployedVersionStatus(descriptorVersion,
    pendingDeploymentVersion));
  res.append('.');
  res.append(getDeployedVersionStatus(descriptorVersion,
    currentlyDeployedVersion));
  return res.toString();
 }

 public static String getDeploymentStatusKey(int version,
   DevelopmentStatus status, DomainObjectUsage usage) {
  Long currentlyDeployedVersion = usage.getCurrentlydeployedvcersion();
  Long pendingDeploymentVersion = usage.getLatestDeployedVersion();
  if ((currentlyDeployedVersion != null)
    && (pendingDeploymentVersion != null)
    && (currentlyDeployedVersion.equals(pendingDeploymentVersion))) {
   pendingDeploymentVersion = null;
  }

  int descriptorVersion = version;
  StringBuffer res = new StringBuffer(
    (status == DevelopmentStatus.APPROVED || status == DevelopmentStatus.OBSOLETE) ? status
      .getName()
      : DevelopmentStatus.DRAFT.getName());
  res.append('.');
  res.append(getDeployedVersionStatus(descriptorVersion,
    pendingDeploymentVersion));
  res.append('.');
  res.append(getDeployedVersionStatus(descriptorVersion,
    currentlyDeployedVersion));
  return res.toString();
 }

 private static String getDeployedVersionStatus(
   int currentDescriptorVersion, Long deployedVersion) {
  if (deployedVersion != null) {
   if (currentDescriptorVersion <= deployedVersion.intValue()) {
    return "Current";
   } else {
    return "Prior";
   }
  } else {
   return "None";
  }
 }

 public static String getStatusText(String statusKey) {
  if (statusKey.equals("DRAFT.None.None"))
   return Messages.STATUS_DRAFT_NONE_NONE;
  else if (statusKey.equals("DRAFT.None.Prior"))
   return Messages.STATUS_DRAFT_NONE_PRIOR;
  else if (statusKey.equals("DRAFT.None.Current"))
   return Messages.STATUS_DRAFT_NONE_CURRENT;
  else if (statusKey.equals("DRAFT.Prior.None"))
   return Messages.STATUS_DRAFT_PRIOR_NONE;
  else if (statusKey.equals("DRAFT.Prior.Prior"))
   return Messages.STATUS_DRAFT_PRIOR_PRIOR;
  else if (statusKey.equals("DRAFT.Prior.Current"))
   return Messages.STATUS_DRAFT_PRIOR_CURRENT;
  else if (statusKey.equals("DRAFT.Current.None"))
   return Messages.STATUS_DRAFT_CURRENT_NONE;
  else if (statusKey.equals("DRAFT.Current.Prior"))
   return Messages.STATUS_DRAFT_CURRENT_PRIOR;
  else if (statusKey.equals("DRAFT.Current.Current"))
   return Messages.STATUS_DRAFT_CURRENT_CURRENT;
  else if (statusKey.equals("APPROVED.None.None"))
   return Messages.STATUS_APPROVED_NONE_NONE;
  else if (statusKey.equals("APPROVED.None.Prior"))
   return Messages.STATUS_APPROVED_NONE_PRIOR;
  else if (statusKey.equals("APPROVED.None.Current"))
   return Messages.STATUS_APPROVED_NONE_CURRENT;
  else if (statusKey.equals("APPROVED.Prior.None"))
   return Messages.STATUS_APPROVED_PRIOR_NONE;
  else if (statusKey.equals("APPROVED.Prior.Prior"))
   return Messages.STATUS_APPROVED_PRIOR_PRIOR;
  else if (statusKey.equals("APPROVED.Prior.Current"))
   return Messages.STATUS_APPROVED_PRIOR_CURRENT;
  else if (statusKey.equals("APPROVED.Current.None"))
   return Messages.STATUS_APPROVED_CURRENT_NONE;
  else if (statusKey.equals("APPROVED.Current.Prior"))
   return Messages.STATUS_APPROVED_CURRENT_PRIOR;
  else if (statusKey.equals("APPROVED.Current.Current"))
   return Messages.STATUS_APPROVED_CURRENT_CURRENT;
  else if (statusKey.equals("OBSOLETE.None.None"))
   return Messages.STATUS_OBSOLETE_NONE_NONE;
  else if (statusKey.equals("OBSOLETE.None.Prior"))
   return Messages.STATUS_OBSOLETE_NONE_PRIOR;
  else if (statusKey.equals("OBSOLETE.None.Current"))
   return Messages.STATUS_OBSOLETE_NONE_CURRENT;
  else if (statusKey.equals("OBSOLETE.Prior.None"))
   return Messages.STATUS_OBSOLETE_PRIOR_NONE;
  else if (statusKey.equals("OBSOLETE.Prior.Prior"))
   return Messages.STATUS_OBSOLETE_PRIOR_PRIOR;
  else if (statusKey.equals("OBSOLETE.Prior.Current"))
   return Messages.STATUS_OBSOLETE_PRIOR_CURRENT;
  else if (statusKey.equals("OBSOLETE.Current.None"))
   return Messages.STATUS_OBSOLETE_CURRENT_NONE;
  else if (statusKey.equals("OBSOLETE.Current.Prior"))
   return Messages.STATUS_OBSOLETE_CURRENT_PRIOR;
  else if (statusKey.equals("OBSOLETE.Current.Current"))
   return Messages.STATUS_OBSOLETE_CURRENT_CURRENT;
  return "";
 }

 public static String getDeploymentText(String statusKey) {
  if (statusKey.equals("DRAFT.None.None"))
   return Messages.DEPLOYMENT_DRAFT_NONE_NONE;
  else if (statusKey.equals("DRAFT.None.Prior"))
   return Messages.DEPLOYMENT_DRAFT_NONE_PRIOR;
  else if (statusKey.equals("DRAFT.None.Current"))
   return Messages.DEPLOYMENT_DRAFT_NONE_CURRENT;
  else if (statusKey.equals("DRAFT.Prior.None"))
   return Messages.DEPLOYMENT_DRAFT_PRIOR_NONE;
  else if (statusKey.equals("DRAFT.Prior.Prior"))
   return Messages.DEPLOYMENT_DRAFT_PRIOR_PRIOR;
  else if (statusKey.equals("DRAFT.Prior.Current"))
   return Messages.DEPLOYMENT_DRAFT_PRIOR_CURRENT;
  else if (statusKey.equals("DRAFT.Current.None"))
   return Messages.DEPLOYMENT_DRAFT_CURRENT_NONE;
  else if (statusKey.equals("DRAFT.Current.Prior"))
   return Messages.DEPLOYMENT_DRAFT_CURRENT_PRIOR;
  else if (statusKey.equals("DRAFT.Current.Current"))
   return Messages.DEPLOYMENT_DRAFT_CURRENT_CURRENT;
  else if (statusKey.equals("APPROVED.None.None"))
   return Messages.DEPLOYMENT_APPROVED_NONE_NONE;
  else if (statusKey.equals("APPROVED.None.Prior"))
   return Messages.DEPLOYMENT_APPROVED_NONE_PRIOR;
  else if (statusKey.equals("APPROVED.None.Current"))
   return Messages.DEPLOYMENT_APPROVED_NONE_CURRENT;
  else if (statusKey.equals("APPROVED.Prior.None"))
   return Messages.DEPLOYMENT_APPROVED_PRIOR_NONE;
  else if (statusKey.equals("APPROVED.Prior.Prior"))
   return Messages.DEPLOYMENT_APPROVED_PRIOR_PRIOR;
  else if (statusKey.equals("APPROVED.Prior.Current"))
   return Messages.DEPLOYMENT_APPROVED_PRIOR_CURRENT;
  else if (statusKey.equals("APPROVED.Current.None"))
   return Messages.DEPLOYMENT_APPROVED_CURRENT_NONE;
  else if (statusKey.equals("APPROVED.Current.Prior"))
   return Messages.DEPLOYMENT_APPROVED_CURRENT_PRIOR;
  else if (statusKey.equals("APPROVED.Current.Current"))
   return Messages.DEPLOYMENT_APPROVED_CURRENT_CURRENT;
  else if (statusKey.equals("OBSOLETE.None.None"))
   return Messages.DEPLOYMENT_OBSOLETE_NONE_NONE;
  else if (statusKey.equals("OBSOLETE.None.Prior"))
   return Messages.DEPLOYMENT_OBSOLETE_NONE_PRIOR;
  else if (statusKey.equals("OBSOLETE.None.Current"))
   return Messages.DEPLOYMENT_OBSOLETE_NONE_CURRENT;
  else if (statusKey.equals("OBSOLETE.Prior.None"))
   return Messages.DEPLOYMENT_OBSOLETE_PRIOR_NONE;
  else if (statusKey.equals("OBSOLETE.Prior.Prior"))
   return Messages.DEPLOYMENT_OBSOLETE_PRIOR_PRIOR;
  else if (statusKey.equals("OBSOLETE.Prior.Current"))
   return Messages.DEPLOYMENT_OBSOLETE_PRIOR_CURRENT;
  else if (statusKey.equals("OBSOLETE.Current.None"))
   return Messages.DEPLOYMENT_OBSOLETE_CURRENT_NONE;
  else if (statusKey.equals("OBSOLETE.Current.Prior"))
   return Messages.DEPLOYMENT_OBSOLETE_CURRENT_PRIOR;
  else if (statusKey.equals("OBSOLETE.Current.Current"))
   return Messages.DEPLOYMENT_OBSOLETE_CURRENT_CURRENT;
  return "";
 }

 public static EntityType componentToEntityType(String componentType) {
  return EntityType.COMPONENT;
 }
}
