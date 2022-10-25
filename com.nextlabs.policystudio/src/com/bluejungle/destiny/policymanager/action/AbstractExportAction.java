/*
 * Created on Apr 04, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id$:
 */

package com.bluejungle.destiny.policymanager.action;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IProgressMonitor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.nextlabs.pf.destiny.importexport.IExporter;

public abstract class AbstractExportAction extends Action {
    private File file;
    private List<DomainObjectDescriptor> policyList;

    public void setPolicyList(List<DomainObjectDescriptor> policyList) {
        this.policyList = policyList;
    }

	
    public void setFile(File file) {
        this.file = file;
    }
    
    @Override
    public void run() {
        final Shell shell = Display.getCurrent().getActiveShell();
        final IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) {
                monitor.setTaskName(ActionMessages.ACTION_EXPORT);
                monitor.beginTask(ActionMessages.EXPORTACTION_MSG1, 100);
                if (policyList.size() == 0) {
                    MessageDialog.openWarning(shell,
                                              ActionMessages.EXPORTACTION_WARNING,
                                              ActionMessages.EXPORTACTION_NO_POLICIES);
                    return;
                }
                monitor.worked(30);
                monitor.setTaskName(ActionMessages.EXPORTACTION_MSG2);
                IExporter exporter = getExporter();
                monitor.worked(50);
                try {
                    monitor.setTaskName(ActionMessages.EXPORTACTION_MSG3);
                    exporter.prepareForExport(policyList);
                    monitor.worked(80);
                    if (file.exists()) {
                        file.delete();
                    }
                    exporter.executeExport(file);
                    exportedFileCompletely(monitor);
                } catch (Exception e) {
					PolicyEditorException exception = (PolicyEditorException)e.getCause();
					if(exception!=null){
			        	PolicyHelpers.timeOutCheck(exception);
					}
                    LoggingUtil.log(IStatus.ERROR, Activator.ID, IStatus.ERROR, e.getMessage(), e);
                    MessageDialog.openError(shell,
                                            ActionMessages.EXPORTACTION_ERROR,
                                            ActionMessages.EXPORTACTION_ERROR_OCCURS);
                    return;
                }
                MessageDialog.openInformation(shell,
                                              ActionMessages.EXPORTACTION_SUCCESS,
                                              ActionMessages.EXPORTACTION_EXPORT_SUCCESSFUL);
            }
        };

        try {
            new ProgressMonitorDialog(shell).run(false, false, op);
        } catch (Exception e) {
            MessageDialog.openError(shell, ActionMessages.EXPORTACTION_ERROR,
                                    ActionMessages.EXPORTACTION_ERROR_OCCURS);
        }
    }

    protected void exportedFileCompletely(IProgressMonitor monitor){
        monitor.worked(100);
    }

    protected abstract IExporter getExporter();

    public abstract String getDefaultExtension();
}
