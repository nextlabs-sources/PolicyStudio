package com.bluejungle.destiny.policymanager.model;

import org.eclipse.swt.graphics.Image;

public class EditorElement {
	private String type;
	private String context;
	private String displayName;
	private Image icon;
	private String panelClass;
	private String editorClass;

	public EditorElement(String type, String context, String displayName,
			Image icon, String panelClass, String editorClass) {
		super();
		this.type = type;
		this.context = context;
		this.displayName = displayName;
		this.icon = icon;
		this.panelClass = panelClass;
		this.editorClass = editorClass;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Image getIcon() {
		return icon;
	}

	public void setIcon(Image icon) {
		this.icon = icon;
	}

	public String getPanelClass() {
		return panelClass;
	}

	public void setPanelClass(String panelClass) {
		this.panelClass = panelClass;
	}

	public String getEditorClass() {
		return editorClass;
	}

	public void setEditorClass(String editorClass) {
		this.editorClass = editorClass;
	}

}
