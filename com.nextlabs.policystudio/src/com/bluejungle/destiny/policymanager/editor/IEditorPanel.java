/*
 * Created on Mar 14, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.ScrolledComposite;

import com.bluejungle.destiny.policymanager.ui.GlobalState.SaveCause;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.CompositePredicate;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/editor/IEditorPanel.java#5 $:
 */

public interface IEditorPanel {
	boolean canRelayout();

	boolean isDisposed();

	Color getBackground();

	void relayout();

	CompositePredicate getControlDomainObject(int controlId, IHasId domainObject);

	void saveContents(SaveCause cause);

	void dispose();

	IHasId getDomainObject();

	boolean isEditable();

	Composite initializeSectionHeading(Composite parent, String title);

	boolean hasCustomProperties();

	Composite getMainComposite();
	
	ScrolledComposite getScrolledComposite();
	
	Composite getLeftEditorComposite();

	Composite getRightEditorComposite();
}
