/*
 * Created on Aug 21, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.io.IOException;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.ConfigurableMessages;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.version.IVersion;
import com.bluejungle.versionexception.InvalidVersionException;
import com.bluejungle.versionfactory.VersionFactory;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main
 *          /com/bluejungle/destiny/policymanager/ui/dialogs/AboutDialog.java#1
 *          $
 */

public class AboutDialog extends Window {

	private MouseAdapter mouseAdapter = new MouseAdapter() {

		@Override
		public void mouseUp(MouseEvent e) {
			close();
		}
	};

	public AboutDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.TITLE | SWT.CLOSE);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(ApplicationMessages.ABOUTPART_ABOUT);
		newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		root.setBackground(ColorBundle.WHITE);
		root.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 400;
		root.setLayoutData(data);

		Label applicationImage = new Label(root, SWT.LEFT);
		applicationImage.setImage(ImageBundle.ABOUT_IMG);
		applicationImage.setBackground(ColorBundle.CE_BLUE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		applicationImage.setLayoutData(data);

		Composite bottom = new Composite(root, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		bottom.setBackground(ColorBundle.WHITE);
		bottom.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 400;
		bottom.setLayoutData(data);

		Composite applicationNameAndVersionComposite = new Composite(bottom,
				SWT.NONE);
		applicationNameAndVersionComposite.setBackground(ColorBundle.WHITE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		applicationNameAndVersionComposite.setLayoutData(data);
		GridLayout applicationNameAndVersionLayout = new GridLayout();
		applicationNameAndVersionLayout.marginWidth = 0;
		applicationNameAndVersionLayout.marginHeight = 0;
		applicationNameAndVersionLayout.numColumns = 2;
		applicationNameAndVersionComposite
				.setLayout(applicationNameAndVersionLayout);

		Label applicationNameLabel = new Label(
				applicationNameAndVersionComposite, SWT.LEFT | SWT.WRAP);
		applicationNameLabel
				.setText(ConfigurableMessages.ABOUTPART_APPLICATION_NAME);
		applicationNameLabel.setFont(FontBundle.ARIAL_9_NORMAL);
		applicationNameLabel.setForeground(ColorBundle.CE_BLUE);
		applicationNameLabel.setBackground(ColorBundle.WHITE);
		data = new GridData();
		applicationNameLabel.setLayoutData(data);

		Label versionLabel = new Label(applicationNameAndVersionComposite,
				SWT.LEFT | SWT.WRAP);
		versionLabel.setFont(FontBundle.ARIAL_9_NORMAL);
		versionLabel.setForeground(ColorBundle.CE_BLUE);
		versionLabel.setBackground(ColorBundle.WHITE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		versionLabel.setLayoutData(data);
		VersionFactory versionFactory = new VersionFactory();
		try {
			IVersion version = versionFactory.getVersion();
			StringBuffer versionString = new StringBuffer();
			versionString.append(version.getMajor());
			versionString.append(".");
			versionString.append(version.getMinor());
			versionString.append(".");
			versionString.append(version.getMaintenance());
			versionString.append(" (");
			versionString.append(version.getBuild());
			versionString.append(")");
			versionLabel.setText(versionString.toString());
		} catch (InvalidVersionException exception) {
			// Shouldn't happen
			LoggingUtil.logWarning(Activator.ID,
					"Failed to find Policy Author version.", exception);
			versionLabel.setText(String
					.valueOf(ApplicationMessages.ABOUTPART_UNKNOWN_VERSION));
		} catch (IOException exception) {
			LoggingUtil.logWarning(Activator.ID,
					"Failed to find Policy Author version.", exception);
			versionLabel.setText(String
					.valueOf(ApplicationMessages.ABOUTPART_UNKNOWN_VERSION));
		}

		new Label(root, SWT.NONE);

		Label infoLabel = new Label(bottom, SWT.WRAP);
		infoLabel.setFont(FontBundle.DEFAULT_NORMAL);
		infoLabel.setForeground(ColorBundle.CE_DK_BLUE);
		infoLabel.setBackground(ColorBundle.WHITE);
		infoLabel.setText(ApplicationMessages.ABOUTPART_LOGGED_IN
				+ GlobalState.server + ApplicationMessages.ABOUTPART_AS
				+ GlobalState.user);
		data = new GridData(GridData.FILL_HORIZONTAL);
		infoLabel.setLayoutData(data);

		Composite space = new Composite(bottom, SWT.NONE);
		space.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
		data = new GridData(GridData.FILL_BOTH);
		space.setLayoutData(data);
		
		Label copyrightLabel2 = new Label(bottom, SWT.LEFT | SWT.WRAP);
		copyrightLabel2.setText(ConfigurableMessages.ABOUTPART_COPYRIGHT_line2);
		copyrightLabel2.setFont(FontBundle.ARIAL_9_NORMAL);
		copyrightLabel2.setBackground(ColorBundle.WHITE);
		data = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL);
		data.widthHint = 582;
		copyrightLabel2.setLayoutData(data);
		
		Label copyrightLabel3 = new Label(bottom, SWT.LEFT | SWT.WRAP);
		copyrightLabel3.setText(ConfigurableMessages.ABOUTPART_COPYRIGHT_line3);
		copyrightLabel3.setFont(FontBundle.ARIAL_9_NORMAL);
		copyrightLabel3.setBackground(ColorBundle.WHITE);
		data = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL);
		data.widthHint = 582;
		copyrightLabel3.setLayoutData(data);
		
		Label copyrightLabel = new Label(bottom, SWT.LEFT | SWT.WRAP);
		copyrightLabel.setText(ConfigurableMessages.ABOUTPART_COPYRIGHT);
		copyrightLabel.setFont(FontBundle.ARIAL_9_NORMAL);
		copyrightLabel.setBackground(ColorBundle.WHITE);
		copyrightLabel.setForeground(ColorBundle.CE_BLUE);
		data = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL);
		data.widthHint = 582;
		copyrightLabel.setLayoutData(data);

		// add mouse up listener to close the window
		addControlListner(root);

		return root;
	}

	private void addControlListner(Control control) {
		control.addMouseListener(mouseAdapter);
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			for (Control child : composite.getChildren()) {
				addControlListner(child);
			}
		}
	}
}
