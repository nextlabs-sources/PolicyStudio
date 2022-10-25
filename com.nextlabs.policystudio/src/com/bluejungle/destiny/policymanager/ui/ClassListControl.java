/*
 * Created on Feb 28, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.SharePointImageConstants;
import com.bluejungle.destiny.policymanager.UserProfileEnum;
import com.bluejungle.destiny.policymanager.editor.IEditorPanel;
import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.model.PolicyServerHelper;
import com.bluejungle.destiny.policymanager.ui.controls.CompositionControl;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.PlatformUtils;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.destiny.services.policy.types.EnrollmentType;
import com.bluejungle.destiny.services.policy.types.PolicyEditorRoles;
import com.bluejungle.destiny.services.policy.types.Realm;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * This is the class for the Class List Control as described in the Policy
 * Manager UI spec.
 * 
 * 
 * @author fuad
 */
public class ClassListControl extends Composite implements IClipboardEnabled {

	private static Map<SubjectAttribute, LeafObjectType> DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP = new HashMap<SubjectAttribute, LeafObjectType>();
	static {
		DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(
				SubjectAttribute.USER_ID, LeafObjectType.USER);
		DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(
				SubjectAttribute.HOST_ID, LeafObjectType.HOST);
		DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(
				SubjectAttribute.APP_ID, LeafObjectType.APPLICATION);
		DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(
				SubjectAttribute.USER_LDAP_GROUP_ID, LeafObjectType.USER_GROUP);
		DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(
				SubjectAttribute.HOST_LDAP_GROUP_ID, LeafObjectType.HOST_GROUP);
		DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(
				SubjectAttribute.CONTACT_ID, LeafObjectType.CONTACT);
	}

	protected CompositePredicate domainObject = null;
	protected SpecType type = null;
	protected List<EditableLabel> labelList = new ArrayList<EditableLabel>();
	protected Map<EditableLabel, IPredicate> labelSpecMap = new HashMap<EditableLabel, IPredicate>();
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
	private final String componentType;
	private boolean acceptLeafObjects = true;
	private boolean isAlreadyPopupDialog;
	private final PredicateModifiedListener predicateModifiedListener = new PredicateModifiedListener();
	private ClassListControl clsLstCtrl; // used for building lists which is done asynchronously. need a placeholder for "this" to be available on a separate thread
	private static final int listSize = 2; // this is the chunk of elements that will be repainted in the list box. For a list with size of 100, after ever listSize, the parentpanel is repainted.
	private int bldLblLstCtr = 0; // this is just the counter for the build list async logic. it goes from 0 to domainObject.predicateCount() and gets incremented in the async thread
	private boolean performRefresh = true;
	
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

	public ClassListControl(Composite parent, EntityType entityType,
			SpecType specType, String componentType, String name, int style) {
		super(parent, style);
		defaultName = name;
		this.entityType = entityType;
		this.componentType = componentType;
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

	public void setDomainObject(SpecType type, CompositePredicate domainObject) {
		this.type = type;
		IPredicate oldDomainObject = domainObject;
		this.domainObject = domainObject;

		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		IEventManager eventManager = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);

		if (oldDomainObject != null) {
			eventManager.unregisterListener(predicateModifiedListener,
					ContextualEventType.PREDICATE_MODIFIED_EVENT,
					oldDomainObject);
		}

		eventManager.registerListener(predicateModifiedListener,
				ContextualEventType.PREDICATE_MODIFIED_EVENT, domainObject);

		initialize();
	}

	private Map<String, EnrollmentType> typeMap;

	private void updateDictionaryRealmsMap() {
		typeMap = new HashMap<String, EnrollmentType>();
		Set<Realm> enrollmentNames = new HashSet<Realm>();
		UserProfileEnum profile = PlatformUtils.getProfile();
		PolicyEditorRoles role = PolicyEditorRoles.CORPORATE; // default value
		if (profile == UserProfileEnum.CORPORATE) {
			role = PolicyEditorRoles.CORPORATE;
		} else if (profile == UserProfileEnum.FILESYSTEM) {
			role = PolicyEditorRoles.FILESYSTEM;
		} else if (profile == UserProfileEnum.PORTAL) {
			role = PolicyEditorRoles.PORTAL;
		}
		try {
			enrollmentNames = PolicyServerProxy.client
					.getDictionaryEnrollmentRealms(role);
		} catch (PolicyEditorException exception) {
			LoggingUtil.logWarning(Activator.ID,
					"Failed to retrieve dictionary realms.", exception);
		}

		for (Realm realm : enrollmentNames) {
			typeMap.put(realm.getName(), realm.getType());
		}
	}

	private void initialize() {
		clsLstCtrl = this; // needed for the buildLabelList that refreshes the list on a separate thread. Useful when the list has many items
		
		updateDictionaryRealmsMap();

		buildLabelList();
		bldLblLstCtr = 0;
		if (isEditable()) {
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					removeSelection();
					addClassLabel.setFocus();
				}
			});
			createAddClassLabel();
			addDropTarget();
		}
	}

	/**
	 * 
	 * @param name
	 * @param entityType
	 * @return true if a new entity was created, false otherwise
	 */
	private boolean displayCreateComponentDialog(String name) {
		if (name.length() > 128) {
			MessageDialog.openError(getDisplay().getActiveShell(),
					ApplicationMessages.CLASSLISTCONTROL_ERROR,
					ApplicationMessages.CLASSLISTCONTROL_ERROR_LENGTH);
			return false;
		}

		StringBuffer msg = new StringBuffer();
		msg.append(ApplicationMessages.CLASSLISTCONTROL_CREATE_MSG1);
		msg.append(name);
		msg.append(ApplicationMessages.CLASSLISTCONTROL_CREATE_MSG2);
		if (MessageDialog.openQuestion(getDisplay().getActiveShell(),
				ApplicationMessages.CLASSLISTCONTROL_CREATE, msg.toString())) {
			PolicyServerProxy.createBlankComponent(name, componentType);
			GlobalState.getInstance().getComponentListPanel(getComponentType())
					.populateList();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Display a dialog stating that the component cannot be created or is not
	 * accessible
	 * 
	 * @param name
	 */
	private void displayComponentNotValidDialog(String name) {
		StringBuffer msg = new StringBuffer();
		msg.append(ApplicationMessages.CLASSLISTCONTROL_NOT_FOUND_MSG1);
		msg.append(name);
		msg.append(ApplicationMessages.CLASSLISTCONTROL_NOT_FOUND_MSG2);
		MessageDialog.openError(getDisplay().getActiveShell(),
				ApplicationMessages.CLASSLISTCONTROL_NOT_FOUND, msg.toString());
	}

	private boolean isNewComponentName(String name) {
		boolean couldBeComponent = EntityInfoProvider
				.isValidComponentName(name)
				&& (EntityInfoProvider.getExistingComponentName(name,
						componentType) == null);

		if (!couldBeComponent) {
			return false;
		}

		if (canAcceptLeafObjects()) {
			SpecType specType = getSpecType();
			LeafObjectType[] leafTypes = getAcceptedLeafObjectTypes(specType);
			for (int i = 0; i < leafTypes.length; i++) {
				if (leafTypes[i] == LeafObjectType.RESOURCE) {
					if (componentType.equals("SERVER")) {
						return false;
					}
					if (isValidResourceAtomName(name)) {
						return false;
					}
				} else {
					if (EntityInfoProvider.getLeafObject(name, leafTypes[i]) != null) {
						return false;
					}
				}
			}
		}
		return PolicyServerProxy.getAllowedEntityTypes().contains(
				getEntityType());
	}

	/**
     * 
     */
	private void createAddClassLabel() {
		addClassLabel = new EditableLabel(this, SWT.SHADOW_NONE);
		addClassLabel.setParentControl(this);
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
				boolean createComponentReference = false;

				if (text.length() != 0) {

					if (isNewComponentName(text)) {
						if (displayCreateComponentDialog(text)) {
							createComponentReference = true;
						} else {
							return false;
						}
					}

					IPredicate refEntry = null;

					String existingComponentName = EntityInfoProvider
							.getExistingComponentName(text, componentType);

					if (existingComponentName != null) {
						text = existingComponentName;
						DomainObjectDescriptor dod = getDescriptorForUnescapedName(
								existingComponentName, getComponentType());
						if (!canAddObjects(new Object[] { text })
								|| !dod.isAccessible()) {
							getDisplay().beep();
							if (!isAlreadyPopupDialog)
								displayComponentNotValidDialog(text);
							return false;
						}
					}

					if (createComponentReference
							|| existingComponentName != null) {
						refEntry = PredicateHelpers.getComponentReference(text,
								getSpecType());
					} else if (canAcceptLeafObjects()) {
						refEntry = getReferenceForName(componentType,
								entityType,
								getAcceptedLeafObjectTypes(getSpecType()), text);
					}

					if (refEntry == null) {
						getDisplay().beep();
						displayComponentNotValidDialog(text);
						return false;
					}

					addElementWithUndo(refEntry);
					addLabel(refEntry,clsLstCtrl);

					parentPanel.relayout();
					if (e.originalEvent != null
							&& e.originalEvent instanceof SelectionEvent) {
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

	private void addContextMenu(final EditableLabel thisLabel,
			final IPredicate thisSpec) {
		IWorkbenchWindow iww = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (iww == null) {
			LoggingUtil.logError(Activator.ID, "Cannot find workbench window",
					null);
		} else {
			Shell shell = thisLabel.getShell();
			Menu contextMenu = new Menu(shell, SWT.POP_UP);
			final MenuItem openItem = new MenuItem(contextMenu, SWT.PUSH);
			openItem.setText(ApplicationMessages.CLASSLISTCONTROL_OPEN);
			contextMenu.addMenuListener(new MenuListener() {

				public void menuShown(MenuEvent e) {
					DomainObjectDescriptor dod = getDescriptorForUnescapedName(
							thisLabel.getText(), getEntityType(thisSpec));
					if (dod != null) {
						openItem.setEnabled(dod.isAccessible());
					}
				}

				public void menuHidden(MenuEvent e) {
				}
			});
			openItem.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event e) {
					DomainObjectDescriptor dod = getDescriptorForUnescapedName(
							thisLabel.getText(), getEntityType(thisSpec));
					if (dod != null) {
						GlobalState.getInstance().loadObjectInEditorPanel(dod);
					}
				}
			});
			thisLabel.setContextMenu(contextMenu);
		}
	}

	private static DomainObjectDescriptor getDescriptorForUnescapedName(
			String name, String type) {
		if (name == null || name.length() == 0) {
			return null;
		}
		Collection<DomainObjectDescriptor> collection = PolicyServerProxy
				.getEntityList(PolicyServerProxy.escape(name), type);
		Iterator<DomainObjectDescriptor> iter = collection.iterator();
		return (iter.hasNext()) ? (DomainObjectDescriptor) iter.next() : null;
	}

	private void addLabel(IPredicate spec, ClassListControl clsLstCtrl) {
		EditableLabel label = new EditableLabel(this, SWT.SHADOW_NONE);
		label.setParentControl(clsLstCtrl);
		boolean isFirstLabel = false;
		if (labelList.size() == 0) {
			isFirstLabel = true;
		}

		labelList.add(label);
		labelSpecMap.put(label, spec);

		findAndSetLabelText(spec, label);
		label.addDragSource();

		GridData gridData = new GridData();
		label.setLayoutData(gridData);

		setLabelFont(label, spec);

		if (getEntityType(spec) != null) {
			addContextMenu(label, spec);
		}

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
					EditableLabel label = (EditableLabel) e.getSource();
					int index = labelList.indexOf(label);
					String text = label.getText().trim();
					boolean createComponentReference = false;

					if (text.length() != 0) {

						if (isNewComponentName(text)) {
							if (displayCreateComponentDialog(text)) {
								createComponentReference = true;
							} else {
								return false;
							}
						}

						IPredicate refEntry = null;
						String existingComponentName = EntityInfoProvider
								.getExistingComponentName(text, componentType);

						if (existingComponentName != null) {
							text = existingComponentName;
							DomainObjectDescriptor dod = getDescriptorForUnescapedName(
									existingComponentName, getComponentType());
							if (!canAddObjects(new Object[] { text })
									|| !dod.isAccessible()) {
								getDisplay().beep();
								if (!isAlreadyPopupDialog)
									displayComponentNotValidDialog(text);
								return false;
							}
						}

						if (createComponentReference
								|| existingComponentName != null) {
							refEntry = PredicateHelpers.getComponentReference(
									existingComponentName, getSpecType());
						} else if (canAcceptLeafObjects()) {
							refEntry = getReferenceForName(componentType,
									entityType,
									getAcceptedLeafObjectTypes(getSpecType()),
									text);
						}

						if (refEntry == null) {
							getDisplay().beep();
							displayComponentNotValidDialog(text);
							return false;
						}

						PredicateHelpers.removePredicateAt(domainObject, index);
						PredicateHelpers.insertPredicateAt(domainObject,
								refEntry, index);

						label.setEditing(false);
						//we don't want to perform a refresh when we are editing a label because this will cause us to clear the existing labellist.
						performRefresh = false;
						parentPanel.relayout();
						//we can set the performRefresh flag back on after the relayout function has been performed.
						performRefresh = true;
						selectControl(label);
					} else {
						deleteIndex(index);
					}

					return true;
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
					deleteSelection(e.originalEvent instanceof DragSourceEvent);
				}

				public boolean startEditing(EditableLabelEvent e) {
					removeSelection();
					return true;
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

	private static IPredicate getReferenceForName(String componentType,
			EntityType entityType, LeafObjectType[] types, String text) {
		IPredicate res = null;
		for (int i = 0; i < types.length && res == null; i++) {
			if (types[i] == LeafObjectType.RESOURCE) {
				if (componentType.equals("PORTAL")) {
					res = new Relation(RelationOp.EQUALS,
							ResourceAttribute.PORTAL_URL,
							ResourceAttribute.PORTAL_URL.build(text));
				} else if (componentType.equals("SERVER")) {
					ResourceAttribute attr = ResourceAttribute.forNameAndType(
							"name", "server");
					res = new Relation(RelationOp.EQUALS, attr, attr
							.build(text));
					// res = PredicateHelpers.getResourceReference(text);
				} else if (isValidResourceAtomName(text)) {
					res = PredicateHelpers.getResourceReference(text);
				}
			} else {
				LeafObject leaf = EntityInfoProvider.getLeafObject(text,
						types[i]);
				if (leaf != null) {
					res = PredicateHelpers.getLeafReference(leaf);
				}
			}
		}
		return res;
	}

	/**
     * 
     */
	public void relayout() {
		if (PredicateHelpers.getRealPredicateCount(domainObject) != labelList
				.size()) {
			if(performRefresh)
				refresh();
		} else {
			int labelIndex = 0;
			for (int i = 0; i < domainObject.predicateCount(); i++) {
				IPredicate pred = domainObject.predicateAt(i);
				if (pred instanceof IDSpec) {
					IDSpec spec = (IDSpec) pred;
					EditableLabel label = (EditableLabel) labelList
							.get(labelIndex);
					if (label == null
							|| !spec.getName().equals(label.getText())) {
						refresh();
					}
					labelIndex++;
				}
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
	private void refresh() {
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
		bldLblLstCtr = 0;
		setRedraw(true);
	}


	private void buildLabelList() {
		/*
		 * A way to make the UI more responsive. The adding of labels to the list is done on an async thread.
		 * For lists having a large number of list items, the earlier way of adding them in the UI thread caused the user to wait for an enormous time (2600 list items = 30 mins)
		 * UI thread being blocked is not a desirable pattern for time consuming computation.
		 * 
		 * With the asyncExec which takes in a Runnable, we are able to build the list and repaint it frequently (after every listSize interval).
		 * The parentPanel is repainted from this separate thread
		 * 
		 * ISSUE - the delete action, deletes the label, but the change of focus takes time. Is observed only when the list size is large, else it is immediate
		 */
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				try{
					//when we are doing a buildLabel list, we do not want to perform a Refresh because that will destroy the label list so when we are finished, nothing is shown in classlist
					performRefresh = false;
					for (int i = bldLblLstCtr; i < domainObject.predicateCount(); i++) {
						Object predicateElement = domainObject.predicateAt(i);
						if (!(predicateElement instanceof PredicateConstants)) {
							addLabel((IPredicate) predicateElement, clsLstCtrl);
						}
						if(i==bldLblLstCtr + listSize){ // we have added listSize worth of new elements to the list
							bldLblLstCtr = bldLblLstCtr + listSize + 1; // because of a 0 based index, not to repeat the listSize element twice, adding + 1
							clsLstCtrl.layout();
							clsLstCtrl.parentPanel.relayout();
							buildLabelList(); // recurse till end of domainObject.predicateCount()
							break;
						}
						if(i == domainObject.predicateCount() -1){ // edge condition where i != bldLblLstCtr + listSize - earlier code block. Handling this condition. I know there is a case where the repainting might be done twice. Can put the logic to make it just once...but, thats for another day
							bldLblLstCtr = domainObject.predicateCount(); 
							clsLstCtrl.layout();
							clsLstCtrl.parentPanel.relayout();
						}
					}
					//switch performRefresh flag back on after buildLabelList is finished
					performRefresh = true;				
				}catch(Exception ex){
					
					// parent() is disposed... do nothing but just get out
					// somebody killed policy studio or disposed the main window
					// should have done if(!clsLstCtrl.getParent().isDisposed()) logic, but handling it outside just to cover all cases.
					// bad way to handle this logic, by a generic exception catcher and gobbling it... but primarily meant for an action where user clicks on X on the main window -> Kills the Policy Studio
				}
			}
		});
		
		
	}

	/**
	 * @param label
	 * @param spec
	 */
	private void setLabelFont(EditableLabel label, IPredicate spec) {
		if (spec instanceof IDSpec) {
			// this is a component reference of some kind
			label.setLabelFont(FontBundle.DEFAULT_UNDERLINE);
		} else if (spec instanceof Relation) {
			IExpression exp = ((Relation) spec).getLHS();
			if (exp instanceof IDSubjectAttribute
					&& (exp == SubjectAttribute.HOST_LDAP_GROUP || exp == SubjectAttribute.USER_LDAP_GROUP)) {
				label.setLabelFont(FontBundle.DEFAULT_BOLD);
			} else {
				label.setLabelFont(FontBundle.DEFAULT_NORMAL);
			}
		} else {
			label.setLabelFont(knownClassFont);
		}
		// TODO: blank warning font
		// } else if (((GroupBase) spec).state == 2) {
		// label.setLabelFont(blankClassFont);
		// label.setForeground(ColorBundle.ORANGE);

	}

	private String getEntityType(IPredicate spec) {
		if (spec instanceof IDSpecRef) {
			IDSpecRef ref = (IDSpecRef) spec;
			String component = PolicyServerHelper.getComponentEnumFormName(ref
					.getReferencedName());
			return component;
		} else {
			return null;
		}
	}

	private void setLabelImage(EditableLabel label, LeafObject object) {
		String domainName = object.getDomainName();
		LeafObjectType type = object.getType();
		if (type == LeafObjectType.USER) {
			if (domainName != null && typeMap.containsKey(domainName)
					&& typeMap.get(domainName).equals(EnrollmentType.value3)) {
				label.setImage(SharePointImageConstants.SHAREPOINT_USER);
			} else {
				label.setImage(ImageBundle.USER_IMG);
			}
		} else if (object.getType() == LeafObjectType.USER_GROUP) {
			if (domainName != null && typeMap.containsKey(domainName)
					&& typeMap.get(domainName).equals(EnrollmentType.value3)) {
				label.setImage(SharePointImageConstants.SHAREPOINT_USERGROUP);
			} else {
				label.setImage(ImageBundle.IMPORTED_USER_GROUP_IMG);
			}
		} else {
			label.setImage(ObjectLabelImageProvider.getImage(object));
		}
	}

	private void setLabelImage(EditableLabel label, IPredicate specRef) {
		label.setImage(ObjectLabelImageProvider.getImage(specRef));
	}

	private void findAndSetLabelText(IPredicate spec,
			EditableLabel labelToUpdate) {
		String labelName = "";
		setLabelImage(labelToUpdate, spec);
		if (spec instanceof IDSpecRef) {
			IDSpecRef specRef = (IDSpecRef) spec;
			labelName = specRef.getReferencedName();
			labelName = labelName.substring(labelName
					.indexOf(PQLParser.SEPARATOR) + 1);
		} else if (spec instanceof Relation) {
			IExpression lhs = ((Relation) spec).getLHS();
			IExpression rhs = ((Relation) spec).getRHS();
			Object rhsValue = rhs.evaluate(null).getValue();
			if (lhs instanceof IDSubjectAttribute) {
				if (DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP
						.containsKey(lhs)
						&& rhsValue instanceof Long) {
					LeafObjectType associatedLeafObjectType = (LeafObjectType) DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP
							.get(lhs);
					LeafObject leafObject = EntityInfoProvider
							.getLeafObjectByID((Long) rhsValue,
									associatedLeafObjectType);

					if (leafObject != null) {
						labelName = leafObject.getName();
						setLabelImage(labelToUpdate, leafObject);
					} else {
						labelName = ApplicationMessages.CLASSLISTCONTROL_ERROR_ENTRY_NOT_FOUND
								+ rhsValue;
						formatLabelForError(labelToUpdate);
					}
				} else {
					// Done for consistency with earlier implement to reduce
					// risk
					labelName = "" + rhsValue;
				}
			} else if (lhs instanceof ResourceAttribute
					&& rhs instanceof Constant) { /* PATH */
				String val = ((Constant) rhs).getRepresentation();
				String res = val.replaceAll("[\\\\]+", "\\\\");
				labelName = val.startsWith("\\\\") ? "\\" + res : res;
			} else {
				// Catch all
				if (rhs instanceof Constant) {
					labelName = ((Constant) rhs).getRepresentation();
				} else {
					labelName = "" + rhsValue;
				}
			}
		}

		labelToUpdate.setText(labelName);
	}

	/**
	 * Format label in error style
	 * 
	 * @param labelToUpdate
	 */
	private void formatLabelForError(EditableLabel labelToUpdate) {
		labelToUpdate.setForeground(ResourceManager.getColor(SWT.COLOR_RED));
	}

	private void deleteIndex(int index) {
		CompositionUndoElement undoElement = new CompositionUndoElement();

		undoElement.setDomainObjectId(((IHasId) GlobalState.getInstance()
				.getCurrentObject()).getId());
		undoElement.setOp(CompositionUndoElementOp.REMOVE_REF);
		undoElement.setControlId(controlId);
		undoElement.setIndex(((CompositionControl) getParent())
				.indexOfObject(this));
		undoElement.setIndexArray(new ArrayList<Integer>());
		undoElement.setRefArray(new ArrayList<IPredicate>());
		EditableLabel label = (EditableLabel) labelList.get(index);
		IPredicate spec = (IPredicate) labelSpecMap.get(label);
		label.dispose();
		undoElement.getIndexArray().add(new Integer(index));
		undoElement.getRefArray().add(spec);
		labelList.remove(label);
		labelSpecMap.remove(label);
		PredicateHelpers.removePredicateAt(domainObject, index);

		GlobalState.getInstance().addUndoElement(undoElement);
		parentPanel.relayout();
	}

	private void deleteSelection(boolean fromDropEvent) {
		CompositionUndoElement undoElement = new CompositionUndoElement();
		undoElement.setDomainObjectId(((IHasId) GlobalState.getInstance()
				.getCurrentObject()).getId());
		// if the delete is due to an element being dragged somewhere, the undo
		// should also undo the dropped element
		undoElement.setContinuation(fromDropEvent);
		undoElement.setOp(CompositionUndoElementOp.REMOVE_REF);
		undoElement.setControlId(controlId);
		// TODO fix parent panel stuff
		undoElement.setIndex(((CompositionControl) getParent())
				.indexOfObject(this));
		undoElement.setIndexArray(new ArrayList<Integer>());
		undoElement.setRefArray(new ArrayList<IPredicate>());
		for (int i = 0; i < selectedControls.size(); i++) {
			EditableLabel label = (EditableLabel) selectedControls.get(i);
			IPredicate spec = (IPredicate) labelSpecMap.get(label);
			label.dispose();
			undoElement.getIndexArray().add(
					new Integer(labelList.indexOf(label)));
			undoElement.getRefArray().add(spec);
			labelList.remove(label);
			labelSpecMap.remove(label);
			domainObject.removePredicate(spec);
			PredicateHelpers.rebalanceDomainObject(domainObject, domainObject
					.getOp());
		}

		GlobalState.getInstance().addUndoElement(undoElement);

		currentSelectedControl = null;
		selectedControls.clear();
		parentPanel.relayout();
	}

	/**
	 * unselect all labels.
	 */
	private void removeSelection() {
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

	private void addDropTarget() {
		// Allow data to be copied or moved to the drop target
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
		DropTarget target = new DropTarget(this, operations);

		// Receive data in Text or File format
		final PolicyObjectTransfer transfer = PolicyObjectTransfer
				.getInstance();
		Transfer[] types = new Transfer[] { transfer };
		target.setTransfer(types);

		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_MOVE;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
				for (int i = 0; i < event.dataTypes.length; i++) {
					if (transfer.isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
						IPredicate[] spec = (IPredicate[]) transfer
								.nativeToJava(event.currentDataType);
						try {
							SpecType draginType = PredicateHelpers
									.getPredicateType(spec[0]);
							SpecType containerType = getSpecType();
							if (draginType == SpecType.PORTAL
									&& containerType == SpecType.RESOURCE) {
								break;
							}
							if (draginType != containerType) {
								event.detail = DND.DROP_NONE;
								return;
							}
						} catch (Exception e) {
							LoggingUtil.logError(Activator.ID,
									"error occurs when dragging over", e);
						}

						if (!canAddObjects(spec)) {
							event.detail = DND.DROP_NONE;
							return;
						}

						break;
					}
				}
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_MOVE) != 0) {
						event.detail = DND.DROP_MOVE;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
			}

			@Override
			public void dropAccept(DropTargetEvent event) {
				IPredicate[] spec = (IPredicate[]) transfer
						.nativeToJava(event.currentDataType);

				if (!canAddObjects(spec)) {
					event.detail = DND.DROP_NONE;
					getDisplay().beep();
				}
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (transfer.isSupportedType(event.currentDataType)) {
					IPredicate[] specArray = (IPredicate[]) transfer
							.nativeToJava(event.currentDataType);
					CompositionUndoElement undoElement = createAddRefUndoElement();
					for (int i = 0; i < specArray.length; i++) {
						// element is always added at end
						undoElement.getIndexArray().add(
								new Integer(PredicateHelpers
										.getRealPredicateCount(domainObject)));
						undoElement.getRefArray().add(specArray[i]);

						PredicateHelpers.addPredicate(domainObject,
								specArray[i]);
						addLabel(specArray[i],clsLstCtrl);
					}
					GlobalState.getInstance().addUndoElement(undoElement);
					parentPanel.relayout();
				}
			}
		});
	}

	/**
	 * add new element and create undo info for it.
	 * 
	 * @param newClass
	 *            new element to be added
	 */
	public void addElementWithUndo(IPredicate spec) {
		CompositionUndoElement undoElement = createAddRefUndoElement();
		// element is always added at end
		undoElement.getIndexArray().add(
				new Integer(PredicateHelpers
						.getRealPredicateCount(domainObject)));
		undoElement.getRefArray().add(spec);

		domainObject.addPredicate(spec);
		PredicateHelpers.rebalanceDomainObject(domainObject, domainObject
				.getOp());

		GlobalState.getInstance().addUndoElement(undoElement);

	}

	public void addLeafElementsWithUndo(List<LeafObject> leafObjectList) {
		if (leafObjectList == null) {
			throw new NullPointerException("specList cannot be null.");
		}

		if (leafObjectList.size() > 0) {
			boolean modified = false;
			CompositionUndoElement undoElement = createAddRefUndoElement();
			// element is always added at end
			Iterator<LeafObject> iter = leafObjectList.iterator();
			while (iter.hasNext()) {
				LeafObject nextLeaf = (LeafObject) iter.next();

				IPredicate spec = PredicateHelpers.getLeafReference(nextLeaf);
				boolean found = false;
				for (int i = 0; i < domainObject.predicateCount(); i++) {
					Object predicateElement = domainObject.predicateAt(i);
					if (!(predicateElement instanceof PredicateConstants)) {
						IPredicate predicate = (IPredicate) predicateElement;
						if (spec.toString().equals(predicate.toString())) {
							found = true;
							break;
						}
					}
				}

				if (!found) {
					// add each object's info to the undo element
					undoElement.getIndexArray().add(
							new Integer(PredicateHelpers
									.getRealPredicateCount(domainObject)));
					undoElement.getRefArray().add(spec);
					// add the object to this list
					domainObject.addPredicate(spec);
					modified = true;
				}
			}

			if (modified) {
				PredicateHelpers.rebalanceDomainObject(domainObject,
						domainObject.getOp());
				GlobalState.getInstance().addUndoElement(undoElement);
			}
		}
	}

	/**
	 * Checks whether all the elements of a collection may be added to the list
	 * control
	 * 
	 * @param candidates
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public boolean canAddObjects(Object[] candidates) {
		isAlreadyPopupDialog = false;
		IHasId parentDomainObject = parentPanel.getDomainObject();
		String parentName = DomainObjectHelper.getName(parentDomainObject);

		Set<String> deniedNames = new HashSet<String>();

		if (!DomainObjectHelper.getObjectType(parentDomainObject).equals(
				ApplicationMessages.DOMAINOBJECTHELPER_POLICY_TYPE)) {
			// --if the parent is a policy, we don't need to worry about self
			// references or circular references

			// check to make sure this is not a reference to this object
			deniedNames.add(parentName);

			// check to make sure this is not a circular reference
			for (DomainObjectDescriptor desc : PolicyServerProxy
					.getReferringComponents(parentName, getEntityType())) {
				deniedNames.add(desc.getName());
			}
		}

		// TODO: enforce special restrictions for access policies

		for (Object candidate : candidates) {
			String candidateName = "";
			if (candidate instanceof String) {
				candidateName = (String) candidate;
			} else if (candidate instanceof IDSpecRef) {
				candidateName = ((IDSpecRef) candidate).getReferencedName();
			} else {
				continue;
			}

			if (deniedNames.contains(candidateName)) {
				isAlreadyPopupDialog = true;
				return false;
			}

			for (int i = 0, n = domainObject.predicateCount(); i < n; i++) {
				Object predicate = domainObject.predicateAt(i);
				String existingName = "";
				if (predicate instanceof String) {
					existingName = (String) candidate;
				} else if (predicate instanceof IDSpecRef) {
					existingName = ((IDSpecRef) predicate).getReferencedName();
				}
				if (candidateName.equals(existingName)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * creates an undo element for add ref
	 * 
	 * @return new CompositionUndoElement
	 */
	private CompositionUndoElement createAddRefUndoElement() {
		CompositionUndoElement undoElement = new CompositionUndoElement();
		undoElement.setDomainObjectId(((IHasId) GlobalState.getInstance()
				.getCurrentObject()).getId());
		undoElement.setOp(CompositionUndoElementOp.ADD_REF);

		undoElement.setIndex(((CompositionControl) getParent())
				.indexOfObject(this));
		undoElement.setIndexArray(new ArrayList<Integer>());
		undoElement.setRefArray(new ArrayList<IPredicate>());
		undoElement.setControlId(controlId);
		return undoElement;
	}

	public void cleanup() {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		IEventManager eventManager = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
		eventManager.unregisterListener(predicateModifiedListener,
				ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT,
				domainObject);
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
	 * @return Returns the entityType.
	 */
	public EntityType getEntityType() {
		return entityType;
	}

	/**
	 * @return Returns the specType.
	 */
	public SpecType getSpecType() {
		return specType;
	}

	public String getComponentType() {
		return componentType;
	}

	public static LeafObjectType[] getAcceptedLeafObjectTypes(SpecType specType) {
		if (specType == SpecType.RESOURCE) {
			return new LeafObjectType[] { LeafObjectType.RESOURCE };
		}
		if (specType == SpecType.USER) {
			return new LeafObjectType[] { LeafObjectType.USER_GROUP,
					LeafObjectType.USER };
		}
		if (specType == SpecType.HOST) {
			return new LeafObjectType[] { LeafObjectType.HOST_GROUP,
					LeafObjectType.HOST };
		}
		if (specType == SpecType.APPLICATION) {
			return new LeafObjectType[] { LeafObjectType.APPLICATION };
		}
		if (specType == SpecType.PORTAL) {
			return new LeafObjectType[] { LeafObjectType.RESOURCE };
		}
		return new LeafObjectType[] {};
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.IClipboardEnabled#copy()
	 */
	public void copy() {
		Clipboard clipboard = new Clipboard(getDisplay());

		IPredicate[] specArray = new IPredicate[selectedControls.size()];
		for (int i = 0; i < selectedControls.size(); i++) {
			specArray[i] = (IPredicate) labelSpecMap.get(selectedControls
					.get(i));
		}

		clipboard.setContents(new Object[] { specArray },
				new Transfer[] { PolicyObjectTransfer.getInstance() });
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.IClipboardEnabled#paste()
	 */
	public void paste() {
		removeSelection();
		Clipboard clipboard = new Clipboard(getDisplay());
		IPredicate[] specArray = (IPredicate[]) clipboard
				.getContents(PolicyObjectTransfer.getInstance());
		if (specArray == null || !canAddObjects(specArray)) {
			getDisplay().beep();
			return;
		}
		if (specArray != null
				&& getSpecType() == PredicateHelpers
						.getPredicateType(specArray[0])) {

			CompositionUndoElement undoElement = createAddRefUndoElement();
			for (int i = 0; i < specArray.length; i++) {
				// element is always added at end
				undoElement.getIndexArray().add(
						new Integer(PredicateHelpers
								.getRealPredicateCount(domainObject)));
				undoElement.getRefArray().add(specArray[i]);

				PredicateHelpers.addPredicate(domainObject, specArray[i]);
				addLabel(specArray[i],clsLstCtrl);
			}
			GlobalState.getInstance().addUndoElement(undoElement);
			parentPanel.relayout();
		}
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.IClipboardEnabled#cut()
	 */
	public void cut() {
		copy();
		deleteSelection(false);
	}

	/**
	 * @return Returns the acceptLeafObjects.
	 */
	public boolean canAcceptLeafObjects() {
		return acceptLeafObjects;
	}

	/**
	 * @param acceptLeafObjects
	 *            The acceptLeafObjects to set.
	 */
	public void setAcceptLeafObjects(boolean acceptLeafObjects) {
		this.acceptLeafObjects = acceptLeafObjects;
	}

	private class PredicateModifiedListener implements IContextualEventListener {

		/**
		 * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
		 */
		public void onEvent(IContextualEvent event) {
			ClassListControl.this.parentPanel.relayout();
		}
	};

	private static boolean isValidResourceAtomName(String name) {
		// FIXME the condition is not optimized and not restricted enough.
		return name.toLowerCase().startsWith("device:")
				|| name.toLowerCase().startsWith("ftp:")
				|| name.toLowerCase().startsWith("sharepoint")
				|| ((name.startsWith("\\")) || (name.indexOf("*") >= 0)
						|| (name.indexOf("?") >= 0)
						|| (name.matches("[a-zA-Z]:\\\\.*")) || name
						.matches("\\[[a-zA-Z_]+\\].*"));
	}

}
