/*
 * Created on Mar 14, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.policystudio.editor.SAP;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import com.bluejungle.destiny.policymanager.editor.IEditorPanel;
import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.CompositionUndoElement;
import com.bluejungle.destiny.policymanager.ui.CompositionUndoElementOp;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.ILeafObjectBrowser;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.LeafObjectBrowserFactory;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.engine.destiny.DefaultFileResourceHandler;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates
 *          .xml#2 $:
 */

public class SAPCompositionControl extends Composite {
	private static final int CONTROL_HEIGHT = 20;
	private static final Point SIZE_REMOVE_BUTTON = new Point(15, 15);
	private static final Point SIZE_LABEL = new Point(40, 20);
	private static final Point SIZE_OP_COMBO = new Point(60, 20);
	private static final Point SIZE_MEMBER_COMPOSITE = new Point(200, 70);
	private static final LeafObjectBrowserFactory leafObjectBrowserFactoy;
	static {
		leafObjectBrowserFactoy = (LeafObjectBrowserFactory) ComponentManagerFactory
				.getComponentManager().getComponent(
						LeafObjectBrowserFactory.class);
	}

	private int controlId;
	private String defaultName;
	private IEditorPanel editorPanel = null;
	private List<Combo> opControlArray = new ArrayList<Combo>();
	private List<SAPClassListControl> classListArray = new ArrayList<SAPClassListControl>();
	private List<Button> removeButtonArray = new ArrayList<Button>();
	private List<Label> labelControlArray = new ArrayList<Label>();
	private List<Button> browseButtonArray = new ArrayList<Button>();
	private List<Label> readOnlyOpArray = new ArrayList<Label>();
	private List<Control> tabListArray = new ArrayList<Control>();
	private String[] operators = new String[] { "in", "not in" };
	private Button addButton = null;
	private CompositePredicate domainObject = null;
	private String labelString = null;
	private boolean editable = true;
	private boolean showAdd = true;
	private boolean showRemove = true;
	private boolean maxOneLine = false;
	private boolean acceptLeafObjects = true;
	private EntityType entityType;
	private SpecType specType;
	private String componentType = null;
	private String lookupLabel;
	private IContextualEventListener predicateModifiedListner = new PredicateModifiedListener();

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param style
	 */
	public SAPCompositionControl(Composite parent, int style, String name,
			String labelString, CompositePredicate domainObject,
			IEditorPanel editorPanel, int controlId, boolean editable,
			boolean acceptLeafObjects, SpecType specType, String componentType,
			String lookupLabel) {
		super(parent, style);
		this.componentType = componentType;
		this.lookupLabel = lookupLabel;
		doSetup(name, labelString, domainObject, editorPanel, controlId,
				editable, acceptLeafObjects, false, new String[] { "in",
						"not in" }, specType);
	}

	public SAPCompositionControl(Composite parent, int style, String name,
			String labelString, CompositePredicate domainObject,
			IEditorPanel editorPanel, int controlId, boolean editable,
			boolean acceptLeafObjects, boolean maxOneLine, String[] operators,
			SpecType specType, String componentType, String lookupLabel) {
		super(parent, style);
		this.componentType = componentType;
		this.lookupLabel = lookupLabel;
		doSetup(name, labelString, domainObject, editorPanel, controlId,
				editable, acceptLeafObjects, maxOneLine, operators, specType);
	}

	private void doSetup(String name, String labelString,
			CompositePredicate domainObject, IEditorPanel editorPanel,
			int controlId, boolean editable, boolean acceptLeafObjects,
			boolean maxOneLine, String[] operators, SpecType specType) {
		this.defaultName = name;
		this.labelString = labelString;
		this.domainObject = domainObject;
		this.controlId = controlId;
		this.maxOneLine = maxOneLine;
		this.operators = operators;
		this.acceptLeafObjects = acceptLeafObjects;
		setSpecType(specType);
		setEntityType(EntityType.COMPONENT);

		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		IEventManager eventManager = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
		eventManager.registerListener(predicateModifiedListner,
				ContextualEventType.PREDICATE_MODIFIED_EVENT, domainObject);

		this.editorPanel = editorPanel;
		setEditable(editable);
		initialize();
		relayout();

		addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				cleanup();
			}
		});
	}

	/*
	 * AW Note: refresh disposes UI widget and recreate them. If you call this
	 * from a widget event handler, call refreshLater instead of refresh. See
	 * bug 585
	 */
	protected void refreshLater() {
		if (isDisposed())
			return;
		getDisplay().asyncExec(new Runnable() {

			public void run() {
				refresh();
			}
		});
	}

	/**
	 * refresh UI based on underlying domain object
	 */
	public void refresh() {
		if (isDisposed())
			return;
		((Control) editorPanel).setRedraw(false);
		disposeWidgetArray(labelControlArray);
		disposeWidgetArray(removeButtonArray);
		disposeWidgetArray(opControlArray);
		disposeWidgetArray(classListArray);
		disposeWidgetArray(browseButtonArray);
		disposeWidgetArray(readOnlyOpArray);

		labelControlArray.clear();
		removeButtonArray.clear();
		opControlArray.clear();
		classListArray.clear();
		browseButtonArray.clear();
		readOnlyOpArray.clear();
		tabListArray.clear();

		if (addButton != null) {
			addButton.dispose();
			addButton = null;
		}

		initialize();
		relayoutParent();
		((Control) editorPanel).setRedraw(true);
	}

	private void disposeWidgetArray(List<? extends Widget> widgets) {
		for (Widget widget : widgets) {
			widget.dispose();
		}
	}

	public void setDomainObject(CompositePredicate domainObject) {
		this.domainObject = domainObject;
	}

	public void initialize() {
		if (maxOneLine) {
			if (PredicateHelpers.getRealPredicateCount(domainObject) > 0) {
				showAdd = false;
				showRemove = true;
			} else {
				showAdd = true;
				showRemove = false;
			}
		}

		for (int i = 0; i < domainObject.predicateCount(); i++) {
			IPredicate spec = (IPredicate) domainObject.predicateAt(i);
			if (spec instanceof CompositePredicate) {
				addExpressionControls((CompositePredicate) spec);
			}
		}
		if ((addButton == null || addButton.isDisposed()) && isEditable()
				&& showAdd) {
			addAddButton();
		}

	}

	/**
     * 
     */
	private void addAddButton() {
		addButton = new Button(this, SWT.FLAT | SWT.CENTER);
		addButton.setText(ApplicationMessages.COMPOSITIONCONTROL_ADD);
		addButton
				.setToolTipText(ApplicationMessages.COMPOSITIONCONTROL_ADD_CONDITION);
		addButton.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// FIXME create the correct object
				Button addButton = (Button) e.getSource();
				addButton.setEnabled(false);
				getDisplay().update();

				CompositePredicate member = new CompositePredicate(
						BooleanOp.OR, new ArrayList<IPredicate>());
				PredicateHelpers.rebalanceDomainObject(member, BooleanOp.OR);
				PredicateHelpers.addPredicate(domainObject, member);
				addExpressionControls(member);

				addAddUndoElement(member);

				refreshLater();
			}
		});

	}

	/**
	 * @param spec
	 */
	private void addExpressionControls(CompositePredicate spec) {
		Label label = new Label(this, SWT.RIGHT);
		String str;
		if (labelControlArray.size() == 0) {
			str = labelString;
		} else {
			str = ApplicationMessages.COMPOSITIONCONTROL_AND;
		}
		label.setEnabled(isEditable());
		label.setText(str);
		label.setBackground(getParent().getBackground());
		labelControlArray.add(label);

		SAPClassListControl c = new SAPClassListControl(this, getEntityType(),
				getSpecType(), getComponentType(), defaultName,
				isEditable() ? SWT.BORDER : SWT.NONE);
		c.setAcceptLeafObjects(acceptLeafObjects);
		c.setEditable(isEditable());
		c.setParentPanel(editorPanel);
		if (PredicateHelpers.isNegationPredicate(spec)) {
			c.setDomainObject(null, (CompositePredicate) spec.predicateAt(0));
		} else {
			c.setDomainObject(null, spec);
		}
		c.setControlId(controlId);
		classListArray.add(c);

		if (isEditable()) {
			if (showRemove) {
				Button removeButton = new Button(this, SWT.FLAT | SWT.CENTER);
				removeButton
						.setText(ApplicationMessages.COMPOSITIONCONTROL_REMOVE);
				removeButton
						.setToolTipText(ApplicationMessages.COMPOSITIONCONTROL_REMOVE_CONDITION);
				removeButton.setCursor(ResourceManager
						.getCursor(SWT.CURSOR_HAND));
				removeButtonArray.add(removeButton);
				removeButton.addSelectionListener(new SelectionListener() {

					public void widgetSelected(SelectionEvent e) {
						Button b = (Button) e.getSource();
						int index = removeButtonArray.indexOf(b);
						removeExpressionControls(index);
						CompositePredicate spec = (CompositePredicate) PredicateHelpers
								.removePredicateAt(domainObject, index);

						addRemoveUndoElement(index, spec);

						refreshLater();
					}

					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			}

			if (operators.length > 0) { // setup combo if we have operator
				// choices
				Combo combo = new Combo(this, SWT.SINGLE | SWT.BORDER
						| SWT.READ_ONLY);
				for (int i = 0; i < operators.length; i++) {
					combo.add(operators[i]);
				}
				if (PredicateHelpers.isNegationPredicate(spec)) {
					combo.setText(operators[1]);
				} else {
					combo.setText(operators[0]);
				}
				opControlArray.add(combo);
				combo.addModifyListener(new ModifyListener() {

					public void modifyText(ModifyEvent e) {
						Combo source = (Combo) e.getSource();
						int index = opControlArray.indexOf(source);
						String newText = source.getText();
						CompositePredicate spec = (CompositePredicate) PredicateHelpers
								.getPredicateAt(domainObject, index);
						if (PredicateHelpers.isNegationPredicate(spec)
								&& operators[0].equals(newText)) {
							IPredicate oldValue = PredicateHelpers
									.removePredicateAt(domainObject, index);
							IPredicate negation = spec.predicateAt(0);
							PredicateHelpers.insertPredicateAt(domainObject,
									negation, index);
							addReplaceUndoElement(index, oldValue, negation);
						} else if (!PredicateHelpers.isNegationPredicate(spec)
								&& operators[1].equals(newText)) {
							IPredicate oldValue = PredicateHelpers
									.removePredicateAt(domainObject, index);
							IPredicate negation = PredicateHelpers
									.getNegationOfPredicate(spec);
							PredicateHelpers.insertPredicateAt(domainObject,
									negation, index);
							addReplaceUndoElement(index, oldValue, negation);
						}
					}
				});
			}

			if (canBrowse()) {
				Button browseButton = new Button(this, SWT.FLAT | SWT.CENTER);
				if (lookupLabel == null) {
					browseButton
							.setText(ApplicationMessages.COMPOSITIONCONTROL_LOOKUP);
				} else {
					browseButton.setText(lookupLabel);
				}
				browseButton.setVisible(false);
				browseButton.setCursor(ResourceManager
						.getCursor(SWT.CURSOR_HAND));
				browseButtonArray.add(browseButton);
				SpecType type = getSpecType();
				if (type == SpecType.RESOURCE) {
					browseButton.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							Button b = (Button) e.getSource();
							int index = browseButtonArray.indexOf(b);
							Shell shell = getDisplay().getActiveShell();
							DirectoryDialog dialog = new DirectoryDialog(shell);
							dialog
									.setText(ApplicationMessages.COMPOSITIONCONTROL_BROWSE_FOR_DIRECTORY);
							dialog
									.setMessage(ApplicationMessages.COMPOSITIONCONTROL_CHOOSE_DIRECTORY);
							if (dialog.open() != null) {
								String path = DefaultFileResourceHandler
										.getCanonicalName(
												dialog.getFilterPath())
										.replaceAll("^file:(///)?", "")
										.replace('/', File.separatorChar);
								if (!path.endsWith("\\")) {
									path = path + "\\**";
								} else {
									path = path + "**";
								}
								IPredicate pred = PredicateHelpers
										.getResourceReference(path);
								((SAPClassListControl) classListArray.get(index))
										.addElementWithUndo(pred);
								((SAPClassListControl) classListArray.get(index))
										.relayout();
							}
							relayoutParent();
						}
					});
				} else if (type == SpecType.PORTAL) {
					browseButton.addSelectionListener(new SelectionAdapter() {

						@SuppressWarnings("unchecked")
						@Override
						public void widgetSelected(SelectionEvent e) {
							Button b = (Button) e.getSource();
							int index = browseButtonArray.indexOf(b);
							Shell shell = getDisplay().getActiveShell();
							ILeafObjectBrowser leafObjectBrowser = leafObjectBrowserFactoy
									.getLeafObjectBrowser(getSpecType(), shell);
							if (leafObjectBrowser.open() == Dialog.OK) {
								List<String> itemsToAdd = leafObjectBrowser
										.getItemsToReturn();
								for (String item : itemsToAdd) {
									IPredicate pred = new Relation(
											RelationOp.EQUALS,
											ResourceAttribute.PORTAL_URL,
											ResourceAttribute.PORTAL_URL
													.build(item));
									((SAPClassListControl) classListArray
											.get(index))
											.addElementWithUndo(pred);
									((SAPClassListControl) classListArray
											.get(index)).relayout();
								}
								relayoutParent();
							}
						}
					});
				} else {
					browseButton.addSelectionListener(new SelectionAdapter() {
						@Override
						@SuppressWarnings("unchecked")
						public void widgetSelected(SelectionEvent e) {
							Button b = (Button) e.getSource();
							int index = browseButtonArray.indexOf(b);
							Shell shell = getDisplay().getActiveShell();

							ILeafObjectBrowser leafObjectBrowser = leafObjectBrowserFactoy
									.getLeafObjectBrowser(getSpecType(), shell);
							List<LeafObject> itemsToAdd = null;
							if (leafObjectBrowser.open() == Dialog.OK) {
								itemsToAdd = leafObjectBrowser
										.getItemsToReturn();
								((SAPClassListControl) SAPCompositionControl.this.classListArray
										.get(index))
										.addLeafElementsWithUndo(itemsToAdd);

								relayoutParent();
							}
						}
					});
				}
			}
		} else { // if not editable
			if (operators.length > 0) {
				Label op = new Label(this, SWT.LEFT);
				op.setEnabled(false);
				if (PredicateHelpers.isNegationPredicate(spec)) {
					op.setText(operators[1]);
				} else {
					op.setText(operators[0]);
				}
				op.setBackground(getParent().getBackground());
				readOnlyOpArray.add(op);
			}
		}
	}

	/**
	 * remove controls corresponding to index
	 * 
	 * @param index
	 */
	protected void removeExpressionControls(int index) {
		Control c = (Control) removeButtonArray.remove(index);
		c.dispose();
		c = (Control) labelControlArray.remove(index);
		c.dispose();
		if (operators.length > 0) {
			c = (Control) opControlArray.remove(index);
			c.dispose();
		}
		c = (Control) classListArray.remove(index);
		c.dispose();
		if (canBrowse()) {
			c = (Control) browseButtonArray.remove(index);
			c.dispose();
		}

		if (index == 0 && labelControlArray.size() > 0) {
			Label label = (Label) labelControlArray.get(0);
			label.setText(labelString);
		}
	}

	public void relayout() {
		Control lastRemoveButton = null;
		tabListArray.clear();
		for (SAPClassListControl control : classListArray) {
			control.relayout();
		}
		int spacing = 5;
		int currentX = spacing;
		int currentY = spacing;

		Point labelControlSize = null;
		if (classListArray.size() > 0) {
			Control t = (Control) labelControlArray.get(0);
			Point labelSize = t.computeSize(SWT.DEFAULT, SWT.DEFAULT);

			labelControlSize = new Point(
					(labelSize.x >= 20) ? labelSize.x : 25, labelSize.y);
		}

		for (int i = 0; i < classListArray.size(); i++) {
			Control t;
			if (isEditable() && showRemove) {
				t = (Control) removeButtonArray.get(i);
				t.setBounds(currentX, currentY, SIZE_REMOVE_BUTTON.x,
						SIZE_REMOVE_BUTTON.y);
				tabListArray.add(t);
				lastRemoveButton = t;
			}
			currentX += SIZE_REMOVE_BUTTON.x + spacing;

			// leave space for add button
			currentX += SIZE_REMOVE_BUTTON.x + spacing;

			t = (Control) labelControlArray.get(i);
			t.setBounds(currentX, currentY, labelControlSize.x, SIZE_LABEL.y);
			currentX += labelControlSize.x + spacing;

			if (operators.length > 0) {
				if (isEditable()) {
					t = (Control) opControlArray.get(i);
					t.setBounds(currentX, currentY, SIZE_OP_COMBO.x,
							SIZE_OP_COMBO.y);
					tabListArray.add(t);
				} else {
					t = (Control) readOnlyOpArray.get(i);
					t.setBounds(currentX, currentY, SIZE_OP_COMBO.x,
							SIZE_OP_COMBO.y);
				}
			}
			currentX += SIZE_OP_COMBO.x;

			t = (Control) classListArray.get(i);
			Point compositeSize = t.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			int width = (compositeSize.x > SIZE_MEMBER_COMPOSITE.x) ? compositeSize.x
					: SIZE_MEMBER_COMPOSITE.x;
			t.setBounds(currentX, currentY, width, compositeSize.y);
			tabListArray.add(t);
			currentX += width + spacing;

			if (isEditable() && canBrowse()) {
				t = (Control) browseButtonArray.get(i);
				if (t instanceof Button) {
					Button button = (Button) t;
					Point size = button
							.computeSize(SWT.DEFAULT, CONTROL_HEIGHT);
					button.setBounds(currentX, currentY, size.x, size.y);
				}
				tabListArray.add(t);
			}
			currentX = spacing;
			if (i < (classListArray.size() - 1)) {
				// don't reset Y the last time around, so that we can put the
				// Add button on that line
				currentY += compositeSize.y + spacing;
			}
		}

		currentX += SIZE_REMOVE_BUTTON.x + spacing;
		if (isEditable() && showAdd) {
			addButton.setBounds(currentX, currentY, SIZE_REMOVE_BUTTON.x,
					SIZE_REMOVE_BUTTON.y);
			if (lastRemoveButton != null) {
				tabListArray.add(tabListArray.indexOf(lastRemoveButton) + 1,
						addButton);
			} else {
				tabListArray.add(addButton);
			}
		}

		setTabList((Control[]) tabListArray.toArray(new Control[tabListArray
				.size()]));
		layout();

	}

	public void setLabelString(String labelString) {
		this.labelString = labelString;
	}

	public int indexOfObject(SAPClassListControl control) {
		return (classListArray.indexOf(control));
	}

	/**
     * 
     */
	private void relayoutParent() {
		setRedraw(false);
		// relayout();
		editorPanel.relayout();
		setRedraw(true);
	}

	/**
	 * adds an undo element for adding a spec to the composition
	 * 
	 * @param spec
	 */
	private void addAddUndoElement(CompositePredicate spec) {
		CompositionUndoElement undoElement = new CompositionUndoElement();
		undoElement.setDomainObjectId(((IHasId) GlobalState.getInstance()
				.getCurrentObject()).getId());
		undoElement.setOp(CompositionUndoElementOp.ADD);
		undoElement.setIndex(PredicateHelpers
				.getRealPredicateCount(domainObject) - 1);
		undoElement.setNewValue(spec);
		undoElement.setControlId(controlId);
		GlobalState.getInstance().addUndoElement(undoElement);
	}

	/**
	 * adds an undo element for removing a spec to the composition
	 * 
	 * @param index
	 *            index of spec being removed
	 * @param spec
	 *            spec being removed
	 */
	private void addRemoveUndoElement(int index, CompositePredicate spec) {
		CompositionUndoElement undoElement = new CompositionUndoElement();
		undoElement.setDomainObjectId(((IHasId) GlobalState.getInstance()
				.getCurrentObject()).getId());
		undoElement.setOp(CompositionUndoElementOp.REMOVE);
		undoElement.setIndex(index);
		undoElement.setOldValue(spec);
		undoElement.setControlId(controlId);
		GlobalState.getInstance().addUndoElement(undoElement);
	}

	private void addReplaceUndoElement(int index, IPredicate oldValue,
			IPredicate newValue) {
		CompositionUndoElement undoElement = new CompositionUndoElement();
		undoElement.setDomainObjectId(((IHasId) GlobalState.getInstance()
				.getCurrentObject()).getId());
		undoElement.setOp(CompositionUndoElementOp.REPLACE);
		undoElement.setIndex(index);
		undoElement.setOldValue(oldValue);
		undoElement.setNewValue(newValue);
		undoElement.setControlId(controlId);
		GlobalState.getInstance().addUndoElement(undoElement);
	}

	public void cleanup() {
		IComponentManager componentManager = ComponentManagerFactory
				.getComponentManager();
		IEventManager eventManager = componentManager
				.getComponent(EventManagerImpl.COMPONENT_INFO);
		eventManager.registerListener(predicateModifiedListner,
				ContextualEventType.PREDICATE_MODIFIED_EVENT, domainObject);
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
	 * @param entityType
	 *            The entityType to set.
	 */
	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}

	/**
	 * @return Returns the specType.
	 */
	public SpecType getSpecType() {
		return specType;
	}

	/**
	 * @param componentType
	 *            The entityType to set.
	 */
	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}

	/**
	 * @return Returns the specType.
	 */
	public String getComponentType() {
		return componentType;
	}

	/**
	 * @param specType
	 *            The specType to set.
	 */
	public void setSpecType(SpecType specType) {
		this.specType = specType;
	}

	public boolean canBrowse() {
		return (acceptLeafObjects && (getSpecType() != SpecType.ACTION));
	}

	/**
	 * @author sgoldstein
	 */
	public class PredicateModifiedListener implements IContextualEventListener {

		/**
		 * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
		 */
		public void onEvent(IContextualEvent event) {
			SAPCompositionControl.this.refreshLater();
		}
	}
}
