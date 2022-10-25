/*
 * Created on Apr 26, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectSearchSpec;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.deployment.AgentAttribute;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/
 *          SetDeploymentTargetDialog.java#2 $
 */

public class SetDeploymentTargetDialog extends Dialog {

	private static final IEventManager EVENT_MANAGER;

	static {
		IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
		EVENT_MANAGER = componentManager.getComponent(EventManagerImpl.COMPONENT_INFO);
	}

	public  enum Deploy {
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

	private static IExpression PORTAL_TYPE_CONST = Constant.build(AgentTypeEnumType.PORTAL.getName());
	private static IExpression DESKTOP_TYPE_CONST = Constant.build(AgentTypeEnumType.DESKTOP.getName());
	private static IExpression FILE_SERVER_TYPE_CONST = Constant.build(AgentTypeEnumType.FILE_SERVER.getName());
	private static IExpression ACTIVE_DIRECTORY_CONST = Constant.build(AgentTypeEnumType.ACTIVE_DIRECTORY.getName());

	private IDPolicy policy;
	private List<IHasId> policies;
	public static Deploy deploymentType;
	private Composite left, rightbottom;
	private Combo comboType;
	private Text searchText;
	private TabFolder folder;
	private TableViewer selectedEnforcersViewer;
	private TableViewer availableFileServerViewer, availablePortalViewer,availableWinDesktopViewer, availableActiveDirectoryViewer;
	private Button buttonAdd, buttonRemove;
	private Button buttonFind, buttonReset;

	private List<LeafObject> availableFileServers, availablePortals,availableWinDesktops, availableActiveDirectories;
	private List<LeafObject> selectedFileServers, selectedPortals,selectedWinDesktops,selectedActiveDirectories;
	private List<LeafObject> selectedEnforcers = new ArrayList<LeafObject>();

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

	private class AvailableContentProvider implements
			IStructuredContentProvider {

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

	private class AvailableLabelProvider extends LabelProvider implements
			ITableLabelProvider {

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
			return 3;
		}

		/**
		 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Viewer viewer, Object element1, Object element2) {
			int cat1 = category(element1);
			int cat2 = category(element2);
			if (cat1 != cat2){
				return cat1 - cat2;
			}
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

	private class SelectedLabelProvider extends LabelProvider implements
			ITableLabelProvider {

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
					}else if (type == LeafObjectType.ACTIVE_DIRECTORY_AGENT) {
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

	public SetDeploymentTargetDialog(Shell parentShell, List<IHasId> policies) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.policies = policies;
		this.policy = (IDPolicy) policies.get(0);

		if (policy.getDeploymentTarget() == null) {
			deploymentType = Deploy.AUTO_DEPLOY;
		} else {
			deploymentType = Deploy.MANUAL_DEPLOY;

			// Load preset target
			final List<IPredicate> idPreds = new ArrayList<IPredicate>();
			IPredicateVisitor idFinder = new DefaultPredicateVisitor() {

				/**
				 * @see DefaultPredicateVisitor#visit(IRelation)
				 */
				@Override
				public void visit(IRelation pred) {
					if (pred.getLHS() == AgentAttribute.ID || pred.getRHS() == AgentAttribute.ID) {
						idPreds.add(pred);
					}
				}
			};
			for (IHasId policyObj : policies) {
				IDPolicy policy = (IDPolicy) policyObj;
				IPredicate deploymentTarget = policy.getDeploymentTarget();
				if (deploymentTarget != null) {
					deploymentTarget.accept(idFinder,IPredicateVisitor.PREORDER);
				}
			}

			List<LeafObject> referencedAgents = Collections.emptyList();
			IPredicate pred = null;
			if (idPreds.size() == 1) {
				pred = idPreds.get(0);
			} else if (idPreds.size() > 1) {
				pred = new CompositePredicate(BooleanOp.OR, idPreds);
			}
			if (pred != null) {
				try {
					LeafObjectSearchSpec leafObjectSearchSpec = new LeafObjectSearchSpec(
							LeafObjectType.DESKTOP_AGENT
							// The particular agent type is disregarded on
							// ID-based
							// queries
							, pred, 0);

					referencedAgents = EntityInfoProvider.runLeafObjectQuery(leafObjectSearchSpec);
				} catch (PolicyEditorException exception) {
					LoggingUtil.logError(Activator.ID,"Failed to retrieve leaf objects", exception);
				}
			}

			// Prepare an id->leaf Map
			Map<Long, LeafObject> byId = new HashMap<Long, LeafObject>();
			for (LeafObject leaf : referencedAgents) {
				byId.put(leaf.getId(), leaf);
			}
			selectedFileServers = getTargets((CompositePredicate) policy.getDeploymentTarget(), Type.FILE_SERVER, byId);
			selectedPortals = getTargets((CompositePredicate) policy.getDeploymentTarget(), Type.PORTAL, byId);
			selectedWinDesktops = getTargets((CompositePredicate) policy.getDeploymentTarget(), Type.WIN_DESKTOP, byId);
			selectedActiveDirectories = getTargets((CompositePredicate) policy.getDeploymentTarget(), Type.ACTIVE_DIRECTORY, byId);

			selectedEnforcers.addAll(selectedFileServers);
			selectedEnforcers.addAll(selectedPortals);
			selectedEnforcers.addAll(selectedWinDesktops);
			selectedEnforcers.addAll(selectedActiveDirectories);
		}
	}
	
	public List<LeafObject> getSelectedEnforcersList (){
		return selectedEnforcers;
	}

	/**
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_SET_DEPLOYMENT_TARGETS);
		newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, DialogMessages.LABEL_SAVE, false);
		createButton(parent, IDialogConstants.CANCEL_ID,IDialogConstants.CANCEL_LABEL, false);

		changeWindowSize();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite root = (Composite) super.createDialogArea(parent);

		Composite bottom = new Composite(root, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		bottom.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		bottom.setLayoutData(data);

		left = new Composite(bottom, SWT.NONE);
		data = new GridData(GridData.FILL_BOTH);
		left.setLayoutData(data);
		layout = new GridLayout();
		left.setLayout(layout);

		Group group = new Group(left, SWT.NONE);
		group.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_AVAILABLE_ENFORCERS);
		data = new GridData(GridData.FILL_BOTH);
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

		Composite right = new Composite(bottom, SWT.NONE);
		data = new GridData(GridData.FILL_BOTH);
		right.setLayoutData(data);
		layout = new GridLayout(2, false);
		right.setLayout(layout);

		label = new Label(right, SWT.NONE);
		label.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_POLICY);

		Label labelPolicy = new Label(right, SWT.NONE);
		labelPolicy.setText(DomainObjectHelper.getDisplayName(policy));
		data = new GridData(GridData.FILL_HORIZONTAL);
		labelPolicy.setLayoutData(data);

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
		column.setWidth(200);
		column = new TableColumn(table, SWT.CENTER);
		column.setText(DialogMessages.SETDEPLOYMENTTARGETDIALOG_TYPE);
		column.setWidth(200);
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
		return parent;
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
			availableActiveDirectories = getSelectableLeafObjects(LeafObjectType.ACTIVE_DIRECTORY_AGENT, 2000);
			filterSelected(availableActiveDirectories);
			availableActiveDirectoryViewer.setInput(availableActiveDirectories);
		}
		updateAddButtonStatus();
		updateRemoveButtonStatus();
	}

	private void filterSelected(List<LeafObject> result) {
		result.removeAll(selectedEnforcers);
	}

	private List<LeafObject> getTargets(CompositePredicate deploymentTarget,Type type, Map<Long, LeafObject> data) {	
		// upgrade from 6.0 to 6.5, policy studio support one more enforcer, which is ACTIVE_DIRECTORY.
		// need to check whether it's got stated in the pql for deployment target.
		if (( deploymentTarget.predicateCount()==3)&&(type==Type.ACTIVE_DIRECTORY)){
			return new ArrayList<LeafObject>();
		}else{
			CompositePredicate fileServers = (CompositePredicate) ((CompositePredicate) deploymentTarget.predicateAt(type.getType())).predicateAt(0);		
			List<IPredicate> predicates = new ArrayList<IPredicate>(fileServers.predicates());
			// remove any dummy false predicates.
			while (predicates.contains(PredicateConstants.FALSE)) {
				predicates.remove(PredicateConstants.FALSE);
			}

			List<LeafObject> leafObjects = new ArrayList<LeafObject>();
			for (IPredicate predicate : predicates) {
				if (predicate instanceof Relation) {
					IExpression rhs = ((Relation) predicate).getRHS();
					Object rhsVal = rhs.evaluate(null).getValue();
					if (data.containsKey(rhsVal)) {
						leafObjects.add(data.get(rhsVal));
					}
				}
			}

			return leafObjects;
		}
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

	private List<LeafObject> getCurrentAvailableList() {
		int index = folder.getSelectionIndex();
		if (index == Type.FILE_SERVER.getType()) {
			return availableFileServers;
		} else if (index == Type.PORTAL.getType()) {
			return availablePortals;
		} else if (index == Type.WIN_DESKTOP.getType()) {
			return availableWinDesktops;
		} else if (index == Type.ACTIVE_DIRECTORY.getType()) {
			return availableActiveDirectories;
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

	private void updateRemoveButtonStatus() {
		ISelection selection = selectedEnforcersViewer.getSelection();
		buttonRemove.setEnabled(false);
		if (!selection.isEmpty()) {
			buttonRemove.setEnabled(true);
		}
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

	public List<LeafObject> getSelectableLeafObjects(LeafObjectType leafObjectType, int maxResults) {
		if (leafObjectType == null) {
			throw new NullPointerException("leafObjectType cannot be null.");
		}

		List<LeafObject> itemsToReturn = null;

		String searchString = searchText.getText();

		IPredicate pred = SubjectAttribute.HOST_NAME.buildRelation(RelationOp.EQUALS, Constant.build(searchString + "*"));
		LeafObjectSearchSpec leafObjectSearchSpec = new LeafObjectSearchSpec(leafObjectType, pred, maxResults);

		try {
			itemsToReturn = EntityInfoProvider.runLeafObjectQuery(leafObjectSearchSpec);
		} catch (PolicyEditorException exception) {
			LoggingUtil.logError(Activator.ID,"Failed to retrieve leaf objects", exception);
		}

		return itemsToReturn;
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

	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {
		Deploy orig = policy.getDeploymentTarget() != null ? Deploy.MANUAL_DEPLOY: Deploy.AUTO_DEPLOY;
		List<IDPolicy> modifiedPolicies = new ArrayList<IDPolicy>();

		if (deploymentType == Deploy.AUTO_DEPLOY && deploymentType != orig) {
			if (policy.getDeploymentTarget() != null) {
				policy.setDeploymentTarget(null);
				modifiedPolicies.add(policy);
			}
		} else if (deploymentType == Deploy.MANUAL_DEPLOY) {
			CompositePredicate deploymentTarget = (CompositePredicate) policy.getDeploymentTarget();
			if (deploymentTarget == null) {
				deploymentTarget = createDeploymentTargetPredicate();
				policy.setDeploymentTarget(deploymentTarget);
			}
			selectedFileServers = new ArrayList<LeafObject>();
			selectedPortals = new ArrayList<LeafObject>();
			selectedWinDesktops = new ArrayList<LeafObject>();
			selectedActiveDirectories = new ArrayList<LeafObject>();
			for (LeafObject object : selectedEnforcers) {
				if (object.getType() == LeafObjectType.FILE_SERVER_AGENT) {
					selectedFileServers.add(object);
				} else if (object.getType() == LeafObjectType.PORTAL_AGENT) {
					selectedPortals.add(object);
				} else if (object.getType() == LeafObjectType.DESKTOP_AGENT) {
					selectedWinDesktops.add(object);
				} else if (object.getType() == LeafObjectType.ACTIVE_DIRECTORY_AGENT) {
					selectedActiveDirectories.add(object);
				}
				
			}
			updatePredicates(deploymentTarget, Type.FILE_SERVER,selectedFileServers);
			updatePredicates(deploymentTarget, Type.PORTAL, selectedPortals);
			updatePredicates(deploymentTarget, Type.WIN_DESKTOP,selectedWinDesktops);
			updatePredicates(deploymentTarget, Type.ACTIVE_DIRECTORY,selectedActiveDirectories);
			modifiedPolicies.add(policy);
		}
		// save all policies
		if (modifiedPolicies.size() != 0) {
			PolicyServerProxy.saveEntities(modifiedPolicies);

			Set<PolicyOrComponentModifiedEvent> eventsToFire = new HashSet<PolicyOrComponentModifiedEvent>();
			Iterator modifiedPoliciesIterator = modifiedPolicies.iterator();
			while (modifiedPoliciesIterator.hasNext()) {
				Object nextModifiedPolicy = modifiedPoliciesIterator.next();
				PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent((IHasId) nextModifiedPolicy);
				eventsToFire.add(objectModifiedEvent);
			}

			EVENT_MANAGER.fireEvent(eventsToFire);
		}
		super.okPressed();
	}

	protected void updatePredicates(CompositePredicate deploymentTarget, Type type, List<LeafObject> leafObjects) {
		CompositePredicate newPred = new CompositePredicate(BooleanOp.OR, PredicateConstants.FALSE);
		if (!leafObjects.isEmpty()) {
			for (LeafObject leaf : leafObjects) {
				newPred.addPredicate(AgentAttribute.ID.buildRelation(RelationOp.EQUALS, Constant.build(leaf.getId().longValue())));
			}
		} else {
			newPred.addPredicate(PredicateConstants.FALSE);
		}
		CompositePredicate compositeAnd = (CompositePredicate) ((CompositePredicate) deploymentTarget.predicateAt(type.getType()));
		compositeAnd.removePredicate(0);
		compositeAnd.insertElement(newPred, 0);
	}

	private CompositePredicate createDeploymentTargetPredicate() {
		CompositePredicate ret = new CompositePredicate(BooleanOp.OR,new ArrayList<IPredicate>());

		// add fileserver predicate
		CompositePredicate fileServerPredicate = new CompositePredicate(BooleanOp.AND, new ArrayList<IPredicate>());
		ret.addPredicate(fileServerPredicate);
		CompositePredicate fileServerList = new CompositePredicate(	BooleanOp.OR, new ArrayList<IPredicate>());
		IRelation fileServerRelation = AgentAttribute.TYPE.buildRelation(RelationOp.EQUALS, FILE_SERVER_TYPE_CONST);
		fileServerPredicate.addPredicate(fileServerList);
		fileServerPredicate.addPredicate(fileServerRelation);
		fileServerList.addPredicate(PredicateConstants.FALSE);
		fileServerList.addPredicate(PredicateConstants.FALSE);

		// add desktop predicate
		CompositePredicate desktopPredicate = new CompositePredicate(BooleanOp.AND, new ArrayList<IPredicate>());
		ret.addPredicate(desktopPredicate);
		CompositePredicate desktopList = new CompositePredicate(BooleanOp.OR, new ArrayList<IPredicate>());
		IRelation desktopRelation = AgentAttribute.TYPE.buildRelation(RelationOp.EQUALS, DESKTOP_TYPE_CONST);
		desktopPredicate.addPredicate(desktopList);
		desktopPredicate.addPredicate(desktopRelation);
		desktopList.addPredicate(PredicateConstants.FALSE);
		desktopList.addPredicate(PredicateConstants.FALSE);

		// add portal predicate
		CompositePredicate ListPredicate = new CompositePredicate(BooleanOp.AND, new ArrayList<IPredicate>());
		ret.addPredicate(ListPredicate);
		CompositePredicate ListList = new CompositePredicate(BooleanOp.OR,	new ArrayList<IPredicate>());
		IRelation ListRelation = AgentAttribute.TYPE.buildRelation(	RelationOp.EQUALS, PORTAL_TYPE_CONST);
		ListPredicate.addPredicate(ListList);
		ListPredicate.addPredicate(ListRelation);
		ListList.addPredicate(PredicateConstants.FALSE);
		ListList.addPredicate(PredicateConstants.FALSE);
		
		// add Active Directory predicate
		CompositePredicate ActiveDPredicate = new CompositePredicate(BooleanOp.AND, new ArrayList<IPredicate>());
		ret.addPredicate(ActiveDPredicate);
		CompositePredicate ActiveDList = new CompositePredicate(BooleanOp.OR,	new ArrayList<IPredicate>());
		IRelation ActiveDRelation = AgentAttribute.TYPE.buildRelation(	RelationOp.EQUALS, ACTIVE_DIRECTORY_CONST);
		ActiveDPredicate.addPredicate(ActiveDList);
		ActiveDPredicate.addPredicate(ActiveDRelation);
		ActiveDList.addPredicate(PredicateConstants.FALSE);
		ActiveDList.addPredicate(PredicateConstants.FALSE);

		return ret;
	}

	public List<IHasId> getPolicies() {
		return policies;
	}
}
