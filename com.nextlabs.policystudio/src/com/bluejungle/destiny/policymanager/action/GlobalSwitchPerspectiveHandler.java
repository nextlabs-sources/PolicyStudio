package com.bluejungle.destiny.policymanager.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class GlobalSwitchPerspectiveHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (PolicyStudioActionFactory.SWITCH_TO_POLICY_AUTHOR_ACTION
				.isEnabled()) {
			PolicyStudioActionFactory.SWITCH_TO_POLICY_AUTHOR_ACTION.run();
		} else {
			PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION.run();
		}
		return null;
	}
}
