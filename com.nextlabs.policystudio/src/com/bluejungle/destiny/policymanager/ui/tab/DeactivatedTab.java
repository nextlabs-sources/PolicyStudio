/*
 * Created on Aug 21, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.tab;

import java.util.List;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Image;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.model.EntityInformation;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyManagerView;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.pf.destiny.lib.DODDigest;

/**
 * @author bmeng
 * @version $Id$
 */

public class DeactivatedTab extends AbstractTab {
	public DeactivatedTab(CTabFolder folder, PolicyManagerView view) {
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
		return ApplicationMessages.DEACTIVATEDTAB_DEACTIVATED;
	}

	@Override
	public Image getTabImage() {
		return ImageBundle.TAB_DEACTIVATED_IMG;
	}

	@Override
	public boolean hasCorrectStatus(DODDigest info) {
		String result = EntityInformation.getStatus(info);
		if (result.indexOf("Inactive") != -1) {
			return true;
		}
		return false;
	}
}
