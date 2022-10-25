/*
 * Created on Mar 14, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.dictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.UserProfileEnum;
import com.bluejungle.destiny.policymanager.model.IRealm;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.BrowserMessages;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.DefaultSelectableLeafObjectSourceImpl;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectsSource;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.SelectableLeafObjectSourceException;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.PlatformUtils;
import com.bluejungle.destiny.services.policy.types.PolicyEditorRoles;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectSearchSpec;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/ui/dialogs/browser/impl/dictionary
 *          /DictionarySelectableLeafObjectSourceImpl.java#1 $
 */

public class DictionarySelectableLeafObjectSourceImpl extends
		DefaultSelectableLeafObjectSourceImpl implements
		ISelectableLeafObjectsSource {

	private List<IRealm> dictionaryRealms;

	private Combo realmInputFieldCombo;
	private Text searchInputFieldText;

	private static final Map<LeafObjectType, SubjectAttribute> LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP = new HashMap<LeafObjectType, SubjectAttribute>();

	static {
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.HOST,
				SubjectAttribute.HOST_NAME);
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.HOST_GROUP,
				SubjectAttribute.HOST_LDAP_GROUP_DISPLAY_NAME);
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.USER,
				SubjectAttribute
						.forNameAndType("displayName", SubjectType.USER));
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.USER_GROUP,
				SubjectAttribute.USER_LDAP_GROUP_DISPLAY_NAME);
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.CONTACT,
				SubjectAttribute.forNameAndType("mail", SubjectType.USER));
	}

	/**
	 * Create an instance of DictionarySelectableLeafObjectSourceImpl
	 * 
	 * @param parent
	 */
	public DictionarySelectableLeafObjectSourceImpl(Composite parent) {
		super(parent);
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.DefaultSelectableLeafObjectSourceImpl#getSelectableLeafObjects(com.bluejungle.pf.destiny.lib.LeafObjectType,
	 *      int)
	 */
	@Override
	public List<LeafObject> getSelectableLeafObjects(
			LeafObjectType leafObjectType, int maxResults)
			throws SelectableLeafObjectSourceException {
		if (leafObjectType == null) {
			throw new NullPointerException("leafObjectType cannot be null.");
		}

		List<LeafObject> itemsToReturn = null;
		String searchString = searchInputFieldText.getText();

		SubjectAttribute attr = LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP
				.get(leafObjectType);
		if (attr == null) {
			throw new IllegalArgumentException("Unexpected type: "
					+ leafObjectType);
		}
		IPredicate pred = attr.buildRelation(RelationOp.EQUALS, Constant
				.build(searchString + "*"));

		IRealm dictionaryRealm = getDictionaryRealm();
		String namespaceId = (dictionaryRealm == IRealm.ALL_REALMS_REALM) ? null
				: dictionaryRealm.getId();

		LeafObjectSearchSpec leafObjectSearchSpec = new LeafObjectSearchSpec(
				leafObjectType, pred, namespaceId, maxResults);

		try {
			itemsToReturn = EntityInfoProvider
					.runLeafObjectQuery(leafObjectSearchSpec);
		} catch (PolicyEditorException exception) {
			throw new SelectableLeafObjectSourceException(
					"Failed to retrieve leaf objects", exception);
		}

		return itemsToReturn;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.DefaultSelectableLeafObjectSourceImpl#buildSearchControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void buildSearchControls(Composite parent) {
		Composite searchControlComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		searchControlComposite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		searchControlComposite.setLayoutData(data);

		// The User Realm Input label and element
		Label sourceInputFieldLabel = new Label(searchControlComposite,
				SWT.NONE);
		sourceInputFieldLabel
				.setText(BrowserMessages.SELECTABLEDICTIONARYBASEDLEAFOBJECTSOURCECOMPOSITE_SOURCE);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		sourceInputFieldLabel.setLayoutData(data);

		realmInputFieldCombo = new Combo(searchControlComposite, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		List<IRealm> dictionaryRealms = getDictionaryRealms();
		ListIterator<IRealm> dictionaryRealmIterator = dictionaryRealms
				.listIterator();
		while (dictionaryRealmIterator.hasNext()) {
			int nextDictionaryRealmIndex = dictionaryRealmIterator.nextIndex();
			IRealm nextDictionaryRealm = (IRealm) dictionaryRealmIterator
					.next();
			realmInputFieldCombo.add(nextDictionaryRealm.getTitle(),
					nextDictionaryRealmIndex);
		}
		realmInputFieldCombo.select(0);
		realmInputFieldCombo
				.addModifyListener(new DictionaryRealmComboModifyListener());

		data = new GridData(GridData.FILL_HORIZONTAL);
		realmInputFieldCombo.setLayoutData(data);

		new Label(searchControlComposite, SWT.NONE);

		// The search input label and field
		Label searchInputFieldLabl = new Label(searchControlComposite, SWT.NONE);
		searchInputFieldLabl
				.setText(BrowserMessages.SELECTABLEDICTIONARYBASEDLEAFOBJECTSOURCECOMPOSITE_NAME_STARTS);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		searchInputFieldLabl.setLayoutData(data);

		searchInputFieldText = new Text(searchControlComposite, SWT.SINGLE
				| SWT.BORDER);
		searchInputFieldText.setTextLimit(128);
		data = new GridData(GridData.FILL_HORIZONTAL);
		searchInputFieldText.setLayoutData(data);
		searchInputFieldText
				.addKeyListener(new RefreshSearchDataOnReturnKeyListener());

		// The find button
		Button findNowButton = new Button(searchControlComposite, SWT.NONE);
		findNowButton
				.setText(BrowserMessages.SELECTABLEDICTIONARYBASEDLEAFOBJECTSOURCECOMPOSITE_FIND_NOW);
		findNowButton.addSelectionListener(new RefreshSearchDataListener());
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		findNowButton.setLayoutData(data);
	}

	/**
	 * Retrieve the list of user realms
	 * 
	 * @return the list of user realms
	 */
	private List<IRealm> getDictionaryRealms() {
		if (dictionaryRealms == null) {
			try {
				UserProfileEnum profile = PlatformUtils.getProfile();
				PolicyEditorRoles role = null;
				if (profile == UserProfileEnum.CORPORATE)
					role = PolicyEditorRoles.CORPORATE;
				else if (profile == UserProfileEnum.FILESYSTEM)
					role = PolicyEditorRoles.FILESYSTEM;
				else if (profile == UserProfileEnum.PORTAL)
					role = PolicyEditorRoles.PORTAL;
				Set<IRealm> dictionaryRealmsSet = PolicyServerProxy
						.getDictionaryRealms(role);
				dictionaryRealms = new ArrayList<IRealm>(dictionaryRealmsSet);
				Collections.sort(dictionaryRealms, new Comparator<IRealm>() {

					public int compare(IRealm o1, IRealm o2) {
						return o1.getId().compareTo(o2.getId());
					}
				});
				dictionaryRealms.add(0, IRealm.ALL_REALMS_REALM);
			} catch (PolicyEditorException exception) {
				LoggingUtil.logWarning(Activator.ID,
						"Failed to retrieve dictionary realms.", exception);
				dictionaryRealms = new ArrayList<IRealm>(1);
				dictionaryRealms.add(IRealm.ALL_REALMS_REALM);
			}
		}
		return dictionaryRealms;
	}

	/**
	 * @return
	 */
	private IRealm getDictionaryRealm() {
		int dictionaryRealmComboSelectedIndex = realmInputFieldCombo
				.getSelectionIndex();
		if (dictionaryRealmComboSelectedIndex == -1) {
			throw new IllegalStateException(
					"Dictionary Realm Combo does not have selected item.");
		}
		return dictionaryRealms.get(dictionaryRealmComboSelectedIndex);
	}

	/**
	 * A ModifyListener attached to the User Realm Drop down box which refreshed
	 * the list of available user and user group subjects listed according to
	 * the newly selected user realm
	 * 
	 * @author sgoldstein
	 */
	private class DictionaryRealmComboModifyListener implements ModifyListener {

		/**
		 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		public void modifyText(ModifyEvent modifyEvent) {
			DictionarySelectableLeafObjectSourceImpl.this
					.refreshSelectableObjectList();
		}
	}
}
