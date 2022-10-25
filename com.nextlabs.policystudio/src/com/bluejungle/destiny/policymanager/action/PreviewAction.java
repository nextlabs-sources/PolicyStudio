/*
 * Created on Jul 10, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/D_Plugins/com.nextlabs.policystudio/src/com
 *          /bluejungle/destiny/policymanager/action/PreviewAction.java#1 $
 */

public class PreviewAction extends Action implements GlobalState.IPartObserver {

	private IWorkbenchWindow workbenchWindow;
	private String partID;

	public PreviewAction(IWorkbenchWindow window, String title, String partID) {
		super(title, IAction.AS_CHECK_BOX);
		this.workbenchWindow = window;
		this.partID = partID;
		GlobalState.getInstance().addPartObserver(this);
		setChecked(false);
	}

	private void validateCheckedState() {
		/*
		 * We need to perform the validation once the menu is stable
		 */
		Display.getCurrent().asyncExec(new Runnable() {

			public void run() {
				boolean v = false;
				IWorkbenchPage page = workbenchWindow.getActivePage();
				if (page != null) {
					if (page.findView(partID) != null) {
						v = true;
					} else {
						v = false;
					}
				} else {
					LoggingUtil.logWarning(Activator.ID,
							"page is null on validateCheckedState", null);
				}
				setChecked(v);
			}
		});
	}

	public void workbenchInitialized() {
		validateCheckedState();
	}

	public void partOpened(IWorkbenchPart aPart) {
		if (aPart.getSite().getId().equals(partID)) {
			validateCheckedState();
		}
	}

	public void partClosed(IWorkbenchPart aPart) {
		if (aPart.getSite().getId().equals(partID)) {
			validateCheckedState();
		}
	}

	@Override
	public void run() {
		IWorkbenchPage page = workbenchWindow.getActivePage();
		IViewPart part;

		if ((part = page.findView(partID)) != null) {
			page.hideView(part);
		} else {
			try {
				page.showView(partID);
			} catch (PartInitException pie) {
				System.err.println("" + pie);
			}
		}
	}
}
