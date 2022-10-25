package com.bluejungle.destiny.policymanager.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchImages;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.model.EditorElementHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyManagerView;
import com.bluejungle.destiny.policymanager.ui.dialogs.ImportDialog;
import com.nextlabs.pf.destiny.importexport.ConflictResolution;
import com.nextlabs.pf.destiny.importexport.ExportEntity;
import com.nextlabs.pf.destiny.importexport.IImportConflict;
import com.nextlabs.pf.destiny.importexport.IImportState;
import com.nextlabs.pf.destiny.importexport.IImportState.Shallow;
import com.nextlabs.pf.destiny.importexport.ImportException;
import com.nextlabs.pf.destiny.importexport.ConflictResolution.ConflictType;
import com.nextlabs.pf.destiny.importexport.impl.Importer;

@SuppressWarnings("restriction")
public class ImportAction extends Action {
	public ImportAction() {
		setEnabled(false);
	}

	@Override
	public String getText() {
		return ActionMessages.ACTION_IMPORT;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_IMPORT_WIZ);
	}

	@Override
	public void run() {
		final Shell shell = Display.getCurrent().getActiveShell();
		ImportDialog dialog = new ImportDialog(shell);
		if (dialog.open() != Window.OK) {
			return;
		}

		final File file = new File(dialog.getPath());
		final Shallow isShallow = dialog.isShallowImport() ? Shallow.SHALLOW
				: Shallow.FULL;
		final String suffix = dialog.getSuffix();
		final int selection = dialog.getSelection();
		final IRunnableWithProgress op = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) {
				try {
					monitor.setTaskName(ActionMessages.ACTION_IMPORT);
					monitor.beginTask(ActionMessages.IMPORTACTION_MSG1, 100);
					Importer importer = new Importer(file, isShallow);
					monitor.worked(10);
					monitor.setTaskName(ActionMessages.IMPORTACTION_MSG2);
					Collection<ExportEntity> fileContents = importer
							.getEntities();
					IImportState importState = importer.doImport(fileContents);
					Collection<IImportConflict> conflicts = importState
							.getConflicts();
					if (!conflicts.isEmpty()) {
						if (selection == 0) {
							// overwrite
							closeAllActiveEditors();
							for (IImportConflict conflict : conflicts) {
								conflict
										.setResolution(ConflictResolution.KEEP_NEW);
							}
						} else if (selection == 1) {
                                                        buildResolutions(importState.getConflicts(), ConflictResolution.RENAME_NEW, suffix);
						} else if (selection == 2) {
							// keep
							for (IImportConflict conflict : conflicts) {
								conflict
										.setResolution(ConflictResolution.KEEP_OLD);
							}

						} else if (selection == 3) {
							// cancel
							MessageDialog.openInformation(shell,
									ActionMessages.IMPORTACTION_DATA_CONFLICT,
									ActionMessages.IMPORTACTION_CANCELLED);
							return;
						}
					}
					monitor.worked(30);
					monitor.setTaskName(ActionMessages.IMPORTACTION_MSG3);
					importer.commitImport();
					monitor.worked(50);
				} catch (Exception e) {
					PolicyEditorException exception = (PolicyEditorException)e.getCause();
					if(exception!=null){
			        	PolicyHelpers.timeOutCheck(exception);
					}
					e.printStackTrace();
					MessageDialog.openError(shell,
							ActionMessages.IMPORTACTION_ERROR,
							ActionMessages.IMPORTACTION_ERROR_OCCURS);
					return;
				}
				monitor.setTaskName(ActionMessages.IMPORTACTION_MSG4);
				EntityInfoProvider.updatePolicyTree();
				for (String component : EditorElementHelper
						.getComponentContexts()) {
					EntityInfoProvider.updateComponentList(component);
				}
				PolicyManagerView.refreshCurrentTab();
				MessageDialog.openInformation(shell,
						ActionMessages.IMPORTACTION_SUCCESS,
						ActionMessages.IMPORTACTION_IMPORT_SUCCESSFUL);
				monitor.worked(100);
			}
		};
		try {
			new ProgressMonitorDialog(shell).run(false, false, op);
		} catch (Exception e) {
			MessageDialog.openError(shell, ActionMessages.IMPORTACTION_ERROR,
					ActionMessages.IMPORTACTION_ERROR_OCCURS);
		}
	}
	public void closeAllActiveEditors(){
		IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();                
		activePage.closeEditors( activePage.getEditorReferences(), true);
	}

    /**
     * Build resolutions (i.e. name conflict solutions) for the entities. A conflict solution might be "overwrite" or "drop the entity"
     * or "give it a new name".
     *
     * This last one, for most entities, involves putting a renameSuffix at the end of the entity name. Entity becomes Entity1. For policy
     * exceptions, however, this is more complicated, because their parent policy might also be renamed. So Parent/Exception might become
     * Parent1/Exception1. Assuming the Parent was renamed to Parent1 and not something else.
     *
     * To fix this, we resolve the conflicts in increasing order of path depth. When resolving a path, we'll add the suffix as usual, but
     * we'll also check to see if the prefix has been resolved previously and, if so, rename it.
     */
    private static void buildResolutions(Collection<IImportConflict> conflicts, ConflictResolution conflictResolution, String renameSuffix) throws ImportException {
        List<IImportConflict> sortedConflicts = new ArrayList<IImportConflict>(conflicts);
        Collections.sort(sortedConflicts,
                         new ConflictsComparator());

        for (IImportConflict conflict : sortedConflicts) {
            if(conflictResolution == ConflictResolution.RENAME_NEW) {
                ConflictResolution renameSolution =
                    new ConflictResolution(ConflictResolution.ConflictType.RENAME_NEW,
                                           renameConflictingEntity(conflict, sortedConflicts, renameSuffix));
                conflict.setResolution(renameSolution);
            }else{
                conflict.setResolution(conflictResolution);
            }
        }
    }

    private static String renameConflictingEntity(IImportConflict conflict, List<IImportConflict> conflicts, String renameSuffix) {
        String newName = conflict.getName();

        // Look for the longest handled conflict whose name matches the first part of our name
        IImportConflict longestMatch = null;

        for (IImportConflict candidate : conflicts) {
            if (candidate.isResolved() && newName.startsWith(candidate.getName() + "/")) {
                longestMatch = candidate;
            }
        }

        if (longestMatch != null) {
            String replacementString = longestMatch.getResolution().getNewName();

            // Replace the prefix with the new name...
            newName = longestMatch.getResolution().getNewName() + newName.substring(longestMatch.getName().length());
        }

        // And add our own suffix
        return newName + renameSuffix;
    }

    private static class ConflictsComparator<T extends IImportConflict> implements Comparator<T> {
        public int compare(T c1, T c2) {
            int d1 = depth(c1);
            int d2 = depth(c2);

            if (d1 == d2) {
                return c1.getName().compareTo(c2.getName());
            }

            return d1-d2;
        }

        private int depth(T c) {
            return countOccurences(c.getName(), '/');
        }
        
        private static int countOccurences(String haystack, char needle) {
            int count = 0;

            if (haystack != null) {
                for (int i = 0; i < haystack.length(); i++) {
                    if (haystack.charAt(i) == needle) {
                        count++;
                    }
                }
            }

            return count;
        }

	}
}
