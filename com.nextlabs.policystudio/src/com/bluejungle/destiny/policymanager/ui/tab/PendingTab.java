/*
 * Created on Aug 21, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.tab;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.model.EntityInformation;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyManagerView;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.DACDeployCheckDependenciesDialog;
import com.bluejungle.destiny.policymanager.ui.dialogs.DeployCheckDependenciesDialog;
import com.bluejungle.destiny.policymanager.ui.dialogs.DeployWizard;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.policy.Policy;

/**
 * @author bmeng
 * @version $Id$
 */

public class PendingTab extends AbstractTab {
	private ToolItem depolyItem;
	private List<DomainObjectDescriptor> activeDeployComponentList = new ArrayList<DomainObjectDescriptor>();

	public PendingTab(CTabFolder folder, PolicyManagerView view) {
		super(folder, view);
	}

	@Override
    protected void addColumns(List<TabColumn> columns) {
        super.addColumns(columns);
        
        int i = columns.indexOf(TabColumn.OWNED_BY_COLUMN);
        if (i == -1) {
            //should not happen but we can continue without lost the functionality
            
            LoggingUtil.logWarning(Activator.ID,
                    "Can't find owned by column, the new columns will added to the end", null);
            
            columns.add(TabColumn.MODIFIED_BY_COLUMN);
            columns.add(TabColumn.SUBMITTED_BY_COLUMN);
        }else {
            columns.add(i,    TabColumn.MODIFIED_BY_COLUMN);
            columns.add(i +1, TabColumn.SUBMITTED_BY_COLUMN);
        }
    }

	@Override
	public String getTabTitle() {
		return ApplicationMessages.PENDINGTAB_PENDING;
	}

	@Override
	public Image getTabImage() {
		return ImageBundle.TAB_PENDING_IMG;
	}

	@Override
	public void createAdditonalComponentTreeToolItem(ToolBar toolBar) {
		depolyItem = new ToolItem(toolBar, SWT.NONE);
		depolyItem.setToolTipText(ApplicationMessages.PENDINGTAB_DEPLOY);
		depolyItem.setEnabled(false);
		depolyItem.setImage(ImageBundle.SUBMIT_IMG);
		depolyItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				deployPolicy();
			}
		});
	}

	private void deployPolicy() {
		IStructuredSelection selection = (IStructuredSelection) getComponentTreeViewer().getSelection();
		if (selection.isEmpty()) {
			return;
		}
		
		GlobalState gs = GlobalState.getInstance();
		IHasId domainObject = (IHasId) gs.getCurrentObject();
		Shell shell = Display.getCurrent().getActiveShell();
		DeployWizard wizard = new DeployWizard(activeDeployComponentList);
		if (!wizard.checkDepencies()) {
			if ((domainObject instanceof Policy)){
				if (PolicyHelpers.isDACPolicyType(domainObject)){
					DACDeployCheckDependenciesDialog DACdlg = new DACDeployCheckDependenciesDialog(
							shell, activeDeployComponentList);
					DACdlg.open();		
				}else{
					DeployCheckDependenciesDialog dlg = new DeployCheckDependenciesDialog(
							shell, activeDeployComponentList);
					dlg.open();
				}
			}else{
				DeployCheckDependenciesDialog dlg = new DeployCheckDependenciesDialog(
						shell, activeDeployComponentList);
				dlg.open();
			}
			return;
		}
		WizardDialog dialog = new WizardDialog(shell, wizard);
		if (dialog.open() != IDialogConstants.OK_ID) {
			return;
		}

		PolicyManagerView.refreshCurrentTab();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateAdditionalComponentTreeToolBarStatus() {
		depolyItem.setEnabled(false);

		IStructuredSelection selection = (IStructuredSelection) getComponentTreeViewer()
				.getSelection();
		if (selection.isEmpty()) {
			return;
		}

		Iterator<DODDigest> iterator = selection.iterator();
		activeDeployComponentList.clear();
		while (iterator.hasNext()) {
			DODDigest digest = (DODDigest) iterator.next();
			DomainObjectDescriptor descriptor;
			try {
				descriptor = PolicyServerProxy
						.getDescriptorById(digest.getId());
			} catch (PolicyEditorException e) {
				LoggingUtil.logError(Activator.ID, "error", e);
				return;
			}
			IHasId hasId = (IHasId) PolicyServerProxy
					.getEntityForDescriptor(descriptor);
			if (PolicyServerProxy.canPerformAction(hasId, DAction.DEPLOY)) {
				DevelopmentStatus status = DomainObjectHelper.getStatus(hasId);
				if (status == DevelopmentStatus.APPROVED) {
					activeDeployComponentList.add(descriptor);
				} else if (status == DevelopmentStatus.OBSOLETE) {
					try {
						DomainObjectUsage entityUsage = PolicyServerProxy.getUsage(descriptor);
						Long currentlyDeployedVersion = entityUsage.getCurrentlydeployedvcersion();
						if (currentlyDeployedVersion != null) {
							activeDeployComponentList.add(descriptor);
						}
					} catch (PolicyEditorException exception) {
						LoggingUtil.logWarning(Activator.ID,
								"Failed to get the currently deployed version.  ScheduleDeploymentAciton menu will be disabled.",
								exception);
					}
				}
			}
		}
		if (activeDeployComponentList.size() == getComponentTreeViewer()
				.getTree().getSelectionCount()) {
			depolyItem.setEnabled(true);
		}
	}

	@Override
	public boolean hasCorrectStatus(DODDigest info) {
		String result = EntityInformation.getStatus(info);
		if (result.indexOf("Submitted for Deployment") != -1
				|| result.indexOf("Pending Deactivation (An earlier version is deployed)") != -1
				|| result.indexOf("Pending Deployment") != -1) {
			return true;
		}
		return false;
	}
}
