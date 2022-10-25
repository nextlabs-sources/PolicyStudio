/*
 * Created on Sep 1, 2005 All sources, binaries and HTML pages (C) copyright
 * 2005 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bluejungle.destiny.policymanager.ui.dialogs.AboutDialog;

/**
 * @author aweber
 */
public class ShowAboutAction extends Action {

	public ShowAboutAction(String text) {
		super(text);
	}

	@Override
	public void run() {
		Shell shell = Display.getCurrent().getActiveShell();
		AboutDialog dialog = new AboutDialog(shell);
		dialog.open();
	}
}
