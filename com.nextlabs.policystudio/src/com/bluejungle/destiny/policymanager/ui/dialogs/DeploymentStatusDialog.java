/*
 * Created on Apr 26, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.bluejungle.destiny.policymanager.UserProfileEnum;
import com.bluejungle.destiny.policymanager.editor.ReadOnlyPanelFactory;
import com.bluejungle.destiny.policymanager.framework.standardlisteners.TableColumnResizeListener;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.util.PlatformUtils;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.AgentStatusDescriptor;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author bmeng
 */

public class DeploymentStatusDialog extends Dialog {

	private static Point WINDOW_SIZE = new Point(800, 600);

	private List<AgentStatusDescriptor> desktopStatus = new ArrayList<AgentStatusDescriptor>();
	private List<AgentStatusDescriptor> fileServerStatus = new ArrayList<AgentStatusDescriptor>();
	private List<AgentStatusDescriptor> portalStatus = new ArrayList<AgentStatusDescriptor>();
	private List<AgentStatusDescriptor> activeDirectoryStatus = new ArrayList<AgentStatusDescriptor>();
	private TabFolder tabFolder;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param style
	 */
	public DeploymentStatusDialog(Shell parent) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setSize(WINDOW_SIZE);
		newShell.setText(DialogMessages.DEPLOYMENTSTATUSDIALOG_TITLE);
		newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);

		setupStatusDescriptors();
	}

	/**
	 * Gets status descriptors and sorts them into fileserver and desktop
	 * descriptors.
	 */
	private void setupStatusDescriptors() {
		Collection<AgentStatusDescriptor> statusDescriptorList = PolicyServerProxy
				.getAgentList();

		if (statusDescriptorList != null) {
			for (AgentStatusDescriptor statusDescriptor : statusDescriptorList) {
				if (statusDescriptor.getAgentType() == AgentTypeEnumType.DESKTOP) {
					desktopStatus.add(statusDescriptor);
				} else if (statusDescriptor.getAgentType() == AgentTypeEnumType.FILE_SERVER) {
					fileServerStatus.add(statusDescriptor);
				} else if (statusDescriptor.getAgentType() == AgentTypeEnumType.PORTAL) {
					portalStatus.add(statusDescriptor);
				} else if (statusDescriptor.getAgentType() == AgentTypeEnumType.ACTIVE_DIRECTORY) {
					activeDirectoryStatus.add(statusDescriptor);
				}
			}
		}
	}

	protected Control createDialogArea(Composite parent) {
		Composite root = (Composite) super.createDialogArea(parent);

		addTabs(root);

		return parent;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID,
				DialogMessages.LABEL_CLOSE, false);
	}

	/**
     * 
     */
	private void addTabs(Composite root) {
		tabFolder = new TabFolder(root, SWT.NONE);
		tabFolder.setLayout(new GridLayout());
		GridData data = new GridData(GridData.FILL_BOTH);
		tabFolder.setLayoutData(data);

		UserProfileEnum profile = PlatformUtils.getProfile();
		if (profile != UserProfileEnum.PORTAL) {
			TabItem desktopTabItem = new TabItem(tabFolder, SWT.NONE);
			desktopTabItem.setText(DialogMessages.DEPLOYMENTSTATUSDIALOG_DESKTOP_ENFORCERS);
			AgentStatusPanel desktopTab = new AgentStatusPanel(tabFolder, SWT.NONE, desktopStatus);
			desktopTabItem.setControl(desktopTab);

			TabItem fileServerTabItem = new TabItem(tabFolder, SWT.NONE);
			fileServerTabItem.setText(DialogMessages.DEPLOYMENTSTATUSDIALOG_FILE_SERVER_ENFORCERS);
			AgentStatusPanel fileServerTab = new AgentStatusPanel(tabFolder,SWT.NONE, fileServerStatus);
			fileServerTabItem.setControl(fileServerTab);
			
			TabItem activeDirectoryTabItem = new TabItem(tabFolder, SWT.NONE);
			activeDirectoryTabItem.setText(DialogMessages.DEPLOYMENTSTATUSDIALOG_ACTIVE_DIRECTORY);
			AgentStatusPanel activeDirectoryTab = new AgentStatusPanel(tabFolder,SWT.NONE, activeDirectoryStatus);
			activeDirectoryTabItem.setControl(activeDirectoryTab);
		}

		if (profile != UserProfileEnum.FILESYSTEM) {
			TabItem portalTabItem = new TabItem(tabFolder, SWT.NONE);
			portalTabItem
					.setText(DialogMessages.DEPLOYMENTSTATUSDIALOG_PORTAL_ENFORCERS);
			AgentStatusPanel portalTab = new AgentStatusPanel(tabFolder,
					SWT.NONE, portalStatus);
			portalTabItem.setControl(portalTab);
		}
	}

	/**
	 * @author fuad
	 */
	private class AgentStatusPanel extends Composite {

		private List<AgentStatusDescriptor> agentStatusList = null;
		private TableViewer agentListTable = null;
		private TableViewer componentListTable = null;
		private Composite rightPanel = null;

		private SelectionAdapter agentColumnSelectionAdapter = new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int direction = SWT.DOWN;
				Table table = agentListTable.getTable();
				TableColumn column = (TableColumn) e.widget;
				if (column == table.getSortColumn()) {
					switch (table.getSortDirection()) {
					case SWT.DOWN:
						direction = SWT.UP;
						break;
					case SWT.UP:
						direction = SWT.DOWN;
						break;
					}
				} else {
					table.setSortColumn(column);
				}
				table.setSortDirection(direction);
				agentListTable.setSorter(new AgentTableSorter(column.getText(),
						direction));
				agentListTable.refresh();
			}
		};

		private SelectionAdapter componentColumnSelectionAdapter = new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int direction = SWT.DOWN;
				Table table = componentListTable.getTable();
				TableColumn column = (TableColumn) e.widget;
				if (column == table.getSortColumn()) {
					switch (table.getSortDirection()) {
					case SWT.DOWN:
						direction = SWT.UP;
						break;
					case SWT.UP:
						direction = SWT.DOWN;
						break;
					}
				} else {
					table.setSortColumn(column);
				}
				table.setSortDirection(direction);
				componentListTable.setSorter(new ComponentTableSorter(column
						.getText(), direction));
				componentListTable.refresh();
			}
		};

		private class AgentTableContentProvider implements
				IStructuredContentProvider {

			List<AgentStatusDescriptor> list = null;

			@SuppressWarnings("unchecked")
			public void inputChanged(Viewer v, Object oldInput, Object newInput) {
				list = (List<AgentStatusDescriptor>) newInput;
			}

			public void dispose() {
			}

			public Object[] getElements(Object parent) {
				if (list != null) {
					return list.toArray();
				}

				return new Object[0];
			}
		}

		private class AgentTableLabelProvider extends LabelProvider implements
				ITableLabelProvider {

			public String getColumnText(Object obj, int index) {

				AgentStatusDescriptor statusDescriptor = (AgentStatusDescriptor) obj;

				switch (index) {
				case 0:
					return statusDescriptor.getHostName();
				case 1:
					return statusDescriptor.getNumPolicies() + "/"
							+ statusDescriptor.getNumComponents();
				}

				return "";

			}

			/**
			 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
			 *      int)
			 */
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
		}

		private class ComponentTableContentProvider implements
				IStructuredContentProvider {

			Collection<DomainObjectDescriptor> list = null;

			@SuppressWarnings("unchecked")
			public void inputChanged(Viewer v, Object oldInput, Object newInput) {
				this.list = (Collection<DomainObjectDescriptor>) newInput;
			}

			public void dispose() {
			}

			public Object[] getElements(Object parent) {
				if (list != null) {
					return (DomainObjectDescriptor[]) list
							.toArray(new DomainObjectDescriptor[list.size()]);
				}

				return new Object[0];
			}
		}

		private class AgentTableSorter extends ViewerSorter {
			private String column;
			private int direction;

			public AgentTableSorter(String column, int direction) {
				this.column = column;
				this.direction = direction;
			}

			/**
			 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(Viewer viewer, Object element1, Object element2) {
				AgentStatusDescriptor descriptor1 = (AgentStatusDescriptor) element1;
				AgentStatusDescriptor descriptor2 = (AgentStatusDescriptor) element2;
				if (column.equals(DialogMessages.DEPLOYMENTSTATUSDIALOG_HOST)) {
					if (direction == SWT.DOWN) {
						return descriptor2.getHostName().compareToIgnoreCase(
								descriptor1.getHostName());
					} else if (direction == SWT.UP) {
						return descriptor1.getHostName().compareToIgnoreCase(
								descriptor2.getHostName());
					}
				} else if (column
						.equals(DialogMessages.DEPLOYMENTSTATUSDIALOG_PC)) {
					String status1 = descriptor1.getNumPolicies() + "/"
							+ descriptor1.getNumComponents();
					String status2 = descriptor2.getNumPolicies() + "/"
							+ descriptor2.getNumComponents();
					if (direction == SWT.DOWN) {
						return status2.compareToIgnoreCase(status1);
					} else if (direction == SWT.UP) {
						return status1.compareToIgnoreCase(status2);
					}
				}
				return descriptor1.getHostName().compareToIgnoreCase(
						descriptor2.getHostName());
			}
		}

		private class ComponentTableSorter extends ViewerSorter {
			private String column;
			private int direction;

			public ComponentTableSorter(String column, int direction) {
				this.column = column;
				this.direction = direction;
			}

			/**
			 * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
			 */
			@SuppressWarnings("deprecation")
			@Override
			public int category(Object element) {
				DomainObjectDescriptor descriptor = (DomainObjectDescriptor) element;
				EntityType type = descriptor.getType();
				if (type == EntityType.POLICY) {
					return 0;
				} else if (type == EntityType.USER) {
					return 1;
				} else if (type == EntityType.HOST) {
					return 2;
				} else if (type == EntityType.APPLICATION) {
					return 3;
				} else if (type == EntityType.RESOURCE) {
					return 4;
				} else if (type == EntityType.ACTION) {
					return 5;
				} else if (type == EntityType.PORTAL) {
					return 6;
				}
				return 7;
			}

			/**
			 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(Viewer viewer, Object element1, Object element2) {
				int cat1 = category(element1);
				int cat2 = category(element2);
				DomainObjectDescriptor descriptor1 = (DomainObjectDescriptor) element1;
				DomainObjectDescriptor descriptor2 = (DomainObjectDescriptor) element2;

				if (column
						.equals(DialogMessages.DEPLOYMENTSTATUSDIALOG_CONTENTS)) {
					if (direction == SWT.DOWN) {
						if (cat1 != cat2) {
							return cat1 - cat2;
						}
						return descriptor2.getName().compareToIgnoreCase(
								descriptor1.getName());
					} else if (direction == SWT.UP) {
						if (cat1 != cat2) {
							return cat2 - cat1;
						}
						return descriptor1.getName().compareToIgnoreCase(
								descriptor2.getName());
					}
				}

				return descriptor1.getName().compareToIgnoreCase(
						descriptor2.getName());
			}
		}

		private class ComponentTableLabelProvider extends LabelProvider
				implements ITableLabelProvider {

			public String getColumnText(Object obj, int index) {
				DomainObjectDescriptor descriptor = (DomainObjectDescriptor) obj;
				return DomainObjectHelper.getDisplayName(descriptor);
			}

			/**
			 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
			 *      int)
			 */
			public Image getColumnImage(Object element, int columnIndex) {
				return ObjectLabelImageProvider.getImage(element);
			}
		}

		/**
		 * Constructor
		 * 
		 * @param parent
		 * @param style
		 * @param agentStatusList
		 */
		public AgentStatusPanel(Composite parent, int style,
				List<AgentStatusDescriptor> agentStatusList) {
			super(parent, style);
			this.agentStatusList = agentStatusList;
			initialize();
		}

		/**
		 * initialize controls
		 */
		public void initialize() {
			GridLayout layout = new GridLayout();
			setLayout(layout);
			GridData data = new GridData(GridData.FILL_BOTH);
			setLayoutData(data);

			SashForm sashForm = new SashForm(this, SWT.HORIZONTAL);
			data = new GridData(GridData.FILL_BOTH);
			sashForm.setLayoutData(data);

			agentListTable = new TableViewer(sashForm, SWT.BORDER | SWT.SINGLE
					| SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
			Table agentTable = agentListTable.getTable();
			TableColumn column = new TableColumn(agentTable, SWT.LEFT);
			column.setWidth(150);
			column.setText(DialogMessages.DEPLOYMENTSTATUSDIALOG_HOST);
			column.addSelectionListener(agentColumnSelectionAdapter);
			column = new TableColumn(agentTable, SWT.LEFT);
			column.setWidth(50);
			column.setText(DialogMessages.DEPLOYMENTSTATUSDIALOG_PC);
			column.addSelectionListener(agentColumnSelectionAdapter);
			agentTable.setHeaderVisible(true);
			agentListTable.setContentProvider(new AgentTableContentProvider());
			agentListTable.setLabelProvider(new AgentTableLabelProvider());
			agentListTable.setInput(agentStatusList);
			agentListTable
					.addSelectionChangedListener(new ISelectionChangedListener() {

						public void selectionChanged(SelectionChangedEvent event) {
							IStructuredSelection selection = (IStructuredSelection) agentListTable
									.getSelection();
							AgentStatusDescriptor statusDescriptor = (AgentStatusDescriptor) selection
									.getFirstElement();
							Collection<DomainObjectDescriptor> descriptorList = PolicyServerProxy
									.deploymentStatusForAgent(statusDescriptor);
							if (descriptorList != null) {
								componentListTable.setInput(descriptorList);
							} else {
								// remove previous panel before adding new one.
								Control[] controls = rightPanel.getChildren();
								for (int i = 0; i < controls.length; i++) {
									controls[i].dispose();
								}
							}
						}
					});
			addListener(SWT.Resize, new TableColumnResizeListener(
					agentListTable.getTable()));

			SashForm rightSash = new SashForm(sashForm, SWT.VERTICAL);
			sashForm.setWeights(new int[] { 2, 1 });

			componentListTable = new TableViewer(rightSash, SWT.BORDER
					| SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL
					| SWT.H_SCROLL);
			Table componentTable = componentListTable.getTable();
			column = new TableColumn(componentTable, SWT.LEFT);
			column.setWidth(150);
			column.setText(DialogMessages.DEPLOYMENTSTATUSDIALOG_CONTENTS);
			column.addSelectionListener(componentColumnSelectionAdapter);
			componentTable.setHeaderVisible(true);
			componentListTable
					.setContentProvider(new ComponentTableContentProvider());
			componentListTable
					.setLabelProvider(new ComponentTableLabelProvider());
			componentListTable
					.addSelectionChangedListener(new ISelectionChangedListener() {

						public void selectionChanged(SelectionChangedEvent event) {
							// remove previous panel before adding new one.
							Control[] controls = rightPanel.getChildren();
							for (int i = 0; i < controls.length; i++) {
								controls[i].dispose();
							}

							IStructuredSelection selection = (IStructuredSelection) componentListTable
									.getSelection();

							if (selection.isEmpty()) {
								// When we navigate agent lists, selection may
								// become empty. When this happens, this method
								// has nothing to do.
								return;
							}

							DomainObjectDescriptor descriptor = (DomainObjectDescriptor) selection
									.getFirstElement();

							selection = (IStructuredSelection) agentListTable
									.getSelection();
							AgentStatusDescriptor statusDescriptor = (AgentStatusDescriptor) selection
									.getFirstElement();

							IHasId entity = (IHasId) PolicyServerProxy
									.getDeployedObject(descriptor,
											statusDescriptor.getLastUpdated());

							if (entity != null) {
								ScrolledComposite scrolledComposite = new ScrolledComposite(
										rightPanel, SWT.V_SCROLL | SWT.H_SCROLL);
								GridData data = new GridData(GridData.FILL_BOTH);
								scrolledComposite.setLayoutData(data);
								scrolledComposite.setLayout(new GridLayout());

								Composite detail = ReadOnlyPanelFactory
										.getEditorPanel(scrolledComposite,
												SWT.NONE, entity);
								data = new GridData(GridData.FILL_BOTH);
								detail.setLayoutData(data);

								scrolledComposite.setExpandHorizontal(true);
								scrolledComposite.setExpandVertical(true);
								scrolledComposite.setMinSize(detail
										.computeSize(SWT.DEFAULT, SWT.DEFAULT));
								scrolledComposite.setContent(detail);

								rightPanel.layout(true, true);
							}
						}
					});
			componentListTable.setInput(new ArrayList<AgentStatusDescriptor>());
			addListener(SWT.Resize, new TableColumnResizeListener(
					componentListTable.getTable()));

			rightPanel = new Composite(rightSash, SWT.BORDER);
			rightPanel.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
			rightPanel.setLayout(new GridLayout());

			data = new GridData(GridData.FILL_BOTH);
			agentTable.setLayoutData(data);

			data = new GridData(GridData.FILL_BOTH);
			componentTable.setLayoutData(data);

			data = new GridData(GridData.FILL_BOTH);
			rightPanel.setLayoutData(data);

			rightSash.setWeights(new int[] { 1, 2 });
		}
	}
}
