/*
 * Created on Jan 22, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.SharePointImageConstants;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.BrowserMessages;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.ILeafObjectBrowser;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceConnectionInfo;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceType;
import com.bluejungle.destiny.services.policy.types.PortalResourceTypes;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNode;
import com.bluejungle.pf.destiny.services.PolicyEditorException;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/browser/impl/
 *          PortalLeafObjectBrowser.java#1 $
 */

public class PortalLeafObjectBrowser extends Dialog implements
		ILeafObjectBrowser {

	private static String PORTALS = "PORTALS";
	private static String[] COLUMNS = new String[] {
			BrowserMessages.PORTALLEAFOBJECTBROWSER_SELECTION,
			BrowserMessages.PORTALLEAFOBJECTBROWSER_INCLUDE };
	private static Point SIZE = new Point(800, 600);
	private static Point SMALLSIZE = new Point(400, 300);

	private TreeViewer treeViewer;
	private TableViewer tableViewer;
	private Text textSearch;
	private String[] URLs;
	private Combo comboServerURL;
	private Label labelConnectedServer;
	private Text textUser, textPassword, textDomain;
	private Button buttonDisplay, buttonAddContainer, buttonRemoveContainer;
	private Set<SelectionModel> selectionSet;
	private int connection;
	private ResourceTreeNode rootNode;
	private String oldDomain = "", oldUser = "", oldPassword = "",
			oldServer = "";
	private Composite left;

	private Map<String, PortalModel> userDefinedPortals;

	private class PortalModel {

		private String URL;
		private String userName;
		private String domain;

		public String getDomain() {
			return this.domain;
		}

		public void setDomain(String domain) {
			this.domain = domain;
		}

		public String getURL() {
			return this.URL;
		}

		public void setURL(String url) {
			URL = url;
		}

		public String getUserName() {
			return this.userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}
	}

	private class SelectionModel {

		private String id;
		private String url;
		private boolean includeChildren;

		public SelectionModel(String id, String url, boolean includeChildren) {
			this.id = id;
			this.url = url;
			this.includeChildren = includeChildren;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public void setIncludeChildren(boolean includeChildren) {
			this.includeChildren = includeChildren;
		}

		public boolean isIncludeChildren() {
			return includeChildren;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SelectionModel) {
				SelectionModel model = (SelectionModel) obj;
				return getId().equalsIgnoreCase(model.getId());
			}
			return super.equals(obj);
		}

		@Override
		public int hashCode() {
			return url.hashCode();
		}

		public String getId() {
			return this.id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}

	private class SelectionCellModifier implements ICellModifier {

		private TableViewer tableViewer;

		public SelectionCellModifier(TableViewer tableViewer) {
			this.tableViewer = tableViewer;
		}

		public boolean canModify(Object element, String property) {
			if (property.equalsIgnoreCase(COLUMNS[0]))
				return false;
			else
				return true;
		}

		public Object getValue(Object element, String property) {
			SelectionModel model = (SelectionModel) element;
			if (property.equalsIgnoreCase(COLUMNS[0]))
				return model.getUrl();
			else if (property.equalsIgnoreCase(COLUMNS[1]))
				return model.isIncludeChildren();
			return null;
		}

		public void modify(Object element, String property, Object value) {
			if (element instanceof Item) {
				element = ((Item) element).getData();
			}
			SelectionModel model = (SelectionModel) element;
			if (property.equalsIgnoreCase(COLUMNS[0]))
				model.setUrl((String) value);
			else if (property.equalsIgnoreCase(COLUMNS[1]))
				model.setIncludeChildren(((Boolean) value).booleanValue());

			tableViewer.refresh();
			updateTreeStatusAll();
		}
	}

	private class SelectionContentProvider implements
			IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return getSelectionSet().toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class SelectionLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			SelectionModel model = (SelectionModel) element;
			Image result = null;
			switch (columnIndex) {
			case 0:
				break;
			case 1:
				if (model.isIncludeChildren())
					result = ImageBundle.CHECKED_IMAGE;
				else
					result = ImageBundle.UNCHECKED_IMAGE;
				break;
			}
			return result;
		}

		public String getColumnText(Object element, int columnIndex) {
			SelectionModel model = (SelectionModel) element;
			String result = "";
			switch (columnIndex) {
			case 0:
				result = model.getUrl();
				break;
			default:
				break;
			}
			return result;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

	}

	private class PortalTreeContentProvder implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			ResourceTreeNode node = (ResourceTreeNode) parentElement;
			try {
				node = PolicyServerProxy.client.getResourceTreeNodeChildren(
						connection, node);
			} catch (PolicyEditorException exception) {
				LoggingUtil.logError(Activator.ID,
						"error get resource tree node children", exception);
				return new ResourceTreeNode[0];
			}

			ResourceTreeNode[] children = node.getChildren();
			Arrays.sort(children, new Comparator<ResourceTreeNode>() {

				public int compare(ResourceTreeNode node1,
						ResourceTreeNode node2) {
					int cat1 = category(node1);
					int cat2 = category(node2);

					if (cat1 != cat2)
						return cat1 - cat2;

					return node1.getName().compareToIgnoreCase(node2.getName());
				}
			});
			return children;
		}

		public int category(ResourceTreeNode node) {
			String type = node.getType();

			if (type.equals(PortalResourceTypes.SITE.getValue()))
				return 0;
			else if (type.equals(PortalResourceTypes.DOCUMENT.getValue()))
				return 1;
			else if (type.equals(PortalResourceTypes.FOLDER.getValue()))
				return 2;
			else if (type.equals(PortalResourceTypes.LIST.getValue()))
				return 3;
			else if (type.equals(PortalResourceTypes.LIST_ITEMS.getValue()))
				return 4;
			else if (type.equals(PortalResourceTypes.PAGE.getValue()))
				return 5;
			else if (type.equals(PortalResourceTypes.PORTAL.getValue()))
				return 6;
			else if (type.equals(PortalResourceTypes.WEB_PART.getValue()))
				return 7;
			return 8;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			ResourceTreeNode node = (ResourceTreeNode) element;
			if (node != null)
				return node.isHasChildren();
			else
				return false;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class PortalViewerFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			ResourceTreeNode node = (ResourceTreeNode) element;
			String text = textSearch.getText();
			String name = node.getName();

			if (name.startsWith(text))
				return true;
			return false;
		}
	}

	private class PortalTreeLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			ResourceTreeNode node = (ResourceTreeNode) element;
			if (node != null)
				return node.getName();
			else
				return "";
		}

		@Override
		public Image getImage(Object element) {
			ResourceTreeNode node = (ResourceTreeNode) element;
			if (node.getType().equals(PortalResourceTypes.DOCUMENT.getValue()))
				return SharePointImageConstants.SHAREPOINT_DOCUMENTS;
			else if (node.getType().equals(
					PortalResourceTypes.FOLDER.getValue()))
				return SharePointImageConstants.SHAREPOINT_FOLDER;
			else if (node.getType().equals(PortalResourceTypes.LIST.getValue()))
				return SharePointImageConstants.SHAREPOINT_LIST;
			else if (node.getType().equals(
					PortalResourceTypes.LIST_ITEMS.getValue()))
				return SharePointImageConstants.SHAREPOINT_LIST;
			else if (node.getType().equals(PortalResourceTypes.PAGE.getValue()))
				return SharePointImageConstants.SHAREPOINT_PAGE;
			else if (node.getType().equals(
					PortalResourceTypes.PORTAL.getValue()))
				return SharePointImageConstants.SHAREPOINT_PAGES;
			else if (node.getType().equals(PortalResourceTypes.SITE.getValue()))
				return SharePointImageConstants.SHAREPOINT_SITES;
			else if (node.getType().equals(
					PortalResourceTypes.WEB_PART.getValue()))
				return SharePointImageConstants.SHAREPOINT_SP_SITES;
			return SharePointImageConstants.SHAREPOINT;
		}
	}

	private String windowTitle;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param windowTitle
	 * @param leafObjectTypes
	 */
	public PortalLeafObjectBrowser(Shell parent, String windowTitle) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.windowTitle = windowTitle;

		loadUserDefinedModel();
	}

	/**
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(windowTitle);
		newShell.setSize(SMALLSIZE);
		newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite root = (Composite) super.createDialogArea(parent);

		Composite bottom = new Composite(root, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		bottom.setLayoutData(data);
		GridLayout layout = new GridLayout(2, false);
		bottom.setLayout(layout);

		left = new Composite(bottom, SWT.NONE);
		data = new GridData();
		data.widthHint = 0;
		data.heightHint = 0;
		left.setLayoutData(data);
		layout = new GridLayout(2, false);
		left.setLayout(layout);

		Composite right = new Composite(bottom, SWT.NONE);
		data = new GridData(GridData.FILL_BOTH);
		right.setLayoutData(data);
		layout = new GridLayout(2, false);
		right.setLayout(layout);

		Label labelSearch = new Label(left, SWT.NONE);
		labelSearch.setVisible(false);
		labelSearch.setText(BrowserMessages.PORTALLEAFOBJECTBROWSER_START_WITH);
		data = new GridData();
		data.heightHint = 0;
		labelSearch.setLayoutData(data);

		textSearch = new Text(left, SWT.BORDER);
		textSearch.setVisible(false);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 0;
		textSearch.setLayoutData(data);
		textSearch.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				treeViewer.refresh();
			}
		});

		labelConnectedServer = new Label(left, SWT.NONE);
		labelConnectedServer.setFont(FontBundle.ARIAL_9_BOLD);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		labelConnectedServer.setLayoutData(data);

		treeViewer = new TreeViewer(left, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		treeViewer.getTree().setLayoutData(data);
		treeViewer.setContentProvider(new PortalTreeContentProvder());
		treeViewer.setLabelProvider(new PortalTreeLabelProvider());
		treeViewer.addFilter(new PortalViewerFilter());
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) treeViewer
						.getSelection();
				if (selection.isEmpty()) {
					buttonAddContainer.setEnabled(false);
					return;
				}
				ResourceTreeNode object = (ResourceTreeNode) selection
						.getFirstElement();
				String id = object.getId();

				buttonAddContainer.setEnabled(true);
				for (SelectionModel model : getSelectionSet()) {
					if (model.getId().equals(id)) {
						buttonAddContainer.setEnabled(false);
						return;
					}
					if (model.includeChildren) {
						{
							TreeItem parent = (treeViewer.getTree()
									.getSelection()[0]).getParentItem();
							while (parent != null) {
								ResourceTreeNode parentnode = (ResourceTreeNode) parent
										.getData();
								String parentid = parentnode.getId();
								if (model.getId().equals(parentid)) {
									buttonAddContainer.setEnabled(false);
									return;
								}
								parent = parent.getParentItem();
							}
						}
					}
				}
			}
		});

		treeViewer.getTree().addTreeListener(new TreeAdapter() {

			@Override
			public void treeExpanded(TreeEvent e) {
				super.treeExpanded(e);
				updateTreeStatusAll();
			}
		});

		Group groupLogin = new Group(right, SWT.NONE);
		groupLogin
				.setText(BrowserMessages.PORTALLEAFOBJECTBROWSER_SHAREPOINT_SERVER);
		layout = new GridLayout(3, false);
		groupLogin.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		groupLogin.setLayoutData(data);

		Label labelServer = new Label(groupLogin, SWT.NONE);
		labelServer
				.setText(BrowserMessages.PORTALLEAFOBJECTBROWSER_SITE_COLLECTION_URL);

		comboServerURL = new Combo(groupLogin, SWT.BORDER | SWT.DROP_DOWN);
		data = new GridData(GridData.FILL_HORIZONTAL);
		comboServerURL.setLayoutData(data);
		try {
			List<String> list = PolicyServerProxy.client.getPortalURLList();
			for (String key : userDefinedPortals.keySet()) {
				PortalModel model = userDefinedPortals.get(key);
				String url = model.getURL();
				if (!list.contains(url)) {
					list.add(url);
				}
			}
			Collections.sort(list);
			URLs = list.toArray(new String[list.size()]);
			comboServerURL.setItems(URLs);
		} catch (PolicyEditorException e) {
			LoggingUtil.logError(Activator.ID, "error get portal url list", e);
		}
		comboServerURL.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				String text = comboServerURL.getText();
				int count = comboServerURL.getItemCount();
				if (count > 0)
					comboServerURL.remove(0, count - 1);
				for (String server : URLs) {
					if (server.indexOf(text) == 0) {
						comboServerURL.add(server);
					}
				}
			}
		});

		comboServerURL.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateDisplayButtonStatus();
			}
		});

		comboServerURL.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = comboServerURL.getText();
				if (userDefinedPortals.keySet().contains(text)) {
					PortalModel model = userDefinedPortals.get(text);
					textUser.setText(model.getUserName());
					textDomain.setText(model.getDomain());
				}
			}
		});

		buttonDisplay = new Button(groupLogin, SWT.PUSH);
		buttonDisplay.setText(BrowserMessages.PORTALLEAFOBJECTBROWSER_DISPLAY);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		buttonDisplay.setLayoutData(data);
		buttonDisplay.addSelectionListener(new SelectionAdapter() {
			/**
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				String url = comboServerURL.getText();
				String user = textUser.getText();
				String password = textPassword.getText();
				String domain = textDomain.getText();

				ExternalDataSourceConnectionInfo info = new ExternalDataSourceConnectionInfo(
						url, user, password, domain,
						ExternalDataSourceType.SHAREPOINT);
				connection = 0;
				try {
					connection = PolicyServerProxy.client
							.createExternalDataSource(info);
				} catch (PolicyEditorException exception) {
					LoggingUtil.logError(Activator.ID,
							"error create external data source", exception);
					MessageDialog.openError(getShell(),
							BrowserMessages.PORTALLEAFOBJECTBROWSER_ERROR,
							BrowserMessages.PORTALLEAFOBJECTBROWSER_ERROR_MSG);
					loadOldStatus();
					return;
				}

				if (connection == 0) {
					MessageDialog.openError(getShell(),
							BrowserMessages.PORTALLEAFOBJECTBROWSER_ERROR,
							BrowserMessages.PORTALLEAFOBJECTBROWSER_ERROR_MSG);
					loadOldStatus();
					return;
				}

				rootNode = new ResourceTreeNode();
				ResourceTreeNode node = (ResourceTreeNode) rootNode;
				try {
					node = PolicyServerProxy.client
							.getResourceTreeNodeChildren(connection, node);
				} catch (PolicyEditorException exception) {
					LoggingUtil.logError(Activator.ID,
							"error get resource tree node children", exception);
					MessageDialog.openError(getShell(),
							BrowserMessages.PORTALLEAFOBJECTBROWSER_ERROR,
							BrowserMessages.PORTALLEAFOBJECTBROWSER_ERROR_MSG);
					loadOldStatus();
					return;
				}

				enlargeSize();

				saveUserDefinedModel();

				// use the information to login to portal server
				treeViewer.setInput(rootNode);

				buttonDisplay.setEnabled(false);
				updateTreeStatusAll();

				labelConnectedServer.setText(url);

				storeOldStatus();
			}

			private void storeOldStatus() {
				oldServer = comboServerURL.getText();
				oldUser = textUser.getText();
				oldPassword = textPassword.getText();
				oldDomain = textDomain.getText();
			}

			private void loadOldStatus() {
				comboServerURL.setText(oldServer);
				textUser.setText(oldUser);
				textPassword.setText(oldPassword);
				textDomain.setText(oldDomain);
				buttonDisplay.setEnabled(false);
			}
		});

		Label labelUser = new Label(groupLogin, SWT.NONE);
		labelUser.setText(BrowserMessages.PORTALLEAFOBJECTBROWSER_USER_NAME);

		textUser = new Text(groupLogin, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		textUser.setLayoutData(data);
		textUser.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateDisplayButtonStatus();
			}
		});

		Label labelPassword = new Label(groupLogin, SWT.NONE);
		labelPassword.setText(BrowserMessages.PORTALLEAFOBJECTBROWSER_PASSWORD);

		textPassword = new Text(groupLogin, SWT.BORDER | SWT.PASSWORD);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		textPassword.setLayoutData(data);
		textPassword.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateDisplayButtonStatus();
			}
		});

		Label labelDomain = new Label(groupLogin, SWT.NONE);
		labelDomain.setText(BrowserMessages.PORTALLEAFOBJECTBROWSER_DOMAIN);

		textDomain = new Text(groupLogin, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		textDomain.setLayoutData(data);
		textDomain.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateDisplayButtonStatus();
			}
		});

		buttonAddContainer = new Button(right, SWT.PUSH);
		buttonAddContainer.setText(BrowserMessages.LABEL_ADD);
		buttonAddContainer.setEnabled(false);
		data = new GridData();
		data.widthHint = 0;
		data.heightHint = 0;
		buttonAddContainer.setLayoutData(data);
		buttonAddContainer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) treeViewer
						.getSelection();
				if (selection.isEmpty())
					return;
				ResourceTreeNode element = (ResourceTreeNode) selection
						.getFirstElement();
				String url = PolicyServerProxy.client.getNormalizedResourceURL(
						element.getUrl(), ExternalDataSourceType.SHAREPOINT);
				SelectionModel model = new SelectionModel(element.getId(), url,
						false);
				getSelectionSet().add(model);
				updateTreeStatusAll();
				tableViewer.setInput(getSelectionSet());
				updateButtonStatus();
				buttonAddContainer.setEnabled(false);
			}
		});

		tableViewer = new TableViewer(right, SWT.BORDER | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();
		table.setVisible(false);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		data = new GridData();
		data.widthHint = 0;
		data.heightHint = 0;
		data.verticalSpan = 3;
		table.setLayoutData(data);
		tableViewer.setColumnProperties(COLUMNS);
		tableViewer.setContentProvider(new SelectionContentProvider());
		tableViewer.setLabelProvider(new SelectionLabelProvider());
		tableViewer.setCellModifier(new SelectionCellModifier(tableViewer));
		tableViewer.setInput(getSelectionSet());
		tableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						ISelection selection = tableViewer.getSelection();
						if (selection.isEmpty())
							buttonRemoveContainer.setEnabled(false);
						else
							buttonRemoveContainer.setEnabled(true);
					}
				});

		CellEditor[] editors = new CellEditor[2];
		TextCellEditor textEditor = new TextCellEditor(table);
		editors[0] = textEditor;
		editors[1] = new CheckboxCellEditor(table);
		tableViewer.setCellEditors(editors);

		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMNS[0]);
		column.setWidth(200);

		column = new TableColumn(table, SWT.LEFT);
		column.setText(COLUMNS[1]);
		column.setWidth(150);

		buttonRemoveContainer = new Button(right, SWT.PUSH);
		buttonRemoveContainer.setText(BrowserMessages.LABEL_REMOVE);
		buttonRemoveContainer.setEnabled(false);
		data = new GridData();
		data.widthHint = 0;
		data.heightHint = 0;
		buttonRemoveContainer.setLayoutData(data);
		buttonRemoveContainer.addSelectionListener(new SelectionAdapter() {

			/**
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				if (selection.isEmpty())
					return;
				SelectionModel model = (SelectionModel) selection
						.getFirstElement();
				getSelectionSet().remove(model);
				updateTreeStatusAll();
				tableViewer.refresh();
				updateButtonStatus();
			}
		});

		// comboServerURL.setText("http://sharepoint2007/");
		// textUser.setText("Administrator");
		// textPassword.setText("123blue!");
		// textDomain.setText("sharepoint2007");

		updateDisplayButtonStatus();

		return parent;
	}

	private void enlargeSize() {
		GridData data = new GridData(GridData.FILL_BOTH);
		left.setLayoutData(data);

		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		buttonAddContainer.setLayoutData(data);

		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		buttonRemoveContainer.setLayoutData(data);

		data = new GridData(GridData.FILL_BOTH);
		data.verticalSpan = 3;
		Table table = tableViewer.getTable();
		table.setLayoutData(data);
		table.setVisible(true);

		getButton(IDialogConstants.OK_ID).setVisible(true);

		Rectangle rectangle = getShell().getDisplay().getBounds();
		int x = (rectangle.width - SIZE.x) / 2;
		x = (x > 0 ? x : 0);
		int y = (rectangle.width - SIZE.y) / 2;
		y = (y > 0 ? x : 0);

		getShell().setBounds(x, y, SIZE.x, SIZE.y);
	}

	private void updateTreeStatus(TreeItem item) {
		ResourceTreeNode node = (ResourceTreeNode) item.getData();
		if (node == null)
			return;
		String id = node.getId();
		item.setForeground(ResourceManager.getColor(SWT.COLOR_BLACK));
		item.setImage(getTreeNodeImage(node, false));

		for (SelectionModel model : getSelectionSet()) {
			if (model.getId().equals(id)) {
				item.setForeground(ResourceManager.getColor(SWT.COLOR_GRAY));
				item.setImage(getTreeNodeImage(node, true));
				break;
			}
			if (model.includeChildren) {
				{
					TreeItem parent = item.getParentItem();
					while (parent != null) {
						ResourceTreeNode parentnode = (ResourceTreeNode) parent
								.getData();
						String parentid = parentnode.getId();
						if (model.getId().equals(parentid)) {
							item.setForeground(ResourceManager
									.getColor(SWT.COLOR_GRAY));
							item.setImage(getTreeNodeImage(node, true));
							break;
						}
						parent = parent.getParentItem();
					}
				}
			}
		}

		TreeItem[] children = item.getItems();
		for (TreeItem child : children) {
			updateTreeStatus(child);
		}
	}

	private void updateTreeStatusAll() {
		Tree tree = treeViewer.getTree();
		for (int i = 0, n = tree.getItemCount(); i < n; i++) {
			TreeItem item = tree.getItem(i);
			updateTreeStatus(item);
		}
	}

	private Image getTreeNodeImage(ResourceTreeNode node, boolean disabled) {
		if (!disabled) {
			if (node.getType().equals(PortalResourceTypes.DOCUMENT.getValue()))
				return SharePointImageConstants.SHAREPOINT_DOCUMENTS;
			else if (node.getType().equals(
					PortalResourceTypes.FOLDER.getValue()))
				return SharePointImageConstants.SHAREPOINT_FOLDER;
			else if (node.getType().equals(PortalResourceTypes.LIST.getValue()))
				return SharePointImageConstants.SHAREPOINT_LIST;
			else if (node.getType().equals(
					PortalResourceTypes.LIST_ITEMS.getValue()))
				return SharePointImageConstants.SHAREPOINT_LIST;
			else if (node.getType().equals(PortalResourceTypes.PAGE.getValue()))
				return SharePointImageConstants.SHAREPOINT_PAGE;
			else if (node.getType().equals(
					PortalResourceTypes.PORTAL.getValue()))
				return SharePointImageConstants.SHAREPOINT_PAGES;
			else if (node.getType().equals(PortalResourceTypes.SITE.getValue()))
				return SharePointImageConstants.SHAREPOINT_SITES;
			else if (node.getType().equals(
					PortalResourceTypes.WEB_PART.getValue()))
				return SharePointImageConstants.SHAREPOINT_SP_SITES;
			return SharePointImageConstants.SHAREPOINT;
		} else {
			if (node.getType().equals(PortalResourceTypes.DOCUMENT.getValue()))
				return SharePointImageConstants.SHAREPOINT_DOCUMENTS_DISABLED;
			else if (node.getType().equals(
					PortalResourceTypes.FOLDER.getValue()))
				return SharePointImageConstants.SHAREPOINT_FOLDER_DISABLED;
			else if (node.getType().equals(PortalResourceTypes.LIST.getValue()))
				return SharePointImageConstants.SHAREPOINT_LIST_DISABLED;
			else if (node.getType().equals(
					PortalResourceTypes.LIST_ITEMS.getValue()))
				return SharePointImageConstants.SHAREPOINT_LIST_DISABLED;
			else if (node.getType().equals(PortalResourceTypes.PAGE.getValue()))
				return SharePointImageConstants.SHAREPOINT_PAGE_DISABLED;
			else if (node.getType().equals(
					PortalResourceTypes.PORTAL.getValue()))
				return SharePointImageConstants.SHAREPOINT_PAGES_DISABLED;
			else if (node.getType().equals(PortalResourceTypes.SITE.getValue()))
				return SharePointImageConstants.SHAREPOINT_SITES_DISABLED;
			else if (node.getType().equals(
					PortalResourceTypes.WEB_PART.getValue()))
				return SharePointImageConstants.SHAREPOINT_SP_SITES_DISABLED;
			return SharePointImageConstants.SHAREPOINT;
		}
	}

	private void updateDisplayButtonStatus() {
		String server = comboServerURL.getText();
		String user = textUser.getText();
		String password = textPassword.getText();
		String domain = textDomain.getText();
		if (server.length() == 0 || user.length() == 0
				|| password.length() == 0 || domain.length() == 0)
			buttonDisplay.setEnabled(false);
		else
			buttonDisplay.setEnabled(true);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);

		getButton(IDialogConstants.OK_ID).setVisible(false);
	}

	public Set<SelectionModel> getSelectionSet() {
		if (selectionSet == null) {
			selectionSet = new HashSet<SelectionModel>();
		}
		return selectionSet;
	}

	private void updateButtonStatus() {
		// Update ok button status
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		TableItem[] selections = tableViewer.getTable().getItems();
		if (selections.length > 0) {
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		} else {
			buttonRemoveContainer.setEnabled(false);
		}
	}

	public List<String> getItemsToReturn() {
		List<String> list = new ArrayList<String>();
		Set<SelectionModel> model = getSelectionSet();
		for (SelectionModel item : model) {
			String add = "";
			if (item.includeChildren) {
				add = item.url + "/**";
			} else {
				add = item.url;
			}
			boolean found = false;
			for (String i : list) {
				if (i.equals(add)) {
					found = true;
					break;
				}
			}
			if (!found) {
				list.add(add);
			}
		}
		return list;
	}

	private void loadUserDefinedModel() {
		userDefinedPortals = new HashMap<String, PortalModel>();
		Preferences preferences = Activator.getDefault().getPluginPreferences();
		String[] portals = preferences.getString(PORTALS).split(
				BrowserMessages.PORTALLEAFOBJECTBROWSER_DELIMETER, -1);
		for (String portal : portals) {
			String items[] = portal.split(
					BrowserMessages.PORTALLEAFOBJECTBROWSER_SEPARATOR, -1);
			if (items.length == 3) {
				PortalModel model = new PortalModel();
				model.setURL(items[0]);
				model.setUserName(items[1]);
				model.setDomain(items[2]);
				userDefinedPortals.put(items[0], model);
			}
		}
	}

	private void saveUserDefinedModel() {
		Preferences preferences = Activator.getDefault().getPluginPreferences();
		PortalModel model = new PortalModel();
		model.setURL(comboServerURL.getText());
		model.setUserName(textUser.getText());
		model.setDomain(textDomain.getText());
		userDefinedPortals.put(comboServerURL.getText(), model);

		StringBuffer output = new StringBuffer();
		for (String key : userDefinedPortals.keySet()) {
			PortalModel item = userDefinedPortals.get(key);
			output.append(item.getURL());
			output.append(BrowserMessages.PORTALLEAFOBJECTBROWSER_SEPARATOR);
			output.append(item.getUserName());
			output.append(BrowserMessages.PORTALLEAFOBJECTBROWSER_SEPARATOR);
			output.append(item.getDomain());
			output.append(BrowserMessages.PORTALLEAFOBJECTBROWSER_DELIMETER);
		}
		int length = output.length();
		if (length > 0) {
			output.deleteCharAt(length - 1);
		}
		preferences.setValue(PORTALS, output.toString());
	}
}
