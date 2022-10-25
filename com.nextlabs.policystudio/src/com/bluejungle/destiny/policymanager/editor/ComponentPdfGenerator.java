/*
 * Created on Jul 3, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;

import com.bluejungle.destiny.policymanager.PlugInConstant;
import com.bluejungle.destiny.policymanager.model.IPdfGenerator;
import com.bluejungle.destiny.policymanager.model.PolicyServerHelper;
import com.bluejungle.destiny.policymanager.model.TimeRelationHelper;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.dialogs.DialogMessages;
import com.bluejungle.destiny.policymanager.util.PluginUtil;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IExpressionReference;
import com.bluejungle.framework.expressions.IExpressionVisitor;
import com.bluejungle.framework.expressions.IFunctionApplication;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.AttributeDescriptor;
import com.bluejungle.pf.destiny.lifecycle.AttributeType;
import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
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

public class ComponentPdfGenerator implements IPdfGenerator {

	private static Map<SubjectAttribute, LeafObjectType> DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP = new HashMap<SubjectAttribute, LeafObjectType>();
	private static Map<String, AttributeType> attributeTypeMap = new HashMap<String, AttributeType>();
	private static Map<String, String> displayNameMap = new HashMap<String, String>();
	private static Map<RelationOp, String> stringOperators = new HashMap<RelationOp, String>();
	private static Map<RelationOp, String> dateOperators = new HashMap<RelationOp, String>();
	private static Map<RelationOp, String> numberOperators = new HashMap<RelationOp, String>();
	private static Map<RelationOp, String> yesNoStates = new HashMap<RelationOp, String>();
	private static Map<String, AttributeDescriptor> attributeMap = new HashMap<String, AttributeDescriptor>();

	static {
		DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(
				SubjectAttribute.USER_ID, LeafObjectType.USER);
		DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(
				SubjectAttribute.HOST_ID, LeafObjectType.HOST);
		DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(
				SubjectAttribute.APP_ID, LeafObjectType.APPLICATION);
		DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(
				SubjectAttribute.USER_LDAP_GROUP_ID, LeafObjectType.USER_GROUP);
		DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(
				SubjectAttribute.HOST_LDAP_GROUP_ID, LeafObjectType.HOST_GROUP);

		stringOperators.put(RelationOp.EQUALS, "is");
		stringOperators.put(RelationOp.NOT_EQUALS, "is not");

		dateOperators.put(RelationOp.GREATER_THAN_EQUALS, "on or after");
		dateOperators.put(RelationOp.LESS_THAN, "before");

		numberOperators.put(RelationOp.EQUALS, "=");
		numberOperators.put(RelationOp.GREATER_THAN, ">");
		numberOperators.put(RelationOp.GREATER_THAN_EQUALS, ">=");
		numberOperators.put(RelationOp.LESS_THAN, "<");
		numberOperators.put(RelationOp.LESS_THAN_EQUALS, "<=");
		numberOperators.put(RelationOp.NOT_EQUALS, "!=");

		yesNoStates.put(RelationOp.GREATER_THAN, "Yes");
		yesNoStates.put(RelationOp.EQUALS, "No");
	}

	private String[] operators1 = new String[] { " in ", " not in " };

	private IDSpec component;
	private DomainObjectDescriptor descriptor;
	private Document document;

	public ComponentPdfGenerator(IDSpec component,
			DomainObjectDescriptor descriptor, Document document) {
		this.component = component;
		this.descriptor = descriptor;
		this.document = document;
	}

	public void generate() throws DocumentException {
		String type = PolicyServerHelper.getTypeFromComponentName(descriptor.getName());
		setupAttributesMap(type);

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

		// Members
		font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
		phrase = new Phrase("Members: ", font);
		paragraph = new Paragraph();
		paragraph.setSpacingBefore(8);
		paragraph.add(phrase);

		document.add(paragraph);

		CompositePredicate predicate = (CompositePredicate) ((ICompositePredicate) component
				.getPredicate()).predicateAt(0);
		List<CompositePredicate> filteredList = filterPredicate(predicate);
		if (!filteredList.isEmpty()) {
			getUserSubjectLabel(operators1, filteredList);
		}

		// Properties
		font = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
		phrase = new Phrase("Whose Properties are: ", font);
		paragraph = new Paragraph();
		paragraph.setSpacingBefore(8);
		paragraph.add(phrase);
		document.add(paragraph);

		IPredicate pre = ((ICompositePredicate) component.getPredicate())
				.predicateAt(1);
		if (pre instanceof PredicateConstants) {
			predicate = new CompositePredicate(BooleanOp.AND,
					new ArrayList<IPredicate>());
			predicate.addPredicate(PredicateConstants.TRUE);
			predicate.addPredicate(PredicateConstants.TRUE);
		} else {
			predicate = (CompositePredicate) pre;
		}

		for (int i = 0; i < predicate.predicateCount(); i++) {
			IPredicate spec = (IPredicate) predicate.predicateAt(i);

			if (spec instanceof Relation) {
				Relation relation = (Relation) spec;
				StringBuffer result = new StringBuffer();
				IExpression expression = relation.getLHS();
				IExpression value = relation.getRHS();
				String name = "";
				if (expression instanceof SpecAttribute) {
					SpecAttribute specAttribute = (SpecAttribute) expression;
					name = specAttribute.getName();
					String displayName = displayNameMap.get(name.toLowerCase());
					if (displayName == null) {
						result.append(name);
					} else {
						result.append(displayName);
					}
				}

				RelationOp operator = relation.getOp();
				result.append(" ");
				if (name
						.equals(EditorMessages.COMPONENTEDITOR_CONTAINS_CONTENT)) {
					result.append("");
				} else {
					if (isDateAttribute(expression)) {
						result.append(dateOperators.get(operator));
					} else if (isNumberAttribute(expression)) {
						result.append(numberOperators.get(operator));
					} else if (isBooleanAttribute(expression)) {
						result.append(yesNoStates.get(operator));
					} else {
						result.append(stringOperators.get(operator));
					}
					result.append(" ");
				}
				if (name
						.equals(EditorMessages.COMPONENTEDITOR_CONTAINS_CONTENT)) {
					String v = (String) ((Constant) value).getValue()
							.getValue();
					result.append(getContentAnalysisInfo(name, v));
				} else {
					if (isDateAttribute(expression)) {
						Long dateValue = (Long) ((Constant) value).getValue()
								.getValue();
						if (dateValue != null) {
							Date d = new Date(dateValue.longValue());
							result.append(SimpleDateFormat.getDateInstance()
									.format(d));
						}
					} else if (!isBooleanAttribute(expression)) {
						result.append(stringFromExpression(value));
					}
				}
				font = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
				phrase = new Phrase(result.toString(), font);
				paragraph = new Paragraph();
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

	private String stringFromExpression(IExpression exp) {
		final String[] res = new String[] { "" };
		if (exp != null) {
			exp.acceptVisitor(new IExpressionVisitor() {

				public void visit(IAttribute attribute) {
					// we should not get here
				}

				public void visit(Constant constant) {
					res[0] = constant.getRepresentation();
				}

				public void visit(IExpressionReference ref) {
					if (ref.isReferenceByName()) {
						res[0] = ref.getReferencedName();
					} else {
						res[0] = ref.getPrintableReference();
					}
				}

				public void visit(IExpression expression) {
					// we should not get here
				}

                public void visit(IFunctionApplication arg0) {
                    // TODO I think I am ok to do nothing
                    
                }
			}, IExpressionVisitor.PREORDER);
		}

		return res[0].replaceAll("/+$", "");
	}

	private boolean isDateAttribute(IExpression exp) {
		if (exp instanceof SpecAttribute) {
			AttributeType type = getTypeForAttribute((SpecAttribute) exp);
			return type == AttributeType.DATE;
		}
		return false;
	}

	private boolean isNumberAttribute(IExpression exp) {
		if (exp instanceof SpecAttribute) {
			AttributeType type = getTypeForAttribute((SpecAttribute) exp);
			return type == AttributeType.LONG;
		}
		return false;
	}

	private boolean isBooleanAttribute(IExpression exp) {
		if (exp instanceof SpecAttribute) {
			AttributeType type = getTypeForAttribute((SpecAttribute) exp);
			return type == AttributeType.BOOLEAN;
		}
		return false;
	}

	private AttributeType getTypeForAttribute(SpecAttribute attribute) {
		return attributeTypeMap.get(attribute.getName());
	}

	private void getUserSubjectLabel(String[] operators,
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
			document.add(paragraph);

			for (int i = 0; i < predicate.predicateCount(); i++) {
				Object predicateElement = predicate.predicateAt(i);
				if (!(predicateElement instanceof PredicateConstants)) {
					phrase = new Phrase(
							findLabel((IPredicate) predicateElement), boldFont);
					paragraph = new Paragraph();
					paragraph.add(phrase);
					document.add(paragraph);
				}
			}
		}
	}

	private String findLabel(IPredicate spec) {
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
				if (DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP
						.containsKey(lhs)
						&& rhsValue instanceof Long) {
					LeafObjectType associatedLeafObjectType = (LeafObjectType) DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP
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

	private String getOperator(String[] operators, CompositePredicate predicate) {
		if (PredicateHelpers.isNegationPredicate(predicate)) {
			return operators[1];
		} else {
			return operators[0];
		}
	}

	private List<CompositePredicate> filterPredicate(
			CompositePredicate predicate) {
		List<CompositePredicate> filteredPredicate = new ArrayList<CompositePredicate>();
		for (int i = 0; i < predicate.predicateCount(); i++) {
			IPredicate spec = (IPredicate) predicate.predicateAt(i);
			if (spec instanceof CompositePredicate) {
				filteredPredicate.add((CompositePredicate) spec);
			}
		}
		return filteredPredicate;
	}

	private String getCompnentName() {
		StringBuffer result = new StringBuffer("Component - ");
		result.append(DomainObjectHelper.getDisplayName(component));
		result.append(" [");
		String type = PolicyServerHelper.getTypeFromComponentName(descriptor
				.getName());
		IConfigurationElement element = PluginUtil
				.getEditorPluginForContext(type);
		result.append(element.getAttribute(PlugInConstant.DISPLAY_NAME));
		result.append("]");
		return result.toString();
	}

	@SuppressWarnings("deprecation")
	private void setupAttributesMap(String type) {
		Collection<AttributeDescriptor> attributes = new ArrayList<AttributeDescriptor>();
		if (type.equals("USER")) {
			attributes = PolicyServerProxy.getAttributes(EntityType.USER);
		} else if (type.equals("HOST")) {
			attributes = PolicyServerProxy.getAttributes(EntityType.HOST);
		} else if (type.equals("RESOURCE")) {
			attributes = PolicyServerProxy.getAttributes(EntityType.RESOURCE);
		} else if (type.equals("PORTAL")) {
			attributes = PolicyServerProxy.getAttributes(EntityType.PORTAL);
		} else if (type.equals("SERVER")) {
		} else if (type.equals("PORTAL")) {
		}
		attributeTypeMap.clear();
		displayNameMap.clear();
		for (AttributeDescriptor attribute : attributes) {
			String displayName = attribute.getDisplayName();
			String pqlName = attribute.getPqlName();
			AttributeType attrType = attribute.getType();

			attributeTypeMap.put(pqlName.toLowerCase(), attrType);
			displayNameMap.put(pqlName.toLowerCase(), displayName);
			attributeMap.put(pqlName, attribute);
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

	/**
	 * content analysis support
	 * 
	 * @param pqlName
	 * @param value
	 * @return the proper value for the contains content
	 */
	private String getContentAnalysisInfo(String pqlName, String value) {
		AttributeDescriptor descriptor = attributeMap.get(pqlName);
		List<String> values = descriptor.getEnumeratedValues();

		String result = "";
		if (value.startsWith("*KEY:")) {
			result += "Keyword(s) ";
			int index1 = value.indexOf(':');
			int index2 = value.lastIndexOf(">=");
			String key = value.substring(index1 + 1, index2);
			result += key;
		} else {
			int index1 = value.indexOf(':');
			int index2 = value.lastIndexOf(">=");
			String name = value.substring(index1 + 1, index2);
			for (String item : values) {
				if (item.startsWith(name) && item.indexOf(':') != -1) {
					int index3 = item.indexOf(':');
					result += item.substring(index3 + 1);
					break;
				}
			}
		}
		result += " ";
		result += EditorMessages.COMPONENTEDITOR_MATCH_COUNT;

		int index4 = value.lastIndexOf(">=");
		int index5 = value.lastIndexOf('*');
		result += value.substring(index4 + 2, index5);

		return result;
	}
}
