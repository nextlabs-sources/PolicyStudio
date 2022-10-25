package com.bluejungle.destiny.policymanager.ui;

/*
 * Created on May 2, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.model.EditorElementHelper;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.PluginUtil;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectSearchSpec;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.epicenter.common.ISpec;

/**
 * @author dstarke
 */
@SuppressWarnings("unchecked")
public class EntityInfoProvider {

	private static final Map<String, List<DODDigest>> componentListStore = new HashMap<String, List<DODDigest>>();
	private static final Map<String, Map<String, DODDigest>> componentMapStore = new HashMap<String, Map<String, DODDigest>>();
	private static final Map<String, List<EntityInfoListener>> componentListeners = new HashMap<String, List<EntityInfoListener>>();

	static {
		for (String ev : EditorElementHelper.getComponentContexts()) {
			componentListStore.put(ev, new ArrayList<DODDigest>());
			componentMapStore.put(ev, new HashMap<String, DODDigest>());
			componentListeners.put(ev, new ArrayList<EntityInfoListener>());
		}
	}
	// private static final List<DODDigest> policyDigestList = new
	// ArrayList<DODDigest>();
	private static final List<DODDigest> policyList = new ArrayList<DODDigest>();
	private static final Map<String, DODDigest> policyMap = new HashMap<String, DODDigest>();

	private static final List<EntityInfoListener> policyListeners = new ArrayList<EntityInfoListener>();

	private static final Map<String, LeafObject>[] leafObjectByName = new Map[LeafObjectType
			.getElementCount()];
	private static final Map<Long, LeafObject>[] leafObjectById = new Map[LeafObjectType
			.getElementCount()];
	private static final Set<Long> allKnownLeafIds = new HashSet<Long>();

	private static final Map<LeafObjectType, SubjectAttribute> LEAF_TO_SUBJ_ID_ATTR = new HashMap<LeafObjectType, SubjectAttribute>();
	private static final Map<LeafObjectType, SubjectAttribute> LEAF_TO_SUBJ_NAME_ATTR = new HashMap<LeafObjectType, SubjectAttribute>();
	private static final Set<IAttribute> ELEMENT_ID_ATTRIBUTES = new HashSet<IAttribute>();
	private static final Set<IAttribute> USER_GROUP_ID_ATTRIBUTES = new HashSet<IAttribute>();
	private static final Set<IAttribute> HOST_GROUP_ID_ATTRIBUTES = new HashSet<IAttribute>();
	private static final IPredicate EMPTY_USER_NAME = SubjectAttribute.USER_NAME
			.buildRelation(RelationOp.EQUALS, Constant.build(""));
	private static final IPredicate EMPTY_GROUP_NAME = SubjectAttribute.USER_LDAP_GROUP
			.buildRelation(RelationOp.EQUALS, Constant.build(""));

	// We'll never get that many, but we must supply a limit
	private static final int MAX_LOCAL_ITEMS = 100000;

	static {
		LEAF_TO_SUBJ_ID_ATTR.put(LeafObjectType.CONTACT,
				SubjectAttribute.CONTACT_ID);
		LEAF_TO_SUBJ_ID_ATTR.put(LeafObjectType.APPLICATION,
				SubjectAttribute.APP_ID);
		LEAF_TO_SUBJ_ID_ATTR.put(LeafObjectType.HOST, SubjectAttribute.HOST_ID);
		LEAF_TO_SUBJ_ID_ATTR.put(LeafObjectType.HOST_GROUP,
				SubjectAttribute.HOST_LDAP_GROUP_ID);
		LEAF_TO_SUBJ_ID_ATTR.put(LeafObjectType.USER, SubjectAttribute.USER_ID);
		LEAF_TO_SUBJ_ID_ATTR.put(LeafObjectType.USER_GROUP,
				SubjectAttribute.USER_LDAP_GROUP_ID);
		LEAF_TO_SUBJ_NAME_ATTR.put(LeafObjectType.APPLICATION,
				SubjectAttribute.APP_NAME);
		LEAF_TO_SUBJ_NAME_ATTR.put(LeafObjectType.HOST,
				SubjectAttribute.HOST_NAME);
		LEAF_TO_SUBJ_NAME_ATTR.put(LeafObjectType.HOST_GROUP,
				SubjectAttribute.HOST_LDAP_GROUP);
		LEAF_TO_SUBJ_NAME_ATTR.put(LeafObjectType.USER,
				SubjectAttribute.USER_NAME);
		LEAF_TO_SUBJ_NAME_ATTR.put(LeafObjectType.APPUSER,
				SubjectAttribute.USER_NAME);
		LEAF_TO_SUBJ_NAME_ATTR.put(LeafObjectType.USER_GROUP,
				SubjectAttribute.USER_LDAP_GROUP);
		LEAF_TO_SUBJ_NAME_ATTR.put(LeafObjectType.ACCESSGROUP,
				SubjectAttribute.USER_LDAP_GROUP);
		int len = leafObjectByName.length;
		assert len == leafObjectById.length;
		for (int i = 0; i < len; i++) {
			leafObjectByName[i] = new HashMap<String, LeafObject>();
			leafObjectById[i] = new HashMap<Long, LeafObject>();
		}
		ELEMENT_ID_ATTRIBUTES.add(ResourceAttribute.OWNER);
		ELEMENT_ID_ATTRIBUTES.add(SubjectAttribute.APP_ID);
		ELEMENT_ID_ATTRIBUTES.add(SubjectAttribute.HOST_ID);
		ELEMENT_ID_ATTRIBUTES.add(SubjectAttribute.USER_ID);
		USER_GROUP_ID_ATTRIBUTES.add(SubjectAttribute.USER_LDAP_GROUP_ID);
		USER_GROUP_ID_ATTRIBUTES.add(ResourceAttribute.OWNER_LDAP_GROUP);
		HOST_GROUP_ID_ATTRIBUTES.add(SubjectAttribute.HOST_LDAP_GROUP_ID);
	}

	/**
	 * Get the current entity list for a given type. Does not ensure that the
	 * list exists or is up to date.
	 */
	public static synchronized List<DODDigest> getComponentList(
			String componentType) {
		return Collections.unmodifiableList(componentListStore
				.get(componentType));
	}

	public static synchronized List<DODDigest> getPolicyList() {
		return Collections.unmodifiableList(policyList);
	}

	/**
	 * Updates the descriptors of the specified objects
	 * 
	 * @param descriptors
	 *            list of objects to update
	 */
	public static synchronized void updateDescriptors(
			Collection<DODDigest> descriptors) {
		for (DODDigest descriptor : descriptors) {
			String type = descriptor.getType();
			String name = descriptor.getName();
			if (!type.equals("POLICY") && !type.equals("FOLDER")) {
				String componentType = getComponentType(descriptor);

				Map<String, DODDigest> componentMap = componentMapStore
						.get(componentType);
				DODDigest oldDescriptor = componentMap.get(name);
				componentMap.put(name, descriptor);

				List<DODDigest> componentList = componentListStore
						.get(componentType);
				int index = componentList.indexOf(oldDescriptor);
				if (index != -1) {
					componentList.remove(oldDescriptor);
					componentList.add(index, descriptor);
				}
			} else if (type.equals("POLICY")) {
				DODDigest oldPolicy = policyMap.get(name);
				policyMap.put(name, descriptor);

				int index = policyList.indexOf(oldPolicy);
				if (index >= 0) {
					policyList.remove(oldPolicy);
					policyList.add(index, descriptor);
				}
			}
		}
	}

	public static DODDigest getComponentDescriptor(String name) {
		return componentMapStore.get(getComponentType(name)).get(name);
	}

	public static DODDigest getPolicyDescriptor(String name) {
		return policyMap.get(name);
	}

	public static DODDigest getPolicyFolderDescriptor(String name) {
		for (DODDigest descriptor : policyList) {
			if (descriptor.getName().equals(name)
					&& descriptor.getType().equals("FOLDER"))
				return descriptor;
		}
		return null;
	}

	public static synchronized LeafObject getLeafObject(String name,
			LeafObjectType type) {
		if (name == null) {
			throw new NullPointerException("name");
		}
		SubjectAttribute subjAttr = (SubjectAttribute) LEAF_TO_SUBJ_NAME_ATTR
				.get(type);
		try {
			if (subjAttr != null && name != null
					&& !leafObjectByName[type.getType()].containsKey(name)) {
				List leafObjects;
				cacheLeafObjects(leafObjects = PolicyServerProxy
						.runLeafObjectQuery(new LeafObjectSearchSpec(type,
								subjAttr.buildRelation(RelationOp.EQUALS,
										Constant.build(name)), 2)));
				if (leafObjects != null && leafObjects.size() == 1) {
					return (LeafObject) leafObjects.iterator().next();
				}
			}
		} catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
			LoggingUtil.logError(Activator.ID, "error get leaf object", e);
		}
		return (LeafObject) leafObjectByName[type.getType()].get(name
				.toUpperCase());
	}

	public static synchronized LeafObject getLeafObjectByID(Long id,
			LeafObjectType type) {
		SubjectAttribute subjAttr = (SubjectAttribute) LEAF_TO_SUBJ_ID_ATTR
				.get(type);
		try {
			if (!leafObjectById[type.getType()].containsKey(id)) {
				if (subjAttr != null) {
					if (id != null) {
						cacheLeafObjects(PolicyServerProxy
								.runLeafObjectQuery(new LeafObjectSearchSpec(
										type,
										subjAttr.buildRelation(
												RelationOp.EQUALS, Constant
														.build(id.longValue())),
										2)));
					}
				} else if (type == LeafObjectType.APPUSER) {
					cacheLeafObjects(PolicyServerProxy
							.runLeafObjectQuery(new LeafObjectSearchSpec(type,
									EMPTY_USER_NAME, MAX_LOCAL_ITEMS)));
					cacheLeafObjects(Arrays
							.asList(new Object[] { PolicyServerProxy
									.getSuperUser() }));
				} else if (type == LeafObjectType.ACCESSGROUP) {
					cacheLeafObjects(PolicyServerProxy
							.runLeafObjectQuery(new LeafObjectSearchSpec(type,
									EMPTY_GROUP_NAME, MAX_LOCAL_ITEMS)));
				}
			}
		} catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
			LoggingUtil
					.logError(Activator.ID, "error get leaf object by id", e);
		}
		return (LeafObject) leafObjectById[type.getType()].get(id);
	}

	private static void cacheLeafObjects(List list) {
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			LeafObject leaf = (LeafObject) iter.next();
			int type = leaf.getType().getType();
			leafObjectById[type].put(leaf.getId(), leaf);
			if (leaf.getType() != LeafObjectType.ACCESSGROUP
					&& leaf.getType() != LeafObjectType.APPUSER) {
				// Application user manager does not share the ID space with the
				// dictionary
				allKnownLeafIds.add(leaf.getId());
			}

			String key = leaf.getUniqueName();
			if (key == null) {
				continue;
			}
			key = key.toUpperCase();
			Object existing = leafObjectByName[type].get(key);
			if (existing != null) {
				if (!existing.equals(leaf)) {
					leafObjectByName[type].remove(key);
				}
			} else {
				leafObjectByName[type].put(key, leaf);
			}
		}
	}

	public static void loadLeafObjectsForPolicies(Collection policies) {
		if (policies == null) {
			throw new NullPointerException("policies");
		}
		IPredicate[] targets = new IPredicate[policies.size()];
		int i = 0;
		for (Iterator iter = policies.iterator(); iter.hasNext(); i++) {
			// Policies references leaf objects only in its deployment target
			targets[i] = ((IDPolicy) iter.next()).getDeploymentTarget();
		}
		addLeafIdsFromPredicates(targets);
	}

	public static void loadLeafObjectsForComponent(ISpec component) {
		if (component == null) {
			throw new NullPointerException("component");
		}
		addLeafIdsFromPredicates(new IPredicate[] { component.getPredicate() });
	}

	private static void addLeafIdsFromPredicates(IPredicate[] preds) {
		if (preds == null) {
			throw new NullPointerException("preds");
		}
		final List<Long> elementIds = new ArrayList<Long>(20);
		final List<Long> userGroupIds = new ArrayList<Long>(20);
		final List<Long> hostGroupIds = new ArrayList<Long>(20);
		for (int i = 0; i != preds.length; i++) {
			preds[i].accept(new DefaultPredicateVisitor() {
				@Override
				public void visit(IRelation pred) {
					// This method does not pay attention to the operator
					// because it looks only for ID-based attributes where
					// the operator is either == or !=
					addIds(pred.getLHS(), pred.getRHS());
					addIds(pred.getRHS(), pred.getLHS());
				}

				// This method assumes that the attribute is on the left
				// and the constant is on the right. We try calling this method
				// both ways, so this approach works even when the attribute
				// and the constant are swapped.
				private void addIds(IExpression lhs, IExpression rhs) {
					if (rhs instanceof Constant) {
						Object val = rhs.evaluate(null).getValue();
						if (val instanceof Long
								&& !allKnownLeafIds.contains(val)) {
							Long longVal = (Long) val;
							if (ELEMENT_ID_ATTRIBUTES.contains(lhs)) {
								elementIds.add(longVal);
							} else if (USER_GROUP_ID_ATTRIBUTES.contains(lhs)) {
								userGroupIds.add(longVal);
							} else if (HOST_GROUP_ID_ATTRIBUTES.contains(lhs)) {
								hostGroupIds.add(longVal);
							}
						}
					}
				}
			}, IPredicateVisitor.POSTORDER);
		}
		loadLeafObjects((Long[]) elementIds
				.toArray(new Long[elementIds.size()]), (Long[]) userGroupIds
				.toArray(new Long[userGroupIds.size()]), (Long[]) hostGroupIds
				.toArray(new Long[hostGroupIds.size()]));
	}

	private static synchronized void loadLeafObjects(Long[] elementIds,
			Long[] userGroupIds, Long[] hostGroupIds) {
		if (elementIds == null || userGroupIds == null || hostGroupIds == null) {
			return;
		}
		if (elementIds.length == 0 && userGroupIds.length == 0
				&& hostGroupIds.length == 0) {
			return;
		}
		try {
			cacheLeafObjects(PolicyServerProxy.getLeafObjectsForIds(
					convert(elementIds), convert(userGroupIds),
					convert(hostGroupIds)));
		} catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
			LoggingUtil.logError(Activator.ID, "error load leaf objects", e);
		}
	}

	private static long[] convert(Long[] ids) {
		long[] res = new long[ids.length];
		for (int i = 0; i != ids.length; i++) {
			res[i] = (ids[i] != null) ? ids[i].longValue() : -1;
		}
		return res;
	}

	public static void updateComponentList(String componentType) {
		Collection<DODDigest> entities = null;
		try {
			entities = PolicyServerProxy.getDODDigests(Arrays
					.asList(new String[] { componentType }));
		} catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
			e.printStackTrace();
		}
		if (entities != null) {
			DODDigest[] tmp = entities.toArray(new DODDigest[entities.size()]);
			Arrays.sort(tmp, DODDigest.CASE_INSENSITIVE_COMPARATOR);
			Map<String, DODDigest> componentMap = componentMapStore
					.get(componentType);
			List<DODDigest> componentList = componentListStore
					.get(componentType);
			componentList.clear();
			componentList.addAll(Arrays.asList(tmp));
			componentMap.clear();
			for (DODDigest spec : entities) {
				componentMap.put(spec.getName(), spec);
			}
			fireComponentDataChanged(componentType);
		}
	}

	public static void updateComponentListAsync(final String componentType) {
		Thread t = new Thread(new Runnable() {

			public void run() {
				updateComponentList(componentType);
			}
		});
		t.start();
	}

	public static void updatePolicyTree() {
		// Collection<EntityType> types = new ArrayList<EntityType>();
		// types.add(EntityType.POLICY);
		// types.add(EntityType.FOLDER);

		Collection<String> types = new ArrayList<String>();
		types.add("POLICY");
		types.add("FOLDER");

		// Collection<DODDigest> policyTreeEntries = PolicyServerProxy
		// .getEntityList("%", types);
		Collection<DODDigest> policyTreeEntries = null;
		try {
			policyTreeEntries = PolicyServerProxy.getDODDigests(types);
		} catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
			e.printStackTrace();
		}

		if (policyTreeEntries != null) {
			policyList.clear();
			policyMap.clear();
			policyList.addAll(policyTreeEntries);
			for (DODDigest spec : policyTreeEntries) {
				if (spec.getType().equals("POLICY"))
					policyMap.put(spec.getName(), spec);
			}
			firePolicyDataChanged();
		}
	}

	public static void updatePolicyTreeAsync() {
		Thread t = new Thread(new Runnable() {

			public void run() {
				updatePolicyTree();
			}
		});
		t.start();
	}

	/**
	 * Search for leaf objects
	 * 
	 * @param searchSpec
	 * @return the list of leaf objects which match the specific search spec
	 * @throws PolicyEditorException
	 */
	public static List<LeafObject> runLeafObjectQuery(
			LeafObjectSearchSpec searchSpec) throws PolicyEditorException {
		if (searchSpec == null) {
			throw new NullPointerException("searchSpec cannot be null.");
		}
		List<LeafObject> res = PolicyServerProxy.runLeafObjectQuery(searchSpec);
		cacheLeafObjects(res);
		return res;
	}

	public synchronized static void addEntityInfoListener(
			EntityInfoListener listener, String type) {
		componentListeners.get(type).add(listener);
	}

	public synchronized static void removeEntityInfoListener(
			EntityInfoListener listener, String type) {
		componentListeners.get(type).remove(listener);
	}

	public synchronized static void addPolicyInfoListener(
			EntityInfoListener listener) {
		policyListeners.add(listener);
	}

	public synchronized static void removePolicyInfoListener(
			EntityInfoListener listener) {
		policyListeners.remove(listener);
	}

	private synchronized static void fireComponentDataChanged(
			String componentType) {
		for (EntityInfoListener listener : componentListeners
				.get(componentType)) {
			sendInfoUpdatedEvent(listener);
		}
	}

	private synchronized static void firePolicyDataChanged() {
		for (EntityInfoListener listener : policyListeners) {
			sendInfoUpdatedEvent(listener);
		}
	}

	private static void sendInfoUpdatedEvent(final EntityInfoListener listener) {
		Display display = Display.getDefault();
		display.asyncExec(new Runnable() {

			public void run() {
				listener.EntityInfoUpdated();
			}
		});
	}

	// -----------------------
	// Individual Entity Info
	// -----------------------

	// The regular expression below defines the following rule:
	// A name is invalid when
	// - It is empty, or
	// - It starts with a whitespace character, or
	// - It ends with a whitespace character, or
	// - it contains a '*','&','~','?', '$', '/' or a '\\' character.
	private static Pattern INVALID_NAME_PATTERN = Pattern
			.compile("|\\s.*|\\S*\\s|[^*&~?$/\\\\]*[*&~?$/\\\\].*");

	public static boolean isValidComponentName(String name) {
		return (name != null)
				&& (!INVALID_NAME_PATTERN.matcher(name).matches());
	}

	/**
	 * Finds an existing component name case-insensitively, or returns null if
	 * the name does not exist.
	 * 
	 * @param name
	 *            the name to check.
	 * @param componentType
	 *            the type of a component or a policy to check.
	 * @return the existing name, or null if it does not exist.
	 */
	public static synchronized String getExistingComponentName(String name,
			String componentType) {
		if (name == null) {
			throw new NullPointerException("name");
		}
		if (name.indexOf(PQLParser.SEPARATOR) == -1) {
			name = componentType.toString() + PQLParser.SEPARATOR + name;
		}
		for (Iterator iter = componentMapStore.get(componentType).keySet()
				.iterator(); iter.hasNext();) {
			String current = (String) iter.next();
			if (name.compareToIgnoreCase(current) == 0) {
				return current;
			}
		}
		return null;
	}

	/**
	 * Finds an existing policy name case-insensitively, or returns null if the
	 * name does not exist.
	 * 
	 * @param name
	 *            the name to check.
	 * @param componentType
	 *            the type of a component or a policy to check.
	 * @return the existing name, or null if it does not exist.
	 */
	public static String getExistingPolicyName(String name) {
		if (name == null) {
			throw new NullPointerException("name");
		}
		for (DODDigest policy : policyMap.values()) {
			String existingName = policy.getName();
			if (name.equalsIgnoreCase(existingName)) {
				return existingName;
			}
		}
		return null;
	}

	public static String getExistingPolicyFolderName(String name) {
		if (name == null) {
			throw new NullPointerException("name");
		}
		for (DODDigest policy : policyList) {
			String existingName = policy.getName();
			if (name.compareToIgnoreCase(existingName) == 0 ) {
//					&& policy.getType().equals("FOLDER")
				return policy.getName();
			}
		}
		return null;
	}

	/**
	 * replaced by updateComponend descriptors
	 * 
	 * Updates the descriptors of the specified objects. TODO: Currently all
	 * entities are updated for entity types in the specified list. We will
	 * change this later to update only the objects in the list. PF will change
	 * saveEntities to return updated descriptors for objects that were saved
	 * 
	 * @param objectList
	 *            list of objects to update
	 */
	public static void refreshDescriptors(Collection<DODDigest> descriptors) {
		Set<String> includedComponentTypes = new HashSet<String>();
		boolean hasPolicy = false;
		for (DODDigest descriptor : descriptors) {
			if (descriptor.getType().equals("POLICY")
					|| descriptor.getType().equals("FOLDER")) {
				hasPolicy = true;
			} else if (descriptor.getType().equals("COMPONENT")) {
				includedComponentTypes.add(getComponentType(descriptor));
			}
		}
		for (String componentType : includedComponentTypes) {
			updateComponentListAsync(componentType);
		}
		if (hasPolicy) {
			updatePolicyTreeAsync();
		}
	}

	/**
	 * removes named object descriptor from map. This is usually called when an
	 * object is being renamed.
	 * 
	 * @param name
	 *            name of object descriptor to remove
	 * @param componentType
	 */
	public static void replaceComponentDescriptor(String oldName,
			DODDigest newDescriptor) {
		String oldType = getComponentType(oldName);
		String newType = getComponentType(newDescriptor);
		if (!oldType.equals(newType)) {
			throw new IllegalArgumentException(
					"trying to change the type by renaming is not supported.");
		}
		Map<String, DODDigest> componentMap = componentMapStore.get(newType);
		DODDigest oldDescriptor = componentMap.remove(oldName);
		componentMap.put(newDescriptor.getName(), newDescriptor);
		List<DODDigest> componentList = componentListStore.get(newType);
		int index = componentList.indexOf(oldDescriptor);
		if (index >= 0) {
			componentList.remove(oldDescriptor);
			componentList.add(index, newDescriptor);
		}
		fireComponentDataChanged(newType);
	}

	/**
	 * removes named object descriptor from map. This is usually called when an
	 * object is being renamed.
	 * 
	 * @param name
	 *            name of object descriptor to remove
	 * @param entityType
	 */
	public static synchronized void replacePolicyDescriptor(String oldName,
			DODDigest newDescriptor) {
		if (newDescriptor.getType().equals("POLICY")) {
			DODDigest oldDescriptor = policyMap.remove(oldName);
			policyMap.put(newDescriptor.getName(), newDescriptor);
			int index = policyList.indexOf(oldDescriptor);
			if (index >= 0) {
				policyList.remove(oldDescriptor);
				policyList.add(index, newDescriptor);
			}
		} else if (newDescriptor.getType().equals("FOLDER")) {
			for (DODDigest descriptor : policyList) {
				if (descriptor.getName().equals(oldName)
						&& descriptor.getType().equals("FOLDER")) {
					policyList.remove(descriptor);
					policyList.add(newDescriptor);
					break;
				}
			}
		}

		firePolicyDataChanged();
	}

	private static String getComponentType(DODDigest descr) {
		return getComponentType(descr.getName());
	}

	private static String getComponentType(String name) {
		int pos = name.indexOf(PQLParser.SEPARATOR);
		if (pos == -1) {
			throw new IllegalArgumentException("descr.name");
		}
		return name.substring(0, pos).toUpperCase();
	}

	public static List<DomainObjectDescriptor> getDescriptorsByDigests(
			List<DODDigest> digests) {
		List<Long> ids = new ArrayList<Long>();
		for (DODDigest digest : digests) {
			ids.add(digest.getId());
		}

		List<DomainObjectDescriptor> descriptors = null;

		try {
			descriptors = PolicyServerProxy.getDescriptorsByIds(ids);
		} catch (PolicyEditorException e) {
        	PolicyHelpers.timeOutCheck(e);
			e.printStackTrace();
		}
		return descriptors;
	}

	public static void updateComponents() {
		for (String component : PluginUtil.getComponentTypeList()) {
			updateComponentList(component);
		}
	}

	public static void updateComponensAsync() {
		Thread t = new Thread(new Runnable() {

			public void run() {
				updateComponents();
			}
		});
		t.start();
	}
}
