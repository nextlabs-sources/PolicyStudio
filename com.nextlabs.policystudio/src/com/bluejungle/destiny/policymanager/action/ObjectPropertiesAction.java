/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.ObjectPropertiesDialog;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/action/CheckDependenciesAction.
 *          java#2 $
 */

public class ObjectPropertiesAction extends BaseDisableableAction {

	/**
	 * Constructor
	 * 
	 */
	public ObjectPropertiesAction() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 */
	public ObjectPropertiesAction(String text) {
		super(text);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param image
	 */
	public ObjectPropertiesAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(ImageBundle.PROPERTIES_IMG);
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return ImageDescriptor
				.createFromImage(ImageBundle.PROPERTIES_DISABLED_IMG);
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param style
	 */
	public ObjectPropertiesAction(String text, int style) {
		super(text, style);
	}

	@Override
	public void run() {
		Set<DomainObjectDescriptor> selectedItems = getSelectedItems();
		DomainObjectDescriptor dod = (DomainObjectDescriptor) selectedItems
				.iterator().next();
		if (dod != null) {
			/*
			 * Bug Fix #2458. Ideally, there would be no explicit calls to save
			 * the editor panel. However, the following is taking place:
			 * 
			 * 1. New component is created 2. A control is activated in the
			 * editor of the new component 3. File/Properties is selected
			 * 
			 * For some reason, in step 3, the part deactivated event for the
			 * Editor Panel is not fired and the normal auto save process does
			 * not take place. If step 2 is not taken, then the event does fire.
			 * This behavior is strange and I was not able to determine why it's
			 * happening. Therefore, we're doing the save here explicitly
			 */
			GlobalState gs = GlobalState.getInstance();
			IHasId currentObject = gs.getCurrentObject();
			if ((currentObject != null)
					&& (currentObject.getId().equals(dod.getId()))) {
				// Save the changes in the editor panel
				gs.saveEditorPanel();
			}

			/*
			 * Now, pull the latest from the server. Having a seperate copy
			 * currently allows cancel in the properties dialog
			 */
			IHasId domainObject = (IHasId) PolicyServerProxy
					.getEntityForDescriptor(dod);
			if (domainObject != null) {
				ObjectPropertiesDialog dlg = new ObjectPropertiesDialog(Display
						.getCurrent().getActiveShell(), domainObject);
				dlg.open();
			}
		}
	}
}