package com.bluejungle.destiny.policymanager.ui.tab;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import com.bluejungle.destiny.policymanager.model.EntityInformation;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.pf.destiny.lib.DODDigest;

abstract class TabColumn {

    String title;
    int aligments;
    int widths;
    
    TabColumn(String title, int aligments, int widths) {
        this.title = title;
        this.aligments = aligments;
        this.widths = widths;
    }

    abstract int compare(DODDigest d1, DODDigest d2);
    
    abstract String getColumnText(DODDigest digest);
    
    abstract Image getColumnImage(Object element);
    
    
    
    static final TabColumn DISPLAY_NAME_COLUMN = new TabColumn(ApplicationMessages.ABSTRACTTAB_OBJECT, SWT.LEFT, 100) {

        @Override
        int compare(DODDigest d1, DODDigest d2) {
            return EntityInformation.getDisplayName(d2).compareToIgnoreCase(EntityInformation.getDisplayName(d1));
        }

        @Override
        String getColumnText(DODDigest digest) {
            return EntityInformation.getDisplayName(digest);
        }

        @Override
        Image getColumnImage(Object element) {
            return ObjectLabelImageProvider.getImage(element);
        }
        
    };
    
    static final TabColumn VERSION_COLUMN = new TabColumn(
            ApplicationMessages.ABSTRACTTAB_VERSION, SWT.RIGHT, 100){

        @Override
        int compare(DODDigest d1, DODDigest d2) {
            int v1 = d1.getDestinyVersion();
            int v2 = d2.getDestinyVersion();
            return v2 - v1;
        }

        @Override
        String getColumnText(DODDigest digest) {
            return Integer.toString(digest.getDestinyVersion());
        }

        @Override
        Image getColumnImage(Object element) {
            return null;
        }
        
    };
    
    static final TabColumn SUBMITTED_TIME_COLUMN = new TabColumn(
            ApplicationMessages.ABSTRACTTAB_SUBMITTED_TIME, SWT.LEFT, 100){

        private final Format formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm aaa");
        
        @Override
        int compare(DODDigest d1, DODDigest d2) {
            Date date1 = d1.getLastModified();
            Date date2 = d2.getLastModified();
            if (date1 == null || date2 == null) {
                return 0;
            }
            return date2.compareTo(date1);
        }

        @Override
        String getColumnText(DODDigest digest) {
            Date date = digest.getLastModified();
            String time = date != null ? formatter.format(date) : "";
            return time;
        }
        
        @Override
        Image getColumnImage(Object element) {
            return null;
        }
        
    };
    
    static final TabColumn OWNED_BY_COLUMN = new TabColumn(
            ApplicationMessages.ABSTRACTTAB_OWNED_BY, SWT.LEFT, 100){

        @Override
        int compare(DODDigest d1, DODDigest d2) {
            String owner1 = d1.getOwnerName();
            String owner2 = d2.getOwnerName();
            return owner2.compareToIgnoreCase(owner1);
        }

        @Override
        String getColumnText(DODDigest digest) {
            return EntityInformation.getActivatedBy(digest);
        }
        
        @Override
        Image getColumnImage(Object element) {
            return null;
        }
        
    };
    
    static final TabColumn STATUS_COLUMN = new TabColumn(
            ApplicationMessages.ABSTRACTTAB_STATUS, SWT.LEFT, 200){

        @Override
        int compare(DODDigest d1, DODDigest d2) {
            String status1 = EntityInformation.getStatus(d1);
            String status2 = EntityInformation.getStatus(d2);
            return status2.compareToIgnoreCase(status1);
        }

        @Override
        String getColumnText(DODDigest digest) {
            return EntityInformation.getStatus(digest);
        }
        
        @Override
        Image getColumnImage(Object element) {
            return null;
        }
        
    };
    
    public static int compare(String s1, String s2){
        if (s1 == null) {
            if (s2 == null) {
                return 0;
            } else {
                return 1;
            }
        } else if (s1 == null) {
            return -1;
        }
        return s1.compareToIgnoreCase(s2);
    }
    
    static final TabColumn MODIFIED_BY_COLUMN = new TabColumn(
            ApplicationMessages.ABSTRACTTAB_COLUMN_MODIFY_BY, SWT.LEFT, 100){

        @Override
        int compare(DODDigest d1, DODDigest d2) {
            String modifier1 = d1.getModifierName();
            String modifier2 = d2.getModifierName();
            return compare(modifier2, modifier1);
        }

        @Override
        String getColumnText(DODDigest digest) {
            String name = digest.getModifierName(); 
            return name != null ? name : "";
        }
        
        @Override
        Image getColumnImage(Object element) {
            return null;
        }
        
    };
    
    static final TabColumn SUBMITTED_BY_COLUMN = new TabColumn(
            ApplicationMessages.ABSTRACTTAB_COLUMN_SUBMIT_BY, SWT.LEFT, 100){

        @Override
        int compare(DODDigest d1, DODDigest d2) {
            String submitter1 = d1.getSubmitterName();
            String submitter2 = d2.getSubmitterName();
            //TODO move this to shared location
            if (submitter1 == null) {
                if (submitter2 == null) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (submitter2 == null) {
                return -1;
            }
            return compare(submitter2, submitter1);
        }

        @Override
        String getColumnText(DODDigest digest) {
            String name = digest.getSubmitterName();
            return name != null ? name : "";
        }
        
        @Override
        Image getColumnImage(Object element) {
            return null;
        }
        
    };
    
    
}
