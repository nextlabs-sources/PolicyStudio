package com.nextlabs.policystudio.preview.host;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.bluejungle.destiny.policymanager.editor.EditorMessages;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.PreviewPanel;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lifecycle.EntityType;

public class DesktopPreviewPanel extends PreviewPanel {

	public DesktopPreviewPanel() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.bluejungle.destiny.policymanager.editor.ComponentEditor#
	 * setupPreviewTable(org.eclipse.swt.widgets.Table)
	 */
	@Override
	protected void setupPreviewTable(Table table) {
		TableColumn c1 = new TableColumn(table, SWT.LEFT);
		c1.setWidth(150);
		c1.setText(EditorMessages.DESKTOPCOMPONENTEDITOR_HOST_NAME);

		TableColumn c2 = new TableColumn(table, SWT.LEFT);
		c2.setWidth(150);
		c2.setText(EditorMessages.DESKTOPCOMPONENTEDITOR_DNS_HOST_NAME);

		tableViewer.setContentProvider(new ViewContentProvider());
		tableViewer.setLabelProvider(new ViewLabelProvider());
		tableViewer.setInput(previewResults);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected EntityType getEntityType() {
		return EntityType.HOST;
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
					return leaf.getUniqueName();
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