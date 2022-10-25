/*
 * Created on Jul 3, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.nextlabs.policystudio.pdf.devicecontrol;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.bluejungle.destiny.policymanager.editor.CommonPolicyConstants;
import com.bluejungle.destiny.policymanager.editor.EditorMessages;
import com.bluejungle.destiny.policymanager.editor.PolicyPdfGenerator;
import com.bluejungle.destiny.policymanager.model.IPdfGenerator;
import com.bluejungle.destiny.policymanager.model.TimeRelationHelper;
import com.bluejungle.destiny.policymanager.ui.ConditionPredicateHelper;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/personal/ivy/policystudio/main/com.nextlabs.policystudio
 *          .pdf.devicecontrol/src/com/nextlabs/policystudio/pdf/devicecontrol/
 *          DeviceControlPolicyPdfGenerator.java#4 $
 */

public class DeviceControlPolicyPdfGenerator extends PolicyPdfGenerator
		implements IPdfGenerator {
	public DeviceControlPolicyPdfGenerator(IDPolicy policy,
			DomainObjectDescriptor descriptor, Document document) {
		super(policy, descriptor, document);
	}

	public void generate() throws DocumentException {
		// policy name
		PdfPTable table = new PdfPTable(1);
		table.setSpacingBefore(24);
		table.setWidthPercentage(100);
		float[] w = { 1f };
		table.setWidths(w);

		String time;
		Collection<DeploymentHistory> records = PolicyServerProxy
				.getDeploymentRecords(descriptor);
		if (!records.isEmpty()) {
			Date latestActiveFrom = TimeRelationHelper
					.getLatestActiveFrom(records);
			Format formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");
			time = formatter.format(latestActiveFrom);
		} else {
			time = "Unknown";
		}

		String version;
		int size = records.size();
		if (size > 0) {
			version = String.valueOf(size);
		} else {
			version = "Unknown";
		}
		Font font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
		Phrase phrase = new Phrase(
				getPolicyName() + " [Device Control Policy]", font);
		Paragraph paragraph = new Paragraph();
		paragraph.add(phrase);

		PdfPCell cell = new PdfPCell(paragraph);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.addCell(cell);

		font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
		phrase = new Phrase("Version: " + version, font);
		paragraph = new Paragraph();
		paragraph.add(phrase);

		cell = new PdfPCell(paragraph);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.addCell(cell);

		phrase = new Phrase("Activation Time: " + time, font);
		paragraph = new Paragraph();
		paragraph.add(phrase);
		cell = new PdfPCell(paragraph);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.addCell(cell);

		phrase = new Phrase("Owner: " + getActivatedBy(descriptor), font);
		paragraph = new Paragraph();
		paragraph.add(phrase);
		cell = new PdfPCell(paragraph);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.addCell(cell);

		document.add(table);

		// enforcement
		font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
		phrase = new Phrase(getEnforcement(), font);
		paragraph = new Paragraph();
		paragraph.setSpacingBefore(8);
		paragraph.add(phrase);
		document.add(paragraph);

		// subject
		font = new Font(Font.TIMES_ROMAN, 12, Font.ITALIC);
		phrase = new Phrase("Subject(s): ", font);
		paragraph = new Paragraph();
		paragraph.setSpacingBefore(8);
		paragraph.add(phrase);

		CompositePredicate users = getUsers();
		List<CompositePredicate> userList = filterPredicate(users);
		boolean isEmpty = true;
		if (!userList.isEmpty()) {
			font = new Font(Font.TIMES_ROMAN, 12, Font.ITALIC);
			phrase = new Phrase("Users", font);
			paragraph.add(phrase);
			document.add(paragraph);

			getUserSubjectLabel(CommonPolicyConstants.operators1, userList);
		} else {
			document.add(paragraph);
		}

		CompositePredicate computers = getComputers();
		List<CompositePredicate> computerList = filterPredicate(computers);
		if (!computerList.isEmpty()) {
			paragraph = new Paragraph();
			if (isEmpty) {
				font = new Font(Font.TIMES_ROMAN, 12, Font.ITALIC);
				phrase = new Phrase("On Computers", font);
				paragraph.add(phrase);
				document.add(paragraph);
				isEmpty = false;
			} else {
				font = new Font(Font.TIMES_ROMAN, 12, Font.ITALIC);
				phrase = new Phrase("And On Computers", font);
				paragraph.add(phrase);
				document.add(paragraph);
			}
			getUserSubjectLabel(CommonPolicyConstants.operators1, computerList);
		}

		// resource
		font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
		phrase = new Phrase("And Attach the Following Resource(s): ", font);
		paragraph = new Paragraph();
		paragraph.setSpacingBefore(8);
		paragraph.add(phrase);
		document.add(paragraph);

		// CompositePredicate fromResources = getRecipients();
		// List<CompositePredicate> fromList = filterPredicate(fromResources);
		// isEmpty = true;
		// if (!fromList.isEmpty()) {
		// getUserSubjectLabel(operators1, fromList);
		// isEmpty = false;
		// }

		// actions
		// font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
		// phrase = new Phrase("Using Channel(s): ", font);
		// paragraph = new Paragraph();
		// paragraph.setSpacingBefore(8);
		// paragraph.add(phrase);
		//
		// CompositePredicate actions = getActions();
		// List<CompositePredicate> actionList = filterPredicate(actions);
		// if (!actionList.isEmpty()) {
		// font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
		// phrase = new Phrase(getActionLabel("", actionList), font);
		// paragraph.add(phrase);
		// }
		// document.add(paragraph);

		// with attachment
		font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
		phrase = new Phrase("Device(s): ", font);
		paragraph = new Paragraph();
		paragraph.setSpacingBefore(8);
		paragraph.add(phrase);

		CompositePredicate attachements = getFromSources();
		List<CompositePredicate> attachementList = filterPredicate(attachements);
		if (!attachementList.isEmpty()) {
			font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
			phrase = new Phrase(getActionLabel("", attachementList), font);
			paragraph.add(phrase);
		}
		document.add(paragraph);

		// conditions
		font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
		phrase = new Phrase("Under Condition(s): ", font);
		paragraph = new Paragraph();
		paragraph.setSpacingBefore(8);
		paragraph.add(phrase);
		document.add(paragraph);

		// IPredicate type = getConnectionTypePredicate();
		// if (type != null) {
		// font = new Font(Font.TIMES_ROMAN, 12, Font.ITALIC);
		// phrase = new Phrase("Connection Type: ", font);
		// paragraph = new Paragraph();
		// paragraph.add(phrase);
		//
		// Long val = (Long) ((IRelation) type).getRHS().evaluate(null)
		// .getValue();
		// int index = val.intValue();
		// if (index == 0) {
		// font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
		// phrase = new Phrase("Local", font);
		// paragraph.add(phrase);
		// } else if (index == 1) {
		// font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
		// phrase = new Phrase("Remote", font);
		// paragraph.add(phrase);
		// }
		// document.add(paragraph);
		// }

		String heartbeat = getHeartbeatInfo();
		if (heartbeat != null) {
			font = new Font(Font.TIMES_ROMAN, 12, Font.ITALIC);
			phrase = new Phrase("Heartbeat: ", font);
			paragraph = new Paragraph();
			paragraph.add(phrase);

			font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
			phrase = new Phrase(heartbeat, font);
			paragraph.add(phrase);

			document.add(paragraph);
		}

		font = new Font(Font.TIMES_ROMAN, 12, Font.ITALIC);
		phrase = new Phrase("Date/Time: ", font);
		paragraph = new Paragraph();
		paragraph.add(phrase);

		IPredicate start = getStartTimePredicate();
		if (start != null) {
			font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
			phrase = new Phrase("Starting " + getDateTime(start), font);
			paragraph.add(phrase);
		} else {
			font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
			phrase = new Phrase("All", font);
			paragraph.add(phrase);
		}

		IPredicate end = getEndTimePredicate();
		if (end != null) {
			font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
			phrase = new Phrase(" Ending " + getDateTime(end), font);
			paragraph.add(phrase);
		}
		document.add(paragraph);

		font = new Font(Font.TIMES_ROMAN, 12, Font.ITALIC);
		phrase = new Phrase("Recurrence: ", font);
		paragraph = new Paragraph();
		paragraph.add(phrase);

		IPredicate scheduleFrom = getScheduleFromPredicate();
		IPredicate scheduleTo = getScheduleToPredicate();
		if (scheduleFrom != null && scheduleTo != null) {
			StringBuffer buffer = new StringBuffer("Time ");
			buffer.append(getTime(scheduleFrom));
			buffer.append(" to ");
			buffer.append(getTime(scheduleTo));

			font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
			phrase = new Phrase(buffer.toString(), font);
			paragraph.add(phrase);
		} else {
			font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
			phrase = new Phrase("24/7", font);
			paragraph.add(phrase);
		}
		document.add(paragraph);

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

			for (int j = 0; j < CommonPolicyConstants.DAY_NAMES.length; j++) {
				if (CommonPolicyConstants.DAY_NAMES[j].toLowerCase().equals(
						savedValue.toLowerCase())) {
					result.append(CommonPolicyConstants.DAY_NAMES[j]);
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
			font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
			phrase = new Phrase(result.toString(), font);
			paragraph = new Paragraph();
			paragraph.add(phrase);
			document.add(paragraph);
		}

		String advancedCondition = ConditionPredicateHelper
				.getFreeTypeConditionString(policy.getConditions());
		if (advancedCondition != null) {
			font = new Font(Font.TIMES_ROMAN, 12, Font.ITALIC);
			phrase = new Phrase("Condition Expression: ", font);
			paragraph = new Paragraph();
			paragraph.add(phrase);

			font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
			phrase = new Phrase(advancedCondition, font);
			paragraph.add(phrase);

			document.add(paragraph);
		}

		// Exception
		font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
		phrase = new Phrase("Subpolicy: ", font);
		paragraph = new Paragraph();
		paragraph.setSpacingBefore(8);
		paragraph.add(phrase);

		List<IPolicyReference> exceptionRefList = getExcptions();
		if (!exceptionRefList.isEmpty()) {
			font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
			phrase = new Phrase(getExceptionLabel("", exceptionRefList), font);
			paragraph.add(phrase);
		}
		document.add(paragraph);
		

		// description
		String description = policy.getDescription();
		if (description != null && description.length() != 0) {
			font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
			phrase = new Phrase("Description: ", font);
			paragraph = new Paragraph();
			paragraph.setSpacingBefore(8);
			paragraph.add(phrase);

			font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
			phrase = new Phrase(description, font);
			paragraph.add(phrase);

			document.add(paragraph);
		}
		
		// tags
		Collection<IPair<String, String>> tags = ((Policy)policy).getTags();
		if(tags!=null && tags.size()!=0){
			font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
			phrase = new Phrase("Tags: ", font);
			paragraph = new Paragraph();
			paragraph.setSpacingBefore(8);
			paragraph.add(phrase);
			
			font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
			phrase = new Phrase(getTagLabel("", tags), font);
			paragraph.add(phrase);

			document.add(paragraph);
		}

		// obligation
		font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
		phrase = new Phrase("Obligations: ", font);
		paragraph = new Paragraph();
		paragraph.setSpacingBefore(4);
		paragraph.add(phrase);
		document.add(paragraph);

		if (!getEnforcement().equals(EditorMessages.POLICYEDITOR_MONITOR)) {
			displayObligationForType(document, EffectType.DENY);
		}

		displayObligationForType(document, EffectType.ALLOW);
	}
}