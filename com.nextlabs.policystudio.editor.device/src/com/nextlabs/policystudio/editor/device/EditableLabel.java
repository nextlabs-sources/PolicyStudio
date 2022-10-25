package com.nextlabs.policystudio.editor.device;

/*
 * Created on Feb 25, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.SWTEventObject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.ui.EditableLabelEvent;
import com.bluejungle.destiny.policymanager.ui.EditableLabelListener;
import com.bluejungle.destiny.policymanager.ui.PolicyObjectTransfer;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.expressions.IPredicate;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates
 *          .xml#2 $:
 */

@SuppressWarnings("restriction")
public class EditableLabel extends Composite {

	protected CLabel label = null;
	protected Text text = null;
	protected boolean isSelected = false;
	protected boolean isEditing = false;
	protected Font labelFont = null;
	protected Color labelColor = null;
	protected int bgColor = SWT.COLOR_WHITE;

	protected List<EditableLabelListener> editableLabelListenerArray = new ArrayList<EditableLabelListener>(
			0);

	private boolean mouseDownWhileSelected = false;
	protected ClassListControl parentControl = null;

	protected Menu contextMenu;
	private boolean dirty = false;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param style
	 */
	public EditableLabel(Composite parent, int style) {
		super(parent, style);
		// setBackground(getDisplay().getSystemColor(bgColor));
		setBackground(getParent().getBackground());

		label = new CLabel(this, style);
		label.setBackground(getParent().getBackground());
		text = new Text(this, SWT.SINGLE | style);
		text.setLocation(2, 2);

		addLabelListeners();
		addTextListeners();

		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				if (isEditing()) {
					Color oldColor = e.gc.getForeground();
					int oldWidth = e.gc.getLineWidth();
					e.gc.setLineWidth(2);
					e.gc.setForeground(getDisplay().getSystemColor(
							SWT.COLOR_BLUE));
					e.gc.drawRectangle(1, 1, text.getSize().x + 2, text
							.getSize().y + 2);

					// restore gc to original condition
					e.gc.setLineWidth(oldWidth);
					e.gc.setForeground(oldColor);
				}
			}
		});

		addControlListener(new ControlAdapter() {
			@Override
			public void controlMoved(ControlEvent e) {
				relayout();
			}

			@Override
			public void controlResized(ControlEvent e) {
				relayout();
			}
		});
	}

	/**
     * 
     */
	private void addTextListeners() {
		text.addListener(SWT.Deactivate, new Listener() {

			public void handleEvent(Event event) {
				saveText(null);
			}
		});
		text.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				saveText(e);
			}
		});
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == '&') {
					e.doit = false;
					return;
				}
				if (e.keyCode == SWT.ESC) {
					cancelEditing();
				}
			}
		});
	}

	/**
	 * Cancel editing and reset text to original value.
	 */
	protected void cancelEditing() {
		text.setText(label.getText());
		setEditing(false);
		fireCancelEditing();
	}

	/**
     * 
     */
	private void addLabelListeners() {
		label.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_UP) {
					fireUpArrow(e);
				} else if (e.keyCode == SWT.ARROW_DOWN) {
					fireDownArrow(e);
				} else if (e.keyCode == SWT.DEL) {
					fireDelete(e);
				}
			}
		});
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 3) {
					return;
				}
				if (isSelected) {
					mouseDownWhileSelected = true;
				}
				fireMouseDown(e);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (e.x >= 0 && e.y >= 0 && e.x < label.getSize().x
						&& e.y < label.getSize().y) {
					if (e.button == 3) {
						if (contextMenu != null) {
							Point loc = ((Control) (e.getSource())).toDisplay(
									e.x, e.y);
							contextMenu.setLocation(loc.x, loc.y);
							contextMenu.setVisible(true);

							Display display = getShell().getDisplay();
							while (!contextMenu.isDisposed()
									&& contextMenu.isVisible()) {
								if (!display.readAndDispatch()) {
									display.sleep();
								}
							}
							contextMenu.setVisible(false);
						}
					} else {
						boolean isControlPressed = ((e.stateMask & SWT.CONTROL) != 0);
						boolean isShiftPressed = ((e.stateMask & SWT.SHIFT) != 0);
						if (isSelected && mouseDownWhileSelected
								&& !isControlPressed && !isShiftPressed) {
							setEditing(true);
						} else {
							fireMouseUp(e);
						}
					}
				}
				mouseDownWhileSelected = false;
			}
		});
	}

	public void setContextMenu(Menu aMenu) {
		contextMenu = aMenu;
	}

	public void relayout() {
		if (isEditing) {
			label.setVisible(false);
			text.setVisible(true);
			label.setSize(getParent().getSize().x, label.computeSize(
					SWT.DEFAULT, SWT.DEFAULT).y);
			text.setSize(getParent().getSize().x - 8, label.computeSize(
					SWT.DEFAULT, SWT.DEFAULT).y - 6);
		} else {
			label.setVisible(true);
			text.setVisible(false);
			text.setSize(0, 0);
			label.setSize(label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, label
					.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 2);
			if (isSelected) {
				label.setBackground(ResourceManager
						.getColor(SWT.COLOR_LIST_SELECTION));
				label.setForeground(ResourceManager
						.getColor(SWT.COLOR_LIST_SELECTION_TEXT));
			} else {
				label.setBackground(getParent().getBackground());
				label.setForeground(labelColor == null ? ResourceManager
						.getColor(SWT.COLOR_BLACK) : labelColor);
			}
		}
		layout();
		getParent().layout();
	}

	public void setText(String str) {
		label.setText(str);
		text.setText(str);
		relayout();
	}

	public String getText() {
		return label.getText();
	}

	public boolean isEditing() {
		return isEditing;
	}

	public void setEditing(boolean isEditing) {
		if (text.isDisposed()) {
			return;
		}
		if (isEditing && !fireStartEditing()) {
			return;
		}

		this.isEditing = isEditing;
		if (isEditing) {
			isSelected = false;
			// label.setEnabled(false);
			text.setEnabled(true);
			// text.forceFocus();
			// text.setSelection(0, text.getText().length());
			dirty = true;
		} else {
			// label.setEnabled(true);
			text.setEnabled(false);
			dirty = false;
		}
		relayout();
		if (isEditing) {
			text.setFocus();
			text.setSelection(0, text.getText().length());
		}

	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		relayout();
		if (isSelected) {
			label.forceFocus();
		}
	}

	public void setLabelFont(Font font) {
		label.setFont(font);
		relayout();
	}

	public void addEditableLabelListener(EditableLabelListener listener) {
		editableLabelListenerArray.add(listener);
	}

	public void removeEditableLabelListener(EditableLabelListener listener) {
		editableLabelListenerArray.remove(listener);
	}

	/**
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
	}

	/**
	 * @param event
	 * 
	 */
	protected void fireDownArrow(KeyEvent event) {
		EditableLabelEvent e = new EditableLabelEvent(this);
		e.originalEvent = event;
		e.stateMask = event.stateMask;
		for (EditableLabelListener listener : editableLabelListenerArray) {
			listener.downArrow(e);
		}
	}

	/**
     * 
     */
	private void fireUpArrow(KeyEvent event) {
		EditableLabelEvent e = new EditableLabelEvent(this);
		e.originalEvent = event;
		e.stateMask = event.stateMask;
		for (EditableLabelListener listener : editableLabelListenerArray) {
			listener.upArrow(e);
		}
	}

	/**
	 * @param e
	 */
	protected void fireMouseUp(MouseEvent event) {
		EditableLabelEvent e = new EditableLabelEvent(this);
		e.originalEvent = event;
		e.x = event.x;
		e.y = event.y;
		e.stateMask = event.stateMask;

		for (EditableLabelListener listener : editableLabelListenerArray) {
			listener.mouseUp(e);
		}
	}

	/**
	 * @param e
	 */
	protected void fireMouseDown(MouseEvent event) {
		EditableLabelEvent e = new EditableLabelEvent(this);
		e.originalEvent = event;
		e.x = event.x;
		e.y = event.y;
		e.stateMask = event.stateMask;

		for (EditableLabelListener listener : editableLabelListenerArray) {
			listener.mouseDown(e);
		}
	}

	/**
	 * @param e
	 */
	private void saveText(SWTEventObject e) {
		if (!isEditing) {
			return;
		}

		String newValue = text.getText();
		if (dirty) {
			label.setText(newValue);
			dirty = false;
			if (fireTextSaved(e)) {
				setEditing(false);
				if (isSelected) {
					label.forceFocus();
				}
			} else {
				dirty = true;
			}
		}
	}

	/**
	 * @param event
	 * @return
	 */
	private boolean fireTextSaved(SWTEventObject event) {
		boolean allowSave = true;
		EditableLabelEvent e = new EditableLabelEvent(this);
		e.originalEvent = event;
		for (EditableLabelListener listener : editableLabelListenerArray) {
			allowSave = allowSave && listener.textSaved(e);
		}
		return allowSave;
	}

	/**
	 * @return false if any of the listeners cancels the edit.
	 */
	private boolean fireStartEditing() {
		boolean allowEditing = true;
		EditableLabelEvent e = new EditableLabelEvent(this);
		for (EditableLabelListener listener : editableLabelListenerArray) {
			allowEditing = allowEditing && listener.startEditing(e);
		}
		return allowEditing;
	}

	/**
	 * @param e
	 */
	protected void fireDelete(SWTEventObject event) {
		EditableLabelEvent e = new EditableLabelEvent(this);
		e.originalEvent = event;
		for (EditableLabelListener listener : editableLabelListenerArray) {
			listener.delete(e);
		}
	}

	/**
     * 
     */
	private void fireCancelEditing() {
		EditableLabelEvent e = new EditableLabelEvent(this);
		for (EditableLabelListener listener : editableLabelListenerArray) {
			listener.fireCancelEditing(e);
		}
	}

	public void addDragSource() {

		// Allow data to be copied or moved from the drag source
		int operations = DND.DROP_MOVE | DND.DROP_COPY;
		DragSource source = new DragSource(label, operations);

		// Provide data in Text format
		Transfer[] types = new Transfer[] { PolicyObjectTransfer.getInstance() };
		source.setTransfer(types);

		source.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				PolicyObjectTransfer transfer = PolicyObjectTransfer
						.getInstance();
				// Provide the data of the requested type.
				if (transfer.isSupportedType(event.dataType)) {
					IPredicate[] specArray = new IPredicate[parentControl.selectedControls
							.size()];
					for (int i = 0; i < parentControl.selectedControls.size(); i++) {
						specArray[i] = (IPredicate) parentControl.labelSpecMap
								.get(parentControl.selectedControls.get(i));
					}
					event.data = specArray;
				}
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				// If a move operation has been performed, delete
				if (event.detail == DND.DROP_MOVE) {
					fireDelete(event);
				}
			}
		});

	}

	public ClassListControl getParentControl() {
		return parentControl;
	}

	public void setParentControl(ClassListControl parentControl) {
		this.parentControl = parentControl;
	}

	@Override
	public void setForeground(Color color) {
		labelColor = color;
		label.setForeground(color);
	}

	public void setImage(Image image) {
		label.setImage(image);
	}
}
