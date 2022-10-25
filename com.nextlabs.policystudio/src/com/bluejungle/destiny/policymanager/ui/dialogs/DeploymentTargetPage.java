/*
 * Created on Jul 18, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectSearchSpec;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/D_Plugins/com.nextlabs.policystudio/src/com
 *          /bluejungle
 *          /destiny/policymanager/ui/dialogs/DeploymentTargetPage.java#1 $
 */

public class DeploymentTargetPage extends WizardPage {

	private Combo comboType;
	private Composite left, rightbottom;
	private Text searchText;
	private Button buttonAdd, buttonRemove;
	private Button buttonFind, buttonReset;
	private Deploy deploymentType = Deploy.AUTO_DEPLOY;
	private TabFolder folder;
	private TableViewer selectedEnforcersViewer;
	private TableViewer availableFileServerViewer, availablePortalViewer, availableWinDesktopViewer, availableActiveDirectoryViewer;

	private List<LeafObject> availableFileServers, availablePortals, availableWinDesktops, availableActiveDirectory;
	private List<LeafObject> selectedEnforcers = new ArrayList<LeafObject>();

	private enum Deploy {
		AUTO_DEPLOY, MANUAL_DEPLOY
	};

	private enum Type {
		FILE_SERVER(0), PORTAL(2), WIN_DESKTOP(1), ACTIVE_DIRECTORY(3);

		private int type;

		Type(int type) {
			this.type = type;
		}

		int getType() {
			return type;
		}
	}

	public int getDeploymentType() {
		if (deploymentType == Deploy.AUTO_DEPLOY) {
			return 0;
		}
		return 1;
	}

	public List<LeafObject> getSelectedEnforcers() {
		return selectedEnforcers;
	}

	private class AvailableSorter extends ViewerSorter {

		/**
		 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Viewer viewer, Object element1, Object element2) {
			LeafObject object1 = (LeafObject) element1;
			LeafObject object2 = (LeafObject) element2;
			return object1.getName().compareToIgnoreCase(object2.getName());
		}
	}

	private class AvailableContentProvider implements IStructuredContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		@SuppressWarnings("unchecked")
		public Object[] getElements(Object parent) {
			if (parent instanceof List) {
				List<LeafObject> elements = (List<LeafObject>) parent;
				return elements.toArray();
			}
			return new Object[0];
		}
	}

	private class AvailableLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof LeafObject) {
				return ((LeafObject) element).getName();
			}
			return "";
		}

		public Image getColumnImage(Object obj, int index) {
			return ObjectLabelImageProvider.getImage(obj);
		}
	}

	private class SelectedSorter extends ViewerSorter {

		@Override
		public int category(Object element) {
			LeafObject object = (LeafObject) element;
			LeafObjectType type = object.getType();
			if (type == LeafObjectType.FILE_SERVER_AGENT) {
				return Type.FILE_SERVER.getType();
			} else if (type == LeafObjectType.PORTAL_AGENT) {
				return Type.PORTAL.getType();
			} else if (type == LeafObjectType.DESKTOP_AGENT) {
				return Type.WIN_DESKTOP.getType();
			} else if (type == LeafObjectType.ACTIVE_DIRECTORY_AGENT) {
				return Type.ACTIVE_DIRECTORY.getType();
			}
			return 4;
		}

		/**
		 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Viewer viewer, Object element1, Object element2) {
			int cat1 = category(element1);
			int cat2 = category(element2);
			if (cat1 != cat2)
				return cat1 - cat2;

			LeafObject object1 = (LeafObject) element1;
			LeafObject object2 = (LeafObject) element2;
			return object1.getName().compareToIgnoreCase(object2.getName());
		}
	}

	private class SelectedContentProvider implements IStructuredContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		@SuppressWarnings("unchecked")
		public Object[] getElements(Object parent) {
			if (parent instanceof List) {
				return ((List<LeafObject>) parent).toArray();
			}
			return new Object[0];
		}
	}

	private class SelectedLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			if (obj instanceof LeafObject) {
				LeafObject leafObject = (LeafObject) obj;
				if (index == 0) {
					return leafObject.getName();
				} else if (index == 1) {
					LeafObjectType type = leafObject.getType();
					if (type == LeafObjectType.FILE_SERVER_AGENT) {
						return DialogMessages.SETDEPLOYMENTTARGETDIALOG_FILE_SERVER;
					} else if (type == LeafObjectType.PORTAL_AGENT) {
						return DialogMessages.SETDEPLOYMENTTARGETDIALOG_PORTAL;
					} else if (type == LeafObjectType.DESKTOP_AGENT) {
						return DialogMessages.SETDEPLOYMENTTARGETDIALOG_WIN_DESKTOP;
					} else if (type == LeafObjectType.ACTIVE_DIRECTORY_AGENT) {
						return DialogMessages.SETDEPLOYMENTTARGETDIALOG_ACTIVE_DIRECTORY;
					}
					return type.getName();
				}
			}
			return null;
		}

		public Image getColumnImage(Object obj, int index) {
			if (index == 0)
				return ObjectLabelImageProvider.getImage(obj);
			else
				return null;
		}
	}

	public DeploymentTargetPage() {
		super("Target", "Target", ImageDescriptor.createFromImage(ImageBundle.TITLE_IMAGE));
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);

		left = new Composite(composite, SWT.NONE);
		data = new GridData(GridData.FILL_BOTH);
		left.setLayoutData(data);
		layout = new GridLayout();
		left.setLayout(layout);

		Group group = new Group(left, SWT.NONE);
		group.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_AVAILABLE_ENFORCERS);
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 300;
		data.heightHint = 300;
		group.setLayoutData(data);
		layout = new GridLayout(3, false);
		group.setLayout(layout);

		Label label = new Label(group, SWT.NONE);
		label.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_NAME_STARTS_WITH);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		label.setLayoutData(data);

		searchText = new Text(group, SWT.BORDER);
		searchText.setTextLimit(128);
		data = new GridData(GridData.FILL_HORIZONTAL);
		searchText.setLayoutData(data);
		searchText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				if (searchText.getText().length() > 0) {
					buttonReset.setEnabled(true);
				} else {
					buttonReset.setEnabled(false);
				}
			}
		});
		searchText.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == '\r')
					loadAvailableEnforcers();
			}
		});

		buttonFind = new Button(group, SWT.PUSH);
		buttonFind.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_FIND);
		data = new GridData();
		buttonFind.setLayoutData(data);
		buttonFind.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				loadAvailableEnforcers();
			}
		});

		buttonReset = new Button(group, SWT.PUSH);
		buttonReset.setEnabled(false);
		buttonReset.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_RESET);
		data = new GridData();
		buttonReset.setLayoutData(data);
		buttonReset.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				searchText.setText("");
			}
		});

		folder = new TabFolder(group, SWT.NONE);
		layout = new GridLayout();
		folder.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 3;
		data.heightHint = 400;
		folder.setLayoutData(data);

		TabItem fileServerTab = new TabItem(folder, SWT.NONE);
		fileServerTab.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_FILE_SERVER);
		availableFileServerViewer = createTableViewer(folder);
		fileServerTab.setControl(availableFileServerViewer.getControl());

		TabItem desktopTab = new TabItem(folder, SWT.NONE);
		desktopTab.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_WIN_DESKTOP);
		availableWinDesktopViewer = createTableViewer(folder);
		desktopTab.setControl(availableWinDesktopViewer.getControl());

		TabItem portalTab = new TabItem(folder, SWT.NONE);
		portalTab.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_PORTAL);
		availablePortalViewer = createTableViewer(folder);
		portalTab.setControl(availablePortalViewer.getControl());
		
		TabItem ActiveDirectoryTab = new TabItem(folder, SWT.NONE);
		ActiveDirectoryTab.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_ACTIVE_DIRECTORY);
		availableActiveDirectoryViewer = createTableViewer(folder);
		ActiveDirectoryTab.setControl(availableActiveDirectoryViewer.getControl());

		folder.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				loadAvailableEnforcers();
			}
		});

		Composite right = new Composite(composite, SWT.NONE);
		data = new GridData(GridData.FILL_BOTH);
		right.setLayoutData(data);
		layout = new GridLayout(2, false);
		right.setLayout(layout);

		label = new Label(right, SWT.NONE);
		label.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_DEPLOYMENT_TYPE);

		comboType = new Combo(right, SWT.BORDER | SWT.READ_ONLY);
		comboType.add(DialogMessages.SETDEPLOYMENTTARGETDIALOG_AUTO_DEPLOYMENT);
		comboType.add(DialogMessages.SETDEPLOYMENTTARGETDIALOG_MANUAL_DEPLOYMENT);
		data = new GridData(GridData.FILL_HORIZONTAL);
		comboType.setLayoutData(data);
		if (deploymentType == Deploy.AUTO_DEPLOY) {
			comboType.select(0);
		} else {
			comboType.select(1);
		}

		comboType.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = comboType.getSelectionIndex();
				if (index == 0) {
					deploymentType = Deploy.AUTO_DEPLOY;
					changeWindowSize();
				} else {
					deploymentType = Deploy.MANUAL_DEPLOY;
					changeWindowSize();
					loadAvailableEnforcers();
				}
			}
		});

		rightbottom = new Composite(right, SWT.NONE);
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 300;
		data.heightHint = 300;
		data.horizontalSpan = 2;
		rightbottom.setLayoutData(data);
		layout = new GridLayout(2, false);
		rightbottom.setLayout(layout);

		Composite buttonbar = new Composite(rightbottom, SWT.NONE);
		data = new GridData(GridData.FILL_VERTICAL);
		buttonbar.setLayoutData(data);
		layout = new GridLayout();
		buttonbar.setLayout(layout);

		buttonAdd = new Button(buttonbar, SWT.PUSH);
		buttonAdd.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_ADD);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		buttonAdd.setLayoutData(data);
		buttonAdd.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				addSelections();
			}
		});

		buttonRemove = new Button(buttonbar, SWT.PUSH);
		buttonRemove.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_REMOVE);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		buttonRemove.setLayoutData(data);
		buttonRemove.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelections();
			}
		});

		group = new Group(rightbottom, SWT.NONE);
		group.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_SELECTED_ENFORCERS);
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 300;
		group.setLayoutData(data);
		layout = new GridLayout();
		group.setLayout(layout);

		selectedEnforcersViewer = new TableViewer(group, SWT.BORDER | SWT.MULTI	| SWT.V_SCROLL | SWT.FULL_SELECTION);
		selectedEnforcersViewer.setContentProvider(new SelectedContentProvider());
		selectedEnforcersViewer.setLabelProvider(new SelectedLabelProvider());
		selectedEnforcersViewer.setSorter(new SelectedSorter());
		Table table = selectedEnforcersViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableColumn column = new TableColumn(table, SWT.CENTER);
		column.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_HOST_NAME);
		column.setWidth(100);
		column = new TableColumn(table, SWT.CENTER);
		column.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_TYPE);
		column.setWidth(100);
		data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		selectedEnforcersViewer.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						updateRemoveButtonStatus();
					}
				});
		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				removeSelections();
			}
		});
		table.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.DEL)
					removeSelections();
			}
		});

		selectedEnforcersViewer.setInput(selectedEnforcers);

		if (deploymentType == Deploy.MANUAL_DEPLOY) {
			loadAvailableEnforcers();
		}

		setControl(composite);

		changeWindowSize();
	}

	private void changeWindowSize() {
		GridData data;
		if (deploymentType == Deploy.AUTO_DEPLOY) {
			data = new GridData();
			data.heightHint = 0;
			data.widthHint = 0;
			left.setLayoutData(data);

			data = new GridData();
			data.heightHint = 0;
			data.widthHint = 0;
			data.horizontalSpan = 2;
			rightbottom.setLayoutData(data);
		} else {
			data = new GridData(GridData.FILL_BOTH);
			left.setLayoutData(data);

			data = new GridData(GridData.FILL_BOTH);
			data.horizontalSpan = 2;
			rightbottom.setLayoutData(data);
			searchText.setFocus();
		}
		getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@SuppressWarnings("unchecked")
	private void removeSelections() {
		IStructuredSelection selection = (IStructuredSelection) selectedEnforcersViewer.getSelection();
		if (selection.isEmpty()) {
			return;
		}

		Iterator<LeafObject> iterator = selection.iterator();
		while (iterator.hasNext()) {
			LeafObject object = (LeafObject) iterator.next();
			selectedEnforcers.remove(object);
		}
		selectedEnforcersViewer.refresh();
		loadAvailableEnforcers();
	}

	private TableViewer createTableViewer(TabFolder parent) {
		TableViewer viewer = new TableViewer(parent, SWT.BORDER | SWT.MULTI	| SWT.V_SCROLL | SWT.FULL_SELECTION);
		Table table = viewer.getTable();
		TableColumn c1 = new TableColumn(table, SWT.CENTER);
		c1.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_HOST_NAME);
		c1.setWidth(400);
		table.getHorizontalBar().setVisible(false);

		viewer.setContentProvider(new AvailableContentProvider());
		viewer.setLabelProvider(new AvailableLabelProvider());
		viewer.setSorter(new AvailableSorter());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				updateAddButtonStatus();
			}
		});
		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				addSelections();
			}
		});

		return viewer;
	}

	@SuppressWarnings("unchecked")
	private void addSelections() {
		IStructuredSelection selection = getCurrentTableViewerSelections();
		if (selection.isEmpty()) {
			return;
		}
		List<LeafObject> currentList = getCurrentAvailableList();
		Iterator<LeafObject> iterator = selection.iterator();
		while (iterator.hasNext()) {
			LeafObject object = (LeafObject) iterator.next();
			selectedEnforcers.add(object);
			currentList.remove(object);
		}

		selectedEnforcersViewer.refresh();
		getCurrentTableViewer().refresh();
	}

	private List<LeafObject> getCurrentAvailableList() {
		int index = folder.getSelectionIndex();
		if (index == Type.FILE_SERVER.getType()) {
			return availableFileServers;
		} else if (index == Type.PORTAL.getType()) {
			return availablePortals;
		} else if (index == Type.WIN_DESKTOP.getType()) {
			return availableWinDesktops;
		}else if (index == Type.ACTIVE_DIRECTORY.getType()) {
			return availableActiveDirectory;
		}
		return null;
	}

	private TableViewer getCurrentTableViewer() {
		int index = folder.getSelectionIndex();
		TableViewer tableViewer = null;
		if (index == Type.FILE_SERVER.getType()) {
			tableViewer = availableFileServerViewer;
		} else if (index == Type.PORTAL.getType()) {
			tableViewer = availablePortalViewer;
		} else if (index == Type.WIN_DESKTOP.getType()) {
			tableViewer = availableWinDesktopViewer;
		} else if (index == Type.ACTIVE_DIRECTORY.getType()) {
			tableViewer = availableActiveDirectoryViewer;
		}
		return tableViewer;
	}

	private void filterSelected(List<LeafObject> result) {
		result.removeAll(selectedEnforcers);
	}

	public List<LeafObject> getSelectableLeafObjects( LeafObjectType leafObjectType, int maxResults) {
		if (leafObjectType == null) {
			throw new NullPointerException("leafObjectType cannot be null.");
		}

		List<LeafObject> itemsToReturn = null;

		String searchString = searchText.getText();

		IPredicate pred = SubjectAttribute.HOST_NAME.buildRelation(	RelationOp.EQUALS, Constant.build(searchString + "*"));
		LeafObjectSearchSpec leafObjectSearchSpec = new LeafObjectSearchSpec(leafObjectType, pred, maxResults);

		try {
			itemsToReturn = EntityInfoProvider.runLeafObjectQuery(leafObjectSearchSpec);
		} catch (PolicyEditorException exception) {
			LoggingUtil.logError(Activator.ID,"Failed to retrieve leaf objects", exception);
		}

		return itemsToReturn;
	}

	private void updateAddButtonStatus() {
		buttonAdd.setEnabled(false);

		IStructuredSelection selection = getCurrentTableViewerSelections();
		if (!selection.isEmpty()) {
			buttonAdd.setEnabled(true);
		}
	}

	private IStructuredSelection getCurrentTableViewerSelections() {
		TableViewer tableViewer = getCurrentTableViewer();
		return (IStructuredSelection) tableViewer.getSelection();
	}

	private void loadAvailableEnforcers() {
		int index = folder.getSelectionIndex();
		if (index == Type.FILE_SERVER.getType()) {
			availableFileServers = getSelectableLeafObjects(LeafObjectType.FILE_SERVER_AGENT, 2000);
			filterSelected(availableFileServers);
			availableFileServerViewer.setInput(availableFileServers);
		} else if (index == Type.PORTAL.getType()) {
			availablePortals = getSelectableLeafObjects(LeafObjectType.PORTAL_AGENT, 2000);
			filterSelected(availablePortals);
			availablePortalViewer.setInput(availablePortals);
		} else if (index == Type.WIN_DESKTOP.getType()) {
			availableWinDesktops = getSelectableLeafObjects(LeafObjectType.DESKTOP_AGENT, 2000);
			filterSelected(availableWinDesktops);
			availableWinDesktopViewer.setInput(availableWinDesktops);
		} else if (index == Type.ACTIVE_DIRECTORY.getType()) {
			availableActiveDirectory = getSelectableLeafObjects(LeafObjectType.ACTIVE_DIRECTORY_AGENT, 2000);
			filterSelected(availableActiveDirectory);
			availableActiveDirectoryViewer.setInput(availableActiveDirectory);
		}
		updateAddButtonStatus();
		updateRemoveButtonStatus();
	}

	private void updateRemoveButtonStatus() {
		ISelection selection = selectedEnforcersViewer.getSelection();
		buttonRemove.setEnabled(false);
		if (!selection.isEmpty()) {
			buttonRemove.setEnabled(true);
		}
	}
}
