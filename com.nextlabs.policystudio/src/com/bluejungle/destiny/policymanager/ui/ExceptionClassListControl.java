/*
 * Created on Mar 1st 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author ichiang
 */
package com.bluejungle.destiny.policymanager.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.action.DeleteAction;
import com.bluejungle.destiny.policymanager.action.PolicyStudioActionFactory;
import com.bluejungle.destiny.policymanager.editor.IEditorPanel;
import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.exceptions.PolicyReference;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectAttribute;
import com.bluejungle.pf.domain.epicenter.action.IAction;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyExceptions;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;



public class ExceptionClassListControl extends Composite implements IClipboardEnabled {
	
	protected List<IPolicyReference> exception = null;
	protected SpecType type = null;
	protected List<EditableLabel> labelList = new ArrayList<EditableLabel>();
	protected Map<EditableLabel, IPolicyReference> labelSpecMap = new HashMap<EditableLabel, IPolicyReference>();
	protected EditableLabel currentSelectedControl = null;
	protected List<EditableLabel> selectedControls = new ArrayList<EditableLabel>();
	protected EditableLabel tabPrimaryControl = null;
	protected IEditorPanel parentPanel = null; // change type
	protected EditableLabel addClassLabel = null;
	protected EditableLabel controlForDeselection = null;
	protected Font knownClassFont = null;
	protected Font blankClassFont = null;
	protected Font knownObjectFont = null;
	private int controlId;
	private String defaultName;
	private boolean editable;
	private final EntityType entityType;
	private final SpecType specType;
	private final String policyType;
	protected final PredicateModifiedListener predicateModifiedListener = new PredicateModifiedListener();
	protected HashMap <String, IDPolicy> exceptionPolicyMap = new HashMap <String, IDPolicy>();

	protected TraverseListener traversalListerer = new TraverseListener() {

		public void keyTraversed(TraverseEvent e) {

			if (e.getSource() == tabPrimaryControl.label) {
				return;
			}

			if (e.detail == SWT.TRAVERSE_TAB_NEXT
					|| e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
				e.doit = false;
				tabPrimaryControl.label.traverse(e.detail);
			}
		}
	};

	public ExceptionClassListControl(Composite parent, EntityType entityType,
			SpecType specType, String type, String name, int style) {
		super(parent, style);
		defaultName = name;
		this.entityType = entityType;
		this.policyType = type;
		this.specType = specType;
		
		FontData fd = getDisplay().getSystemFont().getFontData()[0];
		knownObjectFont = ResourceManager.getFont(fd, false, false);

		fd.setStyle(fd.getStyle() | SWT.BOLD | SWT.UNDERLINE_SINGLE);
		// fd.data.lfUnderline = 1;
		knownClassFont = ResourceManager.getFont(fd, false, true);

		fd.setStyle(fd.getStyle() ^ SWT.BOLD ^ SWT.UNDERLINE_SINGLE);
		// fd.data.lfUnderline = 0;
		blankClassFont = ResourceManager.getFont(fd, false, false);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);
		setBackground(ColorBundle.VERY_LIGHT_GRAY);

		addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				cleanup();
			}
		});
	}

	
	public void setExceptionDomainObject(SpecType type, List<IPolicyReference> exception) {
		this.type = type;
		this.exception = exception;
		IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
		IEventManager eventManager = componentManager.getComponent(EventManagerImpl.COMPONENT_INFO);

		if (exception != null) {
			eventManager.unregisterListener(predicateModifiedListener,
					ContextualEventType.PREDICATE_MODIFIED_EVENT,
					exception);
		}
		eventManager.registerListener(predicateModifiedListener,
				ContextualEventType.PREDICATE_MODIFIED_EVENT, exception);
		initialize();
	}


	private void initialize() {
		buildLabelList();
		if (isEditable()) {
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					removeSelection();
					addClassLabel.setFocus();
				}
			});
			createAddClassLabel();
		}
		relayout();
	}
	
	/**
	 * Display a dialog stating that the Exception policy cannot be created bcuz the name is being used.
	 * 
	 * @param name
	 */
	private void displayExceptionPolicyNameNotValidDialog(String name){
		if (!isNewPolicyName(name)){
			StringBuffer msg = new StringBuffer();
			msg.append(ApplicationMessages.CLASSLISTCONTROL_DUPLICATED_EXCEPTION_MSG1);
			msg.append(name);
			msg.append(ApplicationMessages.CLASSLISTCONTROL_DUPLICATED_EXCEPTION_MSG2);
			MessageDialog.openError(getDisplay().getActiveShell(),
			ApplicationMessages.CLASSLISTCONTROL_EXCEPTION_NAME_EXISTED, msg.toString());
		}
	}
	
	/**
	 * 
	 * @param name
	 * @param entityType
	 * @return true if a new entity was created, false otherwise
	 */
	private boolean displayCreateExceptionPolicyDialog(String name) {
		if (name.length() > 128) {
			MessageDialog.openError(getDisplay().getActiveShell(),
					ApplicationMessages.CLASSLISTCONTROL_ERROR,
					ApplicationMessages.CLASSLISTCONTROL_ERROR_LENGTH);
			return false;
		}

		StringBuffer msg = new StringBuffer();
		msg.append(ApplicationMessages.CLASSLISTCONTROL_CREATE_EXCEPTION_MSG1);
		msg.append(name);
		msg.append(ApplicationMessages.CLASSLISTCONTROL_CREATE_EXCEPTION_MSG2);
		if (MessageDialog.openQuestion(getDisplay().getActiveShell(),ApplicationMessages.CLASSLISTCONTROL_CREATE_EXCEPTION, msg.toString())) {
			return true;
		} else {
			return false;
		}
	}

	private IDPolicy getTopPolicy(){
        IEditorPanel panel = GlobalState.getInstance().getEditorPanel();
        IDPolicy policy = null;
        if (panel != null) {
            IHasId domainObj = panel.getDomainObject();
            if (domainObj instanceof IDPolicy) {
                policy = (IDPolicy) panel.getDomainObject();
            }
        }
		return policy;
	}
	
	/**
	 * @param String name: exception (path) name 
 	 * @param String type: Top level policy type
 	 * @param boolean hasException: ture means we are creating exception policy, then we need to pass the "exception" attribute
 	 * 		  false means we are creating normal top level policy
	 */	
	private IDPolicy createNewPolicy(String name, String type, boolean isException){
		return PolicyServerProxy.createBlankPolicy(exceptionPathName(name), type, true);
	}
	
	private void refreshPolicyTree(IDPolicy policy){
		GlobalState.getInstance().loadObjectInEditorPanel(policy);
		EntityInfoProvider.updatePolicyTreeAsync();
	}
	
	
	
	/**
	 * 
	 * @return the policy type from the mainEditorPanel
	 */
	private String policyType() {
        SortedSet<String> attribute = ((Policy) getTopPolicy()).getAttributes();
        String type = attribute.first();
        return PolicyHelpers.getPolicyTypeString(type);
	}


	/**
	 * @param name
	 * @return true if it's a new exception name, false otherwise
	 */
	private boolean isNewPolicyName(String name) {
		boolean couldBeException = EntityInfoProvider.isValidComponentName(name)&& ((getExistingExceptionName(name, getTopPolicy()) == null));

		if (!couldBeException) {
			return false;
		}
		return couldBeException;
	}
	
	/**
	 * Finds an existing exception name case-insensitively, or returns null if
	 * the name does not exist.
	 * 
	 * @param name
	 *            the exception name to check.
	 * @param policyPath
	 * @return the existing name, or null if it does not exist.
	 */
	private synchronized String getExistingExceptionName(String name, IDPolicy policy) {
		String exceptionPath = exceptionPathName(name);
		IPolicyExceptions exception = policy.getPolicyExceptions();
		List<IPolicyReference> exceptionList = exception.getPolicies();
		
		for (int i = 0; i < exceptionList.size(); i++) {
			String current = exceptionList.get(i).getReferencedName();
			if (exceptionPath.compareToIgnoreCase(current) == 0) {
				return current;
			}
		}

		return null;
	}
	
	private String exceptionPathName (String name){
		if (name == null) {
			throw new NullPointerException("name");
		}
		String policyPath = getTopPolicy().getName();
		if (name.indexOf(PQLParser.SEPARATOR) == -1) {
			name = policyPath + PQLParser.SEPARATOR + name;
		}
		return name;
	}

	protected void createAddClassLabel() {
		addClassLabel = new EditableLabel(this, SWT.SHADOW_NONE);
		addClassLabel.setExceptionParentControl(this);
		addClassLabel.setText(defaultName);

		GridData gridData = new GridData();
		// gridData.grabExcessHorizontalSpace = true;
		addClassLabel.setLayoutData(gridData);
		addClassLabel.addEditableLabelListener(new EditableLabelListener() {

			public void mouseUp(EditableLabelEvent e) {
			}

			public void mouseDown(EditableLabelEvent e) {
				EditableLabel label = (EditableLabel) e.getSource();
				if (e.x >= 0 && e.y >= 0 && e.x < label.getSize().x
						&& e.y < label.getSize().y) {
					removeSelection();
					label.setEditing(true);
				}
			}

			public void mouseRightClick(EditableLabelEvent e) {
			}

			public void textChanged(EditableLabelEvent e) {
			}

			public boolean textSaved(EditableLabelEvent e) {
				EditableLabel label = (EditableLabel) e.getSource();
				String text = label.getText().trim();
				boolean createExceptionReference = false;
				PolicyReference reference = null;
				IDPolicy subpolicy = null;
				
				if (text.length() != 0) {

					if (isNewPolicyName(text)) {
						if (displayCreateExceptionPolicyDialog(text)) {
							subpolicy = createNewPolicy(text, policyType(), true);							
							createExceptionReference = true;
						} else {
							return false;
						}
					}else{
						displayExceptionPolicyNameNotValidDialog(text);
						return false;
					}

					
					if (createExceptionReference){
						reference = new PolicyReference (exceptionPathName(text));
						exception.add(reference);
					}
					refreshPolicyTree(subpolicy);
					addLabel(reference);

					parentPanel.relayout();
					if (e.originalEvent != null && e.originalEvent instanceof SelectionEvent) {
						// Enter was pressed. Add new class.
						// Set text to blank and continue to edit.
						addClassLabel.setText("");
						return (false);
					}
				}
				label.setText(defaultName);
				return (true);
			}

			public void upArrow(EditableLabelEvent e) {
			}

			public void downArrow(EditableLabelEvent e) {
			}

			public void delete(EditableLabelEvent e) {
			}

			public boolean startEditing(EditableLabelEvent e) {
				EditableLabel label = (EditableLabel) e.getSource();
				label.setText("");
				return true;
			}

			public void fireCancelEditing(EditableLabelEvent e) {
				addClassLabel.setText(defaultName);
			}
		});
		// tabbing should tab out of the class list control.
		addClassLabel.label.addTraverseListener(traversalListerer);

	}
	
	private void addContextMenu(final EditableLabel thisLabel, final IPolicyReference thisExceptionPolicy) {
		IWorkbenchWindow iww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (iww == null) {
			LoggingUtil.logError(Activator.ID, "Cannot find workbench window", null);
		} else {
			Shell shell = thisLabel.getShell();
			Menu contextMenu = new Menu(shell, SWT.POP_UP);
			final MenuItem openItem = new MenuItem(contextMenu, SWT.PUSH);
			openItem.setText(ApplicationMessages.CLASSLISTCONTROL_OPEN);
			contextMenu.addMenuListener(new MenuListener() {

				public void menuShown(MenuEvent e) {
					DomainObjectDescriptor dod = PolicyServerProxy.getDescriptorByName(exceptionPathName(thisLabel.getText()));
					if (dod != null) {
						openItem.setEnabled(dod.isAccessible());
						openItem.setEnabled(true);
					}
				}

				public void menuHidden(MenuEvent e) {
				}
			});
			openItem.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event e) {
					DomainObjectDescriptor dod = PolicyServerProxy.getDescriptorByName(exceptionPathName(thisLabel.getText()));
					if (dod != null) {
						GlobalState.getInstance().loadObjectInEditorPanel(dod);
					}
				}
			});
			thisLabel.setContextMenu(contextMenu);
		}
	}

	private void addLabel(IPolicyReference exceptionPolicy) {
		EditableLabel label = new EditableLabel(this, SWT.SHADOW_NONE);
		label.setExceptionParentControl(this);
		boolean isFirstLabel = false;
		if (labelList.size() == 0) {
			isFirstLabel = true;
		}

		labelList.add(label);
		labelSpecMap.put(label, exceptionPolicy);

		findAndSetLabelText(exceptionPolicy, label);
//		label.addDragSource();
		GridData gridData = new GridData();
		label.setLayoutData(gridData);
		setLabelFont(label);
//		if (getEntityType() != null) {
		addContextMenu(label, exceptionPolicy);
//		}

		if (isEditable()) {
			label.addEditableLabelListener(new EditableLabelListener() {

				public void mouseUp(EditableLabelEvent e) {
					if (e.isRightMouseButton()) {
						return;
					}
					EditableLabel label = (EditableLabel) e.getSource();
					boolean isShiftPressed = ((e.stateMask & SWT.SHIFT) != 0);
					boolean isControlPressed = ((e.stateMask & SWT.CONTROL) != 0);

					if (isShiftPressed) {

					} else if (isControlPressed) {
						if (controlForDeselection == label
								&& selectedControls.contains(label)) {
							deselectControl(label);
						}
					} else {
						if (currentSelectedControl == null
								|| label != currentSelectedControl
								|| selectedControls.size() > 1) {
							removeSelection();
							selectControl(label);
						}
					}

					controlForDeselection = null;
				}

				/**
				 * @see com.bluejungle.destiny.policymanager.ui.EditableLabelListener#mouseDown(com.bluejungle.destiny.policymanager.ui.EditableLabelEvent)
				 */
				public void mouseDown(EditableLabelEvent e) {
					if (e.isRightMouseButton()) {
						return;
					}
					EditableLabel label = (EditableLabel) e.getSource();
					boolean isShiftPressed = ((e.stateMask & SWT.SHIFT) != 0);
					boolean isControlPressed = ((e.stateMask & SWT.CONTROL) != 0);
					controlForDeselection = null;

					if (isShiftPressed) {
						selectTo(label);
					} else if (isControlPressed) {
						if (!selectedControls.contains(label)) {
							selectControl(label);
						} else {
							// This variable remembers that this control was
							// selected and should be deselected on mouse up. We
							// do not deselect on mouse down to allow for
							// dragging.
							controlForDeselection = label;
						}
					} else {
						// remove selection and set clicked control as selected.
						// if control is already selected, do nothing.
						if (!selectedControls.contains(label)) {
							removeSelection();
							selectControl(label);
						} else {
							// change selection on mouse up to allow for
							// dragging. (we may be dragging multiple selections
							// and do not want to lose the selection
						}

					}

				}

				public void mouseRightClick(EditableLabelEvent e) {
				}

				public void textChanged(EditableLabelEvent e) {
				}

				public boolean textSaved(EditableLabelEvent e) {
					return false;
				}

				public void upArrow(EditableLabelEvent e) {
					EditableLabel label = (EditableLabel) e.getSource();
					EditableLabel newSelectedControl = label;
					boolean isShiftPressed = ((e.stateMask & SWT.SHIFT) != 0);
					int index = labelList.indexOf(label);
					if (index > 0) {
						newSelectedControl = (EditableLabel) labelList
								.get(index - 1);
					}
					if (!isShiftPressed) {
						removeSelection();
						selectControl(newSelectedControl);
					} else {
						selectTo(newSelectedControl);
					}

				}

				public void downArrow(EditableLabelEvent e) {
					EditableLabel label = (EditableLabel) e.getSource();
					EditableLabel newSelectedControl = label;
					boolean isShiftPressed = ((e.stateMask & SWT.SHIFT) != 0);
					int index = labelList.indexOf(label);
					if (index + 1 < labelList.size()) {
						newSelectedControl = (EditableLabel) labelList
								.get(index + 1);
					}
					if (!isShiftPressed) {
						removeSelection();
						selectControl(newSelectedControl);
					} else {
						selectTo(newSelectedControl);
					}
				}

				public void delete(EditableLabelEvent e) {
					deleteSelection();
				}

				public boolean startEditing(EditableLabelEvent e) {
					removeSelection();
					return false;
				}

				public void fireCancelEditing(EditableLabelEvent e) {
				}
			});
		}

		if (isFirstLabel) {
			label.label.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					removeSelection();
					selectControl((EditableLabel) labelList.get(0));
				}

				@Override
				public void focusLost(FocusEvent e) {
					removeSelection();
				}
			});
		} else {
			// tabbing should tab out of the class list control.
			label.label.addTraverseListener(traversalListerer);
		}
	}


	/**
     * 
     */
	public void relayout() {
		if (exceptionsSize(exception) != labelList.size()) {
			refresh();
		} else {
			int labelIndex = 0;
			for (int i = 0; i < exceptionsSize(exception); i++) {
					EditableLabel label = (EditableLabel) labelList.get(labelIndex);
					String current = PolicyHelpers.exceptionTagName(exception.get(i));
					if (label == null || !label.getText().equals(current)){
						refresh();
					}
					labelIndex++;
			}
		}

		if (addClassLabel != null) {
			addClassLabel.moveBelow(null);
		}

		if (labelList.size() > 0) {
			tabPrimaryControl = (EditableLabel) labelList.get(0);
		} else {
			tabPrimaryControl = addClassLabel;
		}
		if (tabPrimaryControl != null && !tabPrimaryControl.isDisposed()) {
			setTabList(new Control[] { tabPrimaryControl });
		}

		// parentPanel.relayout();
	}

	/**
	 * refresh UI based to show current state of domainObject
	 */
	protected void refresh() {
		setRedraw(false);
		for (int i = 0; i < labelList.size(); i++) {
			EditableLabel label = (EditableLabel) labelList.get(i);
			label.dispose();
		}

		labelList.clear();
		labelSpecMap.clear();
		selectedControls.clear();
		currentSelectedControl = null;

		buildLabelList();
		setRedraw(true);
	}

	private void buildLabelList() {
		for (int i = 0; i < exceptionsSize(exception); i++) {
			Object exceptionPolicy = exception.get(i);
			addLabel((IPolicyReference)exceptionPolicy);
		}
	}
	
	private int exceptionsSize (List<IPolicyReference> exception){
		return exception.size();
	}

	/**
	 * @param label
	 * @param spec
	 */
	private void setLabelFont(EditableLabel label) {

		label.setLabelFont(knownClassFont);
		
		// TODO: blank warning font
		// } else if (((GroupBase) spec).state == 2) {
		// label.setLabelFont(blankClassFont);
		// label.setForeground(ColorBundle.ORANGE);

	}



	private void setLabelImage(EditableLabel label) {
		label.setImage(ImageBundle.POLICY_IMG);
	}

	private void findAndSetLabelText(IPolicyReference exceptionPolicy, EditableLabel labelToUpdate) {
		String labelName = "";
		setLabelImage(labelToUpdate);
		labelName = PolicyHelpers.exceptionTagName(exceptionPolicy);
		labelToUpdate.setText(labelName);
	}


	private void deleteIndex(int index) {
//		CompositionUndoElement undoElement = new CompositionUndoElement();
//		undoElement.setDomainObjectId(((IHasId) GlobalState.getInstance().getCurrentObject()).getId());
//		undoElement.setOp(CompositionUndoElementOp.REMOVE_REF);
//		undoElement.setControlId(controlId);
//		undoElement.setIndex(((ExceptionCompositionControl) getParent()).indexOfObject(this));
//		undoElement.setIndexArray(new ArrayList<Integer>());
//		undoElement.setRefArray(new ArrayList<IPredicate>());
		EditableLabel label = (EditableLabel) labelList.get(index);
//		IPredicate spec = (IPredicate) labelSpecMap.get(label);
		label.dispose();
//		undoElement.getIndexArray().add(new Integer(index));
//		undoElement.getRefArray().add(spec);
		labelList.remove(label);
		labelSpecMap.remove(label);
//		PredicateHelpers.removePredicateAt(domainObject, index);
//		GlobalState.getInstance().addUndoElement(undoElement);
		parentPanel.relayout();
	}
	
	private boolean canDelete (String name){
		boolean canDeleteStatus = true;
		Set <DomainObjectDescriptor> selectionSet = new HashSet <DomainObjectDescriptor>();
		DomainObjectDescriptor dod = PolicyServerProxy.getDescriptorByName(exceptionPathName(name));
		selectionSet.add(dod);
		canDeleteStatus &= DeleteAction.checkDomainObjectsStatus(dod);
		return canDeleteStatus;
	}

	public void deleteSelection() {	
		boolean canDeleteStatus = true;
		Set <DomainObjectDescriptor> selectionSet = new HashSet <DomainObjectDescriptor>();
		for(EditableLabel label: selectedControls){
			DomainObjectDescriptor dod = PolicyServerProxy.getDescriptorByName(exceptionPathName(label.getText()));
			selectionSet.add(dod);
			canDeleteStatus &= DeleteAction.checkDomainObjectsStatus(dod);
			try {
				DomainObjectUsage usuage = PolicyServerProxy.getUsage(dod);
				canDeleteStatus &= (!usuage.hasReferringObjects());
				canDeleteStatus &= (usuage
						.getCurrentlydeployedvcersion() == null);
				canDeleteStatus &= (!usuage.hasFuturedeployments());
			} catch (PolicyEditorException e) {
				e.printStackTrace();
			}
		}
		if (!canDeleteStatus){
			displayCannotDeleteExceptionDialog();
		}else{
			if(displayDeleteExceptionDialog()){
				DeleteAction action = new DeleteAction();
				for (int i = 0; i < selectedControls.size(); i++) {
					EditableLabel label = (EditableLabel) selectedControls.get(i);		

					label.dispose();
					labelList.remove(label);
					exception.remove(labelSpecMap.get(label));
					labelSpecMap.remove(label);	
				}
				action.doDelete(selectionSet);
				currentSelectedControl = null;
				selectedControls.clear();
				parentPanel.relayout();
				
			}
		}
	}
	
	/**
	 * Display a dialog stating that the Exception policy cannot be created bcuz the name is being used.
	 * 
	 * @param name
	 */
	private void displayCannotDeleteExceptionDialog(){
			StringBuffer msg = new StringBuffer();
			msg.append(ApplicationMessages.CLASSLISTCONTROL_CANNOT_DELETE_EXCEPTION_MSG);
			MessageDialog.openError(getDisplay().getActiveShell(),
			ApplicationMessages.CLASSLISTCONTROL_EXCEPTION_DELETION_ERROR_TITLE, msg.toString());
	}
	
	/**
	 * @return true if user wants to delete the policy, false otherwise
	 */
	private boolean displayDeleteExceptionDialog() {

		StringBuffer msg = new StringBuffer();
		msg.append(ApplicationMessages.CLASSLISTCONTROL_DELETE_EXCEPTION_MSG);
		if (MessageDialog.openQuestion(getDisplay().getActiveShell(),ApplicationMessages.CLASSLISTCONTROL_DELETE_EXCEPTION, msg.toString())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * unselect all labels.
	 */
	void removeSelection() {
		if (currentSelectedControl != null) {
			currentSelectedControl.setSelected(false);
		}
		currentSelectedControl = null;
		for (int i = 0; i < selectedControls.size(); i++) {
			EditableLabel label = (EditableLabel) selectedControls.get(i);
			if (label != null)
				label.setSelected(false);
		}
		selectedControls.clear();
	}

	public void setParentPanel(IEditorPanel parent) {
		parentPanel = parent;
	}

	/**
	 * Select all controls from the current selection to the specified control
	 * 
	 * @param label
	 */
	protected void selectTo(EditableLabel label) {
		int currentControlIndex;
		EditableLabel originalSelection = currentSelectedControl;
		if (currentSelectedControl == null) {
			currentControlIndex = 0;
			originalSelection = (EditableLabel) labelList.get(0);
		} else {
			currentControlIndex = labelList.indexOf(currentSelectedControl);
		}
		int selectionControlIndex = labelList.indexOf(label);

		int startIndex;
		int endIndex;

		if (currentControlIndex < selectionControlIndex) {
			startIndex = currentControlIndex;
			endIndex = selectionControlIndex;
		} else {
			startIndex = selectionControlIndex;
			endIndex = currentControlIndex;
		}

		removeSelection();

		for (int i = startIndex; i <= endIndex; i++) {
			EditableLabel el = (EditableLabel) labelList.get(i);
			el.setSelected(true);
			selectedControls.add(el);
		}

		currentSelectedControl = originalSelection;
		// set focus to the selected control
		label.setSelected(true);
	}

	/**
	 * @param label
	 */
	private void selectControl(EditableLabel label) {
		currentSelectedControl = label;
		currentSelectedControl.setSelected(true);
		if (!selectedControls.contains(label)) {
			selectedControls.add(currentSelectedControl);
		}
	}

	/**
	 * Deselects the specified EditableLabel
	 * 
	 * @param label
	 */
	private void deselectControl(EditableLabel label) {
		if (currentSelectedControl == label) {
			currentSelectedControl = null;
		}
		if (selectedControls.contains(label)) {
			selectedControls.remove(label);
		}
		label.setSelected(false);
	}



	public void cleanup() {
		IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
		IEventManager eventManager = componentManager.getComponent(EventManagerImpl.COMPONENT_INFO);
		eventManager.unregisterListener(predicateModifiedListener,ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT,exception);
	}

	@Override
	public void dispose() {
		cleanup();
		super.dispose();
	}

	/**
	 * @return Returns the controlId.
	 */
	public int getControlId() {
		return controlId;
	}

	/**
	 * @param controlId
	 *            The controlId to set.
	 */
	public void setControlId(int controlId) {
		this.controlId = controlId;
	}

	/**
	 * @return Returns the editable.
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @param editable
	 *            The editable to set.
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/**
	 * @return Returns the specType.
	 */
	public SpecType getSpecType() {
		return specType;
	}

	public String getComponentType() {
		return policyType;
	}



	private class PredicateModifiedListener implements IContextualEventListener {

		/**
		 * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
		 */
		public void onEvent(IContextualEvent event) {
			ExceptionClassListControl.this.parentPanel.relayout();
		}
	}

	@Override
	public void copy() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void cut() {
		// TODO Auto-generated method stub	
	}
	@Override
	public void paste() {
		// TODO Auto-generated method stub
	};


}
