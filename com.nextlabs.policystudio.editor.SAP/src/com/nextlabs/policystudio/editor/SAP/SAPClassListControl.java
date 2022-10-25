package com.nextlabs.policystudio.editor.SAP;

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
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.CompositionUndoElement;
import com.bluejungle.destiny.policymanager.ui.CompositionUndoElementOp;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EditableLabelEvent;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.IClipboardEnabled;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyObjectTransfer;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel;
import com.bluejungle.destiny.policymanager.ui.EditableLabelListener;
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
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.IPolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.common.SpecReference;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.CLabel;
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
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class SAPClassListControl extends Composite implements IClipboardEnabled {
	private static Map<SubjectAttribute, LeafObjectType> DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP = new HashMap();

	protected CompositePredicate domainObject = null;
	protected SpecType type = null;
	protected List<EditableLabel> labelList = new ArrayList();
	protected Map<EditableLabel, IPredicate> labelSpecMap = new HashMap();
	protected EditableLabel currentSelectedControl = null;
	protected List<EditableLabel> selectedControls = new ArrayList();
	protected EditableLabel tabPrimaryControl = null;
	protected IEditorPanel parentPanel = null;
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
	private SAPClassListControl clsLstCtrl; // used for building lists which is done asynchronously. need a placeholder for "this" to be available on a separate thread
	private static final int listSize = 2; // this is the chunk of elements that will be repainted in the list box. For a list with size of 100, after ever listSize, the parentpanel is repainted.
	private int bldLblLstCtr = 0; // this is just the counter for the build list async logic. it goes from 0 to domainObject.predicateCount() and gets incremented in the async thread
	private boolean performRefresh = true;
	
	protected TraverseListener traversalListerer = new TraverseListener() {
		public void keyTraversed(TraverseEvent e) {
			if (e.getSource() == SAPClassListControl.this.tabPrimaryControl.label) {
				return;
			}

			if ((e.detail != 16) && (e.detail != 8))
				return;
			e.doit = false;
			SAPClassListControl.this.tabPrimaryControl.label.traverse(e.detail);
		}
	};
	private Map<String, EnrollmentType> typeMap;

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

	public SAPClassListControl(Composite parent, EntityType entityType,
			SpecType specType, String componentType, String name, int style) {
		super(parent, style);
		this.defaultName = name;
		this.entityType = entityType;
		this.componentType = componentType;
		this.specType = specType;
		FontData fd = getDisplay().getSystemFont().getFontData()[0];
		this.knownObjectFont = ResourceManager.getFont(fd, false, false);

		fd.setStyle(fd.getStyle() | 0x1);

		this.knownClassFont = ResourceManager.getFont(fd, false, true);

		fd.setStyle(fd.getStyle() ^ 0x1);

		this.blankClassFont = ResourceManager.getFont(fd, false, false);

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
				SAPClassListControl.this.cleanup();
			}
		});
	}

	public void setDomainObject(SpecType type, CompositePredicate domainObject) {
		this.type = type;
		IPredicate oldDomainObject = domainObject;
		this.domainObject = domainObject;

		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		IEventManager eventManager = (IEventManager) componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);

		if (oldDomainObject != null) {
			eventManager.unregisterListener(this.predicateModifiedListener,
					ContextualEventType.PREDICATE_MODIFIED_EVENT,
					oldDomainObject);
		}

		eventManager.registerListener(this.predicateModifiedListener,
				ContextualEventType.PREDICATE_MODIFIED_EVENT, domainObject);

		initialize();
	}

	private void updateDictionaryRealmsMap() {
		this.typeMap = new HashMap();
		Set<Realm> enrollmentNames = new HashSet<Realm>();
		UserProfileEnum profile = PlatformUtils.getProfile();
		PolicyEditorRoles role = PolicyEditorRoles.CORPORATE;
		if (profile == UserProfileEnum.CORPORATE)
			role = PolicyEditorRoles.CORPORATE;
		else if (profile == UserProfileEnum.FILESYSTEM)
			role = PolicyEditorRoles.FILESYSTEM;
		else if (profile == UserProfileEnum.PORTAL)
			role = PolicyEditorRoles.PORTAL;
		try {
			enrollmentNames = PolicyServerProxy.client
					.getDictionaryEnrollmentRealms(role);
		} catch (PolicyEditorException exception) {
			LoggingUtil.logWarning(Activator.ID,
					"Failed to retrieve dictionary realms.", exception);
		}

		for (Realm realm : enrollmentNames)
			this.typeMap.put(realm.getName(), realm.getType());
	}

	private void initialize() {
		clsLstCtrl = this; // needed for the buildLabelList that refreshes the list on a separate thread. Useful when the list has many items
		updateDictionaryRealmsMap();

		buildLabelList();
		bldLblLstCtr = 0;
		if (isEditable()) {
			addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					SAPClassListControl.this.removeSelection();
					SAPClassListControl.this.addClassLabel.setFocus();
				}
			});
			createAddClassLabel();
			addDropTarget();
		}
	}

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
			PolicyServerProxy.createBlankComponent(name, this.componentType);
			GlobalState.getInstance().getComponentListPanel(getComponentType())
					.populateList();
			return true;
		}
		return false;
	}

	private void displayComponentNotValidDialog(String name) {
		StringBuffer msg = new StringBuffer();
		msg.append(ApplicationMessages.CLASSLISTCONTROL_NOT_FOUND_MSG1);
		msg.append(name);
		msg.append(ApplicationMessages.CLASSLISTCONTROL_NOT_FOUND_MSG2);
		MessageDialog.openError(getDisplay().getActiveShell(),
				ApplicationMessages.CLASSLISTCONTROL_NOT_FOUND, msg.toString());
	}

	private boolean isNewComponentName(String name) {
		boolean couldBeComponent = (EntityInfoProvider
				.isValidComponentName(name))
				&& (EntityInfoProvider.getExistingComponentName(name,
						this.componentType) == null);

		if (!couldBeComponent) {
			return false;
		}

		if (canAcceptLeafObjects()) {
			SpecType specType = getSpecType();
			LeafObjectType[] leafTypes = getAcceptedLeafObjectTypes(specType);
			for (int i = 0; i < leafTypes.length; ++i) {
				if (leafTypes[i] == LeafObjectType.RESOURCE) {
					if (this.componentType.equals("SERVER")) {
						return false;
					}
					if (isValidResourceAtomName(name)) {
						return false;
					}
				} else if (EntityInfoProvider.getLeafObject(name, leafTypes[i]) != null) {
					return false;
				}
			}
		}

		return PolicyServerProxy.getAllowedEntityTypes().contains(
				getEntityType());
	}

	private void createAddClassLabel() {
		this.addClassLabel = new EditableLabel(this, 32);
		this.addClassLabel.setParentControl(this);
		this.addClassLabel.setText(this.defaultName);

		GridData gridData = new GridData();

		this.addClassLabel.setLayoutData(gridData);

		this.addClassLabel
				.addEditableLabelListener(new EditableLabelListener() {
					public void mouseUp(EditableLabelEvent e) {
					}

					public void mouseDown(EditableLabelEvent e) {
						EditableLabel label = (EditableLabel) e.getSource();
						if ((e.x < 0) || (e.y < 0)
								|| (e.x >= label.getSize().x)
								|| (e.y >= label.getSize().y))
							return;
						SAPClassListControl.this.removeSelection();
						label.setEditing(true);
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
								if (displayCreateComponentDialog(text))
									createComponentReference = true;
								else {
									return false;
								}
							}

							IPredicate refEntry = null;

							String existingComponentName = EntityInfoProvider
									.getExistingComponentName(
											text,
											SAPClassListControl.this.componentType);

							if (existingComponentName != null) {
								text = existingComponentName;
								DomainObjectDescriptor dod = getDescriptorForUnescapedName(
										existingComponentName,
										getComponentType());

								if ((!SAPClassListControl.this
										.canAddObjects(new Object[] { text }))
										|| (!dod.isAccessible())) {
									SAPClassListControl.this.getDisplay()
											.beep();
									if (!SAPClassListControl.this.isAlreadyPopupDialog)
										SAPClassListControl.this
												.displayComponentNotValidDialog(text);
									return false;
								}
							}

							if ((createComponentReference)
									|| (existingComponentName != null))
								refEntry = PredicateHelpers
										.getComponentReference(text,
												SAPClassListControl.this
														.getSpecType());
							else if (SAPClassListControl.this
									.canAcceptLeafObjects()) {
								refEntry = getReferenceForName(
										componentType,
										entityType,
										getAcceptedLeafObjectTypes(getSpecType()),
										text);
							}

							if (refEntry == null) {
								SAPClassListControl.this.getDisplay().beep();
								SAPClassListControl.this
										.displayComponentNotValidDialog(text);
								return false;
							}

							SAPClassListControl.this
									.addElementWithUndo(refEntry);
							SAPClassListControl.this.addLabel(refEntry,clsLstCtrl);

							SAPClassListControl.this.parentPanel.relayout();
							if ((e.originalEvent != null)
									&& (e.originalEvent instanceof SelectionEvent)) {
								SAPClassListControl.this.addClassLabel
										.setText("");
								return false;
							}
						}
						label.setText(SAPClassListControl.this.defaultName);
						return true;
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
						SAPClassListControl.this.addClassLabel
								.setText(SAPClassListControl.this.defaultName);
					}
				});
		this.addClassLabel.label.addTraverseListener(this.traversalListerer);
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
			Menu contextMenu = new Menu(shell, 8);
			final MenuItem openItem = new MenuItem(contextMenu, 8);
			openItem.setText(ApplicationMessages.CLASSLISTCONTROL_OPEN);
			contextMenu.addMenuListener(new MenuListener() {
				public void menuShown(MenuEvent e) {
					DomainObjectDescriptor dod = getDescriptorForUnescapedName(
							thisLabel.getText(), getEntityType(thisSpec));
					if (dod != null)
						openItem.setEnabled(dod.isAccessible());
				}

				public void menuHidden(MenuEvent e) {
				}
			});
			openItem.addListener(13, new Listener() {
				public void handleEvent(Event e) {
					DomainObjectDescriptor dod = getDescriptorForUnescapedName(
							thisLabel.getText(), getEntityType(thisSpec));
					if (dod != null)
						GlobalState.getInstance().loadObjectInEditorPanel(dod);
				}
			});
			thisLabel.setContextMenu(contextMenu);
		}
	}

	/*
	 * private static DomainObjectDescriptor
	 * getDescriptorForUnescapedName(String name, String type) { if ((name ==
	 * null) || (name.length() == 0)) { return null; } Collection collection =
	 * PolicyServerProxy.getEntityList(PolicyServerProxy.escape(name), type);
	 * Iterator iter = collection.iterator(); return (iter.hasNext()) ?
	 * (DomainObjectDescriptor)iter.next() : null; }
	 */
	private void addLabel(IPredicate spec, SAPClassListControl clsLstCtrl) {
		EditableLabel label = new EditableLabel(this, 32);
		label.setParentControl(clsLstCtrl);
		boolean isFirstLabel = false;
		if (this.labelList.size() == 0) {
			isFirstLabel = true;
		}

		this.labelList.add(label);
		this.labelSpecMap.put(label, spec);

		findAndSetLabelText(spec, label);
		label.addDragSource();

		GridData gridData = new GridData();
		label.setLayoutData(gridData);

		setLabelFont(label, spec);

		if (getEntityType(spec) != null) {
			addContextMenu(label, spec);
		}

		if (isEditable())
			label.addEditableLabelListener(new EditableLabelListener() {
				public void mouseUp(EditableLabelEvent e) {
					if (e.isRightMouseButton()) {
						return;
					}
					EditableLabel label = (EditableLabel) e.getSource();
					boolean isShiftPressed = (e.stateMask & 0x20000) != 0;
					boolean isControlPressed = (e.stateMask & 0x40000) != 0;

					if (!isShiftPressed) {
						if (isControlPressed) {
							if ((SAPClassListControl.this.controlForDeselection == label)
									&& (SAPClassListControl.this.selectedControls
											.contains(label))) {
								SAPClassListControl.this.deselectControl(label);
							}
						} else if ((SAPClassListControl.this.currentSelectedControl == null)
								|| (label != SAPClassListControl.this.currentSelectedControl)
								|| (SAPClassListControl.this.selectedControls
										.size() > 1)) {
							SAPClassListControl.this.removeSelection();
							SAPClassListControl.this.selectControl(label);
						}
					}

					SAPClassListControl.this.controlForDeselection = null;
				}

				public void mouseDown(EditableLabelEvent e) {
					if (e.isRightMouseButton()) {
						return;
					}
					EditableLabel label = (EditableLabel) e.getSource();
					boolean isShiftPressed = (e.stateMask & 0x20000) != 0;
					boolean isControlPressed = (e.stateMask & 0x40000) != 0;
					SAPClassListControl.this.controlForDeselection = null;

					if (isShiftPressed) {
						SAPClassListControl.this.selectTo(label);
					} else if (isControlPressed) {
						if (!SAPClassListControl.this.selectedControls
								.contains(label)) {
							SAPClassListControl.this.selectControl(label);
						} else {
							SAPClassListControl.this.controlForDeselection = label;
						}

					} else if (!SAPClassListControl.this.selectedControls
							.contains(label)) {
						SAPClassListControl.this.removeSelection();
						SAPClassListControl.this.selectControl(label);
					}
				}

				public void mouseRightClick(EditableLabelEvent e) {
				}

				public void textChanged(EditableLabelEvent e) {
				}

				public boolean textSaved(EditableLabelEvent e) {
					EditableLabel label = (EditableLabel) e.getSource();
					int index = SAPClassListControl.this.labelList
							.indexOf(label);
					String text = label.getText().trim();
					boolean createComponentReference = false;

					if (text.length() != 0) {
						if (isNewComponentName(text)) {
							if (displayCreateComponentDialog(text))
								createComponentReference = true;
							else {
								return false;
							}
						}

						IPredicate refEntry = null;
						String existingComponentName = EntityInfoProvider
								.getExistingComponentName(text,
										SAPClassListControl.this.componentType);

						if (existingComponentName != null) {
							text = existingComponentName;
							DomainObjectDescriptor dod = getDescriptorForUnescapedName(
									existingComponentName, getComponentType());
							if ((!SAPClassListControl.this
									.canAddObjects(new Object[] { text }))
									|| (!dod.isAccessible())) {
								SAPClassListControl.this.getDisplay().beep();
								if (!SAPClassListControl.this.isAlreadyPopupDialog)
									SAPClassListControl.this
											.displayComponentNotValidDialog(text);
								return false;
							}
						}

						if ((createComponentReference)
								|| (existingComponentName != null))
							refEntry = PredicateHelpers.getComponentReference(
									existingComponentName,
									SAPClassListControl.this.getSpecType());
						else if (SAPClassListControl.this
								.canAcceptLeafObjects()) {
							refEntry = getReferenceForName(componentType,
									entityType,
									getAcceptedLeafObjectTypes(getSpecType()),
									text);
						}

						if (refEntry == null) {
							SAPClassListControl.this.getDisplay().beep();
							SAPClassListControl.this
									.displayComponentNotValidDialog(text);
							return false;
						}

						PredicateHelpers.removePredicateAt(
								SAPClassListControl.this.domainObject, index);
						PredicateHelpers.insertPredicateAt(
								SAPClassListControl.this.domainObject,
								refEntry, index);

						label.setEditing(false);
						//we don't want to perform a refresh when we are editing a label because this will cause us to clear the existing labellist.
						performRefresh = false;
						SAPClassListControl.this.parentPanel.relayout();
						//we can set the performRefresh flag back on after the relayout function has been performed.
						performRefresh = true;
						SAPClassListControl.this.selectControl(label);
					} else {
						SAPClassListControl.this.deleteIndex(index);
					}

					return true;
				}

				public void upArrow(EditableLabelEvent e) {
					EditableLabel label = (EditableLabel) e.getSource();
					EditableLabel newSelectedControl = label;
					boolean isShiftPressed = (e.stateMask & 0x20000) != 0;
					int index = SAPClassListControl.this.labelList
							.indexOf(label);
					if (index > 0) {
						newSelectedControl = (EditableLabel) SAPClassListControl.this.labelList
								.get(index - 1);
					}
					if (!isShiftPressed) {
						SAPClassListControl.this.removeSelection();
						SAPClassListControl.this
								.selectControl(newSelectedControl);
					} else {
						SAPClassListControl.this.selectTo(newSelectedControl);
					}
				}

				public void downArrow(EditableLabelEvent e) {
					EditableLabel label = (EditableLabel) e.getSource();
					EditableLabel newSelectedControl = label;
					boolean isShiftPressed = (e.stateMask & 0x20000) != 0;
					int index = SAPClassListControl.this.labelList
							.indexOf(label);
					if (index + 1 < SAPClassListControl.this.labelList.size()) {
						newSelectedControl = (EditableLabel) SAPClassListControl.this.labelList
								.get(index + 1);
					}
					if (!isShiftPressed) {
						SAPClassListControl.this.removeSelection();
						SAPClassListControl.this
								.selectControl(newSelectedControl);
					} else {
						SAPClassListControl.this.selectTo(newSelectedControl);
					}
				}

				public void delete(EditableLabelEvent e) {
					SAPClassListControl.this
							.deleteSelection(e.originalEvent instanceof DragSourceEvent);
				}

				public boolean startEditing(EditableLabelEvent e) {
					SAPClassListControl.this.removeSelection();
					return true;
				}

				public void fireCancelEditing(EditableLabelEvent e) {
				}
			});
		if (isFirstLabel) {
			label.label.addFocusListener(new FocusAdapter() {
				public void focusGained(FocusEvent e) {
					SAPClassListControl.this.removeSelection();
					SAPClassListControl.this
							.selectControl((EditableLabel) SAPClassListControl.this.labelList
									.get(0));
				}

				public void focusLost(FocusEvent e) {
					SAPClassListControl.this.removeSelection();
				}
			});
		} else
			label.label.addTraverseListener(this.traversalListerer);
	}

	/*
	 * private static IPredicate getReferenceForName(String componentType,
	 * EntityType entityType, LeafObjectType[] types, String text) { IPredicate
	 * res = null; for (int i = 0; (i < types.length) && (res == null); ++i) {
	 * if (types[i] == LeafObjectType.RESOURCE) { if
	 * (componentType.equals(ComponentEnum.PORTAL)) { res = new
	 * Relation(RelationOp.EQUALS, ResourceAttribute.PORTAL_URL,
	 * ResourceAttribute.PORTAL_URL.build(text)); } else if
	 * (componentType.equals(ComponentEnum.SERVER)) { ResourceAttribute attr =
	 * ResourceAttribute.forNameAndType( "name", "server"); res = new
	 * Relation(RelationOp.EQUALS, attr, attr.build(text)); } else if
	 * (componentType.equals(ComponentEnum.OBJECT)) { ResourceAttribute attr =
	 * ResourceAttribute.forNameAndType( "name", "object"); res = new
	 * Relation(RelationOp.EQUALS, attr, attr.build(text)); } else if
	 * (isValidResourceAtomName(text)) { res =
	 * PredicateHelpers.getResourceReference(text); } } else { LeafObject leaf =
	 * EntityInfoProvider.getLeafObject(text, types[i]); if (leaf != null) { res
	 * = PredicateHelpers.getLeafReference(leaf); } } } return res; }
	 */
	public void relayout() {
		if (PredicateHelpers.getRealPredicateCount(this.domainObject) != this.labelList
				.size()) {
			if(performRefresh)refresh();
		} else {
			int labelIndex = 0;
			for (int i = 0; i < this.domainObject.predicateCount(); ++i) {
				IPredicate pred = this.domainObject.predicateAt(i);
				if (pred instanceof IDSpec) {
					IDSpec spec = (IDSpec) pred;
					EditableLabel label = (EditableLabel) this.labelList
							.get(labelIndex);
					if ((label == null)
							|| (!spec.getName().equals(label.getText()))) {
						refresh();
					}
					++labelIndex;
				}
			}
		}

		if (this.addClassLabel != null) {
			this.addClassLabel.moveBelow(null);
		}

		if (this.labelList.size() > 0)
			this.tabPrimaryControl = ((EditableLabel) this.labelList.get(0));
		else {
			this.tabPrimaryControl = this.addClassLabel;
		}
		if ((this.tabPrimaryControl != null)
				&& (!this.tabPrimaryControl.isDisposed()))
			setTabList(new Control[] { this.tabPrimaryControl });
	}

	private void refresh() {
		setRedraw(false);
		for (int i = 0; i < this.labelList.size(); ++i) {
			EditableLabel label = (EditableLabel) this.labelList.get(i);
			label.dispose();
		}

		this.labelList.clear();
		this.labelSpecMap.clear();
		this.selectedControls.clear();
		this.currentSelectedControl = null;

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

	private void setLabelFont(EditableLabel label, IPredicate spec) {
		if (spec instanceof IDSpec) {
			label.setLabelFont(FontBundle.DEFAULT_UNDERLINE);
		} else if (spec instanceof Relation) {
			IExpression exp = ((Relation) spec).getLHS();
			if ((exp instanceof IDSubjectAttribute)
					&& (((exp == SubjectAttribute.HOST_LDAP_GROUP) || (exp == SubjectAttribute.USER_LDAP_GROUP))))
				label.setLabelFont(FontBundle.DEFAULT_BOLD);
			else
				label.setLabelFont(FontBundle.DEFAULT_NORMAL);
		} else {
			label.setLabelFont(this.knownClassFont);
		}
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
			if ((domainName != null)
					&& (this.typeMap.containsKey(domainName))
					&& (((EnrollmentType) this.typeMap.get(domainName))
							.equals(EnrollmentType.value3)))
				label.setImage(SharePointImageConstants.SHAREPOINT_USER);
			else
				label.setImage(ImageBundle.USER_IMG);
		} else if (object.getType() == LeafObjectType.USER_GROUP) {
			if ((domainName != null)
					&& (this.typeMap.containsKey(domainName))
					&& (((EnrollmentType) this.typeMap.get(domainName))
							.equals(EnrollmentType.value3)))
				label.setImage(SharePointImageConstants.SHAREPOINT_USERGROUP);
			else
				label.setImage(ImageBundle.IMPORTED_USER_GROUP_IMG);
		} else
			label.setImage(ObjectLabelImageProvider.getImage(object));
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
				if ((DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP
						.containsKey(lhs))
						&& (rhsValue instanceof Long)) {
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
					labelName = "" + rhsValue;
					;
				}
			} else if ((lhs instanceof ResourceAttribute)
					&& (rhs instanceof Constant)) {
				String val = ((Constant) rhs).getRepresentation();
				String res = val.replaceAll("[\\\\]+", "\\\\");
				labelName = (val.startsWith("\\\\")) ? "\\" + res : res;
			} else if (rhs instanceof Constant) {
				labelName = ((Constant) rhs).getRepresentation();
			} else {
				labelName = "" + rhsValue;
				;
			}

		}

		labelToUpdate.setText(labelName);
	}

	private void formatLabelForError(EditableLabel labelToUpdate) {
		labelToUpdate.setForeground(ResourceManager.getColor(3));
	}

	private void deleteIndex(int index) {
		CompositionUndoElement undoElement = new CompositionUndoElement();

		undoElement.setDomainObjectId(GlobalState.getInstance()
				.getCurrentObject().getId());
		undoElement.setOp(CompositionUndoElementOp.REMOVE_REF);
		undoElement.setControlId(this.controlId);
		undoElement.setIndex(((SAPCompositionControl) getParent())
				.indexOfObject(this));
		// undoElement.setIndex(((CompositionControl)getParent()).indexOfObject(this));
		undoElement.setIndexArray(new ArrayList());
		undoElement.setRefArray(new ArrayList());
		EditableLabel label = (EditableLabel) this.labelList.get(index);
		IPredicate spec = (IPredicate) this.labelSpecMap.get(label);
		label.dispose();
		undoElement.getIndexArray().add(new Integer(index));
		undoElement.getRefArray().add(spec);
		this.labelList.remove(label);
		this.labelSpecMap.remove(label);
		PredicateHelpers.removePredicateAt(this.domainObject, index);

		GlobalState.getInstance().addUndoElement(undoElement);
		this.parentPanel.relayout();
	}

	private void deleteSelection(boolean fromDropEvent) {
		CompositionUndoElement undoElement = new CompositionUndoElement();
		undoElement.setDomainObjectId(GlobalState.getInstance()
				.getCurrentObject().getId());

		undoElement.setContinuation(fromDropEvent);
		undoElement.setOp(CompositionUndoElementOp.REMOVE_REF);
		undoElement.setControlId(this.controlId);

		undoElement.setIndex(((SAPCompositionControl) getParent())
				.indexOfObject(this));
		undoElement.setIndexArray(new ArrayList());
		undoElement.setRefArray(new ArrayList());
		for (int i = 0; i < this.selectedControls.size(); ++i) {
			EditableLabel label = (EditableLabel) this.selectedControls.get(i);
			IPredicate spec = (IPredicate) this.labelSpecMap.get(label);
			label.dispose();
			undoElement.getIndexArray().add(
					new Integer(this.labelList.indexOf(label)));
			undoElement.getRefArray().add(spec);
			this.labelList.remove(label);
			this.labelSpecMap.remove(label);
			this.domainObject.removePredicate(spec);
			PredicateHelpers.rebalanceDomainObject(this.domainObject,
					this.domainObject.getOp());
		}

		GlobalState.getInstance().addUndoElement(undoElement);

		this.currentSelectedControl = null;
		this.selectedControls.clear();
		this.parentPanel.relayout();
	}

	private void removeSelection() {
		if (this.currentSelectedControl != null) {
			this.currentSelectedControl.setSelected(false);
		}
		this.currentSelectedControl = null;
		for (int i = 0; i < this.selectedControls.size(); ++i) {
			EditableLabel label = (EditableLabel) this.selectedControls.get(i);
			if (label != null)
				label.setSelected(false);
		}
		this.selectedControls.clear();
	}

	public void setParentPanel(IEditorPanel parent) {
		this.parentPanel = parent;
	}

	protected void selectTo(EditableLabel label) {
		EditableLabel originalSelection = this.currentSelectedControl;
		int currentControlIndex;
		if (this.currentSelectedControl == null) {
			currentControlIndex = 0;
			originalSelection = (EditableLabel) this.labelList.get(0);
		} else {
			currentControlIndex = this.labelList
					.indexOf(this.currentSelectedControl);
		}
		int selectionControlIndex = this.labelList.indexOf(label);
		int endIndex;
		int startIndex;
		if (currentControlIndex < selectionControlIndex) {
			startIndex = currentControlIndex;
			endIndex = selectionControlIndex;
		} else {
			startIndex = selectionControlIndex;
			endIndex = currentControlIndex;
		}

		removeSelection();

		for (int i = startIndex; i <= endIndex; ++i) {
			EditableLabel el = (EditableLabel) this.labelList.get(i);
			el.setSelected(true);
			this.selectedControls.add(el);
		}

		this.currentSelectedControl = originalSelection;

		label.setSelected(true);
	}

	private void selectControl(EditableLabel label) {
		this.currentSelectedControl = label;
		this.currentSelectedControl.setSelected(true);
		if (!this.selectedControls.contains(label))
			this.selectedControls.add(this.currentSelectedControl);
	}

	private void deselectControl(EditableLabel label) {
		if (this.currentSelectedControl == label) {
			this.currentSelectedControl = null;
		}
		if (this.selectedControls.contains(label)) {
			this.selectedControls.remove(label);
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
							if (draginType != SpecType.RESOURCE
									&& draginType != SpecType.PORTAL) {
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
						addLabel(specArray[i], clsLstCtrl);
					}
					GlobalState.getInstance().addUndoElement(undoElement);
					parentPanel.relayout();
				}
			}
		});
	}

	/*
	 * private void addDropTarget() { int operations = 19; DropTarget target =
	 * new DropTarget(this, operations);
	 * 
	 * final PolicyObjectTransfer transfer = PolicyObjectTransfer.getInstance();
	 * Transfer[] types = { transfer }; target.setTransfer(types);
	 * 
	 * target.addDropListener(new DropTargetAdapter() { public void
	 * dragEnter(DropTargetEvent event) { if (event.detail == 16) { if
	 * ((event.operations & 0x1) != 0) event.detail = 2; else { event.detail =
	 * 0; } } int i = 0; break label158: IPredicate[] spec; if
	 * (transfer.isSupportedType(event.dataTypes[i])) { event.currentDataType =
	 * event.dataTypes[i]; spec = (IPredicate[])transfer
	 * .nativeToJava(event.currentDataType); }try { SpecType draginType =
	 * PredicateHelpers.getPredicateType(spec[0]); SpecType containerType =
	 * ClassListControl.this.getSpecType(); if ((draginType == SpecType.PORTAL)
	 * && (containerType == SpecType.RESOURCE)) { return; } if (draginType ==
	 * containerType) break label138; event.detail = 0; label138: label158:
	 * return; } catch (Exception e) { LoggingUtil.logError(Activator.ID,
	 * "error occurs when dragging over", e);
	 * 
	 * if (!ClassListControl.this.canAddObjects(spec)) { event.detail = 0;
	 * return;
	 * 
	 * ++i; if (i < event.dataTypes.length); } } }
	 * 
	 * public void dragOver(DropTargetEvent event) { event.feedback = 9; }
	 * 
	 * public void dragOperationChanged(DropTargetEvent event) { if
	 * (event.detail == 16) if ((event.operations & 0x2) != 0) event.detail = 2;
	 * else event.detail = 0; }
	 * 
	 * public void dropAccept(DropTargetEvent event) { IPredicate[] spec =
	 * (IPredicate[])transfer .nativeToJava(event.currentDataType);
	 * 
	 * if (!ClassListControl.this.canAddObjects(spec)) { event.detail = 0;
	 * ClassListControl.this.getDisplay().beep(); } }
	 * 
	 * public void drop(DropTargetEvent event) { if
	 * (transfer.isSupportedType(event.currentDataType)) { IPredicate[]
	 * specArray = (IPredicate[])transfer .nativeToJava(event.currentDataType);
	 * CompositionUndoElement undoElement =
	 * ClassListControl.this.createAddRefUndoElement(); for (int i = 0; i <
	 * specArray.length; ++i) { undoElement.getIndexArray().add( new Integer(
	 * PredicateHelpers
	 * .getRealPredicateCount(ClassListControl.this.domainObject)));
	 * undoElement.getRefArray().add(specArray[i]);
	 * 
	 * PredicateHelpers.addPredicate(ClassListControl.this.domainObject,
	 * specArray[i]); ClassListControl.this.addLabel(specArray[i]); }
	 * GlobalState.getInstance().addUndoElement(undoElement);
	 * ClassListControl.this.parentPanel.relayout(); } } }); }
	 */
	public void addElementWithUndo(IPredicate spec) {
		CompositionUndoElement undoElement = createAddRefUndoElement();

		undoElement.getIndexArray().add(
				new Integer(PredicateHelpers
						.getRealPredicateCount(this.domainObject)));
		undoElement.getRefArray().add(spec);

		this.domainObject.addPredicate(spec);
		PredicateHelpers.rebalanceDomainObject(this.domainObject,
				this.domainObject.getOp());

		GlobalState.getInstance().addUndoElement(undoElement);
	}

	public void addLeafElementsWithUndo(List<LeafObject> leafObjectList) {
		if (leafObjectList == null) {
			throw new NullPointerException("specList cannot be null.");
		}

		if (leafObjectList.size() > 0) {
			boolean modified = false;
			CompositionUndoElement undoElement = createAddRefUndoElement();

			Iterator iter = leafObjectList.iterator();
			while (iter.hasNext()) {
				LeafObject nextLeaf = (LeafObject) iter.next();

				IPredicate spec = PredicateHelpers.getLeafReference(nextLeaf);
				boolean found = false;
				for (int i = 0; i < this.domainObject.predicateCount(); ++i) {
					Object predicateElement = this.domainObject.predicateAt(i);
					if (!(predicateElement instanceof PredicateConstants)) {
						IPredicate predicate = (IPredicate) predicateElement;
						if (spec.toString().equals(predicate.toString())) {
							found = true;
							break;
						}
					}
				}

				if (found)
					continue;
				undoElement.getIndexArray().add(
						new Integer(PredicateHelpers
								.getRealPredicateCount(this.domainObject)));
				undoElement.getRefArray().add(spec);

				this.domainObject.addPredicate(spec);
				modified = true;
			}

			if (modified) {
				PredicateHelpers.rebalanceDomainObject(this.domainObject,
						this.domainObject.getOp());
				GlobalState.getInstance().addUndoElement(undoElement);
			}
		}
	}

	public boolean canAddObjects(Object[] candidates) {
		this.isAlreadyPopupDialog = false;
		IHasId parentDomainObject = this.parentPanel.getDomainObject();
		String parentName = DomainObjectHelper.getName(parentDomainObject);

		Set deniedNames = new HashSet();

		if (!DomainObjectHelper.getObjectType(parentDomainObject).equals(
				ApplicationMessages.DOMAINOBJECTHELPER_POLICY_TYPE)) {
			deniedNames.add(parentName);

			Iterator localIterator = PolicyServerProxy.getReferringComponents(
					parentName, getEntityType()).iterator();

			while (localIterator.hasNext()) {
				DomainObjectDescriptor desc = (DomainObjectDescriptor) localIterator
						.next();
				deniedNames.add(desc.getName());
			}

		}

		for (Object candidate : candidates) {
			String candidateName = "";
			if (candidate instanceof String) {
				candidateName = (String) candidate;
			} else {
				if (!(candidate instanceof IDSpecRef))
					continue;
				candidateName = ((IDSpecRef) candidate).getReferencedName();
			}

			if (deniedNames.contains(candidateName)) {
				this.isAlreadyPopupDialog = true;
				return false;
			}

			int i = 0;
			for (int n = this.domainObject.predicateCount(); i < n; ++i) {
				Object predicate = this.domainObject.predicateAt(i);
				String existingName = "";
				if (predicate instanceof String)
					existingName = (String) candidate;
				else if (predicate instanceof IDSpecRef) {
					existingName = ((IDSpecRef) predicate).getReferencedName();
				}
				if (candidateName.equals(existingName)) {
					return false;
				}
			}
		}

		return true;
	}

	private CompositionUndoElement createAddRefUndoElement() {
		CompositionUndoElement undoElement = new CompositionUndoElement();
		undoElement.setDomainObjectId(GlobalState.getInstance()
				.getCurrentObject().getId());
		undoElement.setOp(CompositionUndoElementOp.ADD_REF);

		undoElement.setIndex(((SAPCompositionControl) getParent())
				.indexOfObject(this));
		undoElement.setIndexArray(new ArrayList());
		undoElement.setRefArray(new ArrayList());
		undoElement.setControlId(this.controlId);
		return undoElement;
	}

	public void cleanup() {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		IEventManager eventManager = (IEventManager) componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
		eventManager.unregisterListener(this.predicateModifiedListener,
				ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT,
				this.domainObject);
	}

	public void dispose() {
		cleanup();
		super.dispose();
	}

	public int getControlId() {
		return this.controlId;
	}

	public void setControlId(int controlId) {
		this.controlId = controlId;
	}

	public boolean isEditable() {
		return this.editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public EntityType getEntityType() {
		return this.entityType;
	}

	public SpecType getSpecType() {
		return this.specType;
	}

	public String getComponentType() {
		return this.componentType;
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
		return new LeafObjectType[0];
	}

	public void copy() {
		Clipboard clipboard = new Clipboard(getDisplay());

		IPredicate[] specArray = new IPredicate[this.selectedControls.size()];
		for (int i = 0; i < this.selectedControls.size(); ++i) {
			specArray[i] = ((IPredicate) this.labelSpecMap
					.get(this.selectedControls.get(i)));
		}

		clipboard.setContents(new Object[] { specArray },
				new Transfer[] { PolicyObjectTransfer.getInstance() });
	}

	public void paste() {
		removeSelection();
		Clipboard clipboard = new Clipboard(getDisplay());
		IPredicate[] specArray = (IPredicate[]) clipboard
				.getContents(PolicyObjectTransfer.getInstance());
		if ((specArray == null) || (!canAddObjects(specArray))) {
			getDisplay().beep();
			return;
		}
		if ((specArray == null)
				|| (getSpecType() != PredicateHelpers
						.getPredicateType(specArray[0])))
			return;
		CompositionUndoElement undoElement = createAddRefUndoElement();
		for (int i = 0; i < specArray.length; ++i) {
			undoElement.getIndexArray().add(
					new Integer(PredicateHelpers
							.getRealPredicateCount(this.domainObject)));
			undoElement.getRefArray().add(specArray[i]);

			PredicateHelpers.addPredicate(this.domainObject, specArray[i]);
			addLabel(specArray[i],clsLstCtrl);
		}
		GlobalState.getInstance().addUndoElement(undoElement);
		this.parentPanel.relayout();
	}

	public void cut() {
		copy();
		deleteSelection(false);
	}

	public boolean canAcceptLeafObjects() {
		return this.acceptLeafObjects;
	}

	public void setAcceptLeafObjects(boolean acceptLeafObjects) {
		this.acceptLeafObjects = acceptLeafObjects;
	}

	private static boolean isValidResourceAtomName(String name) {
		return (name.toLowerCase().startsWith("object:"));
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

	private static IPredicate getReferenceForName(String componentType,
			EntityType entityType, LeafObjectType[] types, String text) {
		IPredicate res = null;
		for (int i = 0; i < types.length && res == null; i++) {
			if (types[i] == LeafObjectType.RESOURCE) {
				if (componentType.equals("OBJECT")) {
					ResourceAttribute attr = ResourceAttribute.forNameAndType(
							"name", "object");
					res = new Relation(RelationOp.EQUALS, attr, attr
							.build(text));
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

	private class PredicateModifiedListener implements IContextualEventListener {
		private PredicateModifiedListener() {
		}

		public void onEvent(IContextualEvent event) {
			SAPClassListControl.this.parentPanel.relayout();
		}
	}
}
