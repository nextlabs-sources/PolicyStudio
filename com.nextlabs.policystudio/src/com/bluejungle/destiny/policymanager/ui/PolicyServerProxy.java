/*
 * Created on Apr 28, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.ObjectExistsException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.swt.internal.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.model.EnrollmentType;
import com.bluejungle.destiny.policymanager.model.IRealm;
import com.bluejungle.destiny.policymanager.model.PolicyServerHelper;
import com.bluejungle.destiny.policymanager.ui.dialogs.LoginDialog;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.destiny.services.policy.types.PolicyEditorRoles;
import com.bluejungle.destiny.services.policy.types.Realm;
import com.bluejungle.destiny.services.policy.types.PolicyServiceFault;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lib.AgentStatusDescriptor;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectSearchSpec;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lib.PolicyServiceException;
import com.bluejungle.pf.destiny.lifecycle.AttributeDescriptor;
import com.bluejungle.pf.destiny.lifecycle.CircularReferenceException;
import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;
import com.bluejungle.pf.destiny.lifecycle.DeploymentRecord;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.PolicyActionsDescriptor;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.IPolicyEditorClient;
import com.bluejungle.pf.destiny.services.InvalidPasswordException;
import com.bluejungle.pf.destiny.services.PolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.destiny.services.ResourcePreview;
import com.bluejungle.pf.domain.destiny.common.AccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.PolicyFolder;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.epicenter.action.IAction;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

/**
 * @author dstarke, Bo Meng
 * 
 */
public class PolicyServerProxy {
    
    public static IPolicyEditorClient client;

    private static final String CONFIG_FILE_PATH = "/config/config.xml";
    private static final String SERVER_PROPERTY_NAME = "DPSLocation";
    private static final String DEFAULT_SERVER = "https://localhost:8443";
    private static final String SERVER_PREFIX = "https://";

    private static final String[] dayOfWeekNames = new DateFormatSymbols().getWeekdays();

    public static String getDefaultServerAddress() {
        Bundle pluginBundle = Activator.getDefault().getBundle();
        Properties props = new Properties();
        String serverAddress;
        try {
            URL foundURL = FileLocator.find(pluginBundle, new Path(CONFIG_FILE_PATH), null);
            foundURL = FileLocator.toFileURL(foundURL);
            props.load(foundURL.openStream());
            serverAddress = props.getProperty(SERVER_PROPERTY_NAME);
            if (serverAddress == null) {
                LoggingUtil.logInfo(Activator.ID, "DPMS Server address not found, using localhost", null);
                serverAddress = DEFAULT_SERVER;
            }
        } catch (Exception e) {
            LoggingUtil.logError(Activator.ID, "Config file not found, using localhost for DPMS server", e);
            serverAddress = DEFAULT_SERVER;
        }
        return serverAddress;
    }

    public static IDSubject getLoggedInUser() throws PolicyEditorException {
        return client.getLoggedInUser();
    }

    public static boolean canChangePassword() {
        boolean result = false;
        try {
            result = client.canChangePassword();
        } catch (PolicyEditorException ex) {
            LoggingUtil.logWarning(Activator.ID,
                    "Failed to determine if current user is allowed to change password." 
                    + "  Password change action will be disabled", ex);
        }
        return result;
    }

    public static boolean login(String user, String password, String server)
            throws LoginException {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration pfClientConfig = new HashMapConfiguration();
        pfClientConfig.setProperty(PolicyEditorClient.LOCATION_CONFIG_PARAM, SERVER_PREFIX + server);
        pfClientConfig.setProperty(PolicyEditorClient.USERNAME_CONFIG_PARAM, user);
        pfClientConfig.setProperty(PolicyEditorClient.PASSWORD_CONFIG_PARAM, password);

        client = compMgr.getComponent(PolicyEditorClient.COMP_INFO, pfClientConfig);
        client.setConfiguration(pfClientConfig);
        client.login();

        return client.isLoggedIn();
    }

    public static IDSpec createEmptyGroup(String name, String componentType) {
        try {
            EntityType type = PolicyServerHelper.getEntityType(componentType);
            Collection<? extends IHasId> res = 
                    client.getEntitiesForNamesAndType(Collections.singletonList(name)
                            , type, false);
            Iterator<? extends IHasId> iter = res.iterator();
            IDSpec spec = null;
            if (iter.hasNext()) {
                spec = (IDSpec) iter.next();
            }
            if (spec != null && spec.getStatus() != DevelopmentStatus.NEW) {
                // This is not a new object
                return null;
            }
            // Hack. We want to make sure that we don't return a SpecBase with the ILLEGAL type.
            // Not sure the best place to do this, hence the hack. See bug #28461
            if (spec.getSpecType() == SpecType.ILLEGAL) {
                IDSpec revisedSpec = new SpecBase(((SpecBase)spec).getManager(),
                                                  SpecType.RESOURCE,
                                                  spec.getId(),
                                                  spec.getName(),
                                                  spec.getDescription(),
                                                  spec.getStatus(),
                                                  spec.getPredicate(),
                                                  spec.isHidden());
                revisedSpec.setOwner(spec.getOwner());
                revisedSpec.setAccessPolicy((AccessPolicy)spec.getAccessPolicy());

                spec = revisedSpec;
            }
            return spec;
        } catch (PolicyEditorException e) {
            LoggingUtil.logError(Activator.ID, "error while creating new component", e);
            return null;
        }
    }

    @Deprecated
    public static IDSpec createEmptyGroup(String name, EntityType entityType) {
        try {
            Collection<? extends IHasId> res =
                    client.getEntitiesForNamesAndType(Collections.singletonList(name)
                            , entityType, false);
            Iterator<? extends IHasId> iter = res.iterator();
            IDSpec spec = null;
            if (iter.hasNext()) {
                spec = (IDSpec) iter.next();
            }
            if (spec != null && spec.getStatus() != DevelopmentStatus.NEW) {
                // This is not a new object
                return null;
            }
            return spec;
        } catch (PolicyEditorException e) {
            LoggingUtil.logError(Activator.ID, "error while creating new component", e);
            return null;
        }
    }

    public static IDPolicy createNewPolicy(String name) {
        try {
            Collection<? extends IHasId> res =
                    client.getEntitiesForNamesAndType(Collections.singletonList(name)
                            , EntityType.POLICY, false);
            Iterator<? extends IHasId> iter = res.iterator();
            IDPolicy policy = null;
            if (iter.hasNext()) {
                policy = (IDPolicy) iter.next();
            }
            if (policy != null && policy.getStatus() != DevelopmentStatus.NEW) {
                // This is not a new object
                return null;
            }
            return policy;
        } catch (PolicyEditorException e) {
            LoggingUtil.logError(Activator.ID, "error while creating new policy", e);
            return null;
        }
    }

    public static PolicyFolder createNewPolicyFolder(String name) {
        try {
            Collection<? extends IHasId> res =
                    client.getEntitiesForNamesAndType(Collections.singletonList(name)
                            , EntityType.FOLDER, false);
            Iterator<? extends IHasId> iter = res.iterator();
            PolicyFolder pf = null;
            if (iter.hasNext()) {
                pf = (PolicyFolder) iter.next();
            }
            if (pf != null && pf.getStatus() != DevelopmentStatus.NEW) {
                // This is not a new policy folder
                return null;
            }
            return pf;
        } catch (PolicyEditorException e) {
            LoggingUtil.logError(Activator.ID, "error while creating policy folder", e);
            return null;
        }
    }
	private static boolean userSessionExpired(PolicyEditorException exception) {
		if (exception.getCause() != null) {
			PolicyServiceFault psf = (PolicyServiceFault)exception.getCause();
			String reason = psf.getCauseMessage();
			if ((reason != null) && (reason.indexOf("not get current loggedInUser") > 0))
				return true;
		}
		return false;
	}
	
    /**
     * Saves the entities to the server.
     * 
     * @param entities
     *            collection of entities to save
     * @return descriptors of saved objects
     */
    public static Collection<DomainObjectDescriptor> saveEntities(
            Collection<? extends IHasId> entities) {

        try {
            Collection<DomainObjectDescriptor> c = client.saveEntities(entities);
            // EntityInfoProvider.updateDescriptors(c);
            return c;
        } catch (PolicyEditorException e) {
        	String msg = ApplicationMessages.POLICYSERVERPROXY_ERROR_MSG;
            LoggingUtil.logError(Activator.ID, "error while saving entity", e);
            if (!(e == null || 
                  e.getLocalizedMessage() == null ||
                  e.getLocalizedMessage().length() == 0)) {
            	msg = e.getLocalizedMessage();
            }
            PolicyHelpers.timeOutCheck(e);
            return null;
        }
    }
    
    public static Collection<DomainObjectDescriptor> getEntityList(
            String nameFilter, EntityType entityType) {
        try {
            if (entityType != null && nameFilter != null
                    & nameFilter.indexOf('/') == -1) {
                nameFilter = entityType.getName() + "/" + nameFilter;
            }
            return adjustType(client.getDescriptorsForNameAndType(nameFilter, EntityType.COMPONENT, false));
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error fetching entity list", e);
            return null;
        }
    }

    public static Collection<DomainObjectDescriptor> getPolicyList(
            String nameFilter) {
        try {
            return client.getDescriptorsForNameAndType(nameFilter, EntityType.POLICY, false);
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error fetching entity list", e);
            return null;
        }
    }

    public static Collection<DomainObjectDescriptor> getEntityList(
            String nameFilter, String type) {
        try {
            if (nameFilter != null && nameFilter.indexOf(PQLParser.SEPARATOR) == -1) {
                nameFilter = "" + type + PQLParser.SEPARATOR + nameFilter;
            }
            return adjustType(client.getDescriptorsForNameAndType(nameFilter, EntityType.COMPONENT, false));
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error fetching entity list", e);
            return null;
        }
    }

    public static Collection<DomainObjectDescriptor> getEntityList(
            String nameFilter, Collection<EntityType> entityTypes) {
        try {
            return adjustType(client.getDescriptorsForNameAndTypes(nameFilter, entityTypes, false));
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error fetching entity list", e);
            return null;
        }
    }

    public static Collection<DomainObjectDescriptor> getReferredEntitiesForPolicy(
            String nameFilter, String componentType) {
        Collection<DomainObjectDescriptor> entities;
        try {
            Collection<DomainObjectDescriptor> descriptors = 
                    client.getDescriptorsForNameAndType(nameFilter, EntityType.POLICY, false);
            entities = new ArrayList<DomainObjectDescriptor>();
            if (!descriptors.isEmpty()) {
                Collection<DomainObjectDescriptor> dependencies = client.getDependencies(descriptors);
                for (DomainObjectDescriptor desc : dependencies) {
                    if (desc.getName().toLowerCase().startsWith(
                            componentType.toString().toLowerCase() + PQLParser.SEPARATOR)) {
                        entities.add(desc);
                    }
                }
            }
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error fetching referring policies", e);
            return null;
        } catch (CircularReferenceException e) {
            LoggingUtil.logError(Activator.ID,
                    "error fetching referring policies: circular reference detected.", e);
            return null;
        }
        return adjustType(entities);
    }

    @Deprecated
    public static Collection<DomainObjectDescriptor> getReferringComponents(
            String nameFilter, EntityType entityType) {
        try {
            return adjustType(client.getReferringObjectsForGroup(nameFilter, entityType, entityType, false, true));
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error fetching referring entities", e);
            return null;
        }
    }

    public static Collection<DomainObjectDescriptor> getReferringComponents(
            String nameFilter) {
        try {
            return adjustType(client.getReferringObjectsForGroup(nameFilter, EntityType.COMPONENT, EntityType.COMPONENT, false, true));
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error fetching referring entities", e);
            return null;
        }
    }

    public static Collection<DomainObjectDescriptor> getAllReferringObjects(
            String nameFilter) {
        try {
            return adjustType(client.getReferringObjectsForGroup(escape(nameFilter), EntityType.COMPONENT, null, false, true));
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error fetching referring entities", e);
            return null;
        }
    }

    public static Collection<DomainObjectDescriptor> getAllReferringObjectsAsOf(
            Collection<DomainObjectDescriptor> descriptors, Date asof,
            boolean onlyDirect) {
        try {
            return adjustType(client.getReferringObjectsAsOf(descriptors, asof, onlyDirect));
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error fetching referring entities", e);
            return null;
        } catch (CircularReferenceException e) {
            LoggingUtil.logError(Activator.ID, "error fetching referring entities", e);
            return null;
        }
    }

    public static Collection<DomainObjectDescriptor> getPoliciesUsingComponent(
            String nameFilter) {
        try {
        	Collection<DomainObjectDescriptor> temp = client.getReferringObjectsForGroup(nameFilter,
                    EntityType.COMPONENT, null, false, false);
        	List<DomainObjectDescriptor> list = new ArrayList<DomainObjectDescriptor>();
        	for(DomainObjectDescriptor dspr: temp){
        		if(dspr.getType()==EntityType.POLICY) list.add(dspr);
        	}
            return list;
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error while retrieving policies using component", e);
            return null;
        }
    }

    public static Collection<DomainObjectDescriptor> getPoliciesUsingComponent(
            String nameFilter, EntityType entityType) {
        try {
            return adjustType(client.getReferringObjectsForGroup(nameFilter,
                    entityType, EntityType.POLICY, false, false));
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error while retrieving policies using component", e);
            return null;
        }
    }

    public static IDSpec getEntityByName(String name, EntityType entityType) {
        try {
            Collection<? extends IHasId> col =
                    client.getEntitiesForNamesAndType(Collections.singletonList(name)
                            , entityType, false);
            if (col.isEmpty()) {
                return null;
            }
            return (IDSpec) col.iterator().next();
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error while retrieving entity", e);
            return null;
        }
    }
    
	public static DomainObjectDescriptor getDescriptorByName(String name) {
		if (name == null || name.length() == 0) {
			return null;
		}
		Collection<DomainObjectDescriptor> collection = PolicyServerProxy.getPolicyList(PolicyServerProxy.escape(name));
		Iterator<DomainObjectDescriptor> iter = collection.iterator();
		return (iter.hasNext()) ? (DomainObjectDescriptor) iter.next() : null;
	}

    public static Object getEntityForDescriptor(
            DomainObjectDescriptor descriptor) {
        try {
            Collection<? extends IHasId> col = 
                    client.getEntitiesForDescriptors(Collections.singletonList(descriptor));
            if (col.isEmpty()) {
                return null;
            }
            IHasId res = col.iterator().next();
            if (res instanceof SpecBase) {
                SpecBase old = (SpecBase) res;
                SpecType type = PolicyServerHelper.getSpecType(PolicyServerHelper
                                .getTypeFromComponentName(descriptor.getName()));
                String name = old.getName();
                SpecBase sb = new SpecBase(
                        old.getManager()
                      , type
                      , old.getId()
                      , name
                      , old.getDescription()
                      , old.getStatus()
                      , old.getPredicate()
                      , old.isHidden()
                );
                if (old.getAccessPolicy() != null) {
                    sb.setAccessPolicy((AccessPolicy) old.getAccessPolicy());
                }
                if (old.getOwner() != null) {
                    sb.setOwner(old.getOwner());
                }
                return sb;
            }
            return res;
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error while retrieving entity", e);
            return null;
        }
    }

    public static Collection<? extends IHasId> getEntitiesForDescriptors(
            Collection<DomainObjectDescriptor> list) {
        try {
            return client.getEntitiesForDescriptors(list);
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error while retrieving entity", e);
            return null;
        }
    }

    /**
     * Helper class for returning an object along with information about its
     * deployment time
     */
    public static class ObjectVersion {

        public Date activeFrom;
        public Date activeTo;
        public IHasId object;

        private ObjectVersion(IHasId object, Date activeFrom, Date activeTo) {
            this.object = object;
            this.activeFrom = activeFrom;
            this.activeTo = activeTo;
        }

        public static ObjectVersion makeObjectVersion(IHasId object,
                Date activeFrom, Date activeTo) {
            if (object == null) {
                return null;
            }
            return new ObjectVersion(object, activeFrom, activeTo);
        }

    }

    /**
     * returns the currently deployed version of an object, or null if no such
     * object is currently deployed
     * 
     * @param descriptor
     * @return
     */
    public static ObjectVersion getDeployedVersion(
            DomainObjectDescriptor descriptor) {
        try {
            Collection<DeploymentHistory> ranges = client.getDeploymentHistory(descriptor);
            // get the last range
            if (ranges.size() > 0) {
                Date now = new Date();
                TimeRelation theRange = null;
                // find a time range that corresponds to the currently active
                // version
                for (DeploymentHistory range : ranges) {
                    //TODO ugly
                    if (range.getTimeRelation().getActiveFrom().getTime() <= now.getTime()
                            && range.getTimeRelation().getActiveTo().after(now)) {
                        theRange = range.getTimeRelation();
                    }
                }
                // must still be active
                if (theRange != null) {
                    Collection<? extends IHasId> col = 
                        client.getDeployedObjects(Collections.singletonList(descriptor), now);
                    if (col.isEmpty()) {
                        return null;
                    }
                    return ObjectVersion.makeObjectVersion(
                            col.iterator().next()
                          , theRange.getActiveFrom()
                          , theRange.getActiveTo());
                }
            }
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error while retrieving deployed entity", e);
            return null;
        }
        return null;
    }

    /**
     * returns the last scheduled version of an object, or null if no such
     * object exists
     * 
     * @param descriptor
     * @return
     */
    public static ObjectVersion getLastScheduledVersion(
            DomainObjectDescriptor descriptor) {
        try {
            Collection<DeploymentHistory> ranges = client.getDeploymentHistory(descriptor);
            // get the last range
            if (ranges.size() > 0) {
                TimeRelation relation = null;
                for (DeploymentHistory range : ranges) {
                    relation = range.getTimeRelation();
                }
                Date now = new Date();
                // must be active from some time in the future
                if (relation.getActiveFrom().after(now)) {
                    Collection<? extends IHasId> col = 
                        client.getDeployedObjects(
                                Collections.singletonList(descriptor)
                              , relation.getActiveFrom());
                    if (col.isEmpty()) {
                        return null;
                    }
                    return ObjectVersion.makeObjectVersion(
                            col.iterator().next()
                          , relation.getActiveFrom()
                          , relation.getActiveTo());
                }
            }
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error while retrieving scheduled entity", e);
            return null;
        }
        return null;
    }

    /**
     * @param descriptors
     *            collection of DomainObjecDescriptor objects
     * @return domain objects corresponding to the descriptors
     */
    public static Collection<? extends IHasId> getEntitiesForDescriptor(
            Collection<DomainObjectDescriptor> descriptors) {
        try {
            return client.getEntitiesForDescriptors(descriptors);
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error get entities for descriptor", e);
        }
        return null;
    }

    public static Object getEditedEntityMatching(Object o) {
        Object op = GlobalState.getInstance().getOpenedEntityForEntity(o);
        if (op != null) {
            return op;
        } else {
            return o;
        }
    }

    public static Collection<? extends IHasId> getEditedEntitiesMatching(
            Collection<? extends IHasId> col) {
        List<IHasId> mCol = new ArrayList<IHasId>(col);
        List<IHasId> toAdd = new ArrayList<IHasId>();
        Iterator<IHasId> it = mCol.iterator();
        IHasId cur, op;
        GlobalState gs = GlobalState.getInstance();
        while (it.hasNext()) {
            cur = it.next();
            if ((op = gs.getOpenedEntityForEntity(cur)) != null) {
                it.remove();
                toAdd.add(op);
            }
        }

        if (toAdd.size() > 0) {
            for(IHasId a : toAdd) {
                mCol.add(a);
            }
            return mCol;
        } else {
            return col;
        }
    }

    public static Collection<LeafObject> getSubjectPreview(
            DomainObjectDescriptor descriptor) throws PolicyEditorException {
        return client.getMatchingSubjects(descriptor);
    }

    public static Collection<AttributeDescriptor> getAttributes(
            EntityType entityType) {
        try {
            return client.getAttributesForType(entityType);
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error while retrieving attributes", e);
            return null;
        }
    }

    public static Collection<AttributeDescriptor> getCustomAttributes(
            EntityType entityType) {
        try {
            return client.getCustomAttributesForType(entityType);
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error while retrieving custom attributes", e);
            return null;
        }
    }

    /**
     * deploys all objects in the list.
     * 
     * @param objectList
     *            descriptors of objects to deploy
     * @param deploymentTime
     *            time for deployment
     * @return the earliest deployment time between client and server.
     */
    public static Date deployObjects(List<DomainObjectDescriptor> objectList,
            Date deploymentTime) {
        try {
            List<DomainObjectDescriptor> objectsToDeploy = new ArrayList<DomainObjectDescriptor>();
            List<DomainObjectDescriptor> objectsToUndeploy = new ArrayList<DomainObjectDescriptor>();

            for (int i = 0; i < objectList.size(); i++) {
                DomainObjectDescriptor descriptor = objectList.get(i);
                if (descriptor.getStatus() == DevelopmentStatus.APPROVED) {
                    objectsToDeploy.add(descriptor);
                } else if (descriptor.getStatus() == DevelopmentStatus.OBSOLETE) {
                    objectsToUndeploy.add(descriptor);
                }
            }
            // FIXME These 2 calls need to be combined. They are really one
            // deployment from the user point of view.

            // the schedule time is the earliest time between client and server
            Date scheduleDeploymentTime = deploymentTime;
            DeploymentRecord deploymentRecord;
            DeploymentRecord undeploymentRecord;
            deploymentRecord = client.scheduleDeployment(objectsToDeploy, deploymentTime);
            undeploymentRecord = client.scheduleUndeployment(objectsToUndeploy, deploymentTime);

            if (deploymentRecord != null && deploymentRecord.getAsOf().after(deploymentTime)) {
                scheduleDeploymentTime = deploymentRecord.getAsOf();
            }

            if (undeploymentRecord != null && undeploymentRecord.getAsOf().after(deploymentTime)) {
                scheduleDeploymentTime = undeploymentRecord.getAsOf();
            }

            return scheduleDeploymentTime;
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error deploy objects", e);
            return null;
        }
    }

    /**
     * Returns a collection of object descriptors for all domain objects that
     * the specified objects (in list) are dependent on.
     * 
     * @param list
     *            list of objects to get dependencies for
     * @return collection of object descriptors
     */
    public static Collection<DomainObjectDescriptor> getAllDependencies(
            Collection<DomainObjectDescriptor> list) {
        try {
            return adjustType(client.getDependencies(list));
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error get all dependencies", e);
        } catch (CircularReferenceException e) {
            LoggingUtil.logError(Activator.ID, "error get all dependencies", e);
        }
        return null;
    }

    public static Collection<DODDigest> getAllDependenciesDigest(
            Collection<DODDigest> list) {
        try {
            return client.getDependenciesDigest(list);
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error get all dependencies", e);
        } catch (CircularReferenceException e) {
            LoggingUtil.logError(Activator.ID, "error get all dependencies", e);
        }
        return null;
    }
    
    public static Collection<DODDigest> getDirectDependenciesDigest(
            Collection<DODDigest> list) {
        try {
            return client.getDependenciesDigest(list, true);
        } catch (PolicyEditorException e) {
            LoggingUtil.logError(Activator.ID, "error get direct dependencies", e);
        } catch (CircularReferenceException e) {
            LoggingUtil.logError(Activator.ID, "error get direct dependencies", e);
        }
        return null;
    }

    /**
     * Cancels a deployment
     * 
     * @param record
     *            DeploymentRecord instance to be canceled
     */
    public static void cancelDeployment(DeploymentRecord record) {
        try {
            client.cancelScheduledDeployment(record);
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error cancel deployment", e);
        }
    }

    /**
     * returns a collection of deployment records scheduled to be deployed
     * between start and end
     * 
     * @param start
     *            start time
     * @param end
     *            end time
     * @return collection of deployment records
     */
    public static Collection<DeploymentRecord> getDeploymentRecords(Date start,
            Date end) {
        try {
            Collection<DeploymentRecord> ret = null;
            ret = client.getDeploymentRecords(start, end);
            return ret;
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error get deployment records", e);
        }
        return null;
    }

    /**
     * @param descriptor
     * @return collection of deployment records for the specified object
     */
    public static Collection<DeploymentHistory> getDeploymentRecords(
            DomainObjectDescriptor descriptor) {
        try {
            return client.getDeploymentHistory(descriptor);
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "Error getting deployment history", e);
        }
        return null;
    }
    
    /**
     * @param deploymentRecord
     * @return collection of objects in the deployment record.
     */
    public static Collection<DomainObjectDescriptor> getObjectsInDeploymentRecord(
            DeploymentRecord deploymentRecord) {
        try {
            return adjustType(client.getDescriptorsForDeploymentRecord(deploymentRecord));
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error get objects in deployment record", e);
        }
        return null;
    }

    // ------------------------
    // Object Creation Helpers
    // ------------------------
    @Deprecated
    public static IDSpec createBlankComponent(String name, EntityType entityType) {
        IDSpec spec = createEmptyGroup(name, entityType);
        if (null == spec) {
            return null;
        }
        if (entityType == EntityType.USER) {
            PredicateHelpers.fillInStandardComponent(spec);
        } else if (entityType == EntityType.RESOURCE) {
            PredicateHelpers.fillInStandardComponent(spec);
        } else if (entityType == EntityType.HOST) {
            PredicateHelpers.fillInStandardComponent(spec);
        } else if (entityType == EntityType.APPLICATION) {
            PredicateHelpers.fillInStandardComponent(spec);
        } else if (entityType == EntityType.ACTION) {
            PredicateHelpers.fillInActionComponent(spec);
        }
        saveEntity(spec);
        return spec;
    }

    public static IDSpec createBlankComponent(String name, String componentType) {
        name = componentType.toString() + PQLParser.SEPARATOR + name;
        IDSpec spec = createEmptyGroup(name, componentType);
        if (null == spec) {
            return null;
        }
        if (componentType.equals("USER")) {
            PredicateHelpers.fillInStandardComponent(spec);
        } else if (componentType.equals("RESOURCE")) {
            PredicateHelpers.fillInStandardComponent(spec);
        } else if (componentType.equals("PORTAL")) {
            PredicateHelpers.fillInStandardComponent(spec);
        } else if (componentType.equals("SERVER")) {
            PredicateHelpers.fillInStandardComponent(spec);
        } else if (componentType.equals("HOST")) {
            PredicateHelpers.fillInStandardComponent(spec);
        } else if (componentType.equals("APPLICATION")) {
            PredicateHelpers.fillInStandardComponent(spec);
        } else if (componentType.equals("ACTION")) {
            PredicateHelpers.fillInActionComponent(spec);
        } else {
            PredicateHelpers.fillInStandardComponent(spec);
        }
        saveEntity(spec);
        return spec;
    }
    
    public static IDPolicy createBlankPolicy(String name, String type) {
    	return createBlankPolicy(name,type, false);
    }
	/**
	 * @param String name: exception (path) name 
 	 * @param String type: Top level policy type
 	 * @param boolean hasException: ture means we are creating exception policy, then we need to pass the "exception" attribute
 	 * 		  false means we are creating normal top level policy
	 */	
    public static IDPolicy createBlankPolicy(String name, String type, boolean hasException) {
        IDPolicy policy = createNewPolicy(name);
        if (hasException){
        	policy.setAttribute(IPolicy.EXCEPTION_ATTRIBUTE, true);
        }
        if (policy != null) {
            PredicateHelpers.fillInPolicy(policy, type);
            saveEntity(policy);
        }
        return policy;
    }

    public static List<DODDigest> createBlankPolicyFolder(String name) {
        PolicyFolder pf = createNewPolicyFolder(name);
        return saveEntityDigest(pf);
    }

    /**
     * @param descriptors
     *            descriptors of objects for which deployed descriptors are
     *            being requested
     * @return collection of descriptors of the deployed version of the
     *         specified objects as of the current time.
     */
    public static Collection<DomainObjectDescriptor> getDeployedDescriptors(
            Collection<DomainObjectDescriptor> descriptors) {
        try {
            return adjustType(client.getDeployedObjectDescriptors(descriptors,
                    new GregorianCalendar().getTime()));
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error get deployed descriptors", e);
        }
        return null;
    }

    /**
     * @param descriptors
     *            descriptor of object for which deployed descriptor is being
     *            requested
     * @param asof
     *            time for which deployed descriptor is requested.
     * 
     * @return descriptor of the deployed version of the specified object as of
     *         the current time.
     */
    public static DomainObjectDescriptor getDeployedDescriptors(
            DomainObjectDescriptor descriptor, Date asof) {
        try {
            Collection<DomainObjectDescriptor> deployedList = null;
            deployedList = adjustType(client.getDeployedObjectDescriptors(
                    Collections.singletonList(descriptor), asof));
            Iterator<DomainObjectDescriptor> iterator = deployedList.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error get deployed descriptors", e);
        }
        return null;
    }

    /**
     * @param descriptor
     *            desriptor of object to retrieve
     * @param asof
     *            get deployed version as of
     * @return object corresponding to the specified descriptor that was
     *         deployed at the specified time.
     */
    public static IHasId getDeployedObject(DomainObjectDescriptor descriptor, Date asof) {
        try {
            Collection<? extends IHasId> entities = null;
            entities = client.getDeployedObjects(Collections.singletonList(descriptor), asof);
            if (entities != null && !entities.isEmpty()) {
                return (IHasId) entities.iterator().next();
            }
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "Could not get deployed object", e);
        }
        return null;
    }

    /**
     * returns the resource preview object for the specified resource component
     * 
     * @param descriptor
     *            descriptor of resource components
     * @return ResourcePreview object
     */
    public static ResourcePreview getResourcePreview(
            DomainObjectDescriptor descriptor) {
        try {
            ResourcePreview ret = null;
            ret = client.getResourcePreview(descriptor);
            return ret;
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error get resource preview", e);
        }
        return null;
    }

    /**
     * 
     * @param object
     * @param action
     * @return return whether an action can be performed on an object
     */
    public static boolean canPerformAction(IHasId object, IAction action) {
        try {
            if (object == null) {
                return false;
            }
            Collection<? extends IAction> actions = client.allowedActions(object);
            return actions == null || actions.contains(action);
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error get perform action status", e);
            return false;
        }
    }
    
    /**
     * 
     * @param object
     * @return return actions allowed for the current user on the object
     */
    public static Collection<? extends IAction> allowedActions(IHasId object) {
        try {
            if (object == null) {
                return null;
            }
            return client.allowedActions(object);
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error getting allowed actions", e);
            return null;
        }
    }

    public static Collection<DomainObjectDescriptor> filterByAllowedAction(
            Collection<DomainObjectDescriptor> descriptors, IAction action) {
        try {
            if (descriptors == null || action == null) {
                return Collections.emptySet();
            }
            return adjustType(client.ensureOperationIsAllowed(descriptors, action));
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "error filter by allowed action", e);
            return Collections.emptySet();
        }
    }

    /**
     * @return collection of descriptors of objects that are approved but not
     *         yet deployed.
     */
    public static Collection<DomainObjectDescriptor> getObjectsToDeploy() {
        Collection<DomainObjectDescriptor> descriptors = null;
        Collection<DomainObjectDescriptor> deployedDescriptors = null;
        try {
            Calendar end = new GregorianCalendar();
            end.setTime(new Date(UnmodifiableDate.END_OF_TIME.getTime()));
            end.set(Calendar.YEAR, end.get(Calendar.YEAR) - 1);

            descriptors = new ArrayList<DomainObjectDescriptor>(client
                    .getDescriptorsForState(DevelopmentStatus.APPROVED, false));
            // add pending deactivation components
            Collection<DomainObjectDescriptor> descriptorsDeactivation = new ArrayList<DomainObjectDescriptor>(
                    client.getDescriptorsForState(DevelopmentStatus.OBSOLETE, false));
            descriptors.addAll(descriptorsDeactivation);

            deployedDescriptors = new ArrayList<DomainObjectDescriptor>(
                    client.getDeployedObjectDescriptors(descriptors, end.getTime()));
            for (DomainObjectDescriptor deployedDescriptor : deployedDescriptors) {
                for (DomainObjectDescriptor descriptor : descriptors) {
                    if (descriptor.getId().equals(deployedDescriptor.getId())) {
                        if (descriptor.getVersion() == deployedDescriptor.getVersion()) {
                            descriptors.remove(descriptor);
                        }
                        break;
                    }
                }
            }
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "Could not get approved descriptors.", e);
        }

        return adjustType(descriptors);
    }

    /**
     * @param statusDescriptor
     * @return
     */
    public static Collection<DomainObjectDescriptor> deploymentStatusForAgent(
            AgentStatusDescriptor statusDescriptor) {
        try {
            Collection<DomainObjectDescriptor> unfiltered = null;
            unfiltered = client.deploymentStatusForAgent(statusDescriptor);
            List<DomainObjectDescriptor> res = new ArrayList<DomainObjectDescriptor>(unfiltered.size());
            for (DomainObjectDescriptor dod : unfiltered) {
                if (dod.getType() != EntityType.LOCATION) {
                    res.add(dod);
                }
            }
            return adjustType(res);
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "Could not get deployment status for agent.", e);
        }
        return null;
    }

    /**
     * @return
     */
    public static Collection<AgentStatusDescriptor> getAgentList() {
        try {
            return client.getAgentList();
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "Could not get agent list.", e);
        }
        return null;
    }

    /**
     * @param descriptor
     * @param asof
     * @return collection of agents on which the specified object is deployed.
     */
    public static Collection<AgentStatusDescriptor> getAgentsForDeployedObject(
            DomainObjectDescriptor descriptor, Date asof) {
        try {
            return client.getAgentsForDeployedObject(descriptor, asof);
        } catch (PolicyEditorException e) {
            LoggingUtil.logWarning(Activator.ID, "Could not get agents for deployed object.", null);
        }
        return null;
    }

    public static LeafObject getSuperUser() throws PolicyEditorException {
        return client.getSuperUser();
    }

    public static void changePassword(String oldPassword, String newPassword)
            throws InvalidPasswordException, PolicyEditorException {
        client.changePassword(oldPassword, newPassword);
    }

    public static Collection<EntityType> getAllowedEntityTypes() {
        try {
            return client.allowedEntities();
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            return Collections.emptySet();
        }
    }

    public static Date getNextDeploymentTime() {
        Calendar res = null;
        try {
            String defaultScheduleStr = client
                    .getConfigValue("DefaultDeploymentTime");
            if (defaultScheduleStr != null) {
                // Try parsing the date
                String[] tok = defaultScheduleStr.split("[ :]");
                int mm = -1, hh = -1, dd = -1, period = -1;
                switch (tok.length) {
                case 2:
                    hh = parseHours(tok[0]);
                    mm = parseMins(tok[1]);
                    break;
                case 3:
                    dd = parseDayOfWeek(tok[0]);
                    hh = parseHours(tok[1]);
                    mm = parseMins(tok[2]);
                    break;
                case 4:
                    period = parsePeriod(tok[0]);
                    dd = parseDayOfWeek(tok[1]);
                    hh = parseHours(tok[2]);
                    mm = parseMins(tok[3]);
                    break;
                }
                if (hh != -1 && mm != -1) {
                    res = new GregorianCalendar();
                    res.setTimeInMillis(System.currentTimeMillis());
                    res.add(Calendar.MILLISECOND, 1);
                    rollTo(0, res, Calendar.MILLISECOND);
                    rollTo(0, res, Calendar.SECOND);
                    rollTo(mm, res, Calendar.MINUTE);
                    rollTo(hh, res, Calendar.HOUR_OF_DAY);
                    if (dd != -1) {
                        int safety = 0;
                        while (safety++ < 10000
                                && res.get(Calendar.DAY_OF_WEEK) != dd) {
                            res.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        if (period != -1) {
                            safety = 0;
                            if (period != 6) {
                                while (safety++ < 10000
                                        && (res.get(Calendar.DAY_OF_WEEK) != dd || res
                                                .get(Calendar.DAY_OF_WEEK_IN_MONTH) != period)) {
                                    res.add(Calendar.DAY_OF_MONTH, 1);
                                }
                            } else {
                                // Last day of week in a month
                                do {
                                    Calendar tmp = new GregorianCalendar();
                                    tmp.setTimeInMillis(res.getTimeInMillis());
                                    tmp.add(Calendar.DAY_OF_MONTH, 7);
                                    if (tmp.get(Calendar.MONTH) == res.get(Calendar.MONTH)) {
                                        res.add(Calendar.DAY_OF_MONTH, 7);
                                    } else {
                                        break;
                                    }
                                } while (safety++ < 10000);
                            }
                        }
                    }
                }
            }
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
        }
        if (res == null) {
            // We did not get the schedule - use 12 midnight
            res = new GregorianCalendar();
            res.setTimeInMillis(System.currentTimeMillis());
            res.add(Calendar.MILLISECOND, 1);
            rollTo(0, res, Calendar.MILLISECOND);
            rollTo(0, res, Calendar.SECOND);
            rollTo(0, res, Calendar.MINUTE);
            rollTo(0, res, Calendar.HOUR_OF_DAY);
        }
        return UnmodifiableDate.forTime(res.getTimeInMillis());
    }

    public static void updateComputersWithAgents() throws PolicyEditorException {
        client.updateComputersWithAgents();
    }

    private static void rollTo(int val, Calendar cal, int field) {
        int safety = 0;
        while (safety++ < 10000 && cal.get(field) != val) {
            cal.add(field, 1);
        }
    }

    private static int parseHours(String hh) {
        if (hh == null) {
            return -1;
        }
        try {
            int res = Integer.parseInt(hh);
            return (res >= 0 && res < 24) ? res : -1;
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    private static int parseMins(String mm) {
        if (mm == null) {
            return -1;
        }
        try {
            int res = Integer.parseInt(mm);
            return (res >= 0 && res < 60) ? res : -1;
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    private static int parseDayOfWeek(String ss) {
        if (dayOfWeekNames != null && ss != null) {
            for (int i = 0; i != dayOfWeekNames.length; i++) {
                if (ss.equalsIgnoreCase(dayOfWeekNames[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int parsePeriod(String ss) {
        if (ss != null) {
            if ("first".equalsIgnoreCase(ss) || ss.startsWith("1")) {
                return 1;
            }
            if ("second".equalsIgnoreCase(ss) || ss.startsWith("2")) {
                return 2;
            }
            if ("third".equalsIgnoreCase(ss) || ss.startsWith("3")) {
                return 3;
            }
            if ("fourth".equalsIgnoreCase(ss) || ss.startsWith("4")) {
                return 4;
            }
            if ("fifth".equalsIgnoreCase(ss) || ss.startsWith("5")) {
                return 5;
            }
            if ("last".equalsIgnoreCase(ss)) {
                return 6;
            }
        }
        return -1;
    }

    /**
     * Determine if there are object ready to deploy
     * 
     * @return true if there are object to deploy; false otherwise
     * @throws PolicyEditorException
     */
    public static boolean hasObjectsToDeploy() {
        boolean result = false;
        try {
            result = client.hasObjectsToDeploy();
        } catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
            LoggingUtil.logError(Activator.ID, "fail to query whether we have objects to depoly", e);
        }
        return result;
    }

    /**
     * @param domainObjectDescriptor
     * @return
     * @throws PolicyEditorException
     */
    public static DomainObjectUsage getUsage(
            DomainObjectDescriptor domainObjectDescriptor)
            throws PolicyEditorException {
        List<DomainObjectUsage> usageList = client.getUsageList(
                Collections.singletonList(domainObjectDescriptor));
        return usageList.get(0);
    }

    /**
     * Escapes a name to make it suitable for use in queries that do not expect
     * wildcards.
     * 
     * @param name
     *            the name to escape.
     * @return the escaped name.
     */
    public static String escape(String name) {
        return name.replaceAll("[_%]", "\\\\$0");
    }

    /**
     * Search for leaf objects using a spec.
     * 
     * @param searchSpec
     * @return the list of matching leaf objects
     * @throws PolicyEditorException
     */
    public static List<LeafObject> runLeafObjectQuery(
            LeafObjectSearchSpec searchSpec) throws PolicyEditorException {
        if (searchSpec == null) {
            throw new NullPointerException("searchSpec cannot be null.");
        }

        return client.runLeafObjectQuery(searchSpec);
    }

    /**
     * Search for leaf objects using an array of IDs
     * 
     * @param elementIds
     *            an array of element IDs for which to get leaf objects.
     * @param userGroupIds
     *            an array of element IDs for which to get leaf objects.
     * @param hostGroupIds
     *            an array of element IDs for which to get leaf objects.
     * @return the list of matching leaf objects
     * @throws PolicyEditorException
     */
    public static List<LeafObject> getLeafObjectsForIds(long[] elementIds,
            long[] userGroupIds, long[] hostGroupIds)
            throws PolicyEditorException {
        if (elementIds == null || userGroupIds == null || hostGroupIds == null) {
            throw new NullPointerException("ids may not be null.");
        }
        return client.getLeafObjectsForIds(elementIds, userGroupIds,
                hostGroupIds);
    }

    public static Set<IRealm> getDictionaryRealms(PolicyEditorRoles profile)
            throws PolicyEditorException {
        Set<IRealm> realmsToReturn = new HashSet<IRealm>();
        Set<Realm> enrollmentNames = client.getDictionaryEnrollmentRealms(profile);
        for (final Realm enrollment : enrollmentNames) {
            final String name = enrollment.getName();
            realmsToReturn.add(new IRealm() {

                /**
                 * @see com.bluejungle.destiny.policymanager.model.IRealm#getId()
                 */
                public String getId() {
                    return name;
                }

                /**
                 * @see com.bluejungle.destiny.policymanager.model.IRealm#getTitle()
                 */
                public String getTitle() {
                    return name;
                }

                /**
                 * @see com.bluejungle.destiny.policymanager.model.IRealm#getEnrollmentType()
                 */
                public EnrollmentType getEnrollmentType() {
                    return EnrollmentType.fromName(enrollment.getType().getValue());
                }
            });
        }
        return realmsToReturn;
    }

    private static List<DomainObjectDescriptor> adjustType(
            Collection<DomainObjectDescriptor> entities) {
        // List<DomainObjectDescriptor> res = new
        // ArrayList<DomainObjectDescriptor>();
        // for (DomainObjectDescriptor descr : entities) {
        // EntityType type;
        // if (descr.getType() == null
        // || descr.getType() == EntityType.ILLEGAL
        // || descr.getType() == EntityType.COMPONENT) {
        // String name = descr.getName();
        // int pos = name.indexOf(PQLParser.SEPARATOR);
        // if (pos != -1) {
        // type = EntityType.COMPONENT;
        // } else {
        // type = descr.getType();
        // }
        // } else {
        // type = descr.getType();
        // }
        // res.add(new DomainObjectDescriptor(descr.getId(), descr.getName(),
        // descr.getOwner(), descr.getAccessPolicy(), type, descr
        // .getDescription(), descr.getStatus(), descr
        // .getVersion(), descr.getWhenCreated(), descr
        // .getLastUpdated(), descr.isHidden(), descr
        // .isAccessible(), descr.hasDependencies()));
        // }
        return new ArrayList<DomainObjectDescriptor>(entities);
    }

    /**
     * Tries to acquire a lock for an object. If the acquisition fails, a
     * PolicyEditorException is thrown
     * 
     * @param key
     *            an object for which we need to acquire a lock.
     * @param force
     *            a flag indicating whether an existing lock, if any, should be
     *            broken.
     * @throws PolicyEditorException
     *             when the operation cannot complete or if the lock is already
     *             owned.
     */
    public static void acquireLock(IHasId object) throws PolicyEditorException {
        client.acquireLock(object, false);
    }

    /**
     * Releases the lock if held by the specified holder.
     * 
     * @param object
     *            the object the lock for which needs to be released.
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    public static void releaseLock(IHasId object) throws PolicyEditorException {
        client.releaseLock(object);

    }

    /**
     * Determine if the lock for the specified object is owned
     * 
     * @param domainObject
     *            the object being tested
     * @return true if it is owned; false otherwise
     * @throws PolicyEditorException
     *             if the test fails
     */
    public static boolean hasLock(IHasId domainObject)
            throws PolicyEditorException {
        return client.hasLock(domainObject);
    }

    /**
     * Get all the basic actions
     * 
     * @return list of PolicyActionsDescriptor
     * @throws PolicyEditorException
     */
    public static Collection<PolicyActionsDescriptor> getAllPolicyActions()
            throws PolicyEditorException {
        return client.getAllPolicyActions();
    }

    public static List<DomainObjectDescriptor> getDescriptorsByIds(
            List<Long> ids) throws PolicyEditorException {
        return client.getDescriptorsByIds(ids);
    }

    public static List<DODDigest> getDODDigests(Collection<String> types)
            throws PolicyEditorException {
        List<DODDigest> result = client.getDODDigests(types);
        return result;
    }

    public static DomainObjectDescriptor getDescriptorById(Long id)
            throws PolicyEditorException {
        List<Long> ids = Arrays.asList(new Long[] { id });
        List<DomainObjectDescriptor> descriptors = getDescriptorsByIds(ids);

        return descriptors.get(0);
    }

    public static List<DomainObjectDescriptor> getDescriptorsByDigests(
            List<DODDigest> digests) throws PolicyEditorException {
        List<Long> ids = new ArrayList<Long>();
        for (DODDigest digest : digests) {
            ids.add(digest.getId());
        }
        return client.getDescriptorsByIds(ids);
    }

    /**
     * Saves entity to the server
     * 
     * @param entity
     * @return descriptors of saved objects
     */
    public static Collection<DomainObjectDescriptor> saveEntity(IHasId entity) {
        return saveEntities(Collections.singletonList(entity));
    }

    /**
     * Saves entity to the server
     * 
     * @param entity
     * @return descriptors of saved objects
     */
    public static List<DODDigest> saveEntityDigest(IHasId entity) {
        return saveEntitiesDigest(Collections.singletonList(entity));
    }

    public static List<DODDigest> saveEntitiesDigest(
            Collection<? extends IHasId> entities) {
        try {
            List<DODDigest> c = null;
            c = client.saveEntitiesDigest(entities);
            EntityInfoProvider.updateDescriptors(c);
            return c;
        } catch (PolicyEditorException e) {
        	String msg = ApplicationMessages.POLICYSERVERPROXY_ERROR_MSG;
            LoggingUtil.logError(Activator.ID, "error while saving entity", e);
            if (!(e == null ||
                e.getLocalizedMessage() == null ||
                e.getLocalizedMessage().length() == 0)) {
            	msg = e.getLocalizedMessage();
            }
            PolicyHelpers.timeOutCheck(e);
//            if (userSessionExpired(e)) {
//            	msg = ApplicationMessages.POLICYSERVERPROXY_LOGIN_MSG;
//            }      
//            MessageDialog
//            	.openError(
//            			Display.getCurrent().getActiveShell(),
//                        ApplicationMessages.POLICYSERVERPROXY_ERROR, 
//                        msg);
//            if (userSessionExpired(e)) {
//            	PolicyHelpers.shutDownWorkBench();
//            }
            return null;
        }

    }
    
    private static final Cache userIdToNameCache;
    static {
        // boolean eternal, long timeToLiveSeconds, long timeToIdleSeconds
        userIdToNameCache = new Cache(
                PolicyServerProxy.class.getSimpleName() + "." + "userIdToNameCache"
              , 1000    // int maximumSize
              , false   // boolean overflowToDisk
              , true   // boolean eternal
              , 0 // long timeToLiveSeconds forever
              , 0 // long timeToIdleSeconds forever
        );
        try {
            CacheManager.getInstance().addCache(userIdToNameCache);
        } catch (IllegalStateException e) {
            throw new ExceptionInInitializerError(e);
        } catch (ObjectExistsException e) {
            throw new ExceptionInInitializerError(e);
        } catch (CacheException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    private static String getUserName(LeafObject user) {
        // look com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO
        
        // the username is login@domain. we only want the username but not domain
        
        String uniqueName = user.getUniqueName();
        int seperatorIndex= uniqueName.indexOf('@');
        if (seperatorIndex <= 0) {
            // check == 0 too, that means the login name is empty
            // the login should not be empty so we return the original string
            return uniqueName;
        }
        
        return uniqueName.substring(0, seperatorIndex);
    }
    
    public static String getUserName(Long userId) throws PolicyEditorException {
        String modifierName;
        if (userId != null) {
            try {
                Element element = userIdToNameCache.get(userId);
                if (element != null) {
                    modifierName = (String) element.getValue();
                    return modifierName;
                }
            } catch (IllegalStateException e) {
                LoggingUtil.logError(Activator.ID, "Fail to lookup username from userid in cache", e);
            } catch (CacheException e) {
                LoggingUtil.logError(Activator.ID, "Fail to lookup username from userid in cache", e);
            }
            
            LeafObject superuser = PolicyServerProxy.getSuperUser();
            if (superuser != null && userId.equals(superuser.getId())) {
                modifierName = getUserName(superuser);
            } else {
                IPredicate pred = SubjectAttribute.APPUSER_ID.buildRelation(
                        RelationOp.EQUALS, Constant.build(userId));
                
                LeafObjectSearchSpec leafObjectSearchSpec = new LeafObjectSearchSpec(
                        LeafObjectType.APPUSER, pred, 1);
                
                List<LeafObject> user = EntityInfoProvider.runLeafObjectQuery(leafObjectSearchSpec);
                if(user == null || user.size() !=1){
                    LoggingUtil.logWarning(Activator.ID, "User not found" + userId, null);
                    modifierName = "Unknown User (" + userId.toString() + ")";
                } else {
                    modifierName = getUserName(user.get(0));
                }
            }
            assert modifierName != null;
            userIdToNameCache.put(new Element(userId, modifierName));
        } else {
            modifierName = "";
        }
        return modifierName;
    }
}
