package com.bluejungle.destiny.policymanager.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;

public class GlobalDuplicateHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IAction action = PolicyStudioActionFactory.getDuplicateAction();
		action.run();

		return null;
	}
}
