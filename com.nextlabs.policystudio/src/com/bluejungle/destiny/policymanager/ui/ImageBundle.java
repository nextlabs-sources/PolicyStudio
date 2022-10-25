/*
 * Created on May 11, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.util.ResourceManager;

/**
 * This class provides static references to images that are used across the app.
 * We should not add images that are not used extensively because images take OS
 * resources and should not be created unless needed.
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates
 *          .xml#2 $:
 */

public class ImageBundle {

	public static final Image FOLDER_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/folder.gif");
	public static final Image FOLDER_DISABLED_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/folder_disabled.gif");
	public static final Image FOLDER_OPEN_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/folderopen.gif");

	public static final Image CREATE_FOLDER_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/create_folder.gif");
	public static final Image DELETE_FOLDER_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/delete_folder.gif");

	public static final Image POLICY_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/policy.gif");
	public static final Image POLICYSTUDIO_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/policystudio.gif");

	public static final Image USER_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/user.gif");
	public static final Image CONTACT_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/contact.gif");
	public static final Image FILE_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/file.gif");
	public static final Image APPLICATION_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/application.gif");
	public static final Image DESKTOP_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/desktop.gif");

	public static final Image APP_USER_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/user.gif");
	public static final Image APP_USER_GROUP_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/userGroup.gif");

	public static final Image IMPORTED_USER_GROUP_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/userGroup.gif");
	public static final Image IMPORTED_HOST_GROUP_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/desktopGroup.gif");

	public static final Image ABOUT_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/about.png");
	public static final Image TITLE_IMAGE = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/title.gif");

	public static final Image LIST_PANEL_ITEM_CLOSED_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/view_menu_rt.gif");
	public static final Image LIST_PANEL_ITEM_OPENED_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/view_menu.gif");

	public static Image[] ANIMATED_BUSY_IMAGE;

	public static final Image STATIC_BUSY_IMAGE = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/non_animated_gears.gif");

	public static final Image CHECKED_IMAGE = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/checked.gif");
	public static final Image UNCHECKED_IMAGE = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/unchecked.gif");

	public static final Image PROPERTIES_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/properties.gif");
	public static final Image PROPERTIES_DISABLED_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/properties_disabled.gif");
	public static final Image DELETE_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/delete.gif");
	public static final Image DELETE_DISABLED_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/delete_disabled.gif");
	public static final Image PRINT_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/print.gif");
	public static final Image PRINT_DISABLED_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/print_disabled.gif");
	public static final Image SUBMIT_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/submit.gif");
	public static final Image DEACTIVATE_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/deactivate.gif");
	public static final Image REFRESH_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/refresh.gif");

	public static final Image TAB_DRAFT_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/tab_draft.gif");
	public static final Image TAB_PENDING_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/tab_pending.gif");
	public static final Image TAB_DEPLOYED_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/tab_deployed.gif");
	public static final Image TAB_DEACTIVATED_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/tab_deactivated.gif");
	public static final Image TAB_ALL_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/tab_all.gif");

	public static final Image DUPLICATE_IMG = ResourceManager.getPluginImage(
			Activator.getDefault(), "/resources/images/duplicate.gif");

	public static final Image DUPLICATE_DISABLED_IMG = ResourceManager
			.getPluginImage(Activator.getDefault(),
					"/resources/images/duplicate_disabled.gif");

        public static final Image XACML_EXPORT = ResourceManager
                        .getPluginImage(Activator.getDefault(),
                                        "/resources/images/xacml_export.gif");

        public static final Image XACML_EXPORT_DISABLED = ResourceManager
                        .getPluginImage(Activator.getDefault(),
                                        "/resources/images/xacml_export_disabled.gif");
        public static final Image POLICY_LOGO = ResourceManager.getPluginImage(
        		Activator.getDefault(), "/resources/images/p.gif");
            
	static {
		String key = "/resources/images/animated_gears.gif";
		URL url = Activator.getDefault().getBundle().getEntry(key);
		try {
			InputStream imageInputStream = new BufferedInputStream(url
					.openStream());
			ImageLoader animatedImageLoader = new ImageLoader();
			animatedImageLoader.load(imageInputStream);
			ANIMATED_BUSY_IMAGE = new Image[animatedImageLoader.data.length];
			for (int i = 0; i < animatedImageLoader.data.length; i++) {
				ANIMATED_BUSY_IMAGE[i] = ResourceManager.getImage(key + i,
						animatedImageLoader.data[i]);
			}
		} catch (IOException exception) {
			// Not much we can do here
			ANIMATED_BUSY_IMAGE = new Image[] { STATIC_BUSY_IMAGE };
		}
	}

	/**
	 * Constructor
	 * 
	 */
	public ImageBundle() {
		super();
	}
}
