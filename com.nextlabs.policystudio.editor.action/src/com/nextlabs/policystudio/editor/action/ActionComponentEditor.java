/*
 * Created on May 31, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.policystudio.editor.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.bluejungle.destiny.policymanager.editor.BaseComponentEditor;
import com.bluejungle.destiny.policymanager.editor.EditorMessages;
import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.model.IClientComponent;
import com.bluejungle.destiny.policymanager.model.IClientEditorPanel;
import com.bluejungle.destiny.policymanager.ui.ActionComponentUndoElement;
import com.bluejungle.destiny.policymanager.ui.ActionComponentUndoElementOp;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.util.PluginUtil;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.PolicyActionsDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.epicenter.action.IAction;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author Bo Meng
 */
public class ActionComponentEditor extends BaseComponentEditor {
	private List<PolicyActionsDescriptor> listBasicActions = new ArrayList<PolicyActionsDescriptor>();
	private List<PolicyActionsDescriptor> listSelectedActions = new ArrayList<PolicyActionsDescriptor>();

	private TreeViewer basicActionsTreeViewer;
	private TableViewer selectedActionsTableViewer;
	private Button buttonAdd, buttonRemove;
	private CompositePredicate actionDomainObj;
	private PolicyActionsDescriptor targetDescriptor;

	private class BasicActionsTreeContentProvider implements
			ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if (parentElement.equals("root")) {
				Set<String> categories = new HashSet<String>();
				for (int i = 0, n = listBasicActions.size(); i < n; i++) {
					PolicyActionsDescriptor descriptor = listBasicActions
							.get(i);
					categories.add(descriptor.getCategory());
				}

				String[] result = categories.toArray(new String[categories
						.size()]);
				Arrays.sort(result);
				return result;
			}
			if (parentElement instanceof String) {
				List<PolicyActionsDescriptor> items = new ArrayList<PolicyActionsDescriptor>();
				for (int i = 0, n = listBasicActions.size(); i < n; i++) {
					PolicyActionsDescriptor descriptor = listBasicActions
							.get(i);
					if (descriptor.getCategory().equals(parentElement)) {
						items.add(descriptor);
					}
				}

				sortDescriptorsList(items);
				PolicyActionsDescriptor[] result = items
						.toArray(new PolicyActionsDescriptor[items.size()]);
				return result;
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element.equals("root")) {
				return listBasicActions.size() > 0;
			}
			if (element instanceof String) {
				List<PolicyActionsDescriptor> items = new ArrayList<PolicyActionsDescriptor>();
				for (int i = 0, n = listBasicActions.size(); i < n; i++) {
					PolicyActionsDescriptor descriptor = listBasicActions
							.get(i);
					if (descriptor.getCategory().equals(element)) {
						items.add(descriptor);
					}
				}
				return items.size() > 0;
			}
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

	private class BasicActionsTreeLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			if (element instanceof String) {
				return ImageBundle.FOLDER_OPEN_IMG;
			} else {
				return PluginUtil.getContextImageForContext("ACTION");
			}
		}

		@Override
		public String getText(Object element) {
			if (element instanceof String) {
				String name = (String) element;
				return name;
			}
			if (element instanceof PolicyActionsDescriptor) {
				PolicyActionsDescriptor descriptor = (PolicyActionsDescriptor) element;
				return descriptor.getDisplayName();
			}
			return null;
		}
	}

	private class SelectedActionsTableContentProvider implements
			IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			if (inputElement.equals("root")) {
				PolicyActionsDescriptor[] result = listSelectedActions
						.toArray(new PolicyActionsDescriptor[listSelectedActions
								.size()]);
				Arrays.sort(result, new Comparator<PolicyActionsDescriptor>() {

					public int compare(PolicyActionsDescriptor o1,
							PolicyActionsDescriptor o2) {
						return o1.getDisplayName().compareTo(
								o2.getDisplayName());
					}
				});
				return result;
			}
			return new Object[0];
		}

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}
	}

	private class SelectedActionsLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			return PluginUtil.getContextImageForContext("ACTION");
		}

		@Override
		public String getText(Object element) {
			PolicyActionsDescriptor descriptor = (PolicyActionsDescriptor) element;
			return descriptor.getDisplayName();
		}
	}

	/**
	 * @param parent
	 * @param style
	 * @param domainObject
	 * @param showPropertyExpressions
	 */
	public ActionComponentEditor(IClientEditorPanel panel,
			final IClientComponent component) {
		super(panel, component);
		setShowPropertyExpressions(false);

		final ObjectModifiedListner objectModifiedListner = new ObjectModifiedListner();

		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		final IEventManager eventManager = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
		eventManager.registerListener(objectModifiedListner,
				ContextualEventType.PREDICATE_MODIFIED_EVENT, component
						.getComponent());

		panel.getComposite().addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				eventManager.unregisterListener(objectModifiedListner,
						ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT,
						component);
			}
		});
	}

	@Override
	public void updateFromDomainObject() {
		readActionsFromDomainObject();
	}

	@Override
	protected void initializeMembers() {
		GridLayout layout = new GridLayout(3, false);
		membersComposite.setLayout(layout);

		initializeActions(membersComposite);
		readActionsFromDomainObject();
	}

	private void addToActionSet(IDAction action) {
		Set<IDAction> actions = new HashSet<IDAction>();
		actions.add(action);
		Set<IDAction> actionSet = PredicateHelpers
				.getActionSet(actionDomainObj);
		ActionComponentUndoElement undo = new ActionComponentUndoElement();
		actionSet.addAll(actions);
		PredicateHelpers.updateActionSet(actionDomainObj, actionSet);
		undo.setOp(ActionComponentUndoElementOp.ADD_ACTION);
		undo.setNewValue(actions.toArray(new IPredicate[actions.size()]));
		GlobalState.getInstance().addUndoElement(undo);
	}

	private void removeFromActionSet(IDAction action) {
		Set<IDAction> actions = new HashSet<IDAction>();
		actions.add(action);
		Set<IDAction> actionSet = PredicateHelpers
				.getActionSet(actionDomainObj);
		ActionComponentUndoElement undo = new ActionComponentUndoElement();
		actionSet.remove(action);
		PredicateHelpers.updateActionSet(actionDomainObj, actionSet);
		undo.setOp(ActionComponentUndoElementOp.REMOVE_ACTION);
		undo.setOldValue(actions.toArray(new IPredicate[actions.size()]));
	}

	@Override
	protected void relayoutMembers() {
		readActionsFromDomainObject();
	}

	private void initializeActions(Composite actionSection) {
		Color background = panel.getBackground();
		actionSection.setBackground(background);

		Label label = new Label(actionSection, SWT.NONE);
		label.setEnabled(panel.isEditable());
		label.setBackground(background);
		label.setText(EditorMessages.ACTIONCOMPONENTEDITOR_BASIC_ACTIONS);

		new Label(actionSection, SWT.NONE);

		label = new Label(actionSection, SWT.NONE);
		label.setEnabled(panel.isEditable());
		label.setBackground(background);
		label.setText(EditorMessages.ACTIONCOMPONENTEDITOR_SELECTED_ACTIONS);

		basicActionsTreeViewer = new TreeViewer(actionSection, SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);
		basicActionsTreeViewer
				.setContentProvider(new BasicActionsTreeContentProvider());
		basicActionsTreeViewer
				.setLabelProvider(new BasicActionsTreeLabelProvider());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 100;
		data.heightHint = 500;
		basicActionsTreeViewer.getTree().setLayoutData(data);
		basicActionsTreeViewer.getTree().setEnabled(panel.isEditable());

		basicActionsTreeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						updateButtonStatus();
					}
				});

		Composite composite = new Composite(actionSection, SWT.NONE);
		composite.setBackground(background);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		buttonAdd = new Button(composite, SWT.PUSH);
		buttonAdd.setEnabled(false);
		buttonAdd.setText(EditorMessages.ACTIONCOMPONENTEDITOR_ADD);
		buttonAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) basicActionsTreeViewer
						.getSelection();
				Object element = selection.getFirstElement();
				if (element instanceof String) {
					String category = (String) element;
					for (int i = listBasicActions.size() - 1; i >= 0; i--) {
						PolicyActionsDescriptor descriptor = listBasicActions
								.get(i);
						if (descriptor.getCategory().equals(category)) {
							if (isValidOperation(descriptor)) {
								listSelectedActions.add(descriptor);
								listBasicActions.remove(descriptor);
								addToActionSet((IDAction) descriptor
										.getAction());
								sortDescriptorsList(listBasicActions);
							} else {
								basicActionsTreeViewer.refresh();
								selectedActionsTableViewer.refresh();
								updateButtonStatus();
								return;
							}
						}
					}
				} else if (element instanceof PolicyActionsDescriptor) {
					PolicyActionsDescriptor descriptor = (PolicyActionsDescriptor) element;
					if (isValidOperation(descriptor)) {
						listSelectedActions.add(descriptor);
						listBasicActions.remove(descriptor);
						addToActionSet((IDAction) descriptor.getAction());
						sortDescriptorsList(listBasicActions);
					}
				}
				basicActionsTreeViewer.refresh();
				selectedActionsTableViewer.refresh();
				updateButtonStatus();
			}
		});

		new Label(composite, SWT.NONE);

		buttonRemove = new Button(composite, SWT.PUSH);
		buttonRemove.setEnabled(false);
		buttonRemove.setText(EditorMessages.ACTIONCOMPONENTEDITOR_REMOVE);
		buttonRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) selectedActionsTableViewer
						.getSelection();
				Object element = selection.getFirstElement();
				if (element instanceof PolicyActionsDescriptor) {
					PolicyActionsDescriptor descriptor = (PolicyActionsDescriptor) element;
					listSelectedActions.remove(descriptor);
					listBasicActions.add(descriptor);
					removeFromActionSet((IDAction) descriptor.getAction());
					sortDescriptorsList(listBasicActions);
				}
				basicActionsTreeViewer.refresh();
				selectedActionsTableViewer.refresh();
				basicActionsTreeViewer.expandAll();
				updateButtonStatus();
			}
		});

		selectedActionsTableViewer = new TableViewer(actionSection, SWT.BORDER);
		selectedActionsTableViewer
				.setContentProvider(new SelectedActionsTableContentProvider());
		selectedActionsTableViewer
				.setLabelProvider(new SelectedActionsLabelProvider());
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 100;
		selectedActionsTableViewer.getTable().setEnabled(panel.isEditable());
		selectedActionsTableViewer.getTable().setLayoutData(data);

		selectedActionsTableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						updateButtonStatus();
					}
				});
	}

	/**
	 * Verify if the descriptor can be added to the list
	 * 
	 * @param descriptor
	 * @return true when the descriptor can co-exist with the existing ones
	 */
	private boolean isValidOperation(PolicyActionsDescriptor descriptor) {
		IAction action = descriptor.getAction();
		if (action.equals(DAction.OPEN)) {
			if (isContained(DAction.RUN)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.DELETE)) {
			if (isContained(DAction.RUN)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.MOVE)) {
			if (isContained(DAction.RUN)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.RENAME)) {
			if (isContained(DAction.RUN)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.RUN)) {
			if (isContained(DAction.OPEN) || isContained(DAction.DELETE)
					|| isContained(DAction.MOVE) || isContained(DAction.RENAME)
					|| isContained(DAction.CHANGE_PROPERTIES)
					|| isContained(DAction.CHANGE_SECURITY)
					|| isContained(DAction.CREATE_NEW)
					|| isContained(DAction.COPY)
					|| isContained(DAction.COPY_PASTE)
					|| isContained(DAction.PRINT)
					|| isContained(DAction.EXPORT)
					|| isContained(DAction.ATTACH)
					|| isContained(DAction.EMAIL) || isContained(DAction.IM)
					|| isContained(DAction.MEETING) || isContained(DAction.AVD)
					|| isContained(DAction.SHARE)
					|| isContained(DAction.RECORD)
					|| isContained(DAction.QUESTION)
					|| isContained(DAction.VOICE) || isContained(DAction.VIDEO)
					|| isContained(DAction.JOIN)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.CHANGE_PROPERTIES)) {
			if (isContained(DAction.RUN) || isContained(DAction.EXPORT)
					|| isContained(DAction.ATTACH)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.CHANGE_SECURITY)) {
			if (isContained(DAction.RUN) || isContained(DAction.EXPORT)
					|| isContained(DAction.ATTACH)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.EDIT)) {
			if (isContained(DAction.RUN)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.COPY)) {
			if (isContained(DAction.RUN)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.COPY_PASTE)) {
			if (isContained(DAction.RUN) || isContained(DAction.EXPORT)
					|| isContained(DAction.ATTACH)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.PRINT)) {
			if (isContained(DAction.RUN)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.EXPORT)) {
			if (isContained(DAction.RUN)
					|| isContained(DAction.CHANGE_PROPERTIES)
					|| isContained(DAction.CHANGE_SECURITY)
					|| isContained(DAction.COPY_PASTE)
					|| isContained(DAction.EMAIL) || isContained(DAction.IM)
					|| isContained(DAction.MEETING) || isContained(DAction.AVD)
					|| isContained(DAction.SHARE)
					|| isContained(DAction.RECORD)
					|| isContained(DAction.QUESTION)
					|| isContained(DAction.VOICE) || isContained(DAction.VIDEO)
					|| isContained(DAction.JOIN)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.ATTACH)) {
			if (isContained(DAction.RUN)
					|| isContained(DAction.CHANGE_PROPERTIES)
					|| isContained(DAction.CHANGE_SECURITY)
					|| isContained(DAction.COPY_PASTE)
					|| isContained(DAction.EMAIL) || isContained(DAction.IM)
					|| isContained(DAction.MEETING) || isContained(DAction.AVD)
					|| isContained(DAction.SHARE)
					|| isContained(DAction.RECORD)
					|| isContained(DAction.QUESTION)
					|| isContained(DAction.VOICE) || isContained(DAction.VIDEO)
					|| isContained(DAction.JOIN)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.EMAIL)) {
			if (isContained(DAction.RUN) || isContained(DAction.EXPORT)
					|| isContained(DAction.ATTACH)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.IM)) {
			if (isContained(DAction.RUN) || isContained(DAction.EXPORT)
					|| isContained(DAction.ATTACH)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.MEETING)) {
			if (isContained(DAction.RUN) || isContained(DAction.EXPORT)
					|| isContained(DAction.ATTACH)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.AVD)) {
			if (isContained(DAction.RUN) || isContained(DAction.EXPORT)
					|| isContained(DAction.ATTACH)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.SHARE)) {
			if (isContained(DAction.RUN) || isContained(DAction.EXPORT)
					|| isContained(DAction.ATTACH)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.RECORD)) {
			if (isContained(DAction.RUN) || isContained(DAction.EXPORT)
					|| isContained(DAction.ATTACH)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.QUESTION)) {
			if (isContained(DAction.RUN) || isContained(DAction.EXPORT)
					|| isContained(DAction.ATTACH)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.VOICE)) {
			if (isContained(DAction.RUN) || isContained(DAction.EXPORT)
					|| isContained(DAction.ATTACH)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.VIDEO)) {
			if (isContained(DAction.RUN) || isContained(DAction.EXPORT)
					|| isContained(DAction.ATTACH)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		} else if (action.equals(DAction.JOIN)) {
			if (isContained(DAction.RUN) || isContained(DAction.EXPORT)
					|| isContained(DAction.ATTACH)) {
				showErrorDialog(descriptor, targetDescriptor);
				return false;
			}
		}
		return true;
	}

	private void showErrorDialog(PolicyActionsDescriptor source,
			PolicyActionsDescriptor target) {
		String message = NLS.bind(
				EditorMessages.ACTIONCOMPONENTEDITOR_ERROR_MSG, source
						.getDisplayName(), targetDescriptor.getDisplayName());
		MessageDialog.openError(Display.getCurrent().getActiveShell(),
				EditorMessages.ACTIONCOMPONENTEDITOR_ERROR, message);
	}

	private boolean isContained(IAction action) {
		targetDescriptor = null;
		for (PolicyActionsDescriptor descriptor : listSelectedActions) {
			IAction actions = descriptor.getAction();
			if (actions.equals(action)) {
				targetDescriptor = descriptor;
				return true;
			}
		}
		return false;
	}

	private void updateButtonStatus() {
		IStructuredSelection selection = (IStructuredSelection) basicActionsTreeViewer
				.getSelection();
		if (selection.isEmpty()) {
			buttonAdd.setEnabled(false);
		} else {
			buttonAdd.setEnabled(true);
		}

		selection = (IStructuredSelection) selectedActionsTableViewer
				.getSelection();
		if (selection.isEmpty()) {
			buttonRemove.setEnabled(false);
		} else {
			buttonRemove.setEnabled(true);
		}
	}

	private void readActionsFromDomainObject() {
		actionDomainObj = getControlDomainObject(CONTROL_ID_COMPOSITION,
				component);
		Set<IDAction> initialState = PredicateHelpers
				.getActionSet(actionDomainObj);

		listBasicActions = new ArrayList<PolicyActionsDescriptor>();
		listSelectedActions = new ArrayList<PolicyActionsDescriptor>();
		try {
			listBasicActions = (List<PolicyActionsDescriptor>) PolicyServerProxy
					.getAllPolicyActions();
		} catch (PolicyEditorException e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					EditorMessages.ACTIONCOMPONENTEDITOR_ERROR,
					EditorMessages.ACTIONCOMPONENTEDITOR_ERROR_RETRIEVE);
		}
		sortDescriptorsList(listBasicActions);
		for (IDAction action : initialState) {
			for (PolicyActionsDescriptor descriptor : listBasicActions) {
				if (descriptor.getAction().equals(action)) {
					listBasicActions.remove(descriptor);
					listSelectedActions.add(descriptor);
					break;
				}
			}
		}

		basicActionsTreeViewer.setInput("root");
		basicActionsTreeViewer.expandAll();
		selectedActionsTableViewer.setInput("root");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bluejungle.destiny.policymanager.editor.ComponentEditor#getSpecType()
	 */
	@Override
	protected SpecType getSpecType() {
		return SpecType.ACTION;
	}

	@Override
	public CompositePredicate getControlDomainObject(int controlId,
			IHasId domainObject) {
		return (CompositePredicate) ((IDSpec) domainObject).getPredicate();
	}

	@Override
	protected String getMemberLabel() {
		return EditorMessages.ACTIONCOMPONENTEDITOR_ACTION_COMPONENT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.bluejungle.destiny.policymanager.editor.EditorPanel#
	 * getObjectTypeLabelText()
	 */
	public String getObjectTypeLabelText() {
		return EditorMessages.ACTIONCOMPONENTEDITOR_ACTION_COMPONENT;
	}

	// -------------------------
	// Irrelevant Methods
	// -------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bluejungle.destiny.policymanager.editor.EditorPanel#getEntityType()
	 */
	@SuppressWarnings("deprecation")
	public EntityType getEntityType() {
		return EntityType.ACTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.bluejungle.destiny.policymanager.editor.ComponentEditor#
	 * getPropertyOperatorList()
	 */
	@Override
	protected List<String> getPropertyOperatorList() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bluejungle.destiny.policymanager.editor.ComponentEditor#getPropertyList
	 * ()
	 */
	@Override
	protected List<String> getPropertyList() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bluejungle.destiny.policymanager.editor.ComponentEditor#setupPreviewTable
	 * (org.eclipse.swt.widgets.Table)
	 */
	protected void setupPreviewTable(Table table) {
	}

	/**
	 * @author sgoldstein
	 */
	public class ObjectModifiedListner implements IContextualEventListener {

		/**
		 * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
		 */
		public void onEvent(IContextualEvent event) {
			updateFromDomainObject();
		}
	}

	@Override
	protected String getComponentType() {
		return "ACTION";
	}

	private void sortDescriptorsList(List<PolicyActionsDescriptor> list) {
		Collections.sort(list, new Comparator<PolicyActionsDescriptor>() {

			public int compare(PolicyActionsDescriptor o1,
					PolicyActionsDescriptor o2) {
				String cat1 = o1.getCategory();
				String cat2 = o2.getCategory();
				if (cat1.equalsIgnoreCase(cat2)) {
					String name1 = o1.getDisplayName();
					String name2 = o2.getDisplayName();
					return name1.compareToIgnoreCase(name2);
				}
				return cat1.compareToIgnoreCase(cat2);
			}
		});
	}

	@Override
	public boolean hasCustomProperties() {
		return false;
	}
}
