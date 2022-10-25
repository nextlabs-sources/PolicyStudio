/*
 * Created on Apr 26, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.action.PolicyStudioActionFactory;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyManagerView;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.ILeafObjectBrowser;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.LeafObjectBrowserFactory;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.PlatformUtils;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.GroupAccess;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.common.UserAccess;
import com.bluejungle.pf.domain.destiny.exceptions.PolicyReference;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.epicenter.action.IAction;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

/**
 * @author fuad
 * @version $Id:
 */

public class ObjectPropertiesDialog extends Dialog {

	private static final IEventManager EVENT_MANAGER;
	static {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		EVENT_MANAGER = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
	}

	private static final LeafObjectBrowserFactory leafObjectBrowserFactoy;
	static {
		leafObjectBrowserFactoy = (LeafObjectBrowserFactory) ComponentManagerFactory
				.getComponentManager().getComponent(
						LeafObjectBrowserFactory.class);
	}

	private static Point WINDOW_SIZE = new Point(400, 600);
	private IHasId domainObject = null;
	private String originalName = null;
	private TabFolder tabFolder = null;

	private GeneralTab generalTab;
	private AccessControlTab accessControlTab;
	private boolean allowAdminActions;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public ObjectPropertiesDialog(Shell parent, IHasId domainObject) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.domainObject = domainObject;
		this.originalName = DomainObjectHelper.getName(domainObject);

		allowAdminActions = PolicyServerProxy.canPerformAction(domainObject,
				DAction.ADMIN);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(DialogMessages.OBJECTPROPERTIESDIALOG_QUOTE
				+ DomainObjectHelper.getName(domainObject)
				+ DialogMessages.OBJECTPROPERTIESDIALOG_QUOTE
				+ DialogMessages.OBJECTPROPERTIESDIALOG_PROPERTIES);
		newShell.setSize(WINDOW_SIZE);
		newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite root = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(root, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(data);
		initialize(container);

		return parent;
	}

	/**
	 * Create all controls to set up the dialog.
	 */
	private void initialize(Composite root) {
		GridLayout layout = new GridLayout();
		root.setLayout(layout);

		addTabs(root);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, DialogMessages.LABEL_SAVE,
				true);
		getButton(IDialogConstants.OK_ID).setEnabled(allowAdminActions);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}
	
	protected void excuteChangeForDomainObject (IHasId domainObject, String originalName ){		
		// Find open tabs with dependencies of the current object,
		// if any
		GlobalState gs = GlobalState.getInstance();
		gs.closeEditorFor(domainObject);
		EntityType currentType = DomainObjectHelper
				.getEntityType(domainObject);
		Collection<DomainObjectDescriptor> deps = new ArrayList<DomainObjectDescriptor>();
		deps.addAll(PolicyServerProxy.getAllReferringObjects(originalName));
		
		// close the other editors
		for (DomainObjectDescriptor descriptor : deps) {
			IHasId hasId = (IHasId) PolicyServerProxy
					.getEntityForDescriptor(descriptor);
			gs.closeEditorFor(hasId);
		}
		
		List<DomainObjectDescriptor> openedDescrs = new ArrayList<DomainObjectDescriptor>();
		if (deps != null) {
			for (DomainObjectDescriptor descr : deps) {
				IHasId opened = gs.getOpenedEntityForID(descr.getId());
				if (opened != null) {
					openedDescrs.add(new DomainObjectDescriptor(opened
							.getId(), "", new Long(0), null,
							EntityType.ILLEGAL, null,
							DevelopmentStatus.DRAFT));
				}
			}
		}
		// EntityInfoProvider.updateDescriptors(PolicyServerProxy.saveEntity(domainObject));
		Collection<DODDigest> c = PolicyServerProxy
				.saveEntityDigest(domainObject);

		Collection<? extends IHasId> changedEntities = PolicyServerProxy
				.getEntitiesForDescriptors(openedDescrs);
		if (changedEntities != null) {
			for (IHasId changed : changedEntities) {
				gs.forceLoadObjectInEditorPanel(changed);
			}
		}
		DODDigest newDescriptor = (DODDigest) c.iterator().next();
		if (currentType == EntityType.POLICY
				|| currentType == EntityType.FOLDER) {
			EntityInfoProvider.replacePolicyDescriptor(originalName,
					newDescriptor);
		} else {
			EntityInfoProvider.replaceComponentDescriptor(originalName,
					newDescriptor);
		}
		Boolean inPolicyAuthorView = PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION.isEnabled();
		if(!inPolicyAuthorView){
			PolicyManagerView.refreshCurrentTab();
		}
	}

	@Override
	protected void okPressed() {
		// Both save(0 methods have desired side effects, so use '|'
		// instead of the '||'.
		GlobalState gs = GlobalState.getInstance();
		EntityType currentType = DomainObjectHelper.getEntityType(domainObject);
		if (generalTab.save() || accessControlTab.save()) {		
			excuteChangeForDomainObject ( domainObject, originalName );
			if (currentType != EntityType.FOLDER ) {
				gs.forceLoadObjectInEditorPanel(domainObject);
			}
		}
		super.okPressed();
	}

	private void addTabs(Composite root) {
		tabFolder = new TabFolder(root, SWT.NONE);
		GridLayout layout = new GridLayout();
		tabFolder.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		tabFolder.setLayoutData(data);

		TabItem generalTabItem = new TabItem(tabFolder, SWT.NONE);
		generalTabItem.setText(DialogMessages.OBJECTPROPERTIESDIALOG_GENERAL);
		generalTab = new GeneralTab(tabFolder, SWT.NONE, domainObject);
		data = new GridData(GridData.FILL_BOTH);
		generalTab.setLayoutData(data);
		generalTabItem.setControl(generalTab);

		TabItem accessControlTabItem = new TabItem(tabFolder, SWT.NONE);
		accessControlTabItem
				.setText(DialogMessages.OBJECTPROPERTIESDIALOG_ACCESS_CONTROL);
		accessControlTab = new AccessControlTab(tabFolder, SWT.NONE,
				domainObject);
		data = new GridData(GridData.FILL_BOTH);
		accessControlTab.setLayoutData(data);
		accessControlTabItem.setControl(accessControlTab);
	}

	/**
	 * @author fuad
	 */
	private class GeneralTab extends Composite {

		private IHasId domainObject = null;
		private Text nameText = null;

		private boolean isChanged = false;
		private boolean isRename = false;

		/**
		 * Constructor
		 * 
		 * @param parent
		 * @param style
		 */
		public GeneralTab(Composite parent, int style, IHasId domainObject) {
			super(parent, style);
			this.domainObject = domainObject;
			initialize();
		}

		/**
		 * initialize controls
		 */
		public void initialize() {
			GridLayout layout = new GridLayout(2, false);
			setLayout(layout);

			Label labelName = new Label(this, SWT.NONE);
			labelName.setText(DialogMessages.OBJECTPROPERTIESDIALOG_NAME);
			GridData data = new GridData();
			labelName.setLayoutData(data);

			String fullName = DomainObjectHelper.getName(domainObject);
			int folderSeparatorIndex = fullName
					.lastIndexOf(PQLParser.SEPARATOR);
			final String name;
			final String location;
			if (folderSeparatorIndex >= 0) {
				name = fullName.substring(fullName
						.lastIndexOf(PQLParser.SEPARATOR) + 1);
				location = fullName.substring(0, fullName
						.lastIndexOf(PQLParser.SEPARATOR) + 1);
			} else {
				name = fullName;
				location = "";
			}
			nameText = new Text(this, SWT.SINGLE | SWT.BORDER);
			nameText.setText(name);
			nameText.setTextLimit(128);
			nameText.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent e) {
					PlatformUtils.validCharForName(e);
				}
			});
			data = new GridData(GridData.FILL_HORIZONTAL);
			nameText.setLayoutData(data);

			// Renaming folders is not allowed
			if (!PolicyServerProxy
					.canPerformAction(domainObject, DAction.WRITE)) {
				nameText.setEnabled(false);
			}
			if (DomainObjectHelper.getEntityType(domainObject) == EntityType.FOLDER) {
				List<DODDigest> policies = EntityInfoProvider.getPolicyList();
				DomainObjectDescriptor descriptor = DomainObjectHelper
						.getCachedDescriptor(domainObject);
				String folderName = descriptor.getName() + PQLParser.SEPARATOR;
				for (DODDigest policy : policies) {
					if (policy.getName().startsWith(folderName)) {
						nameText.setEnabled(false);
						break;
					}
				}
			}

			nameText.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					isChanged = true;
					String newName = nameText.getText();
					String fullName = location + newName;
					EntityType entityType = DomainObjectHelper
							.getEntityType(domainObject);
					boolean nameIsValid = EntityInfoProvider
							.isValidComponentName(newName);
					boolean nameIsUnique = name.equalsIgnoreCase(newName);
					if (entityType == EntityType.POLICY
							|| entityType == EntityType.FOLDER) {
						nameIsUnique |= (EntityInfoProvider
								.getExistingPolicyName(fullName) == null);
					} else {
						int pos = fullName.indexOf(PQLParser.SEPARATOR);
						if (pos != -1) {
							String typeName = fullName.substring(0, pos);
							nameIsUnique |= (EntityInfoProvider
									.getExistingComponentName(fullName,
											typeName) == null);
						}
					}
					getButton(IDialogConstants.OK_ID).setEnabled(
							nameIsValid && nameIsUnique);
				}
			});

			Label typeLabel = new Label(this, SWT.NONE);
			typeLabel.setText(DialogMessages.OBJECTPROPERTIESDIALOG_TYPE);
			data = new GridData();
			typeLabel.setLayoutData(data);

			Label typeValue = new Label(this, SWT.NONE);
			String type = DomainObjectHelper.getObjectType(domainObject);
			typeValue.setText(type);
			data = new GridData(GridData.FILL_HORIZONTAL);
			typeValue.setLayoutData(data);

			Label locationLabel = null;
			Label locationValue = null;
			if (type == ApplicationMessages.DOMAINOBJECTHELPER_POLICY_TYPE) {
				locationLabel = new Label(this, SWT.NONE);
				locationLabel
						.setText(DialogMessages.OBJECTPROPERTIESDIALOG_LOCATION);
				data = new GridData();
				locationLabel.setLayoutData(data);

				locationValue = new Label(this, SWT.NONE);
				data = new GridData(GridData.FILL_HORIZONTAL);
				locationValue.setLayoutData(data);
				locationValue
						.setText(DialogMessages.OBJECTPROPERTIESDIALOG_SLASH
								+ location);
				locationValue.setToolTipText(formatForToolTip(location));

				// remove the priority by bmeng on Mar 14, 2007
				// Label separator1 = new Label(this, SWT.SEPARATOR |
				// SWT.HORIZONTAL);
				// data = new GridData(GridData.FILL_HORIZONTAL);
				// data.horizontalSpan = 2;
				// separator1.setLayoutData(data);
				//
				// priorityLabel = new Label(this, SWT.NONE);
				// priorityLabel.setText(DialogMessages.OBJECTPROPERTIESDIALOG_PRIORITY);
				// data = new GridData();
				// locationLabel.setLayoutData(data);
				//
				// priorityCombo = new Combo(this, SWT.NONE);
				// data = new GridData(GridData.FILL_HORIZONTAL);
				// priorityCombo.setLayoutData(data);
			}

			Label separator2 = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
			data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;
			separator2.setLayoutData(data);

			Label editStatusLabel = new Label(this, SWT.NONE);
			editStatusLabel
					.setText(DialogMessages.OBJECTPROPERTIESDIALOG_STATUS);
			data = new GridData();
			editStatusLabel.setLayoutData(data);

			Label editStatusValue = new Label(this, SWT.SINGLE | SWT.WRAP);
			data = new GridData(GridData.FILL_HORIZONTAL);
			editStatusValue.setLayoutData(data);

			DomainObjectDescriptor descriptor = DomainObjectHelper
					.getCachedDescriptor(domainObject);
			editStatusValue.setText("");
			if (descriptor.getType() != EntityType.FOLDER) {
				try {
					DomainObjectUsage usage = PolicyServerProxy
							.getUsage(descriptor);
					String statusKey = DomainObjectHelper
							.getDeploymentStatusKey(descriptor, usage);
					String statusText = DomainObjectHelper
							.getStatusText(statusKey);
					String deploymentText = DomainObjectHelper
							.getDeploymentText(statusKey);
					editStatusValue.setText(statusText + "\n" + deploymentText);
				} catch (PolicyEditorException exception) {
					LoggingUtil
							.logWarning(
									Activator.ID,
									"Failed to load object usage for selected entity.  Status text will not be accurate in Properties dialog.",
									exception);
				}
			} else {
				editStatusLabel.setVisible(false);
			}
		}

		private String getObjectName() {
			String name = nameText.getText();
			String currentName = DomainObjectHelper.getName(domainObject);
			int folderSeparatorIndex = currentName
					.lastIndexOf(PQLParser.SEPARATOR);
			String location = "";
			if (folderSeparatorIndex >= 0) {
				location = currentName.substring(0, folderSeparatorIndex + 1);
			}

			return location + name;
		}

		/**
		 * Writes changes to the object if any.
		 * 
		 * @return true if save is needed
		 */
		public boolean save() {
			String newName = getObjectName();
			String oldName = DomainObjectHelper.getName(domainObject);
			isRename = !newName.equals(oldName);
			EntityType currentType = DomainObjectHelper.getEntityType(domainObject);
			GlobalState gs = GlobalState.getInstance();
			if (isChanged) {
				if (isRename) {
					if(currentType == EntityType.POLICY){
						changeExceptionName (domainObject, newName);
						changeLowerLevelReference(domainObject, newName);
					}
					DomainObjectHelper.setName(domainObject, newName);			
					if (currentType == EntityType.POLICY && PolicyHelpers.isSubPolicy(domainObject)){
						int index = newName.lastIndexOf(PQLParser.SEPARATOR);
						String topDomainObjectDespName = newName.substring(0, index);
						DomainObjectDescriptor dod = PolicyServerProxy.getDescriptorByName(topDomainObjectDespName);
						IHasId dj = (IHasId) PolicyServerProxy.getEntityForDescriptor(dod);
						gs.closeEditorFor(dj);
						changeUpperLevelReference (domainObject, oldName);
					}
				}
			}
			return isChanged;
		}
			
		public void changeExceptionName (IHasId domainObject, String rootName){
			List <IHasId> treeNodes = changeRefToPolicy(addTreeNodes(domainObject));
			GlobalState gs = GlobalState.getInstance();
			if (treeNodes.size()>0){
				for (IHasId temp : treeNodes ){
					gs.closeEditorFor(temp);
					String oldName = DomainObjectHelper.getName(temp);
					String newName = replaceWithNewName(rootName, oldName);
					changeLowerLevelReference(temp, rootName);
					DomainObjectHelper.setName(temp, newName);
					excuteChangeForDomainObject ( temp, oldName );
					PolicyServerProxy.saveEntity(temp);
				}
			}
		}
		
		public void changeLowerLevelReference(IHasId temp, String rootName){
			IPolicy policy = (IPolicy)temp;
			List<IPolicyReference> exceptionRefs = new ArrayList<IPolicyReference>();
			List<IPolicyReference> exceptionRefsCopy = policy.getPolicyExceptions().getPolicies();
			if(exceptionRefsCopy.size()>0){
				for(IPolicyReference ref: exceptionRefsCopy){
					String oldName = ref.getReferencedName();
					String newName = replaceWithNewName( rootName, oldName);
					PolicyReference reference = new PolicyReference (newName);
					exceptionRefs.add(reference);
				}
				policy.getPolicyExceptions().setPolicies(exceptionRefs);
				PolicyServerProxy.saveEntity(policy);
			}
		}
		
		public void changeUpperLevelReference(IHasId temp, String oldName){
			String newName = DomainObjectHelper.getName(temp);
			int index = newName.lastIndexOf(PQLParser.SEPARATOR);
			String topDomainObjectDespName = newName.substring(0, index);
			
			DomainObjectDescriptor dod = PolicyServerProxy.getDescriptorByName(topDomainObjectDespName);
			IHasId dj = (IHasId) PolicyServerProxy.getEntityForDescriptor(dod);
			IPolicy policy = (IPolicy)dj;
			List<IPolicyReference> exceptionRefs = policy.getPolicyExceptions().getPolicies();
			exceptionRefs.remove(PolicyHelpers.findRefByName(exceptionRefs, oldName));
			exceptionRefs.add(new PolicyReference (newName));
			policy.getPolicyExceptions().setPolicies(exceptionRefs);
			PolicyServerProxy.saveEntity(policy);
		}
		
		public String replaceWithNewName(String rootName, String exceptionName){
			String[] root = rootName.split("/");
			String[] node = exceptionName.split("/");
			int index = root.length-1;
			node[index] = node[index].replace(node[index],root[index]);
			StringBuilder builder = new StringBuilder();

			for (int i=0; i<node.length; i++) {
				builder.append(node[i]);
				if(i!=node.length-1){
					builder.append("/");
				}
			}
			return builder.toString();
		}
		
		public List <IHasId> changeRefToPolicy (List <IPolicyReference> ref){
			List <IHasId> policiesList = new ArrayList <IHasId> ();
			for(IPolicyReference pf: ref){
				DomainObjectDescriptor dod = PolicyServerProxy.getDescriptorByName(pf.getReferencedName());
				IHasId dj = (IHasId) PolicyServerProxy.getEntityForDescriptor(dod);
				policiesList.add(dj);
			}
			return policiesList;
		}
		
		/**
		 * use a recursion here to get the whole tree that underneath the domainobject
		 * 
		 */
		
		public List <IPolicyReference> addTreeNodes(IHasId domainObject){
			IPolicy policy = (IPolicy)domainObject;
			List<IPolicyReference> exceptions = policy.getPolicyExceptions().getPolicies();
			int hasException = exceptions.size();
			if (exceptions.size()==0){
				return new ArrayList<IPolicyReference> ();
			}else{
				List <IPolicyReference> policiesTreeList = new ArrayList<IPolicyReference> ();
				policiesTreeList.addAll(exceptions);
				for(IPolicyReference pf: exceptions){
					DomainObjectDescriptor dod = PolicyServerProxy.getDescriptorByName(pf.getReferencedName());
					IHasId dj = (IHasId) PolicyServerProxy.getEntityForDescriptor(dod);
					policiesTreeList.addAll(addTreeNodes(dj));
				}
				return policiesTreeList;
			}
		}
		
		/**
		 * Returns the isRename.
		 * 
		 * @return the isRename.
		 */
		public boolean isRename() {
			return isRename;
		}
	}

	/**
	 * @author fuad
	 */
	private class AccessControlTab extends Composite {

		private Text ownerText = null;
		private TableViewer groupList = null;
		private Button addButton = null;
		private Button removeButton = null;
		private Button adminCheckbox = null;
		private Button readCheckbox = null;
		private Button writeCheckbox = null;
		private Button submitCheckbox = null;
		private Button deployCheckbox = null;
		private Button deleteCheckbox = null;

		private IHasId domainObject = null;
		private IAccessPolicy accessPolicy = null;
		private Collection<Object> userGroupActions = null;
		private Set<Object> removedUserAndGroups = new HashSet<Object>();
		private boolean isChanged = false;

		private class TableContentProvider implements
				IStructuredContentProvider {

			public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			}

			public void dispose() {
			}

			public Object[] getElements(Object parent) {
				if (userGroupActions != null) {
					Object[] res = userGroupActions.toArray();
					Arrays.sort(res, new Comparator<Object>() {

						public int compare(Object lhs, Object rhs) {
							if (lhs.getClass() != rhs.getClass()) {
								return (lhs instanceof UserAccess) ? 1 : -1;
							} else {
								String lhsName, rhsName;
								if (lhs instanceof UserAccess) {
									lhsName = EntityInfoProvider
											.getLeafObjectByID(
													((UserAccess) lhs)
															.getUserId(),
													LeafObjectType.APPUSER)
											.getName();
									rhsName = EntityInfoProvider
											.getLeafObjectByID(
													((UserAccess) rhs)
															.getUserId(),
													LeafObjectType.APPUSER)
											.getName();
								} else if (lhs instanceof GroupAccess) {
									lhsName = EntityInfoProvider
											.getLeafObjectByID(
													((GroupAccess) lhs)
															.getGroupId(),
													LeafObjectType.ACCESSGROUP)
											.getName();
									rhsName = EntityInfoProvider
											.getLeafObjectByID(
													((GroupAccess) rhs)
															.getGroupId(),
													LeafObjectType.ACCESSGROUP)
											.getName();
								} else {
									return 0;
								}
								return lhsName.compareToIgnoreCase(rhsName);
							}
						}
					});
					return res;
				}

				return new Object[0];
			}
		}

		private class TableLabelProvider extends LabelProvider implements
				ITableLabelProvider {

			public String getColumnText(Object obj, int index) {
				if (obj instanceof GroupAccess) {
					GroupAccess groupAccess = (GroupAccess) obj;
					assert groupAccess.getGroupId() != null;
					return EntityInfoProvider.getLeafObjectByID(
							groupAccess.getGroupId(),
							LeafObjectType.ACCESSGROUP).getName();
				} else if (obj instanceof UserAccess) {
					UserAccess userAccess = (UserAccess) obj;
					assert userAccess.getUserId() != null;
					return EntityInfoProvider.getLeafObjectByID(
							userAccess.getUserId(), LeafObjectType.APPUSER)
							.getName();
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

		/**
		 * Constructor
		 * 
		 * @param parent
		 * @param style
		 */
		public AccessControlTab(Composite parent, int style, IHasId domainObject) {
			super(parent, style);
			this.domainObject = domainObject;
			accessPolicy = DomainObjectHelper.getAccessPolicy(domainObject);
			if (accessPolicy != null) {
				userGroupActions = new HashSet<Object>(accessPolicy
						.getAllUserGroupActions());
			}
			initialize();
		}

		/**
		 * Saves changes to the access policy if it is changed
		 * 
		 * @return true if save is required
		 */
		public boolean save() {
			if (isChanged) {
				for (Object userOrGroup : removedUserAndGroups) {
					try {
						if (userOrGroup instanceof GroupAccess) {
							accessPolicy.setActionsForGroup(
									((GroupAccess) userOrGroup).getGroupId(),
									null);
						} else if (userOrGroup instanceof UserAccess) {
							accessPolicy.setActionsForUser(
									((UserAccess) userOrGroup).getUserId(),
									null);
						}
					} catch (PQLException e) {
						// show error. should not throw an exception.
					}
				}

				for (Object userOrGroup : userGroupActions) {
					if (userOrGroup instanceof GroupAccess) {
						GroupAccess groupAccess = (GroupAccess) userOrGroup;
						try {
							accessPolicy.setActionsForGroup(groupAccess
									.getGroupId(), groupAccess.getActions());
						} catch (PQLException e) {
							// show error. why is there an exception here?
						}
					} else if (userOrGroup instanceof UserAccess) {
						UserAccess userAccess = (UserAccess) userOrGroup;
						try {
							accessPolicy.setActionsForUser(userAccess
									.getUserId(), userAccess.getActions());
						} catch (PQLException e) {
							// show error. why is there an exception here?
						}
					}

				}
			}
			return isChanged;
		}

		private void updateReadCheckboxStatus(Object obj) {
			boolean otherRightsExist = adminCheckbox.getSelection()
					|| writeCheckbox.getSelection()
					|| submitCheckbox.getSelection()
					|| deployCheckbox.getSelection()
					|| deleteCheckbox.getSelection();

			if (otherRightsExist && !readCheckbox.getSelection()) {
				addActionToUserOrGroup(obj, DAction.READ);
				readCheckbox.setSelection(allowAdminActions);
			}
			readCheckbox.setEnabled(!otherRightsExist && allowAdminActions);
		}

		/**
		 * create and initialize all controls
		 */
		public void initialize() {
			GridLayout layout = new GridLayout(2, false);
			setLayout(layout);

			Label ownerLabel = new Label(this, SWT.NONE);
			ownerLabel.setText(DialogMessages.OBJECTPROPERTIESDIALOG_OWNER);
			GridData data = new GridData();
			ownerLabel.setLayoutData(data);

			ownerText = new Text(this, SWT.SINGLE | SWT.BORDER);
			Long ownerId = DomainObjectHelper.getOwnerId(domainObject);
			LeafObject ownerLeaf = EntityInfoProvider.getLeafObjectByID(
					ownerId, LeafObjectType.APPUSER);
			if (ownerLeaf == null) {
				ownerLeaf = new LeafObject(LeafObjectType.USER);
				ownerLeaf
						.setName(DialogMessages.OBJECTPROPERTIESDIALOG_UNKNOWN_OR_DELETED);
			}
			ownerText.setText(ownerLeaf.getName());
			ownerText.setEditable(false);
			data = new GridData(GridData.FILL_HORIZONTAL);
			ownerText.setLayoutData(data);

			Group group = new Group(this, SWT.SHADOW_ETCHED_IN);
			group.setText(DialogMessages.OBJECTPROPERTIESDIALOG_ACCESS_RIGHTS);
			data = new GridData(GridData.FILL_BOTH);
			data.horizontalSpan = 2;
			group.setLayoutData(data);
			layout = new GridLayout();
			group.setLayout(layout);

			Label groupLabel = new Label(group, SWT.NONE);
			groupLabel
					.setText(DialogMessages.OBJECTPROPERTIESDIALOG_GROUP_OR_USER_NAMES);
			data = new GridData(GridData.FILL_HORIZONTAL);
			groupLabel.setLayoutData(data);

			groupList = new TableViewer(group, SWT.BORDER | SWT.SINGLE
					| SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
			Table table = groupList.getTable();
			TableColumn column = new TableColumn(table, SWT.LEFT);
			column.setWidth(300);
			data = new GridData(GridData.FILL_BOTH);
			table.setLayoutData(data);

			groupList.setContentProvider(new TableContentProvider());
			groupList.setLabelProvider(new TableLabelProvider());
			if (PolicyServerProxy.canPerformAction(domainObject, DAction.READ)) {
				groupList.setInput(userGroupActions);
			} else {
				groupList.setInput(null);
			}
			updateTableFonts();

			groupList
					.addSelectionChangedListener(new ISelectionChangedListener() {

						public void selectionChanged(SelectionChangedEvent event) {
							applyGroupOrUserSelection();
						}
					});

			Composite buttonBar = new Composite(group, SWT.NONE);
			data = new GridData(GridData.HORIZONTAL_ALIGN_END);
			buttonBar.setLayoutData(data);
			layout = new GridLayout(3, false);
			buttonBar.setLayout(layout);

			addButton = new Button(buttonBar, SWT.PUSH);
			addButton.setEnabled(allowAdminActions);
			addButton.setText(DialogMessages.OBJECTPROPERTIESDIALOG_ADD);
			setButtonLayoutData(addButton);
			addButton.addSelectionListener(new SelectionAdapter() {
				@SuppressWarnings("unchecked")
				@Override
				public void widgetSelected(SelectionEvent e) {
					ILeafObjectBrowser leafObjectBrowser = leafObjectBrowserFactoy
							.getLeafObjectBrowser(SpecType.APPUSER, getShell());
					if (leafObjectBrowser.open() == Dialog.CANCEL) {
						return;
					}
					List users = leafObjectBrowser.getItemsToReturn();
					for (Iterator i = users.iterator(); i.hasNext();) {
						LeafObject leaf = (LeafObject) i.next();
						Long leafId = leaf.getId();

						if (leaf.getType() == LeafObjectType.APPUSER) {
							Collection<IAction> actions = accessPolicy
									.getActionsForUser(leafId);
							if (actions == null || actions.size() == 0) {
								actions = new ArrayList<IAction>();
								actions.add(DAction.READ);
							}
							userGroupActions
									.add(new UserAccess(leafId, actions));
							removedUserAndGroups.remove(new UserAccess(leafId,
									actions));
						} else if (leaf.getType() == LeafObjectType.ACCESSGROUP) {
							Collection<IAction> actions = accessPolicy
									.getActionsForGroup(leafId);
							if (actions == null || actions.size() == 0) {
								actions = new ArrayList<IAction>();
								actions.add(DAction.READ);
							}
							userGroupActions.add(new GroupAccess(leafId,
									actions));
							removedUserAndGroups.remove(new GroupAccess(leafId,
									actions));
						}
					}
					isChanged = true;
					refreshGroupList();
				}

			});
			addButton.setEnabled(allowAdminActions);

			removeButton = new Button(buttonBar, SWT.PUSH);
			removeButton.setEnabled(allowAdminActions);
			removeButton.setText(DialogMessages.OBJECTPROPERTIESDIALOG_REMOVE);
			setButtonLayoutData(removeButton);
			removeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection selection = (IStructuredSelection) groupList
							.getSelection();
					Object obj = selection.getFirstElement();

					if (obj != null) {
						userGroupActions.remove(obj);
						removedUserAndGroups.add(obj);
						isChanged = true;
						refreshGroupList();
					}
				}
			});

			removeButton.setEnabled(allowAdminActions);

			Label permissionsLabel = new Label(group, SWT.NONE);
			String displayName = DomainObjectHelper
					.getDisplayName(domainObject);
			permissionsLabel
					.setText(DialogMessages.OBJECTPROPERTIESDIALOG_PERMISSION_FOR
							+ displayName);
			permissionsLabel.setToolTipText(formatForToolTip(displayName));
			data = new GridData(GridData.FILL_HORIZONTAL);
			permissionsLabel.setLayoutData(data);

			adminCheckbox = new Button(group, SWT.CHECK);
			adminCheckbox
					.setText(DialogMessages.OBJECTPROPERTIESDIALOG_SET_ACCESS);
			data = new GridData(GridData.FILL_HORIZONTAL);
			adminCheckbox.setLayoutData(data);
			adminCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection selection = (IStructuredSelection) groupList
							.getSelection();
					Object obj = selection.getFirstElement();
					if (adminCheckbox.getSelection()) {
						addActionToUserOrGroup(obj, DAction.ADMIN);
					} else {
						removeActionFromUserOrGroup(obj, DAction.ADMIN);
					}
					updateReadCheckboxStatus(obj);
				}
			});
			adminCheckbox.setEnabled(allowAdminActions);

			readCheckbox = new Button(group, SWT.CHECK);
			readCheckbox.setText(DialogMessages.OBJECTPROPERTIESDIALOG_READ);
			data = new GridData(GridData.FILL_HORIZONTAL);
			readCheckbox.setLayoutData(data);
			readCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection selection = (IStructuredSelection) groupList
							.getSelection();
					Object obj = selection.getFirstElement();
					if (readCheckbox.getSelection()) {
						addActionToUserOrGroup(obj, DAction.READ);
					} else {
						removeActionFromUserOrGroup(obj, DAction.READ);
					}
				}
			});
			readCheckbox.setEnabled(allowAdminActions);

			writeCheckbox = new Button(group, SWT.CHECK);
			writeCheckbox.setText(DialogMessages.OBJECTPROPERTIESDIALOG_WRITE);
			data = new GridData(GridData.FILL_HORIZONTAL);
			writeCheckbox.setLayoutData(data);
			writeCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection selection = (IStructuredSelection) groupList
							.getSelection();
					Object obj = selection.getFirstElement();
					if (writeCheckbox.getSelection()) {
						addActionToUserOrGroup(obj, DAction.WRITE);
					} else {
						removeActionFromUserOrGroup(obj, DAction.WRITE);
					}
					updateReadCheckboxStatus(obj);
				}

			});
			writeCheckbox.setEnabled(allowAdminActions);

			submitCheckbox = new Button(group, SWT.CHECK);
			submitCheckbox
					.setText(DialogMessages.OBJECTPROPERTIESDIALOG_SUBMIT);
			data = new GridData(GridData.FILL_HORIZONTAL);
			submitCheckbox.setLayoutData(data);
			submitCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection selection = (IStructuredSelection) groupList
							.getSelection();
					Object obj = selection.getFirstElement();
					if (submitCheckbox.getSelection()) {
						addActionToUserOrGroup(obj, DAction.APPROVE);
					} else {
						removeActionFromUserOrGroup(obj, DAction.APPROVE);
					}
					updateReadCheckboxStatus(obj);
				}

			});
			submitCheckbox.setEnabled(allowAdminActions);

			deployCheckbox = new Button(group, SWT.CHECK);
			deployCheckbox
					.setText(DialogMessages.OBJECTPROPERTIESDIALOG_DEPLOY);
			data = new GridData(GridData.FILL_HORIZONTAL);
			deployCheckbox.setLayoutData(data);
			deployCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection selection = (IStructuredSelection) groupList
							.getSelection();
					Object obj = selection.getFirstElement();
					if (deployCheckbox.getSelection()) {
						addActionToUserOrGroup(obj, DAction.DEPLOY);
					} else {
						removeActionFromUserOrGroup(obj, DAction.DEPLOY);
					}
					updateReadCheckboxStatus(obj);
				}

			});
			deployCheckbox.setEnabled(allowAdminActions);

			deleteCheckbox = new Button(group, SWT.CHECK);
			deleteCheckbox
					.setText(DialogMessages.OBJECTPROPERTIESDIALOG_DELETE);
			data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;
			deleteCheckbox.setLayoutData(data);
			deleteCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection selection = (IStructuredSelection) groupList
							.getSelection();
					Object obj = selection.getFirstElement();
					if (deleteCheckbox.getSelection()) {
						addActionToUserOrGroup(obj, DAction.DELETE);
					} else {
						removeActionFromUserOrGroup(obj, DAction.DELETE);
					}
					updateReadCheckboxStatus(obj);
				}
			});
			deleteCheckbox.setEnabled(allowAdminActions);

			// select first element after adding selection changed listener
			table.select(0);
			applyGroupOrUserSelection();
		}

		/**
		 * @param obj
		 */
		protected void selectUserOrGroup(Object obj) {
			adminCheckbox.setSelection(false);
			readCheckbox.setSelection(false);
			writeCheckbox.setSelection(false);
			submitCheckbox.setSelection(false);
			deployCheckbox.setSelection(false);
			deleteCheckbox.setSelection(false);

			if (obj == null) {
				return;
			}

			Collection<IAction> actions = null;
			if (obj instanceof GroupAccess) {
				removeButton.setEnabled(allowAdminActions);

				GroupAccess groupAccess = (GroupAccess) obj;
				actions = groupAccess.getActions();
			} else if (obj instanceof UserAccess) {
				removeButton.setEnabled(allowAdminActions);

				UserAccess userAccess = (UserAccess) obj;
				actions = userAccess.getActions();
			}

			if (actions != null) {
				for (IAction action : actions) {
					if (action == DAction.ADMIN) {
						adminCheckbox.setSelection(true);
					} else if (action == DAction.READ) {
						readCheckbox.setSelection(true);
					} else if (action == DAction.WRITE) {
						writeCheckbox.setSelection(true);
					} else if (action == DAction.APPROVE) {
						submitCheckbox.setSelection(true);
					} else if (action == DAction.DEPLOY) {
						deployCheckbox.setSelection(true);
					} else if (action == DAction.DELETE) {
						deleteCheckbox.setSelection(true);
					}
				}
			}
			updateReadCheckboxStatus(obj);
		}

		/**
		 * @param obj
		 */
		private void addActionToUserOrGroup(Object obj, IDAction action) {
			if (obj instanceof GroupAccess) {
				GroupAccess groupAccess = (GroupAccess) obj;
				groupAccess.getActions().add(action);
			} else if (obj instanceof UserAccess) {
				UserAccess userAccess = (UserAccess) obj;
				userAccess.getActions().add(action);
			}
			isChanged = true;
		}

		/**
		 * @param obj
		 * @param x
		 */
		private void removeActionFromUserOrGroup(Object obj, IDAction action) {
			if (obj instanceof GroupAccess) {
				GroupAccess groupAccess = (GroupAccess) obj;
				groupAccess.getActions().remove(action);
			} else if (obj instanceof UserAccess) {
				UserAccess userAccess = (UserAccess) obj;
				userAccess.getActions().remove(action);
			}
			isChanged = true;
		}

		// private Object getUserOrRole(Object roleOrUserAccess) {
		// if (groupOrUserAccess instanceof GroupAccess) {
		// return ((RoleAccess) roleOrUserAccess).getRole();
		// } else if (roleOrUserAccess instanceof UserAccess) {
		// return ((UserAccess) roleOrUserAccess).getUniqueName();
		// }
		// return null;
		// }

		/**
		 * refresh the group/user list.
		 */
		private void refreshGroupList() {
			groupList.refresh();
			updateTableFonts();

		}

		/**
		 * Enumerates the group list and sets appopriate fonts based on type of
		 * objects
		 */
		private void updateTableFonts() {
			Table table = groupList.getTable();
			TableItem[] items = table.getItems();

			for (int i = 0; i < items.length; i++) {
				TableItem item = items[i];
				Object obj = item.getData();
				if (obj instanceof UserAccess) {
					item.setFont(FontBundle.DEFAULT_UNDERLINE);
				} else {
					item.setFont(FontBundle.DEFAULT_NORMAL);
				}
			}
		}

		/**
		 * 
		 */
		private void applyGroupOrUserSelection() {
			IStructuredSelection selection = (IStructuredSelection) groupList
					.getSelection();
			Object obj = selection.getFirstElement();
			if (obj != null) {
				adminCheckbox.setEnabled(allowAdminActions);
				readCheckbox.setEnabled(allowAdminActions);
				writeCheckbox.setEnabled(allowAdminActions);
				submitCheckbox.setEnabled(allowAdminActions);
				deployCheckbox.setEnabled(allowAdminActions);
				deleteCheckbox.setEnabled(allowAdminActions);
				selectUserOrGroup(obj);
				removeButton.setEnabled(allowAdminActions);
			} else {
				adminCheckbox.setSelection(false);
				adminCheckbox.setEnabled(false);
				readCheckbox.setSelection(false);
				readCheckbox.setEnabled(false);
				writeCheckbox.setSelection(false);
				writeCheckbox.setEnabled(false);
				submitCheckbox.setSelection(false);
				submitCheckbox.setEnabled(false);
				deployCheckbox.setSelection(false);
				deployCheckbox.setEnabled(false);
				deleteCheckbox.setSelection(false);
				deleteCheckbox.setEnabled(false);
				removeButton.setEnabled(false);
			}
		}

	}

	private static String formatForToolTip(String str) {
		StringBuffer res = new StringBuffer();
		String indent = "";
		boolean seenSeparator = false;
		for (int i = 0; i != str.length(); i++) {
			if (str.charAt(i) == PQLParser.SEPARATOR) {
				if (!seenSeparator && res.length() != 0) {
					// Ignore doubled and initial separators
					res.append('\n');
					res.append(indent);
					indent += " ";
					seenSeparator = true;
				}
			} else {
				if (seenSeparator) {
					seenSeparator = false;
					res.append("+");
				}
				res.append(str.charAt(i));
			}
		}
		if (seenSeparator) {
			res.delete(res.lastIndexOf("\n"), res.length());
		}
		return res.toString();
	}
}
