package com.nextlabs.policystudio.editor.communication;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.policymanager.model.CreationExtension;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;

public class PolicyCreation extends CreationExtension {

	public PolicyCreation() {
	}

	@Override
	public void postCreation(IDPolicy policy) {

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

		policy.setAttribute("COMMUNICATION_POLICY", true);
		target.setToSubjectPred(toPred);
		target.setActionPred(actPred);
	}
}
