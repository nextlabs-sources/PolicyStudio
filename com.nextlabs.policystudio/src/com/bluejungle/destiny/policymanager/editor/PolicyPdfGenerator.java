package com.bluejungle.destiny.policymanager.editor;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.action.CheckDependenciesAction;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers.EffectTypeEnum;
import com.bluejungle.destiny.policymanager.ui.dialogs.DialogMessages;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.obligation.CustomObligation;
import com.bluejungle.pf.domain.destiny.obligation.DisplayObligation;
import com.bluejungle.pf.domain.destiny.obligation.NotifyObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectAttribute;
import com.bluejungle.pf.domain.epicenter.action.IAction;
import com.bluejungle.pf.domain.epicenter.common.ISpec;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyExceptions;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;
import com.bluejungle.pf.domain.epicenter.misc.IEffectType;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

public class PolicyPdfGenerator {

	protected IDPolicy policy;
	protected DomainObjectDescriptor descriptor;
	protected Document document;

	public PolicyPdfGenerator(IDPolicy policy,
			DomainObjectDescriptor descriptor, Document document) {
		this.policy = policy;
		this.document = document;
		this.descriptor = descriptor;
	}

	@SuppressWarnings("unchecked")
	protected void displayObligationForType(Document document,
			IDEffectType effectType) throws DocumentException {
		List<IObligation> logList = findObligations("log", effectType);
		List<IObligation> displayList = findObligations("display", effectType);
		List<IObligation> notifyList = findObligations("notify", effectType);
		List<IObligation> customList = findObligations("custom", effectType);
		if (!logList.isEmpty() || !displayList.isEmpty()
				|| !notifyList.isEmpty() || !customList.isEmpty()) {
			Paragraph paragraph = new Paragraph();
			Font font = new Font(Font.TIMES_ROMAN, 12, Font.ITALIC);
			if (effectType.getType() == EffectType.ALLOW_TYPE) {
				Phrase phrase = new Phrase("On Allow, Monitor: ", font);
				paragraph.add(phrase);
			} else if (effectType.getType() == EffectType.DENY_TYPE) {
				Phrase phrase = new Phrase("On Deny: ", font);
				paragraph.add(phrase);
			}

			StringBuffer result = new StringBuffer();
			if (!logList.isEmpty()) {
				result.append("Log");
			}
			if (!displayList.isEmpty()) {
				if (result.length() == 0) {
					result.append("Display user alert");
				} else {
					result.append(", display user alert");
				}
			}
			if (!notifyList.isEmpty()) {
				if (result.length() == 0) {
					result.append("Send email");
				} else {
					result.append(", send email");
				}
			}
			if (!customList.isEmpty()) {
				if (result.length() == 0) {
					result.append("Custom obligation");
				} else {
					result.append(", custom obligation");
				}
			}

			font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
			Phrase phrase = new Phrase(result.toString(), font);
			paragraph.add(phrase);
			document.add(paragraph);

			if (!displayList.isEmpty()) {
				paragraph = new Paragraph();
				font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
				phrase = new Phrase("Send Message: ", font);
				paragraph.add(phrase);

				DisplayObligation displayObligation = (DisplayObligation) displayList
						.get(0);
				font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
				phrase = new Phrase(displayObligation.getMessage(), font);
				paragraph.add(phrase);

				document.add(paragraph);
			}

			if (!notifyList.isEmpty()) {
				paragraph = new Paragraph();
				font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
				phrase = new Phrase("Email To: ", font);
				paragraph.add(phrase);

				NotifyObligation notifyObligation = (NotifyObligation) notifyList
						.get(0);

				font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
				phrase = new Phrase(notifyObligation.getEmailAddresses(), font);
				paragraph.add(phrase);
				document.add(paragraph);

				paragraph = new Paragraph();
				font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
				phrase = new Phrase("Email Message: ", font);
				paragraph.add(phrase);

				font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
				phrase = new Phrase(notifyObligation.getBody(), font);
				paragraph.add(phrase);
				document.add(paragraph);
			}

			if (!customList.isEmpty()) {
				paragraph = new Paragraph();
				font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
				phrase = new Phrase("Custom Obligation: \n", font);
				paragraph.add(phrase);
				document.add(paragraph);

				for (Object object : customList) {
					result = new StringBuffer();
					CustomObligation obligation = (CustomObligation) object;
					result.append(obligation.getCustomObligationName());

					result = new StringBuffer();
					List<String> args = (List<String>) obligation
							.getCustomObligationArgs();
					if (args.size() > 0) {
						result.append("[ ");
					}
					for (int i = 0, n = args.size(); i < n; i++) {
						String arg = args.get(i);
						if (i > 0 && i % 2 == 1) {
							result.append("=");
						}
						result.append(arg);
						if (i > 0 && i % 2 == 1) {
							result.append(" ");
						}
					}
					if (args.size() > 0) {
						result.append("]");
					}
					font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
					phrase = new Phrase(result.toString(), font);
					paragraph = new Paragraph();
					paragraph.add(phrase);
					document.add(paragraph);
				}
			}
		}
	}

	protected List<CompositePredicate> filterPredicate(
			CompositePredicate predicate) {
		List<CompositePredicate> filteredPredicate = new ArrayList<CompositePredicate>();
		for (int i = 0; i < predicate.predicateCount(); i++) {
			IPredicate spec = (IPredicate) predicate.predicateAt(i);
			if (spec instanceof CompositePredicate
					&& !isRedundentPredicate((CompositePredicate) spec)) {
				filteredPredicate.add((CompositePredicate) spec);
			}
		}
		return filteredPredicate;
	}

	protected String findLabel(IPredicate spec) {
		String labelName = "";
		if (spec instanceof IDSpecRef) {
			IDSpecRef specRef = (IDSpecRef) spec;
			labelName = specRef.getReferencedName();
			labelName = labelName.substring(labelName
					.indexOf(PQLParser.SEPARATOR) + 1);
		} else if (spec instanceof Relation) {
			IExpression lhs = ((Relation) spec).getLHS();
			IExpression rhs = ((Relation) spec).getRHS();
			Object rhsValue = rhs.evaluate(null).getValue();
			if (lhs instanceof IDSubjectAttribute) {
				if (CommonPolicyConstants.SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP
						.containsKey(lhs)
						&& rhsValue instanceof Long) {
					LeafObjectType associatedLeafObjectType = CommonPolicyConstants.SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP
							.get(lhs);
					LeafObject leafObject = EntityInfoProvider
							.getLeafObjectByID((Long) rhsValue,
									associatedLeafObjectType);
					if (leafObject != null) {
						labelName = leafObject.getName();
					} else {
						labelName = "Entry Not Found - " + rhsValue;
					}
				} else {
					// Done for consistency with earlier implement to reduce
					// risk
					labelName = "" + rhsValue;
				}
			} else if (lhs instanceof ResourceAttribute
					&& rhs instanceof Constant) { /* PATH */
				String val = ((Constant) rhs).getRepresentation();
				String res = val.replaceAll("[\\\\]+", "\\\\");
				labelName = val.startsWith("\\\\") ? "\\" + res : res;
			} else {
				// Catch all
				if (rhs instanceof Constant) {
					labelName = ((Constant) rhs).getRepresentation();
				} else {
					labelName = "" + rhsValue;
				}
			}
		}

		return labelName;
	}

	protected List<IObligation> findObligations(String obligationType,
			IDEffectType effectType) {
		List<IObligation> obligationsToReturn = new LinkedList<IObligation>();

		Collection<IObligation> obligations = policy.getObligations(effectType);
		for (IObligation nextObligation : obligations) {
			String nextObligationType = nextObligation.getType();
			if (nextObligationType.equals(obligationType)) {
				obligationsToReturn.add(nextObligation);
			}
		}
		return obligationsToReturn;
	}

	protected String getActionLabel(String name,
			List<CompositePredicate> predicates) {
		StringBuffer result = new StringBuffer(name);

		for (CompositePredicate predicate : predicates) {
			for (int i = 0; i < predicate.predicateCount(); i++) {
				Object predicateElement = predicate.predicateAt(i);
				if (!(predicateElement instanceof PredicateConstants)) {
					result.append(findLabel((IPredicate) predicateElement));
					result.append(", ");
				}
			}
		}

		if (result.length() > 2) {
			result.deleteCharAt(result.length() - 1);
			result.deleteCharAt(result.length() - 1);
		}

		return result.toString();
	}

	protected String getExceptionLabel(String name,	List<IPolicyReference> exceptions) {
		StringBuffer result = new StringBuffer(name);

		for (IPolicyReference exception : exceptions) {
			String exceptionTagName = PolicyHelpers.exceptionTagName(exception);
			result.append(exceptionTagName);
			result.append(", ");
		}
		if (result.length() > 2) {
			result.deleteCharAt(result.length() - 1);
			result.deleteCharAt(result.length() - 1);
		}
		return result.toString();
	}
	
	protected String getTagLabel(String name,	Collection<IPair<String, String>> tags) {
		StringBuffer result = new StringBuffer(name);

		for (IPair tag : tags) {
			String tagName = tag.first()+":"+tag.second();
			result.append(tagName);
			result.append(", ");
		}
		if (result.length() > 2) {
			result.deleteCharAt(result.length() - 1);
			result.deleteCharAt(result.length() - 1);
		}
		return result.toString();
	}

	protected CompositePredicate getActions() {
		ITarget target = policy.getTarget();
		return (CompositePredicate) target.getActionPred();
	}
	
	protected List<IPolicyReference> getExcptions() {
        IPolicyExceptions exception =  policy.getPolicyExceptions();
        return exception.getPolicies();
	}

	protected String getActivatedBy(DomainObjectDescriptor descriptor) {
		LeafObject ownerLeaf = EntityInfoProvider.getLeafObjectByID(descriptor
				.getOwner(), LeafObjectType.APPUSER);
		if (ownerLeaf == null) {
			return DialogMessages.OBJECTPROPERTIESDIALOG_UNKNOWN_OR_DELETED;
		}
		return ownerLeaf.getName();
	}

	protected CompositePredicate getApplications() {
		ITarget target = policy.getTarget();
		CompositePredicate subject = (CompositePredicate) target
				.getSubjectPred();
		return (CompositePredicate) subject.predicateAt(2);
	}

	protected CompositePredicate getComputers() {
		ITarget target = policy.getTarget();
		CompositePredicate subject = (CompositePredicate) target
				.getSubjectPred();
		return (CompositePredicate) subject.predicateAt(1);
	}

	protected IPredicate getConnectionTypePredicate() {
		return PredicateHelpers.getConnectionType(policy.getConditions());
	}

	protected String getDateTime(IPredicate predicate) {
		Long dateVal = (Long) ((IRelation) predicate).getRHS().evaluate(null)
				.getValue();
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(dateVal.longValue());

		Format formatter = new SimpleDateFormat("MM/dd/yy hh:mm a");
		return formatter.format(calendar.getTime());
	}

	protected IPredicate getEndTimePredicate() {
		return PredicateHelpers.getEndTime(policy.getConditions());
	}

	protected String getEnforcement() {
		IEffectType effect = policy.getMainEffect();
		IEffectType otherwise = policy.getOtherwiseEffect();
        if(PolicyHelpers.isDACPolicyType(policy)){
        	if (effect == EffectType.DENY && otherwise == EffectType.ALLOW){
        		effect = EffectType.ALLOW;
        		otherwise = EffectType.DENY;
        	}
    		policy.setMainEffect(effect);
    		policy.setOtherwiseEffect(otherwise);
        }
        EffectTypeEnum effectTypeEnum = PolicyHelpers.getIndexForEffect(effect, otherwise);
        return PolicyHelpers.getStringFromEffectType(effectTypeEnum);
	}

	protected CompositePredicate getFromSources() {
		ITarget target = policy.getTarget();
		return (CompositePredicate) target.getFromResourcePred();
	}

	protected String getHeartbeatInfo() {
		Relation rel = (Relation) PredicateHelpers.getHeartbeat(policy.getConditions());
		Long val = null;
		if (rel != null) {
			val = (Long) rel.getRHS().evaluate(null).getValue();
		}
		if (val == null) {
			return null;
		} else {
			return "Time since last heartbeat is "
					+ String.valueOf(val.longValue() / 60) + " min(s)";
		}
	}

	protected String getOperator(String[] operators,
			CompositePredicate predicate) {
		if (PredicateHelpers.isNegationPredicate(predicate)) {
			return operators[1];
		} else {
			return operators[0];
		}
	}

	protected String getPolicyName() {
		String name = policy.getName();
		int index = name.lastIndexOf(PQLParser.SEPARATOR);
		if (index < 0) {
			return name;
		}
		return "Policy - " + name.substring(index + 1);
	}

	protected CompositePredicate getRecipients() {
		ITarget target = policy.getTarget();
		return (CompositePredicate) target.getToSubjectPred();
	}

	protected IPredicate getScheduleFromPredicate() {
		IPredicate condition = policy.getConditions();
		return PredicateHelpers.getDailyFromTime(condition);
	}

	protected IPredicate getScheduleToPredicate() {
		IPredicate condition = policy.getConditions();
		return PredicateHelpers.getDailyToTime(condition);
	}

	protected IPredicate getStartTimePredicate() {
		return PredicateHelpers.getStartTime(policy.getConditions());
	}

	protected String getTime(IPredicate predicate) {
		Constant fromConst = (Constant) ((Relation) predicate).getRHS();

		Date fromDate = null;
		try {
			fromDate = DateFormat.getTimeInstance().parse(
					unquote(fromConst.getRepresentation()));
			Format formatter = new SimpleDateFormat("hh:mm a");
			return formatter.format(fromDate);
		} catch (ParseException e) {
			LoggingUtil.logError(Activator.ID, "error get time", e);
			return "";
		}
	}

	protected CompositePredicate getToSources() {
		ITarget target = policy.getTarget();
		return (CompositePredicate) target.getToResourcePred();
	}

	protected CompositePredicate getUsers() {
		ITarget target = policy.getTarget();
		CompositePredicate subject = (CompositePredicate) target
				.getSubjectPred();
		return (CompositePredicate) subject.predicateAt(0);
	}

	protected void getUserSubjectLabel(String[] operators,
			List<CompositePredicate> predicates) throws DocumentException {
		boolean firsttime = true;
		Font italicFont = new Font(Font.TIMES_ROMAN, 12, Font.ITALIC);
		Font boldFont = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
		Phrase phrase;
		Paragraph paragraph;
		for (CompositePredicate predicate : predicates) {
			String operator = getOperator(operators, predicate);
			if (PredicateHelpers.isNegationPredicate(predicate)) {
				predicate = (CompositePredicate) predicate.predicateAt(0);
			}

			paragraph = new Paragraph();
			if (firsttime) {
				phrase = new Phrase(operator, italicFont);
				paragraph.add(phrase);

				firsttime = false;
			} else {
				phrase = new Phrase("And" + operator, italicFont);
				paragraph.add(phrase);
			}

			StringBuffer result = new StringBuffer();
			boolean inner_firsttime = true;
			for (int i = 0; i < predicate.predicateCount(); i++) {
				Object predicateElement = predicate.predicateAt(i);
				if (!(predicateElement instanceof PredicateConstants)) {
					if (inner_firsttime) {
						inner_firsttime = false;
					} else {
						result.append(" OR ");
					}
					result.append(findLabel((IPredicate) predicateElement));
				}
			}

			if (result.indexOf(" OR ") != -1) {
				phrase = new Phrase("(" + result.toString() + ")", boldFont);
				paragraph.add(phrase);
			} else {
				phrase = new Phrase(result.toString(), boldFont);
				paragraph.add(phrase);
			}
			document.add(paragraph);
		}
	}

	protected String getWeekdayForExpression(IExpression exp) {
		Long i = (Long) exp.evaluate(null).getValue();
		return CommonPolicyConstants.DAY_NAMES[i.intValue() - 1];
	}

	protected boolean isRedundentPredicate(CompositePredicate predicate) {
		boolean isSimple = true;
		for (int i = 0, n = predicate.predicateCount(); i < n; i++) {
			IPredicate pred = predicate.predicateAt(i);
			if (pred instanceof CompositePredicate) {
				isSimple = isSimple
						&& isRedundentPredicate((CompositePredicate) pred);
			} else if (pred instanceof IAction
					|| pred instanceof IPredicateReference
					|| pred instanceof IRelation || pred instanceof ISpec) {
				isSimple = false;
				break;
			}
		}

		return isSimple;
	}

	protected String unquote(String s) {
		if (s == null) {
			return null;
		}
		if (s.length() < 2 || s.charAt(0) != '"'
				|| s.charAt(s.length() - 1) != '"') {
			return s;
		} else {
			return s.substring(1, s.length() - 1);
		}
	}
}
