/*
 * Created on Aug 22, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElement;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElementOp;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.obligation.NotifyObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/D_Plugins/com.nextlabs.policystudio/src/com
 *          /bluejungle
 *          /destiny/policymanager/editor/SendEmailObligationEditor.java#1 $
 */

public class SendEmailObligationEditor extends BaseObligationEditor {

	private Button check;
	private Group groupEmail;
	private Label labelEmailTo, labelEmailMsg;
	private Text textEmailTo, textEmailMsg;

	public SendEmailObligationEditor(Composite parent, IDPolicy policy,
			IDEffectType effectType, boolean enabled) {
		super(parent, policy, effectType, enabled);
	}

	@Override
	protected void init() {
		super.init();

		obligationCheckBox.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IDPolicy policy = getPolicy();
				IDEffectType effectType = getEffectType();
				boolean checkBoxIsSelected = obligationCheckBox.getSelection();
				if (checkBoxIsSelected) {
					IObligation obligationToAdd = createObligation();
					policy.addObligation(obligationToAdd, effectType);
					
					PolicyUndoElement.add(
					        PolicyUndoElementOp.ADD_OBLIGATION
					      , null
					      , new PolicyUndoElement.ObligationRecord(effectType, obligationToAdd)
					);

					createDetail();
				} else {
					IObligation obligationToRemove = getObligation();
					policy.deleteObligation(obligationToRemove, effectType);
					PolicyUndoElement.add(
                       PolicyUndoElementOp.REMOVE_OBLIGATION
                     , new PolicyUndoElement.ObligationRecord(effectType, obligationToRemove)
                     , null
               );

					disposeDetail();
				}

				SendEmailObligationEditor.this.layout();
				GlobalState.getInstance().getEditorPanel().relayout();
			}
		});

		if (obligationExists()) {
			createDetail();
		}
	}

	private void createDetail() {
		check = new Button(this, SWT.CHECK);
		check.setVisible(false);

		groupEmail = new Group(this, SWT.NONE);
		groupEmail.setEnabled(getEnabled());
		GridLayout layout = new GridLayout(2, false);
		groupEmail.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		groupEmail.setLayoutData(data);

		labelEmailTo = new Label(groupEmail, SWT.NONE);
		labelEmailTo.setEnabled(getEnabled());
		labelEmailTo.setText(EditorMessages.POLICYEDITOR_TO);
		data = new GridData(GridData.HORIZONTAL_ALIGN_END
				| GridData.VERTICAL_ALIGN_BEGINNING);
		labelEmailTo.setLayoutData(data);

		textEmailTo = new Text(groupEmail, SWT.SINGLE | SWT.BORDER);
		textEmailTo.setEnabled(getEnabled());
		data = new GridData();
		data.widthHint = 200;
		textEmailTo.setLayoutData(data);
		textEmailTo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				NotifyObligation notifyObligation = (NotifyObligation) getObligation();
				String newValue = textEmailTo.getText();
				String oldValue = notifyObligation.getEmailAddresses();
				if (newValue != null && !newValue.equals(oldValue)) {
					notifyObligation.setEmailAddresses(newValue);
				}
			}
		});

		labelEmailMsg = new Label(groupEmail, SWT.NONE);
		labelEmailMsg.setEnabled(getEnabled());
		labelEmailMsg.setText(EditorMessages.POLICYEDITOR_MESSAGE);
		data = new GridData(GridData.HORIZONTAL_ALIGN_END
				| GridData.VERTICAL_ALIGN_BEGINNING);
		labelEmailMsg.setLayoutData(data);

		textEmailMsg = new Text(groupEmail, SWT.MULTI | SWT.BORDER | SWT.WRAP
				| SWT.V_SCROLL);
		textEmailMsg.setEnabled(getEnabled());
		data = new GridData();
		data.widthHint = 200;
		data.heightHint = 3 * textEmailMsg.getLineHeight();
		textEmailMsg.setLayoutData(data);
		textEmailMsg.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				NotifyObligation notifyObligation = (NotifyObligation) getObligation();
				String newValue = textEmailMsg.getText();
				String oldValue = notifyObligation.getBody();
				if (newValue != null && !newValue.equals(oldValue)) {
					notifyObligation.setBody(newValue);
				}
			}
		});

		setBackground(getBackground());
	}

	private void disposeDetail() {
		check.dispose();
		labelEmailTo.dispose();
		labelEmailMsg.dispose();
		textEmailTo.dispose();
		textEmailMsg.dispose();
		groupEmail.dispose();
		labelEmailTo = null;
		labelEmailMsg = null;
		textEmailTo = null;
		textEmailMsg = null;
		groupEmail = null;
		check = null;
	}

	@Override
	protected void initData() {
		super.initData();

		if (obligationExists()) {
			NotifyObligation notifyObligation = (NotifyObligation) getObligation();
			textEmailTo.setText(notifyObligation.getEmailAddresses());
			textEmailMsg.setText(notifyObligation.getBody());
		}
	}

	@Override
	protected String getTitle() {
		return EditorMessages.POLICYEDITOR_SEND_EMAIL;
	}

	@Override
	protected String getObligationType() {
		return NotifyObligation.OBLIGATION_NAME;
	}

	protected IObligation createObligation() {
		return getObligationManager().createNotifyObligation("", "");
	}
}
