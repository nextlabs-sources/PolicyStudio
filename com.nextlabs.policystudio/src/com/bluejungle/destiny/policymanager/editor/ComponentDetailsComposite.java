/*
 * Created on Mar 13, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
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
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectAttribute;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/editor/UserDetailsComposite.java#1
 *          $
 */

public class ComponentDetailsComposite extends Composite {
	private static final Map<String, Collection<AttributeDescriptor>> attributeMaps = new HashMap<String, Collection<AttributeDescriptor>>();
	private static Map<String, AttributeType> attributeTypeMap = new HashMap<String, AttributeType>();
	private static Map<String, String> displayNameMap = new HashMap<String, String>();
	private static Map<RelationOp, String> stringOperators = new HashMap<RelationOp, String>();
	private static Map<RelationOp, String> dateOperators = new HashMap<RelationOp, String>();
	private static Map<RelationOp, String> numberOperators = new HashMap<RelationOp, String>();
	private static Map<RelationOp, String> yesNoStates = new HashMap<RelationOp, String>();
	private static Map<String, AttributeDescriptor> attributeMap = new HashMap<String, AttributeDescriptor>();
	static {
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
	private Image image;
	private String description;

	public ComponentDetailsComposite(Composite parent, int style,
			IDSpec component, Image image, String descritpion, String type) {
		super(parent, style);
		this.component = component;
		this.image = image;
		this.description = descritpion;

		setupAttributesMap(type);

		initialize();
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

			Collection<AttributeDescriptor> resourceAttributes = PolicyServerProxy.getAttributes(EntityType.RESOURCE);
			List<AttributeDescriptor> additionalAttributes = new ArrayList<AttributeDescriptor>();
			for (AttributeDescriptor descriptor : resourceAttributes) {
				String group = descriptor.getGroupName();
				if (group != null
						&& group
								.equals(EditorMessages.COMPONENTEDITOR_WITH_CONTENT)) {
					additionalAttributes.add(descriptor);
				}
			}
			attributes.addAll(additionalAttributes);
		} else if (type.equals("SERVER")) {
		}
		attributeMaps.put(type, attributes);
		for (AttributeDescriptor attribute : attributes) {
			String displayName = attribute.getDisplayName();
			String pqlName = attribute.getPqlName();
			AttributeType attrType = attribute.getType();

			attributeTypeMap.put(pqlName.toLowerCase(), attrType);
			displayNameMap.put(pqlName.toLowerCase(), displayName);
			attributeMap.put(pqlName, attribute);
		}

		// add for HPE support
		AttributeDescriptor descriptor = new AttributeDescriptor(null, "Port",
				AttributeType.LONG, false, ResourceAttribute.PORTAL_SUB_TYPE);

		attributeTypeMap.put("port", AttributeType.LONG);
		displayNameMap.put("port", "Port");
		attributeMap.put("port", descriptor);
	}

	private void initialize() {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		setLayout(layout);

		displayComponentName();

		new Label(this, SWT.NONE);

		displayMembers();

		new Label(this, SWT.NONE);

		displayProperties();

		new Label(this, SWT.NONE);

		displayDescription();

		setBackgroud(this);
	}

	private void displayDescription() {
		String description = component.getDescription();

		if (description != null && description.length() != 0) {
			Composite composite = new Composite(this, SWT.NONE);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			composite.setLayoutData(data);
			GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = 0;
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = 5;
			composite.setLayout(layout);

			Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
			label.setText("Description:");
			label.setFont(FontBundle.ARIAL_9_NORMAL);
			data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
			label.setLayoutData(data);

			label = new Label(composite, SWT.LEFT | SWT.WRAP);
			label.setText(description);
			label.setFont(FontBundle.ARIAL_9_BOLD);
			data = new GridData(GridData.FILL_HORIZONTAL);
			label.setLayoutData(data);

			new Label(this, SWT.NONE);
		}
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
		int index5 = value.lastIndexOf(";*");
		result += value.substring(index4 + 2, index5);

		return result;
	}

	private void displayProperties() {
		GridLayout layout;
		GridData data;
		Composite composite = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 5;
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
		label.setFont(FontBundle.ARIAL_9_NORMAL);
		label.setText("Whose Properties are:");
		data = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(data);

		IPredicate pre = ((ICompositePredicate) component.getPredicate())
				.predicateAt(1);
		CompositePredicate predicate;
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
						if (isOptionalAttribute(expression)
								&& value.equals(Constant.NULL)) {
							if (operator.equals(RelationOp.NOT_EQUALS)) {
								result.append("is set");
							} else if (operator.equals(RelationOp.EQUALS)) {
								result.append("is not set");
							}
						} else {
							result.append(stringOperators.get(operator));
						}
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
						Long dateValue = (Long) ((Constant) relation.getRHS())
								.getValue().getValue();
						if (dateValue != null) {
							Date d = new Date(dateValue.longValue());
							result.append(SimpleDateFormat.getDateInstance()
									.format(d));
						}
					} else if (!isBooleanAttribute(expression)) {
						if (value.equals(Constant.NULL)) {
						} else if (value instanceof SpecAttribute) {
							SpecAttribute attribute = (SpecAttribute) value;
							AttributeDescriptor descriptor = getAttributeDescriptorForName(attribute
									.getName());
							List<AttributeDescriptor> attributes = descriptor
									.getAllowedAttributes();
							if (attributes.size() > 0) {
								IAttribute attr = (IAttribute) value;
								for (AttributeDescriptor desc : attributes) {
									if (desc.getPqlName().equalsIgnoreCase(
											attr.getName())) {
										result.append(desc.getDisplayName());
										break;
									}
								}
							} else {
								result.append(descriptor.getDisplayName());
							}
						} else {
							result.append(stringFromExpression(value));
						}
					}
				}
				label = new Label(composite, SWT.LEFT | SWT.WRAP);
				label.setFont(FontBundle.ARIAL_9_BOLD);
				label.setText(result.toString());
				data = new GridData(GridData.FILL_HORIZONTAL);
				label.setLayoutData(data);
			}
		}
	}

	public AttributeDescriptor getAttributeDescriptorForName(String name) {
		Collection<AttributeDescriptor> descriptors = attributeMaps.get(name);
		for (AttributeDescriptor descriptor : descriptors) {
			if (descriptor.getPqlName().equalsIgnoreCase(name)) {
				return descriptor;
			}
		}
		return null;
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
                    // TODO make sure it is ok to do nothing
                }
			}, IExpressionVisitor.PREORDER);
		}

		return res[0].replaceAll("/+$", "");
	}

	private void displayMembers() {
		GridLayout layout;
		GridData data;
		Composite composite = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 5;
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
		label.setFont(FontBundle.ARIAL_9_NORMAL);
		label.setText("Members:");
		data = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(data);

		CompositePredicate predicate = (CompositePredicate) ((ICompositePredicate) component
				.getPredicate()).predicateAt(0);
		List<CompositePredicate> filteredList = filterPredicate(predicate);
		if (!filteredList.isEmpty()) {
			Composite sub = new Composite(this, SWT.NONE);
			data = new GridData(GridData.FILL_HORIZONTAL);
			sub.setLayoutData(data);
			layout = new GridLayout();
			layout.marginHeight = 0;
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = 5;
			sub.setLayout(layout);

			getUserSubjectLabel(sub, operators1, filteredList);
		}
	}

	private void getUserSubjectLabel(Composite parent, String[] operators,
			List<CompositePredicate> predicates) {
		boolean firsttime = true;
		GridData data;
		for (CompositePredicate predicate : predicates) {
			String operator = getOperator(operators, predicate);
			if (PredicateHelpers.isNegationPredicate(predicate)) {
				predicate = (CompositePredicate) predicate.predicateAt(0);
			}
			Label label = new Label(parent, SWT.LEFT | SWT.WRAP);
			label.setFont(FontBundle.ARIAL_9_ITALIC);
			if (firsttime) {
				label.setText(operator.trim());
				firsttime = false;
			} else {
				label.setText(("And" + operator).trim());
			}
			data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
			label.setLayoutData(data);

			for (int i = 0; i < predicate.predicateCount(); i++) {
				Object predicateElement = predicate.predicateAt(i);
				if (!(predicateElement instanceof PredicateConstants)) {
					label = new Label(parent, SWT.LEFT | SWT.WRAP);
					label.setText(findLabel((IPredicate) predicateElement));
					label.setFont(FontBundle.ARIAL_9_BOLD);
					data = new GridData(GridData.FILL_HORIZONTAL);
					label.setLayoutData(data);
				}
			}
		}
	}

	private String getOperator(String[] operators, CompositePredicate predicate) {
		if (PredicateHelpers.isNegationPredicate(predicate)) {
			return operators[1];
		} else {
			return operators[0];
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

	private void setBackgroud(Control parent) {
		parent.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
		if (parent instanceof Composite) {

			for (Control control : ((Composite) parent).getChildren()) {
				setBackgroud(control);
			}
		}
	}

	private void displayComponentName() {
		GridLayout layout;
		GridData data;
		Composite composite = new Composite(this, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 5;
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(data);

		Label labelPolicyImage = new Label(composite, SWT.NONE);
		labelPolicyImage.setImage(image);
		data = new GridData(GridData.BEGINNING);
		labelPolicyImage.setLayoutData(data);

		Label labelPolicyName = new Label(composite, SWT.LEFT | SWT.WRAP);
		labelPolicyName.setFont(FontBundle.ARIAL_9_ITALIC);
		labelPolicyName.setText(getCompnentName() + " [" + description + "]");
		data = new GridData(GridData.FILL_HORIZONTAL);
		labelPolicyName.setLayoutData(data);
	}

	private String getCompnentName() {
		return DomainObjectHelper.getDisplayName(component);
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

	@SuppressWarnings("deprecation")
	private boolean isOptionalAttribute(IExpression exp) {
		IAttribute attribute = (IAttribute) exp;
		SpecType specType = PredicateHelpers.getExpressionType(exp);
		if (specType.equals(SpecType.PORTAL)) {
			return false;
		}
		EntityType type = EntityType.forSpecType(specType);
		String name = attribute.getName();
		if (name.equalsIgnoreCase("company")) {
			return true;
		}
		Collection<AttributeDescriptor> descriptors = null;
		if (type.equals(EntityType.USER)) {
			descriptors = attributeMaps.get("USER");
		} else if (type.equals(EntityType.HOST)) {
			descriptors = attributeMaps.get("HOST");
		} else if (type.equals(EntityType.APPLICATION)) {
			descriptors = attributeMaps.get("APPLICATION");
		} else if (type.equals(EntityType.ACTION)) {
			descriptors = attributeMaps.get("ACTION");
		} else if (type.equals(EntityType.RESOURCE)) {
			descriptors = attributeMaps.get("RESOURCE");
		} else if (type.equals(EntityType.PORTAL)) {
			descriptors = attributeMaps.get("PORTAL");
		}

		if (descriptors != null) {
			for (AttributeDescriptor descriptor : descriptors) {
				if (descriptor.getPqlName().equalsIgnoreCase(name)) {
					if (!descriptor.isRequired()) {
						return true;
					}
				}
			}
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
}
