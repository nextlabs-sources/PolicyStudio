/*
 * Created on Apr 6, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.framework.BaseTableLabelProvider;
import com.bluejungle.destiny.policymanager.framework.standardlisteners.TableColumnResizeListener;
import com.bluejungle.destiny.policymanager.ui.dialogs.browser.BrowserMessages;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;

/**
 * An SWT composite that contains a table or tables listing the leaf objects
 * available for selection. The source of the list or lists are provided through
 * {@see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.
 * ISelectableLeafObjectTableModel} instances. If more than one {@see
 * com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.
 * ISelectableLeafObjectTableModel} instance is provided, the tables are nested
 * within a {@see org.eclipse.swt.widgets.TabFolder}
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/branch/Destiny_Beta4_Stable/main/src/client/policymanager
 *          /src
 *          /java/main/com/bluejungle/destiny/policymanager/ui/dialogs/browser
 *          /impl/SelectableLeafObjectTabbedTableComposite.java#1 $
 */
@SuppressWarnings( { "unchecked", "deprecation" })
public class SelectableLeafObjectTabbedTableComposite extends Composite {

	private static final Object EMPTY_ROOT_INPUT = new Object();

	/**
	 * Number of rows in each table to be visible.
	 */
	private static final int NUM_ROWS_TO_SHOW = 10;
	/**
	 * Maximum number of leaf objects to display This number is really too
	 * large, but PM wanted it that way. We'll have to see what performance
	 * tests show
	 */
	private static final int MAX_NUM_LEAF_OBJECTS_TO_DISPLAY = 2000;

	private ISelectableLeafObjectsSource selectableLeafObjectSource;
	private final List selectableLeafObjectTableModels;
	private final List<SelectableLeafObjectTableComposite> selectableLeafObjectTables;
	private final Set<SelectableLeafObjectTableComposite> selectableLeafObjectTablesNeedingRefresh = new HashSet<SelectableLeafObjectTableComposite>();
	private final ListenerList leafObjectSelectionListenerList = new ListenerList();
	private SelectableLeafObjectTableComposite currentSelectableLeafObjectTable;
	private Label endUserMessage;

	/**
	 * Create an instance of SelectableLeafObjectTabbedTableComposite
	 * 
	 * @param parent
	 * @param selectableLeafObjectSource
	 * @param style
	 */
	public SelectableLeafObjectTabbedTableComposite(Composite parent,
			ISelectableLeafObjectsSource selectableLeafObjectSource,
			List selectableLeafObjectTableModels) {
		super(parent, SWT.NONE);
		if (selectableLeafObjectSource == null) {
			throw new NullPointerException(
					"selectableLeafObjectSource cannot be null.");
		}

		if (selectableLeafObjectTableModels == null) {
			throw new NullPointerException(
					"selectableLeafObjectTableModels cannot be null.");
		}

		this.selectableLeafObjectSource = selectableLeafObjectSource;
		this.selectableLeafObjectTableModels = selectableLeafObjectTableModels;
		selectableLeafObjectTables = new ArrayList<SelectableLeafObjectTableComposite>(
				selectableLeafObjectTableModels.size());
		init();

		selectableLeafObjectSource
				.addListModificationListener(new ISelectableLeafObjectListModificationListener() {

					/**
					 * @see com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl.ISelectableLeafObjectListModificationListener#onChange()
					 */
					public void onChange() {
						SelectableLeafObjectTabbedTableComposite.this.refresh();
					}
				});
	}

	/**
	 * Retrieve the list of leaf objecrs currently selected (highlighted)
	 * 
	 * @return the list of leaf objecrs currently selected (highlighted)
	 */
	public List getSelectedAvailableLeafObjects() {
		return currentSelectableLeafObjectTable
				.getSelectedAvailableLeafObjects();
	}

	/**
	 * Remove from the list of available leaf objects the ones that are
	 * currently selected (i.e. the ones which would be retrieved from a call to
	 * {@see #getSelectedAvailableLeafObjects()}
	 */
	public void removeSelectedAvailableLeafObjects() {
		currentSelectableLeafObjectTable.removeSelectedAvailableLeafObjects();
	}

	/**
	 * Remove the specified leaf object from the list of available leaf objects
	 * 
	 * @param leafObjectToRemove
	 *            the leaf object to remove
	 */
	public void removeLeafObject(LeafObject leafObjectToRemove) {
		if (leafObjectToRemove == null) {
			throw new NullPointerException("leafObjectToRemove cannot be null.");
		}

		currentSelectableLeafObjectTable.removeLeafObject(leafObjectToRemove);
	}

	/**
	 * Add to the list of available leaf objects the specific list of leaf
	 * objects which as previously been removed
	 * 
	 * @param itemsToReAdd
	 *            the leaf objects to re-add to the list of available leaf
	 *            objects
	 */
	public void reAddSelectedAvailableLeafObjects(List itemsToReAdd) {
		if (itemsToReAdd == null) {
			throw new NullPointerException("itemsToReAdd cannot be null.");
		}

		Iterator<SelectableLeafObjectTableComposite> selectableLeafObjectTablesIterator = selectableLeafObjectTables
				.iterator();
		while (selectableLeafObjectTablesIterator.hasNext()) {
			SelectableLeafObjectTableComposite nextSelectableLeafObjectTableModel = (SelectableLeafObjectTableComposite) selectableLeafObjectTablesIterator
					.next();

			// Will re-add the appropriate items to the table model
			nextSelectableLeafObjectTableModel
					.reAddSelectedAvailableLeafObjects(itemsToReAdd);
		}
	}

	/**
	 * Refresh the list of avialable leaf objects. Should be invoked after
	 * setting the search string
	 */
	public void refresh() {
		endUserMessage.setVisible(false);
		((GridData) endUserMessage.getLayoutData()).exclude = true;
		selectableLeafObjectTablesNeedingRefresh
				.addAll(selectableLeafObjectTables);
		currentSelectableLeafObjectTable.refresh();
		selectableLeafObjectTablesNeedingRefresh
				.remove(currentSelectableLeafObjectTable);
	}

	/**
	 * Add a listener to be notified when one or more leaf objects are chosen by
	 * the end user (e.g. the items are double clicked)
	 * 
	 * @param listener
	 *            the listner to be notified
	 */
	public void addLeafObjectSelectionListener(
			ILeafObjectSelectionListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener cannot be null.");
		}

		leafObjectSelectionListenerList.add(listener);
	}

	/**
	 * Initialize and layout the components in this composite
	 * 
	 */
	private void init() {
		GridLayout layout = new GridLayout();
		setLayout(layout);

		endUserMessage = new Label(this, SWT.WRAP);
		endUserMessage
				.setText(BrowserMessages.SELECTABLELEAFOBJECTTABBEDTABLECOMPOSITE_DATA_RETRIEVAL_FAILED_MESSAGE);
		endUserMessage.setVisible(false);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.exclude = true;
		endUserMessage.setLayoutData(data);

		if (selectableLeafObjectTableModels.size() > 1) {
			buildTabs(selectableLeafObjectTableModels);
		} else {
			SelectableLeafObjectTableComposite singleTable = buildSingleTable(
					(ISelectableLeafObjectTableModel) selectableLeafObjectTableModels
							.get(0), this);
			currentSelectableLeafObjectTable = singleTable;
			selectableLeafObjectTables.add(singleTable);
		}
	}

	/**
	 * Build the tab folder and nested leaf object tables
	 * 
	 * @param selectableLeafObjectTablesModels
	 *            the list of {@see ISelectableLeafObjectTableModel} instances
	 *            to be used as sources for the leaf object tables
	 */
	private TabFolder buildTabs(List selectableLeafObjectTableModels) {
		TabFolder leafObjectTabFolder = new TabFolder(this, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		leafObjectTabFolder.setLayoutData(data);
		leafObjectTabFolder
				.addSelectionListener(new TabItemSelectionListener());

		Iterator selectableLeafObjectTableModelsIterator = selectableLeafObjectTableModels
				.iterator();
		while (selectableLeafObjectTableModelsIterator.hasNext()) {
			ISelectableLeafObjectTableModel nextTableModel = (ISelectableLeafObjectTableModel) selectableLeafObjectTableModelsIterator
					.next();
			TabItem nextTabItem = new TabItem(leafObjectTabFolder, SWT.NONE);
			nextTabItem.setText(nextTableModel.getTitle());
			SelectableLeafObjectTableComposite nextTable = buildSingleTable(
					nextTableModel, leafObjectTabFolder);
			selectableLeafObjectTables.add(nextTable);
			nextTabItem.setControl(nextTable);
		}

		currentSelectableLeafObjectTable = (SelectableLeafObjectTableComposite) leafObjectTabFolder
				.getSelection()[0].getControl();

		currentSelectableLeafObjectTable.getTableViewer().setInput(
				EMPTY_ROOT_INPUT);

		return leafObjectTabFolder;
	}

	/**
	 * Build a single selectable leaf object table from the specified leaf
	 * object table model
	 * 
	 * @param nextTableModel
	 * @return the table which was built
	 */
	private SelectableLeafObjectTableComposite buildSingleTable(
			ISelectableLeafObjectTableModel nextTableModel,
			Composite tableParent) {
		if (nextTableModel == null) {
			throw new NullPointerException(
					"nextTableModel, tableParent cannot be null.");
		}

		if (tableParent == null) {
			throw new NullPointerException("tableParent cannot be null.");
		}

		SelectableLeafObjectTableComposite result = new SelectableLeafObjectTableComposite(
				tableParent, nextTableModel);
		GridData data = new GridData(GridData.FILL_BOTH);
		result.setLayoutData(data);

		return result;
	}

	/**
	 * @return
	 */
	public int getTableHeight() {
		return currentSelectableLeafObjectTable.computeSize(SWT.DEFAULT,
				SWT.DEFAULT).y;
	}

	/**
	 * Display an end user message indicating a failure encountered when
	 * retrieving data
	 */
	private void displayDataRetrievalError() {
		endUserMessage
				.setText(BrowserMessages.SELECTABLELEAFOBJECTTABBEDTABLECOMPOSITE_DATA_RETRIEVAL_FAILED_MESSAGE);
		endUserMessage.setVisible(true);
		((GridData) endUserMessage.getLayoutData()).exclude = false;
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		endUserMessage.setLayoutData(data);
	}

	/**
	 * Display a warning message encountered when the list of available leaf
	 * objects exceeds the maximum
	 */
	public void displayDataRetrievalLengthWarning() {
		endUserMessage
				.setText(BrowserMessages.SELECTABLELEAFOBJECTTABBEDTABLECOMPOSITE_EXCEEDED_MAX_RESULT_MESSAGE);
		endUserMessage.setVisible(true);
		((GridData) endUserMessage.getLayoutData()).exclude = false;
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		endUserMessage.setLayoutData(data);
	}

	/**
	 * Fire a leaf object selection event
	 * 
	 * @param leafObjectSelection
	 *            the event to fire
	 */
	private void fireLeafObjectSelectionEvent(LeafObject leafObjectSelection) {
		LeafObjectSelectionEvent eventToFire = new LeafObjectSelectionEvent(
				leafObjectSelection);
		Object[] listeners = leafObjectSelectionListenerList.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((ILeafObjectSelectionListener) listeners[i])
					.onLeafObjectSelection(eventToFire);
		}
	}

	/**
	 * A {@see TableViewer} content provider based on an {@see
	 * ISelectableLeafObjectTableModel} instance
	 * 
	 * @author sgoldstein
	 */
	private class SelectionLeafObjectTableModelContentProvider implements
			IStructuredContentProvider {

		private final ISelectableLeafObjectTableModel wrappedTableModel;

		/**
		 * 
		 * Create an instance of SelectionLeafObjectTableModelContentProvider
		 * 
		 * @param wrappedTableModel
		 */
		public SelectionLeafObjectTableModelContentProvider(
				ISelectableLeafObjectTableModel wrappedTableModel) {
			if (wrappedTableModel == null) {
				throw new NullPointerException(
						"wrappedTableModel cannot be null.");
			}

			this.wrappedTableModel = wrappedTableModel;
		}

		/**
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			// FIX ME - Caching!
			Object[] elementsToReturn = null;

			try {
				List elements = SelectableLeafObjectTabbedTableComposite.this.selectableLeafObjectSource
						.getSelectableLeafObjects(wrappedTableModel
								.getLeafObjectType(),
								MAX_NUM_LEAF_OBJECTS_TO_DISPLAY + 1);
				int numElementsReturned = elements.size();
				if (numElementsReturned > MAX_NUM_LEAF_OBJECTS_TO_DISPLAY) {
					SelectableLeafObjectTabbedTableComposite.this
							.displayDataRetrievalLengthWarning();
					numElementsReturned = MAX_NUM_LEAF_OBJECTS_TO_DISPLAY;
				}
				elementsToReturn = new Object[numElementsReturned];
				Iterator elementsIterator = elements.iterator();
				for (int i = 0; elementsIterator.hasNext()
						&& i < numElementsReturned; i++) {
					Object nextElement = elementsIterator.next();
					elementsToReturn[i] = nextElement;
				}
			} catch (SelectableLeafObjectSourceException exception) {
				LoggingUtil.logWarning(Activator.ID,
						"Failed to load leaf object for leaf object browser",
						exception);
				SelectableLeafObjectTabbedTableComposite.this
						.displayDataRetrievalError();
				elementsToReturn = new Object[0];
			}

			return elementsToReturn;
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			// DO NOTHING
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// DO NOTHING
		}
	}

	/**
	 * A {@see TableViewer} label provider based on an {@see
	 * ISelectableLeafObjectTableModel} instance
	 * 
	 * @author sgoldstein
	 */
	public class SelectionLeafObjectTableModelLabelProvider extends
			BaseTableLabelProvider {

		private final ISelectableLeafObjectTableModel wrappedTableModel;

		/**
		 * Create an instance of SelectionLeafObjectLabelProvider
		 * 
		 * @param nextTableModel
		 */
		public SelectionLeafObjectTableModelLabelProvider(
				ISelectableLeafObjectTableModel nextTableModel) {
			wrappedTableModel = nextTableModel;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			Image imageToReturn = null;
			if (columnIndex == 0) {
				imageToReturn = wrappedTableModel
						.getImage((LeafObject) element);
			}

			return imageToReturn;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			return wrappedTableModel.getText((LeafObject) element, columnIndex);
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
		 *      java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

	}

	/**
	 * A listener responsible for handling the event of an end user selecting
	 * the tab of a selectable leaf object list
	 * 
	 * @author sgoldstein
	 */
	private class TabItemSelectionListener extends SelectionAdapter {

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent event) {
			TabItem selectedItem = (TabItem) event.item;
			SelectableLeafObjectTableComposite nextCurrentSelectableLeafObjectTable = (SelectableLeafObjectTableComposite) selectedItem
					.getControl();
			if (SelectableLeafObjectTabbedTableComposite.this.selectableLeafObjectTablesNeedingRefresh
					.contains(nextCurrentSelectableLeafObjectTable)) {
				nextCurrentSelectableLeafObjectTable.refresh();
				SelectableLeafObjectTabbedTableComposite.this.selectableLeafObjectTablesNeedingRefresh
						.remove(nextCurrentSelectableLeafObjectTable);
			}

			SelectableLeafObjectTabbedTableComposite.this.currentSelectableLeafObjectTable = nextCurrentSelectableLeafObjectTable;
			if (nextCurrentSelectableLeafObjectTable != null) {
				TableViewer viewer = SelectableLeafObjectTabbedTableComposite.this.currentSelectableLeafObjectTable
						.getTableViewer();
				if (viewer.getTable().getItemCount() == 0)
					viewer.setInput(EMPTY_ROOT_INPUT);
			}
		}
	}

	/**
	 * A composite containing the selectable leaf object table itself. Enhances
	 * the SWT table by adding some auto column resizing behavior and an event
	 * listener for handling double click events on a table item
	 * 
	 * @author sgoldstein
	 */
	private class SelectableLeafObjectTableComposite extends Composite {

		private TableViewer wrappedTableViewer;
		private final ISelectableLeafObjectTableModel selectableLeafObjectTableModel;

		/**
		 * Create an instance of SelectableLeafObjectTableComponent
		 * 
		 * @param parent
		 * @param style
		 */
		public SelectableLeafObjectTableComposite(Composite parent,
				ISelectableLeafObjectTableModel selectableLeafObjectTableModel) {
			super(parent, SWT.NONE);
			if (selectableLeafObjectTableModel == null) {
				throw new NullPointerException(
						"selectableLeafObjectTableModel cannot be null.");
			}

			this.selectableLeafObjectTableModel = selectableLeafObjectTableModel;
			init();
		}

		/**
		 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
		 */
		@Override
		public Point computeSize(int wHint, int hHint, boolean changed) {
			// Standard SWT call
			checkWidget();

			// Pack all columns
			Table tableComponent = wrappedTableViewer.getTable();
			TableColumn[] tableColumns = tableComponent.getColumns();
			for (int i = 0; i < tableColumns.length; i++) {
				tableColumns[i].pack();
			}

			int height = NUM_ROWS_TO_SHOW * tableComponent.getItemHeight()
					+ tableComponent.getHeaderHeight();
			height = Math.max(height, hHint);

			return tableComponent.computeSize(wHint, height);
		}

		/**
		 * Intialize and layout components
		 * 
		 */
		private void init() {
			// Fill layout MUST be used here in order to get vertical
			// scrollbar!!!
			// setLayout(new FillLayout());
			GridLayout layout = new GridLayout();
			setLayout(layout);

			wrappedTableViewer = new TableViewer(this, SWT.MULTI
					| SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
			wrappedTableViewer
					.setContentProvider(new SelectionLeafObjectTableModelContentProvider(
							selectableLeafObjectTableModel));
			wrappedTableViewer
					.setLabelProvider(new SelectionLeafObjectTableModelLabelProvider(
							selectableLeafObjectTableModel));
			Table createdTable = wrappedTableViewer.getTable();
			createdTable.setLinesVisible(true);
			GridData data = new GridData(GridData.FILL_BOTH);
			createdTable.setLayoutData(data);

			List<String> headers = selectableLeafObjectTableModel
					.getColumnHeaders();
			Iterator<String> headersIterator = headers.iterator();
			while (headersIterator.hasNext()) {
				String nextHeaderTitle = (String) headersIterator.next();
				TableColumn nextColumn = new TableColumn(createdTable, SWT.LEFT);
				nextColumn.setText(nextHeaderTitle);
				nextColumn.setWidth(SWT.DEFAULT);
			}

			if (headers.size() > 1) {
				createdTable.setHeaderVisible(true);
			}

			// Add mouose listener to catch double clicks
			createdTable.addMouseListener(new MouseAdapter() {

				/**
				 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
				 */
				@Override
				public void mouseDoubleClick(MouseEvent event) {
					Table eventSource = (Table) event.getSource();
					// Make sure it's our table. Not needed, but adds a little
					// robustness
					if (eventSource
							.equals(SelectableLeafObjectTableComposite.this.wrappedTableViewer
									.getTable())) {
						Point doubleClickLocation = new Point(event.x, event.y);
						TableItem chosenItem = eventSource
								.getItem(doubleClickLocation);
						if (chosenItem != null) {
							LeafObject chosenLeafObject = (LeafObject) chosenItem
									.getData();
							SelectableLeafObjectTabbedTableComposite.this
									.fireLeafObjectSelectionEvent(chosenLeafObject);
						}
					}
				}
			});

			// wrappedTableViewer.setInput(EMPTY_ROOT_INPUT);

			if (selectableLeafObjectTableModel.getLeafObjectType() == LeafObjectType.APPLICATION)
				wrappedTableViewer.setInput(EMPTY_ROOT_INPUT);

			addListener(SWT.Resize, new TableColumnResizeListener(createdTable,
					this));
		}

		/**
		 * Remove from the list of available leaf objects the ones that are
		 * currently selected (i.e. the ones which would be retrieved from a
		 * call to {@see #getSelectedAvailableLeafObjects()}
		 */
		private void removeSelectedAvailableLeafObjects() {
			IStructuredSelection selectedObjects = (IStructuredSelection) wrappedTableViewer
					.getSelection();
			wrappedTableViewer.remove(selectedObjects.toArray());
		}

		/**
		 * Remove the specified leaf object from the list of available leaf
		 * objects
		 * 
		 * @param leafObjectToRemove
		 *            the leaf object to remove
		 */
		private void removeLeafObject(LeafObject leafObjectToRemove) {
			if (leafObjectToRemove == null) {
				throw new NullPointerException(
						"leafObjectToRemove cannot be null.");
			}

			wrappedTableViewer.remove(leafObjectToRemove);
		}

		/**
		 * Retrieve the list of leaf objecrs currently selected (highlighted)
		 * 
		 * @return the list of leaf objecrs currently selected (highlighted)
		 */
		private List getSelectedAvailableLeafObjects() {
			IStructuredSelection selectedObjects = (IStructuredSelection) wrappedTableViewer
					.getSelection();
			return selectedObjects.toList();
		}

		/**
		 * Will readd the items that are appropriate for the table
		 * 
		 * @param itemsToReAdd
		 */
		private void reAddSelectedAvailableLeafObjects(List itemsToReAdd) {
			if (itemsToReAdd == null) {
				throw new NullPointerException("itemsToReAdd cannot be null.");
			}

			Iterator itemsToReAddIterator = itemsToReAdd.iterator();
			while (itemsToReAddIterator.hasNext()) {
				LeafObject nextItem = (LeafObject) itemsToReAddIterator.next();
				if (selectableLeafObjectTableModel.supportsLeafObject(nextItem)) {
					wrappedTableViewer.add(nextItem);
				}
			}
		}

		public TableViewer getTableViewer() {
			return wrappedTableViewer;
		}

		/**
		 * Refresh the tables contents based on the backing content provider
		 * 
		 */
		private void refresh() {
			wrappedTableViewer.refresh();
		}
	}
}
