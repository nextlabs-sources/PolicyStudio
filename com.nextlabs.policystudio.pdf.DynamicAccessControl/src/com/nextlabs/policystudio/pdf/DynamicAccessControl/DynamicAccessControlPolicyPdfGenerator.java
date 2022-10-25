/**
 * Created on July, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 * @author Ivy Chiang
 * 
 */

package com.nextlabs.policystudio.pdf.DynamicAccessControl;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.bluejungle.destiny.policymanager.editor.CommonPolicyConstants;
import com.bluejungle.destiny.policymanager.editor.PolicyPdfGenerator;
import com.bluejungle.destiny.policymanager.editor.EditorMessages;
import com.bluejungle.destiny.policymanager.model.IPdfGenerator;
import com.bluejungle.destiny.policymanager.model.TimeRelationHelper;
import com.bluejungle.destiny.policymanager.ui.ConditionPredicateHelper;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.framework.utils.TimeRelation;
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

public class DynamicAccessControlPolicyPdfGenerator extends PolicyPdfGenerator implements IPdfGenerator {
    public DynamicAccessControlPolicyPdfGenerator(IDPolicy policy,
            DomainObjectDescriptor descriptor, Document document) {
        super(policy, descriptor, document);
    }
    @Override
    public void generate() throws DocumentException {
        // policy name
        PdfPTable table = new PdfPTable(1);
        table.setSpacingBefore(24);
        table.setWidthPercentage(100);
        float[] w = { 1f };
        table.setWidths(w);

        String time;
        Collection<DeploymentHistory> records = PolicyServerProxy.getDeploymentRecords(descriptor);
        if (!records.isEmpty()) {
            Date latestActiveFrom = TimeRelationHelper.getLatestActiveFrom(records);
            Format formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");
            time = formatter.format(latestActiveFrom);
        }else {
            time = "Unknown";
        }

        String version;
        int size = records.size();
        if (size > 0) {
            version = String.valueOf(size);
        }else {
            version = "Unknown";
        }
        Font font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
        Phrase phrase = new Phrase(getPolicyName() + " [Dynamic Access Control Policy]", font);
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

        // subject > user
        CompositePredicate users = getUsers();
        List<CompositePredicate> userList = filterPredicate(users);
        boolean isEmpty = true;
        if (!userList.isEmpty()) {
            font = new Font(Font.TIMES_ROMAN, 12, Font.ITALIC);
            phrase = new Phrase("Users", font);
            paragraph.add(phrase);
            document.add(paragraph);

            getUserSubjectLabel(CommonPolicyConstants.operators1, userList);
        }
        // subject > computer
        CompositePredicate computers = getComputers();
        List<CompositePredicate> computerList = filterPredicate(computers);
        if (!computerList.isEmpty()) {
            paragraph = new Paragraph();
            if (isEmpty) {
                font = new Font(Font.TIMES_ROMAN, 12, Font.ITALIC);
                phrase = new Phrase("Computers", font);
                paragraph.add(phrase);
                document.add(paragraph);
                isEmpty = false;
            } else {
                font = new Font(Font.TIMES_ROMAN, 12, Font.ITALIC);
                phrase = new Phrase("And Computers", font);
                paragraph.add(phrase);
                document.add(paragraph);
            }
            getUserSubjectLabel(CommonPolicyConstants.operators1, computerList);
        }

        // Perform the following
        font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
        phrase = new Phrase("From performing the Action(s): ", font);
        paragraph = new Paragraph();
        paragraph.setSpacingBefore(8);
        paragraph.add(phrase);

        // Perform the following > actions
        CompositePredicate actions = getActions();
        List<CompositePredicate> actionList = filterPredicate(actions);
        if (!actionList.isEmpty()) {
            font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
            phrase = new Phrase(getActionLabel("", actionList), font);
            paragraph.add(phrase);
        }
        document.add(paragraph);

        // On resource
        font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
        phrase = new Phrase("On Resource(s): ", font);
        paragraph = new Paragraph();
        paragraph.setSpacingBefore(8);
        paragraph.add(phrase);
        document.add(paragraph);

        // On resource > target (from Resources)
        CompositePredicate fromResources = getFromSources();
        List<CompositePredicate> fromList = filterPredicate(fromResources);
        isEmpty = true;
        if (!fromList.isEmpty()) {
            getUserSubjectLabel(CommonPolicyConstants.operators1, fromList);
            isEmpty = false;
        }

        // On resource > target (to Resources)
        CompositePredicate toSources = getToSources();
        List<CompositePredicate> toList = filterPredicate(toSources);
        if (!toList.isEmpty()) {
            if (isEmpty)
                getUserSubjectLabel(CommonPolicyConstants.operators21, toList);
            else
                getUserSubjectLabel(CommonPolicyConstants.operators22, toList);
        }

        // Conditions
        if (!getEnforcement().equals(EditorMessages.POLICYEDITOR_MONITOR)) {
            font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
            phrase = new Phrase("Under Condition(s): ", font);
            paragraph = new Paragraph();
            paragraph.setSpacingBefore(8);
            paragraph.add(phrase);
            document.add(paragraph);
            
            // Conditions > Condition Expression
            String advancedCondition = ConditionPredicateHelper.getFreeTypeConditionString(policy.getConditions());
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
                phrase = new Phrase(getExceptionLabel("",exceptionRefList), font);
                paragraph.add(phrase);
            }
            document.add(paragraph);
        }

        // Description
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

        // Obligation
        font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
        phrase = new Phrase("Obligations: ", font);
        paragraph = new Paragraph();
        paragraph.setSpacingBefore(4);
        paragraph.add(phrase);
        document.add(paragraph);
//
//        if (!getEnforcement().equals(EditorMessages.POLICYEDITOR_MONITOR)) {
//            displayObligationForType(document, EffectType.DENY);
//        }

        displayObligationForType(document, EffectType.ALLOW);
    }
}
