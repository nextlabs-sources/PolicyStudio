/*
 * Created on Aug 22, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.obligation.DObligationManager;
import com.bluejungle.pf.domain.destiny.obligation.IDObligationManager;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/D_Plugins/com.nextlabs.policystudio/src/com
 *          /bluejungle/destiny/policymanager/editor/BaseObligationEditor.java#1
 *          $
 */

abstract public class BaseObligationEditor extends Composite {
	private IDObligationManager obligationManager = (IDObligationManager) ComponentManagerFactory
			.getComponentManager().getComponent(DObligationManager.COMP_INFO);
	private IDPolicy policy;
	private IDEffectType effectType;
	protected Button obligationCheckBox;

	public BaseObligationEditor(Composite parent, IDPolicy policy,
			IDEffectType effectType, boolean enabled) {
		super(parent, SWT.NONE);

		this.policy = policy;
		this.effectType = effectType;

		setEnabled(enabled);

		init();

		initData();
	}

	protected void init() {
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		setLayoutData(data);

		obligationCheckBox = new Button(this, SWT.CHECK);
		obligationCheckBox.setText(getTitle());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		obligationCheckBox.setLayoutData(data);
	}

	protected void initData() {
		boolean initiallySelected = obligationExists();
		obligationCheckBox.setSelection(initiallySelected);
		obligationCheckBox.setEnabled(getEnabled());
	}

	abstract protected String getTitle();

	abstract protected String getObligationType();

	protected boolean obligationExists() {
		return !findObligations().isEmpty();
	}

	public List<IObligation> findObligations() {
		List<IObligation> obligationsToReturn = new LinkedList<IObligation>();

		String obligationType = getObligationType();
		IDPolicy policy = getPolicy();
		IDEffectType effectType = getEffectType();
		Collection<IObligation> obligations = policy.getObligations(effectType);
		for (IObligation nextObligation : obligations) {
			String nextObligationType = nextObligation.getType();
			if (nextObligationType.equals(obligationType)) {
				obligationsToReturn.add(nextObligation);
			}
		}
		return obligationsToReturn;
	}

	public IDPolicy getPolicy() {
		return policy;
	}

	public IDEffectType getEffectType() {
		return effectType;
	}

	public IDObligationManager getObligationManager() {
		return obligationManager;
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		for (Control child : getChildren()) {
			setControlBackground(child, color);
		}
	}

	private void setControlBackground(Control control, Color color) {
		control.setBackground(color);
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			for (Control child : composite.getChildren()) {
				setControlBackground(child, color);
			}
		}
	}

	protected IObligation getObligation() {
		List<IObligation> obligationToReturn = findObligations();
		if (obligationToReturn.isEmpty()) {
			throw new IllegalStateException(
					"Obligation is not present in policy");
		}

		return obligationToReturn.get(0);
	}
}
