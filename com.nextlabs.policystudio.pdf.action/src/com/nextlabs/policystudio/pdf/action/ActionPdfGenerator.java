/*
 * Created on Jul 3, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.nextlabs.policystudio.pdf.action;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.policymanager.model.IPdfGenerator;
import com.bluejungle.destiny.policymanager.model.TimeRelationHelper;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.dialogs.DialogMessages;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;
import com.bluejungle.pf.destiny.lifecycle.PolicyActionsDescriptor;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
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
 * @version $Id$
 */

public class ActionPdfGenerator implements IPdfGenerator {

	private static Map<IDAction, String> actionsMap = new HashMap<IDAction, String>();
	static {
		List<PolicyActionsDescriptor> listBasicActions = new ArrayList<PolicyActionsDescriptor>();
		try {
			listBasicActions = (List<PolicyActionsDescriptor>) PolicyServerProxy
					.getAllPolicyActions();
		} catch (PolicyEditorException e) {
		}

		for (int i = 0, n = listBasicActions.size(); i < n; i++) {
			PolicyActionsDescriptor descriptor = listBasicActions.get(i);
			actionsMap.put((IDAction) descriptor.getAction(), descriptor
					.getDisplayName());
		}
	}

	private IDSpec component;
	private DomainObjectDescriptor descriptor;
	private Document document;

	public ActionPdfGenerator(IDSpec component,
			DomainObjectDescriptor descriptor, Document document) {
		this.component = component;
		this.descriptor = descriptor;
		this.document = document;
	}
	
	public void generate() throws DocumentException {
		// component name
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
		Phrase phrase = new Phrase(getCompnentName(), font);
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

		// basic actions
		font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
		phrase = new Phrase("Basic Action(s): ", font);
		paragraph = new Paragraph();
		paragraph.setSpacingBefore(8);
		paragraph.add(phrase);
		document.add(paragraph);

		Set<IDAction> actionSet = PredicateHelpers
				.getActionSet((CompositePredicate) component.getPredicate());
		for (IDAction action : actionSet) {
			String text = actionsMap.get(action);
			if (text != null) {
				font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
				paragraph = new Paragraph();
				phrase = new Phrase(text, font);
				paragraph.add(phrase);
				document.add(paragraph);
			}
		}

		// description
		String description = component.getDescription();
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
	}

	private String getActivatedBy(DomainObjectDescriptor descriptor) {
		LeafObject ownerLeaf = EntityInfoProvider.getLeafObjectByID(descriptor
				.getOwner(), LeafObjectType.APPUSER);
		if (ownerLeaf == null) {
			return DialogMessages.OBJECTPROPERTIESDIALOG_UNKNOWN_OR_DELETED;
		}
		return ownerLeaf.getName();
	}

	private String getCompnentName() {
		StringBuffer result = new StringBuffer("Component - ");
		result.append(DomainObjectHelper.getDisplayName(component));
		result.append(" [Action Component]");
		return result.toString();
	}
}
