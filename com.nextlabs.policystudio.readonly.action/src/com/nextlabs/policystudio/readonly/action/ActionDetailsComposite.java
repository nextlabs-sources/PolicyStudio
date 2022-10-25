/*
 * Created on Mar 13, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.nextlabs.policystudio.readonly.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.util.PluginUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.pf.destiny.lifecycle.PolicyActionsDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.IDSpec;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/editor/ActionDetailsComposite.java
 *          #4 $
 */

public class ActionDetailsComposite extends Composite {

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

	public ActionDetailsComposite(Composite parent, int style,
			IDSpec component, Image image, String descritpion, String type) {
		super(parent, style);
		this.component = component;

		initialize();
	}

	private void initialize() {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		setLayout(layout);

		displayComponentName();

		new Label(this, SWT.NONE);

		displayActions();

		new Label(this, SWT.NONE);

		displayDescription();

		setBackgroud(this);
	}

	private void displayDescription() {
		String description = component.getDescription();

		if (description != null && description.length() != 0) {
			GridData data;
			Label label = new Label(this, SWT.LEFT | SWT.WRAP);
			label.setText("Description: " + description);
			label.setFont(FontBundle.ARIAL_9_BOLD);
			data = new GridData(GridData.FILL_HORIZONTAL);
			label.setLayoutData(data);
		}
	}

	private void displayActions() {
		GridLayout layout;
		GridData data;
		Label label = new Label(this, SWT.LEFT | SWT.WRAP);
		label.setText("Basic Action(s): ");
		label.setFont(FontBundle.ARIAL_9_NORMAL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(data);

		Composite composite = new Composite(this, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(data);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);

		Set<IDAction> actionSet = PredicateHelpers
				.getActionSet((CompositePredicate) component.getPredicate());
		for (IDAction action : actionSet) {
			String text = actionsMap.get(action);
			if (text != null) {
				label = new Label(composite, SWT.LEFT | SWT.WRAP);
				label.setText(text);
				label.setFont(FontBundle.ARIAL_9_BOLD);
				data = new GridData(GridData.FILL_HORIZONTAL);
				label.setLayoutData(data);
			}
		}
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
		labelPolicyImage.setImage(PluginUtil.getContextImageForContext("ACTION"));
		data = new GridData(GridData.BEGINNING);
		labelPolicyImage.setLayoutData(data);

		Label labelPolicyName = new Label(composite, SWT.LEFT | SWT.WRAP);
		labelPolicyName.setFont(FontBundle.ARIAL_9_ITALIC);
		labelPolicyName.setText(getCompnentName() + " [Action Component]");
		data = new GridData(GridData.FILL_HORIZONTAL);
		labelPolicyName.setLayoutData(data);
	}

	private String getCompnentName() {
		return DomainObjectHelper.getDisplayName(component);
	}
}
