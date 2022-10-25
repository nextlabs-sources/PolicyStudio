/*
 * Created on Mar 8, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.nextlabs.policystudio.readonly.communication;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.editor.BasePolicyEditor;
import com.bluejungle.destiny.policymanager.editor.CommonPolicyConstants;
import com.bluejungle.destiny.policymanager.editor.CommonPolicyDetailsComposite;
import com.bluejungle.destiny.policymanager.editor.EditorMessages;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers.EffectTypeEnum;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/
 *          bluejungle/destiny/policymanager/editor/PolicyDetailsComposite.java
 *          #2 $
 */

public class CommunicationPolicyDetailsComposite extends
		CommonPolicyDetailsComposite {
	public CommunicationPolicyDetailsComposite(Composite parent, int style,
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
		displayEnforcement();

		new Label(this, SWT.NONE);
		diaplaySubjects();

		new Label(this, SWT.NONE);
		displayRecipients();

		new Label(this, SWT.NONE);
		displayActions();

		new Label(this, SWT.NONE);
		displayResources();

		new Label(this, SWT.NONE);
		displayDateTime();
		
		new Label(this, SWT.NONE);
		displayExceptions();
		
		new Label(this, SWT.NONE);
		displayDescription();
		
		new Label(this, SWT.NONE);
		displayTag();

		Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		separator.setLayoutData(data);

		displayObligations();

		setBackgroud(this);
	}

	private void displayRecipients() {
		GridData data;
		GridLayout layout;
		Label labelOnResource = new Label(this, SWT.LEFT | SWT.WRAP);
		labelOnResource.setText("And Recipient(s)");
		labelOnResource.setFont(FontBundle.ARIAL_9_NORMAL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		labelOnResource.setLayoutData(data);

		CompositePredicate fromResources = getRecipients();
		List<CompositePredicate> fromList = filterPredicate(fromResources);
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
		}
	}

	private void displayResources() {
		GridData data;
		GridLayout layout;
		Label labelOnResource = new Label(this, SWT.LEFT | SWT.WRAP);
		labelOnResource.setText("With Attachment(s):");
		labelOnResource.setFont(FontBundle.ARIAL_9_NORMAL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		labelOnResource.setLayoutData(data);

		CompositePredicate fromResources = getFromSources();
		List<CompositePredicate> fromList = filterPredicate(fromResources);
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
		}
	}

	private void displayActions() {
		GridData data;

		Label labelPerform = new Label(this, SWT.LEFT | SWT.WRAP);
		labelPerform.setText("Using Channel(s):");
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
		labelSubjects.setText("Communication between Sender(s): ");
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
				label.setText("On Computers");
				isEmpty = false;
			} else {
				label.setText("And On Computers");
			}
			data = new GridData();
			label.setLayoutData(data);

			getUserSubjectLabel(composite, CommonPolicyConstants.operators1, computerList);
		}

		if (!policy.hasAttribute("access")) {
			CompositePredicate applications = getApplications();
			List<CompositePredicate> applicationList = filterPredicate(applications);
			if (!applicationList.isEmpty()) {
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
					label.setText("Using Applications");
					isEmpty = false;
				} else {
					label.setText("And Using Applications");
				}

				data = new GridData();
				label.setLayoutData(data);

				getUserSubjectLabel(composite, CommonPolicyConstants.operators1, applicationList);
			}
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
		labelPolicyName.setText(getPolicyName() + " [Communication Policy]");
		data = new GridData(GridData.FILL_HORIZONTAL);
		labelPolicyName.setLayoutData(data);
	}
}
