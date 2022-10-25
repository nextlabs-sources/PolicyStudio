package com.bluejungle.destiny.policymanager.model;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.ScrolledComposite;

import com.bluejungle.destiny.policymanager.editor.EditorPanel;
import com.bluejungle.destiny.policymanager.editor.IEditorPanel;
import com.bluejungle.destiny.policymanager.ui.GlobalState.SaveCause;

public class ClientEditorPanel implements IClientEditorPanel {
	private IEditorPanel panel;

	public IEditorPanel getEditorPanel() {
		return panel;
	}

	public void setEditorPanel(IEditorPanel panel) {
		this.panel = panel;
	}

	public Composite getComposite() {
		return (Composite) panel;
	}
	
	public ScrolledComposite getScrolledComposite(){
		return panel.getScrolledComposite();
	}
	
	public Composite getLeftComposite() {
		return (Composite) panel.getLeftEditorComposite();
	}

	public Composite addSectionComposite() {
		return EditorPanel.addSectionComposite(panel.getMainComposite(), panel
				.getBackground());
	}
	
	public Composite addLeftEditorSectionComposite(){
		return EditorPanel.addLeftEditorSectionComposite(panel.getLeftEditorComposite(), panel.getBackground());
	}
	
	public Composite addRightEditorSectionComposite(){
		return EditorPanel.addRightEditorSectionComposite(panel.getRightEditorComposite(), panel.getBackground());
	}

	public Composite initializeSectionHeading(Composite parent, String title) {
		return panel.initializeSectionHeading(parent, title);
	}

	public boolean isEditable() {
		return panel.isEditable();
	}

	public Color getBackground() {
		return panel.getBackground();
	}

	public boolean hasCustomProperties() {
		return panel.hasCustomProperties();
	}

	public void saveContents(SaveCause cause) {
		panel.saveContents(cause);
	}

	public boolean canRelayout() {
		return panel.canRelayout();
	}

	public void relayout() {
		panel.relayout();
	}

	public boolean isDisposed() {
		return panel.isDisposed();
	}
}
