package com.bluejungle.destiny.policymanager.model;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.ScrolledComposite;

import com.bluejungle.destiny.policymanager.editor.IEditorPanel;
import com.bluejungle.destiny.policymanager.ui.GlobalState.SaveCause;

public interface IClientEditorPanel {
	IEditorPanel getEditorPanel();

	Composite getComposite();
	
	ScrolledComposite getScrolledComposite();
	
	Composite getLeftComposite();

	void setEditorPanel(IEditorPanel panel);

	Composite addSectionComposite();
	
	Composite addLeftEditorSectionComposite();
	
	Composite addRightEditorSectionComposite();

	Composite initializeSectionHeading(Composite parent, String title);

	boolean isEditable();

	Color getBackground();

	boolean hasCustomProperties();

	void saveContents(SaveCause cause);

	boolean canRelayout();
	
	void relayout();
	
	boolean isDisposed();
}
