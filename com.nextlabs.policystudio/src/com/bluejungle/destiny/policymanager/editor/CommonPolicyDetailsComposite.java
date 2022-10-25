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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.action.CheckDependenciesAction;
import com.bluejungle.destiny.policymanager.ui.ConditionPredicateHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers.EffectTypeEnum;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
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

public abstract class CommonPolicyDetailsComposite extends Composite {

	protected IDPolicy policy;

	public CommonPolicyDetailsComposite(Composite parent, int style, IDPolicy policy) {
		super(parent, style);
		this.policy = policy;

		initialize();
	}

	protected void displayDateTime() {
		GridLayout layout;
		GridData data;
		Composite composite;
		Label label;
		Label labelDateTime = new Label(this, SWT.LEFT | SWT.WRAP);
		labelDateTime.setText("Under Conditions:");
		labelDateTime.setFont(FontBundle.ARIAL_9_NORMAL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		labelDateTime.setLayoutData(data);

		IPredicate type = getConnectionTypePredicate();
		if (type != null) {
			composite = new Composite(this, SWT.NONE);
			data = new GridData(GridData.FILL_HORIZONTAL);
			composite.setLayoutData(data);
			layout = new GridLayout(2, false);
			layout.marginHeight = 0;
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = 5;
			composite.setLayout(layout);

			label = new Label(composite, SWT.NONE);
			label.setText("Connection Type:");
			label.setFont(FontBundle.ARIAL_9_ITALIC);
			data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
			label.setLayoutData(data);

			label = new Label(composite, SWT.LEFT | SWT.WRAP);
			label.setFont(FontBundle.ARIAL_9_BOLD);
			Long val = (Long) ((IRelation) type).getRHS().evaluate(null)
					.getValue();
			int index = val.intValue();
			if (index == 0) {
				label.setText("Local");
			} else if (index == 1) {
				Relation rel1 = (Relation) PredicateHelpers
						.getConnectionSite(policy.getConditions());
				String val1 = "";
				if (rel1 != null) {
					val1 = (String) ((IRelation) rel1).getRHS().evaluate(null)
							.getValue();
				}
				label.setText("Remote [" + val1 + "]");
			}
		}

		String heartbeat = getHeartbeatInfo();
		if (heartbeat != null) {
			composite = new Composite(this, SWT.NONE);
			data = new GridData(GridData.FILL_HORIZONTAL);
			composite.setLayoutData(data);
			layout = new GridLayout(2, false);
			layout.marginHeight = 0;
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = 5;
			composite.setLayout(layout);

			label = new Label(composite, SWT.NONE);
			label.setText("Heartbeat:");
			label.setFont(FontBundle.ARIAL_9_ITALIC);
			data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
			label.setLayoutData(data);

			label = new Label(composite, SWT.LEFT | SWT.WRAP);
			label.setFont(FontBundle.ARIAL_9_BOLD);
			label.setText(heartbeat);
			data = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
			label.setLayoutData(data);
		}

		composite = new Composite(this, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(data);
		layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 5;
		composite.setLayout(layout);

		label = new Label(composite, SWT.NONE);
		label.setText("Date/Time:");
		label.setFont(FontBundle.ARIAL_9_ITALIC);
		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		label.setLayoutData(data);

		IPredicate start = getStartTimePredicate();
		if (start != null) {
			Label labelStart = new Label(composite, SWT.LEFT | SWT.WRAP);
			String text = "Starting " + getDateTime(start);
			labelStart.setFont(FontBundle.ARIAL_9_BOLD);
			labelStart.setText(text);
			data = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
			labelStart.setLayoutData(data);
		} else {
			Label labelAll = new Label(composite, SWT.NONE);
			labelAll.setText("All");
			labelAll.setFont(FontBundle.ARIAL_9_BOLD);
		}

		IPredicate end = getEndTimePredicate();
		if (end != null) {
			Label labelEnd = new Label(composite, SWT.NONE);
			labelEnd.setFont(FontBundle.ARIAL_9_BOLD);
			String text = "Ending " + getDateTime(end);
			labelEnd.setText(text);
			data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;
			labelEnd.setLayoutData(data);
		}

		composite = new Composite(this, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(data);
		layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 5;
		composite.setLayout(layout);

		label = new Label(composite, SWT.NONE);
		label.setText("Recurrence:");
		label.setFont(FontBundle.ARIAL_9_ITALIC);
		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		label.setLayoutData(data);

		IPredicate scheduleFrom = getScheduleFromPredicate();
		IPredicate scheduleTo = getScheduleToPredicate();
		label = new Label(composite, SWT.WRAP);
		label.setFont(FontBundle.ARIAL_9_BOLD);
		if (scheduleFrom != null && scheduleTo != null) {
			StringBuffer buffer = new StringBuffer("Time ");
			buffer.append(getTime(scheduleFrom));
			buffer.append(" to ");
			buffer.append(getTime(scheduleTo));
			label.setText(buffer.toString());
			data = new GridData(GridData.FILL_HORIZONTAL);
			label.setLayoutData(data);
		} else {
			label.setText("24/7");
		}

		StringBuffer result = new StringBuffer("Days - ");
		IPredicate dowim = PredicateHelpers.getDOWIMPredicate(policy
				.getConditions());
		IPredicate weekday = PredicateHelpers.getWeekDayPredicate(policy
				.getConditions());
		if (dowim != null) {
			IExpression exp = ((Relation) dowim).getRHS();
			Long storedCount = (Long) exp.evaluate(null).getValue();
			int count = storedCount.intValue();
			if (count == -1) {
				// assuming the last index is "last"
				count = CommonPolicyConstants.DAY_COUNT_LABELS.length;
			}
			count--; // subtract 1 to get the array index;

			result.append("The ");
			result.append(CommonPolicyConstants.DAY_COUNT_LABELS[count]);
			result.append(" ");

			IExpression weekdayExp = ((Relation) weekday).getRHS();
			String savedValue = weekdayExp.toString();
			// remove the quotes that this always seems to have:
			savedValue = savedValue.substring(1, savedValue.length() - 1);

			for (int i = 0; i < CommonPolicyConstants.DAY_NAMES.length; i++) {
				if (CommonPolicyConstants.DAY_NAMES[i].toLowerCase().equals(savedValue.toLowerCase())) {
					result.append(CommonPolicyConstants.DAY_NAMES[i]);
					break;
				}
			}

			result.append(" of every month");
		} else if (weekday != null) {
			if (weekday instanceof Relation) {
				result.append(getWeekdayForExpression(((Relation) weekday)
						.getRHS()));
			} else if (weekday instanceof CompositePredicate) {
				Iterator<IPredicate> iter = ((CompositePredicate) weekday)
						.predicates().iterator();
				boolean firsttime = true;
				while (iter.hasNext()) {
					IExpression exp = ((Relation) iter.next()).getRHS();
					if (!firsttime) {
						result.append(", ");
					}
					result.append(getWeekdayForExpression(exp));
					firsttime = false;
				}
			}
		} else {
			IPredicate day = PredicateHelpers.getDayOfMonthPredicate(policy
					.getConditions());
			if (day != null) { // setup day section
				String dayText = ((Relation) day).getRHS().toString();
				result.append("Day ");
				result.append(dayText);
				result.append(" of every month");
			}
		}

		if (result.length() > 7) {
			composite = new Composite(this, SWT.NONE);
			data = new GridData(GridData.FILL_HORIZONTAL);
			composite.setLayoutData(data);
			layout = new GridLayout(2, false);
			layout.marginHeight = 0;
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = 5;
			composite.setLayout(layout);

			label = new Label(composite, SWT.WRAP);
			label.setFont(FontBundle.ARIAL_9_BOLD);
			data = new GridData(GridData.FILL_HORIZONTAL);
			label.setLayoutData(data);
			label.setText(result.toString());
		}
		displayAdvancedCondition();

	}
	
	protected void displayAdvancedCondition(){
		
		String advancedCondition = ConditionPredicateHelper.getFreeTypeConditionString(policy.getConditions());
		if(advancedCondition != null) {
			Composite composite = new Composite(this, SWT.NONE);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
            composite.setLayoutData(data);
            GridLayout layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 5;
            composite.setLayout(layout);
    
            Label label = new Label(composite, SWT.NONE);
            label.setText("Condition Expression:");
            label.setFont(FontBundle.ARIAL_9_ITALIC);
            data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
            label.setLayoutData(data);
    
            label = new Label(composite, SWT.WRAP);
            label.setFont(FontBundle.ARIAL_9_BOLD);
            label.setText(advancedCondition);
		}
	}

	protected void displayDescription() {
		String description = policy.getDescription();

		if (description != null && description.length() != 0) {
			Label label = new Label(this, SWT.LEFT | SWT.WRAP);
			label.setText("Description:");
			label.setFont(FontBundle.ARIAL_9_NORMAL);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			label.setLayoutData(data);

			label = new Label(this, SWT.LEFT | SWT.WRAP);
			label.setText(description);
			label.setFont(FontBundle.ARIAL_9_BOLD);
			data = new GridData(GridData.FILL_HORIZONTAL);
			label.setLayoutData(data);

			new Label(this, SWT.NONE);
		}
	}
	
	protected void displayTag() {
		Collection<IPair<String, String>> tags = ((Policy)policy).getTags();
		if(tags!=null && tags.size()!=0){
			Label label = new Label(this, SWT.LEFT | SWT.WRAP);
			label.setText("Tags:");
			label.setFont(FontBundle.ARIAL_9_NORMAL);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			label.setLayoutData(data);

			Label labelAction = new Label(this, SWT.LEFT | SWT.WRAP);
			labelAction.setText(getTagLabel("",tags));
			labelAction.setFont(FontBundle.ARIAL_9_BOLD);
			data = new GridData(GridData.FILL_HORIZONTAL);
			labelAction.setLayoutData(data);

			new Label(this, SWT.NONE);
		}
	}

	protected void displayEnforcement() {
		GridLayout layout;
		GridData data;
		Composite composite = new Composite(this, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 5;
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);

		Label label = new Label(composite, SWT.NONE);
		label.setText("Enforcement:");
		label.setFont(FontBundle.ARIAL_9_NORMAL);

		Label labelEnforcement = new Label(composite, SWT.LEFT | SWT.WRAP);
		labelEnforcement.setFont(FontBundle.ARIAL_9_BOLD);
		labelEnforcement.setText(getEnforcement());
		data = new GridData(GridData.FILL_HORIZONTAL);
		labelEnforcement.setLayoutData(data);
	}

	@SuppressWarnings("unchecked")
	protected void displayObligationForType(IDEffectType effectType) {
		GridLayout layout;
		GridData data;
		Label label;
		List<IObligation> logList = findObligations("log", effectType);
		List<IObligation> displayList = findObligations("display", effectType);
		List<IObligation> notifyList = findObligations("notify", effectType);
		List<IObligation> customList = findObligations("custom", effectType);
		if (!logList.isEmpty() || !displayList.isEmpty()
				|| !notifyList.isEmpty() || !customList.isEmpty()) {
			Composite composite = new Composite(this, SWT.NONE);
			data = new GridData(GridData.FILL_HORIZONTAL);
			composite.setLayoutData(data);
			layout = new GridLayout(2, false);
			layout.marginHeight = 0;
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = 5;
			composite.setLayout(layout);

			label = new Label(composite, SWT.NONE);
			if (effectType.getType() == EffectType.ALLOW_TYPE) {
				label.setText("On Allow, Monitor:");
			} else if (effectType.getType() == EffectType.DENY_TYPE) {
				label.setText("On Deny:");
			}
			label.setFont(FontBundle.ARIAL_9_ITALIC);
			data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
			label.setLayoutData(data);

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

			label = new Label(composite, SWT.LEFT | SWT.WRAP);
			label.setFont(FontBundle.ARIAL_9_BOLD);
			label.setText(result.toString());
			data = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
			label.setLayoutData(data);

			if (!displayList.isEmpty()) {
				composite = new Composite(this, SWT.NONE);
				data = new GridData(GridData.FILL_HORIZONTAL);
				composite.setLayoutData(data);
				layout = new GridLayout(2, false);
				layout.marginHeight = 0;
				layout.verticalSpacing = 0;
				layout.horizontalSpacing = 5;
				composite.setLayout(layout);

				label = new Label(composite, SWT.NONE);
				label.setText("Send Message:");
				label.setFont(FontBundle.ARIAL_9_NORMAL);
				data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
				label.setLayoutData(data);

				DisplayObligation displayObligation = (DisplayObligation) displayList
						.get(0);
				label = new Label(composite, SWT.WRAP);
				label.setFont(FontBundle.ARIAL_9_BOLD);
				data = new GridData(GridData.FILL_HORIZONTAL);
				label.setLayoutData(data);
				label.setText(displayObligation.getMessage());
			}

			if (!notifyList.isEmpty()) {
				composite = new Composite(this, SWT.NONE);
				data = new GridData(GridData.FILL_HORIZONTAL);
				composite.setLayoutData(data);
				layout = new GridLayout(2, false);
				layout.marginHeight = 0;
				layout.verticalSpacing = 0;
				layout.horizontalSpacing = 5;
				composite.setLayout(layout);

				NotifyObligation notifyObligation = (NotifyObligation) notifyList
						.get(0);

				label = new Label(composite, SWT.NONE);
				label.setText("Email To:");
				label.setFont(FontBundle.ARIAL_9_NORMAL);
				data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
				label.setLayoutData(data);

				label = new Label(composite, SWT.WRAP);
				label.setFont(FontBundle.ARIAL_9_BOLD);
				data = new GridData(GridData.FILL_HORIZONTAL);
				label.setLayoutData(data);
				label.setText(notifyObligation.getEmailAddresses());

				composite = new Composite(this, SWT.NONE);
				data = new GridData(GridData.FILL_HORIZONTAL);
				composite.setLayoutData(data);
				layout = new GridLayout(2, false);
				layout.marginHeight = 0;
				layout.verticalSpacing = 0;
				layout.horizontalSpacing = 5;
				composite.setLayout(layout);

				label = new Label(composite, SWT.NONE);
				label.setText("Email Message:");
				label.setFont(FontBundle.ARIAL_9_NORMAL);
				data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
				label.setLayoutData(data);

				label = new Label(composite, SWT.WRAP);
				label.setFont(FontBundle.ARIAL_9_BOLD);
				data = new GridData(GridData.FILL_HORIZONTAL);
				label.setLayoutData(data);
				label.setText(notifyObligation.getBody());
			}

			if (!customList.isEmpty()) {
				composite = new Composite(this, SWT.NONE);
				data = new GridData(GridData.FILL_HORIZONTAL);
				composite.setLayoutData(data);
				layout = new GridLayout(1, false);
				layout.marginHeight = 0;
				layout.verticalSpacing = 0;
				layout.horizontalSpacing = 5;
				composite.setLayout(layout);

				label = new Label(composite, SWT.NONE);
				label.setText("Custom Obligation:");
				label.setFont(FontBundle.ARIAL_9_NORMAL);
				data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
				label.setLayoutData(data);

				for (Object object : customList) {
					result = new StringBuffer();
					CustomObligation obligation = (CustomObligation) object;
					result.append(obligation.getCustomObligationName());

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

					label = new Label(composite, SWT.WRAP);
					label.setFont(FontBundle.ARIAL_9_BOLD);
					data = new GridData(GridData.FILL_HORIZONTAL);
					label.setLayoutData(data);
					label.setText(result.toString());
				}
			}
		}
	}

	protected void displayObligations() {
		GridData data;
		Label label = new Label(this, SWT.LEFT | SWT.WRAP);
		label.setText("Obligations:");
		label.setFont(FontBundle.ARIAL_9_NORMAL);
		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		label.setLayoutData(data);

		if (!getEnforcement().equals(EditorMessages.POLICYEDITOR_MONITOR)) {
			displayObligationForType(EffectType.DENY);
		}
		displayObligationForType(EffectType.ALLOW);
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
			String tagName = tag.first()+" : "+tag.second();
			result.append(tagName);
			result.append("\n");
		}
		if (result.length() > 2) {
			result.deleteCharAt(result.length() - 1);
		}
		return result.toString();
	}
	
	protected List<IPolicyReference> getExcptions() {
        IPolicyExceptions exception =  policy.getPolicyExceptions();
        return exception.getPolicies();
	}

	protected CompositePredicate getActions() {
		ITarget target = policy.getTarget();
		return (CompositePredicate) target.getActionPred();
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
		Relation rel = (Relation) PredicateHelpers.getHeartbeat(policy
				.getConditions());
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
		return name.substring(index + 1);
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

	protected void getUserSubjectLabel(Composite parent, String[] operators,
			List<CompositePredicate> predicates) {
		boolean firsttime = true;
		for (CompositePredicate predicate : predicates) {
			Composite container = new Composite(parent, SWT.NONE);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;
			container.setLayoutData(data);
			GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = 0;
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = 5;
			container.setLayout(layout);

			String operator = getOperator(operators, predicate);
			if (PredicateHelpers.isNegationPredicate(predicate)) {
				predicate = (CompositePredicate) predicate.predicateAt(0);
			}
			Label label = new Label(container, SWT.LEFT | SWT.WRAP);
			label.setFont(FontBundle.ARIAL_9_ITALIC);
			if (firsttime) {
				label.setText(operator.trim());
				firsttime = false;
			} else {
				label.setText(("And" + operator).trim());
			}
			data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
			label.setLayoutData(data);

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
			label = new Label(container, SWT.LEFT | SWT.WRAP);
			label.setFont(FontBundle.ARIAL_9_BOLD);
			if (result.indexOf(" OR ") != -1) {
				label.setText("(" + result.toString() + ")");
			} else {
				label.setText(result.toString());
			}
			data = new GridData(GridData.FILL_HORIZONTAL);
			label.setLayoutData(data);
		}
	}

	protected String getWeekdayForExpression(IExpression exp) {
		Long i = (Long) exp.evaluate(null).getValue();
		return CommonPolicyConstants.DAY_NAMES[i.intValue() - 1];
	}

	abstract protected void initialize();

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

	protected void setBackgroud(Control parent) {
		parent.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
		if (parent instanceof Composite) {

			for (Control control : ((Composite) parent).getChildren()) {
				setBackgroud(control);
			}
		}
	}
	
	protected void displayExceptions() {
		GridData data;

		Label labelPerform = new Label(this, SWT.LEFT | SWT.WRAP);
		labelPerform.setText("Subpolicy:");
		labelPerform.setFont(FontBundle.ARIAL_9_NORMAL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		labelPerform.setLayoutData(data);

        List<IPolicyReference> exceptionRefList = getExcptions();
		if (!exceptionRefList.isEmpty()) {
			Label labelAction = new Label(this, SWT.LEFT | SWT.WRAP);
			labelAction.setText(getExceptionLabel("",exceptionRefList));
			labelAction.setFont(FontBundle.ARIAL_9_BOLD);
			data = new GridData(GridData.FILL_HORIZONTAL);
			labelAction.setLayoutData(data);
		}
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
