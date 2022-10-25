package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.BrowserMessages;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectSearchSpec;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;

public class DefaultSelectableLeafObjectSourceImpl implements
		ISelectableLeafObjectsSource {

	private Text searchInputFieldText;
	private Set<ISelectableLeafObjectListModificationListener> modificationListeners = new HashSet<ISelectableLeafObjectListModificationListener>();

	private static final Map<LeafObjectType, SubjectAttribute> LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP = new HashMap<LeafObjectType, SubjectAttribute>();

	static {
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.HOST,
				SubjectAttribute.HOST_NAME);
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.HOST_GROUP,
				SubjectAttribute.HOST_LDAP_GROUP);
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.APPUSER,
				SubjectAttribute.USER_NAME);
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.ACCESSGROUP,
				SubjectAttribute.USER_LDAP_GROUP);
		LEAF_OBJECT_TYPE_TO_ATTRIBUTE_MAP.put(LeafObjectType.APPLICATION,
				SubjectAttribute.APP_NAME);
	}

	/**
	 * Create an instance of DefaultSelectableLeafObjectSourceImpl
	 * 
	 * @param parent
	 */
	public DefaultSelectableLeafObjectSourceImpl(Composite parent) {
		if (parent == null) {
			throw new NullPointerException("parent cannot be null.");
		}

		Group findMembersGroup = new Group(parent, SWT.NONE);
		findMembersGroup
				.setText(BrowserMessages.DEFAULTLEAFOBJECTBROWSER_FIND_MEMBERS);
		GridLayout layout = new GridLayout();
		findMembersGroup.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		findMembersGroup.setLayoutData(data);

		buildSearchControls(findMembersGroup);
	}

	/**
	 * 
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectsSource#getSelectableLeafObjects(com.bluejungle.pf.destiny.lib.LeafObjectType,
	 *      int)
	 */
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
		LeafObjectSearchSpec leafObjectSearchSpec = new LeafObjectSearchSpec(
				leafObjectType, pred, maxResults);

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
	 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectsSource#addListModificationListener(com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectListModificationListener)
	 */
	public void addListModificationListener(
			ISelectableLeafObjectListModificationListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener cannot be null.");
		}

		modificationListeners.add(listener);
	}

	protected void buildSearchControls(Composite parent) {
		Composite searchControlComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		searchControlComposite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		searchControlComposite.setLayoutData(data);

		Label searchInputFieldLabl = new Label(searchControlComposite, SWT.NONE);
		searchInputFieldLabl
				.setText(BrowserMessages.DEFAULTSELECTABLELEAFOBJECTSOURCECOMPOSITE_NAME_STARTS_WITH);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		searchInputFieldLabl.setLayoutData(data);

		searchInputFieldText = new Text(searchControlComposite, SWT.SINGLE
				| SWT.BORDER);
		searchInputFieldText.setTextLimit(128);
		data = new GridData(GridData.FILL_HORIZONTAL);
		searchInputFieldText.setLayoutData(data);
		searchInputFieldText
				.addKeyListener(new RefreshSearchDataOnReturnKeyListener());

		Button findNowButton = new Button(searchControlComposite, SWT.NONE);
		findNowButton
				.setText(BrowserMessages.DEFAULTSELECTABLELEAFOBJECTSOURCECOMPOSITE_FIND_NOW);
		findNowButton.addSelectionListener(new RefreshSearchDataListener());
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		findNowButton.setLayoutData(data);
	}

	/**
     * 
     */
	protected void refreshSelectableObjectList() {
		for (ISelectableLeafObjectListModificationListener nextListener : modificationListeners) {
			nextListener.onChange();
		}
	}

	/**
	 * Listener invoked when a new search is requested
	 * 
	 * @author sgoldstein
	 */
	protected class RefreshSearchDataListener extends SelectionAdapter {

		public RefreshSearchDataListener() {
		}

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent event) {
			DefaultSelectableLeafObjectSourceImpl.this
					.refreshSelectableObjectList();
		}
	}

	protected class RefreshSearchDataOnReturnKeyListener extends KeyAdapter {

		public RefreshSearchDataOnReturnKeyListener() {
		}

		/**
		 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
		 */
		@Override
		public void keyReleased(KeyEvent event) {
			if (event.character == SWT.CR) {
				DefaultSelectableLeafObjectSourceImpl.this
						.refreshSelectableObjectList();
			}
		}
	}
}
