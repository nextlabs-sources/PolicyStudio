/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * This class implements a generic object selection dialog. The constructor
 * takes a collection of DomainObjectDescriptors and shows them in a list. The
 * dialog allows the user to select/deselect from the list and allows the caller
 * to get the list of selected objects when the dialog is closed.
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/
 *          SubmitCheckDependenciesDialog.java#1 $
 */

public class ObjectSelectionDialog extends Dialog {

	private List<DomainObjectDescriptor> descriptors;
	private List<DomainObjectDescriptor> selectedObjects;

	private String title;
	private TableViewer tableViewer;

	private class TableSorter extends ViewerSorter {

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

			if (cat1 != cat2) {
				return cat1 - cat2;
			}

			DomainObjectDescriptor descriptor1 = (DomainObjectDescriptor) element1;
			DomainObjectDescriptor descriptor2 = (DomainObjectDescriptor) element2;
			return DomainObjectHelper.getDisplayName(descriptor1)
					.compareToIgnoreCase(
							DomainObjectHelper.getDisplayName(descriptor2));
		}
	}

	private class TableContentProvider implements IStructuredContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (descriptors != null) {
				return descriptors.toArray();
			}

			return new Object[0];
		}
	}

	private class TableLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			return DomainObjectHelper
					.getDisplayName((DomainObjectDescriptor) obj);
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
	 * @param descriptors
	 *            list of object descriptors to show
	 * @param title
	 *            title of dialog
	 */
	public ObjectSelectionDialog(Shell parent,
			Collection<DomainObjectDescriptor> descriptors, String title) {
		super(parent);
		this.title = title;
		this.descriptors = new ArrayList<DomainObjectDescriptor>(descriptors);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText(title);
		newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite root = (Composite) super.createDialogArea(parent);

		Composite bottom = new Composite(root, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		bottom.setLayoutData(data);
		GridLayout layout = new GridLayout();
		bottom.setLayout(layout);

		initialize(bottom);
		return parent;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				DialogMessages.LABEL_CLOSE, false);
	}

	@Override
	protected void okPressed() {
		setupSelectedObjects();

		super.okPressed();
	}

	/**
	 * intialize and layout all controls.
	 */
	private void initialize(Composite root) {
		tableViewer = new TableViewer(root, SWT.BORDER | SWT.SINGLE
				| SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
		tableViewer.setContentProvider(new TableContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.setSorter(new TableSorter());

		Table table = tableViewer.getTable();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 300;
		table.setLayoutData(data);
		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setWidth(300);
		table.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				TableItem[] items = tableViewer.getTable().getItems();
				for (TableItem item : items) {
					if (item.getChecked()) {
						getButton(IDialogConstants.OK_ID).setEnabled(true);
					}
				}
			}
		});

		tableViewer.setInput(descriptors);

		TableItem[] items = table.getItems();
		for (TableItem item : items) {
			item.setChecked(true);
		}
		if (items.length == 0) {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}

	/**
	 * adds all selected descriptors to selectedObjects
	 */
	public void setupSelectedObjects() {
		selectedObjects = new ArrayList<DomainObjectDescriptor>();
		for (TableItem item : tableViewer.getTable().getItems()) {
			if (item.getChecked()) {
				selectedObjects.add((DomainObjectDescriptor) item.getData());
			}
		}
	}

	/**
	 * Returns the selectedObjects.
	 * 
	 * @return the selectedObjects.
	 */
	public List<DomainObjectDescriptor> getSelectedObjects() {
		return selectedObjects;
	}
}