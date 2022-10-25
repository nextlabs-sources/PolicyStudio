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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElement;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElementOp;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.obligation.DisplayObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/D_Plugins/com.nextlabs.policystudio/src/com
 *          /bluejungle
 *          /destiny/policymanager/editor/SendMessageObligationEditor.java#1 $
 */

public class SendMessageObligationEditor extends BaseObligationEditor {

	private Button check;
	private Text textMessage;

	public SendMessageObligationEditor(Composite parent, IDPolicy policy,
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

				SendMessageObligationEditor.this.layout();
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

		textMessage = new Text(this, SWT.MULTI | SWT.BORDER | SWT.WRAP
				| SWT.V_SCROLL);
		textMessage.setEnabled(getEnabled());
		GridData data = new GridData();
		data.heightHint = textMessage.getLineHeight() * 3;
		data.widthHint = 200;
		textMessage.setLayoutData(data);
		textMessage.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				DisplayObligation displayObligation = (DisplayObligation) getObligation();
				String newValue = textMessage.getText();
				String oldValue = displayObligation.getMessage();
				if (newValue != null && !newValue.equals(oldValue)) {
					displayObligation.setMessage(newValue);
				}
			}
		});

		setBackground(getBackground());
	}

	private void disposeDetail() {
		check.dispose();
		check = null;
		textMessage.dispose();
		textMessage = null;
	}

	@Override
	protected void initData() {
		super.initData();

		if (obligationExists()) {
			DisplayObligation displayObligation = (DisplayObligation) getObligation();
			textMessage.setText(displayObligation.getMessage());
		}
	}

	@Override
	protected String getTitle() {
		return EditorMessages.POLICYEDITOR_DISPLAY_USER_ALERT;
	}

	@Override
	protected String getObligationType() {
		return DisplayObligation.OBLIGATION_NAME;
	}

	protected IObligation createObligation() {
		return getObligationManager().createDisplayObligation("");
	}
}
