/*
 * Created on Apr 5, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.BrowserMessages;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.ILeafObjectBrowser;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;

/**
 * 
 * The default implementation of the leaf object browser. This browser will work
 * with multiple leaf object types. Though, there are some subclasses which add
 * specific behavior for particular leaf object types
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/browser/impl/
 *          DefaultLeafObjectBrowser.java#3 $
 * 
 */

public class DefaultLeafObjectBrowser extends TitleAreaDialog implements
		ILeafObjectBrowser {

	private SelectableLeafObjectTabbedTableComposite selectableLeafObjectTabbedTable;

	/**
	 * 
	 * Using ArrayList implementation for performance reasons. See
	 * ArrayListToArrayContentProvider
	 * 
	 */
	private List<LeafObject> itemsToReturn = new ArrayList<LeafObject>();

	private TableViewer membersToAddList;
	private List<LeafObjectType> leafObjectTypes;
	private String windowTitle;
	private Button addButton, removeButton;

	/**
	 * 
	 * Create an instance of DefaultLeafObjectBrowser
	 * 
	 * @param parent
	 * @param windowTitle
	 * @param leafObjectTypes
	 */
	public DefaultLeafObjectBrowser(Shell parent, String windowTitle,
			List<LeafObjectType> leafObjectTypes) {
		super(parent);
		if (leafObjectTypes == null) {
			throw new NullPointerException("leafObjectTypes cannot be null.");
		}
		if (windowTitle == null) {
			throw new NullPointerException("windowTitle cannot be null.");
		}

		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.leafObjectTypes = leafObjectTypes;
		this.windowTitle = windowTitle;
	}

	/**
	 * 
	 * Retrieve the leafObjectTypes.
	 * 
	 * @return the leafObjectTypes.
	 * 
	 */

	public final List<LeafObjectType> getLeafObjectTypes() {
		return leafObjectTypes;
	}

	public final List<LeafObject> getItemsToReturn() {
		List<LeafObject> result = new ArrayList<LeafObject>();
		for (LeafObject object : itemsToReturn) {
			boolean found = false;
			for (LeafObject item : result) {
				if (item.getId().equals(object.getId())) {
					found = true;
					break;
				}
			}

			if (!found) {
				result.add(object);
			}
		}
		return result;
	}

	@Override
	final protected Control createDialogArea(Composite parent) {
		Composite root = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(root, SWT.NONE);

		init(container);

		return parent;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(getWindowTitle());
		newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
	}

	@Override
	public void create() {
		super.create();
		setTitle(getWindowTitle());
		setTitleImage(ImageBundle.TITLE_IMAGE);
	}

	protected List<ISelectableLeafObjectTableModel> getSelectableLeafObjectTableModels() {
		List<ISelectableLeafObjectTableModel> selectableLeafObjectTableModels = new ArrayList<ISelectableLeafObjectTableModel>(
				leafObjectTypes.size());
		Iterator<LeafObjectType> leafObjectTypesIterator = leafObjectTypes
				.iterator();
		while (leafObjectTypesIterator.hasNext()) {
			LeafObjectType nextLeafObjectType = leafObjectTypesIterator.next();
			ISelectableLeafObjectTableModel nextSelectableLeafObjectTableModel = new DefaultSelectableLeafObjectTableModel(
					nextLeafObjectType);
			selectableLeafObjectTableModels
					.add(nextSelectableLeafObjectTableModel);
		}

		return selectableLeafObjectTableModels;
	}

	protected ISelectableLeafObjectsSource createSelectableLeafObjectSource(
			Composite parent) {
		return new DefaultSelectableLeafObjectSourceImpl(parent);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				false);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Layout and initialize this dialog
	 * 
	 */
	private void init(Composite root) {
		GridLayout layout = new GridLayout();
		root.setLayout(layout);

		GridData data = new GridData(GridData.FILL_BOTH);
		root.setLayoutData(data);

		ISelectableLeafObjectsSource selectableLeafObjectSource = createSelectableLeafObjectSource(root);

		Group selectMembersGroup = new Group(root, SWT.NONE);
		selectMembersGroup
				.setText(BrowserMessages.DEFAULTLEAFOBJECTBROWSER_MEMBERS);
		layout = new GridLayout(3, false);
		selectMembersGroup.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		selectMembersGroup.setLayoutData(data);

		selectableLeafObjectTabbedTable = createSelectableLeafObjectTable(
				selectMembersGroup, selectableLeafObjectSource);
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 400;
		selectableLeafObjectTabbedTable.setLayoutData(data);

		Composite buttonComposite = new Composite(selectMembersGroup, SWT.NONE);
		data = new GridData(GridData.FILL_VERTICAL);
		buttonComposite.setLayoutData(data);
		layout = new GridLayout();
		buttonComposite.setLayout(layout);

		new Label(buttonComposite, SWT.NONE);
		new Label(buttonComposite, SWT.NONE);

		addButton = new Button(buttonComposite, SWT.NONE);
		addButton.setText(BrowserMessages.LABEL_ADD);
		addButton.addSelectionListener(new ShuttleMembersSelectionListener());
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		addButton.setLayoutData(data);

		removeButton = new Button(buttonComposite, SWT.NONE);
		removeButton.setText(BrowserMessages.LABEL_REMOVE);
		removeButton
				.addSelectionListener(new ReturnShuttleMembersSelectionListener());
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		removeButton.setLayoutData(data);

		membersToAddList = createAddMembersTable(selectMembersGroup);
		membersToAddList.setInput(itemsToReturn);
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 200;
		membersToAddList.getTable().setLayoutData(data);
	}

	/**
	 * 
	 * Retrieve the windowTitle.
	 * 
	 * @return the windowTitle.
	 * 
	 */

	private String getWindowTitle() {
		return windowTitle;
	}

	/**
	 * Build the add members table.
	 * 
	 * @param parent
	 *            the parent component of the components in the add members
	 *            group
	 */
	private TableViewer createAddMembersTable(Group parent) {
		TableViewer tableViewerToCreate = new TableViewer(parent, SWT.MULTI
				| SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.BORDER
				| SWT.V_SCROLL);
		tableViewerToCreate
				.setContentProvider(new ArrayListToArrayContentProvider());
		tableViewerToCreate.setLabelProvider(new LeafObjectNameLabelProvider());
		Table nestedTable = tableViewerToCreate.getTable();
		nestedTable.addKeyListener(new ReturnShuttleMembersKeyListener());

		return tableViewerToCreate;
	}

	/**
	 * 
	 * Remove the selected items from the members list and return them to the
	 * available leaf objects list
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void returnSelectedMembers() {
		IStructuredSelection itemsToRemove = (IStructuredSelection) membersToAddList
				.getSelection();
		List<LeafObject> itemsToRemoveAsList = itemsToRemove.toList();
		itemsToReturn.removeAll(itemsToRemoveAsList);
		if (itemsToReturn.isEmpty()) {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		membersToAddList.refresh();

		selectableLeafObjectTabbedTable
				.reAddSelectedAvailableLeafObjects(itemsToRemoveAsList);
	}

	@SuppressWarnings("unchecked")
	private void shuttleSelectedMembers() {
		List<LeafObject> selectedItems = selectableLeafObjectTabbedTable
				.getSelectedAvailableLeafObjects();
		Preferences preferences = Activator.getDefault().getPluginPreferences();
		int items = preferences.getInt("MAX_ITEMS");
		if (items == 0) {
			items = 50;
		}
		if (itemsToReturn.size() + selectedItems.size() > items) {
			setMessage(
					NLS
							.bind(
									BrowserMessages.DEFAULTLEAFOBJECTBROWSER_ERROR_TOO_MANY_ITEMS,
									items), IMessageProvider.ERROR);
			return;
		}
		itemsToReturn.addAll(selectedItems);
		selectableLeafObjectTabbedTable.removeSelectedAvailableLeafObjects();
		membersToAddList.refresh(false);
		if (itemsToReturn.size() > 0) {
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		} else {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}

	private void shuttleMember(LeafObject memberToShuttle) {
		if (memberToShuttle == null) {
			throw new NullPointerException("memberToShuttle cannot be null.");
		}
		itemsToReturn.add(memberToShuttle);
		selectableLeafObjectTabbedTable.removeLeafObject(memberToShuttle);
		membersToAddList.refresh(false);
		if (itemsToReturn.size() > 0) {
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		} else {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}

	/**
	 * @param parent
	 * @param selectableLeafObjectSource
	 */
	private SelectableLeafObjectTabbedTableComposite createSelectableLeafObjectTable(
			Group parent,
			ISelectableLeafObjectsSource selectableLeafObjectSource) {
		List<ISelectableLeafObjectTableModel> selectableLeafObjectTableModels = getSelectableLeafObjectTableModels();

		SelectableLeafObjectTabbedTableComposite compositeToCreate = new SelectableLeafObjectTabbedTableComposite(
				parent, selectableLeafObjectSource,
				selectableLeafObjectTableModels);
		compositeToCreate
				.addLeafObjectSelectionListener(new ILeafObjectSelectionListener() {

					/**
					 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ILeafObjectSelectionListener#onLeafObjectSelection(com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.LeafObjectSelectionEvent)
					 */
					public void onLeafObjectSelection(
							LeafObjectSelectionEvent event) {
						DefaultLeafObjectBrowser.this.shuttleMember(event
								.getLeafObjectSelected());
					}

				});

		return compositeToCreate;
	}

	/**
	 * Listener responsible for shuttling members from the available list to the
	 * members to add list
	 * 
	 * @author sgoldstein
	 */
	private class ShuttleMembersSelectionListener extends SelectionAdapter {

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent event) {
			setMessage("", IMessageProvider.NONE);
			DefaultLeafObjectBrowser.this.shuttleSelectedMembers();
		}
	}

	/**
	 * Listener responsible for moving items from the members to add list back
	 * to the available list
	 * 
	 * @author sgoldstein
	 */
	private class ReturnShuttleMembersKeyListener extends KeyAdapter {

		/**
		 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
		 */
		@Override
		public void keyPressed(KeyEvent event) {
			if (event.character == SWT.DEL) {
				DefaultLeafObjectBrowser.this.returnSelectedMembers();
			}
		}
	}

	/**
	 * Listener responsible for removing members from the members to add list to
	 * the available list
	 * 
	 * @author sgoldstein
	 */
	private class ReturnShuttleMembersSelectionListener extends
			SelectionAdapter {

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent event) {
			setMessage("", IMessageProvider.NONE);
			DefaultLeafObjectBrowser.this.returnSelectedMembers();
		}
	}

	/**
	 * A generic content provider which takes the array list passed to it and
	 * converts it into an Object[]
	 * 
	 * @author sgoldstein
	 */
	private class ArrayListToArrayContentProvider implements
			IStructuredContentProvider {

		/**
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			return ((ArrayList) inputElement).toArray();
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
}
