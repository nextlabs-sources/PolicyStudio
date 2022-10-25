package com.nextlabs.policystudio.preview.application;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.bluejungle.destiny.policymanager.editor.EditorMessages;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.PreviewPanel;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lifecycle.EntityType;

public class ApplicationPreviewPanel extends PreviewPanel {

	public ApplicationPreviewPanel() {
	}

	@Override	protected void setupPreviewTable(Table table) {
		TableColumn c1 = new TableColumn(table, SWT.LEFT);
		c1.setWidth(100);
		c1.setText(EditorMessages.APPLICATIONCOMPONENTEDITOR_APPLICATION_NAME);

		TableColumn c2 = new TableColumn(table, SWT.LEFT);
		c2.setWidth(100);
		c2.setText(EditorMessages.APPLICATIONCOMPONENTEDITOR_EXECUTABLE_FILE);

		tableViewer.setContentProvider(new ViewContentProvider());
		tableViewer.setLabelProvider(new ViewLabelProvider());
		tableViewer.setInput(this.previewResults);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected EntityType getEntityType() {
		return EntityType.APPLICATION;
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.PreviewPanel#makePreviewItem(java.lang.Object)
	 */
	@Override
	protected PreviewItem makePreviewItem(Object data) {
		final LeafObject leaf = (LeafObject) data;
		return new PreviewItem() {

			public String getText(int index) {
				if (index == 0) {
					return leaf.getName();
				} else if (index == 1) {
					return leaf.getUid();
				}
				return null;
			}

			public Image getImage(int index) {
				return index == 0 ? ObjectLabelImageProvider.getImage(leaf)
						: null;
			}
		};
	}
}
