package com.nextlabs.policystudio.preview.resource;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.bluejungle.destiny.policymanager.action.PolicyStudioActionFactory;
import com.bluejungle.destiny.policymanager.editor.EditorMessages;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PreviewPanel;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.utils.NetworkUtils;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.destiny.services.ResourcePreview;
import com.bluejungle.pf.engine.destiny.DefaultFileResourceHandler;

public class ResourcePreviewPanel extends PreviewPanel {

	private Thread previewThread = null;

	private boolean isCancelled = false;

	private Object threadSynchronizer = new Object();

	private static PreviewItem PREVIEW_IN_PROGRESS = previewText("...");

	private static PreviewItem PREVIEW_NEEDS_ROOT = previewText("Cannot preview resources without a starting location.");

	private static PreviewItem PREVIEW_DONE = previewText("Preview is complete.");

	private static PreviewItem PREVIEW_CANCELLED = previewText("Preview is cancelled.");

	public ResourcePreviewPanel() {
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.editor.ComponentEditorPanel#preview()
	 */
	@Override
	protected void preview() {
		GlobalState.getInstance().saveEditorPanel();
		Thread newThread = new Thread() {
			@Override
			public void run() {
				synchronized (threadSynchronizer) {
					// kill previous preview thread if it is still running.
					if (previewThread != null && previewThread.isAlive()) {
						isCancelled = true;
						try {
							previewThread.join();
						} catch (InterruptedException e) {
						}
					}
					previewThread = this;
					previewResults.clear();
					addPreviewResult(PREVIEW_IN_PROGRESS);
					isCancelled = false;
				}

				try {
					DODDigest descriptor = null;
					if (PolicyStudioActionFactory.SWITCH_TO_POLICY_AUTHOR_ACTION
							.isEnabled()) {
						descriptor = EntityInfoProvider
								.getComponentDescriptor(DomainObjectHelper
										.getName(hasId));
					} else {
						descriptor = EntityInfoProvider
								.getComponentDescriptor(DomainObjectHelper
										.getName((IHasId) GlobalState
												.getInstance()
												.getCurrentObject()));
					}
					DomainObjectDescriptor des = null;
					try {
						des = PolicyServerProxy.getDescriptorById(descriptor
								.getId());
					} catch (PolicyEditorException e) {
						e.printStackTrace();
						return;
					}
					ResourcePreview resourcePreview = PolicyServerProxy
							.getResourcePreview(des);
					if (resourcePreview != null) {
						if (resourcePreview.tryAllLocalRoots()) {
							// try all local roots here
							File[] roots = File.listRoots();
							for (int i = 0; !isCancelled && i < roots.length; i++) {
								enumerateFiles(roots[i], resourcePreview);
							}
						}

						if (resourcePreview.tryAllNetworkRoots()) {
							// TODO: try all network roots here
						}

						if (!resourcePreview.tryAllLocalRoots()
								|| !resourcePreview.tryAllNetworkRoots()) {
							Collection<String> rootsToTry = resourcePreview
									.getRoots();
							if (rootsToTry.isEmpty()) {
								addPreviewResult(PREVIEW_NEEDS_ROOT);
							}
							Iterator<String> iterator = rootsToTry.iterator();
							while (!isCancelled && iterator.hasNext()) {
								String root = (String) iterator.next();
								File rootFile = new File(
										DefaultFileResourceHandler
												.getNativeName(root));
								if (rootFile.isDirectory()) {
									enumerateFiles(rootFile, resourcePreview);
								} else {
									// this may be a server. get shares on
									// the server
									String[] rootList = NetworkUtils
											.getSharedFolderList(root);
									for (int i = 0; !isCancelled
											&& i < rootList.length; i++) {
										rootFile = new File(root, rootList[i]);
										enumerateFiles(rootFile,
												resourcePreview);
									}
								}
							}
						}
					}
				} finally {
					addPreviewResult(isCancelled ? PREVIEW_CANCELLED
							: PREVIEW_DONE);
				}
			}
		};
		newThread.start();
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.editor.ComponentEditorPanel#cancelPreview()
	 */
	@Override
	protected void cancelPreview() {

		if (this.previewThread != null && this.previewThread.isAlive()) {
			this.isCancelled = true;
		} else {
			super.cancelPreview();
		}
	}

	/**
	 * enumerates all files in the specified folder recursively and adds
	 * matching files to preview list.
	 * 
	 * @param rootFile
	 *            folder to enumerate
	 */
	private void enumerateFiles(File rootFile,
			final ResourcePreview resourcePreview) {
		File[] fileList = rootFile.listFiles((FileFilter) resourcePreview);

		if (fileList == null) {
			return;
		}

		for (File file : fileList) {
			if (isCancelled) {
				return;
			}
			if (resourcePreview.accept(file)) {
				addPreviewResult(makePreviewItem(file.getAbsolutePath()));
			}
		}

		// get directories
		File[] dirs = rootFile.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return resourcePreview.isProbableRoot(pathname);
			}
		});

		for (File dir : dirs) {
			if (isCancelled) {
				return;
			}
			enumerateFiles(dir, resourcePreview);
		}
	}

	private void addPreviewResult(PreviewItem item) {
		if (previewResults != null && previewResults.size() == 1
				&& previewResults.get(0) == PREVIEW_IN_PROGRESS) {
			previewResults.clear();
		}
		previewResults.add(item);
		// If we're adding a row that says the preview is cancelled, force
		// the refresh
		refreshPreviewTableInUIThread(item == PREVIEW_CANCELLED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.bluejungle.destiny.policymanager.editor.ComponentEditor#
	 * setupPreviewTable(org.eclipse.swt.widgets.Table)
	 */
	@Override
	protected void setupPreviewTable(Table table) {
		TableColumn c1 = new TableColumn(table, SWT.LEFT);
		c1.setWidth(150);
		c1.setText(EditorMessages.RESOURCECOMPONENTEDITOR_FILE_NAME);

		TableColumn c2 = new TableColumn(table, SWT.LEFT);
		c2.setWidth(150);
		c2.setText(EditorMessages.RESOURCECOMPONENTEDITOR_PATH);

		tableViewer.setContentProvider(new ViewContentProvider());
		tableViewer.setLabelProvider(new ViewLabelProvider());
		tableViewer.setInput(this.previewResults);

	}

	@SuppressWarnings("deprecation")
	@Override
	protected EntityType getEntityType() {
		return EntityType.RESOURCE;
	}

	/**
	 * Refresh the preview table. Makes the call in the UI thread
	 */
	public void refreshPreviewTableInUIThread(boolean oneLastTime) {
		if (this.isCancelled && !oneLastTime) {
			return;
		}
		// parentEditor.getDisplay().syncExec(new Runnable() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				refreshPreviewTable(null);
			}
		});
	}

	protected void cleanup() {
		this.cancelPreview();
	}

	/**
	 * @see com.bluejungle.destiny.policymanager.ui.PreviewPanel#makePreviewItem(java.lang.Object)
	 */
	@Override
	protected PreviewItem makePreviewItem(Object data) {
		final String fileName = (String) data;
		return new PreviewItem() {

			public String getText(int index) {
				int pos = fileName.lastIndexOf(File.separatorChar);
				if (index == 0) {
					return fileName.substring(pos + 1);
				} else if (index == 1) {
					return (pos != -1) ? fileName.substring(0, pos) : "";
				}
				return null;
			}

			public Image getImage(int index) {
				return index == 0 ? ImageBundle.FILE_IMG : null;
			}
		};
	}
}
