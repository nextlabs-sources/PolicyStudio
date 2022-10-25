/**
 * Created on July, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 * @author Ivy Chiang
 * 
 */
package com.nextlabs.policystudio.readonly.DynamicAccessControl;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.editor.CommonPolicyConstants;
import com.bluejungle.destiny.policymanager.editor.CommonPolicyDetailsComposite;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;


public class DynamicAccessControlPolicyDetailsComposite extends
		CommonPolicyDetailsComposite {
	public DynamicAccessControlPolicyDetailsComposite(Composite parent, int style,
			IDPolicy policy) {
		super(parent, style, policy);
	}

	@Override
	protected void initialize() {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		setLayout(layout);
		displayPolicyName();
		
		if(!PolicyHelpers.isSubPolicy(policy)){
			new Label(this, SWT.NONE);
			displayExceptions();
			
			new Label(this, SWT.NONE);
			displayDescription();
			
			new Label(this, SWT.NONE);
			displayTag();
		}else{
			new Label(this, SWT.NONE);
			diaplaySubjects();

			new Label(this, SWT.NONE);
			displayActions();

			new Label(this, SWT.NONE);
			displayResources();
			
			new Label(this, SWT.NONE);
			displayAdvancedCondition();
			
			new Label(this, SWT.NONE);
			displayDescription();
			
			new Label(this, SWT.NONE);
			displayTag();
			
			Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			separator.setLayoutData(data);
			
			displayObligations();
		}
		setBackgroud(this);
	}

	private void displayResources() {
		GridData data;
		GridLayout layout;
		Label labelOnResource = new Label(this, SWT.LEFT | SWT.WRAP);
		labelOnResource.setText("On Resource(s):");
		labelOnResource.setFont(FontBundle.ARIAL_9_NORMAL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		labelOnResource.setLayoutData(data);

		CompositePredicate fromResources = getFromSources();
		List<CompositePredicate> fromList = filterPredicate(fromResources);
		boolean isEmpty = true;
		if (!fromList.isEmpty()) {
			Composite composite = new Composite(this, SWT.NONE);
			data = new GridData(GridData.FILL_HORIZONTAL);
			composite.setLayoutData(data);
			layout = new GridLayout(2, false);
			layout.marginHeight = 0;
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = 5;
			composite.setLayout(layout);

			getUserSubjectLabel(composite, CommonPolicyConstants.operators1, fromList);
			isEmpty = false;
		}

		CompositePredicate toSources = getToSources();
		List<CompositePredicate> toList = filterPredicate(toSources);
		if (!toList.isEmpty()) {
			Composite composite = new Composite(this, SWT.NONE);
			data = new GridData(GridData.FILL_HORIZONTAL);
			composite.setLayoutData(data);
			layout = new GridLayout(2, false);
			layout.marginHeight = 0;
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = 0;
			composite.setLayout(layout);

			if (isEmpty) {
				getUserSubjectLabel(composite, CommonPolicyConstants.operators21, toList);
			} else {
				getUserSubjectLabel(composite, CommonPolicyConstants.operators22, toList);
			}
		}

	}

	private void displayActions() {
		GridData data;

		Label labelPerform = new Label(this, SWT.LEFT | SWT.WRAP);
		labelPerform.setText("From performing the Action(s):");
		labelPerform.setFont(FontBundle.ARIAL_9_NORMAL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		labelPerform.setLayoutData(data);

		CompositePredicate actions = getActions();
		List<CompositePredicate> actionList = filterPredicate(actions);
		if (!actionList.isEmpty()) {
			Label labelAction = new Label(this, SWT.LEFT | SWT.WRAP);
			labelAction.setText(getActionLabel("", actionList));
			labelAction.setFont(FontBundle.ARIAL_9_BOLD);
			data = new GridData(GridData.FILL_HORIZONTAL);
			labelAction.setLayoutData(data);
		}
	}

	private void diaplaySubjects() {
		GridLayout layout;
		GridData data;
		Composite composite = new Composite(this, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(data);
		layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);

		Label labelSubjects = new Label(composite, SWT.LEFT | SWT.WRAP);
		labelSubjects.setText("Subject(s): ");
		labelSubjects.setFont(FontBundle.ARIAL_9_NORMAL);
		data = new GridData();
		labelSubjects.setLayoutData(data);

		CompositePredicate users = getUsers();
		List<CompositePredicate> userList = filterPredicate(users);
		boolean isEmpty = true;
		if (!userList.isEmpty()) {
			Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
			label.setText("Users");
			label.setFont(FontBundle.ARIAL_9_ITALIC);
			isEmpty = false;
			data = new GridData();
			label.setLayoutData(data);

			getUserSubjectLabel(composite, CommonPolicyConstants.operators1, userList);
		}

		CompositePredicate computers = getComputers();
		List<CompositePredicate> computerList = filterPredicate(computers);
		if (!computerList.isEmpty()) {
			composite = new Composite(this, SWT.NONE);
			data = new GridData(GridData.FILL_HORIZONTAL);
			composite.setLayoutData(data);
			layout = new GridLayout(2, false);
			layout.marginHeight = 0;
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = 0;
			composite.setLayout(layout);

			Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
			label.setFont(FontBundle.ARIAL_9_ITALIC);
			if (isEmpty) {
				label.setText("Computers");
				isEmpty = false;
			} else {
				label.setText("And Computers");
			}
			data = new GridData();
			label.setLayoutData(data);

			getUserSubjectLabel(composite, CommonPolicyConstants.operators1, computerList);
		}

	}

	private void displayPolicyName() {
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
		labelPolicyImage.setImage(ImageBundle.POLICY_IMG);
		data = new GridData(GridData.BEGINNING);
		labelPolicyImage.setLayoutData(data);

		Label labelPolicyName = new Label(composite, SWT.LEFT | SWT.WRAP);
		labelPolicyName.setFont(FontBundle.ARIAL_9_ITALIC);
		labelPolicyName.setText(getPolicyName() + " [Dynamic Access Control Policy]");
		data = new GridData(GridData.FILL_HORIZONTAL);
		labelPolicyName.setLayoutData(data);
	}
}
