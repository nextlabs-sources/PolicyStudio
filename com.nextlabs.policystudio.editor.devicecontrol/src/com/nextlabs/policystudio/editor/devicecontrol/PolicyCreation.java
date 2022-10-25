package com.nextlabs.policystudio.editor.devicecontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.model.CreationExtension;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.PolicyActionsDescriptor;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecManager;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.obligation.DObligationManager;
import com.bluejungle.pf.domain.destiny.obligation.IDObligationManager;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;

public class PolicyCreation extends CreationExtension {
	public PolicyCreation() {
	}

	@Override
	public void postCreation(IDPolicy policy) {
		IDObligationManager oblMgr = (IDObligationManager) ComponentManagerFactory
				.getComponentManager().getComponent(
						DObligationManager.COMP_INFO);

		ITarget target = policy.getTarget();

		CompositePredicate userPred = new CompositePredicate(BooleanOp.AND,
				PredicateHelpers.buildConstantArrayList(true));
		CompositePredicate hostPred = new CompositePredicate(BooleanOp.AND,
				PredicateHelpers.buildConstantArrayList(true));
		CompositePredicate appPred = new CompositePredicate(BooleanOp.AND,
				PredicateHelpers.buildConstantArrayList(true));

		List<IPredicate> subjectParts = new ArrayList<IPredicate>();
		subjectParts.add(userPred);
		subjectParts.add(hostPred);
		subjectParts.add(appPred);
		CompositePredicate subjectPred = new CompositePredicate(BooleanOp.AND,
				subjectParts);

		target.setSubjectPred(subjectPred);

		CompositePredicate fromPred = new CompositePredicate(BooleanOp.AND,
				PredicateHelpers.buildConstantArrayList(true));
		target.setFromResourcePred(fromPred);

		CompositePredicate toPred = new CompositePredicate(BooleanOp.AND,
				PredicateHelpers.buildConstantArrayList(true));

		List<IPredicate> dateParts = new ArrayList<IPredicate>();
		dateParts.add(PredicateConstants.TRUE);
		dateParts.add(PredicateConstants.TRUE);
		dateParts.add(PredicateConstants.TRUE);

		// always log deny enforcements
		IObligation newObligation = oblMgr.createLogObligation();
		policy.addObligation(newObligation, EffectType.DENY);

		CompositePredicate actPred = new CompositePredicate(BooleanOp.AND,
				PredicateHelpers.buildConstantArrayList(true));
		CompositePredicate member = new CompositePredicate(BooleanOp.OR,
				new ArrayList<IPredicate>());
		PredicateHelpers.rebalanceDomainObject(member, BooleanOp.OR);

		// check if Attach Device action exists
		Collection<DomainObjectDescriptor> entities = PolicyServerProxy
				.getEntityList("ACTION/Attach Device", EntityType.COMPONENT);
		if (entities == null || entities.size() == 0) {
			// not found, create that action
			try {
				List<PolicyActionsDescriptor> listBasicActions = (List<PolicyActionsDescriptor>) PolicyServerProxy
						.getAllPolicyActions();
				IDAction action = null;
				for (PolicyActionsDescriptor descriptor : listBasicActions) {
					if (descriptor.getAction().getName()
							.equals("ATTACH_DEVICE")) {
						action = (IDAction) descriptor.getAction();
						break;
					}
				}

				if (action != null) {
					IDSpec spec = PolicyServerProxy.createBlankComponent(
							"Attach Device", "ACTION");
					EntityInfoProvider.updateComponentList("ACTION");
					CompositePredicate actionDomainObj = (CompositePredicate) spec
							.getPredicate();

					Set<IDAction> actions = new HashSet<IDAction>();
					actions.add(action);
					Set<IDAction> actionSet = PredicateHelpers
							.getActionSet(actionDomainObj);
					actionSet.addAll(actions);
					PredicateHelpers
							.updateActionSet(actionDomainObj, actionSet);
					PolicyServerProxy.saveEntity(spec);
				}
			} catch (PolicyEditorException e) {
				LoggingUtil.logError(Activator.ID, e.getMessage(), e);
			}
		}

		IDSpecManager sm = (IDSpecManager) ComponentManagerFactory
				.getComponentManager().getComponent(IDSpecManager.COMP_INFO);
		IPredicate ref = sm.getSpecReference("ACTION/Attach Device");
		PredicateHelpers.addPredicate(member, ref);
		PredicateHelpers.rebalanceDomainObject(member, BooleanOp.OR);

		PredicateHelpers.addPredicate(actPred, member);

		policy.setAttribute("DEVICE_CONTROL_POLICY", true);
		target.setToResourcePred(toPred);
		target.setActionPred(actPred);
	}

	@Override
	public boolean preCreation() {
		boolean found = false;
		List<PolicyActionsDescriptor> listBasicActions = null;
		try {
			listBasicActions = (List<PolicyActionsDescriptor>) PolicyServerProxy
					.getAllPolicyActions();
		} catch (PolicyEditorException e1) {
			LoggingUtil.logError(Activator.ID, e1.getMessage(), e1);
		}
		for (PolicyActionsDescriptor descriptor : listBasicActions) {
			if (descriptor.getAction().getName().equals("ATTACH_DEVICE")) {
				found = true;
				break;
			}
		}
		if (!found) {
			Shell shell = Display.getCurrent().getActiveShell();
			MessageDialog.openError(shell, EditorMessages.POLICYCREATION_ERROR,
					EditorMessages.POLICYCREATION_ERROR_CONFIG);
			return false;
		}

		return true;
	}
}
