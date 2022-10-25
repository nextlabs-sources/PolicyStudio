package com.bluejungle.destiny.policymanager.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.bluejungle.destiny.policymanager.action.PolicyStudioActionFactory;
import com.bluejungle.destiny.policymanager.editor.DomainObjectEditor;
import com.bluejungle.destiny.policymanager.framework.standardlisteners.TableColumnResizeListener;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;

public abstract class PreviewPanel implements PreviewView.IPreviewPanel {
	protected IHasId hasId;
	protected Composite root;
	protected Label label;
	protected Button go, cancel;
	protected TableViewer tableViewer;
	protected List<PreviewItem> previewResults;
	protected DomainObjectEditor parentEditor;

	protected class ViewContentProvider implements IStructuredContentProvider {

		public ViewContentProvider() {
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return previewResults.toArray();
		}
	}

	protected class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public ViewLabelProvider() {
		}

		public String getColumnText(Object obj, int index) {
			return ((PreviewItem) obj).getText(index);
		}

		public Image getColumnImage(Object obj, int index) {
			return ((PreviewItem) obj).getImage(index);
		}
	}

	/**
	 * Items in the previewResults are of this type.
	 */
	public interface PreviewItem {

		String getText(int index);

		Image getImage(int index);
	}

	protected static PreviewItem previewText(final String text) {
		return new PreviewItem() {

			public String getText(int index) {
				return (index == 0) ? text : "";
			}

			public Image getImage(int index) {
				return null;
			}
		};
	}

	private static PreviewItem NOT_STARTED = previewText("Click [Go] to preview.");

	public PreviewPanel() {
		previewResults = new ArrayList<PreviewItem>();
	}

	public void setParentEditor(DomainObjectEditor anEditor) {
		this.parentEditor = anEditor;
	}

	public void createControls(Composite parent) {
		root = new Composite(parent, SWT.NONE);
		root.setLayout(new FormLayout());

		label = new Label(root, SWT.NONE);
		label.setText(ApplicationMessages.PREVIEWPANEL_PREVIEW);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0, 10);
		formData.left = new FormAttachment(0, 10);
		label.setLayoutData(formData);

		Button cancel = new Button(root, SWT.FLAT | SWT.PUSH);
		cancel.setText(ApplicationMessages.PREVIEWPANEL_CANCEL);
		formData = new FormData();
		formData.top = new FormAttachment(0, 5);
		formData.right = new FormAttachment(100, -5);
		formData.bottom = new FormAttachment(0, 25);
		cancel.setLayoutData(formData);
		cancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelPreview();
				if (previewResults.isEmpty()) {
					previewResults.add(NOT_STARTED);
				}
				tableViewer.refresh();
			}
		});

		Button go = new Button(root, SWT.FLAT | SWT.PUSH);
		go.setText(ApplicationMessages.PREVIEWPANEL_GO);
		formData = new FormData();
		formData.top = new FormAttachment(0, 5);
		formData.right = new FormAttachment(cancel, -5, SWT.LEFT);
		formData.bottom = new FormAttachment(0, 25);
		go.setLayoutData(formData);
		go.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IHasId currentObject = null;
				if (PolicyStudioActionFactory.SWITCH_TO_POLICY_AUTHOR_ACTION
						.isEnabled()) {
					currentObject = hasId;
				} else {
					currentObject = GlobalState.getInstance()
							.getCurrentObject();
				}
				DevelopmentStatus status = DomainObjectHelper
						.getStatus(currentObject);
				if ((status == DevelopmentStatus.DRAFT)
						|| (status == DevelopmentStatus.NEW)
						|| (status == DevelopmentStatus.EMPTY)) {
					if (PolicyServerProxy.saveEntity(currentObject) == null) {
						MessageDialog
								.openError(
										Display.getCurrent().getActiveShell(),
										ApplicationMessages.PREVIEWPANEL_ERROR_SAVING,
										ApplicationMessages.PREVIEWPANEL_ERROR_SAVING_MSG);
						return;
					}
				}

				preview();
				refreshPreviewTable(null);
			}
		});

		tableViewer = new TableViewer(root, SWT.BORDER | SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);

		formData = new FormData();
		formData.top = new FormAttachment(go, 5, SWT.BOTTOM);
		formData.left = new FormAttachment(0, 5);
		formData.right = new FormAttachment(100, -5);
		formData.bottom = new FormAttachment(100, -5);
		table.setLayoutData(formData);

		setupPreviewTable(table);

		formData = new FormData();
		formData.top = new FormAttachment(table, 20, SWT.TOP);
		formData.left = new FormAttachment(table, 10, SWT.LEFT);
		previewResults.add(NOT_STARTED);
		refreshPreviewTable(null);

		root
				.addListener(SWT.Resize, new TableColumnResizeListener(table,
						root));
	}

	public Composite getRootControl() {
		return root;
	}

	protected void setupPreviewTable(Table aTable) {
	}

	public void refreshPreviewTable(String element) {
		if (this.tableViewer.getTable().isDisposed()) {
			return;
		}

		if (element == null) {
			tableViewer.refresh();
		} else {
			tableViewer.refresh(element);
		}
	}

	public void setHasId(IHasId hasId) {
		this.hasId = hasId;
	}

	protected abstract PreviewItem makePreviewItem(Object data);

	protected void preview() {
		GlobalState.getInstance().saveEditorPanel();
		previewResults.clear();
		try {
			IHasId currentSelection = null;
			if (PolicyStudioActionFactory.SWITCH_TO_POLICY_AUTHOR_ACTION
					.isEnabled()) {
				currentSelection = hasId;
			} else {
				currentSelection = GlobalState.getInstance().getCurrentObject();
			}
			DODDigest digest = EntityInfoProvider
					.getComponentDescriptor(DomainObjectHelper
							.getName(currentSelection));
			DomainObjectDescriptor descriptor = PolicyServerProxy
					.getDescriptorById(digest.getId());
			Collection<LeafObject> results = PolicyServerProxy
					.getSubjectPreview(descriptor);
			if (results != null) {
				if (!results.isEmpty()) {
					for (LeafObject leafObject : results) {
						previewResults.add(makePreviewItem(leafObject));
					}
				} else {
					previewResults
							.add(previewText(ApplicationMessages.PREVIEWPANEL_NO_RESULT));
				}
			}
		} catch (PolicyEditorException e) {
			previewResults
					.add(previewText(ApplicationMessages.PREVIEWPANEL_CANNOT_PREVIEW));
		}
	}

	protected abstract EntityType getEntityType();

	protected void cancelPreview() {
		previewResults.clear();
	}
}