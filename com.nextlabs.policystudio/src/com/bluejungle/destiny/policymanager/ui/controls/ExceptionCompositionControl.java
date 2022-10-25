/*
 * Created on Mar 1st 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author ichiang
 */

package com.bluejungle.destiny.policymanager.ui.controls;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

import com.bluejungle.destiny.policymanager.editor.IEditorPanel;
import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.ExceptionClassListControl;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.LeafObjectBrowserFactory;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;

public class ExceptionCompositionControl extends Composite {
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
	private List<ExceptionClassListControl> exceptionClassListArray = new ArrayList<ExceptionClassListControl>();
	private List<Button> removeButtonArray = new ArrayList<Button>();
	private List<Label> labelControlArray = new ArrayList<Label>();
	private List<Button> browseButtonArray = new ArrayList<Button>();
	private List<Label> readOnlyOpArray = new ArrayList<Label>();
	private List<Control> tabListArray = new ArrayList<Control>();
	private String[] operators = new String[] { "in", "not in" };
	private Button addButton = null;
	private String labelString = null;
	private boolean editable = true;
	private boolean showAdd = true;
	private boolean showRemove = true;
	private boolean maxOneLine = false;
	private boolean acceptLeafObjects = true;
	private EntityType entityType;
	private SpecType specType;
	private String type = null;
	private String lookupLabel;
	private IContextualEventListener predicateModifiedListner = new PredicateModifiedListener();
	private List<IPolicyReference> exceptionRefList = null;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param style
	 */

	public ExceptionCompositionControl(Composite parent, int style,
			String name, String labelString,
			List<IPolicyReference> exceptionRefList, IEditorPanel editorPanel,
			int controlId, boolean editable, boolean acceptLeafObjects,
			boolean maxOneLine, String[] operators, SpecType specType,
			String type, String lookupLabel) {
		super(parent, style);
		this.type = type;
		this.lookupLabel = lookupLabel;
		doSetup(name, labelString, exceptionRefList, editorPanel, controlId,
				editable, acceptLeafObjects, maxOneLine, operators, specType);
	}

	private void doSetup(String name, String labelString,
			List<IPolicyReference> exceptionRefList, IEditorPanel editorPanel,
			int controlId, boolean editable, boolean acceptLeafObjects,
			boolean maxOneLine, String[] operators, SpecType specType) {
		this.defaultName = name;
		this.labelString = labelString;
		this.exceptionRefList = exceptionRefList;
		this.controlId = controlId;
		this.maxOneLine = maxOneLine;
		this.operators = operators;
		this.acceptLeafObjects = acceptLeafObjects;
		setSpecType(specType);
		setEntityType(EntityType.POLICY);

		IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
		IEventManager eventManager = componentManager.getComponent(EventManagerImpl.COMPONENT_INFO);
		eventManager.registerListener(predicateModifiedListner,	ContextualEventType.PREDICATE_MODIFIED_EVENT, exceptionRefList);

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
		disposeWidgetArray(exceptionClassListArray);
		disposeWidgetArray(browseButtonArray);
		disposeWidgetArray(readOnlyOpArray);

		labelControlArray.clear();
		removeButtonArray.clear();
		opControlArray.clear();
		exceptionClassListArray.clear();
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

	public void setDomainObject(List<IPolicyReference> exceptionList) {
		this.exceptionRefList = exceptionList;
	}

	public void initialize() {
		if (maxOneLine) {
			showAdd = false;
			showRemove = false;
		}
		addExpressionControls();
	}

	/**
	 * @param spec
	 */
	private void addExpressionControls() {
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

		ExceptionClassListControl c = new ExceptionClassListControl(this,
				getEntityType(), getSpecType(), getType(), defaultName,
				isEditable() ? SWT.BORDER : SWT.NONE);
		// c.setAcceptLeafObjects(acceptLeafObjects);
		c.setEditable(isEditable());
		c.setParentPanel(editorPanel);
		c.setExceptionDomainObject(SpecType.ILLEGAL, exceptionRefList);
		c.setControlId(controlId);
		exceptionClassListArray.add(c);
	}

	public void relayout() {
		Control lastRemoveButton = null;
		tabListArray.clear();
		for (ExceptionClassListControl control : exceptionClassListArray) {
			control.relayout();
		}
		int spacing = 5;
		int currentX = spacing;
		int currentY = spacing;

		Point labelControlSize = null;
		if (exceptionClassListArray.size() > 0) {
			Control t = (Control) labelControlArray.get(0);
			Point labelSize = t.computeSize(SWT.DEFAULT, SWT.DEFAULT);

			labelControlSize = new Point(
					(labelSize.x >= 20) ? labelSize.x : 25, labelSize.y);
		}

		for (int i = 0; i < exceptionClassListArray.size(); i++) {
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

			t = (Control) exceptionClassListArray.get(i);
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
			if (i < (exceptionClassListArray.size() - 1)) {
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

		setTabList((Control[]) tabListArray.toArray(new Control[tabListArray.size()]));
		layout();

	}

	public void setLabelString(String labelString) {
		this.labelString = labelString;
	}

	public int indexOfObject(ExceptionClassListControl control) {
		return (exceptionClassListArray.indexOf(control));
	}

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

	public void cleanup() {
		IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
		IEventManager eventManager = componentManager.getComponent(EventManagerImpl.COMPONENT_INFO);
		eventManager.registerListener(predicateModifiedListner,	ContextualEventType.PREDICATE_MODIFIED_EVENT, exceptionRefList);
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
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return Returns the specType.
	 */
	public String getType() {
		return type;
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
			ExceptionCompositionControl.this.refreshLater();
		}
	}
}
